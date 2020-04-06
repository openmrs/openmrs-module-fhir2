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
import ca.uhn.fhir.rest.param.QuantityAndListParam;
import ca.uhn.fhir.rest.param.ReferenceAndListParam;
import ca.uhn.fhir.rest.param.ReferenceParam;
import ca.uhn.fhir.rest.param.StringAndListParam;
import ca.uhn.fhir.rest.param.TokenAndListParam;
import lombok.AccessLevel;
import lombok.Setter;
import org.hl7.fhir.r4.model.Observation;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.api.FhirObservationService;
import org.openmrs.module.fhir2.api.dao.FhirObservationDao;
import org.openmrs.module.fhir2.api.search.ISearchQuery;
import org.openmrs.module.fhir2.api.search.param.SearchParameterMap;
import org.openmrs.module.fhir2.api.translators.ObservationTranslator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional
@Setter(AccessLevel.PACKAGE)
public class FhirObservationServiceImpl implements FhirObservationService {
	
	@Autowired
	private FhirObservationDao dao;
	
	@Autowired
	private ObservationTranslator observationTranslator;
	
	@Autowired
	private ISearchQuery<FhirObservationDao, ObservationTranslator> searchQuery;
	
	@Override
	@Transactional(readOnly = true)
	public Observation getObservationByUuid(String uuid) {
		return observationTranslator.toFhirResource(dao.getObsByUuid(uuid));
	}
	
	@Override
	@Transactional(readOnly = true)
	public IBundleProvider searchForObservations(ReferenceAndListParam encounterReference,
	        ReferenceAndListParam patientReference, ReferenceParam hasMemberReference, TokenAndListParam valueConcept,
	        DateRangeParam valueDateParam, QuantityAndListParam valueQuantityParam, StringAndListParam valueStringParam,
	        DateRangeParam date, TokenAndListParam code, SortSpec sort) {
		
		SearchParameterMap theParams = new SearchParameterMap();
		theParams.addAndParam(FhirConstants.ENCOUNTER_REFERENCE_SEARCH_HANDLER, encounterReference);
		theParams.addAndParam(FhirConstants.PATIENT_REFERENCE_SEARCH_HANDLER, patientReference);
		theParams.addAndParam(FhirConstants.CODED_SEARCH_HANDLER, code);
		theParams.addAndParam(FhirConstants.VALUE_CODED_SEARCH_HANDLER, valueConcept);
		theParams.addReferenceParam(FhirConstants.HAS_MEMBER_SEARCH_HANDLER, hasMemberReference);
		theParams.addAndParam(FhirConstants.AND_LIST_PARAMS_SEARCH_HANDLER, "valueText", valueStringParam);
		theParams.addAndParam(FhirConstants.AND_LIST_PARAMS_SEARCH_HANDLER, "valueNumeric", valueQuantityParam);
		theParams.addAndParam(FhirConstants.DATE_RANGE_SEARCH_HANDLER, "obsDatetime", date);
		theParams.addAndParam(FhirConstants.DATE_RANGE_SEARCH_HANDLER, "valueDatetime", valueDateParam);
		theParams.setSortSpec(sort);
		
		return searchQuery.getQueryResults(theParams, dao, observationTranslator);
	}
}
