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
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ca.uhn.fhir.rest.api.server.IBundleProvider;
import org.hamcrest.Matchers;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openmrs.CohortMembership;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.api.FhirGlobalPropertyService;
import org.openmrs.module.fhir2.api.dao.FhirCohortMembershipDao;
import org.openmrs.module.fhir2.api.search.SearchQuery;
import org.openmrs.module.fhir2.api.search.SearchQueryBundleProvider;
import org.openmrs.module.fhir2.api.search.SearchQueryInclude;
import org.openmrs.module.fhir2.api.search.param.SearchParameterMap;
import org.openmrs.module.fhir2.api.translators.GroupMemberTranslator_2_1;
import org.openmrs.module.fhir2.model.GroupMember;

@RunWith(MockitoJUnitRunner.class)
public class FhirGroupMemberService_2_1Test {
	
	private static final Integer GROUP_MEMBER_ID = 123;
	
	private static final String GROUP_MEMBER_UUID = "1359f03d-55d9-4961-b8f8-9a59eddc1f59";
	
	private static final String BAD_GROUP_MEMBER_UUID = "02ed36f0-6167-4372-a641-d27b92f7deae";
	
	@Mock
	private FhirCohortMembershipDao dao;
	
	@Mock
	private GroupMemberTranslator_2_1 translator;
	
	@Mock
	private FhirGlobalPropertyService globalPropertyService;
	
	@Mock
	private SearchQueryInclude<GroupMember> searchQueryInclude;
	
	@Mock
	private SearchQuery<CohortMembership, GroupMember, FhirCohortMembershipDao, GroupMemberTranslator_2_1, SearchQueryInclude<GroupMember>> searchQuery;
	
	private FhirGroupMemberService_2_1 groupMemberService;
	
	private static final int START_INDEX = 0;
	
	private static final int END_INDEX = 10;
	
	@Before
	public void setup() {
		groupMemberService = new FhirGroupMemberService_2_1();
		groupMemberService.setFhirCohortMembershipDao(dao);
		groupMemberService.setGroupMemberTranslator21(translator);
		groupMemberService.setSearchQuery(searchQuery);
		groupMemberService.setSearchQueryInclude(searchQueryInclude);
	}
	
	private List<IBaseResource> get(IBundleProvider results) {
		return results.getResources(START_INDEX, END_INDEX);
	}
	
	@Test
	public void shouldSearchForGroupMembersByGroupUuid() {
		GroupMember groupMember = mock(GroupMember.class);
		CohortMembership cohortMembership = mock(CohortMembership.class);
		
		List<CohortMembership> memberships = new ArrayList<>();
		memberships.add(cohortMembership);
		
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.GROUP_MEMBERS_SEARCH_HANDLER,
		    GROUP_MEMBER_UUID);
		
		when(dao.getSearchResults(any(), any())).thenReturn(memberships);
		when(dao.getSearchResultIds(any())).thenReturn(Collections.singletonList(GROUP_MEMBER_ID));
		when(translator.toFhirResource(cohortMembership)).thenReturn(groupMember);
		when(searchQuery.getQueryResults(any(), any(), any(), any())).thenReturn(
		    new SearchQueryBundleProvider<>(theParams, dao, translator, globalPropertyService, searchQueryInclude));
		when(searchQueryInclude.getIncludedResources(any(), any())).thenReturn(Collections.emptySet());
		
		IBundleProvider results = groupMemberService.getGroupMembers(GROUP_MEMBER_UUID);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, Matchers.notNullValue());
		assertThat(resultList, not(empty()));
		assertThat(resultList, hasSize(1));
	}
}
