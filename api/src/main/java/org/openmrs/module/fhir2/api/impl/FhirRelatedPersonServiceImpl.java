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
import org.hl7.fhir.r4.model.RelatedPerson;
import org.openmrs.Relationship;
import org.openmrs.module.fhir2.api.FhirRelatedPersonService;
import org.openmrs.module.fhir2.api.dao.FhirRelatedPersonDao;
import org.openmrs.module.fhir2.api.search.SearchQuery;
import org.openmrs.module.fhir2.api.search.SearchQueryInclude;
import org.openmrs.module.fhir2.api.search.param.RelatedPersonSearchParams;
import org.openmrs.module.fhir2.api.translators.RelatedPersonTranslator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional
@Getter(AccessLevel.PROTECTED)
@Setter(AccessLevel.PACKAGE)
public class FhirRelatedPersonServiceImpl extends BaseFhirService<RelatedPerson, Relationship> implements FhirRelatedPersonService {
	
	@Autowired
	private FhirRelatedPersonDao dao;
	
	@Autowired
	private RelatedPersonTranslator translator;
	
	@Autowired
	private SearchQueryInclude<RelatedPerson> searchQueryInclude;
	
	@Autowired
	private SearchQuery<Relationship, RelatedPerson, FhirRelatedPersonDao, RelatedPersonTranslator, SearchQueryInclude<RelatedPerson>> searchQuery;
	
	@Override
	public IBundleProvider searchForRelatedPeople(RelatedPersonSearchParams relatedPersonSearchParams) {
		return searchQuery.getQueryResults(relatedPersonSearchParams.toSearchParameterMap(), dao, translator,
		    searchQueryInclude);
	}
	
}
