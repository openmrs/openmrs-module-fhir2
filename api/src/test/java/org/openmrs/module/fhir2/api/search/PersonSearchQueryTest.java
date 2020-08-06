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

import static org.exparity.hamcrest.date.DateMatchers.sameDay;
import static org.exparity.hamcrest.date.DateMatchers.sameOrAfter;
import static org.exparity.hamcrest.date.DateMatchers.sameOrBefore;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hl7.fhir.r4.model.Person.SP_ADDRESS_CITY;
import static org.hl7.fhir.r4.model.Person.SP_ADDRESS_COUNTRY;
import static org.hl7.fhir.r4.model.Person.SP_ADDRESS_POSTALCODE;
import static org.hl7.fhir.r4.model.Person.SP_ADDRESS_STATE;
import static org.hl7.fhir.r4.model.Person.SP_BIRTHDATE;
import static org.hl7.fhir.r4.model.Person.SP_NAME;
import static org.openmrs.util.OpenmrsUtil.compareWithNullAsGreatest;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import ca.uhn.fhir.rest.api.SortOrderEnum;
import ca.uhn.fhir.rest.api.SortSpec;
import ca.uhn.fhir.rest.api.server.IBundleProvider;
import ca.uhn.fhir.rest.param.DateRangeParam;
import ca.uhn.fhir.rest.param.StringAndListParam;
import ca.uhn.fhir.rest.param.StringOrListParam;
import ca.uhn.fhir.rest.param.StringParam;
import ca.uhn.fhir.rest.param.TokenAndListParam;
import ca.uhn.fhir.rest.param.TokenOrListParam;
import ca.uhn.fhir.rest.param.TokenParam;
import org.hamcrest.comparator.ComparatorMatcherBuilder;
import org.hl7.fhir.r4.model.Enumerations;
import org.hl7.fhir.r4.model.HumanName;
import org.hl7.fhir.r4.model.Person;
import org.junit.Test;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.TestFhirSpringConfiguration;
import org.openmrs.module.fhir2.api.dao.FhirPersonDao;
import org.openmrs.module.fhir2.api.search.param.SearchParameterMap;
import org.openmrs.module.fhir2.api.translators.PersonTranslator;
import org.openmrs.test.BaseModuleContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

@ContextConfiguration(classes = TestFhirSpringConfiguration.class, inheritLocations = false)
public class PersonSearchQueryTest extends BaseModuleContextSensitiveTest {
	
	private static final String ADDRESS_SEARCH_FILE = "org/openmrs/module/fhir2/api/dao/impl/FhirPatientDaoImplTest_address_data.xml";
	
	private static final String MALE_GENDER = "male";
	
	private static final String WRONG_GENDER = "wrong-gender";
	
	private static final String PERSON_NAME = "Super";
	
	private static final String PERSON_PARTIAL_NAME = "Sup";
	
	private static final String NOT_FOUND_NAME = "not found name";
	
	private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
	
	private static final String BIRTH_DATE = "1975-04-08";
	
	private static final String NOT_FOUND_BIRTH_DATE = "0001-01-01";
	
	private static final String CITY = "Indianapolis";
	
	private static final String STATE = "IN";
	
	private static final String POSTAL_CODE = "46202";
	
	private static final String COUNTRY = "USA";
	
	private static final String PERSON_UUID = "da7f524f-27ce-4bb2-86d6-6d1d05312bd5";
	
	private static final String DATE_CREATED = "2006-01-18";
	
	private static final String MATCHING_DATE_CREATED = "2005-09-22";
	
	private static final int START_INDEX = 0;
	
	private static final int END_INDEX = 10;
	
	private static final ComparatorMatcherBuilder<HumanName> NAME_MATCHER = ComparatorMatcherBuilder.comparedBy((o1, o2) -> {
		int ret;
		ret = compareWithNullAsGreatest(o1.getFamily(), o2.getFamily()); // familyName
		
		if (ret == 0) { // familyName2
			ret = compareWithNullAsGreatest(o1.getExtension().get(2).getValue().toString(),
			    o2.getExtension().get(2).getValue().toString());
		}
		
		if (ret == 0) { // givenName + middleName
			ret = compareWithNullAsGreatest(o1.getGivenAsSingleString(), o2.getGivenAsSingleString());
		}
		
		if (ret == 0) { // familyNamePrefix
			ret = compareWithNullAsGreatest(o1.getExtension().get(1).getValue().toString(),
			    o2.getExtension().get(1).getValue().toString());
		}
		
		if (ret == 0) { // familyNameSuffix
			ret = compareWithNullAsGreatest(o1.getExtension().get(3).getValue().toString(),
			    o2.getExtension().get(3).getValue().toString());
		}
		
		if (ret == 0) {
			ret = o1.equalsDeep(o2) ? 0 : -1;
		}
		
		return ret;
	});
	
	@Autowired
	private FhirPersonDao dao;
	
	@Autowired
	private PersonTranslator translator;
	
	@Autowired
	private SearchQuery<org.openmrs.Person, Person, FhirPersonDao, PersonTranslator> searchQuery;
	
	private List<Person> get(IBundleProvider results) {
		return results.getResources(START_INDEX, END_INDEX).stream().filter(it -> it instanceof Person)
		        .map(it -> (Person) it).collect(Collectors.toList());
	}
	
	private IBundleProvider search(SearchParameterMap theParams) {
		return searchQuery.getQueryResults(theParams, dao, translator);
	}
	
	@Test
	public void shouldReturnCollectionOfPeopleForMatchOnPersonName() {
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.NAME_SEARCH_HANDLER,
		    FhirConstants.NAME_PROPERTY, new StringAndListParam().addAnd(new StringParam(PERSON_NAME)));
		
		IBundleProvider people = search(theParams);
		
		assertThat(people, notNullValue());
		assertThat(people.size(), greaterThanOrEqualTo(1));
		
		List<Person> resultList = get(people);
		
		assertThat(resultList, not(empty()));
		assertThat(resultList.get(0).getNameFirstRep().getNameAsSingleString(), containsString(PERSON_NAME));
	}
	
	@Test
	public void shouldReturnCollectionOfPeopleForPartialMatchOnPersonName() {
		StringAndListParam personName = new StringAndListParam()
		        .addAnd(new StringOrListParam().add(new StringParam(PERSON_PARTIAL_NAME)));
		
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.NAME_SEARCH_HANDLER,
		    FhirConstants.NAME_PROPERTY, personName);
		
		IBundleProvider people = search(theParams);
		
		assertThat(people, notNullValue());
		assertThat(people.size(), greaterThanOrEqualTo(1));
		
		List<Person> resultList = get(people);
		
		assertThat(resultList, not(empty()));
		assertThat(resultList.get(0).getNameFirstRep().getNameAsSingleString(), containsString(PERSON_PARTIAL_NAME));
	}
	
	@Test
	public void shouldReturnEmptyCollectionForNoMatchOnPersonName() {
		StringAndListParam personName = new StringAndListParam()
		        .addAnd(new StringOrListParam().add(new StringParam(NOT_FOUND_NAME)));
		
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.NAME_SEARCH_HANDLER,
		    FhirConstants.NAME_PROPERTY, personName);
		
		IBundleProvider people = search(theParams);
		
		assertThat(people, notNullValue());
		assertThat(people.size(), equalTo(0));
		
		List<Person> resultList = get(people);
		
		assertThat(resultList, is(empty()));
	}
	
	@Test
	public void shouldReturnCollectionOfPeopleForMatchingGender() {
		TokenAndListParam gender = new TokenAndListParam().addAnd(new TokenOrListParam().add(MALE_GENDER));
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.GENDER_SEARCH_HANDLER, gender);
		
		IBundleProvider people = search(theParams);
		
		assertThat(people, notNullValue());
		assertThat(people.size(), greaterThanOrEqualTo(1));
		
		List<Person> resultList = get(people);
		
		assertThat(resultList, not(empty()));
		assertThat(resultList.get(0).getGenderElement().getValue(), equalTo(Enumerations.AdministrativeGender.MALE));
	}
	
	@Test
	public void shouldReturnEmptyCollectionForNoMatchOnGender() {
		TokenAndListParam gender = new TokenAndListParam().addAnd(new TokenOrListParam().add(WRONG_GENDER));
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.GENDER_SEARCH_HANDLER, gender);
		
		IBundleProvider people = search(theParams);
		
		List<Person> resultList = get(people);
		
		assertThat(people, notNullValue());
		assertThat(resultList, is(empty()));
	}
	
	@Test
	public void shouldReturnCollectionOfPeopleForMatchOnBirthDate() throws ParseException {
		DateRangeParam dateRangeParam = new DateRangeParam().setLowerBound(BIRTH_DATE).setUpperBound(BIRTH_DATE);
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.DATE_RANGE_SEARCH_HANDLER,
		    dateRangeParam);
		
		IBundleProvider people = search(theParams);
		
		assertThat(people, notNullValue());
		assertThat(people.size(), greaterThanOrEqualTo(1));
		
		List<Person> resultList = get(people);
		
		assertThat(resultList, not(empty()));
		assertThat(resultList.get(0).getBirthDate(), sameDay(DATE_FORMAT.parse(BIRTH_DATE)));
	}
	
	@Test
	public void shouldReturnEmptyCollectionForNoMatchOnBirthDate() {
		DateRangeParam dateRangeParam = new DateRangeParam().setLowerBound(NOT_FOUND_BIRTH_DATE)
		        .setUpperBound(NOT_FOUND_BIRTH_DATE);
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.DATE_RANGE_SEARCH_HANDLER,
		    dateRangeParam);
		
		IBundleProvider people = search(theParams);
		
		assertThat(people, notNullValue());
		assertThat(people.size(), equalTo(0));
		
		List<Person> resultList = get(people);
		
		assertThat(resultList, empty());
	}
	
	@Test
	public void shouldReturnCollectionOfPeopleForMatchOnCity() {
		StringAndListParam city = new StringAndListParam().addAnd(new StringOrListParam().add(new StringParam(CITY)));
		
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.ADDRESS_SEARCH_HANDLER,
		    FhirConstants.CITY_PROPERTY, city);
		
		IBundleProvider people = search(theParams);
		
		assertThat(people, notNullValue());
		assertThat(people.size(), greaterThanOrEqualTo(1));
		
		List<Person> resultList = get(people);
		
		assertThat(resultList, not(empty()));
		assertThat(resultList.get(0).getAddressFirstRep().getCity(), equalTo(CITY));
	}
	
	@Test
	public void shouldReturnCollectionOfPeopleForMatchOnState() {
		StringAndListParam state = new StringAndListParam().addAnd(new StringOrListParam().add(new StringParam(STATE)));
		
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.ADDRESS_SEARCH_HANDLER,
		    FhirConstants.STATE_PROPERTY, state);
		
		IBundleProvider people = search(theParams);
		
		assertThat(people, notNullValue());
		assertThat(people.size(), greaterThanOrEqualTo(1));
		
		List<Person> resultList = get(people);
		
		assertThat(resultList, not(empty()));
		assertThat(resultList.get(0).getAddressFirstRep().getState(), equalTo(STATE));
	}
	
	@Test
	public void shouldReturnCollectionOfPeopleForMatchOnPostalCode() {
		StringAndListParam postalCode = new StringAndListParam()
		        .addAnd(new StringOrListParam().add(new StringParam(POSTAL_CODE)));
		
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.ADDRESS_SEARCH_HANDLER,
		    FhirConstants.POSTAL_CODE_PROPERTY, postalCode);
		
		IBundleProvider people = search(theParams);
		
		assertThat(people, notNullValue());
		assertThat(people.size(), greaterThanOrEqualTo(1));
		
		List<Person> resultList = get(people);
		
		assertThat(resultList, not(empty()));
		assertThat(resultList.get(0).getAddressFirstRep().getPostalCode(), equalTo(POSTAL_CODE));
	}
	
	@Test
	public void shouldReturnCollectionOfPeopleForMatchOnCountry() {
		StringAndListParam country = new StringAndListParam().addAnd(new StringOrListParam().add(new StringParam(COUNTRY)));
		
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.ADDRESS_SEARCH_HANDLER,
		    FhirConstants.COUNTRY_PROPERTY, country);
		
		IBundleProvider people = search(theParams);
		
		assertThat(people, notNullValue());
		assertThat(people.size(), greaterThanOrEqualTo(1));
		
		List<Person> resultList = get(people);
		
		assertThat(resultList, not(empty()));
		assertThat(resultList.get(0).getAddressFirstRep().getCountry(), equalTo(COUNTRY));
	}
	
	@Test
	public void shouldReturnCollectionOfPeopleForMatchOnUUID() {
		TokenAndListParam uuid = new TokenAndListParam().addAnd(new TokenParam(PERSON_UUID));
		
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.COMMON_SEARCH_HANDLER,
		    FhirConstants.ID_PROPERTY, uuid);
		
		IBundleProvider results = search(theParams);
		
		assertThat(results, notNullValue());
		assertThat(results.size(), greaterThanOrEqualTo(1));
		
		List<Person> resultList = get(results);
		
		assertThat(resultList, not(empty()));
		assertThat(resultList.get(0).getIdElement().getIdPart(), equalTo(PERSON_UUID));
	}
	
	@Test
	public void shouldReturnCollectionOfPeopleForMatchOnLastUpdatedDateCreated() {
		DateRangeParam lastUpdated = new DateRangeParam().setUpperBound(DATE_CREATED).setLowerBound(DATE_CREATED);
		
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.COMMON_SEARCH_HANDLER,
		    FhirConstants.LAST_UPDATED_PROPERTY, lastUpdated);
		
		IBundleProvider results = search(theParams);
		
		assertThat(results, notNullValue());
		assertThat(results.size(), equalTo(4));
		
		List<Person> resultList = get(results);
		
		assertThat(resultList, not(empty()));
		assertThat(resultList.size(), equalTo(4));
	}
	
	@Test
	public void shouldReturnCollectionOfPeopleForMatchOnUuidAndLastUpdated() {
		TokenAndListParam uuid = new TokenAndListParam().addAnd(new TokenParam(PERSON_UUID));
		DateRangeParam lastUpdated = new DateRangeParam().setUpperBound(MATCHING_DATE_CREATED)
		        .setLowerBound(MATCHING_DATE_CREATED);
		
		SearchParameterMap theParams = new SearchParameterMap()
		        .addParameter(FhirConstants.COMMON_SEARCH_HANDLER, FhirConstants.ID_PROPERTY, uuid)
		        .addParameter(FhirConstants.COMMON_SEARCH_HANDLER, FhirConstants.LAST_UPDATED_PROPERTY, lastUpdated);
		
		IBundleProvider results = search(theParams);
		
		assertThat(results, notNullValue());
		assertThat(results.size(), equalTo(1));
		
		List<Person> resultList = get(results);
		
		assertThat(resultList, not(empty()));
		assertThat(resultList, hasSize(equalTo(1)));
		assertThat(resultList.get(0).getIdElement().getIdPart(), equalTo(PERSON_UUID));
	}
	
	@Test
	public void shouldReturnEmptyCollectionForMismatchOnUuidAndLastUpdated() {
		TokenAndListParam uuid = new TokenAndListParam().addAnd(new TokenParam(PERSON_UUID));
		DateRangeParam lastUpdated = new DateRangeParam().setUpperBound(DATE_CREATED).setLowerBound(DATE_CREATED);
		
		SearchParameterMap theParams = new SearchParameterMap()
		        .addParameter(FhirConstants.COMMON_SEARCH_HANDLER, FhirConstants.ID_PROPERTY, uuid)
		        .addParameter(FhirConstants.COMMON_SEARCH_HANDLER, FhirConstants.LAST_UPDATED_PROPERTY, lastUpdated);
		
		IBundleProvider results = search(theParams);
		
		assertThat(results, notNullValue());
		assertThat(results.size(), equalTo(0));
		
		List<Person> resultList = get(results);
		
		assertThat(resultList, empty());
	}
	
	@Test
	public void shouldReturnCollectionOfPeopleSortedByGivenName() {
		SortSpec sort = new SortSpec();
		sort.setParamName(SP_NAME);
		sort.setOrder(SortOrderEnum.ASC);
		
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.NAME_SEARCH_HANDLER,
		    FhirConstants.NAME_PROPERTY, new StringAndListParam().addAnd(new StringParam(""))).setSortSpec(sort);
		
		IBundleProvider results = search(theParams);
		
		assertThat(results, notNullValue());
		assertThat(results.size(), greaterThan(1));
		
		List<Person> people = get(results);
		
		assertThat(people, hasSize(greaterThan(1)));
		
		for (int i = 1; i < people.size(); i++) {
			assertThat(people.get(i - 1).getNameFirstRep(), NAME_MATCHER.lessThanOrEqualTo(people.get(i).getNameFirstRep()));
		}
		
		sort.setOrder(SortOrderEnum.DESC);
		
		theParams.setSortSpec(sort);
		
		results = search(theParams);
		
		assertThat(results, notNullValue());
		assertThat(results.size(), greaterThan(1));
		
		people = get(results);
		
		assertThat(people, hasSize(greaterThan(1)));
		
		for (int i = 1; i < people.size(); i++) {
			assertThat(people.get(i - 1).getNameFirstRep(),
			    NAME_MATCHER.greaterThanOrEqualTo(people.get(i).getNameFirstRep()));
		}
	}
	
	@Test
	public void shouldReturnCollectionOfPeopleSortedByBirthDate() {
		SortSpec sort = new SortSpec();
		sort.setParamName(SP_BIRTHDATE);
		sort.setOrder(SortOrderEnum.ASC);
		
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.DATE_RANGE_SEARCH_HANDLER,
		    "birthdate", new DateRangeParam().setLowerBound("1900-01-01").setUpperBoundInclusive(new Date()))
		        .setSortSpec(sort);
		
		IBundleProvider results = search(theParams);
		
		assertThat(results, notNullValue());
		assertThat(results.size(), greaterThan(1));
		
		List<Person> people = get(results);
		
		assertThat(people, hasSize(greaterThan(1)));
		
		for (int i = 1; i < people.size(); i++) {
			assertThat(people.get(i - 1).getBirthDate(), sameOrBefore(people.get(i).getBirthDate()));
		}
		
		sort.setOrder(SortOrderEnum.DESC);
		
		theParams.setSortSpec(sort);
		
		results = search(theParams);
		
		assertThat(results, notNullValue());
		assertThat(results.size(), greaterThan(1));
		
		people = get(results);
		
		assertThat(people, hasSize(greaterThan(1)));
		
		for (int i = 1; i < people.size(); i++) {
			assertThat(people.get(i - 1).getBirthDate(), sameOrAfter(people.get(i).getBirthDate()));
		}
	}
	
	@Test
	public void shouldReturnCollectionOfPeopleSortedByCity() throws Exception {
		executeDataSet(ADDRESS_SEARCH_FILE);
		
		SortSpec sort = new SortSpec();
		sort.setParamName(SP_ADDRESS_CITY);
		sort.setOrder(SortOrderEnum.ASC);
		
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.ADDRESS_SEARCH_HANDLER,
		    FhirConstants.CITY_PROPERTY, new StringAndListParam().addAnd(new StringParam("City"))).setSortSpec(sort);
		
		IBundleProvider results = search(theParams);
		
		assertThat(results, notNullValue());
		assertThat(results.size(), greaterThan(1));
		
		List<Person> people = get(results);
		
		assertThat(people, hasSize(greaterThan(1)));
		
		for (int i = 1; i < people.size(); i++) {
			assertThat(people.get(i - 1).getAddressFirstRep().getCity(),
			    lessThanOrEqualTo(people.get(i).getAddressFirstRep().getCity()));
		}
		
		sort.setOrder(SortOrderEnum.DESC);
		
		theParams.setSortSpec(sort);
		
		results = search(theParams);
		
		assertThat(results, notNullValue());
		assertThat(results.size(), greaterThan(1));
		
		people = get(results);
		
		assertThat(people, hasSize(greaterThan(1)));
		
		for (int i = 1; i < people.size(); i++) {
			assertThat(people.get(i - 1).getAddressFirstRep().getCity(),
			    greaterThanOrEqualTo(people.get(i).getAddressFirstRep().getCity()));
		}
	}
	
	@Test
	public void shouldReturnCollectionOfPeopleSortedByState() throws Exception {
		executeDataSet(ADDRESS_SEARCH_FILE);
		
		SortSpec sort = new SortSpec();
		sort.setParamName(SP_ADDRESS_STATE);
		sort.setOrder(SortOrderEnum.ASC);
		
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.ADDRESS_SEARCH_HANDLER,
		    FhirConstants.STATE_PROPERTY, new StringAndListParam().addAnd(new StringParam("M"))).setSortSpec(sort);
		
		IBundleProvider results = search(theParams);
		
		assertThat(results, notNullValue());
		assertThat(results.size(), greaterThan(1));
		
		List<Person> people = get(results);
		
		assertThat(people, hasSize(greaterThan(1)));
		
		for (int i = 1; i < people.size(); i++) {
			assertThat(people.get(i - 1).getAddressFirstRep().getState(),
			    lessThanOrEqualTo(people.get(i).getAddressFirstRep().getState()));
		}
		
		sort.setOrder(SortOrderEnum.DESC);
		
		theParams.setSortSpec(sort);
		
		results = search(theParams);
		
		assertThat(results, notNullValue());
		assertThat(results.size(), greaterThan(1));
		
		people = get(results);
		
		assertThat(people, hasSize(greaterThan(1)));
		
		for (int i = 1; i < people.size(); i++) {
			assertThat(people.get(i - 1).getAddressFirstRep().getState(),
			    greaterThanOrEqualTo(people.get(i).getAddressFirstRep().getState()));
		}
	}
	
	@Test
	public void shouldReturnCollectionOfPeopleSortedByPostalCode() throws Exception {
		executeDataSet(ADDRESS_SEARCH_FILE);
		
		SortSpec sort = new SortSpec();
		sort.setParamName(SP_ADDRESS_POSTALCODE);
		sort.setOrder(SortOrderEnum.ASC);
		
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.ADDRESS_SEARCH_HANDLER,
		    FhirConstants.POSTAL_CODE_PROPERTY, new StringAndListParam().addAnd(new StringParam("0"))).setSortSpec(sort);
		
		IBundleProvider results = search(theParams);
		
		assertThat(results, notNullValue());
		assertThat(results.size(), greaterThan(1));
		
		List<Person> people = get(results);
		
		assertThat(people, hasSize(greaterThan(1)));
		
		for (int i = 1; i < people.size(); i++) {
			assertThat(people.get(i - 1).getAddressFirstRep().getPostalCode(),
			    lessThanOrEqualTo(people.get(i).getAddressFirstRep().getPostalCode()));
		}
		
		sort.setOrder(SortOrderEnum.DESC);
		
		theParams.setSortSpec(sort);
		
		results = search(theParams);
		
		assertThat(results, notNullValue());
		assertThat(results.size(), greaterThan(1));
		
		people = get(results);
		
		assertThat(people, hasSize(greaterThan(1)));
		
		for (int i = 1; i < people.size(); i++) {
			assertThat(people.get(i - 1).getAddressFirstRep().getPostalCode(),
			    greaterThanOrEqualTo(people.get(i).getAddressFirstRep().getPostalCode()));
		}
	}
	
	@Test
	public void shouldReturnCollectionOfPeopleSortedByCountry() throws Exception {
		executeDataSet(ADDRESS_SEARCH_FILE);
		
		SortSpec sort = new SortSpec();
		sort.setParamName(SP_ADDRESS_COUNTRY);
		sort.setOrder(SortOrderEnum.ASC);
		
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.ADDRESS_SEARCH_HANDLER,
		    FhirConstants.COUNTRY_PROPERTY, new StringAndListParam().addAnd(new StringParam("F"))).setSortSpec(sort);
		
		IBundleProvider results = search(theParams);
		
		assertThat(results, notNullValue());
		assertThat(results.size(), greaterThan(1));
		
		List<Person> people = get(results);
		
		assertThat(people, hasSize(greaterThan(1)));
		
		for (int i = 1; i < people.size(); i++) {
			assertThat(people.get(i - 1).getAddressFirstRep().getCountry(),
			    lessThanOrEqualTo(people.get(i).getAddressFirstRep().getCountry()));
		}
		
		sort.setOrder(SortOrderEnum.DESC);
		
		theParams.setSortSpec(sort);
		
		results = search(theParams);
		
		assertThat(results, notNullValue());
		assertThat(results.size(), greaterThan(1));
		
		people = get(results);
		
		assertThat(people, hasSize(greaterThan(1)));
		
		for (int i = 1; i < people.size(); i++) {
			assertThat(people.get(i - 1).getAddressFirstRep().getCountry(),
			    greaterThanOrEqualTo(people.get(i).getAddressFirstRep().getCountry()));
		}
	}
	
	@Test
	public void shouldHandleComplexQuery() throws ParseException {
		TokenAndListParam genderParam = new TokenAndListParam().addAnd(new TokenOrListParam().add(MALE_GENDER));
		DateRangeParam birthDateParam = new DateRangeParam().setLowerBound(BIRTH_DATE).setUpperBound(BIRTH_DATE);
		StringAndListParam cityParam = new StringAndListParam().addAnd(new StringOrListParam().add(new StringParam(CITY)));
		StringAndListParam stateParam = new StringAndListParam().addAnd(new StringOrListParam().add(new StringParam(STATE)));
		StringAndListParam postalCodeParam = new StringAndListParam()
		        .addAnd(new StringOrListParam().add(new StringParam(POSTAL_CODE)));
		StringAndListParam countryParam = new StringAndListParam()
		        .addAnd(new StringOrListParam().add(new StringParam(COUNTRY)));
		
		SearchParameterMap theParams = new SearchParameterMap()
		        .addParameter(FhirConstants.GENDER_SEARCH_HANDLER, genderParam)
		        .addParameter(FhirConstants.DATE_RANGE_SEARCH_HANDLER, birthDateParam)
		        .addParameter(FhirConstants.ADDRESS_SEARCH_HANDLER, FhirConstants.CITY_PROPERTY, cityParam)
		        .addParameter(FhirConstants.ADDRESS_SEARCH_HANDLER, FhirConstants.STATE_PROPERTY, stateParam)
		        .addParameter(FhirConstants.ADDRESS_SEARCH_HANDLER, FhirConstants.POSTAL_CODE_PROPERTY, postalCodeParam)
		        .addParameter(FhirConstants.ADDRESS_SEARCH_HANDLER, FhirConstants.COUNTRY_PROPERTY, countryParam);
		
		IBundleProvider people = search(theParams);
		
		assertThat(people, notNullValue());
		assertThat(people.size(), greaterThanOrEqualTo(1));
		
		List<Person> resultList = get(people);
		assertThat(resultList, not(empty()));
		
		Person person = resultList.get(0);
		
		assertThat(person.getGenderElement().getValue(), equalTo(Enumerations.AdministrativeGender.MALE));
		assertThat(person.getBirthDate(), sameDay(DATE_FORMAT.parse(BIRTH_DATE)));
		assertThat(person.getAddressFirstRep().getCity(), equalTo(CITY));
		assertThat(person.getAddressFirstRep().getState(), equalTo(STATE));
		assertThat(person.getAddressFirstRep().getPostalCode(), equalTo(POSTAL_CODE));
		assertThat(person.getAddressFirstRep().getCountry(), equalTo(COUNTRY));
		assertThat(person.getId(), equalTo(PERSON_UUID));
	}
}
