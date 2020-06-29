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
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.nullValue;

import java.util.Collections;

import org.hibernate.SessionFactory;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.Concept;
import org.openmrs.Drug;
import org.openmrs.DrugIngredient;
import org.openmrs.module.fhir2.TestFhirSpringConfiguration;
import org.openmrs.module.fhir2.api.dao.FhirConceptDao;
import org.openmrs.test.BaseModuleContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;

@ContextConfiguration(classes = TestFhirSpringConfiguration.class, inheritLocations = false)
public class FhirMedicationDaoImplTest extends BaseModuleContextSensitiveTest {
	
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
	
	private FhirMedicationDaoImpl medicationDao;
	
	@Before
	public void setup() throws Exception {
		medicationDao = new FhirMedicationDaoImpl();
		medicationDao.setSessionFactory(sessionFactory);
		executeDataSet(MEDICATION_INITIAL_DATA_XML);
	}
	
	@Test
	public void getMedicationByUuid_shouldGetByUuid() {
		Drug medication = medicationDao.get(MEDICATION_UUID);
		assertThat(medication, notNullValue());
		assertThat(medication.getUuid(), notNullValue());
		assertThat(medication.getUuid(), equalTo(MEDICATION_UUID));
	}
	
	@Test
	public void getMedicationByUuid_shouldReturnNullWhenCalledWithUnknownUuid() {
		Drug medication = medicationDao.get(WRONG_MEDICATION_UUID);
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
		
		Drug result = medicationDao.createOrUpdate(drug);
		assertThat(result, notNullValue());
		assertThat(result.getUuid(), equalTo(NEW_MEDICATION_UUID));
		assertThat(result.getIngredients().size(), greaterThanOrEqualTo(1));
		assertThat(result.getIngredients().iterator().next().getUuid(), equalTo(INGREDIENT_UUID));
	}
	
	@Test
	public void saveMedication_shouldUpdateMedicationCorrectly() {
		Drug drug = medicationDao.get(MEDICATION_UUID);
		drug.setStrength("1000mg");
		
		Drug result = medicationDao.createOrUpdate(drug);
		assertThat(result, notNullValue());
		assertThat(result.getStrength(), equalTo("1000mg"));
	}
	
	@Test
	public void deleteMedication_shouldDeleteMedication() {
		Drug result = medicationDao.delete(MEDICATION_UUID);
		
		assertThat(result, notNullValue());
		assertThat(result.getRetired(), equalTo(true));
		assertThat(result.getRetireReason(), equalTo("Retired via FHIR API"));
	}
	
	@Test
	public void deleteMedication_shouldReturnNullIfDrugToDeleteDoesNotExist() {
		Drug result = medicationDao.delete(WRONG_MEDICATION_UUID);
		
		assertThat(result, nullValue());
	}
	
}
