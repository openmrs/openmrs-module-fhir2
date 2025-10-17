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

import static org.hl7.fhir.r4.model.Patient.SP_FAMILY;
import static org.hl7.fhir.r4.model.Patient.SP_GIVEN;
import static org.hl7.fhir.r4.model.Person.SP_ADDRESS_CITY;
import static org.hl7.fhir.r4.model.Person.SP_ADDRESS_COUNTRY;
import static org.hl7.fhir.r4.model.Person.SP_ADDRESS_POSTALCODE;
import static org.hl7.fhir.r4.model.Person.SP_ADDRESS_STATE;
import static org.hl7.fhir.r4.model.Person.SP_BIRTHDATE;
import static org.hl7.fhir.r4.model.Person.SP_NAME;

import javax.annotation.Nonnull;
import javax.persistence.criteria.*;

import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import ca.uhn.fhir.rest.param.DateParam;
import ca.uhn.fhir.rest.param.DateRangeParam;
import ca.uhn.fhir.rest.param.StringAndListParam;
import ca.uhn.fhir.rest.param.TokenAndListParam;
import org.apache.commons.lang3.time.DateUtils;
import org.openmrs.PersonName;
import org.openmrs.Relationship;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.api.dao.FhirRelatedPersonDao;
import org.openmrs.module.fhir2.api.dao.internals.OpenmrsFhirCriteriaContext;
import org.openmrs.module.fhir2.api.dao.internals.OpenmrsFhirCriteriaSubquery;
import org.openmrs.module.fhir2.api.search.param.PropParam;
import org.openmrs.module.fhir2.api.search.param.SearchParameterMap;
import org.springframework.stereotype.Component;

@Component
public class FhirRelatedPersonDaoImpl extends BaseFhirDao<Relationship> implements FhirRelatedPersonDao {
	
	@Override
	protected <U> void setupSearchParams(@Nonnull OpenmrsFhirCriteriaContext<Relationship, U> criteriaContext,
	        @Nonnull SearchParameterMap theParams) {
		From<?, ?> personJoin = criteriaContext.addJoin("personA", "m");
		theParams.getParameters().forEach(entry -> {
			switch (entry.getKey()) {
				case FhirConstants.NAME_SEARCH_HANDLER:
					entry.getValue().forEach(param -> handleNames(criteriaContext, (StringAndListParam) param.getParam(),
					    null, null, personJoin));
					break;
				case FhirConstants.GENDER_SEARCH_HANDLER:
					entry.getValue()
					        .forEach(param -> handleGender(criteriaContext, personJoin, FhirConstants.GENDER_PROPERTY,
					            (TokenAndListParam) param.getParam()).ifPresent(criteriaContext::addPredicate));
					break;
				case FhirConstants.DATE_RANGE_SEARCH_HANDLER:
					entry.getValue()
					        .forEach(param -> handleDateRange(criteriaContext, personJoin, (DateRangeParam) param.getParam())
					                .ifPresent(criteriaContext::addPredicate));
					break;
				case FhirConstants.ADDRESS_SEARCH_HANDLER:
					handleAddresses(criteriaContext, entry);
					break;
				case FhirConstants.COMMON_SEARCH_HANDLER:
					handleCommonSearchParameters(criteriaContext, entry.getValue()).ifPresent(criteriaContext::addPredicate);
					break;
			}
		});
	}
	
	// TODO: find a way of integrating this with the handleDateRange functionality in BaseDao!
	private <U> Optional<Predicate> handleDateRange(OpenmrsFhirCriteriaContext<Relationship, U> criteriaContext,
	        From<?, ?> personJoin, DateRangeParam param) {
		if (param == null) {
			return Optional.empty();
		}
		
		return Optional.ofNullable(criteriaContext.getCriteriaBuilder()
		        .and(toCriteriaArray(Stream.of(handleDate(criteriaContext, personJoin, param.getLowerBound()),
		            handleDate(criteriaContext, personJoin, param.getUpperBound())))));
	}
	
	private <U> Optional<Predicate> handleDate(OpenmrsFhirCriteriaContext<Relationship, U> criteriaContext,
	        From<?, ?> personJoin, DateParam dateParam) {
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
				    criteriaContext.getCriteriaBuilder().greaterThanOrEqualTo(personJoin.get("birthdate"), dateStart),
				    criteriaContext.getCriteriaBuilder().lessThan(personJoin.get("birthdate"), dateEnd)));
			case NOT_EQUAL:
				return Optional.of(criteriaContext.getCriteriaBuilder()
				        .not(criteriaContext.getCriteriaBuilder().and(
				            criteriaContext.getCriteriaBuilder().greaterThanOrEqualTo(personJoin.get("birthdate"),
				                dateStart),
				            criteriaContext.getCriteriaBuilder().lessThan(personJoin.get("birthdate"), dateEnd))));
			case LESSTHAN_OR_EQUALS:
			case LESSTHAN:
				return Optional
				        .of(criteriaContext.getCriteriaBuilder().lessThanOrEqualTo(personJoin.get("birthdate"), dateEnd));
			case GREATERTHAN_OR_EQUALS:
			case GREATERTHAN:
				return Optional.of(
				    criteriaContext.getCriteriaBuilder().greaterThanOrEqualTo(personJoin.get("birthdate"), dateStart));
			case STARTS_AFTER:
				return Optional.of(criteriaContext.getCriteriaBuilder().greaterThan(personJoin.get("birthdate"), dateEnd));
			case ENDS_BEFORE:
				return Optional.of(criteriaContext.getCriteriaBuilder().lessThan(personJoin.get("birthdate"), dateEnd));
		}
		
		return Optional.empty();
	}
	
	@Override
	protected <T, U> Collection<Order> paramToProps(@Nonnull OpenmrsFhirCriteriaContext<T, U> criteriaContext,
	        @Nonnull SortState<T, U> sortState) {
		String param = sortState.getParameter();
		
		if (param == null) {
			return null;
		}
		
		From<?, ?> person = criteriaContext.addJoin("personA", "m");
		if (param.equals(SP_NAME) || param.equals(SP_GIVEN) || param.equals(SP_FAMILY)) {
			CriteriaBuilder cb = criteriaContext.getCriteriaBuilder();

            // first criteria query: get the first preferred, non-voided person name
            /*
             * Should be something like the inner query in the below:
             *
             * select *
             * from person p1_
             * join person_name pn1_ on
             *     pn1_.person_id = p1_.person_id and
             * 	   pn1_.person_name_id = (
             *         select min(personNameId)
             *         from person_name pn2_
             *         where pn2_.voided = false and pn2_.preferred = true and pn2_.person_id = p1.person_id
             *     )
             */
			OpenmrsFhirCriteriaSubquery<PersonName, Integer> personNameFirstSubquery = criteriaContext
			        .addSubquery(PersonName.class);
			personNameFirstSubquery.addPredicate(cb.and(cb.equal(personNameFirstSubquery.getRoot().get("voided"), false),
			    cb.equal(personNameFirstSubquery.getRoot().get("preferred"), true),
			    cb.equal(personNameFirstSubquery.getRoot().get("person"), criteriaContext.getRoot())));
			personNameFirstSubquery.getSubquery().select(cb.min(personNameFirstSubquery.getRoot().get("personNameId")));

            // second criteria query, used in `or` clause to ensure that there is no preferred person name
            /*
             * Should be something like:
             * select *
             * from person p1_
             * join person_name pn1_ on
             *     pn1_.person_id = p1_.person_id and
             *     not exists (
             * 	       select *
             *         from person_name pn2_
             *         where pn2_.voided = false and pn2_.preferred = true and pn2_.person_id = p1.person_id
             *     )
             */
			OpenmrsFhirCriteriaSubquery<PersonName, Integer> personNameSecondSubquery = criteriaContext
			        .addSubquery(PersonName.class);
			personNameSecondSubquery.addPredicate(cb.and(cb.equal(personNameSecondSubquery.getRoot().get("voided"), false),
			    cb.equal(personNameSecondSubquery.getRoot().get("preferred"), true),
			    cb.equal(personNameSecondSubquery.getRoot().get("person"), criteriaContext.getRoot())));
            personNameSecondSubquery.getSubquery().select(personNameSecondSubquery.getRoot().get("personNameId"));

            // third criteria query, just get the first non-voided person name
            /*
             * Should be something like the inner query in the below:
             *
             * select *
             * from person p1_
             * join person_name pn1_ on
             *     pn1_.person_id = p1_.person_id and
             * 	   pn1_.person_name_id = (
             *         select min(personNameId)
             *         from person_name pn2_
             *         where pn2_.voided = false and pn2_.person_id = p1.person_id
             *     )
             */
			OpenmrsFhirCriteriaSubquery<PersonName, Integer> personNameThirdSubquery = criteriaContext
			        .addSubquery(PersonName.class);
			personNameThirdSubquery.addPredicate(cb.and(cb.equal(personNameThirdSubquery.getRoot().get("voided"), false),
			    cb.equal(personNameThirdSubquery.getRoot().get("person"), criteriaContext.getRoot())));
			personNameThirdSubquery.getSubquery().select(cb.min(personNameThirdSubquery.getRoot().get("personNameId")));

			//pn
			Join<?, ?> personName = criteriaContext.addJoin(person, "names", "pn", JoinType.LEFT,
			    (personNameJoin) -> cb.and(cb.equal(personNameJoin.get("voided"), false),
			        cb.or(cb.equal(personNameJoin.get("personNameId"), personNameFirstSubquery.finalizeQuery()),
			            cb.and(cb.not(cb.exists(personNameSecondSubquery.finalizeQuery())),
			                cb.equal(personNameJoin.get("personNameId"), personNameThirdSubquery.finalizeQuery())),
			            cb.isNull(personNameJoin.get("personNameId")))));
			
			String[] properties = null;
			switch (param) {
				case SP_NAME:
					properties = new String[] { "familyName", "familyName2", "givenName", "middleName", "familyNamePrefix",
					        "familyNameSuffix" };
					break;
				case SP_GIVEN:
					properties = new String[] { "givenName" };
					break;
				case SP_FAMILY:
					properties = new String[] { "familyName" };
					break;
			}
			
			switch (sortState.getSortOrder()) {
				case ASC:
					for (String property : properties) {
						criteriaContext.addOrder(criteriaContext.getCriteriaBuilder().asc(personName.get(property)));
					}
					break;
				case DESC:
					for (String property : properties) {
						criteriaContext.addOrder(criteriaContext.getCriteriaBuilder().desc(personName.get(property)));
					}
					break;
			}
			
		}
		
		return super.paramToProps(criteriaContext, sortState);
	}
	
	@Override
	protected <V, U> Path<?> paramToProp(@Nonnull OpenmrsFhirCriteriaContext<V, U> criteriaContext, @Nonnull String param) {
		From<?, ?> personJoin = criteriaContext.getJoin("m").orElseGet(() -> criteriaContext.addJoin("person", "m"));
        if (param.startsWith("address")) {
            From<?, ?> personAddressJoin = criteriaContext.getJoin("pad")
                    .orElseGet(() -> criteriaContext.addJoin(personJoin, "addresses", "pad", JoinType.LEFT));

            switch (param) {
                case SP_ADDRESS_CITY:
                    return personAddressJoin.get("cityVillage");
                case SP_ADDRESS_STATE:
                    return personAddressJoin.get("stateProvince");
                case SP_ADDRESS_POSTALCODE:
                    return personAddressJoin.get("postalCode");
                case SP_ADDRESS_COUNTRY:
                    return personAddressJoin.get("country");
            }
        }

		switch (param) {
			case SP_BIRTHDATE:
				return personJoin.get("birthdate");
			default:
				return super.paramToProp(criteriaContext, param);
		}
	}
	
	private <U> void handleAddresses(OpenmrsFhirCriteriaContext<Relationship, U> criteriaContext,
	        Map.Entry<String, List<PropParam<?>>> entry) {
		StringAndListParam city = null;
		StringAndListParam country = null;
		StringAndListParam postalCode = null;
		StringAndListParam state = null;
		for (PropParam<?> param : entry.getValue()) {
			switch (param.getPropertyName()) {
				case FhirConstants.CITY_PROPERTY:
					city = ((StringAndListParam) param.getParam());
					break;
				case FhirConstants.STATE_PROPERTY:
					state = ((StringAndListParam) param.getParam());
					break;
				case FhirConstants.POSTAL_CODE_PROPERTY:
					postalCode = ((StringAndListParam) param.getParam());
					break;
				case FhirConstants.COUNTRY_PROPERTY:
					country = ((StringAndListParam) param.getParam());
					break;
			}
		}
		
		From<?, ?> personJoin = criteriaContext.addJoin("personA", "m");
		From<?, ?> padJoin = criteriaContext.addJoin(personJoin, "addresses", "pad");
		handlePersonAddress(criteriaContext, padJoin, city, state, postalCode, country)
		        .ifPresent(criteriaContext::addPredicate);
	}
	
}
