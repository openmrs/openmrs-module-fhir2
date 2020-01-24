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

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;

import lombok.AccessLevel;
import lombok.Getter;
import org.hl7.fhir.r4.model.Enumerations;
import org.hl7.fhir.r4.model.Person;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openmrs.module.fhir2.api.FhirPersonService;
import org.springframework.mock.web.MockHttpServletResponse;

@RunWith(MockitoJUnitRunner.class)
public class PersonFhirResourceProviderTest extends BaseFhirResourceProviderTest<PersonFhirResourceProvider> {

	private static final String PERSON_UUID = "12e3rt-23kk90-dfj384k-34k23";

	private static final String WRONG_PERSON_UUID = "34xxh45-xxx88xx-uu443-t4t2j5";

	@Mock
	private FhirPersonService fhirPersonService;

	@Getter(AccessLevel.PACKAGE)
	private PersonFhirResourceProvider resourceProvider;

	private Person person;

	@Before
	@Override
	public void setup() throws Exception {
		resourceProvider = new PersonFhirResourceProvider();
		resourceProvider.setFhirPersonService(fhirPersonService);
		super.setup();
	}

	@Before
	public void initPerson() {
		person = new Person();
		person.setId(PERSON_UUID);
		person.setGender(Enumerations.AdministrativeGender.MALE);
	}

	@Test
	public void shouldReturnResourceType() {
		assertThat(resourceProvider.getResourceType(), equalTo(Person.class));
		assertThat(resourceProvider.getResourceType().getName(), equalTo(Person.class.getName()));
	}

	@Test
	public void getPersonById_shouldReturnPerson() throws Exception {
		when(fhirPersonService.getPersonByUuid(PERSON_UUID)).thenReturn(person);

		MockHttpServletResponse response = get("/Person/" + PERSON_UUID)
				.accept(FhirMediaTypes.JSON)
				.go();

		assertThat(response, isOk());
	}

	@Test
	public void getPersonWithWrongUuid_shouldReturnIsNotFoundStatus() throws Exception {
		MockHttpServletResponse response = get("/Person/" + WRONG_PERSON_UUID).go();

		assertThat(response, isNotFound());
	}

}
