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
import org.openmrs.Location;
import org.openmrs.LocationAttribute;
import org.openmrs.LocationAttributeType;
import org.openmrs.api.LocationService;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.api.FhirGlobalPropertyService;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class LocationTelecomTranslatorImplTest {
	
	private static final String CONTACT_POINT_ID = "c0938432-1691-11df-97a5-7038c432aabf";
	
	private static final String CONTACT_POINT_VALUE = "Pipeline";
	
	private static final String LOCATION_ATTRIBUTE_UUID = "c0938432-1691-11df-97a5-7038c432abcd";
	
	private static final String LOCATION_ATTRIBUTE_VALUE = "Neiya street";
	
	private static final String LOCATION_ATTRIBUTE_TYPE_UUID = "abcde432-1691-11df-97a5-7038c432abcd";
	
	private static final String LOCATION_ATTRIBUTE_TYPE_NAME = "street name";
	
	private static final String TEST_LOCATION_ATTRIBUTE_UUID = "c0938432-1691-1111-97a5-7038c432abcd";
	
	private static final String TEST_LOCATION_ATTRIBUTE_VALUE = "Ngeria street";
	
	private static final String LOCATION_UUID = "c0938432-1691-11df-97a5-7038c432aaba";
	
	@Mock
	private LocationService locationService;
	
	@Mock
	private FhirGlobalPropertyService propertyService;
	
	private LocationTelecomTranslatorImpl translator;
	
	private LocationAttribute locationAttribute;
	
	private ContactPoint contactPoint;
	
	@Before
	public void setup() {
		translator = new LocationTelecomTranslatorImpl();
		translator.setLocationService(locationService);
		translator.setGlobalPropertyService(propertyService);
		
		locationAttribute = new LocationAttribute();
		contactPoint = new ContactPoint();
	}
	
	@Test
	public void toFhirResource_shouldTranslateUuidToId() {
		locationAttribute.setUuid(LOCATION_ATTRIBUTE_UUID);
		locationAttribute.setValue(LOCATION_ATTRIBUTE_VALUE);
		contactPoint = translator.toFhirResource(locationAttribute);
		assertNotNull(contactPoint.getId());
		assertEquals(contactPoint.getId(), LOCATION_ATTRIBUTE_UUID);
	}
	
	@Test
	public void toFhirResource_shouldTranslateValueToContactPointValue() {
		locationAttribute.setUuid(LOCATION_ATTRIBUTE_UUID);
		locationAttribute.setValue(LOCATION_ATTRIBUTE_VALUE);
		contactPoint = translator.toFhirResource(locationAttribute);
		assertNotNull(contactPoint.getValue());
		assertEquals(contactPoint.getValue(), LOCATION_ATTRIBUTE_VALUE);
	}
	
	@Test
	public void toOpenmrsType_shouldTranslateIdToUuid() {
		contactPoint.setId(CONTACT_POINT_ID);
		contactPoint.setValue(CONTACT_POINT_VALUE);
		locationAttribute = translator.toOpenmrsType(contactPoint);
		assertNotNull(locationAttribute.getUuid());
		assertEquals(locationAttribute.getUuid(), CONTACT_POINT_ID);
	}
	
	@Test
	public void toOpenmrsType_shouldTranslateontactPointValueToValue() {
		contactPoint.setId(CONTACT_POINT_ID);
		contactPoint.setValue(CONTACT_POINT_VALUE);
		locationAttribute = translator.toOpenmrsType(contactPoint);
		assertNotNull(locationAttribute.getValue());
		assertEquals(locationAttribute.getValue(), CONTACT_POINT_VALUE);
	}
	
	@Test
	public void toOpenmrsType_shouldUpdateLocationAttributeUuid() {
		locationAttribute.setUuid(LOCATION_ATTRIBUTE_UUID);
		
		contactPoint.setId(TEST_LOCATION_ATTRIBUTE_UUID);
		
		LocationAttribute attribute = translator.toOpenmrsType(locationAttribute, contactPoint);
		assertNotNull(attribute.getUuid());
		assertEquals(attribute.getUuid(), TEST_LOCATION_ATTRIBUTE_UUID);
	}
	
	@Test
	public void toOpenmrsType_shouldUpdateLocationAttributeValue() {
		locationAttribute.setValue(LOCATION_ATTRIBUTE_VALUE);
		
		contactPoint.setValue(TEST_LOCATION_ATTRIBUTE_VALUE);
		
		LocationAttribute attribute = translator.toOpenmrsType(locationAttribute, contactPoint);
		assertNotNull(attribute.getValue());
		assertEquals(attribute.getValue(), TEST_LOCATION_ATTRIBUTE_VALUE);
	}
	
	@Test
	public void toOpenmrsType_shouldNotUpdateLocationAttributeIfContactPointIsNull() {
		locationAttribute.setUuid(LOCATION_ATTRIBUTE_UUID);
		locationAttribute.setValue(LOCATION_ATTRIBUTE_VALUE);
		
		LocationAttribute attribute = translator.toOpenmrsType(locationAttribute, null);
		assertNotNull(attribute.getUuid());
		assertEquals(attribute.getUuid(), LOCATION_ATTRIBUTE_UUID);
		assertEquals(attribute.getValue(), LOCATION_ATTRIBUTE_VALUE);
		assertEquals(attribute, locationAttribute);
	}
	
	@Test
	public void toOpenmrsType_shouldAddCorrectLocationAttributeType() {
		locationAttribute.setUuid(LOCATION_ATTRIBUTE_UUID);
		locationAttribute.setValue(LOCATION_ATTRIBUTE_VALUE);
		
		LocationAttributeType attributeType = new LocationAttributeType();
		attributeType.setUuid(LOCATION_ATTRIBUTE_TYPE_UUID);
		attributeType.setName(LOCATION_ATTRIBUTE_TYPE_NAME);
		locationAttribute.setAttributeType(attributeType);
		assertNotNull(locationAttribute.getAttributeType());
		
		contactPoint.setId(TEST_LOCATION_ATTRIBUTE_UUID);
		contactPoint.setValue(TEST_LOCATION_ATTRIBUTE_VALUE);
		
		LocationAttribute attribute = translator.toOpenmrsType(locationAttribute, contactPoint);
		assertNotNull(attribute);
		assertNotNull(attribute.getValue());
		assertNotNull(attribute.getAttributeType().getName());
		assertEquals(attribute.getAttributeType().getUuid(), LOCATION_ATTRIBUTE_TYPE_UUID);
		
	}
	
	@Test
	public void shouldTranslateWithCorrectLocationAttributeTypeForStreetName() {
		contactPoint.setId(CONTACT_POINT_ID);
		contactPoint.setValue(CONTACT_POINT_VALUE);
		
		LocationAttributeType attributeType = new LocationAttributeType();
		attributeType.setUuid(LOCATION_ATTRIBUTE_TYPE_UUID);
		attributeType.setName(LOCATION_ATTRIBUTE_TYPE_NAME);
		
		when(propertyService.getGlobalProperty(FhirConstants.LOCATION_ATTRIBUTE_TYPE_PROPERTY)).thenReturn(
		    LOCATION_ATTRIBUTE_TYPE_UUID);
		when(locationService.getLocationAttributeTypeByUuid(LOCATION_ATTRIBUTE_TYPE_UUID)).thenReturn(attributeType);
		
		LocationAttribute locationAttribute = translator.toOpenmrsType(contactPoint);
		assertNotNull(locationAttribute);
		assertEquals(locationAttribute.getAttributeType().getUuid(), LOCATION_ATTRIBUTE_TYPE_UUID);
	}
}
