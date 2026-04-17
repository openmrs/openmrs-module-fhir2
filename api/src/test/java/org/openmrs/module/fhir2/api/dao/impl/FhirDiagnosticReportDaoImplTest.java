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
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.fail;
import static org.openmrs.util.PrivilegeConstants.GET_OBS;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.openmrs.Obs;
import org.openmrs.api.APIAuthenticationException;
import org.openmrs.api.ConceptService;
import org.openmrs.api.ObsService;
import org.openmrs.api.PatientService;
import org.openmrs.api.context.Context;
import org.openmrs.module.fhir2.BaseFhirContextSensitiveTest;
import org.openmrs.module.fhir2.api.dao.FhirDiagnosticReportDao;
import org.openmrs.module.fhir2.api.search.param.SearchParameterMap;
import org.openmrs.module.fhir2.model.FhirDiagnosticReport;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

public class FhirDiagnosticReportDaoImplTest extends BaseFhirContextSensitiveTest {
	
	private static final String DATA_XML = "org/openmrs/module/fhir2/api/dao/impl/FhirDiagnosticReportDaoImplTest_initial_data.xml";
	
	private static final String DIAGNOSTIC_REPORT_UUID = "1e589127-f391-4d0c-8e98-e0a158b2be22";
	
	private static final String OBS_UUID = "dc386962-1c42-49ea-bed2-97650c66f742";
	
	@Autowired
	private ObjectFactory<FhirDiagnosticReportDao> daoFactory;
	
	@Autowired
	@Qualifier("patientService")
	private PatientService patientService;
	
	@Autowired
	@Qualifier("conceptService")
	private ConceptService conceptService;
	
	@Autowired
	@Qualifier("obsService")
	private ObsService obsService;
	
	private FhirDiagnosticReportDao dao;
	
	@Before
	public void setup() throws Exception {
		dao = daoFactory.getObject();
		executeDataSet(DATA_XML);
	}
	
	@Test
	public void get_shouldGetFhirDiagnosticReportByUuid() {
		FhirDiagnosticReport result = dao.get(DIAGNOSTIC_REPORT_UUID);
		
		assertThat(result, notNullValue());
		assertThat(result.getUuid(), equalTo(DIAGNOSTIC_REPORT_UUID));
	}
	
	@Test
	public void get_shouldRequireGetObsPrivilege() {
		Context.logout();
		
		try {
			dao.get(DIAGNOSTIC_REPORT_UUID);
			fail("Expected APIAuthenticationException for missing privilege, but it was not thrown");
		}
		catch (APIAuthenticationException e) {
			assertThat(e.getMessage(), containsString("Privilege"));
		}
		
		try {
			Context.addProxyPrivilege(GET_OBS);
			assertThat(dao.get(DIAGNOSTIC_REPORT_UUID), notNullValue());
		}
		finally {
			Context.removeProxyPrivilege(GET_OBS);
		}
	}
	
	@Test
	public void get_shouldRequireGetObsPrivilegeWithCollection() {
		Context.logout();
		
		try {
			dao.get(Arrays.asList(DIAGNOSTIC_REPORT_UUID));
			fail("Expected APIAuthenticationException for missing privilege, but it was not thrown");
		}
		catch (APIAuthenticationException e) {
			assertThat(e.getMessage(), containsString("Privilege"));
		}
		
		try {
			Context.addProxyPrivilege(GET_OBS);
			List<FhirDiagnosticReport> reports = dao.get(Arrays.asList(DIAGNOSTIC_REPORT_UUID));
			assertThat(reports, notNullValue());
		}
		finally {
			Context.removeProxyPrivilege(GET_OBS);
		}
	}
	
	@Test
	public void getSearchResults_shouldRequireGetObsPrivilegeFor() {
		Context.logout();
		
		try {
			dao.getSearchResults(new SearchParameterMap());
			fail("Expected APIAuthenticationException for missing privilege, but it was not thrown");
		}
		catch (APIAuthenticationException e) {
			assertThat(e.getMessage(), containsString("Privilege"));
		}
		
		try {
			Context.addProxyPrivilege(GET_OBS);
			List<FhirDiagnosticReport> reports = dao.getSearchResults(new SearchParameterMap());
			assertThat(reports, notNullValue());
		}
		finally {
			Context.removeProxyPrivilege(GET_OBS);
		}
	}
	
	@Test
	public void getSearchResultsCount_shouldRequireGetObsPrivilege() {
		Context.logout();
		
		try {
			dao.getSearchResultsCount(new SearchParameterMap());
			fail("Expected APIAuthenticationException for missing privilege, but it was not thrown");
		}
		catch (APIAuthenticationException e) {
			assertThat(e.getMessage(), containsString("Privilege"));
		}
		
		try {
			Context.addProxyPrivilege(GET_OBS);
			int count = dao.getSearchResultsCount(new SearchParameterMap());
			assertThat(count, notNullValue());
		}
		finally {
			Context.removeProxyPrivilege(GET_OBS);
		}
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
