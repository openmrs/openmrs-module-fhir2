/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.fhir2.provider.r4;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.exparity.hamcrest.date.DateMatchers.sameDay;
import static org.openmrs.module.fhir2.api.util.GeneralUtils.inputStreamToString;

import java.io.InputStream;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Objects;

import lombok.AccessLevel;
import lombok.Getter;
import org.hl7.fhir.r4.model.Encounter;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.module.fhir2.providers.r4.BaseFhirR4IntegrationTest;
import org.openmrs.module.fhir2.providers.r4.EncounterFhirResourceProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletResponse;

public class EncounterFhirResourceProvider_2_2IntegrationTest
		extends BaseFhirR4IntegrationTest<EncounterFhirResourceProvider, Encounter> {
	
	private static final String ENCOUNTER_UUID = "430bbb70-6a9c-4e1e-badb-9d1034b1b5e9";
	
	private static final String ENCOUNTER_DATA_XML = "org/openmrs/module/fhir2/api/dao/impl/FhirEncounter_2_2_initial_data.xml";
	
	private static final String JSON_PATCH_ENCOUNTER_PATCH = "org/openmrs/module/fhir2/providers/Encounter_patch.json";
	
	@Autowired
	@Getter(AccessLevel.PUBLIC)
	private EncounterFhirResourceProvider resourceProvider;
	
	@Before
	@Override
	public void setup() throws Exception {
		super.setup();
		executeDataSet(ENCOUNTER_DATA_XML);
	}
	
	@Test
	public void shouldReturnExistingEncounterAsJson() throws Exception {
		MockHttpServletResponse response = get("/Encounter/" + ENCOUNTER_UUID).accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), is(FhirMediaTypes.JSON.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		Encounter encounter = readResponse(response);
		
		assertThat(encounter, notNullValue());
		assertThat(encounter.getIdElement().getIdPart(), equalTo(ENCOUNTER_UUID));
		assertThat(encounter, validResource());
	}
	
	@Test
	public void shouldPatchExistingMedicationViaJsonMergePatch() throws Exception {
		String jsonEncounterPatch;
		try (InputStream is = this.getClass().getClassLoader().getResourceAsStream(JSON_PATCH_ENCOUNTER_PATCH)) {
			Objects.requireNonNull(is);
			jsonEncounterPatch = inputStreamToString(is, UTF_8);
		}
		
		MockHttpServletResponse response = patch("/Encounter/" + ENCOUNTER_UUID).jsonMergePatch(jsonEncounterPatch)
				.accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), is(FhirMediaTypes.JSON.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		Encounter encounter = readResponse(response);
		
		assertThat(encounter, notNullValue());
		assertThat(encounter.getIdElement().getIdPart(), equalTo(ENCOUNTER_UUID));
		assertThat(encounter, validResource());
		
		assertThat(encounter.getPeriod(), notNullValue());
		assertThat(encounter.getPeriod().hasStart(), is(true));
		assertThat(encounter.getPeriod().getStart(),
				sameDay(LocalDate.of(2005, 2, 1).atStartOfDay(ZoneId.ofOffset("UTC", ZoneOffset.of("+05:30")))
						.withZoneSameInstant(ZoneId.systemDefault()).toLocalDate()));
	}
}
