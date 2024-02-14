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

import java.util.Collection;
import java.util.List;
import java.util.Map;

import ca.uhn.fhir.rest.param.DateRangeParam;
import ca.uhn.fhir.rest.param.StringAndListParam;
import ca.uhn.fhir.rest.param.TokenAndListParam;
import lombok.AccessLevel;
import lombok.Setter;
import org.openmrs.PersonName;
import org.openmrs.Relationship;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.api.dao.FhirRelatedPersonDao;
import org.openmrs.module.fhir2.api.search.param.PropParam;
import org.openmrs.module.fhir2.api.search.param.SearchParameterMap;
import org.springframework.stereotype.Component;

@Component
@Setter(AccessLevel.PACKAGE)
public class FhirRelatedPersonDaoImpl extends BaseFhirDao<Relationship> implements FhirRelatedPersonDao {
	
	@Override
	protected <U> void setupSearchParams(OpenmrsFhirCriteriaContext<Relationship, U> criteriaContext,
	        SearchParameterMap theParams) {
		From<?, ?> personJoin = criteriaContext.addJoin("personA", "m");
		theParams.getParameters().forEach(entry -> {
			switch (entry.getKey()) {
				case FhirConstants.NAME_SEARCH_HANDLER:
					entry.getValue().forEach(param -> handleNames(criteriaContext, (StringAndListParam) param.getParam(),
					    null, null, personJoin));
					break;
				case FhirConstants.GENDER_SEARCH_HANDLER:
					entry.getValue().forEach(
					    param -> handleGender(criteriaContext, personJoin, "gender", (TokenAndListParam) param.getParam())
					            .ifPresent(c -> criteriaContext.addPredicate(c).finalizeQuery()));
					break;
				case FhirConstants.DATE_RANGE_SEARCH_HANDLER:
					entry.getValue().forEach(
					    param -> handleDateRange(criteriaContext, "birthdate", (DateRangeParam) param.getParam())
					            .ifPresent(c -> criteriaContext.addPredicate(c).finalizeQuery()));
					break;
				case FhirConstants.ADDRESS_SEARCH_HANDLER:
					handleAddresses(criteriaContext, entry);
					break;
				case FhirConstants.COMMON_SEARCH_HANDLER:
					handleCommonSearchParameters(criteriaContext, entry.getValue())
					        .ifPresent(c -> criteriaContext.addPredicate(c).finalizeQuery());
					break;
			}
		});
	}
	
	@Override
	protected <T, U> Collection<Order> paramToProps(OpenmrsFhirCriteriaContext<T, U> criteriaContext,
	        @Nonnull SortState sortState) {
		String param = sortState.getParameter();
		
		if (param == null) {
			return null;
		}
		
		From<?, ?> personJoin = criteriaContext.addJoin("personA", "m");
		if (param.startsWith("address") && !criteriaContext.getJoin("pad").isPresent()) {
			criteriaContext.addJoin(personJoin, "addresses", "pad", javax.persistence.criteria.JoinType.LEFT);
		} else if (param.equals(SP_NAME) || param.equals(SP_GIVEN) || param.equals(SP_FAMILY)) {
			if (!criteriaContext.getJoin("pn").isPresent()) {
				criteriaContext.addJoin(personJoin, "names", "pn", javax.persistence.criteria.JoinType.LEFT);
			}
			
			Root<PersonName> subRoot = criteriaContext.getCriteriaQuery().subquery(Integer.class).from(PersonName.class);
			
			Predicate predicate = criteriaContext.getCriteriaBuilder()
			        .and(criteriaContext.getCriteriaBuilder().equal(criteriaContext.getRoot().get("pn.voided"), false),
			            criteriaContext.getCriteriaBuilder().or(
			                criteriaContext.getCriteriaBuilder().and(
			                    criteriaContext.getCriteriaBuilder().equal(criteriaContext.getRoot().get("pn.preferred"),
			                        true),
			                    criteriaContext.getCriteriaBuilder().equal(criteriaContext.getRoot().get("pn.personNameId"),
			                        criteriaContext.getCriteriaQuery().subquery(Integer.class)
			                                .select(criteriaContext.getCriteriaBuilder()
			                                        .min(criteriaContext.getRoot().get("pn1.personNameId")))
			                                .where(criteriaContext.getCriteriaBuilder().and(
			                                    criteriaContext.getCriteriaBuilder().equal(subRoot.get("preferred"), true),
			                                    criteriaContext.getCriteriaBuilder().equal(
			                                        subRoot.get("person_id"),
			                                        criteriaContext.getRoot().get("person_id")))))),
			                criteriaContext.getCriteriaBuilder().and(criteriaContext.getCriteriaBuilder().not(
			                    criteriaContext.getCriteriaBuilder().exists(criteriaContext.getCriteriaQuery()
			                            .subquery(Integer.class).select(criteriaContext.getRoot().get("pn2.personNameId"))
			                            .where(criteriaContext.getCriteriaBuilder().and(
			                                criteriaContext.getCriteriaBuilder().equal(subRoot.get("pn2.preferred"), true),
			                                criteriaContext.getCriteriaBuilder().equal(
			                                    subRoot.get("pn2").get("person").get("personId"),
			                                    criteriaContext.getRoot().get("personId")))))),
			                    criteriaContext.getCriteriaBuilder().equal(
			                        criteriaContext.getRoot().get("pn.personNameId"),
			                        criteriaContext.getCriteriaQuery().subquery(Integer.class)
			                                .select(criteriaContext.getCriteriaBuilder()
			                                        .min(criteriaContext.getRoot().get("pn3.personNameId")))
			                                .where(criteriaContext.getCriteriaBuilder().and(
			                                    criteriaContext.getCriteriaBuilder().equal(subRoot.get("pn3.preferred"),
			                                        false),
			                                    criteriaContext.getCriteriaBuilder().equal(
			                                        subRoot.get("pn3").get("person").get("personId"),
			                                        criteriaContext.getRoot().get("personId")))))),
			                criteriaContext.getCriteriaBuilder().isNull(criteriaContext.getRoot().get("pn.personNameId"))));
			criteriaContext.getCriteriaQuery().where(predicate);
			
			String[] properties = null;
			switch (param) {
				case SP_NAME:
					properties = new String[] { "pn.familyName", "pn.familyName2", "pn.givenName", "pn.middleName",
					        "pn.familyNamePrefix", "pn.familyNameSuffix" };
					break;
				case SP_GIVEN:
					properties = new String[] { "pn.givenName" };
					break;
				case SP_FAMILY:
					properties = new String[] { "pn.familyName" };
					break;
			}
			
			switch (sortState.getSortOrder()) {
				case ASC:
					for (String property : properties) {
						criteriaContext
						        .addOrder(criteriaContext.getCriteriaBuilder().asc(criteriaContext.getRoot().get(property)));
					}
					break;
				case DESC:
					for (String property : properties) {
						criteriaContext.addOrder(
						    criteriaContext.getCriteriaBuilder().desc(criteriaContext.getRoot().get(property)));
					}
					break;
			}
			
		}
		
		return super.paramToProps(criteriaContext, sortState);
	}
	
	@Override
	protected <V, U> String paramToProp(OpenmrsFhirCriteriaContext<V, U> criteriaContext, @Nonnull String param) {
		From<?, ?> personJoin = criteriaContext.addJoin("personA", "m");
		From<?,?> pad = criteriaContext.addJoin(personJoin, "addresses", "pad", javax.persistence.criteria.JoinType.LEFT);
		switch (param) {
			case SP_BIRTHDATE:
				 personJoin.get("birthdate");
			case SP_ADDRESS_CITY:
				 pad.get("cityVillage");
			case SP_ADDRESS_STATE:
				pad.get("stateProvince");
			case SP_ADDRESS_POSTALCODE:
				pad.get("postalCode");
			case SP_ADDRESS_COUNTRY:
				pad.get("country");
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
		        .ifPresent(c -> criteriaContext.addPredicate(c).finalizeQuery());
	}
	
}
