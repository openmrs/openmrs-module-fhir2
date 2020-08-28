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

import static org.hibernate.criterion.Restrictions.and;
import static org.hibernate.criterion.Restrictions.eq;
import static org.hibernate.criterion.Restrictions.isNull;
import static org.hibernate.criterion.Restrictions.or;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import ca.uhn.fhir.rest.param.DateRangeParam;
import ca.uhn.fhir.rest.param.TokenAndListParam;
import lombok.AccessLevel;
import lombok.Setter;
import org.hibernate.Criteria;
import org.hibernate.criterion.Criterion;
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
	public List<PersonAttribute> getActiveAttributesByPersonAndAttributeTypeUuid(Person person,
	        String personAttributeTypeUuid) {
		return (List<PersonAttribute>) getSessionFactory().getCurrentSession().createCriteria(PersonAttribute.class)
		        .createAlias("person", "p", JoinType.INNER_JOIN, eq("p.id", person.getId()))
		        .createAlias("attributeType", "pat").add(eq("pat.uuid", personAttributeTypeUuid)).add(eq("voided", false))
		        .list();
	}
	
	@Override
	protected void setupSearchParams(Criteria criteria, SearchParameterMap theParams) {
		theParams.getParameters().forEach(entry -> {
			switch (entry.getKey()) {
				case FhirConstants.NAME_SEARCH_HANDLER:
					entry.getValue().forEach(param -> handleNames(criteria, entry.getValue()));
					break;
				case FhirConstants.GENDER_SEARCH_HANDLER:
					entry.getValue().forEach(
					    param -> handleGender("gender", (TokenAndListParam) param.getParam()).ifPresent(criteria::add));
					break;
				case FhirConstants.DATE_RANGE_SEARCH_HANDLER:
					entry.getValue().forEach(
					    param -> handleDateRange("birthdate", (DateRangeParam) param.getParam()).ifPresent(criteria::add));
					break;
				case FhirConstants.ADDRESS_SEARCH_HANDLER:
					handleAddresses(criteria, entry);
					break;
				case FhirConstants.COMMON_SEARCH_HANDLER:
					handleCommonSearchParameters(entry.getValue()).ifPresent(criteria::add);
					break;
			}
		});
	}
	
	@Override
	protected Optional<Criterion> handleLastUpdated(DateRangeParam param) {
		return Optional.of(or(toCriteriaArray(handleDateRange("personDateChanged", param), Optional.of(and(toCriteriaArray(
		    Stream.of(Optional.of(isNull("personDateChanged")), handleDateRange("personDateCreated", param))))))));
	}
	
	@Override
	protected String getSqlAlias() {
		return "this_";
	}
	
	@Override
	protected void handleVoidable(Criteria criteria) {
		criteria.add(eq("personVoided", false));
	}
	
	@Override
	protected Person voidObject(Person object) {
		object.setPersonVoided(true);
		object.setPersonVoidReason("Voided via FHIR API");
		return object;
	}
}
