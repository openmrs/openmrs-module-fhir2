/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.fhir2.provider.r3;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.openmrs.module.fhir2.FhirConstants.OPENMRS_FHIR_EXT_MEDICATION_REQUEST_FULFILLER_STATUS;
import static org.openmrs.module.fhir2.api.util.GeneralUtils.inputStreamToString;

import java.io.InputStream;
import java.util.Objects;

import lombok.AccessLevel;
import lombok.Getter;
import org.hl7.fhir.dstu3.model.Extension;
import org.hl7.fhir.dstu3.model.MedicationRequest;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.module.fhir2.BaseFhirIntegrationTest;
import org.openmrs.module.fhir2.providers.r3.BaseFhirR3IntegrationTest;
import org.openmrs.module.fhir2.providers.r3.MedicationRequestFhirResourceProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletResponse;

public class MedicationRequestFhirResourceProvider_2_2IntegrationTest extends BaseFhirR3IntegrationTest<MedicationRequestFhirResourceProvider, MedicationRequest> {
	
	private static final String MEDICATION_REQUEST_UUID = "546ba5a6-5aa6-4325-afc0-50bc00d5ffa1";
	
	private static final String MEDICATION_REQUEST_DATA_XML = "org/openmrs/module/fhir2/api/dao/impl/FhirMedicationRequest_2_2_initial_data.xml";
	
	private static final String JSON_PATCH_MEDICATION_REQUEST_PATH = "org/openmrs/module/fhir2/providers/MedicationRequest_patch.json";
	
	@Getter(AccessLevel.PUBLIC)
	@Autowired
	private MedicationRequestFhirResourceProvider resourceProvider;
	
	@Before
	@Override
	public void setup() throws Exception {
		super.setup();
		executeDataSet(MEDICATION_REQUEST_DATA_XML);
	}
	
	@Test
	public void shouldReturnExistingMedicationRequestAsJson() throws Exception {
		MockHttpServletResponse response = get("/MedicationRequest/" + MEDICATION_REQUEST_UUID)
		        .accept(BaseFhirIntegrationTest.FhirMediaTypes.JSON).go();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), is(BaseFhirIntegrationTest.FhirMediaTypes.JSON.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		MedicationRequest medicationRequest = readResponse(response);
		
		assertThat(medicationRequest, notNullValue());
		assertThat(medicationRequest.getIdElement().getIdPart(), equalTo(MEDICATION_REQUEST_UUID));
		assertThat(medicationRequest, validResource());
		
		// confirm that the new fulfiller extension has been added
		Extension extension = null;
		for (Extension e : medicationRequest.getExtension()) {
			if (e.getUrl().equalsIgnoreCase(OPENMRS_FHIR_EXT_MEDICATION_REQUEST_FULFILLER_STATUS)) {
				extension = e;
				break;
			}
		}
		
		assertThat(extension, notNullValue());
		assertThat(extension.getValue().toString(), is("RECEIVED"));
		
	}
	
	@Test
	public void shouldPatchExistingMedicationRequestViaJson() throws Exception {
		String jsonMedicationRequestPatch;
		try (InputStream is = this.getClass().getClassLoader().getResourceAsStream(JSON_PATCH_MEDICATION_REQUEST_PATH)) {
			Objects.requireNonNull(is);
			jsonMedicationRequestPatch = inputStreamToString(is, UTF_8);
		}
		
		MockHttpServletResponse response = patch("/MedicationRequest/" + MEDICATION_REQUEST_UUID)
		        .jsonMergePatch(jsonMedicationRequestPatch).accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), is(FhirMediaTypes.JSON.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		MedicationRequest medicationRequest = readResponse(response);
		
		assertThat(medicationRequest, notNullValue());
		assertThat(medicationRequest.getIdElement().getIdPart(), equalTo(MEDICATION_REQUEST_UUID));
		assertThat(medicationRequest, validResource());
		
		// confirm that the fulfiller extension has been updated
		Extension extension = null;
		for (Extension e : medicationRequest.getExtension()) {
			if (e.getUrl().equalsIgnoreCase(OPENMRS_FHIR_EXT_MEDICATION_REQUEST_FULFILLER_STATUS)) {
				extension = e;
				break;
			}
		}
		
		assertThat(extension, notNullValue());
		assertThat(extension.getValue().toString(), is("COMPLETED"));
	}
}
