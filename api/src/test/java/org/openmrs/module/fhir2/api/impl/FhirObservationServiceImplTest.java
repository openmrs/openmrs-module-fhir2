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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;

import ca.uhn.fhir.rest.api.server.IBundleProvider;
import ca.uhn.fhir.rest.param.NumberParam;
import ca.uhn.fhir.rest.param.ReferenceAndListParam;
import ca.uhn.fhir.rest.param.ReferenceOrListParam;
import ca.uhn.fhir.rest.param.ReferenceParam;
import ca.uhn.fhir.rest.param.StringParam;
import ca.uhn.fhir.rest.param.TokenAndListParam;
import ca.uhn.fhir.rest.param.TokenParam;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.Observation;
import org.hl7.fhir.r4.model.Patient;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openmrs.Obs;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.FhirTestConstants;
import org.openmrs.module.fhir2.api.FhirGlobalPropertyService;
import org.openmrs.module.fhir2.api.dao.FhirObservationDao;
import org.openmrs.module.fhir2.api.search.SearchQuery;
import org.openmrs.module.fhir2.api.search.SearchQueryBundleProvider;
import org.openmrs.module.fhir2.api.search.SearchQueryInclude;
import org.openmrs.module.fhir2.api.search.param.SearchParameterMap;
import org.openmrs.module.fhir2.api.translators.ObservationTranslator;

@RunWith(MockitoJUnitRunner.class)
public class FhirObservationServiceImplTest {
	
	private static final String OBS_UUID = "12345-abcde-12345";
	
	private static final String PATIENT_GIVEN_NAME = "Clement";
	
	private static final String PATIENT_UUID = "5946f880-b197-400b-9caa-a3c661d23041";
	
	private static final String CIEL_DIASTOLIC_BP = "5086";
	
	private static final String LOINC_SYSTOLIC_BP = "8480-6";
	
	@Mock
	private FhirObservationDao dao;
	
	@Mock
	private FhirGlobalPropertyService globalPropertyService;
	
	@Mock
	private SearchQueryInclude<Observation> searchQueryInclude;
	
	@Mock
	private SearchQuery<Obs, Observation, FhirObservationDao, ObservationTranslator, SearchQueryInclude<Observation>> searchQuery;
	
	@Mock
	private ObservationTranslator translator;
	
	private FhirObservationServiceImpl fhirObservationService;
	
	@Before
	public void setup() {
		fhirObservationService = new FhirObservationServiceImpl() {
			
			@Override
			protected void validateObject(Obs object) {
			}
		};
		
		fhirObservationService.setDao(dao);
		fhirObservationService.setSearchQuery(searchQuery);
		fhirObservationService.setTranslator(translator);
		fhirObservationService.setSearchQueryInclude(searchQueryInclude);
	}
	
	@Test
	public void getObservationByUuid_shouldReturnObservationByUuid() {
		Obs obs = new Obs();
		obs.setUuid(OBS_UUID);
		Observation observation = new Observation();
		observation.setId(OBS_UUID);
		when(dao.get(OBS_UUID)).thenReturn(obs);
		when(translator.toFhirResource(obs)).thenReturn(observation);
		
		Observation result = fhirObservationService.get(OBS_UUID);
		
		assertThat(result, notNullValue());
		assertThat(result.getId(), equalTo(OBS_UUID));
	}
	
	@Test
	public void searchForObservations_shouldReturnObservationsByParameters() {
		Obs obs = new Obs();
		obs.setUuid(OBS_UUID);
		
		Observation observation = new Observation();
		observation.setId(OBS_UUID);
		
		ReferenceAndListParam patientReference = new ReferenceAndListParam();
		ReferenceParam patient = new ReferenceParam();
		
		patient.setValue(PATIENT_GIVEN_NAME);
		patient.setChain(Patient.SP_GIVEN);
		
		patientReference.addValue(new ReferenceOrListParam().add(patient));
		SearchParameterMap theParams = new SearchParameterMap();
		theParams.addParameter(FhirConstants.PATIENT_REFERENCE_SEARCH_HANDLER, patientReference);
		
		when(globalPropertyService.getGlobalProperty(anyString(), anyInt())).thenReturn(10);
		when(dao.getSearchResultUuids(any())).thenReturn(Collections.singletonList(OBS_UUID));
		when(dao.getSearchResults(any(), any())).thenReturn(Collections.singletonList(obs));
		when(searchQuery.getQueryResults(any(), any(), any(), any())).thenReturn(
		    new SearchQueryBundleProvider<>(theParams, dao, translator, globalPropertyService, searchQueryInclude));
		when(searchQueryInclude.getIncludedResources(any(), any())).thenReturn(Collections.emptySet());
		when(translator.toFhirResource(obs)).thenReturn(observation);
		
		IBundleProvider results = fhirObservationService.searchForObservations(null, patientReference, null, null, null,
		    null, null, null, null, null, null, null, null, null, null);
		
		assertThat(results, notNullValue());
		assertThat(results.size(), equalTo(1));
		assertThat(results.preferredPageSize(), equalTo(10));
		
		List<IBaseResource> resultList = results.getResources(1, 10);
		
		assertThat(resultList, not(empty()));
		assertThat(resultList, hasSize(equalTo(1)));
	}
	
	@Test
	public void getLastnObservations_shouldReturnRecentNObservations() {
		Obs obs = new Obs();
		obs.setUuid(OBS_UUID);
		
		Observation observation = new Observation();
		observation.setId(OBS_UUID);
		
		NumberParam max = new NumberParam(2);
		ReferenceAndListParam referenceParam = new ReferenceAndListParam();
		ReferenceParam patient = new ReferenceParam();
		
		patient.setValue(PATIENT_UUID);
		
		referenceParam.addValue(new ReferenceOrListParam().add(patient));
		
		TokenAndListParam categories = new TokenAndListParam().addAnd(new TokenParam().setValue("laboratory"));
		
		TokenAndListParam code = new TokenAndListParam().addAnd(
		    new TokenParam().setSystem(FhirTestConstants.LOINC_SYSTEM_URL).setValue(LOINC_SYSTOLIC_BP),
		    new TokenParam().setSystem(FhirTestConstants.CIEL_SYSTEM_URN).setValue(CIEL_DIASTOLIC_BP));
		
		SearchParameterMap theParams = new SearchParameterMap()
		        .addParameter(FhirConstants.PATIENT_REFERENCE_SEARCH_HANDLER, referenceParam)
		        .addParameter(FhirConstants.CODED_SEARCH_HANDLER, code)
		        .addParameter(FhirConstants.CATEGORY_SEARCH_HANDLER, categories)
		        .addParameter(FhirConstants.MAX_SEARCH_HANDLER, max)
		        .addParameter(FhirConstants.LASTN_OBSERVATION_SEARCH_HANDLER, new StringParam());
		
		when(globalPropertyService.getGlobalProperty(anyString(), anyInt())).thenReturn(10);
		when(dao.getSearchResultUuids(any())).thenReturn(Collections.singletonList(OBS_UUID));
		when(dao.getSearchResults(any(), any())).thenReturn(Collections.singletonList(obs));
		when(searchQuery.getQueryResults(any(), any(), any(), any())).thenReturn(
		    new SearchQueryBundleProvider<>(theParams, dao, translator, globalPropertyService, searchQueryInclude));
		when(searchQueryInclude.getIncludedResources(any(), any())).thenReturn(Collections.emptySet());
		when(translator.toFhirResource(obs)).thenReturn(observation);
		
		IBundleProvider results = fhirObservationService.getLastnObservations(max, referenceParam, categories, code);
		
		assertThat(results, notNullValue());
		assertThat(results.size(), equalTo(1));
		assertThat(results.preferredPageSize(), equalTo(10));
		
		List<IBaseResource> resultList = results.getResources(1, 10);
		
		assertThat(resultList, not(empty()));
		assertThat(resultList, hasSize(equalTo(1)));
	}
	
	@Test
	public void getLastn_shouldReturnFirstRecentObservationsWhenMaxIsMissing() {
		Obs obs = new Obs();
		obs.setUuid(OBS_UUID);
		
		Observation observation = new Observation();
		observation.setId(OBS_UUID);
		
		NumberParam max = new NumberParam(1);
		ReferenceAndListParam referenceParam = new ReferenceAndListParam();
		ReferenceParam patient = new ReferenceParam();
		
		patient.setValue(PATIENT_UUID);
		
		referenceParam.addValue(new ReferenceOrListParam().add(patient));
		
		TokenAndListParam categories = new TokenAndListParam().addAnd(new TokenParam().setValue("laboratory"));
		
		TokenAndListParam code = new TokenAndListParam().addAnd(
		    new TokenParam().setSystem(FhirTestConstants.LOINC_SYSTEM_URL).setValue(LOINC_SYSTOLIC_BP),
		    new TokenParam().setSystem(FhirTestConstants.CIEL_SYSTEM_URN).setValue(CIEL_DIASTOLIC_BP));
		
		SearchParameterMap theParams = new SearchParameterMap()
		        .addParameter(FhirConstants.PATIENT_REFERENCE_SEARCH_HANDLER, referenceParam)
		        .addParameter(FhirConstants.CODED_SEARCH_HANDLER, code)
		        .addParameter(FhirConstants.CATEGORY_SEARCH_HANDLER, categories)
		        .addParameter(FhirConstants.MAX_SEARCH_HANDLER, max)
		        .addParameter(FhirConstants.LASTN_OBSERVATION_SEARCH_HANDLER, new StringParam());
		
		when(globalPropertyService.getGlobalProperty(anyString(), anyInt())).thenReturn(10);
		when(dao.getSearchResultUuids(any())).thenReturn(Collections.singletonList(OBS_UUID));
		when(dao.getSearchResults(any(), any())).thenReturn(Collections.singletonList(obs));
		when(searchQuery.getQueryResults(any(), any(), any(), any())).thenReturn(
		    new SearchQueryBundleProvider<>(theParams, dao, translator, globalPropertyService, searchQueryInclude));
		when(searchQueryInclude.getIncludedResources(any(), any())).thenReturn(Collections.emptySet());
		when(translator.toFhirResource(obs)).thenReturn(observation);
		
		IBundleProvider results = fhirObservationService.getLastnObservations(null, referenceParam, categories, code);
		
		assertThat(results, notNullValue());
		assertThat(results.size(), equalTo(1));
		assertThat(results.preferredPageSize(), equalTo(10));
		
		List<IBaseResource> resultList = results.getResources(1, 10);
		
		assertThat(resultList, not(empty()));
		assertThat(resultList, hasSize(equalTo(1)));
	}
	
	@Test
	public void getLastnObservations_shouldReturnRecentNObservationsForAllPatientsWhenNoPatientIsSpecified() {
		Obs obs = new Obs();
		obs.setUuid(OBS_UUID);
		
		Observation observation = new Observation();
		observation.setId(OBS_UUID);
		
		NumberParam max = new NumberParam(2);
		
		TokenAndListParam categories = new TokenAndListParam().addAnd(new TokenParam().setValue("laboratory"));
		
		TokenAndListParam code = new TokenAndListParam().addAnd(
		    new TokenParam().setSystem(FhirTestConstants.LOINC_SYSTEM_URL).setValue(LOINC_SYSTOLIC_BP),
		    new TokenParam().setSystem(FhirTestConstants.CIEL_SYSTEM_URN).setValue(CIEL_DIASTOLIC_BP));
		
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.CODED_SEARCH_HANDLER, code)
		        .addParameter(FhirConstants.CATEGORY_SEARCH_HANDLER, categories)
		        .addParameter(FhirConstants.MAX_SEARCH_HANDLER, max)
		        .addParameter(FhirConstants.LASTN_OBSERVATION_SEARCH_HANDLER, new StringParam());
		
		when(globalPropertyService.getGlobalProperty(anyString(), anyInt())).thenReturn(10);
		when(dao.getSearchResultUuids(any())).thenReturn(Collections.singletonList(OBS_UUID));
		when(dao.getSearchResults(any(), any())).thenReturn(Collections.singletonList(obs));
		when(searchQuery.getQueryResults(any(), any(), any(), any())).thenReturn(
		    new SearchQueryBundleProvider<>(theParams, dao, translator, globalPropertyService, searchQueryInclude));
		when(searchQueryInclude.getIncludedResources(any(), any())).thenReturn(Collections.emptySet());
		when(translator.toFhirResource(obs)).thenReturn(observation);
		
		IBundleProvider results = fhirObservationService.getLastnObservations(max, null, categories, code);
		
		assertThat(results, notNullValue());
		assertThat(results.size(), equalTo(1));
		assertThat(results.preferredPageSize(), equalTo(10));
		
		List<IBaseResource> resultList = results.getResources(1, 10);
		
		assertThat(resultList, not(empty()));
		assertThat(resultList, hasSize(equalTo(1)));
	}
	
	@Test
	public void getLastnEncountersObservations_shouldReturnRecentNEncountersObservations() {
		Obs obs = new Obs();
		obs.setUuid(OBS_UUID);
		
		Observation observation = new Observation();
		observation.setId(OBS_UUID);
		
		NumberParam max = new NumberParam(2);
		ReferenceAndListParam referenceParam = new ReferenceAndListParam();
		ReferenceParam patient = new ReferenceParam();
		
		patient.setValue(PATIENT_UUID);
		
		referenceParam.addValue(new ReferenceOrListParam().add(patient));
		
		TokenAndListParam categories = new TokenAndListParam().addAnd(new TokenParam().setValue("laboratory"));
		
		TokenAndListParam code = new TokenAndListParam().addAnd(
		    new TokenParam().setSystem(FhirTestConstants.LOINC_SYSTEM_URL).setValue(LOINC_SYSTOLIC_BP),
		    new TokenParam().setSystem(FhirTestConstants.CIEL_SYSTEM_URN).setValue(CIEL_DIASTOLIC_BP));
		
		SearchParameterMap theParams = new SearchParameterMap()
		        .addParameter(FhirConstants.PATIENT_REFERENCE_SEARCH_HANDLER, referenceParam)
		        .addParameter(FhirConstants.CODED_SEARCH_HANDLER, code)
		        .addParameter(FhirConstants.CATEGORY_SEARCH_HANDLER, categories)
		        .addParameter(FhirConstants.MAX_SEARCH_HANDLER, max)
		        .addParameter(FhirConstants.LASTN_OBSERVATION_SEARCH_HANDLER, new StringParam());
		
		when(globalPropertyService.getGlobalProperty(anyString(), anyInt())).thenReturn(10);
		when(dao.getSearchResultUuids(any())).thenReturn(Collections.singletonList(OBS_UUID));
		when(dao.getSearchResults(any(), any())).thenReturn(Collections.singletonList(obs));
		when(searchQuery.getQueryResults(any(), any(), any(), any())).thenReturn(
		    new SearchQueryBundleProvider<>(theParams, dao, translator, globalPropertyService, searchQueryInclude));
		when(searchQueryInclude.getIncludedResources(any(), any())).thenReturn(Collections.emptySet());
		when(translator.toFhirResource(obs)).thenReturn(observation);
		
		IBundleProvider results = fhirObservationService.getLastnEncountersObservations(max, referenceParam, categories,
		    code);
		
		assertThat(results, notNullValue());
		assertThat(results.size(), equalTo(1));
		assertThat(results.preferredPageSize(), equalTo(10));
		
		List<IBaseResource> resultList = results.getResources(1, 10);
		
		assertThat(resultList, not(empty()));
		assertThat(resultList, hasSize(equalTo(1)));
	}
	
	@Test
	public void getLastnEncountersObservations_shouldReturnFirstRecentEncountersObservationsWhenMaxIsMissing() {
		Obs obs = new Obs();
		obs.setUuid(OBS_UUID);
		
		Observation observation = new Observation();
		observation.setId(OBS_UUID);
		
		NumberParam max = new NumberParam(1);
		ReferenceAndListParam referenceParam = new ReferenceAndListParam();
		ReferenceParam patient = new ReferenceParam();
		
		patient.setValue(PATIENT_UUID);
		
		referenceParam.addValue(new ReferenceOrListParam().add(patient));
		
		TokenAndListParam categories = new TokenAndListParam().addAnd(new TokenParam().setValue("laboratory"));
		
		TokenAndListParam code = new TokenAndListParam().addAnd(
		    new TokenParam().setSystem(FhirTestConstants.LOINC_SYSTEM_URL).setValue(LOINC_SYSTOLIC_BP),
		    new TokenParam().setSystem(FhirTestConstants.CIEL_SYSTEM_URN).setValue(CIEL_DIASTOLIC_BP));
		
		SearchParameterMap theParams = new SearchParameterMap()
		        .addParameter(FhirConstants.PATIENT_REFERENCE_SEARCH_HANDLER, referenceParam)
		        .addParameter(FhirConstants.CODED_SEARCH_HANDLER, code)
		        .addParameter(FhirConstants.CATEGORY_SEARCH_HANDLER, categories)
		        .addParameter(FhirConstants.MAX_SEARCH_HANDLER, max)
		        .addParameter(FhirConstants.LASTN_OBSERVATION_SEARCH_HANDLER, new StringParam());
		
		when(globalPropertyService.getGlobalProperty(anyString(), anyInt())).thenReturn(10);
		when(dao.getSearchResultUuids(any())).thenReturn(Collections.singletonList(OBS_UUID));
		when(dao.getSearchResults(any(), any())).thenReturn(Collections.singletonList(obs));
		when(searchQuery.getQueryResults(any(), any(), any(), any())).thenReturn(
		    new SearchQueryBundleProvider<>(theParams, dao, translator, globalPropertyService, searchQueryInclude));
		when(searchQueryInclude.getIncludedResources(any(), any())).thenReturn(Collections.emptySet());
		when(translator.toFhirResource(obs)).thenReturn(observation);
		
		IBundleProvider results = fhirObservationService.getLastnEncountersObservations(null, referenceParam, categories,
		    code);
		
		assertThat(results, notNullValue());
		assertThat(results.size(), equalTo(1));
		assertThat(results.preferredPageSize(), equalTo(10));
		
		List<IBaseResource> resultList = results.getResources(1, 10);
		
		assertThat(resultList, not(empty()));
		assertThat(resultList, hasSize(equalTo(1)));
	}
	
	@Test
	public void getLastnEncountersObservations_shouldReturnRecentNEncountersObservationsForAllPatientsWhenNoPatientIsSpecified() {
		Obs obs = new Obs();
		obs.setUuid(OBS_UUID);
		
		Observation observation = new Observation();
		observation.setId(OBS_UUID);
		
		NumberParam max = new NumberParam(2);
		
		TokenAndListParam categories = new TokenAndListParam().addAnd(new TokenParam().setValue("laboratory"));
		
		TokenAndListParam code = new TokenAndListParam().addAnd(
		    new TokenParam().setSystem(FhirTestConstants.LOINC_SYSTEM_URL).setValue(LOINC_SYSTOLIC_BP),
		    new TokenParam().setSystem(FhirTestConstants.CIEL_SYSTEM_URN).setValue(CIEL_DIASTOLIC_BP));
		
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.CODED_SEARCH_HANDLER, code)
		        .addParameter(FhirConstants.CATEGORY_SEARCH_HANDLER, categories)
		        .addParameter(FhirConstants.MAX_SEARCH_HANDLER, max)
		        .addParameter(FhirConstants.LASTN_OBSERVATION_SEARCH_HANDLER, new StringParam());
		
		when(globalPropertyService.getGlobalProperty(anyString(), anyInt())).thenReturn(10);
		when(dao.getSearchResultUuids(any())).thenReturn(Collections.singletonList(OBS_UUID));
		when(dao.getSearchResults(any(), any())).thenReturn(Collections.singletonList(obs));
		when(searchQuery.getQueryResults(any(), any(), any(), any())).thenReturn(
		    new SearchQueryBundleProvider<>(theParams, dao, translator, globalPropertyService, searchQueryInclude));
		when(searchQueryInclude.getIncludedResources(any(), any())).thenReturn(Collections.emptySet());
		when(translator.toFhirResource(obs)).thenReturn(observation);
		
		IBundleProvider results = fhirObservationService.getLastnEncountersObservations(max, null, categories, code);
		
		assertThat(results, notNullValue());
		assertThat(results.size(), equalTo(1));
		assertThat(results.preferredPageSize(), equalTo(10));
		
		List<IBaseResource> resultList = results.getResources(1, 10);
		
		assertThat(resultList, not(empty()));
		assertThat(resultList, hasSize(equalTo(1)));
	}
	
}
