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
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.hl7.fhir.r4.model.BooleanType;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Extension;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.StringType;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openmrs.Concept;
import org.openmrs.Location;
import org.openmrs.PersonAttribute;
import org.openmrs.PersonAttributeType;
import org.openmrs.api.PersonService;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.api.FhirConceptService;
import org.openmrs.module.fhir2.api.FhirLocationService;
import org.openmrs.module.fhir2.api.translators.ConceptTranslator;
import org.openmrs.module.fhir2.api.translators.LocationReferenceTranslator;

@RunWith(MockitoJUnitRunner.class)
public class PersonAttributeTranslatorImplTest {
	
	private static final String PERSON_ATTRIBUTE_UUID = "12o3et5kl3-2e323-23g23-232h3y343s";
	
	private static final String ATTRIBUTE_TYPE_UUID = "14d4f066-15f5-102d-96e4-000c29c2a5d7";
	
	private static final String ATTRIBUTE_TYPE_NAME = "Contact";
	
	private static final String NOT_IN_SYSTEM_ATTRIBUTE_TYPE_NAME = "Contact";
	
	private static final String STRING_ATTRIBUTE_VALUE = "254723723456";
	
	private static final String BOOLEAN_ATTRIBUTE_VALUE = "true";
	
	private static final String CONCEPT_ATTRIBUTE_VALUE = "1000";
	
	private static final Integer LOCATION_ATTRIBUTE_ID = 1;
	
	private static final String LOCATION_ATTRIBUTE_UUID_VALUE = "ae919697-60a2-4f72-834c-e7d9df3ecf62";
	
	private static final String LOCATION_NAME = "Test Location";
	
	@Mock
	private FhirLocationService locationService;
	
	@Mock
	private PersonService personService;
	
	@Mock
	private FhirConceptService conceptService;
	
	@Mock
	private ConceptTranslator conceptTranslator;
	
	@Mock
	private LocationReferenceTranslator locationReferenceTranslator;
	
	@InjectMocks
	private PersonAttributeTranslatorImpl personAttributeTranslator;
	
	private PersonAttribute personAttribute;
	
	private PersonAttributeType personAttributeType;
	
	@Before
	public void setup() {
		personAttributeType = new PersonAttributeType();
		personAttributeType.setUuid(ATTRIBUTE_TYPE_UUID);
		personAttributeType.setName(ATTRIBUTE_TYPE_NAME);
		personAttributeType.setFormat("java.lang.String");
		
		personAttribute = new PersonAttribute();
		
		personAttribute.setUuid(PERSON_ATTRIBUTE_UUID);
		personAttribute.setAttributeType(personAttributeType);
		personAttribute.setValue(STRING_ATTRIBUTE_VALUE);
		
		when(personService.getPersonAttributeTypeByName(ATTRIBUTE_TYPE_NAME)).thenReturn(personAttributeType);
	}
	
	@Test
	public void shouldTranslatePersonAttributeToFhirExtension() {
		Extension result = personAttributeTranslator.toFhirResource(personAttribute);
		
		assertThat(result, notNullValue());
		assertTrue(result.hasUrl());
		assertTrue(result.hasExtension());
		assertThat(result.getUrl(), equalTo(FhirConstants.OPENMRS_FHIR_EXT_PERSON_ATTRIBUTE));
		assertThat(result.getExtensionByUrl(FhirConstants.OPENMRS_FHIR_EXT_PERSON_ATTRIBUTE_TYPE), notNullValue());
		assertThat(result.getExtensionByUrl(FhirConstants.OPENMRS_FHIR_EXT_PERSON_ATTRIBUTE_VALUE), notNullValue());
		assertThat(result.getExtensionByUrl(FhirConstants.OPENMRS_FHIR_EXT_PERSON_ATTRIBUTE_TYPE).getValue().toString(),
		    equalTo(ATTRIBUTE_TYPE_NAME));
		assertThat(result.getExtensionByUrl(FhirConstants.OPENMRS_FHIR_EXT_PERSON_ATTRIBUTE_VALUE).getValue().toString(),
		    notNullValue());
		assertThat(((StringType) result.getExtensionByUrl(FhirConstants.OPENMRS_FHIR_EXT_PERSON_ATTRIBUTE_VALUE).getValue())
		        .toString(),
		    equalTo(STRING_ATTRIBUTE_VALUE));
	}
	
	@Test
	public void shouldTranslateStringPersonAttributeToFhirExtension() {
		personAttributeType.setFormat("java.lang.String");
		personAttribute.setValue(STRING_ATTRIBUTE_VALUE);
		
		Extension result = personAttributeTranslator.toFhirResource(personAttribute);
		assertThat(result, notNullValue());
		
		Extension valueExtension = result.getExtensionByUrl(FhirConstants.OPENMRS_FHIR_EXT_PERSON_ATTRIBUTE_VALUE);
		assertThat(valueExtension, notNullValue());
		assertThat(valueExtension.getValue() instanceof StringType, is(true));
		assertThat(((StringType) valueExtension.getValue()).getValue(), equalTo(STRING_ATTRIBUTE_VALUE));
	}
	
	@Test
	public void shouldTranslateBooleanPersonAttributeToFhirExtension() {
		personAttributeType.setFormat("java.lang.Boolean");
		personAttribute.setValue(BOOLEAN_ATTRIBUTE_VALUE);
		
		Extension result = personAttributeTranslator.toFhirResource(personAttribute);
		assertThat(result, notNullValue());
		
		Extension valueExtension = result.getExtensionByUrl(FhirConstants.OPENMRS_FHIR_EXT_PERSON_ATTRIBUTE_VALUE);
		assertThat(valueExtension, notNullValue());
		assertThat(valueExtension.getValue() instanceof BooleanType, is(true));
		assertThat(((BooleanType) valueExtension.getValue()).getValue(), is(true));
	}
	
	@Test
	public void shouldTranslateLocationPersonAttributeToFhirExtension() {
		personAttributeType.setFormat("org.openmrs.Location");
		personAttribute.setValue(LOCATION_ATTRIBUTE_ID.toString());
		
		Location location = new Location();
		location.setName(LOCATION_NAME);
		location.setUuid(LOCATION_ATTRIBUTE_UUID_VALUE);
		
		Reference locationReference = new Reference();
		locationReference.setReference("Location/" + LOCATION_ATTRIBUTE_UUID_VALUE);
		locationReference.setType(FhirConstants.LOCATION);
		locationReference.setDisplay(LOCATION_NAME);
		
		when(locationService.get(LOCATION_ATTRIBUTE_ID)).thenReturn(location);
		when(locationReferenceTranslator.toFhirResource(location)).thenReturn(locationReference);
		
		Extension result = personAttributeTranslator.toFhirResource(personAttribute);
		assertThat(result, notNullValue());
		
		Extension valueExtension = result.getExtensionByUrl(FhirConstants.OPENMRS_FHIR_EXT_PERSON_ATTRIBUTE_VALUE);
		assertThat(valueExtension, notNullValue());
		assertThat(valueExtension.getValue() instanceof Reference, is(true));
		Reference reference = (Reference) valueExtension.getValue();
		assertThat(reference.getReference(), not("Location/" + LOCATION_ATTRIBUTE_ID));
		assertThat(reference.getReference(), equalTo("Location/" + LOCATION_ATTRIBUTE_UUID_VALUE));
		assertThat(reference.getDisplay(), equalTo(LOCATION_NAME));
		assertThat(reference.getType(), equalTo("Location"));
	}
	
	@Test
	public void shouldTranslateConceptPersonAttributeToFhirExtension() {
		personAttributeType.setFormat("org.openmrs.Concept");
		personAttribute.setValue(CONCEPT_ATTRIBUTE_VALUE);
		
		Concept concept = new Concept();
		concept.setConceptId(Integer.parseInt(CONCEPT_ATTRIBUTE_VALUE));
		when(conceptService.get(Integer.parseInt(CONCEPT_ATTRIBUTE_VALUE))).thenReturn(concept);
		
		CodeableConcept codeableConcept = new CodeableConcept();
		codeableConcept.setText("ConceptText");
		when(conceptTranslator.toFhirResource(concept)).thenReturn(codeableConcept);
		
		Extension result = personAttributeTranslator.toFhirResource(personAttribute);
		assertThat(result, notNullValue());
		
		Extension valueExtension = result.getExtensionByUrl(FhirConstants.OPENMRS_FHIR_EXT_PERSON_ATTRIBUTE_VALUE);
		assertThat(valueExtension, notNullValue());
		
		assertThat(valueExtension.getValue() instanceof CodeableConcept, is(true));
		verify(conceptTranslator).toFhirResource(concept);
	}
	
	@Test
	public void shouldReturnNullWhenPersonAttributeIsNotSupported() {
		personAttributeType.setFormat("invalid-format");
		Extension result = personAttributeTranslator.toFhirResource(personAttribute);
		
		assertThat(result, equalTo(null));
	}
	
	@Test
	public void shouldReturnNullWhenPersonAttributeIsNull() {
		Extension result = personAttributeTranslator.toFhirResource(null);
		
		assertThat(result, equalTo(null));
	}
	
	@Test
	public void shouldReturnNullWhenExtensionIsNull() {
		PersonAttribute result = personAttributeTranslator.toOpenmrsType(null);
		
		assertThat(result, equalTo(null));
	}
	
	@Test
	public void shouldReturnNullWhenExtensionIsInvalid() {
		Extension extension = new Extension();
		extension.setUrl("invalid-url");
		
		PersonAttribute result = personAttributeTranslator.toOpenmrsType(extension);
		
		assertThat(result, equalTo(null));
	}
	
	@Test
	public void shouldReturnNullWhenLocationTypePersonAttributeHasNulLValue() {
		personAttributeType.setFormat("org.openmrs.Location");
		personAttribute.setValue(null);
		
		Extension result = personAttributeTranslator.toFhirResource(personAttribute);
		
		assertThat(result, equalTo(null));
	}
	
	@Test
	public void shouldReturnNullWhenConceptTypePersonAttributeHasNulLValue() {
		personAttributeType.setFormat("org.openmrs.Concept");
		personAttribute.setValue(null);
		
		Extension result = personAttributeTranslator.toFhirResource(personAttribute);
		
		assertThat(result, equalTo(null));
	}
	
	@Test
	public void shouldTranslateStringTypeExtensionToPersonAttribute() {
		Extension personAttributeTypeExtension = new Extension();
		personAttributeTypeExtension.setUrl(FhirConstants.OPENMRS_FHIR_EXT_PERSON_ATTRIBUTE_TYPE);
		personAttributeTypeExtension.setValue(new StringType(ATTRIBUTE_TYPE_NAME));
		
		Extension personAttributeValueExtension = new Extension();
		personAttributeValueExtension.setUrl(FhirConstants.OPENMRS_FHIR_EXT_PERSON_ATTRIBUTE_VALUE);
		personAttributeValueExtension.setValue(new StringType(STRING_ATTRIBUTE_VALUE));
		
		Extension extension = new Extension();
		extension.setUrl(FhirConstants.OPENMRS_FHIR_EXT_PERSON_ATTRIBUTE);
		extension.addExtension(personAttributeTypeExtension);
		extension.addExtension(personAttributeValueExtension);
		
		PersonAttribute result = personAttributeTranslator.toOpenmrsType(extension);
		
		assertThat(result, notNullValue());
		assertThat(result.getAttributeType(), equalTo(personAttributeType));
		assertThat(result.getValue(), equalTo(STRING_ATTRIBUTE_VALUE));
	}
	
	@Test
	public void shouldNotTranslateExtensionWithoutPersonAttributeTypeInSystemToPersonAttribute() {
		Extension personAttributeTypeExtension = new Extension();
		personAttributeTypeExtension.setUrl(FhirConstants.OPENMRS_FHIR_EXT_PERSON_ATTRIBUTE_TYPE);
		personAttributeTypeExtension.setValue(new StringType(NOT_IN_SYSTEM_ATTRIBUTE_TYPE_NAME));
		
		Extension personAttributeValueExtension = new Extension();
		personAttributeValueExtension.setUrl(FhirConstants.OPENMRS_FHIR_EXT_PERSON_ATTRIBUTE_VALUE);
		personAttributeValueExtension.setValue(new StringType(STRING_ATTRIBUTE_VALUE));
		
		when(personService.getPersonAttributeTypeByName(NOT_IN_SYSTEM_ATTRIBUTE_TYPE_NAME)).thenReturn(null);
		
		Extension extension = new Extension();
		extension.setUrl(FhirConstants.OPENMRS_FHIR_EXT_PERSON_ATTRIBUTE);
		extension.addExtension(personAttributeTypeExtension);
		extension.addExtension(personAttributeValueExtension);
		
		PersonAttribute result = personAttributeTranslator.toOpenmrsType(extension);
		
		assertThat(result, equalTo(null));
	}
	
	@Test
	public void shouldTranslateBooleanTypeExtensionToPersonAttribute() {
		Extension personAttributeTypeExtension = new Extension();
		personAttributeTypeExtension.setUrl(FhirConstants.OPENMRS_FHIR_EXT_PERSON_ATTRIBUTE_TYPE);
		personAttributeTypeExtension.setValue(new StringType(ATTRIBUTE_TYPE_NAME));
		
		Extension personAttributeValueExtension = new Extension();
		personAttributeValueExtension.setUrl(FhirConstants.OPENMRS_FHIR_EXT_PERSON_ATTRIBUTE_VALUE);
		personAttributeValueExtension.setValue(new BooleanType(true));
		
		Extension extension = new Extension();
		extension.setUrl(FhirConstants.OPENMRS_FHIR_EXT_PERSON_ATTRIBUTE);
		extension.addExtension(personAttributeTypeExtension);
		extension.addExtension(personAttributeValueExtension);
		
		PersonAttribute result = personAttributeTranslator.toOpenmrsType(extension);
		
		assertThat(result, notNullValue());
		assertThat(result.getAttributeType(), equalTo(personAttributeType));
		assertThat(result.getValue(), equalTo("true"));
	}
	
	@Test
	public void shouldTranslateReferenceTypeExtensionToPersonAttribute() {
		Extension personAttributeTypeExtension = new Extension();
		personAttributeTypeExtension.setUrl(FhirConstants.OPENMRS_FHIR_EXT_PERSON_ATTRIBUTE_TYPE);
		personAttributeTypeExtension.setValue(new StringType(ATTRIBUTE_TYPE_NAME));
		
		Extension personAttributeValueExtension = new Extension();
		personAttributeValueExtension.setUrl(FhirConstants.OPENMRS_FHIR_EXT_PERSON_ATTRIBUTE_VALUE);
		
		Reference reference = new Reference();
		reference.setReference("Location/" + LOCATION_ATTRIBUTE_UUID_VALUE);
		personAttributeValueExtension.setValue(reference);
		
		Extension extension = new Extension();
		extension.setUrl(FhirConstants.OPENMRS_FHIR_EXT_PERSON_ATTRIBUTE);
		extension.addExtension(personAttributeTypeExtension);
		extension.addExtension(personAttributeValueExtension);
		
		Location location = new Location();
		location.setName(LOCATION_NAME);
		location.setId(LOCATION_ATTRIBUTE_ID);
		location.setUuid(LOCATION_ATTRIBUTE_UUID_VALUE);
		
		when(locationService.getByUuid(LOCATION_ATTRIBUTE_UUID_VALUE)).thenReturn(location);
		
		PersonAttribute result = personAttributeTranslator.toOpenmrsType(extension);
		
		assertThat(result, notNullValue());
		assertThat(result.getAttributeType(), equalTo(personAttributeType));
		assertThat(result.getValue(), equalTo(LOCATION_ATTRIBUTE_ID.toString()));
	}
	
	@Test
	public void shouldTranslateCodeableConceptTypeExtensionToPersonAttribute() {
		Extension personAttributeTypeExtension = new Extension();
		personAttributeTypeExtension.setUrl(FhirConstants.OPENMRS_FHIR_EXT_PERSON_ATTRIBUTE_TYPE);
		personAttributeTypeExtension.setValue(new StringType(ATTRIBUTE_TYPE_NAME));
		
		Extension personAttributeValueExtension = new Extension();
		personAttributeValueExtension.setUrl(FhirConstants.OPENMRS_FHIR_EXT_PERSON_ATTRIBUTE_VALUE);
		
		CodeableConcept codeableConcept = new CodeableConcept();
		codeableConcept.setText("Test");
		personAttributeValueExtension.setValue(codeableConcept);
		
		Extension extension = new Extension();
		extension.setUrl(FhirConstants.OPENMRS_FHIR_EXT_PERSON_ATTRIBUTE);
		extension.addExtension(personAttributeTypeExtension);
		extension.addExtension(personAttributeValueExtension);
		
		Concept concept = new Concept();
		concept.setConceptId(Integer.parseInt(CONCEPT_ATTRIBUTE_VALUE));
		when(conceptTranslator.toOpenmrsType(codeableConcept)).thenReturn(concept);
		
		PersonAttribute result = personAttributeTranslator.toOpenmrsType(extension);
		
		assertThat(result, notNullValue());
		assertThat(result.getAttributeType(), equalTo(personAttributeType));
		assertThat(result.getValue(), equalTo(CONCEPT_ATTRIBUTE_VALUE));
	}
	
	@Test
	public void shouldReturnNullWhenPersonAttributeIsVoided() {
		personAttribute.setVoided(true);
		Extension result = personAttributeTranslator.toFhirResource(personAttribute);
		assertThat(result, equalTo(null));
	}
	
	@Test
	public void shouldReturnNullWhenConceptIdIsNotAnInteger() {
		CodeableConcept result = personAttributeTranslator.buildCodeableConcept("notAnInt");
		assertThat(result, equalTo(null));
	}
	
}
