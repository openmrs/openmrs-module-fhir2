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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

import javax.inject.Inject;

import java.util.Collection;

import ca.uhn.fhir.rest.param.DateParam;
import ca.uhn.fhir.rest.param.DateRangeParam;
import ca.uhn.fhir.rest.param.StringOrListParam;
import ca.uhn.fhir.rest.param.StringParam;
import ca.uhn.fhir.rest.param.TokenOrListParam;
import ca.uhn.fhir.rest.param.TokenParam;
import org.hibernate.SessionFactory;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.Patient;
import org.openmrs.module.fhir2.TestFhirSpringConfiguration;
import org.openmrs.test.BaseModuleContextSensitiveTest;
import org.springframework.test.context.ContextConfiguration;

@ContextConfiguration(classes = TestFhirSpringConfiguration.class, inheritLocations = false)
public class FhirPatientDaoImplTest extends BaseModuleContextSensitiveTest {
	
	private static final String PATIENT_UUID = "256ccf6d-6b41-455c-9be2-51ff4386ae76";
	
	private static final String BAD_PATIENT_UUID = "282390a6-3608-496d-9025-aecbc1235670";
	
	private static final String PATIENT_SEARCH_DATA_XML = "org/openmrs/api/include/PatientServiceTest-findPatients.xml";
	
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
	
	private static final String PATIENT_BIRTHDATE_WITH_TIME = "1976-08-25T13:44:57.0";
	
	private static final String PATIENT_BIRTHDATE_LOWER_BOUND = "1975-04-08";
	
	private static final String PATIENT_BIRTHDATE_PATIENT_UUID = "ca17fcc5-ec96-487f-b9ea-42973c8973e3";
	
	private static final String PATIENT_ADDRESS_CITY = "Indianapolis";
	
	private static final String PATIENT_ADDRESS_STATE = "IN";
	
	private static final String PATIENT_ADDRESS_COUNTRY = "USA";
	
	private static final String PATIENT_ADDRESS_POSTAL_CODE = "46202";
	
	private static final String PATIENT_ADDRESS_PATIENT_UUID = "61b38324-e2fd-4feb-95b7-9e9a2a4400df";
	
	private FhirPatientDaoImpl dao;
	
	@Inject
	private SessionFactory sessionFactory;
	
	@Before
	public void setup() throws Exception {
		dao = new FhirPatientDaoImpl();
		dao.setSessionFactory(sessionFactory);
		executeDataSet(PATIENT_SEARCH_DATA_XML);
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
		Collection<Patient> results = dao.searchForPatients(new StringOrListParam().add(new StringParam(PATIENT_GIVEN_NAME)),
		    null, null, null, null, null, null, null, null, null, null, null, null);
		
		assertThat(results, notNullValue());
		assertThat(results, not(empty()));
		assertThat(results.iterator().next().getGivenName(), equalTo(PATIENT_GIVEN_NAME));
	}
	
	@Test
	public void searchForPatients_shouldReturnMultiplePatientsForPartialMatch() {
		Collection<Patient> results = dao.searchForPatients(
		    new StringOrListParam().add(new StringParam(PATIENT_PARTIAL_GIVEN_NAME)), null, null, null, null, null, null,
		    null, null, null, null, null, null);
		
		assertThat(results, notNullValue());
		assertThat(results, not(empty()));
		assertThat(results.size(), greaterThan(1));
	}
	
	@Test
	public void searchForPatients_shouldReturnEmptyCollectionWhenPatientNameNotMatched() {
		Collection<Patient> results = dao.searchForPatients(
		    new StringOrListParam().add(new StringParam(PATIENT_NOT_FOUND_NAME)), null, null, null, null, null, null, null,
		    null, null, null, null, null);
		
		assertThat(results, notNullValue());
		assertThat(results, empty());
	}
	
	@Test
	public void searchForPatients_shouldSearchForPatientsByGivenName() {
		Collection<Patient> results = dao.searchForPatients(null,
		    new StringOrListParam().add(new StringParam(PATIENT_GIVEN_NAME)), null, null, null, null, null, null, null, null,
		    null, null, null);
		
		assertThat(results, notNullValue());
		assertThat(results, not(empty()));
		assertThat(results.iterator().next().getGivenName(), equalTo(PATIENT_GIVEN_NAME));
	}
	
	@Test
	public void searchForPatients_shouldReturnMultiplePatientsForPartialMatchOnGivenName() {
		Collection<Patient> results = dao.searchForPatients(null,
		    new StringOrListParam().add(new StringParam(PATIENT_PARTIAL_GIVEN_NAME)), null, null, null, null, null, null,
		    null, null, null, null, null);
		
		assertThat(results, notNullValue());
		assertThat(results, not(empty()));
		assertThat(results.size(), greaterThan(1));
	}
	
	@Test
	public void searchForPatients_shouldReturnEmptyCollectionWhenPatientGivenNameNotMatched() {
		Collection<Patient> results = dao.searchForPatients(null,
		    new StringOrListParam().add(new StringParam(PATIENT_NOT_FOUND_NAME)), null, null, null, null, null, null, null,
		    null, null, null, null);
		
		assertThat(results, notNullValue());
		assertThat(results, empty());
	}
	
	@Test
	public void searchForPatients_shouldSearchForPatientsByFamilyName() {
		Collection<Patient> results = dao.searchForPatients(null, null,
		    new StringOrListParam().add(new StringParam(PATIENT_FAMILY_NAME)), null, null, null, null, null, null, null,
		    null, null, null);
		
		assertThat(results, notNullValue());
		assertThat(results, not(empty()));
		assertThat(results.iterator().next().getFamilyName(), equalTo(PATIENT_FAMILY_NAME));
	}
	
	@Test
	public void searchForPatients_shouldReturnMultiplePatientsForPartialMatchOnFamilyName() {
		Collection<Patient> results = dao.searchForPatients(null, null,
		    new StringOrListParam().add(new StringParam(PATIENT_PARTIAL_FAMILY_NAME)), null, null, null, null, null, null,
		    null, null, null, null);
		
		assertThat(results, notNullValue());
		assertThat(results, not(empty()));
		assertThat(results.size(), greaterThan(1));
	}
	
	@Test
	public void searchForPatients_shouldReturnEmptyCollectionWhenPatientFamilyNameNotMatched() {
		Collection<Patient> results = dao.searchForPatients(null, null,
		    new StringOrListParam().add(new StringParam(PATIENT_NOT_FOUND_NAME)), null, null, null, null, null, null, null,
		    null, null, null);
		
		assertThat(results, notNullValue());
		assertThat(results, empty());
	}
	
	@Test
	public void searchForPatients_shouldSearchForPatientsByIdentifier() {
		Collection<Patient> results = dao.searchForPatients(null, null, null,
		    new TokenOrListParam().add(new TokenParam(null, PATIENT_IDENTIFIER)), null, null, null, null, null, null, null,
		    null, null);
		
		assertThat(results, notNullValue());
		assertThat(results, not(empty()));
		assertThat(results.iterator().next().getUuid(), equalTo(PATIENT_IDENTIFIER_PATIENT_UUID));
	}
	
	@Test
	public void searchForPatients_shouldSearchForPatientsByIdentifierWithType() {
		Collection<Patient> results = dao.searchForPatients(null, null, null,
		    new TokenOrListParam().add(new TokenParam(PATIENT_IDENTIFIER_TYPE, PATIENT_IDENTIFIER)), null, null, null, null,
		    null, null, null, null, null);
		
		assertThat(results, notNullValue());
		assertThat(results, not(empty()));
		assertThat(results.iterator().next().getUuid(), equalTo(PATIENT_IDENTIFIER_PATIENT_UUID));
	}
	
	@Test
	public void searchForPatients_shouldReturnEmptyCollectionWhenIdentifierNotMatched() {
		Collection<Patient> results = dao.searchForPatients(null, null, null,
		    new TokenOrListParam().add(new TokenParam(PATIENT_IDENTIFIER_TYPE, BAD_PATIENT_IDENTIFIER)), null, null, null,
		    null, null, null, null, null, null);
		
		assertThat(results, notNullValue());
		assertThat(results, empty());
	}
	
	@Test
	public void searchForPatients_shouldReturnEmptyCollectionWhenIdentifierTypeNotMatched() {
		Collection<Patient> results = dao.searchForPatients(null, null, null,
		    new TokenOrListParam().add(new TokenParam(BAD_PATIENT_IDENTIFIER_TYPE, PATIENT_IDENTIFIER)), null, null, null,
		    null, null, null, null, null, null);
		
		assertThat(results, notNullValue());
		assertThat(results, empty());
	}
	
	@Test
	public void searchForPatients_shouldReturnPatientsByGender() {
		final String GENDER_PROPERTY = "gender";
		
		Collection<Patient> results = dao.searchForPatients(null, null, null, null,
		    new TokenOrListParam().add(new TokenParam(PATIENT_MALE_GENDER)), null, null, null, null, null, null, null, null);
		
		assertThat(results, notNullValue());
		assertThat(results, not(empty()));
		assertThat(results, everyItem(hasProperty(GENDER_PROPERTY, equalTo("M"))));
		
		results = dao.searchForPatients(null, null, null, null,
		    new TokenOrListParam().add(new TokenParam(PATIENT_FEMALE_GENDER)), null, null, null, null, null, null, null,
		    null);
		
		assertThat(results, notNullValue());
		assertThat(results, not(empty()));
		assertThat(results, everyItem(hasProperty(GENDER_PROPERTY, equalTo("F"))));
	}
	
	@Test
	public void searchForPatients_shouldReturnEmptyCollectionWhenGenderNotMatched() {
		Collection<Patient> results = dao.searchForPatients(null, null, null, null,
		    new TokenOrListParam().add(new TokenParam(PATIENT_WRONG_GENDER)), null, null, null, null, null, null, null,
		    null);
		
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
	public void searchForPatients_shouldTruncateBirthDateToDay() {
		Collection<Patient> results = dao.searchForPatients(null, null, null, null, null,
		    new DateRangeParam(new DateParam(PATIENT_BIRTHDATE_WITH_TIME)), null, null, null, null, null, null, null);
		
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
		    new StringOrListParam().add(new StringParam(PATIENT_ADDRESS_CITY)), null, null, null, null);
		
		assertThat(results, notNullValue());
		assertThat(results, not(empty()));
		assertThat(results.iterator().next().getUuid(), equalTo(PATIENT_ADDRESS_PATIENT_UUID));
	}
	
	@Test
	public void searchForPatients_shouldSearchForPatientsByState() {
		Collection<Patient> results = dao.searchForPatients(null, null, null, null, null, null, null, null, null,
		    new StringOrListParam().add(new StringParam(PATIENT_ADDRESS_STATE)), null, null, null);
		
		assertThat(results, notNullValue());
		assertThat(results, not(empty()));
		assertThat(results.iterator().next().getUuid(), equalTo(PATIENT_ADDRESS_PATIENT_UUID));
	}
	
	@Test
	public void searchForPatients_shouldSearchForPatientsCountry() {
		Collection<Patient> results = dao.searchForPatients(null, null, null, null, null, null, null, null, null, null, null,
		    new StringOrListParam().add(new StringParam(PATIENT_ADDRESS_COUNTRY)), null);
		
		assertThat(results, notNullValue());
		assertThat(results, not(empty()));
		assertThat(results.iterator().next().getUuid(), equalTo(PATIENT_ADDRESS_PATIENT_UUID));
	}
	
	@Test
	public void searchForPatients_shouldSearchForPatientsByPostalCode() {
		Collection<Patient> results = dao.searchForPatients(null, null, null, null, null, null, null, null, null, null,
		    new StringOrListParam().add(new StringParam(PATIENT_ADDRESS_POSTAL_CODE)), null, null);
		
		assertThat(results, notNullValue());
		assertThat(results, not(empty()));
		assertThat(results.iterator().next().getUuid(), equalTo(PATIENT_ADDRESS_PATIENT_UUID));
	}
}
