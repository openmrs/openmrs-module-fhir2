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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import ca.uhn.fhir.rest.api.server.IBundleProvider;
import ca.uhn.fhir.rest.param.ReferenceAndListParam;
import ca.uhn.fhir.rest.param.ReferenceOrListParam;
import ca.uhn.fhir.rest.param.ReferenceParam;
import org.hl7.fhir.r4.model.Observation;
import org.hl7.fhir.r4.model.Patient;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openmrs.Obs;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.api.dao.FhirObservationDao;
import org.openmrs.module.fhir2.api.search.ISearchQuery;
import org.openmrs.module.fhir2.api.search.param.SearchParameterMap;
import org.openmrs.module.fhir2.api.translators.ObservationTranslator;

@RunWith(MockitoJUnitRunner.class)
public class FhirObservationServiceImplTest extends BaseIBundleProviderTest<Observation> {
	
	private static final String OBS_UUID = "12345-abcde-12345";
	
	private static final String PATIENT_GIVEN_NAME = "Clement";
	
	@Mock
	private FhirObservationDao dao;
	
	@Mock
	private ISearchQuery<FhirObservationDao, ObservationTranslator> searchQuery;
	
	@Mock
	private ObservationTranslator observationTranslator;
	
	private FhirObservationServiceImpl fhirObservationService;
	
	@Before
	public void setup() {
		fhirObservationService = new FhirObservationServiceImpl();
		fhirObservationService.setDao(dao);
		fhirObservationService.setSearchQuery(searchQuery);
		fhirObservationService.setObservationTranslator(observationTranslator);
	}
	
	@Test
	public void getObservationByUuid_shouldReturnObservationByUuid() {
		Obs obs = new Obs();
		obs.setUuid(OBS_UUID);
		Observation observation = new Observation();
		observation.setId(OBS_UUID);
		when(dao.getObsByUuid(OBS_UUID)).thenReturn(obs);
		when(observationTranslator.toFhirResource(obs)).thenReturn(observation);
		
		Observation result = fhirObservationService.getObservationByUuid(OBS_UUID);
		
		assertThat(result, notNullValue());
		assertThat(result.getId(), equalTo(OBS_UUID));
	}
	
	@Test
	public void searchForObservations_shouldReturnObservationsByParameters() {
		Observation observation = new Observation();
		observation.setId(OBS_UUID);
		
		ReferenceAndListParam patientReference = new ReferenceAndListParam();
		ReferenceParam patient = new ReferenceParam();
		
		patient.setValue(PATIENT_GIVEN_NAME);
		patient.setChain(Patient.SP_GIVEN);
		
		patientReference.addValue(new ReferenceOrListParam().add(patient));
		SearchParameterMap theParams = new SearchParameterMap();
		theParams.addAndParam(FhirConstants.PATIENT_REFERENCE_SEARCH_HANDLER, patientReference);
		
		when(searchQuery.getQueryResults(any(), any(), any())).thenReturn(getQueryResults(observation));
		
		IBundleProvider results = fhirObservationService.searchForObservations(null, patientReference, null, null, null,
		    null, null, null, null, null);
		
		assertThat(get(results), not(empty()));
		assertThat(get(results), hasSize(equalTo(1)));
	}
}
