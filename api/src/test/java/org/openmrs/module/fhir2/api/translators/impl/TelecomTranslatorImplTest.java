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

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.hl7.fhir.r4.model.ContactPoint;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openmrs.LocationAttribute;
import org.openmrs.LocationAttributeType;
import org.openmrs.PersonAttribute;
import org.openmrs.PersonAttributeType;
import org.openmrs.ProviderAttribute;
import org.openmrs.ProviderAttributeType;
import org.openmrs.api.LocationService;
import org.openmrs.api.PersonService;
import org.openmrs.api.ProviderService;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.api.FhirContactPointMapService;
import org.openmrs.module.fhir2.api.FhirGlobalPropertyService;
import org.openmrs.module.fhir2.model.FhirContactPointMap;

@RunWith(MockitoJUnitRunner.class)
public class TelecomTranslatorImplTest {
	
	private static final String CONTACT_POINT_ID = "e2323we23-323j34-23k23-23m23";
	
	private static final String CONTACT_POINT_VALUE = "254237283723723";
	
	private static final String PERSON_ATTRIBUTE_UUID = "12o3et5kl3-2e323-23g23-232h3y343s";
	
	private static final String PERSON_ATTRIBUTE_VALUE = "254723723456";
	
	private static final String PERSON_ATTRIBUTE_TYPE_UUID = "14d4f066-15f5-102d-96e4-000c29c2a5d7";
	
	private static final String FHIR_CONTACT_POINT_MAP_TYPE_UUID = "fe637eac-7698-4763-8de0-923437fd3132";
	
	private static final String ATTRIBUTE_TYPE_NAME = "Contact";
	
	private static final String NEW_PERSON_ATTRIBUTE_UUID = "28932b-349023-29323-23sj23";
	
	private static final String NEW_PERSON_ATTRIBUTE_VALUE = "0722 934 389";
	
	private static final String LOCATION_ATTRIBUTE_UUID = "aa5986a9-1f40-4428-8214-aeae58fabac4";
	
	private static final String LOCATION_ATTRIBUTE_VALUE = "Neiya street";
	
	private static final String LOCATION_ATTRIBUTE_TYPE_UUID = "abcde432-1691-11df-97a5-7038c432abcd";
	
	private static final String LOCATION_ATTRIBUTE_TYPE_NAME = "street name";
	
	private static final String TEST_LOCATION_ATTRIBUTE_UUID = "0aaa1a7b-38f9-4a85-aa86-e02fc8fdab78";
	
	private static final String TEST_LOCATION_ATTRIBUTE_VALUE = "Ngeria street";
	
	private static final String PROVIDER_ATTRIBUTE_UUID = "WW90WW-XX44XX-23K23-88DD88DD";
	
	private static final String PROVIDER_ATTRIBUTE_VALUE = "+2547234950456";
	
	private static final String PROVIDER_ATTRIBUTE_TYPE_UUID = "14d4f066-15f5-102d-96e4-000c29c2a5d7";
	
	private static final String PROVIDER_ATTRIBUTE_TYPE_NAME = "PHONE";
	
	private static final String NEW_PROVIDER_ATTRIBUTE_UUID = "28932b-XX4XXX-29323-0000";
	
	private static final String NEW_PROVIDER_ATTRIBUTE_VALUE = "+254712 XXX XXX";
	
	@Mock
	private FhirContactPointMapService fhirContactPointMapService;
	
	@Mock
	private PersonService personService;
	
	@Mock
	private LocationService locationService;
	
	@Mock
	private ProviderService providerService;
	
	@Mock
	private FhirGlobalPropertyService globalPropertyService;
	
	private TelecomTranslatorImpl telecomTranslator;
	
	private LocationAttribute locationAttribute;
	
	private PersonAttribute personAttribute;
	
	private PersonAttributeType personAttributeType;
	
	private ProviderAttributeType providerAttributeType;
	
	private LocationAttributeType locationAttributeType;
	
	private ProviderAttribute providerAttribute;
	
	private ContactPoint contactPoint;
	
	private FhirContactPointMap personContactPointMap;
	
	@Before
	public void setUp() {
		telecomTranslator = new TelecomTranslatorImpl();
		telecomTranslator.setPersonService(personService);
		telecomTranslator.setLocationService(locationService);
		telecomTranslator.setProviderService(providerService);
		telecomTranslator.setGlobalPropertyService(globalPropertyService);
		telecomTranslator.setFhirContactPointMapService(fhirContactPointMapService);
		
		locationAttribute = new LocationAttribute();
		personAttribute = new PersonAttribute();
		providerAttribute = new ProviderAttribute();
		personAttributeType = new PersonAttributeType();
		providerAttributeType = new ProviderAttributeType();
		locationAttributeType = new LocationAttributeType();
		contactPoint = new ContactPoint();
		
		personContactPointMap = new FhirContactPointMap();
	}
	
	@Test
	public void toFhirResource_shouldTranslatePersonAttributeUseToFhirContactPointUse() {
		when(fhirContactPointMapService.getFhirContactPointMapForPersonAttributeType(any())).thenReturn(
		    Optional.of(new FhirContactPointMap(null, "person", null, null, ContactPoint.ContactPointUse.WORK, null)));
		
		contactPoint = telecomTranslator.toFhirResource(personAttribute);
		assertThat(contactPoint, notNullValue());
		assertThat(contactPoint.getUse(), equalTo(ContactPoint.ContactPointUse.WORK));
	}
	
	@Test
	public void toFhirResource_shouldTranslatePersonAttributeSystemToFhirContactPointSystem() {
		when(fhirContactPointMapService.getFhirContactPointMapForPersonAttributeType(any())).thenReturn(
		    Optional.of(new FhirContactPointMap(null, "person", null, ContactPoint.ContactPointSystem.EMAIL, null, null)));
		
		contactPoint = telecomTranslator.toFhirResource(personAttribute);
		assertThat(contactPoint, notNullValue());
		assertThat(contactPoint.getSystem(), equalTo(ContactPoint.ContactPointSystem.EMAIL));
	}
	
	@Test
	public void toFhirResource_shouldTranslatePersonAttributeRankToFhirContactPointRank() {
		when(fhirContactPointMapService.getFhirContactPointMapForPersonAttributeType(any()))
		        .thenReturn(Optional.of(new FhirContactPointMap(null, "person", null, null, null, 1)));
		
		contactPoint = telecomTranslator.toFhirResource(personAttribute);
		assertThat(contactPoint, notNullValue());
		assertThat(contactPoint.getRank(), equalTo(1));
	}
	
	@Test
	public void toFhirResource_shouldTranslatePersonAttributeUuidToFhirContactPointId() {
		personAttribute.setUuid(PERSON_ATTRIBUTE_UUID);
		ContactPoint contactPoint = telecomTranslator.toFhirResource(personAttribute);
		assertThat(contactPoint, notNullValue());
		assertThat(contactPoint.getId(), equalTo(PERSON_ATTRIBUTE_UUID));
	}
	
	@Test
	public void toFhirResource_shouldTranslatePersonAttributeValueToFhirContactPointValue() {
		personAttribute.setValue(PERSON_ATTRIBUTE_VALUE);
		ContactPoint contactPoint = telecomTranslator.toFhirResource(personAttribute);
		assertThat(contactPoint, notNullValue());
		assertThat(contactPoint.getValue(), equalTo(PERSON_ATTRIBUTE_VALUE));
	}
	
	@Test
	public void toFhirResource_shouldTranslateProviderAttributeUseToFhirContactPointUse() {
		providerAttribute.setUuid(PROVIDER_ATTRIBUTE_UUID);
		providerAttribute.setValue(PROVIDER_ATTRIBUTE_VALUE);
		
		when(fhirContactPointMapService.getFhirContactPointMapForAttributeType(any())).thenReturn(
		    Optional.of(new FhirContactPointMap(null, "provider", null, null, ContactPoint.ContactPointUse.WORK, null)));
		
		contactPoint = telecomTranslator.toFhirResource(providerAttribute);
		assertThat(contactPoint, notNullValue());
		assertThat(contactPoint.getValue(), equalTo(PROVIDER_ATTRIBUTE_VALUE));
		assertThat(contactPoint.getId(), equalTo(PROVIDER_ATTRIBUTE_UUID));
		assertThat(contactPoint.getUse(), equalTo(ContactPoint.ContactPointUse.WORK));
	}
	
	@Test
	public void toFhirResource_shouldTranslateProviderAttributeSystemToFhirContactPointSystem() {
		providerAttribute.setUuid(PROVIDER_ATTRIBUTE_UUID);
		providerAttribute.setValue(PROVIDER_ATTRIBUTE_VALUE);
		
		when(fhirContactPointMapService.getFhirContactPointMapForAttributeType(any())).thenReturn(
		    Optional.of(new FhirContactPointMap(null, "provider", null, ContactPoint.ContactPointSystem.EMAIL, null, null)));
		
		contactPoint = telecomTranslator.toFhirResource(providerAttribute);
		assertThat(contactPoint, notNullValue());
		assertThat(contactPoint.getValue(), equalTo(PROVIDER_ATTRIBUTE_VALUE));
		assertThat(contactPoint.getId(), equalTo(PROVIDER_ATTRIBUTE_UUID));
		assertThat(contactPoint.getSystem(), equalTo(ContactPoint.ContactPointSystem.EMAIL));
	}
	
	@Test
	public void toFhirResource_shouldTranslateProviderAttributeRankToFhirContactPointRank() {
		providerAttribute.setUuid(PROVIDER_ATTRIBUTE_UUID);
		providerAttribute.setValue(PROVIDER_ATTRIBUTE_VALUE);
		
		when(fhirContactPointMapService.getFhirContactPointMapForAttributeType(any()))
		        .thenReturn(Optional.of(new FhirContactPointMap(null, "provider", null, null, null, 1)));
		
		contactPoint = telecomTranslator.toFhirResource(providerAttribute);
		assertThat(contactPoint, notNullValue());
		assertThat(contactPoint.getValue(), equalTo(PROVIDER_ATTRIBUTE_VALUE));
		assertThat(contactPoint.getId(), equalTo(PROVIDER_ATTRIBUTE_UUID));
		assertThat(contactPoint.getRank(), equalTo(1));
	}
	
	@Test
	public void toFhirResource_shouldTranslateLocationAttributeUseToFhirContactPointUse() {
		locationAttribute.setUuid(LOCATION_ATTRIBUTE_UUID);
		locationAttribute.setValue(LOCATION_ATTRIBUTE_VALUE);
		
		when(fhirContactPointMapService.getFhirContactPointMapForAttributeType(any())).thenReturn(
		    Optional.of(new FhirContactPointMap(null, "location", null, null, ContactPoint.ContactPointUse.WORK, null)));
		
		contactPoint = telecomTranslator.toFhirResource(locationAttribute);
		assertThat(contactPoint, notNullValue());
		assertThat(contactPoint.getValue(), equalTo(LOCATION_ATTRIBUTE_VALUE));
		assertThat(contactPoint.getId(), equalTo(LOCATION_ATTRIBUTE_UUID));
		assertThat(contactPoint.getUse(), equalTo(ContactPoint.ContactPointUse.WORK));
	}
	
	@Test
	public void toFhirResource_shouldTranslateLocationAttributeSystemToFhirContactPointSystem() {
		locationAttribute.setUuid(LOCATION_ATTRIBUTE_UUID);
		locationAttribute.setValue(LOCATION_ATTRIBUTE_VALUE);
		
		when(fhirContactPointMapService.getFhirContactPointMapForAttributeType(any())).thenReturn(
		    Optional.of(new FhirContactPointMap(null, "location", null, ContactPoint.ContactPointSystem.EMAIL, null, null)));
		
		contactPoint = telecomTranslator.toFhirResource(locationAttribute);
		assertThat(contactPoint, notNullValue());
		assertThat(contactPoint.getValue(), equalTo(LOCATION_ATTRIBUTE_VALUE));
		assertThat(contactPoint.getId(), equalTo(LOCATION_ATTRIBUTE_UUID));
		assertThat(contactPoint.getSystem(), equalTo(ContactPoint.ContactPointSystem.EMAIL));
	}
	
	@Test
	public void toFhirResource_shouldTranslateLocationAttributeRankToFhirContactPointRank() {
		locationAttribute.setUuid(LOCATION_ATTRIBUTE_UUID);
		locationAttribute.setValue(LOCATION_ATTRIBUTE_VALUE);
		
		when(fhirContactPointMapService.getFhirContactPointMapForAttributeType(any()))
		        .thenReturn(Optional.of(new FhirContactPointMap(null, "location", null, null, null, 1)));
		
		contactPoint = telecomTranslator.toFhirResource(locationAttribute);
		assertThat(contactPoint, notNullValue());
		assertThat(contactPoint.getValue(), equalTo(LOCATION_ATTRIBUTE_VALUE));
		assertThat(contactPoint.getId(), equalTo(LOCATION_ATTRIBUTE_UUID));
		assertThat(contactPoint.getRank(), equalTo(1));
	}
	
	@Test
	public void toOpenmrsType_shouldTranslateFhirContactPointIdToPersonAttributeUuid() {
		ContactPoint contactPoint = new ContactPoint();
		contactPoint.setId(CONTACT_POINT_ID);
		PersonAttribute result = (PersonAttribute) telecomTranslator.toOpenmrsType(new PersonAttribute(), contactPoint);
		assertThat(result, notNullValue());
		assertThat(result.getUuid(), equalTo(CONTACT_POINT_ID));
	}
	
	@Test
	public void toOpenmrsType_shouldTranslateFhirContactPointValueToPersonAttributeValue() {
		ContactPoint contactPoint = new ContactPoint();
		contactPoint.setValue(CONTACT_POINT_VALUE);
		PersonAttribute result = (PersonAttribute) telecomTranslator.toOpenmrsType(new PersonAttribute(), contactPoint);
		assertThat(result, notNullValue());
		assertThat(result.getValue(), equalTo(CONTACT_POINT_VALUE));
	}
	
	@Test
	public void toOpenmrsType_shouldUpdatePersonAttributeUuid() {
		personAttribute.setUuid(PERSON_ATTRIBUTE_UUID);
		
		ContactPoint contactPoint = new ContactPoint();
		contactPoint.setId(NEW_PERSON_ATTRIBUTE_UUID);
		
		PersonAttribute result = (PersonAttribute) telecomTranslator.toOpenmrsType(personAttribute, contactPoint);
		assertThat(result, notNullValue());
		assertThat(result.getUuid(), notNullValue());
		assertThat(result.getUuid(), equalTo(NEW_PERSON_ATTRIBUTE_UUID));
	}
	
	@Test
	public void toOpenmrsType_shouldUpdatePersonAttributeValue() {
		personAttribute.setValue(PERSON_ATTRIBUTE_VALUE);
		
		ContactPoint contactPoint = new ContactPoint();
		contactPoint.setValue(NEW_PERSON_ATTRIBUTE_VALUE);
		
		PersonAttribute result = (PersonAttribute) telecomTranslator.toOpenmrsType(personAttribute, contactPoint);
		assertThat(result, notNullValue());
		assertThat(result.getValue(), notNullValue());
		assertThat(result.getValue(), equalTo(NEW_PERSON_ATTRIBUTE_VALUE));
	}
	
	@Test
	public void toOpenmrsType_shouldUpdatePersonAttributeWithTheCorrectPersonAttributeTypeUuid() {
		personAttribute.setUuid(PERSON_ATTRIBUTE_UUID);
		personAttribute.setValue(PERSON_ATTRIBUTE_VALUE);
		personAttributeType.setName(ATTRIBUTE_TYPE_NAME);
		personAttributeType.setUuid(PERSON_ATTRIBUTE_TYPE_UUID);
		personAttribute.setAttributeType(personAttributeType);
		
		ContactPoint contactPoint = new ContactPoint();
		contactPoint.setId(NEW_PERSON_ATTRIBUTE_UUID);
		contactPoint.setValue(NEW_PERSON_ATTRIBUTE_VALUE);
		
		when(globalPropertyService.getGlobalProperty(FhirConstants.PERSON_CONTACT_POINT_ATTRIBUTE_TYPE))
		        .thenReturn(PERSON_ATTRIBUTE_TYPE_UUID);
		when(personService.getPersonAttributeTypeByUuid(PERSON_ATTRIBUTE_TYPE_UUID)).thenReturn(personAttributeType);
		
		PersonAttribute result = (PersonAttribute) telecomTranslator.toOpenmrsType(personAttribute, contactPoint);
		assertThat(result, notNullValue());
		assertThat(result.getValue(), notNullValue());
		assertThat(result.getAttributeType().getUuid(), equalTo(PERSON_ATTRIBUTE_TYPE_UUID));
	}
	
	@Test
	public void toOpenmrsType_shouldTranslateWithCorrectPersonAttributeTypeForContactDetails() {
		ContactPoint contactPoint = new ContactPoint();
		contactPoint.setId(CONTACT_POINT_ID);
		contactPoint.setValue(CONTACT_POINT_VALUE);
		personAttributeType.setName(ATTRIBUTE_TYPE_NAME);
		personAttributeType.setUuid(PERSON_ATTRIBUTE_TYPE_UUID);
		
		when(globalPropertyService.getGlobalProperty(FhirConstants.PERSON_CONTACT_POINT_ATTRIBUTE_TYPE))
		        .thenReturn(PERSON_ATTRIBUTE_TYPE_UUID);
		when(personService.getPersonAttributeTypeByUuid(PERSON_ATTRIBUTE_TYPE_UUID)).thenReturn(personAttributeType);
		
		PersonAttribute result = (PersonAttribute) telecomTranslator.toOpenmrsType(new PersonAttribute(), contactPoint);
		assertThat(result, notNullValue());
		assertThat(result.getAttributeType().getUuid(), equalTo(PERSON_ATTRIBUTE_TYPE_UUID));
	}
	
	@Test
	public void toFhirResource_shouldTranslateUuidToId() {
		locationAttribute.setUuid(LOCATION_ATTRIBUTE_UUID);
		locationAttribute.setValue(LOCATION_ATTRIBUTE_VALUE);
		contactPoint = telecomTranslator.toFhirResource(locationAttribute);
		assertThat(contactPoint.getId(), notNullValue());
		assertThat(contactPoint.getId(), equalTo(LOCATION_ATTRIBUTE_UUID));
	}
	
	@Test
	public void toFhirResource_shouldTranslateValueToContactPointValue() {
		locationAttribute.setUuid(LOCATION_ATTRIBUTE_UUID);
		locationAttribute.setValue(LOCATION_ATTRIBUTE_VALUE);
		contactPoint = telecomTranslator.toFhirResource(locationAttribute);
		assertThat(contactPoint.getValue(), notNullValue());
		assertThat(contactPoint.getValue(), equalTo(LOCATION_ATTRIBUTE_VALUE));
	}
	
	@Test
	public void toOpenmrsType_shouldTranslateIdToUuid() {
		contactPoint.setId(CONTACT_POINT_ID);
		contactPoint.setValue(CONTACT_POINT_VALUE);
		locationAttribute = (LocationAttribute) telecomTranslator.toOpenmrsType(new LocationAttribute(), contactPoint);
		assertThat(locationAttribute.getUuid(), notNullValue());
		assertThat(locationAttribute.getUuid(), equalTo(CONTACT_POINT_ID));
	}
	
	@Test
	public void toOpenmrsType_shouldTranslateContactPointValueToLocationAttributeValue() {
		contactPoint.setId(CONTACT_POINT_ID);
		contactPoint.setValue(CONTACT_POINT_VALUE);
		locationAttribute = (LocationAttribute) telecomTranslator.toOpenmrsType(new LocationAttribute(), contactPoint);
		assertThat(locationAttribute.getValue(), notNullValue());
		assertThat(locationAttribute.getValue(), equalTo(CONTACT_POINT_VALUE));
	}
	
	@Test
	public void toOpenmrsType_shouldUpdateLocationAttributeUuid() {
		locationAttribute.setUuid(LOCATION_ATTRIBUTE_UUID);
		
		contactPoint.setId(TEST_LOCATION_ATTRIBUTE_UUID);
		
		LocationAttribute attribute = (LocationAttribute) telecomTranslator.toOpenmrsType(locationAttribute, contactPoint);
		assertThat(attribute.getUuid(), notNullValue());
		assertThat(attribute.getUuid(), equalTo(TEST_LOCATION_ATTRIBUTE_UUID));
	}
	
	@Test
	public void toOpenmrsType_shouldReturnNullIfAttributeIsNull() {
		Object result = telecomTranslator.toOpenmrsType(null, contactPoint);
		assertThat(result, nullValue());
	}
	
	@Test
	public void toOpenmrsType_shouldUpdateLocationAttributeValue() {
		locationAttribute.setValue(LOCATION_ATTRIBUTE_VALUE);
		
		contactPoint.setValue(TEST_LOCATION_ATTRIBUTE_VALUE);
		
		LocationAttribute attribute = (LocationAttribute) telecomTranslator.toOpenmrsType(locationAttribute, contactPoint);
		assertThat(attribute.getValue(), notNullValue());
		assertThat(attribute.getValue(), equalTo(TEST_LOCATION_ATTRIBUTE_VALUE));
	}
	
	@Test
	public void toOpenmrsType_shouldNotUpdateLocationAttributeIfContactPointIsNull() {
		locationAttribute.setUuid(LOCATION_ATTRIBUTE_UUID);
		locationAttribute.setValue(LOCATION_ATTRIBUTE_VALUE);
		
		LocationAttribute attribute = (LocationAttribute) telecomTranslator.toOpenmrsType(locationAttribute, null);
		assertThat(attribute.getUuid(), notNullValue());
		assertThat(attribute.getUuid(), equalTo(LOCATION_ATTRIBUTE_UUID));
		assertThat(attribute.getValue(), equalTo(LOCATION_ATTRIBUTE_VALUE));
		assertThat(attribute, equalTo(locationAttribute));
	}
	
	@Test
	public void toOpenmrsType_shouldAddCorrectLocationAttributeType() {
		locationAttribute.setUuid(LOCATION_ATTRIBUTE_UUID);
		locationAttribute.setValue(LOCATION_ATTRIBUTE_VALUE);
		LocationAttributeType attributeType = new LocationAttributeType();
		attributeType.setUuid(LOCATION_ATTRIBUTE_TYPE_UUID);
		attributeType.setName(LOCATION_ATTRIBUTE_TYPE_NAME);
		locationAttribute.setAttributeType(attributeType);
		
		contactPoint.setId(TEST_LOCATION_ATTRIBUTE_UUID);
		contactPoint.setValue(TEST_LOCATION_ATTRIBUTE_VALUE);
		
		when(globalPropertyService.getGlobalProperty(FhirConstants.LOCATION_CONTACT_POINT_ATTRIBUTE_TYPE))
		        .thenReturn(LOCATION_ATTRIBUTE_TYPE_UUID);
		when(locationService.getLocationAttributeTypeByUuid(LOCATION_ATTRIBUTE_TYPE_UUID)).thenReturn(attributeType);
		
		LocationAttribute attribute = (LocationAttribute) telecomTranslator.toOpenmrsType(locationAttribute, contactPoint);
		assertThat(attribute, notNullValue());
		assertThat(attribute.getValue(), notNullValue());
		assertThat(attribute.getAttributeType().getUuid(), equalTo(LOCATION_ATTRIBUTE_TYPE_UUID));
		assertThat(attribute.getAttributeType().getName(), equalTo(LOCATION_ATTRIBUTE_TYPE_NAME));
		
	}
	
	@Test
	public void toOpenmrsType_shouldTranslateWithCorrectLocationAttributeTypeForStreetName() {
		contactPoint.setId(CONTACT_POINT_ID);
		contactPoint.setValue(CONTACT_POINT_VALUE);
		
		LocationAttributeType attributeType = new LocationAttributeType();
		attributeType.setUuid(LOCATION_ATTRIBUTE_TYPE_UUID);
		attributeType.setName(LOCATION_ATTRIBUTE_TYPE_NAME);
		
		when(globalPropertyService.getGlobalProperty(FhirConstants.LOCATION_CONTACT_POINT_ATTRIBUTE_TYPE))
		        .thenReturn(LOCATION_ATTRIBUTE_TYPE_UUID);
		when(locationService.getLocationAttributeTypeByUuid(LOCATION_ATTRIBUTE_TYPE_UUID)).thenReturn(attributeType);
		
		LocationAttribute locationAttribute = (LocationAttribute) telecomTranslator.toOpenmrsType(new LocationAttribute(),
		    contactPoint);
		assertThat(locationAttribute, notNullValue());
		assertThat(locationAttribute.getAttributeType().getUuid(), equalTo(LOCATION_ATTRIBUTE_TYPE_UUID));
	}
	
	@Test
	public void toFhirResource_shouldTranslateProviderAttributeUuidToFhirContactPointId() {
		ProviderAttribute providerAttribute = new ProviderAttribute();
		providerAttribute.setUuid(PROVIDER_ATTRIBUTE_UUID);
		providerAttribute.setValue(PROVIDER_ATTRIBUTE_VALUE);
		ContactPoint contactPoint = telecomTranslator.toFhirResource(providerAttribute);
		assertThat(contactPoint, notNullValue());
		assertThat(contactPoint.getId(), equalTo(PROVIDER_ATTRIBUTE_UUID));
	}
	
	@Test
	public void toFhirResource_shouldTranslateProviderAttributeValueToFhirContactPointValue() {
		ProviderAttribute providerAttribute = new ProviderAttribute();
		providerAttribute.setValue(PROVIDER_ATTRIBUTE_VALUE);
		ContactPoint contactPoint = telecomTranslator.toFhirResource(providerAttribute);
		assertThat(contactPoint, notNullValue());
		assertThat(contactPoint.getValue(), equalTo(PROVIDER_ATTRIBUTE_VALUE));
	}
	
	@Test
	public void toOpenmrsType_shouldTranslateFhirContactPointIdToProviderAttributeUuid() {
		ContactPoint contactPoint = new ContactPoint();
		contactPoint.setId(CONTACT_POINT_ID);
		ProviderAttribute result = (ProviderAttribute) telecomTranslator.toOpenmrsType(new ProviderAttribute(),
		    contactPoint);
		assertThat(result, notNullValue());
		assertThat(result.getUuid(), equalTo(CONTACT_POINT_ID));
	}
	
	@Test
	public void toOpenmrsType_shouldTranslateFhirContactPointValueToProviderAttributeValue() {
		ContactPoint contactPoint = new ContactPoint();
		contactPoint.setValue(CONTACT_POINT_VALUE);
		ProviderAttribute result = (ProviderAttribute) telecomTranslator.toOpenmrsType(new ProviderAttribute(),
		    contactPoint);
		assertThat(result, notNullValue());
		assertThat(result.getValue(), equalTo(CONTACT_POINT_VALUE));
	}
	
	@Test
	public void toOpenmrsType_shouldUpdateProviderAttributeUuid() {
		ProviderAttribute providerAttribute = new ProviderAttribute();
		providerAttribute.setUuid(PROVIDER_ATTRIBUTE_UUID);
		
		ContactPoint contactPoint = new ContactPoint();
		contactPoint.setId(NEW_PROVIDER_ATTRIBUTE_UUID);
		
		ProviderAttribute result = (ProviderAttribute) telecomTranslator.toOpenmrsType(providerAttribute, contactPoint);
		assertThat(result, notNullValue());
		assertThat(result.getUuid(), notNullValue());
		assertThat(result.getUuid(), equalTo(NEW_PROVIDER_ATTRIBUTE_UUID));
	}
	
	@Test
	public void toOpenmrsType_shouldUpdateProviderAttributeValue() {
		ProviderAttribute providerAttribute = new ProviderAttribute();
		providerAttribute.setValue(PROVIDER_ATTRIBUTE_VALUE);
		
		ContactPoint contactPoint = new ContactPoint();
		contactPoint.setValue(NEW_PROVIDER_ATTRIBUTE_VALUE);
		
		ProviderAttribute result = (ProviderAttribute) telecomTranslator.toOpenmrsType(providerAttribute, contactPoint);
		assertThat(result, notNullValue());
		assertThat(result.getValue(), notNullValue());
		assertThat(result.getValue(), equalTo(NEW_PROVIDER_ATTRIBUTE_VALUE));
	}
	
	@Test
	public void toOpenmrsType_shouldUpdateProviderAttributeWithTheCorrectPersonAttributeTypeUuid() {
		providerAttribute.setUuid(PERSON_ATTRIBUTE_UUID);
		providerAttribute.setValue(PERSON_ATTRIBUTE_VALUE);
		ProviderAttributeType attributeType = new ProviderAttributeType();
		attributeType.setName(ATTRIBUTE_TYPE_NAME);
		attributeType.setUuid(PERSON_ATTRIBUTE_TYPE_UUID);
		providerAttribute.setAttributeType(attributeType);
		
		ContactPoint contactPoint = new ContactPoint();
		contactPoint.setId(NEW_PERSON_ATTRIBUTE_UUID);
		contactPoint.setValue(NEW_PERSON_ATTRIBUTE_VALUE);
		
		when(globalPropertyService.getGlobalProperty(FhirConstants.PROVIDER_CONTACT_POINT_ATTRIBUTE_TYPE))
		        .thenReturn(PROVIDER_ATTRIBUTE_TYPE_UUID);
		when(providerService.getProviderAttributeTypeByUuid(PROVIDER_ATTRIBUTE_TYPE_UUID)).thenReturn(attributeType);
		
		ProviderAttribute result = (ProviderAttribute) telecomTranslator.toOpenmrsType(providerAttribute, contactPoint);
		assertThat(result, notNullValue());
		assertThat(result.getValue(), notNullValue());
		assertThat(result.getAttributeType().getUuid(), equalTo(PROVIDER_ATTRIBUTE_TYPE_UUID));
	}
	
	@Test
	public void toOpenmrsType_shouldTranslateWithCorrectProviderAttributeTypeForContactDetails() {
		ContactPoint contactPoint = new ContactPoint();
		contactPoint.setId(CONTACT_POINT_ID);
		contactPoint.setValue(CONTACT_POINT_VALUE);
		ProviderAttributeType attributeType = new ProviderAttributeType();
		attributeType.setName(PROVIDER_ATTRIBUTE_TYPE_NAME);
		attributeType.setUuid(PROVIDER_ATTRIBUTE_TYPE_UUID);
		
		when(globalPropertyService.getGlobalProperty(FhirConstants.PROVIDER_CONTACT_POINT_ATTRIBUTE_TYPE))
		        .thenReturn(PROVIDER_ATTRIBUTE_TYPE_UUID);
		when(providerService.getProviderAttributeTypeByUuid(PROVIDER_ATTRIBUTE_TYPE_UUID)).thenReturn(attributeType);
		
		ProviderAttribute result = (ProviderAttribute) telecomTranslator.toOpenmrsType(new ProviderAttribute(),
		    contactPoint);
		assertThat(result, notNullValue());
		assertThat(result.getAttributeType().getUuid(), equalTo(PROVIDER_ATTRIBUTE_TYPE_UUID));
	}
}
