/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.fhir2.providers.r4;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;

import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.api.server.IBundleProvider;
import ca.uhn.fhir.rest.param.ReferenceAndListParam;
import ca.uhn.fhir.rest.param.ReferenceOrListParam;
import ca.uhn.fhir.rest.param.ReferenceParam;
import ca.uhn.fhir.rest.server.exceptions.InvalidRequestException;
import ca.uhn.fhir.rest.server.exceptions.MethodNotAllowedException;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.Immunization;
import org.hl7.fhir.r4.model.Patient;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.api.FhirImmunizationService;
import org.openmrs.module.fhir2.providers.BaseFhirProvenanceResourceTest;

@RunWith(MockitoJUnitRunner.class)
public class ImmunizationFhirResourceProviderTest extends BaseFhirProvenanceResourceTest<Immunization> {
	
	private static final String IMMUNIZATION_UUID = "017312a1-cf56-43ab-ae87-44070b801d1c";
	
	private static final String WRONG_IMMUNIZATION_UUID = "017312a1-df56-43ab-ae87-44070b801d1c";
	
	private static final String PATIENT_UUID = "017312a1-cf56-43ab-ae87-44070b801d1c";
	
	@Mock
	private FhirImmunizationService immunizationService;
	
	private ImmunizationFhirResourceProvider resourceProvider;
	
	private Immunization immunization;
	
	private Patient patient;
	
	@Before
	public void setup() {
		resourceProvider = new ImmunizationFhirResourceProvider();
		resourceProvider.setImmunizationService(immunizationService);
	}
	
	@Before
	public void initImmunization() {
		patient = new Patient();
		patient.setId(PATIENT_UUID);
		
		immunization = new Immunization();
		immunization.setId(IMMUNIZATION_UUID);
		immunization.setPatientTarget(patient);
		setProvenanceResources(immunization);
	}
	
	@Test
	public void getResourceType_shouldReturnResourceType() {
		assertThat(resourceProvider.getResourceType(), equalTo(Immunization.class));
		assertThat(resourceProvider.getResourceType().getName(), equalTo(Immunization.class.getName()));
	}
	
	@Test
	public void getImmunizationById_shouldReturnImmunization() {
		IdType id = new IdType();
		id.setValue(IMMUNIZATION_UUID);
		when(immunizationService.get(IMMUNIZATION_UUID)).thenReturn(immunization);
		
		Immunization result = resourceProvider.getImmunizationByUuid(id);
		assertThat(result.isResource(), is(true));
		assertThat(result, notNullValue());
		assertThat(result.getId(), notNullValue());
		assertThat(result.getId(), equalTo(IMMUNIZATION_UUID));
	}
	
	@Test(expected = ResourceNotFoundException.class)
	public void getImmunizationByWithWrongId_shouldThrowResourceNotFoundException() {
		IdType idType = new IdType();
		idType.setValue(WRONG_IMMUNIZATION_UUID);
		assertThat(resourceProvider.getImmunizationByUuid(idType).isResource(), is(true));
		assertThat(resourceProvider.getImmunizationByUuid(idType), nullValue());
	}
	
	@Test
	public void createImmunization_shouldCreateNewImmunization() {
		when(immunizationService.create(immunization)).thenReturn(immunization);
		
		MethodOutcome result = resourceProvider.createImmunization(immunization);
		
		assertThat(result, notNullValue());
		assertThat(result.getResource(), equalTo(immunization));
	}
	
	@Test
	public void updateImmunization_shouldUpdateRequestedImmunization() {
		when(immunizationService.update(IMMUNIZATION_UUID, immunization)).thenReturn(immunization);
		
		MethodOutcome result = resourceProvider.updateImmunization(new IdType().setValue(IMMUNIZATION_UUID), immunization);
		
		assertThat(result, notNullValue());
		assertThat(result.getResource(), equalTo(immunization));
	}
	
	@Test(expected = InvalidRequestException.class)
	public void updatePatient_shouldThrowInvalidRequestExceptionForUuidMismatch() {
		when(immunizationService.update(WRONG_IMMUNIZATION_UUID, immunization)).thenThrow(InvalidRequestException.class);
		
		resourceProvider.updateImmunization(new IdType().setValue(WRONG_IMMUNIZATION_UUID), immunization);
	}
	
	@Test(expected = InvalidRequestException.class)
	public void updateImmunization_shouldThrowInvalidRequestExceptionForMissingId() {
		Immunization noIdImmunization = new Immunization();
		
		when(immunizationService.update(IMMUNIZATION_UUID, noIdImmunization)).thenThrow(InvalidRequestException.class);
		
		resourceProvider.updateImmunization(new IdType().setValue(IMMUNIZATION_UUID), noIdImmunization);
	}
	
	@Test(expected = MethodNotAllowedException.class)
	public void updateImmunization_shouldThrowMethodNotAllowedIfDoesNotExist() {
		immunization.setId(WRONG_IMMUNIZATION_UUID);
		
		when(immunizationService.update(WRONG_IMMUNIZATION_UUID, immunization)).thenThrow(MethodNotAllowedException.class);
		
		resourceProvider.updateImmunization(new IdType().setValue(WRONG_IMMUNIZATION_UUID), immunization);
	}
	
	@Test
	public void searchImmunizations_shouldReturnMatchingImmunizationsWhenPatientParamIsSpecified() {
		ReferenceAndListParam patient = new ReferenceAndListParam();
		patient.addValue(new ReferenceOrListParam().add(new ReferenceParam(Immunization.SP_PATIENT, "John")));
		
		when(immunizationService.searchImmunizations(any(), any(), any()))
		        .thenReturn(new MockIBundleProvider<>(Collections.singletonList(immunization), 10, 1));
		
		IBundleProvider result = resourceProvider.searchImmunizations(patient, null, null);
		
		List<IBaseResource> resources = getResources(result, 1, 0);
		
		assertThat(result, notNullValue());
		assertThat(resources, hasSize(equalTo(1)));
		assertThat(resources.get(0), notNullValue());
		assertThat(resources.get(0).fhirType(), equalTo(FhirConstants.IMMUNIZATION));
		assertThat(resources.get(0).getIdElement().getIdPart(), equalTo(IMMUNIZATION_UUID));
	}
	
	private List<IBaseResource> getResources(IBundleProvider results, int theFromIndex, int theToIndex) {
		return results.getResources(theFromIndex, theToIndex);
		
	}
}
