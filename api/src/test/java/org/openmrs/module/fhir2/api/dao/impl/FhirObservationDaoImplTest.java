/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.fhir2.api.dao.impl;

import static org.exparity.hamcrest.date.DateMatchers.sameOrAfter;
import static org.exparity.hamcrest.date.DateMatchers.sameOrBefore;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

import javax.inject.Inject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import ca.uhn.fhir.rest.api.SortOrderEnum;
import ca.uhn.fhir.rest.api.SortSpec;
import ca.uhn.fhir.rest.param.ReferenceParam;
import ca.uhn.fhir.rest.param.TokenAndListParam;
import ca.uhn.fhir.rest.param.TokenParam;
import org.hl7.fhir.r4.model.Patient;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.Obs;
import org.openmrs.module.fhir2.TestFhirSpringConfiguration;
import org.openmrs.test.BaseModuleContextSensitiveTest;
import org.springframework.test.context.ContextConfiguration;

@ContextConfiguration(classes = TestFhirSpringConfiguration.class, inheritLocations = false)
public class FhirObservationDaoImplTest extends BaseModuleContextSensitiveTest {
	
	private static final String OBS_DATA_XML = "org/openmrs/module/fhir2/api/dao/impl/FhirObservationDaoImplTest_initial_data_suppl.xml";
	
	private static final String OBS_UUID = "39fb7f47-e80a-4056-9285-bd798be13c63";
	
	private static final String BAD_OBS_UUID = "121b73a6-e1a4-4424-8610-d5765bf2fdf7";
	
	private static final String OBS_CONCEPT_ID = "5089";
	
	private static final String PATIENT_UUID = "5946f880-b197-400b-9caa-a3c661d23041";
	
	private static final String PATIENT_GIVEN_NAME = "Collet";
	
	private static final String PATIENT_FAMILY_NAME = "Chebaskwony";
	
	private static final String PATIENT_IDENTIFIER = "6TS-4";
	
	private static final String ENCOUNTER_UUID = "6519d653-393b-4118-9c83-a3715b82d4ac";
	
	private static final String SNOMED_SYSTEM_URI = "http://snomed.info/sct";
	
	private static final String OBS_SNOMED_CODE = "2332523";
	
	@Inject
	FhirObservationDaoImpl dao;
	
	@Before
	public void setup() throws Exception {
		executeDataSet(OBS_DATA_XML);
	}
	
	@Test
	public void getObsByUuid_shouldGetObsByUuid() {
		Obs result = dao.getObsByUuid(OBS_UUID);
		
		assertThat(result, notNullValue());
		assertThat(result.getUuid(), equalTo(OBS_UUID));
	}
	
	@Test
	public void getObsByUuid_shouldReturnNullIfObsNotFoundByUuid() {
		Obs result = dao.getObsByUuid(BAD_OBS_UUID);
		
		assertThat(result, nullValue());
	}
	
	@Test
	public void searchForObs_shouldSearchForObsByConceptId() {
		TokenAndListParam code = new TokenAndListParam();
		TokenParam codingToken = new TokenParam();
		codingToken.setValue(OBS_CONCEPT_ID);
		code.addAnd(codingToken);
		
		Collection<Obs> results = dao.searchForObservations(null, null, code, null);
		
		assertThat(results, notNullValue());
		assertThat(results, not(empty()));
		assertThat(results, hasItem(hasProperty("uuid", equalTo(OBS_UUID))));
	}
	
	@Test
	public void searchForObs_shouldReturnObsByConceptMapping() {
		TokenAndListParam code = new TokenAndListParam();
		TokenParam codingToken = new TokenParam();
		codingToken.setSystem(SNOMED_SYSTEM_URI);
		codingToken.setValue(OBS_SNOMED_CODE);
		code.addAnd(codingToken);
		
		Collection<Obs> results = dao.searchForObservations(null, null, code, null);
		
		assertThat(results, notNullValue());
		assertThat(results, not(empty()));
		assertThat(results, hasItem(hasProperty("uuid", equalTo(OBS_UUID))));
	}
	
	@Test
	public void searchForObs_shouldReturnObsByPatientUuid() {
		ReferenceParam patientReference = new ReferenceParam();
		patientReference.setChain("");
		patientReference.setValue(PATIENT_UUID);
		
		Collection<Obs> results = dao.searchForObservations(null, patientReference, null, null);
		
		assertThat(results, notNullValue());
		assertThat(results, not(empty()));
		assertThat(results, hasItem(hasProperty("uuid", equalTo(OBS_UUID))));
	}
	
	@Test
	public void searchForObs_shouldReturnObsByPatientGivenName() {
		ReferenceParam patientReference = new ReferenceParam();
		patientReference.setChain(Patient.SP_GIVEN);
		patientReference.setValue(PATIENT_GIVEN_NAME);
		
		Collection<Obs> results = dao.searchForObservations(null, patientReference, null, null);
		
		assertThat(results, notNullValue());
		assertThat(results, not(empty()));
		assertThat(results, hasItem(hasProperty("uuid", equalTo(OBS_UUID))));
	}
	
	@Test
	public void searchForObs_shouldReturnObsByPatientFamilyName() {
		ReferenceParam patientReference = new ReferenceParam();
		patientReference.setChain(Patient.SP_FAMILY);
		patientReference.setValue(PATIENT_FAMILY_NAME);
		
		Collection<Obs> results = dao.searchForObservations(null, patientReference, null, null);
		
		assertThat(results, notNullValue());
		assertThat(results, not(empty()));
		assertThat(results, hasItem(hasProperty("uuid", equalTo(OBS_UUID))));
	}
	
	@Test
	public void searchForObs_shouldReturnObsByPatientName() {
		ReferenceParam patientReference = new ReferenceParam();
		patientReference.setChain(Patient.SP_NAME);
		patientReference.setValue(PATIENT_GIVEN_NAME + " " + PATIENT_FAMILY_NAME);
		
		Collection<Obs> results = dao.searchForObservations(null, patientReference, null, null);
		
		assertThat(results, notNullValue());
		assertThat(results, not(empty()));
		assertThat(results, hasItem(hasProperty("uuid", equalTo(OBS_UUID))));
	}
	
	@Test
	public void searchForObs_shouldReturnObsByPatientIdentifier() {
		ReferenceParam patientReference = new ReferenceParam();
		patientReference.setChain(Patient.SP_IDENTIFIER);
		patientReference.setValue(PATIENT_IDENTIFIER);
		
		Collection<Obs> results = dao.searchForObservations(null, patientReference, null, null);
		
		assertThat(results, notNullValue());
		assertThat(results, not(empty()));
		assertThat(results, hasItem(hasProperty("uuid", equalTo(OBS_UUID))));
	}
	
	@Test
	public void searchForObs_shouldReturnObsByEncounter() {
		ReferenceParam encounterReference = new ReferenceParam();
		encounterReference.setValue(ENCOUNTER_UUID);
		
		Collection<Obs> results = dao.searchForObservations(encounterReference, null, null, null);
		
		assertThat(results, notNullValue());
		assertThat(results, not(empty()));
		assertThat(results, hasItem(hasProperty("uuid", equalTo(OBS_UUID))));
	}
	
	@Test
	public void searchForObs_shouldSortObsAsRequested() {
		SortSpec sort = new SortSpec();
		sort.setParamName("date");
		sort.setOrder(SortOrderEnum.ASC);
		
		Collection<Obs> results = dao.searchForObservations(null, null, null, sort);
		
		assertThat(results, notNullValue());
		assertThat(results, not(empty()));
		assertThat(results.size(), greaterThan(1));
		
		List<Obs> resultsList = new ArrayList<>(results);
		// pair-wise compare of all obs by date
		for (int i = 1; i < resultsList.size(); i++) {
			assertThat(resultsList.get(i - 1).getObsDatetime(), sameOrBefore(resultsList.get(i).getObsDatetime()));
		}
		
		sort.setOrder(SortOrderEnum.DESC);
		
		results = dao.searchForObservations(null, null, null, sort);
		
		assertThat(results, notNullValue());
		assertThat(results, not(empty()));
		assertThat(results.size(), greaterThan(1));
		
		resultsList = new ArrayList<>(results);
		// pair-wise compare of all obs by date
		for (int i = 1; i < resultsList.size(); i++) {
			assertThat(resultsList.get(i - 1).getObsDatetime(), sameOrAfter(resultsList.get(i).getObsDatetime()));
		}
	}
}
