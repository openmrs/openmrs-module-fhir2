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
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.openmrs.module.fhir2.api.translators.impl.ReferenceHandlingTranslator.getReferenceId;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.exparity.hamcrest.date.DateMatchers;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.ContactPoint;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Reference;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openmrs.BaseOpenmrsData;
import org.openmrs.Location;
import org.openmrs.LocationAttribute;
import org.openmrs.LocationAttributeType;
import org.openmrs.LocationTag;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.api.FhirGlobalPropertyService;
import org.openmrs.module.fhir2.api.dao.FhirLocationDao;
import org.openmrs.module.fhir2.api.translators.LocationAddressTranslator;
import org.openmrs.module.fhir2.api.translators.LocationReferenceTranslator;
import org.openmrs.module.fhir2.api.translators.LocationTagTranslator;
import org.openmrs.module.fhir2.api.translators.LocationTypeTranslator;
import org.openmrs.module.fhir2.api.translators.TelecomTranslator;

@RunWith(MockitoJUnitRunner.class)
public class LocationTranslatorImplTest {
	
	private static final String LOCATION_UUID = "c0938432-1691-11df-97a5-7038c432";
	
	private static final String LOCATION_NAME = "Test location 1";
	
	private static final String LOCATION_DESCRIPTION = "Test description";
	
	private static final String LOCATION_LATITUDE = "25.5";
	
	private static final String LOCATION_LONGITUDE = "25.5";
	
	private static final String LOCATION_ATTRIBUTE_UUID = "b1aaf1e6-28e9-4736-a5a1-52960e282700";
	
	private static final String LOCATION_ATTRIBUTE_VALUE = "Neiya street";
	
	private static final String LOCATION_ATTRIBUTE_TYPE_UUID = "6a5766a2-e3c3-4edc-ae88-2aafcf5bcb34";
	
	private static final String LOCATION_ATTRIBUTE_TYPE_NAME = "street name";
	
	private static final String LOGIN_TAG_NAME = "Login location";
	
	private static final String LOGIN_TAG_DESCRIPTION = "Used to identify login locations";
	
	private static final String LAB_TAG_NAME = "Lab location";
	
	private static final String LAB_TAG_DESCRIPTION = "Used to identify lab locations";
	
	private static final String PARENT_LOCATION_UUID = "23e9cdce-7ffa-4261-862d-4b75bef6e7f3";
	
	private static final String PARENT_LOCATION_NAME = "Parent Location";
	
	@Mock
	private LocationAddressTranslator locationAddressTranslator;
	
	private final LocationReferenceTranslator locationReferenceTranslator = new LocationReferenceTranslatorImpl();
	
	@Mock
	private LocationTagTranslator locationTagTranslator;
	
	@Mock
	private TelecomTranslator<BaseOpenmrsData> telecomTranslator;
	
	@Mock
	private LocationTypeTranslator locationTypeTranslator;
	
	@Mock
	private FhirLocationDao fhirLocationDao;
	
	@Mock
	private FhirGlobalPropertyService propertyService;
	
	private LocationTranslatorImpl locationTranslator;
	
	private Location omrsLocation;
	
	@Before
	public void setup() {
		omrsLocation = new Location();
		
		((LocationReferenceTranslatorImpl) locationReferenceTranslator).setLocationDao(fhirLocationDao);
		
		locationTranslator = new LocationTranslatorImpl();
		locationTranslator.setLocationAddressTranslator(locationAddressTranslator);
		locationTranslator.setLocationReferenceTranslator(locationReferenceTranslator);
		locationTranslator.setTelecomTranslator(telecomTranslator);
		locationTranslator.setFhirLocationDao(fhirLocationDao);
		locationTranslator.setLocationTagTranslator(locationTagTranslator);
		locationTranslator.setLocationTypeTranslator(locationTypeTranslator);
		locationTranslator.setPropertyService(propertyService);
	}
	
	@Test
	public void shouldTranslateOpenmrsLocationToFhirLocation() {
		org.hl7.fhir.r4.model.Location fhirLocation = locationTranslator.toFhirResource(omrsLocation);
		
		assertThat(omrsLocation, notNullValue());
		assertThat(fhirLocation, notNullValue());
		assertThat(omrsLocation.getName(), equalTo(fhirLocation.getName()));
	}
	
	@Test
	public void shouldTranslateLocationUuidToFhirIdType() {
		omrsLocation.setUuid(LOCATION_UUID);
		
		org.hl7.fhir.r4.model.Location fhirLocation = locationTranslator.toFhirResource(omrsLocation);
		
		assertThat(fhirLocation, notNullValue());
		assertThat(fhirLocation.getId(), notNullValue());
		assertThat(fhirLocation.getId(), equalTo(LOCATION_UUID));
	}
	
	@Test
	public void shouldTranslateLocationNameToFhirNameType() {
		omrsLocation.setName(LOCATION_NAME);
		
		org.hl7.fhir.r4.model.Location fhirLocation = locationTranslator.toFhirResource(omrsLocation);
		
		assertThat(fhirLocation, notNullValue());
		assertThat(fhirLocation.getName(), notNullValue());
		assertThat(fhirLocation.getName(), equalTo(LOCATION_NAME));
	}
	
	@Test
	public void shouldTranslateLocationDescriptionToFhirDescriptionType() {
		omrsLocation.setDescription(LOCATION_DESCRIPTION);
		
		org.hl7.fhir.r4.model.Location fhirLocation = locationTranslator.toFhirResource(omrsLocation);
		
		assertThat(fhirLocation, notNullValue());
		assertThat(fhirLocation.getDescription(), notNullValue());
		assertThat(fhirLocation.getDescription(), equalTo(LOCATION_DESCRIPTION));
	}
	
	@Test
	public void shouldTranslateFhirLocationToOmrsLocation() {
		org.hl7.fhir.r4.model.Location location = new org.hl7.fhir.r4.model.Location();
		
		org.openmrs.Location omrsLocation = locationTranslator.toOpenmrsType(location);
		
		assertThat(omrsLocation, notNullValue());
		assertThat(omrsLocation.getName(), equalTo(location.getName()));
	}
	
	@Test
	public void shouldTranslateLocationIdToUuid() {
		org.hl7.fhir.r4.model.Location location = new org.hl7.fhir.r4.model.Location();
		location.setId(LOCATION_UUID);
		
		org.openmrs.Location omrsLocation = locationTranslator.toOpenmrsType(location);
		
		assertThat(omrsLocation, notNullValue());
		assertThat(omrsLocation.getUuid(), notNullValue());
		assertThat(omrsLocation.getUuid(), equalTo(LOCATION_UUID));
	}
	
	@Test
	public void shouldTranslateLocationNameToOpenmrsNameType() {
		org.hl7.fhir.r4.model.Location location = new org.hl7.fhir.r4.model.Location();
		location.setName(LOCATION_NAME);
		org.openmrs.Location omrsLocation = locationTranslator.toOpenmrsType(location);
		assertThat(omrsLocation, notNullValue());
		assertThat(omrsLocation.getName(), notNullValue());
		assertThat(omrsLocation.getName(), equalTo(LOCATION_NAME));
	}
	
	@Test
	public void shouldTranslateLocationDescriptionToOpenmrsDescriptionType() {
		org.hl7.fhir.r4.model.Location location = new org.hl7.fhir.r4.model.Location();
		location.setDescription(LOCATION_DESCRIPTION);
		
		org.openmrs.Location omrsLocation = locationTranslator.toOpenmrsType(location);
		
		assertThat(omrsLocation, notNullValue());
		assertThat(omrsLocation.getDescription(), notNullValue());
		assertThat(omrsLocation.getDescription(), equalTo(LOCATION_DESCRIPTION));
	}
	
	@Test
	public void shouldCreateFhirPositionObjectFromLatitudeAndLongitude() {
		omrsLocation.setLatitude(LOCATION_LATITUDE);
		omrsLocation.setLongitude(LOCATION_LONGITUDE);
		
		org.hl7.fhir.r4.model.Location fhirLocation = locationTranslator.toFhirResource(omrsLocation);
		
		assertThat(fhirLocation, notNullValue());
		assertThat(fhirLocation.getPosition(), notNullValue());
		assertThat(fhirLocation.getPosition().getLatitude(), notNullValue());
		assertThat(fhirLocation.getPosition().getLongitude(), notNullValue());
	}
	
	@Test
	public void shouldSetFhirLocationToActiveIfLocationIsNotRetired() {
		omrsLocation.setRetired(false);
		
		org.hl7.fhir.r4.model.Location fhirLocation = locationTranslator.toFhirResource(omrsLocation);
		
		assertThat(fhirLocation, notNullValue());
		assertThat(fhirLocation.getStatus(), equalTo(org.hl7.fhir.r4.model.Location.LocationStatus.ACTIVE));
	}
	
	@Test
	public void shouldSetFhirLocationToInActiveIfLocationIsRetired() {
		omrsLocation.setRetired(true);
		
		org.hl7.fhir.r4.model.Location fhirLocation = locationTranslator.toFhirResource(omrsLocation);
		
		assertThat(fhirLocation, notNullValue());
		assertThat(fhirLocation.getStatus(), equalTo(org.hl7.fhir.r4.model.Location.LocationStatus.INACTIVE));
	}
	
	@Test
	public void shouldTranslateLocationAttributeToFhirContactPoint() {
		LocationAttribute locationAttribute = new LocationAttribute();
		locationAttribute.setUuid(LOCATION_ATTRIBUTE_UUID);
		locationAttribute.setValue(LOCATION_ATTRIBUTE_VALUE);
		
		LocationAttributeType attributeType = new LocationAttributeType();
		attributeType.setName(LOCATION_ATTRIBUTE_TYPE_NAME);
		attributeType.setUuid(LOCATION_ATTRIBUTE_TYPE_UUID);
		locationAttribute.setAttributeType(attributeType);
		
		Location omrsLocation = new Location();
		omrsLocation.setUuid(LOCATION_UUID);
		omrsLocation.addAttribute(locationAttribute);
		
		when(propertyService.getGlobalProperty(FhirConstants.LOCATION_CONTACT_POINT_ATTRIBUTE_TYPE))
		        .thenReturn(LOCATION_ATTRIBUTE_TYPE_UUID);
		
		Map<Location, List<LocationAttribute>> activeAttributeMap = new HashMap<>(1);
		activeAttributeMap.put(omrsLocation, Collections.singletonList(locationAttribute));
		when(fhirLocationDao.getActiveAttributesByLocationsAndAttributeTypeUuid(any(Collection.class),
		    eq(LOCATION_ATTRIBUTE_TYPE_UUID))).thenReturn(activeAttributeMap);
		
		ContactPoint contactPoint = new ContactPoint();
		contactPoint.setId(LOCATION_ATTRIBUTE_UUID);
		contactPoint.setValue(LOCATION_ATTRIBUTE_VALUE);
		when(telecomTranslator.toFhirResource(locationAttribute)).thenReturn(contactPoint);
		
		org.hl7.fhir.r4.model.Location location = locationTranslator.toFhirResource(omrsLocation);
		
		assertThat(location, notNullValue());
		assertThat(location.hasTelecom(), equalTo(true));
		assertThat(location.getTelecom(), hasSize(greaterThanOrEqualTo(1)));
		assertThat(location.getTelecom().get(0).getValue(), equalTo(LOCATION_ATTRIBUTE_VALUE));
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
		ContactPoint contactPoint = location.addTelecom();
		contactPoint.setId(LOCATION_ATTRIBUTE_UUID);
		contactPoint.setValue(LOCATION_ATTRIBUTE_VALUE);
		
		when(telecomTranslator.toOpenmrsType(any(LocationAttribute.class), eq(contactPoint))).thenReturn(locationAttribute);
		
		Location omrsLocation = locationTranslator.toOpenmrsType(location);
		
		assertThat(omrsLocation, notNullValue());
		assertThat(omrsLocation.getAttributes(), notNullValue());
		assertThat(omrsLocation.getAttributes(), hasSize(greaterThanOrEqualTo(1)));
	}
	
	@Test
	public void toFhirResource_shouldTranslateOpenmrsTagsToFhirLocationTags() {
		LocationTag tag = new LocationTag(LOGIN_TAG_NAME, LOGIN_TAG_DESCRIPTION);
		omrsLocation.addTag(tag);
		
		org.hl7.fhir.r4.model.Location fhirLocation = locationTranslator.toFhirResource(omrsLocation);
		
		assertThat(fhirLocation.getMeta().getTag(), notNullValue());
		assertThat(fhirLocation.getMeta().getTag(), hasSize(greaterThanOrEqualTo(1)));
		assertThat(fhirLocation.getMeta().getTag().get(0).getCode(), is(LOGIN_TAG_NAME));
	}
	
	@Test
	public void toOpenmrsType_shouldTranslateFhirTagsToOpenmrsLocationTags() {
		LocationTag omrsTag = new LocationTag(LAB_TAG_NAME, LAB_TAG_DESCRIPTION);
		List<Coding> tags = new ArrayList<>();
		
		Coding tag = new Coding();
		tag.setCode(LAB_TAG_NAME);
		tag.setDisplay(LAB_TAG_DESCRIPTION);
		tags.add(tag);
		
		org.hl7.fhir.r4.model.Location fhirLocation = new org.hl7.fhir.r4.model.Location();
		fhirLocation.getMeta().setTag(tags);
		
		when(locationTagTranslator.toOpenmrsType(tag)).thenReturn(omrsTag);
		
		omrsLocation = locationTranslator.toOpenmrsType(fhirLocation);
		
		assertThat(omrsLocation.getTags(), notNullValue());
		assertThat(omrsLocation.getTags(), hasSize(greaterThanOrEqualTo(1)));
		assertThat(omrsLocation.getTags().iterator().next().getName(), is(LAB_TAG_NAME));
	}
	
	@Test
	public void toOpenmrsType_shouldTranslateFhirTypeToOpenmrsLocationAttributes() {
		LocationAttribute omrsAttr = new LocationAttribute();
		CodeableConcept typeConcept = new CodeableConcept();
		typeConcept.setId(LOCATION_ATTRIBUTE_TYPE_UUID);
		
		org.hl7.fhir.r4.model.Location fhirLocation = new org.hl7.fhir.r4.model.Location();
		
		fhirLocation.setType(Collections.singletonList(typeConcept));
		
		omrsLocation.addAttribute(omrsAttr);
		
		when(locationTypeTranslator.toOpenmrsType(any(), any())).thenReturn(omrsLocation);
		
		Location result = locationTranslator.toOpenmrsType(fhirLocation);
		
		assertThat(result.getActiveAttributes(), notNullValue());
		assertThat(omrsLocation.getActiveAttributes(), hasSize(greaterThanOrEqualTo(1)));
		assertThat(omrsLocation.getActiveAttributes().stream().findFirst().get(), is(omrsAttr));
	}
	
	@Test
	public void toFhirResource_shouldTranslateOpenmrsParentLocationToFhirReference() {
		Location parentLocation = new Location();
		parentLocation.setUuid(PARENT_LOCATION_UUID);
		parentLocation.setName(PARENT_LOCATION_NAME);
		omrsLocation.setParentLocation(parentLocation);
		
		Reference locationReference = locationTranslator.toFhirResource(omrsLocation).getPartOf();
		
		assertThat(locationReference, notNullValue());
		assertThat(locationReference.getType(), is(FhirConstants.LOCATION));
		assertThat(locationReference.getDisplay(), is(PARENT_LOCATION_NAME));
		assertThat(getReferenceId(locationReference).orElse(null), equalTo(PARENT_LOCATION_UUID));
	}
	
	@Test
	public void toFhirResource_shouldReturnNullReferenceIfParentLocationIsNull() {
		omrsLocation.setParentLocation(null);
		Reference locationReference = locationTranslator.toFhirResource(omrsLocation).getPartOf();
		assertThat(locationReference.getDisplay(), nullValue());
	}
	
	@Test
	public void toFhirResource_shouldTranslateOpenMrsDateChangedToLastUpdatedDate() {
		omrsLocation.setDateChanged(new Date());
		
		org.hl7.fhir.r4.model.Location location = locationTranslator.toFhirResource(omrsLocation);
		
		assertThat(location, notNullValue());
		assertThat(location.getMeta().getLastUpdated(), DateMatchers.sameDay(new Date()));
	}
	
	@Test
	public void toFhirResource_shouldTranslateOpenMrsDateChangedToVersionId() {
		omrsLocation.setDateChanged(new Date());
		
		org.hl7.fhir.r4.model.Location location = locationTranslator.toFhirResource(omrsLocation);
		
		assertThat(location, notNullValue());
		assertThat(location.getMeta().getVersionId(), notNullValue());
	}
	
	@Test
	public void getOpenmrsParentLocation_shouldReturnNullIfReferenceIsNull() {
		Location result = locationTranslator.getOpenmrsParentLocation(null);
		
		assertThat(result, nullValue());
	}
	
	@Test
	public void getOpenmrsParentLocation_shouldReturnCorrectParentLocation() {
		Reference locationReference = new Reference().setReference(FhirConstants.LOCATION + "/" + PARENT_LOCATION_UUID)
		        .setType(FhirConstants.LOCATION).setIdentifier(new Identifier().setValue(PARENT_LOCATION_UUID));
		
		Location parentLocation = new Location();
		parentLocation.setUuid(PARENT_LOCATION_UUID);
		when(fhirLocationDao.get(PARENT_LOCATION_UUID)).thenReturn(parentLocation);
		
		Location result = locationTranslator.getOpenmrsParentLocation(locationReference);
		
		assertThat(result, notNullValue());
		assertThat(result.getUuid(), is(PARENT_LOCATION_UUID));
	}
	
	@Test
	public void getOpenmrsParentLocation_shouldReturnNullIfLocationHasNoIdentifier() {
		Reference locationReference = new Reference().setReference(FhirConstants.LOCATION + "/")
		        .setType(FhirConstants.LOCATION);
		
		Location result = locationTranslator.getOpenmrsParentLocation(locationReference);
		
		assertThat(result, nullValue());
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void getOpenmrsParentLocation_shouldThrowExceptionIfReferenceIsntForLocation() {
		Reference reference = new Reference().setReference("Unknown" + "/" + PARENT_LOCATION_NAME).setType("Unknown");
		
		locationTranslator.getOpenmrsParentLocation(reference);
	}
	
	@Test
	public void toOpenmrsType_shouldTranslateFhirPositionToLocationLatitude() {
		org.hl7.fhir.r4.model.Location.LocationPositionComponent position = new org.hl7.fhir.r4.model.Location.LocationPositionComponent();
		position.setLatitude(new BigDecimal(LOCATION_LATITUDE));
		position.setLongitude(new BigDecimal(LOCATION_LONGITUDE));
		
		org.hl7.fhir.r4.model.Location location = new org.hl7.fhir.r4.model.Location();
		location.setPosition(position);
		
		org.openmrs.Location omrsLocation = locationTranslator.toOpenmrsType(location);
		
		assertThat(omrsLocation, notNullValue());
		assertThat(omrsLocation.getLatitude(), is(LOCATION_LATITUDE));
		assertThat(omrsLocation.getLongitude(), is(LOCATION_LONGITUDE));
	}
}
