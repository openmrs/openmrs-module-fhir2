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
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.openmrs.module.fhir2.FhirConstants.NAME_PROPERTY;
import static org.openmrs.module.fhir2.FhirConstants.NAME_SEARCH_HANDLER;

import java.util.HashSet;
import java.util.List;

import ca.uhn.fhir.model.api.Include;
import ca.uhn.fhir.rest.api.server.IBundleProvider;
import ca.uhn.fhir.rest.param.DateRangeParam;
import ca.uhn.fhir.rest.param.StringAndListParam;
import ca.uhn.fhir.rest.param.StringOrListParam;
import ca.uhn.fhir.rest.param.StringParam;
import ca.uhn.fhir.rest.param.TokenAndListParam;
import ca.uhn.fhir.rest.param.TokenOrListParam;
import ca.uhn.fhir.rest.param.TokenParam;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.Encounter;
import org.hl7.fhir.r4.model.MedicationRequest;
import org.hl7.fhir.r4.model.Practitioner;
import org.hl7.fhir.r4.model.ServiceRequest;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.Provider;
import org.openmrs.module.fhir2.BaseFhirContextSensitiveTest;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.api.dao.FhirPractitionerDao;
import org.openmrs.module.fhir2.api.search.param.SearchParameterMap;
import org.openmrs.module.fhir2.api.translators.PractitionerTranslator;
import org.springframework.beans.factory.annotation.Autowired;

public class PractitionerSearchQueryTest extends BaseFhirContextSensitiveTest {
	
	private static final String PRACTITIONER_INITIAL_DATA_XML = "org/openmrs/module/fhir2/api/dao/impl/FhirPractitionerDaoImplTest_initial_data.xml";
	
	private static final String PRACTITIONER_UUID = "f9badd80-ab76-11e2-9e96-0800200c9a66";
	
	private static final String PRACTITIONER_GIVEN_NAME = "John";
	
	private static final String WRONG_GIVEN_NAME = "Wrong given name";
	
	private static final String PRACTITIONER_FAMILY_NAME = "Doe";
	
	private static final String WRONG_FAMILY_NAME = "Wrong family name";
	
	private static final String PRACTITIONER_IDENTIFIER = "347834-gf";
	
	private static final String NOT_FOUND_PRACTITIONER_NAME = "waf";
	
	private static final String NOT_FOUND_PRACTITIONER_IDENTIFIER = "38934-t";
	
	private static final String CITY = "Indianapolis";
	
	private static final String WRONG_CITY = "Wrong city";
	
	private static final String STATE = "IN";
	
	private static final String WRONG_STATE = "Wrong state";
	
	private static final String POSTAL_CODE = "46202";
	
	private static final String WRONG_POSTAL_CODE = "Wrong postal code";
	
	private static final String COUNTRY = "USA";
	
	private static final String WRONG_COUNTRY = "Wrong country";
	
	private static final String DATE_CREATED = "2005-01-01";
	
	private static final String DATE_CHANGED = "2010-03-31";
	
	private static final String DATE_RETIRED = "2010-09-03";
	
	private static final int START_INDEX = 0;
	
	private static final int END_INDEX = 10;
	
	@Autowired
	private FhirPractitionerDao dao;
	
	@Autowired
	private PractitionerTranslator<Provider> translator;
	
	@Autowired
	private SearchQueryInclude<Practitioner> searchQueryInclude;
	
	@Autowired
	private SearchQuery<Provider, Practitioner, FhirPractitionerDao, PractitionerTranslator<Provider>, SearchQueryInclude<Practitioner>> searchQuery;
	
	@Before
	public void setUp() throws Exception {
		executeDataSet(PRACTITIONER_INITIAL_DATA_XML);
	}
	
	private IBundleProvider search(SearchParameterMap theParams) {
		return searchQuery.getQueryResults(theParams, dao, translator, searchQueryInclude);
	}
	
	private List<IBaseResource> get(IBundleProvider results) {
		return results.getResources(START_INDEX, END_INDEX);
	}
	
	@Test
	public void searchForPractitioners_shouldReturnPractitionersByName() {
		StringAndListParam name = new StringAndListParam()
		        .addAnd(new StringOrListParam().add(new StringParam(PRACTITIONER_GIVEN_NAME)));
		SearchParameterMap theParams = new SearchParameterMap().addParameter(NAME_SEARCH_HANDLER, NAME_PROPERTY, name);
		
		IBundleProvider results = search(theParams);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList, not(empty()));
		assertThat(resultList, hasSize(equalTo(1)));
		assertThat(resultList.iterator().next().getIdElement().getIdPart(), equalTo(PRACTITIONER_UUID));
	}
	
	@Test
	public void searchForPractitioners_shouldReturnEmptyCollectionWhenNameNotMatched() {
		StringAndListParam name = new StringAndListParam()
		        .addAnd(new StringOrListParam().add(new StringParam(NOT_FOUND_PRACTITIONER_NAME)));
		SearchParameterMap theParams = new SearchParameterMap().addParameter(NAME_SEARCH_HANDLER, NAME_PROPERTY, name);
		
		IBundleProvider results = search(theParams);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList, is(empty()));
	}
	
	@Test
	public void searchForPractitioners_shouldReturnPractitionersByIdentifier() {
		TokenAndListParam identifier = new TokenAndListParam().addAnd(new TokenOrListParam().add(PRACTITIONER_IDENTIFIER));
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.IDENTIFIER_SEARCH_HANDLER,
		    identifier);
		
		IBundleProvider results = search(theParams);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList, not(empty()));
		assertThat(resultList, hasSize(greaterThanOrEqualTo(1)));
		assertThat(((Practitioner) resultList.iterator().next()).getIdentifierFirstRep().getValue(),
		    equalTo(PRACTITIONER_IDENTIFIER));
	}
	
	@Test
	public void searchForPractitioners_shouldReturnEmptyCollectionWhenIdentifierNotMatched() {
		TokenAndListParam identifier = new TokenAndListParam()
		        .addAnd(new TokenOrListParam().add(NOT_FOUND_PRACTITIONER_IDENTIFIER));
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.IDENTIFIER_SEARCH_HANDLER,
		    identifier);
		
		IBundleProvider results = search(theParams);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList, is(empty()));
	}
	
	@Test
	public void searchForPractitioners_shouldReturnPractitionersByGivenName() {
		StringAndListParam givenName = new StringAndListParam().addAnd(new StringParam(PRACTITIONER_GIVEN_NAME));
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.NAME_SEARCH_HANDLER,
		    FhirConstants.GIVEN_PROPERTY, givenName);
		
		IBundleProvider results = search(theParams);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList, not(empty()));
		assertThat(resultList, hasSize(greaterThanOrEqualTo(1)));
		assertThat(resultList, everyItem(hasProperty("name",
		    hasItem(hasProperty("given", hasItem(hasProperty("value", equalTo(PRACTITIONER_GIVEN_NAME))))))));
	}
	
	@Test
	public void searchForPractitioners_shouldReturnEmptyCollectionForWrongGivenName() {
		StringAndListParam givenName = new StringAndListParam().addAnd(new StringParam(WRONG_GIVEN_NAME));
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.NAME_SEARCH_HANDLER,
		    FhirConstants.GIVEN_PROPERTY, givenName);
		
		IBundleProvider results = search(theParams);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList, empty());
	}
	
	@Test
	public void searchForPractitioners_shouldReturnPractitionersByFamilyName() {
		StringAndListParam familyName = new StringAndListParam().addAnd(new StringParam(PRACTITIONER_FAMILY_NAME));
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.NAME_SEARCH_HANDLER,
		    FhirConstants.FAMILY_PROPERTY, familyName);
		
		IBundleProvider results = search(theParams);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList, not(empty()));
		assertThat(resultList, hasSize(greaterThanOrEqualTo(1)));
		assertThat(resultList,
		    everyItem(hasProperty("name", hasItem(hasProperty("family", equalTo(PRACTITIONER_FAMILY_NAME))))));
	}
	
	@Test
	public void searchForPractitioners_shouldReturnEmptyCollectionForWrongFamilyName() {
		StringAndListParam familyName = new StringAndListParam().addAnd(new StringParam(WRONG_FAMILY_NAME));
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.NAME_SEARCH_HANDLER,
		    FhirConstants.FAMILY_PROPERTY, familyName);
		
		IBundleProvider results = search(theParams);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList, empty());
	}
	
	@Test
	public void searchForPractitioners_shouldReturnPractitionersByMatchingGivenAndFamilyName() {
		StringAndListParam givenName = new StringAndListParam().addAnd(new StringParam(PRACTITIONER_GIVEN_NAME));
		StringAndListParam familyName = new StringAndListParam().addAnd(new StringParam(PRACTITIONER_FAMILY_NAME));
		
		SearchParameterMap theParams = new SearchParameterMap()
		        .addParameter(FhirConstants.NAME_SEARCH_HANDLER, FhirConstants.GIVEN_PROPERTY, givenName)
		        .addParameter(FhirConstants.NAME_SEARCH_HANDLER, FhirConstants.FAMILY_PROPERTY, familyName);
		
		IBundleProvider results = search(theParams);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList, not(empty()));
		assertThat(resultList, hasSize(greaterThanOrEqualTo(1)));
		assertThat(resultList, everyItem(hasProperty("name",
		    hasItem(hasProperty("given", hasItem(hasProperty("value", equalTo(PRACTITIONER_GIVEN_NAME))))))));
		assertThat(resultList,
		    everyItem(hasProperty("name", hasItem(hasProperty("family", equalTo(PRACTITIONER_FAMILY_NAME))))));
	}
	
	@Test
	public void searchForPractitioners_shouldReturnEmptyCollectionForMismatchingGivenAndFamilyName() {
		StringAndListParam givenName = new StringAndListParam().addAnd(new StringParam(WRONG_GIVEN_NAME));
		StringAndListParam familyName = new StringAndListParam().addAnd(new StringParam(PRACTITIONER_FAMILY_NAME));
		
		SearchParameterMap theParams = new SearchParameterMap()
		        .addParameter(FhirConstants.NAME_SEARCH_HANDLER, FhirConstants.GIVEN_PROPERTY, givenName)
		        .addParameter(FhirConstants.NAME_SEARCH_HANDLER, FhirConstants.FAMILY_PROPERTY, familyName);
		
		IBundleProvider results = search(theParams);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList, empty());
	}
	
	@Test
	public void searchForPractitioners_shouldReturnPractitionersByCity() {
		StringAndListParam city = new StringAndListParam().addAnd(new StringParam(CITY));
		
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.ADDRESS_SEARCH_HANDLER,
		    FhirConstants.CITY_PROPERTY, city);
		
		IBundleProvider results = search(theParams);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList, not(empty()));
		assertThat(resultList, hasSize(equalTo(1)));
		assertThat(((Practitioner) resultList.iterator().next()).getAddressFirstRep().getCity(), equalTo(CITY));
	}
	
	@Test
	public void searchForPractitioners_shouldReturnEmptyCollectionByWrongCity() {
		StringAndListParam city = new StringAndListParam().addAnd(new StringParam(WRONG_CITY));
		
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.ADDRESS_SEARCH_HANDLER,
		    FhirConstants.CITY_PROPERTY, city);
		
		IBundleProvider results = search(theParams);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList, empty());
	}
	
	@Test
	public void searchForPractitioners_shouldReturnPractitionersByState() {
		StringAndListParam state = new StringAndListParam().addAnd(new StringParam(STATE));
		
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.ADDRESS_SEARCH_HANDLER,
		    FhirConstants.STATE_PROPERTY, state);
		
		IBundleProvider results = search(theParams);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList, not(empty()));
		assertThat(resultList, hasSize(equalTo(1)));
		assertThat(((Practitioner) resultList.iterator().next()).getAddressFirstRep().getState(), equalTo(STATE));
	}
	
	@Test
	public void searchForPractitioners_shouldReturnEmptyCollectionByWrongState() {
		StringAndListParam state = new StringAndListParam().addAnd(new StringParam(WRONG_STATE));
		
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.ADDRESS_SEARCH_HANDLER,
		    FhirConstants.STATE_PROPERTY, state);
		
		IBundleProvider results = search(theParams);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList, empty());
	}
	
	@Test
	public void searchForPractitioners_shouldReturnPractitionersByPostalCode() {
		StringAndListParam postalCode = new StringAndListParam().addAnd(new StringParam(POSTAL_CODE));
		
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.ADDRESS_SEARCH_HANDLER,
		    FhirConstants.POSTAL_CODE_PROPERTY, postalCode);
		
		IBundleProvider results = search(theParams);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList, not(empty()));
		assertThat(resultList, hasSize(equalTo(1)));
		assertThat(((Practitioner) resultList.iterator().next()).getAddressFirstRep().getPostalCode(), equalTo(POSTAL_CODE));
	}
	
	@Test
	public void searchForPractitioners_shouldReturnEmptyCollectionByWrongPostalCode() {
		StringAndListParam postalCode = new StringAndListParam().addAnd(new StringParam(WRONG_POSTAL_CODE));
		
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.ADDRESS_SEARCH_HANDLER,
		    FhirConstants.POSTAL_CODE_PROPERTY, postalCode);
		
		IBundleProvider results = search(theParams);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList, empty());
	}
	
	@Test
	public void searchForPractitioners_shouldReturnPractitionersByCountry() {
		StringAndListParam country = new StringAndListParam().addAnd(new StringParam(COUNTRY));
		
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.ADDRESS_SEARCH_HANDLER,
		    FhirConstants.COUNTRY_PROPERTY, country);
		
		IBundleProvider results = search(theParams);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList, not(empty()));
		assertThat(resultList, hasSize(equalTo(1)));
		assertThat(((Practitioner) resultList.iterator().next()).getAddressFirstRep().getCountry(), equalTo(COUNTRY));
	}
	
	@Test
	public void searchForPractitioners_shouldReturnEmptyCollectionByWrongCountry() {
		StringAndListParam country = new StringAndListParam().addAnd(new StringParam(WRONG_COUNTRY));
		
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.ADDRESS_SEARCH_HANDLER,
		    FhirConstants.COUNTRY_PROPERTY, country);
		
		IBundleProvider results = search(theParams);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList, empty());
	}
	
	@Test
	public void searchForPractitioners_shouldReturnPractitionersByMatchingCityAndCountry() {
		StringAndListParam city = new StringAndListParam().addAnd(new StringParam(CITY));
		StringAndListParam country = new StringAndListParam().addAnd(new StringParam(COUNTRY));
		
		SearchParameterMap theParams = new SearchParameterMap()
		        .addParameter(FhirConstants.ADDRESS_SEARCH_HANDLER, FhirConstants.CITY_PROPERTY, city)
		        .addParameter(FhirConstants.ADDRESS_SEARCH_HANDLER, FhirConstants.COUNTRY_PROPERTY, country);
		
		IBundleProvider results = search(theParams);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList, not(empty()));
		assertThat(resultList, hasSize(equalTo(1)));
		assertThat(((Practitioner) resultList.iterator().next()).getAddressFirstRep().getCity(), equalTo(CITY));
		assertThat(((Practitioner) resultList.iterator().next()).getAddressFirstRep().getCountry(), equalTo(COUNTRY));
	}
	
	@Test
	public void searchForPractitioners_shouldReturnEmptyCollectionByMismatchingCityAndCountry() {
		StringAndListParam city = new StringAndListParam().addAnd(new StringParam(WRONG_CITY));
		StringAndListParam country = new StringAndListParam().addAnd(new StringParam(COUNTRY));
		
		SearchParameterMap theParams = new SearchParameterMap()
		        .addParameter(FhirConstants.ADDRESS_SEARCH_HANDLER, FhirConstants.CITY_PROPERTY, city)
		        .addParameter(FhirConstants.ADDRESS_SEARCH_HANDLER, FhirConstants.COUNTRY_PROPERTY, country);
		
		IBundleProvider results = search(theParams);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList, empty());
	}
	
	@Test
	public void searchForPractitioners_shouldHandleComplexQuery() {
		StringAndListParam name = new StringAndListParam()
		        .addAnd(new StringOrListParam().add(new StringParam(PRACTITIONER_GIVEN_NAME)));
		TokenAndListParam identifier = new TokenAndListParam().addAnd(new TokenOrListParam().add(PRACTITIONER_IDENTIFIER));
		
		SearchParameterMap theParams = new SearchParameterMap().addParameter(NAME_SEARCH_HANDLER, NAME_PROPERTY, name)
		        .addParameter(FhirConstants.IDENTIFIER_SEARCH_HANDLER, identifier);
		
		IBundleProvider results = search(theParams);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList, not(empty()));
		assertThat(resultList, hasSize(equalTo(1)));
		assertThat(((Practitioner) resultList.iterator().next()).getIdentifierFirstRep().getValue(),
		    equalTo(PRACTITIONER_IDENTIFIER));
		assertThat(resultList.iterator().next().getIdElement().getIdPart(), equalTo(PRACTITIONER_UUID));
	}
	
	@Test
	public void searchForPractitioners_shouldSearchForPractitionersByUuid() {
		TokenAndListParam uuid = new TokenAndListParam().addAnd(new TokenParam(PRACTITIONER_UUID));
		
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.COMMON_SEARCH_HANDLER,
		    FhirConstants.ID_PROPERTY, uuid);
		
		IBundleProvider results = search(theParams);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList, not(empty()));
		assertThat(resultList, hasSize(equalTo(1)));
		assertThat(((Practitioner) resultList.iterator().next()).getIdElement().getIdPart(), equalTo(PRACTITIONER_UUID));
	}
	
	@Test
	public void searchForPractitioners_shouldSearchForPractitionersByLastUpdatedDateCreated() {
		DateRangeParam lastUpdated = new DateRangeParam().setUpperBound(DATE_CREATED).setLowerBound(DATE_CREATED);
		
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.COMMON_SEARCH_HANDLER,
		    FhirConstants.LAST_UPDATED_PROPERTY, lastUpdated);
		
		IBundleProvider results = search(theParams);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList, not(empty()));
		assertThat(resultList.size(), equalTo(2));
	}
	
	@Test
	public void searchForPractitioners_shouldSearchForPractitionersByLastUpdatedDateChanged() {
		DateRangeParam lastUpdated = new DateRangeParam().setUpperBound(DATE_CHANGED).setLowerBound(DATE_CHANGED);
		
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.COMMON_SEARCH_HANDLER,
		    FhirConstants.LAST_UPDATED_PROPERTY, lastUpdated);
		
		IBundleProvider results = search(theParams);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList, not(empty()));
		assertThat(resultList, hasSize(equalTo(1)));
	}
	
	@Test
	public void searchForPractitioners_shouldSearchForPractitionersByMatchingUuidAndLastUpdated() {
		TokenAndListParam uuid = new TokenAndListParam().addAnd(new TokenParam(PRACTITIONER_UUID));
		DateRangeParam lastUpdated = new DateRangeParam().setUpperBound(DATE_CREATED).setLowerBound(DATE_CREATED);
		
		SearchParameterMap theParams = new SearchParameterMap()
		        .addParameter(FhirConstants.COMMON_SEARCH_HANDLER, FhirConstants.ID_PROPERTY, uuid)
		        .addParameter(FhirConstants.COMMON_SEARCH_HANDLER, FhirConstants.LAST_UPDATED_PROPERTY, lastUpdated);
		
		IBundleProvider results = search(theParams);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList, not(empty()));
		assertThat(resultList, hasSize(equalTo(1)));
		assertThat(((Practitioner) resultList.iterator().next()).getIdElement().getIdPart(), equalTo(PRACTITIONER_UUID));
	}
	
	@Test
	public void searchForPractitioners_shouldReturnEmptyListByMismatchingUuidAndLastUpdated() {
		TokenAndListParam uuid = new TokenAndListParam().addAnd(new TokenParam(PRACTITIONER_UUID));
		DateRangeParam lastUpdated = new DateRangeParam().setUpperBound(DATE_RETIRED).setLowerBound(DATE_RETIRED);
		
		SearchParameterMap theParams = new SearchParameterMap()
		        .addParameter(FhirConstants.COMMON_SEARCH_HANDLER, FhirConstants.ID_PROPERTY, uuid)
		        .addParameter(FhirConstants.COMMON_SEARCH_HANDLER, FhirConstants.LAST_UPDATED_PROPERTY, lastUpdated);
		
		IBundleProvider results = search(theParams);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList, empty());
	}
	
	@Test
	public void searchForPractitioners_shouldAddEncountersToRelatedResults() {
		TokenAndListParam uuid = new TokenAndListParam().addAnd(new TokenParam(PRACTITIONER_UUID));
		HashSet<Include> revIncludes = new HashSet<>();
		revIncludes.add(new Include("Encounter:participant"));
		
		SearchParameterMap theParams = new SearchParameterMap()
		        .addParameter(FhirConstants.COMMON_SEARCH_HANDLER, FhirConstants.ID_PROPERTY, uuid)
		        .addParameter(FhirConstants.REVERSE_INCLUDE_SEARCH_HANDLER, revIncludes);
		
		IBundleProvider results = search(theParams);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList, not(empty()));
		assertThat(resultList, hasSize(equalTo(5))); // included resources added as part of result list
		assertThat(resultList.subList(1, 5),
		    everyItem(allOf(is(instanceOf(Encounter.class)), hasProperty("participantFirstRep", hasProperty("individual",
		        hasProperty("referenceElement", hasProperty("idPart", equalTo(PRACTITIONER_UUID))))))));
	}
	
	@Test
	public void searchForPractitioners_shouldAddMedicationRequestsToRelatedResults() {
		TokenAndListParam uuid = new TokenAndListParam().addAnd(new TokenParam(PRACTITIONER_UUID));
		HashSet<Include> revIncludes = new HashSet<>();
		revIncludes.add(new Include("MedicationRequest:requester"));
		
		SearchParameterMap theParams = new SearchParameterMap()
		        .addParameter(FhirConstants.COMMON_SEARCH_HANDLER, FhirConstants.ID_PROPERTY, uuid)
		        .addParameter(FhirConstants.REVERSE_INCLUDE_SEARCH_HANDLER, revIncludes);
		
		IBundleProvider results = search(theParams);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList, not(empty()));
		assertThat(resultList, hasSize(equalTo(9))); // included resources added as part of result list
		assertThat(resultList.subList(1, resultList.size()), everyItem(allOf(is(instanceOf(MedicationRequest.class)),
		    hasProperty("requester", hasProperty("referenceElement", hasProperty("idPart", equalTo(PRACTITIONER_UUID)))))));
	}
	
	@Test
	public void searchForPractitioners_shouldAddServiceRequestsToRelatedResults() {
		TokenAndListParam uuid = new TokenAndListParam().addAnd(new TokenParam(PRACTITIONER_UUID));
		HashSet<Include> revIncludes = new HashSet<>();
		revIncludes.add(new Include("ServiceRequest:requester"));
		
		SearchParameterMap theParams = new SearchParameterMap()
		        .addParameter(FhirConstants.COMMON_SEARCH_HANDLER, FhirConstants.ID_PROPERTY, uuid)
		        .addParameter(FhirConstants.REVERSE_INCLUDE_SEARCH_HANDLER, revIncludes);
		
		IBundleProvider results = search(theParams);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList, not(empty()));
		assertThat(resultList, hasSize(equalTo(4))); // included resources added as part of result list
		assertThat(resultList.subList(1, 4), everyItem(allOf(is(instanceOf(ServiceRequest.class)),
		    hasProperty("requester", hasProperty("referenceElement", hasProperty("idPart", equalTo(PRACTITIONER_UUID)))))));
	}
	
	@Test
	public void searchForPractitioners_shouldAddProcedureRequestsToRelatedResults() {
		TokenAndListParam uuid = new TokenAndListParam().addAnd(new TokenParam(PRACTITIONER_UUID));
		HashSet<Include> revIncludes = new HashSet<>();
		revIncludes.add(new Include("ProcedureRequest:requester"));
		
		SearchParameterMap theParams = new SearchParameterMap()
		        .addParameter(FhirConstants.COMMON_SEARCH_HANDLER, FhirConstants.ID_PROPERTY, uuid)
		        .addParameter(FhirConstants.REVERSE_INCLUDE_SEARCH_HANDLER, revIncludes);
		
		IBundleProvider results = search(theParams);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList, not(empty()));
		assertThat(resultList, hasSize(equalTo(4))); // included resources added as part of result list
		assertThat(resultList.subList(1, 4), everyItem(allOf(is(instanceOf(ServiceRequest.class)),
		    hasProperty("requester", hasProperty("referenceElement", hasProperty("idPart", equalTo(PRACTITIONER_UUID)))))));
	}
	
	@Test
	public void searchForPractitioners_shouldHandleMultipleReverseIncludes() {
		TokenAndListParam uuid = new TokenAndListParam().addAnd(new TokenParam(PRACTITIONER_UUID));
		HashSet<Include> revIncludes = new HashSet<>();
		revIncludes.add(new Include("ServiceRequest:requester"));
		revIncludes.add(new Include("Encounter:participant"));
		
		SearchParameterMap theParams = new SearchParameterMap()
		        .addParameter(FhirConstants.COMMON_SEARCH_HANDLER, FhirConstants.ID_PROPERTY, uuid)
		        .addParameter(FhirConstants.REVERSE_INCLUDE_SEARCH_HANDLER, revIncludes);
		
		IBundleProvider results = search(theParams);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList, not(empty()));
		assertThat(resultList, hasSize(equalTo(8))); // included resources (4 encounters + 3 service requests) added as part of result list
		assertThat(resultList.subList(1, 8),
		    everyItem(anyOf(
		        allOf(is(instanceOf(Encounter.class)),
		            hasProperty("participantFirstRep",
		                hasProperty("individual",
		                    hasProperty("referenceElement", hasProperty("idPart", equalTo(PRACTITIONER_UUID)))))),
		        allOf(is(instanceOf(ServiceRequest.class)), hasProperty("requester",
		            hasProperty("referenceElement", hasProperty("idPart", equalTo(PRACTITIONER_UUID))))))));
	}
	
}
