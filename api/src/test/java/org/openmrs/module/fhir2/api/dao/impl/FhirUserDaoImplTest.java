/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.fhir2.api.dao.impl;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;

import java.util.List;

import ca.uhn.fhir.rest.param.TokenAndListParam;
import ca.uhn.fhir.rest.param.TokenParam;
import org.hibernate.SessionFactory;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.User;
import org.openmrs.module.fhir2.BaseFhirContextSensitiveTest;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.api.search.param.SearchParameterMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

public class FhirUserDaoImplTest extends BaseFhirContextSensitiveTest {
	
	private static final String USER_NAME = "firstaccount";
	
	private static final String USER_INITIAL_DATA_XML = "org/openmrs/api/include/UserServiceTest.xml";
	
	private static final String PRACTITIONER_USER_DATA_XML = "org/openmrs/module/fhir2/api/dao/impl/FhirUserDaoImplTest_practitioner_data.xml";
	
	private static final String PRACTITIONER_USERNAME = "testclerk";
	
	private static final String PRACTITIONER_SYSTEM_ID = "9-9";
	
	@Autowired
	@Qualifier("sessionFactory")
	private SessionFactory sessionFactory;
	
	private FhirUserDaoImpl fhirUserDao;
	
	@Before
	public void setup() throws Exception {
		fhirUserDao = new FhirUserDaoImpl();
		fhirUserDao.setSessionFactory(sessionFactory);
		executeDataSet(USER_INITIAL_DATA_XML);
		executeDataSet(PRACTITIONER_USER_DATA_XML);
	}
	
	@Test
	public void shouldGetUserByUserName() {
		User user = fhirUserDao.getUserByUserName(USER_NAME);
		
		assertThat(user, notNullValue());
		assertThat(user.getUsername(), equalTo(USER_NAME));
	}
	
	@Test
	public void getSearchResults_shouldReturnNothingForNonMatchingIdentifier() {
		List<User> results = fhirUserDao.getSearchResults(identifierSearch("nomatchxyz"));
		
		assertThat(results, empty());
	}
	
	@Test
	public void getSearchResults_shouldMatchIdentifierAgainstSystemId() {
		List<User> results = fhirUserDao.getSearchResults(identifierSearch(PRACTITIONER_SYSTEM_ID));
		
		assertThat(results, hasSize(1));
		assertThat(results.get(0).getSystemId(), equalTo(PRACTITIONER_SYSTEM_ID));
	}
	
	@Test
	public void getSearchResults_shouldMatchIdentifierAgainstUsername() {
		List<User> results = fhirUserDao.getSearchResults(identifierSearch(PRACTITIONER_USERNAME));
		
		assertThat(results, hasSize(1));
		assertThat(results.get(0).getUsername(), equalTo(PRACTITIONER_USERNAME));
	}
	
	@Test
	public void hasDistinctResults_shouldBeFalseSoDuplicatesAreRemoved() {
		assertThat(fhirUserDao.hasDistinctResults(), equalTo(false));
	}
	
	private SearchParameterMap identifierSearch(String value) {
		return new SearchParameterMap().addParameter(FhirConstants.IDENTIFIER_SEARCH_HANDLER,
		    new TokenAndListParam().addAnd(new TokenParam(value)));
	}
}
