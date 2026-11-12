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
import org.hl7.fhir.r4.model.Practitioner;
import org.openmrs.Provider;
import org.openmrs.module.fhir2.api.FhirOpenmrsProviderService;
import org.openmrs.module.fhir2.api.dao.FhirPractitionerDao;
import org.openmrs.module.fhir2.api.search.SearchQuery;
import org.openmrs.module.fhir2.api.search.SearchQueryInclude;
import org.openmrs.module.fhir2.api.search.param.SearchParameterMap;
import org.openmrs.module.fhir2.api.translators.PractitionerTranslator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class FhirOpenmrsProviderServiceImpl extends BaseFhirService<Practitioner, Provider> implements FhirOpenmrsProviderService {
	
	@Getter(value = PROTECTED)
	@Setter(value = PROTECTED, onMethod_ = @Autowired)
	private FhirPractitionerDao dao;
	
	@Getter(value = PROTECTED)
	@Setter(value = PROTECTED, onMethod_ = @Autowired)
	private PractitionerTranslator<Provider> translator;
	
	@Getter(value = PROTECTED)
	@Setter(value = PROTECTED, onMethod_ = @Autowired)
	private SearchQueryInclude<Practitioner> searchQueryInclude;
	
	@Getter(value = PROTECTED)
	@Setter(value = PROTECTED, onMethod_ = @Autowired)
	private SearchQuery<Provider, Practitioner, FhirPractitionerDao, PractitionerTranslator<Provider>, SearchQueryInclude<Practitioner>> searchQuery;
	
	@Override
	public IBundleProvider searchForPractitioners(SearchParameterMap theParams) {
		return searchQuery.getQueryResults(theParams, dao, translator, searchQueryInclude);
	}
}
