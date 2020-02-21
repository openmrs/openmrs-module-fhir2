/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.fhir2.providers;

import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.Mockito.when;

import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import org.hl7.fhir.r4.model.AllergyIntolerance;
import org.hl7.fhir.r4.model.IdType;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openmrs.module.fhir2.api.FhirAllergyIntoleranceService;

@RunWith(MockitoJUnitRunner.class)
public class AllergyIntoleranceFhirResourceProviderTest {
	
	private static final String ALLERGY_UUID = "1085AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
	
	private static final String WRONG_ALLERGY_UUID = "2085AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
	
	@Mock
	private FhirAllergyIntoleranceService service;
	
	private AllergyIntoleranceFhirResourceProvider resourceProvider;
	
	private AllergyIntolerance allergyIntolerance;
	
	@Before
	public void setup() {
		resourceProvider = new AllergyIntoleranceFhirResourceProvider();
		resourceProvider.setFhirAllergyIntoleranceService(service);
	}
	
	@Before
	public void initAllergyIntolerance() {
		allergyIntolerance = new AllergyIntolerance();
		allergyIntolerance.setId(ALLERGY_UUID);
		allergyIntolerance.addCategory(AllergyIntolerance.AllergyIntoleranceCategory.FOOD);
	}
	
	@Test
	public void getResourceType_shouldReturnResourceType() {
		assertThat(resourceProvider.getResourceType(), equalTo(AllergyIntolerance.class));
		assertThat(resourceProvider.getResourceType().getName(), equalTo(AllergyIntolerance.class.getName()));
	}
	
	@Test
	public void getAllergyIntoleranceByUuid_shouldReturnMatchingAllergy() {
		when(service.getAllergyIntoleranceByUuid(ALLERGY_UUID)).thenReturn(allergyIntolerance);
		
		IdType id = new IdType();
		id.setValue(ALLERGY_UUID);
		AllergyIntolerance allergy = resourceProvider.getAllergyIntoleranceByUuid(id);
		assertThat(allergy, notNullValue());
		assertThat(allergy.getId(), notNullValue());
		assertThat(allergy.getId(), equalTo(ALLERGY_UUID));
	}
	
	@Test(expected = ResourceNotFoundException.class)
	public void getAllergyIntoleranceByUuid_shouldThrowResourceNotFoundException() {
		IdType id = new IdType();
		id.setValue(WRONG_ALLERGY_UUID);
		AllergyIntolerance result = resourceProvider.getAllergyIntoleranceByUuid(id);
		assertThat(result, nullValue());
	}
}
