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
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

import java.util.Collection;
import java.util.Date;
import java.util.List;

import ca.uhn.fhir.rest.param.TokenAndListParam;
import ca.uhn.fhir.rest.param.TokenParam;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.Obs;
import org.openmrs.api.ConceptService;
import org.openmrs.api.PatientService;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.TestFhirSpringConfiguration;
import org.openmrs.module.fhir2.api.dao.FhirConditionDao;
import org.openmrs.module.fhir2.api.search.param.SearchParameterMap;
import org.openmrs.test.BaseModuleContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

@ContextConfiguration(classes = TestFhirSpringConfiguration.class, inheritLocations = false)
public class FhirConditionDaoImplTest extends BaseModuleContextSensitiveTest {
	
	private static final Integer VOIDED_OBS_CONDITION_ID = 33;
	
	private static final String EXISTING_OBS_CONDITION_UUID = "86sgf-1f7d-4394-a316-0a458edf28c4";
	
	private static final String WRONG_OBS_CONDITION_UUID = "430bbb70-6a9c-4e1e-badb-9d1034b1b5e9";
	
	private static final String NEW_OBS_CONDITION_UUID = "NEWbbb70-6a9c-4e1e-badb-9d1034b1b5e9";
	
	private static final String OBS_CONDITION_INITIAL_DATA_XML = "org/openmrs/module/fhir2/api/dao/impl/FhirObsConditionDaoImplTest_initial_data.xml";
	
	private static final String OBS_CONDITION_CONCEPT_ID = "116128";
	
	private static final String OBS_CONDITION_CODED_CONCEPT_ID = "1284";
	
	private static final String WORNG_OBS_CONDITION_CONCEPT_ID = "116145";
	
	private static final Integer PATIENT_ID = 6;
	
	@Autowired
	private FhirConditionDao<Obs> dao;
	
	@Autowired
	private ConceptService conceptService;
	
	@Autowired
	private PatientService patientService;
	
	@Before
	public void setUp() throws Exception {
		executeDataSet(OBS_CONDITION_INITIAL_DATA_XML);
	}
	
	@Test
	public void get_shouldGetObsConditionByUuid() {
		Obs result = dao.get(EXISTING_OBS_CONDITION_UUID);
		assertThat(result, notNullValue());
		assertThat(result.getUuid(), equalTo(EXISTING_OBS_CONDITION_UUID));
	}
	
	@Test
	public void get_shouldReturnNullIfObsNotFoundByUuid() {
		Obs result = dao.get(WRONG_OBS_CONDITION_UUID);
		assertThat(result, nullValue());
	}
	
	@Test
	public void search_shouldReturnConditonResourceUuidsWithObsCoded_1284() {
		TokenAndListParam code = new TokenAndListParam();
		TokenParam codingToken = new TokenParam();
		codingToken.setValue(OBS_CONDITION_CONCEPT_ID);
		code.addAnd(codingToken);
		
		SearchParameterMap theParams = new SearchParameterMap();
		theParams.addParameter(FhirConstants.CODED_SEARCH_HANDLER, code);
		
		List<Integer> matchingResourceIds = dao.getSearchResultIds(theParams);
		assertThat(matchingResourceIds, hasSize(2));
	}
	
	@Test
	public void search_shouldReturnNoConditonResourceUuidsWhenNoWrongConceptIdIsUsed() {
		TokenAndListParam code = new TokenAndListParam();
		TokenParam codingToken = new TokenParam();
		codingToken.setValue(WORNG_OBS_CONDITION_CONCEPT_ID);
		code.addAnd(codingToken);
		
		SearchParameterMap theParams = new SearchParameterMap();
		theParams.addParameter(FhirConstants.CODED_SEARCH_HANDLER, code);
		
		List<Integer> matchingResourceIds = dao.getSearchResultIds(theParams);
		assertThat(matchingResourceIds, hasSize(0));
	}
	
	@Test
	public void search_shouldReturnNoVoidedConditionResourceUuids() {
		TokenAndListParam code = new TokenAndListParam();
		TokenParam codingToken = new TokenParam();
		codingToken.setValue(OBS_CONDITION_CONCEPT_ID);
		code.addAnd(codingToken);
		
		SearchParameterMap theParams = new SearchParameterMap();
		theParams.addParameter(FhirConstants.CODED_SEARCH_HANDLER, code);
		
		List<Integer> matchingResourceIds = dao.getSearchResultIds(theParams);
		assertThat(matchingResourceIds, not(hasItem(VOIDED_OBS_CONDITION_ID)));
	}
	
	@Test
	public void search_shouldReturnSearchQuery() {
		TokenAndListParam code = new TokenAndListParam();
		TokenParam codingToken = new TokenParam();
		codingToken.setValue(OBS_CONDITION_CONCEPT_ID);
		code.addAnd(codingToken);
		
		SearchParameterMap theParams = new SearchParameterMap();
		theParams.addParameter(FhirConstants.CODED_SEARCH_HANDLER, code);
		
		List<Integer> matchingResourceIds = dao.getSearchResultIds(theParams);
		Collection<Obs> obs = dao.getSearchResults(theParams, matchingResourceIds);
		assertThat(obs, notNullValue());
		assertThat(obs, hasSize(2));
	}
	
	@Test
	public void shouldSaveNewObsCondition() {
		Obs obsCondition = new Obs();
		obsCondition.setUuid(NEW_OBS_CONDITION_UUID);
		obsCondition.setObsDatetime(new Date());
		obsCondition.setConcept(conceptService.getConcept(OBS_CONDITION_CODED_CONCEPT_ID));
		
		org.openmrs.Patient patient = patientService.getPatient(PATIENT_ID);
		obsCondition.setPerson(patient);
		obsCondition.setValueCoded(conceptService.getConcept(OBS_CONDITION_CONCEPT_ID));
		
		dao.createOrUpdate(obsCondition);
		
		Obs result = dao.get(NEW_OBS_CONDITION_UUID);
		assertThat(result, notNullValue());
		assertThat(result.getUuid(), equalTo(NEW_OBS_CONDITION_UUID));
	}
	
	@Test
	public void shouldDeleteObsCondition() {
		Obs result = dao.delete(EXISTING_OBS_CONDITION_UUID);
		
		assertThat(result, notNullValue());
		assertThat(result.getUuid(), equalTo(EXISTING_OBS_CONDITION_UUID));
		assertThat(result.getVoided(), equalTo(true));
		
	}
}
