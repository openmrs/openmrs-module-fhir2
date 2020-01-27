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
import org.hl7.fhir.r4.model.Person;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openmrs.module.fhir2.api.FhirPersonService;
import org.openmrs.module.fhir2.web.servlet.BaseFhirResourceProviderTest;
import org.springframework.mock.web.MockHttpServletResponse;

@RunWith(MockitoJUnitRunner.class)
public class PersonFhirResourceProviderWebTest extends BaseFhirResourceProviderTest<PersonFhirResourceProvider, Person> {

	private static final String PERSON_UUID = "8a849d5e-6011-4279-a124-40ada5a687de";

	private static final String WRONG_PERSON_UUID = "9bf0d1ac-62a8-4440-a5a1-eb1015a7cc65";

	@Mock
	private FhirPersonService personService;

	@Getter(AccessLevel.PUBLIC)
	private PersonFhirResourceProvider resourceProvider;

	@Before
	@Override
	public void setup() throws Exception {
		resourceProvider = new PersonFhirResourceProvider();
		resourceProvider.setFhirPersonService(personService);
		super.setup();
	}

	@Test
	public void shouldReturnPersonByUuid() throws Exception {
		Person person = new Person();
		person.setId(PERSON_UUID);
		when(personService.getPersonByUuid(PERSON_UUID)).thenReturn(person);

		MockHttpServletResponse response = get("/Person/" + PERSON_UUID)
				.accept(FhirMediaTypes.JSON)
				.go();

		assertThat(response, isOk());
		assertThat(response.getContentType(), equalTo(FhirMediaTypes.JSON.toString()));

		Person resource = readResponse(response);
		assertThat(resource.getIdElement().getIdPart(), equalTo(PERSON_UUID));
	}

	@Test
	public void shouldReturn404IfPersonNotFound() throws Exception {
		when(personService.getPersonByUuid(WRONG_PERSON_UUID)).thenReturn(null);

		MockHttpServletResponse response = get("/Person/" + WRONG_PERSON_UUID)
				.accept(FhirMediaTypes.JSON)
				.go();

		assertThat(response, isNotFound());
	}
}
