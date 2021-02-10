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

import static org.hibernate.criterion.Projections.property;
import static org.hibernate.criterion.Restrictions.and;
import static org.hibernate.criterion.Restrictions.between;
import static org.hibernate.criterion.Restrictions.eq;
import static org.hibernate.criterion.Restrictions.ge;
import static org.hibernate.criterion.Restrictions.gt;
import static org.hibernate.criterion.Restrictions.ilike;
import static org.hibernate.criterion.Restrictions.in;
import static org.hibernate.criterion.Restrictions.isNull;
import static org.hibernate.criterion.Restrictions.le;
import static org.hibernate.criterion.Restrictions.lt;
import static org.hibernate.criterion.Restrictions.ne;
import static org.hibernate.criterion.Restrictions.not;
import static org.hibernate.criterion.Restrictions.or;
import static org.hibernate.criterion.Subqueries.propertyEq;

import javax.annotation.Nonnull;

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
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.hibernate.Criteria;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.internal.CriteriaImpl;
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
	 * Determines whether or not the given criteria object already has a given alias. This is useful to
	 * determine whether a mapping has already been made or whether a given alias is already in use.
	 *
	 * @param criteria the {@link Criteria} object to examine
	 * @param alias the alias to look for
	 * @return true if the alias exists in this criteria object, false otherwise
	 */
	protected boolean lacksAlias(@Nonnull Criteria criteria, @Nonnull String alias) {
		Optional<Iterator<CriteriaImpl.Subcriteria>> subcriteria = asImpl(criteria).map(CriteriaImpl::iterateSubcriteria);
		
		return subcriteria.filter(subcriteriaIterator -> containsAlias(subcriteriaIterator, alias)).isPresent();
	}
	
	/**
	 * Determines whether any of the {@link CriteriaImpl.Subcriteria} objects returned by a given
	 * iterator are mapped to the specified alias.
	 *
	 * @param subcriteriaIterator an {@link Iterator} of {@link CriteriaImpl.Subcriteria} to check for
	 *            the given alias
	 * @param alias the alias to look for
	 * @return true if any of the given subcriteria use the specified alias, false otherwise
	 */
	protected boolean containsAlias(Iterator<CriteriaImpl.Subcriteria> subcriteriaIterator, @Nonnull String alias) {
		return stream(subcriteriaIterator).noneMatch(sc -> sc.getAlias().equals(alias));
	}
	
	/**
	 * A generic handler for any subtype of {@link IQueryParameterAnd} which creates a criterion that
	 * represents the intersection of all of the parameters contained
	 *
	 * @param andListParam the {@link IQueryParameterAnd} to handle
	 * @param handler a {@link Function} which maps a parameter to a {@link Criterion}
	 * @param <T> the subtype of {@link IQueryParameterOr} that this {@link IQueryParameterAnd} contains
	 * @param <U> the subtype of {@link IQueryParameterType} for this parameter
	 * @return the resulting criterion, which is the intersection of all of the unions of contained
	 *         parameters
	 */
	protected <T extends IQueryParameterOr<U>, U extends IQueryParameterType> Optional<Criterion> handleAndListParam(
	        IQueryParameterAnd<T> andListParam, Function<U, Optional<Criterion>> handler) {
		if (andListParam == null) {
			return Optional.empty();
		}
		
		return Optional.of(and(
		    toCriteriaArray(handleAndListParam(andListParam).map(orListParam -> handleOrListParam(orListParam, handler)))));
	}
	
	@SuppressWarnings("unused")
	protected <T extends IQueryParameterOr<U>, U extends IQueryParameterType> Optional<Criterion> handleAndListParamBy(
	        IQueryParameterAnd<T> andListParam, Function<IQueryParameterOr<U>, Optional<Criterion>> handler) {
		if (andListParam == null) {
			return Optional.empty();
		}
		
		return Optional.of(and(toCriteriaArray(handleAndListParam(andListParam).map(handler))));
	}
	
	protected <T extends IQueryParameterOr<U>, U extends IQueryParameterType> Optional<Criterion> handleAndListParamAsStream(
	        IQueryParameterAnd<T> andListParam, Function<U, Stream<Optional<Criterion>>> handler) {
		if (andListParam == null) {
			return Optional.empty();
		}
		
		return Optional.of(and(toCriteriaArray(
		    handleAndListParam(andListParam).map(orListParam -> handleOrListParamAsStream(orListParam, handler)))));
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
	protected <T extends IQueryParameterType> Optional<Criterion> handleOrListParam(IQueryParameterOr<T> orListParam,
	        Function<T, Optional<Criterion>> handler) {
		if (orListParam == null) {
			return Optional.empty();
		}
		
		return Optional.of(or(toCriteriaArray(handleOrListParam(orListParam).map(handler))));
	}
	
	protected <T extends IQueryParameterType> Optional<Criterion> handleOrListParamAsStream(IQueryParameterOr<T> orListParam,
	        Function<T, Stream<Optional<Criterion>>> handler) {
		if (orListParam == null) {
			return Optional.empty();
		}
		
		return Optional.of(or(toCriteriaArray(handleOrListParam(orListParam).flatMap(handler))));
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
	protected <T extends IQueryParameterOr<TokenParam>> Optional<Criterion> handleAndListParamBySystem(
	        IQueryParameterAnd<T> andListParam,
	        BiFunction<String, List<TokenParam>, Optional<Criterion>> systemTokenHandler) {
		if (andListParam == null) {
			return Optional.empty();
		}
		
		return Optional.of(and(toCriteriaArray(
		    handleAndListParam(andListParam).map(param -> handleOrListParamBySystem(param, systemTokenHandler)))));
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
	protected Optional<Criterion> handleOrListParamBySystem(IQueryParameterOr<TokenParam> orListParam,
	        BiFunction<String, List<TokenParam>, Optional<Criterion>> systemTokenHandler) {
		
		if (orListParam == null) {
			return Optional.empty();
		}
		
		return Optional
		        .of(or(toCriteriaArray(handleOrListParam(orListParam).collect(Collectors.groupingBy(this::groupBySystem))
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
	protected Optional<Criterion> handleBoolean(String propertyName, TokenAndListParam booleanToken) {
		if (booleanToken == null) {
			return Optional.empty();
		}
		
		// note that we use a custom implementation here as Boolean.valueOf() and Boolean.parse() only determine whether
		// the string matches "true". We could potentially be passed a non-valid Boolean value here.
		return handleAndListParam(booleanToken, token -> {
			if (token.getValue().equalsIgnoreCase("true")) {
				return handleBooleanProperty(propertyName, true);
			} else if (token.getValue().equalsIgnoreCase("false")) {
				return handleBooleanProperty(propertyName, false);
			}
			
			return Optional.empty();
		});
	}
	
	protected Optional<Criterion> handleBooleanProperty(String propertyName, boolean booleanVal) {
		return Optional.of(eq(propertyName, booleanVal));
	}
	
	/**
	 * A handler for a {@link DateRangeParam}, which represents an inclusive set of {@link DateParam}s
	 *
	 * @param propertyName the name of the property in the query to use
	 * @param dateRangeParam the {@link DateRangeParam} to handle
	 * @return a {@link Criterion} to be added to the query for the indicated date range
	 */
	protected Optional<Criterion> handleDateRange(String propertyName, DateRangeParam dateRangeParam) {
		if (dateRangeParam == null) {
			return Optional.empty();
		}
		
		return Optional.of(and(toCriteriaArray(Stream.of(handleDate(propertyName, dateRangeParam.getLowerBound()),
		    handleDate(propertyName, dateRangeParam.getUpperBound())))));
	}
	
	/**
	 * A handler for a {@link DateParam}, which represents a day and an comparator
	 *
	 * @param propertyName the name of the property in the query to use
	 * @param dateParam the {@link DateParam} to handle
	 * @return a {@link Criterion} to be added to the query for the indicate date param
	 */
	protected Optional<Criterion> handleDate(String propertyName, DateParam dateParam) {
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
				return Optional.of(and(ge(propertyName, dateStart), lt(propertyName, dateEnd)));
			case NOT_EQUAL:
				return Optional.of(not(and(ge(propertyName, dateStart), lt(propertyName, dateEnd))));
			case LESSTHAN_OR_EQUALS:
			case LESSTHAN:
				return Optional.of(le(propertyName, dateEnd));
			case GREATERTHAN_OR_EQUALS:
			case GREATERTHAN:
				return Optional.of(ge(propertyName, dateStart));
			case STARTS_AFTER:
				return Optional.of(gt(propertyName, dateEnd));
			case ENDS_BEFORE:
				return Optional.of(lt(propertyName, dateStart));
		}
		
		return Optional.empty();
	}
	
	protected Optional<Criterion> handleQuantity(String propertyName, QuantityParam quantityParam) {
		if (quantityParam == null) {
			return Optional.empty();
		}
		
		BigDecimal value = quantityParam.getValue();
		if (quantityParam.getPrefix() == null || quantityParam.getPrefix() == ParamPrefixEnum.APPROXIMATE) {
			String plainString = quantityParam.getValue().toPlainString();
			int dotIdx = plainString.indexOf('.');
			
			BigDecimal approxRange = APPROX_RANGE.multiply(value);
			if (dotIdx == -1) {
				return Optional.of(
				    between(propertyName, value.subtract(approxRange).doubleValue(), value.add(approxRange).doubleValue()));
			} else {
				int precision = plainString.length() - (dotIdx);
				double mul = Math.pow(10, -precision);
				double val = mul * 5.0d;
				return Optional.of(between(propertyName, value.subtract(new BigDecimal(val)).doubleValue(),
				    value.add(new BigDecimal(val)).doubleValue()));
			}
		} else {
			double val = value.doubleValue();
			switch (quantityParam.getPrefix()) {
				case EQUAL:
					return Optional.of(eq(propertyName, val));
				case NOT_EQUAL:
					return Optional.of(ne(propertyName, val));
				case LESSTHAN_OR_EQUALS:
					return Optional.of(le(propertyName, val));
				case LESSTHAN:
					return Optional.of(lt(propertyName, val));
				case GREATERTHAN_OR_EQUALS:
					return Optional.of(ge(propertyName, val));
				case GREATERTHAN:
					return Optional.of(gt(propertyName, val));
			}
		}
		
		return Optional.empty();
	}
	
	protected Optional<Criterion> handleQuantity(@Nonnull String propertyName, QuantityAndListParam quantityAndListParam) {
		if (quantityAndListParam == null) {
			return Optional.empty();
		}
		
		return handleAndListParam(quantityAndListParam, quantityParam -> handleQuantity(propertyName, quantityParam));
	}
	
	protected Optional<Criterion> handleEncounterReference(@Nonnull String encounterAlias,
	        ReferenceAndListParam encounterReference) {
		if (encounterReference == null) {
			return Optional.empty();
		}
		
		return handleAndListParam(encounterReference,
		    token -> Optional.of(eq(String.format("%s.uuid", encounterAlias), token.getIdPart())));
	}
	
	protected Optional<Criterion> handleGender(@Nonnull String propertyName, TokenAndListParam gender) {
		if (gender == null) {
			return Optional.empty();
		}
		
		return handleAndListParam(gender, token -> {
			try {
				AdministrativeGender administrativeGender = AdministrativeGender.fromCode(token.getValue());
				
				if (administrativeGender == null) {
					return Optional.of(isNull(propertyName));
				}
				
				switch (administrativeGender) {
					case MALE:
						return Optional.of(ilike(propertyName, "M", MatchMode.EXACT));
					case FEMALE:
						return Optional.of(ilike(propertyName, "F", MatchMode.EXACT));
					case OTHER:
					case UNKNOWN:
					case NULL:
						return Optional.of(isNull(propertyName));
				}
			}
			catch (FHIRException ignored) {}
			
			return Optional.of(ilike(propertyName, token.getValue(), MatchMode.EXACT));
		});
	}
	
	protected Optional<Criterion> handleLocationReference(@Nonnull String locationAlias,
	        ReferenceAndListParam locationReference) {
		if (locationReference == null) {
			return Optional.empty();
		}
		
		return handleAndListParam(locationReference, token -> {
			if (token.getChain() != null) {
				switch (token.getChain()) {
					case Location.SP_NAME:
						return propertyLike(String.format("%s.name", locationAlias), token.getValue());
					case Location.SP_ADDRESS_CITY:
						return propertyLike(String.format("%s.cityVillage", locationAlias), token.getValue());
					case Location.SP_ADDRESS_STATE:
						return propertyLike(String.format("%s.stateProvince", locationAlias), token.getValue());
					case Location.SP_ADDRESS_POSTALCODE:
						return propertyLike(String.format("%s.postalCode", locationAlias), token.getValue());
					case Location.SP_ADDRESS_COUNTRY:
						return propertyLike(String.format("%s.country", locationAlias), token.getValue());
				}
			} else {
				return Optional.of(eq(String.format("%s.uuid", locationAlias), token.getValue()));
			}
			
			return Optional.empty();
		});
		
	}
	
	protected void handleParticipantReference(Criteria criteria, ReferenceAndListParam participantReference) {
		if (participantReference != null && lacksAlias(criteria, "ep")) {
			criteria.createAlias("encounterProviders", "ep");
			
			handleAndListParam(participantReference, participantToken -> {
				if (participantToken.getChain() != null) {
					switch (participantToken.getChain()) {
						case Practitioner.SP_IDENTIFIER:
							if (lacksAlias(criteria, "p")) {
								criteria.createAlias("ep.provider", "p");
							}
							return Optional.of(ilike("p.identifier", participantToken.getValue()));
						case Practitioner.SP_GIVEN:
							if ((lacksAlias(criteria, "pro")
							        && (lacksAlias(criteria, "ps") && (lacksAlias(criteria, "pn"))))) {
								criteria.createAlias("ep.provider", "pro").createAlias("pro.person", "ps")
								        .createAlias("ps.names", "pn");
							}
							return Optional.of(ilike("pn.givenName", participantToken.getValue(), MatchMode.START));
						case Practitioner.SP_FAMILY:
							if ((lacksAlias(criteria, "pro")
							        && (lacksAlias(criteria, "ps") && (lacksAlias(criteria, "pn"))))) {
								criteria.createAlias("ep.provider", "pro").createAlias("pro.person", "ps")
								        .createAlias("ps.names", "pn");
							}
							return Optional.of(ilike("pn.familyName", participantToken.getValue(), MatchMode.START));
						case Practitioner.SP_NAME:
							if ((lacksAlias(criteria, "pro")
							        && (lacksAlias(criteria, "ps") && (lacksAlias(criteria, "pn"))))) {
								criteria.createAlias("ep.provider", "pro").createAlias("pro.person", "ps")
								        .createAlias("ps.names", "pn");
							}
							
							List<Optional<Criterion>> criterionList = new ArrayList<>();
							
							for (String token : StringUtils.split(participantToken.getValue(), " \t,")) {
								criterionList.add(propertyLike("pn.givenName", token));
								criterionList.add(propertyLike("pn.middleName", token));
								criterionList.add(propertyLike("pn.familyName", token));
							}
							
							return Optional.of(or(toCriteriaArray(criterionList)));
					}
				} else {
					if (lacksAlias(criteria, "pro")) {
						criteria.createAlias("ep.provider", "pro");
					}
					return Optional.of(eq("pro.uuid", participantToken.getValue()));
				}
				
				return Optional.empty();
			}).ifPresent(criteria::add);
			
		}
	}
	
	//Added this method to allow handling classes with provider instead  of encounterProvider
	protected void handleProviderReference(Criteria criteria, ReferenceAndListParam providerReference) {
		if (providerReference != null) {
			criteria.createAlias("orderer", "or");
			
			handleAndListParam(providerReference, participantToken -> {
				if (participantToken.getChain() != null) {
					switch (participantToken.getChain()) {
						case Practitioner.SP_IDENTIFIER:
							return Optional.of(ilike("or.identifier", participantToken.getValue()));
						case Practitioner.SP_GIVEN:
							if ((lacksAlias(criteria, "ps") && (lacksAlias(criteria, "pn")))) {
								criteria.createAlias("or.person", "ps").createAlias("ps.names", "pn");
							}
							return Optional.of(ilike("pn.givenName", participantToken.getValue(), MatchMode.START));
						case Practitioner.SP_FAMILY:
							if ((lacksAlias(criteria, "ps") && (lacksAlias(criteria, "pn")))) {
								criteria.createAlias("or.person", "ps").createAlias("ps.names", "pn");
							}
							return Optional.of(ilike("pn.familyName", participantToken.getValue(), MatchMode.START));
						case Practitioner.SP_NAME:
							if ((lacksAlias(criteria, "ps") && (lacksAlias(criteria, "pn")))) {
								criteria.createAlias("or.person", "ps").createAlias("ps.names", "pn");
							}
							
							List<Optional<Criterion>> criterionList = new ArrayList<>();
							
							for (String token : StringUtils.split(participantToken.getValue(), " \t,")) {
								criterionList.add(propertyLike("pn.givenName", token));
								criterionList.add(propertyLike("pn.middleName", token));
								criterionList.add(propertyLike("pn.familyName", token));
							}
							
							return Optional.of(or(toCriteriaArray(criterionList)));
					}
				} else {
					return Optional.of(eq("or.uuid", participantToken.getValue()));
				}
				
				return Optional.empty();
			}).ifPresent(criteria::add);
		}
	}
	
	protected Optional<Criterion> handleCodeableConcept(Criteria criteria, TokenAndListParam concepts,
	        @Nonnull String conceptAlias, @Nonnull String conceptMapAlias, @Nonnull String conceptReferenceTermAlias) {
		if (concepts == null) {
			return Optional.empty();
		}
		
		return handleAndListParamBySystem(concepts, (system, tokens) -> {
			if (system.isEmpty()) {
				return Optional.of(or(
				    in(String.format("%s.conceptId", conceptAlias),
				        tokensToParams(tokens).map(NumberUtils::toInt).collect(Collectors.toList())),
				    in(String.format("%s.uuid", conceptAlias), tokensToList(tokens))));
			} else {
				if (lacksAlias(criteria, conceptMapAlias)) {
					criteria.createAlias(String.format("%s.conceptMappings", conceptAlias), conceptMapAlias).createAlias(
					    String.format("%s.conceptReferenceTerm", conceptMapAlias), conceptReferenceTermAlias);
				}
				
				return Optional.of(generateSystemQuery(system, tokensToList(tokens), conceptReferenceTermAlias));
			}
		});
	}
	
	protected void handleNames(Criteria criteria, StringAndListParam name, StringAndListParam given,
	        StringAndListParam family) {
		handleNames(criteria, name, given, family, null);
	}
	
	protected void handleNames(Criteria criteria, StringAndListParam name, StringAndListParam given,
	        StringAndListParam family, String personAlias) {
		if (name == null && given == null && family == null) {
			return;
		}
		
		if (lacksAlias(criteria, "pn")) {
			if (StringUtils.isNotBlank(personAlias)) {
				criteria.createAlias(String.format("%s.names", personAlias), "pn");
			} else {
				criteria.createAlias("names", "pn");
			}
		}
		
		if (name != null) {
			handleAndListParamAsStream(name,
			    (nameParam) -> Arrays.stream(StringUtils.split(nameParam.getValue(), " \t,"))
			            .map(token -> new StringParam().setValue(token).setExact(nameParam.isExact())
			                    .setContains(nameParam.isContains()))
			            .map(tokenParam -> Arrays.asList(propertyLike("pn.givenName", tokenParam),
			                propertyLike("pn.middleName", tokenParam), propertyLike("pn.familyName", tokenParam)))
			            .flatMap(Collection::stream)).ifPresent(criteria::add);
		}
		
		if (given != null) {
			handleAndListParam(given, (givenName) -> propertyLike("pn.givenName", givenName)).ifPresent(criteria::add);
		}
		
		if (family != null) {
			handleAndListParam(family, (familyName) -> propertyLike("pn.familyName", familyName)).ifPresent(criteria::add);
		}
	}
	
	protected void handlePatientReference(Criteria criteria, ReferenceAndListParam patientReference) {
		handlePatientReference(criteria, patientReference, "patient");
	}
	
	protected void handlePatientReference(Criteria criteria, ReferenceAndListParam patientReference,
	        String associationPath) {
		if (patientReference != null) {
			criteria.createAlias(associationPath, "p");
			
			handleAndListParam(patientReference, patientToken -> {
				if (patientToken.getChain() != null) {
					switch (patientToken.getChain()) {
						case Patient.SP_IDENTIFIER:
							if (lacksAlias(criteria, "pi")) {
								criteria.createAlias("p.identifiers", "pi");
							}
							return Optional.of(ilike("pi.identifier", patientToken.getValue()));
						case Patient.SP_GIVEN:
							if (lacksAlias(criteria, "pn")) {
								criteria.createAlias("p.names", "pn");
							}
							return Optional.of(ilike("pn.givenName", patientToken.getValue(), MatchMode.START));
						case Patient.SP_FAMILY:
							if (lacksAlias(criteria, "pn")) {
								criteria.createAlias("p.names", "pn");
							}
							return Optional.of(ilike("pn.familyName", patientToken.getValue(), MatchMode.START));
						case Patient.SP_NAME:
							if (lacksAlias(criteria, "pn")) {
								criteria.createAlias("p.names", "pn");
							}
							List<Optional<Criterion>> criterionList = new ArrayList<>();
							
							for (String token : StringUtils.split(patientToken.getValue(), " \t,")) {
								criterionList.add(propertyLike("pn.givenName", token));
								criterionList.add(propertyLike("pn.middleName", token));
								criterionList.add(propertyLike("pn.familyName", token));
							}
							
							return Optional.of(or(toCriteriaArray(criterionList)));
					}
				} else {
					return Optional.of(eq("p.uuid", patientToken.getValue()));
				}
				
				return Optional.empty();
			}).ifPresent(criteria::add);
		}
	}
	
	protected Optional<Criterion> handleCommonSearchParameters(List<PropParam<?>> theCommonParams) {
		List<Optional<Criterion>> criterionList = new ArrayList<>();
		
		for (PropParam<?> commonSearchParam : theCommonParams) {
			switch (commonSearchParam.getPropertyName()) {
				case FhirConstants.ID_PROPERTY:
					criterionList.add(handleAndListParam((TokenAndListParam) commonSearchParam.getParam(),
					    param -> Optional.of(eq("uuid", param.getValue()))));
					break;
				case FhirConstants.LAST_UPDATED_PROPERTY:
					criterionList.add(handleLastUpdated((DateRangeParam) commonSearchParam.getParam()));
					break;
			}
		}
		
		return Optional.of(and(toCriteriaArray(criterionList.stream())));
	}
	
	/**
	 * This function should be overridden by implementations. It is used to return a criterion for
	 * _lastUpdated from resources where there are multiple properties to be considered.
	 *
	 * @param param the DateRangeParam used to query for _lastUpdated
	 * @return an optional criterion for the query
	 */
	protected abstract Optional<Criterion> handleLastUpdated(DateRangeParam param);
	
	protected Optional<Criterion> handlePersonAddress(String aliasPrefix, StringAndListParam city, StringAndListParam state,
	        StringAndListParam postalCode, StringAndListParam country) {
		if (city == null && state == null && postalCode == null && country == null) {
			return Optional.empty();
		}
		
		List<Optional<Criterion>> criterionList = new ArrayList<>();
		
		if (city != null) {
			criterionList.add(handleAndListParam(city, c -> propertyLike(String.format("%s.cityVillage", aliasPrefix), c)));
		}
		
		if (state != null) {
			criterionList
			        .add(handleAndListParam(state, c -> propertyLike(String.format("%s.stateProvince", aliasPrefix), c)));
		}
		
		if (postalCode != null) {
			criterionList
			        .add(handleAndListParam(postalCode, c -> propertyLike(String.format("%s.postalCode", aliasPrefix), c)));
		}
		
		if (country != null) {
			criterionList.add(handleAndListParam(country, c -> propertyLike(String.format("%s.country", aliasPrefix), c)));
		}
		
		if (criterionList.size() == 0) {
			return Optional.empty();
		}
		
		return Optional.of(and(toCriteriaArray(criterionList.stream())));
	}
	
	protected Optional<Criterion> handleMedicationReference(@Nonnull String medicationAlias,
	        ReferenceAndListParam medicationReference) {
		if (medicationReference == null) {
			return Optional.empty();
		}
		
		return handleAndListParam(medicationReference,
		    token -> Optional.of(eq(String.format("%s.uuid", medicationAlias), token.getIdPart())));
	}
	
	/**
	 * Use this method to properly implement sorting for your query. Note that for this method to work,
	 * you must override one or more of: {@link #paramToProps(SortState)},
	 * {@link #paramToProps(String)}, or {@link #paramToProp(String)}.
	 *
	 * @param criteria the current criteria
	 * @param sort the {@link SortSpec} which defines the sorting to be translated
	 */
	protected void handleSort(Criteria criteria, SortSpec sort) {
		handleSort(criteria, sort, this::paramToProps).ifPresent(l -> l.forEach(criteria::addOrder));
	}
	
	protected Optional<List<Order>> handleSort(Criteria criteria, SortSpec sort,
	        Function<SortState, Collection<Order>> paramToProp) {
		List<Order> orderings = new ArrayList<>();
		SortSpec sortSpec = sort;
		while (sortSpec != null) {
			SortState state = SortState.builder().criteria(criteria).sortOrder(sortSpec.getOrder())
			        .parameter(sortSpec.getParamName().toLowerCase()).build();
			
			Collection<Order> orders = paramToProp.apply(state);
			if (orders != null) {
				orderings.addAll(orders);
			}
			
			sortSpec = sortSpec.getChain();
		}
		
		if (orderings.size() == 0) {
			return Optional.empty();
		}
		
		return Optional.of(orderings);
	}
	
	protected Criterion generateSystemQuery(String system, List<String> codes, String conceptReferenceTermAlias) {
		DetachedCriteria conceptSourceCriteria = DetachedCriteria.forClass(FhirConceptSource.class).add(eq("url", system))
		        .setProjection(property("conceptSource"));
		
		if (codes.size() > 1) {
			return and(propertyEq(String.format("%s.conceptSource", conceptReferenceTermAlias), conceptSourceCriteria),
			    in(String.format("%s.code", conceptReferenceTermAlias), codes));
		} else {
			return and(propertyEq(String.format("%s.conceptSource", conceptReferenceTermAlias), conceptSourceCriteria),
			    eq(String.format("%s.code", conceptReferenceTermAlias), codes.get(0)));
		}
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
	protected Collection<Order> paramToProps(@Nonnull SortState sortState) {
		Collection<String> prop = paramToProps(sortState.getParameter());
		
		if (prop != null) {
			switch (sortState.getSortOrder()) {
				case ASC:
					return prop.stream().map(Order::asc).collect(Collectors.toList());
				case DESC:
					return prop.stream().map(Order::desc).collect(Collectors.toList());
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
	protected Collection<String> paramToProps(@Nonnull String param) {
		String prop = paramToProp(param);
		
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
	protected String paramToProp(@Nonnull String param) {
		return null;
	}
	
	protected Optional<Criterion> propertyLike(@Nonnull String propertyName, String value) {
		if (value == null) {
			return Optional.empty();
		}
		
		return propertyLike(propertyName, new StringParam(value));
	}
	
	protected Optional<Criterion> propertyLike(@Nonnull String propertyName, StringParam param) {
		if (param == null) {
			return Optional.empty();
		}
		
		if (param.isExact()) {
			return Optional.of(ilike(propertyName, param.getValue(), MatchMode.EXACT));
		} else if (param.isContains()) {
			return Optional.of(ilike(propertyName, param.getValue(), MatchMode.ANYWHERE));
		}
		
		return Optional.of(ilike(propertyName, param.getValue(), MatchMode.START));
	}
	
	protected Optional<CriteriaImpl> asImpl(Criteria criteria) {
		if (CriteriaImpl.class.isAssignableFrom(criteria.getClass())) {
			return Optional.of((CriteriaImpl) criteria);
		} else if (CriteriaImpl.Subcriteria.class.isAssignableFrom(criteria.getClass())) {
			return Optional.of((CriteriaImpl) ((CriteriaImpl.Subcriteria) criteria).getParent());
		}
		
		return Optional.empty();
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
	protected final Criterion[] toCriteriaArray(Optional<Criterion>... criteria) {
		return toCriteriaArray(Arrays.stream(criteria));
	}
	
	protected Criterion[] toCriteriaArray(Collection<Optional<Criterion>> collection) {
		return toCriteriaArray(collection.stream());
	}
	
	protected Criterion[] toCriteriaArray(Stream<Optional<Criterion>> criteriaStream) {
		return criteriaStream.filter(Optional::isPresent).map(Optional::get).toArray(Criterion[]::new);
	}
	
	/**
	 * This object is used to store the state of the sorting
	 */
	@Data
	@Builder
	@EqualsAndHashCode
	public static final class SortState {
		
		private Criteria criteria;
		
		private SortOrderEnum sortOrder;
		
		private String parameter;
	}
	
	protected Optional<Criterion> handleAgeByDateProperty(@Nonnull String datePropertyName, @Nonnull QuantityParam age) {
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
				return Optional.of(and(ge(datePropertyName, lowerBound), le(datePropertyName, upperBound)));
			} else {
				return Optional.of(not(and(ge(datePropertyName, lowerBound), le(datePropertyName, upperBound))));
			}
		}
		
		switch (prefix) {
			case LESSTHAN_OR_EQUALS:
			case LESSTHAN:
			case STARTS_AFTER:
				return Optional
				        .of(ge(datePropertyName, Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant())));
			case GREATERTHAN_OR_EQUALS:
			case GREATERTHAN:
				return Optional
				        .of(le(datePropertyName, Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant())));
			// Ignoring ENDS_BEFORE as it is not meaningful for age.
		}
		
		return Optional.empty();
	}
	
}
