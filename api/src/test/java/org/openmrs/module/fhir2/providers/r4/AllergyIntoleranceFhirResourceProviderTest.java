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

import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;
import static org.mockito.hamcrest.MockitoHamcrest.argThat;

import java.util.Collections;
import java.util.List;

import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.api.server.IBundleProvider;
import ca.uhn.fhir.rest.param.DateRangeParam;
import ca.uhn.fhir.rest.param.ReferenceAndListParam;
import ca.uhn.fhir.rest.param.ReferenceOrListParam;
import ca.uhn.fhir.rest.param.ReferenceParam;
import ca.uhn.fhir.rest.param.TokenAndListParam;
import ca.uhn.fhir.rest.param.TokenOrListParam;
import ca.uhn.fhir.rest.param.TokenParam;
import ca.uhn.fhir.rest.server.exceptions.InvalidRequestException;
import ca.uhn.fhir.rest.server.exceptions.MethodNotAllowedException;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.AllergyIntolerance;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.OperationOutcome;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.Provenance;
import org.hl7.fhir.r4.model.Resource;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.api.FhirAllergyIntoleranceService;
import org.openmrs.module.fhir2.providers.BaseFhirProvenanceResourceTest;

@RunWith(MockitoJUnitRunner.class)
public class AllergyIntoleranceFhirResourceProviderTest extends BaseFhirProvenanceResourceTest<AllergyIntolerance> {
	
	private static final String ALLERGY_UUID = "1085AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
	
	private static final String WRONG_ALLERGY_UUID = "2085AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
	
	private static final String CODED_ALLERGEN_UUID = "5085AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
	
	private static final String SEVERITY_CONCEPT_UUID = "5088AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
	
	private static final String CODED_REACTION_UUID = "5087AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
	
	private static final String LAST_UPDATED_DATE = "2020-09-03";
	
	private static final int START_INDEX = 0;
	
	private static final int END_INDEX = 10;
	
	private static final int PREFERRED_PAGE_SIZE = 1;
	
	private static final int COUNT = 1;
	
	@Mock
	private FhirAllergyIntoleranceService service;
	
	private AllergyIntoleranceFhirResourceProvider resourceProvider;
	
	private AllergyIntolerance allergyIntolerance;
	
	@Before
	public void setup() {
		resourceProvider = new AllergyIntoleranceFhirResourceProvider();
		resourceProvider.setFhirAllergyIntoleranceService(service);
	}
	
	@Before
	public void initAllergyIntolerance() {
		allergyIntolerance = new AllergyIntolerance();
		allergyIntolerance.setId(ALLERGY_UUID);
		allergyIntolerance.addCategory(AllergyIntolerance.AllergyIntoleranceCategory.FOOD);
		setProvenanceResources(allergyIntolerance);
	}
	
	private List<IBaseResource> get(IBundleProvider results) {
		return results.getResources(START_INDEX, END_INDEX);
	}
	
	@Test
	public void getResourceType_shouldReturnResourceType() {
		assertThat(resourceProvider.getResourceType(), equalTo(AllergyIntolerance.class));
		assertThat(resourceProvider.getResourceType().getName(), equalTo(AllergyIntolerance.class.getName()));
	}
	
	@Test
	public void getAllergyIntoleranceByUuid_shouldReturnMatchingAllergy() {
		when(service.get(ALLERGY_UUID)).thenReturn(allergyIntolerance);
		
		IdType id = new IdType();
		id.setValue(ALLERGY_UUID);
		AllergyIntolerance allergy = resourceProvider.getAllergyIntoleranceByUuid(id);
		assertThat(allergy, notNullValue());
		assertThat(allergy.getId(), notNullValue());
		assertThat(allergy.getId(), equalTo(ALLERGY_UUID));
	}
	
	@Test(expected = ResourceNotFoundException.class)
	public void getAllergyIntoleranceByUuid_shouldThrowResourceNotFoundException() {
		IdType id = new IdType();
		id.setValue(WRONG_ALLERGY_UUID);
		AllergyIntolerance result = resourceProvider.getAllergyIntoleranceByUuid(id);
		assertThat(result, nullValue());
	}
	
	@Test
	public void getAllergyIntoleranceHistory_shouldReturnProvenanceResources() {
		IdType id = new IdType();
		id.setValue(ALLERGY_UUID);
		when(service.get(ALLERGY_UUID)).thenReturn(allergyIntolerance);
		
		List<Resource> resources = resourceProvider.getAllergyIntoleranceHistoryById(id);
		assertThat(resources, not(empty()));
		assertThat(resources.stream().findAny().isPresent(), is(true));
		assertThat(resources.stream().findAny().get().getResourceType().name(), equalTo(Provenance.class.getSimpleName()));
	}
	
	@Test(expected = ResourceNotFoundException.class)
	public void getAllergyIntoleranceHistoryByWithWrongId_shouldThrowResourceNotFoundException() {
		IdType idType = new IdType();
		idType.setValue(WRONG_ALLERGY_UUID);
		assertThat(resourceProvider.getAllergyIntoleranceHistoryById(idType).isEmpty(), is(true));
		assertThat(resourceProvider.getAllergyIntoleranceHistoryById(idType).size(), equalTo(0));
		
	}
	
	@Test
	public void searchForAllergies_shouldReturnMatchingBundleOfAllergiesByIdentifier() {
		ReferenceAndListParam patient = new ReferenceAndListParam();
		patient.addValue(
		    new ReferenceOrListParam().add(new ReferenceParam().setValue("M4001-1").setChain(Patient.SP_IDENTIFIER)));
		
		when(service.searchForAllergies(argThat(is(patient)), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(),
		    isNull(), isNull())).thenReturn(
		        new MockIBundleProvider<>(Collections.singletonList(allergyIntolerance), PREFERRED_PAGE_SIZE, COUNT));
		
		IBundleProvider results = resourceProvider.searchForAllergies(patient, null, null, null, null, null, null, null,
		    null, null);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList.get(0).fhirType(), is(FhirConstants.ALLERGY_INTOLERANCE));
		assertThat(resultList, hasSize(greaterThanOrEqualTo(1)));
		assertThat(((AllergyIntolerance) resultList.iterator().next()).getId(), equalTo(ALLERGY_UUID));
	}
	
	@Test
	public void searchForAllergies_shouldReturnMatchingBundleOfAllergiesByPatientGivenName() {
		ReferenceAndListParam patient = new ReferenceAndListParam();
		patient.addValue(new ReferenceOrListParam().add(new ReferenceParam().setValue("John").setChain(Patient.SP_GIVEN)));
		
		when(service.searchForAllergies(argThat(is(patient)), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(),
		    isNull(), isNull())).thenReturn(
		        new MockIBundleProvider<>(Collections.singletonList(allergyIntolerance), PREFERRED_PAGE_SIZE, COUNT));
		
		IBundleProvider results = resourceProvider.searchForAllergies(patient, null, null, null, null, null, null, null,
		    null, null);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList.get(0).fhirType(), is(FhirConstants.ALLERGY_INTOLERANCE));
		assertThat(resultList, hasSize(greaterThanOrEqualTo(1)));
		assertThat(((AllergyIntolerance) resultList.iterator().next()).getId(), equalTo(ALLERGY_UUID));
	}
	
	@Test
	public void searchForAllergies_shouldReturnMatchingBundleOfAllergiesByPatientFamilyName() {
		ReferenceAndListParam patient = new ReferenceAndListParam();
		patient.addValue(new ReferenceOrListParam().add(new ReferenceParam().setValue("John").setChain(Patient.SP_FAMILY)));
		
		when(service.searchForAllergies(argThat(is(patient)), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(),
		    isNull(), isNull())).thenReturn(
		        new MockIBundleProvider<>(Collections.singletonList(allergyIntolerance), PREFERRED_PAGE_SIZE, COUNT));
		
		IBundleProvider results = resourceProvider.searchForAllergies(patient, null, null, null, null, null, null, null,
		    null, null);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList.get(0).fhirType(), is(FhirConstants.ALLERGY_INTOLERANCE));
		assertThat(resultList, hasSize(greaterThanOrEqualTo(1)));
		assertThat(((AllergyIntolerance) resultList.iterator().next()).getId(), equalTo(ALLERGY_UUID));
	}
	
	@Test
	public void searchForAllergies_shouldReturnMatchingBundleOfAllergiesByPatientName() {
		ReferenceAndListParam patient = new ReferenceAndListParam();
		patient.addValue(
		    new ReferenceOrListParam().add(new ReferenceParam().setValue("John Doe").setChain(Patient.SP_NAME)));
		
		when(service.searchForAllergies(argThat(is(patient)), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(),
		    isNull(), isNull())).thenReturn(
		        new MockIBundleProvider<>(Collections.singletonList(allergyIntolerance), PREFERRED_PAGE_SIZE, COUNT));
		
		IBundleProvider results = resourceProvider.searchForAllergies(patient, null, null, null, null, null, null, null,
		    null, null);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList.get(0).fhirType(), is(FhirConstants.ALLERGY_INTOLERANCE));
		assertThat(resultList, hasSize(greaterThanOrEqualTo(1)));
		assertThat(((AllergyIntolerance) resultList.iterator().next()).getId(), equalTo(ALLERGY_UUID));
	}
	
	@Test
	public void searchForAllergies_shouldReturnMatchingBundleOfAllergiesWhenPatientGivenNameIsSpecifiedAsSubject() {
		ReferenceAndListParam subject = new ReferenceAndListParam();
		subject.addValue(new ReferenceOrListParam().add(new ReferenceParam().setValue("John").setChain(Patient.SP_GIVEN)));
		
		when(service.searchForAllergies(argThat(is(subject)), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(),
		    isNull(), isNull())).thenReturn(
		        new MockIBundleProvider<>(Collections.singletonList(allergyIntolerance), PREFERRED_PAGE_SIZE, COUNT));
		
		IBundleProvider results = resourceProvider.searchForAllergies(null, subject, null, null, null, null, null, null,
		    null, null);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList.get(0).fhirType(), is(FhirConstants.ALLERGY_INTOLERANCE));
		assertThat(resultList, hasSize(greaterThanOrEqualTo(1)));
		assertThat(((AllergyIntolerance) resultList.iterator().next()).getId(), equalTo(ALLERGY_UUID));
	}
	
	@Test
	public void searchForAllergies_shouldReturnMatchingBundleOfAllergiesByCategory() {
		TokenAndListParam category = new TokenAndListParam();
		category.addAnd(new TokenOrListParam().addOr(new TokenParam().setValue("food")));
		
		when(service.searchForAllergies(isNull(), argThat(is(category)), isNull(), isNull(), isNull(), isNull(), isNull(),
		    isNull(), isNull())).thenReturn(
		        new MockIBundleProvider<>(Collections.singletonList(allergyIntolerance), PREFERRED_PAGE_SIZE, COUNT));
		
		IBundleProvider results = resourceProvider.searchForAllergies(null, null, category, null, null, null, null, null,
		    null, null);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList.get(0).fhirType(), is(FhirConstants.ALLERGY_INTOLERANCE));
		assertThat(resultList, hasSize(greaterThanOrEqualTo(1)));
		assertThat(((AllergyIntolerance) resultList.iterator().next()).getId(), equalTo(ALLERGY_UUID));
	}
	
	@Test
	public void searchForAllergies_shouldReturnMatchingBundleOfAllergiesByAllergen() {
		TokenAndListParam allergen = new TokenAndListParam();
		allergen.addAnd(new TokenOrListParam().addOr(new TokenParam().setValue(CODED_ALLERGEN_UUID)));
		
		when(service.searchForAllergies(isNull(), isNull(), argThat(is(allergen)), isNull(), isNull(), isNull(), isNull(),
		    isNull(), isNull())).thenReturn(
		        new MockIBundleProvider<>(Collections.singletonList(allergyIntolerance), PREFERRED_PAGE_SIZE, COUNT));
		
		IBundleProvider results = resourceProvider.searchForAllergies(null, null, null, allergen, null, null, null, null,
		    null, null);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList.get(0).fhirType(), is(FhirConstants.ALLERGY_INTOLERANCE));
		assertThat(resultList, hasSize(greaterThanOrEqualTo(1)));
		assertThat(((AllergyIntolerance) resultList.iterator().next()).getId(), equalTo(ALLERGY_UUID));
	}
	
	@Test
	public void searchForAllergies_shouldReturnMatchingBundleOfAllergiesBySeverity() {
		TokenAndListParam severity = new TokenAndListParam();
		severity.addAnd(new TokenOrListParam().addOr(new TokenParam().setValue(SEVERITY_CONCEPT_UUID)));
		
		when(service.searchForAllergies(isNull(), isNull(), isNull(), argThat(is(severity)), isNull(), isNull(), isNull(),
		    isNull(), isNull())).thenReturn(
		        new MockIBundleProvider<>(Collections.singletonList(allergyIntolerance), PREFERRED_PAGE_SIZE, COUNT));
		
		IBundleProvider results = resourceProvider.searchForAllergies(null, null, null, null, severity, null, null, null,
		    null, null);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList.get(0).fhirType(), is(FhirConstants.ALLERGY_INTOLERANCE));
		assertThat(resultList, hasSize(greaterThanOrEqualTo(1)));
		assertThat(((AllergyIntolerance) resultList.iterator().next()).getId(), equalTo(ALLERGY_UUID));
	}
	
	@Test
	public void searchForAllergies_shouldReturnMatchingBundleOfAllergiesByManifestation() {
		TokenAndListParam manifestation = new TokenAndListParam();
		manifestation.addAnd(new TokenOrListParam().addOr(new TokenParam().setValue(CODED_REACTION_UUID)));
		
		when(service.searchForAllergies(isNull(), isNull(), isNull(), isNull(), argThat(is(manifestation)), isNull(),
		    isNull(), isNull(), isNull())).thenReturn(
		        new MockIBundleProvider<>(Collections.singletonList(allergyIntolerance), PREFERRED_PAGE_SIZE, COUNT));
		
		IBundleProvider results = resourceProvider.searchForAllergies(null, null, null, null, null, manifestation, null,
		    null, null, null);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList.get(0).fhirType(), is(FhirConstants.ALLERGY_INTOLERANCE));
		assertThat(resultList, hasSize(greaterThanOrEqualTo(1)));
		assertThat(((AllergyIntolerance) resultList.iterator().next()).getId(), equalTo(ALLERGY_UUID));
	}
	
	@Test
	public void searchForAllergies_shouldReturnMatchingBundleOfAllergiesByStatus() {
		TokenAndListParam status = new TokenAndListParam();
		status.addAnd(new TokenOrListParam().addOr(new TokenParam().setValue("active")));
		
		when(service.searchForAllergies(isNull(), isNull(), isNull(), isNull(), isNull(), argThat(is(status)), isNull(),
		    isNull(), isNull())).thenReturn(
		        new MockIBundleProvider<>(Collections.singletonList(allergyIntolerance), PREFERRED_PAGE_SIZE, COUNT));
		
		IBundleProvider results = resourceProvider.searchForAllergies(null, null, null, null, null, null, status, null, null,
		    null);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList.get(0).fhirType(), is(FhirConstants.ALLERGY_INTOLERANCE));
		assertThat(resultList, hasSize(greaterThanOrEqualTo(1)));
		assertThat(((AllergyIntolerance) resultList.iterator().next()).getId(), equalTo(ALLERGY_UUID));
	}
	
	@Test
	public void searchForAllergies_shouldReturnMatchingBundleOfAllergiesByUUID() {
		TokenAndListParam uuid = new TokenAndListParam();
		uuid.addAnd(new TokenOrListParam().addOr(new TokenParam().setValue(ALLERGY_UUID)));
		
		when(service.searchForAllergies(isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), argThat(is(uuid)),
		    isNull(), isNull())).thenReturn(
		        new MockIBundleProvider<>(Collections.singletonList(allergyIntolerance), PREFERRED_PAGE_SIZE, COUNT));
		
		IBundleProvider results = resourceProvider.searchForAllergies(null, null, null, null, null, null, null, uuid, null,
		    null);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList.get(0).fhirType(), is(FhirConstants.ALLERGY_INTOLERANCE));
		assertThat(resultList, hasSize(greaterThanOrEqualTo(1)));
		assertThat(((AllergyIntolerance) resultList.iterator().next()).getId(), equalTo(ALLERGY_UUID));
	}
	
	@Test
	public void searchForAllergies_shouldReturnMatchingBundleOfAllergiesByLastUpdated() {
		DateRangeParam dateRangeParam = new DateRangeParam().setLowerBound(LAST_UPDATED_DATE)
		        .setLowerBound(LAST_UPDATED_DATE);
		
		when(service.searchForAllergies(isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(),
		    argThat(is(dateRangeParam)), isNull())).thenReturn(
		        new MockIBundleProvider<>(Collections.singletonList(allergyIntolerance), PREFERRED_PAGE_SIZE, COUNT));
		
		IBundleProvider results = resourceProvider.searchForAllergies(null, null, null, null, null, null, null, null,
		    dateRangeParam, null);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList.get(0).fhirType(), is(FhirConstants.ALLERGY_INTOLERANCE));
		assertThat(resultList, hasSize(greaterThanOrEqualTo(1)));
		assertThat(((AllergyIntolerance) resultList.iterator().next()).getId(), equalTo(ALLERGY_UUID));
	}
	
	@Test
	public void createAllergyIntolerance_shouldCreateAllergyIntolerance() {
		when(service.create(allergyIntolerance)).thenReturn(allergyIntolerance);
		
		MethodOutcome result = resourceProvider.createAllergy(allergyIntolerance);
		assertThat(result, notNullValue());
		assertThat(result.getCreated(), is(true));
		assertThat(result.getResource(), equalTo(allergyIntolerance));
	}
	
	@Test
	public void updateAllergyIntolerance_shouldUpdateRequestedAllergyIntolerance() {
		when(service.update(ALLERGY_UUID, allergyIntolerance)).thenReturn(allergyIntolerance);
		
		MethodOutcome result = resourceProvider.updateAllergy(new IdType().setValue(ALLERGY_UUID), allergyIntolerance);
		assertThat(result, notNullValue());
		assertThat(result.getResource(), equalTo(allergyIntolerance));
	}
	
	@Test(expected = InvalidRequestException.class)
	public void updateAllergyIntolerance_shouldThrowInvalidRequestForUuidMismatchException() {
		when(service.update(WRONG_ALLERGY_UUID, allergyIntolerance)).thenThrow(InvalidRequestException.class);
		
		resourceProvider.updateAllergy(new IdType().setValue(WRONG_ALLERGY_UUID), allergyIntolerance);
	}
	
	@Test(expected = InvalidRequestException.class)
	public void updateAllergyIntolerance_shouldThrowInvalidRequestForMissingId() {
		AllergyIntolerance noIdAllergyIntolerance = new AllergyIntolerance();
		
		when(service.update(ALLERGY_UUID, noIdAllergyIntolerance)).thenThrow(InvalidRequestException.class);
		
		resourceProvider.updateAllergy(new IdType().setValue(ALLERGY_UUID), noIdAllergyIntolerance);
	}
	
	@Test(expected = MethodNotAllowedException.class)
	public void updateAllergyIntolerance_shouldThrowMethodNotAllowedIfDoesNotExist() {
		AllergyIntolerance wrongAllergyIntolerance = new AllergyIntolerance();
		wrongAllergyIntolerance.setId(WRONG_ALLERGY_UUID);
		
		when(service.update(WRONG_ALLERGY_UUID, wrongAllergyIntolerance)).thenThrow(MethodNotAllowedException.class);
		
		resourceProvider.updateAllergy(new IdType().setValue(WRONG_ALLERGY_UUID), wrongAllergyIntolerance);
	}
	
	@Test
	public void deleteAllergyIntolerance_shouldDeleteRequestedAllergyIntolerance() {
		when(service.delete(ALLERGY_UUID)).thenReturn(allergyIntolerance);
		
		OperationOutcome result = resourceProvider.deleteAllergy(new IdType().setValue(ALLERGY_UUID));
		assertThat(result, notNullValue());
		assertThat(result.getIssue(), notNullValue());
		assertThat(result.getIssueFirstRep().getSeverity(), equalTo(OperationOutcome.IssueSeverity.INFORMATION));
		assertThat(result.getIssueFirstRep().getDetails().getCodingFirstRep().getCode(), equalTo("MSG_DELETED"));
	}
	
	@Test(expected = ResourceNotFoundException.class)
	public void deleteAllergyIntolerance_shouldThrowResourceNotFoundExceptionWhenIdRefersToNonExistentAllergyIntolerance() {
		when(service.delete(WRONG_ALLERGY_UUID)).thenReturn(null);
		resourceProvider.deleteAllergy(new IdType().setValue(WRONG_ALLERGY_UUID));
	}
}
