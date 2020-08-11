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
import ca.uhn.fhir.rest.param.TokenAndListParam;
import ca.uhn.fhir.rest.param.TokenOrListParam;
import ca.uhn.fhir.rest.param.TokenParam;
import ca.uhn.fhir.rest.server.exceptions.InvalidRequestException;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.Medication;
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

@RunWith(MockitoJUnitRunner.class)
public class FhirMedicationServiceImplTest {
	
	private static final String MEDICATION_UUID = "1359f03d-55d9-4961-b8f8-9a59eddc1f59";
	
	private static final String WRONG_MEDICATION_UUID = "02ed36f0-6167-4372-a641-d27b92f7deae";
	
	private static final String CODE = "5087AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
	
	private static final String LAST_UPDATED_DATE = "2020-09-03";
	
	private static final int START_INDEX = 0;
	
	private static final int END_INDEX = 10;
	
	@Mock
	private MedicationTranslator medicationTranslator;
	
	@Mock
	private FhirMedicationDao medicationDao;
	
	@Mock
	private FhirGlobalPropertyService globalPropertyService;
	
	@Mock
	private SearchQueryInclude<Medication> searchQueryInclude;
	
	@Mock
	private SearchQuery<Drug, Medication, FhirMedicationDao, MedicationTranslator, SearchQueryInclude<Medication>> searchQuery;
	
	private FhirMedicationServiceImpl fhirMedicationService;
	
	private Medication medication;
	
	private Drug drug;
	
	@Before
	public void setup() {
		fhirMedicationService = new FhirMedicationServiceImpl() {
			
			@Override
			protected void validateObject(Drug object) {
			}
		};
		
		fhirMedicationService.setTranslator(medicationTranslator);
		fhirMedicationService.setDao(medicationDao);
		fhirMedicationService.setSearchQuery(searchQuery);
		fhirMedicationService.setSearchQueryInclude(searchQueryInclude);
		
		medication = new Medication();
		medication.setId(MEDICATION_UUID);
		
		drug = new Drug();
		drug.setUuid(MEDICATION_UUID);
	}
	
	private List<IBaseResource> get(IBundleProvider results) {
		return results.getResources(START_INDEX, END_INDEX);
	}
	
	@Test
	public void getMedicationByUuid_shouldGetMedicationByUuid() {
		when(medicationDao.get(MEDICATION_UUID)).thenReturn(drug);
		when(medicationTranslator.toFhirResource(drug)).thenReturn(medication);
		
		Medication medication = fhirMedicationService.get(MEDICATION_UUID);
		assertThat(medication, notNullValue());
		assertThat(medication.getId(), notNullValue());
		assertThat(medication.getId(), equalTo(MEDICATION_UUID));
	}
	
	@Test
	public void getMedicationByUuid_shouldThrowResourceNotFoundWhenCalledWithUnknownUuid() {
		assertThrows(ResourceNotFoundException.class, () -> fhirMedicationService.get(WRONG_MEDICATION_UUID));
	}
	
	@Test
	public void searchForMedications_shouldSearchMedicationsByCode() {
		List<Drug> medications = new ArrayList<>();
		medications.add(drug);
		
		TokenAndListParam code = new TokenAndListParam();
		code.addAnd(new TokenOrListParam().addOr(new TokenParam().setValue(CODE)));
		
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.CODED_SEARCH_HANDLER, code);
		
		when(medicationDao.getSearchResultUuids(any())).thenReturn(Collections.singletonList(MEDICATION_UUID));
		when(medicationDao.getSearchResults(any(), any(), anyInt(), anyInt())).thenReturn(medications);
		when(searchQuery.getQueryResults(any(), any(), any(), any())).thenReturn(new SearchQueryBundleProvider<>(theParams,
		        medicationDao, medicationTranslator, globalPropertyService, searchQueryInclude));
		when(searchQueryInclude.getIncludedResources(any(), any())).thenReturn(Collections.emptySet());
		when(medicationTranslator.toFhirResource(drug)).thenReturn(medication);
		
		IBundleProvider result = fhirMedicationService.searchForMedications(code, null, null, null, null);
		
		List<IBaseResource> resultList = get(result);
		
		assertThat(result, notNullValue());
		assertThat(resultList, not(empty()));
		assertThat(resultList, hasSize(greaterThanOrEqualTo(1)));
	}
	
	@Test
	public void searchForMedications_shouldSearchMedicationsByDosageForm() {
		List<Drug> medications = new ArrayList<>();
		medications.add(drug);
		
		TokenAndListParam dosageForm = new TokenAndListParam();
		dosageForm.addAnd(new TokenOrListParam().addOr(new TokenParam().setValue(CODE)));
		
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.DOSAGE_FORM_SEARCH_HANDLER,
		    dosageForm);
		
		when(medicationDao.getSearchResultUuids(any())).thenReturn(Collections.singletonList(MEDICATION_UUID));
		when(medicationDao.getSearchResults(any(), any(), anyInt(), anyInt())).thenReturn(medications);
		when(searchQuery.getQueryResults(any(), any(), any(), any())).thenReturn(new SearchQueryBundleProvider<>(theParams,
		        medicationDao, medicationTranslator, globalPropertyService, searchQueryInclude));
		when(searchQueryInclude.getIncludedResources(any(), any())).thenReturn(Collections.emptySet());
		when(medicationTranslator.toFhirResource(drug)).thenReturn(medication);
		
		IBundleProvider result = fhirMedicationService.searchForMedications(null, dosageForm, null, null, null);
		
		List<IBaseResource> resultList = get(result);
		
		assertThat(result, notNullValue());
		assertThat(resultList, not(empty()));
		assertThat(resultList, hasSize(greaterThanOrEqualTo(1)));
	}
	
	@Test
	public void searchForMedications_shouldSearchMedicationsByIngredientCode() {
		List<Drug> medications = new ArrayList<>();
		medications.add(drug);
		
		TokenAndListParam ingredientCode = new TokenAndListParam();
		ingredientCode.addAnd(new TokenOrListParam().addOr(new TokenParam().setValue(CODE)));
		
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.INGREDIENT_SEARCH_HANDLER,
		    ingredientCode);
		
		when(medicationDao.getSearchResultUuids(any())).thenReturn(Collections.singletonList(MEDICATION_UUID));
		when(medicationDao.getSearchResults(any(), any(), anyInt(), anyInt())).thenReturn(medications);
		when(searchQuery.getQueryResults(any(), any(), any(), any())).thenReturn(new SearchQueryBundleProvider<>(theParams,
		        medicationDao, medicationTranslator, globalPropertyService, searchQueryInclude));
		when(searchQueryInclude.getIncludedResources(any(), any())).thenReturn(Collections.emptySet());
		when(medicationTranslator.toFhirResource(drug)).thenReturn(medication);
		
		IBundleProvider result = fhirMedicationService.searchForMedications(null, null, ingredientCode, null, null);
		
		List<IBaseResource> resultList = get(result);
		
		assertThat(result, notNullValue());
		assertThat(resultList, not(empty()));
		assertThat(resultList.size(), greaterThanOrEqualTo(1));
	}
	
	@Test
	public void searchForMedications_shouldSearchMedicationsByUUID() {
		TokenAndListParam uuid = new TokenAndListParam().addAnd(new TokenParam(MEDICATION_UUID));
		
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.COMMON_SEARCH_HANDLER,
		    FhirConstants.ID_PROPERTY, uuid);
		
		when(medicationDao.getSearchResultUuids(any())).thenReturn(Collections.singletonList(MEDICATION_UUID));
		when(medicationDao.getSearchResults(any(), any(), anyInt(), anyInt())).thenReturn(Collections.singletonList(drug));
		when(searchQuery.getQueryResults(any(), any(), any(), any())).thenReturn(new SearchQueryBundleProvider<>(theParams,
		        medicationDao, medicationTranslator, globalPropertyService, searchQueryInclude));
		when(searchQueryInclude.getIncludedResources(any(), any())).thenReturn(Collections.emptySet());
		when(medicationTranslator.toFhirResource(drug)).thenReturn(medication);
		
		IBundleProvider result = fhirMedicationService.searchForMedications(null, null, null, uuid, null);
		
		List<IBaseResource> resultList = get(result);
		
		assertThat(result, notNullValue());
		assertThat(resultList, not(empty()));
		assertThat(resultList, hasSize(greaterThanOrEqualTo(1)));
	}
	
	@Test
	public void searchForMedications_shouldSearchMedicationsByLastUpdated() {
		DateRangeParam lastUpdated = new DateRangeParam().setUpperBound(LAST_UPDATED_DATE).setLowerBound(LAST_UPDATED_DATE);
		
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.COMMON_SEARCH_HANDLER,
		    FhirConstants.LAST_UPDATED_PROPERTY, lastUpdated);
		
		when(medicationDao.getSearchResultUuids(any())).thenReturn(Collections.singletonList(MEDICATION_UUID));
		when(medicationDao.getSearchResults(any(), any(), anyInt(), anyInt())).thenReturn(Collections.singletonList(drug));
		when(searchQuery.getQueryResults(any(), any(), any(), any())).thenReturn(new SearchQueryBundleProvider<>(theParams,
		        medicationDao, medicationTranslator, globalPropertyService, searchQueryInclude));
		when(searchQueryInclude.getIncludedResources(any(), any())).thenReturn(Collections.emptySet());
		when(medicationTranslator.toFhirResource(drug)).thenReturn(medication);
		
		IBundleProvider result = fhirMedicationService.searchForMedications(null, null, null, null, lastUpdated);
		
		List<IBaseResource> resultList = get(result);
		
		assertThat(result, notNullValue());
		assertThat(resultList, not(empty()));
		assertThat(resultList, hasSize(greaterThanOrEqualTo(1)));
	}
	
	@Test
	public void saveMedication_shouldSaveNewMedication() {
		Drug drug = new Drug();
		drug.setUuid(MEDICATION_UUID);
		
		Medication medication = new Medication();
		medication.setId(MEDICATION_UUID);
		
		when(medicationTranslator.toFhirResource(drug)).thenReturn(medication);
		when(medicationTranslator.toOpenmrsType(medication)).thenReturn(drug);
		when(medicationDao.createOrUpdate(drug)).thenReturn(drug);
		
		Medication result = fhirMedicationService.create(medication);
		assertThat(result, notNullValue());
		assertThat(result.getId(), equalTo(MEDICATION_UUID));
	}
	
	@Test(expected = InvalidRequestException.class)
	public void updateMedication_shouldThrowInvalidRequestExceptionIfIdIsNull() {
		Medication medication = new Medication();
		medication.setId(MEDICATION_UUID);
		
		fhirMedicationService.update(null, medication);
	}
	
	@Test(expected = InvalidRequestException.class)
	public void updateMedication_shouldThrowInvalidRequestException() {
		Medication medication = new Medication();
		medication.setId(MEDICATION_UUID);
		
		fhirMedicationService.update(WRONG_MEDICATION_UUID, medication);
	}
	
	@Test(expected = ResourceNotFoundException.class)
	public void updateMedication_shouldThrowResourceNotFoundException() {
		Medication medication = new Medication();
		medication.setId(MEDICATION_UUID);
		
		fhirMedicationService.update(MEDICATION_UUID, medication);
	}
	
	@Test
	public void updateMedication_shouldUpdateMedication() {
		Drug drug = new Drug();
		drug.setUuid(MEDICATION_UUID);
		
		Medication medication = new Medication();
		medication.setId(MEDICATION_UUID);
		medication.setStatus(Medication.MedicationStatus.INACTIVE);
		
		when(medicationDao.get(MEDICATION_UUID)).thenReturn(drug);
		when(medicationTranslator.toFhirResource(drug)).thenReturn(medication);
		when(medicationTranslator.toOpenmrsType(drug, medication)).thenReturn(drug);
		when(medicationDao.createOrUpdate(drug)).thenReturn(drug);
		
		Medication result = fhirMedicationService.update(MEDICATION_UUID, medication);
		assertThat(result, notNullValue());
		assertThat(result.getStatus(), equalTo(Medication.MedicationStatus.INACTIVE));
	}
	
	@Test
	public void deleteMedication_shouldDeleteMedication() {
		medication.setStatus(Medication.MedicationStatus.INACTIVE);
		when(medicationDao.delete(MEDICATION_UUID)).thenReturn(drug);
		when(medicationTranslator.toFhirResource(drug)).thenReturn(medication);
		
		Medication medication = fhirMedicationService.delete(MEDICATION_UUID);
		assertThat(medication, notNullValue());
		assertThat(medication.getId(), equalTo(MEDICATION_UUID));
		assertThat(medication.getStatus(), equalTo(Medication.MedicationStatus.INACTIVE));
	}
}
