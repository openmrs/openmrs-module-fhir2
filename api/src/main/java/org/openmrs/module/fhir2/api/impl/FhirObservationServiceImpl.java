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
import lombok.Getter;
import lombok.Setter;
import org.hl7.fhir.r4.model.Observation;
import org.openmrs.Obs;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.api.FhirObservationService;
import org.openmrs.module.fhir2.api.dao.FhirObservationDao;
import org.openmrs.module.fhir2.api.search.SearchQuery;
import org.openmrs.module.fhir2.api.search.param.SearchParameterMap;
import org.openmrs.module.fhir2.api.translators.ObservationTranslator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional
@Getter(AccessLevel.PROTECTED)
@Setter(AccessLevel.PACKAGE)
public class FhirObservationServiceImpl extends BaseFhirService<Observation, org.openmrs.Obs> implements FhirObservationService {
	
	@Autowired
	private FhirObservationDao dao;
	
	@Autowired
	private ObservationTranslator translator;
	
	@Autowired
	private SearchQuery<Obs, Observation, FhirObservationDao, ObservationTranslator> searchQuery;
	
	@Override
	@Transactional(readOnly = true)
	public Observation get(String uuid) {
		return translator.toFhirResource(dao.get(uuid));
	}
	
	@Override
	@Transactional(readOnly = true)
	public IBundleProvider searchForObservations(ReferenceAndListParam encounterReference,
	        ReferenceAndListParam patientReference, ReferenceParam hasMemberReference, TokenAndListParam valueConcept,
	        DateRangeParam valueDateParam, QuantityAndListParam valueQuantityParam, StringAndListParam valueStringParam,
	        DateRangeParam date, TokenAndListParam code, SortSpec sort) {
		
		SearchParameterMap theParams = new SearchParameterMap()
		        .addParameter(FhirConstants.ENCOUNTER_REFERENCE_SEARCH_HANDLER, encounterReference)
		        .addParameter(FhirConstants.PATIENT_REFERENCE_SEARCH_HANDLER, patientReference)
		        .addParameter(FhirConstants.CODED_SEARCH_HANDLER, code)
		        .addParameter(FhirConstants.VALUE_CODED_SEARCH_HANDLER, valueConcept)
		        .addParameter(FhirConstants.HAS_MEMBER_SEARCH_HANDLER, hasMemberReference)
		        .addParameter(FhirConstants.AND_LIST_PARAMS_SEARCH_HANDLER, "valueText", valueStringParam)
		        .addParameter(FhirConstants.AND_LIST_PARAMS_SEARCH_HANDLER, "valueNumeric", valueQuantityParam)
		        .addParameter(FhirConstants.DATE_RANGE_SEARCH_HANDLER, "obsDatetime", date)
		        .addParameter(FhirConstants.DATE_RANGE_SEARCH_HANDLER, "valueDatetime", valueDateParam).setSortSpec(sort);
		
		return searchQuery.getQueryResults(theParams, dao, translator);
	}
}
