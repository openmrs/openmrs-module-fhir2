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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;

import java.util.List;

import ca.uhn.fhir.rest.param.ReferenceAndListParam;
import ca.uhn.fhir.rest.param.ReferenceOrListParam;
import ca.uhn.fhir.rest.param.ReferenceParam;
import ca.uhn.fhir.rest.param.TokenAndListParam;
import ca.uhn.fhir.rest.param.TokenParam;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.Diagnosis;
import org.openmrs.Encounter;
import org.openmrs.api.EncounterService;
import org.openmrs.module.fhir2.BaseFhirContextSensitiveTest;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.api.dao.FhirDiagnosisDao;
import org.openmrs.module.fhir2.api.search.param.SearchParameterMap;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;

public class FhirDiagnosisDaoImplTest extends BaseFhirContextSensitiveTest {
	
	private static final String PATIENT_UUID = "da7f524f-27ce-4bb2-86d6-6d1d05312bd5";
	
	private static final String ENCOUNTER_UUID = "y403fafb-e5e4-42d0-9d11-4f52e89d123r";
	
	private static final String DIAGNOSIS_UUID = "f3a0d4ef-500d-4301-9237-ee2adfed60ec";
	
	private static final String CIEL_URN = "https://openconceptlab.org/orgs/CIEL/sources/CIEL";
	
	private static final String BIPOLAR_CODE = "115924";
	
	@Autowired
	private EncounterService encounterService;
	
	@Autowired
	private ObjectFactory<FhirDiagnosisDao> daoFactory;
	
	private FhirDiagnosisDao dao;
	
	@Before
	public void setUp() {
		executeDataSet("org/openmrs/module/fhir2/api/dao/impl/FhirDiagnosisDaoImplTest_initial_dataset.xml");
		dao = daoFactory.getObject();
	}
	
	@Test
	public void createOrUpdate_shouldSaveDiagnosis() {
		Diagnosis diagnosis = new Diagnosis();
		diagnosis.setUuid(DIAGNOSIS_UUID);
		Encounter encounter = encounterService.getEncounter(3);
		diagnosis.setEncounter(encounter);
		diagnosis.setPatient(encounter.getPatient());
		diagnosis.setRank(1);
		
		dao.createOrUpdate(diagnosis);
		
		Diagnosis result = dao.get(DIAGNOSIS_UUID);
		assertThat(result, notNullValue());
		assertThat(result.getUuid(), equalTo(DIAGNOSIS_UUID));
	}
	
	@Test
	public void search_shouldWorkWithNoParameters() {
		List<Diagnosis> diagnoses = dao.getSearchResults(new SearchParameterMap());
		
		assertThat(diagnoses, notNullValue());
		assertThat(diagnoses, hasSize(greaterThanOrEqualTo(1)));
	}
	
	@Test
	public void search_shouldHandleEmptyParams() {
		SearchParameterMap params = new SearchParameterMap()
		        .addParameter(FhirConstants.PATIENT_REFERENCE_SEARCH_HANDLER, new ReferenceAndListParam())
		        .addParameter(FhirConstants.CODED_SEARCH_HANDLER, new TokenAndListParam());
		
		List<Diagnosis> diagnoses = dao.getSearchResults(params);
		
		assertThat(diagnoses, notNullValue());
		assertThat(diagnoses, hasSize(greaterThanOrEqualTo(1)));
	}
	
	@Test
	public void search_shouldHandleDiagnosisCodeParam() {
		SearchParameterMap params = new SearchParameterMap().addParameter(FhirConstants.CODED_SEARCH_HANDLER,
		    new TokenAndListParam().addAnd(new TokenParam(CIEL_URN, BIPOLAR_CODE)));
		
		List<Diagnosis> diagnoses = dao.getSearchResults(params);
		
		assertThat(diagnoses, notNullValue());
		assertThat(diagnoses, hasSize(equalTo(1)));
		assertThat(diagnoses, everyItem(hasProperty("diagnosis",
		    hasProperty("coded", hasProperty("uuid", equalTo(BIPOLAR_CODE + "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA"))))));
	}
	
	@Test
	public void search_shouldSkipDiagnosisCodeWhenParamNull() {
		SearchParameterMap params = new SearchParameterMap().addParameter(FhirConstants.CODED_SEARCH_HANDLER, null);
		
		List<Diagnosis> diagnoses = dao.getSearchResults(params);
		
		assertThat(diagnoses, notNullValue());
		assertThat(diagnoses, hasSize(greaterThanOrEqualTo(1)));
	}
	
	@Test
	public void search_shouldHandlePatientReferenceParam() {
		SearchParameterMap params = new SearchParameterMap().addParameter(FhirConstants.PATIENT_REFERENCE_SEARCH_HANDLER,
		    new ReferenceAndListParam().addAnd(new ReferenceOrListParam().add(new ReferenceParam(PATIENT_UUID))));
		
		List<Diagnosis> diagnoses = dao.getSearchResults(params);
		
		assertThat(diagnoses, notNullValue());
		assertThat(diagnoses, hasSize(equalTo(1)));
		assertThat(diagnoses, everyItem(hasProperty("patient", hasProperty("uuid", equalTo(PATIENT_UUID)))));
	}
	
	@Test
	public void setupSearchParams_shouldHandleEncounterReferenceParam() {
		SearchParameterMap params = new SearchParameterMap().addParameter(FhirConstants.ENCOUNTER_REFERENCE_SEARCH_HANDLER,
		    new ReferenceAndListParam().addAnd(new ReferenceOrListParam().add(new ReferenceParam(ENCOUNTER_UUID))));
		
		List<Diagnosis> diagnoses = dao.getSearchResults(params);
		
		assertThat(diagnoses, notNullValue());
		assertThat(diagnoses, hasSize(equalTo(1)));
		assertThat(diagnoses, everyItem(hasProperty("encounter", hasProperty("uuid", equalTo(ENCOUNTER_UUID)))));
	}
}
