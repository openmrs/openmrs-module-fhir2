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
import org.hl7.fhir.r4.model.MedicationDispense;
import org.openmrs.annotation.OpenmrsProfile;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.api.FhirMedicationDispenseService;
import org.openmrs.module.fhir2.api.dao.FhirMedicationDispenseDao;
import org.openmrs.module.fhir2.api.search.SearchQuery;
import org.openmrs.module.fhir2.api.search.SearchQueryInclude;
import org.openmrs.module.fhir2.api.search.param.MedicationDispenseSearchParams;
import org.openmrs.module.fhir2.api.search.param.SearchParameterMap;
import org.openmrs.module.fhir2.api.translators.MedicationDispenseTranslator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional
@Setter(AccessLevel.PACKAGE)
@Getter(AccessLevel.PROTECTED)
@OpenmrsProfile(openmrsPlatformVersion = "2.6.* - 2.*")
public class FhirMedicationDispenseServiceImpl_2_6 extends BaseFhirService<MedicationDispense, org.openmrs.MedicationDispense> implements FhirMedicationDispenseService {
	
	@Autowired
	private FhirMedicationDispenseDao<org.openmrs.MedicationDispense> dao;
	
	@Autowired
	private MedicationDispenseTranslator<org.openmrs.MedicationDispense> translator;
	
	@Autowired
	private SearchQueryInclude<MedicationDispense> searchQueryInclude;
	
	@Autowired
	private SearchQuery<org.openmrs.MedicationDispense, MedicationDispense, FhirMedicationDispenseDao<org.openmrs.MedicationDispense>, MedicationDispenseTranslator<org.openmrs.MedicationDispense>, SearchQueryInclude<MedicationDispense>> searchQuery;
	
	@Override
	public IBundleProvider searchMedicationDispenses(MedicationDispenseSearchParams params) {
		
		SearchParameterMap theParams = new SearchParameterMap()
		        .addParameter(FhirConstants.COMMON_SEARCH_HANDLER, FhirConstants.ID_PROPERTY, params.getId())
		        .addParameter(FhirConstants.COMMON_SEARCH_HANDLER, FhirConstants.LAST_UPDATED_PROPERTY,
		            params.getLastUpdated())
		        .addParameter(FhirConstants.PATIENT_REFERENCE_SEARCH_HANDLER, params.getPatient())
		        .addParameter(FhirConstants.ENCOUNTER_REFERENCE_SEARCH_HANDLER, params.getEncounter())
		        .addParameter(FhirConstants.MEDICATION_REQUEST_REFERENCE_SEARCH_HANDLER, params.getMedicationRequest())
		        .addParameter(FhirConstants.INCLUDE_SEARCH_HANDLER, params.getIncludes()).setSortSpec(params.getSort());
		
		return searchQuery.getQueryResults(theParams, dao, translator, searchQueryInclude);
	}
}
