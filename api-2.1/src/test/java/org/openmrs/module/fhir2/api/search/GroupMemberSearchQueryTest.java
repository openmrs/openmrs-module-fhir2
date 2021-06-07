/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.fhir2.api.search;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;

import java.util.List;

import ca.uhn.fhir.rest.api.server.IBundleProvider;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.TestFhirSpringConfiguration;
import org.openmrs.module.fhir2.api.dao.FhirCohortMembershipDao;
import org.openmrs.module.fhir2.api.search.param.SearchParameterMap;
import org.openmrs.module.fhir2.api.translators.GroupMemberTranslator_2_1;
import org.openmrs.module.fhir2.model.GroupMember;
import org.openmrs.test.BaseModuleContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

@ContextConfiguration(classes = TestFhirSpringConfiguration.class, inheritLocations = false)
public class GroupMemberSearchQueryTest extends BaseModuleContextSensitiveTest {
	
	private static final String COHORT_MEMBER_INITIAL_DATA_XML = "org/openmrs/module/fhir2/api/dao/impl/FhirCohortMemberDaoImplTest_initial_data.xml";
	
	private static final int START_INDEX = 0;
	
	private static final int END_INDEX = 10;
	
	@Autowired
	private FhirCohortMembershipDao dao;
	
	@Autowired
	private GroupMemberTranslator_2_1 translator;
	
	@Autowired
	private SearchQueryInclude<GroupMember> searchQueryInclude;
	
	@Autowired
	SearchQuery<org.openmrs.CohortMembership, GroupMember, FhirCohortMembershipDao, GroupMemberTranslator_2_1, SearchQueryInclude<GroupMember>> searchQuery;
	
	private List<IBaseResource> get(IBundleProvider results) {
		return results.getResources(START_INDEX, END_INDEX);
	}
	
	private IBundleProvider search(SearchParameterMap theParams) {
		return searchQuery.getQueryResults(theParams, dao, translator, searchQueryInclude);
	}
	
	@Before
	public void setup() throws Exception {
		executeDataSet(COHORT_MEMBER_INITIAL_DATA_XML);
	}
	
	@Test
	public void searchForGroupMembers_shouldSearchForGroupMembersByGroupUuid() {
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.GROUP_MEMBERS_SEARCH_HANDLER,
		    "2d64befb-3b2e-48e5-85f5-353d43e23e48");
		
		IBundleProvider results = search(theParams);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList, not(empty()));
		assertThat(resultList, hasSize((6)));
	}
}
