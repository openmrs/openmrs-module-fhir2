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

import ca.uhn.fhir.rest.api.SortSpec;
import ca.uhn.fhir.rest.api.server.IBundleProvider;
import ca.uhn.fhir.rest.param.DateRangeParam;
import ca.uhn.fhir.rest.param.ReferenceAndListParam;
import ca.uhn.fhir.rest.param.TokenAndListParam;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.hl7.fhir.r4.model.AllergyIntolerance;
import org.openmrs.Allergy;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.api.FhirAllergyIntoleranceService;
import org.openmrs.module.fhir2.api.dao.FhirAllergyIntoleranceDao;
import org.openmrs.module.fhir2.api.search.SearchQuery;
import org.openmrs.module.fhir2.api.search.param.SearchParameterMap;
import org.openmrs.module.fhir2.api.translators.AllergyIntoleranceTranslator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional
@Setter(AccessLevel.PACKAGE)
@Getter(AccessLevel.PROTECTED)
public class FhirAllergyIntoleranceServiceImpl extends BaseFhirService<AllergyIntolerance, Allergy> implements FhirAllergyIntoleranceService {
	
	@Autowired
	private AllergyIntoleranceTranslator translator;
	
	@Autowired
	private FhirAllergyIntoleranceDao dao;
	
	@Autowired
	private SearchQuery<org.openmrs.Allergy, AllergyIntolerance, FhirAllergyIntoleranceDao, AllergyIntoleranceTranslator> searchQuery;
	
	@Override
	@Transactional(readOnly = true)
	public IBundleProvider searchForAllergies(ReferenceAndListParam patientReference, TokenAndListParam category,
	        TokenAndListParam allergen, TokenAndListParam severity, TokenAndListParam manifestationCode,
	        TokenAndListParam clinicalStatus, TokenAndListParam id, DateRangeParam lastUpdated, SortSpec sort) {
		
		SearchParameterMap theParams = new SearchParameterMap()
		        .addParameter(FhirConstants.PATIENT_REFERENCE_SEARCH_HANDLER, patientReference)
		        .addParameter(FhirConstants.CATEGORY_SEARCH_HANDLER, category)
		        .addParameter(FhirConstants.ALLERGEN_SEARCH_HANDLER, allergen)
		        .addParameter(FhirConstants.SEVERITY_SEARCH_HANDLER, severity)
		        .addParameter(FhirConstants.CODED_SEARCH_HANDLER, manifestationCode)
		        .addParameter(FhirConstants.BOOLEAN_SEARCH_HANDLER, clinicalStatus)
		        .addParameter(FhirConstants.COMMON_SEARCH_HANDLER, FhirConstants.ID_PROPERTY, id)
		        .addParameter(FhirConstants.COMMON_SEARCH_HANDLER, FhirConstants.LAST_UPDATED_PROPERTY, lastUpdated)
		        .setSortSpec(sort);
		
		return searchQuery.getQueryResults(theParams, dao, translator);
	}
}
