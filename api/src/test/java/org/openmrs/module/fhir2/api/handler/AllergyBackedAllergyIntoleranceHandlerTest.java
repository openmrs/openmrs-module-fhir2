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
import org.openmrs.module.fhir2.api.search.SearchQueryInclude;
import org.openmrs.module.fhir2.api.search.param.SearchParameterMap;
import org.openmrs.module.fhir2.api.translators.AllergyIntoleranceTranslator;

/**
 * Tests the CRUD/search wiring and dispatch predicates of the default AllergyIntolerance handler.
 * Orchestrator-level concerns (fan-out, the typed {@code searchForAllergies} entry point) live in
 * {@code FhirAllergyIntoleranceServiceImplTest}.
 */
@RunWith(MockitoJUnitRunner.class)
public class AllergyBackedAllergyIntoleranceHandlerTest {
	
	private static final String ALLERGY_UUID = "1085AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
	
	private static final String WRONG_ALLERGY_UUID = "2085AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
	
	private static final String CODED_ALLERGEN_UUID = "5085AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
	
	private static final String SEVERITY_CONCEPT_UUID = "5088AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
	
	private static final String CODED_REACTION_UUID = "5087AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
	
	private static final String LAST_UPDATED_DATE = "2020-09-03";
	
	private static final int START_INDEX = 0;
	
	private static final int END_INDEX = 10;
	
	@Mock
	private FhirAllergyIntoleranceDao dao;
	
	@Mock
	private AllergyIntoleranceTranslator translator;
	
	@Mock
	private FhirGlobalPropertyService globalPropertyService;
	
	@Mock
	private SearchQueryInclude<AllergyIntolerance> searchQueryInclude;
	
	@Mock
	private SearchQuery<Allergy, AllergyIntolerance, FhirAllergyIntoleranceDao, AllergyIntoleranceTranslator, SearchQueryInclude<AllergyIntolerance>> searchQuery;
	
	private AllergyBackedAllergyIntoleranceHandler handler;
	
	private Allergy omrsAllergy;
	
	private AllergyIntolerance fhirAllergy;
	
	@Before
	public void setup() {
		handler = new AllergyBackedAllergyIntoleranceHandler() {
			
			@Override
			protected void validateObject(Allergy object) {
			}
		};
		
		handler.setTranslator(translator);
		handler.setDao(dao);
		handler.setSearchQuery(searchQuery);
		handler.setSearchQueryInclude(searchQueryInclude);
		
		omrsAllergy = new Allergy();
		omrsAllergy.setUuid(ALLERGY_UUID);
		
		fhirAllergy = new AllergyIntolerance();
		fhirAllergy.setId(ALLERGY_UUID);
	}
	
	private List<IBaseResource> get(IBundleProvider results) {
		return results.getResources(START_INDEX, END_INDEX);
	}
	
	private IBundleProvider stubbedBundle(SearchParameterMap theParams) {
		when(dao.getSearchResults(any())).thenReturn(Collections.singletonList(omrsAllergy));
		when(searchQuery.getQueryResults(any(), any(), any(), any())).thenReturn(
		    new SearchQueryBundleProvider<>(theParams, dao, translator, globalPropertyService, searchQueryInclude));
		when(searchQueryInclude.getIncludedResources(any(), any())).thenReturn(Collections.emptySet());
		when(translator.toFhirResource(omrsAllergy)).thenReturn(fhirAllergy);
		when(translator.toFhirResources(anyCollection())).thenCallRealMethod();
		return handler.search(theParams);
	}
	
	// ---- dispatch predicates ----
	
	@Test
	public void shouldExposeAllergyIntoleranceImplicitProfile() {
		assertThat(handler.getImplicitProfile(),
		    equalTo("http://fhir.openmrs.org/StructureDefinition/openmrs-allergyintolerance"));
	}
	
	@Test
	public void canHandle_shouldAlwaysReturnTrue() {
		assertTrue(handler.canHandle(new AllergyIntolerance()));
	}
	
	@Test
	public void acceptsSearch_shouldAcceptAnySearch() {
		assertTrue(handler.acceptsSearch(new SearchParameterMap()));
	}
	
	// ---- get / create ----
	
	@Test
	public void get_shouldGetAllergyIntoleranceByUuid() {
		when(dao.get(ALLERGY_UUID)).thenReturn(omrsAllergy);
		when(translator.toFhirResource(omrsAllergy)).thenReturn(fhirAllergy);
		
		AllergyIntolerance result = handler.get(ALLERGY_UUID);
		assertThat(result, notNullValue());
		assertThat(result.getId(), equalTo(ALLERGY_UUID));
	}
	
	@Test
	public void get_shouldThrowResourceNotFoundWhenCalledWithWrongUuid() {
		assertThrows(ResourceNotFoundException.class, () -> handler.get(WRONG_ALLERGY_UUID));
	}
	
	@Test
	public void create_shouldSaveNewAllergy() {
		when(translator.toFhirResource(omrsAllergy)).thenReturn(fhirAllergy);
		when(translator.toOpenmrsType(fhirAllergy)).thenReturn(omrsAllergy);
		when(dao.createOrUpdate(omrsAllergy)).thenReturn(omrsAllergy);
		
		AllergyIntolerance result = handler.create(fhirAllergy);
		assertThat(result, notNullValue());
		assertThat(result.getId(), equalTo(ALLERGY_UUID));
	}
	
	// ---- search ----
	
	@Test
	public void search_shouldSearchForAllergiesByPatientIdentifier() {
		ReferenceAndListParam patientParam = new ReferenceAndListParam();
		patientParam.addValue(
		    new ReferenceOrListParam().add(new ReferenceParam().setValue("M4001-1").setChain(Patient.SP_IDENTIFIER)));
		
		List<IBaseResource> resultList = get(stubbedBundle(
		    new SearchParameterMap().addParameter(FhirConstants.PATIENT_REFERENCE_SEARCH_HANDLER, patientParam)));
		
		assertThat(resultList, not(empty()));
		assertThat(resultList, hasSize(greaterThanOrEqualTo(1)));
	}
	
	@Test
	public void search_shouldSearchForAllergiesByPatientName() {
		ReferenceAndListParam patientParam = new ReferenceAndListParam();
		patientParam.addValue(
		    new ReferenceOrListParam().add(new ReferenceParam().setValue("John Doe").setChain(Patient.SP_NAME)));
		
		List<IBaseResource> resultList = get(stubbedBundle(
		    new SearchParameterMap().addParameter(FhirConstants.PATIENT_REFERENCE_SEARCH_HANDLER, patientParam)));
		
		assertThat(resultList, not(empty()));
		assertThat(resultList, hasSize(greaterThanOrEqualTo(1)));
	}
	
	@Test
	public void search_shouldSearchForAllergiesByCategory() {
		TokenAndListParam category = new TokenAndListParam()
		        .addAnd(new TokenOrListParam().addOr(new TokenParam().setValue("food")));
		
		List<IBaseResource> resultList = get(
		    stubbedBundle(new SearchParameterMap().addParameter(FhirConstants.CATEGORY_SEARCH_HANDLER, category)));
		
		assertThat(resultList, not(empty()));
		assertThat(resultList, hasSize(greaterThanOrEqualTo(1)));
	}
	
	@Test
	public void search_shouldSearchForAllergiesByAllergen() {
		TokenAndListParam allergen = new TokenAndListParam()
		        .addAnd(new TokenOrListParam().addOr(new TokenParam().setValue(CODED_ALLERGEN_UUID)));
		
		List<IBaseResource> resultList = get(
		    stubbedBundle(new SearchParameterMap().addParameter(FhirConstants.ALLERGEN_SEARCH_HANDLER, allergen)));
		
		assertThat(resultList, not(empty()));
		assertThat(resultList, hasSize(greaterThanOrEqualTo(1)));
	}
	
	@Test
	public void search_shouldSearchForAllergiesBySeverity() {
		TokenAndListParam severity = new TokenAndListParam()
		        .addAnd(new TokenOrListParam().addOr(new TokenParam().setValue(SEVERITY_CONCEPT_UUID)));
		
		List<IBaseResource> resultList = get(
		    stubbedBundle(new SearchParameterMap().addParameter(FhirConstants.SEVERITY_SEARCH_HANDLER, severity)));
		
		assertThat(resultList, not(empty()));
		assertThat(resultList, hasSize(greaterThanOrEqualTo(1)));
	}
	
	@Test
	public void search_shouldSearchForAllergiesByManifestation() {
		TokenAndListParam manifestation = new TokenAndListParam()
		        .addAnd(new TokenOrListParam().addOr(new TokenParam().setValue(CODED_REACTION_UUID)));
		
		List<IBaseResource> resultList = get(
		    stubbedBundle(new SearchParameterMap().addParameter(FhirConstants.CODED_SEARCH_HANDLER, manifestation)));
		
		assertThat(resultList, not(empty()));
		assertThat(resultList, hasSize(greaterThanOrEqualTo(1)));
	}
	
	@Test
	public void search_shouldSearchForAllergiesByClinicalStatus() {
		TokenAndListParam status = new TokenAndListParam()
		        .addAnd(new TokenOrListParam().addOr(new TokenParam().setValue("active")));
		
		List<IBaseResource> resultList = get(
		    stubbedBundle(new SearchParameterMap().addParameter(FhirConstants.BOOLEAN_SEARCH_HANDLER, status)));
		
		assertThat(resultList, not(empty()));
		assertThat(resultList, hasSize(greaterThanOrEqualTo(1)));
	}
	
	@Test
	public void search_shouldSearchForAllergiesByUUID() {
		TokenAndListParam uuid = new TokenAndListParam().addAnd(new TokenParam(ALLERGY_UUID));
		
		List<IBaseResource> resultList = get(stubbedBundle(
		    new SearchParameterMap().addParameter(FhirConstants.COMMON_SEARCH_HANDLER, FhirConstants.ID_PROPERTY, uuid)));
		
		assertThat(resultList, not(empty()));
		assertThat(resultList, hasSize(greaterThanOrEqualTo(1)));
	}
	
	@Test
	public void search_shouldSearchForAllergiesByLastUpdated() {
		DateRangeParam lastUpdated = new DateRangeParam().setUpperBound(LAST_UPDATED_DATE).setLowerBound(LAST_UPDATED_DATE);
		
		List<IBaseResource> resultList = get(stubbedBundle(new SearchParameterMap()
		        .addParameter(FhirConstants.COMMON_SEARCH_HANDLER, FhirConstants.LAST_UPDATED_PROPERTY, lastUpdated)));
		
		assertThat(resultList, not(empty()));
		assertThat(resultList.size(), greaterThanOrEqualTo(1));
	}
	
	@Test
	public void search_shouldAddPatientsToReturnedResultsWhenIncluded() {
		HashSet<Include> includes = new HashSet<>();
		includes.add(new Include("AllergyIntolerance:patient"));
		
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.INCLUDE_SEARCH_HANDLER, includes);
		
		when(dao.getSearchResults(any())).thenReturn(Collections.singletonList(omrsAllergy));
		when(searchQuery.getQueryResults(any(), any(), any(), any())).thenReturn(
		    new SearchQueryBundleProvider<>(theParams, dao, translator, globalPropertyService, searchQueryInclude));
		when(searchQueryInclude.getIncludedResources(any(), any())).thenReturn(Collections.singleton(new Patient()));
		when(translator.toFhirResource(omrsAllergy)).thenReturn(fhirAllergy);
		when(translator.toFhirResources(anyCollection())).thenCallRealMethod();
		
		List<IBaseResource> resultList = get(handler.search(theParams));
		
		assertThat(resultList, not(empty()));
		assertThat(resultList.size(), greaterThanOrEqualTo(2));
		assertThat(resultList, hasItem(is(instanceOf(Patient.class))));
	}
	
	@Test
	public void search_shouldNotAddPatientsToReturnedResultsForEmptyInclude() {
		List<IBaseResource> resultList = get(stubbedBundle(
		    new SearchParameterMap().addParameter(FhirConstants.INCLUDE_SEARCH_HANDLER, new HashSet<Include>())));
		
		assertThat(resultList, not(empty()));
		assertThat(resultList, hasSize(greaterThanOrEqualTo(1)));
	}
}
