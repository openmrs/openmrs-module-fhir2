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
import static org.openmrs.module.fhir2.api.util.GeneralUtils.inputStreamToString;

import java.io.InputStream;
import java.util.Objects;

import lombok.AccessLevel;
import lombok.Getter;
import org.hl7.fhir.r4.model.Medication;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.module.fhir2.BaseFhirIntegrationTest;
import org.openmrs.module.fhir2.providers.r4.BaseFhirR4IntegrationTest;
import org.openmrs.module.fhir2.providers.r4.MedicationFhirResourceProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletResponse;

public class MedicationFhirResourceProvider_2_2IntegrationTest extends BaseFhirR4IntegrationTest<MedicationFhirResourceProvider, Medication> {
	
	private static final String MEDICATION_UUID = "1085AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
	
	private static final String MEDICATION_DATA_XML = "org/openmrs/module/fhir2/api/dao/impl/FhirMedication_2_2_initial_data.xml";
	
	private static final String JSON_PATCH_MEDICATION_PATH = "org/openmrs/module/fhir2/providers/Medication_patch.json";
	
	private static final String JSON_PATCH_MEDICATION_FILE = "org/openmrs/module/fhir2/providers/Medication_json_patch.json";
	
	@Autowired
	@Getter(AccessLevel.PUBLIC)
	private MedicationFhirResourceProvider resourceProvider;
	
	@Before
	@Override
	public void setup() throws Exception {
		super.setup();
		executeDataSet(MEDICATION_DATA_XML);
	}
	
	@Test
	public void shouldReturnExistingMedicationAsJson() throws Exception {
		MockHttpServletResponse response = get("/Medication/" + MEDICATION_UUID)
		        .accept(BaseFhirIntegrationTest.FhirMediaTypes.JSON).go();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), is(BaseFhirIntegrationTest.FhirMediaTypes.JSON.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		Medication medication = readResponse(response);
		
		assertThat(medication, notNullValue());
		assertThat(medication.getIdElement().getIdPart(), equalTo(MEDICATION_UUID));
		assertThat(medication, validResource());
		assertThat(medication.getStatus(), is(Medication.MedicationStatus.ACTIVE));
	}
	
	@Test
	public void shouldPatchExistingMedicationViaJsonMergePatch() throws Exception {
		String jsonMedicationPatch;
		try (InputStream is = this.getClass().getClassLoader().getResourceAsStream(JSON_PATCH_MEDICATION_PATH)) {
			Objects.requireNonNull(is);
			jsonMedicationPatch = inputStreamToString(is, UTF_8);
		}
		
		MockHttpServletResponse response = patch("/Medication/" + MEDICATION_UUID).jsonMergePatch(jsonMedicationPatch)
		        .accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), is(FhirMediaTypes.JSON.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		Medication medication = readResponse(response);
		
		assertThat(medication, notNullValue());
		assertThat(medication.getIdElement().getIdPart(), equalTo(MEDICATION_UUID));
		assertThat(medication, validResource());
		
		assertThat(medication.getStatus(), is(Medication.MedicationStatus.ACTIVE));
	}
	
	@Test
	public void shouldPatchExistingMedicationViaJsonPatch() throws Exception {
		String jsonMedicationPatch;
		try (InputStream is = this.getClass().getClassLoader().getResourceAsStream(JSON_PATCH_MEDICATION_FILE)) {
			Objects.requireNonNull(is);
			jsonMedicationPatch = inputStreamToString(is, UTF_8);
		}
		
		MockHttpServletResponse response = patch("/Medication/" + MEDICATION_UUID).jsonPatch(jsonMedicationPatch)
				.accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), is(FhirMediaTypes.JSON.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		Medication medication = readResponse(response);
		
		assertThat(medication, notNullValue());
		assertThat(medication.getIdElement().getIdPart(), equalTo(MEDICATION_UUID));
		assertThat(medication, validResource());
		
		assertThat(medication.getStatus(), is(Medication.MedicationStatus.ACTIVE));
	}
}
