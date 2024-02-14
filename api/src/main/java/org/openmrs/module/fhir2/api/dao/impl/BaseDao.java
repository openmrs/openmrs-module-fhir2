/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.fhir2.api.dao.impl;

import javax.annotation.Nonnull;
import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAmount;
import java.time.temporal.TemporalUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import ca.uhn.fhir.model.api.IQueryParameterAnd;
import ca.uhn.fhir.model.api.IQueryParameterOr;
import ca.uhn.fhir.model.api.IQueryParameterType;
import ca.uhn.fhir.rest.api.SortOrderEnum;
import ca.uhn.fhir.rest.api.SortSpec;
import ca.uhn.fhir.rest.param.DateParam;
import ca.uhn.fhir.rest.param.DateRangeParam;
import ca.uhn.fhir.rest.param.ParamPrefixEnum;
import ca.uhn.fhir.rest.param.QuantityAndListParam;
import ca.uhn.fhir.rest.param.QuantityParam;
import ca.uhn.fhir.rest.param.ReferenceAndListParam;
import ca.uhn.fhir.rest.param.StringAndListParam;
import ca.uhn.fhir.rest.param.StringParam;
import ca.uhn.fhir.rest.param.TokenAndListParam;
import ca.uhn.fhir.rest.param.TokenOrListParam;
import ca.uhn.fhir.rest.param.TokenParam;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Criterion;
import org.hl7.fhir.dstu3.model.Encounter;
import org.hl7.fhir.exceptions.FHIRException;
import org.hl7.fhir.r4.model.Location;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.Practitioner;
import org.hl7.fhir.r4.model.codesystems.AdministrativeGender;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.api.search.param.PropParam;
import org.openmrs.module.fhir2.api.util.LocalDateTimeFactory;
import org.openmrs.module.fhir2.model.FhirConceptSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

/**
 * <p>
 * A base class for OpenMRS FHIR2 Dao objects. It provides helpers to make generating complex
 * queries simpler.
 * </p>
 * <p>
 * For example, to create a query for people with the first name "Liam" and last name either
 * "Hemsworth" or "Neeson", the following code can be used: <pre>{@code
 *  StringAndListParam firstNames = new StringAndListParam().addAnd(new StringParam("Liam"));
 *  StringOrListParam lastNames = new StringOrListParam.addOr(new StringParam("Hemsworth), new StringParam("Neeson"));
 *  Criteria criteria = sessionFactory.getCurrentSession().createCriteria(Person.class);
 *  criteria.createAlias("names", "pn");
 *  Stream.of(
 *      handleAndParam(firstNames, name -> Optional.of(eq("pn.givenName", name))),
 *      handleOrParam(lastNames, name -> Optional.of(eq("pn.familyName", name))
 *  ).filter(Optional::isPresent).map(Optional::get).forEach(criteria::add);
 * }</pre>
 * </p>
 * <p>
 * This will generate a query that looks something like: <pre>{@code
 *  SELECT *
 *  FROM patient this_
 *      JOIN patient_name pn ON pn.person_id = this_.person_id
 *  WHERE pn.given_name = "Liam" AND (pn.family_name = "Hemsworth" or pn.family_name = "Neeson")
 * }</pre>
 * </p>
 * <p>
 * These methods can also be used to simplify the code to generate very complex queries. For
 * example, the following code allows grouping {@link TokenParam} representing
 * {@link org.hl7.fhir.r4.model.CodeableConcept}s into groups based on systems with correct AND / OR
 * logic: <pre>{@code
 *  Criteria criteria = sessionFactory.getCurrentSession().createCriteria(Obs.class);
 *  TokenAndListParam code = ...;
 *  handleAndListParamBySystem(code, (system, tokens) -> {
 *     if (system.isEmpty()) {
 *         return Optional.of(or(
 *             in("c.conceptId", tokensToParams(tokens).map(NumberUtils::toInt).collect(Collectors.toList())),
 * 	               in("c.uuid", tokensToList(tokens))));
 *     } else {
 *         if (!containsAlias(criteria, "cm")) {
 *             criteria.createAlias("c.conceptMappings", "cm").createAlias("cm.conceptReferenceTerm", "crt");
 *         }
 *         DetachedCriteria conceptSourceCriteria = DetachedCriteria.forClass(FhirConceptSource.class).add(eq("url", system))
 *             .setProjection(property("conceptSource"));
 * 	       if (codes.size() > 1) {
 *            return Optional.of(and(propertyEq("crt.conceptSource", conceptSourceCriteria), in("crt.code", codes)));
 *         } else {
 *             return Optional.of(and(propertyEq("crt.conceptSource", conceptSourceCriteria), eq("crt.code", codes.get(0))));
 *         };
 *     }
 *  }).ifPresent(criteria::add);
 * }</pre>
 * </p>
 * <p>
 * This can generate queries that look something like: <pre>{@code
 *   SELECT *
 *   FROM obs this_
 *       JOIN concept c ON this_.concept_id = c.concept_id
 *       JOIN concept_reference_map cm on c.concept_id = cm.concept_id
 *       JOIN concept_reference_term crt on cm.concept_reference_term_id = crt.concept_reference_term_id
 *   WHERE ((
 *         crt.concept_source_id = (select concept_source_id from fhir_concept_source where url = ?)
 *     AND crt.code in (?, ?, ?)
 *   ) OR (
 *         crt.concept_source_id = (select concept_source_id from fhir_concept_source where url = ?)
 *     AND crt.code = ?
 *   )) AND (
 *         crt.concept_source_id = (select concept_source_id from fhir_concept_source where url = ?)
 *     AND crt.code in (?, ?, ?)
 *   );
 * }</pre>
 * </p>
 */
public abstract class BaseDao {
	
	private static final BigDecimal APPROX_RANGE = new BigDecimal("0.1");
	
	@Autowired
	private LocalDateTimeFactory localDateTimeFactory;
	
	@Autowired
	@Getter(AccessLevel.PUBLIC)
	@Setter(AccessLevel.PUBLIC)
	@Qualifier("sessionFactory")
	protected SessionFactory sessionFactory;
	
	/**
	 * Converts an {@link Iterable} to a {@link Stream}
	 *
	 * @param iterable the iterable
	 * @param <T> any type
	 * @return a stream containing the same objects as the iterable
	 */
	@SuppressWarnings("unused")
	protected static <T> Stream<T> stream(Iterable<T> iterable) {
		return stream(iterable.iterator());
	}
	
	/**
	 * Converts an {@link Iterator} to a {@link Stream}
	 *
	 * @param iterator the iterator
	 * @param <T> any type
	 * @return a stream containing the same objects as the iterator
	 */
	protected static <T> Stream<T> stream(Iterator<T> iterator) {
		return StreamSupport.stream(Spliterators.spliteratorUnknownSize(iterator, Spliterator.ORDERED), false);
	}
	
	/**
	 * Converts an {@link Iterable} to a {@link Stream} operated on in parallel
	 *
	 * @param iterable the iterable
	 * @param <T> any type
	 * @return a stream containing the same objects as the iterable
	 */
	@SuppressWarnings("unused")
	protected static <T> Stream<T> parallelStream(Iterable<T> iterable) {
		return parallelStream(iterable.iterator());
	}
	
	/**
	 * Converts an {@link Iterator} to a {@link Stream} operated on in parallel
	 *
	 * @param iterator the iterator
	 * @param <T> any type
	 * @return a stream containing the same objects as the iterator
	 */
	protected static <T> Stream<T> parallelStream(Iterator<T> iterator) {
		return StreamSupport.stream(Spliterators.spliteratorUnknownSize(iterator, Spliterator.ORDERED), true);
	}
	
	/**
	 * A generic handler for any subtype of {@link IQueryParameterAnd} which creates a criterion that
	 * represents the intersection of all the parameters contained
	 *
	 * @param andListParam the {@link IQueryParameterAnd} to handle
	 * @param handler a {@link Function} which maps a parameter to a {@link Criterion}
	 * @param <T> the subtype of {@link IQueryParameterOr} that this {@link IQueryParameterAnd} contains
	 * @param <U> the subtype of {@link IQueryParameterType} for this parameter
	 * @return the resulting criterion, which is the intersection of all the unions of contained
	 *         parameters
	 */
	protected <T extends IQueryParameterOr<U>, U extends IQueryParameterType> Optional<Predicate> handleAndListParam(
	        CriteriaBuilder criteriaBuilder, IQueryParameterAnd<T> andListParam, Function<U, Optional<Predicate>> handler) {
		if (andListParam == null) {
			return Optional.empty();
		}
		
		return Optional.ofNullable(criteriaBuilder.and(toCriteriaArray(
		    handleAndListParam(andListParam).map(orListParam -> handleOrListParam(criteriaBuilder, orListParam, handler)))));
	}
	
	@SuppressWarnings("unused")
	protected <T extends IQueryParameterOr<U>, U extends IQueryParameterType> Optional<Predicate> handleAndListParamBy(
	        CriteriaBuilder criteriaBuilder, IQueryParameterAnd<T> andListParam,
	        Function<IQueryParameterOr<U>, Optional<Predicate>> handler) {
		if (andListParam == null) {
			return Optional.empty();
		}
		
		return Optional.of(criteriaBuilder.and((toCriteriaArray(handleAndListParam(andListParam).map(handler)))));
	}
	
	protected <T extends IQueryParameterOr<U>, U extends IQueryParameterType> Optional<Predicate> handleAndListParamAsStream(
	        CriteriaBuilder criteriaBuilder, IQueryParameterAnd<T> andListParam,
	        Function<U, Stream<Optional<Predicate>>> handler) {
		if (andListParam == null) {
			return Optional.empty();
		}
		
		return Optional.of(criteriaBuilder.and((toCriteriaArray(handleAndListParam(andListParam)
		        .map(orListParam -> handleOrListParamAsStream(criteriaBuilder, orListParam, handler))))));
	}
	
	/**
	 * A generic handler for any subtype of {@link IQueryParameterOr} which creates a criterion that
	 * represents the union of all the parameters
	 *
	 * @param orListParam the {@link IQueryParameterOr} to handle
	 * @param handler a {@link Function} which maps a parameter to a {@link Criterion}
	 * @param <T> the subtype of {@link IQueryParameterType} for this parameter
	 * @return the resulting criterion, which is the union of all contained parameters
	 */
	protected <T extends IQueryParameterType> Optional<Predicate> handleOrListParam(CriteriaBuilder criteriaBuilder,
	        IQueryParameterOr<T> orListParam, Function<T, Optional<Predicate>> handler) {
		if (orListParam == null) {
			return Optional.empty();
		}
		
		return Optional.of(criteriaBuilder.or(toCriteriaArray(handleOrListParam(orListParam).map(handler))));
	}
	
	protected <T extends IQueryParameterType> Optional<Predicate> handleOrListParamAsStream(CriteriaBuilder criteriaBuilder,
	        IQueryParameterOr<T> orListParam, Function<T, Stream<Optional<Predicate>>> handler) {
		if (orListParam == null) {
			return Optional.empty();
		}
		
		return Optional.of(criteriaBuilder.or(toCriteriaArray(handleOrListParam(orListParam).flatMap(handler))));
	}
	
	/**
	 * Handler for a {@link IQueryParameterAnd} of {@link TokenParam}s where tokens should be grouped
	 * and handled according to the system they belong to This is useful for queries drawing their
	 * values from CodeableConcepts
	 *
	 * @param andListParam the {@link IQueryParameterAnd} to handle
	 * @param systemTokenHandler a {@link BiFunction} taking the system and associated list of
	 *            {@link TokenParam}s and returning a {@link Criterion}
	 * @return a {@link Criterion} representing the intersection of all produced {@link Criterion}
	 */
	protected <T extends IQueryParameterOr<TokenParam>> Optional<Predicate> handleAndListParamBySystem(
	        CriteriaBuilder criteriaBuilder, IQueryParameterAnd<T> andListParam,
	        BiFunction<String, List<TokenParam>, Optional<Predicate>> systemTokenHandler) {
		if (andListParam == null) {
			return Optional.empty();
		}
		
		return Optional.of(criteriaBuilder.and(toCriteriaArray(handleAndListParam(andListParam)
		        .map(param -> handleOrListParamBySystem(criteriaBuilder, param, systemTokenHandler)))));
	}
	
	/**
	 * Handler for a {@link IQueryParameterOr} of {@link TokenParam}s where tokens should be grouped and
	 * handled according to the system they belong to This is useful for queries drawing their values
	 * from CodeableConcepts
	 *
	 * @param orListParam the {@link IQueryParameterOr} to handle
	 * @param systemTokenHandler a {@link BiFunction} taking the system and associated list of
	 *            {@link TokenParam}s and returning a {@link Criterion}
	 * @return a {@link Criterion} representing the union of all produced {@link Criterion}
	 */
	protected Optional<Predicate> handleOrListParamBySystem(CriteriaBuilder criteriaBuilder,
	        IQueryParameterOr<TokenParam> orListParam,
	        BiFunction<String, List<TokenParam>, Optional<Predicate>> systemTokenHandler) {
		
		if (orListParam == null) {
			return Optional.empty();
		}
		
		return Optional.of(criteriaBuilder
		        .or(toCriteriaArray(handleOrListParam(orListParam).collect(Collectors.groupingBy(this::groupBySystem))
		                .entrySet().stream().map(e -> systemTokenHandler.apply(e.getKey(), e.getValue())))));
	}
	
	/**
	 * Handler for a {@link TokenOrListParam} that represents boolean values
	 *
	 * @param propertyName the name of the property in the query to use
	 * @param booleanToken the {@link TokenOrListParam} to handle
	 * @return a {@link Criterion} to be added to the query indicating that the property matches the
	 *         given value
	 */
	protected <T, U> Optional<Predicate> handleBoolean(OpenmrsFhirCriteriaContext<T, U> criteriaContext, String propertyName,
	        TokenAndListParam booleanToken) {
		if (booleanToken == null) {
			return Optional.empty();
		}
		
		// note that we use a custom implementation here as Boolean.valueOf() and Boolean.parse() only determine whether
		// the string matches "true". We could potentially be passed a non-valid Boolean value here.
		return handleAndListParam(criteriaContext.getCriteriaBuilder(), booleanToken, token -> {
			if (token.getValue().equalsIgnoreCase("true")) {
				return handleBooleanProperty(criteriaContext, propertyName, true);
			} else if (token.getValue().equalsIgnoreCase("false")) {
				return handleBooleanProperty(criteriaContext, propertyName, false);
			}
			
			return Optional.empty();
		});
	}
	
	protected <T, U> Optional<Predicate> handleBooleanProperty(OpenmrsFhirCriteriaContext<T, U> criteriaContext,
	        String propertyName, boolean booleanVal) {
		return Optional
		        .of(criteriaContext.getCriteriaBuilder().equal(criteriaContext.getRoot().get(propertyName), booleanVal));
	}
	
	/**
	 * A handler for a {@link DateRangeParam}, which represents an inclusive set of {@link DateParam}s
	 *
	 * @param propertyName the name of the property in the query to use
	 * @param dateRangeParam the {@link DateRangeParam} to handle
	 * @return a {@link Criterion} to be added to the query for the indicated date range
	 */
	protected <T, U> Optional<Predicate> handleDateRange(OpenmrsFhirCriteriaContext<T, U> criteriaContext,
	        String propertyName, DateRangeParam dateRangeParam) {
		if (dateRangeParam == null) {
			return Optional.empty();
		}
		
		return Optional.ofNullable(criteriaContext.getCriteriaBuilder()
		        .and(toCriteriaArray(Stream.of(handleDate(criteriaContext, propertyName, dateRangeParam.getLowerBound()),
		            handleDate(criteriaContext, propertyName, dateRangeParam.getUpperBound())))));
	}
	
	/**
	 * A handler for a {@link DateParam}, which represents a day and an comparator
	 *
	 * @param propertyName the name of the property in the query to use
	 * @param dateParam the {@link DateParam} to handle
	 * @return a {@link Predicate} to be added to the query for the indicate date param
	 */
	protected <T, U> Optional<Predicate> handleDate(OpenmrsFhirCriteriaContext<T, U> criteriaContext, String propertyName,
	        DateParam dateParam) {
		if (dateParam == null) {
			return Optional.empty();
		}
		
		int calendarPrecision = dateParam.getPrecision().getCalendarConstant();
		if (calendarPrecision > Calendar.SECOND) {
			calendarPrecision = Calendar.SECOND;
		}
		// TODO We may want to not use the default Calendar
		Date dateStart = DateUtils.truncate(dateParam.getValue(), calendarPrecision);
		Date dateEnd = DateUtils.ceiling(dateParam.getValue(), calendarPrecision);
		
		// TODO This does not properly handle FHIR Periods and Timings, though its unclear if we are using those
		// see https://www.hl7.org/fhir/search.html#date
		switch (dateParam.getPrefix()) {
			case EQUAL:
				return Optional.of(criteriaContext.getCriteriaBuilder().and(
				    criteriaContext.getCriteriaBuilder().greaterThanOrEqualTo(criteriaContext.getRoot().get(propertyName),
				        dateStart),
				    criteriaContext.getCriteriaBuilder().lessThan(criteriaContext.getRoot().get(propertyName), dateEnd)));
			case NOT_EQUAL:
				return Optional.of(criteriaContext.getCriteriaBuilder().not(criteriaContext.getCriteriaBuilder().and(
				    criteriaContext.getCriteriaBuilder().greaterThanOrEqualTo(criteriaContext.getRoot().get(propertyName),
				        dateStart),
				    criteriaContext.getCriteriaBuilder().lessThan(criteriaContext.getRoot().get(propertyName), dateEnd))));
			case LESSTHAN_OR_EQUALS:
			case LESSTHAN:
				return Optional.of(criteriaContext.getCriteriaBuilder()
				        .lessThanOrEqualTo(criteriaContext.getRoot().get(propertyName), dateEnd));
			case GREATERTHAN_OR_EQUALS:
			case GREATERTHAN:
				return Optional.of(criteriaContext.getCriteriaBuilder()
				        .greaterThanOrEqualTo(criteriaContext.getRoot().get(propertyName), dateStart));
			case STARTS_AFTER:
				return Optional.of(
				    criteriaContext.getCriteriaBuilder().greaterThan(criteriaContext.getRoot().get(propertyName), dateEnd));
			case ENDS_BEFORE:
				return Optional.of(
				    criteriaContext.getCriteriaBuilder().lessThan(criteriaContext.getRoot().get(propertyName), dateEnd));
		}
		
		return Optional.empty();
	}
	
	protected <T, U> Optional<Predicate> handleQuantity(OpenmrsFhirCriteriaContext<T, U> criteriaContext,
	        String propertyName, QuantityParam quantityParam) {
		if (quantityParam == null) {
			return Optional.empty();
		}
		
		BigDecimal value = quantityParam.getValue();
		if (quantityParam.getPrefix() == null || quantityParam.getPrefix() == ParamPrefixEnum.APPROXIMATE) {
			String plainString = quantityParam.getValue().toPlainString();
			int dotIdx = plainString.indexOf('.');
			
			BigDecimal approxRange = APPROX_RANGE.multiply(value);
			if (dotIdx == -1) {
				double lowerBound = value.subtract(approxRange).doubleValue();
				double upperBound = value.add(approxRange).doubleValue();
				return Optional.of(criteriaContext.getCriteriaBuilder().between(criteriaContext.getRoot().get(propertyName),
				    lowerBound, upperBound));
			} else {
				int precision = plainString.length() - (dotIdx);
				double mul = Math.pow(10, -precision);
				double val = mul * 5.0d;
				double lowerBound = value.subtract(new BigDecimal(val)).doubleValue();
				double upperBound = value.add(new BigDecimal(val)).doubleValue();
				return Optional.of(criteriaContext.getCriteriaBuilder().between(criteriaContext.getRoot().get(propertyName),
				    lowerBound, upperBound));
			}
		} else {
			double val = value.doubleValue();
			switch (quantityParam.getPrefix()) {
				case EQUAL:
					return Optional.of(
					    criteriaContext.getCriteriaBuilder().equal(criteriaContext.getRoot().get(propertyName), val));
				case NOT_EQUAL:
					return Optional.of(
					    criteriaContext.getCriteriaBuilder().notEqual(criteriaContext.getRoot().get(propertyName), val));
				case LESSTHAN_OR_EQUALS:
					return Optional.of(criteriaContext.getCriteriaBuilder()
					        .lessThanOrEqualTo(criteriaContext.getRoot().get(propertyName), val));
				case LESSTHAN:
					return Optional.of(
					    criteriaContext.getCriteriaBuilder().lessThan(criteriaContext.getRoot().get(propertyName), val));
				case GREATERTHAN_OR_EQUALS:
					return Optional.of(criteriaContext.getCriteriaBuilder()
					        .greaterThanOrEqualTo(criteriaContext.getRoot().get(propertyName), val));
				case GREATERTHAN:
					return Optional.of(
					    criteriaContext.getCriteriaBuilder().greaterThan(criteriaContext.getRoot().get(propertyName), val));
			}
		}
		
		return Optional.empty();
	}
	
	protected <T, U> Optional<Predicate> handleQuantity(OpenmrsFhirCriteriaContext<T, U> criteriaContext,
	        @Nonnull String propertyName, QuantityAndListParam quantityAndListParam) {
		if (quantityAndListParam == null) {
			return Optional.empty();
		}
		
		return handleAndListParam(criteriaContext.getCriteriaBuilder(), quantityAndListParam,
		    quantityParam -> handleQuantity(criteriaContext, propertyName, quantityParam));
	}
	
	protected <T, U> void handleEncounterReference(OpenmrsFhirCriteriaContext<T, U> criteriaContext,
	        ReferenceAndListParam encounterReference, @Nonnull String encounterAlias) {
		handleEncounterReference(criteriaContext, encounterReference, encounterAlias, "encounter");
	}
	
	protected <T, U> void handleEncounterReference(OpenmrsFhirCriteriaContext<T, U> criteriaContext,
	        ReferenceAndListParam encounterReference, @Nonnull String encounterAlias, @Nonnull String associationPath) {
		
		if (encounterReference == null) {
			return;
		}
		Join<?, ?> associationPathEncounterJoin = criteriaContext.addJoin(associationPath, encounterAlias);
		
		handleAndListParam(criteriaContext.getCriteriaBuilder(), encounterReference, token -> {
			if (token.getChain() != null) {
				switch (token.getChain()) {
					case Encounter.SP_TYPE:
						Join<?, ?> associationPathEncounterEncounterTypeJoin = criteriaContext
						        .addJoin(associationPathEncounterJoin, "encounterType", "et");
						return propertyLike(criteriaContext, associationPathEncounterEncounterTypeJoin, "uuid",
						    new StringParam(token.getValue(), true));
				}
			} else {
				return Optional.of(
				    criteriaContext.getCriteriaBuilder().equal(associationPathEncounterJoin.get("uuid"), token.getIdPart()));
			}
			
			return Optional.empty();
		}).ifPresent(c -> criteriaContext.addPredicate(c).finalizeQuery());
	}
	
	protected <T, U> Optional<Predicate> handleGender(OpenmrsFhirCriteriaContext<T, U> criteriaContext, From<?, ?> from,
	        @Nonnull String propertyName, TokenAndListParam gender) {
		if (gender == null) {
			return Optional.empty();
		}
		
		return handleAndListParam(criteriaContext.getCriteriaBuilder(), gender, token -> {
			try {
				AdministrativeGender administrativeGender = AdministrativeGender.fromCode(token.getValue());
				
				if (administrativeGender == null) {
					return Optional.of(criteriaContext.getCriteriaBuilder().isNull(from.get(propertyName)));
				}
				
				switch (administrativeGender) {
					case MALE:
						return Optional.of(criteriaContext.getCriteriaBuilder().like(from.get(propertyName), "M"));
					case FEMALE:
						return Optional.of(criteriaContext.getCriteriaBuilder().like(from.get(propertyName), "F"));
					case OTHER:
					case UNKNOWN:
					case NULL:
						return Optional.of(criteriaContext.getCriteriaBuilder().isNull(from.get(propertyName)));
				}
			}
			catch (FHIRException ignored) {}
			return Optional.of(criteriaContext.getCriteriaBuilder().like(from.get(propertyName), token.getValue()));
		});
	}
	
	protected <T, U> Optional<Predicate> handleLocationReference(OpenmrsFhirCriteriaContext<T, U> criteriaContext,
	        @Nonnull From<?, ?> locationAlias, ReferenceAndListParam locationReference) {
		
		if (locationReference == null) {
			return Optional.empty();
		}
		
		return handleAndListParam(criteriaContext.getCriteriaBuilder(), locationReference, token -> {
			if (token.getChain() != null) {
				switch (token.getChain()) {
					case Location.SP_NAME:
						return propertyLike(criteriaContext, locationAlias, "name", token.getValue());
					case Location.SP_ADDRESS_CITY:
						return propertyLike(criteriaContext, locationAlias, "cityVillage", token.getValue());
					case Location.SP_ADDRESS_STATE:
						return propertyLike(criteriaContext, locationAlias, "stateProvince", token.getValue());
					case Location.SP_ADDRESS_POSTALCODE:
						return propertyLike(criteriaContext, locationAlias, "postalCode", token.getValue());
					case Location.SP_ADDRESS_COUNTRY:
						return propertyLike(criteriaContext, locationAlias, "country", token.getValue());
				}
			} else {
				return Optional.of(criteriaContext.getCriteriaBuilder().equal(locationAlias.get("uuid"), token.getValue()));
			}
			
			return Optional.empty();
		});
	}
	
	protected <T, U> void handleParticipantReference(OpenmrsFhirCriteriaContext<T, U> criteriaContext,
	        ReferenceAndListParam participantReference, From<?, ?> epJoin) {
		if (participantReference != null) {
			
			handleAndListParam(criteriaContext.getCriteriaBuilder(), participantReference, participantToken -> {
				if (participantToken.getChain() != null) {
					switch (participantToken.getChain()) {
						case Practitioner.SP_IDENTIFIER:
							criteriaContext.addJoin(epJoin, "provider", "p");
							return criteriaContext.getJoin("p").map(providerJoin -> criteriaContext.getCriteriaBuilder()
							        .like(providerJoin.get("identifier"), participantToken.getValue()));
						case Practitioner.SP_GIVEN: {
							Join<?, ?> encounterProviderProvider = criteriaContext.addJoin(epJoin, "provider", "pro");
							Join<?, ?> encounterProviderPerson = criteriaContext.addJoin(encounterProviderProvider, "person",
							    "ps");
							Join<?, ?> encounterProviderPersonName = criteriaContext.addJoin(encounterProviderPerson,
							    "names", "pn");
							
							return Optional.of(criteriaContext.getCriteriaBuilder()
							        .like(encounterProviderPersonName.get("givenName"), participantToken.getValue()));
						}
						case Practitioner.SP_FAMILY: {
							Join<?, ?> encounterProviderProvider = criteriaContext.addJoin(epJoin, "provider", "pro");
							Join<?, ?> encounterProviderPerson = criteriaContext.addJoin(encounterProviderProvider, "person",
							    "ps");
							Join<?, ?> encounterProviderPersonName = criteriaContext.addJoin(encounterProviderPerson,
							    "names", "pn");
							
							return Optional.of(criteriaContext.getCriteriaBuilder()
							        .like(encounterProviderPersonName.get("familyName"), participantToken.getValue()));
						}
						case Practitioner.SP_NAME: {
							Join<?, ?> encounterProviderProvider = criteriaContext.addJoin(epJoin, "provider", "pro");
							Join<?, ?> encounterProviderPerson = criteriaContext.addJoin(encounterProviderProvider, "person",
							    "ps");
							Join<?, ?> encounterProviderPersonName = criteriaContext.addJoin(encounterProviderPerson,
							    "names", "pn");
							
							List<Optional<? extends Predicate>> predicateList = new ArrayList<>();
							
							for (String token : StringUtils.split(participantToken.getValue(), " \t,")) {
								predicateList
								        .add(propertyLike(criteriaContext, encounterProviderPersonName, "givenName", token));
								predicateList.add(
								    propertyLike(criteriaContext, encounterProviderPersonName, "middleName", token));
								predicateList.add(
								    propertyLike(criteriaContext, encounterProviderPersonName, "familyName", token));
							}
							
							return Optional.of(criteriaContext.getCriteriaBuilder().or(toCriteriaArray(predicateList)));
						}
					}
				} else {
					Join<?, ?> encounterProviderProvider = criteriaContext.addJoin(epJoin, "provider", "pro");
					return Optional.of(criteriaContext.getCriteriaBuilder().equal(encounterProviderProvider.get("uuid"),
					    participantToken.getValue()));
				}
				
				return Optional.empty();
			}).ifPresent(c -> criteriaContext.addPredicate(c).finalizeQuery());
		}
	}
	
	//Added this method to allow handling classes with provider instead  of encounterProvider
	protected <T, U> void handleProviderReference(OpenmrsFhirCriteriaContext<T, U> criteriaContext,
	        ReferenceAndListParam providerReference) {
		if (providerReference != null) {
			Join<?, ?> orderer = criteriaContext.addJoin("orderer", "ord");
			
			handleAndListParam(criteriaContext.getCriteriaBuilder(), providerReference, participantToken -> {
				if (participantToken.getChain() != null) {
					switch (participantToken.getChain()) {
						case Practitioner.SP_IDENTIFIER:
							return Optional.of(criteriaContext.getCriteriaBuilder().like(orderer.get("identifier"),
							    participantToken.getValue()));
						case Practitioner.SP_GIVEN: {
							Join<?, ?> ordererPerson = criteriaContext.addJoin(orderer, "person", "ps");
							Join<?, ?> ordererName = criteriaContext.addJoin(ordererPerson, "names", "pn");
							
							return Optional.of(criteriaContext.getCriteriaBuilder().like(ordererName.get("givenName"),
							    participantToken.getValue()));
						}
						case Practitioner.SP_FAMILY: {
							Join<?, ?> ordererPerson = criteriaContext.addJoin(orderer, "person", "ps");
							Join<?, ?> ordererName = criteriaContext.addJoin(ordererPerson, "names", "pn");
							
							return Optional.of(criteriaContext.getCriteriaBuilder().like(ordererName.get("familyName"),
							    participantToken.getValue()));
						}
						case Practitioner.SP_NAME: {
							Join<?, ?> ordererPerson = criteriaContext.addJoin(orderer, "person", "ps");
							Join<?, ?> ordererName = criteriaContext.addJoin(ordererPerson, "names", "pn");
							
							List<Optional<? extends Predicate>> predicateList = new ArrayList<>();
							
							for (String token : StringUtils.split(participantToken.getValue(), " \t,")) {
								predicateList.add(propertyLike(criteriaContext, ordererName, "givenName", token));
								predicateList.add(propertyLike(criteriaContext, ordererName, "middleName", token));
								predicateList.add(propertyLike(criteriaContext, ordererName, "familyName", token));
							}
							
							return Optional.of(criteriaContext.getCriteriaBuilder().or(toCriteriaArray(predicateList)));
						}
					}
				} else {
					return Optional.of(
					    criteriaContext.getCriteriaBuilder().equal(orderer.get("uuid"), participantToken.getValue()));
				}
				
				return Optional.empty();
			}).ifPresent(c -> criteriaContext.addPredicate(c).finalizeQuery());
		}
	}
	
	protected <T, U> Optional<Predicate> handleCodeableConcept(OpenmrsFhirCriteriaContext<T, U> criteriaContext,
	        TokenAndListParam concepts, @Nonnull From<?, ?> conceptAlias, @Nonnull String conceptMapAlias,
	        @Nonnull String conceptReferenceTermAlias) {
		if (concepts == null) {
			return Optional.empty();
		}
		
		return handleAndListParamBySystem(criteriaContext.getCriteriaBuilder(), concepts, (system, tokens) -> {
			if (system.isEmpty()) {
				criteriaContext.getCriteriaBuilder()
				        .literal(tokensToParams(tokens).map(NumberUtils::toInt).collect(Collectors.toList()));
				return Optional
				        .of(criteriaContext
				                .getCriteriaBuilder().or(
				                    criteriaContext.getCriteriaBuilder()
				                            .in(conceptAlias.get("conceptId")
				                                    .in(criteriaContext.getCriteriaBuilder()
				                                            .literal(tokensToParams(tokens).map(NumberUtils::toInt)
				                                                    .collect(Collectors.toList())))),
				                    criteriaContext.getCriteriaBuilder().in(conceptAlias.get("uuid")
				                            .in(criteriaContext.getCriteriaBuilder().literal(tokensToList(tokens))))));
			} else {
				Join<?, ?> conceptMapAliasJoin = criteriaContext.addJoin(conceptAlias, "conceptMappings", conceptMapAlias);
				criteriaContext.addJoin(conceptMapAliasJoin, "conceptReferenceTerm", conceptReferenceTermAlias);
				
				return generateSystemQuery(criteriaContext, system, tokensToList(tokens), conceptReferenceTermAlias);
			}
		});
	}
	
	protected <T, U> void handleNames(OpenmrsFhirCriteriaContext<T, U> criteriaContext, StringAndListParam name,
	        StringAndListParam given, StringAndListParam family) {
		handleNames(criteriaContext, name, given, family, criteriaContext.getRoot());
	}
	
	protected <T, U> void handleNames(OpenmrsFhirCriteriaContext<T, U> criteriaContext, StringAndListParam name,
	        StringAndListParam given, StringAndListParam family, From<?, ?> person) {
		
		if (name == null && given == null && family == null) {
			return;
		}
		
		Join<?, ?> personNameAliasJoin = criteriaContext.addJoin(person, "names", "pn",
		    (personNameAlias) -> criteriaContext.getCriteriaBuilder().equal(personNameAlias.get("voided"), false));
		
		if (name != null) {
			handleAndListParamAsStream(criteriaContext.getCriteriaBuilder(), name,
			    (nameParam) -> Arrays.stream(StringUtils.split(nameParam.getValue(), " \t,"))
			            .map(token -> new StringParam().setValue(token).setExact(nameParam.isExact())
			                    .setContains(nameParam.isContains()))
			            .map(tokenParam -> Arrays.asList(
			                propertyLike(criteriaContext, personNameAliasJoin, "givenName", tokenParam),
			                propertyLike(criteriaContext, personNameAliasJoin, "middleName", tokenParam),
			                propertyLike(criteriaContext, personNameAliasJoin, "familyName", tokenParam)))
			            .flatMap(Collection::stream)).ifPresent(c -> criteriaContext.addPredicate(c).finalizeQuery());
		}
		
		if (given != null) {
			handleAndListParam(criteriaContext.getCriteriaBuilder(), given,
			    (givenName) -> propertyLike(criteriaContext, personNameAliasJoin, "givenName", givenName))
			            .ifPresent(c -> criteriaContext.addPredicate(c).finalizeQuery());
		}
		
		if (family != null) {
			handleAndListParam(criteriaContext.getCriteriaBuilder(), family,
			    (familyName) -> propertyLike(criteriaContext, personNameAliasJoin, "familyName", familyName))
			            .ifPresent(c -> criteriaContext.addPredicate(c).finalizeQuery());
		}
	}
	
	protected <T, U> void handlePatientReference(OpenmrsFhirCriteriaContext<T, U> criteriaContext,
	        ReferenceAndListParam patientReference) {
		handlePatientReference(criteriaContext, patientReference, "patient");
	}
	
	protected <T, U> void handlePatientReference(OpenmrsFhirCriteriaContext<T, U> criteriaContext,
	        ReferenceAndListParam patientReference, String associationPath) {
		if (patientReference != null) {
			Join<?, ?> associationPathJoin = criteriaContext.addJoin(associationPath, "p");
			handleAndListParam(criteriaContext.getCriteriaBuilder(), patientReference, patientToken -> {
				if (patientToken.getChain() != null) {
					switch (patientToken.getChain()) {
						case Patient.SP_IDENTIFIER:
							Join<?, ?> associationPathIdentifiersJoin = criteriaContext.addJoin(associationPathJoin,
							    "identifiers", "pi");
							return Optional.of(criteriaContext.getCriteriaBuilder()
							        .like(associationPathIdentifiersJoin.get("identifier"), patientToken.getValue()));
						case Patient.SP_GIVEN: {
							Join<?, ?> associationPathNamesJoin = criteriaContext.addJoin(associationPathJoin, "names",
							    "pn");
							return Optional.of(criteriaContext.getCriteriaBuilder()
							        .like(associationPathNamesJoin.get("givenName"), patientToken.getValue()));
						}
						case Patient.SP_FAMILY: {
							Join<?, ?> associationPathNamesJoin = criteriaContext.addJoin(associationPathJoin, "names",
							    "pn");
							return Optional.of(criteriaContext.getCriteriaBuilder()
							        .like(associationPathNamesJoin.get("familyName"), patientToken.getValue()));
						}
						case Patient.SP_NAME:
							Join<?, ?> associationPathNamesJoin = criteriaContext.addJoin(associationPathJoin, "names",
							    "pn");
							
							List<Optional<? extends Predicate>> criterionList = new ArrayList<>();
							
							for (String token : StringUtils.split(patientToken.getValue(), " \t,")) {
								criterionList
								        .add(propertyLike(criteriaContext, associationPathNamesJoin, "givenName", token));
								criterionList
								        .add(propertyLike(criteriaContext, associationPathNamesJoin, "middleName", token));
								criterionList
								        .add(propertyLike(criteriaContext, associationPathNamesJoin, "familyName", token));
							}
							return Optional.of(criteriaContext.getCriteriaBuilder().or(toCriteriaArray(criterionList)));
					}
				} else {
					return Optional.of(criteriaContext.getCriteriaBuilder().equal(associationPathJoin.get("uuid"),
					    patientToken.getValue()));
				}
				
				return Optional.empty();
			}).ifPresent(c -> criteriaContext.addPredicate(c).finalizeQuery());
		}
	}
	
	protected <T, U> Optional<Predicate> handleCommonSearchParameters(OpenmrsFhirCriteriaContext<T, U> criteriaContext,
	        List<PropParam<?>> theCommonParams) {
		List<Optional<? extends Predicate>> criterionList = new ArrayList<>();
		
		for (PropParam<?> commonSearchParam : theCommonParams) {
			switch (commonSearchParam.getPropertyName()) {
				case FhirConstants.ID_PROPERTY:
					criterionList.add(handleAndListParam(criteriaContext.getCriteriaBuilder(),
					    (TokenAndListParam) commonSearchParam.getParam(), param -> Optional.of(criteriaContext
					            .getCriteriaBuilder().equal(criteriaContext.getRoot().get("uuid"), param.getValue()))));
					break;
				case FhirConstants.LAST_UPDATED_PROPERTY:
					criterionList.add(handleLastUpdated(criteriaContext, (DateRangeParam) commonSearchParam.getParam()));
					break;
			}
		}
		return Optional.of(criteriaContext.getCriteriaBuilder().and(toCriteriaArray(criterionList.stream())));
	}
	
	/**
	 * This function should be overridden by implementations. It is used to return a criterion for
	 * _lastUpdated from resources where there are multiple properties to be considered.
	 *
	 * @param param the DateRangeParam used to query for _lastUpdated
	 * @return an optional criterion for the query
	 */
	protected abstract <T, U> Optional<Predicate> handleLastUpdated(OpenmrsFhirCriteriaContext<T, U> criteriaContext,
	        DateRangeParam param);
	
	protected <T, U> Optional<Predicate> handlePersonAddress(OpenmrsFhirCriteriaContext<T, U> criteriaContext,
	        From<?, ?> aliasPrefix, StringAndListParam city, StringAndListParam state, StringAndListParam postalCode,
	        StringAndListParam country) {
		if (city == null && state == null && postalCode == null && country == null) {
			return Optional.empty();
		}
		
		List<Optional<? extends Predicate>> predicateList = new ArrayList<>();
		
		if (city != null) {
			predicateList.add(handleAndListParam(criteriaContext.getCriteriaBuilder(), city,
			    c -> propertyLike(criteriaContext, aliasPrefix, "cityVillage", c)));
		}
		
		if (state != null) {
			predicateList.add(handleAndListParam(criteriaContext.getCriteriaBuilder(), state,
			    c -> propertyLike(criteriaContext, aliasPrefix, "stateProvince", c)));
		}
		
		if (postalCode != null) {
			predicateList.add(handleAndListParam(criteriaContext.getCriteriaBuilder(), postalCode,
			    c -> propertyLike(criteriaContext, aliasPrefix, "postalCode", c)));
		}
		
		if (country != null) {
			predicateList.add(handleAndListParam(criteriaContext.getCriteriaBuilder(), country,
			    c -> propertyLike(criteriaContext, aliasPrefix, "country", c)));
		}
		
		return Optional.of(criteriaContext.getCriteriaBuilder().and(toCriteriaArray(predicateList.stream())));
	}
	
	protected <T, U> Optional<Predicate> handleMedicationReference(OpenmrsFhirCriteriaContext<T, U> criteriaContext,
	        @Nonnull From<?, ?> medicationAlias, ReferenceAndListParam medicationReference) {
		if (medicationReference == null) {
			return Optional.empty();
		}
		
		return handleAndListParam(criteriaContext.getCriteriaBuilder(), medicationReference, token -> Optional
		        .of(criteriaContext.getCriteriaBuilder().equal(medicationAlias.get("uuid"), token.getIdPart())));
	}
	
	protected <T, U> Optional<Predicate> handleMedicationRequestReference(OpenmrsFhirCriteriaContext<T, U> criteriaContext,
	        @Nonnull String drugOrderAlias, ReferenceAndListParam drugOrderReference) {
		if (drugOrderReference == null) {
			return Optional.empty();
		}
		
		return handleAndListParam(criteriaContext.getCriteriaBuilder(), drugOrderReference,
		    token -> Optional.of(criteriaContext.getCriteriaBuilder()
		            .equal(getRootOrJoin(criteriaContext, drugOrderAlias).get("uuid"), token.getIdPart())));
	}
	
	/**
	 * Use this method to properly implement sorting for your query. Note that for this method to work,
	 * you must override one or more of: {@link #paramToProps(OpenmrsFhirCriteriaContext, SortState)},
	 * {@link #paramToProps(OpenmrsFhirCriteriaContext, String)}, or
	 * {@link #paramToProp(OpenmrsFhirCriteriaContext, String)}.
	 *
	 * @param criteriaContext The {@link OpenmrsFhirCriteriaContext} for the current query
	 * @param sort the {@link SortSpec} which defines the sorting to be translated
	 */
	protected <T, U> void handleSort(OpenmrsFhirCriteriaContext<T, U> criteriaContext, SortSpec sort) {
		handleSort(criteriaContext, sort, this::paramToProps).ifPresent(l -> l.forEach(criteriaContext::addOrder));
	}
	
	protected <T, U> Optional<List<javax.persistence.criteria.Order>> handleSort(
	        OpenmrsFhirCriteriaContext<T, U> criteriaContext, SortSpec sort,
	        BiFunction<OpenmrsFhirCriteriaContext<T, U>, SortState, Collection<javax.persistence.criteria.Order>> paramToProp) {
		List<javax.persistence.criteria.Order> orderings = new ArrayList<>();
		SortSpec sortSpec = sort;
		while (sortSpec != null) {
			SortState state = SortState.builder().context(criteriaContext).sortOrder(sortSpec.getOrder())
			        .parameter(sortSpec.getParamName().toLowerCase()).build();
			
			Collection<javax.persistence.criteria.Order> orders = paramToProp.apply(criteriaContext, state);
			if (orders != null) {
				orderings.addAll(orders);
			}
			
			sortSpec = sortSpec.getChain();
		}
		
		if (orderings.isEmpty()) {
			return Optional.empty();
		}
		
		return Optional.of(orderings);
	}
	
	protected <T, U> Optional<Predicate> generateSystemQuery(OpenmrsFhirCriteriaContext<T, U> criteriaContext, String system,
	        List<String> codes, String conceptReferenceTermAlias) {
		OpenmrsFhirCriteriaSubquery<FhirConceptSource> conceptSourceSubquery = criteriaContext
		        .addSubquery(FhirConceptSource.class);
		conceptSourceSubquery.addPredicate(
		    conceptSourceSubquery.getCriteriaBuilder().equal(conceptSourceSubquery.getRoot().get("url"), system));
		
		return criteriaContext.getJoin(conceptReferenceTermAlias)
		        .map((conceptReferenceTermJoin) -> criteriaContext.getCriteriaBuilder().and(
		            criteriaContext.getCriteriaBuilder().in(conceptReferenceTermJoin.get("conceptSource"))
		                    .value(conceptSourceSubquery.finalizeQuery()),
		            criteriaContext.getCriteriaBuilder().in(conceptReferenceTermJoin.get("code")).value(codes)));
	}
	
	protected <T, U> Predicate generateActiveOrderQuery(OpenmrsFhirCriteriaContext<T, U> criteriaContext, String path,
	        Date onDate) {
		// ACTIVE = date activated null or less than or equal to current datetime, date stopped null or in the future, auto expire date null or in the future
		return criteriaContext.getCriteriaBuilder().and(
		    criteriaContext.getCriteriaBuilder().or(
		        criteriaContext.getCriteriaBuilder().isNull(getRootOrJoin(criteriaContext, path).get("dateActivated")),
		        criteriaContext.getCriteriaBuilder().lessThan(getRootOrJoin(criteriaContext, path).get("dateActivated"),
		            onDate)),
		    criteriaContext.getCriteriaBuilder().or(
		        criteriaContext.getCriteriaBuilder().isNull(getRootOrJoin(criteriaContext, path).get("dateStopped")),
		        criteriaContext.getCriteriaBuilder().greaterThan(getRootOrJoin(criteriaContext, path).get("dateStopped"),
		            onDate)),
		    criteriaContext.getCriteriaBuilder().or(
		        criteriaContext.getCriteriaBuilder().isNull(getRootOrJoin(criteriaContext, path).get("autoExpireDate")),
		        criteriaContext.getCriteriaBuilder().greaterThan(getRootOrJoin(criteriaContext, path).get("autoExpireDate"),
		            onDate)));
	}
	
	protected <T, U> Predicate generateActiveOrderQuery(OpenmrsFhirCriteriaContext<T, U> criteriaContext, String path) {
		return generateActiveOrderQuery(criteriaContext, path, new Date());
	}
	
	protected <T, U> Predicate generateActiveOrderQuery(OpenmrsFhirCriteriaContext<T, U> criteriaContext, Date onDate) {
		return generateActiveOrderQuery(criteriaContext, "", onDate);
	}
	
	protected <T, U> Predicate generateActiveOrderQuery(OpenmrsFhirCriteriaContext<T, U> criteriaContext) {
		return generateActiveOrderQuery(criteriaContext, new Date());
	}
	
	protected <T, U> Predicate generateNotCancelledOrderQuery(OpenmrsFhirCriteriaContext<T, U> criteriaContext) {
		return generateNotCancelledOrderQuery(criteriaContext, "");
	}
	
	protected <T, U> Predicate generateNotCancelledOrderQuery(OpenmrsFhirCriteriaContext<T, U> criteriaContext,
	        String path) {
		Date now = new Date();
		
		return criteriaContext.getCriteriaBuilder().or(
		    criteriaContext.getCriteriaBuilder().isNull(getRootOrJoin(criteriaContext, path).get("dateStopped")),
		    criteriaContext.getCriteriaBuilder().greaterThan(getRootOrJoin(criteriaContext, path).get("dateStopped"), now));
	}
	
	protected TokenOrListParam convertStringStatusToBoolean(TokenOrListParam statusParam) {
		if (statusParam != null) {
			return handleOrListParam(statusParam).map(s -> {
				switch (s.getValue()) {
					case "active":
						return Optional.of("false");
					case "inactive":
						return Optional.of("true");
					default:
						return Optional.empty();
				}
			}).filter(Optional::isPresent).map(Optional::get).collect(TokenOrListParam::new,
			    (tp, v) -> tp.add(String.valueOf(v)), (tp1, tp2) -> tp2.getListAsCodings().forEach(tp1::add));
		}
		
		return null;
	}
	
	protected TokenAndListParam convertStringStatusToBoolean(TokenAndListParam statusParam) {
		if (statusParam != null) {
			return handleAndListParam(statusParam).map(this::convertStringStatusToBoolean).collect(TokenAndListParam::new,
			    TokenAndListParam::addAnd, (tp1, tp2) -> tp2.getValuesAsQueryTokens().forEach(tp1::addAnd));
		}
		
		return null;
	}
	
	/**
	 * This function should be overridden by implementations. It is used to map FHIR parameter names to
	 * their corresponding values in the query.
	 *
	 * @param sortState a {@link SortState} object describing the current sort state
	 * @return the corresponding ordering(s) needed for this property
	 */
	protected <T, U> Collection<Order> paramToProps(OpenmrsFhirCriteriaContext<T, U> criteriaContext,
	        @Nonnull SortState sortState) {
		Collection<String> prop = paramToProps(criteriaContext, sortState.getParameter());
		if (prop != null) {
			switch (sortState.getSortOrder()) {
				case ASC:
					return prop.stream().map(s -> criteriaContext.getCriteriaBuilder().asc(criteriaContext.getRoot().get(s)))
					        .collect(Collectors.toList());
				case DESC:
					return prop.stream()
					        .map(s -> criteriaContext.getCriteriaBuilder().desc(criteriaContext.getRoot().get(s)))
					        .collect(Collectors.toList());
			}
		}
		
		return null;
	}
	
	/**
	 * This function should be overridden by implementations. It is used to map FHIR parameter names to
	 * properties where there is only a single property.
	 *
	 * @param param the FHIR parameter to map
	 * @return the name of the corresponding property from the current query
	 */
	protected <T, U> Collection<String> paramToProps(OpenmrsFhirCriteriaContext<T, U> criteriaContext,
	        @Nonnull String param) {
		String prop = paramToProp(criteriaContext, param);
		
		if (prop != null) {
			return Collections.singleton(prop);
		}
		
		return null;
	}
	
	/**
	 * This function should be overridden by implementations. It is used to map FHIR parameter names to
	 * properties where there is only a single property.
	 *
	 * @param param the FHIR parameter to map
	 * @return the name of the corresponding property from the current query
	 */
	protected <T, U> String paramToProp(OpenmrsFhirCriteriaContext<T, U> criteriaContext, @Nonnull String param) {
		return null;
	}
	
	protected <T, U> Optional<Predicate> propertyLike(OpenmrsFhirCriteriaContext<T, U> criteriaContext, From<?, ?> from,
	        @Nonnull String propertyName, String value) {
		if (value == null) {
			return Optional.empty();
		}
		
		return propertyLike(criteriaContext, from, propertyName, new StringParam(value));
	}
	
	protected <T, U> Optional<Predicate> propertyLike(OpenmrsFhirCriteriaContext<T, U> criteriaContext, From<?, ?> from,
	        @Nonnull String propertyName, StringParam param) {
		if (param == null) {
			return Optional.empty();
		}
		
		Predicate likePredicate;
		if (param.isExact()) {
			likePredicate = criteriaContext.getCriteriaBuilder().equal(from.get(propertyName), param.getValue());
		} else if (param.isContains()) {
			likePredicate = criteriaContext.getCriteriaBuilder().like(from.get(propertyName), "%" + param.getValue() + "%");
		} else {
			likePredicate = criteriaContext.getCriteriaBuilder().like(from.get(propertyName), param.getValue() + "%");
		}
		
		return Optional.of(likePredicate);
	}
	
	protected List<String> tokensToList(List<TokenParam> tokens) {
		return tokensToParams(tokens).collect(Collectors.toList());
	}
	
	protected Stream<String> tokensToParams(List<TokenParam> tokens) {
		return tokens.stream().map(TokenParam::getValue);
	}
	
	private String groupBySystem(@Nonnull TokenParam token) {
		return StringUtils.trimToEmpty(token.getSystem());
	}
	
	protected <T extends IQueryParameterOr<U>, U extends IQueryParameterType> Stream<T> handleAndListParam(
	        IQueryParameterAnd<T> andListParameter) {
		return andListParameter.getValuesAsQueryTokens().stream();
	}
	
	protected <T extends IQueryParameterType> Stream<T> handleOrListParam(IQueryParameterOr<T> orListParameter) {
		return orListParameter.getValuesAsQueryTokens().stream();
	}
	
	@SafeVarargs
	@SuppressWarnings("unused")
	protected final Predicate[] toCriteriaArray(Optional<? extends Predicate>... predicate) {
		return toCriteriaArray(Arrays.stream(predicate));
	}
	
	protected Predicate[] toCriteriaArray(Collection<Optional<? extends Predicate>> collection) {
		return toCriteriaArray(collection.stream());
	}
	
	protected Predicate[] toCriteriaArray(Stream<Optional<? extends Predicate>> predicateStream) {
		return predicateStream.filter(Optional::isPresent).map(Optional::get).toArray(Predicate[]::new);
	}
	
	/**
	 * This object is used to store the state of the sorting
	 */
	@Data
	@Builder
	@EqualsAndHashCode
	public static final class SortState {
		
		private OpenmrsFhirCriteriaContext context;
		
		private SortOrderEnum sortOrder;
		
		private String parameter;
	}
	
	protected <T, U> Optional<Predicate> handleAgeByDateProperty(OpenmrsFhirCriteriaContext<T, U> criteriaContext,
	        @Nonnull String datePropertyName, @Nonnull QuantityParam age) {
		BigDecimal value = age.getValue();
		if (value == null) {
			throw new IllegalArgumentException("Age value should be provided in " + age);
		}
		
		String unit = age.getUnits();
		if (unit == null) {
			throw new IllegalArgumentException("Age unit should be provided in " + age);
		}
		
		LocalDateTime localDateTime = localDateTimeFactory.now();
		
		TemporalAmount temporalAmount;
		TemporalUnit temporalUnit;
		// TODO check if HAPI FHIR already defines these constant strings. These are mostly from
		// http://www.hl7.org/fhir/valueset-age-units.html with the exception of "s" which is not
		// listed but was seen in FHIR examples: http://www.hl7.org/fhir/datatypes-examples.html#Quantity
		switch (unit) {
			case "s":
				temporalUnit = ChronoUnit.SECONDS;
				temporalAmount = Duration.ofSeconds(value.longValue());
				break;
			case "min":
				temporalUnit = ChronoUnit.MINUTES;
				temporalAmount = Duration.ofMinutes(value.longValue());
				break;
			case "h":
				temporalUnit = ChronoUnit.HOURS;
				temporalAmount = Duration.ofHours(value.longValue());
				break;
			case "d":
				temporalUnit = ChronoUnit.DAYS;
				temporalAmount = Period.ofDays(value.intValue());
				break;
			case "wk":
				temporalUnit = ChronoUnit.WEEKS;
				temporalAmount = Period.ofWeeks(value.intValue());
				break;
			case "mo":
				temporalUnit = ChronoUnit.MONTHS;
				temporalAmount = Period.ofMonths(value.intValue());
				break;
			case "a":
				temporalUnit = ChronoUnit.YEARS;
				temporalAmount = Period.ofYears(value.intValue());
				break;
			default:
				throw new IllegalArgumentException(
				        "Invalid unit " + unit + " in age " + age + " should be one of 'min', 'h', 'd', 'wk', 'mo', 'a'");
		}
		
		localDateTime = localDateTime.minus(temporalAmount);
		
		ParamPrefixEnum prefix = age.getPrefix();
		if (prefix == null) {
			prefix = ParamPrefixEnum.EQUAL;
		}
		
		if (prefix == ParamPrefixEnum.EQUAL || prefix == ParamPrefixEnum.NOT_EQUAL) {
			// Create a range for the targeted unit; the interval length is determined by the unit and
			// its center is `offsetSeconds` in the past.
			final long offset;
			
			// Duration only supports hours as a chunk of seconds
			if (temporalUnit == ChronoUnit.HOURS) {
				offset = temporalAmount.get(ChronoUnit.SECONDS) / (2 * 3600);
			} else {
				offset = temporalAmount.get(temporalUnit) / 2;
			}
			
			LocalDateTime lowerBoundDateTime = LocalDateTime.from(localDateTime).minus(Duration.of(offset, temporalUnit));
			Date lowerBound = Date.from(lowerBoundDateTime.atZone(ZoneId.systemDefault()).toInstant());
			
			LocalDateTime upperBoundDateTime = LocalDateTime.from(localDateTime).plus(offset, temporalUnit);
			Date upperBound = Date.from(upperBoundDateTime.atZone(ZoneId.systemDefault()).toInstant());
			
			if (prefix == ParamPrefixEnum.EQUAL) {
				return Optional.ofNullable(criteriaContext.getCriteriaBuilder().and(
				    criteriaContext.getCriteriaBuilder()
				            .greaterThanOrEqualTo(criteriaContext.getRoot().get(datePropertyName), lowerBound),
				    criteriaContext.getCriteriaBuilder().lessThanOrEqualTo(criteriaContext.getRoot().get(datePropertyName),
				        upperBound)));
			} else {
				return Optional.ofNullable(criteriaContext.getCriteriaBuilder()
				        .not(criteriaContext.getCriteriaBuilder().and(
				            criteriaContext.getCriteriaBuilder()
				                    .greaterThanOrEqualTo(criteriaContext.getRoot().get(datePropertyName), lowerBound),
				            criteriaContext.getCriteriaBuilder()
				                    .lessThanOrEqualTo(criteriaContext.getRoot().get(datePropertyName), upperBound))));
			}
		}
		
		switch (prefix) {
			case LESSTHAN_OR_EQUALS:
			case LESSTHAN:
			case STARTS_AFTER:
				return Optional.ofNullable(criteriaContext.getCriteriaBuilder().greaterThanOrEqualTo(
				    criteriaContext.getRoot().get(datePropertyName),
				    Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant())));
			case GREATERTHAN_OR_EQUALS:
			case GREATERTHAN:
				return Optional.ofNullable(
				    criteriaContext.getCriteriaBuilder().lessThanOrEqualTo(criteriaContext.getRoot().get(datePropertyName),
				        Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant())));
			// Ignoring ENDS_BEFORE as it is not meaningful for age.
		}
		
		return Optional.empty();
	}
	
	protected <T, U> OpenmrsFhirCriteriaContext<T, U> createCriteriaContext(Class<? super T> rootType) {
		EntityManager em = sessionFactory.getCurrentSession();
		CriteriaBuilder cb = em.getCriteriaBuilder();
		@SuppressWarnings("unchecked")
		CriteriaQuery<U> cq = (CriteriaQuery<U>) cb.createQuery(rootType);
		@SuppressWarnings("unchecked")
		Root<T> root = (Root<T>) cq.from(rootType);
		
		return new OpenmrsFhirCriteriaContext<>(em, cb, cq, root);
	}
	
	protected <T, U> From<?, ?> getRootOrJoin(OpenmrsFhirCriteriaContext<T, U> criteriaContext, String alias) {
		if (alias.isEmpty()) {
			return criteriaContext.getRoot();
		} else {
			return criteriaContext.getJoin(alias).orElseThrow(() -> new IllegalStateException(
			        "Tried to reference alias " + alias + " before creating a join with that name"));
		}
	}
	
	protected interface Specification<T> {
		
		/**
		 * Creates a WHERE clause for a query of the referenced entity in form of a {@link Predicate} for
		 * the given {@link Root} and {@link CriteriaQuery}.
		 *
		 * @param root must not be {@literal null}.
		 * @param query must not be {@literal null}.
		 * @param cb must not be {@literal null}.
		 * @return a {@link Predicate}, may be {@literal null}. <a href=
		 *         "https://github.com/spring-projects/spring-data-jpa/blob/42a20a0c151f52de0f0c75cfb8c278a619baddec/spring-data-jpa/src/main/java/org/springframework/data/jpa/domain/Specification.java#L105">to
		 *         spring-data jpa implementation</a>
		 */
		Predicate toPredicate(@Nonnull Root<T> root, @Nonnull CriteriaQuery<T> query, @Nonnull CriteriaBuilder cb);
	}
	
}
