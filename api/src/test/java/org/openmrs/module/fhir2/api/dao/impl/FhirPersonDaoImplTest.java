/*
  This Source Code Form is subject to the terms of the Mozilla Public License,
  v. 2.0. If a copy of the MPL was not distributed with this file, You can
  obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
  the terms of the Healthcare Disclaimer located at http://openmrs.org/license.

  Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
  graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.fhir2.api.dao.impl;

import org.junit.Before;
import org.junit.Test;
import org.openmrs.Person;
import org.openmrs.api.PersonService;
import org.openmrs.module.fhir2.TestFhirSpringConfiguration;
import org.openmrs.test.BaseModuleContextSensitiveTest;
import org.springframework.test.context.ContextConfiguration;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

@ContextConfiguration(classes = TestFhirSpringConfiguration.class, inheritLocations = false)
public class FhirPersonDaoImplTest extends BaseModuleContextSensitiveTest {
	
	private static final String PERSON_UUID = "61b38324-e2fd-4feb-95b7-9e9a2a4400df";
	
	private static final String WRONG_PERSON_UUID = "wrong_person_uuid";
	
	private static final String PERSON_INITIAL_DATA_XML = "org/openmrs/module/fhir2/api/dao/impl/FhirPersonDaoImplTest_initial_data.xml";
	
	private static final String GENDER = "M";
	
	private static final String GIVEN_NAME = "John";
	
	private FhirPersonDaoImpl fhirPersonDao;
	
	@Inject
	@Named("personService")
	private Provider<PersonService> personServiceProvider;
	
	@Before
	public void setup() throws Exception {
		fhirPersonDao = new FhirPersonDaoImpl();
		fhirPersonDao.setPersonService(personServiceProvider.get());
		executeDataSet(PERSON_INITIAL_DATA_XML);
	}
	
	@Test
	public void getPersonByUuid_shouldReturnMatchingPerson() {
		Person person = fhirPersonDao.getPersonByUuid(PERSON_UUID);
		assertNotNull(person);
		assertEquals(person.getUuid(), PERSON_UUID);
		assertEquals(person.getGender(), GENDER);
		assertEquals(person.getGivenName(), GIVEN_NAME);
	}
	
	@Test
	public void getPersonByWithWrongUuid_shouldReturnNullPerson() {
		Person person = fhirPersonDao.getPersonByUuid(WRONG_PERSON_UUID);
		assertNull(person);
	}
}
