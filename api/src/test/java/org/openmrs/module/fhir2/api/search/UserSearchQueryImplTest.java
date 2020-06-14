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
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;

import java.util.List;
import java.util.stream.Collectors;

import ca.uhn.fhir.rest.api.server.IBundleProvider;
import ca.uhn.fhir.rest.param.StringAndListParam;
import ca.uhn.fhir.rest.param.StringOrListParam;
import ca.uhn.fhir.rest.param.StringParam;
import ca.uhn.fhir.rest.param.TokenAndListParam;
import ca.uhn.fhir.rest.param.TokenOrListParam;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.Practitioner;
import org.junit.Test;
import org.openmrs.User;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.TestFhirSpringConfiguration;
import org.openmrs.module.fhir2.api.dao.FhirUserDao;
import org.openmrs.module.fhir2.api.search.param.SearchParameterMap;
import org.openmrs.module.fhir2.api.translators.PractitionerTranslator;
import org.openmrs.test.BaseModuleContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

@ContextConfiguration(classes = TestFhirSpringConfiguration.class, inheritLocations = false)
public class UserSearchQueryImplTest extends BaseModuleContextSensitiveTest {
	
	private static final String USER_UUID = "1010d442-e134-11de-babe-001e378eb67e";
	
	private static final String USER_NAME = "admin";
	
	private static final String USER_IDENTIFIER = "1-8";
	
	private static final String NOT_FOUND_USER_NAME = "waf";
	
	private static final String NOT_FOUND_USER_IDENTIFIER = "38934-t";
	
	private static final int START_INDEX = 0;
	
	private static final int END_INDEX = 10;
	
	@Autowired
	private FhirUserDao dao;
	
	@Autowired
	private PractitionerTranslator<User> translator;
	
	@Autowired
	private SearchQuery<User, Practitioner, FhirUserDao, PractitionerTranslator<User>> searchQuery;
	
	/* @Before
	public void setUp() throws Exception {
		executeDataSet(PRACTITIONER_INITIAL_DATA_XML);
	}
	 */
	
	private IBundleProvider search(SearchParameterMap theParams) {
		return searchQuery.getQueryResults(theParams, dao, translator);
	}
	
	private List<IBaseResource> get(IBundleProvider results) {
		return results.getResources(START_INDEX, END_INDEX);
	}
	
	@Test
	public void searchForUsers_shouldReturnUsersByName() {
		StringAndListParam name = new StringAndListParam().addAnd(new StringOrListParam().add(new StringParam(USER_NAME)));
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.NAME_SEARCH_HANDLER, name);
		
		IBundleProvider results = search(theParams);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList, not(empty()));
		assertThat(resultList.size(), equalTo(1));
		assertThat(resultList.iterator().next().getIdElement().getIdPart(), equalTo(USER_UUID));
	}
	
	@Test
	public void searchForPractitioners_shouldReturnEmptyCollectionWhenNameNotMatched() {
		StringAndListParam name = new StringAndListParam()
		        .addAnd(new StringOrListParam().add(new StringParam(NOT_FOUND_USER_NAME)));
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.NAME_SEARCH_HANDLER, name);
		
		IBundleProvider results = search(theParams);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList, is(empty()));
	}
	
	@Test
	public void searchForPractitioners_shouldReturnPractitionersByIdentifier() {
		TokenAndListParam identifier = new TokenAndListParam().addAnd(new TokenOrListParam().add(USER_IDENTIFIER));
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.IDENTIFIER_SEARCH_HANDLER,
		    identifier);
		
		IBundleProvider results = search(theParams);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList, not(empty()));
		assertThat(resultList.size(), greaterThanOrEqualTo(1));
		assertThat(resultList.iterator().next().getIdElement().getIdPart(), equalTo(USER_UUID));

	}
	
	@Test
	public void searchForPractitioners_shouldReturnEmptyCollectionWhenIdentifierNotMatched() {
		TokenAndListParam identifier = new TokenAndListParam().addAnd(new TokenOrListParam().add(NOT_FOUND_USER_IDENTIFIER));
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.IDENTIFIER_SEARCH_HANDLER,
		    identifier);
		
		IBundleProvider results = search(theParams);
		
		List<Practitioner> resultList = get(results).stream().map(p -> (Practitioner) p).collect(Collectors.toList());
		
		assertThat(results, notNullValue());
		assertThat(resultList, is(empty()));
	}
	
	@Test
	public void searchForPractitioners_shouldHandleComplexQuery() {
		StringAndListParam name = new StringAndListParam().addAnd(new StringOrListParam().add(new StringParam(USER_NAME)));
		TokenAndListParam identifier = new TokenAndListParam().addAnd(new TokenOrListParam().add(USER_IDENTIFIER));
		
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.NAME_SEARCH_HANDLER, name)
		        .addParameter(FhirConstants.IDENTIFIER_SEARCH_HANDLER, identifier);
		
		IBundleProvider results = search(theParams);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList, not(empty()));
		assertThat(resultList.size(), equalTo(1));
		assertThat(resultList.iterator().next().getIdElement().getIdPart(), equalTo(USER_UUID));
	}
	
}
