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
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Predicate;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import ca.uhn.fhir.rest.param.DateRangeParam;
import ca.uhn.fhir.rest.param.TokenAndListParam;
import org.openmrs.Person;
import org.openmrs.PersonAttribute;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.api.dao.FhirPersonDao;
import org.openmrs.module.fhir2.api.dao.internals.OpenmrsFhirCriteriaContext;
import org.openmrs.module.fhir2.api.search.param.SearchParameterMap;
import org.springframework.stereotype.Component;

@Component
public class FhirPersonDaoImpl extends BasePersonDao<Person> implements FhirPersonDao {
	
	@Override
	public List<PersonAttribute> getActiveAttributesByPersonAndAttributeTypeUuid(@Nonnull Person person,
	        @Nonnull String personAttributeTypeUuid) {
		OpenmrsFhirCriteriaContext<PersonAttribute, PersonAttribute> criteriaContext = createCriteriaContext(
		    PersonAttribute.class);
		CriteriaBuilder cb = criteriaContext.getCriteriaBuilder();
		
		criteriaContext.addJoin("person", "p",
		    (from) -> cb.and(cb.equal(from.get("personId"), person.getId()), cb.equal(from.get("personVoided"), false)));
		criteriaContext.addJoin("attributeType", "pat",
		    (from) -> cb.and(cb.equal(from.get("uuid"), personAttributeTypeUuid), cb.equal(from.get("retired"), false)));
		criteriaContext.addPredicate(cb.equal(criteriaContext.getRoot().get("voided"), false));
		
		return criteriaContext.getEntityManager().createQuery(criteriaContext.finalizeQuery()).getResultList();
	}
	
	@Override
	protected <U> void setupSearchParams(@Nonnull OpenmrsFhirCriteriaContext<Person, U> criteriaContext,
	        @Nonnull SearchParameterMap theParams) {
		theParams.getParameters().forEach(entry -> {
			switch (entry.getKey()) {
				case FhirConstants.NAME_SEARCH_HANDLER:
					entry.getValue().forEach(param -> handleNames(criteriaContext, entry.getValue()));
					break;
				case FhirConstants.GENDER_SEARCH_HANDLER:
					entry.getValue()
					        .forEach(param -> getSearchQueryHelper()
					                .handleGender(criteriaContext, getPersonProperty(criteriaContext),
					                    FhirConstants.GENDER_PROPERTY, (TokenAndListParam) param.getParam())
					                .ifPresent(criteriaContext::addPredicate));
					break;
				case FhirConstants.DATE_RANGE_SEARCH_HANDLER:
					entry.getValue()
					        .forEach(param -> getSearchQueryHelper()
					                .handleDateRange(criteriaContext, "birthdate", (DateRangeParam) param.getParam())
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
	
	@Override
	protected <T, U> Optional<Predicate> handleLastUpdated(@Nonnull OpenmrsFhirCriteriaContext<T, U> criteriaContext,
	        DateRangeParam param) {
		return Optional.of(criteriaContext.getCriteriaBuilder().or(toCriteriaArray(
		    getSearchQueryHelper().handleDateRange(criteriaContext, "personDateChanged", param),
		    Optional.of(criteriaContext.getCriteriaBuilder()
		            .and(toCriteriaArray(Stream.of(
		                Optional.of(
		                    criteriaContext.getCriteriaBuilder().isNull(criteriaContext.getRoot().get("personDateChanged"))),
		                getSearchQueryHelper().handleDateRange(criteriaContext, "personDateCreated", param))))))));
	}
	
	@Override
	protected <U> void handleVoidable(@Nonnull OpenmrsFhirCriteriaContext<Person, U> criteriaContext) {
		criteriaContext.addPredicate(
		    criteriaContext.getCriteriaBuilder().equal(criteriaContext.getRoot().get("personVoided"), false));
	}
	
}
