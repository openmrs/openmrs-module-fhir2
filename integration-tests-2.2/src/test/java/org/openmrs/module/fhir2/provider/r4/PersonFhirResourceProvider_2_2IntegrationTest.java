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
import org.hl7.fhir.r4.model.Enumerations;
import org.hl7.fhir.r4.model.Person;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.module.fhir2.providers.r4.BaseFhirR4IntegrationTest;
import org.openmrs.module.fhir2.providers.r4.PersonFhirResourceProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletResponse;

public class PersonFhirResourceProvider_2_2IntegrationTest extends BaseFhirR4IntegrationTest<PersonFhirResourceProvider, Person> {
	
	private static final String PERSON_UUID = "61b38324-e2fd-4feb-95b7-9e9a2a4400df";
	
	private static final String PERSON_DATA_XML = "org/openmrs/module/fhir2/api/dao/impl/FhirPerson_2_2_initial_data.xml";
	
	private static final String JSON_PATCH_PERSON_PATH = "org/openmrs/module/fhir2/providers/Person_json_merge_patch.json";
	
	@Getter(AccessLevel.PUBLIC)
	@Autowired
	private PersonFhirResourceProvider resourceProvider;
	
	@Before
	@Override
	public void setup() throws Exception {
		super.setup();
		executeDataSet(PERSON_DATA_XML);
	}
	
	@Test
	public void shouldReturnExistingPersonAsJson() throws Exception {
		MockHttpServletResponse response = get("/Person/" + PERSON_UUID).accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), is(FhirMediaTypes.JSON.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		Person person = readResponse(response);
		
		assertThat(person, notNullValue());
		assertThat(person.getIdElement().getIdPart(), equalTo(PERSON_UUID));
		assertThat(person, validResource());
	}
	
	@Test
	public void shouldPatchPersonResourceViaJsonMergePatch() throws Exception {
		String jsonPersonPatch;
		try (InputStream is = this.getClass().getClassLoader().getResourceAsStream(JSON_PATCH_PERSON_PATH)) {
			Objects.requireNonNull(is);
			jsonPersonPatch = inputStreamToString(is, UTF_8);
		}
		
		MockHttpServletResponse response = patch("/Person/" + PERSON_UUID).jsonMergePatch(jsonPersonPatch)
		        .accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), is(FhirMediaTypes.JSON.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		Person person = readResponse(response);
		
		assertThat(person, notNullValue());
		assertThat(person.getIdElement().getIdPart(), equalTo(PERSON_UUID));
		assertThat(person, validResource());
		
		assertThat(person.getGender(), equalTo(Enumerations.AdministrativeGender.FEMALE));
	}
}
