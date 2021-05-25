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

import javax.annotation.Nonnull;

import ca.uhn.fhir.rest.api.server.IBundleProvider;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.openmrs.CohortMembership;
import org.openmrs.annotation.OpenmrsProfile;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.api.FhirGroupMemberService;
import org.openmrs.module.fhir2.api.dao.CohortMembershipDao;
import org.openmrs.module.fhir2.api.search.SearchQuery;
import org.openmrs.module.fhir2.api.search.SearchQueryInclude;
import org.openmrs.module.fhir2.api.search.param.SearchParameterMap;
import org.openmrs.module.fhir2.api.translators.GroupMemberTranslator_2_1;
import org.openmrs.module.fhir2.model.GroupMember;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Primary
@Component
@Transactional
@Setter(AccessLevel.PACKAGE)
@Getter(AccessLevel.PROTECTED)
@OpenmrsProfile(openmrsPlatformVersion = "2.1.0 - 2.*")
public class FhirGroupMemberService_2_1 implements FhirGroupMemberService {
	
	@Autowired
	private CohortMembershipDao cohortMembershipDao;
	
	@Autowired
	private GroupMemberTranslator_2_1 groupMemberTranslator21;
	
	@Autowired
	private SearchQueryInclude<GroupMember> searchQueryInclude;
	
	@Autowired
	private SearchQuery<CohortMembership, GroupMember, CohortMembershipDao, GroupMemberTranslator_2_1, SearchQueryInclude<GroupMember>> searchQuery;
	
	@Override
	public IBundleProvider getGroupMembers(@Nonnull String groupUuid) {
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.GROUP_MEMBERS_SEARCH_HANDLER,
		    groupUuid);
		return searchQuery.getQueryResults(theParams, cohortMembershipDao, groupMemberTranslator21, searchQueryInclude);
	}
}
