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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import ca.uhn.fhir.rest.api.server.IBundleProvider;
import ca.uhn.fhir.rest.param.ReferenceAndListParam;
import ca.uhn.fhir.rest.param.ReferenceOrListParam;
import ca.uhn.fhir.rest.param.ReferenceParam;
import ca.uhn.fhir.rest.param.TokenAndListParam;
import ca.uhn.fhir.rest.param.TokenOrListParam;
import ca.uhn.fhir.rest.param.TokenParam;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.AllergyIntolerance;
import org.hl7.fhir.r4.model.Patient;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openmrs.Allergy;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.api.dao.FhirAllergyIntoleranceDao;
import org.openmrs.module.fhir2.api.search.SearchQuery;
import org.openmrs.module.fhir2.api.search.SearchQueryBundleProvider;
import org.openmrs.module.fhir2.api.search.param.SearchParameterMap;
import org.openmrs.module.fhir2.api.translators.AllergyIntoleranceTranslator;

@RunWith(MockitoJUnitRunner.class)
public class FhirAllergyIntoleranceServiceImplTest {
	
	private static final String ALLERGY_UUID = "1085AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
	
	private static final String WRONG_ALLERGY_UUID = "2085AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
	
	private static final String CODED_ALLERGEN_UUID = "5085AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
	
	private static final String SEVERITY_CONCEPT_UUID = "5088AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
	
	private static final String CODED_REACTION_UUID = "5087AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
	
	private static final int START_INDEX = 0;
	
	private static final int END_INDEX = 10;
	
	@Mock
	private FhirAllergyIntoleranceDao allergyIntoleranceDao;
	
	@Mock
	private AllergyIntoleranceTranslator translator;
	
	@Mock
	private SearchQuery<Allergy, AllergyIntolerance, FhirAllergyIntoleranceDao, AllergyIntoleranceTranslator> searchQuery;
	
	private FhirAllergyIntoleranceServiceImpl service;
	
	private Allergy omrsAllergy;
	
	private AllergyIntolerance fhirAllergy;
	
	@Before
	public void setup() {
		service = new FhirAllergyIntoleranceServiceImpl();
		service.setTranslator(translator);
		service.setDao(allergyIntoleranceDao);
		service.setSearchQuery(searchQuery);
		
		omrsAllergy = new Allergy();
		omrsAllergy.setUuid(ALLERGY_UUID);
		
		fhirAllergy = new AllergyIntolerance();
		fhirAllergy.setId(ALLERGY_UUID);
	}
	
	private List<IBaseResource> get(IBundleProvider results) {
		return results.getResources(START_INDEX, END_INDEX);
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
		
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.PATIENT_REFERENCE_SEARCH_HANDLER,
		    patientParam);
		
		when(allergyIntoleranceDao.search(any(), anyInt(), anyInt())).thenReturn(allergies);
		when(searchQuery.getQueryResults(any(), any(), any()))
		        .thenReturn(new SearchQueryBundleProvider<>(theParams, allergyIntoleranceDao, translator));
		when(translator.toFhirResource(omrsAllergy)).thenReturn(fhirAllergy);
		
		IBundleProvider results = service.searchForAllergies(patientParam, null, null, null, null, null);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList, not(empty()));
		assertThat(resultList.size(), greaterThanOrEqualTo(1));
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
		
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.PATIENT_REFERENCE_SEARCH_HANDLER,
		    patientParam);
		
		when(allergyIntoleranceDao.search(any(), anyInt(), anyInt())).thenReturn(allergies);
		when(searchQuery.getQueryResults(any(), any(), any()))
		        .thenReturn(new SearchQueryBundleProvider<>(theParams, allergyIntoleranceDao, translator));
		when(translator.toFhirResource(omrsAllergy)).thenReturn(fhirAllergy);
		
		IBundleProvider results = service.searchForAllergies(patientParam, null, null, null, null, null);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList, not(empty()));
		assertThat(resultList.size(), greaterThanOrEqualTo(1));
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
		
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.PATIENT_REFERENCE_SEARCH_HANDLER,
		    patientParam);
		
		when(allergyIntoleranceDao.search(any(), anyInt(), anyInt())).thenReturn(allergies);
		when(searchQuery.getQueryResults(any(), any(), any()))
		        .thenReturn(new SearchQueryBundleProvider<>(theParams, allergyIntoleranceDao, translator));
		when(translator.toFhirResource(omrsAllergy)).thenReturn(fhirAllergy);
		
		IBundleProvider results = service.searchForAllergies(patientParam, null, null, null, null, null);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList.size(), greaterThanOrEqualTo(1));
		assertThat(resultList, not(empty()));
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
		
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.PATIENT_REFERENCE_SEARCH_HANDLER,
		    patientParam);
		
		when(allergyIntoleranceDao.search(any(), anyInt(), anyInt())).thenReturn(allergies);
		when(searchQuery.getQueryResults(any(), any(), any()))
		        .thenReturn(new SearchQueryBundleProvider<>(theParams, allergyIntoleranceDao, translator));
		when(translator.toFhirResource(omrsAllergy)).thenReturn(fhirAllergy);
		
		IBundleProvider results = service.searchForAllergies(patientParam, null, null, null, null, null);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList.size(), greaterThanOrEqualTo(1));
		assertThat(resultList, not(empty()));
	}
	
	@Test
	public void searchForAllergies_shouldSearchForAllergiesByCategory() {
		Collection<Allergy> allergies = new ArrayList<>();
		allergies.add(omrsAllergy);
		
		TokenAndListParam category = new TokenAndListParam();
		category.addAnd(new TokenOrListParam().addOr(new TokenParam().setValue("food")));
		
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.CATEGORY_SEARCH_HANDLER,
		    category);
		
		when(allergyIntoleranceDao.search(any(), anyInt(), anyInt())).thenReturn(allergies);
		when(searchQuery.getQueryResults(any(), any(), any()))
		        .thenReturn(new SearchQueryBundleProvider<>(theParams, allergyIntoleranceDao, translator));
		when(translator.toFhirResource(omrsAllergy)).thenReturn(fhirAllergy);
		
		IBundleProvider results = service.searchForAllergies(null, category, null, null, null, null);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList, not(empty()));
		assertThat(resultList.size(), greaterThanOrEqualTo(1));
	}
	
	@Test
	public void searchForAllergies_shouldSearchForAllergiesByAllergen() {
		Collection<Allergy> allergies = new ArrayList<>();
		allergies.add(omrsAllergy);
		
		TokenAndListParam allergen = new TokenAndListParam();
		allergen.addAnd(new TokenOrListParam().addOr(new TokenParam().setValue(CODED_ALLERGEN_UUID)));
		
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.ALLERGEN_SEARCH_HANDLER,
		    allergen);
		
		when(allergyIntoleranceDao.search(any(), anyInt(), anyInt())).thenReturn(allergies);
		when(searchQuery.getQueryResults(any(), any(), any()))
		        .thenReturn(new SearchQueryBundleProvider<>(theParams, allergyIntoleranceDao, translator));
		when(translator.toFhirResource(omrsAllergy)).thenReturn(fhirAllergy);
		
		IBundleProvider results = service.searchForAllergies(null, null, allergen, null, null, null);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList.size(), greaterThanOrEqualTo(1));
		assertThat(resultList, not(empty()));
	}
	
	@Test
	public void searchForAllergies_shouldSearchForAllergiesBySeverity() {
		Collection<Allergy> allergies = new ArrayList<>();
		allergies.add(omrsAllergy);
		
		TokenAndListParam severity = new TokenAndListParam();
		severity.addAnd(new TokenOrListParam().addOr(new TokenParam().setValue(SEVERITY_CONCEPT_UUID)));
		
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.SEVERITY_SEARCH_HANDLER,
		    severity);
		
		when(allergyIntoleranceDao.search(any(), anyInt(), anyInt())).thenReturn(allergies);
		when(searchQuery.getQueryResults(any(), any(), any()))
		        .thenReturn(new SearchQueryBundleProvider<>(theParams, allergyIntoleranceDao, translator));
		when(translator.toFhirResource(omrsAllergy)).thenReturn(fhirAllergy);
		
		IBundleProvider results = service.searchForAllergies(null, null, null, severity, null, null);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList, not(empty()));
		assertThat(resultList.size(), greaterThanOrEqualTo(1));
	}
	
	@Test
	public void searchForAllergies_shouldSearchForAllergiesByManifestation() {
		Collection<Allergy> allergies = new ArrayList<>();
		allergies.add(omrsAllergy);
		
		TokenAndListParam manifestation = new TokenAndListParam();
		manifestation.addAnd(new TokenOrListParam().addOr(new TokenParam().setValue(CODED_REACTION_UUID)));
		
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.CODED_SEARCH_HANDLER,
		    manifestation);
		
		when(allergyIntoleranceDao.search(any(), anyInt(), anyInt())).thenReturn(allergies);
		when(searchQuery.getQueryResults(any(), any(), any()))
		        .thenReturn(new SearchQueryBundleProvider<>(theParams, allergyIntoleranceDao, translator));
		when(translator.toFhirResource(omrsAllergy)).thenReturn(fhirAllergy);
		
		IBundleProvider results = service.searchForAllergies(null, null, null, null, manifestation, null);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList, not(empty()));
		assertThat(resultList.size(), greaterThanOrEqualTo(1));
	}
	
	@Test
	public void searchForAllergies_shouldSearchForAllergiesByClinicalStatus() {
		Collection<Allergy> allergies = new ArrayList<>();
		allergies.add(omrsAllergy);
		
		TokenAndListParam status = new TokenAndListParam();
		status.addAnd(new TokenOrListParam().addOr(new TokenParam().setValue("active")));
		
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.BOOLEAN_SEARCH_HANDLER, status);
		
		when(allergyIntoleranceDao.search(any(), anyInt(), anyInt())).thenReturn(allergies);
		when(searchQuery.getQueryResults(any(), any(), any()))
		        .thenReturn(new SearchQueryBundleProvider<>(theParams, allergyIntoleranceDao, translator));
		when(translator.toFhirResource(omrsAllergy)).thenReturn(fhirAllergy);
		
		IBundleProvider results = service.searchForAllergies(null, null, null, null, null, status);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList, not(empty()));
		assertThat(resultList.size(), greaterThanOrEqualTo(1));
	}
	
	@Test
	public void saveAllergy_shouldSaveNewAllergy() {
		Allergy allergy = new Allergy();
		allergy.setUuid(ALLERGY_UUID);
		
		AllergyIntolerance allergyIntolerance = new AllergyIntolerance();
		allergyIntolerance.setId(ALLERGY_UUID);
		
		when(translator.toFhirResource(allergy)).thenReturn(allergyIntolerance);
		when(translator.toOpenmrsType(allergyIntolerance)).thenReturn(allergy);
		when(allergyIntoleranceDao.createOrUpdate(allergy)).thenReturn(allergy);
		
		AllergyIntolerance result = service.create(allergyIntolerance);
		assertThat(result, notNullValue());
		assertThat(result.getId(), equalTo(ALLERGY_UUID));
	}
	
}
