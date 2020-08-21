/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.fhir2.api.translators.impl;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.Mockito.when;

import org.hl7.fhir.r4.model.Coding;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openmrs.Location;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.api.mappings.EncounterClassMap;

@RunWith(MockitoJUnitRunner.class)
public class BaseEncounterTranslatorTest {
	
	private static final String LOCATION_UUID = "276379ef-07ce-4108-b5e0-c4dc21964b4f";
	
	@Mock
	private EncounterClassMap encounterClassMap;
	
	private BaseEncounterTranslator baseEncounterTranslator;
	
	@Before
	public void setup() {
		baseEncounterTranslator = new BaseEncounterTranslator() {};
		baseEncounterTranslator.setEncounterClassMap(encounterClassMap);
	}
	
	@Test
	public void shouldMapLocationToClass() {
		Location location = new Location();
		location.setUuid(LOCATION_UUID);
		when(encounterClassMap.getFhirClass(LOCATION_UUID)).thenReturn("AMB");
		
		Coding result = baseEncounterTranslator.mapLocationToClass(location);
		
		assertThat(result, notNullValue());
		assertThat(result, notNullValue());
		assertThat(result.getSystem(), is(FhirConstants.ENCOUNTER_CLASS_VALUE_SET_URI));
		assertThat(result.getCode(), is("AMB"));
	}
	
	@Test
	public void shouldMapLocationToAMBCodeWhenLocationIsNull() {
		Coding result = baseEncounterTranslator.mapLocationToClass(null);
		
		assertThat(result, notNullValue());
		assertThat(result.getSystem(), is(FhirConstants.ENCOUNTER_CLASS_VALUE_SET_URI));
		assertThat(result.getCode(), is("AMB"));
	}
}
