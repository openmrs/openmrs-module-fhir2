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
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;

import java.util.List;

import ca.uhn.fhir.rest.api.server.IBundleProvider;
import ca.uhn.fhir.rest.param.ReferenceAndListParam;
import ca.uhn.fhir.rest.param.ReferenceOrListParam;
import ca.uhn.fhir.rest.param.ReferenceParam;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.Group;
import org.hl7.fhir.r4.model.Practitioner;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.module.fhir2.BaseFhirContextSensitiveTest;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.api.dao.FhirGroupDao;
import org.openmrs.module.fhir2.api.search.param.SearchParameterMap;
import org.openmrs.module.fhir2.api.translators.GroupTranslator;
import org.springframework.beans.factory.annotation.Autowired;

public class GroupSearchQueryTest extends BaseFhirContextSensitiveTest {
	
	private static final String COHORT_INITIAL_DATA_XML = "org/openmrs/module/fhir2/api/dao/impl/FhirCohortDaoImplTest_initial_data.xml";
	
	private static final String PARTICIPANT_FAMILY_NAME = "User";
	
	private static final String WRONG_FAMILY_NAME = "Wrong family name";
	
	private static final String PARTICIPANT_GIVEN_NAME = "Super";
	
	private static final String WRONG_GIVEN_NAME = "Wrong given name";
	
	private static final String PARTICIPANT_FULL_NAME = "Super User";
	
	private static final String WRONG_NAME = "Wrong name";
	
	private static final String PARTICIPANT_UUID = "1010d442-e134-11de-babe-001e378eb67e";
	
	private static final String WRONG_UUID = "c2299800-cca9-11e0-9572-abcdef0c9a66";
	
	private static final int START_INDEX = 0;
	
	private static final int END_INDEX = 10;
	
	@Autowired
	private FhirGroupDao dao;
	
	@Autowired
	private GroupTranslator translator;
	
	@Autowired
	private SearchQueryInclude<org.hl7.fhir.r4.model.Group> searchQueryInclude;
	
	@Autowired
	SearchQuery<org.openmrs.Cohort, org.hl7.fhir.r4.model.Group, FhirGroupDao, GroupTranslator, SearchQueryInclude<org.hl7.fhir.r4.model.Group>> searchQuery;
	
	private List<IBaseResource> get(IBundleProvider results) {
		return results.getResources(START_INDEX, END_INDEX);
	}
	
	private IBundleProvider search(SearchParameterMap theParams) {
		return searchQuery.getQueryResults(theParams, dao, translator, searchQueryInclude);
	}
	
	@Before
	public void setup() throws Exception {
		executeDataSet(COHORT_INITIAL_DATA_XML);
	}
	
	@Test
	public void searchForGroups_shouldSearchForGroupsByPractitionerGivenName() {
		ReferenceAndListParam participantReference = new ReferenceAndListParam();
		ReferenceParam participant = new ReferenceParam();
		
		participant.setValue(PARTICIPANT_GIVEN_NAME);
		participant.setChain(Practitioner.SP_GIVEN);
		
		participantReference.addValue(new ReferenceOrListParam().add(participant));
		
		SearchParameterMap theParams = new SearchParameterMap()
		        .addParameter(FhirConstants.PARTICIPANT_REFERENCE_SEARCH_HANDLER, participantReference);
		IBundleProvider results = search(theParams);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList, not(empty()));
		assertThat(resultList, hasSize(greaterThanOrEqualTo(1)));
	}
	
	@Test
	public void searchForGroups_shouldSearchForGroupsByMultiplePractitionerGivenNameOr() {
		ReferenceAndListParam participantReference = new ReferenceAndListParam();
		ReferenceParam participant = new ReferenceParam();
		
		participant.setValue(PARTICIPANT_GIVEN_NAME);
		participant.setChain(Practitioner.SP_GIVEN);
		
		ReferenceParam badParticipant = new ReferenceParam();
		
		badParticipant.setValue(WRONG_GIVEN_NAME);
		badParticipant.setChain(Practitioner.SP_GIVEN);
		
		participantReference.addValue(new ReferenceOrListParam().add(participant).add(badParticipant));
		
		SearchParameterMap theParams = new SearchParameterMap()
		        .addParameter(FhirConstants.PARTICIPANT_REFERENCE_SEARCH_HANDLER, participantReference);
		IBundleProvider results = search(theParams);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList, not(empty()));
		assertThat(resultList, hasSize(greaterThanOrEqualTo(1)));
	}
	
	@Test
	public void searchForGroups_shouldReturnEmptyListOfGroupsByMultiplePractitionerGivenNameAnd() {
		ReferenceAndListParam participantReference = new ReferenceAndListParam();
		ReferenceParam participant = new ReferenceParam();
		
		participant.setValue(PARTICIPANT_GIVEN_NAME);
		participant.setChain(Practitioner.SP_GIVEN);
		
		ReferenceParam badParticipant = new ReferenceParam();
		
		badParticipant.setValue(WRONG_GIVEN_NAME);
		badParticipant.setChain(Practitioner.SP_GIVEN);
		
		participantReference.addValue(new ReferenceOrListParam().add(participant))
		        .addAnd(new ReferenceOrListParam().add(badParticipant));
		
		SearchParameterMap theParams = new SearchParameterMap()
		        .addParameter(FhirConstants.PARTICIPANT_REFERENCE_SEARCH_HANDLER, participantReference);
		IBundleProvider results = search(theParams);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList, empty());
	}
	
	@Test
	public void searchForGroups_shouldSearchForGroupsByPractitionerFamilyName() {
		ReferenceAndListParam participantReference = new ReferenceAndListParam();
		ReferenceParam participant = new ReferenceParam();
		
		participant.setValue(PARTICIPANT_FAMILY_NAME);
		participant.setChain(Practitioner.SP_FAMILY);
		
		participantReference.addValue(new ReferenceOrListParam().add(participant));
		
		SearchParameterMap theParams = new SearchParameterMap()
		        .addParameter(FhirConstants.PARTICIPANT_REFERENCE_SEARCH_HANDLER, participantReference);
		IBundleProvider results = search(theParams);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList, not(empty()));
		assertThat(resultList, hasSize(greaterThanOrEqualTo(1)));
	}
	
	@Test
	public void searchForGroups_shouldSearchForGroupsByMultiplePractitionerFamilyNameOr() {
		ReferenceAndListParam participantReference = new ReferenceAndListParam();
		ReferenceParam participant = new ReferenceParam();
		
		participant.setValue(PARTICIPANT_FAMILY_NAME);
		participant.setChain(Practitioner.SP_FAMILY);
		
		ReferenceParam badParticipant = new ReferenceParam();
		
		badParticipant.setValue(WRONG_FAMILY_NAME);
		badParticipant.setChain(Practitioner.SP_FAMILY);
		
		participantReference.addValue(new ReferenceOrListParam().add(participant).add(badParticipant));
		
		SearchParameterMap theParams = new SearchParameterMap()
		        .addParameter(FhirConstants.PARTICIPANT_REFERENCE_SEARCH_HANDLER, participantReference);
		IBundleProvider results = search(theParams);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList, not(empty()));
		assertThat(resultList, hasSize(greaterThanOrEqualTo(1)));
	}
	
	@Test
	public void searchForGroups_shouldReturnEmptyListOfGroupsByMultiplePractitionerFamilyNameAnd() {
		ReferenceAndListParam participantReference = new ReferenceAndListParam();
		ReferenceParam participant = new ReferenceParam();
		
		participant.setValue(PARTICIPANT_FAMILY_NAME);
		participant.setChain(Practitioner.SP_FAMILY);
		
		ReferenceParam badParticipant = new ReferenceParam();
		
		badParticipant.setValue(WRONG_FAMILY_NAME);
		badParticipant.setChain(Practitioner.SP_FAMILY);
		
		participantReference.addValue(new ReferenceOrListParam().add(participant))
		        .addAnd(new ReferenceOrListParam().add(badParticipant));
		
		SearchParameterMap theParams = new SearchParameterMap()
		        .addParameter(FhirConstants.PARTICIPANT_REFERENCE_SEARCH_HANDLER, participantReference);
		IBundleProvider results = search(theParams);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList, empty());
	}
	
	@Test
	public void searchForGroups_shouldSearchForGroupsByPractitionerName() {
		ReferenceAndListParam participantReference = new ReferenceAndListParam();
		ReferenceParam participant = new ReferenceParam();
		
		participant.setValue(PARTICIPANT_FULL_NAME);
		participant.setChain(Practitioner.SP_NAME);
		
		participantReference.addValue(new ReferenceOrListParam().add(participant));
		
		SearchParameterMap theParams = new SearchParameterMap()
		        .addParameter(FhirConstants.PARTICIPANT_REFERENCE_SEARCH_HANDLER, participantReference);
		IBundleProvider results = search(theParams);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList, not(empty()));
		assertThat(resultList, hasSize(greaterThanOrEqualTo(1)));
	}
	
	@Test
	public void searchForGroups_shouldSearchForGroupsByMultiplePractitionerNameOr() {
		ReferenceAndListParam participantReference = new ReferenceAndListParam();
		ReferenceParam participant = new ReferenceParam();
		
		participant.setValue(PARTICIPANT_FULL_NAME);
		participant.setChain(Practitioner.SP_NAME);
		
		ReferenceParam badParticipant = new ReferenceParam();
		
		badParticipant.setValue(WRONG_NAME);
		badParticipant.setChain(Practitioner.SP_NAME);
		
		participantReference.addValue(new ReferenceOrListParam().add(participant).add(badParticipant));
		
		SearchParameterMap theParams = new SearchParameterMap()
		        .addParameter(FhirConstants.PARTICIPANT_REFERENCE_SEARCH_HANDLER, participantReference);
		IBundleProvider results = search(theParams);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList, not(empty()));
		assertThat(resultList, hasSize(greaterThanOrEqualTo(1)));
	}
	
	@Test
	public void searchForGroups_shouldReturnEmptyListOfGroupsByMultiplePractitionerNameAnd() {
		ReferenceAndListParam participantReference = new ReferenceAndListParam();
		ReferenceParam participant = new ReferenceParam();
		
		participant.setValue(PARTICIPANT_FULL_NAME);
		participant.setChain(Practitioner.SP_NAME);
		
		ReferenceParam badParticipant = new ReferenceParam();
		
		badParticipant.setValue(WRONG_NAME);
		badParticipant.setChain(Practitioner.SP_NAME);
		
		participantReference.addValue(new ReferenceOrListParam().add(participant))
		        .addAnd(new ReferenceOrListParam().add(badParticipant));
		
		SearchParameterMap theParams = new SearchParameterMap()
		        .addParameter(FhirConstants.PARTICIPANT_REFERENCE_SEARCH_HANDLER, participantReference);
		IBundleProvider results = search(theParams);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList, empty());
	}
	
	@Test
	public void searchForGroups_shouldSearchForGroupsByPractitionerUuid() {
		ReferenceAndListParam participantReference = new ReferenceAndListParam();
		ReferenceParam participant = new ReferenceParam();
		
		participant.setValue(PARTICIPANT_UUID);
		
		participantReference.addValue(new ReferenceOrListParam().add(participant));
		
		SearchParameterMap theParams = new SearchParameterMap()
		        .addParameter(FhirConstants.PARTICIPANT_REFERENCE_SEARCH_HANDLER, participantReference);
		IBundleProvider results = search(theParams);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList, not(empty()));
		assertThat(resultList, hasSize(greaterThanOrEqualTo(1)));
		assertThat(((Group) resultList.iterator().next()).getManagingEntity().getReferenceElement().getIdPart(),
		    equalTo(PARTICIPANT_UUID));
	}
	
	@Test
	public void searchForGroups_shouldSearchForGroupsByMultiplePractitionerUuidOr() {
		ReferenceAndListParam participantReference = new ReferenceAndListParam();
		ReferenceParam participant = new ReferenceParam();
		
		participant.setValue(PARTICIPANT_UUID);
		
		ReferenceParam badParticipant = new ReferenceParam();
		
		badParticipant.setValue(WRONG_UUID);
		
		participantReference.addValue(new ReferenceOrListParam().add(participant).add(badParticipant));
		
		SearchParameterMap theParams = new SearchParameterMap()
		        .addParameter(FhirConstants.PARTICIPANT_REFERENCE_SEARCH_HANDLER, participantReference);
		IBundleProvider results = search(theParams);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList, not(empty()));
		assertThat(resultList, hasSize(greaterThanOrEqualTo(1)));
	}
	
	@Test
	public void searchForGroups_shouldReturnEmptyListOfGroupsByMultiplePractitionerUuidAnd() {
		ReferenceAndListParam participantReference = new ReferenceAndListParam();
		ReferenceParam participant = new ReferenceParam();
		
		participant.setValue(PARTICIPANT_UUID);
		
		ReferenceParam badParticipant = new ReferenceParam();
		
		badParticipant.setValue(WRONG_UUID);
		
		participantReference.addValue(new ReferenceOrListParam().add(participant))
		        .addAnd(new ReferenceOrListParam().add(badParticipant));
		
		SearchParameterMap theParams = new SearchParameterMap()
		        .addParameter(FhirConstants.PARTICIPANT_REFERENCE_SEARCH_HANDLER, participantReference);
		IBundleProvider results = search(theParams);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList, empty());
	}
}
