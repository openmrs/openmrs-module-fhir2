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
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

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
	protected void setupSearchParams(OpenmrsFhirCriteriaContext<Relationship> criteriaContext, SearchParameterMap theParams) {
		criteriaContext.getRoot().join("personA", javax.persistence.criteria.JoinType.INNER).alias("m");
		theParams.getParameters().forEach(entry -> {
			switch (entry.getKey()) {
				case FhirConstants.NAME_SEARCH_HANDLER:
					entry.getValue().forEach(
					    param -> handleNames(criteriaContext, (StringAndListParam) param.getParam(), null, null, "m"));
					break;
				case FhirConstants.GENDER_SEARCH_HANDLER:
					entry.getValue().forEach(param -> handleGender("m.gender", (TokenAndListParam) param.getParam())
					        .ifPresent(criteriaContext::addPredicate));
					criteriaContext.finalizeQuery();
					break;
				case FhirConstants.DATE_RANGE_SEARCH_HANDLER:
					entry.getValue().forEach(param -> handleDateRange("m.birthdate", (DateRangeParam) param.getParam())
					        .ifPresent(criteriaContext::addPredicate));
					criteriaContext.finalizeQuery();
					break;
				case FhirConstants.ADDRESS_SEARCH_HANDLER:
					handleAddresses(criteriaContext, entry);
					break;
				case FhirConstants.COMMON_SEARCH_HANDLER:
					handleCommonSearchParameters(entry.getValue()).ifPresent(criteriaContext::addPredicate);
					break;
			}
		});
	}
	
	@Override
	protected Collection<Order> paramToProps(@Nonnull SortState sortState) {
		OpenmrsFhirCriteriaContext<Relationship> criteriaContext = createCriteriaContext();
		String param = sortState.getParameter();
		
		if (param == null) {
			return null;
		}
		
		CriteriaBuilder cb = sortState.getCriteriaBuilder();
		if (param.startsWith("address") && lacksAlias(cb, "pad")) {
			criteriaContext.getRoot().join("m.addresses", javax.persistence.criteria.JoinType.LEFT).alias("pad");
		} else if (param.equals(SP_NAME) || param.equals(SP_GIVEN) || param.equals(SP_FAMILY)) {
			if (lacksAlias(cb, "pn")) {
				criteriaContext.getRoot().join("m.names", javax.persistence.criteria.JoinType.LEFT).alias("pn");
			}
			
			Root<PersonName> subRoot = criteriaContext.getCriteriaQuery().subquery(Integer.class).from(PersonName.class);
			
			Predicate predicate = criteriaContext.getCriteriaBuilder()
			        .and(criteriaContext.getCriteriaBuilder().equal(criteriaContext.getRoot().get("pn").get("voided"), false),
					        criteriaContext.getCriteriaBuilder().or(
							        criteriaContext.getCriteriaBuilder().and(criteriaContext.getCriteriaBuilder().equal(criteriaContext.getRoot().get("pn").get("preferred"), true),
									        criteriaContext.getCriteriaBuilder().equal(criteriaContext.getRoot().get("pn").get("personNameId"),
			                        criteriaContext.getCriteriaQuery().subquery(Integer.class)
			                                .select(criteriaContext.getCriteriaBuilder().min(criteriaContext.getRoot().get("pn1").get("personNameId")))
			                                .where(criteriaContext.getCriteriaBuilder().and(
					                                criteriaContext.getCriteriaBuilder().equal(subRoot.get("preferred"), true),
					                                criteriaContext.getCriteriaBuilder().equal(subRoot.get("person_id"), criteriaContext.getRoot().get("person_id")))))),
							        criteriaContext.getCriteriaBuilder().and(
									        criteriaContext.getCriteriaBuilder().not(criteriaContext.getCriteriaBuilder().exists(
			                        criteriaContext.getCriteriaQuery().subquery(Integer.class).select(criteriaContext.getRoot().get("pn2").get("personNameId")).where(
					                        criteriaContext.getCriteriaBuilder().and(criteriaContext.getCriteriaBuilder().equal(subRoot.get("pn2").get("preferred"), true),
							                        criteriaContext.getCriteriaBuilder().equal(subRoot.get("pn2").get("person").get("personId"),
			                                    criteriaContext.getRoot().get("personId")))))),
									        criteriaContext.getCriteriaBuilder().equal(criteriaContext.getRoot().get("pn").get("personNameId"),
			                        criteriaContext.getCriteriaQuery().subquery(Integer.class)
			                                .select(criteriaContext.getCriteriaBuilder().min(criteriaContext.getRoot().get("pn3").get("personNameId")))
			                                .where(criteriaContext.getCriteriaBuilder().and(
					                                criteriaContext.getCriteriaBuilder().equal(subRoot.get("pn3").get("preferred"), false),
					                                criteriaContext.getCriteriaBuilder().equal(subRoot.get("pn3").get("person").get("personId"),
			                                        criteriaContext.getRoot().get("personId")))))),
							        criteriaContext.getCriteriaBuilder().isNull(criteriaContext.getRoot().get("pn").get("personNameId"))));
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
						criteriaContext.addOrder(criteriaContext.getCriteriaBuilder().asc(criteriaContext.getRoot().get(property)));
					}
					break;
				case DESC:
					for (String property : properties) {
						criteriaContext.addOrder(criteriaContext.getCriteriaBuilder().desc(criteriaContext.getRoot().get(property)));
					}
					break;
			}
			
		}
		
		return super.paramToProps(sortState);
	}
	
	@Override
	protected String paramToProp(@Nonnull String param) {
		switch (param) {
			case SP_BIRTHDATE:
				return "m.birthdate";
			case SP_ADDRESS_CITY:
				return "pad.cityVillage";
			case SP_ADDRESS_STATE:
				return "pad.stateProvince";
			case SP_ADDRESS_POSTALCODE:
				return "pad.postalCode";
			case SP_ADDRESS_COUNTRY:
				return "pad.country";
			default:
				return super.paramToProp(param);
		}
	}
	
	private void handleAddresses(OpenmrsFhirCriteriaContext<Relationship> criteriaContext, Map.Entry<String, List<PropParam<?>>> entry) {
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

		handlePersonAddress("pad", city, state, postalCode, country).ifPresent(c -> {
			criteriaContext.getRoot().join("m.addresses").alias("pad");
			criteriaContext.addPredicate(c);
			criteriaContext.finalizeQuery();
		});
	}
}
