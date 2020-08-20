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
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

import java.util.Date;

import org.hibernate.SessionFactory;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.Obs;
import org.openmrs.api.ConceptService;
import org.openmrs.api.ObsService;
import org.openmrs.api.PatientService;
import org.openmrs.module.fhir2.TestFhirSpringConfiguration;
import org.openmrs.test.BaseModuleContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;

@ContextConfiguration(classes = TestFhirSpringConfiguration.class, inheritLocations = false)
public class FhirDiagnosticReportDaoImplTest extends BaseModuleContextSensitiveTest {
	
	private static final String DATA_XML = "org/openmrs/module/fhir2/api/dao/impl/FhirDiagnosticReportDaoImplTest_initial_data.xml";
	
	private static final String UUID = "d899333c-5bd4-45cc-b1e7-2f9542dbcbf6";
	
	private static final String NEW_UUID = "655b64a2-1513-4f07-9d1c-0da7fa80840a";
	
	private static final String CHILD_UUID = "dc386962-1c42-49ea-bed2-97650c66f742";
	
	private FhirDiagnosticReportDaoImpl dao;
	
	@Autowired
	private SessionFactory sessionFactory;
	
	@Autowired
	@Qualifier("patientService")
	private PatientService patientService;
	
	@Autowired
	@Qualifier("conceptService")
	private ConceptService conceptService;
	
	@Autowired
	@Qualifier("obsService")
	private ObsService obsService;
	
	@Before
	public void setup() throws Exception {
		dao = new FhirDiagnosticReportDaoImpl();
		dao.setSessionFactory(sessionFactory);
		executeDataSet(DATA_XML);
	}
	
	@Test
	public void getObsGroupByUuid_shouldGetObsGroupByUuid() {
		Obs result = dao.get(UUID);
		
		assertThat(result, notNullValue());
		assertThat(result.getUuid(), equalTo(UUID));
	}
	
	@Test
	public void getObsGroupByUuid_shouldReturnNullForObsNotHavingGroupMembers() {
		Obs result = dao.get(CHILD_UUID);
		assertThat(result, nullValue());
	}
	
	@Test
	public void saveObsGroup_shouldSaveNewObsGroup() {
		Obs newObs = new Obs();
		newObs.setObsDatetime(new Date());
		newObs.setPerson(patientService.getPatient(7));
		newObs.setConcept(conceptService.getConcept(5085));
		newObs.addGroupMember(obsService.getObsByUuid(CHILD_UUID));
		
		Obs result = dao.createOrUpdate(newObs);
		
		assertThat(result, notNullValue());
		assertThat(result.getPersonId(), equalTo(7));
		assertThat(result.isObsGrouping(), equalTo(true));
		assertThat(result.getGroupMembers(), hasSize(equalTo(1)));
		assertThat(result.getGroupMembers().iterator().next().getUuid(), equalTo(CHILD_UUID));
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void saveObsGroup_shouldNotSaveObsWithNoGroupMembers() {
		Obs newObs = new Obs();
		newObs.setUuid(NEW_UUID);
		
		dao.createOrUpdate(newObs);
	}
	
	@Test
	public void saveObsGroup_shouldUpdateExistingObsGroup() {
		Obs existingObsGroup = dao.get(UUID);
		
		Obs newMember = new Obs();
		newMember.setConcept(conceptService.getConcept(19));
		newMember.setObsDatetime(new Date());
		newMember.setEncounter(existingObsGroup.getEncounter());
		newMember.setPerson(existingObsGroup.getPerson());
		newMember.setValueText("blah blah blah");
		
		existingObsGroup.addGroupMember(newMember);
		
		Obs result = dao.createOrUpdate(existingObsGroup);
		
		assertThat(result, notNullValue());
		assertThat(result.getUuid(), equalTo(UUID));
		assertThat(result.getGroupMembers(), hasSize(equalTo(2)));
	}
	
}
