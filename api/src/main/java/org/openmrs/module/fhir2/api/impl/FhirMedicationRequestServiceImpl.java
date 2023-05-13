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
import ca.uhn.fhir.rest.param.DateRangeParam;
import ca.uhn.fhir.rest.param.ReferenceAndListParam;
import ca.uhn.fhir.rest.param.TokenAndListParam;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.hl7.fhir.r4.model.MedicationRequest;
import org.openmrs.DrugOrder;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.api.FhirMedicationRequestService;
import org.openmrs.module.fhir2.api.dao.FhirMedicationRequestDao;
import org.openmrs.module.fhir2.api.search.SearchQuery;
import org.openmrs.module.fhir2.api.search.SearchQueryInclude;
import org.openmrs.module.fhir2.api.search.param.SearchParameterMap;
import org.openmrs.module.fhir2.api.translators.MedicationRequestTranslator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Setter(AccessLevel.PACKAGE)
@Getter(AccessLevel.PROTECTED)
public class FhirMedicationRequestServiceImpl extends BaseFhirService<MedicationRequest, org.openmrs.DrugOrder> implements FhirMedicationRequestService {
	
	@Autowired
	private MedicationRequestTranslator translator;
	
	@Autowired
	private FhirMedicationRequestDao dao;
	
	@Autowired
	private SearchQueryInclude<MedicationRequest> searchQueryInclude;
	
	@Autowired
	private SearchQuery<DrugOrder, MedicationRequest, FhirMedicationRequestDao, MedicationRequestTranslator, SearchQueryInclude<MedicationRequest>> searchQuery;
	
	@Override
	public IBundleProvider searchForMedicationRequests(ReferenceAndListParam patientReference,
	        ReferenceAndListParam encounterReference, TokenAndListParam code, ReferenceAndListParam participantReference,
	        ReferenceAndListParam medicationReference, TokenAndListParam id, TokenAndListParam status,
	        TokenAndListParam fulfillerStatus, DateRangeParam lastUpdated, HashSet<Include> includes,
	        HashSet<Include> revIncludes) {
		
		SearchParameterMap theParams = new SearchParameterMap()
		        .addParameter(FhirConstants.ENCOUNTER_REFERENCE_SEARCH_HANDLER, encounterReference)
		        .addParameter(FhirConstants.PATIENT_REFERENCE_SEARCH_HANDLER, patientReference)
		        .addParameter(FhirConstants.CODED_SEARCH_HANDLER, code)
		        .addParameter(FhirConstants.PARTICIPANT_REFERENCE_SEARCH_HANDLER, participantReference)
		        .addParameter(FhirConstants.MEDICATION_REFERENCE_SEARCH_HANDLER, medicationReference)
		        .addParameter(FhirConstants.COMMON_SEARCH_HANDLER, FhirConstants.ID_PROPERTY, id)
		        .addParameter(FhirConstants.STATUS_SEARCH_HANDLER, status)
		        .addParameter(FhirConstants.FULFILLER_STATUS_SEARCH_HANDLER, fulfillerStatus)
		        .addParameter(FhirConstants.COMMON_SEARCH_HANDLER, FhirConstants.LAST_UPDATED_PROPERTY, lastUpdated)
		        .addParameter(FhirConstants.INCLUDE_SEARCH_HANDLER, includes)
		        .addParameter(FhirConstants.REVERSE_INCLUDE_SEARCH_HANDLER, revIncludes);
		
		return searchQuery.getQueryResults(theParams, dao, translator, searchQueryInclude);
	}
	
}
