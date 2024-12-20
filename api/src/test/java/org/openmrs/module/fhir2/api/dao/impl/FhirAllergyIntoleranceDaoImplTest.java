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
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.Before;
import org.junit.Test;
import org.openmrs.Allergen;
import org.openmrs.AllergenType;
import org.openmrs.Allergy;
import org.openmrs.AllergyReaction;
import org.openmrs.Concept;
import org.openmrs.module.fhir2.TestFhirSpringConfiguration;
import org.openmrs.module.fhir2.api.dao.FhirAllergyIntoleranceDao;
import org.openmrs.module.fhir2.api.dao.FhirConceptDao;
import org.openmrs.test.BaseModuleContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

@ContextConfiguration(classes = TestFhirSpringConfiguration.class, inheritLocations = false)
public class FhirAllergyIntoleranceDaoImplTest extends BaseModuleContextSensitiveTest {
	
	private static final String ALLERGY_INTOLERANCE_INITIAL_DATA_XML = "org/openmrs/module/fhir2/api/dao/impl/FhirAllergyIntoleranceDaoImplTest_initial_data.xml";
	
	private static final String ALLERGY_UUID = "1085AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
	
	private static final String NEW_ALLERGY_UUID = "9999AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
	
	private static final String CODED_REACTION_UUID = "5087AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
	
	@Autowired
	private FhirAllergyIntoleranceDao allergyDao;
	
	@Autowired
	private FhirConceptDao conceptDao;
	
	@Before
	public void setup() throws Exception {
		executeDataSet(ALLERGY_INTOLERANCE_INITIAL_DATA_XML);
	}
	
	@Test
	public void getAllergyIntoleranceByUuid_shouldGetByUuid() {
		Allergy allergy = allergyDao.get(ALLERGY_UUID);
		assertThat(allergy, notNullValue());
		assertThat(allergy.getUuid(), notNullValue());
		assertThat(allergy.getUuid(), equalTo(ALLERGY_UUID));
	}
	
	@Test
	public void getAllergyIntoleranceByUuid_shouldReturnNullWhenCalledWithUnknownUuid() {
		Allergy allergy = allergyDao.get(NEW_ALLERGY_UUID);
		assertThat(allergy, nullValue());
	}
	
	@Test
	public void saveAllergy_shouldSaveAllergyCorrectly() {
		Allergy existing = allergyDao.get(ALLERGY_UUID);
		
		Allergy newAllergy = new Allergy();
		
		newAllergy.setPatient(existing.getPatient());
		
		Allergen allergen = new Allergen();
		allergen.setAllergenType(AllergenType.ENVIRONMENT);
		allergen.setCodedAllergen(conceptDao.get("5086AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA"));
		
		newAllergy.setAllergen(allergen);
		newAllergy.setUuid(NEW_ALLERGY_UUID);
		
		Allergy result = allergyDao.createOrUpdate(newAllergy);
		assertThat(result, notNullValue());
		assertThat(result.getUuid(), equalTo(NEW_ALLERGY_UUID));
	}
	
	@Test
	public void saveAllergy_shouldSaveAllergyReactionsCorrectly() {
		Allergy existing = allergyDao.get(ALLERGY_UUID);
		Allergy newAllergy = new Allergy();
		newAllergy.setPatient(existing.getPatient());
		
		Allergen allergen = new Allergen();
		allergen.setAllergenType(AllergenType.ENVIRONMENT);
		allergen.setCodedAllergen(conceptDao.get("5086AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA"));
		
		newAllergy.setAllergen(allergen);
		newAllergy.setUuid(NEW_ALLERGY_UUID);
		
		Concept codedReaction = conceptDao.get(CODED_REACTION_UUID);
		
		AllergyReaction reaction = new AllergyReaction(newAllergy, codedReaction, "Test Reaction");
		newAllergy.addReaction(reaction);
		
		Allergy result = allergyDao.createOrUpdate(newAllergy);
		assertThat(result, notNullValue());
		assertThat(result.getUuid(), equalTo(NEW_ALLERGY_UUID));
		assertThat(result.getReactions().get(0).getReaction().getUuid(), equalTo(CODED_REACTION_UUID));
	}
	
}
