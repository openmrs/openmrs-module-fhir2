/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.fhir2.api.impl;

import org.hl7.fhir.r4.model.Person;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.openmrs.PersonName;
import org.openmrs.module.fhir2.api.dao.FhirPersonDao;
import org.openmrs.module.fhir2.api.translators.PersonTranslator;

import java.util.ArrayList;
import java.util.Collection;

import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class FhirPersonServiceImplTest {

	private static final String GIVEN_NAME = "John";

	private static final String FAMILY_NAME = "kipchumba";

	private static final String PERSON_PARTIAL_NAME = "kip";

	private static final String NOT_FOUND_NAME = "not found name";

	@Mock
	private FhirPersonDao dao;

	@Mock
	private PersonTranslator personTranslator;

	private FhirPersonServiceImpl personService;

	@Before
	public void setUp() {
		personService = new FhirPersonServiceImpl();
		personService.setFhirPersonDao(dao);
		personService.setPersonTranslator(personTranslator);
	}

	@Test
	public void shouldReturnCollectionOfPersonForMatchOnPersonName() {
		Collection<org.openmrs.Person> people = new ArrayList<>();
		PersonName name = new PersonName();
		name.setUuid("test-uuid-1223-2312");
		name.setGivenName(GIVEN_NAME);
		org.openmrs.Person person = new org.openmrs.Person();
		person.setUuid("1223-2323-2323-nd23");
		person.setGender("M");
		person.addName(name);
		people.add(person);

		when(dao.findPersonsByName(GIVEN_NAME)).thenReturn(people);

		Collection<Person> personCollection = personService.findPersonsByName(GIVEN_NAME);
		assertNotNull(personCollection);
		assertEquals(personCollection.size(), 1);
	}

	@Test
	public void shouldReturnCollectionOfPersonsForPartialMatchOnPersonName() {
		Collection<org.openmrs.Person> people = new ArrayList<>();
		PersonName name = new PersonName();
		name.setUuid("test-uuid-1223-2312");
		name.setFamilyName(FAMILY_NAME);
		org.openmrs.Person person = new org.openmrs.Person();
		person.setUuid("1223-2323-2323-nd23");
		person.setGender("M");
		person.addName(name);
		people.add(person);
		when(dao.findPersonsByName(PERSON_PARTIAL_NAME)).thenReturn(people);

		Collection<Person> personCollection = personService.findPersonsByName(PERSON_PARTIAL_NAME);
		assertNotNull(personCollection);
		assertThat(personCollection, not(empty()));
		assertThat(personCollection.size(), greaterThanOrEqualTo(1));
	}

	@Test
	public void findPersonsByNonExistingName_shouldReturnEmptyCollection() {
		Collection<Person> people = personService.findPersonsByName(NOT_FOUND_NAME);
		assertThat(people, is(empty()));
	}
}
