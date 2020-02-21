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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.when;

import lombok.AccessLevel;
import lombok.Getter;
import org.hl7.fhir.r4.model.AllergyIntolerance;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openmrs.module.fhir2.api.FhirAllergyIntoleranceService;
import org.openmrs.module.fhir2.web.servlet.BaseFhirResourceProviderTest;
import org.springframework.mock.web.MockHttpServletResponse;

@RunWith(MockitoJUnitRunner.class)
public class AllergyIntoleranceFhirResourceProviderWebTest extends BaseFhirResourceProviderTest<AllergyIntoleranceFhirResourceProvider, AllergyIntolerance> {
	
	private static final String ALLERGY_UUID = "1085AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
	
	private static final String WRONG_ALLERGY_UUID = "2085AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
	
	@Mock
	private FhirAllergyIntoleranceService allergyService;
	
	@Getter(AccessLevel.PUBLIC)
	private AllergyIntoleranceFhirResourceProvider allergyProvider;
	
	@Before
	@Override
	public void setup() throws Exception {
		allergyProvider = new AllergyIntoleranceFhirResourceProvider();
		allergyProvider.setFhirAllergyIntoleranceService(allergyService);
		super.setup();
	}
	
	@Override
	public AllergyIntoleranceFhirResourceProvider getResourceProvider() {
		return allergyProvider;
	}
	
	@Test
	public void getAllergyIntoleranceByUuid_shouldReturnAllergy() throws Exception {
		AllergyIntolerance allergy = new AllergyIntolerance();
		allergy.setId(ALLERGY_UUID);
		when(allergyService.getAllergyIntoleranceByUuid(ALLERGY_UUID)).thenReturn(allergy);
		
		MockHttpServletResponse response = get("/AllergyIntolerance/" + ALLERGY_UUID).accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), equalTo(FhirMediaTypes.JSON.toString()));
		assertThat(readResponse(response).getIdElement().getIdPart(), equalTo(ALLERGY_UUID));
	}
	
	@Test
	public void getAllergyIntoleranceByUuid_shouldReturn404() throws Exception {
		when(allergyService.getAllergyIntoleranceByUuid(WRONG_ALLERGY_UUID)).thenReturn(null);
		
		MockHttpServletResponse response = get("/AllergyIntolerance/" + WRONG_ALLERGY_UUID).accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isNotFound());
	}
}
