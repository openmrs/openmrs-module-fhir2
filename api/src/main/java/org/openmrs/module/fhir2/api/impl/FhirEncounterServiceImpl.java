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
import org.hl7.fhir.r4.model.Encounter;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.api.FhirEncounterService;
import org.openmrs.module.fhir2.api.search.param.EncounterSearchParams;
import org.openmrs.module.fhir2.api.search.param.SearchParameterMap;
import org.springframework.stereotype.Component;

@Component
public class FhirEncounterServiceImpl extends BaseCompositeFhirService<Encounter> implements FhirEncounterService {
	
	@Override
	public IBundleProvider searchForEncounters(EncounterSearchParams searchParameters) {
		return doSearch(searchParameters.toSearchParameterMap());
	}
	
	@Override
	public IBundleProvider getEncounterEverything(TokenParam encounterId) {
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.EVERYTHING_SEARCH_HANDLER, "")
		        .addParameter(FhirConstants.COMMON_SEARCH_HANDLER, FhirConstants.ID_PROPERTY,
		            new TokenAndListParam().addAnd(encounterId));
		
		populateReverseIncludeForEverythingOperationParams(theParams);
		populateIncludeForEverythingOperationParams(theParams);
		
		return doSearch(theParams);
	}
	
	private void populateReverseIncludeForEverythingOperationParams(SearchParameterMap theParams) {
		HashSet<Include> revIncludes = new HashSet<>();
		
		revIncludes.add(new Include(FhirConstants.OBSERVATION + ":" + FhirConstants.INCLUDE_ENCOUNTER_PARAM));
		revIncludes.add(new Include(FhirConstants.DIAGNOSTIC_REPORT + ":" + FhirConstants.INCLUDE_ENCOUNTER_PARAM));
		revIncludes.add(new Include(FhirConstants.MEDICATION_REQUEST + ":" + FhirConstants.INCLUDE_ENCOUNTER_PARAM));
		revIncludes.add(new Include(FhirConstants.SERVICE_REQUEST + ":" + FhirConstants.INCLUDE_ENCOUNTER_PARAM));
		
		theParams.addParameter(FhirConstants.REVERSE_INCLUDE_SEARCH_HANDLER, revIncludes);
	}
	
	private void populateIncludeForEverythingOperationParams(SearchParameterMap theParams) {
		HashSet<Include> includes = new HashSet<>();
		
		includes.add(new Include(FhirConstants.ENCOUNTER + ":" + FhirConstants.INCLUDE_PATIENT_PARAM));
		includes.add(new Include(FhirConstants.ENCOUNTER + ":" + FhirConstants.INCLUDE_LOCATION_PARAM));
		includes.add(new Include(FhirConstants.ENCOUNTER + ":" + FhirConstants.INCLUDE_PARTICIPANT_PARAM));
		
		theParams.addParameter(FhirConstants.INCLUDE_SEARCH_HANDLER, includes);
	}
}
