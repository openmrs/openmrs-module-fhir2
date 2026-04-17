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
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.fail;
import static org.openmrs.util.PrivilegeConstants.GET_CONCEPTS;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.hibernate.SessionFactory;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.Concept;
import org.openmrs.Drug;
import org.openmrs.DrugIngredient;
import org.openmrs.api.APIAuthenticationException;
import org.openmrs.api.context.Context;
import org.openmrs.module.fhir2.BaseFhirContextSensitiveTest;
import org.openmrs.module.fhir2.api.dao.FhirConceptDao;
import org.openmrs.module.fhir2.api.dao.FhirMedicationDao;
import org.openmrs.module.fhir2.api.search.param.SearchParameterMap;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

public class FhirMedicationDaoImplTest extends BaseFhirContextSensitiveTest {
	
	private static final String MEDICATION_UUID = "1085AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
	
	private static final String WRONG_MEDICATION_UUID = "9085AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
	
	private static final String INGREDIENT_UUID = "5088AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
	
	private static final String NEW_MEDICATION_UUID = "1088AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
	
	private static final String NEW_CONCEPT_UUID = "5086AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
	
	private static final String MEDICATION_INITIAL_DATA_XML = "org/openmrs/module/fhir2/api/dao/impl/FhirMedicationDaoImplTest_initial_data.xml";
	
	@Autowired
	@Qualifier("sessionFactory")
	private SessionFactory sessionFactory;
	
	@Autowired
	private FhirConceptDao fhirConceptDao;
	
	@Autowired
	private ObjectFactory<FhirMedicationDao> daoFactory;
	
	private FhirMedicationDao dao;
	
	private FhirMedicationDaoImpl daoImpl;
	
	@Before
	public void setup() throws Exception {
		executeDataSet(MEDICATION_INITIAL_DATA_XML);
		
		dao = daoFactory.getObject();
		daoImpl = new FhirMedicationDaoImpl();
		daoImpl.setSessionFactory(sessionFactory);
	}
	
	@Test
	public void getMedicationByUuid_shouldGetByUuid() {
		Drug medication = daoImpl.get(MEDICATION_UUID);
		assertThat(medication, notNullValue());
		assertThat(medication.getUuid(), notNullValue());
		assertThat(medication.getUuid(), equalTo(MEDICATION_UUID));
	}
	
	@Test
	public void getMedicationByUuid_shouldReturnNullWhenCalledWithUnknownUuid() {
		Drug medication = daoImpl.get(WRONG_MEDICATION_UUID);
		assertThat(medication, nullValue());
	}
	
	@Test
	public void saveMedication_shouldSaveNewMedication() {
		Drug drug = new Drug();
		drug.setUuid(NEW_MEDICATION_UUID);
		
		Concept concept = fhirConceptDao.get(NEW_CONCEPT_UUID);
		drug.setConcept(concept);
		
		DrugIngredient ingredient = new DrugIngredient();
		ingredient.setUuid(INGREDIENT_UUID);
		ingredient.setIngredient(concept);
		drug.setIngredients(Collections.singleton(ingredient));
		
		Drug result = daoImpl.createOrUpdate(drug);
		assertThat(result, notNullValue());
		assertThat(result.getUuid(), equalTo(NEW_MEDICATION_UUID));
		assertThat(result.getIngredients().size(), greaterThanOrEqualTo(1));
		assertThat(result.getIngredients().iterator().next().getUuid(), equalTo(INGREDIENT_UUID));
	}
	
	@Test
	public void saveMedication_shouldUpdateMedicationCorrectly() {
		Drug drug = daoImpl.get(MEDICATION_UUID);
		drug.setStrength("1000mg");
		
		Drug result = daoImpl.createOrUpdate(drug);
		assertThat(result, notNullValue());
		assertThat(result.getStrength(), equalTo("1000mg"));
	}
	
	@Test
	public void deleteMedication_shouldDeleteMedication() {
		Drug result = daoImpl.delete(MEDICATION_UUID);
		
		assertThat(result, notNullValue());
		assertThat(result.getRetired(), equalTo(true));
		assertThat(result.getRetireReason(), equalTo("Retired via FHIR API"));
	}
	
	@Test
	public void deleteMedication_shouldReturnNullIfDrugToDeleteDoesNotExist() {
		Drug result = daoImpl.delete(WRONG_MEDICATION_UUID);
		
		assertThat(result, nullValue());
	}
	
	@Test
	public void shouldRequireGetConceptsPrivilegeForGet() {
		Context.logout();
		
		try {
			dao.get(MEDICATION_UUID);
			fail("Expected APIAuthenticationException for missing privilege, but it was not thrown");
		}
		catch (APIAuthenticationException e) {
			assertThat(e.getMessage(), containsString("Privilege"));
		}
		
		try {
			Context.addProxyPrivilege(GET_CONCEPTS);
			assertThat(dao.get(MEDICATION_UUID), notNullValue());
		}
		finally {
			Context.removeProxyPrivilege(GET_CONCEPTS);
		}
	}
	
	@Test
	public void shouldRequireGetConceptsPrivilegeForGetByCollection() {
		Context.logout();
		
		try {
			dao.get(Arrays.asList(MEDICATION_UUID));
			fail("Expected APIAuthenticationException for missing privilege, but it was not thrown");
		}
		catch (APIAuthenticationException e) {
			assertThat(e.getMessage(), containsString("Privilege"));
		}
		
		try {
			Context.addProxyPrivilege(GET_CONCEPTS);
			List<Drug> medications = dao.get(Arrays.asList(MEDICATION_UUID));
			assertThat(medications, notNullValue());
		}
		finally {
			Context.removeProxyPrivilege(GET_CONCEPTS);
		}
	}
	
	@Test
	public void shouldRequireGetConceptsPrivilegeForGetSearchResults() {
		Context.logout();
		
		try {
			dao.getSearchResults(new SearchParameterMap());
			fail("Expected APIAuthenticationException for missing privilege, but it was not thrown");
		}
		catch (APIAuthenticationException e) {
			assertThat(e.getMessage(), containsString("Privilege"));
		}
		
		try {
			Context.addProxyPrivilege(GET_CONCEPTS);
			List<Drug> medications = dao.getSearchResults(new SearchParameterMap());
			assertThat(medications, notNullValue());
		}
		finally {
			Context.removeProxyPrivilege(GET_CONCEPTS);
		}
	}
	
	@Test
	public void shouldRequireGetConceptsPrivilegeForGetSearchResultsCount() {
		Context.logout();
		
		try {
			dao.getSearchResultsCount(new SearchParameterMap());
			fail("Expected APIAuthenticationException for missing privilege, but it was not thrown");
		}
		catch (APIAuthenticationException e) {
			assertThat(e.getMessage(), containsString("Privilege"));
		}
		
		try {
			Context.addProxyPrivilege(GET_CONCEPTS);
			int count = dao.getSearchResultsCount(new SearchParameterMap());
			assertThat(count, notNullValue());
		}
		finally {
			Context.removeProxyPrivilege(GET_CONCEPTS);
		}
	}
}
