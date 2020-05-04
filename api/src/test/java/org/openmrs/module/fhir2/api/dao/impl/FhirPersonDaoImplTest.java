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

import static org.exparity.hamcrest.date.DateMatchers.sameOrAfter;
import static org.exparity.hamcrest.date.DateMatchers.sameOrBefore;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.hl7.fhir.r4.model.Person.SP_ADDRESS_CITY;
import static org.hl7.fhir.r4.model.Person.SP_ADDRESS_COUNTRY;
import static org.hl7.fhir.r4.model.Person.SP_ADDRESS_POSTALCODE;
import static org.hl7.fhir.r4.model.Person.SP_ADDRESS_STATE;
import static org.hl7.fhir.r4.model.Person.SP_BIRTHDATE;
import static org.hl7.fhir.r4.model.Person.SP_NAME;
import static org.openmrs.util.OpenmrsUtil.compareWithNullAsGreatest;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import ca.uhn.fhir.rest.api.SortOrderEnum;
import ca.uhn.fhir.rest.api.SortSpec;
import ca.uhn.fhir.rest.param.DateRangeParam;
import ca.uhn.fhir.rest.param.StringOrListParam;
import ca.uhn.fhir.rest.param.StringParam;
import ca.uhn.fhir.rest.param.TokenOrListParam;
import org.exparity.hamcrest.date.DateMatchers;
import org.hamcrest.comparator.ComparatorMatcherBuilder;
import org.hibernate.SessionFactory;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.Person;
import org.openmrs.PersonAttribute;
import org.openmrs.PersonName;
import org.openmrs.module.fhir2.TestFhirSpringConfiguration;
import org.openmrs.test.BaseModuleContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;

@ContextConfiguration(classes = TestFhirSpringConfiguration.class, inheritLocations = false)
public class FhirPersonDaoImplTest extends BaseModuleContextSensitiveTest {
	
	private static final String PERSON_UUID = "61b38324-e2fd-4feb-95b7-9e9a2a4400df";
	
	private static final String WRONG_PERSON_UUID = "wrong_person_uuid";
	
	private static final String PERSON_INITIAL_DATA_XML = "org/openmrs/module/fhir2/api/dao/impl/FhirPersonDaoImplTest_initial_data.xml";
	
	private static final String MALE_GENDER = "male";
	
	private static final String FEMALE_GENDER = "female";
	
	private static final String OTHER_GENDER = "other";
	
	private static final String UNKNOWN_GENDER = "unknown";
	
	private static final String NULL_GENDER = null;
	
	private static final String WRONG_GENDER = "wrong-gender";
	
	private static final String GIVEN_NAME = "John";
	
	private static final String PERSON_NAME = "John";
	
	private static final String PERSON_PARTIAL_NAME = "Joh";
	
	private static final String NOT_FOUND_NAME = "not found name";
	
	private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
	
	private static final String BIRTH_DATE = "1999-12-20";
	
	private static final String NOT_FOUND_BIRTH_DATE = "0001-01-01";
	
	private static final String PERSON_ATTRIBUTE_TYPE_UUID = "14d4f066-15f5-102d-96e4-000c29c2a5d7";
	
	private static final String CITY = "Indianapolis";
	
	private static final String STATE = "IN";
	
	private static final String POSTAL_CODE = "46202";
	
	private static final String COUNTRY = "USA";
	
	private static final String PERSON_ADDRESS_PERSON_UUID = "61b38324-e2fd-4feb-95b7-9e9a2a4400df";
	
	private static final ComparatorMatcherBuilder<PersonName> NAME_MATCHER = ComparatorMatcherBuilder
	        .comparedBy((o1, o2) -> {
		        int ret;
		        ret = compareWithNullAsGreatest(o1.getFamilyName(), o2.getFamilyName());
		        
		        if (ret == 0) {
			        ret = compareWithNullAsGreatest(o1.getFamilyName2(), o2.getFamilyName2());
		        }
		        
		        if (ret == 0) {
			        ret = compareWithNullAsGreatest(o1.getGivenName(), o2.getGivenName());
		        }
		        
		        if (ret == 0) {
			        ret = compareWithNullAsGreatest(o1.getMiddleName(), o2.getMiddleName());
		        }
		        
		        if (ret == 0) {
			        ret = compareWithNullAsGreatest(o1.getFamilyNamePrefix(), o2.getFamilyNamePrefix());
		        }
		        
		        if (ret == 0) {
			        ret = compareWithNullAsGreatest(o1.getFamilyNameSuffix(), o2.getFamilyNameSuffix());
		        }
		        
		        if (ret == 0) {
			        ret = o1.equalsContent(o2) ? 0 : -1;
		        }
		        
		        return ret;
	        });
	
	private FhirPersonDaoImpl fhirPersonDao;
	
	@Autowired
	@Qualifier("sessionFactory")
	private SessionFactory sessionFactory;
	
	@Before
	public void setup() throws Exception {
		fhirPersonDao = new FhirPersonDaoImpl();
		fhirPersonDao.setSessionFactory(sessionFactory);
		executeDataSet(PERSON_INITIAL_DATA_XML);
	}
	
	@Test
	public void getPersonByUuid_shouldReturnMatchingPerson() {
		Person person = fhirPersonDao.getPersonByUuid(PERSON_UUID);
		assertThat(person, notNullValue());
		assertThat(person.getUuid(), equalTo(PERSON_UUID));
		assertThat(person.getGender(), equalTo("M"));
		assertThat(person.getGivenName(), equalTo(GIVEN_NAME));
	}
	
	@Test
	public void getPersonByWithWrongUuid_shouldReturnNullPerson() {
		Person person = fhirPersonDao.getPersonByUuid(WRONG_PERSON_UUID);
		assertThat(person, nullValue());
	}
	
	@Test
	public void shouldReturnCollectionOfPeopleForMatchOnPersonName() {
		StringOrListParam stringOrListParam = new StringOrListParam().add(new StringParam(PERSON_NAME));
		Collection<Person> people = fhirPersonDao.searchForPeople(stringOrListParam, null, null, null, null, null, null,
		    null);
		assertThat(people, notNullValue());
		assertThat(people, not(empty()));
		assertThat(people.size(), greaterThanOrEqualTo(1));
	}
	
	@Test
	public void shouldReturnCollectionOfPeopleForPartialMatchOnPersonName() {
		StringOrListParam stringOrListParam = new StringOrListParam().add(new StringParam(PERSON_PARTIAL_NAME));
		Collection<Person> people = fhirPersonDao.searchForPeople(stringOrListParam, null, null, null, null, null, null,
		    null);
		assertThat(people, notNullValue());
		assertThat(people, not(empty()));
		assertThat(people.size(), greaterThanOrEqualTo(1));
	}
	
	@Test
	public void shouldReturnEmptyCollectionForNoMatchOnPersonName() {
		StringOrListParam stringOrListParam = new StringOrListParam().add(new StringParam(NOT_FOUND_NAME));
		Collection<Person> people = fhirPersonDao.searchForPeople(stringOrListParam, null, null, null, null, null, null,
		    null);
		assertThat(people, is(empty()));
	}
	
	@Test
	public void shouldReturnCollectionOfPeopleForMatchingGender() {
		final String GENDER_PROPERTY = "gender";
		Collection<Person> people = fhirPersonDao.searchForPeople(null, new TokenOrListParam().add(MALE_GENDER), null, null,
		    null, null, null, null);
		
		assertThat(people, notNullValue());
		assertThat(people, not(empty()));
		assertThat(people, everyItem(hasProperty(GENDER_PROPERTY, equalTo("M"))));
		
		people = fhirPersonDao.searchForPeople(null, new TokenOrListParam().add(FEMALE_GENDER), null, null, null, null, null,
		    null);
		
		assertThat(people, notNullValue());
		assertThat(people, not(empty()));
		assertThat(people, everyItem(hasProperty(GENDER_PROPERTY, equalTo("F"))));
		
		people = fhirPersonDao.searchForPeople(null, new TokenOrListParam().add(OTHER_GENDER), null, null, null, null, null,
		    null);
		
		assertThat(people, notNullValue());
		assertThat(people, not(empty()));
		assertThat(people, everyItem(hasProperty(GENDER_PROPERTY, nullValue())));
		
		people = fhirPersonDao.searchForPeople(null, new TokenOrListParam().add(NULL_GENDER), null, null, null, null, null,
		    null);
		
		assertThat(people, notNullValue());
		assertThat(people, not(empty()));
		assertThat(people, everyItem(hasProperty(GENDER_PROPERTY, nullValue())));
		
		people = fhirPersonDao.searchForPeople(null, new TokenOrListParam().add(UNKNOWN_GENDER), null, null, null, null,
		    null, null);
		
		assertThat(people, notNullValue());
		assertThat(people, not(empty()));
		assertThat(people, everyItem(hasProperty(GENDER_PROPERTY, nullValue())));
	}
	
	@Test
	public void shouldReturnEmptyCollectionForNoMatchOnGender() {
		TokenOrListParam tokenOrListParam = new TokenOrListParam().add(WRONG_GENDER);
		Collection<Person> people = fhirPersonDao.searchForPeople(null, tokenOrListParam, null, null, null, null, null,
		    null);
		
		assertThat(people, notNullValue());
		assertThat(people, is(empty()));
	}
	
	@Test
	public void shouldReturnCollectionOfPeopleForMatchOnBirthDate() throws ParseException {
		DateRangeParam dateRangeParam = new DateRangeParam().setLowerBound(BIRTH_DATE).setUpperBound(BIRTH_DATE);
		Collection<Person> people = fhirPersonDao.searchForPeople(null, null, dateRangeParam, null, null, null, null, null);
		
		assertThat(people, notNullValue());
		assertThat(people.size(), greaterThanOrEqualTo(1));
		assertThat(people.stream().findAny().isPresent(), is(true));
		assertThat(people.stream().findAny().get().getBirthdate(), DateMatchers.sameDay(DATE_FORMAT.parse(BIRTH_DATE)));
	}
	
	@Test
	public void shouldReturnEmptyCollectionForNoMatchOnBirthDate() {
		DateRangeParam dateRangeParam = new DateRangeParam().setLowerBound(NOT_FOUND_BIRTH_DATE)
		        .setUpperBound(NOT_FOUND_BIRTH_DATE);
		Collection<Person> people = fhirPersonDao.searchForPeople(null, null, dateRangeParam, null, null, null, null, null);
		assertThat(people, notNullValue());
		assertThat(people, empty());
	}
	
	@Test
	public void shouldReturnCollectionOfPeopleForMatchOnCity() {
		StringOrListParam stringOrListParam = new StringOrListParam().add(new StringParam(CITY));
		Collection<Person> people = fhirPersonDao.searchForPeople(null, null, null, stringOrListParam, null, null, null,
		    null);
		assertThat(people, notNullValue());
		assertThat(people, not(empty()));
		assertThat(people.size(), greaterThanOrEqualTo(1));
		assertThat(people.stream().findAny().isPresent(), is(true));
		assertThat(people.stream().findAny().get().getUuid(), equalTo(PERSON_ADDRESS_PERSON_UUID));
	}
	
	@Test
	public void shouldReturnCollectionOfPeopleForMatchOnState() {
		StringOrListParam stringOrListParam = new StringOrListParam().add(new StringParam(STATE));
		Collection<Person> people = fhirPersonDao.searchForPeople(null, null, null, null, stringOrListParam, null, null,
		    null);
		assertThat(people, notNullValue());
		assertThat(people, not(empty()));
		assertThat(people.size(), greaterThanOrEqualTo(1));
		assertThat(people.stream().findAny().isPresent(), is(true));
		assertThat(people.stream().findAny().get().getUuid(), equalTo(PERSON_ADDRESS_PERSON_UUID));
	}
	
	@Test
	public void shouldReturnCollectionOfPeopleForMatchOnPostalCode() {
		StringOrListParam stringOrListParam = new StringOrListParam().add(new StringParam(POSTAL_CODE));
		Collection<Person> people = fhirPersonDao.searchForPeople(null, null, null, null, null, stringOrListParam, null,
		    null);
		assertThat(people, notNullValue());
		assertThat(people, not(empty()));
		assertThat(people.size(), greaterThanOrEqualTo(1));
		assertThat(people.stream().findAny().isPresent(), is(true));
		assertThat(people.stream().findAny().get().getUuid(), equalTo(PERSON_ADDRESS_PERSON_UUID));
	}
	
	@Test
	public void shouldReturnCollectionOfPeopleForMatchOnCountry() {
		StringOrListParam stringOrListParam = new StringOrListParam().add(new StringParam(COUNTRY));
		Collection<Person> people = fhirPersonDao.searchForPeople(null, null, null, null, null, null, stringOrListParam,
		    null);
		assertThat(people, notNullValue());
		assertThat(people, not(empty()));
		assertThat(people.size(), greaterThanOrEqualTo(1));
		assertThat(people.stream().findAny().isPresent(), is(true));
		assertThat(people.stream().findAny().get().getUuid(), equalTo(PERSON_ADDRESS_PERSON_UUID));
	}
	
	@Test
	public void shouldReturnCollectionOfPeopleSortedByName() {
		SortSpec sort = new SortSpec();
		sort.setParamName("name");
		sort.setOrder(SortOrderEnum.ASC);
		
		List<Person> people = getPersonListForSorting(sort);
		
		for (int i = 1; i < people.size(); i++) {
			assertThat(people.get(i - 1).getPersonName(), NAME_MATCHER.lessThanOrEqualTo(people.get(i).getPersonName()));
		}
		
		sort.setOrder(SortOrderEnum.DESC);
		
		people = getPersonListForSorting(sort);
		
		for (int i = 1; i < people.size(); i++) {
			assertThat(people.get(i - 1).getPersonName(), NAME_MATCHER.greaterThanOrEqualTo(people.get(i).getPersonName()));
		}
	}
	
	@Test
	public void shouldReturnCollectionOfPeopleSortedByBirthDate() {
		SortSpec sort = new SortSpec();
		sort.setParamName("birthdate");
		sort.setOrder(SortOrderEnum.ASC);
		List<Person> people = getPersonListForSorting(sort);
		
		for (int i = 1; i < people.size(); i++) {
			assertThat(people.get(i - 1).getBirthdate(), sameOrBefore(people.get(i).getBirthdate()));
		}
		
		sort.setOrder(SortOrderEnum.DESC);
		people = getPersonListForSorting(sort);
		
		for (int i = 1; i < people.size(); i++) {
			assertThat(people.get(i - 1).getBirthdate(), sameOrAfter(people.get(i).getBirthdate()));
		}
	}
	
	@Test
	public void shouldReturnCollectionOfPeopleSortedByCity() {
		SortSpec sort = new SortSpec();
		sort.setParamName("address-city");
		sort.setOrder(SortOrderEnum.ASC);
		
		List<Person> people = getPersonListForSorting(sort);
		
		for (int i = 1; i < people.size(); i++) {
			assertThat(people.get(i - 1).getPersonAddress().getCityVillage(),
			    lessThanOrEqualTo(people.get(i).getPersonAddress().getCityVillage()));
		}
		
		sort.setOrder(SortOrderEnum.DESC);
		
		people = getPersonListForSorting(sort);
		
		for (int i = 1; i < people.size(); i++) {
			assertThat(people.get(i - 1).getPersonAddress().getCityVillage(),
			    greaterThanOrEqualTo(people.get(i).getPersonAddress().getCityVillage()));
		}
	}
	
	@Test
	public void shouldReturnCollectionOfPeopleSortedByState() {
		SortSpec sort = new SortSpec();
		sort.setParamName("address-state");
		sort.setOrder(SortOrderEnum.ASC);
		
		List<Person> people = getPersonListForSorting(sort);
		
		for (int i = 1; i < people.size(); i++) {
			assertThat(people.get(i - 1).getPersonAddress().getStateProvince(),
			    lessThanOrEqualTo(people.get(i).getPersonAddress().getStateProvince()));
		}
		
		sort.setOrder(SortOrderEnum.DESC);
		
		people = getPersonListForSorting(sort);
		
		for (int i = 1; i < people.size(); i++) {
			assertThat(people.get(i - 1).getPersonAddress().getStateProvince(),
			    greaterThanOrEqualTo(people.get(i).getPersonAddress().getStateProvince()));
		}
	}
	
	@Test
	public void shouldReturnCollectionOfPeopleSortedByPostalCode() {
		SortSpec sort = new SortSpec();
		sort.setParamName("address-postalcode");
		sort.setOrder(SortOrderEnum.ASC);
		
		List<Person> people = getPersonListForSorting(sort);
		
		for (int i = 1; i < people.size(); i++) {
			assertThat(people.get(i - 1).getPersonAddress().getPostalCode(),
			    lessThanOrEqualTo(people.get(i).getPersonAddress().getPostalCode()));
		}
		
		sort.setOrder(SortOrderEnum.DESC);
		
		people = getPersonListForSorting(sort);
		
		for (int i = 1; i < people.size(); i++) {
			assertThat(people.get(i - 1).getPersonAddress().getPostalCode(),
			    greaterThanOrEqualTo(people.get(i).getPersonAddress().getPostalCode()));
		}
	}
	
	@Test
	public void shouldReturnCollectionOfPeopleSortedByCountry() {
		SortSpec sort = new SortSpec();
		sort.setParamName("address-country");
		sort.setOrder(SortOrderEnum.ASC);
		
		List<Person> people = getPersonListForSorting(sort);
		
		for (int i = 1; i < people.size(); i++) {
			assertThat(people.get(i - 1).getPersonAddress().getCountry(),
			    lessThanOrEqualTo(people.get(i).getPersonAddress().getCountry()));
		}
		
		sort.setOrder(SortOrderEnum.DESC);
		
		people = getPersonListForSorting(sort);
		
		for (int i = 1; i < people.size(); i++) {
			assertThat(people.get(i - 1).getPersonAddress().getCountry(),
			    greaterThanOrEqualTo(people.get(i).getPersonAddress().getCountry()));
		}
	}
	
	@Test
	public void shouldHandleComplexQuery() throws ParseException {
		StringOrListParam nameParam = new StringOrListParam().add(new StringParam(PERSON_NAME));
		TokenOrListParam genderParam = new TokenOrListParam().add(MALE_GENDER);
		DateRangeParam birthDateParam = new DateRangeParam().setLowerBound(BIRTH_DATE).setUpperBound(BIRTH_DATE);
		StringOrListParam cityParam = new StringOrListParam().add(new StringParam(CITY));
		StringOrListParam stateParam = new StringOrListParam().add(new StringParam(STATE));
		StringOrListParam postalCodeParam = new StringOrListParam().add(new StringParam(POSTAL_CODE));
		StringOrListParam countryParam = new StringOrListParam().add(new StringParam(COUNTRY));
		
		Collection<Person> people = fhirPersonDao.searchForPeople(nameParam, genderParam, birthDateParam, cityParam,
		    stateParam, postalCodeParam, countryParam, null);
		
		assertThat(people, notNullValue());
		assertThat(people, not(empty()));
		assertThat(people.size(), greaterThanOrEqualTo(1));
		assertThat(people.iterator().next().getGivenName(), equalTo(PERSON_NAME));
		assertThat(people.iterator().next().getGender(), equalTo("M"));
		assertThat(people.iterator().next().getBirthdate(), DateMatchers.sameDay(DATE_FORMAT.parse(BIRTH_DATE)));
		assertThat(people.iterator().next().getUuid(), equalTo(PERSON_ADDRESS_PERSON_UUID));
	}
	
	@Test
	public void getActiveAttributesByPersonAndAttributeTypeUuid_shouldReturnPersonAttribute() {
		Person person = new Person();
		person.setUuid(PERSON_UUID);
		
		List<PersonAttribute> attributeList = fhirPersonDao.getActiveAttributesByPersonAndAttributeTypeUuid(person,
		    PERSON_ATTRIBUTE_TYPE_UUID);
		
		assertThat(attributeList, notNullValue());
	}
	
	private List<Person> getPersonListForSorting(SortSpec sort) {
		Collection<Person> people = fhirPersonDao.searchForPeople(null, null, null, null, null, null, null, sort);
		
		assertThat(people, notNullValue());
		assertThat(people, not(empty()));
		assertThat(people.size(), greaterThan(1));
		
		List<Person> peopleList = new ArrayList<>(people);
		// Remove people with sort parameter value null, to allow comparison while asserting. 
		switch (sort.getParamName()) {
			case SP_NAME:
				peopleList.removeIf(p -> p.getPersonName() == null);
				break;
			case SP_BIRTHDATE:
				peopleList.removeIf(p -> p.getBirthdate() == null);
				break;
			case SP_ADDRESS_CITY:
				peopleList.removeIf(p -> p.getPersonAddress() == null || p.getPersonAddress().getCityVillage() == null);
				break;
			case SP_ADDRESS_STATE:
				peopleList.removeIf(p -> p.getPersonAddress() == null || p.getPersonAddress().getStateProvince() == null);
				break;
			case SP_ADDRESS_POSTALCODE:
				peopleList.removeIf(p -> p.getPersonAddress() == null || p.getPersonAddress().getPostalCode() == null);
				break;
			case SP_ADDRESS_COUNTRY:
				peopleList.removeIf(p -> p.getPersonAddress() == null || p.getPersonAddress().getCountry() == null);
				break;
		}
		
		assertThat(peopleList.size(), greaterThan(1));
		
		return peopleList;
	}
}
