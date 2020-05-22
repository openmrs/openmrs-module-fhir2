/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.fhir2.providers.r3;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;
import static org.mockito.hamcrest.MockitoHamcrest.argThat;

import java.util.Collections;
import java.util.Date;
import java.util.List;

import ca.uhn.fhir.rest.param.DateRangeParam;
import ca.uhn.fhir.rest.param.StringAndListParam;
import ca.uhn.fhir.rest.param.StringOrListParam;
import ca.uhn.fhir.rest.param.StringParam;
import ca.uhn.fhir.rest.param.TokenAndListParam;
import ca.uhn.fhir.rest.param.TokenOrListParam;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.dstu3.model.IdType;
import org.hl7.fhir.dstu3.model.Patient;
import org.hl7.fhir.dstu3.model.Resource;
import org.hl7.fhir.r4.model.*;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openmrs.module.fhir2.api.FhirPatientService;

@RunWith(MockitoJUnitRunner.class)
public class PatientFhirR3ResourceProviderTest extends BaseFhirR3ProvenanceResourceTest<org.hl7.fhir.r4.model.Patient> {
	
	private static final String PATIENT_UUID = "017312a1-cf56-43ab-ae87-44070b801d1c";
	
	private static final String WRONG_PATIENT_UUID = "017312a1-cf56-43ab-ae87-44070b801d1c";
	
	private static final String NAME = "Rick";
	
	private static final String GIVEN_NAME = "Nihilism";
	
	private static final String FAMILY_NAME = "Sanchez";
	
	private static final String GENDER = "male";
	
	private static final String IDENTIFIER = "M10000";
	
	private static final String BIRTH_DATE = "1947-04-01";
	
	private static final String DEATH_DATE = "2019-12-15";
	
	private static final String CITY = "Seattle";
	
	private static final String STATE = "Washington";
	
	private static final String COUNTRY = "Washington";
	
	private static final String POSTAL_CODE = "98136";
	
	@Mock
	private FhirPatientService patientService;
	
	private PatientFhirResourceProvider patientFhirResourceProvider;
	
	private org.hl7.fhir.r4.model.Patient patient;
	
	@Before
	public void setup() {
		patientFhirResourceProvider = new PatientFhirResourceProvider();
		patientFhirResourceProvider.setPatientService(patientService);
	}
	
	@Before
	public void initPatient() {
		HumanName name = new HumanName();
		name.addGiven(GIVEN_NAME);
		name.setFamily(FAMILY_NAME);
		
		patient = new org.hl7.fhir.r4.model.Patient();
		patient.setId(PATIENT_UUID);
		patient.addName(name);
		patient.setActive(true);
		patient.setBirthDate(new Date());
		patient.setGender(Enumerations.AdministrativeGender.MALE);
		setProvenanceResources(patient);
	}
	
	@Test
	public void getResourceType_shouldReturnResourceType() {
		assertThat(patientFhirResourceProvider.getResourceType(), equalTo(Patient.class));
		assertThat(patientFhirResourceProvider.getResourceType().getName(), equalTo(Patient.class.getName()));
	}
	
	@Test
	public void getPatientById_shouldReturnPatient() {
		IdType id = new IdType();
		id.setValue(PATIENT_UUID);
		when(patientService.get(PATIENT_UUID)).thenReturn(patient);
		
		Patient result = patientFhirResourceProvider.getPatientById(id);
		
		assertThat(result.isResource(), is(true));
		assertThat(result, notNullValue());
		assertThat(result.getId(), notNullValue());
		assertThat(result.getId(), equalTo(PATIENT_UUID));
	}
	
	@Test(expected = ResourceNotFoundException.class)
	public void getPatientByWithWrongId_shouldThrowResourceNotFoundException() {
		IdType idType = new IdType();
		idType.setValue(WRONG_PATIENT_UUID);
		assertThat(patientFhirResourceProvider.getPatientById(idType).isResource(), is(true));
		assertThat(patientFhirResourceProvider.getPatientById(idType), nullValue());
	}
	
	@Test
	public void searchPatients_shouldReturnMatchingBundleOfPatientsByName() {
		StringAndListParam nameParam = new StringAndListParam().addAnd(new StringOrListParam().add(new StringParam(NAME)));
		when(patientService.searchForPatients(argThat(is(nameParam)), isNull(), isNull(), isNull(), isNull(), isNull(),
		    isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull()))
		            .thenReturn(Collections.singletonList(patient));
		
		Bundle results = patientFhirResourceProvider.searchPatients(nameParam, null, null, null, null, null, null, null,
		    null, null, null, null, null);
		
		assertThat(results, notNullValue());
		assertThat(results.isResource(), is(true));
		assertThat(results.getEntry().size(), greaterThanOrEqualTo(1));
	}
	
	@Test
	public void searchPatients_shouldReturnMatchingBundleOfPatientsByGivenName() {
		StringAndListParam givenNameParam = new StringAndListParam()
		        .addAnd(new StringOrListParam().add(new StringParam(NAME)));
		when(patientService.searchForPatients(isNull(), argThat(is(givenNameParam)), isNull(), isNull(), isNull(), isNull(),
		    isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull()))
		            .thenReturn(Collections.singletonList(patient));
		
		Bundle results = patientFhirResourceProvider.searchPatients(null, givenNameParam, null, null, null, null, null, null,
		    null, null, null, null, null);
		
		assertThat(results, notNullValue());
		assertThat(results.isResource(), is(true));
		assertThat(results.getEntry().size(), greaterThanOrEqualTo(1));
	}
	
	@Test
	public void searchPatients_shouldReturnMatchingBundleOfPatientsByFamilyName() {
		StringAndListParam familyNameParam = new StringAndListParam()
		        .addAnd(new StringOrListParam().add(new StringParam(FAMILY_NAME)));
		when(patientService.searchForPatients(isNull(), isNull(), argThat(is(familyNameParam)), isNull(), isNull(), isNull(),
		    isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull()))
		            .thenReturn(Collections.singletonList(patient));
		
		Bundle results = patientFhirResourceProvider.searchPatients(null, null, familyNameParam, null, null, null, null,
		    null, null, null, null, null, null);
		
		assertThat(results, notNullValue());
		assertThat(results.isResource(), is(true));
		assertThat(results.getEntry().size(), greaterThanOrEqualTo(1));
	}
	
	@Test
	public void searchPatients_shouldReturnMatchingBundleOfPatientsByIdentifier() {
		TokenAndListParam identifierParam = new TokenAndListParam().addAnd(new TokenOrListParam().add(IDENTIFIER));
		when(patientService.searchForPatients(isNull(), isNull(), isNull(), argThat(is(identifierParam)), isNull(), isNull(),
		    isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull()))
		            .thenReturn(Collections.singletonList(patient));
		
		Bundle results = patientFhirResourceProvider.searchPatients(null, null, null, identifierParam, null, null, null,
		    null, null, null, null, null, null);
		
		assertThat(results, notNullValue());
		assertThat(results.isResource(), is(true));
		assertThat(results.getEntry().size(), greaterThanOrEqualTo(1));
	}
	
	@Test
	public void searchForPatients_shouldReturnMatchingBundleOfPatientsByGender() {
		TokenAndListParam genderParam = new TokenAndListParam().addAnd(new TokenOrListParam().add(GENDER));
		when(patientService.searchForPatients(isNull(), isNull(), isNull(), isNull(), argThat(is(genderParam)), isNull(),
		    isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull()))
		            .thenReturn(Collections.singletonList(patient));
		
		Bundle results = patientFhirResourceProvider.searchPatients(null, null, null, null, genderParam, null, null, null,
		    null, null, null, null, null);
		
		assertThat(results, notNullValue());
		assertThat(results.isResource(), is(true));
		assertThat(results.getEntry().size(), greaterThanOrEqualTo(1));
	}
	
	@Test
	public void searchForPatients_shouldReturnMatchingBundleOfPatientsByBirthDate() {
		DateRangeParam birthDateParam = new DateRangeParam().setLowerBound(BIRTH_DATE).setUpperBound(BIRTH_DATE);
		when(patientService.searchForPatients(isNull(), isNull(), isNull(), isNull(), isNull(), argThat(is(birthDateParam)),
		    isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull()))
		            .thenReturn(Collections.singletonList(patient));
		
		Bundle results = patientFhirResourceProvider.searchPatients(null, null, null, null, null, birthDateParam, null, null,
		    null, null, null, null, null);
		
		assertThat(results, notNullValue());
		assertThat(results.isResource(), is(true));
		assertThat(results.getEntry().size(), greaterThanOrEqualTo(1));
	}
	
	@Test
	public void searchForPatients_shouldReturnMatchingBundleOfPatientsByDeathDate() {
		DateRangeParam deathDateParam = new DateRangeParam().setLowerBound(DEATH_DATE).setUpperBound(DEATH_DATE);
		when(patientService.searchForPatients(isNull(), isNull(), isNull(), isNull(), isNull(), isNull(),
		    argThat(is(deathDateParam)), isNull(), isNull(), isNull(), isNull(), isNull(), isNull()))
		            .thenReturn(Collections.singletonList(patient));
		
		Bundle results = patientFhirResourceProvider.searchPatients(null, null, null, null, null, null, deathDateParam, null,
		    null, null, null, null, null);
		
		assertThat(results, notNullValue());
		assertThat(results.isResource(), is(true));
		assertThat(results.getEntry().size(), greaterThanOrEqualTo(1));
	}
	
	@Test
	public void searchForPatients_shouldReturnMatchingBundleOfPatientsByDeceased() {
		TokenAndListParam deceasedParam = new TokenAndListParam().addAnd(new TokenOrListParam().add("true"));
		when(patientService.searchForPatients(isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(),
		    argThat(is(deceasedParam)), isNull(), isNull(), isNull(), isNull(), isNull()))
		            .thenReturn(Collections.singletonList(patient));
		
		Bundle results = patientFhirResourceProvider.searchPatients(null, null, null, null, null, null, null, deceasedParam,
		    null, null, null, null, null);
		
		assertThat(results, notNullValue());
		assertThat(results.isResource(), is(true));
		assertThat(results.getEntry().size(), greaterThanOrEqualTo(1));
	}
	
	@Test
	public void searchForPatients_shouldReturnMatchingBundleOfPatientsByCity() {
		StringAndListParam cityParam = new StringAndListParam().addAnd(new StringOrListParam().add(new StringParam(CITY)));
		when(patientService.searchForPatients(isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(),
		    argThat(is(cityParam)), isNull(), isNull(), isNull(), isNull())).thenReturn(Collections.singletonList(patient));
		
		Bundle results = patientFhirResourceProvider.searchPatients(null, null, null, null, null, null, null, null,
		    cityParam, null, null, null, null);
		
		assertThat(results, notNullValue());
		assertThat(results.isResource(), is(true));
		assertThat(results.getEntry().size(), greaterThanOrEqualTo(1));
	}
	
	@Test
	public void searchForPatients_shouldReturnMatchingBundleOfPatientsByState() {
		StringAndListParam stateParam = new StringAndListParam().addAnd(new StringOrListParam().add(new StringParam(STATE)));
		when(patientService.searchForPatients(isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(),
		    isNull(), argThat(is(stateParam)), isNull(), isNull(), isNull())).thenReturn(Collections.singletonList(patient));
		
		Bundle results = patientFhirResourceProvider.searchPatients(null, null, null, null, null, null, null, null, null,
		    stateParam, null, null, null);
		
		assertThat(results, notNullValue());
		assertThat(results.isResource(), is(true));
		assertThat(results.getEntry().size(), greaterThanOrEqualTo(1));
	}
	
	@Test
	public void searchForPatients_shouldReturnMatchingBundleOfPatientsByPostalCode() {
		StringAndListParam postalCodeParam = new StringAndListParam()
		        .addAnd(new StringOrListParam().add(new StringParam(POSTAL_CODE)));
		when(patientService.searchForPatients(isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(),
		    isNull(), isNull(), argThat(is(postalCodeParam)), isNull(), isNull()))
		            .thenReturn(Collections.singletonList(patient));
		
		Bundle results = patientFhirResourceProvider.searchPatients(null, null, null, null, null, null, null, null, null,
		    null, postalCodeParam, null, null);
		
		assertThat(results, notNullValue());
		assertThat(results.isResource(), is(true));
		assertThat(results.getEntry().size(), greaterThanOrEqualTo(1));
	}
	
	@Test
	public void searchForPatients_shouldReturnMatchingBundleOfPatientsByCountry() {
		StringAndListParam countryParam = new StringAndListParam()
		        .addAnd(new StringOrListParam().add(new StringParam(COUNTRY)));
		when(patientService.searchForPatients(isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(),
		    isNull(), isNull(), isNull(), argThat(is(countryParam)), isNull()))
		            .thenReturn(Collections.singletonList(patient));
		
		Bundle results = patientFhirResourceProvider.searchPatients(null, null, null, null, null, null, null, null, null,
		    null, null, countryParam, null);
		
		assertThat(results, notNullValue());
		assertThat(results.isResource(), is(true));
		assertThat(results.getEntry().size(), greaterThanOrEqualTo(1));
	}
	
	@Test
	public void getPatientResourceHistory_shouldReturnListOfResource() {
		IdType id = new IdType();
		id.setValue(PATIENT_UUID);
		when(patientService.get(PATIENT_UUID)).thenReturn(patient);
		
		List<Resource> resources = patientFhirResourceProvider.getPatientHistoryById(id);
		assertThat(resources, notNullValue());
		assertThat(resources, not(empty()));
		assertThat(resources.size(), equalTo(2));
	}
	
	@Test
	public void getPatientResourceHistory_shouldReturnProvenanceResources() {
		IdType id = new IdType();
		id.setValue(PATIENT_UUID);
		when(patientService.get(PATIENT_UUID)).thenReturn(patient);
		
		List<Resource> resources = patientFhirResourceProvider.getPatientHistoryById(id);
		assertThat(resources, not(empty()));
		assertThat(resources.stream().findAny().isPresent(), is(true));
		assertThat(resources.stream().findAny().get().getResourceType().name(), equalTo(Provenance.class.getSimpleName()));
	}
	
	@Test(expected = ResourceNotFoundException.class)
	public void getPatientHistoryByWithWrongId_shouldThrowResourceNotFoundException() {
		IdType idType = new IdType();
		idType.setValue(WRONG_PATIENT_UUID);
		assertThat(patientFhirResourceProvider.getPatientHistoryById(idType).isEmpty(), is(true));
		assertThat(patientFhirResourceProvider.getPatientHistoryById(idType).size(), equalTo(0));
	}
}
