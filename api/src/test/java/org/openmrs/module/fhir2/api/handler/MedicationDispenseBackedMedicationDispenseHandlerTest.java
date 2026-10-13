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
import java.util.List;

import ca.uhn.fhir.rest.api.server.IBundleProvider;
import ca.uhn.fhir.rest.param.ReferenceAndListParam;
import ca.uhn.fhir.rest.param.ReferenceOrListParam;
import ca.uhn.fhir.rest.param.ReferenceParam;
import ca.uhn.fhir.rest.param.TokenAndListParam;
import ca.uhn.fhir.rest.param.TokenParam;
import ca.uhn.fhir.rest.server.exceptions.InvalidRequestException;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.MedicationDispense;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.api.FhirGlobalPropertyService;
import org.openmrs.module.fhir2.api.dao.FhirMedicationDispenseDao;
import org.openmrs.module.fhir2.api.search.SearchQuery;
import org.openmrs.module.fhir2.api.search.SearchQueryBundleProvider;
import org.openmrs.module.fhir2.api.search.SearchQueryInclude;
import org.openmrs.module.fhir2.api.search.param.SearchParameterMap;
import org.openmrs.module.fhir2.api.translators.MedicationDispenseTranslator;

/**
 * Tests the CRUD/search wiring and dispatch predicates of the default MedicationDispense handler.
 * Orchestrator-level concerns (fan-out, the typed {@code searchMedicationDispenses} entry point)
 * live in {@code FhirMedicationDispenseServiceImplTest}.
 */
@RunWith(MockitoJUnitRunner.class)
public class MedicationDispenseBackedMedicationDispenseHandlerTest {
	
	private static final String MEDICATION_DISPENSE_UUID = "43578769-f1a4-46af-b08b-d9fe8a07066f";
	
	private static final String NEW_DISPENSE_UUID = "a15e4988-d07a-11ec-8307-0242ac110002";
	
	private static final int START_INDEX = 0;
	
	private static final int END_INDEX = 10;
	
	@Mock
	private FhirMedicationDispenseDao<org.openmrs.MedicationDispense> dao;
	
	@Mock
	private MedicationDispenseTranslator<org.openmrs.MedicationDispense> translator;
	
	@Mock
	private FhirGlobalPropertyService globalPropertyService;
	
	@Mock
	private SearchQueryInclude<MedicationDispense> searchQueryInclude;
	
	@Mock
	private SearchQuery<org.openmrs.MedicationDispense, MedicationDispense, FhirMedicationDispenseDao<org.openmrs.MedicationDispense>, MedicationDispenseTranslator<org.openmrs.MedicationDispense>, SearchQueryInclude<MedicationDispense>> searchQuery;
	
	private MedicationDispenseBackedMedicationDispenseHandler handler;
	
	private org.openmrs.MedicationDispense openmrsDispense;
	
	private MedicationDispense fhirDispense;
	
	@Before
	public void setup() {
		handler = new MedicationDispenseBackedMedicationDispenseHandler() {
			
			@Override
			protected void validateObject(org.openmrs.MedicationDispense object) {
			}
		};
		handler.setDao(dao);
		handler.setTranslator(translator);
		handler.setSearchQuery(searchQuery);
		handler.setSearchQueryInclude(searchQueryInclude);
		
		openmrsDispense = new org.openmrs.MedicationDispense();
		openmrsDispense.setUuid(MEDICATION_DISPENSE_UUID);
		
		fhirDispense = new MedicationDispense();
		fhirDispense.setId(MEDICATION_DISPENSE_UUID);
	}
	
	// ---- dispatch predicates ----
	
	@Test
	public void shouldExposeMedicationDispenseImplicitProfile() {
		assertThat(handler.getImplicitProfile(),
		    equalTo("http://fhir.openmrs.org/StructureDefinition/openmrs-medicationdispense"));
	}
	
	@Test
	public void canHandle_shouldAlwaysReturnTrue() {
		assertTrue(handler.canHandle(new MedicationDispense()));
	}
	
	@Test
	public void acceptsSearch_shouldAcceptAnySearch() {
		assertTrue(handler.acceptsSearch(new SearchParameterMap()));
	}
	
	// ---- get ----
	
	@Test
	public void get_shouldGetMedicationDispenseByUuid() {
		when(dao.get(MEDICATION_DISPENSE_UUID)).thenReturn(openmrsDispense);
		when(translator.toFhirResource(openmrsDispense)).thenReturn(fhirDispense);
		
		MedicationDispense result = handler.get(MEDICATION_DISPENSE_UUID);
		
		assertThat(result, notNullValue());
		assertThat(result.getId(), equalTo(MEDICATION_DISPENSE_UUID));
	}
	
	@Test
	public void get_shouldThrowExceptionWhenMissingUuid() {
		assertThrows(ResourceNotFoundException.class, () -> handler.get(NEW_DISPENSE_UUID));
	}
	
	// ---- create / update / delete ----
	
	@Test
	public void create_shouldCreateNewMedicationDispense() {
		org.openmrs.MedicationDispense newOpenmrs = new org.openmrs.MedicationDispense();
		newOpenmrs.setUuid(NEW_DISPENSE_UUID);
		MedicationDispense newFhir = new MedicationDispense();
		newFhir.setId(NEW_DISPENSE_UUID);
		
		when(translator.toFhirResource(newOpenmrs)).thenReturn(newFhir);
		when(dao.createOrUpdate(newOpenmrs)).thenReturn(newOpenmrs);
		when(translator.toOpenmrsType(newFhir)).thenReturn(newOpenmrs);
		
		MedicationDispense result = handler.create(newFhir);
		
		assertThat(result, notNullValue());
		assertThat(result.getId(), equalTo(NEW_DISPENSE_UUID));
	}
	
	@Test
	public void update_shouldUpdateExistingMedicationDispense() {
		when(dao.get(MEDICATION_DISPENSE_UUID)).thenReturn(openmrsDispense);
		when(translator.toFhirResource(openmrsDispense)).thenReturn(fhirDispense);
		when(dao.createOrUpdate(openmrsDispense)).thenReturn(openmrsDispense);
		when(translator.toOpenmrsType(any(org.openmrs.MedicationDispense.class), any(MedicationDispense.class)))
		        .thenReturn(openmrsDispense);
		
		MedicationDispense result = handler.update(MEDICATION_DISPENSE_UUID, fhirDispense);
		
		assertThat(result, notNullValue());
		assertThat(result.getId(), equalTo(MEDICATION_DISPENSE_UUID));
	}
	
	@Test
	public void update_shouldThrowExceptionWhenIdIsNull() {
		assertThrows(InvalidRequestException.class, () -> handler.update(null, new MedicationDispense()));
	}
	
	@Test
	public void update_shouldThrowExceptionWhenMedicationDispenseIsNull() {
		assertThrows(InvalidRequestException.class, () -> handler.update(MEDICATION_DISPENSE_UUID, null));
	}
	
	@Test
	public void update_shouldThrowExceptionWhenMedicationDispenseIdIsNull() {
		assertThrows(InvalidRequestException.class,
		    () -> handler.update(MEDICATION_DISPENSE_UUID, new MedicationDispense()));
	}
	
	@Test
	public void update_shouldThrowExceptionWhenIdDoesNotMatchCurrentId() {
		MedicationDispense mismatched = new MedicationDispense();
		mismatched.setId(NEW_DISPENSE_UUID);
		assertThrows(InvalidRequestException.class, () -> handler.update(MEDICATION_DISPENSE_UUID, mismatched));
	}
	
	@Test
	public void delete_shouldDeleteExistingMedicationDispense() {
		when(dao.delete(MEDICATION_DISPENSE_UUID)).thenReturn(openmrsDispense);
		
		handler.delete(MEDICATION_DISPENSE_UUID);
	}
	
	@Test
	public void delete_shouldThrowExceptionWhenIdIsNull() {
		assertThrows(InvalidRequestException.class, () -> handler.delete(null));
	}
	
	// ---- search ----
	
	@Test
	public void search_shouldGetSearchResults() {
		ReferenceAndListParam patientParam = new ReferenceAndListParam();
		patientParam.addValue(new ReferenceOrListParam().add(new ReferenceParam("patient-ref")));
		TokenAndListParam idParam = new TokenAndListParam().addAnd(new TokenParam(MEDICATION_DISPENSE_UUID));
		
		SearchParameterMap theParams = new SearchParameterMap()
		        .addParameter(FhirConstants.COMMON_SEARCH_HANDLER, FhirConstants.ID_PROPERTY, idParam)
		        .addParameter(FhirConstants.PATIENT_REFERENCE_SEARCH_HANDLER, patientParam);
		
		when(dao.getSearchResults(any())).thenReturn(Collections.singletonList(openmrsDispense));
		when(searchQuery.getQueryResults(any(), any(), any(), any())).thenReturn(
		    new SearchQueryBundleProvider<>(theParams, dao, translator, globalPropertyService, searchQueryInclude));
		when(searchQueryInclude.getIncludedResources(any(), any())).thenReturn(Collections.emptySet());
		when(translator.toFhirResource(openmrsDispense)).thenReturn(fhirDispense);
		when(translator.toFhirResources(anyCollection())).thenCallRealMethod();
		
		IBundleProvider result = handler.search(theParams);
		List<IBaseResource> resultList = result.getResources(START_INDEX, END_INDEX);
		
		assertThat(result, notNullValue());
		assertThat(resultList, not(empty()));
		assertThat(resultList, hasSize(greaterThanOrEqualTo(1)));
	}
}
