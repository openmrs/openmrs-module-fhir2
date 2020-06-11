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
import ca.uhn.fhir.rest.param.StringAndListParam;
import ca.uhn.fhir.rest.param.StringOrListParam;
import ca.uhn.fhir.rest.param.StringParam;
import ca.uhn.fhir.rest.param.TokenAndListParam;
import ca.uhn.fhir.rest.param.TokenOrListParam;
import org.exparity.hamcrest.date.DateMatchers;
import org.hamcrest.comparator.ComparatorMatcherBuilder;
import org.hibernate.SessionFactory;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.PersonName;
import org.openmrs.Relationship;
import org.openmrs.module.fhir2.TestFhirSpringConfiguration;
import org.openmrs.test.BaseModuleContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;

@ContextConfiguration(classes = TestFhirSpringConfiguration.class, inheritLocations = false)
public class FhirRelatedPersonDaoImplTest extends BaseModuleContextSensitiveTest {
	
	private static final String RELATIONSHIP_UUID = "c3c91630-8563-481b-8efa-48e10c139a3d";
	
	private static final String BAD_RELATIONSHIP_UUID = "d4c91630-8563-481b-8efa-48e10c139w6e";
	
	private static final String RELATIONSHIP_DATA_XML = "org/openmrs/module/fhir2/api/dao/impl/FhirRelatedPersonDaoImplTest_intial_data.xml";
	
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
	
	@Autowired
	@Qualifier("sessionFactory")
	private SessionFactory sessionFactory;
	
	private FhirRelatedPersonDaoImpl fhirrelatedPersonDao;
	
	@Before
	public void setup() throws Exception {
		fhirrelatedPersonDao = new FhirRelatedPersonDaoImpl();
		fhirrelatedPersonDao.setSessionFactory(sessionFactory);
		executeDataSet(RELATIONSHIP_DATA_XML);
	}
	
	@Test
	public void getRelationshipByUuid_shouldReturnMatchingRelationship() {
		Relationship relationship = fhirrelatedPersonDao.get(RELATIONSHIP_UUID);
		assertThat(relationship, notNullValue());
		assertThat(relationship.getUuid(), notNullValue());
		assertThat(relationship.getUuid(), equalTo(RELATIONSHIP_UUID));
	}
	
	@Test
	public void getRelationshipWithWrongUuid_shouldReturnNull() {
		Relationship relationship = fhirrelatedPersonDao.get(BAD_RELATIONSHIP_UUID);
		assertThat(relationship, nullValue());
	}
	
	@Test
	public void shouldReturnCollectionOfRelationsForMatchOnPersonName() {
		StringAndListParam stringOrListParam = new StringAndListParam()
		        .addAnd(new StringOrListParam().add(new StringParam(PERSON_NAME)));
		Collection<Relationship> relations = fhirrelatedPersonDao.searchRelationships(stringOrListParam, null, null, null,
		    null, null, null, null);
		assertThat(relations, notNullValue());
		assertThat(relations, not(empty()));
		assertThat(relations.size(), greaterThanOrEqualTo(1));
	}
	
	@Test
	public void shouldReturnCollectionOfRelationsForPartialMatchOnPersonName() {
		StringAndListParam stringOrListParam = new StringAndListParam()
		        .addAnd(new StringOrListParam().add(new StringParam(PERSON_PARTIAL_NAME)));
		Collection<Relationship> relationships = fhirrelatedPersonDao.searchRelationships(stringOrListParam, null, null,
		    null, null, null, null, null);
		assertThat(relationships, notNullValue());
		assertThat(relationships, not(empty()));
		assertThat(relationships.size(), greaterThanOrEqualTo(1));
	}
	
	@Test
	public void shouldReturnEmptyCollectionForNoMatchOnPersonName() {
		StringAndListParam stringOrListParam = new StringAndListParam()
		        .addAnd(new StringOrListParam().add(new StringParam(NOT_FOUND_NAME)));
		Collection<Relationship> relationships = fhirrelatedPersonDao.searchRelationships(stringOrListParam, null, null,
		    null, null, null, null, null);
		assertThat(relationships, is(empty()));
	}
	
	@Test
	public void shouldReturnCollectionOfRelationsForMatchingGenderOfRelatedPerson() {
		final String GENDER_PROPERTY = "gender";
		final String RELATED_PERSON_PROPERTY = "gender";
		
		Collection<Relationship> relationships = fhirrelatedPersonDao.searchRelationships(null,
		    new TokenAndListParam().addAnd(new TokenOrListParam().add(MALE_GENDER)), null, null, null, null, null, null);
		
		assertThat(relationships, notNullValue());
		assertThat(relationships, not(empty()));
		assertThat(relationships, everyItem(hasProperty("personA", hasProperty("gender", equalTo("M")))));
		
		relationships = fhirrelatedPersonDao.searchRelationships(null,
		    new TokenAndListParam().addAnd(new TokenOrListParam().add(FEMALE_GENDER)), null, null, null, null, null, null);
		
		assertThat(relationships, notNullValue());
		assertThat(relationships, not(empty()));
		assertThat(relationships, everyItem(hasProperty("personA", hasProperty("gender", equalTo("F")))));
		
		relationships = fhirrelatedPersonDao.searchRelationships(null,
		    new TokenAndListParam().addAnd(new TokenOrListParam().add(OTHER_GENDER)), null, null, null, null, null, null);
		
		assertThat(relationships, notNullValue());
		assertThat(relationships, not(empty()));
		assertThat(relationships, everyItem(hasProperty("personA", hasProperty("gender", nullValue()))));
		
		relationships = fhirrelatedPersonDao.searchRelationships(null,
		    new TokenAndListParam().addAnd(new TokenOrListParam().add(NULL_GENDER)), null, null, null, null, null, null);
		
		assertThat(relationships, notNullValue());
		assertThat(relationships, not(empty()));
		assertThat(relationships, everyItem(hasProperty("personA", hasProperty("gender", nullValue()))));
		
		relationships = fhirrelatedPersonDao.searchRelationships(null,
		    new TokenAndListParam().addAnd(new TokenOrListParam().add(UNKNOWN_GENDER)), null, null, null, null, null, null);
		
		assertThat(relationships, notNullValue());
		assertThat(relationships, not(empty()));
		assertThat(relationships, everyItem(hasProperty("personA", hasProperty("gender", nullValue()))));
	}
	
	@Test
	public void shouldReturnEmptyCollectionForNoMatchOnGender() {
		TokenAndListParam tokenAndListParam = new TokenAndListParam().addAnd(new TokenOrListParam().add(WRONG_GENDER));
		Collection<Relationship> relationships = fhirrelatedPersonDao.searchRelationships(null, tokenAndListParam, null,
		    null, null, null, null, null);
		
		assertThat(relationships, notNullValue());
		assertThat(relationships, is(empty()));
	}
	
	@Test
	public void shouldReturnCollectionOfRelationForMatchOnBirthDateofRelatedPerson() throws ParseException {
		DateRangeParam dateRangeParam = new DateRangeParam().setLowerBound(BIRTH_DATE).setUpperBound(BIRTH_DATE);
		Collection<Relationship> relationships = fhirrelatedPersonDao.searchRelationships(null, null, dateRangeParam, null,
		    null, null, null, null);
		
		assertThat(relationships, notNullValue());
		assertThat(relationships.size(), greaterThanOrEqualTo(1));
		assertThat(relationships.stream().findAny().isPresent(), is(true));
		assertThat(relationships.stream().findAny().get().getPersonA().getBirthdate(),
		    DateMatchers.sameDay(DATE_FORMAT.parse(BIRTH_DATE)));
	}
	
	@Test
	public void shouldReturnEmptyCollectionForNoMatchOnBirthDate() {
		DateRangeParam dateRangeParam = new DateRangeParam().setLowerBound(NOT_FOUND_BIRTH_DATE)
		        .setUpperBound(NOT_FOUND_BIRTH_DATE);
		Collection<Relationship> relationships = fhirrelatedPersonDao.searchRelationships(null, null, dateRangeParam, null,
		    null, null, null, null);
		assertThat(relationships, notNullValue());
		assertThat(relationships, empty());
	}
	
	@Test
	public void shouldReturnCollectionOfRelatedPeopleForMatchOnCity() {
		StringAndListParam stringAndListParam = new StringAndListParam()
		        .addAnd(new StringOrListParam().add(new StringParam(CITY)));
		Collection<Relationship> relationships = fhirrelatedPersonDao.searchRelationships(null, null, null,
		    stringAndListParam, null, null, null, null);
		assertThat(relationships, notNullValue());
		assertThat(relationships, not(empty()));
		assertThat(relationships.size(), greaterThanOrEqualTo(1));
		assertThat(relationships.stream().findAny().isPresent(), is(true));
		assertThat(relationships.stream().findAny().get().getPersonA().getUuid(), equalTo(PERSON_ADDRESS_PERSON_UUID));
	}
	
	@Test
	public void shouldReturnCollectionOfRelatedPeopleForMatchOnState() {
		StringAndListParam stringAndListParam = new StringAndListParam()
		        .addAnd(new StringOrListParam().add(new StringParam(STATE)));
		Collection<Relationship> relationships = fhirrelatedPersonDao.searchRelationships(null, null, null, null,
		    stringAndListParam, null, null, null);
		assertThat(relationships, notNullValue());
		assertThat(relationships, not(empty()));
		assertThat(relationships.size(), greaterThanOrEqualTo(1));
		assertThat(relationships.stream().findAny().isPresent(), is(true));
		assertThat(relationships.stream().findAny().get().getPersonA().getUuid(), equalTo(PERSON_ADDRESS_PERSON_UUID));
	}
	
	@Test
	public void shouldReturnCollectionOfRelatedPeopleForMatchOnPostalCode() {
		StringAndListParam stringAndListParam = new StringAndListParam()
		        .addAnd(new StringOrListParam().add(new StringParam(POSTAL_CODE)));
		Collection<Relationship> relationships = fhirrelatedPersonDao.searchRelationships(null, null, null, null, null,
		    stringAndListParam, null, null);
		assertThat(relationships, notNullValue());
		assertThat(relationships, not(empty()));
		assertThat(relationships.size(), greaterThanOrEqualTo(1));
		assertThat(relationships.stream().findAny().isPresent(), is(true));
		assertThat(relationships.stream().findAny().get().getPersonA().getUuid(), equalTo(PERSON_ADDRESS_PERSON_UUID));
	}
	
	@Test
	public void shouldReturnCollectionOfRelatedPeopleForMatchOnCountry() {
		StringAndListParam stringAndListParam = new StringAndListParam()
		        .addAnd(new StringOrListParam().add(new StringParam(COUNTRY)));
		Collection<Relationship> relationships = fhirrelatedPersonDao.searchRelationships(null, null, null, null, null, null,
		    stringAndListParam, null);
		assertThat(relationships, notNullValue());
		assertThat(relationships, not(empty()));
		assertThat(relationships.size(), greaterThanOrEqualTo(1));
		assertThat(relationships.stream().findAny().isPresent(), is(true));
		assertThat(relationships.stream().findAny().get().getPersonA().getUuid(), equalTo(PERSON_ADDRESS_PERSON_UUID));
	}
	
	@Test
	public void shouldReturnCollectionOfRelatedPeopleSortedByName() {
		SortSpec sort = new SortSpec();
		sort.setParamName("name");
		sort.setOrder(SortOrderEnum.ASC);
		
		List<Relationship> relationshipList = getRelationListForSorting(sort);
		
		for (int i = 1; i < relationshipList.size(); i++) {
			assertThat(relationshipList.get(i - 1).getPersonA().getPersonName(),
			    NAME_MATCHER.lessThanOrEqualTo(relationshipList.get(i).getPersonA().getPersonName()));
		}
		
		sort.setOrder(SortOrderEnum.DESC);
		
		relationshipList = getRelationListForSorting(sort);
		
		for (int i = 1; i < relationshipList.size(); i++) {
			assertThat(relationshipList.get(i - 1).getPersonA().getPersonName(),
			    NAME_MATCHER.greaterThanOrEqualTo(relationshipList.get(i).getPersonA().getPersonName()));
		}
	}
	
	@Test
	public void shouldReturnCollectionOfRelatedPeopleSortedByBirthDate() {
		SortSpec sort = new SortSpec();
		sort.setParamName("birthdate");
		sort.setOrder(SortOrderEnum.ASC);
		List<Relationship> relationshipList = getRelationListForSorting(sort);
		
		for (int i = 1; i < relationshipList.size(); i++) {
			assertThat(relationshipList.get(i - 1).getPersonA().getBirthdate(),
			    sameOrBefore(relationshipList.get(i).getPersonA().getBirthdate()));
		}
		
		sort.setOrder(SortOrderEnum.DESC);
		relationshipList = getRelationListForSorting(sort);
		
		for (int i = 1; i < relationshipList.size(); i++) {
			assertThat(relationshipList.get(i - 1).getPersonA().getBirthdate(),
			    sameOrAfter(relationshipList.get(i).getPersonA().getBirthdate()));
		}
	}
	
	@Test
	public void shouldReturnCollectionOfRelatedPeopleSortedByCity() {
		SortSpec sort = new SortSpec();
		sort.setParamName("address-city");
		sort.setOrder(SortOrderEnum.ASC);
		
		List<Relationship> relationshipList = getRelationListForSorting(sort);
		
		for (int i = 1; i < relationshipList.size(); i++) {
			assertThat(relationshipList.get(i - 1).getPersonA().getPersonAddress().getCityVillage(),
			    lessThanOrEqualTo(relationshipList.get(i).getPersonA().getPersonAddress().getCityVillage()));
		}
		
		sort.setOrder(SortOrderEnum.DESC);
		
		relationshipList = getRelationListForSorting(sort);
		
		for (int i = 1; i < relationshipList.size(); i++) {
			assertThat(relationshipList.get(i - 1).getPersonA().getPersonAddress().getCityVillage(),
			    greaterThanOrEqualTo(relationshipList.get(i).getPersonA().getPersonAddress().getCityVillage()));
		}
	}
	
	@Test
	public void shouldReturnCollectionOfRelatedPeopleSortedByState() {
		SortSpec sort = new SortSpec();
		sort.setParamName("address-state");
		sort.setOrder(SortOrderEnum.ASC);
		
		List<Relationship> relationshipList = getRelationListForSorting(sort);
		
		for (int i = 1; i < relationshipList.size(); i++) {
			assertThat(relationshipList.get(i - 1).getPersonA().getPersonAddress().getStateProvince(),
			    lessThanOrEqualTo(relationshipList.get(i).getPersonA().getPersonAddress().getStateProvince()));
		}
		
		sort.setOrder(SortOrderEnum.DESC);
		
		relationshipList = getRelationListForSorting(sort);
		
		for (int i = 1; i < relationshipList.size(); i++) {
			assertThat(relationshipList.get(i - 1).getPersonA().getPersonAddress().getStateProvince(),
			    greaterThanOrEqualTo(relationshipList.get(i).getPersonA().getPersonAddress().getStateProvince()));
		}
	}
	
	@Test
	public void shouldReturnCollectionOfRelatedPeopleSortedByPostalCode() {
		SortSpec sort = new SortSpec();
		sort.setParamName("address-postalcode");
		sort.setOrder(SortOrderEnum.ASC);
		
		List<Relationship> relationshipList = getRelationListForSorting(sort);
		
		for (int i = 1; i < relationshipList.size(); i++) {
			assertThat(relationshipList.get(i - 1).getPersonA().getPersonAddress().getPostalCode(),
			    lessThanOrEqualTo(relationshipList.get(i).getPersonA().getPersonAddress().getPostalCode()));
		}
		
		sort.setOrder(SortOrderEnum.DESC);
		
		relationshipList = getRelationListForSorting(sort);
		
		for (int i = 1; i < relationshipList.size(); i++) {
			assertThat(relationshipList.get(i - 1).getPersonA().getPersonAddress().getPostalCode(),
			    greaterThanOrEqualTo(relationshipList.get(i).getPersonA().getPersonAddress().getPostalCode()));
		}
	}
	
	@Test
	public void shouldReturnCollectionOfRelatedPeopleSortedByCountry() {
		SortSpec sort = new SortSpec();
		sort.setParamName("address-country");
		sort.setOrder(SortOrderEnum.ASC);
		
		List<Relationship> relationshipList = getRelationListForSorting(sort);
		
		for (int i = 1; i < relationshipList.size(); i++) {
			assertThat(relationshipList.get(i - 1).getPersonA().getPersonAddress().getCountry(),
			    lessThanOrEqualTo(relationshipList.get(i).getPersonA().getPersonAddress().getCountry()));
		}
		
		sort.setOrder(SortOrderEnum.DESC);
		
		relationshipList = getRelationListForSorting(sort);
		
		for (int i = 1; i < relationshipList.size(); i++) {
			assertThat(relationshipList.get(i - 1).getPersonA().getPersonAddress().getCountry(),
			    greaterThanOrEqualTo(relationshipList.get(i).getPersonA().getPersonAddress().getCountry()));
		}
	}
	
	@Test
	public void shouldHandleComplexQuery() throws ParseException {
		StringAndListParam nameParam = new StringAndListParam()
		        .addAnd(new StringOrListParam().add(new StringParam(PERSON_NAME)));
		TokenAndListParam genderParam = new TokenAndListParam().addAnd(new TokenOrListParam().add(MALE_GENDER));
		DateRangeParam birthDateParam = new DateRangeParam().setLowerBound(BIRTH_DATE).setUpperBound(BIRTH_DATE);
		StringAndListParam cityParam = new StringAndListParam().addAnd(new StringOrListParam().add(new StringParam(CITY)));
		StringAndListParam stateParam = new StringAndListParam().addAnd(new StringOrListParam().add(new StringParam(STATE)));
		StringAndListParam postalCodeParam = new StringAndListParam()
		        .addAnd(new StringOrListParam().add(new StringParam(POSTAL_CODE)));
		StringAndListParam countryParam = new StringAndListParam()
		        .addAnd(new StringOrListParam().add(new StringParam(COUNTRY)));
		
		Collection<Relationship> relationships = fhirrelatedPersonDao.searchRelationships(nameParam, genderParam,
		    birthDateParam, cityParam, stateParam, postalCodeParam, countryParam, null);
		
		assertThat(relationships, notNullValue());
		assertThat(relationships, not(empty()));
		assertThat(relationships.size(), greaterThanOrEqualTo(1));
		assertThat(relationships.iterator().next().getPersonA().getGivenName(), equalTo(PERSON_NAME));
		assertThat(relationships.iterator().next().getPersonA().getGender(), equalTo("M"));
		assertThat(relationships.iterator().next().getPersonA().getBirthdate(),
		    DateMatchers.sameDay(DATE_FORMAT.parse(BIRTH_DATE)));
		assertThat(relationships.iterator().next().getPersonA().getUuid(), equalTo(PERSON_ADDRESS_PERSON_UUID));
	}
	
	private List<Relationship> getRelationListForSorting(SortSpec sort) {
		Collection<Relationship> relationships = fhirrelatedPersonDao.searchRelationships(null, null, null, null, null, null,
		    null, sort);
		
		assertThat(relationships, notNullValue());
		assertThat(relationships, not(empty()));
		assertThat(relationships.size(), greaterThan(1));
		
		List<Relationship> relationshipList = new ArrayList<>(relationships);
		// Remove people with sort parameter value null, to allow comparison while asserting.
		switch (sort.getParamName()) {
			case SP_NAME:
				relationshipList.removeIf(p -> p.getPersonA().getPersonName() == null);
				break;
			case SP_BIRTHDATE:
				relationshipList.removeIf(p -> p.getPersonA().getBirthdate() == null);
				break;
			case SP_ADDRESS_CITY:
				relationshipList.removeIf(p -> p.getPersonA().getPersonAddress() == null
				        || p.getPersonA().getPersonAddress().getCityVillage() == null);
				break;
			case SP_ADDRESS_STATE:
				relationshipList.removeIf(p -> p.getPersonA().getPersonAddress() == null
				        || p.getPersonA().getPersonAddress().getStateProvince() == null);
				break;
			case SP_ADDRESS_POSTALCODE:
				relationshipList.removeIf(p -> p.getPersonA().getPersonAddress() == null
				        || p.getPersonA().getPersonAddress().getPostalCode() == null);
				break;
			case SP_ADDRESS_COUNTRY:
				relationshipList.removeIf(p -> p.getPersonA().getPersonAddress() == null
				        || p.getPersonA().getPersonAddress().getCountry() == null);
				break;
		}
		
		assertThat(relationshipList.size(), greaterThan(1));
		
		return relationshipList;
	}
	
}
