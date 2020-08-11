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
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.openmrs.module.fhir2.FhirConstants.ADDRESS_SEARCH_HANDLER;
import static org.openmrs.module.fhir2.FhirConstants.CITY_PROPERTY;
import static org.openmrs.module.fhir2.FhirConstants.COMMON_SEARCH_HANDLER;
import static org.openmrs.module.fhir2.FhirConstants.COUNTRY_PROPERTY;
import static org.openmrs.module.fhir2.FhirConstants.FAMILY_PROPERTY;
import static org.openmrs.module.fhir2.FhirConstants.GIVEN_PROPERTY;
import static org.openmrs.module.fhir2.FhirConstants.IDENTIFIER_SEARCH_HANDLER;
import static org.openmrs.module.fhir2.FhirConstants.ID_PROPERTY;
import static org.openmrs.module.fhir2.FhirConstants.LAST_UPDATED_PROPERTY;
import static org.openmrs.module.fhir2.FhirConstants.NAME_PROPERTY;
import static org.openmrs.module.fhir2.FhirConstants.NAME_SEARCH_HANDLER;
import static org.openmrs.module.fhir2.FhirConstants.POSTAL_CODE_PROPERTY;
import static org.openmrs.module.fhir2.FhirConstants.STATE_PROPERTY;

import java.util.List;
import java.util.stream.Collectors;

import ca.uhn.fhir.rest.api.server.IBundleProvider;
import ca.uhn.fhir.rest.param.DateRangeParam;
import ca.uhn.fhir.rest.param.StringAndListParam;
import ca.uhn.fhir.rest.param.StringOrListParam;
import ca.uhn.fhir.rest.param.StringParam;
import ca.uhn.fhir.rest.param.TokenAndListParam;
import ca.uhn.fhir.rest.param.TokenOrListParam;
import ca.uhn.fhir.rest.param.TokenParam;
import org.hl7.fhir.r4.model.Practitioner;
import org.junit.Before;
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
public class UserSearchQueryTest extends BaseModuleContextSensitiveTest {
	
	private static final String USER_INITIAL_DATA_XML = "org/openmrs/module/fhir2/api/dao/impl/FhirUserDaoImplTest_initial_data.xml";
	
	private static final String USER_UUID = "1010d442-e134-11de-babe-001e378eb67e";
	
	private static final String USER_NAME = "admin";
	
	private static final String NOT_FOUND_USER_NAME = "waf";
	
	private static final String NOT_FOUND_USER_IDENTIFIER = "38934-t";
	
	private static final String USER_GIVEN_NAME = "Super";
	
	private static final String WRONG_GIVEN_NAME = "Wrong given name";
	
	private static final String USER_FAMILY_NAME = "User";
	
	private static final String WRONG_FAMILY_NAME = "Wrong family name";
	
	private static final String CITY = "Kamwokya";
	
	private static final String WRONG_CITY = "Wrong city";
	
	private static final String STATE = "IN";
	
	private static final String WRONG_STATE = "Wrong state";
	
	private static final String POSTAL_CODE = "256";
	
	private static final String WRONG_POSTAL_CODE = "Wrong postal code";
	
	private static final String COUNTRY = "UGANDA";
	
	private static final String WRONG_COUNTRY = "Wrong country";
	
	private static final String DATE_CREATED = "2005-01-01";
	
	private static final String DATE_CHANGED = "2007-09-20";
	
	private static final String DATE_RETIRED = "2010-09-03";
	
	private static final int START_INDEX = 0;
	
	private static final int END_INDEX = 10;
	
	@Autowired
	private FhirUserDao dao;
	
	@Autowired
	private PractitionerTranslator<User> translator;
	
	@Autowired
	private SearchQueryInclude<Practitioner> searchQueryInclude;
	
	@Autowired
	private SearchQuery<User, Practitioner, FhirUserDao, PractitionerTranslator<User>, SearchQueryInclude<Practitioner>> searchQuery;
	
	private IBundleProvider search(SearchParameterMap theParams) {
		return searchQuery.getQueryResults(theParams, dao, translator, searchQueryInclude);
	}
	
	private List<Practitioner> get(IBundleProvider results) {
		return results.getResources(START_INDEX, END_INDEX).stream().filter(it -> it instanceof Practitioner)
		        .map(it -> (Practitioner) it).collect(Collectors.toList());
	}
	
	@Before
	public void setUp() throws Exception {
		executeDataSet(USER_INITIAL_DATA_XML);
	}
	
	@Test
	public void searchForUsers_shouldReturnUsersByName() {
		StringAndListParam name = new StringAndListParam()
		        .addAnd(new StringOrListParam().add(new StringParam(USER_GIVEN_NAME)));
		SearchParameterMap theParams = new SearchParameterMap().addParameter(NAME_SEARCH_HANDLER, NAME_PROPERTY, name);
		
		IBundleProvider results = search(theParams);
		
		assertThat(results, notNullValue());
		assertThat(results.size(), equalTo(2));
		
		List<Practitioner> resultList = get(results);
		
		assertThat(resultList, hasSize(equalTo(2)));
		assertThat(resultList, hasItem(hasProperty("id", equalTo(USER_UUID))));
	}
	
	@Test
	public void searchForUsers_shouldReturnEmptyCollectionWhenNameNotMatched() {
		StringAndListParam name = new StringAndListParam()
		        .addAnd(new StringOrListParam().add(new StringParam(NOT_FOUND_USER_NAME)));
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.NAME_SEARCH_HANDLER,
		    FhirConstants.NAME_PROPERTY, name);
		
		IBundleProvider results = search(theParams);
		
		assertThat(results, notNullValue());
		assertThat(results.size(), equalTo(0));
		
		List<Practitioner> resultList = get(results);
		
		assertThat(resultList, is(empty()));
	}
	
	@Test
	public void searchForUsers_shouldReturnUsersByIdentifier() {
		TokenAndListParam identifier = new TokenAndListParam().addAnd(new TokenOrListParam().add(USER_NAME));
		SearchParameterMap theParams = new SearchParameterMap().addParameter(IDENTIFIER_SEARCH_HANDLER, identifier);
		
		IBundleProvider results = search(theParams);
		
		assertThat(results, notNullValue());
		assertThat(results.size(), greaterThanOrEqualTo(1));
		
		List<Practitioner> resultList = get(results);
		
		assertThat(resultList, hasSize(greaterThanOrEqualTo(1)));
		assertThat(resultList.get(0).getIdElement().getIdPart(), equalTo(USER_UUID));
		
	}
	
	@Test
	public void searchForUsers_shouldReturnEmptyCollectionWhenIdentifierNotMatched() {
		TokenAndListParam identifier = new TokenAndListParam().addAnd(new TokenOrListParam().add(NOT_FOUND_USER_IDENTIFIER));
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.IDENTIFIER_SEARCH_HANDLER,
		    identifier);
		
		IBundleProvider results = search(theParams);
		
		assertThat(results, notNullValue());
		assertThat(results.size(), equalTo(0));
		
		List<Practitioner> resultList = get(results);
		
		assertThat(resultList, is(empty()));
	}
	
	@Test
	public void searchForUsers_shouldReturnUsersByGivenName() {
		StringAndListParam givenName = new StringAndListParam().addAnd(new StringParam(USER_GIVEN_NAME));
		SearchParameterMap theParams = new SearchParameterMap().addParameter(NAME_SEARCH_HANDLER, GIVEN_PROPERTY, givenName);
		
		IBundleProvider results = search(theParams);
		
		assertThat(results, notNullValue());
		assertThat(results.size(), greaterThanOrEqualTo(1));
		
		List<Practitioner> resultList = get(results);
		
		assertThat(resultList, hasSize(greaterThanOrEqualTo(1)));
		assertThat(resultList, everyItem(
		    hasProperty("name", hasItem(hasProperty("given", hasItem(hasProperty("value", equalTo(USER_GIVEN_NAME))))))));
	}
	
	@Test
	public void searchForUsers_shouldReturnEmptyCollectionForWrongGivenName() {
		StringAndListParam givenName = new StringAndListParam().addAnd(new StringParam(WRONG_GIVEN_NAME));
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.NAME_SEARCH_HANDLER,
		    FhirConstants.GIVEN_PROPERTY, givenName);
		
		IBundleProvider results = search(theParams);
		
		assertThat(results, notNullValue());
		assertThat(results.size(), equalTo(0));
		
		List<Practitioner> resultList = get(results);
		
		assertThat(resultList, empty());
	}
	
	@Test
	public void searchForUsers_shouldReturnUsersByFamilyName() {
		StringAndListParam familyName = new StringAndListParam().addAnd(new StringParam(USER_FAMILY_NAME));
		SearchParameterMap theParams = new SearchParameterMap().addParameter(NAME_SEARCH_HANDLER, FAMILY_PROPERTY,
		    familyName);
		
		IBundleProvider results = search(theParams);
		
		assertThat(results, notNullValue());
		assertThat(results.size(), greaterThanOrEqualTo(1));
		
		List<Practitioner> resultList = get(results);
		
		assertThat(resultList, hasSize(greaterThanOrEqualTo(1)));
		assertThat(resultList, everyItem(hasProperty("name", hasItem(hasProperty("family", equalTo(USER_FAMILY_NAME))))));
	}
	
	@Test
	public void searchForUsers_shouldReturnEmptyCollectionForWrongFamilyName() {
		StringAndListParam familyName = new StringAndListParam().addAnd(new StringParam(WRONG_FAMILY_NAME));
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.NAME_SEARCH_HANDLER,
		    FhirConstants.FAMILY_PROPERTY, familyName);
		
		IBundleProvider results = search(theParams);
		
		assertThat(results, notNullValue());
		assertThat(results.size(), equalTo(0));
		
		List<Practitioner> resultList = get(results);
		
		assertThat(resultList, empty());
	}
	
	@Test
	public void searchForUsers_shouldReturnUsersByMatchingGivenAndFamilyName() {
		StringAndListParam givenName = new StringAndListParam().addAnd(new StringParam(USER_GIVEN_NAME));
		StringAndListParam familyName = new StringAndListParam().addAnd(new StringParam(USER_FAMILY_NAME));
		
		SearchParameterMap theParams = new SearchParameterMap().addParameter(NAME_SEARCH_HANDLER, GIVEN_PROPERTY, givenName)
		        .addParameter(NAME_SEARCH_HANDLER, FAMILY_PROPERTY, familyName);
		
		IBundleProvider results = search(theParams);
		
		assertThat(results, notNullValue());
		assertThat(results.size(), greaterThanOrEqualTo(1));
		
		List<Practitioner> resultList = get(results);
		
		assertThat(resultList, hasSize(greaterThanOrEqualTo(1)));
		assertThat(resultList, everyItem(
		    hasProperty("name", hasItem(hasProperty("given", hasItem(hasProperty("value", equalTo(USER_GIVEN_NAME))))))));
		assertThat(resultList, everyItem(hasProperty("name", hasItem(hasProperty("family", equalTo(USER_FAMILY_NAME))))));
	}
	
	@Test
	public void searchForUsers_shouldReturnEmptyCollectionForMismatchingGivenAndFamilyName() {
		StringAndListParam givenName = new StringAndListParam().addAnd(new StringParam(WRONG_GIVEN_NAME));
		StringAndListParam familyName = new StringAndListParam().addAnd(new StringParam(USER_FAMILY_NAME));
		
		SearchParameterMap theParams = new SearchParameterMap()
		        .addParameter(FhirConstants.NAME_SEARCH_HANDLER, FhirConstants.GIVEN_PROPERTY, givenName)
		        .addParameter(FhirConstants.NAME_SEARCH_HANDLER, FhirConstants.FAMILY_PROPERTY, familyName);
		
		IBundleProvider results = search(theParams);
		
		assertThat(results, notNullValue());
		assertThat(results.size(), equalTo(0));
		
		List<Practitioner> resultList = get(results);
		
		assertThat(resultList, empty());
	}
	
	@Test
	public void searchForUsers_shouldReturnUsersByCity() {
		StringAndListParam city = new StringAndListParam().addAnd(new StringParam(CITY));
		
		SearchParameterMap theParams = new SearchParameterMap().addParameter(ADDRESS_SEARCH_HANDLER, CITY_PROPERTY, city);
		
		IBundleProvider results = search(theParams);
		
		assertThat(results, notNullValue());
		assertThat(results.size(), greaterThanOrEqualTo(1));
		
		List<Practitioner> resultList = get(results);
		
		assertThat(resultList, hasSize(greaterThanOrEqualTo(1)));
		assertThat(resultList.get(0).getAddressFirstRep().getCity(), equalTo(CITY));
	}
	
	@Test
	public void searchForUsers_shouldReturnEmptyCollectionByWrongCity() {
		StringAndListParam city = new StringAndListParam().addAnd(new StringParam(WRONG_CITY));
		
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.ADDRESS_SEARCH_HANDLER,
		    FhirConstants.CITY_PROPERTY, city);
		
		IBundleProvider results = search(theParams);
		
		assertThat(results, notNullValue());
		assertThat(results.size(), equalTo(0));
		
		List<Practitioner> resultList = get(results);
		
		assertThat(resultList, empty());
	}
	
	@Test
	public void searchForUsers_shouldReturnUsersByState() {
		StringAndListParam state = new StringAndListParam().addAnd(new StringParam(STATE));
		
		SearchParameterMap theParams = new SearchParameterMap().addParameter(ADDRESS_SEARCH_HANDLER, STATE_PROPERTY, state);
		
		IBundleProvider results = search(theParams);
		
		assertThat(results, notNullValue());
		assertThat(results.size(), greaterThanOrEqualTo(1));
		
		List<Practitioner> resultList = get(results);
		
		assertThat(resultList, hasSize(greaterThanOrEqualTo(1)));
		assertThat(resultList.get(0).getAddressFirstRep().getState(), equalTo(STATE));
	}
	
	@Test
	public void searchForUsers_shouldReturnEmptyCollectionByWrongState() {
		StringAndListParam state = new StringAndListParam().addAnd(new StringParam(WRONG_STATE));
		
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.ADDRESS_SEARCH_HANDLER,
		    FhirConstants.STATE_PROPERTY, state);
		
		IBundleProvider results = search(theParams);
		
		assertThat(results, notNullValue());
		assertThat(results.size(), equalTo(0));
		
		List<Practitioner> resultList = get(results);
		
		assertThat(resultList, empty());
	}
	
	@Test
	public void searchForUsers_shouldReturnUsersByPostalCode() {
		StringAndListParam postalCode = new StringAndListParam().addAnd(new StringParam(POSTAL_CODE));
		
		SearchParameterMap theParams = new SearchParameterMap().addParameter(ADDRESS_SEARCH_HANDLER, POSTAL_CODE_PROPERTY,
		    postalCode);
		
		IBundleProvider results = search(theParams);
		
		assertThat(results, notNullValue());
		assertThat(results.size(), greaterThanOrEqualTo(1));
		
		List<Practitioner> resultList = get(results);
		
		assertThat(resultList, hasSize(greaterThanOrEqualTo(1)));
		assertThat(resultList.get(0).getAddressFirstRep().getPostalCode(), equalTo(POSTAL_CODE));
	}
	
	@Test
	public void searchForUsers_shouldReturnEmptyCollectionByWrongPostalCode() {
		StringAndListParam postalCode = new StringAndListParam().addAnd(new StringParam(WRONG_POSTAL_CODE));
		
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.ADDRESS_SEARCH_HANDLER,
		    FhirConstants.POSTAL_CODE_PROPERTY, postalCode);
		
		IBundleProvider results = search(theParams);
		
		assertThat(results, notNullValue());
		assertThat(results.size(), equalTo(0));
		
		List<Practitioner> resultList = get(results);
		
		assertThat(resultList, empty());
	}
	
	@Test
	public void searchForUsers_shouldReturnUsersByCountry() {
		StringAndListParam country = new StringAndListParam().addAnd(new StringParam(COUNTRY));
		
		SearchParameterMap theParams = new SearchParameterMap().addParameter(ADDRESS_SEARCH_HANDLER, COUNTRY_PROPERTY,
		    country);
		
		IBundleProvider results = search(theParams);
		
		assertThat(results, notNullValue());
		assertThat(results.size(), greaterThanOrEqualTo(1));
		
		List<Practitioner> resultList = get(results);
		
		assertThat(resultList, hasSize(greaterThanOrEqualTo(1)));
		assertThat(resultList.get(0).getAddressFirstRep().getCountry(), equalTo(COUNTRY));
	}
	
	@Test
	public void searchForUsers_shouldReturnEmptyCollectionByWrongCountry() {
		StringAndListParam country = new StringAndListParam().addAnd(new StringParam(WRONG_COUNTRY));
		
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.ADDRESS_SEARCH_HANDLER,
		    FhirConstants.COUNTRY_PROPERTY, country);
		
		IBundleProvider results = search(theParams);
		
		assertThat(results, notNullValue());
		assertThat(results.size(), equalTo(0));
		
		List<Practitioner> resultList = get(results);
		
		assertThat(resultList, empty());
	}
	
	@Test
	public void searchForUsers_shouldReturnUsersByMatchingCityAndCountry() {
		StringAndListParam city = new StringAndListParam().addAnd(new StringParam(CITY));
		StringAndListParam country = new StringAndListParam().addAnd(new StringParam(COUNTRY));
		
		SearchParameterMap theParams = new SearchParameterMap().addParameter(ADDRESS_SEARCH_HANDLER, CITY_PROPERTY, city)
		        .addParameter(ADDRESS_SEARCH_HANDLER, COUNTRY_PROPERTY, country);
		
		IBundleProvider results = search(theParams);
		
		assertThat(results, notNullValue());
		assertThat(results.size(), greaterThanOrEqualTo(1));
		
		List<Practitioner> resultList = get(results);
		
		assertThat(resultList, hasSize(greaterThanOrEqualTo(1)));
		assertThat(resultList.get(0).getAddressFirstRep().getCity(), equalTo(CITY));
		assertThat(resultList.get(0).getAddressFirstRep().getCountry(), equalTo(COUNTRY));
	}
	
	@Test
	public void searchForUsers_shouldReturnEmptyCollectionByMismatchingCityAndCountry() {
		StringAndListParam city = new StringAndListParam().addAnd(new StringParam(WRONG_CITY));
		StringAndListParam country = new StringAndListParam().addAnd(new StringParam(COUNTRY));
		
		SearchParameterMap theParams = new SearchParameterMap()
		        .addParameter(FhirConstants.ADDRESS_SEARCH_HANDLER, FhirConstants.CITY_PROPERTY, city)
		        .addParameter(FhirConstants.ADDRESS_SEARCH_HANDLER, FhirConstants.COUNTRY_PROPERTY, country);
		
		IBundleProvider results = search(theParams);
		
		assertThat(results, notNullValue());
		assertThat(results.size(), equalTo(0));
		
		List<Practitioner> resultList = get(results);
		
		assertThat(resultList, empty());
	}
	
	@Test
	public void searchForUsers_shouldHandleComplexQuery() {
		StringAndListParam name = new StringAndListParam()
		        .addAnd(new StringOrListParam().add(new StringParam(USER_GIVEN_NAME)));
		TokenAndListParam identifier = new TokenAndListParam().addAnd(new TokenOrListParam().add(USER_NAME));
		
		SearchParameterMap theParams = new SearchParameterMap().addParameter(NAME_SEARCH_HANDLER, NAME_PROPERTY, name)
		        .addParameter(IDENTIFIER_SEARCH_HANDLER, identifier);
		
		IBundleProvider results = search(theParams);
		
		assertThat(results, notNullValue());
		assertThat(results.size(), equalTo(1));
		
		List<Practitioner> resultList = get(results);
		
		assertThat(resultList, hasSize(equalTo(1)));
		assertThat(resultList.get(0).getIdElement().getIdPart(), equalTo(USER_UUID));
	}
	
	@Test
	public void searchForUsers_shouldSearchForUsersByUuid() {
		TokenAndListParam uuid = new TokenAndListParam().addAnd(new TokenParam(USER_UUID));
		
		SearchParameterMap theParams = new SearchParameterMap().addParameter(COMMON_SEARCH_HANDLER, ID_PROPERTY, uuid);
		
		IBundleProvider results = search(theParams);
		
		assertThat(results, notNullValue());
		assertThat(results.size(), equalTo(1));
		
		List<Practitioner> resultList = get(results);
		
		assertThat(resultList, hasSize(equalTo(1)));
		assertThat(resultList.get(0).getIdElement().getIdPart(), equalTo(USER_UUID));
	}
	
	@Test
	public void searchForUsers_shouldSearchForUsersByLastUpdatedDateCreated() {
		DateRangeParam lastUpdated = new DateRangeParam().setUpperBound(DATE_CREATED).setLowerBound(DATE_CREATED);
		
		SearchParameterMap theParams = new SearchParameterMap().addParameter(COMMON_SEARCH_HANDLER, LAST_UPDATED_PROPERTY,
		    lastUpdated);
		
		IBundleProvider results = search(theParams);
		
		assertThat(results, notNullValue());
		assertThat(results.size(), equalTo(1));
		
		List<Practitioner> resultList = get(results);
		
		assertThat(resultList, hasSize(equalTo(1)));
	}
	
	@Test
	public void searchForUsers_shouldSearchForUsersByLastUpdatedDateChanged() {
		DateRangeParam lastUpdated = new DateRangeParam().setUpperBound(DATE_CHANGED).setLowerBound(DATE_CHANGED);
		
		SearchParameterMap theParams = new SearchParameterMap().addParameter(COMMON_SEARCH_HANDLER, LAST_UPDATED_PROPERTY,
		    lastUpdated);
		
		IBundleProvider results = search(theParams);
		
		assertThat(results, notNullValue());
		assertThat(results.size(), equalTo(1));
		
		List<Practitioner> resultList = get(results);
		
		assertThat(resultList, hasSize(equalTo(1)));
	}
	
	@Test
	public void searchForUsers_shouldReturnEmptyListByMismatchingUuidAndLastUpdated() {
		TokenAndListParam uuid = new TokenAndListParam().addAnd(new TokenParam(USER_UUID));
		DateRangeParam lastUpdated = new DateRangeParam().setUpperBound(DATE_RETIRED).setLowerBound(DATE_RETIRED);
		
		SearchParameterMap theParams = new SearchParameterMap()
		        .addParameter(FhirConstants.COMMON_SEARCH_HANDLER, FhirConstants.ID_PROPERTY, uuid)
		        .addParameter(FhirConstants.COMMON_SEARCH_HANDLER, FhirConstants.LAST_UPDATED_PROPERTY, lastUpdated);
		
		IBundleProvider results = search(theParams);
		
		assertThat(results, notNullValue());
		assertThat(results.size(), equalTo(0));
		
		List<Practitioner> resultList = get(results);
		
		assertThat(resultList, empty());
	}
	
}
