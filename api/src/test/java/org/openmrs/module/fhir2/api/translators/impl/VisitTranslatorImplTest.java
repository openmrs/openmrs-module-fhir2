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
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.exparity.hamcrest.date.DateMatchers;
import org.hamcrest.CoreMatchers;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Encounter;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.Provenance;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.Resource;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifier;
import org.openmrs.PatientIdentifierType;
import org.openmrs.PersonName;
import org.openmrs.Visit;
import org.openmrs.VisitType;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.api.mappings.EncounterClassMap;
import org.openmrs.module.fhir2.api.translators.EncounterLocationTranslator;
import org.openmrs.module.fhir2.api.translators.PatientReferenceTranslator;
import org.openmrs.module.fhir2.api.translators.ProvenanceTranslator;
import org.openmrs.module.fhir2.api.util.FhirUtils;

@RunWith(MockitoJUnitRunner.class)
public class VisitTranslatorImplTest {
	
	private static final String VISIT_UUID = "65aefd46-973d-4526-89de-93842c80ad11";
	
	private static final String PATIENT_UUID = "98aefd46-973d-6726-89de-93842c80ad34";
	
	private static final String GIVEN_NAME = "Nicky";
	
	private static final String FAMILY_NAME = "sanchez";
	
	private static final String PATIENT_URI = FhirConstants.PATIENT + "/" + PATIENT_UUID;
	
	private static final String PATIENT_IDENTIFIER = "100034D-W";
	
	private static final String LOCATION_UUID = "276379ef-07ce-4108-b5e0-c4dc21964b4f";
	
	private static final String TEST_FHIR_CLASS = "Test Class";
	
	private static final String TYPE_CODE = "encounter-type-code";
	
	private static final String TYPE_DISPLAY = "Visit";
	
	private static final String VISIT_TYPE_CODE = "Visit";
	
	@Mock
	private EncounterLocationTranslator encounterLocationTranslator;
	
	@Mock
	private PatientReferenceTranslator patientReferenceTranslator;
	
	@Mock
	private ProvenanceTranslator<org.openmrs.Visit> provenanceTranslator;
	
	@Mock
	private VisitTypeTranslatorImpl visitTypeTranslator;
	
	@Mock
	private EncounterClassMap encounterClassMap;
	
	private VisitTranslatorImpl visitTranslator;
	
	@Before
	public void setup() {
		visitTranslator = new VisitTranslatorImpl();
		visitTranslator.setEncounterLocationTranslator(encounterLocationTranslator);
		visitTranslator.setPatientReferenceTranslator(patientReferenceTranslator);
		visitTranslator.setProvenanceTranslator(provenanceTranslator);
		visitTranslator.setEncounterClassMap(encounterClassMap);
		visitTranslator.setVisitTypeTranslator(visitTypeTranslator);
	}
	
	@Test
	public void toFhirResource_shouldConvertUuidToId() {
		Visit visit = new Visit();
		visit.setUuid(VISIT_UUID);
		
		Encounter result = visitTranslator.toFhirResource(visit);
		
		assertThat(result.getId(), equalTo(VISIT_UUID));
	}
	
	@Test
	public void toFhirResource_shouldConvertStatus() {
		Visit visit = new Visit();
		
		Encounter result = visitTranslator.toFhirResource(visit);
		
		assertThat(result.getStatus(), is(Encounter.EncounterStatus.UNKNOWN));
	}
	
	@Test
	public void toOpenmrsType_shouldConvertIdToUuid() {
		Encounter encounter = new Encounter();
		encounter.setId(VISIT_UUID);
		
		Visit result = visitTranslator.toOpenmrsType(encounter);
		
		assertThat(result.getUuid(), equalTo(VISIT_UUID));
	}
	
	@Test(expected = NullPointerException.class)
	public void toFhirResource_shouldThrowExceptionWhenVisitIsNull() {
		visitTranslator.toFhirResource(null);
	}
	
	@Test(expected = NullPointerException.class)
	public void toOpenMrsType_shouldThrowExceptionWhenEncounterIsNull() {
		visitTranslator.toOpenmrsType(null);
	}
	
	@Test(expected = NullPointerException.class)
	public void toOpenMrsType_shouldThrowExceptionWhenExistingVisitIsNull() {
		visitTranslator.toOpenmrsType(null, new Encounter());
	}
	
	@Test
	public void toOpenMrsType_shouldTranslateToOpenMrsPatient() {
		Reference patientRef = new Reference();
		patientRef.setReference(PATIENT_URI);
		
		Encounter encounter = new Encounter();
		encounter.setId(VISIT_UUID);
		encounter.setSubject(patientRef);
		
		Patient patient = new Patient();
		patient.setUuid(PATIENT_UUID);
		when(patientReferenceTranslator.toOpenmrsType(patientRef)).thenReturn(patient);
		org.openmrs.Visit result = visitTranslator.toOpenmrsType(encounter);
		assertThat(result, notNullValue());
		assertThat(result.getPatient(), notNullValue());
		assertThat(result.getPatient().getUuid(), notNullValue());
		assertThat(result.getPatient().getUuid(), equalTo(PATIENT_UUID));
	}
	
	@Test
	public void toFhirResource_shouldTranslateToSubject() {
		Reference patientRef = new Reference();
		patientRef.setReference(PATIENT_URI);
		PatientIdentifier identifier = new PatientIdentifier();
		identifier.setIdentifier(PATIENT_IDENTIFIER);
		
		PatientIdentifierType identifierType = new PatientIdentifierType();
		identifierType.setName("Test patient identifier type");
		identifier.setIdentifierType(identifierType);
		
		PersonName name = new PersonName();
		name.setGivenName(GIVEN_NAME);
		name.setFamilyName(FAMILY_NAME);
		Patient patient = new Patient();
		patient.setUuid(PATIENT_UUID);
		patient.addIdentifier(identifier);
		patient.addName(name);
		
		Visit visit = new Visit();
		visit.setUuid(VISIT_UUID);
		visit.setPatient(patient);
		
		when(patientReferenceTranslator.toFhirResource(patient)).thenReturn(patientRef);
		Encounter result = visitTranslator.toFhirResource(visit);
		
		assertThat(result, notNullValue());
		assertThat(result.getSubject(), notNullValue());
		assertThat(result.getSubject().getReference(), equalTo(PATIENT_URI));
	}
	
	@Test
	public void toFhirResource_shouldTranslateToEncounterClassFhirType() {
		when(encounterClassMap.getFhirClass(LOCATION_UUID)).thenReturn(TEST_FHIR_CLASS);
		
		Location location = new Location();
		location.setUuid(LOCATION_UUID);
		
		Visit visit = new Visit();
		visit.setUuid(VISIT_UUID);
		visit.setLocation(location);
		
		Encounter result = visitTranslator.toFhirResource(visit);
		
		assertThat(result, notNullValue());
		assertThat(result.getClass_(), notNullValue());
		assertThat(result.getClass_().getSystem(), CoreMatchers.is(FhirConstants.ENCOUNTER_CLASS_VALUE_SET_URI));
		assertThat(result.getClass_().getCode(), CoreMatchers.is(TEST_FHIR_CLASS));
	}
	
	@Test
	public void toFhirResource_shouldTranslateToEncounterDefaultClassFhirType() {
		Visit visit = new Visit();
		visit.setUuid(VISIT_UUID);
		visit.setLocation(null);
		
		Encounter result = visitTranslator.toFhirResource(visit);
		
		assertThat(result, notNullValue());
		assertThat(result.getClass_(), notNullValue());
		assertThat(result.getClass_().getSystem(), CoreMatchers.is(FhirConstants.ENCOUNTER_CLASS_VALUE_SET_URI));
		assertThat(result.getClass_().getCode(), CoreMatchers.is("AMB"));
	}
	
	@Test
	public void toFhirResource_shouldTranslateToLastUpdatedDate() {
		Visit visit = new Visit();
		visit.setDateChanged(new Date());
		
		Encounter result = visitTranslator.toFhirResource(visit);
		assertThat(result, notNullValue());
		assertThat(result.getMeta().getLastUpdated(), DateMatchers.sameDay(new Date()));
	}
	
	@Test
	public void toFhirResource_shouldAddProvenances() {
		Visit visit = new Visit();
		visit.setUuid(VISIT_UUID);
		
		Provenance provenance = new Provenance();
		provenance.setId(new IdType(FhirUtils.newUuid()));
		provenance.setRecorded(new Date());
		
		when(provenanceTranslator.getCreateProvenance(visit)).thenReturn(provenance);
		when(provenanceTranslator.getUpdateProvenance(visit)).thenReturn(provenance);
		
		Encounter result = visitTranslator.toFhirResource(visit);
		List<Resource> resources = result.getContained();
		assertThat(resources, notNullValue());
		assertThat(resources, not(empty()));
		assertThat(resources.stream().findAny().isPresent(), CoreMatchers.is(true));
		assertThat(resources.stream().findAny().get().isResource(), CoreMatchers.is(true));
		assertThat(resources.stream().findAny().get().getResourceType().name(), equalTo(Provenance.class.getSimpleName()));
	}
	
	@Test
	public void toOpenMrsType_shouldTranslateTypeToVisitType() {
		Encounter encounter = new Encounter();
		encounter.setId(VISIT_UUID);
		
		VisitType visitType = new VisitType();
		visitType.setName(TYPE_DISPLAY);
		visitType.setUuid(TYPE_CODE);
		when(visitTypeTranslator.toOpenmrsType(ArgumentMatchers.any())).thenReturn(visitType);
		
		Visit result = visitTranslator.toOpenmrsType(encounter);
		
		assertThat(result, notNullValue());
		assertThat(result.getVisitType(), equalTo(visitType));
	}
	
	@Test
	public void toFhirResource_shouldTranslateTypeToEncounterType() {
		CodeableConcept codeableConcept = new CodeableConcept();
		codeableConcept.addCoding().setSystem(FhirConstants.VISIT_TYPE_SYSTEM_URI).setCode("1");
		
		Visit visit = new Visit();
		visit.setUuid(VISIT_UUID);
		
		when(visitTypeTranslator.toFhirResource(ArgumentMatchers.any()))
		        .thenReturn(Collections.singletonList(codeableConcept));
		
		Encounter result = visitTranslator.toFhirResource(visit);
		
		assertThat(result, notNullValue());
		assertThat(result.getType(), not(empty()));
		assertThat(result.getTypeFirstRep(), equalTo(codeableConcept));
	}
	
	@Test
	public void toFhirResource_shouldHaveEncounterTag() {
		Visit visit = new Visit();
		visit.setUuid(VISIT_UUID);
		
		Encounter result = visitTranslator.toFhirResource(visit);
		
		assertThat(result, notNullValue());
		assertThat(result.getMeta().getTag(), notNullValue());
		assertThat(result.getMeta().getTag().get(0).getSystem(), equalTo(FhirConstants.OPENMRS_FHIR_EXT_ENCOUNTER_TAG));
		assertThat(result.getMeta().getTag().get(0).getCode(), equalTo(VISIT_TYPE_CODE));
		assertThat(result.getMeta().getTag().get(0).getDisplay(), equalTo(TYPE_DISPLAY));
		
	}
}
