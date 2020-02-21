/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.fhir2.api.impl;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;

import org.hl7.fhir.r4.model.AllergyIntolerance;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openmrs.Allergy;
import org.openmrs.module.fhir2.api.dao.FhirAllergyIntoleranceDao;
import org.openmrs.module.fhir2.api.translators.AllergyIntoleranceTranslator;

@RunWith(MockitoJUnitRunner.class)
public class FhirAllergyIntoleranceServiceImplTest {
	
	private static final String ALLERGY_UUID = "1085AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
	
	private static final String WRONG_ALLERGY_UUID = "2085AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
	
	@Mock
	private FhirAllergyIntoleranceDao allergyIntoleranceDao;
	
	@Mock
	private AllergyIntoleranceTranslator translator;
	
	private FhirAllergyIntoleranceServiceImpl service;
	
	private Allergy omrsAllergy;
	
	private AllergyIntolerance fhirAllergy;
	
	@Before
	public void setup() {
		service = new FhirAllergyIntoleranceServiceImpl();
		service.setAllergyIntoleranceTranslator(translator);
		service.setAllergyIntoleranceDao(allergyIntoleranceDao);
		
		omrsAllergy = new Allergy();
		omrsAllergy.setUuid(ALLERGY_UUID);
		
		fhirAllergy = new AllergyIntolerance();
		fhirAllergy.setId(ALLERGY_UUID);
	}
	
	@Test
	public void getAllergyIntoleranceByUuid_shouldGetAllergyIntoleranceByUuid() {
		when(allergyIntoleranceDao.getAllergyIntoleranceByUuid(ALLERGY_UUID)).thenReturn(omrsAllergy);
		when(translator.toFhirResource(omrsAllergy)).thenReturn(fhirAllergy);
		
		AllergyIntolerance result = service.getAllergyIntoleranceByUuid(ALLERGY_UUID);
		assertThat(result, notNullValue());
		assertThat(result.getId(), notNullValue());
		assertThat(result.getId(), equalTo(ALLERGY_UUID));
	}
	
	@Test
	public void getAllergyIntoleranceByUuid_shouldReturnNullWhenCalledWithWrongUuid() {
		AllergyIntolerance result = service.getAllergyIntoleranceByUuid(WRONG_ALLERGY_UUID);
		assertThat(result, nullValue());
	}
	
}
