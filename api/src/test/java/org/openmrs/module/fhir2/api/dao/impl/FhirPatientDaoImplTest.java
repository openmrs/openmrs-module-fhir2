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
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.hl7.fhir.r4.model.Patient.SP_ADDRESS_CITY;
import static org.hl7.fhir.r4.model.Patient.SP_ADDRESS_COUNTRY;
import static org.hl7.fhir.r4.model.Patient.SP_ADDRESS_POSTALCODE;
import static org.hl7.fhir.r4.model.Patient.SP_ADDRESS_STATE;
import static org.hl7.fhir.r4.model.Patient.SP_BIRTHDATE;
import static org.hl7.fhir.r4.model.Patient.SP_DEATH_DATE;
import static org.hl7.fhir.r4.model.Patient.SP_FAMILY;
import static org.hl7.fhir.r4.model.Patient.SP_GIVEN;
import static org.hl7.fhir.r4.model.Patient.SP_NAME;
import static org.openmrs.util.OpenmrsUtil.compareWithNullAsGreatest;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import ca.uhn.fhir.rest.api.SortOrderEnum;
import ca.uhn.fhir.rest.api.SortSpec;
import ca.uhn.fhir.rest.param.DateParam;
import ca.uhn.fhir.rest.param.DateRangeParam;
import ca.uhn.fhir.rest.param.StringAndListParam;
import ca.uhn.fhir.rest.param.StringOrListParam;
import ca.uhn.fhir.rest.param.StringParam;
import ca.uhn.fhir.rest.param.TokenAndListParam;
import ca.uhn.fhir.rest.param.TokenOrListParam;
import ca.uhn.fhir.rest.param.TokenParam;
import org.hamcrest.comparator.ComparatorMatcherBuilder;
import org.hibernate.SessionFactory;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.Patient;
import org.openmrs.PersonAddress;
import org.openmrs.PersonName;
import org.openmrs.module.fhir2.TestFhirSpringConfiguration;
import org.openmrs.test.BaseModuleContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

@ContextConfiguration(classes = TestFhirSpringConfiguration.class, inheritLocations = false)
public class FhirPatientDaoImplTest extends BaseModuleContextSensitiveTest {
	
	private static final String PATIENT_UUID = "256ccf6d-6b41-455c-9be2-51ff4386ae76";
	
	private static final String BAD_PATIENT_UUID = "282390a6-3608-496d-9025-aecbc1235670";
	
	private static final String[] PATIENT_SEARCH_DATA_FILES = {
	        "org/openmrs/api/include/PatientServiceTest-findPatients.xml",
	        "org/openmrs/module/fhir2/api/dao/impl/FhirPatientDaoImplTest_address_data.xml" };
	
	private static final String PATIENT_GIVEN_NAME = "Jeannette";
	
	private static final String PATIENT_PARTIAL_GIVEN_NAME = "Jean";
	
	private static final String PATIENT_FAMILY_NAME = "Claudent";
	
	private static final String PATIENT_PARTIAL_FAMILY_NAME = "Claud";
	
	private static final String PATIENT_NOT_FOUND_NAME = "Igor";
	
	private static final String PATIENT_IDENTIFIER_PATIENT_UUID = "30e2aa2a-4ed1-415d-84c5-ba29016c14b7";
	
	private static final String PATIENT_IDENTIFIER = "563422";
	
	private static final String BAD_PATIENT_IDENTIFIER = "99999999";
	
	private static final String PATIENT_IDENTIFIER_TYPE = "Test Identifier Type";
	
	private static final String BAD_PATIENT_IDENTIFIER_TYPE = "Non-Existent Identifier";
	
	private static final String PATIENT_MALE_GENDER = "male";
	
	private static final String PATIENT_FEMALE_GENDER = "female";
	
	private static final String PATIENT_WRONG_GENDER = "wrong-gender";
	
	private static final String PATIENT_BIRTHDATE = "1976-08-25";
	
	private static final String PATIENT_BIRTHDATE_LOWER_BOUND = "1975-04-08";
	
	private static final String PATIENT_BIRTHDATE_PATIENT_UUID = "ca17fcc5-ec96-487f-b9ea-42973c8973e3";
	
	private static final String PATIENT_ADDRESS_CITY = "Indianapolis";
	
	private static final String PATIENT_ADDRESS_STATE = "IN";
	
	private static final String PATIENT_ADDRESS_COUNTRY = "USA";
	
	private static final String PATIENT_ADDRESS_POSTAL_CODE = "46202";
	
	private static final String PATIENT_ADDRESS_PATIENT_UUID = "61b38324-e2fd-4feb-95b7-9e9a2a4400df";
	
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
	
	private FhirPatientDaoImpl dao;
	
	@Autowired
	private SessionFactory sessionFactory;
	
	@Before
	public void setup() throws Exception {
		dao = new FhirPatientDaoImpl();
		dao.setSessionFactory(sessionFactory);
		for (String search_data : PATIENT_SEARCH_DATA_FILES) {
			executeDataSet(search_data);
		}
	}
	
	@Test
	public void getPatientById_shouldRetrievePatientById() {
		Patient result = dao.getPatientById(4);
		
		assertThat(result, notNullValue());
		assertThat(result.getUuid(), equalTo(PATIENT_UUID));
		assertThat(result.getId(), equalTo(4));
	}
	
	@Test
	public void getPatientById_shouldReturnNullIfPatientNotFound() {
		assertThat(dao.getPatientById(0), nullValue());
	}
	
	@Test
	public void getPatientByUuid_shouldRetrievePatientByUuid() {
		Patient result = dao.getPatientByUuid(PATIENT_UUID);
		
		assertThat(result, notNullValue());
		assertThat(result.getUuid(), equalTo(PATIENT_UUID));
	}
	
	@Test
	public void getPatientByUuid_shouldReturnNullIfPatientNotFound() {
		Patient result = dao.getPatientByUuid(BAD_PATIENT_UUID);
		
		assertThat(result, nullValue());
	}
	
	@Test
	public void searchForPatients_shouldSearchForPatientsByName() {
		Collection<Patient> results = dao.searchForPatients(
		    new StringAndListParam().addAnd(new StringOrListParam().add(new StringParam(PATIENT_GIVEN_NAME))), null, null,
		    null, null, null, null, null, null, null, null, null, null);
		
		assertThat(results, notNullValue());
		assertThat(results, not(empty()));
		assertThat(results.iterator().next().getGivenName(), equalTo(PATIENT_GIVEN_NAME));
	}
	
	@Test
	public void searchForPatients_shouldReturnMultiplePatientsForPartialMatch() {
		Collection<Patient> results = dao.searchForPatients(
		    new StringAndListParam().addAnd(new StringOrListParam().add(new StringParam(PATIENT_PARTIAL_GIVEN_NAME))), null,
		    null, null, null, null, null, null, null, null, null, null, null);
		
		assertThat(results, notNullValue());
		assertThat(results, not(empty()));
		assertThat(results.size(), greaterThan(1));
	}
	
	@Test
	public void searchForPatients_shouldReturnEmptyCollectionWhenPatientNameNotMatched() {
		Collection<Patient> results = dao.searchForPatients(
		    new StringAndListParam().addAnd(new StringOrListParam().add(new StringParam(PATIENT_NOT_FOUND_NAME))), null,
		    null, null, null, null, null, null, null, null, null, null, null);
		
		assertThat(results, notNullValue());
		assertThat(results, empty());
	}
	
	@Test
	public void searchForPatients_shouldSearchForPatientsByGivenName() {
		Collection<Patient> results = dao.searchForPatients(null,
		    new StringAndListParam().addAnd(new StringOrListParam().add(new StringParam(PATIENT_GIVEN_NAME))), null, null,
		    null, null, null, null, null, null, null, null, null);
		
		assertThat(results, notNullValue());
		assertThat(results, not(empty()));
		assertThat(results.iterator().next().getGivenName(), equalTo(PATIENT_GIVEN_NAME));
	}
	
	@Test
	public void searchForPatients_shouldReturnMultiplePatientsForPartialMatchOnGivenName() {
		Collection<Patient> results = dao.searchForPatients(null,
		    new StringAndListParam().addAnd(new StringOrListParam().add(new StringParam(PATIENT_PARTIAL_GIVEN_NAME))), null,
		    null, null, null, null, null, null, null, null, null, null);
		
		assertThat(results, notNullValue());
		assertThat(results, not(empty()));
		assertThat(results.size(), greaterThan(1));
	}
	
	@Test
	public void searchForPatients_shouldReturnEmptyCollectionWhenPatientGivenNameNotMatched() {
		Collection<Patient> results = dao.searchForPatients(null,
		    new StringAndListParam().addAnd(new StringOrListParam().add(new StringParam(PATIENT_NOT_FOUND_NAME))), null,
		    null, null, null, null, null, null, null, null, null, null);
		
		assertThat(results, notNullValue());
		assertThat(results, empty());
	}
	
	@Test
	public void searchForPatients_shouldSearchForPatientsByFamilyName() {
		Collection<Patient> results = dao.searchForPatients(null, null,
		    new StringAndListParam().addAnd(new StringOrListParam().add(new StringParam(PATIENT_FAMILY_NAME))), null, null,
		    null, null, null, null, null, null, null, null);
		
		assertThat(results, notNullValue());
		assertThat(results, not(empty()));
		assertThat(results.iterator().next().getFamilyName(), equalTo(PATIENT_FAMILY_NAME));
	}
	
	@Test
	public void searchForPatients_shouldReturnMultiplePatientsForPartialMatchOnFamilyName() {
		Collection<Patient> results = dao.searchForPatients(null, null,
		    new StringAndListParam().addAnd(new StringOrListParam().add(new StringParam(PATIENT_PARTIAL_FAMILY_NAME))), null,
		    null, null, null, null, null, null, null, null, null);
		
		assertThat(results, notNullValue());
		assertThat(results, not(empty()));
		assertThat(results.size(), greaterThan(1));
	}
	
	@Test
	public void searchForPatients_shouldReturnEmptyCollectionWhenPatientFamilyNameNotMatched() {
		Collection<Patient> results = dao.searchForPatients(null, null,
		    new StringAndListParam().addAnd(new StringOrListParam().add(new StringParam(PATIENT_NOT_FOUND_NAME))), null,
		    null, null, null, null, null, null, null, null, null);
		
		assertThat(results, notNullValue());
		assertThat(results, empty());
	}
	
	@Test
	public void searchForPatients_shouldSearchForPatientsByIdentifier() {
		Collection<Patient> results = dao.searchForPatients(null, null, null,
		    new TokenAndListParam().addAnd(new TokenOrListParam().add(new TokenParam(null, PATIENT_IDENTIFIER))), null, null,
		    null, null, null, null, null, null, null);
		
		assertThat(results, notNullValue());
		assertThat(results, not(empty()));
		assertThat(results.iterator().next().getUuid(), equalTo(PATIENT_IDENTIFIER_PATIENT_UUID));
	}
	
	@Test
	public void searchForPatients_shouldSearchForPatientsByIdentifierWithType() {
		Collection<Patient> results = dao.searchForPatients(null, null, null,
		    new TokenAndListParam()
		            .addAnd(new TokenOrListParam().add(new TokenParam(PATIENT_IDENTIFIER_TYPE, PATIENT_IDENTIFIER))),
		    null, null, null, null, null, null, null, null, null);
		
		assertThat(results, notNullValue());
		assertThat(results, not(empty()));
		assertThat(results.iterator().next().getUuid(), equalTo(PATIENT_IDENTIFIER_PATIENT_UUID));
	}
	
	@Test
	public void searchForPatients_shouldReturnEmptyCollectionWhenIdentifierNotMatched() {
		Collection<Patient> results = dao.searchForPatients(null, null, null,
		    new TokenAndListParam()
		            .addAnd(new TokenOrListParam().add(new TokenParam(PATIENT_IDENTIFIER_TYPE, BAD_PATIENT_IDENTIFIER))),
		    null, null, null, null, null, null, null, null, null);
		
		assertThat(results, notNullValue());
		assertThat(results, empty());
	}
	
	@Test
	public void searchForPatients_shouldReturnEmptyCollectionWhenIdentifierTypeNotMatched() {
		Collection<Patient> results = dao.searchForPatients(null, null, null,
		    new TokenAndListParam()
		            .addAnd(new TokenOrListParam().add(new TokenParam(BAD_PATIENT_IDENTIFIER_TYPE, PATIENT_IDENTIFIER))),
		    null, null, null, null, null, null, null, null, null);
		
		assertThat(results, notNullValue());
		assertThat(results, empty());
	}
	
	@Test
	public void searchForPatients_shouldReturnPatientsByGender() {
		final String GENDER_PROPERTY = "gender";
		
		Collection<Patient> results = dao.searchForPatients(null, null, null, null,
		    new TokenAndListParam().addAnd(new TokenOrListParam().add(new TokenParam(PATIENT_MALE_GENDER))), null, null,
		    null, null, null, null, null, null);
		
		assertThat(results, notNullValue());
		assertThat(results, not(empty()));
		assertThat(results, everyItem(hasProperty(GENDER_PROPERTY, equalTo("M"))));
		
		results = dao.searchForPatients(null, null, null, null,
		    new TokenAndListParam().addAnd(new TokenOrListParam().add(new TokenParam(PATIENT_FEMALE_GENDER))), null, null,
		    null, null, null, null, null, null);
		
		assertThat(results, notNullValue());
		assertThat(results, not(empty()));
		assertThat(results, everyItem(hasProperty(GENDER_PROPERTY, equalTo("F"))));
	}
	
	@Test
	public void searchForPatients_shouldReturnEmptyCollectionWhenGenderNotMatched() {
		Collection<Patient> results = dao.searchForPatients(null, null, null, null,
		    new TokenAndListParam().addAnd(new TokenOrListParam().add(new TokenParam(PATIENT_WRONG_GENDER))), null, null,
		    null, null, null, null, null, null);
		
		assertThat(results, notNullValue());
		assertThat(results, empty());
	}
	
	@Test
	public void searchForPatients_shouldSearchForPatientsByBirthDate() {
		Collection<Patient> results = dao.searchForPatients(null, null, null, null, null,
		    new DateRangeParam(new DateParam(PATIENT_BIRTHDATE)), null, null, null, null, null, null, null);
		
		assertThat(results, notNullValue());
		assertThat(results, not(empty()));
		assertThat(results, hasItem(hasProperty("uuid", equalTo(PATIENT_BIRTHDATE_PATIENT_UUID))));
	}
	
	@Test
	public void searchForPatients_shouldSearchForPatientsByBirthDateWithLowerBound() {
		Collection<Patient> results = dao.searchForPatients(null, null, null, null, null,
		    new DateRangeParam().setLowerBound(PATIENT_BIRTHDATE), null, null, null, null, null, null, null);
		
		assertThat(results, notNullValue());
		assertThat(results, not(empty()));
		assertThat(results, hasSize(greaterThan(1)));
		assertThat(results, hasItem(hasProperty("uuid", equalTo(PATIENT_BIRTHDATE_PATIENT_UUID))));
	}
	
	@Test
	public void searchForPatients_shouldSearchForPatientsByBirthDateWithUpperBound() {
		Collection<Patient> results = dao.searchForPatients(null, null, null, null, null,
		    new DateRangeParam().setUpperBound(PATIENT_BIRTHDATE), null, null, null, null, null, null, null);
		
		assertThat(results, notNullValue());
		assertThat(results, not(empty()));
		assertThat(results, hasSize(greaterThan(1)));
		assertThat(results, hasItem(hasProperty("uuid", equalTo(PATIENT_BIRTHDATE_PATIENT_UUID))));
	}
	
	@Test
	public void searchForPatients_shouldSearchForPatientsByBirthDateWithinBoundaries() {
		Collection<Patient> results = dao.searchForPatients(null, null, null, null, null,
		    new DateRangeParam().setLowerBound(PATIENT_BIRTHDATE_LOWER_BOUND).setUpperBound(PATIENT_BIRTHDATE), null, null,
		    null, null, null, null, null);
		
		assertThat(results, notNullValue());
		assertThat(results, not(empty()));
		assertThat(results, hasSize(greaterThan(1)));
		assertThat(results, hasItem(hasProperty("uuid", equalTo(PATIENT_BIRTHDATE_PATIENT_UUID))));
	}
	
	@Test
	public void searchForPatients_shouldSearchForPatientsByCity() {
		Collection<Patient> results = dao.searchForPatients(null, null, null, null, null, null, null, null,
		    new StringAndListParam().addAnd(new StringOrListParam().add(new StringParam(PATIENT_ADDRESS_CITY))), null, null,
		    null, null);
		
		assertThat(results, notNullValue());
		assertThat(results, not(empty()));
		assertThat(results.iterator().next().getUuid(), equalTo(PATIENT_ADDRESS_PATIENT_UUID));
	}
	
	@Test
	public void searchForPatients_shouldSearchForPatientsByState() {
		Collection<Patient> results = dao.searchForPatients(null, null, null, null, null, null, null, null, null,
		    new StringAndListParam().addAnd(new StringOrListParam().add(new StringParam(PATIENT_ADDRESS_STATE))), null, null,
		    null);
		
		assertThat(results, notNullValue());
		assertThat(results, not(empty()));
		assertThat(results.iterator().next().getUuid(), equalTo(PATIENT_ADDRESS_PATIENT_UUID));
	}
	
	@Test
	public void searchForPatients_shouldSearchForPatientsCountry() {
		Collection<Patient> results = dao.searchForPatients(null, null, null, null, null, null, null, null, null, null, null,
		    new StringAndListParam().addAnd(new StringOrListParam().add(new StringParam(PATIENT_ADDRESS_COUNTRY))), null);
		
		assertThat(results, notNullValue());
		assertThat(results, not(empty()));
		assertThat(results.iterator().next().getUuid(), equalTo(PATIENT_ADDRESS_PATIENT_UUID));
	}
	
	@Test
	public void searchForPatients_shouldSearchForPatientsByPostalCode() {
		Collection<Patient> results = dao.searchForPatients(null, null, null, null, null, null, null, null, null, null,
		    new StringAndListParam().addAnd(new StringOrListParam().add(new StringParam(PATIENT_ADDRESS_POSTAL_CODE))), null,
		    null);
		
		assertThat(results, notNullValue());
		assertThat(results, not(empty()));
		assertThat(results.iterator().next().getUuid(), equalTo(PATIENT_ADDRESS_PATIENT_UUID));
	}
	
	@Test
	public void shouldReturnCollectionOfPatientsSortedByName() {
		SortSpec sort = new SortSpec();
		sort.setParamName("name");
		sort.setOrder(SortOrderEnum.ASC);
		
		List<Patient> people = getPatientListForSorting(sort);
		
		for (int i = 1; i < people.size(); i++) {
			assertThat(people.get(i - 1).getPersonName(), NAME_MATCHER.lessThanOrEqualTo(people.get(i).getPersonName()));
		}
		
		sort.setOrder(SortOrderEnum.DESC);
		
		people = getPatientListForSorting(sort);
		
		for (int i = 1; i < people.size(); i++) {
			assertThat(people.get(i - 1).getPersonName(), NAME_MATCHER.greaterThanOrEqualTo(people.get(i).getPersonName()));
		}
	}
	
	@Test
	public void shouldReturnCollectionOfPatientsSortedByGivenName() {
		SortSpec sort = new SortSpec();
		sort.setParamName(SP_GIVEN);
		sort.setOrder(SortOrderEnum.ASC);
		
		List<Patient> people = getPatientListForSorting(sort);
		
		for (int i = 1; i < people.size(); i++) {
			assertThat(people.get(i - 1).getGivenName(), lessThanOrEqualTo(people.get(i).getGivenName()));
		}
		
		sort.setOrder(SortOrderEnum.DESC);
		
		people = getPatientListForSorting(sort);
		
		for (int i = 1; i < people.size(); i++) {
			assertThat(people.get(i - 1).getGivenName(), greaterThanOrEqualTo(people.get(i).getGivenName()));
		}
	}
	
	@Test
	public void shouldReturnCollectionOfPatientsSortedByFamilyName() {
		SortSpec sort = new SortSpec();
		sort.setParamName(SP_FAMILY);
		sort.setOrder(SortOrderEnum.ASC);
		
		List<Patient> people = getPatientListForSorting(sort);
		
		for (int i = 1; i < people.size(); i++) {
			assertThat(people.get(i - 1).getFamilyName(), lessThanOrEqualTo(people.get(i).getFamilyName()));
		}
		
		sort.setOrder(SortOrderEnum.DESC);
		
		people = getPatientListForSorting(sort);
		
		for (int i = 1; i < people.size(); i++) {
			assertThat(people.get(i - 1).getFamilyName(), greaterThanOrEqualTo(people.get(i).getFamilyName()));
		}
	}
	
	@Test
	public void shouldReturnCollectionOfPatientsSortedByBirthDate() {
		SortSpec sort = new SortSpec();
		sort.setParamName("birthdate");
		sort.setOrder(SortOrderEnum.ASC);
		List<Patient> patients = getPatientListForSorting(sort);
		
		for (int i = 1; i < patients.size(); i++) {
			assertThat(patients.get(i - 1).getBirthdate(), sameOrBefore(patients.get(i).getBirthdate()));
		}
		
		sort.setOrder(SortOrderEnum.DESC);
		patients = getPatientListForSorting(sort);
		
		for (int i = 1; i < patients.size(); i++) {
			assertThat(patients.get(i - 1).getBirthdate(), sameOrAfter(patients.get(i).getBirthdate()));
		}
	}
	
	@Test
	public void shouldReturnCollectionOfPatientsSortedByDeathDate() {
		SortSpec sort = new SortSpec();
		sort.setParamName(SP_DEATH_DATE);
		sort.setOrder(SortOrderEnum.ASC);
		List<Patient> patients = getPatientListForSorting(sort);
		
		for (int i = 1; i < patients.size(); i++) {
			assertThat(patients.get(i - 1).getDeathDate(), sameOrBefore(patients.get(i).getDeathDate()));
		}
		
		sort.setOrder(SortOrderEnum.DESC);
		patients = getPatientListForSorting(sort);
		
		for (int i = 1; i < patients.size(); i++) {
			assertThat(patients.get(i - 1).getDeathDate(), sameOrAfter(patients.get(i).getDeathDate()));
		}
	}
	
	@Test
	public void shouldReturnCollectionOfPatientsSortedByCity() {
		SortSpec sort = new SortSpec();
		sort.setParamName("address-city");
		sort.setOrder(SortOrderEnum.ASC);
		
		List<Patient> patients = getPatientListForSorting(sort);
		
		for (int i = 1; i < patients.size(); i++) {
			assertThat(patients.get(i - 1).getPersonAddress().getCityVillage(),
			    lessThanOrEqualTo(patients.get(i).getPersonAddress().getCityVillage()));
		}
		
		sort.setOrder(SortOrderEnum.DESC);
		
		patients = getPatientListForSorting(sort);
		
		for (int i = 1; i < patients.size(); i++) {
			assertThat(patients.get(i - 1).getPersonAddress().getCityVillage(),
			    greaterThanOrEqualTo(patients.get(i).getPersonAddress().getCityVillage()));
		}
	}
	
	@Test
	public void shouldReturnCollectionOfPatientsSortedByState() {
		SortSpec sort = new SortSpec();
		sort.setParamName("address-state");
		sort.setOrder(SortOrderEnum.ASC);
		
		List<Patient> patients = getPatientListForSorting(sort);
		
		for (int i = 1; i < patients.size(); i++) {
			assertThat(patients.get(i - 1).getPersonAddress().getStateProvince(),
			    lessThanOrEqualTo(patients.get(i).getPersonAddress().getStateProvince()));
		}
		
		sort.setOrder(SortOrderEnum.DESC);
		
		patients = getPatientListForSorting(sort);
		
		for (int i = 1; i < patients.size(); i++) {
			assertThat(patients.get(i - 1).getPersonAddress().getStateProvince(),
			    greaterThanOrEqualTo(patients.get(i).getPersonAddress().getStateProvince()));
		}
	}
	
	@Test
	public void shouldReturnCollectionOfPatientsSortedByPostalCode() {
		SortSpec sort = new SortSpec();
		sort.setParamName("address-postalcode");
		sort.setOrder(SortOrderEnum.ASC);
		
		List<Patient> patients = getPatientListForSorting(sort);
		
		for (int i = 1; i < patients.size(); i++) {
			assertThat(patients.get(i - 1).getPersonAddress().getPostalCode(),
			    lessThanOrEqualTo(patients.get(i).getPersonAddress().getPostalCode()));
		}
		
		sort.setOrder(SortOrderEnum.DESC);
		
		patients = getPatientListForSorting(sort);
		
		for (int i = 1; i < patients.size(); i++) {
			assertThat(patients.get(i - 1).getPersonAddress().getPostalCode(),
			    greaterThanOrEqualTo(patients.get(i).getPersonAddress().getPostalCode()));
		}
	}
	
	@Test
	public void shouldReturnCollectionOfPatientsSortedByCountry() {
		SortSpec sort = new SortSpec();
		sort.setParamName("address-country");
		sort.setOrder(SortOrderEnum.ASC);
		
		List<Patient> patients = getPatientListForSorting(sort);
		
		for (int i = 1; i < patients.size(); i++) {
			assertThat(patients.get(i - 1).getPersonAddress().getCountry(),
			    lessThanOrEqualTo(patients.get(i).getPersonAddress().getCountry()));
		}
		
		sort.setOrder(SortOrderEnum.DESC);
		
		patients = getPatientListForSorting(sort);
		
		for (int i = 1; i < patients.size(); i++) {
			assertThat(patients.get(i - 1).getPersonAddress().getCountry(),
			    greaterThanOrEqualTo(patients.get(i).getPersonAddress().getCountry()));
		}
	}
	
	private List<Patient> getPatientListForSorting(SortSpec sort) {
		Collection<Patient> patients = dao.searchForPatients(null, null, null, null, null, null, null, null, null, null,
		    null, null, sort);
		
		assertThat(patients, notNullValue());
		assertThat(patients, not(empty()));
		assertThat(patients.size(), greaterThan(1));
		
		List<Patient> patientList = new ArrayList<>(patients);
		// remove patients with sort parameter value null, to allow comparison while asserting. 
		switch (sort.getParamName()) {
			case SP_NAME:
				patientList.removeIf(p -> p.getPersonName() == null);
			case SP_GIVEN:
				patientList.removeIf(p -> p.getGivenName() == null);
				break;
			case SP_FAMILY:
				patientList.removeIf(p -> p.getFamilyName() == null);
				break;
			case SP_BIRTHDATE:
				patientList.removeIf(p -> p.getBirthdate() == null);
				break;
			case SP_DEATH_DATE:
				patientList.removeIf(p -> p.getDeathDate() == null);
				break;
			case SP_ADDRESS_CITY:
				patientList.removeIf(p -> addressComponentNullOrEmpty(p.getPersonAddress(), "city"));
				break;
			case SP_ADDRESS_STATE:
				patientList.removeIf(p -> addressComponentNullOrEmpty(p.getPersonAddress(), "state"));
				break;
			case SP_ADDRESS_POSTALCODE:
				patientList.removeIf(p -> addressComponentNullOrEmpty(p.getPersonAddress(), "postalCode"));
				break;
			case SP_ADDRESS_COUNTRY:
				patientList.removeIf(p -> addressComponentNullOrEmpty(p.getPersonAddress(), "country"));
				break;
		}
		
		assertThat(patientList.size(), greaterThan(1));
		
		return patientList;
	}
	
	private boolean addressComponentNullOrEmpty(PersonAddress address, String component) {
		if (address == null) {
			return true;
		}
		
		switch (component) {
			case "city":
				return address.getCityVillage() == null || address.getCityVillage().isEmpty();
			case "state":
				return address.getStateProvince() == null || address.getStateProvince().isEmpty();
			case "postalCode":
				return address.getPostalCode() == null || address.getPostalCode().isEmpty();
			case "country":
				return address.getCountry() == null || address.getCountry().isEmpty();
		}
		
		return false;
	}
}
