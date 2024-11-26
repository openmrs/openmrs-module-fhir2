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
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Path;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import ca.uhn.fhir.rest.param.StringAndListParam;
import org.openmrs.Auditable;
import org.openmrs.OpenmrsObject;
import org.openmrs.Patient;
import org.openmrs.Person;
import org.openmrs.PersonName;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.api.search.param.PropParam;

/**
 * Base class for Person-related DAO objects. This helps standardise the logic used to search for
 * and sort objects that represent a Person, including {@link Person}, {@link Patient}, but also
 * classes like {@link org.openmrs.Provider} and {@link org.openmrs.User}.
 *
 * @param <T> The OpenMRS object that represents a person.
 */
public abstract class BasePersonDao<T extends OpenmrsObject & Auditable> extends BaseFhirDao<T> {
	
	/**
	 * This is intended to be overridden by subclasses to provide the {@link From} that defines the
	 * Person for this object
	 *
	 * @return the {@link From} object that points to the person for this object
	 */
	@SuppressWarnings("UnstableApiUsage")
	protected <V, U> From<?, ?> getPersonProperty(OpenmrsFhirCriteriaContext<V, U> criteriaContext) {
		Class<? super T> rawType = typeToken.getRawType();
		if (rawType.equals(Person.class) || rawType.equals(Patient.class)) {
			return criteriaContext.getRoot();
		}
		
		return criteriaContext.addJoin("person", "person");
	}
	
	@Override
	protected <V, U> Collection<Order> paramToProps(@Nonnull OpenmrsFhirCriteriaContext<V, U> criteriaContext,
                                                    @Nonnull SortState<V, U> sortState) {
		String param = sortState.getParameter();
		
		if (param == null) {
			return null;
		}
		
		From<?, ?> person = getPersonProperty(criteriaContext);
		if (param.startsWith("address")) {
			criteriaContext.addJoin(person, "addresses", "pad");
		} else if (param.equals(SP_NAME) || param.equals(SP_GIVEN) || param.equals(SP_FAMILY)) {
			CriteriaBuilder cb = criteriaContext.getCriteriaBuilder();
			
			// For person names, we have several subqueries to ensure that we are only working against a single person name
			
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
			    cb.equal(personNameFirstSubquery.getRoot().get("person"), criteriaContext.getRoot())));
			
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
			personNameThirdSubquery.getSubquery().select(cb.min(personNameFirstSubquery.getRoot().get("personNameId")));
			
			Join<?, ?> personName = criteriaContext.addJoin(person, "names", "pn", JoinType.LEFT,
			    (personNameJoin) -> cb.and(cb.equal(personNameJoin.get("voided"), false), cb.or(
			        // preferred patient name
			        cb.equal(personNameJoin.get("personNameId"), personNameFirstSubquery.finalizeQuery()),
			        // first patient name if there are no preferred patient name
			        cb.and(cb.not(cb.exists(personNameSecondSubquery.finalizeQuery())),
			            cb.equal(personNameJoin.get("personNameId"), personNameThirdSubquery.finalizeQuery())),
			        // ultimate fallback, I guess?
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
			
			List<Order> sortStateOrders = new ArrayList<>();
			switch (sortState.getSortOrder()) {
				case ASC:
					for (String property : properties) {
						sortStateOrders.add(criteriaContext.getCriteriaBuilder().asc(personName.get(property)));
					}
					break;
				case DESC:
					for (String property : properties) {
						sortStateOrders.add(criteriaContext.getCriteriaBuilder().desc(personName.get(property)));
					}
					break;
			}
			
			return sortStateOrders;
		}
		
		return super.paramToProps(criteriaContext, sortState);
	}
	
	@Override
	protected <V, U> Path<?> paramToProp(@Nonnull OpenmrsFhirCriteriaContext<V, U> criteriaContext, @Nonnull String param) {
		From<?, ?> person = getPersonProperty(criteriaContext);
		From<?, ?> address = criteriaContext.getJoin("pad")
		        .orElseGet(() -> criteriaContext.addJoin(person, "addresses", "pad"));
		
		switch (param) {
			case SP_BIRTHDATE:
				return person.get("birthdate");
			case SP_ADDRESS_CITY:
				return address.get("cityVillage");
			case SP_ADDRESS_STATE:
				return address.get("stateProvince");
			case SP_ADDRESS_POSTALCODE:
				return address.get("postalCode");
			case SP_ADDRESS_COUNTRY:
				return address.get("country");
			default:
				return null;
		}
	}
	
	protected <U> void handleAddresses(OpenmrsFhirCriteriaContext<T, U> criteriaContext,
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
		
		From<?, ?> person = getPersonProperty(criteriaContext);
		From<?, ?> padJoin = criteriaContext.addJoin(person, "addresses", "pad");
		handlePersonAddress(criteriaContext, padJoin, city, state, postalCode, country)
		        .ifPresent(criteriaContext::addPredicate);
	}
	
	protected <U> void handleNames(OpenmrsFhirCriteriaContext<T, U> criteriaContext, List<PropParam<?>> params) {
		StringAndListParam name = null;
		StringAndListParam given = null;
		StringAndListParam family = null;
		
		for (PropParam<?> param : params) {
			switch (param.getPropertyName()) {
				case FhirConstants.NAME_PROPERTY:
					name = (StringAndListParam) param.getParam();
					break;
				case FhirConstants.GIVEN_PROPERTY:
					given = (StringAndListParam) param.getParam();
					break;
				case FhirConstants.FAMILY_PROPERTY:
					family = (StringAndListParam) param.getParam();
					break;
			}
		}
		
		handleNames(criteriaContext, name, given, family, getPersonProperty(criteriaContext));
	}
}
