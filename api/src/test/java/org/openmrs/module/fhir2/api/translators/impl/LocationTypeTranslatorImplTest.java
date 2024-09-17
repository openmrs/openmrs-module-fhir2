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
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.any;
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
	
	private static final String TYPE_ATTRIBUTE_UUID = "937f1528-6c54-4dcb-9982-fe6a7211e2ad";
	
	private static final String LOCATION_ATTRIBUTE_TYPE_UUID = "dd746472-5d3b-4d11-a5da-30710158cc78";
	
	private static final String LOCATION_TYPE_SYSTEM_URL = "https://terminology.hl7.org/CodeSystem/v3-RoleCode";
	
	@Mock
	private ConceptTranslator conceptTranslator;
	
	@Mock
	private FhirLocationDao locationDao;
	
	@Mock
	private FhirConceptDao conceptDao;
	
	@Mock
	FhirGlobalPropertyService globalPropertyService;
	
	private Location omrsLocation;
	
	private LocationTypeTranslatorImpl locationTypeTranslator;
	
	@Before
	public void setup() {
		omrsLocation = new Location();
		locationTypeTranslator = new LocationTypeTranslatorImpl();
		locationTypeTranslator.setConceptDao(conceptDao);
		locationTypeTranslator.setLocationDao(locationDao);
		locationTypeTranslator.setConceptTranslator(conceptTranslator);
		locationTypeTranslator.setGlobalPropertyService(globalPropertyService);
	}
	
	@Test
	public void toFhirResource_shouldTranslateLocationConceptAttributeToFhir() {
		LocationAttribute typeAttribute = new LocationAttribute();
		LocationAttributeType typeAttributeType = new LocationAttributeType();
		Concept typeConcept = new Concept();
		CodeableConcept fhirTypeConcept = new CodeableConcept();
		
		fhirTypeConcept.setId(FHIR_TYPE_CONCEPT_UUID);
		typeConcept.setUuid(TYPE_CONCEPT_UUID);
		typeAttribute.setUuid(TYPE_ATTRIBUTE_UUID);
		typeAttribute.setAttributeType(typeAttributeType);
		typeAttribute.setValue(TYPE_CONCEPT_UUID);
		
		omrsLocation.setAttribute(typeAttribute);
		
		when(conceptTranslator.toFhirResource(eq(typeConcept))).thenReturn(fhirTypeConcept);
		when(conceptDao.get(eq(TYPE_CONCEPT_UUID))).thenReturn(typeConcept);
		when(locationDao.getLocationAttributeTypeByUuid(LOCATION_ATTRIBUTE_TYPE_UUID)).thenReturn(typeAttributeType);
		when(globalPropertyService.getGlobalProperty(FhirConstants.LOCATION_TYPE_ATTRIBUTE_TYPE))
		        .thenReturn(LOCATION_ATTRIBUTE_TYPE_UUID);
		
		List<CodeableConcept> result = locationTypeTranslator.toFhirResource(omrsLocation);
		
		assertThat(result, notNullValue());
		assertThat(result, hasSize(greaterThan(0)));
		assertThat(result.get(0).getId(), equalTo(FHIR_TYPE_CONCEPT_UUID));
	}
	
	@Test
	public void toFhirResource_shouldReturnEmptyListForNonexistantAttribute() {
		List<CodeableConcept> result = locationTypeTranslator.toFhirResource(omrsLocation);
		
		assertThat(result, hasSize(equalTo(0)));
	}
	
	@Test
	public void toOpenmrsType_shouldTranslateFhirTypeCodeableConceptToNewLocationAttribute() {
		LocationAttribute typeAttribute = new LocationAttribute();
		LocationAttributeType typeAttributeType = new LocationAttributeType();
		Concept typeConcept = new Concept();
		CodeableConcept fhirTypeConcept = new CodeableConcept();
		
		fhirTypeConcept.setId(FHIR_TYPE_CONCEPT_UUID);
		fhirTypeConcept.setCoding(Collections.singletonList(new Coding().setSystem(LOCATION_TYPE_SYSTEM_URL)));
		typeConcept.setUuid(TYPE_CONCEPT_UUID);
		typeAttribute.setUuid(TYPE_ATTRIBUTE_UUID);
		typeAttribute.setAttributeType(typeAttributeType);
		typeAttribute.setValue(TYPE_CONCEPT_UUID);
		
		when(locationDao.getLocationAttributeTypeByUuid(LOCATION_ATTRIBUTE_TYPE_UUID)).thenReturn(typeAttributeType);
		when(globalPropertyService.getGlobalProperty(FhirConstants.LOCATION_TYPE_ATTRIBUTE_TYPE))
		        .thenReturn(LOCATION_ATTRIBUTE_TYPE_UUID);
		when(conceptTranslator.toOpenmrsType(eq(fhirTypeConcept))).thenReturn(typeConcept);
		
		Location result = locationTypeTranslator.toOpenmrsType(omrsLocation, Collections.singletonList(fhirTypeConcept));
		
		assertThat(result, notNullValue());
		assertThat(result.getActiveAttributes(), hasSize(greaterThan(0)));
		assertThat(result.getActiveAttributes().stream().findFirst().get().getValue(), equalTo(TYPE_CONCEPT_UUID));
	}
	
	@Test
	public void toOpenmrsType_shouldTranslateFhirTypeCodeableConceptToExistingLocationAttribute() {
		LocationAttribute typeAttribute = new LocationAttribute();
		LocationAttributeType typeAttributeType = new LocationAttributeType();
		Concept typeConcept = new Concept();
		CodeableConcept fhirTypeConcept = new CodeableConcept();
		
		fhirTypeConcept.setId(FHIR_TYPE_CONCEPT_UUID);
		fhirTypeConcept.setCoding(Collections.singletonList(new Coding().setSystem(LOCATION_TYPE_SYSTEM_URL)));
		typeConcept.setUuid(TYPE_CONCEPT_UUID);
		typeAttribute.setUuid(TYPE_ATTRIBUTE_UUID);
		typeAttribute.setAttributeType(typeAttributeType);
		typeAttribute.setValue(TYPE_CONCEPT_UUID);
		omrsLocation.addAttribute(typeAttribute);
		
		when(locationDao.getLocationAttributeTypeByUuid(LOCATION_ATTRIBUTE_TYPE_UUID)).thenReturn(typeAttributeType);
		when(globalPropertyService.getGlobalProperty(FhirConstants.LOCATION_TYPE_ATTRIBUTE_TYPE))
		        .thenReturn(LOCATION_ATTRIBUTE_TYPE_UUID);
		when(conceptTranslator.toOpenmrsType(eq(fhirTypeConcept))).thenReturn(typeConcept);
		when(locationDao.getActiveAttributesByLocationAndAttributeTypeUuid(any(), any()))
		        .thenReturn(Collections.singletonList(typeAttribute));
		
		Location result = locationTypeTranslator.toOpenmrsType(omrsLocation, Collections.singletonList(fhirTypeConcept));
		
		assertThat(result, notNullValue());
		assertThat(result.getActiveAttributes(), hasSize(greaterThan(0)));
		assertThat(result.getActiveAttributes().stream().findFirst().get().getValue(), equalTo(TYPE_CONCEPT_UUID));
	}
}
