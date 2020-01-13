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

import org.hl7.fhir.r4.model.ContactPoint;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.openmrs.PersonAttribute;
import org.openmrs.PersonAttributeType;
import org.openmrs.api.PersonService;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.api.FhirGlobalPropertyService;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class TelecomTranslatorImplTest {
	
	private static final String CONTACT_POINT_ID = "e2323we23-323j34-23k23-23m23";
	
	private static final String CONTACT_POINT_VALUE = "254237283723723";
	
	private static final String PERSON_ATTRIBUTE_UUID = "12o3et5kl3-2e323-23g23-232h3y343s";
	
	private static final String PERSON_ATTRIBUTE_VALUE = "254723723456";
	
	private static final String PERSON_ATTRIBUTE_TYPE_UUID = "14d4f066-15f5-102d-96e4-000c29c2a5d7";
	
	private static final String ATTRIBUTE_TYPE_NAME = "Contact";
	
	private static final String NEW_PERSON_ATTRIBUTE_UUID = "28932b-349023-29323-23sj23";
	
	private static final String NEW_PERSON_ATTRIBUTE_VALUE = "0722 934 389";
	
	@Mock
	private PersonService personService;
	
	@Mock
	private FhirGlobalPropertyService administrationService;
	
	private TelecomTranslatorImpl telecomTranslator;
	
	@Before
	public void setUp() {
		telecomTranslator = new TelecomTranslatorImpl();
		telecomTranslator.setPersonService(personService);
		telecomTranslator.setGlobalPropertyService(administrationService);
	}
	
	@Test
	public void shouldTranslatePersonAttributeUuidToFhirContactPointId() {
		PersonAttribute personAttribute = new PersonAttribute();
		personAttribute.setUuid(PERSON_ATTRIBUTE_UUID);
		ContactPoint contactPoint = telecomTranslator.toFhirResource(personAttribute);
		assertThat(contactPoint, notNullValue());
		assertThat(contactPoint.getId(), equalTo(PERSON_ATTRIBUTE_UUID));
	}
	
	@Test
	public void shouldTranslatePersonAttributeValueToFhirContactPointValue() {
		PersonAttribute personAttribute = new PersonAttribute();
		personAttribute.setValue(PERSON_ATTRIBUTE_VALUE);
		ContactPoint contactPoint = telecomTranslator.toFhirResource(personAttribute);
		assertThat(contactPoint, notNullValue());
		assertThat(contactPoint.getValue(), equalTo(PERSON_ATTRIBUTE_VALUE));
	}
	
	@Test
	public void shouldTranslateFhirContactPointIdToPersonAttributeUuid() {
		ContactPoint contactPoint = new ContactPoint();
		contactPoint.setId(CONTACT_POINT_ID);
		PersonAttribute result = telecomTranslator.toOpenmrsType(contactPoint);
		assertThat(result, notNullValue());
		assertThat(result.getUuid(), equalTo(CONTACT_POINT_ID));
	}
	
	@Test
	public void shouldTranslateFhirContactPointValueToPersonAttributeValue() {
		ContactPoint contactPoint = new ContactPoint();
		contactPoint.setValue(CONTACT_POINT_VALUE);
		PersonAttribute result = telecomTranslator.toOpenmrsType(contactPoint);
		assertThat(result, notNullValue());
		assertThat(result.getValue(), equalTo(CONTACT_POINT_VALUE));
	}
	
	@Test
	public void shouldUpdatePersonAttributeUuid() {
		PersonAttribute personAttribute = new PersonAttribute();
		personAttribute.setUuid(PERSON_ATTRIBUTE_UUID);
		
		ContactPoint contactPoint = new ContactPoint();
		contactPoint.setId(NEW_PERSON_ATTRIBUTE_UUID);
		
		PersonAttribute result = telecomTranslator.toOpenmrsType(personAttribute, contactPoint);
		assertThat(result, notNullValue());
		assertThat(result.getUuid(), notNullValue());
		assertThat(result.getUuid(), equalTo(NEW_PERSON_ATTRIBUTE_UUID));
	}
	
	@Test
	public void shouldUpdatePersonAttributeValue() {
		PersonAttribute personAttribute = new PersonAttribute();
		personAttribute.setValue(PERSON_ATTRIBUTE_VALUE);
		
		ContactPoint contactPoint = new ContactPoint();
		contactPoint.setValue(NEW_PERSON_ATTRIBUTE_VALUE);
		
		PersonAttribute result = telecomTranslator.toOpenmrsType(personAttribute, contactPoint);
		assertThat(result, notNullValue());
		assertThat(result.getValue(), notNullValue());
		assertThat(result.getValue(), equalTo(NEW_PERSON_ATTRIBUTE_VALUE));
	}
	
	@Test
	public void shouldUpdatePersonAttributeWithTheCorrectPersonAttributeTypeUuid() {
		PersonAttribute personAttribute = new PersonAttribute();
		personAttribute.setUuid(PERSON_ATTRIBUTE_UUID);
		personAttribute.setValue(PERSON_ATTRIBUTE_VALUE);
		PersonAttributeType attributeType = new PersonAttributeType();
		attributeType.setName(ATTRIBUTE_TYPE_NAME);
		attributeType.setUuid(PERSON_ATTRIBUTE_TYPE_UUID);
		personAttribute.setAttributeType(attributeType);
		
		ContactPoint contactPoint = new ContactPoint();
		contactPoint.setId(NEW_PERSON_ATTRIBUTE_UUID);
		contactPoint.setValue(NEW_PERSON_ATTRIBUTE_VALUE);
		
		PersonAttribute result = telecomTranslator.toOpenmrsType(personAttribute, contactPoint);
		assertThat(result, notNullValue());
		assertThat(result.getValue(), notNullValue());
		assertThat(result.getAttributeType().getUuid(), equalTo(PERSON_ATTRIBUTE_TYPE_UUID));
	}
	
	@Test
	public void shouldTranslateWithCorrectPersonAttributeTypeForContactDetails() {
		ContactPoint contactPoint = new ContactPoint();
		contactPoint.setId(CONTACT_POINT_ID);
		contactPoint.setValue(CONTACT_POINT_VALUE);
		PersonAttributeType attributeType = new PersonAttributeType();
		attributeType.setName(ATTRIBUTE_TYPE_NAME);
		attributeType.setUuid(PERSON_ATTRIBUTE_TYPE_UUID);
		
		when(administrationService.getGlobalProperty(FhirConstants.PERSON_ATTRIBUTE_TYPE_PROPERTY)).thenReturn(
		    PERSON_ATTRIBUTE_TYPE_UUID);
		when(personService.getPersonAttributeTypeByUuid(PERSON_ATTRIBUTE_TYPE_UUID)).thenReturn(attributeType);
		
		PersonAttribute result = telecomTranslator.toOpenmrsType(contactPoint);
		assertThat(result, notNullValue());
		assertThat(result.getAttributeType().getUuid(), equalTo(PERSON_ATTRIBUTE_TYPE_UUID));
	}
}
