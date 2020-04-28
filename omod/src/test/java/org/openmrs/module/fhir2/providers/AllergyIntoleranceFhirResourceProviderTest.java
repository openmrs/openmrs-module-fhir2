/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.fhir2.providers;

import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;
import static org.mockito.hamcrest.MockitoHamcrest.argThat;

import java.util.Collections;
import java.util.List;

import ca.uhn.fhir.rest.param.ReferenceAndListParam;
import ca.uhn.fhir.rest.param.ReferenceOrListParam;
import ca.uhn.fhir.rest.param.ReferenceParam;
import ca.uhn.fhir.rest.param.TokenAndListParam;
import ca.uhn.fhir.rest.param.TokenOrListParam;
import ca.uhn.fhir.rest.param.TokenParam;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import org.hl7.fhir.r4.model.AllergyIntolerance;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.Provenance;
import org.hl7.fhir.r4.model.Resource;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openmrs.module.fhir2.api.FhirAllergyIntoleranceService;
import org.openmrs.module.fhir2.web.servlet.BaseFhirProvenanceResourceTest;

@RunWith(MockitoJUnitRunner.class)
public class AllergyIntoleranceFhirResourceProviderTest extends BaseFhirProvenanceResourceTest<AllergyIntolerance> {
	
	private static final String ALLERGY_UUID = "1085AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
	
	private static final String WRONG_ALLERGY_UUID = "2085AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
	
	private static final String CODED_ALLERGEN_UUID = "5085AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
	
	private static final String SEVERITY_CONCEPT_UUID = "5088AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
	
	private static final String CODED_REACTION_UUID = "5087AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
	
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
		
		when(service.searchForAllergies(argThat(is(patient)), isNull(), isNull(), isNull(), isNull(), isNull()))
		        .thenReturn(Collections.singletonList(allergyIntolerance));
		
		Bundle results = resourceProvider.searchForAllergies(patient, null, null, null, null, null);
		assertThat(results, notNullValue());
		assertThat(results.isResource(), is(true));
		assertThat(results.getEntry().size(), greaterThanOrEqualTo(1));
	}
	
	@Test
	public void searchForAllergies_shouldReturnMatchingBundleOfAllergiesByPatientGivenName() {
		ReferenceAndListParam patient = new ReferenceAndListParam();
		patient.addValue(new ReferenceOrListParam().add(new ReferenceParam().setValue("John").setChain(Patient.SP_GIVEN)));
		
		when(service.searchForAllergies(argThat(is(patient)), isNull(), isNull(), isNull(), isNull(), isNull()))
		        .thenReturn(Collections.singletonList(allergyIntolerance));
		
		Bundle results = resourceProvider.searchForAllergies(patient, null, null, null, null, null);
		assertThat(results, notNullValue());
		assertThat(results.isResource(), is(true));
		assertThat(results.getEntry().size(), greaterThanOrEqualTo(1));
	}
	
	@Test
	public void searchForAllergies_shouldReturnMatchingBundleOfAllergiesByPatientFamilyName() {
		ReferenceAndListParam patient = new ReferenceAndListParam();
		patient.addValue(new ReferenceOrListParam().add(new ReferenceParam().setValue("John").setChain(Patient.SP_FAMILY)));
		
		when(service.searchForAllergies(argThat(is(patient)), isNull(), isNull(), isNull(), isNull(), isNull()))
		        .thenReturn(Collections.singletonList(allergyIntolerance));
		
		Bundle results = resourceProvider.searchForAllergies(patient, null, null, null, null, null);
		assertThat(results, notNullValue());
		assertThat(results.isResource(), is(true));
		assertThat(results.getEntry().size(), greaterThanOrEqualTo(1));
	}
	
	@Test
	public void searchForAllergies_shouldReturnMatchingBundleOfAllergiesByPatientName() {
		ReferenceAndListParam patient = new ReferenceAndListParam();
		patient.addValue(
		    new ReferenceOrListParam().add(new ReferenceParam().setValue("John Doe").setChain(Patient.SP_NAME)));
		
		when(service.searchForAllergies(argThat(is(patient)), isNull(), isNull(), isNull(), isNull(), isNull()))
		        .thenReturn(Collections.singletonList(allergyIntolerance));
		
		Bundle results = resourceProvider.searchForAllergies(patient, null, null, null, null, null);
		assertThat(results, notNullValue());
		assertThat(results.isResource(), is(true));
		assertThat(results.getEntry().size(), greaterThanOrEqualTo(1));
	}
	
	@Test
	public void searchForAllergies_shouldReturnMatchingBundleOfAllergiesByCategory() {
		TokenOrListParam category = new TokenOrListParam();
		category.addOr(new TokenParam().setValue("food"));
		
		when(service.searchForAllergies(isNull(), argThat(is(category)), isNull(), isNull(), isNull(), isNull()))
		        .thenReturn(Collections.singletonList(allergyIntolerance));
		
		Bundle results = resourceProvider.searchForAllergies(null, category, null, null, null, null);
		assertThat(results, notNullValue());
		assertThat(results.isResource(), is(true));
		assertThat(results.getEntry().size(), greaterThanOrEqualTo(1));
	}
	
	@Test
	public void searchForAllergies_shouldReturnMatchingBundleOfAllergiesByAllergen() {
		TokenAndListParam allergen = new TokenAndListParam();
		allergen.addAnd(new TokenOrListParam().addOr(new TokenParam().setValue(CODED_ALLERGEN_UUID)));
		
		when(service.searchForAllergies(isNull(), isNull(), argThat(is(allergen)), isNull(), isNull(), isNull()))
		        .thenReturn(Collections.singletonList(allergyIntolerance));
		
		Bundle results = resourceProvider.searchForAllergies(null, null, allergen, null, null, null);
		
		assertThat(results, notNullValue());
		assertThat(results.isResource(), is(true));
		assertThat(results.getEntry().size(), greaterThanOrEqualTo(1));
	}
	
	@Test
	public void searchForAllergies_shouldReturnMatchingBundleOfAllergiesBySeverity() {
		TokenOrListParam severity = new TokenOrListParam();
		severity.addOr(new TokenParam().setValue(SEVERITY_CONCEPT_UUID));
		
		when(service.searchForAllergies(isNull(), isNull(), isNull(), argThat(is(severity)), isNull(), isNull()))
		        .thenReturn(Collections.singletonList(allergyIntolerance));
		
		Bundle results = resourceProvider.searchForAllergies(null, null, null, severity, null, null);
		assertThat(results, notNullValue());
		assertThat(results.isResource(), is(true));
		assertThat(results.getEntry().size(), greaterThanOrEqualTo(1));
	}
	
	@Test
	public void searchForAllergies_shouldReturnMatchingBundleOfAllergiesByManifestation() {
		TokenAndListParam manifestation = new TokenAndListParam();
		manifestation.addAnd(new TokenOrListParam().addOr(new TokenParam().setValue(CODED_REACTION_UUID)));
		
		when(service.searchForAllergies(isNull(), isNull(), isNull(), isNull(), argThat(is(manifestation)), isNull()))
		        .thenReturn(Collections.singletonList(allergyIntolerance));
		
		Bundle results = resourceProvider.searchForAllergies(null, null, null, null, manifestation, null);
		assertThat(results, notNullValue());
		assertThat(results.isResource(), is(true));
		assertThat(results.getEntry().size(), greaterThanOrEqualTo(1));
	}
	
	@Test
	public void searchForAllergies_shouldReturnMatchingBundleOfAllergiesByStatus() {
		TokenOrListParam status = new TokenOrListParam();
		status.addOr(new TokenParam().setValue("active"));
		
		when(service.searchForAllergies(isNull(), isNull(), isNull(), isNull(), isNull(), argThat(is(status))))
		        .thenReturn(Collections.singletonList(allergyIntolerance));
		
		Bundle results = resourceProvider.searchForAllergies(null, null, null, null, null, status);
		assertThat(results, notNullValue());
		assertThat(results.isResource(), is(true));
		assertThat(results.getEntry().size(), greaterThanOrEqualTo(1));
	}
	
}
