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
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

import java.util.List;

import ca.uhn.fhir.rest.param.HasAndListParam;
import ca.uhn.fhir.rest.param.HasOrListParam;
import ca.uhn.fhir.rest.param.HasParam;
import org.hibernate.SessionFactory;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.Patient;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.TestFhirSpringConfiguration;
import org.openmrs.module.fhir2.api.search.param.SearchParameterMap;
import org.openmrs.test.BaseModuleContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

@ContextConfiguration(classes = TestFhirSpringConfiguration.class, inheritLocations = false)
public class FhirPatientDaoImplTest extends BaseModuleContextSensitiveTest {
	
	private static final String PATIENT_UUID = "256ccf6d-6b41-455c-9be2-51ff4386ae76";
	
	private static final String BAD_PATIENT_UUID = "282390a6-3608-496d-9025-aecbc1235670";
	
	private static final String GROUP_A = "dfb29c44-2e39-46c4-8cd7-18f21c6d47b1";
	
	private static final String GROUP_B = "a25ce1d7-326c-43ff-a87f-63d9d2f60f11";
	
	private static final String GROUP_C = "6f4816fb-0b75-4e25-aac0-4944a6d3b697";
	
	private static final String PATIENT1_GROUP_A = "256ccf6d-6b41-455c-9be2-51ff4386ae76";
	
	private static final String PATIENT2_GROUP_A = "30e2aa2a-4ed1-415d-84c5-ba29016c14b7";
	
	private static final String PATIENT3_GROUP_A = "8d703ff2-c3e2-4070-9737-73e713d5a50d";
	
	private static final String PATIENT4_GROUP_A = "ca17fcc5-ec96-487f-b9ea-42973c8973e3";
	
	private static final String PATIENT1_GROUP_C = "c7c1416f9-3beb-40fe-9043-1ce70ea9df53";
	
	private static final String[] PATIENT_SEARCH_DATA_FILES = {
	        "org/openmrs/module/fhir2/api/dao/impl/FhirPatientDaoImplTest_initial_data.xml",
	        "org/openmrs/module/fhir2/api/dao/impl/FhirPatientDaoImplTest_address_data.xml" };
	
	private FhirPatientDaoImpl dao;
	
	private FhirGroupDaoImpl groupDao;
	
	@Autowired
	private SessionFactory sessionFactory;
	
	@Before
	public void setup() throws Exception {
		groupDao = new FhirGroupDaoImpl();
		groupDao.setSessionFactory(sessionFactory);
		dao = new FhirPatientDaoImpl();
		dao.setSessionFactory(sessionFactory);
		dao.setGroupDao(groupDao);
		for (String search_data : PATIENT_SEARCH_DATA_FILES) {
			executeDataSet(search_data);
		}
	}
	
	@Test
	public void getPatientById_shouldRetrievePatientById() {
		Patient result = dao.getPatientById(4);
		
		assertThat(result, notNullValue());
		assertThat(result.getUuid(), equalTo(PATIENT_UUID));
		assertThat(result.getId(), equalTo(4));
	}
	
	@Test
	public void getPatientById_shouldReturnNullIfPatientNotFound() {
		assertThat(dao.getPatientById(0), nullValue());
	}
	
	@Test
	public void getPatientByUuid_shouldRetrievePatientByUuid() {
		Patient result = dao.get(PATIENT_UUID);
		
		assertThat(result, notNullValue());
		assertThat(result.getUuid(), equalTo(PATIENT_UUID));
	}
	
	@Test
	public void getPatientByUuid_shouldReturnNullIfPatientNotFound() {
		Patient result = dao.get(BAD_PATIENT_UUID);
		
		assertThat(result, nullValue());
	}
	
	@Test
	public void getSearchResults_shouldReturnPatientsSearchResults() {
		HasAndListParam groupParam = new HasAndListParam().addAnd(
		    new HasOrListParam().add(new HasParam(FhirConstants.GROUP, FhirConstants.INCLUDE_MEMBER_PARAM, "id", GROUP_A)));
		
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.HAS_SEARCH_HANDLER, groupParam);
		List<Patient> result = dao.getSearchResults(theParams);
		
		assertThat(result, notNullValue());
		assertThat(result.size(), equalTo(4));
		assertThat(result.get(0).getUuid(), equalTo(PATIENT1_GROUP_A));
		assertThat(result.get(1).getUuid(), equalTo(PATIENT2_GROUP_A));
		assertThat(result.get(2).getUuid(), equalTo(PATIENT3_GROUP_A));
		assertThat(result.get(3).getUuid(), equalTo(PATIENT4_GROUP_A));
	}
	
	@Test
	public void getSearchResults_shouldReturnPatientsFromTwoGroupsSearchResults() {
		HasAndListParam groupParam = new HasAndListParam().addAnd(
		    new HasOrListParam().add(new HasParam(FhirConstants.GROUP, FhirConstants.INCLUDE_MEMBER_PARAM, "id", GROUP_A))
		            .add(new HasParam(FhirConstants.GROUP, FhirConstants.INCLUDE_MEMBER_PARAM, "id", GROUP_C)));
		
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.HAS_SEARCH_HANDLER, groupParam);
		List<Patient> result = dao.getSearchResults(theParams);
		
		assertThat(result, notNullValue());
		assertThat(result.get(0).getUuid(), equalTo(PATIENT1_GROUP_A));
		assertThat(result.get(1).getUuid(), equalTo(PATIENT2_GROUP_A));
		assertThat(result.get(2).getUuid(), equalTo(PATIENT3_GROUP_A));
		assertThat(result.get(3).getUuid(), equalTo(PATIENT4_GROUP_A));
		assertThat(result.get(4).getUuid(), equalTo(PATIENT1_GROUP_C));
	}
	
	@Test
	public void getSearchResults_shouldReturnEmptyList() {
		HasAndListParam groupParam = new HasAndListParam().addAnd(
		    new HasOrListParam().add(new HasParam(FhirConstants.GROUP, FhirConstants.INCLUDE_MEMBER_PARAM, "id", GROUP_B)));
		
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.HAS_SEARCH_HANDLER, groupParam);
		List<Patient> result = dao.getSearchResults(theParams);
		
		assertThat(result, notNullValue());
		assertThat(result, empty());
	}
}
