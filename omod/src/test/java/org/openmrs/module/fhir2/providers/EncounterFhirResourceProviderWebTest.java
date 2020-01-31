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
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;

import javax.servlet.ServletException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

import lombok.AccessLevel;
import lombok.Getter;
import org.hl7.fhir.r4.model.Encounter;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openmrs.module.fhir2.api.FhirEncounterService;
import org.openmrs.module.fhir2.web.servlet.BaseFhirResourceProviderTest;
import org.springframework.mock.web.MockHttpServletResponse;

@RunWith(MockitoJUnitRunner.class)
public class EncounterFhirResourceProviderWebTest extends BaseFhirResourceProviderTest<EncounterFhirResourceProvider, Encounter> {
	
	private static final String ENCOUNTER_UUID = "8a849d5e-6011-4279-a124-40ada5a687de";
	
	private static final String WRONG_ENCOUNTER_UUID = "9bf0d1ac-62a8-4440-a5a1-eb1015a7cc65";
	
	private static final String PATIENT_IDENTIFIER = "h43489-h";
	
	private static final String WRONG_PATIENT_IDENTIFIER = "xx78xx-h";
	
	@Mock
	private FhirEncounterService encounterService;
	
	@Getter(AccessLevel.PUBLIC)
	private EncounterFhirResourceProvider resourceProvider;
	
	@Before
	@Override
	public void setup() throws Exception {
		resourceProvider = new EncounterFhirResourceProvider();
		resourceProvider.setEncounterService(encounterService);
		super.setup();
	}
	
	@Test
	public void getEncounterByUuid_shouldReturnEncounter() throws Exception {
		Encounter encounter = new Encounter();
		encounter.setId(ENCOUNTER_UUID);
		when(encounterService.getEncounterByUuid(ENCOUNTER_UUID)).thenReturn(encounter);
		
		MockHttpServletResponse response = get("/Encounter/" + ENCOUNTER_UUID).accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), equalTo(FhirMediaTypes.JSON.toString()));
		assertThat(readResponse(response).getIdElement().getIdPart(), equalTo(ENCOUNTER_UUID));
	}
	
	@Test
	public void getEncounterByWrongUuid_shouldReturn404() throws Exception {
		when(encounterService.getEncounterByUuid(WRONG_ENCOUNTER_UUID)).thenReturn(null);
		
		MockHttpServletResponse response = get("/Encounter/" + WRONG_ENCOUNTER_UUID).accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isNotFound());
	}
	
	@Test
	public void findEncountersByPatientIdentifier_shouldReturnBundleOfEncounters() throws IOException, ServletException {
		Encounter encounter = new Encounter();
		encounter.setId(ENCOUNTER_UUID);
		when(encounterService.findEncountersByPatientIdentifier(PATIENT_IDENTIFIER))
		        .thenReturn(Collections.singletonList(encounter));
		
		MockHttpServletResponse response = get("/Encounter?patient.identifier=" + PATIENT_IDENTIFIER)
		        .accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), equalTo(FhirMediaTypes.JSON.toString()));
		assertThat(readBundleResponse(response).getEntry().size(), greaterThanOrEqualTo(1));
	}
	
	@Test
	public void findEncountersByWrongPatientIdentifier_shouldReturnBundleWithEmptyEntries()
	        throws IOException, ServletException {
		when(encounterService.findEncountersByPatientIdentifier(WRONG_PATIENT_IDENTIFIER)).thenReturn(new ArrayList<>());
		
		MockHttpServletResponse response = get("/Encounter?patient.identifier=" + WRONG_PATIENT_IDENTIFIER)
		        .accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), equalTo(FhirMediaTypes.JSON.toString()));
		assertThat(readBundleResponse(response).getEntry(), is(empty()));
	}
	
}
