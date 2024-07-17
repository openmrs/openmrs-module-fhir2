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
import static org.hamcrest.Matchers.*;

import java.util.ArrayList;
import java.util.List;

import ca.uhn.fhir.rest.param.*;
import org.hibernate.SessionFactory;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.Form;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.TestFhirSpringConfiguration;
import org.openmrs.module.fhir2.api.search.param.SearchParameterMap;
import org.openmrs.test.BaseModuleContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;

@ContextConfiguration(classes = TestFhirSpringConfiguration.class, inheritLocations = false)
public class FhirQuestionnaireDaoImplTest extends BaseModuleContextSensitiveTest {
	
	private static final Integer FORM_2_ID = 2;
	
	private static final String FORM_2_UUID = "504c83c7-cfbf-4ae7-a4da-bdfa3236689f";
	
	private static final String FORM_2_NAME = "Form 2 name";
	
	private static final String QUESTIONNAIRE_SEARCH_DATA_FILES = "org/openmrs/module/fhir2/api/dao/impl/FhirQuestionnaireDaoImplTest_initial_data.xml";
	
	private FhirQuestionnaireDaoImpl dao;
	
	@Autowired
	@Qualifier("sessionFactory")
	private SessionFactory sessionFactory;
	
	@Before
	public void setup() throws Exception {
		dao = new FhirQuestionnaireDaoImpl();
		dao.setSessionFactory(sessionFactory);
		executeDataSet(QUESTIONNAIRE_SEARCH_DATA_FILES);
	}
	
	@Test
	public void getQuestionnaireById_shouldRetrieveQuestionnaireById() {
		Form result = dao.getQuestionnaireById(2);
		
		assertThat(result, notNullValue());
		assertThat(result.getUuid(), equalTo(FORM_2_UUID));
		assertThat(result.getId(), equalTo(FORM_2_ID));
	}
	
	@Test
	public void getQuestionnaireById_shouldRetrieveQuestionnaireByIds() {
		List<Integer> ids = new ArrayList<>();
		ids.add(2);
		ids.add(3);
		List<Form> result = dao.getQuestionnairesByIds(ids);
		
		assertThat(result, notNullValue());
		assertThat(result.size(), equalTo(2));
		assertThat(result.get(0).getUuid(), equalTo(FORM_2_UUID));
		assertThat(result.get(0).getId(), equalTo(FORM_2_ID));
	}
	
	@Test
	public void getQuestionnaireById_shouldReturnNullIfQuestionnaireNotFound() {
		assertThat(dao.getQuestionnaireById(0), nullValue());
	}
	
	@Test
	public void getSearchResults_shouldRetrieveAllQuestionnaires() {
		
		List<Form> result = dao.getSearchResults(new SearchParameterMap());
		
		assertThat(result, notNullValue());
		assertThat(result.size(), equalTo(2));
		assertThat(result.get(0).getUuid(), equalTo(FORM_2_UUID));
		assertThat(result.get(0).getId(), equalTo(FORM_2_ID));
	}
	
	@Test
	public void getSearchResults_shouldRetrieveQuestionnaireByParamName() {
		
		StringAndListParam listParam = new StringAndListParam();
		listParam.addAnd(new StringParam(FORM_2_NAME));
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.NAME_SEARCH_HANDLER, listParam);
		
		List<Form> result = dao.getSearchResults(theParams);
		
		assertThat(result, notNullValue());
		assertThat(result.get(0).getUuid(), equalTo(FORM_2_UUID));
		assertThat(result.get(0).getId(), equalTo(FORM_2_ID));
	}
	
}
