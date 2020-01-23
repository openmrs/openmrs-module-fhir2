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
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.hl7.fhir.r4.model.Enumerations;
import org.hl7.fhir.r4.model.Person;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.openmrs.module.fhir2.WebTestFhirSpringConfiguration;
import org.openmrs.module.fhir2.api.FhirPersonService;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = WebTestFhirSpringConfiguration.class)
public class PersonFhirResourceProviderTest {

	private static final String PERSON_UUID = "12e3rt-23kk90-dfj384k-34k23";

	private static final String WRONG_PERSON_UUID = "34xxh45-xxx88xx-uu443-t4t2j5";

	@InjectMocks
	private PersonFhirResourceProvider personFhirResourceProvider;

	@Mock
	private FhirPersonService fhirPersonService;

	private MockMvc mockMvc;

	private Person person;

	@Before
	public void setUp() {
		personFhirResourceProvider = new PersonFhirResourceProvider();
		personFhirResourceProvider.setFhirPersonService(fhirPersonService);
		this.mockMvc = MockMvcBuilders.standaloneSetup(personFhirResourceProvider).build();
	}

	@Before
	public void initPerson() {
		person = new Person();
		person.setId(PERSON_UUID);
		person.setGender(Enumerations.AdministrativeGender.MALE);
	}

	@Test
	public void shouldReturnResourceType() {
		assertThat(personFhirResourceProvider.getResourceType(), equalTo(Person.class));
		assertThat(personFhirResourceProvider.getResourceType().getName(), equalTo(Person.class.getName()));
	}

	@Test
	public void getPersonById_shouldReturnPerson() throws Exception {
		when(fhirPersonService.getPersonByUuid(PERSON_UUID)).thenReturn(person);
		mockMvc.perform(get("/Person/{id}", PERSON_UUID)
				.accept(MediaType.APPLICATION_JSON)
				.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andDo(print())
				.andExpect(MockMvcResultMatchers.jsonPath("$.resourceType").exists())
				.andExpect(MockMvcResultMatchers.jsonPath("$.resourceType").value("Person"))
				.andExpect(MockMvcResultMatchers.jsonPath("$.id").value(PERSON_UUID))
				.andExpect(content().contentType(MediaType.APPLICATION_JSON));

	}

	@Test
	public void getPersonWithWrongUuid_shouldReturnIsNotFoundStatus() throws Exception {
		mockMvc.perform(get("/Person/{id}", WRONG_PERSON_UUID)
				.accept(MediaType.ALL_VALUE))
				.andExpect(status().isNotFound());

	}

}
