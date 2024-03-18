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
import org.hl7.fhir.r4.model.ServiceRequest;
import org.openmrs.TestOrder;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.api.FhirServiceRequestService;
import org.openmrs.module.fhir2.api.dao.FhirServiceRequestDao;
import org.openmrs.module.fhir2.api.search.SearchQuery;
import org.openmrs.module.fhir2.api.search.SearchQueryInclude;
import org.openmrs.module.fhir2.api.search.param.SearchParameterMap;
import org.openmrs.module.fhir2.api.search.param.ServiceRequestSearchParams;
import org.openmrs.module.fhir2.api.translators.ServiceRequestTranslator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional
@Getter(AccessLevel.PROTECTED)
@Setter(AccessLevel.PACKAGE)
public class FhirServiceRequestServiceImpl extends BaseFhirService<ServiceRequest, TestOrder> implements FhirServiceRequestService {
	
	@Autowired
	private ServiceRequestTranslator<TestOrder> translator;
	
	@Autowired
	private FhirServiceRequestDao<TestOrder> dao;
	
	@Autowired
	private SearchQueryInclude<ServiceRequest> searchQueryInclude;
	
	@Autowired
	private SearchQuery<TestOrder, ServiceRequest, FhirServiceRequestDao<TestOrder>, ServiceRequestTranslator<TestOrder>, SearchQueryInclude<ServiceRequest>> searchQuery;
	
	@Override
	public IBundleProvider searchForServiceRequests(ServiceRequestSearchParams serviceRequestSearchParams) {
		SearchParameterMap theParams = new SearchParameterMap()
		        .addParameter(FhirConstants.PATIENT_REFERENCE_SEARCH_HANDLER,
		            serviceRequestSearchParams.getPatientReference())
		        .addParameter(FhirConstants.CODED_SEARCH_HANDLER, serviceRequestSearchParams.getCode())
		        .addParameter(FhirConstants.ENCOUNTER_REFERENCE_SEARCH_HANDLER,
		            serviceRequestSearchParams.getEncounterReference())
		        .addParameter(FhirConstants.PARTICIPANT_REFERENCE_SEARCH_HANDLER,
		            serviceRequestSearchParams.getParticipantReference())
		        .addParameter(FhirConstants.DATE_RANGE_SEARCH_HANDLER, serviceRequestSearchParams.getOccurrence())
		        .addParameter(FhirConstants.COMMON_SEARCH_HANDLER, FhirConstants.ID_PROPERTY,
		            serviceRequestSearchParams.getId())
		        .addParameter(FhirConstants.COMMON_SEARCH_HANDLER, FhirConstants.LAST_UPDATED_PROPERTY,
		            serviceRequestSearchParams.getLastUpdated())
		        .addParameter(FhirConstants.INCLUDE_SEARCH_HANDLER, serviceRequestSearchParams.getIncludes())
		        .addParameter(FhirConstants.HAS_SEARCH_HANDLER, serviceRequestSearchParams.getHasAndListParam());
		
		return searchQuery.getQueryResults(theParams, dao, translator, searchQueryInclude);
	}
}
