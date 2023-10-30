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
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.metamodel.EntityType;

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
import org.hibernate.Criteria;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Order;
import org.hibernate.internal.CriteriaImpl;
import org.hibernate.sql.JoinType;
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
	 * Determines whether or not the given criteria object already has a given alias. This is useful to
	 * determine whether a mapping has already been made or whether a given alias is already in use.
	 *
	 * @param criteriaBuilder the {@link CriteriaBuilder} object to examine
	 * @param alias the alias to look for
	 * @return true if the alias exists in this criteria object, false otherwise
	 */
	protected boolean lacksAlias(CriteriaBuilder criteriaBuilder, String alias) {
		CriteriaQuery<Long> query = criteriaBuilder.createQuery(Long.class);
		Root<?> root = query.from(Long.class);
		Root<?> subqueryRoot = query.from(root.getJavaType());
		Expression<Long> countExpression = criteriaBuilder.count(subqueryRoot.get(alias));
		Predicate aliasNotExistsPredicate = criteriaBuilder.equal(countExpression, 0L);
		
		query.select(countExpression).where(aliasNotExistsPredicate);
		
		return criteriaBuilder.createQuery(Long.class).select(criteriaBuilder.literal(1L)).where(aliasNotExistsPredicate)
		        .getSelection().getJavaType() == Long.class;
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
	protected <T extends IQueryParameterOr<U>, U extends IQueryParameterType> Optional<Predicate> handleAndListParam(
	        IQueryParameterAnd<T> andListParam, Function<U, Optional<Predicate>> handler) {
		if (andListParam == null) {
			return Optional.empty();
		}
		
		EntityManager entityManager = sessionFactory.getCurrentSession();
		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		
		return Optional.ofNullable(criteriaBuilder.and(
		    toCriteriaArray(handleAndListParam(andListParam).map(orListParam -> handleOrListParam(orListParam, handler)))));
	}
	
	@SuppressWarnings("unused")
	protected <T extends IQueryParameterOr<U>, U extends IQueryParameterType> Optional<Predicate> handleAndListParamBy(
	        IQueryParameterAnd<T> andListParam, Function<IQueryParameterOr<U>, Optional<Predicate>> handler) {
		if (andListParam == null) {
			return Optional.empty();
		}
		
		EntityManager entityManager = sessionFactory.getCurrentSession();
		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		
		return Optional.of(criteriaBuilder.and((toCriteriaArray(handleAndListParam(andListParam).map(handler)))));
	}
	
	protected <T extends IQueryParameterOr<U>, U extends IQueryParameterType> Optional<Predicate> handleAndListParamAsStream(
	        IQueryParameterAnd<T> andListParam, Function<U, Stream<Optional<Predicate>>> handler) {
		if (andListParam == null) {
			return Optional.empty();
		}
		
		EntityManager entityManager = sessionFactory.getCurrentSession();
		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		
		return Optional.of(criteriaBuilder.and((toCriteriaArray(
		    handleAndListParam(andListParam).map(orListParam -> handleOrListParamAsStream(orListParam, handler))))));
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
	protected <T extends IQueryParameterType> Optional<Predicate> handleOrListParam(IQueryParameterOr<T> orListParam,
	        Function<T, Optional<Predicate>> handler) {
		if (orListParam == null) {
			return Optional.empty();
		}
		
		EntityManager entityManager = sessionFactory.getCurrentSession();
		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		
		return Optional.of(criteriaBuilder.or(toCriteriaArray(handleOrListParam(orListParam).map(handler))));
	}
	
	protected <T extends IQueryParameterType> Optional<Predicate> handleOrListParamAsStream(IQueryParameterOr<T> orListParam,
	        Function<T, Stream<Optional<Predicate>>> handler) {
		if (orListParam == null) {
			return Optional.empty();
		}
		
		EntityManager entityManager = sessionFactory.getCurrentSession();
		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		
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
	        IQueryParameterAnd<T> andListParam,
	        BiFunction<String, List<TokenParam>, Optional<Predicate>> systemTokenHandler) {
		if (andListParam == null) {
			return Optional.empty();
		}
		
		EntityManager entityManager = sessionFactory.getCurrentSession();
		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		Predicate predicate = criteriaBuilder.and(toCriteriaArray(
		    handleAndListParam(andListParam).map(param -> handleOrListParamBySystem(param, systemTokenHandler))));
		
		return Optional.of(predicate);
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
	protected Optional<Predicate> handleOrListParamBySystem(IQueryParameterOr<TokenParam> orListParam,
	        BiFunction<String, List<TokenParam>, Optional<Predicate>> systemTokenHandler) {
		
		if (orListParam == null) {
			return Optional.empty();
		}
		
		EntityManager entityManager = sessionFactory.getCurrentSession();
		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		Predicate predicate = criteriaBuilder
		        .or(toCriteriaArray(handleOrListParam(orListParam).collect(Collectors.groupingBy(this::groupBySystem))
		                .entrySet().stream().map(e -> systemTokenHandler.apply(e.getKey(), e.getValue()))));
		
		return Optional.of(predicate);
	}
	
	/**
	 * Handler for a {@link TokenOrListParam} that represents boolean values
	 *
	 * @param propertyName the name of the property in the query to use
	 * @param booleanToken the {@link TokenOrListParam} to handle
	 * @return a {@link Criterion} to be added to the query indicating that the property matches the
	 *         given value
	 */
	protected Optional<Predicate> handleBoolean(String propertyName, TokenAndListParam booleanToken) {
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
	
	protected Optional<Predicate> handleBooleanProperty(String propertyName, boolean booleanVal) {
		EntityManager entityManager = sessionFactory.getCurrentSession();
		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<Object> criteriaQuery = criteriaBuilder.createQuery();
		Root<?> root = criteriaQuery.from(Object.class);
		return Optional.of(criteriaBuilder.equal(root.get(propertyName), booleanVal));
	}
	
	/**
	 * A handler for a {@link DateRangeParam}, which represents an inclusive set of {@link DateParam}s
	 *
	 * @param propertyName the name of the property in the query to use
	 * @param dateRangeParam the {@link DateRangeParam} to handle
	 * @return a {@link Criterion} to be added to the query for the indicated date range
	 */
	protected Optional<Predicate> handleDateRange(String propertyName, DateRangeParam dateRangeParam) {
		if (dateRangeParam == null) {
			return Optional.empty();
		}
		
		EntityManager entityManager = sessionFactory.getCurrentSession();
		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<EntityType> criteriaQuery = criteriaBuilder.createQuery(EntityType.class);
		Root<EntityType> root = criteriaQuery.from(EntityType.class);
		
		Predicate predicate = criteriaBuilder
		        .and(toCriteriaArray(Stream.of(handleDate(propertyName, dateRangeParam.getLowerBound()),
		            handleDate(propertyName, dateRangeParam.getUpperBound()))));
		
		return Optional.ofNullable(predicate);
	}
	
	/**
	 * A handler for a {@link DateParam}, which represents a day and an comparator
	 *
	 * @param propertyName the name of the property in the query to use
	 * @param dateParam the {@link DateParam} to handle
	 * @return a {@link Predicate} to be added to the query for the indicate date param
	 */
	protected Optional<Predicate> handleDate(String propertyName, DateParam dateParam) {
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
		
		EntityManager entityManager = sessionFactory.getCurrentSession();
		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<EntityType> criteriaQuery = criteriaBuilder.createQuery(EntityType.class);
		Root<EntityType> root = criteriaQuery.from(EntityType.class);
		
		// TODO This does not properly handle FHIR Periods and Timings, though its unclear if we are using those
		// see https://www.hl7.org/fhir/search.html#date
		switch (dateParam.getPrefix()) {
			case EQUAL:
				return Optional
				        .of(criteriaBuilder.and(criteriaBuilder.greaterThanOrEqualTo(root.get(propertyName), dateStart),
				            criteriaBuilder.lessThan(root.get(propertyName), dateEnd)));
			case NOT_EQUAL:
				return Optional.of(criteriaBuilder
				        .not(criteriaBuilder.and(criteriaBuilder.greaterThanOrEqualTo(root.get(propertyName), dateStart),
				            criteriaBuilder.lessThan(root.get(propertyName), dateEnd))));
			case LESSTHAN_OR_EQUALS:
			case LESSTHAN:
				return Optional.of(criteriaBuilder.lessThanOrEqualTo(root.get(propertyName), dateEnd));
			case GREATERTHAN_OR_EQUALS:
			case GREATERTHAN:
				return Optional.of(criteriaBuilder.greaterThanOrEqualTo(root.get(propertyName), dateStart));
			case STARTS_AFTER:
				return Optional.of(criteriaBuilder.greaterThan(root.get(propertyName), dateEnd));
			case ENDS_BEFORE:
				return Optional.of(criteriaBuilder.lessThan(root.get(propertyName), dateEnd));
		}
		
		return Optional.empty();
	}
	
	protected Optional<Predicate> handleQuantity(String propertyName, QuantityParam quantityParam) {
		if (quantityParam == null) {
			return Optional.empty();
		}
		
		EntityManager entityManager = sessionFactory.getCurrentSession();
		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<EntityType> criteriaQuery = criteriaBuilder.createQuery(EntityType.class);
		Root<EntityType> root = criteriaQuery.from(EntityType.class);
		
		BigDecimal value = quantityParam.getValue();
		if (quantityParam.getPrefix() == null || quantityParam.getPrefix() == ParamPrefixEnum.APPROXIMATE) {
			String plainString = quantityParam.getValue().toPlainString();
			int dotIdx = plainString.indexOf('.');
			
			BigDecimal approxRange = APPROX_RANGE.multiply(value);
			if (dotIdx == -1) {
				double lowerBound = value.subtract(approxRange).doubleValue();
				double upperBound = value.add(approxRange).doubleValue();
				return Optional.of(criteriaBuilder.between(root.get(propertyName), lowerBound, upperBound));
			} else {
				int precision = plainString.length() - (dotIdx);
				double mul = Math.pow(10, -precision);
				double val = mul * 5.0d;
				double lowerBound = value.subtract(new BigDecimal(val)).doubleValue();
				double upperBound = value.add(new BigDecimal(val)).doubleValue();
				return Optional.of(criteriaBuilder.between(root.get(propertyName), lowerBound, upperBound));
			}
		} else {
			double val = value.doubleValue();
			switch (quantityParam.getPrefix()) {
				case EQUAL:
					return Optional.of(criteriaBuilder.equal(root.get(propertyName), val));
				case NOT_EQUAL:
					return Optional.of(criteriaBuilder.notEqual(root.get(propertyName), val));
				case LESSTHAN_OR_EQUALS:
					return Optional.of(criteriaBuilder.lessThanOrEqualTo(root.get(propertyName), val));
				case LESSTHAN:
					return Optional.of(criteriaBuilder.lessThan(root.get(propertyName), val));
				case GREATERTHAN_OR_EQUALS:
					return Optional.of(criteriaBuilder.greaterThanOrEqualTo(root.get(propertyName), val));
				case GREATERTHAN:
					return Optional.of(criteriaBuilder.greaterThan(root.get(propertyName), val));
			}
		}
		
		return Optional.empty();
	}
	
	protected Optional<Predicate> handleQuantity(@Nonnull String propertyName, QuantityAndListParam quantityAndListParam) {
		if (quantityAndListParam == null) {
			return Optional.empty();
		}
		
		return handleAndListParam(quantityAndListParam, quantityParam -> handleQuantity(propertyName, quantityParam));
	}
	
	protected void handleEncounterReference(CriteriaBuilder criteria, ReferenceAndListParam encounterReference,
	        @Nonnull String encounterAlias) {
		handleEncounterReference(criteria, encounterReference, encounterAlias, "encounter");
	}
	
	protected void handleEncounterReference(CriteriaBuilder criteriaBuilder, ReferenceAndListParam encounterReference,
	        @Nonnull String encounterAlias, @Nonnull String associationPath) {
		EntityManager entityManager = sessionFactory.getCurrentSession();
		criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<EntityType> criteriaQuery = criteriaBuilder.createQuery(EntityType.class);
		Root<EntityType> root = criteriaQuery.from(EntityType.class);
		
		if (encounterReference == null) {
			return;
		}
		
		if (lacksAlias(criteriaBuilder, encounterAlias)) {
			root.join(associationPath).alias(encounterAlias);
		}
		
		CriteriaBuilder finalCriteriaBuilder = criteriaBuilder;
		handleAndListParam(encounterReference, token -> {
			if (token.getChain() != null) {
				switch (token.getChain()) {
					case Encounter.SP_TYPE:
						if (lacksAlias(finalCriteriaBuilder, "et")) {
							root.join(String.format("%s.encounterType", encounterAlias)).alias("et");
						}
						return propertyLike("et.uuid", new StringParam(token.getValue(), true));
				}
			} else {
				return Optional.of(
				    finalCriteriaBuilder.equal(root.get(String.format("%s.uuid", encounterAlias)), token.getIdPart()));
			}
			
			return Optional.empty();
		}).ifPresent(criteriaQuery::where);
	}
	
	protected Optional<Predicate> handleGender(@Nonnull String propertyName, TokenAndListParam gender) {
		EntityManager entityManager = sessionFactory.getCurrentSession();
		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<EntityType> criteriaQuery = criteriaBuilder.createQuery(EntityType.class);
		Root<EntityType> root = criteriaQuery.from(EntityType.class);
		
		if (gender == null) {
			return Optional.empty();
		}
		
		return handleAndListParam(gender, token -> {
			try {
				AdministrativeGender administrativeGender = AdministrativeGender.fromCode(token.getValue());
				
				if (administrativeGender == null) {
					return Optional.of(criteriaBuilder.isNull(root.get(propertyName)));
				}
				
				switch (administrativeGender) {
					case MALE:
						return Optional.of(criteriaBuilder.like(root.get(propertyName), "M"));
					case FEMALE:
						return Optional.of(criteriaBuilder.like(root.get(propertyName), "F"));
					case OTHER:
					case UNKNOWN:
					case NULL:
						return Optional.of(criteriaBuilder.isNull(root.get(propertyName)));
				}
			}
			catch (FHIRException ignored) {}
			return Optional.of(criteriaBuilder.like(root.get(propertyName), token.getValue()));
		});
	}
	
	protected Optional<Predicate> handleLocationReference(@Nonnull String locationAlias,
	        ReferenceAndListParam locationReference) {
		EntityManager entityManager = sessionFactory.getCurrentSession();
		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<EntityType> criteriaQuery = criteriaBuilder.createQuery(EntityType.class);
		Root<EntityType> root = criteriaQuery.from(EntityType.class);
		
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
				return Optional
				        .of(criteriaBuilder.equal(root.get(String.format("%s.uuid", locationAlias)), token.getValue()));
			}
			
			return Optional.empty();
		});
	}
	
	protected void handleParticipantReference(CriteriaBuilder criteriaBuilder, ReferenceAndListParam participantReference) {
		if (participantReference != null) {
			
			EntityManager entityManager = sessionFactory.getCurrentSession();
			criteriaBuilder = entityManager.getCriteriaBuilder();
			CriteriaQuery<Object> criteriaQuery = criteriaBuilder.createQuery(Object.class);
			Root<?> root = criteriaQuery.from(Object.class);
			
			if (lacksAlias(criteriaBuilder, "ep")) {
				return;
			}
			
			CriteriaBuilder finalCriteriaBuilder = criteriaBuilder;
			handleAndListParam(participantReference, participantToken -> {
				if (participantToken.getChain() != null) {
					switch (participantToken.getChain()) {
						case Practitioner.SP_IDENTIFIER:
							if (lacksAlias(finalCriteriaBuilder, "p")) {
								root.join("ep.provider").alias("p");
							}
							return Optional
							        .of(finalCriteriaBuilder.like(root.get("p.identifier"), participantToken.getValue()));
						case Practitioner.SP_GIVEN:
							if ((lacksAlias(finalCriteriaBuilder, "pro") && (lacksAlias(finalCriteriaBuilder, "ps")
							        && (lacksAlias(finalCriteriaBuilder, "pn"))))) {
								root.join("ep.provider").alias("pro");
								root.join("pro.person").alias("ps");
								root.join("ps.names").alias("pn");
							}
							return Optional
							        .of(finalCriteriaBuilder.like(root.get("pn.givenName"), participantToken.getValue()));
						case Practitioner.SP_FAMILY:
							if ((lacksAlias(finalCriteriaBuilder, "pro") && (lacksAlias(finalCriteriaBuilder, "ps")
							        && (lacksAlias(finalCriteriaBuilder, "pn"))))) {
								root.join("ep.provider").alias("pro");
								root.join("pro.person").alias("ps");
								root.join("ps.names").alias("pn");
							}
							return Optional
							        .of(finalCriteriaBuilder.like(root.get("pn.familyName"), participantToken.getValue()));
						case Practitioner.SP_NAME:
							if ((lacksAlias(finalCriteriaBuilder, "pro") && (lacksAlias(finalCriteriaBuilder, "ps")
							        && (lacksAlias(finalCriteriaBuilder, "pn"))))) {
								root.join("ep.provider").alias("pro");
								root.join("pro.person").alias("ps");
								root.join("ps.names").alias("pn");
							}
							
							List<Optional<? extends Predicate>> predicateList = new ArrayList<>();
							
							for (String token : StringUtils.split(participantToken.getValue(), " \t,")) {
								predicateList.add(propertyLike("pn.givenName", token));
								predicateList.add(propertyLike("pn.middleName", token));
								predicateList.add(propertyLike("pn.familyName", token));
							}
							
							return Optional.of(finalCriteriaBuilder.or(toCriteriaArray(predicateList)));
					}
				} else {
					if (lacksAlias(finalCriteriaBuilder, "pro")) {
						root.join("ep.provider").alias("pro");
					}
					return Optional.of(finalCriteriaBuilder.equal(root.get("pro.uuid"), participantToken.getValue()));
				}
				
				return Optional.empty();
			}).ifPresent(criteriaQuery::where);
		}
	}
	
	//Added this method to allow handling classes with provider instead  of encounterProvider
	protected void handleProviderReference(CriteriaBuilder criteriaBuilder, ReferenceAndListParam providerReference) {
		
		EntityManager entityManager = sessionFactory.getCurrentSession();
		criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<Object> criteriaQuery = criteriaBuilder.createQuery(Object.class);
		Root<?> root = criteriaQuery.from(Object.class);
		
		if (providerReference != null) {
			criteriaQuery.select(root.get("orderer").alias("or"));
			
			CriteriaBuilder finalCriteriaBuilder = criteriaBuilder;
			handleAndListParam(providerReference, participantToken -> {
				if (participantToken.getChain() != null) {
					switch (participantToken.getChain()) {
						case Practitioner.SP_IDENTIFIER:
							return Optional
							        .of(finalCriteriaBuilder.like(root.get("or.identifier"), participantToken.getValue()));
						case Practitioner.SP_GIVEN:
							if ((lacksAlias(finalCriteriaBuilder, "ps") && (lacksAlias(finalCriteriaBuilder, "pn")))) {
								root.join("or.person").alias("ps");
								root.join("ps.names").alias("pn");
							}
							return Optional
							        .of(finalCriteriaBuilder.like(root.get("pn.givenName"), participantToken.getValue()));
						case Practitioner.SP_FAMILY:
							if ((lacksAlias(finalCriteriaBuilder, "ps") && (lacksAlias(finalCriteriaBuilder, "pn")))) {
								root.join("or.person").alias("ps");
								root.join("ps.names").alias("pn");
							}
							return Optional
							        .of(finalCriteriaBuilder.like(root.get("pn.familyName"), participantToken.getValue()));
						case Practitioner.SP_NAME:
							if ((lacksAlias(finalCriteriaBuilder, "ps") && (lacksAlias(finalCriteriaBuilder, "pn")))) {
								root.join("or.person").alias("ps");
								root.join("ps.names").alias("pn");
							}
							
							List<Optional<? extends Predicate>> predicateList = new ArrayList<>();
							
							for (String token : StringUtils.split(participantToken.getValue(), " \t,")) {
								predicateList.add(propertyLike("pn.givenName", token));
								predicateList.add(propertyLike("pn.middleName", token));
								predicateList.add(propertyLike("pn.familyName", token));
							}
							
							return Optional.of(finalCriteriaBuilder.or(toCriteriaArray(predicateList)));
					}
				} else {
					return Optional.of(finalCriteriaBuilder.equal(root.get("ro.uuid"), participantToken.getValue()));
				}
				
				return Optional.empty();
			}).ifPresent(criteriaQuery::where);
		}
	}
	
	protected Optional<Predicate> handleCodeableConcept(CriteriaBuilder criteriaBuilder, TokenAndListParam concepts,
	        @Nonnull String conceptAlias, @Nonnull String conceptMapAlias, @Nonnull String conceptReferenceTermAlias) {
		EntityManager entityManager = sessionFactory.getCurrentSession();
		criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<Object> criteriaQuery = criteriaBuilder.createQuery(Object.class);
		Root<?> root = criteriaQuery.from(Object.class);
		
		if (concepts == null) {
			return Optional.empty();
		}
		
		CriteriaBuilder finalCriteriaBuilder = criteriaBuilder;
		return handleAndListParamBySystem(concepts, (system, tokens) -> {
			if (system.isEmpty()) {
				finalCriteriaBuilder.literal(tokensToParams(tokens).map(NumberUtils::toInt).collect(Collectors.toList()));
				return Optional
				        .of(finalCriteriaBuilder.or(
				            finalCriteriaBuilder.in(root.get(String.format("%s.conceptId", conceptAlias))
				                    .in(finalCriteriaBuilder.literal(
				                        tokensToParams(tokens).map(NumberUtils::toInt).collect(Collectors.toList())))),
				            finalCriteriaBuilder.in(root.get(String.format("%s.uuid", conceptAlias))
				                    .in(finalCriteriaBuilder.literal(tokensToList(tokens))))));
				
			} else {
				if (lacksAlias(finalCriteriaBuilder, conceptMapAlias)) {
					root.join(String.format("%s.conceptMappings", conceptAlias)).alias(conceptMapAlias);
					root.join(String.format("%s.conceptReferenceTerm", conceptMapAlias)).alias(conceptReferenceTermAlias);
				}
				
				return Optional.of(generateSystemQuery(system, tokensToList(tokens), conceptReferenceTermAlias));
			}
		});
	}
	
	protected void handleNames(CriteriaBuilder criteria, StringAndListParam name, StringAndListParam given,
	        StringAndListParam family) {
		handleNames(criteria, name, given, family, null);
	}
	
	protected void handleNames(CriteriaBuilder criteriaBuilder, StringAndListParam name, StringAndListParam given,
	        StringAndListParam family, String personAlias) {
		
		EntityManager entityManager = sessionFactory.getCurrentSession();
		criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<Object> criteriaQuery = criteriaBuilder.createQuery(Object.class);
		Root<?> root = criteriaQuery.from(Object.class);
		
		if (name == null && given == null && family == null) {
			return;
		}
		
		if (lacksAlias(criteriaBuilder, "pn")) {
			if (StringUtils.isNotBlank(personAlias)) {
				root.join(String.format("%s.names", personAlias), javax.persistence.criteria.JoinType.INNER).alias("pn");
				criteriaBuilder.equal(root.get("pn.voided"), false);
			} else {
				root.join("names", javax.persistence.criteria.JoinType.INNER).alias("pn");
				criteriaBuilder.equal(root.get("pn.voided"), false);
			}
		}
		
		if (name != null) {
			handleAndListParamAsStream(name,
			    (nameParam) -> Arrays.stream(StringUtils.split(nameParam.getValue(), " \t,"))
			            .map(token -> new StringParam().setValue(token).setExact(nameParam.isExact())
			                    .setContains(nameParam.isContains()))
			            .map(tokenParam -> Arrays.asList(propertyLike("pn.givenName", tokenParam),
			                propertyLike("pn.middleName", tokenParam), propertyLike("pn.familyName", tokenParam)))
			            .flatMap(Collection::stream)).ifPresent(criteriaQuery::where);
		}
		
		if (given != null) {
			handleAndListParam(given, (givenName) -> propertyLike("pn.givenName", givenName))
			        .ifPresent(criteriaQuery::where);
		}
		
		if (family != null) {
			handleAndListParam(family, (familyName) -> propertyLike("pn.familyName", familyName))
			        .ifPresent(criteriaQuery::where);
		}
	}
	
	protected void handlePatientReference(CriteriaBuilder criteriaBuilder, ReferenceAndListParam patientReference) {
		handlePatientReference(criteriaBuilder, patientReference, "patient");
	}
	
	protected void handlePatientReference(CriteriaBuilder criteriaBuilder, ReferenceAndListParam patientReference,
	        String associationPath) {
		
		EntityManager entityManager = sessionFactory.getCurrentSession();
		criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<Object> criteriaQuery = criteriaBuilder.createQuery(Object.class);
		Root<?> root = criteriaQuery.from(Object.class);
		
		if (patientReference != null) {
			criteriaQuery.select(root.get(associationPath).alias("p"));
			
			CriteriaBuilder finalCriteriaBuilder = criteriaBuilder;
			handleAndListParam(patientReference, patientToken -> {
				if (patientToken.getChain() != null) {
					switch (patientToken.getChain()) {
						case Patient.SP_IDENTIFIER:
							if (lacksAlias(finalCriteriaBuilder, "pi")) {
								criteriaQuery.select(root.get("p.identifiers").alias("pi"));
							}
							return Optional
							        .of(finalCriteriaBuilder.like(root.get("pi.identifier"), patientToken.getValue()));
						case Patient.SP_GIVEN:
							if (lacksAlias(finalCriteriaBuilder, "pn")) {
								criteriaQuery.select(root.get("p.names").alias("pn"));
							}
							//							return Optional.of(ilike("pn.givenName", patientToken.getValue(), MatchMode.START));
							return Optional.of(finalCriteriaBuilder.like(root.get("pi.givenName"), patientToken.getValue()));
						case Patient.SP_FAMILY:
							if (lacksAlias(finalCriteriaBuilder, "pn")) {
								criteriaQuery.select(root.get("p.names").alias("pn"));
							}
							//							return Optional.of(ilike("pn.familyName", patientToken.getValue(), MatchMode.START));
							return Optional
							        .of(finalCriteriaBuilder.like(root.get("pi.familyName"), patientToken.getValue()));
						case Patient.SP_NAME:
							if (lacksAlias(finalCriteriaBuilder, "pn")) {
								criteriaQuery.select(root.get("p.names").alias("pn"));
							}
							List<Optional<? extends Predicate>> criterionList = new ArrayList<>();
							
							for (String token : StringUtils.split(patientToken.getValue(), " \t,")) {
								criterionList.add(propertyLike("pn.givenName", token));
								criterionList.add(propertyLike("pn.middleName", token));
								criterionList.add(propertyLike("pn.familyName", token));
							}
							return Optional.of(finalCriteriaBuilder.or(toCriteriaArray(criterionList)));
					}
				} else {
					return Optional.of(finalCriteriaBuilder.equal(root.get("p.uuid"), patientToken.getValue()));
				}
				
				return Optional.empty();
			}).ifPresent(criteriaQuery::where);
		}
	}
	
	protected Optional<Predicate> handleCommonSearchParameters(List<PropParam<?>> theCommonParams) {
		
		EntityManager entityManager = sessionFactory.getCurrentSession();
		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<EntityType> criteriaQuery = criteriaBuilder.createQuery(EntityType.class);
		Root<?> root = criteriaQuery.from(EntityType.class);
		
		List<Optional<? extends Predicate>> criterionList = new ArrayList<>();
		
		for (PropParam<?> commonSearchParam : theCommonParams) {
			switch (commonSearchParam.getPropertyName()) {
				case FhirConstants.ID_PROPERTY:
					criterionList.add(handleAndListParam((TokenAndListParam) commonSearchParam.getParam(),
					    param -> Optional.of(criteriaBuilder.equal(root.get("uuid"), param.getValue()))));
					break;
				case FhirConstants.LAST_UPDATED_PROPERTY:
					criterionList.add(handleLastUpdated((DateRangeParam) commonSearchParam.getParam()));
					break;
			}
		}
		return Optional.of(criteriaBuilder.and(toCriteriaArray(criterionList.stream())));
	}
	
	/**
	 * This function should be overridden by implementations. It is used to return a criterion for
	 * _lastUpdated from resources where there are multiple properties to be considered.
	 *
	 * @param param the DateRangeParam used to query for _lastUpdated
	 * @return an optional criterion for the query
	 */
	protected abstract Optional<Predicate> handleLastUpdated(DateRangeParam param);
	
	protected Optional<Predicate> handlePersonAddress(String aliasPrefix, StringAndListParam city, StringAndListParam state,
	        StringAndListParam postalCode, StringAndListParam country) {
		if (city == null && state == null && postalCode == null && country == null) {
			return Optional.empty();
		}
		
		List<Optional<? extends Predicate>> predicateList = new ArrayList<>();
		
		if (city != null) {
			predicateList.add(handleAndListParam(city, c -> propertyLike(String.format("%s.cityVillage", aliasPrefix), c)));
		}
		
		if (state != null) {
			predicateList
			        .add(handleAndListParam(state, c -> propertyLike(String.format("%s.stateProvince", aliasPrefix), c)));
		}
		
		if (postalCode != null) {
			predicateList
			        .add(handleAndListParam(postalCode, c -> propertyLike(String.format("%s.postalCode", aliasPrefix), c)));
		}
		
		if (country != null) {
			predicateList.add(handleAndListParam(country, c -> propertyLike(String.format("%s.country", aliasPrefix), c)));
		}
		
		EntityManager entityManager = sessionFactory.getCurrentSession();
		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<EntityType> criteriaQuery = criteriaBuilder.createQuery(EntityType.class);
		Predicate predicate = criteriaBuilder.and(toCriteriaArray(predicateList.stream()));
		
		return Optional.of(predicate);
	}
	
	protected Optional<Predicate> handleMedicationReference(@Nonnull String medicationAlias,
	        ReferenceAndListParam medicationReference) {
		EntityManager entityManager = sessionFactory.getCurrentSession();
		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<EntityType> criteriaQuery = criteriaBuilder.createQuery(EntityType.class);
		Root<?> root = criteriaQuery.from(EntityType.class);
		
		if (medicationReference == null) {
			return Optional.empty();
		}
		
		return handleAndListParam(medicationReference, token -> Optional
		        .of(criteriaBuilder.equal(root.get(String.format("%s.uuid", medicationAlias)), token.getIdPart())));
	}
	
	protected Optional<Predicate> handleMedicationRequestReference(@Nonnull String drugOrderAlias,
	        ReferenceAndListParam drugOrderReference) {
		EntityManager entityManager = sessionFactory.getCurrentSession();
		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<EntityType> criteriaQuery = criteriaBuilder.createQuery(EntityType.class);
		Root<?> root = criteriaQuery.from(EntityType.class);
		
		if (drugOrderReference == null) {
			return Optional.empty();
		}
		
		return handleAndListParam(drugOrderReference, token -> Optional
		        .of(criteriaBuilder.equal(root.get(String.format("%s.uuid", drugOrderAlias)), token.getIdPart())));
	}
	
	/**
	 * Use this method to properly implement sorting for your query. Note that for this method to work,
	 * you must override one or more of: {@link #paramToProps(SortState)},
	 * {@link #paramToProps(String)}, or {@link #paramToProp(String)}.
	 *
	 * @param criteriaBuilder the current criteria
	 * @param sort the {@link SortSpec} which defines the sorting to be translated
	 */
	protected void handleSort(CriteriaBuilder criteriaBuilder, SortSpec sort) {
		EntityManager manager = sessionFactory.getCurrentSession();
		criteriaBuilder = manager.getCriteriaBuilder();
		CriteriaQuery<Object> criteriaQuery = criteriaBuilder.createQuery(Object.class);
		
		handleSort(criteriaBuilder, sort, this::paramToProps).ifPresent(l -> l.forEach(criteriaQuery::orderBy));
	}
	
	@SuppressWarnings("unchecked")
	protected Optional<List<javax.persistence.criteria.Order>> handleSort(CriteriaBuilder criteriaBuilder, SortSpec sort,
	        Function<SortState, Collection<javax.persistence.criteria.Order>> paramToProp) {
		List<javax.persistence.criteria.Order> orderings = new ArrayList<>();
		SortSpec sortSpec = sort;
		while (sortSpec != null) {
			SortState state = SortState.builder().criteriaBuilder(criteriaBuilder).sortOrder(sortSpec.getOrder())
			        .parameter(sortSpec.getParamName().toLowerCase()).build();
			
			Collection<javax.persistence.criteria.Order> orders = paramToProp.apply(state);
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
	
	protected Predicate generateSystemQuery(String system, List<String> codes, String conceptReferenceTermAlias) {
		EntityManager entityManager = sessionFactory.getCurrentSession();
		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<FhirConceptSource> criteriaQuery = criteriaBuilder.createQuery(FhirConceptSource.class);
		Root<FhirConceptSource> root = criteriaQuery.from(FhirConceptSource.class);
		
		criteriaQuery.select(root.get("conceptSource")).where(criteriaBuilder.equal(root.get("url"), system));
		
		if (codes.size() > 1) {
			return criteriaBuilder.and(
			    criteriaBuilder.equal(root.get(String.format("%s.conceptSource", conceptReferenceTermAlias)), criteriaQuery),
			    criteriaBuilder.in(root.get(String.format("%s.code", conceptReferenceTermAlias)).get(codes.toString())));
		} else {
			return criteriaBuilder.and(
			    criteriaBuilder.equal(root.get(String.format("%s.conceptSource", conceptReferenceTermAlias)), criteriaQuery),
			    criteriaBuilder.equal(root.get(String.format("%s.code", conceptReferenceTermAlias)), codes.get(0)));
		}
	}
	
	protected Predicate generateActiveOrderQuery(String path, Date onDate) {
		EntityManager entityManager = sessionFactory.getCurrentSession();
		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<EntityType> criteriaQuery = criteriaBuilder.createQuery(EntityType.class);
		Root<EntityType> root = criteriaQuery.from(EntityType.class);
		if (StringUtils.isNotBlank(path)) {
			path = path + ".";
		}
		
		// ACTIVE = date activated null or less than or equal to current datetime, date stopped null or in the future, auto expire date null or in the future
		return criteriaBuilder.and(
		    criteriaBuilder.or(criteriaBuilder.isNull(root.get(path + "dateActivated")),
		        criteriaBuilder.lessThan(root.get(path + "dateActivated"), onDate)),
		    criteriaBuilder.or(criteriaBuilder.isNull(root.get(path + "dateStopped")),
		        criteriaBuilder.greaterThan(root.get(path + "dateStopped"), onDate)),
		    criteriaBuilder.or(criteriaBuilder.isNull(root.get(path + "autoExpireDate")),
		        criteriaBuilder.greaterThan(root.get(path + "autoExpireDate"), onDate)));
	}
	
	protected Predicate generateActiveOrderQuery(String path) {
		return generateActiveOrderQuery(path, new Date());
	}
	
	protected Predicate generateActiveOrderQuery(Date onDate) {
		return generateActiveOrderQuery("", onDate);
	}
	
	protected Predicate generateActiveOrderQuery() {
		return generateActiveOrderQuery("", new Date());
	}
	
	protected Predicate generateNotCancelledOrderQuery() {
		return generateNotCancelledOrderQuery("");
	}
	
	protected Predicate generateNotCancelledOrderQuery(String path) {
		EntityManager entityManager = sessionFactory.getCurrentSession();
		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<EntityType> criteriaQuery = criteriaBuilder.createQuery(EntityType.class);
		Root<EntityType> root = criteriaQuery.from(EntityType.class);
		if (StringUtils.isNotBlank(path)) {
			path = path + ".";
		}
		
		Date now = new Date();
		
		return criteriaBuilder.or(criteriaBuilder.isNull(root.get(path + "dateStopped")),
		    criteriaBuilder.greaterThan(root.get(path + "dateStopped"), now));
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
	protected Collection<javax.persistence.criteria.Order> paramToProps(@Nonnull SortState sortState) {
		Collection<String> prop = paramToProps(sortState.getParameter());
		// TODO : Look into this and convert it correctly
		if (prop != null) {
			switch (sortState.getSortOrder()) {
				case ASC:
					return null;
				//					return prop.stream().map(Order::asc).collect(Collectors.toList());
				case DESC:
					return null;
				//					return prop.stream().map(Order::desc).collect(Collectors.toList());
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
	
	protected Optional<Predicate> propertyLike(@Nonnull String propertyName, String value) {
		if (value == null) {
			return Optional.empty();
		}
		
		return propertyLike(propertyName, new StringParam(value));
	}
	
	protected Optional<Predicate> propertyLike(@Nonnull String propertyName, StringParam param) {
		EntityManager entityManager = sessionFactory.getCurrentSession();
		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<EntityType> criteriaQuery = criteriaBuilder.createQuery(EntityType.class);
		Root<EntityType> root = criteriaQuery.from(EntityType.class);
		
		if (param == null) {
			return Optional.empty();
		}
		
		Predicate likePredicate;
		
		if (param.isExact()) {
			likePredicate = criteriaBuilder.equal(root.get(propertyName), param.getValue());
		} else if (param.isContains()) {
			likePredicate = criteriaBuilder.like(root.get(propertyName), "%" + param.getValue() + "%");
		} else {
			likePredicate = criteriaBuilder.like(root.get(propertyName), param.getValue() + "%");
		}
		
		return Optional.of(likePredicate);
	}
	
	@SuppressWarnings("unchecked")
	protected Optional<CriteriaBuilder> asImpl(CriteriaBuilder criteriaBuilder) {
		if (CriteriaBuilder.class.isAssignableFrom(criteriaBuilder.getClass())) {
			return Optional.of(criteriaBuilder);
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
		
		private CriteriaBuilder criteriaBuilder;
		
		private SortOrderEnum sortOrder;
		
		private String parameter;
	}
	
	protected Optional<Predicate> handleAgeByDateProperty(@Nonnull String datePropertyName, @Nonnull QuantityParam age) {
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
				EntityManager entityManager = sessionFactory.getCurrentSession();
				CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
				CriteriaQuery<EntityType> criteriaQuery = criteriaBuilder.createQuery(EntityType.class);
				Root<EntityType> root = criteriaQuery.from(EntityType.class);
				
				Predicate predicate = criteriaBuilder.and(
				    criteriaBuilder.greaterThanOrEqualTo(root.get(datePropertyName), lowerBound),
				    criteriaBuilder.lessThanOrEqualTo(root.get(datePropertyName), upperBound));
				
				return Optional.ofNullable(predicate);
			} else {
				EntityManager entityManager = sessionFactory.getCurrentSession();
				CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
				CriteriaQuery<EntityType> criteriaQuery = criteriaBuilder.createQuery(EntityType.class);
				Root<EntityType> root = criteriaQuery.from(EntityType.class);
				
				Predicate predicate = criteriaBuilder.and(
				    criteriaBuilder.greaterThanOrEqualTo(root.get(datePropertyName), lowerBound),
				    criteriaBuilder.lessThanOrEqualTo(root.get(datePropertyName), upperBound));
				
				return Optional.ofNullable(predicate);
			}
		}
		
		switch (prefix) {
			case LESSTHAN_OR_EQUALS:
			case LESSTHAN:
			case STARTS_AFTER:
				EntityManager entityManager = sessionFactory.getCurrentSession();
				CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
				CriteriaQuery<EntityType> criteriaQuery = criteriaBuilder.createQuery(EntityType.class);
				Root<EntityType> root = criteriaQuery.from(EntityType.class);
				
				Date dateToCompare = Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
				
				return Optional
				        .ofNullable(criteriaBuilder.greaterThanOrEqualTo(root.get("datePropertyName"), dateToCompare));
			case GREATERTHAN_OR_EQUALS:
			case GREATERTHAN:
				entityManager = sessionFactory.getCurrentSession();
				criteriaBuilder = entityManager.getCriteriaBuilder();
				criteriaQuery = criteriaBuilder.createQuery(EntityType.class);
				root = criteriaQuery.from(EntityType.class);
				
				dateToCompare = Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
				
				return Optional.ofNullable(criteriaBuilder.lessThanOrEqualTo(root.get("datePropertyName"), dateToCompare));
			// Ignoring ENDS_BEFORE as it is not meaningful for age.
		}
		
		return Optional.empty();
	}
	
}
