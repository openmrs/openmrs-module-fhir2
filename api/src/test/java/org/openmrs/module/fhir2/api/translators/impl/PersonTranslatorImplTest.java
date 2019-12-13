/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.fhir2.api.translators.impl;

import org.hl7.fhir.r4.model.Enumerations;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.openmrs.Person;
import org.openmrs.module.fhir2.api.translators.GenderTranslator;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class PersonTranslatorImplTest {

	private static final String PERSON_UUID = "1223et-098342-2723bsd";

	@Mock
	private GenderTranslator genderTranslator;

	private PersonTranslatorImpl personTranslator;

	private org.openmrs.Person person;

	@Before
	public void setup() {
		person = new Person();
		personTranslator = new PersonTranslatorImpl();
		personTranslator.setGenderTranslator(genderTranslator);
	}

	@Test
	public void shouldTranslateOpenmrsPersonToFhirPerson() {
		org.hl7.fhir.r4.model.Person result = personTranslator.toFhirResource(person);
		assertNotNull(result);
	}

	@Test
	public void shouldTranslatePersonUuidToFhirIdType() {
		person.setUuid(PERSON_UUID);
		org.hl7.fhir.r4.model.Person result = personTranslator.toFhirResource(person);
		assertNotNull(result);
		assertNotNull(result.getId());
		assertEquals(result.getId(), PERSON_UUID);
	}

	@Test
	public void shouldTranslateOpenmrsGenderToFhirGenderType() {
		when(genderTranslator.toFhirResource(argThat(equalTo("F")))).thenReturn(Enumerations.AdministrativeGender.FEMALE);
		person.setGender("F");

		org.hl7.fhir.r4.model.Person result = personTranslator.toFhirResource(person);
		assertNotNull(result);
		assertNotNull(result.getGender());
		assertEquals(result.getGender(), Enumerations.AdministrativeGender.FEMALE);

	}

}
