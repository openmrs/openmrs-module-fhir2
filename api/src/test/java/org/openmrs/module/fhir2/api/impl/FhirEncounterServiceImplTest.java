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
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import ca.uhn.fhir.model.api.Include;
import ca.uhn.fhir.rest.api.server.IBundleProvider;
import ca.uhn.fhir.rest.param.DateParam;
import ca.uhn.fhir.rest.param.DateRangeParam;
import ca.uhn.fhir.rest.param.HasAndListParam;
import ca.uhn.fhir.rest.param.HasOrListParam;
import ca.uhn.fhir.rest.param.HasParam;
import ca.uhn.fhir.rest.param.ReferenceAndListParam;
import ca.uhn.fhir.rest.param.ReferenceOrListParam;
import ca.uhn.fhir.rest.param.ReferenceParam;
import ca.uhn.fhir.rest.param.TokenAndListParam;
import ca.uhn.fhir.rest.param.TokenOrListParam;
import ca.uhn.fhir.rest.param.TokenParam;
import ca.uhn.fhir.rest.server.SimpleBundleProvider;
import ca.uhn.fhir.rest.server.exceptions.InvalidRequestException;
import org.hamcrest.Matchers;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Location;
import org.hl7.fhir.r4.model.Observation;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.Practitioner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openmrs.Encounter;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.api.FhirGlobalPropertyService;
import org.openmrs.module.fhir2.api.FhirVisitService;
import org.openmrs.module.fhir2.api.dao.FhirEncounterDao;
import org.openmrs.module.fhir2.api.search.SearchQuery;
import org.openmrs.module.fhir2.api.search.SearchQueryBundleProvider;
import org.openmrs.module.fhir2.api.search.SearchQueryInclude;
import org.openmrs.module.fhir2.api.search.param.EncounterSearchParams;
import org.openmrs.module.fhir2.api.search.param.SearchParameterMap;
import org.openmrs.module.fhir2.api.translators.EncounterTranslator;
import org.openmrs.module.fhir2.providers.r3.MockIBundleProvider;

@RunWith(MockitoJUnitRunner.class)
public class FhirEncounterServiceImplTest {
	
	private static final Integer ENCOUNTER_ID = 123;
	
	private static final String ENCOUNTER_UUID = "344kk343-45hj45-34jk34-34ui33";
	
	private static final String ENCOUNTER_TYPE_UUID = "07000be2-26b6-4cce-8b40-866d8435b613";
	
	private static final String WRONG_ENCOUNTER_UUID = "344kk343-45hj45-34jk34-34ui34";
	
	private static final String ENCOUNTER_DATETIME = "2005-01-01T00:00:00.0";
	
	private static final String PATIENT_FAMILY_NAME = "Doe";
	
	private static final String ENCOUNTER_ADDRESS_STATE = "MA";
	
	private static final String PARTICIPANT_IDENTIFIER = "1";
	
	private static final String LAST_UPDATED_DATE = "2020-09-03";
	
	private static final int START_INDEX = 0;
	
	private static final int END_INDEX = 10;
	
	@Mock
	private FhirEncounterDao dao;
	
	@Mock
	private EncounterTranslator<Encounter> encounterTranslator;
	
	@Mock
	private FhirVisitService visitService;
	
	@Mock
	private FhirGlobalPropertyService globalPropertyService;
	
	@Mock
	private SearchQueryInclude<org.hl7.fhir.r4.model.Encounter> searchQueryInclude;
	
	@Mock
	private SearchQuery<Encounter, org.hl7.fhir.r4.model.Encounter, FhirEncounterDao, EncounterTranslator<Encounter>, SearchQueryInclude<org.hl7.fhir.r4.model.Encounter>> searchQuery;
	
	private FhirEncounterServiceImpl encounterService;
	
	private org.openmrs.Encounter openMrsEncounter;
	
	private org.hl7.fhir.r4.model.Encounter fhirEncounter;
	
	@Before
	public void setUp() {
		encounterService = new FhirEncounterServiceImpl() {
			
			@Override
			protected void validateObject(Encounter object) {
			}
		};
		encounterService.setDao(dao);
		encounterService.setTranslator(encounterTranslator);
		encounterService.setVisitService(visitService);
		encounterService.setSearchQuery(searchQuery);
		encounterService.setSearchQueryInclude(searchQueryInclude);
		
		openMrsEncounter = new Encounter();
		openMrsEncounter.setUuid(ENCOUNTER_UUID);
		
		fhirEncounter = new org.hl7.fhir.r4.model.Encounter();
		fhirEncounter.setId(ENCOUNTER_UUID);
	}
	
	private List<IBaseResource> get(IBundleProvider results) {
		return results.getResources(START_INDEX, END_INDEX);
	}
	
	@Test
	public void get_shouldGetEncounterByUuid() {
		when(dao.get(ENCOUNTER_UUID)).thenReturn(openMrsEncounter);
		when(encounterTranslator.toFhirResource(openMrsEncounter)).thenReturn(fhirEncounter);
		
		org.hl7.fhir.r4.model.Encounter fhirEncounter = encounterService.get(ENCOUNTER_UUID);
		
		assertThat(fhirEncounter, notNullValue());
		assertThat(fhirEncounter.getId(), notNullValue());
		assertThat(fhirEncounter.getId(), equalTo(ENCOUNTER_UUID));
	}
	
	@Test
	public void get_shouldGetEncounterByUuidFromOpenMrsVisit() {
		when(visitService.get(ENCOUNTER_UUID)).thenReturn(fhirEncounter);
		
		org.hl7.fhir.r4.model.Encounter fhirEncounter = encounterService.get(ENCOUNTER_UUID);
		
		assertThat(fhirEncounter, notNullValue());
		assertThat(fhirEncounter.getId(), notNullValue());
		assertThat(fhirEncounter.getId(), equalTo(ENCOUNTER_UUID));
	}
	
	@Test(expected = InvalidRequestException.class)
	public void create_shouldThrowInvalidRequestExceptionWhenEncounterIsMissing() {
		encounterService.create(null);
	}
	
	@Test
	public void create_shouldCreateEncounter() {
		CodeableConcept codeableConcept = new CodeableConcept();
		codeableConcept.addCoding().setSystem(FhirConstants.ENCOUNTER_TYPE_SYSTEM_URI).setCode("1");
		fhirEncounter.setType(Collections.singletonList(codeableConcept));
		
		when(encounterTranslator.toOpenmrsType(fhirEncounter)).thenReturn(openMrsEncounter);
		when(dao.createOrUpdate(openMrsEncounter)).thenReturn(openMrsEncounter);
		when(encounterTranslator.toFhirResource(openMrsEncounter)).thenReturn(fhirEncounter);
		
		org.hl7.fhir.r4.model.Encounter encounter = encounterService.create(fhirEncounter);
		
		assertThat(encounter, notNullValue());
	}
	
	@Test
	public void create_shouldCreateEncounterFromOpenMrsVisit() {
		CodeableConcept codeableConcept = new CodeableConcept();
		codeableConcept.addCoding().setSystem(FhirConstants.VISIT_TYPE_SYSTEM_URI).setCode("1");
		fhirEncounter.setType(Collections.singletonList(codeableConcept));
		
		when(visitService.create(fhirEncounter)).thenReturn(fhirEncounter);
		
		org.hl7.fhir.r4.model.Encounter result = encounterService.create(fhirEncounter);
		
		assertThat(result, notNullValue());
		assertThat(result.getId(), equalTo(ENCOUNTER_UUID));
	}
	
	@Test(expected = InvalidRequestException.class)
	public void create_shouldThrowInvalidRequestExceptionWhenTypeIsMissing() {
		encounterService.create(fhirEncounter);
	}
	
	@Test(expected = InvalidRequestException.class)
	public void create_shouldThrowInvalidRequestExceptionWhenTypeIsNotEncounterOrVisit() {
		CodeableConcept codeableConcept = new CodeableConcept();
		codeableConcept.addCoding().setSystem(FhirConstants.VISIT_TYPE_SYSTEM_URI).setCode("1");
		codeableConcept.addCoding().setSystem(FhirConstants.ENCOUNTER_TYPE_SYSTEM_URI).setCode("2");
		fhirEncounter.setType(Collections.singletonList(codeableConcept));
		encounterService.create(fhirEncounter);
	}
	
	@Test
	public void update_shouldUpdateEncounter() {
		CodeableConcept codeableConcept = new CodeableConcept();
		codeableConcept.addCoding().setSystem(FhirConstants.ENCOUNTER_TYPE_SYSTEM_URI).setCode("1");
		fhirEncounter.setType(Collections.singletonList(codeableConcept));
		
		when(encounterTranslator.toOpenmrsType(openMrsEncounter, fhirEncounter)).thenReturn(openMrsEncounter);
		when(dao.createOrUpdate(openMrsEncounter)).thenReturn(openMrsEncounter);
		when(dao.get(ENCOUNTER_UUID)).thenReturn(openMrsEncounter);
		when(encounterTranslator.toFhirResource(openMrsEncounter)).thenReturn(fhirEncounter);
		
		org.hl7.fhir.r4.model.Encounter encounter = encounterService.update(ENCOUNTER_UUID, fhirEncounter);
		
		assertThat(encounter, notNullValue());
		assertThat(encounter.getId(), equalTo(ENCOUNTER_UUID));
	}
	
	@Test
	public void update_shouldUpdateEncounterFromOpenMrsVisit() {
		CodeableConcept codeableConcept = new CodeableConcept();
		codeableConcept.addCoding().setSystem(FhirConstants.VISIT_TYPE_SYSTEM_URI).setCode("1");
		fhirEncounter.setType(Collections.singletonList(codeableConcept));
		
		when(visitService.update(ENCOUNTER_UUID, fhirEncounter)).thenReturn(fhirEncounter);
		
		org.hl7.fhir.r4.model.Encounter result = encounterService.update(ENCOUNTER_UUID, fhirEncounter);
		
		assertThat(result, notNullValue());
		assertThat(result.getId(), equalTo(ENCOUNTER_UUID));
	}
	
	@Test(expected = InvalidRequestException.class)
	public void update_shouldThrowInvalidRequestExceptionIfIdIsNull() {
		CodeableConcept codeableConcept = new CodeableConcept();
		codeableConcept.addCoding().setSystem(FhirConstants.ENCOUNTER_TYPE_SYSTEM_URI).setCode("1");
		fhirEncounter.setType(Collections.singletonList(codeableConcept));
		
		encounterService.update(null, fhirEncounter);
	}
	
	@Test(expected = InvalidRequestException.class)
	public void update_shouldThrowInvalidRequestException() {
		CodeableConcept codeableConcept = new CodeableConcept();
		codeableConcept.addCoding().setSystem(FhirConstants.ENCOUNTER_TYPE_SYSTEM_URI).setCode("1");
		fhirEncounter.setType(Collections.singletonList(codeableConcept));
		
		encounterService.update(WRONG_ENCOUNTER_UUID, fhirEncounter);
	}
	
	@Test(expected = InvalidRequestException.class)
	public void update_shouldThrowInvalidRequestExceptionWhenTypeIsMissing() {
		encounterService.update(ENCOUNTER_UUID, fhirEncounter);
	}
	
	@Test(expected = InvalidRequestException.class)
	public void update_shouldThrowInvalidRequestExceptionWhenTypeIsNotEncounterOrVisit() {
		CodeableConcept codeableConcept = new CodeableConcept();
		codeableConcept.addCoding().setSystem(FhirConstants.VISIT_TYPE_SYSTEM_URI).setCode("1");
		codeableConcept.addCoding().setSystem(FhirConstants.ENCOUNTER_TYPE_SYSTEM_URI).setCode("2");
		fhirEncounter.setType(Collections.singletonList(codeableConcept));
		encounterService.update(ENCOUNTER_UUID, fhirEncounter);
	}
	
	@Test(expected = InvalidRequestException.class)
	public void delete_shouldThrowInvalidRequestExceptionWhenUidIsMissing() {
		encounterService.delete(null);
	}
	
	@Test
	public void delete_shouldDeleteEncounter() {
		when(dao.delete(ENCOUNTER_UUID)).thenReturn(openMrsEncounter);
		
		encounterService.delete(ENCOUNTER_UUID);
	}
	
	@Test
	public void delete_shouldDeleteEncounterFromOpenMrsVisit() {
		when(dao.delete(ENCOUNTER_UUID)).thenReturn(null);
		
		encounterService.delete(ENCOUNTER_UUID);
	}
	
	@Test
	public void searchForEncounter_shouldReturnCollectionOfEncounterByDate() {
		List<Encounter> encounters = new ArrayList<>();
		DateRangeParam dateRangeParam = new DateRangeParam(new DateParam(ENCOUNTER_DATETIME));
		
		encounters.add(openMrsEncounter);
		
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.DATE_RANGE_SEARCH_HANDLER,
		    dateRangeParam);
		
		fhirEncounter.setId(ENCOUNTER_UUID);
		
		when(dao.getSearchResults(any())).thenReturn(encounters);
		when(dao.getSearchResultsCount(any())).thenReturn(1);
		when(encounterTranslator.toFhirResource(openMrsEncounter)).thenReturn(fhirEncounter);
		when(searchQuery.getQueryResults(any(), any(), any(), any())).thenReturn(
		    new SearchQueryBundleProvider<>(theParams, dao, encounterTranslator, globalPropertyService, searchQueryInclude));
		when(searchQueryInclude.getIncludedResources(any(), any())).thenReturn(Collections.emptySet());
		
		when(visitService.searchForVisits(any())).thenReturn(new SimpleBundleProvider());
		
		EncounterSearchParams encounterSearchParams = new EncounterSearchParams();
		encounterSearchParams.setDate(dateRangeParam);
		IBundleProvider results = encounterService.searchForEncounters(encounterSearchParams);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList, not(empty()));
		assertThat(((org.hl7.fhir.r4.model.Encounter) resultList.iterator().next()).getId(), equalTo(ENCOUNTER_UUID));
	}
	
	@Test
	public void searchForEncounter_shouldReturnCollectionOfEncounterByLocation() {
		ReferenceAndListParam location = new ReferenceAndListParam();
		
		location.addValue(new ReferenceOrListParam()
		        .add(new ReferenceParam().setValue(ENCOUNTER_ADDRESS_STATE).setChain(Location.SP_ADDRESS_STATE)));
		
		List<Encounter> encounters = new ArrayList<>();
		encounters.add(openMrsEncounter);
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.LOCATION_REFERENCE_SEARCH_HANDLER,
		    location);
		
		when(dao.getSearchResults(any())).thenReturn(encounters);
		when(dao.getSearchResultsCount(any())).thenReturn(1);
		when(encounterTranslator.toFhirResource(openMrsEncounter)).thenReturn(fhirEncounter);
		when(searchQuery.getQueryResults(any(), any(), any(), any())).thenReturn(
		    new SearchQueryBundleProvider<>(theParams, dao, encounterTranslator, globalPropertyService, searchQueryInclude));
		when(searchQueryInclude.getIncludedResources(any(), any())).thenReturn(Collections.emptySet());
		
		when(visitService.searchForVisits(any())).thenReturn(new SimpleBundleProvider());
		
		EncounterSearchParams encounterSearchParams = new EncounterSearchParams();
		encounterSearchParams.setLocation(location);
		IBundleProvider results = encounterService.searchForEncounters(encounterSearchParams);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList, not(empty()));
		assertThat(resultList, hasSize(greaterThanOrEqualTo(1)));
	}
	
	@Test
	public void searchForEncounter_shouldReturnCollectionOfEncounterByParticipant() {
		ReferenceAndListParam participant = new ReferenceAndListParam();
		
		participant.addValue(new ReferenceOrListParam()
		        .add(new ReferenceParam().setValue(PARTICIPANT_IDENTIFIER).setChain(Practitioner.SP_IDENTIFIER)));
		
		List<Encounter> encounters = new ArrayList<>();
		encounters.add(openMrsEncounter);
		
		fhirEncounter.setId(ENCOUNTER_UUID);
		
		SearchParameterMap theParams = new SearchParameterMap()
		        .addParameter(FhirConstants.PARTICIPANT_REFERENCE_SEARCH_HANDLER, participant);
		
		when(dao.getSearchResults(any())).thenReturn(encounters);
		when(dao.getSearchResultsCount(any())).thenReturn(1);
		when(encounterTranslator.toFhirResource(openMrsEncounter)).thenReturn(fhirEncounter);
		when(searchQuery.getQueryResults(any(), any(), any(), any())).thenReturn(
		    new SearchQueryBundleProvider<>(theParams, dao, encounterTranslator, globalPropertyService, searchQueryInclude));
		when(searchQueryInclude.getIncludedResources(any(), any())).thenReturn(Collections.emptySet());
		
		when(visitService.searchForVisits(any())).thenReturn(new SimpleBundleProvider());
		
		EncounterSearchParams encounterSearchParams = new EncounterSearchParams();
		encounterSearchParams.setParticipant(participant);
		IBundleProvider results = encounterService.searchForEncounters(encounterSearchParams);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, Matchers.notNullValue());
		assertThat(resultList, not(empty()));
		assertThat(resultList, hasSize(greaterThanOrEqualTo(1)));
	}
	
	@Test
	public void searchForEncounter_shouldReturnCollectionOfEncounterBySubject() {
		ReferenceAndListParam subject = new ReferenceAndListParam();
		
		subject.addValue(
		    new ReferenceOrListParam().add(new ReferenceParam().setValue(PATIENT_FAMILY_NAME).setChain(Patient.SP_FAMILY)));
		
		List<Encounter> encounters = new ArrayList<>();
		encounters.add(openMrsEncounter);
		
		fhirEncounter.setId(ENCOUNTER_UUID);
		
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.PATIENT_REFERENCE_SEARCH_HANDLER,
		    subject);
		
		when(dao.getSearchResults(any())).thenReturn(encounters);
		when(dao.getSearchResultsCount(any())).thenReturn(1);
		when(encounterTranslator.toFhirResource(openMrsEncounter)).thenReturn(fhirEncounter);
		when(searchQuery.getQueryResults(any(), any(), any(), any())).thenReturn(
		    new SearchQueryBundleProvider<>(theParams, dao, encounterTranslator, globalPropertyService, searchQueryInclude));
		when(searchQueryInclude.getIncludedResources(any(), any())).thenReturn(Collections.emptySet());
		
		when(visitService.searchForVisits(any())).thenReturn(new SimpleBundleProvider());
		
		EncounterSearchParams encounterSearchParams = new EncounterSearchParams();
		encounterSearchParams.setSubject(subject);
		IBundleProvider results = encounterService.searchForEncounters(encounterSearchParams);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, Matchers.notNullValue());
		assertThat(resultList, not(empty()));
		assertThat(resultList, hasSize(greaterThanOrEqualTo(1)));
	}
	
	@Test
	public void searchForEncounter_shouldReturnCollectionOfEncounterByUUID() {
		TokenAndListParam uuid = new TokenAndListParam().addAnd(new TokenParam(ENCOUNTER_UUID));
		
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.COMMON_SEARCH_HANDLER,
		    FhirConstants.ID_PROPERTY, uuid);
		
		when(dao.getSearchResults(any())).thenReturn(Collections.singletonList(openMrsEncounter));
		when(dao.getSearchResultsCount(any())).thenReturn(1);
		when(encounterTranslator.toFhirResource(openMrsEncounter)).thenReturn(fhirEncounter);
		when(searchQuery.getQueryResults(any(), any(), any(), any())).thenReturn(
		    new SearchQueryBundleProvider<>(theParams, dao, encounterTranslator, globalPropertyService, searchQueryInclude));
		when(searchQueryInclude.getIncludedResources(any(), any())).thenReturn(Collections.emptySet());
		
		when(visitService.searchForVisits(any())).thenReturn(new SimpleBundleProvider());
		
		EncounterSearchParams encounterSearchParams = new EncounterSearchParams();
		encounterSearchParams.setId(uuid);
		IBundleProvider results = encounterService.searchForEncounters(encounterSearchParams);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList, not(empty()));
		assertThat(resultList, hasSize(greaterThanOrEqualTo(1)));
	}
	
	@Test
	public void searchForEncounter_shouldReturnCollectionOfEncounterByTypeUUID() {
		TokenAndListParam typeUuid = new TokenAndListParam().addAnd(new TokenParam(ENCOUNTER_TYPE_UUID));
		
		SearchParameterMap theParams = new SearchParameterMap()
		        .addParameter(FhirConstants.ENCOUNTER_TYPE_REFERENCE_SEARCH_HANDLER, typeUuid);
		
		List<Encounter> encounters = new ArrayList<>();
		encounters.add(openMrsEncounter);
		fhirEncounter.setId(ENCOUNTER_UUID);
		
		when(dao.getSearchResults(any())).thenReturn(encounters);
		when(dao.getSearchResultsCount(any())).thenReturn(1);
		when(encounterTranslator.toFhirResource(openMrsEncounter)).thenReturn(fhirEncounter);
		when(searchQuery.getQueryResults(any(), any(), any(), any())).thenReturn(
		    new SearchQueryBundleProvider<>(theParams, dao, encounterTranslator, globalPropertyService, searchQueryInclude));
		when(searchQueryInclude.getIncludedResources(any(), any())).thenReturn(Collections.emptySet());
		
		when(visitService.searchForVisits(any())).thenReturn(new SimpleBundleProvider());
		
		EncounterSearchParams encounterSearchParams = new EncounterSearchParams();
		encounterSearchParams.setEncounterType(typeUuid);
		IBundleProvider results = encounterService.searchForEncounters(encounterSearchParams);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList, not(empty()));
		assertThat(resultList, hasSize(greaterThanOrEqualTo(1)));
	}
	
	@Test
	public void searchForEncounter_shouldReturnCollectionOfEncounterByLastUpdated() {
		DateRangeParam lastUpdated = new DateRangeParam().setUpperBound(LAST_UPDATED_DATE).setLowerBound(LAST_UPDATED_DATE);
		
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.COMMON_SEARCH_HANDLER,
		    FhirConstants.LAST_UPDATED_PROPERTY, lastUpdated);
		
		when(dao.getSearchResults(any())).thenReturn(Collections.singletonList(openMrsEncounter));
		when(dao.getSearchResultsCount(any())).thenReturn(1);
		when(encounterTranslator.toFhirResource(openMrsEncounter)).thenReturn(fhirEncounter);
		when(searchQuery.getQueryResults(any(), any(), any(), any())).thenReturn(
		    new SearchQueryBundleProvider<>(theParams, dao, encounterTranslator, globalPropertyService, searchQueryInclude));
		when(searchQueryInclude.getIncludedResources(any(), any())).thenReturn(Collections.emptySet());
		
		when(visitService.searchForVisits(any())).thenReturn(new SimpleBundleProvider());
		
		EncounterSearchParams encounterSearchParams = new EncounterSearchParams();
		encounterSearchParams.setLastUpdated(lastUpdated);
		IBundleProvider results = encounterService.searchForEncounters(encounterSearchParams);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList, not(empty()));
		assertThat(resultList.size(), greaterThanOrEqualTo(1));
	}
	
	@Test
	public void searchForEncounter_shouldAddIncludedResourcesToResultList() {
		HashSet<Include> includes = new HashSet<>();
		includes.add(new Include("Encounter:patient"));
		
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.INCLUDE_SEARCH_HANDLER, includes);
		
		when(dao.getSearchResults(any())).thenReturn(Collections.singletonList(openMrsEncounter));
		when(dao.getSearchResultsCount(any())).thenReturn(1);
		when(encounterTranslator.toFhirResource(openMrsEncounter)).thenReturn(fhirEncounter);
		when(searchQuery.getQueryResults(any(), any(), any(), any())).thenReturn(
		    new SearchQueryBundleProvider<>(theParams, dao, encounterTranslator, globalPropertyService, searchQueryInclude));
		when(searchQueryInclude.getIncludedResources(any(), any())).thenReturn(Collections.singleton(new Patient()));
		
		when(visitService.searchForVisits(any())).thenReturn(new SimpleBundleProvider());
		
		EncounterSearchParams encounterSearchParams = new EncounterSearchParams();
		encounterSearchParams.setIncludes(includes);
		IBundleProvider results = encounterService.searchForEncounters(encounterSearchParams);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList, not(empty()));
		assertThat(resultList.size(), greaterThanOrEqualTo(2));
		assertThat(resultList, hasItem(is(instanceOf(Patient.class))));
	}
	
	@Test
	public void searchForEncounter_shouldNotAddRelatedResourcesToResultListForEmptyInclude() {
		HashSet<Include> includes = new HashSet<>();
		
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.INCLUDE_SEARCH_HANDLER, includes);
		
		when(dao.getSearchResults(any())).thenReturn(Collections.singletonList(openMrsEncounter));
		when(dao.getSearchResultsCount(any())).thenReturn(1);
		when(encounterTranslator.toFhirResource(openMrsEncounter)).thenReturn(fhirEncounter);
		when(searchQuery.getQueryResults(any(), any(), any(), any())).thenReturn(
		    new SearchQueryBundleProvider<>(theParams, dao, encounterTranslator, globalPropertyService, searchQueryInclude));
		when(searchQueryInclude.getIncludedResources(any(), any())).thenReturn(Collections.emptySet());
		
		when(visitService.searchForVisits(any())).thenReturn(new SimpleBundleProvider());
		
		EncounterSearchParams encounterSearchParams = new EncounterSearchParams();
		encounterSearchParams.setIncludes(includes);
		IBundleProvider results = encounterService.searchForEncounters(encounterSearchParams);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList, not(empty()));
		assertThat(resultList.size(), greaterThanOrEqualTo(1));
	}
	
	@Test
	public void searchForEncounter_shouldAddReverseIncludedResourcesToResultList() {
		HashSet<Include> revIncludes = new HashSet<>();
		revIncludes.add(new Include("Observation:encounter"));
		
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.REVERSE_INCLUDE_SEARCH_HANDLER,
		    revIncludes);
		
		when(dao.getSearchResults(any())).thenReturn(Collections.singletonList(openMrsEncounter));
		when(dao.getSearchResultsCount(any())).thenReturn(1);
		when(encounterTranslator.toFhirResource(openMrsEncounter)).thenReturn(fhirEncounter);
		when(searchQuery.getQueryResults(any(), any(), any(), any())).thenReturn(
		    new SearchQueryBundleProvider<>(theParams, dao, encounterTranslator, globalPropertyService, searchQueryInclude));
		when(searchQueryInclude.getIncludedResources(any(), any())).thenReturn(Collections.singleton(new Observation()));
		
		when(visitService.searchForVisits(any())).thenReturn(new SimpleBundleProvider());
		
		EncounterSearchParams encounterSearchParams = new EncounterSearchParams();
		encounterSearchParams.setRevIncludes(revIncludes);
		IBundleProvider results = encounterService.searchForEncounters(encounterSearchParams);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList, not(empty()));
		assertThat(resultList.size(), greaterThanOrEqualTo(2));
		assertThat(resultList, hasItem(is(instanceOf(Observation.class))));
	}
	
	@Test
	public void searchForEncounter_shouldNotAddRelatedResourcesToResultListForEmptyRevInclude() {
		HashSet<Include> revIncludes = new HashSet<>();
		
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.REVERSE_INCLUDE_SEARCH_HANDLER,
		    revIncludes);
		
		when(dao.getSearchResults(any())).thenReturn(Collections.singletonList(openMrsEncounter));
		when(dao.getSearchResultsCount(any())).thenReturn(1);
		when(encounterTranslator.toFhirResource(openMrsEncounter)).thenReturn(fhirEncounter);
		when(searchQuery.getQueryResults(any(), any(), any(), any())).thenReturn(
		    new SearchQueryBundleProvider<>(theParams, dao, encounterTranslator, globalPropertyService, searchQueryInclude));
		when(searchQueryInclude.getIncludedResources(any(), any())).thenReturn(Collections.emptySet());
		
		when(visitService.searchForVisits(any())).thenReturn(new SimpleBundleProvider());
		
		EncounterSearchParams encounterSearchParams = new EncounterSearchParams();
		encounterSearchParams.setRevIncludes(revIncludes);
		IBundleProvider results = encounterService.searchForEncounters(encounterSearchParams);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList, not(empty()));
		assertThat(resultList, hasSize(greaterThanOrEqualTo(1)));
	}
	
	@Test
	public void searchForEncounter_shouldOnlyReturnEncountersThatHaveAssociatedMedicationRequests() {
		HasOrListParam hasOrListParam = new HasOrListParam();
		hasOrListParam.add(new HasParam("MedicationRequest", "encounter", "intent", "order"));
		HasAndListParam hasAndListParam = new HasAndListParam();
		hasAndListParam.addAnd(hasOrListParam);
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.HAS_SEARCH_HANDLER,
		    hasAndListParam);
		
		when(dao.getSearchResults(any())).thenReturn(Collections.singletonList(openMrsEncounter));
		when(dao.getSearchResultsCount(any())).thenReturn(1);
		when(encounterTranslator.toFhirResource(openMrsEncounter)).thenReturn(fhirEncounter);
		when(searchQuery.getQueryResults(any(), any(), any(), any())).thenReturn(
		    new SearchQueryBundleProvider<>(theParams, dao, encounterTranslator, globalPropertyService, searchQueryInclude));
		when(searchQueryInclude.getIncludedResources(any(), any())).thenReturn(Collections.emptySet());
		
		when(visitService.searchForVisits(any())).thenReturn(new SimpleBundleProvider());
		
		EncounterSearchParams encounterSearchParams = new EncounterSearchParams();
		encounterSearchParams.setHasAndListParam(hasAndListParam);
		IBundleProvider results = encounterService.searchForEncounters(encounterSearchParams);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList, not(empty()));
		assertThat(resultList, hasSize(greaterThanOrEqualTo(1)));
	}
	
	@Test
	public void searchEncounters_shouldIncludeAddResourcesThatMatchTagParam() {
		
		int numEncounters = 9;
		List<org.hl7.fhir.r4.model.Encounter> fhirEncounters = new ArrayList<>();
		for (int i = 0; i < numEncounters; i++) {
			fhirEncounters.add(new org.hl7.fhir.r4.model.Encounter());
		}
		when(searchQuery.getQueryResults(any(), any(), any(), any()))
		        .thenReturn(new MockIBundleProvider<>(fhirEncounters, 10, 1));
		
		int numVisits = 5;
		List<org.hl7.fhir.r4.model.Encounter> fhirVisits = new ArrayList<>();
		for (int i = 0; i < numVisits; i++) {
			fhirVisits.add(new org.hl7.fhir.r4.model.Encounter());
		}
		when(visitService.searchForVisits(any())).thenReturn(new MockIBundleProvider<>(fhirVisits, 10, 1));
		
		{
			IBundleProvider results = encounterService.searchForEncounters(new EncounterSearchParams());
			List<IBaseResource> resultList = get(results);
			assertThat(results, notNullValue());
			assertThat(resultList, not(empty()));
			assertThat(resultList, hasSize(numEncounters + numVisits));
		}
		
		{
			TokenAndListParam tokenAndListParam = new TokenAndListParam();
			tokenAndListParam.addAnd(new TokenParam(FhirConstants.OPENMRS_FHIR_EXT_ENCOUNTER_TAG, "encounter"));
			SearchParameterMap theParams = new SearchParameterMap();
			theParams.addParameter(FhirConstants.TAG_SEARCH_HANDLER, tokenAndListParam);
			EncounterSearchParams encounterSearchParams = new EncounterSearchParams();
			encounterSearchParams.setTag(tokenAndListParam);
			IBundleProvider results = encounterService.searchForEncounters(encounterSearchParams);
			
			List<IBaseResource> resultList = get(results);
			
			assertThat(results, notNullValue());
			assertThat(resultList, not(empty()));
			assertThat(resultList, hasSize(numEncounters));
		}
		
		{
			TokenAndListParam tokenAndListParam = new TokenAndListParam();
			tokenAndListParam.addAnd(new TokenParam(FhirConstants.OPENMRS_FHIR_EXT_ENCOUNTER_TAG, "visit"));
			SearchParameterMap theParams = new SearchParameterMap();
			theParams.addParameter(FhirConstants.TAG_SEARCH_HANDLER, tokenAndListParam);
			EncounterSearchParams encounterSearchParams = new EncounterSearchParams();
			encounterSearchParams.setTag(tokenAndListParam);
			IBundleProvider results = encounterService.searchForEncounters(encounterSearchParams);
			
			List<IBaseResource> resultList = get(results);
			
			assertThat(results, notNullValue());
			assertThat(resultList, not(empty()));
			assertThat(resultList, hasSize(numVisits));
		}
		
		{
			TokenAndListParam tokenAndListParam = new TokenAndListParam();
			TokenOrListParam tokenOrListParam = new TokenOrListParam();
			tokenOrListParam.add(FhirConstants.OPENMRS_FHIR_EXT_ENCOUNTER_TAG, "encounter");
			tokenOrListParam.add(FhirConstants.OPENMRS_FHIR_EXT_ENCOUNTER_TAG, "visit");
			tokenAndListParam.addAnd(tokenOrListParam);
			SearchParameterMap theParams = new SearchParameterMap();
			theParams.addParameter(FhirConstants.TAG_SEARCH_HANDLER, tokenAndListParam);
			EncounterSearchParams encounterSearchParams = new EncounterSearchParams();
			encounterSearchParams.setTag(tokenAndListParam);
			IBundleProvider results = encounterService.searchForEncounters(encounterSearchParams);
			
			List<IBaseResource> resultList = get(results);
			
			assertThat(results, notNullValue());
			assertThat(resultList, not(empty()));
			assertThat(resultList, hasSize(numEncounters + numVisits));
		}
	}
	
	@Test
	public void getEncounterEverything_shouldReturnAllInformationAboutSpecifiedEncounter() {
		TokenParam encounterId = new TokenParam().setValue(ENCOUNTER_UUID);
		
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.EVERYTHING_SEARCH_HANDLER, "")
		        .addParameter(FhirConstants.COMMON_SEARCH_HANDLER, FhirConstants.ID_PROPERTY, encounterId);
		
		populateIncludeForEverythingOperationParams(theParams);
		populateReverseIncludeForEverythingOperationParams(theParams);
		
		when(dao.getSearchResults(any())).thenReturn(Collections.singletonList(openMrsEncounter));
		when(dao.getSearchResultsCount(any())).thenReturn(1);
		when(encounterTranslator.toFhirResource(openMrsEncounter)).thenReturn(fhirEncounter);
		when(searchQuery.getQueryResults(any(), any(), any(), any())).thenReturn(
		    new SearchQueryBundleProvider<>(theParams, dao, encounterTranslator, globalPropertyService, searchQueryInclude));
		
		when(visitService.searchForVisits(any())).thenReturn(new SimpleBundleProvider());
		
		IBundleProvider results = encounterService.getEncounterEverything(encounterId);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList, not(empty()));
		assertThat(resultList.size(), greaterThanOrEqualTo(1));
		
	}
	
	private void populateReverseIncludeForEverythingOperationParams(SearchParameterMap theParams) {
		HashSet<Include> revIncludes = new HashSet<>();
		
		revIncludes.add(new Include(FhirConstants.OBSERVATION + ":" + FhirConstants.INCLUDE_ENCOUNTER_PARAM));
		revIncludes.add(new Include(FhirConstants.DIAGNOSTIC_REPORT + ":" + FhirConstants.INCLUDE_ENCOUNTER_PARAM));
		revIncludes.add(new Include(FhirConstants.MEDICATION_REQUEST + ":" + FhirConstants.INCLUDE_ENCOUNTER_PARAM));
		revIncludes.add(new Include(FhirConstants.SERVICE_REQUEST + ":" + FhirConstants.INCLUDE_ENCOUNTER_PARAM));
		
		theParams.addParameter(FhirConstants.REVERSE_INCLUDE_SEARCH_HANDLER, revIncludes);
	}
	
	private void populateIncludeForEverythingOperationParams(SearchParameterMap theParams) {
		HashSet<Include> includes = new HashSet<>();
		
		includes.add(new Include(FhirConstants.ENCOUNTER + ":" + FhirConstants.INCLUDE_PATIENT_PARAM));
		includes.add(new Include(FhirConstants.ENCOUNTER + ":" + FhirConstants.INCLUDE_LOCATION_PARAM));
		includes.add(new Include(FhirConstants.ENCOUNTER + ":" + FhirConstants.INCLUDE_PARTICIPANT_PARAM));
		
		theParams.addParameter(FhirConstants.INCLUDE_SEARCH_HANDLER, includes);
	}
}
