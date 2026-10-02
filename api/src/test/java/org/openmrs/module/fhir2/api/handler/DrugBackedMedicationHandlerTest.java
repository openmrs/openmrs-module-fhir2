/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.fhir2.api.handler;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import ca.uhn.fhir.model.api.Include;
import ca.uhn.fhir.rest.api.server.IBundleProvider;
import ca.uhn.fhir.rest.param.DateRangeParam;
import ca.uhn.fhir.rest.param.TokenAndListParam;
import ca.uhn.fhir.rest.param.TokenOrListParam;
import ca.uhn.fhir.rest.param.TokenParam;
import ca.uhn.fhir.rest.server.exceptions.InvalidRequestException;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.Medication;
import org.hl7.fhir.r4.model.MedicationRequest;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openmrs.Drug;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.api.FhirGlobalPropertyService;
import org.openmrs.module.fhir2.api.dao.FhirMedicationDao;
import org.openmrs.module.fhir2.api.search.SearchQuery;
import org.openmrs.module.fhir2.api.search.SearchQueryBundleProvider;
import org.openmrs.module.fhir2.api.search.SearchQueryInclude;
import org.openmrs.module.fhir2.api.search.param.SearchParameterMap;
import org.openmrs.module.fhir2.api.translators.MedicationTranslator;

/**
 * Tests the CRUD/search wiring and dispatch predicates of the default Medication handler.
 * Orchestrator-level concerns (fan-out, the typed {@code searchForMedications} entry point) live in
 * {@code FhirMedicationServiceImplTest}.
 */
@RunWith(MockitoJUnitRunner.class)
public class DrugBackedMedicationHandlerTest {
	
	private static final String MEDICATION_UUID = "1359f03d-55d9-4961-b8f8-9a59eddc1f59";
	
	private static final String WRONG_MEDICATION_UUID = "02ed36f0-6167-4372-a641-d27b92f7deae";
	
	private static final String CODE = "5087AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
	
	private static final String LAST_UPDATED_DATE = "2020-09-03";
	
	private static final int START_INDEX = 0;
	
	private static final int END_INDEX = 10;
	
	@Mock
	private MedicationTranslator translator;
	
	@Mock
	private FhirMedicationDao dao;
	
	@Mock
	private FhirGlobalPropertyService globalPropertyService;
	
	@Mock
	private SearchQueryInclude<Medication> searchQueryInclude;
	
	@Mock
	private SearchQuery<Drug, Medication, FhirMedicationDao, MedicationTranslator, SearchQueryInclude<Medication>> searchQuery;
	
	private DrugBackedMedicationHandler handler;
	
	private Medication medication;
	
	private Drug drug;
	
	@Before
	public void setup() {
		handler = new DrugBackedMedicationHandler() {
			
			@Override
			protected void validateObject(Drug object) {
			}
		};
		
		handler.setTranslator(translator);
		handler.setDao(dao);
		handler.setSearchQuery(searchQuery);
		handler.setSearchQueryInclude(searchQueryInclude);
		
		medication = new Medication();
		medication.setId(MEDICATION_UUID);
		
		drug = new Drug();
		drug.setUuid(MEDICATION_UUID);
	}
	
	private List<IBaseResource> get(IBundleProvider results) {
		return results.getResources(START_INDEX, END_INDEX);
	}
	
	private IBundleProvider stubbedBundle(SearchParameterMap theParams) {
		when(dao.getSearchResults(any())).thenReturn(Collections.singletonList(drug));
		when(searchQuery.getQueryResults(any(), any(), any(), any())).thenReturn(
		    new SearchQueryBundleProvider<>(theParams, dao, translator, globalPropertyService, searchQueryInclude));
		when(searchQueryInclude.getIncludedResources(any(), any())).thenReturn(Collections.emptySet());
		when(translator.toFhirResource(drug)).thenReturn(medication);
		when(translator.toFhirResources(anyCollection())).thenCallRealMethod();
		return handler.search(theParams);
	}
	
	// ---- dispatch predicates ----
	
	@Test
	public void shouldExposeMedicationImplicitProfile() {
		assertThat(handler.getImplicitProfile(), equalTo("http://fhir.openmrs.org/StructureDefinition/openmrs-medication"));
	}
	
	@Test
	public void canHandle_shouldAlwaysReturnTrue() {
		assertTrue(handler.canHandle(new Medication()));
	}
	
	@Test
	public void acceptsSearch_shouldAcceptAnySearch() {
		assertTrue(handler.acceptsSearch(new SearchParameterMap()));
	}
	
	// ---- get ----
	
	@Test
	public void get_shouldGetMedicationByUuid() {
		when(dao.get(MEDICATION_UUID)).thenReturn(drug);
		when(translator.toFhirResource(drug)).thenReturn(medication);
		
		Medication result = handler.get(MEDICATION_UUID);
		assertThat(result, notNullValue());
		assertThat(result.getId(), equalTo(MEDICATION_UUID));
	}
	
	@Test
	public void get_shouldThrowResourceNotFoundWhenCalledWithUnknownUuid() {
		assertThrows(ResourceNotFoundException.class, () -> handler.get(WRONG_MEDICATION_UUID));
	}
	
	// ---- create / update / delete ----
	
	@Test
	public void create_shouldSaveNewMedication() {
		when(translator.toFhirResource(drug)).thenReturn(medication);
		when(translator.toOpenmrsType(medication)).thenReturn(drug);
		when(dao.createOrUpdate(drug)).thenReturn(drug);
		
		Medication result = handler.create(medication);
		assertThat(result, notNullValue());
		assertThat(result.getId(), equalTo(MEDICATION_UUID));
	}
	
	@Test(expected = InvalidRequestException.class)
	public void update_shouldThrowInvalidRequestExceptionIfIdIsNull() {
		handler.update(null, medication);
	}
	
	@Test(expected = InvalidRequestException.class)
	public void update_shouldThrowInvalidRequestExceptionWhenIdMismatch() {
		handler.update(WRONG_MEDICATION_UUID, medication);
	}
	
	@Test(expected = ResourceNotFoundException.class)
	public void update_shouldThrowResourceNotFoundWhenMedicationMissing() {
		handler.update(MEDICATION_UUID, medication);
	}
	
	@Test
	public void update_shouldUpdateMedication() {
		medication.setStatus(Medication.MedicationStatus.INACTIVE);
		
		when(dao.get(MEDICATION_UUID)).thenReturn(drug);
		when(translator.toFhirResource(drug)).thenReturn(medication);
		when(translator.toOpenmrsType(drug, medication)).thenReturn(drug);
		when(dao.createOrUpdate(drug)).thenReturn(drug);
		
		Medication result = handler.update(MEDICATION_UUID, medication);
		assertThat(result, notNullValue());
		assertThat(result.getStatus(), equalTo(Medication.MedicationStatus.INACTIVE));
	}
	
	@Test
	public void delete_shouldDeleteMedication() {
		when(dao.delete(MEDICATION_UUID)).thenReturn(drug);
		
		handler.delete(MEDICATION_UUID);
	}
	
	// ---- search ----
	
	@Test
	public void search_shouldSearchMedicationsByCode() {
		TokenAndListParam code = new TokenAndListParam()
		        .addAnd(new TokenOrListParam().addOr(new TokenParam().setValue(CODE)));
		
		List<IBaseResource> resultList = get(
		    stubbedBundle(new SearchParameterMap().addParameter(FhirConstants.CODED_SEARCH_HANDLER, code)));
		
		assertThat(resultList, not(empty()));
		assertThat(resultList, hasSize(greaterThanOrEqualTo(1)));
	}
	
	@Test
	public void search_shouldSearchMedicationsByDosageForm() {
		TokenAndListParam dosageForm = new TokenAndListParam()
		        .addAnd(new TokenOrListParam().addOr(new TokenParam().setValue(CODE)));
		
		List<IBaseResource> resultList = get(
		    stubbedBundle(new SearchParameterMap().addParameter(FhirConstants.DOSAGE_FORM_SEARCH_HANDLER, dosageForm)));
		
		assertThat(resultList, not(empty()));
		assertThat(resultList, hasSize(greaterThanOrEqualTo(1)));
	}
	
	@Test
	public void search_shouldSearchMedicationsByIngredientCode() {
		TokenAndListParam ingredientCode = new TokenAndListParam()
		        .addAnd(new TokenOrListParam().addOr(new TokenParam().setValue(CODE)));
		
		List<IBaseResource> resultList = get(
		    stubbedBundle(new SearchParameterMap().addParameter(FhirConstants.INGREDIENT_SEARCH_HANDLER, ingredientCode)));
		
		assertThat(resultList, not(empty()));
		assertThat(resultList.size(), greaterThanOrEqualTo(1));
	}
	
	@Test
	public void search_shouldSearchMedicationsByUUID() {
		TokenAndListParam uuid = new TokenAndListParam().addAnd(new TokenParam(MEDICATION_UUID));
		
		List<IBaseResource> resultList = get(stubbedBundle(
		    new SearchParameterMap().addParameter(FhirConstants.COMMON_SEARCH_HANDLER, FhirConstants.ID_PROPERTY, uuid)));
		
		assertThat(resultList, not(empty()));
		assertThat(resultList, hasSize(greaterThanOrEqualTo(1)));
	}
	
	@Test
	public void search_shouldSearchMedicationsByLastUpdated() {
		DateRangeParam lastUpdated = new DateRangeParam().setUpperBound(LAST_UPDATED_DATE).setLowerBound(LAST_UPDATED_DATE);
		
		List<IBaseResource> resultList = get(stubbedBundle(new SearchParameterMap()
		        .addParameter(FhirConstants.COMMON_SEARCH_HANDLER, FhirConstants.LAST_UPDATED_PROPERTY, lastUpdated)));
		
		assertThat(resultList, not(empty()));
		assertThat(resultList, hasSize(greaterThanOrEqualTo(1)));
	}
	
	@Test
	public void search_shouldAddReverseIncludedResourcesToResultList() {
		HashSet<Include> revIncludes = new HashSet<>();
		revIncludes.add(new Include("MedicationRequest:medication"));
		
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.REVERSE_INCLUDE_SEARCH_HANDLER,
		    revIncludes);
		
		when(dao.getSearchResults(any())).thenReturn(Collections.singletonList(drug));
		when(searchQuery.getQueryResults(any(), any(), any(), any())).thenReturn(
		    new SearchQueryBundleProvider<>(theParams, dao, translator, globalPropertyService, searchQueryInclude));
		when(searchQueryInclude.getIncludedResources(any(), any()))
		        .thenReturn(Collections.singleton(new MedicationRequest()));
		when(translator.toFhirResource(drug)).thenReturn(medication);
		when(translator.toFhirResources(anyCollection())).thenCallRealMethod();
		
		List<IBaseResource> resultList = get(handler.search(theParams));
		
		assertThat(resultList, not(empty()));
		assertThat(resultList.size(), greaterThanOrEqualTo(2));
		assertThat(resultList, hasItem(is(instanceOf(MedicationRequest.class))));
	}
	
	@Test
	public void search_shouldNotAddRelatedResourcesToResultListForEmptyRevInclude() {
		List<IBaseResource> resultList = get(stubbedBundle(
		    new SearchParameterMap().addParameter(FhirConstants.REVERSE_INCLUDE_SEARCH_HANDLER, new HashSet<Include>())));
		
		assertThat(resultList, not(empty()));
		assertThat(resultList.size(), greaterThanOrEqualTo(1));
	}
}
