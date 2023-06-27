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
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import ca.uhn.fhir.model.api.Include;
import ca.uhn.fhir.rest.api.SortSpec;
import ca.uhn.fhir.rest.api.server.IBundleProvider;
import ca.uhn.fhir.rest.param.DateRangeParam;
import ca.uhn.fhir.rest.param.ReferenceAndListParam;
import ca.uhn.fhir.rest.param.ReferenceOrListParam;
import ca.uhn.fhir.rest.param.ReferenceParam;
import ca.uhn.fhir.rest.param.TokenAndListParam;
import ca.uhn.fhir.rest.param.TokenParam;
import ca.uhn.fhir.rest.server.exceptions.InvalidRequestException;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openmrs.MedicationDispense;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.api.FhirGlobalPropertyService;
import org.openmrs.module.fhir2.api.dao.FhirMedicationDispenseDao;
import org.openmrs.module.fhir2.api.search.SearchQuery;
import org.openmrs.module.fhir2.api.search.SearchQueryBundleProvider;
import org.openmrs.module.fhir2.api.search.SearchQueryInclude;
import org.openmrs.module.fhir2.api.search.param.MedicationDispenseSearchParams;
import org.openmrs.module.fhir2.api.search.param.SearchParameterMap;
import org.openmrs.module.fhir2.api.translators.MedicationDispenseTranslator;

@RunWith(MockitoJUnitRunner.class)
public class FhirMedicationDispenseServiceImpl_2_6Test {
	
	private static final Integer MEDICATION_DISPENSE_ID = 123;
	
	private static final String MEDICATION_DISPENSE_UUID = "43578769-f1a4-46af-b08b-d9fe8a07066f";
	
	private static final String NEW_DISPENSE_UUID = "a15e4988-d07a-11ec-8307-0242ac110002";
	
	@Mock
	private FhirMedicationDispenseDao<MedicationDispense> dao;
	
	@Mock
	private MedicationDispenseTranslator<MedicationDispense> translator;
	
	@Mock
	private FhirGlobalPropertyService globalPropertyService;
	
	@Mock
	private SearchQueryInclude searchQueryInclude;
	
	@Mock
	private SearchQuery<MedicationDispense, org.hl7.fhir.r4.model.MedicationDispense, FhirMedicationDispenseDao<MedicationDispense>, MedicationDispenseTranslator<MedicationDispense>, SearchQueryInclude<org.hl7.fhir.r4.model.MedicationDispense>> searchQuery;
	
	private FhirMedicationDispenseServiceImpl_2_6 dispenseService;
	
	private MedicationDispense openmrsDispense;
	
	private org.hl7.fhir.r4.model.MedicationDispense fhirDispense;
	
	@Before
	public void setup() {
		dispenseService = new FhirMedicationDispenseServiceImpl_2_6() {
			
			@Override
			protected void validateObject(MedicationDispense object) {
			}
		};
		dispenseService.setDao(dao);
		dispenseService.setTranslator(translator);
		dispenseService.setSearchQuery(searchQuery);
		dispenseService.setSearchQueryInclude(searchQueryInclude);
		
		openmrsDispense = new MedicationDispense();
		openmrsDispense.setUuid(MEDICATION_DISPENSE_UUID);
		
		fhirDispense = new org.hl7.fhir.r4.model.MedicationDispense();
		fhirDispense.setId(MEDICATION_DISPENSE_UUID);
	}
	
	@Test
	public void shouldGetMedicationDispenseByUuid() {
		when(dao.get(MEDICATION_DISPENSE_UUID)).thenReturn(openmrsDispense);
		when(translator.toFhirResource(openmrsDispense)).thenReturn(fhirDispense);
		
		org.hl7.fhir.r4.model.MedicationDispense dispense = dispenseService.get(MEDICATION_DISPENSE_UUID);
		
		assertThat(dispense, notNullValue());
		assertThat(dispense.getId(), notNullValue());
		assertThat(dispense.getId(), equalTo(MEDICATION_DISPENSE_UUID));
	}
	
	@Test
	public void shouldThrowExceptionWhenGetMissingUuid() {
		assertThrows(ResourceNotFoundException.class, () -> dispenseService.get(NEW_DISPENSE_UUID));
	}
	
	@Test
	public void create_shouldCreateNewMedicationDispense() {
		MedicationDispense openmrsDispense = new MedicationDispense();
		openmrsDispense.setUuid(NEW_DISPENSE_UUID);
		
		org.hl7.fhir.r4.model.MedicationDispense fhirDispense = new org.hl7.fhir.r4.model.MedicationDispense();
		fhirDispense.setId(NEW_DISPENSE_UUID);
		
		when(translator.toFhirResource(openmrsDispense)).thenReturn(fhirDispense);
		when(dao.createOrUpdate(openmrsDispense)).thenReturn(openmrsDispense);
		when(translator.toOpenmrsType(fhirDispense)).thenReturn(openmrsDispense);
		
		org.hl7.fhir.r4.model.MedicationDispense result = dispenseService.create(fhirDispense);
		
		assertThat(result, notNullValue());
		assertThat(result.getId(), notNullValue());
		assertThat(result.getId(), equalTo(NEW_DISPENSE_UUID));
	}
	
	@Test
	public void update_shouldUpdateExistingMedicationDispense() {
		MedicationDispense openmrsDispense = new MedicationDispense();
		openmrsDispense.setUuid(MEDICATION_DISPENSE_UUID);
		
		org.hl7.fhir.r4.model.MedicationDispense fhirDispense = new org.hl7.fhir.r4.model.MedicationDispense();
		fhirDispense.setId(MEDICATION_DISPENSE_UUID);
		
		when(dao.get(MEDICATION_DISPENSE_UUID)).thenReturn(openmrsDispense);
		when(translator.toFhirResource(openmrsDispense)).thenReturn(fhirDispense);
		when(dao.createOrUpdate(openmrsDispense)).thenReturn(openmrsDispense);
		when(translator.toOpenmrsType(any(MedicationDispense.class), any(org.hl7.fhir.r4.model.MedicationDispense.class)))
		        .thenReturn(openmrsDispense);
		
		org.hl7.fhir.r4.model.MedicationDispense result = dispenseService.update(MEDICATION_DISPENSE_UUID, fhirDispense);
		
		assertThat(result, notNullValue());
		assertThat(result.getId(), notNullValue());
		assertThat(result.getId(), equalTo(MEDICATION_DISPENSE_UUID));
	}
	
	@Test
	public void update_shouldThrowExceptionWhenIdIsNull() {
		org.hl7.fhir.r4.model.MedicationDispense fhirDispense = new org.hl7.fhir.r4.model.MedicationDispense();
		assertThrows(InvalidRequestException.class, () -> dispenseService.update(null, fhirDispense));
	}
	
	@Test
	public void update_shouldThrowExceptionWhenMedicationDispenseIsNull() {
		assertThrows(InvalidRequestException.class, () -> dispenseService.update(MEDICATION_DISPENSE_UUID, null));
	}
	
	@Test
	public void update_shouldThrowExceptionWhenMedicationDispenseIdIsNull() {
		org.hl7.fhir.r4.model.MedicationDispense fhirDispense = new org.hl7.fhir.r4.model.MedicationDispense();
		assertThrows(InvalidRequestException.class, () -> dispenseService.update(MEDICATION_DISPENSE_UUID, fhirDispense));
	}
	
	@Test
	public void update_shouldThrowExceptionWhenMedicationDispenseIdDoesNotMatchCurrentId() {
		org.hl7.fhir.r4.model.MedicationDispense fhirDispense = new org.hl7.fhir.r4.model.MedicationDispense();
		fhirDispense.setId(NEW_DISPENSE_UUID);
		assertThrows(InvalidRequestException.class, () -> dispenseService.update(MEDICATION_DISPENSE_UUID, fhirDispense));
	}
	
	@Test
	public void delete_shouldDeleteExistingMedicationDispense() {
		MedicationDispense openmrsDispense = new MedicationDispense();
		openmrsDispense.setUuid(MEDICATION_DISPENSE_UUID);
		
		org.hl7.fhir.r4.model.MedicationDispense fhirDispense = new org.hl7.fhir.r4.model.MedicationDispense();
		fhirDispense.setId(MEDICATION_DISPENSE_UUID);
		
		when(dao.delete(MEDICATION_DISPENSE_UUID)).thenReturn(openmrsDispense);
		dispenseService.delete(MEDICATION_DISPENSE_UUID);
	}
	
	@Test
	public void delete_shouldThrowExceptionWhenIdIsNull() {
		assertThrows(InvalidRequestException.class, () -> dispenseService.delete(null));
	}
	
	@Test
	public void searchMedicationDispenses_shouldGetSearchResults() {
		String patientReference = "patient-ref";
		String encounterReference = "encounter-ref";
		String medicationRequestRef = "medication-request-ref";
		String lastUpdatedDate = "2020-09-03";
		
		ReferenceAndListParam patientParam = new ReferenceAndListParam();
		patientParam.addValue(new ReferenceOrListParam().add(new ReferenceParam(patientReference)));
		
		ReferenceAndListParam encounterParam = new ReferenceAndListParam();
		encounterParam.addValue(new ReferenceOrListParam().addOr(new ReferenceParam(encounterReference)));
		
		ReferenceAndListParam medicationRequestParam = new ReferenceAndListParam();
		encounterParam.addValue(new ReferenceOrListParam().addOr(new ReferenceParam(medicationRequestRef)));
		
		TokenAndListParam idParam = new TokenAndListParam().addAnd(new TokenParam(MEDICATION_DISPENSE_UUID));
		
		DateRangeParam lastUpdatedParam = new DateRangeParam().setLowerBound(lastUpdatedDate).setUpperBound(lastUpdatedDate);
		
		SortSpec sortParam = new SortSpec("sort param");
		
		HashSet<Include> includes = new HashSet<>();
		
		SearchParameterMap theParams = new SearchParameterMap()
		        .addParameter(FhirConstants.COMMON_SEARCH_HANDLER, FhirConstants.ID_PROPERTY, idParam)
		        .addParameter(FhirConstants.COMMON_SEARCH_HANDLER, FhirConstants.LAST_UPDATED_PROPERTY, lastUpdatedParam)
		        .addParameter(FhirConstants.PATIENT_REFERENCE_SEARCH_HANDLER, patientParam)
		        .addParameter(FhirConstants.ENCOUNTER_REFERENCE_SEARCH_HANDLER, encounterParam)
		        .addParameter(FhirConstants.MEDICATION_REQUEST_REFERENCE_SEARCH_HANDLER, medicationRequestParam)
		        .addParameter(FhirConstants.INCLUDE_SEARCH_HANDLER, includes).setSortSpec(sortParam);
		
		when(dao.getSearchResults(any())).thenReturn(Collections.singletonList(openmrsDispense));
		when(searchQuery.getQueryResults(any(), any(), any(), any())).thenReturn(
		    new SearchQueryBundleProvider<>(theParams, dao, translator, globalPropertyService, searchQueryInclude));
		when(searchQueryInclude.getIncludedResources(any(), any())).thenReturn(Collections.emptySet());
		when(translator.toFhirResource(openmrsDispense)).thenReturn(fhirDispense);
		
		MedicationDispenseSearchParams params = new MedicationDispenseSearchParams();
		params.setId(idParam);
		params.setLastUpdated(lastUpdatedParam);
		params.setPatient(patientParam);
		params.setEncounter(encounterParam);
		params.setMedicationRequest(medicationRequestParam);
		params.setIncludes(includes);
		params.setSort(sortParam);
		
		IBundleProvider result = dispenseService.searchMedicationDispenses(params);
		
		List<IBaseResource> resultList = result.getResources(0, 10);
		
		assertThat(result, notNullValue());
		assertThat(resultList, not(empty()));
		assertThat(resultList, hasSize(greaterThanOrEqualTo(1)));
	}
}
