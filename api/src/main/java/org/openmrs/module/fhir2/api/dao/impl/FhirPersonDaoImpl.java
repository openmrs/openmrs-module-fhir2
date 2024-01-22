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

import static org.hibernate.criterion.Restrictions.eq;

import javax.annotation.Nonnull;
import javax.persistence.criteria.Predicate;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import ca.uhn.fhir.rest.param.DateRangeParam;
import ca.uhn.fhir.rest.param.TokenAndListParam;
import lombok.AccessLevel;
import lombok.Setter;
import org.hibernate.sql.JoinType;
import org.openmrs.Person;
import org.openmrs.PersonAttribute;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.api.dao.FhirPersonDao;
import org.openmrs.module.fhir2.api.search.param.SearchParameterMap;
import org.springframework.stereotype.Component;

@Component
@Setter(AccessLevel.PACKAGE)
public class FhirPersonDaoImpl extends BasePersonDao<Person> implements FhirPersonDao {
	
	@Override
	@SuppressWarnings("unchecked")
	public List<PersonAttribute> getActiveAttributesByPersonAndAttributeTypeUuid(@Nonnull Person person,
	        @Nonnull String personAttributeTypeUuid) {
		return (List<PersonAttribute>) getSessionFactory().getCurrentSession().createCriteria(PersonAttribute.class)
		        .createAlias("person", "p", JoinType.INNER_JOIN, eq("p.id", person.getId()))
		        .createAlias("attributeType", "pat").add(eq("pat.uuid", personAttributeTypeUuid)).add(eq("voided", false))
		        .list();
	}
	
	@Override
	protected <U> void setupSearchParams(OpenmrsFhirCriteriaContext<Person,U> criteriaContext, SearchParameterMap theParams) {
		theParams.getParameters().forEach(entry -> {
			switch (entry.getKey()) {
				case FhirConstants.NAME_SEARCH_HANDLER:
					entry.getValue().forEach(param -> handleNames(criteriaContext, entry.getValue()));
					break;
				case FhirConstants.GENDER_SEARCH_HANDLER:
					entry.getValue().forEach(param -> handleGender(criteriaContext, getPersonProperty(criteriaContext),
					    "gender", (TokenAndListParam) param.getParam()).ifPresent(c -> criteriaContext.addPredicate(c).finalizeQuery()));
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
					handleCommonSearchParameters(criteriaContext, entry.getValue()).ifPresent(c -> criteriaContext.addPredicate(c).finalizeQuery());
					break;
			}
		});
	}
	
	@Override
	protected <T,U> Optional<Predicate> handleLastUpdated(OpenmrsFhirCriteriaContext<T,U> criteriaContext,
	        DateRangeParam param) {
		return Optional.of(criteriaContext.getCriteriaBuilder().or(toCriteriaArray(
		    handleDateRange(criteriaContext, "personDateChanged", param),
		    Optional.of(criteriaContext.getCriteriaBuilder()
		            .and(toCriteriaArray(Stream.of(
		                Optional.of(
		                    criteriaContext.getCriteriaBuilder().isNull(criteriaContext.getRoot().get("personDateChanged"))),
		                handleDateRange(criteriaContext, "personDateCreated", param))))))));
	}
	
	@Override
	protected <U> void handleVoidable(OpenmrsFhirCriteriaContext<Person,U> criteriaContext) {
		criteriaContext.addPredicate(
		    criteriaContext.getCriteriaBuilder().equal(criteriaContext.getRoot().get("personVoided"), false));
	}
	
}
