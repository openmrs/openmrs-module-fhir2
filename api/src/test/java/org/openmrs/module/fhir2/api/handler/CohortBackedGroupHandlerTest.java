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
import java.util.List;

import ca.uhn.fhir.rest.api.server.IBundleProvider;
import ca.uhn.fhir.rest.param.ReferenceAndListParam;
import ca.uhn.fhir.rest.param.ReferenceOrListParam;
import ca.uhn.fhir.rest.param.ReferenceParam;
import ca.uhn.fhir.rest.server.exceptions.InvalidRequestException;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
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

/**
 * Tests the CRUD/search wiring and dispatch predicates of the default Group handler.
 * Orchestrator-level concerns (fan-out, the typed {@code searchForGroups} entry point) live in
 * {@code FhirGroupServiceImplTest}.
 */
@RunWith(MockitoJUnitRunner.class)
public class CohortBackedGroupHandlerTest {
	
	private static final String COHORT_UUID = "1359f03d-55d9-4961-b8f8-9a59eddc1f59";
	
	private static final String BAD_COHORT_UUID = "02ed36f0-6167-4372-a641-d27b92f7deae";
	
	private static final int START_INDEX = 0;
	
	private static final int END_INDEX = 10;
	
	@Mock
	private FhirGroupDao dao;
	
	@Mock
	private GroupTranslator translator;
	
	@Mock
	private FhirGlobalPropertyService globalPropertyService;
	
	@Mock
	private SearchQueryInclude<Group> searchQueryInclude;
	
	@Mock
	private SearchQuery<Cohort, Group, FhirGroupDao, GroupTranslator, SearchQueryInclude<Group>> searchQuery;
	
	private CohortBackedGroupHandler handler;
	
	private Group group;
	
	private Cohort cohort;
	
	@Before
	public void setup() {
		handler = new CohortBackedGroupHandler() {
			
			@Override
			protected void validateObject(Cohort object) {
			}
		};
		handler.setDao(dao);
		handler.setTranslator(translator);
		handler.setSearchQuery(searchQuery);
		handler.setSearchQueryInclude(searchQueryInclude);
		
		group = new Group();
		group.setId(COHORT_UUID);
		
		cohort = new Cohort();
		cohort.setUuid(COHORT_UUID);
	}
	
	private List<IBaseResource> get(IBundleProvider results) {
		return results.getResources(START_INDEX, END_INDEX);
	}
	
	// ---- dispatch predicates ----
	
	@Test
	public void shouldExposeGroupImplicitProfile() {
		assertThat(handler.getImplicitProfile(), equalTo("http://fhir.openmrs.org/StructureDefinition/openmrs-group"));
	}
	
	@Test
	public void canHandle_shouldAlwaysReturnTrue() {
		assertTrue(handler.canHandle(new Group()));
	}
	
	@Test
	public void acceptsSearch_shouldAcceptAnySearch() {
		assertTrue(handler.acceptsSearch(new SearchParameterMap()));
	}
	
	// ---- get ----
	
	@Test
	public void get_shouldGetGroupByUuid() {
		when(dao.get(COHORT_UUID)).thenReturn(cohort);
		when(translator.toFhirResource(cohort)).thenReturn(group);
		
		Group result = handler.get(COHORT_UUID);
		assertThat(result, notNullValue());
		assertThat(result.getId(), equalTo(COHORT_UUID));
	}
	
	@Test
	public void get_shouldThrowResourceNotFoundWhenCalledWithUnknownUuid() {
		assertThrows(ResourceNotFoundException.class, () -> handler.get(BAD_COHORT_UUID));
	}
	
	// ---- create / update / delete ----
	
	@Test
	public void create_shouldSaveNewGroup() {
		when(translator.toFhirResource(cohort)).thenReturn(group);
		when(translator.toOpenmrsType(group)).thenReturn(cohort);
		when(dao.createOrUpdate(cohort)).thenReturn(cohort);
		
		Group result = handler.create(group);
		assertThat(result, notNullValue());
		assertThat(result.getId(), equalTo(COHORT_UUID));
	}
	
	@Test(expected = InvalidRequestException.class)
	public void update_shouldThrowInvalidRequestExceptionIfIdIsNull() {
		handler.update(null, group);
	}
	
	@Test(expected = InvalidRequestException.class)
	public void update_shouldThrowInvalidRequestExceptionIfIdIsBad() {
		handler.update(BAD_COHORT_UUID, group);
	}
	
	@Test(expected = ResourceNotFoundException.class)
	public void update_shouldThrowResourceNotFoundException() {
		handler.update(COHORT_UUID, group);
	}
	
	@Test
	public void update_shouldUpdateGroup() {
		cohort.setVoided(false);
		group.setActive(false);
		
		when(dao.get(COHORT_UUID)).thenReturn(cohort);
		when(translator.toFhirResource(cohort)).thenReturn(group);
		when(translator.toOpenmrsType(cohort, group)).thenReturn(cohort);
		when(dao.createOrUpdate(cohort)).thenReturn(cohort);
		
		Group result = handler.update(COHORT_UUID, group);
		assertThat(result, notNullValue());
		assertThat(result.getActive(), is(false));
	}
	
	@Test
	public void delete_shouldDeleteGroup() {
		when(dao.delete(COHORT_UUID)).thenReturn(cohort);
		
		handler.delete(COHORT_UUID);
	}
	
	// ---- search ----
	
	@Test
	public void search_shouldReturnGroupsByParticipant() {
		ReferenceAndListParam participant = new ReferenceAndListParam();
		participant.addValue(
		    new ReferenceOrListParam().add(new ReferenceParam().setValue(COHORT_UUID).setChain(Practitioner.SP_RES_ID)));
		
		SearchParameterMap theParams = new SearchParameterMap()
		        .addParameter(FhirConstants.PARTICIPANT_REFERENCE_SEARCH_HANDLER, participant);
		
		when(dao.getSearchResults(any())).thenReturn(Collections.singletonList(cohort));
		when(translator.toFhirResource(cohort)).thenReturn(group);
		when(translator.toFhirResources(anyCollection())).thenCallRealMethod();
		when(searchQuery.getQueryResults(any(), any(), any(), any())).thenReturn(
		    new SearchQueryBundleProvider<>(theParams, dao, translator, globalPropertyService, searchQueryInclude));
		when(searchQueryInclude.getIncludedResources(any(), any())).thenReturn(Collections.emptySet());
		
		List<IBaseResource> resultList = get(handler.search(theParams));
		
		assertThat(resultList, not(empty()));
		assertThat(resultList, hasSize(greaterThanOrEqualTo(1)));
	}
}
