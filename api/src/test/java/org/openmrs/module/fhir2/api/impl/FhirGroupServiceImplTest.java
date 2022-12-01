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
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ca.uhn.fhir.rest.api.server.IBundleProvider;
import ca.uhn.fhir.rest.param.ReferenceAndListParam;
import ca.uhn.fhir.rest.param.ReferenceOrListParam;
import ca.uhn.fhir.rest.param.ReferenceParam;
import ca.uhn.fhir.rest.server.exceptions.InvalidRequestException;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import org.hamcrest.Matchers;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.Group;
import org.hl7.fhir.r4.model.Practitioner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openmrs.Cohort;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.api.FhirGlobalPropertyService;
import org.openmrs.module.fhir2.api.dao.FhirGroupDao;
import org.openmrs.module.fhir2.api.search.SearchQuery;
import org.openmrs.module.fhir2.api.search.SearchQueryBundleProvider;
import org.openmrs.module.fhir2.api.search.SearchQueryInclude;
import org.openmrs.module.fhir2.api.search.param.SearchParameterMap;
import org.openmrs.module.fhir2.api.translators.GroupTranslator;

@RunWith(MockitoJUnitRunner.class)
public class FhirGroupServiceImplTest {
	
	private static final Integer COHORT_ID = 123;
	
	private static final String COHORT_UUID = "1359f03d-55d9-4961-b8f8-9a59eddc1f59";
	
	private static final String BAD_COHORT_UUID = "02ed36f0-6167-4372-a641-d27b92f7deae";
	
	@Mock
	private FhirGroupDao dao;
	
	@Mock
	private GroupTranslator translator;
	
	@Mock
	private FhirGlobalPropertyService globalPropertyService;
	
	@Mock
	private SearchQueryInclude<org.hl7.fhir.r4.model.Group> searchQueryInclude;
	
	@Mock
	private SearchQuery<Cohort, org.hl7.fhir.r4.model.Group, FhirGroupDao, GroupTranslator, SearchQueryInclude<org.hl7.fhir.r4.model.Group>> searchQuery;
	
	private FhirGroupServiceImpl groupService;
	
	private Group group;
	
	private Cohort cohort;
	
	private static final int START_INDEX = 0;
	
	private static final int END_INDEX = 10;
	
	@Before
	public void setup() {
		groupService = new FhirGroupServiceImpl() {
			
			@Override
			protected void validateObject(Cohort object) {
			}
		};
		groupService.setDao(dao);
		groupService.setTranslator(translator);
		groupService.setSearchQuery(searchQuery);
		groupService.setSearchQueryInclude(searchQueryInclude);
		
		group = new Group();
		group.setId(COHORT_UUID);
		
		cohort = new Cohort();
		cohort.setUuid(COHORT_UUID);
	}
	
	private List<IBaseResource> get(IBundleProvider results) {
		return results.getResources(START_INDEX, END_INDEX);
	}
	
	@Test
	public void getGroupByUuid_shouldGetGroupByUuid() {
		when(dao.get(COHORT_UUID)).thenReturn(cohort);
		when(translator.toFhirResource(cohort)).thenReturn(group);
		
		Group group = groupService.get(COHORT_UUID);
		assertThat(group, notNullValue());
		assertThat(group.getId(), notNullValue());
		assertThat(group.getId(), equalTo(COHORT_UUID));
	}
	
	@Test
	public void getGroupByUuid_shouldThrowResourceNotFoundWhenCalledWithUnknownUuid() {
		assertThrows(ResourceNotFoundException.class, () -> groupService.get(BAD_COHORT_UUID));
	}
	
	@Test
	public void shouldSaveNewGroup() {
		Cohort cohort = new Cohort();
		cohort.setUuid(COHORT_UUID);
		
		Group group = new Group();
		group.setId(COHORT_UUID);
		
		when(translator.toFhirResource(cohort)).thenReturn(group);
		when(translator.toOpenmrsType(group)).thenReturn(cohort);
		when(dao.createOrUpdate(cohort)).thenReturn(cohort);
		
		Group result = groupService.create(group);
		assertThat(result, notNullValue());
		assertThat(result.getId(), equalTo(COHORT_UUID));
	}
	
	@Test(expected = InvalidRequestException.class)
	public void updateGroupShouldThrowInvalidRequestExceptionIfIdIsNull() {
		Group group = new Group();
		group.setId(COHORT_UUID);
		
		groupService.update(null, group);
	}
	
	@Test(expected = InvalidRequestException.class)
	public void updateGroupShouldThrowInvalidRequestExceptionIfIdIsBad() {
		Group group = new Group();
		group.setId(COHORT_UUID);
		
		groupService.update(BAD_COHORT_UUID, group);
	}
	
	@Test(expected = ResourceNotFoundException.class)
	public void updateGroupShouldThrowResourceNotFoundException() {
		Group group = new Group();
		group.setId(COHORT_UUID);
		
		groupService.update(COHORT_UUID, group);
	}
	
	@Test
	public void shouldUpdateGroup() {
		Cohort cohort = new Cohort();
		cohort.setUuid(COHORT_UUID);
		cohort.setVoided(false);
		
		Group group = new Group();
		group.setId(COHORT_UUID);
		group.setActive(false);
		
		when(dao.get(COHORT_UUID)).thenReturn(cohort);
		when(translator.toFhirResource(cohort)).thenReturn(group);
		when(translator.toOpenmrsType(cohort, group)).thenReturn(cohort);
		when(dao.createOrUpdate(cohort)).thenReturn(cohort);
		
		Group result = groupService.update(COHORT_UUID, group);
		assertThat(result, notNullValue());
		assertThat(result.getActive(), is(false));
	}
	
	@Test
	public void shouldDeleteGroup() {
		Group group = new Group();
		group.setId(COHORT_UUID);
		
		when(dao.delete(COHORT_UUID)).thenReturn(cohort);
		
		groupService.delete(COHORT_UUID);
	}
	
	@Test
	public void searchForGroups_shouldReturnCollectionOfGroupByParticipant() {
		ReferenceAndListParam participant = new ReferenceAndListParam();
		
		participant.addValue(
		    new ReferenceOrListParam().add(new ReferenceParam().setValue(COHORT_UUID).setChain(Practitioner.SP_RES_ID)));
		
		List<Cohort> cohorts = new ArrayList<>();
		cohorts.add(cohort);
		
		SearchParameterMap theParams = new SearchParameterMap()
		        .addParameter(FhirConstants.PARTICIPANT_REFERENCE_SEARCH_HANDLER, participant);
		
		when(dao.getSearchResults(any(), any())).thenReturn(cohorts);
		when(dao.getSearchResultIds(any())).thenReturn(Collections.singletonList(COHORT_ID));
		when(translator.toFhirResource(cohort)).thenReturn(group);
		when(searchQuery.getQueryResults(any(), any(), any(), any())).thenReturn(
		    new SearchQueryBundleProvider<>(theParams, dao, translator, globalPropertyService, searchQueryInclude));
		when(searchQueryInclude.getIncludedResources(any(), any())).thenReturn(Collections.emptySet());
		
		IBundleProvider results = groupService.searchForGroups(participant);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, Matchers.notNullValue());
		assertThat(resultList, not(empty()));
		assertThat(resultList, hasSize(greaterThanOrEqualTo(1)));
	}
}
