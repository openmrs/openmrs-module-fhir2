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

import static org.hibernate.criterion.Order.asc;
import static org.hibernate.criterion.Order.desc;
import static org.hibernate.criterion.Projections.property;
import static org.hibernate.criterion.Restrictions.and;
import static org.hibernate.criterion.Restrictions.eq;
import static org.hibernate.criterion.Restrictions.ge;
import static org.hibernate.criterion.Restrictions.gt;
import static org.hibernate.criterion.Restrictions.ilike;
import static org.hibernate.criterion.Restrictions.in;
import static org.hibernate.criterion.Restrictions.le;
import static org.hibernate.criterion.Restrictions.lt;
import static org.hibernate.criterion.Restrictions.not;
import static org.hibernate.criterion.Restrictions.or;
import static org.hibernate.criterion.Subqueries.propertyEq;

import javax.validation.constraints.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
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
import ca.uhn.fhir.model.api.TemporalPrecisionEnum;
import ca.uhn.fhir.rest.api.SortSpec;
import ca.uhn.fhir.rest.param.DateParam;
import ca.uhn.fhir.rest.param.DateRangeParam;
import ca.uhn.fhir.rest.param.ReferenceParam;
import ca.uhn.fhir.rest.param.StringOrListParam;
import ca.uhn.fhir.rest.param.StringParam;
import ca.uhn.fhir.rest.param.TokenOrListParam;
import ca.uhn.fhir.rest.param.TokenParam;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.hibernate.Criteria;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.internal.CriteriaImpl;
import org.hibernate.sql.JoinType;
import org.hl7.fhir.exceptions.FHIRException;
import org.hl7.fhir.r4.model.Location;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.Practitioner;
import org.hl7.fhir.r4.model.codesystems.AdministrativeGender;
import org.openmrs.module.fhir2.FhirConceptSource;

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
public abstract class BaseDaoImpl {
	
	/**
	 * Converts an {@link Iterable} to a {@link Stream}
	 *
	 * @param iterable the iterable
	 * @param <T> any type
	 * @return a stream containing the same objects as the iterable
	 */
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
	protected boolean containsAlias(@NotNull Criteria criteria, @NotNull String alias) {
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
	protected boolean containsAlias(Iterator<CriteriaImpl.Subcriteria> subcriteriaIterator, @NotNull String alias) {
		return stream(subcriteriaIterator).anyMatch(sc -> sc.getAlias().equals(alias));
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
		
		return Optional.of(or(toCriteriaArray(handleOrListParam(orListParam).map(handler).flatMap(s -> s))));
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
	protected Optional<Criterion> handleBoolean(String propertyName, TokenOrListParam booleanToken) {
		if (booleanToken == null) {
			return Optional.empty();
		}
		
		// note that we use a custom implementation here as Boolean.valueOf() and Boolean.parse() only determine whether
		// the string matches "true". We could potentially be passed a non-valid Boolean value here.
		return handleOrListParam(booleanToken, token -> {
			if (token.getValue().equalsIgnoreCase("true")) {
				return Optional.of(eq(propertyName, true));
			} else if (token.getValue().equalsIgnoreCase("false")) {
				return Optional.of(eq(propertyName, false));
			}
			
			return Optional.empty();
		});
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
		
		Date dayStart, dayEnd;
		if (dateParam.getPrecision().ordinal() > TemporalPrecisionEnum.DAY.ordinal()) {
			// TODO We may want to not use the default Calendar
			dayStart = DateUtils.truncate(dateParam.getValue(), Calendar.DATE);
		} else {
			dayStart = dateParam.getValue();
		}
		
		// TODO We may want to not use the default Calendar
		dayEnd = DateUtils.ceiling(dayStart, Calendar.DATE);
		
		// TODO This does not properly handle FHIR Periods and Timings, though its unclear if we are using those
		// see https://www.hl7.org/fhir/search.html#date
		switch (dateParam.getPrefix()) {
			case EQUAL:
				return Optional.of(and(ge(propertyName, dayStart), lt(propertyName, dayEnd)));
			case NOT_EQUAL:
				return Optional.of(not(and(ge(propertyName, dayStart), lt(propertyName, dayEnd))));
			case LESSTHAN_OR_EQUALS:
			case LESSTHAN:
				return Optional.of(le(propertyName, dayEnd));
			case GREATERTHAN_OR_EQUALS:
			case GREATERTHAN:
				return Optional.of(ge(propertyName, dayStart));
			case STARTS_AFTER:
				return Optional.of(gt(propertyName, dayEnd));
			case ENDS_BEFORE:
				return Optional.of(lt(propertyName, dayStart));
		}
		
		return Optional.empty();
	}
	
	protected Optional<Criterion> handleEncounterReference(@NotNull String encounterAlias,
	        ReferenceParam encounterReference) {
		if (encounterReference == null || encounterReference.getIdPart() == null) {
			return Optional.empty();
		}
		
		return Optional.of(eq(String.format("%s.uuid", encounterAlias), encounterReference.getIdPart()));
	}
	
	protected Optional<Criterion> handleGender(@NotNull String propertyName, TokenOrListParam gender) {
		if (gender == null) {
			return Optional.empty();
		}
		
		return handleOrListParam(gender, token -> {
			try {
				AdministrativeGender administrativeGender = AdministrativeGender.fromCode(token.getValue());
				switch (administrativeGender) {
					case MALE:
						return Optional.of(ilike(propertyName, "M", MatchMode.EXACT));
					case FEMALE:
						return Optional.of(ilike(propertyName, "F", MatchMode.EXACT));
					case OTHER:
						return Optional.of(not(or(eq(propertyName, "M"), eq(propertyName, "F"))));
					case UNKNOWN:
					case NULL:
						return Optional.empty();
				}
			}
			catch (FHIRException ignored) {}
			
			return Optional.empty();
		});
	}
	
	protected void handleLocationReference(Criteria criteria, ReferenceParam locationReference) {
		if (locationReference != null) {
			criteria.createAlias("location", "l");
			
			if (locationReference.getChain() != null) {
				switch (locationReference.getChain()) {
					case Location.SP_ADDRESS_CITY:
						propertyLike("l.cityVillage", locationReference.getValue()).ifPresent(criteria::add);
						break;
					case Location.SP_ADDRESS_STATE:
						propertyLike("l.stateProvince", locationReference.getValue()).ifPresent(criteria::add);
						break;
					case Location.SP_ADDRESS_POSTALCODE:
						propertyLike("l.postalCode", locationReference.getValue()).ifPresent(criteria::add);
						break;
					case Location.SP_ADDRESS_COUNTRY:
						propertyLike("l.country", locationReference.getValue()).ifPresent(criteria::add);
						break;
					case "":
						criteria.add(eq("l.uuid", locationReference.getValue()));
						break;
				}
			}
		}
	}
	
	protected void handleParticipantReference(Criteria criteria, ReferenceParam participantReference) {
		if (participantReference != null) {
			criteria.createAlias("encounterProviders", "ep");
			
			if (participantReference.getChain() != null) {
				switch (participantReference.getChain()) {
					case Practitioner.SP_IDENTIFIER:
						criteria.createAlias("ep.provider", "p").add(ilike("p.identifier", participantReference.getValue()));
						break;
					case Practitioner.SP_GIVEN:
						criteria.createAlias("ep.provider", "pro").createAlias("pro.person", "ps")
						        .createAlias("ps.names", "pn")
						        .add(ilike("pn.givenName", participantReference.getValue(), MatchMode.START));
						break;
					case Practitioner.SP_FAMILY:
						criteria.createAlias("ep.provider", "pro").createAlias("pro.person", "ps")
						        .createAlias("ps.names", "pn")
						        .add(ilike("pn.familyName", participantReference.getValue(), MatchMode.START));
						break;
					case Practitioner.SP_NAME:
						criteria.createAlias("ep.provider", "pro").createAlias("pro.person", "ps").createAlias("ps.names",
						    "pn");
						
						List<Optional<Criterion>> criterionList = new ArrayList<>();
						
						for (String token : StringUtils.split(participantReference.getValue(), " \t,")) {
							criterionList.add(propertyLike("pn.givenName", token));
							criterionList.add(propertyLike("pn.middleName", token));
							criterionList.add(propertyLike("pn.familyName", token));
						}
						
						criteria.add(or(toCriteriaArray(criterionList)));
						break;
					case "":
						criteria.add(eq("ep.uuid", participantReference.getValue()));
						break;
				}
			}
		}
	}
	
	protected void handleIdentifier(Criteria criteria, TokenOrListParam identifier) {
		if (identifier == null) {
			return;
		}
		
		criteria.createAlias("identifiers", "pi", JoinType.INNER_JOIN, eq("pi.voided", false));
		
		handleOrListParamBySystem(identifier, (system, tokens) -> {
			if (system.isEmpty()) {
				return Optional.of(in("pi.identifier", tokensToList(tokens)));
			} else {
				if (!containsAlias(criteria, "pit")) {
					criteria.createAlias("pi.identifierType", "pit");
				}
				
				return Optional.of(and(eq("pit.name", system), in("pi.identifier", tokensToList(tokens))));
			}
		}).ifPresent(criteria::add);
	}
	
	protected void handleNames(Criteria criteria, StringOrListParam name, StringOrListParam given,
	        StringOrListParam family) {
		if (name == null && given == null && family == null) {
			return;
		}
		
		criteria.createAlias("names", "pn");
		
		if (name != null) {
			handleOrListParamAsStream(name,
			    (nameParam) -> Arrays.stream(StringUtils.split(nameParam.getValue(), " \t,"))
			            .map(token -> new StringParam().setValue(token).setExact(nameParam.isExact())
			                    .setContains(nameParam.isContains()))
			            .map(tokenParam -> Arrays.asList(propertyLike("pn.givenName", tokenParam),
			                propertyLike("pn.middleName", tokenParam), propertyLike("pn.familyName", tokenParam)))
			            .flatMap(Collection::stream)).ifPresent(criteria::add);
		}
		
		if (given != null) {
			handleOrListParam(given, (givenName) -> propertyLike("pn.givenName", givenName)).ifPresent(criteria::add);
		}
		
		if (family != null) {
			handleOrListParam(family, (familyName) -> propertyLike("pn.familyName", familyName)).ifPresent(criteria::add);
		}
	}
	
	protected void handlePatientReference(Criteria criteria, ReferenceParam patientReference) {
		handlePatientReference(criteria, patientReference, "patient");
	}
	
	protected void handlePatientReference(Criteria criteria, ReferenceParam patientReference, String associationPath) {
		if (patientReference != null) {
			criteria.createAlias(associationPath, "p");
			
			if (patientReference.getChain() != null) {
				switch (patientReference.getChain()) {
					case Patient.SP_IDENTIFIER:
						criteria.createAlias("p.identifiers", "pi").add(ilike("pi.identifier", patientReference.getValue()));
						break;
					case Patient.SP_GIVEN:
						criteria.createAlias("p.names", "pn")
						        .add(ilike("pn.givenName", patientReference.getValue(), MatchMode.START));
						break;
					case Patient.SP_FAMILY:
						criteria.createAlias("p.names", "pn")
						        .add(ilike("pn.familyName", patientReference.getValue(), MatchMode.START));
						break;
					case Patient.SP_NAME:
						criteria.createAlias("p.names", "pn");
						
						List<Optional<Criterion>> criterionList = new ArrayList<>();
						
						for (String token : StringUtils.split(patientReference.getValue(), " \t,")) {
							criterionList.add(propertyLike("pn.givenName", token));
							criterionList.add(propertyLike("pn.middleName", token));
							criterionList.add(propertyLike("pn.familyName", token));
						}
						
						criteria.add(or(toCriteriaArray(criterionList)));
						break;
					case "":
						criteria.add(eq("p.uuid", patientReference.getValue()));
						break;
				}
			}
		}
	}
	
	protected Optional<Criterion> handlePersonAddress(String aliasPrefix, StringOrListParam city, StringOrListParam state,
	        StringOrListParam postalCode, StringOrListParam country) {
		if (city == null && state == null && postalCode == null && country == null) {
			return Optional.empty();
		}
		
		List<Optional<Criterion>> criterionList = new ArrayList<>();
		
		if (city != null) {
			criterionList.add(
			    handleOrListParam(city, c -> Optional.of(eq(String.format("%s.cityVillage", aliasPrefix), c.getValue()))));
		}
		
		if (state != null) {
			criterionList.add(handleOrListParam(state,
			    c -> Optional.of(eq(String.format("%s.stateProvince", aliasPrefix), c.getValue()))));
		}
		
		if (postalCode != null) {
			criterionList.add(handleOrListParam(postalCode,
			    c -> Optional.of(eq(String.format("%s.postalCode", aliasPrefix), c.getValue()))));
		}
		
		if (country != null) {
			criterionList.add(
			    handleOrListParam(country, c -> Optional.of(eq(String.format("%s.country", aliasPrefix), c.getValue()))));
		}
		
		if (criterionList.size() == 0) {
			return Optional.empty();
		}
		
		return Optional.of(or(toCriteriaArray(criterionList.stream())));
	}
	
	protected void handleSort(Criteria criteria, SortSpec sort) {
		handleSort(sort, this::paramToProp).ifPresent(l -> l.forEach(criteria::addOrder));
	}
	
	protected Optional<List<Order>> handleSort(SortSpec sort, Function<String, String> paramToProp) {
		List<Order> orderings = new ArrayList<>();
		SortSpec sortSpec = sort;
		while (sortSpec != null) {
			String prop = paramToProp.apply(sortSpec.getParamName());
			if (prop != null) {
				switch (sortSpec.getOrder()) {
					case DESC:
						orderings.add(desc(prop));
						break;
					case ASC:
						orderings.add(asc(prop));
						break;
				}
			}
			
			sortSpec = sortSpec.getChain();
		}
		
		if (orderings.size() == 0) {
			return Optional.empty();
		} else {
			return Optional.of(orderings);
		}
	}
	
	protected Criterion generateSystemQuery(String system, List<String> codes) {
		DetachedCriteria conceptSourceCriteria = DetachedCriteria.forClass(FhirConceptSource.class).add(eq("url", system))
		        .setProjection(property("conceptSource"));
		
		if (codes.size() > 1) {
			return and(propertyEq("crt.conceptSource", conceptSourceCriteria), in("crt.code", codes));
		} else {
			return and(propertyEq("crt.conceptSource", conceptSourceCriteria), eq("crt.code", codes.get(0)));
		}
	}
	
	/**
	 * This function should be overridden by implementations. It exists to map a FHIR parameter value to
	 * the corresponding property name. This is used to correctly sort the returned results.
	 *
	 * @param param the name of the FHIR parameter
	 * @return the corresponding property in the query
	 */
	protected String paramToProp(@NotNull String param) {
		return null;
	}
	
	protected Optional<Criterion> propertyLike(@NotNull String propertyName, String value) {
		if (value == null) {
			return Optional.empty();
		}
		
		return propertyLike(propertyName, new StringParam(value));
	}
	
	protected Optional<Criterion> propertyLike(@NotNull String propertyName, StringParam param) {
		if (param == null) {
			return Optional.empty();
		}
		
		if (param.isExact()) {
			return Optional.of(ilike(propertyName, param.getValue(), MatchMode.EXACT));
		} else if (param.isContains()) {
			return Optional.of(ilike(propertyName, param.getValue(), MatchMode.ANYWHERE));
		} else {
			return Optional.of(ilike(propertyName, param.getValue(), MatchMode.START));
		}
	}
	
	protected Optional<CriteriaImpl> asImpl(Criteria criteria) {
		if (CriteriaImpl.class.isAssignableFrom(criteria.getClass())) {
			return Optional.of((CriteriaImpl) criteria);
		} else if (CriteriaImpl.Subcriteria.class.isAssignableFrom(criteria.getClass())) {
			return Optional.of((CriteriaImpl) ((CriteriaImpl.Subcriteria) criteria).getParent());
		} else {
			return Optional.empty();
		}
	}
	
	protected List<String> tokensToList(List<TokenParam> tokens) {
		return tokensToParams(tokens).collect(Collectors.toList());
	}
	
	protected Stream<String> tokensToParams(List<TokenParam> tokens) {
		return tokens.stream().map(TokenParam::getValue);
	}
	
	private String groupBySystem(@NotNull TokenParam token) {
		return StringUtils.trimToEmpty(token.getSystem());
	}
	
	protected <T extends IQueryParameterOr<U>, U extends IQueryParameterType> Stream<T> handleAndListParam(
	        IQueryParameterAnd<T> andListParameter) {
		return andListParameter.getValuesAsQueryTokens().stream();
	}
	
	protected <T extends IQueryParameterType> Stream<T> handleOrListParam(IQueryParameterOr<T> orListParameter) {
		return orListParameter.getValuesAsQueryTokens().stream();
	}
	
	protected Criterion[] toCriteriaArray(Collection<Optional<Criterion>> collection) {
		return toCriteriaArray(collection.stream());
	}
	
	protected Criterion[] toCriteriaArray(Stream<Optional<Criterion>> criteriaStream) {
		return criteriaStream.filter(Optional::isPresent).map(Optional::get).toArray(Criterion[]::new);
	}
}
