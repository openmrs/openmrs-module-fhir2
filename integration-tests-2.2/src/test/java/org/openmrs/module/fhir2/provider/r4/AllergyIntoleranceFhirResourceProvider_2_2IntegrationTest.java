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
import org.hl7.fhir.r4.model.AllergyIntolerance;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.module.fhir2.providers.r4.AllergyIntoleranceFhirResourceProvider;
import org.openmrs.module.fhir2.providers.r4.BaseFhirR4IntegrationTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletResponse;

public class AllergyIntoleranceFhirResourceProvider_2_2IntegrationTest extends BaseFhirR4IntegrationTest<AllergyIntoleranceFhirResourceProvider, AllergyIntolerance> {
	
	private static final String ALLERGY_UUID = "1085AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
	
	private static final String ALLERGY_DATA_XML = "org/openmrs/module/fhir2/api/dao/impl/FhirAllergyIntolerance_2_2_initial_data.xml";
	
	private static final String JSON_PATCH_ALLERGY_PATH = "org/openmrs/module/fhir2/providers/AllergyIntolerance_json_merge_patch.json";
	
	@Getter(AccessLevel.PUBLIC)
	@Autowired
	private AllergyIntoleranceFhirResourceProvider resourceProvider;
	
	@Before
	@Override
	public void setup() throws Exception {
		super.setup();
		executeDataSet(ALLERGY_DATA_XML);
	}
	
	@Test
	public void shouldReturnExistingAllergyAsJson() throws Exception {
		MockHttpServletResponse response = get("/AllergyIntolerance/" + ALLERGY_UUID).accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), is(FhirMediaTypes.JSON.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		AllergyIntolerance allergyIntolerance = readResponse(response);
		
		assertThat(allergyIntolerance, notNullValue());
		assertThat(allergyIntolerance.getIdElement().getIdPart(), equalTo(ALLERGY_UUID));
		assertThat(allergyIntolerance, validResource());
	}
	
	@Test
	public void shouldPatchExistingAllergyViaJsonMergePatch() throws Exception {
		String jsonAllergyPatch;
		try (InputStream is = this.getClass().getClassLoader().getResourceAsStream(JSON_PATCH_ALLERGY_PATH)) {
			Objects.requireNonNull(is);
			jsonAllergyPatch = inputStreamToString(is, UTF_8);
		}
		
		MockHttpServletResponse response = patch("/AllergyIntolerance/" + ALLERGY_UUID)
		        .jsonMergePatch(jsonAllergyPatch).accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), is(FhirMediaTypes.JSON.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		AllergyIntolerance allergyIntolerance = readResponse(response);
		
		assertThat(allergyIntolerance, notNullValue());
		assertThat(allergyIntolerance.getIdElement().getIdPart(), equalTo(ALLERGY_UUID));
		assertThat(allergyIntolerance, validResource());
		
		//ensure clinical status has been patched
		assertThat(allergyIntolerance.hasClinicalStatus(), is(true));
		assertThat(allergyIntolerance.getClinicalStatus().getCodingFirstRep().getSystem(),
		    equalTo("http://terminology.hl7.org/CodeSystem/allergyintolerance-clinical"));
		assertThat(allergyIntolerance.getClinicalStatus().getCodingFirstRep().getCode(), equalTo("active"));
		assertThat(allergyIntolerance.getClinicalStatus().getText(), equalTo("Active"));
	}
	
}
