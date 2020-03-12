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
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.exparity.hamcrest.date.DateMatchers;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.ContactPoint;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Provenance;
import org.hl7.fhir.r4.model.Reference;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openmrs.Location;
import org.openmrs.LocationAttribute;
import org.openmrs.LocationAttributeType;
import org.openmrs.LocationTag;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.api.FhirGlobalPropertyService;
import org.openmrs.module.fhir2.api.dao.FhirLocationDao;
import org.openmrs.module.fhir2.api.translators.CustomizableMetadataTranslator;
import org.openmrs.module.fhir2.api.translators.LocationAddressTranslator;
import org.openmrs.module.fhir2.api.translators.TelecomTranslator;
import org.openmrs.module.fhir2.api.util.FhirUtils;

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
	
	private static final String LOGIN_TAG_NAME = "Login location";
	
	private static final String LOGIN_TAG_DESCRIPTION = "Used to identify login locations";
	
	private static final String LAB_TAG_NAME = "Lab location";
	
	private static final String LAB_TAG_DESCRIPTION = "Used to identify lab locations";
	
	private static final String PARENT_LOCATION_UUID = "c0938432-1691-11df-97a5-7038c432u4e6";
	
	private static final String PARENT_LOCATION_NAME = "Parent Location";
	
	@Mock
	private LocationAddressTranslator locationAddressTranslator;
	
	@Mock
	private TelecomTranslator<Object> telecomTranslator;
	
	@Mock
	private FhirLocationDao fhirLocationDao;
	
	@Mock
	private FhirGlobalPropertyService propertyService;
	
	@Mock
	private CustomizableMetadataTranslator<LocationAttribute, Location> customizableMetadataTranslator;
	
	private LocationTranslatorImpl locationTranslator;
	
	private Location omrsLocation;
	
	@Before
	public void setup() {
		omrsLocation = new Location();
		locationTranslator = new LocationTranslatorImpl();
		locationTranslator.setLocationAddressTranslator(locationAddressTranslator);
		locationTranslator.setTelecomTranslator(telecomTranslator);
		locationTranslator.setFhirLocationDao(fhirLocationDao);
		locationTranslator.setPropertyService(propertyService);
		locationTranslator.setCustomizableMetadataTranslator(customizableMetadataTranslator);
		
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
		
		Location omrsLocation = locationTranslator.toOpenmrsType(location);
		assertThat(omrsLocation, notNullValue());
		assertThat(omrsLocation.getAttributes(), notNullValue());
		assertThat(omrsLocation.getAttributes().size(), greaterThanOrEqualTo(1));
	}
	
	@Test
	public void getLocationContactDetails_shouldWorkAsExpected() {
		Location omrsLocation = new Location();
		omrsLocation.setUuid(LOCATION_UUID);
		
		LocationAttribute locationAttribute = new LocationAttribute();
		locationAttribute.setUuid(LOCATION_ATTRIBUTE_UUID);
		locationAttribute.setValue(LOCATION_ATTRIBUTE_VALUE);
		
		LocationAttributeType attributeType = new LocationAttributeType();
		attributeType.setUuid(LOCATION_ATTRIBUTE_TYPE_UUID);
		attributeType.setName(LOCATION_ATTRIBUTE_TYPE_NAME);
		locationAttribute.setAttributeType(attributeType);
		omrsLocation.setAttribute(locationAttribute);
		
		List<ContactPoint> contactPoints = locationTranslator.getLocationContactDetails(omrsLocation);
		
		assertThat(contactPoints, notNullValue());
		assertThat(omrsLocation.getActiveAttributes().size(), equalTo(1));
		
	}
	
	@Test
	public void toFhirResource_shoudTranslateOpenmrsTagsToFhirLocationTags() {
		LocationTag tag = new LocationTag(LOGIN_TAG_NAME, LOGIN_TAG_DESCRIPTION);
		omrsLocation.addTag(tag);
		org.hl7.fhir.r4.model.Location fhirLocation = locationTranslator.toFhirResource(omrsLocation);
		assertThat(fhirLocation.getMeta().getTag(), notNullValue());
		assertThat(fhirLocation.getMeta().getTag(), hasSize(greaterThanOrEqualTo(1)));
		assertThat(fhirLocation.getMeta().getTag().get(0).getCode(), is(LOGIN_TAG_NAME));
	}
	
	@Test
	public void toOpenmrsType_shouldTranslateFhirTagsToOpenmrsLocationTags() {
		List<Coding> tags = new ArrayList<>();
		Coding tag = new Coding();
		tag.setCode(LAB_TAG_NAME);
		tag.setDisplay(LAB_TAG_DESCRIPTION);
		tags.add(tag);
		org.hl7.fhir.r4.model.Location fhirLocation = new org.hl7.fhir.r4.model.Location();
		fhirLocation.getMeta().setTag(tags);
		omrsLocation = locationTranslator.toOpenmrsType(fhirLocation);
		assertThat(omrsLocation.getTags(), notNullValue());
		assertThat(omrsLocation.getTags(), hasSize(greaterThanOrEqualTo(1)));
		assertThat(omrsLocation.getTags().iterator().next().getName(), is(LAB_TAG_NAME));
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
		assertThat(locationTranslator.getReferenceId(locationReference), equalTo(PARENT_LOCATION_UUID));
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
		when(fhirLocationDao.getLocationByUuid(PARENT_LOCATION_UUID)).thenReturn(parentLocation);
		Location result = locationTranslator.getOpenmrsParentLocation(locationReference);
		assertThat(result, notNullValue());
		assertThat(result.getUuid(), is(PARENT_LOCATION_UUID));
	}
	
	@Test
	public void getOpenmrsParentLocation_shouldReturnNullIfLocationHasNoIdentifier() {
		Reference locationReference = new Reference().setReference(FhirConstants.LOCATION + "/" + PARENT_LOCATION_UUID)
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
	public void toOpenmrsType_shouldRetireLocationIfFhirLocationIsInActive() {
		org.hl7.fhir.r4.model.Location location = new org.hl7.fhir.r4.model.Location();
		location.setStatus(org.hl7.fhir.r4.model.Location.LocationStatus.INACTIVE);
		
		Location omrsLocation = locationTranslator.toOpenmrsType(location);
		assertThat(omrsLocation, notNullValue());
		assertThat(omrsLocation.getRetired(), is(true));
		assertThat(omrsLocation.getRetireReason(), is("Retired by FHIR module"));
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
	
	@Test
	public void toOpenmrsType_shouldTranslateLastUpdatedDateToDateChanged() {
		org.hl7.fhir.r4.model.Location location = new org.hl7.fhir.r4.model.Location();
		location.getMeta().setLastUpdated(new Date());
		
		Location omrsLocation = locationTranslator.toOpenmrsType(location);
		assertThat(omrsLocation, notNullValue());
		assertThat(omrsLocation.getDateChanged(), DateMatchers.sameDay(new Date()));
	}
	
	@Test
	public void toFhirResource_shouldAddProvenanceResources() {
		Location location = new Location();
		location.setUuid(LOCATION_UUID);
		Provenance provenance = new Provenance();
		provenance.setId(new IdType(FhirUtils.uniqueUuid()));
		when(customizableMetadataTranslator.getCreateProvenance(location)).thenReturn(provenance);
		when(customizableMetadataTranslator.getUpdateProvenance(location)).thenReturn(provenance);
		org.hl7.fhir.r4.model.Location result = locationTranslator.toFhirResource(location);
		assertThat(result, notNullValue());
		assertThat(result.getContained(), not(empty()));
		assertThat(result.getContained().size(), greaterThanOrEqualTo(2));
		assertThat(result.getContained().stream()
		        .anyMatch(resource -> resource.getResourceType().name().equals(Provenance.class.getSimpleName())),
		    is(true));
	}

	@Test
	public void shouldNotAddUpdateProvenanceIfDateChangedAndChangedByAreBothNull() {
		Provenance provenance = new Provenance();
		provenance.setId(new IdType(FhirUtils.uniqueUuid()));

		org.openmrs.Location location = new org.openmrs.Location();
		location.setUuid(LOCATION_UUID);
		location.setDateChanged(null);
		location.setChangedBy(null);
		when(customizableMetadataTranslator.getCreateProvenance(location)).thenReturn(provenance);
		when(customizableMetadataTranslator.getUpdateProvenance(location)).thenReturn(null);

		org.hl7.fhir.r4.model.Location result = locationTranslator.toFhirResource(location);
		assertThat(result, notNullValue());
		assertThat(result.getContained(), not(empty()));
		assertThat(result.getContained().size(), equalTo(1));
		assertThat(result.getContained().stream()
						.anyMatch(resource -> resource.getResourceType().name().equals(Provenance.class.getSimpleName())),
				is(true));
	}
}
