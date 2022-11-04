/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.fhir2.api.impl;

import ca.uhn.fhir.rest.api.server.IBundleProvider;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.hl7.fhir.r4.model.Person;
import org.openmrs.module.fhir2.api.FhirPersonService;
import org.openmrs.module.fhir2.api.dao.FhirPersonDao;
import org.openmrs.module.fhir2.api.search.SearchQuery;
import org.openmrs.module.fhir2.api.search.SearchQueryInclude;
import org.openmrs.module.fhir2.api.search.param.PersonSearchParams;
import org.openmrs.module.fhir2.api.translators.PersonTranslator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional
@Getter(AccessLevel.PROTECTED)
@Setter(AccessLevel.PACKAGE)
public class FhirPersonServiceImpl extends BaseFhirService<Person, org.openmrs.Person> implements FhirPersonService {
	
	@Autowired
	private FhirPersonDao dao;
	
	@Autowired
	private PersonTranslator translator;
	
	@Autowired
	private SearchQueryInclude<Person> searchQueryInclude;
	
	@Autowired
	private SearchQuery<org.openmrs.Person, Person, FhirPersonDao, PersonTranslator, SearchQueryInclude<Person>> searchQuery;
	
	@Override
	public IBundleProvider searchForPeople(PersonSearchParams personSearchParams) {
		return searchQuery.getQueryResults(personSearchParams.toSearchParameterMap(), dao, translator, searchQueryInclude);
	}
	
	@Override
	protected boolean isVoided(org.openmrs.Person person) {
		return person.getPersonVoided();
	}
}
