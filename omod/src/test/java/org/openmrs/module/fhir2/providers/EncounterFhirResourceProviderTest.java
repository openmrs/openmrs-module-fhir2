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

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;

import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import org.hl7.fhir.r4.model.Encounter;
import org.hl7.fhir.r4.model.IdType;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openmrs.module.fhir2.api.FhirEncounterService;

@RunWith(MockitoJUnitRunner.class)
public class EncounterFhirResourceProviderTest {
	
	private static final String ENCOUNTER_UUID = "123xx34-623hh34-22hj89-23hjy5";
	
	private static final String WRONG_ENCOUNTER_UUID = "c3467w-hi4jer83-56hj34-23hjy5";
	
	@Mock
	private FhirEncounterService encounterService;
	
	private EncounterFhirResourceProvider resourceProvider;
	
	private Encounter encounter;
	
	@Before
	public void setup() {
		resourceProvider = new EncounterFhirResourceProvider();
		resourceProvider.setEncounterService(encounterService);
	}
	
	@Before
	public void initEncounter() {
		encounter = new Encounter();
		encounter.setId(ENCOUNTER_UUID);
		encounter.setStatus(Encounter.EncounterStatus.UNKNOWN);
	}
	
	@Test
	public void getResourceType_shouldReturnResourceType() {
		assertThat(resourceProvider.getResourceType(), equalTo(Encounter.class));
		assertThat(resourceProvider.getResourceType().getName(), equalTo(Encounter.class.getName()));
	}
	
	@Test
	public void getEncounterByUuid_shouldReturnMatchingEncounter() {
		IdType id = new IdType();
		id.setValue(ENCOUNTER_UUID);
		when(encounterService.getEncounterByUuid(ENCOUNTER_UUID)).thenReturn(encounter);
		Encounter result = resourceProvider.getEncounterByUuid(id);
		assertThat(result, notNullValue());
		assertThat(result.getId(), notNullValue());
		assertThat(result.getId(), equalTo(ENCOUNTER_UUID));
		assertThat(result.getStatus(), equalTo(Encounter.EncounterStatus.UNKNOWN));
	}
	
	@Test(expected = ResourceNotFoundException.class)
	public void getEncounterWithWrongUuid_shouldThrowResourceNotFoundException() {
		IdType id = new IdType();
		id.setValue(WRONG_ENCOUNTER_UUID);
		Encounter result = resourceProvider.getEncounterByUuid(id);
		assertThat(result, nullValue());
	}
}
