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
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;

import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openmrs.Concept;
import org.openmrs.Location;
import org.openmrs.LocationAttribute;
import org.openmrs.LocationAttributeType;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.api.FhirGlobalPropertyService;
import org.openmrs.module.fhir2.api.dao.FhirConceptDao;
import org.openmrs.module.fhir2.api.dao.FhirLocationDao;
import org.openmrs.module.fhir2.api.translators.ConceptTranslator;

@RunWith(MockitoJUnitRunner.class)
public class LocationTypeTranslatorImplTest {
	
	private static final String TYPE_CONCEPT_UUID = "91df3897-1066-46a1-a403-714b737af00b";
	
	private static final String FHIR_TYPE_CONCEPT_UUID = "693a6e1f-7026-4406-adf9-c7ea3b1e8d6e";
	
	private static final String ATTRIBUTE_TYPE_UUID = "b9d36c82-5c73-11e3-ae03-0800271c1b75";
	
	@Mock
	private ConceptTranslator conceptTranslator;
	
	@Mock
	private FhirGlobalPropertyService globalPropertyService;
	
	@Mock
	private FhirLocationDao locationDao;
	
	@Mock
	private FhirConceptDao conceptDao;
	
	private Location omrsLocation;
	
	private LocationTypeTranslatorImpl locationTypeTranslator;
	
	@Before
	public void setup() {
		omrsLocation = new Location();
		locationTypeTranslator = new LocationTypeTranslatorImpl();
		locationTypeTranslator.setConceptTranslator(conceptTranslator);
		locationTypeTranslator.setGlobalPropertyService(globalPropertyService);
		locationTypeTranslator.setLocationDao(locationDao);
		locationTypeTranslator.setConceptDao(conceptDao);
	}
	
	@Test
	public void toFhirResource_shouldTranslateLocationConceptAttributeToFhir() {
		Concept typeConcept = new Concept();
		CodeableConcept fhirTypeConcept = new CodeableConcept();
		
		fhirTypeConcept.setId(FHIR_TYPE_CONCEPT_UUID);
		typeConcept.setUuid(TYPE_CONCEPT_UUID);
		
		LocationAttributeType typeAttributeType = new LocationAttributeType();
		typeAttributeType.setUuid(ATTRIBUTE_TYPE_UUID);
		
		LocationAttribute typeAttribute = new LocationAttribute();
		typeAttribute.setAttributeType(typeAttributeType);
		typeAttribute.setValue(TYPE_CONCEPT_UUID);
		omrsLocation.addAttribute(typeAttribute);
		
		when(globalPropertyService.getGlobalProperty(FhirConstants.LOCATION_TYPE_ATTRIBUTE_TYPE))
		        .thenReturn(ATTRIBUTE_TYPE_UUID);
		when(locationDao.getLocationAttributeTypeByUuid(ATTRIBUTE_TYPE_UUID)).thenReturn(typeAttributeType);
		when(conceptDao.get(TYPE_CONCEPT_UUID)).thenReturn(typeConcept);
		when(conceptTranslator.toFhirResource(eq(typeConcept))).thenReturn(fhirTypeConcept);
		
		List<CodeableConcept> result = locationTypeTranslator.toFhirResource(omrsLocation);
		
		assertThat(result, notNullValue());
		assertThat(result, hasSize(greaterThan(0)));
		assertThat(result.get(0).getId(), equalTo(FHIR_TYPE_CONCEPT_UUID));
	}
	
	@Test
	public void toFhirResource_shouldReturnEmptyListForNullType() {
		when(globalPropertyService.getGlobalProperty(FhirConstants.LOCATION_TYPE_ATTRIBUTE_TYPE)).thenReturn(null);

		List<CodeableConcept> result = locationTypeTranslator.toFhirResource(omrsLocation);

		assertThat(result, hasSize(equalTo(0)));
	}
	
	@Test
	public void toOpenmrsType_shouldTranslateFhirTypeCodeableConceptToLocationAttribute() {
		Concept typeConcept = new Concept();
		Coding typeCoding = new Coding();
		CodeableConcept fhirTypeConcept = new CodeableConcept();
		
		typeCoding.setId(FHIR_TYPE_CONCEPT_UUID);
		fhirTypeConcept.setId(FHIR_TYPE_CONCEPT_UUID);
		fhirTypeConcept.setCoding(Collections.singletonList(typeCoding));
		typeConcept.setUuid(TYPE_CONCEPT_UUID);
		
		LocationAttributeType typeAttributeType = new LocationAttributeType();
		typeAttributeType.setUuid(ATTRIBUTE_TYPE_UUID);
		
		when(globalPropertyService.getGlobalProperty(FhirConstants.LOCATION_TYPE_ATTRIBUTE_TYPE))
		        .thenReturn(ATTRIBUTE_TYPE_UUID);
		when(locationDao.getLocationAttributeTypeByUuid(ATTRIBUTE_TYPE_UUID)).thenReturn(typeAttributeType);
		when(locationDao.getActiveAttributesByLocationAndAttributeTypeUuid(omrsLocation, ATTRIBUTE_TYPE_UUID))
		        .thenReturn(Collections.emptyList());
		when(conceptTranslator.toOpenmrsType(eq(fhirTypeConcept))).thenReturn(typeConcept);
		
		Location result = locationTypeTranslator.toOpenmrsType(omrsLocation, Collections.singletonList(fhirTypeConcept));
		
		assertThat(result, notNullValue());
		assertThat(result.getAttributes(), not(empty()));
	}
}
