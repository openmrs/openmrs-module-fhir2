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
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.List;

import ca.uhn.fhir.rest.param.ReferenceAndListParam;
import ca.uhn.fhir.rest.param.ReferenceOrListParam;
import ca.uhn.fhir.rest.param.ReferenceParam;
import org.hibernate.SessionFactory;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.MedicationDispense;
import org.openmrs.api.ConceptService;
import org.openmrs.api.EncounterService;
import org.openmrs.api.OrderService;
import org.openmrs.api.PatientService;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.TestFhirSpringConfiguration;
import org.openmrs.module.fhir2.api.search.param.SearchParameterMap;
import org.openmrs.test.BaseModuleContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;

@ContextConfiguration(classes = TestFhirSpringConfiguration.class, inheritLocations = false)
public class FhirMedicationDispenseDaoImpl_2_6Test extends BaseModuleContextSensitiveTest {
	
	public static final String EXISTING_DISPENSE_UUID = "1bcb299c-b687-11ec-8065-0242ac110002";
	
	public static final String NEW_DISPENSE_UUID = "a15e4988-d07a-11ec-8307-0242ac110002";
	
	public static final String COMPLETED_STATUS_UUID = "7d8791fb-b67e-11ec-8065-0242ac110002";
	
	@Autowired
	private PatientService patientService;
	
	@Autowired
	private EncounterService encounterService;
	
	@Autowired
	private OrderService orderService;
	
	@Autowired
	private ConceptService conceptService;
	
	@Autowired
	@Qualifier("sessionFactory")
	private SessionFactory sessionFactory;
	
	private FhirMedicationDispenseDaoImpl_2_6 dao;
	
	@Before
	public void setUp() {
		dao = new FhirMedicationDispenseDaoImpl_2_6();
		dao.setSessionFactory(sessionFactory);
		executeDataSet("org/openmrs/api/include/MedicationDispenseServiceTest-initialData.xml");
		updateSearchIndex();
	}
	
	@Test
	public void shouldRetrieveMedicationDispenseByUuid() {
		MedicationDispense dispense = dao.get(EXISTING_DISPENSE_UUID);
		assertThat(dispense, notNullValue());
		assertThat(dispense.getUuid(), notNullValue());
		assertThat(dispense.getUuid(), equalTo(EXISTING_DISPENSE_UUID));
	}
	
	@Test
	public void shouldReturnNullWhenGetMedicationDispneseByWrongUuid() {
		MedicationDispense dispense = dao.get(NEW_DISPENSE_UUID);
		assertThat(dispense, nullValue());
	}
	
	@Test
	public void shouldSaveNewMedicationDispense() {
		MedicationDispense dispense = new MedicationDispense();
		dispense.setUuid(NEW_DISPENSE_UUID);
		dispense.setPatient(patientService.getPatient(6));
		dispense.setConcept(conceptService.getConcept(792));
		dispense.setStatus(conceptService.getConcept(11112));
		dao.createOrUpdate(dispense);
		
		MedicationDispense result = dao.get(NEW_DISPENSE_UUID);
		assertThat(result, notNullValue());
		assertThat(result.getUuid(), equalTo(NEW_DISPENSE_UUID));
	}
	
	@Test
	public void shouldUpdateExistingDispense() {
		MedicationDispense dispense = dao.get(EXISTING_DISPENSE_UUID);
		assertThat(dispense.getStatus().getUuid(), not(equalTo(COMPLETED_STATUS_UUID)));
		
		dispense.setStatus(conceptService.getConceptByUuid(COMPLETED_STATUS_UUID));
		dao.createOrUpdate(dispense);
		
		MedicationDispense result = dao.get(EXISTING_DISPENSE_UUID);
		
		assertThat(result, notNullValue());
		assertThat(result.getUuid(), equalTo(EXISTING_DISPENSE_UUID));
		assertThat(result.getStatus().getUuid(), equalTo(COMPLETED_STATUS_UUID));
	}
	
	@Test
	public void shouldGetSearchResultUuidsForMatchingPatients() {
		String patientUuid = patientService.getPatient(7).getUuid();
		ReferenceAndListParam patientAndParam = new ReferenceAndListParam();
		ReferenceOrListParam patientOrParam = new ReferenceOrListParam();
		patientOrParam.add(new ReferenceParam(patientUuid));
		patientAndParam.addValue(patientOrParam);
		SearchParameterMap theParams = new SearchParameterMap();
		theParams.addParameter(FhirConstants.PATIENT_REFERENCE_SEARCH_HANDLER, patientAndParam);
		
		List<String> results = dao.getSearchResultUuids(theParams);
		assertThat(results.size(), equalTo(2));
		assertThat(results.contains("7a0282eb-b686-11ec-8065-0242ac110002"), is(true));
		assertThat(results.contains("1bcb299c-b687-11ec-8065-0242ac110002"), is(true));
	}
	
	@Test
	public void shouldGetSearchResultUuidsForMatchingEncounters() {
		String encounterUuid = encounterService.getEncounter(3).getUuid();
		ReferenceAndListParam param = new ReferenceAndListParam();
		param.addValue(new ReferenceOrListParam().add(new ReferenceParam(encounterUuid)));
		SearchParameterMap theParams = new SearchParameterMap();
		theParams.addParameter(FhirConstants.ENCOUNTER_REFERENCE_SEARCH_HANDLER, param);
		
		List<String> results = dao.getSearchResultUuids(theParams);
		assertThat(results.size(), equalTo(1));
		assertThat(results.contains("7a0282eb-b686-11ec-8065-0242ac110002"), is(true));
	}
	
	@Test
	public void shouldGetSearchResultUuidsForMatchingDrugOrders() {
		String drugOrderUuid = orderService.getOrder(2).getUuid();
		ReferenceAndListParam param = new ReferenceAndListParam();
		param.addValue(new ReferenceOrListParam().add(new ReferenceParam(drugOrderUuid)));
		SearchParameterMap theParams = new SearchParameterMap();
		theParams.addParameter(FhirConstants.MEDICATION_REQUEST_REFERENCE_SEARCH_HANDLER, param);
		
		List<String> results = dao.getSearchResultUuids(theParams);
		assertThat(results.size(), equalTo(1));
		assertThat(results.contains("b75c5c9e-b66c-11ec-8065-0242ac110002"), is(true));
	}
	
	@Test
	public void shouldDeleteExistingDispense() {
		MedicationDispense dispense = dao.get(EXISTING_DISPENSE_UUID);
		assertThat(dispense, notNullValue());
		assertThat(dispense.getVoided(), equalTo(Boolean.FALSE));
		
		dao.delete(EXISTING_DISPENSE_UUID);
		
		MedicationDispense result = dao.get(EXISTING_DISPENSE_UUID);
		assertThat(result, notNullValue());
		assertThat(result.getUuid(), equalTo(EXISTING_DISPENSE_UUID));
		assertThat(result.getVoided(), equalTo(Boolean.TRUE));
		
	}
}
