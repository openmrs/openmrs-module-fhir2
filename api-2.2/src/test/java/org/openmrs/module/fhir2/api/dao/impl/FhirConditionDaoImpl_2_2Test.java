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
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.exparity.hamcrest.date.DateMatchers;
import org.hibernate.SessionFactory;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.CodedOrFreeText;
import org.openmrs.Condition;
import org.openmrs.ConditionClinicalStatus;
import org.openmrs.ConditionVerificationStatus;
import org.openmrs.Patient;
import org.openmrs.api.ConceptService;
import org.openmrs.api.PatientService;
import org.openmrs.module.fhir2.TestFhirSpringConfiguration;
import org.openmrs.test.BaseModuleContextSensitiveTest;
import org.springframework.test.context.ContextConfiguration;

@ContextConfiguration(classes = TestFhirSpringConfiguration.class, inheritLocations = false)
public class FhirConditionDaoImpl_2_2Test extends BaseModuleContextSensitiveTest {
	
	private static final String CONDITION_UUID = "2cc6880e-2c46-15e4-9038-a6c5e4d22fb7";
	
	private static final String NEW_CONDITION_UUID = "3dd6880e-2c46-15e4-9038-a6c5e4d22gh8";
	
	private static final String EXISTING_CONDITION_UUID = "2cc6880e-2c46-11e4-9138-a6c5e4d20fb7";
	
	private static final String WRONG_CONDITION_UUID = "430bbb70-6a9c-4e1e-badb-9d1034b1b5e9";
	
	private static final String CONDITION_INITIAL_DATA_XML = "org/openmrs/module/fhir2/api/dao/impl/FhirConditionDaoImplTest_initial_data.xml";
	
	private static final String END_REASON = "End reason";
	
	private static final Integer PATIENT_ID = 2;
	
	private static final String CONDITION_CONCEPT_UUID = "d102c80f-1yz9-4da3-bb88-8122ce8868df";
	
	@Inject
	@Named("sessionFactory")
	private Provider<SessionFactory> sessionFactoryProvider;
	
	@Inject
	private PatientService patientService;
	
	@Inject
	private ConceptService conceptService;
	
	@Inject
	private FhirConditionDaoImpl_2_2 dao;
	
	@Before
	public void setUp() {
		dao = new FhirConditionDaoImpl_2_2();
		dao.setSessionFactory(sessionFactoryProvider.get());
		executeDataSet(CONDITION_INITIAL_DATA_XML);
	}
	
	@Test
	public void shouldRetrieveConditionByUuid() {
		Condition condition = dao.getConditionByUuid(CONDITION_UUID);
		assertThat(condition, notNullValue());
		assertThat(condition.getUuid(), notNullValue());
		assertThat(condition.getUuid(), equalTo(CONDITION_UUID));
	}
	
	@Test
	public void shouldReturnNullWhenGetConditionByWrongUuid() {
		Condition condition = dao.getConditionByUuid(WRONG_CONDITION_UUID);
		assertThat(condition, nullValue());
	}
	
	@Test
	public void shouldSaveNewCondition() {
		Condition condition = new Condition();
		condition.setUuid(CONDITION_UUID);
		condition.setOnsetDate(new Date());
		condition.setEndDate(null);
		
		Patient patient = patientService.getPatient(PATIENT_ID);
		condition.setPatient(patient);
		
		dao.saveCondition(condition);
		Condition result = dao.getConditionByUuid(CONDITION_UUID);
		assertThat(result, notNullValue());
		assertThat(result.getUuid(), equalTo(CONDITION_UUID));
	}
	
	@Test
	public void shouldReturnExistingConditionIfBothAreEquals() throws Exception {
		Condition condition = new Condition();
		condition.setConditionId(PATIENT_ID);
		condition.setUuid(EXISTING_CONDITION_UUID);
		condition.setPatient(patientService.getPatient(PATIENT_ID));
		condition.setClinicalStatus(ConditionClinicalStatus.ACTIVE);
		
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date onsetDate = sdf.parse("2017-01-12 00:00:50");
		condition.setOnsetDate(onsetDate);
		condition.setVerificationStatus(ConditionVerificationStatus.CONFIRMED);
		
		CodedOrFreeText codedOrFreeText = new CodedOrFreeText();
		codedOrFreeText.setCoded(conceptService.getConceptByUuid(CONDITION_CONCEPT_UUID));
		condition.setCondition(codedOrFreeText);
		
		Condition result = dao.saveCondition(condition);
		assertThat(result, notNullValue());
		assertThat(result.getUuid(), equalTo(condition.getUuid()));
		assertThat(result.getClinicalStatus(), equalTo(condition.getClinicalStatus()));
		assertThat(result.getVerificationStatus(), equalTo(condition.getVerificationStatus()));
	}
	
	@Test
	public void shouldUpdateExistingCondition() {
		Condition condition = new Condition();
		condition.setUuid(EXISTING_CONDITION_UUID);
		
		condition.setPatient(patientService.getPatient(PATIENT_ID));
		condition.setOnsetDate(new Date());
		condition.setClinicalStatus(ConditionClinicalStatus.HISTORY_OF);
		
		dao.saveCondition(condition);
		Condition result = dao.getConditionByUuid(EXISTING_CONDITION_UUID);
		
		assertThat(result, notNullValue());
		assertThat(result.getUuid(), equalTo(EXISTING_CONDITION_UUID));
		assertThat(result.getPatient(), equalTo(patientService.getPatient(PATIENT_ID)));
		assertThat(result.getEndDate(), notNullValue());
		assertThat(result.getEndDate(), DateMatchers.sameDay(condition.getOnsetDate()));
	}
	
	@Test
	public void shouldSetConditionVoidedStatusTrue() {
		Condition condition = new Condition();
		condition.setUuid(EXISTING_CONDITION_UUID);
		condition.setPatient(patientService.getPatient(PATIENT_ID));
		condition.setClinicalStatus(ConditionClinicalStatus.ACTIVE);
		condition.setOnsetDate(new Date());
		
		dao.saveCondition(condition);
		Condition result = dao.getConditionByUuid(EXISTING_CONDITION_UUID);
		assertThat(result, notNullValue());
		assertThat(result.getVoided(), is(true));
	}
	
	@Test
	public void shouldSetEndDateIfEndReasonIsNotNull() {
		Condition condition = new Condition();
		condition.setUuid(NEW_CONDITION_UUID);
		condition.setPatient(patientService.getPatient(PATIENT_ID));
		condition.setClinicalStatus(ConditionClinicalStatus.ACTIVE);
		condition.setEndReason(END_REASON);
		
		dao.saveCondition(condition);
		Condition result = dao.getConditionByUuid(NEW_CONDITION_UUID);
		assertThat(result, notNullValue());
		assertThat(result.getEndDate(), notNullValue());
		assertThat(result.getEndDate(), DateMatchers.sameDay(new Date()));
	}
}
