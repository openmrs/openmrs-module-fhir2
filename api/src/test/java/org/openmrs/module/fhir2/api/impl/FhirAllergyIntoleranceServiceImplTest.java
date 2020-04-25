/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.fhir2.api.impl;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;
import static org.mockito.hamcrest.MockitoHamcrest.argThat;

import java.util.ArrayList;
import java.util.Collection;

import ca.uhn.fhir.rest.param.ReferenceAndListParam;
import ca.uhn.fhir.rest.param.ReferenceOrListParam;
import ca.uhn.fhir.rest.param.ReferenceParam;
import ca.uhn.fhir.rest.param.TokenAndListParam;
import ca.uhn.fhir.rest.param.TokenOrListParam;
import ca.uhn.fhir.rest.param.TokenParam;
import org.hl7.fhir.r4.model.AllergyIntolerance;
import org.hl7.fhir.r4.model.Patient;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openmrs.Allergy;
import org.openmrs.module.fhir2.api.dao.FhirAllergyIntoleranceDao;
import org.openmrs.module.fhir2.api.translators.AllergyIntoleranceTranslator;

@RunWith(MockitoJUnitRunner.class)
public class FhirAllergyIntoleranceServiceImplTest {
	
	private static final String ALLERGY_UUID = "1085AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
	
	private static final String WRONG_ALLERGY_UUID = "2085AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
	
	private static final String CODED_ALLERGEN_UUID = "5085AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
	
	private static final String SEVERITY_CONCEPT_UUID = "5088AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
	
	private static final String CODED_REACTION_UUID = "5087AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
	
	@Mock
	private FhirAllergyIntoleranceDao allergyIntoleranceDao;
	
	@Mock
	private AllergyIntoleranceTranslator translator;
	
	private FhirAllergyIntoleranceServiceImpl service;
	
	private Allergy omrsAllergy;
	
	private AllergyIntolerance fhirAllergy;
	
	@Before
	public void setup() {
		service = new FhirAllergyIntoleranceServiceImpl();
		service.setAllergyIntoleranceTranslator(translator);
		service.setAllergyIntoleranceDao(allergyIntoleranceDao);
		
		omrsAllergy = new Allergy();
		omrsAllergy.setUuid(ALLERGY_UUID);
		
		fhirAllergy = new AllergyIntolerance();
		fhirAllergy.setId(ALLERGY_UUID);
	}
	
	@Test
	public void getAllergyIntoleranceByUuid_shouldGetAllergyIntoleranceByUuid() {
		when(allergyIntoleranceDao.get(ALLERGY_UUID)).thenReturn(omrsAllergy);
		when(translator.toFhirResource(omrsAllergy)).thenReturn(fhirAllergy);
		
		AllergyIntolerance result = service.get(ALLERGY_UUID);
		assertThat(result, notNullValue());
		assertThat(result.getId(), notNullValue());
		assertThat(result.getId(), equalTo(ALLERGY_UUID));
	}
	
	@Test
	public void getAllergyIntoleranceByUuid_shouldReturnNullWhenCalledWithWrongUuid() {
		AllergyIntolerance result = service.get(WRONG_ALLERGY_UUID);
		assertThat(result, nullValue());
	}
	
	@Test
	public void searchForAllergies_shouldSearchForAllergiesByIdentifier() {
		Collection<Allergy> allergies = new ArrayList<>();
		allergies.add(omrsAllergy);
		ReferenceAndListParam patientParam = new ReferenceAndListParam();
		ReferenceParam referenceParam = new ReferenceParam();
		
		referenceParam.setValue("M4001-1");
		referenceParam.setChain(Patient.SP_IDENTIFIER);
		
		patientParam.addValue(new ReferenceOrListParam().add(referenceParam));
		
		when(allergyIntoleranceDao.searchForAllergies(argThat(equalTo(patientParam)), isNull(), isNull(), isNull(), isNull(),
		    isNull())).thenReturn(allergies);
		Collection<org.hl7.fhir.r4.model.AllergyIntolerance> results = service.searchForAllergies(patientParam, null, null,
		    null, null, null);
		assertThat(results, notNullValue());
		assertThat(results, not(empty()));
		assertThat(results.size(), greaterThanOrEqualTo(1));
	}
	
	@Test
	public void searchForAllergies_shouldSearchForAllergiesByPatientGivenName() {
		Collection<Allergy> allergies = new ArrayList<>();
		allergies.add(omrsAllergy);
		ReferenceAndListParam patientParam = new ReferenceAndListParam();
		ReferenceParam referenceParam = new ReferenceParam();
		
		referenceParam.setValue("John");
		referenceParam.setChain(Patient.SP_GIVEN);
		
		patientParam.addValue(new ReferenceOrListParam().add(referenceParam));
		
		when(allergyIntoleranceDao.searchForAllergies(argThat(equalTo(patientParam)), isNull(), isNull(), isNull(), isNull(),
		    isNull())).thenReturn(allergies);
		Collection<org.hl7.fhir.r4.model.AllergyIntolerance> results = service.searchForAllergies(patientParam, null, null,
		    null, null, null);
		assertThat(results, notNullValue());
		assertThat(results, not(empty()));
		assertThat(results.size(), greaterThanOrEqualTo(1));
	}
	
	@Test
	public void searchForAllergies_shouldSearchForAllergiesByPatientFamilyName() {
		Collection<Allergy> allergies = new ArrayList<>();
		allergies.add(omrsAllergy);
		ReferenceAndListParam patientParam = new ReferenceAndListParam();
		ReferenceParam referenceParam = new ReferenceParam();
		
		referenceParam.setValue("John");
		referenceParam.setChain(Patient.SP_FAMILY);
		
		patientParam.addValue(new ReferenceOrListParam().add(referenceParam));
		
		when(allergyIntoleranceDao.searchForAllergies(argThat(equalTo(patientParam)), isNull(), isNull(), isNull(), isNull(),
		    isNull())).thenReturn(allergies);
		Collection<org.hl7.fhir.r4.model.AllergyIntolerance> results = service.searchForAllergies(patientParam, null, null,
		    null, null, null);
		assertThat(results, notNullValue());
		assertThat(results, not(empty()));
		assertThat(results.size(), greaterThanOrEqualTo(1));
	}
	
	@Test
	public void searchForAllergies_shouldSearchForAllergiesByPatientName() {
		Collection<Allergy> allergies = new ArrayList<>();
		allergies.add(omrsAllergy);
		ReferenceAndListParam patientParam = new ReferenceAndListParam();
		ReferenceParam referenceParam = new ReferenceParam();
		
		referenceParam.setValue("John Doe");
		referenceParam.setChain(Patient.SP_NAME);
		
		patientParam.addValue(new ReferenceOrListParam().add(referenceParam));
		
		when(allergyIntoleranceDao.searchForAllergies(argThat(equalTo(patientParam)), isNull(), isNull(), isNull(), isNull(),
		    isNull())).thenReturn(allergies);
		Collection<org.hl7.fhir.r4.model.AllergyIntolerance> results = service.searchForAllergies(patientParam, null, null,
		    null, null, null);
		assertThat(results, notNullValue());
		assertThat(results, not(empty()));
		assertThat(results.size(), greaterThanOrEqualTo(1));
	}
	
	@Test
	public void searchForAllergies_shouldSearchForAllergiesByCategory() {
		Collection<Allergy> allergies = new ArrayList<>();
		allergies.add(omrsAllergy);
		
		TokenOrListParam category = new TokenOrListParam();
		category.addOr(new TokenParam().setValue("food"));
		
		when(allergyIntoleranceDao.searchForAllergies(isNull(), argThat(equalTo(category)), isNull(), isNull(), isNull(),
		    isNull())).thenReturn(allergies);
		Collection<org.hl7.fhir.r4.model.AllergyIntolerance> results = service.searchForAllergies(null, category, null, null,
		    null, null);
		assertThat(results, notNullValue());
		assertThat(results.size(), greaterThanOrEqualTo(1));
	}
	
	@Test
	public void searchForAllergies_shouldSearchForAllergiesByAllergen() {
		Collection<Allergy> allergies = new ArrayList<>();
		allergies.add(omrsAllergy);
		
		TokenAndListParam allergen = new TokenAndListParam();
		allergen.addAnd(new TokenOrListParam().addOr(new TokenParam().setValue(CODED_ALLERGEN_UUID)));
		
		when(allergyIntoleranceDao.searchForAllergies(isNull(), isNull(), argThat(equalTo(allergen)), isNull(), isNull(),
		    isNull())).thenReturn(allergies);
		Collection<org.hl7.fhir.r4.model.AllergyIntolerance> results = service.searchForAllergies(null, null, allergen, null,
		    null, null);
		assertThat(results, notNullValue());
		assertThat(results.size(), greaterThanOrEqualTo(1));
	}
	
	@Test
	public void searchForAllergies_shouldSearchForAllergiesBySeverity() {
		Collection<Allergy> allergies = new ArrayList<>();
		allergies.add(omrsAllergy);
		
		TokenOrListParam severity = new TokenOrListParam();
		severity.addOr(new TokenParam().setValue(SEVERITY_CONCEPT_UUID));
		
		when(allergyIntoleranceDao.searchForAllergies(isNull(), isNull(), isNull(), argThat(equalTo(severity)), isNull(),
		    isNull())).thenReturn(allergies);
		Collection<org.hl7.fhir.r4.model.AllergyIntolerance> results = service.searchForAllergies(null, null, null, severity,
		    null, null);
		assertThat(results, notNullValue());
		assertThat(results.size(), greaterThanOrEqualTo(1));
	}
	
	@Test
	public void searchForAllergies_shouldSearchForAllergiesByManifestation() {
		Collection<Allergy> allergies = new ArrayList<>();
		allergies.add(omrsAllergy);
		
		TokenAndListParam manifestation = new TokenAndListParam();
		manifestation.addAnd(new TokenOrListParam().addOr(new TokenParam().setValue(CODED_REACTION_UUID)));
		
		when(allergyIntoleranceDao.searchForAllergies(isNull(), isNull(), isNull(), isNull(),
		    argThat(equalTo(manifestation)), isNull())).thenReturn(allergies);
		Collection<org.hl7.fhir.r4.model.AllergyIntolerance> results = service.searchForAllergies(null, null, null, null,
		    manifestation, null);
		assertThat(results, notNullValue());
		assertThat(results.size(), greaterThanOrEqualTo(1));
	}
	
	@Test
	public void searchForAllergies_shouldSearchForAllergiesByClinicalStatus() {
		Collection<Allergy> allergies = new ArrayList<>();
		allergies.add(omrsAllergy);
		
		TokenOrListParam status = new TokenOrListParam();
		status.addOr(new TokenParam().setValue("active"));
		
		when(allergyIntoleranceDao.searchForAllergies(isNull(), isNull(), isNull(), isNull(), isNull(),
		    argThat(equalTo(status)))).thenReturn(allergies);
		Collection<org.hl7.fhir.r4.model.AllergyIntolerance> results = service.searchForAllergies(null, null, null, null,
		    null, status);
		assertThat(results, notNullValue());
		assertThat(results.size(), greaterThanOrEqualTo(1));
	}
	
}
