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

import java.util.Date;

import org.exparity.hamcrest.date.DateMatchers;
import org.hibernate.SessionFactory;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.Concept;
import org.openmrs.Patient;
import org.openmrs.module.emrapi.conditionslist.Condition;
import org.openmrs.module.fhir2.TestFhirSpringConfiguration;
import org.openmrs.module.fhir2.api.dao.FhirConceptDao;
import org.openmrs.test.BaseModuleContextSensitiveTest;
import org.springframework.test.context.ContextConfiguration;

@ContextConfiguration(classes = TestFhirSpringConfiguration.class, inheritLocations = false)
public class FhirConditionDaoImpl_2_0Test extends BaseModuleContextSensitiveTest {
	
	private static final String CONDITION_UUID = "3cc6880e-2c46-11e4-9038-a6c5e4d22fb8";
	
	private static final String EXISTING_CONDITION_UUID = "c84i8o0e-2n46-11e4-58f4-a6i5e4d22fb7";
	
	private static final String PATIENT_UUID = "86s04d42-3ca8-11e3-bf2b-0x7009861s97";
	
	private static final String SAVED_CONDITION_UUID = "2cc6880e-2c46-11e4-9038-a6c5e4d22fb7";
	
	private static final String NEW_CONDITION_UUID = "3dd6880e-2c46-55e4-9038-a6c5e4d22gh4";
	
	private static final String WRONG_CONDITION_UUID = "430bbb70-6a9c-4e1e-badb-9d1034b1b5e9";
	
	private static final String CONDITION_INITIAL_DATA_XML = "org/openmrs/module/fhir2/api/dao/impl/FhirConditionDaoImplTest_initial_data.xml";
	
	private static final String CONCEPT_UUID = "d102c80f-1yz9-4da3-bb88-8122ce8868df";
	
	private static final String END_REASON_CONCEPT_UUID = "s102c80f-1yz9-4da3-bb88-8122ce8868ss";
	
	@Inject
	@Named("sessionFactory")
	private Provider<SessionFactory> sessionFactoryProvider;
	
	@Inject
	private FhirPatientDaoImpl patientDao;
	
	@Inject
	private FhirConceptDao conceptDao;
	
	private FhirConditionDaoImpl_2_0 dao;
	
	@Before
	public void setUp() throws Exception {
		dao = new FhirConditionDaoImpl_2_0();
		dao.setSessionFactory(sessionFactoryProvider.get());
		executeDataSet(CONDITION_INITIAL_DATA_XML);
	}
	
	@Test
	public void shouldRetrieveConditionByUuid() {
		Condition condition = dao.getConditionByUuid(SAVED_CONDITION_UUID);
		assertThat(condition, notNullValue());
		assertThat(condition.getUuid(), notNullValue());
		assertThat(condition.getUuid(), equalTo(SAVED_CONDITION_UUID));
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
		
		Patient patient = patientDao.getPatientByUuid(PATIENT_UUID);
		condition.setPatient(patient);
		
		Concept concept = conceptDao.getConceptByUuid(CONCEPT_UUID).orElse(null);
		condition.setConcept(concept);
		
		dao.saveCondition(condition);
		Condition result = dao.getConditionByUuid(CONDITION_UUID);
		assertThat(result, notNullValue());
		assertThat(result.getUuid(), equalTo(CONDITION_UUID));
	}
	
	@Test
	public void shouldReturnExistingConditionIfBothAreEquals() {
		Condition condition = new Condition();
		condition.setUuid(EXISTING_CONDITION_UUID);
		condition.setConcept(conceptDao.getConceptByUuid(CONCEPT_UUID).orElse(null));
		condition.setPatient(patientDao.getPatientByUuid(PATIENT_UUID));
		condition.setStatus(Condition.Status.ACTIVE);
		
		dao.saveCondition(condition);
		Condition result = dao.getConditionByUuid(EXISTING_CONDITION_UUID);
		assertThat(result, notNullValue());
		assertThat(result, equalTo(dao.getConditionByUuid(EXISTING_CONDITION_UUID)));
	}
	
	@Test
	public void shouldUpdateExistingCondition() {
		Condition condition = new Condition();
		condition.setUuid(EXISTING_CONDITION_UUID);
		condition.setConcept(conceptDao.getConceptByUuid(CONCEPT_UUID).orElse(null));
		condition.setPatient(patientDao.getPatientByUuid(PATIENT_UUID));
		condition.setOnsetDate(new Date());
		condition.setStatus(Condition.Status.HISTORY_OF);
		
		dao.saveCondition(condition);
		Condition result = dao.getConditionByUuid(EXISTING_CONDITION_UUID);
		
		assertThat(result, notNullValue());
		assertThat(result.getUuid(), equalTo(EXISTING_CONDITION_UUID));
		assertThat(result.getConcept(), equalTo(conceptDao.getConceptByUuid(CONCEPT_UUID).orElse(null)));
		assertThat(result.getPatient(), equalTo(patientDao.getPatientByUuid(PATIENT_UUID)));
		assertThat(result.getEndDate(), DateMatchers.sameDay(condition.getOnsetDate()));
	}
	
	@Test
	public void shouldSetConditionVoidedStatusTrue() {
		Condition condition = new Condition();
		condition.setUuid(EXISTING_CONDITION_UUID);
		condition.setConcept(conceptDao.getConceptByUuid(CONCEPT_UUID).orElse(null));
		condition.setPatient(patientDao.getPatientByUuid(PATIENT_UUID));
		condition.setStatus(Condition.Status.ACTIVE);
		condition.setEndReason(conceptDao.getConceptByUuid(END_REASON_CONCEPT_UUID).orElse(null));
		
		dao.saveCondition(condition);
		Condition result = dao.getConditionByUuid(EXISTING_CONDITION_UUID);
		assertThat(result, notNullValue());
		assertThat(result.getVoided(), is(true));
	}
	
	@Test
	public void shouldSetEndDateIfEndReasonIsNotNull() {
		Condition condition = new Condition();
		condition.setUuid(NEW_CONDITION_UUID);
		condition.setConcept(conceptDao.getConceptByUuid(CONCEPT_UUID).orElse(null));
		condition.setPatient(patientDao.getPatientByUuid(PATIENT_UUID));
		condition.setEndReason(conceptDao.getConceptByUuid(END_REASON_CONCEPT_UUID).orElse(null));
		
		dao.saveCondition(condition);
		Condition result = dao.getConditionByUuid(NEW_CONDITION_UUID);
		assertThat(result, notNullValue());
		assertThat(result.getEndDate(), notNullValue());
		assertThat(result.getEndDate(), DateMatchers.sameDay(new Date()));
	}
}
