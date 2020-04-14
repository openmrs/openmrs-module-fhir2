/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.fhir2.api.translators.impl;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.hl7.fhir.r4.model.ContactPoint;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openmrs.Person;
import org.openmrs.PersonAttribute;
import org.openmrs.PersonAttributeType;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.api.FhirGlobalPropertyService;
import org.openmrs.module.fhir2.api.dao.FhirPersonDao;
import org.openmrs.module.fhir2.api.translators.TelecomTranslator;

@RunWith(MockitoJUnitRunner.class)
public class PersonTelecomTranslatorImplTest {
	
	private static final String PERSON_UUID = "1223et-098342-2723bsd";
	
	private static final String PERSON_ATTRIBUTE_UUID = "12o3et5kl3-2e323-23g23-232h3y343s";
	
	private static final String PERSON_ATTRIBUTE_VALUE = "254723723456";
	
	private static final String PERSON_ATTRIBUTE_TYPE_NAME = "Contact";
	
	private static final String PERSON_ATTRIBUTE_TYPE_UUID = "14d4f066-15f5-102d-96e4-000c29c2a5d7";
	
	private static final String CONTACT_VALUE = "254701884000";
	
	private static final String CONTACT_ID = "uu23823gf-3834sd-s934n-34nss";
	
	@Mock
	private FhirGlobalPropertyService globalPropertyService;
	
	@Mock
	private TelecomTranslator<Object> telecomTranslator;
	
	@Mock
	private FhirPersonDao personDao;
	
	private PersonTelecomTranslatorImpl personTelecomTranslator;
	
	private Person person;
	
	private ContactPoint contactPoint;
	
	@Before
	public void setup() {
		personTelecomTranslator = new PersonTelecomTranslatorImpl();
		personTelecomTranslator.setFhirPersonDao(personDao);
		personTelecomTranslator.setGlobalPropertyService(globalPropertyService);
		personTelecomTranslator.setTelecomTranslator(telecomTranslator);
		
		person = new Person();
		person.setUuid(PERSON_UUID);
		PersonAttributeType attributeType = new PersonAttributeType();
		attributeType.setName(PERSON_ATTRIBUTE_TYPE_NAME);
		attributeType.setUuid(PERSON_ATTRIBUTE_TYPE_UUID);
		PersonAttribute personAttribute = new PersonAttribute();
		personAttribute.setUuid(PERSON_ATTRIBUTE_UUID);
		personAttribute.setValue(PERSON_ATTRIBUTE_VALUE);
		personAttribute.setAttributeType(attributeType);
		person.addAttribute(personAttribute);
		
		contactPoint = new ContactPoint();
		contactPoint.setId(CONTACT_ID);
		contactPoint.setValue(CONTACT_VALUE);
	}
	
	@Test
	public void toFhirResource_shouldTranslatePersonAttributeToContactPoint() {
		when(globalPropertyService.getGlobalProperty(FhirConstants.PERSON_ATTRIBUTE_TYPE_PROPERTY))
		        .thenReturn(PERSON_ATTRIBUTE_TYPE_UUID);
		when(personDao.getActiveAttributesByPersonAndAttributeTypeUuid(person, PERSON_ATTRIBUTE_TYPE_UUID))
		        .thenReturn(person.getActiveAttributes());
		List<ContactPoint> result = personTelecomTranslator.toFhirResource(person);
		assertThat(result, notNullValue());
		assertThat(result.isEmpty(), is(false));
		assertThat(result.size(), equalTo(1));
	}
	
	@Test
	public void toFhirResource_shouldTranslateToEmptyContactPointList() {
		person.setAttributes(null);
		List<ContactPoint> result = personTelecomTranslator.toFhirResource(person);
		assertThat(result, notNullValue());
		assertThat(result.isEmpty(), is(true));
	}
	
	@Test
	public void toOpenMrsType_shouldTranslateContactPointToPersonAttribute() {
		Set<PersonAttribute> result = personTelecomTranslator.toOpenmrsType(Collections.singletonList(contactPoint));
		assertThat(result, notNullValue());
		assertThat(result.isEmpty(), is(false));
		assertThat(result.size(), equalTo(1));
	}
	
	@Test
	public void toOpenMrsType_shouldTranslateToEmptyPersonAttributeSet() {
		Set<PersonAttribute> result = personTelecomTranslator.toOpenmrsType(new ArrayList<>());
		assertThat(result, notNullValue());
		assertThat(result.isEmpty(), is(true));
	}
	
}
