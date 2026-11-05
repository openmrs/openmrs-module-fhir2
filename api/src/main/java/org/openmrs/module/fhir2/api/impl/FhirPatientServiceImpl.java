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

import java.util.HashSet;

import ca.uhn.fhir.model.api.Include;
import ca.uhn.fhir.rest.api.server.IBundleProvider;
import ca.uhn.fhir.rest.param.TokenAndListParam;
import ca.uhn.fhir.rest.param.TokenParam;
import org.hl7.fhir.r4.model.Patient;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.api.FhirPatientService;
import org.openmrs.module.fhir2.api.search.param.OpenmrsPatientSearchParams;
import org.openmrs.module.fhir2.api.search.param.PatientSearchParams;
import org.openmrs.module.fhir2.api.search.param.SearchParameterMap;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class FhirPatientServiceImpl extends BaseCompositeFhirService<Patient> implements FhirPatientService {
	
	@Override
	@Transactional(readOnly = true)
	public IBundleProvider searchForPatients(PatientSearchParams patientSearchParams) {
		return doSearch(patientSearchParams.toSearchParameterMap());
	}
	
	@Override
	@Transactional(readOnly = true)
	public IBundleProvider searchForPatients(OpenmrsPatientSearchParams patientSearchParams) {
		return doSearch(patientSearchParams.toSearchParameterMap());
	}
	
	@Override
	@Transactional(readOnly = true)
	public IBundleProvider getPatientEverything(TokenParam patientId) {
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.EVERYTHING_SEARCH_HANDLER, "")
		        .addParameter(FhirConstants.COMMON_SEARCH_HANDLER, FhirConstants.ID_PROPERTY,
		            new TokenAndListParam().addAnd(patientId));
		
		populateEverythingOperationParams(theParams);
		return doSearch(theParams);
	}
	
	@Override
	@Transactional(readOnly = true)
	public IBundleProvider getPatientEverything() {
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.EVERYTHING_SEARCH_HANDLER, "");
		
		populateEverythingOperationParams(theParams);
		return doSearch(theParams);
	}
	
	private void populateEverythingOperationParams(SearchParameterMap theParams) {
		HashSet<Include> revIncludes = new HashSet<>();
		
		revIncludes.add(new Include(FhirConstants.OBSERVATION + ":" + FhirConstants.INCLUDE_PATIENT_PARAM));
		revIncludes.add(new Include(FhirConstants.ALLERGY_INTOLERANCE + ":" + FhirConstants.INCLUDE_PATIENT_PARAM));
		revIncludes.add(new Include(FhirConstants.DIAGNOSTIC_REPORT + ":" + FhirConstants.INCLUDE_PATIENT_PARAM));
		revIncludes.add(new Include(FhirConstants.ENCOUNTER + ":" + FhirConstants.INCLUDE_PATIENT_PARAM));
		revIncludes.add(new Include(FhirConstants.MEDICATION_REQUEST + ":" + FhirConstants.INCLUDE_PATIENT_PARAM));
		revIncludes.add(new Include(FhirConstants.SERVICE_REQUEST + ":" + FhirConstants.INCLUDE_PATIENT_PARAM));
		revIncludes.add(new Include(FhirConstants.PROCEDURE_REQUEST + ":" + FhirConstants.INCLUDE_PATIENT_PARAM));
		
		theParams.addParameter(FhirConstants.REVERSE_INCLUDE_SEARCH_HANDLER, revIncludes);
	}
}
