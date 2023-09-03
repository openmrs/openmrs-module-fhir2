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
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

import ca.uhn.fhir.model.api.Include;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.api.server.IBundleProvider;
import ca.uhn.fhir.rest.param.DateRangeParam;
import ca.uhn.fhir.rest.param.StringAndListParam;
import ca.uhn.fhir.rest.param.StringOrListParam;
import ca.uhn.fhir.rest.param.StringParam;
import ca.uhn.fhir.rest.param.TokenAndListParam;
import ca.uhn.fhir.rest.param.TokenOrListParam;
import ca.uhn.fhir.rest.param.TokenParam;
import ca.uhn.fhir.rest.server.exceptions.InvalidRequestException;
import ca.uhn.fhir.rest.server.exceptions.MethodNotAllowedException;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import org.hl7.fhir.convertors.factory.VersionConvertorFactory_30_40;
import org.hl7.fhir.dstu3.model.IdType;
import org.hl7.fhir.dstu3.model.OperationOutcome;
import org.hl7.fhir.dstu3.model.Patient;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.Enumerations;
import org.hl7.fhir.r4.model.HumanName;
import org.hl7.fhir.r4.model.Observation;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.api.FhirPatientService;
import org.openmrs.module.fhir2.api.search.param.PatientSearchParams;

@RunWith(MockitoJUnitRunner.class)
public class PatientFhirResourceProviderTest extends BaseFhirR3ProvenanceResourceTest<org.hl7.fhir.r4.model.Patient> {
	
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
	
	private static final String LAST_UPDATED_DATE = "2020-09-03";
	
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
	public void createPatient_shouldCreateNewPatient() {
		when(patientService.create(any(org.hl7.fhir.r4.model.Patient.class))).thenReturn(patient);
		
		MethodOutcome result = patientFhirResourceProvider.createPatient(
				(Patient) VersionConvertorFactory_30_40.convertResource(patient));
		
		assertThat(result, notNullValue());
		assertThat(result.getCreated(), is(true));
		assertThat(result.getResource(), notNullValue());
		assertThat(result.getResource().getIdElement().getIdPart(), equalTo(PATIENT_UUID));
	}
	
	@Test
	public void updatePatient_shouldUpdateRequestedPatient() {
		when(patientService.update(eq(PATIENT_UUID), any(org.hl7.fhir.r4.model.Patient.class))).thenReturn(patient);
		
		MethodOutcome result = patientFhirResourceProvider.updatePatient(new IdType().setValue(PATIENT_UUID),
				(Patient) VersionConvertorFactory_30_40.convertResource(patient));
		
		assertThat(result, notNullValue());
		assertThat(result.getResource(), notNullValue());
		assertThat(result.getResource().getIdElement().getIdPart(), equalTo(PATIENT_UUID));
	}
	
	@Test(expected = InvalidRequestException.class)
	public void updatePatient_shouldThrowInvalidRequestForUuidMismatch() {
		when(patientService.update(eq(WRONG_PATIENT_UUID), any(org.hl7.fhir.r4.model.Patient.class)))
		        .thenThrow(InvalidRequestException.class);
		
		patientFhirResourceProvider.updatePatient(new IdType().setValue(WRONG_PATIENT_UUID),
				(Patient) VersionConvertorFactory_30_40.convertResource(patient));
	}
	
	@Test(expected = InvalidRequestException.class)
	public void updatePatient_shouldThrowInvalidRequestForMissingId() {
		org.hl7.fhir.r4.model.Patient noIdPatient = new org.hl7.fhir.r4.model.Patient();
		
		when(patientService.update(eq(PATIENT_UUID), any(org.hl7.fhir.r4.model.Patient.class)))
		        .thenThrow(InvalidRequestException.class);
		
		patientFhirResourceProvider.updatePatient(new IdType().setValue(PATIENT_UUID),
				(Patient) VersionConvertorFactory_30_40.convertResource(noIdPatient));
	}
	
	@Test(expected = MethodNotAllowedException.class)
	public void updatePatient_shouldThrowMethodNotAllowedIfDoesNotExist() {
		org.hl7.fhir.r4.model.Patient wrongPatient = new org.hl7.fhir.r4.model.Patient();
		wrongPatient.setId(WRONG_PATIENT_UUID);
		
		when(patientService.update(eq(WRONG_PATIENT_UUID), any(org.hl7.fhir.r4.model.Patient.class)))
		        .thenThrow(MethodNotAllowedException.class);
		
		patientFhirResourceProvider.updatePatient(new IdType().setValue(WRONG_PATIENT_UUID),
				(Patient) VersionConvertorFactory_30_40.convertResource(wrongPatient));
	}
	
	@Test
	public void deletePatient_shouldDeleteRequestedPatient() {
		OperationOutcome result = patientFhirResourceProvider.deletePatient(new IdType().setValue(PATIENT_UUID));
		
		assertThat(result, notNullValue());
		assertThat(result.getIssue(), notNullValue());
		assertThat(result.getIssueFirstRep().getSeverity(), equalTo(OperationOutcome.IssueSeverity.INFORMATION));
		assertThat(result.getIssueFirstRep().getDetails().getCodingFirstRep().getCode(), equalTo("MSG_DELETED"));
		assertThat(result.getIssueFirstRep().getDetails().getCodingFirstRep().getDisplay(),
		    equalTo("This resource has been deleted"));
	}
	
	@Test
	public void searchPatients_shouldReturnMatchingBundleOfPatientsByName() {
		StringAndListParam nameParam = new StringAndListParam().addAnd(new StringOrListParam().add(new StringParam(NAME)));
		
		when(patientService.searchForPatients(new PatientSearchParams(nameParam, null, null, null, null, null, null, null,
		        null, null, null, null, null, null, null, null)))
		                .thenReturn(new MockIBundleProvider<>(Collections.singletonList(patient), 10, 1));
		
		IBundleProvider results = patientFhirResourceProvider.searchPatients(nameParam, null, null, null, null, null, null,
		    null, null, null, null, null, null, null, null, null);
		List<IBaseResource> resources = getResources(results);
		
		assertThat(resources, notNullValue());
		assertThat(resources, hasSize(equalTo(1)));
		assertThat(resources.get(0).fhirType(), is(FhirConstants.PATIENT));
		assertThat(resources.get(0).getIdElement().getIdPart(), is(PATIENT_UUID));
	}
	
	@Test
	public void searchPatients_shouldReturnMatchingBundleOfPatientsByGivenName() {
		StringAndListParam givenNameParam = new StringAndListParam()
		        .addAnd(new StringOrListParam().add(new StringParam(NAME)));
		when(patientService.searchForPatients(new PatientSearchParams(null, givenNameParam, null, null, null, null, null,
		        null, null, null, null, null, null, null, null, null)))
		                .thenReturn(new MockIBundleProvider<>(Collections.singletonList(patient), 10, 1));
		
		IBundleProvider results = patientFhirResourceProvider.searchPatients(null, givenNameParam, null, null, null, null,
		    null, null, null, null, null, null, null, null, null, null);
		List<IBaseResource> resources = getResources(results);
		
		assertThat(resources, notNullValue());
		assertThat(resources, hasSize(equalTo(1)));
		assertThat(resources.get(0).fhirType(), is(FhirConstants.PATIENT));
		assertThat(resources.get(0).getIdElement().getIdPart(), is(PATIENT_UUID));
	}
	
	@Test
	public void searchPatients_shouldReturnMatchingBundleOfPatientsByFamilyName() {
		StringAndListParam familyNameParam = new StringAndListParam()
		        .addAnd(new StringOrListParam().add(new StringParam(FAMILY_NAME)));
		when(patientService.searchForPatients(new PatientSearchParams(null, null, familyNameParam, null, null, null, null,
		        null, null, null, null, null, null, null, null, null)))
		                .thenReturn(new MockIBundleProvider<>(Collections.singletonList(patient), 10, 1));
		
		IBundleProvider results = patientFhirResourceProvider.searchPatients(null, null, familyNameParam, null, null, null,
		    null, null, null, null, null, null, null, null, null, null);
		List<IBaseResource> resources = getResources(results);
		
		assertThat(resources, notNullValue());
		assertThat(resources, hasSize(equalTo(1)));
		assertThat(resources.get(0).fhirType(), is(FhirConstants.PATIENT));
		assertThat(resources.get(0).getIdElement().getIdPart(), is(PATIENT_UUID));
	}
	
	@Test
	public void searchPatients_shouldReturnMatchingBundleOfPatientsByIdentifier() {
		TokenAndListParam identifierParam = new TokenAndListParam().addAnd(new TokenOrListParam().add(IDENTIFIER));
		when(patientService.searchForPatients(new PatientSearchParams(null, null, null, identifierParam, null, null, null,
		        null, null, null, null, null, null, null, null, null)))
		                .thenReturn(new MockIBundleProvider<>(Collections.singletonList(patient), 10, 1));
		
		IBundleProvider results = patientFhirResourceProvider.searchPatients(null, null, null, identifierParam, null, null,
		    null, null, null, null, null, null, null, null, null, null);
		List<IBaseResource> resources = getResources(results);
		
		assertThat(resources, notNullValue());
		assertThat(resources, hasSize(equalTo(1)));
		assertThat(resources.get(0).fhirType(), is(FhirConstants.PATIENT));
		assertThat(resources.get(0).getIdElement().getIdPart(), is(PATIENT_UUID));
	}
	
	@Test
	public void searchForPatients_shouldReturnMatchingBundleOfPatientsByGender() {
		TokenAndListParam genderParam = new TokenAndListParam().addAnd(new TokenOrListParam().add(GENDER));
		when(patientService.searchForPatients(new PatientSearchParams(null, null, null, null, genderParam, null, null, null,
		        null, null, null, null, null, null, null, null)))
		                .thenReturn(new MockIBundleProvider<>(Collections.singletonList(patient), 10, 1));
		
		IBundleProvider results = patientFhirResourceProvider.searchPatients(null, null, null, null, genderParam, null, null,
		    null, null, null, null, null, null, null, null, null);
		List<IBaseResource> resources = getResources(results);
		
		assertThat(resources, notNullValue());
		assertThat(resources, hasSize(equalTo(1)));
		assertThat(resources.get(0).fhirType(), is(FhirConstants.PATIENT));
		assertThat(resources.get(0).getIdElement().getIdPart(), is(PATIENT_UUID));
	}
	
	@Test
	public void searchForPatients_shouldReturnMatchingBundleOfPatientsByBirthDate() {
		DateRangeParam birthDateParam = new DateRangeParam().setLowerBound(BIRTH_DATE).setUpperBound(BIRTH_DATE);
		when(patientService.searchForPatients(new PatientSearchParams(null, null, null, null, null, birthDateParam, null,
		        null, null, null, null, null, null, null, null, null)))
		                .thenReturn(new MockIBundleProvider<>(Collections.singletonList(patient), 10, 1));
		
		IBundleProvider results = patientFhirResourceProvider.searchPatients(null, null, null, null, null, birthDateParam,
		    null, null, null, null, null, null, null, null, null, null);
		List<IBaseResource> resources = getResources(results);
		
		assertThat(resources, notNullValue());
		assertThat(resources, hasSize(equalTo(1)));
		assertThat(resources.get(0).fhirType(), is(FhirConstants.PATIENT));
		assertThat(resources.get(0).getIdElement().getIdPart(), is(PATIENT_UUID));
	}
	
	@Test
	public void searchForPatients_shouldReturnMatchingBundleOfPatientsByDeathDate() {
		DateRangeParam deathDateParam = new DateRangeParam().setLowerBound(DEATH_DATE).setUpperBound(DEATH_DATE);
		when(patientService.searchForPatients(new PatientSearchParams(null, null, null, null, null, null, deathDateParam,
		        null, null, null, null, null, null, null, null, null)))
		                .thenReturn(new MockIBundleProvider<>(Collections.singletonList(patient), 10, 1));
		
		IBundleProvider results = patientFhirResourceProvider.searchPatients(null, null, null, null, null, null,
		    deathDateParam, null, null, null, null, null, null, null, null, null);
		List<IBaseResource> resources = getResources(results);
		
		assertThat(resources, notNullValue());
		assertThat(resources, hasSize(equalTo(1)));
		assertThat(resources.get(0).fhirType(), is(FhirConstants.PATIENT));
		assertThat(resources.get(0).getIdElement().getIdPart(), is(PATIENT_UUID));
	}
	
	@Test
	public void searchForPatients_shouldReturnMatchingBundleOfPatientsByDeceased() {
		TokenAndListParam deceasedParam = new TokenAndListParam().addAnd(new TokenOrListParam().add("true"));
		when(patientService.searchForPatients(new PatientSearchParams(null, null, null, null, null, null, null,
		        deceasedParam, null, null, null, null, null, null, null, null)))
		                .thenReturn(new MockIBundleProvider<>(Collections.singletonList(patient), 10, 1));
		
		IBundleProvider results = patientFhirResourceProvider.searchPatients(null, null, null, null, null, null, null,
		    deceasedParam, null, null, null, null, null, null, null, null);
		List<IBaseResource> resources = getResources(results);
		
		assertThat(resources, notNullValue());
		assertThat(resources, hasSize(equalTo(1)));
		assertThat(resources.get(0).fhirType(), is(FhirConstants.PATIENT));
		assertThat(resources.get(0).getIdElement().getIdPart(), is(PATIENT_UUID));
	}
	
	@Test
	public void searchForPatients_shouldReturnMatchingBundleOfPatientsByCity() {
		StringAndListParam cityParam = new StringAndListParam().addAnd(new StringOrListParam().add(new StringParam(CITY)));
		when(patientService.searchForPatients(new PatientSearchParams(null, null, null, null, null, null, null, null,
		        cityParam, null, null, null, null, null, null, null)))
		                .thenReturn(new MockIBundleProvider<>(Collections.singletonList(patient), 10, 1));
		
		IBundleProvider results = patientFhirResourceProvider.searchPatients(null, null, null, null, null, null, null, null,
		    cityParam, null, null, null, null, null, null, null);
		List<IBaseResource> resources = getResources(results);
		
		assertThat(resources, notNullValue());
		assertThat(resources, hasSize(equalTo(1)));
		assertThat(resources.get(0).fhirType(), is(FhirConstants.PATIENT));
		assertThat(resources.get(0).getIdElement().getIdPart(), is(PATIENT_UUID));
	}
	
	@Test
	public void searchForPatients_shouldReturnMatchingBundleOfPatientsByState() {
		StringAndListParam stateParam = new StringAndListParam().addAnd(new StringOrListParam().add(new StringParam(STATE)));
		when(patientService.searchForPatients(new PatientSearchParams(null, null, null, null, null, null, null, null, null,
		        stateParam, null, null, null, null, null, null)))
		                .thenReturn(new MockIBundleProvider<>(Collections.singletonList(patient), 10, 1));
		
		IBundleProvider results = patientFhirResourceProvider.searchPatients(null, null, null, null, null, null, null, null,
		    null, stateParam, null, null, null, null, null, null);
		List<IBaseResource> resources = getResources(results);
		
		assertThat(resources, notNullValue());
		assertThat(resources, hasSize(equalTo(1)));
		assertThat(resources.get(0).fhirType(), is(FhirConstants.PATIENT));
		assertThat(resources.get(0).getIdElement().getIdPart(), is(PATIENT_UUID));
	}
	
	@Test
	public void searchForPatients_shouldReturnMatchingBundleOfPatientsByPostalCode() {
		StringAndListParam postalCodeParam = new StringAndListParam()
		        .addAnd(new StringOrListParam().add(new StringParam(POSTAL_CODE)));
		when(patientService.searchForPatients(new PatientSearchParams(null, null, null, null, null, null, null, null, null,
		        null, postalCodeParam, null, null, null, null, null)))
		                .thenReturn(new MockIBundleProvider<>(Collections.singletonList(patient), 10, 1));
		
		IBundleProvider results = patientFhirResourceProvider.searchPatients(null, null, null, null, null, null, null, null,
		    null, null, postalCodeParam, null, null, null, null, null);
		List<IBaseResource> resources = getResources(results);
		
		assertThat(resources, notNullValue());
		assertThat(resources, hasSize(equalTo(1)));
		assertThat(resources.get(0).fhirType(), is(FhirConstants.PATIENT));
		assertThat(resources.get(0).getIdElement().getIdPart(), is(PATIENT_UUID));
	}
	
	@Test
	public void searchForPatients_shouldReturnMatchingBundleOfPatientsByCountry() {
		StringAndListParam countryParam = new StringAndListParam()
		        .addAnd(new StringOrListParam().add(new StringParam(COUNTRY)));
		when(patientService.searchForPatients(new PatientSearchParams(null, null, null, null, null, null, null, null, null,
		        null, null, countryParam, null, null, null, null)))
		                .thenReturn(new MockIBundleProvider<>(Collections.singletonList(patient), 10, 1));
		
		IBundleProvider results = patientFhirResourceProvider.searchPatients(null, null, null, null, null, null, null, null,
		    null, null, null, countryParam, null, null, null, null);
		List<IBaseResource> resources = getResources(results);
		
		assertThat(resources, notNullValue());
		assertThat(resources, hasSize(equalTo(1)));
		assertThat(resources.get(0).fhirType(), is(FhirConstants.PATIENT));
		assertThat(resources.get(0).getIdElement().getIdPart(), is(PATIENT_UUID));
	}
	
	@Test
	public void searchForPatients_shouldReturnMatchingBundleOfPatientsByUUID() {
		TokenAndListParam uuid = new TokenAndListParam().addAnd(new TokenParam(PATIENT_UUID));
		
		when(patientService.searchForPatients(new PatientSearchParams(null, null, null, null, null, null, null, null, null,
		        null, null, null, uuid, null, null, null)))
		                .thenReturn(new MockIBundleProvider<>(Collections.singletonList(patient), 10, 1));
		
		IBundleProvider results = patientFhirResourceProvider.searchPatients(null, null, null, null, null, null, null, null,
		    null, null, null, null, uuid, null, null, null);
		List<IBaseResource> resources = getResources(results);
		
		assertThat(resources, notNullValue());
		assertThat(resources, hasSize(equalTo(1)));
		assertThat(resources.get(0).fhirType(), is(FhirConstants.PATIENT));
		assertThat(resources.get(0).getIdElement().getIdPart(), is(PATIENT_UUID));
	}
	
	@Test
	public void searchForPatients_shouldReturnMatchingBundleOfPatientsByLastUpdated() {
		DateRangeParam lastUpdated = new DateRangeParam().setLowerBound(LAST_UPDATED_DATE).setUpperBound(LAST_UPDATED_DATE);
		
		when(patientService.searchForPatients(new PatientSearchParams(null, null, null, null, null, null, null, null, null,
		        null, null, null, null, lastUpdated, null, null)))
		                .thenReturn(new MockIBundleProvider<>(Collections.singletonList(patient), 10, 1));
		
		IBundleProvider results = patientFhirResourceProvider.searchPatients(null, null, null, null, null, null, null, null,
		    null, null, null, null, null, lastUpdated, null, null);
		List<IBaseResource> resources = getResources(results);
		
		assertThat(resources, notNullValue());
		assertThat(resources, hasSize(equalTo(1)));
		assertThat(resources.get(0).fhirType(), is(FhirConstants.PATIENT));
		assertThat(resources.get(0).getIdElement().getIdPart(), is(PATIENT_UUID));
	}
	
	@Test
	public void searchForPatients_shouldAddRelatedResourcesForRevInclude() {
		HashSet<Include> revIncludes = new HashSet<>();
		revIncludes.add(new Include("Observation:patient"));
		
		when(patientService.searchForPatients(new PatientSearchParams(null, null, null, null, null, null, null, null, null,
		        null, null, null, null, null, null, revIncludes)))
		                .thenReturn(new MockIBundleProvider<>(Arrays.asList(patient, new Observation()), 10, 1));
		
		IBundleProvider results = patientFhirResourceProvider.searchPatients(null, null, null, null, null, null, null, null,
		    null, null, null, null, null, null, null, revIncludes);
		
		List<IBaseResource> resultList = getResources(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList.size(), greaterThanOrEqualTo(2));
		assertThat(resultList.get(0).fhirType(), equalTo(FhirConstants.PATIENT));
		assertThat(resultList.get(1).fhirType(), equalTo(FhirConstants.OBSERVATION));
		assertThat(((Patient) resultList.iterator().next()).getId(), equalTo(PATIENT_UUID));
	}
	
	@Test
	public void searchForPatients_shouldNotAddResourcesForEmptyRevInclude() {
		when(patientService.searchForPatients(new PatientSearchParams(null, null, null, null, null, null, null, null, null,
		        null, null, null, null, null, null, null)))
		                .thenReturn(new MockIBundleProvider<>(Collections.singletonList(patient), 10, 1));
		
		HashSet<Include> revIncludes = new HashSet<>();
		
		IBundleProvider results = patientFhirResourceProvider.searchPatients(null, null, null, null, null, null, null, null,
		    null, null, null, null, null, null, null, revIncludes);
		
		List<IBaseResource> resultList = getResources(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList.size(), equalTo(1));
		assertThat(resultList.get(0).fhirType(), equalTo(FhirConstants.PATIENT));
		assertThat(((Patient) resultList.iterator().next()).getId(), equalTo(PATIENT_UUID));
	}
	
	@Test
	public void searchForPatients_shouldReturnPatientEverything() {
		when(patientService.getPatientEverything(any()))
		        .thenReturn(new MockIBundleProvider<>(Collections.singletonList(patient), 10, 1));
		
		IBundleProvider results = patientFhirResourceProvider.getPatientEverything(new IdType(PATIENT_UUID));
		
		List<IBaseResource> resultList = getAllResources(results);
		
		assertThat(resultList, notNullValue());
		assertThat(resultList.size(), equalTo(1));
		assertThat(resultList.get(0).fhirType(), equalTo(FhirConstants.PATIENT));
		assertThat(((Patient) resultList.iterator().next()).getId(), equalTo(PATIENT_UUID));
	}
	
	@Test
	public void searchForPatients_shouldReturnNullForPatientEverythingWhenIdParamIsMissing() {
		IBundleProvider results = patientFhirResourceProvider.getPatientEverything(null);
		
		assertThat(results, nullValue());
	}
	
	@Test
	public void searchForPatients_shouldReturnNullForPatientEverythingWhenIdPartIsMissingInIdParam() {
		IBundleProvider results = patientFhirResourceProvider.getPatientEverything(new IdType());
		
		assertThat(results, nullValue());
	}
	
	@Test
	public void searchForPatients_shouldReturnNullPatientEverythingWhenIdPartIsEmptyInIdParam() {
		IBundleProvider results = patientFhirResourceProvider.getPatientEverything(new IdType(""));
		
		assertThat(results, nullValue());
	}
	
	@Test
	public void searchForPatients_shouldReturnPatientEverythingForTypeLevel() {
		when(patientService.getPatientEverything())
		        .thenReturn(new MockIBundleProvider<>(Collections.singletonList(patient), 10, 1));
		
		IBundleProvider results = patientFhirResourceProvider.getPatientEverything();
		
		List<IBaseResource> resultList = getAllResources(results);
		
		assertThat(resultList, notNullValue());
		assertThat(resultList, hasSize(1));
		assertThat(resultList.get(0).fhirType(), equalTo(FhirConstants.PATIENT));
		assertThat(((Patient) resultList.iterator().next()).getId(), equalTo(PATIENT_UUID));
	}
	
	private List<IBaseResource> getAllResources(IBundleProvider result) {
		return result.getAllResources();
	}
	
	private List<IBaseResource> getResources(IBundleProvider result) {
		return result.getResources(0, 10);
	}
}
