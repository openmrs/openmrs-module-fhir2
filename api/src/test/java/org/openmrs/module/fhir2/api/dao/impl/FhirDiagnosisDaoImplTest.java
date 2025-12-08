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
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.fail;
import static org.openmrs.util.PrivilegeConstants.GET_DIAGNOSES;

import java.util.Arrays;
import java.util.List;

import ca.uhn.fhir.rest.param.ReferenceAndListParam;
import ca.uhn.fhir.rest.param.ReferenceOrListParam;
import ca.uhn.fhir.rest.param.ReferenceParam;
import ca.uhn.fhir.rest.param.TokenAndListParam;
import ca.uhn.fhir.rest.param.TokenOrListParam;
import ca.uhn.fhir.rest.param.TokenParam;
import org.hibernate.Criteria;
import org.hibernate.SessionFactory;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.Diagnosis;
import org.openmrs.Encounter;
import org.openmrs.api.APIAuthenticationException;
import org.openmrs.api.EncounterService;
import org.openmrs.api.context.Context;
import org.openmrs.module.fhir2.BaseFhirContextSensitiveTest;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.api.dao.FhirDiagnosisDao;
import org.openmrs.module.fhir2.api.search.param.SearchParameterMap;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

public class FhirDiagnosisDaoImplTest extends BaseFhirContextSensitiveTest {
	
	private static final String DIAGNOSIS_UUID = "11111111-2222-3333-4444-555555555555";
	
	@Autowired
	@Qualifier("sessionFactory")
	private SessionFactory sessionFactory;
	
	@Autowired
	private ObjectFactory<FhirDiagnosisDao> daoFactory;
	
	@Autowired
	private EncounterService encounterService;
	
	private FhirDiagnosisDao dao;
	
	private FhirDiagnosisDaoImpl daoImpl;
	
	@Before
	public void setUp() throws Exception {
		dao = daoFactory.getObject();
		daoImpl = new FhirDiagnosisDaoImpl();
		daoImpl.setSessionFactory(sessionFactory);
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
	public void hasDistinctResults_shouldReturnFalse() {
		assertThat(daoImpl.hasDistinctResults(), equalTo(false));
	}
	
	@Test
	public void get_shouldRequireGetDiagnosesPrivilege() {
		Context.logout();
		
		try {
			dao.get(DIAGNOSIS_UUID);
			fail("Expected APIAuthenticationException for missing privilege, but it was not thrown");
		}
		catch (APIAuthenticationException e) {
			assertThat(e.getMessage(), containsString("Privilege"));
		}
		
		try {
			Context.addProxyPrivilege(GET_DIAGNOSES);
			dao.get(DIAGNOSIS_UUID);
		}
		finally {
			Context.removeProxyPrivilege(GET_DIAGNOSES);
		}
	}
	
	@Test
	public void get_shouldRequireGetDiagnosesPrivilegeWithCollection() {
		Context.logout();
		
		try {
			dao.get(Arrays.asList(DIAGNOSIS_UUID));
			fail("Expected APIAuthenticationException for missing privilege, but it was not thrown");
		}
		catch (APIAuthenticationException e) {
			assertThat(e.getMessage(), containsString("Privilege"));
		}
		
		try {
			Context.addProxyPrivilege(GET_DIAGNOSES);
			List<Diagnosis> diagnoses = dao.get(Arrays.asList(DIAGNOSIS_UUID));
			assertThat(diagnoses, notNullValue());
		}
		finally {
			Context.removeProxyPrivilege(GET_DIAGNOSES);
		}
	}
	
	@Test
	public void getSearchResults_shouldRequireGetDiagnosesPrivilege() {
		Context.logout();
		
		try {
			dao.getSearchResults(new SearchParameterMap());
			fail("Expected APIAuthenticationException for missing privilege, but it was not thrown");
		}
		catch (APIAuthenticationException e) {
			assertThat(e.getMessage(), containsString("Privilege"));
		}
		
		try {
			Context.addProxyPrivilege(GET_DIAGNOSES);
			List<Diagnosis> diagnoses = dao.getSearchResults(new SearchParameterMap());
			assertThat(diagnoses, notNullValue());
		}
		finally {
			Context.removeProxyPrivilege(GET_DIAGNOSES);
		}
	}
	
	@Test
	public void getSearchResultsCount_shouldRequireGetDiagnosesPrivilege() {
		Context.logout();
		
		try {
			dao.getSearchResultsCount(new SearchParameterMap());
			fail("Expected APIAuthenticationException for missing privilege, but it was not thrown");
		}
		catch (APIAuthenticationException e) {
			assertThat(e.getMessage(), containsString("Privilege"));
		}
		
		try {
			Context.addProxyPrivilege(GET_DIAGNOSES);
			int count = dao.getSearchResultsCount(new SearchParameterMap());
			assertThat(count, notNullValue());
		}
		finally {
			Context.removeProxyPrivilege(GET_DIAGNOSES);
		}
	}
	
	@Test
	public void setupSearchParams_shouldHandleEmptyParams() {
		Criteria criteria = sessionFactory.getCurrentSession().createCriteria(Diagnosis.class);
		SearchParameterMap params = new SearchParameterMap()
		        .addParameter(FhirConstants.PATIENT_REFERENCE_SEARCH_HANDLER, new ReferenceAndListParam())
		        .addParameter(FhirConstants.CODED_SEARCH_HANDLER, new TokenAndListParam());
		daoImpl.setupSearchParams(criteria, params);
		assertThat(criteria, notNullValue());
	}
	
	@Test
	public void setupSearchParams_shouldHandleDiagnosisCodeParam() {
		Criteria criteria = sessionFactory.getCurrentSession().createCriteria(Diagnosis.class);
		TokenAndListParam codeParam = new TokenAndListParam();
		codeParam.addAnd(new TokenOrListParam().add(new TokenParam("test")));
		
		SearchParameterMap params = new SearchParameterMap().addParameter(FhirConstants.CODED_SEARCH_HANDLER, codeParam);
		
		daoImpl.setupSearchParams(criteria, params);
		
		assertThat(criteria, notNullValue());
	}
	
	@Test
	public void setupSearchParams_shouldSkipDiagnosisCodeWhenParamNull() {
		Criteria criteria = sessionFactory.getCurrentSession().createCriteria(Diagnosis.class);
		SearchParameterMap params = new SearchParameterMap().addParameter(FhirConstants.CODED_SEARCH_HANDLER, null);
		
		daoImpl.setupSearchParams(criteria, params);
		
		assertThat(criteria, notNullValue());
	}
	
	@Test
	public void setupSearchParams_shouldHandlePatientReferenceParam() {
		Criteria criteria = sessionFactory.getCurrentSession().createCriteria(Diagnosis.class);
		ReferenceAndListParam patientParam = new ReferenceAndListParam();
		patientParam.addValue(new ReferenceOrListParam().add(new ReferenceParam("123")));
		
		SearchParameterMap params = new SearchParameterMap().addParameter(FhirConstants.PATIENT_REFERENCE_SEARCH_HANDLER,
		    patientParam);
		
		daoImpl.setupSearchParams(criteria, params);
		
		assertThat(criteria, notNullValue());
	}
	
	@Test
	public void setupSearchParams_shouldHandleEncounterReferenceParam() {
		Criteria criteria = sessionFactory.getCurrentSession().createCriteria(Diagnosis.class);
		ReferenceAndListParam encounterParam = new ReferenceAndListParam();
		encounterParam.addValue(new ReferenceOrListParam().add(new ReferenceParam("3")));
		
		SearchParameterMap params = new SearchParameterMap().addParameter(FhirConstants.ENCOUNTER_REFERENCE_SEARCH_HANDLER,
		    encounterParam);
		
		daoImpl.setupSearchParams(criteria, params);
		
		assertThat(criteria, notNullValue());
	}
}
