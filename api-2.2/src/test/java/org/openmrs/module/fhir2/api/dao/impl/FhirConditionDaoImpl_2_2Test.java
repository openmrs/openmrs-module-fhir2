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

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.hibernate.SessionFactory;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.openmrs.CodedOrFreeText;
import org.openmrs.Condition;
import org.openmrs.ConditionClinicalStatus;
import org.openmrs.ConditionVerificationStatus;
import org.openmrs.api.ConceptService;
import org.openmrs.api.PatientService;
import org.openmrs.module.fhir2.TestFhirSpringConfiguration;
import org.openmrs.module.fhir2.api.util.LocalDateTimeFactory;
import org.openmrs.test.BaseModuleContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;

@ContextConfiguration(classes = TestFhirSpringConfiguration.class, inheritLocations = false)
public class FhirConditionDaoImpl_2_2Test extends BaseModuleContextSensitiveTest {
	
	private static final String CONDITION_UUID = "2cc6880e-2c46-15e4-9038-a6c5e4d22fb7";
	
	private static final String NEW_CONDITION_UUID = "3dd6880e-2c46-15e4-9038-a6c5e4d22gh8";
	
	private static final String EXISTING_CONDITION_UUID = "604953c5-b5c6-4e1e-be95-e37d8f392046";
	
	private static final String WRONG_CONDITION_UUID = "430bbb70-6a9c-4e1e-badb-9d1034b1b5e9";
	
	private static final String CONDITION_INITIAL_DATA_XML = "org/openmrs/module/fhir2/api/dao/impl/FhirConditionDaoImplTest_initial_data.xml";
	
	private static final Integer PATIENT_ID = 6;
	
	private static final Integer CONDITION_ID = 9;
	
	// This corresponds to the EXISTING_CONDITION_UUID above.
	private static final String CONDITION_CONCEPT_UUID = "c607c80f-1ea9-4da3-bb88-6276ce8868dd";
	
	@Autowired
	@Qualifier("sessionFactory")
	private SessionFactory sessionFactory;
	
	@Autowired
	private PatientService patientService;
	
	@Autowired
	private ConceptService conceptService;
	
	@Mock
	private LocalDateTimeFactory localDateTimeFactory;
	
	private FhirConditionDaoImpl_2_2 dao;
	
	@Before
	public void setUp() {
		dao = new FhirConditionDaoImpl_2_2();
		dao.setSessionFactory(sessionFactory);
		dao.setLocalDateTimeFactory(localDateTimeFactory);
		
		executeDataSet(CONDITION_INITIAL_DATA_XML);
	}
	
	@Test
	public void shouldRetrieveConditionByUuid() {
		Condition condition = dao.get(CONDITION_UUID);
		assertThat(condition, notNullValue());
		assertThat(condition.getUuid(), notNullValue());
		assertThat(condition.getUuid(), equalTo(CONDITION_UUID));
	}
	
	@Test
	public void shouldReturnNullWhenGetConditionByWrongUuid() {
		Condition condition = dao.get(WRONG_CONDITION_UUID);
		assertThat(condition, nullValue());
	}
	
	@Test
	public void shouldSaveNewCondition() {
		Condition condition = new Condition();
		condition.setUuid(NEW_CONDITION_UUID);
		condition.setOnsetDate(new Date());
		condition.setEndDate(null);
		
		org.openmrs.Patient patient = patientService.getPatient(PATIENT_ID);
		condition.setPatient(patient);
		
		dao.createOrUpdate(condition);
		
		Condition result = dao.get(NEW_CONDITION_UUID);
		assertThat(result, notNullValue());
		assertThat(result.getUuid(), equalTo(NEW_CONDITION_UUID));
	}
	
	@Test
	public void shouldReturnExistingConditionIfBothAreEquals() throws Exception {
		Condition condition = new Condition();
		condition.setConditionId(CONDITION_ID);
		condition.setUuid(EXISTING_CONDITION_UUID);
		condition.setPatient(patientService.getPatient(PATIENT_ID));
		condition.setClinicalStatus(ConditionClinicalStatus.ACTIVE);
		
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date onsetDate = sdf.parse("2020-03-13 19:00:00");
		condition.setOnsetDate(onsetDate);
		condition.setVerificationStatus(ConditionVerificationStatus.CONFIRMED);
		
		CodedOrFreeText codedOrFreeText = new CodedOrFreeText();
		codedOrFreeText.setCoded(conceptService.getConceptByUuid(CONDITION_CONCEPT_UUID));
		condition.setCondition(codedOrFreeText);
		
		Condition result = dao.createOrUpdate(condition);
		assertThat(result, notNullValue());
		assertThat(result.getUuid(), equalTo(condition.getUuid()));
		assertThat(result.getClinicalStatus(), equalTo(condition.getClinicalStatus()));
		assertThat(result.getVerificationStatus(), equalTo(condition.getVerificationStatus()));
	}
	
	@Test
	public void shouldUpdateExistingCondition() {
		Condition condition = dao.get(EXISTING_CONDITION_UUID);
		
		condition.setPatient(patientService.getPatient(PATIENT_ID));
		condition.setOnsetDate(new Date());
		condition.setClinicalStatus(ConditionClinicalStatus.HISTORY_OF);
		
		dao.createOrUpdate(condition);
		Condition result = dao.get(EXISTING_CONDITION_UUID);
		
		assertThat(result, notNullValue());
		assertThat(result.getUuid(), equalTo(EXISTING_CONDITION_UUID));
		assertThat(result.getPatient(), equalTo(patientService.getPatient(PATIENT_ID)));
		assertThat(result.getEndDate(), nullValue());
	}
}
