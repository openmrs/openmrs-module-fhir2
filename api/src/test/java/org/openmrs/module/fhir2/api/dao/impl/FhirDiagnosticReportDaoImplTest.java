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

import java.util.Date;

import org.hibernate.SessionFactory;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.Obs;
import org.openmrs.api.ConceptService;
import org.openmrs.api.ObsService;
import org.openmrs.api.PatientService;
import org.openmrs.module.fhir2.BaseFhirContextSensitiveTest;
import org.openmrs.module.fhir2.model.FhirDiagnosticReport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

public class FhirDiagnosticReportDaoImplTest extends BaseFhirContextSensitiveTest {
	
	private static final String DATA_XML = "org/openmrs/module/fhir2/api/dao/impl/FhirDiagnosticReportDaoImplTest_initial_data.xml";
	
	private static final String DIAGNOSTIC_REPORT_UUID = "1e589127-f391-4d0c-8e98-e0a158b2be22";
	
	private static final String OBS_UUID = "dc386962-1c42-49ea-bed2-97650c66f742";
	
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
	public void get_shouldGetFhirDiagnosticReportByUuid() {
		FhirDiagnosticReport result = dao.get(DIAGNOSTIC_REPORT_UUID);
		
		assertThat(result, notNullValue());
		assertThat(result.getUuid(), equalTo(DIAGNOSTIC_REPORT_UUID));
	}
	
	@Test
	public void saveOrUpdate_shouldSaveNewFhirDiagnosticReport() {
		FhirDiagnosticReport newDiagnosticReport = new FhirDiagnosticReport();
		newDiagnosticReport.setStatus(FhirDiagnosticReport.DiagnosticReportStatus.UNKNOWN);
		newDiagnosticReport.setIssued(new Date());
		newDiagnosticReport.setSubject(patientService.getPatient(7));
		newDiagnosticReport.setCode(conceptService.getConcept(5085));
		newDiagnosticReport.getResults().add(obsService.getObsByUuid(OBS_UUID));
		
		FhirDiagnosticReport result = dao.createOrUpdate(newDiagnosticReport);
		
		assertThat(result, notNullValue());
		assertThat(result.getSubject().getId(), equalTo(7));
		assertThat(result.getResults(), hasSize(equalTo(1)));
		assertThat(result.getResults().iterator().next().getUuid(), equalTo(OBS_UUID));
	}
	
	@Test
	public void saveOrUpdate_shouldUpdateExistingFhirDiagnosticReport() {
		FhirDiagnosticReport existingObsGroup = dao.get(DIAGNOSTIC_REPORT_UUID);
		
		Obs newMember = new Obs();
		newMember.setConcept(conceptService.getConcept(19));
		newMember.setObsDatetime(new Date());
		newMember.setEncounter(existingObsGroup.getEncounter());
		newMember.setPerson(existingObsGroup.getSubject());
		newMember.setValueText("blah blah blah");
		
		existingObsGroup.getResults().add(newMember);
		
		FhirDiagnosticReport result = dao.createOrUpdate(existingObsGroup);
		
		assertThat(result, notNullValue());
		assertThat(result.getUuid(), equalTo(DIAGNOSTIC_REPORT_UUID));
		assertThat(result.getResults(), hasSize(equalTo(3)));
	}
}
