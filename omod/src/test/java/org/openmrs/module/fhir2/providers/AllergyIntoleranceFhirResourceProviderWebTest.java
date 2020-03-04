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
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.Mockito.when;
import static org.openmrs.module.fhir2.FhirConstants.AUT;
import static org.openmrs.module.fhir2.FhirConstants.AUTHOR;

import javax.servlet.ServletException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;

import lombok.AccessLevel;
import lombok.Getter;
import org.hl7.fhir.r4.model.AllergyIntolerance;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.Provenance;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.api.FhirAllergyIntoleranceService;
import org.openmrs.module.fhir2.api.util.FhirUtils;
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
	
	@Test
	public void shouldVerifyAllergyIntoleranceHistoryByIdUri() throws Exception {
		AllergyIntolerance allergyIntolerance = new AllergyIntolerance();
		allergyIntolerance.setId(ALLERGY_UUID);
		when(allergyService.getAllergyIntoleranceByUuid(ALLERGY_UUID)).thenReturn(allergyIntolerance);
		
		MockHttpServletResponse response = getAllergyIntoleranceHistoryRequest();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), equalTo(BaseFhirResourceProviderTest.FhirMediaTypes.JSON.toString()));
	}
	
	@Test
	public void shouldGetAllergyIntoleranceHistoryById() throws IOException, ServletException {
		Provenance provenance = new Provenance();
		provenance.setId(new IdType(FhirUtils.uniqueUuid()));
		provenance.setRecorded(new Date());
		provenance.setActivity(new CodeableConcept().addCoding(
		    new Coding().setCode("CREATE").setSystem(FhirConstants.FHIR_TERMINOLOGY_DATA_OPERATION).setDisplay("create")));
		provenance.addAgent(new Provenance.ProvenanceAgentComponent()
		        .setType(new CodeableConcept().addCoding(new Coding().setCode(AUT).setDisplay(AUTHOR)
		                .setSystem(FhirConstants.FHIR_TERMINOLOGY_PROVENANCE_PARTICIPANT_TYPE)))
		        .addRole(new CodeableConcept().addCoding(
		            new Coding().setCode("").setDisplay("").setSystem(FhirConstants.FHIR_TERMINOLOGY_PARTICIPATION_TYPE))));
		AllergyIntolerance allergyIntolerance = new AllergyIntolerance();
		allergyIntolerance.setId(ALLERGY_UUID);
		allergyIntolerance.addContained(provenance);
		
		when(allergyService.getAllergyIntoleranceByUuid(ALLERGY_UUID)).thenReturn(allergyIntolerance);
		
		MockHttpServletResponse response = getAllergyIntoleranceHistoryRequest();
		
		Bundle results = readBundleResponse(response);
		assertThat(results, notNullValue());
		assertThat(results.hasEntry(), is(true));
		assertThat(results.getEntry().get(0).getResource(), notNullValue());
		assertThat(results.getEntry().get(0).getResource().getResourceType().name(),
		    equalTo(Provenance.class.getSimpleName()));
		
	}
	
	@Test
	public void getAllergyIntoleranceHistoryById_shouldReturnBundleWithEmptyEntriesIfResourceContainedIsEmpty()
	        throws Exception {
		AllergyIntolerance allergyIntolerance = new AllergyIntolerance();
		allergyIntolerance.setId(ALLERGY_UUID);
		allergyIntolerance.setContained(new ArrayList<>());
		when(allergyService.getAllergyIntoleranceByUuid(ALLERGY_UUID)).thenReturn(allergyIntolerance);
		
		MockHttpServletResponse response = getAllergyIntoleranceHistoryRequest();
		Bundle results = readBundleResponse(response);
		assertThat(results.hasEntry(), is(false));
	}
	
	@Test
	public void getAllergyIntoleranceHistoryById_shouldReturn404IfAllergyIntoleranceIdIsWrong() throws Exception {
		MockHttpServletResponse response = get("/AllergyIntolerance/" + WRONG_ALLERGY_UUID + "/_history")
		        .accept(BaseFhirResourceProviderTest.FhirMediaTypes.JSON).go();
		
		assertThat(response, isNotFound());
	}
	
	private MockHttpServletResponse getAllergyIntoleranceHistoryRequest() throws IOException, ServletException {
		return get("/AllergyIntolerance/" + ALLERGY_UUID + "/_history")
		        .accept(BaseFhirResourceProviderTest.FhirMediaTypes.JSON).go();
	}
}
