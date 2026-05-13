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

import static lombok.AccessLevel.PROTECTED;

import ca.uhn.fhir.rest.api.server.IBundleProvider;
import lombok.Getter;
import lombok.Setter;
import org.hl7.fhir.r4.model.Encounter;
import org.openmrs.module.fhir2.api.FhirOpenmrsEncounterService;
import org.openmrs.module.fhir2.api.dao.FhirEncounterDao;
import org.openmrs.module.fhir2.api.search.SearchQuery;
import org.openmrs.module.fhir2.api.search.SearchQueryInclude;
import org.openmrs.module.fhir2.api.search.param.SearchParameterMap;
import org.openmrs.module.fhir2.api.translators.EncounterTranslator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class FhirOpenmrsEncounterServiceImpl extends BaseFhirService<Encounter, org.openmrs.Encounter> implements FhirOpenmrsEncounterService {
	
	@Getter(value = PROTECTED)
	@Setter(value = PROTECTED, onMethod_ = @Autowired)
	private FhirEncounterDao dao;
	
	@Getter(value = PROTECTED)
	@Setter(value = PROTECTED, onMethod_ = @Autowired)
	private EncounterTranslator<org.openmrs.Encounter> translator;
	
	@Getter(value = PROTECTED)
	@Setter(value = PROTECTED, onMethod_ = @Autowired)
	private SearchQueryInclude<Encounter> searchQueryInclude;
	
	@Getter(value = PROTECTED)
	@Setter(value = PROTECTED, onMethod_ = @Autowired)
	private SearchQuery<org.openmrs.Encounter, Encounter, FhirEncounterDao, EncounterTranslator<org.openmrs.Encounter>, SearchQueryInclude<Encounter>> searchQuery;
	
	@Override
	public IBundleProvider searchForEncounters(SearchParameterMap theParams) {
		return searchQuery.getQueryResults(theParams, dao, translator, searchQueryInclude);
	}
}
