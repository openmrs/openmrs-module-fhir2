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
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ca.uhn.fhir.rest.api.server.IBundleProvider;
import ca.uhn.fhir.rest.param.DateRangeParam;
import ca.uhn.fhir.rest.param.ReferenceAndListParam;
import ca.uhn.fhir.rest.param.ReferenceOrListParam;
import ca.uhn.fhir.rest.param.ReferenceParam;
import ca.uhn.fhir.rest.param.TokenAndListParam;
import ca.uhn.fhir.rest.param.TokenOrListParam;
import ca.uhn.fhir.rest.param.TokenParam;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
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
import org.openmrs.module.fhir2.api.FhirGlobalPropertyService;
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
	
	private static final String LAST_UPDATED_DATE = "2020-09-03";
	
	private static final int START_INDEX = 0;
	
	private static final int END_INDEX = 10;
	
	@Mock
	private FhirAllergyIntoleranceDao allergyIntoleranceDao;
	
	@Mock
	private AllergyIntoleranceTranslator translator;
	
	@Mock
	private FhirGlobalPropertyService globalPropertyService;
	
	@Mock
	private SearchQuery<Allergy, AllergyIntolerance, FhirAllergyIntoleranceDao, AllergyIntoleranceTranslator> searchQuery;
	
	private FhirAllergyIntoleranceServiceImpl service;
	
	private Allergy omrsAllergy;
	
	private AllergyIntolerance fhirAllergy;
	
	@Before
	public void setup() {
		service = new FhirAllergyIntoleranceServiceImpl() {
			
			@Override
			protected void validateObject(Allergy object) {
			}
		};
		
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
	public void getAllergyIntoleranceByUuid_shouldResourceNotFoundWhenCalledWithWrongUuid() {
		assertThrows(ResourceNotFoundException.class, () -> service.get(WRONG_ALLERGY_UUID));
	}
	
	@Test
	public void searchForAllergies_shouldSearchForAllergiesByIdentifier() {
		List<Allergy> allergies = new ArrayList<>();
		allergies.add(omrsAllergy);
		ReferenceAndListParam patientParam = new ReferenceAndListParam();
		ReferenceParam referenceParam = new ReferenceParam();
		
		referenceParam.setValue("M4001-1");
		referenceParam.setChain(Patient.SP_IDENTIFIER);
		
		patientParam.addValue(new ReferenceOrListParam().add(referenceParam));
		
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.PATIENT_REFERENCE_SEARCH_HANDLER,
		    patientParam);
		
		when(allergyIntoleranceDao.getSearchResults(any(), any(), anyInt(), anyInt())).thenReturn(allergies);
		when(allergyIntoleranceDao.getSearchResultUuids(any())).thenReturn(Collections.singletonList(ALLERGY_UUID));
		when(searchQuery.getQueryResults(any(), any(), any())).thenReturn(
		    new SearchQueryBundleProvider<>(theParams, allergyIntoleranceDao, translator, globalPropertyService));
		when(translator.toFhirResource(omrsAllergy)).thenReturn(fhirAllergy);
		
		IBundleProvider results = service.searchForAllergies(patientParam, null, null, null, null, null, null, null, null);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList, not(empty()));
		assertThat(resultList, hasSize(greaterThanOrEqualTo(1)));
	}
	
	@Test
	public void searchForAllergies_shouldSearchForAllergiesByPatientGivenName() {
		List<Allergy> allergies = new ArrayList<>();
		allergies.add(omrsAllergy);
		ReferenceAndListParam patientParam = new ReferenceAndListParam();
		ReferenceParam referenceParam = new ReferenceParam();
		
		referenceParam.setValue("John");
		referenceParam.setChain(Patient.SP_GIVEN);
		
		patientParam.addValue(new ReferenceOrListParam().add(referenceParam));
		
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.PATIENT_REFERENCE_SEARCH_HANDLER,
		    patientParam);
		
		when(allergyIntoleranceDao.getSearchResults(any(), any(), anyInt(), anyInt())).thenReturn(allergies);
		when(allergyIntoleranceDao.getSearchResultUuids(any())).thenReturn(Collections.singletonList(ALLERGY_UUID));
		when(searchQuery.getQueryResults(any(), any(), any())).thenReturn(
		    new SearchQueryBundleProvider<>(theParams, allergyIntoleranceDao, translator, globalPropertyService));
		when(translator.toFhirResource(omrsAllergy)).thenReturn(fhirAllergy);
		
		IBundleProvider results = service.searchForAllergies(patientParam, null, null, null, null, null, null, null, null);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList, not(empty()));
		assertThat(resultList, hasSize(greaterThanOrEqualTo(1)));
	}
	
	@Test
	public void searchForAllergies_shouldSearchForAllergiesByPatientFamilyName() {
		List<Allergy> allergies = new ArrayList<>();
		allergies.add(omrsAllergy);
		ReferenceAndListParam patientParam = new ReferenceAndListParam();
		ReferenceParam referenceParam = new ReferenceParam();
		
		referenceParam.setValue("John");
		referenceParam.setChain(Patient.SP_FAMILY);
		
		patientParam.addValue(new ReferenceOrListParam().add(referenceParam));
		
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.PATIENT_REFERENCE_SEARCH_HANDLER,
		    patientParam);
		
		when(allergyIntoleranceDao.getSearchResults(any(), any(), anyInt(), anyInt())).thenReturn(allergies);
		when(allergyIntoleranceDao.getSearchResultUuids(any())).thenReturn(Collections.singletonList(ALLERGY_UUID));
		when(searchQuery.getQueryResults(any(), any(), any())).thenReturn(
		    new SearchQueryBundleProvider<>(theParams, allergyIntoleranceDao, translator, globalPropertyService));
		when(translator.toFhirResource(omrsAllergy)).thenReturn(fhirAllergy);
		
		IBundleProvider results = service.searchForAllergies(patientParam, null, null, null, null, null, null, null, null);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList, hasSize(greaterThanOrEqualTo(1)));
		assertThat(resultList, not(empty()));
	}
	
	@Test
	public void searchForAllergies_shouldSearchForAllergiesByPatientName() {
		List<Allergy> allergies = new ArrayList<>();
		allergies.add(omrsAllergy);
		ReferenceAndListParam patientParam = new ReferenceAndListParam();
		ReferenceParam referenceParam = new ReferenceParam();
		
		referenceParam.setValue("John Doe");
		referenceParam.setChain(Patient.SP_NAME);
		
		patientParam.addValue(new ReferenceOrListParam().add(referenceParam));
		
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.PATIENT_REFERENCE_SEARCH_HANDLER,
		    patientParam);
		
		when(allergyIntoleranceDao.getSearchResults(any(), any(), anyInt(), anyInt())).thenReturn(allergies);
		when(allergyIntoleranceDao.getSearchResultUuids(any())).thenReturn(Collections.singletonList(ALLERGY_UUID));
		when(searchQuery.getQueryResults(any(), any(), any())).thenReturn(
		    new SearchQueryBundleProvider<>(theParams, allergyIntoleranceDao, translator, globalPropertyService));
		when(translator.toFhirResource(omrsAllergy)).thenReturn(fhirAllergy);
		
		IBundleProvider results = service.searchForAllergies(patientParam, null, null, null, null, null, null, null, null);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList, hasSize(greaterThanOrEqualTo(1)));
		assertThat(resultList, not(empty()));
	}
	
	@Test
	public void searchForAllergies_shouldSearchForAllergiesByCategory() {
		List<Allergy> allergies = new ArrayList<>();
		allergies.add(omrsAllergy);
		
		TokenAndListParam category = new TokenAndListParam();
		category.addAnd(new TokenOrListParam().addOr(new TokenParam().setValue("food")));
		
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.CATEGORY_SEARCH_HANDLER,
		    category);
		
		when(allergyIntoleranceDao.getSearchResults(any(), any(), anyInt(), anyInt())).thenReturn(allergies);
		when(allergyIntoleranceDao.getSearchResultUuids(any())).thenReturn(Collections.singletonList(ALLERGY_UUID));
		when(searchQuery.getQueryResults(any(), any(), any())).thenReturn(
		    new SearchQueryBundleProvider<>(theParams, allergyIntoleranceDao, translator, globalPropertyService));
		when(translator.toFhirResource(omrsAllergy)).thenReturn(fhirAllergy);
		
		IBundleProvider results = service.searchForAllergies(null, category, null, null, null, null, null, null, null);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList, not(empty()));
		assertThat(resultList, hasSize(greaterThanOrEqualTo(1)));
	}
	
	@Test
	public void searchForAllergies_shouldSearchForAllergiesByAllergen() {
		List<Allergy> allergies = new ArrayList<>();
		allergies.add(omrsAllergy);
		
		TokenAndListParam allergen = new TokenAndListParam();
		allergen.addAnd(new TokenOrListParam().addOr(new TokenParam().setValue(CODED_ALLERGEN_UUID)));
		
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.ALLERGEN_SEARCH_HANDLER,
		    allergen);
		
		when(allergyIntoleranceDao.getSearchResults(any(), any(), anyInt(), anyInt())).thenReturn(allergies);
		when(allergyIntoleranceDao.getSearchResultUuids(any())).thenReturn(Collections.singletonList(ALLERGY_UUID));
		when(searchQuery.getQueryResults(any(), any(), any())).thenReturn(
		    new SearchQueryBundleProvider<>(theParams, allergyIntoleranceDao, translator, globalPropertyService));
		when(translator.toFhirResource(omrsAllergy)).thenReturn(fhirAllergy);
		
		IBundleProvider results = service.searchForAllergies(null, null, allergen, null, null, null, null, null, null);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList, hasSize(greaterThanOrEqualTo(1)));
		assertThat(resultList, not(empty()));
	}
	
	@Test
	public void searchForAllergies_shouldSearchForAllergiesBySeverity() {
		List<Allergy> allergies = new ArrayList<>();
		allergies.add(omrsAllergy);
		
		TokenAndListParam severity = new TokenAndListParam();
		severity.addAnd(new TokenOrListParam().addOr(new TokenParam().setValue(SEVERITY_CONCEPT_UUID)));
		
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.SEVERITY_SEARCH_HANDLER,
		    severity);
		
		when(allergyIntoleranceDao.getSearchResults(any(), any(), anyInt(), anyInt())).thenReturn(allergies);
		when(allergyIntoleranceDao.getSearchResultUuids(any())).thenReturn(Collections.singletonList(ALLERGY_UUID));
		when(searchQuery.getQueryResults(any(), any(), any())).thenReturn(
		    new SearchQueryBundleProvider<>(theParams, allergyIntoleranceDao, translator, globalPropertyService));
		when(translator.toFhirResource(omrsAllergy)).thenReturn(fhirAllergy);
		
		IBundleProvider results = service.searchForAllergies(null, null, null, severity, null, null, null, null, null);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList, not(empty()));
		assertThat(resultList, hasSize(greaterThanOrEqualTo(1)));
	}
	
	@Test
	public void searchForAllergies_shouldSearchForAllergiesByManifestation() {
		List<Allergy> allergies = new ArrayList<>();
		allergies.add(omrsAllergy);
		
		TokenAndListParam manifestation = new TokenAndListParam();
		manifestation.addAnd(new TokenOrListParam().addOr(new TokenParam().setValue(CODED_REACTION_UUID)));
		
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.CODED_SEARCH_HANDLER,
		    manifestation);
		
		when(allergyIntoleranceDao.getSearchResults(any(), any(), anyInt(), anyInt())).thenReturn(allergies);
		when(allergyIntoleranceDao.getSearchResultUuids(any())).thenReturn(Collections.singletonList(ALLERGY_UUID));
		when(searchQuery.getQueryResults(any(), any(), any())).thenReturn(
		    new SearchQueryBundleProvider<>(theParams, allergyIntoleranceDao, translator, globalPropertyService));
		when(translator.toFhirResource(omrsAllergy)).thenReturn(fhirAllergy);
		
		IBundleProvider results = service.searchForAllergies(null, null, null, null, manifestation, null, null, null, null);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList, not(empty()));
		assertThat(resultList, hasSize(greaterThanOrEqualTo(1)));
	}
	
	@Test
	public void searchForAllergies_shouldSearchForAllergiesByClinicalStatus() {
		List<Allergy> allergies = new ArrayList<>();
		allergies.add(omrsAllergy);
		
		TokenAndListParam status = new TokenAndListParam();
		status.addAnd(new TokenOrListParam().addOr(new TokenParam().setValue("active")));
		
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.BOOLEAN_SEARCH_HANDLER, status);
		
		when(allergyIntoleranceDao.getSearchResults(any(), any(), anyInt(), anyInt())).thenReturn(allergies);
		when(allergyIntoleranceDao.getSearchResultUuids(any())).thenReturn(Collections.singletonList(ALLERGY_UUID));
		when(searchQuery.getQueryResults(any(), any(), any())).thenReturn(
		    new SearchQueryBundleProvider<>(theParams, allergyIntoleranceDao, translator, globalPropertyService));
		when(translator.toFhirResource(omrsAllergy)).thenReturn(fhirAllergy);
		
		IBundleProvider results = service.searchForAllergies(null, null, null, null, null, status, null, null, null);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList, not(empty()));
		assertThat(resultList, hasSize(greaterThanOrEqualTo(1)));
	}
	
	@Test
	public void searchForAllergies_shouldSearchForAllergiesByUUID() {
		TokenAndListParam uuid = new TokenAndListParam().addAnd(new TokenParam(ALLERGY_UUID));
		
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.COMMON_SEARCH_HANDLER,
		    FhirConstants.ID_PROPERTY, uuid);
		
		when(allergyIntoleranceDao.getSearchResults(any(), any(), anyInt(), anyInt()))
		        .thenReturn(Collections.singletonList(omrsAllergy));
		when(allergyIntoleranceDao.getSearchResultUuids(any())).thenReturn(Collections.singletonList(ALLERGY_UUID));
		when(searchQuery.getQueryResults(any(), any(), any())).thenReturn(
		    new SearchQueryBundleProvider<>(theParams, allergyIntoleranceDao, translator, globalPropertyService));
		when(translator.toFhirResource(omrsAllergy)).thenReturn(fhirAllergy);
		
		IBundleProvider results = service.searchForAllergies(null, null, null, null, null, null, uuid, null, null);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList, not(empty()));
		assertThat(resultList, hasSize(greaterThanOrEqualTo(1)));
	}
	
	@Test
	public void searchForAllergies_shouldSearchForAllergiesByLastUpdated() {
		DateRangeParam lastUpdated = new DateRangeParam().setUpperBound(LAST_UPDATED_DATE).setLowerBound(LAST_UPDATED_DATE);
		
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.COMMON_SEARCH_HANDLER,
		    FhirConstants.LAST_UPDATED_PROPERTY, lastUpdated);
		
		when(allergyIntoleranceDao.getSearchResults(any(), any(), anyInt(), anyInt()))
		        .thenReturn(Collections.singletonList(omrsAllergy));
		when(allergyIntoleranceDao.getSearchResultUuids(any())).thenReturn(Collections.singletonList(ALLERGY_UUID));
		when(searchQuery.getQueryResults(any(), any(), any())).thenReturn(
		    new SearchQueryBundleProvider<>(theParams, allergyIntoleranceDao, translator, globalPropertyService));
		when(translator.toFhirResource(omrsAllergy)).thenReturn(fhirAllergy);
		
		IBundleProvider results = service.searchForAllergies(null, null, null, null, null, null, null, lastUpdated, null);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList, not(empty()));
		assertThat(resultList, hasSize(greaterThanOrEqualTo(1)));
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
