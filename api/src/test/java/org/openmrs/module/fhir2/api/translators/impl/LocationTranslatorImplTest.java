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
import org.openmrs.module.fhir2.api.translators.LocationAddressTranslator;
import org.openmrs.module.fhir2.api.translators.LocationTelecomTranslator;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class LocationTranslatorImplTest {
	
	private static final String LOCATION_UUID = "c0938432-1691-11df-97a5-7038c432aaba";
	
	private static final String LOCATION_NAME = "Test location 1";
	
	private static final String LOCATION_DESCRIPTION = "Test description";
	
	private static final String LOCATION_LATITUDE = "25.5";
	
	private static final String LOCATION_LONGITUDE = "25.5";
	
	private static final String CONTACT_POINT_ID = "c0938432-1691-11df-97a5-7038c432aabf";
	
	private static final String CONTACT_POINT_VALUE = "Pipeline";
	
	private static final String LOCATION_ATTRIBUTE_UUID = "c0938432-1691-11df-97a5-7038c432abcd";
	
	private static final String LOCATION_ATTRIBUTE_VALUE = "Neiya street";
	
	private static final String LOCATION_ATTRIBUTE_TYPE_UUID = "abcde432-1691-11df-97a5-7038c432abcd";
	
	private static final String LOCATION_ATTRIBUTE_TYPE_NAME = "street name";
	
	@Mock
	private LocationAddressTranslator locationAddressTranslator;
	
	@Mock
	private LocationTelecomTranslator telecomTranslator;
	
	@Mock
	private LocationService locationService;
	
	@Mock
	private FhirGlobalPropertyService propertyService;
	
	private LocationTranslatorImpl locationTranslator;
	
	private Location omrsLocation;
	
	@Before
	public void setup() {
		omrsLocation = new Location();
		locationTranslator = new LocationTranslatorImpl();
		locationTranslator.setLocationAddressTranslator(locationAddressTranslator);
		locationTranslator.setLocationTelecomTranslator(telecomTranslator);
		locationTranslator.setLocationService(locationService);
		locationTranslator.setPropertyService(propertyService);
		
	}
	
	@Test
	public void shouldTranslateOpenmrsLocationToFhirLocation() {
		org.hl7.fhir.r4.model.Location fhirLocation = locationTranslator.toFhirResource(omrsLocation);
		assertNotNull(omrsLocation);
		assertNotNull(fhirLocation);
		assertEquals(omrsLocation.getName(), fhirLocation.getName());
	}
	
	@Test
	public void shouldTranslateLocationUuidToFhiIdType() {
		omrsLocation.setUuid(LOCATION_UUID);
		org.hl7.fhir.r4.model.Location fhirLocation = locationTranslator.toFhirResource(omrsLocation);
		assertNotNull(fhirLocation);
		assertNotNull(fhirLocation.getId());
		assertEquals(fhirLocation.getId(), LOCATION_UUID);
	}
	
	@Test
	public void shouldTranslateLocationNameToFhirNameType() {
		omrsLocation.setName(LOCATION_NAME);
		org.hl7.fhir.r4.model.Location fhirLocation = locationTranslator.toFhirResource(omrsLocation);
		assertNotNull(fhirLocation);
		assertNotNull(fhirLocation.getName());
		assertEquals(fhirLocation.getName(), LOCATION_NAME);
	}
	
	@Test
	public void shouldTranslateLocationDescriptionToFhirDescriptionType() {
		omrsLocation.setDescription(LOCATION_DESCRIPTION);
		org.hl7.fhir.r4.model.Location fhirLocation = locationTranslator.toFhirResource(omrsLocation);
		assertNotNull(fhirLocation);
		assertNotNull(fhirLocation.getDescription());
		assertEquals(fhirLocation.getDescription(), LOCATION_DESCRIPTION);
	}
	
	@Test
	public void toFhirResource_shouldReturnEmptyLocationWhenCalledWitEmptyOmrsLocation() {
		org.hl7.fhir.r4.model.Location location = locationTranslator.toFhirResource(null);
		assertNull(location.getName());
		assertNull(location.getDescription());
		assertNull(location.getId());
		assertNull(location.getPosition().getLatitude());
		assertNull(location.getPosition().getLongitude());
		assertNull(location.getAddress().getCity());
	}
	
	@Test
	public void toOpenmrsType_shouldReturnEmptyLocationWhenCalledWithEmptyFhirLocation() {
		Location location = locationTranslator.toOpenmrsType(null);
		assertNull(location.getName());
		assertNull(location.getDescription());
		assertNull(location.getId());
		assertNull(location.getLatitude());
		assertNull(location.getLongitude());
		assertNull(location.getCountry());
	}
	
	@Test
	public void shouldTranslateFhirLocationToOmrsLocation() {
		org.hl7.fhir.r4.model.Location location = new org.hl7.fhir.r4.model.Location();
		org.openmrs.Location omrsLocation = locationTranslator.toOpenmrsType(location);
		assertNotNull(omrsLocation);
		assertEquals(omrsLocation.getName(), location.getName());
	}
	
	@Test
	public void shouldTranslateLocationIdToUuid() {
		org.hl7.fhir.r4.model.Location location = new org.hl7.fhir.r4.model.Location();
		location.setId(LOCATION_UUID);
		org.openmrs.Location omrsLocation = locationTranslator.toOpenmrsType(location);
		assertNotNull(omrsLocation);
		assertNotNull(omrsLocation.getUuid());
		assertEquals(omrsLocation.getUuid(), LOCATION_UUID);
	}
	
	@Test
	public void shouldTranslateLocationNameToOpenmrsNameType() {
		org.hl7.fhir.r4.model.Location location = new org.hl7.fhir.r4.model.Location();
		location.setName(LOCATION_NAME);
		org.openmrs.Location omrsLocation = locationTranslator.toOpenmrsType(location);
		assertNotNull(omrsLocation);
		assertNotNull(omrsLocation.getName());
		assertEquals(omrsLocation.getName(), LOCATION_NAME);
	}
	
	@Test
	public void shouldTranslateLocationDescriptionToOpenmrsDescriptionType() {
		org.hl7.fhir.r4.model.Location location = new org.hl7.fhir.r4.model.Location();
		location.setDescription(LOCATION_DESCRIPTION);
		org.openmrs.Location omrsLocation = locationTranslator.toOpenmrsType(location);
		assertNotNull(omrsLocation);
		assertNotNull(omrsLocation.getDescription());
		assertEquals(omrsLocation.getDescription(), LOCATION_DESCRIPTION);
	}
	
	@Test
	public void shouldCreateFhirPositionObjectFromLatitudeAndLongitude() {
		omrsLocation.setLatitude(LOCATION_LATITUDE);
		omrsLocation.setLongitude(LOCATION_LONGITUDE);
		org.hl7.fhir.r4.model.Location fhirLocation = locationTranslator.toFhirResource(omrsLocation);
		assertNotNull(fhirLocation);
		assertNotNull(fhirLocation.getPosition());
		assertNotNull(fhirLocation.getPosition().getLatitude());
		assertNotNull(fhirLocation.getPosition().getLongitude());
	}
	
	@Test
	public void shouldSetFhirLocationToActiveIfLocationIsNotRetired() {
		omrsLocation.setRetired(false);
		org.hl7.fhir.r4.model.Location fhirLocation = locationTranslator.toFhirResource(omrsLocation);
		assertNotNull(fhirLocation);
		assertEquals(fhirLocation.getStatus(), org.hl7.fhir.r4.model.Location.LocationStatus.ACTIVE);
	}
	
	@Test
	public void shouldSetFhirLocationToInActiveIfLocationIsRetired() {
		omrsLocation.setRetired(true);
		org.hl7.fhir.r4.model.Location fhirLocation = locationTranslator.toFhirResource(omrsLocation);
		assertNotNull(fhirLocation);
		assertEquals(fhirLocation.getStatus(), org.hl7.fhir.r4.model.Location.LocationStatus.INACTIVE);
	}
	
	@Test
	public void shouldTranslateLocationAttributeToFhirContactPoint() {
		ContactPoint contactPoint = new ContactPoint();
		contactPoint.setId(CONTACT_POINT_ID);
		contactPoint.setValue(CONTACT_POINT_VALUE);
		
		LocationAttribute locationAttribute = new LocationAttribute();
		locationAttribute.setUuid(LOCATION_ATTRIBUTE_UUID);
		locationAttribute.setValue(LOCATION_ATTRIBUTE_VALUE);
		
		LocationAttributeType attributeType = new LocationAttributeType();
		attributeType.setName(LOCATION_ATTRIBUTE_TYPE_NAME);
		attributeType.setUuid(LOCATION_ATTRIBUTE_TYPE_UUID);
		locationAttribute.setAttributeType(attributeType);
		
		when(telecomTranslator.toFhirResource(locationAttribute)).thenReturn(contactPoint);
		
		org.hl7.fhir.r4.model.Location location = locationTranslator.toFhirResource(new Location());
		assertThat(location, notNullValue());
		assertThat(location.getTelecom(), notNullValue());
		
	}
	
	@Test
	public void shouldTranslateFhirContactPointToLocationAttribute() {
		LocationAttribute locationAttribute = new LocationAttribute();
		locationAttribute.setUuid(LOCATION_ATTRIBUTE_UUID);
		locationAttribute.setValue(LOCATION_ATTRIBUTE_VALUE);
		
		LocationAttributeType attributeType = new LocationAttributeType();
		attributeType.setName(LOCATION_ATTRIBUTE_TYPE_NAME);
		attributeType.setUuid(LOCATION_ATTRIBUTE_TYPE_UUID);
		locationAttribute.setAttributeType(attributeType);
		
		org.hl7.fhir.r4.model.Location location = new org.hl7.fhir.r4.model.Location();
		ContactPoint contactPoint = location.getTelecomFirstRep();
		contactPoint.setId(LOCATION_ATTRIBUTE_UUID);
		contactPoint.setValue(LOCATION_ATTRIBUTE_VALUE);
		
		when(telecomTranslator.toOpenmrsType(contactPoint)).thenReturn(locationAttribute);
		
		Location omrsLocation = locationTranslator.toOpenmrsType(location);
		assertThat(omrsLocation, notNullValue());
		assertThat(omrsLocation.getActiveAttributes(), notNullValue());
		assertThat(omrsLocation.getAttributes().size(), greaterThanOrEqualTo(1));
		assertThat(omrsLocation.getActiveAttributes().stream().findAny().isPresent(), is(true));
		assertThat(omrsLocation.getActiveAttributes().stream().findAny().get().getAttributeType(), equalTo(attributeType));
	}
	
	@Test
	public void shouldTranslateWithCorrectLocationAttributeTypeForContactDetails() {
		ContactPoint contactPoint = new ContactPoint();
		contactPoint.setId(CONTACT_POINT_ID);
		contactPoint.setValue(CONTACT_POINT_VALUE);
		
		LocationAttributeType attributeType = new LocationAttributeType();
		attributeType.setName(LOCATION_ATTRIBUTE_TYPE_NAME);
		attributeType.setUuid(LOCATION_ATTRIBUTE_TYPE_UUID);
		
		when(propertyService.getGlobalProperty(FhirConstants.LOCATION_ATTRIBUTE_TYPE_PROPERTY)).thenReturn(
		    LOCATION_ATTRIBUTE_TYPE_UUID);
		when(locationService.getLocationAttributeTypeByUuid(LOCATION_ATTRIBUTE_TYPE_UUID)).thenReturn(attributeType);
		
		LocationAttribute attribute = telecomTranslator.toOpenmrsType(contactPoint);
		assertThat(attributeType.getUuid(), equalTo(LOCATION_ATTRIBUTE_TYPE_UUID));
	}
	
	@Test
	public void getLocationContactDetails_shouldWorkAsExpected(){
		Set<LocationAttribute> locationAttributes = new LinkedHashSet<>();
		Location omrsLocation = new Location();
		omrsLocation.setUuid(LOCATION_UUID);
		LocationAttribute locationAttribute = new LocationAttribute();
		locationAttribute.setUuid(LOCATION_ATTRIBUTE_UUID);
		locationAttribute.setValue(LOCATION_ATTRIBUTE_VALUE);

		LocationAttributeType attributeType = new LocationAttributeType();
		attributeType.setUuid(LOCATION_ATTRIBUTE_TYPE_UUID);
		attributeType.setName(LOCATION_ATTRIBUTE_TYPE_NAME);
		locationAttribute.setAttributeType(attributeType);
		locationAttributes.add(locationAttribute);
		omrsLocation.setAttribute(locationAttribute);

		List<ContactPoint> contactPoints = locationTranslator.getLocationContactDetails(omrsLocation);

		assertThat(contactPoints, notNullValue());
		assertThat(omrsLocation.getActiveAttributes().size(), equalTo(1));

	}
}
