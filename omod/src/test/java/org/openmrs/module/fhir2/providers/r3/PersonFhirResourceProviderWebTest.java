/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.fhir2.providers.r3;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsStringIgnoringCase;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import javax.servlet.ServletException;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;

import ca.uhn.fhir.model.api.Include;
import ca.uhn.fhir.rest.param.DateRangeParam;
import ca.uhn.fhir.rest.param.StringAndListParam;
import ca.uhn.fhir.rest.param.TokenAndListParam;
import lombok.AccessLevel;
import lombok.Getter;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.dstu3.model.OperationOutcome;
import org.hl7.fhir.dstu3.model.Person;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.api.FhirPersonService;
import org.openmrs.module.fhir2.providers.r4.MockIBundleProvider;
import org.springframework.mock.web.MockHttpServletResponse;

@RunWith(MockitoJUnitRunner.class)
public class PersonFhirResourceProviderWebTest extends BaseFhirR3ResourceProviderWebTest<PersonFhirResourceProvider, Person> {
	
	private static final String PERSON_NAME = "Hannibal Lector";
	
	private static final String PERSON_GENDER = "male";
	
	private static final String PERSON_UUID = "8a849d5e-6011-4279-a124-40ada5a687de";
	
	private static final String WRONG_PERSON_UUID = "9bf0d1ac-62a8-4440-a5a1-eb1015a7cc65";
	
	private static final String ADDRESS_FIELD = "Washington";
	
	private static final String POSTAL_CODE = "98136";
	
	private static final String LAST_UPDATED_DATE = "eq2020-09-03";
	
	private static final String JSON_CREATE_PERSON_PATH = "org/openmrs/module/fhir2/providers/PersonWebTest_create.json";
	
	private static final String JSON_UPDATE_PERSON_PATH = "org/openmrs/module/fhir2/providers/PersonWebTest_update.json";
	
	private static final String JSON_UPDATE_PERSON_NO_ID_PATH = "org/openmrs/module/fhir2/providers/PersonWebTest_updateWithoutId.json";
	
	private static final String JSON_UPDATE_PERSON_WRONG_ID_PATH = "org/openmrs/module/fhir2/providers/PersonWebTest_updateWithWrongId.json";
	
	@Mock
	private FhirPersonService personService;
	
	@Getter(AccessLevel.PUBLIC)
	private PersonFhirResourceProvider resourceProvider;
	
	@Captor
	private ArgumentCaptor<StringAndListParam> stringAndListCaptor;
	
	@Captor
	private ArgumentCaptor<TokenAndListParam> tokenAndListCaptor;
	
	@Captor
	private ArgumentCaptor<DateRangeParam> dateRangeCaptor;
	
	@Captor
	private ArgumentCaptor<HashSet<Include>> includeArgumentCaptor;
	
	@Before
	@Override
	public void setup() throws ServletException {
		resourceProvider = new PersonFhirResourceProvider();
		resourceProvider.setPersonService(personService);
		super.setup();
	}
	
	@Test
	public void shouldReturnPersonByUuid() throws Exception {
		org.hl7.fhir.r4.model.Person person = new org.hl7.fhir.r4.model.Person();
		person.setId(PERSON_UUID);
		when(personService.get(PERSON_UUID)).thenReturn(person);
		
		MockHttpServletResponse response = get("/Person/" + PERSON_UUID).accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), equalTo(FhirMediaTypes.JSON.toString()));
		
		Person resource = readResponse(response);
		assertThat(resource.getIdElement().getIdPart(), equalTo(PERSON_UUID));
	}
	
	@Test
	public void shouldReturn404IfPersonNotFound() throws Exception {
		when(personService.get(WRONG_PERSON_UUID)).thenReturn(null);
		
		MockHttpServletResponse response = get("/Person/" + WRONG_PERSON_UUID).accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isNotFound());
	}
	
	@Test
	public void shouldGetPersonByName() throws Exception {
		verifyUri(String.format("/Person/?name=%s", PERSON_NAME));
		
		verify(personService).searchForPeople(stringAndListCaptor.capture(), isNull(), isNull(), isNull(), isNull(),
		    isNull(), isNull(), isNull(), isNull(), isNull(), isNull());
		
		assertThat(stringAndListCaptor.getValue(), notNullValue());
		assertThat(stringAndListCaptor.getValue().getValuesAsQueryTokens(), not(empty()));
		assertThat(stringAndListCaptor.getValue().getValuesAsQueryTokens().get(0).getValuesAsQueryTokens().get(0).getValue(),
		    equalTo(PERSON_NAME));
	}
	
	@Test
	public void shouldGetPersonByGender() throws Exception {
		verifyUri(String.format("/Person/?gender=%s", PERSON_GENDER));
		
		verify(personService).searchForPeople(isNull(), tokenAndListCaptor.capture(), isNull(), isNull(), isNull(), isNull(),
		    isNull(), isNull(), isNull(), isNull(), isNull());
		
		assertThat(tokenAndListCaptor.getValue(), notNullValue());
		assertThat(tokenAndListCaptor.getValue().getValuesAsQueryTokens(), not(empty()));
		assertThat(tokenAndListCaptor.getValue().getValuesAsQueryTokens().get(0).getValuesAsQueryTokens().get(0).getValue(),
		    equalTo(PERSON_GENDER));
	}
	
	@Test
	public void shouldGetPersonByBirthDate() throws Exception {
		verifyUri("/Person/?birthdate=eq1975-02-02");
		
		verify(personService).searchForPeople(isNull(), isNull(), dateRangeCaptor.capture(), isNull(), isNull(), isNull(),
		    isNull(), isNull(), isNull(), isNull(), isNull());
		assertThat(dateRangeCaptor.getValue(), notNullValue());
		
		Calendar calendar = Calendar.getInstance();
		calendar.set(1975, Calendar.FEBRUARY, 2);
		
		assertThat(dateRangeCaptor.getValue().getLowerBound().getValue(),
		    equalTo(DateUtils.truncate(calendar.getTime(), Calendar.DATE)));
		assertThat(dateRangeCaptor.getValue().getUpperBound().getValue(),
		    equalTo(DateUtils.truncate(calendar.getTime(), Calendar.DATE)));
	}
	
	@Test
	public void shouldGetPersonByBirthDateGreaterThanOrEqualTo() throws Exception {
		verifyUri("/Person/?birthdate=ge1975-02-02");
		
		verify(personService).searchForPeople(isNull(), isNull(), dateRangeCaptor.capture(), isNull(), isNull(), isNull(),
		    isNull(), isNull(), isNull(), isNull(), isNull());
		assertThat(dateRangeCaptor.getValue(), notNullValue());
		
		Calendar calendar = Calendar.getInstance();
		calendar.set(1975, Calendar.FEBRUARY, 2);
		
		assertThat(dateRangeCaptor.getValue().getLowerBound().getValue(),
		    equalTo(DateUtils.truncate(calendar.getTime(), Calendar.DATE)));
		assertThat(dateRangeCaptor.getValue().getUpperBound(), nullValue());
	}
	
	@Test
	public void shouldGetPersonByBirthDateGreaterThan() throws Exception {
		verifyUri("/Person/?birthdate=gt1975-02-02");
		
		verify(personService).searchForPeople(isNull(), isNull(), dateRangeCaptor.capture(), isNull(), isNull(), isNull(),
		    isNull(), isNull(), isNull(), isNull(), isNull());
		assertThat(dateRangeCaptor.getValue(), notNullValue());
		
		Calendar calendar = Calendar.getInstance();
		calendar.set(1975, Calendar.FEBRUARY, 2);
		
		assertThat(dateRangeCaptor.getValue().getLowerBound().getValue(),
		    equalTo(DateUtils.truncate(calendar.getTime(), Calendar.DATE)));
		assertThat(dateRangeCaptor.getValue().getUpperBound(), nullValue());
	}
	
	@Test
	public void shouldGetPersonByBirthDateLessThanOrEqualTo() throws Exception {
		verifyUri("/Person/?birthdate=le1975-02-02");
		
		verify(personService).searchForPeople(isNull(), isNull(), dateRangeCaptor.capture(), isNull(), isNull(), isNull(),
		    isNull(), isNull(), isNull(), isNull(), isNull());
		assertThat(dateRangeCaptor.getValue(), notNullValue());
		
		Calendar calendar = Calendar.getInstance();
		calendar.set(1975, Calendar.FEBRUARY, 2);
		
		assertThat(dateRangeCaptor.getValue().getLowerBound(), nullValue());
		assertThat(dateRangeCaptor.getValue().getUpperBound().getValue(),
		    equalTo(DateUtils.truncate(calendar.getTime(), Calendar.DATE)));
	}
	
	@Test
	public void shouldGetPersonByBirthDateLessThan() throws Exception {
		verifyUri("/Person/?birthdate=lt1975-02-02");
		
		verify(personService).searchForPeople(isNull(), isNull(), dateRangeCaptor.capture(), isNull(), isNull(), isNull(),
		    isNull(), isNull(), isNull(), isNull(), isNull());
		assertThat(dateRangeCaptor.getValue(), notNullValue());
		
		Calendar calendar = Calendar.getInstance();
		calendar.set(1975, Calendar.FEBRUARY, 2);
		
		assertThat(dateRangeCaptor.getValue().getLowerBound(), nullValue());
		assertThat(dateRangeCaptor.getValue().getUpperBound().getValue(),
		    equalTo(DateUtils.truncate(calendar.getTime(), Calendar.DATE)));
	}
	
	@Test
	public void shouldGetPersonByBirthDateBetween() throws Exception {
		verifyUri("/Person/?birthdate=ge1975-02-02&birthdate=le1980-02-02");
		
		verify(personService).searchForPeople(isNull(), isNull(), dateRangeCaptor.capture(), isNull(), isNull(), isNull(),
		    isNull(), isNull(), isNull(), isNull(), isNull());
		
		Calendar lowerBound = Calendar.getInstance();
		lowerBound.set(1975, Calendar.FEBRUARY, 2);
		Calendar upperBound = Calendar.getInstance();
		upperBound.set(1980, Calendar.FEBRUARY, 2);
		
		assertThat(dateRangeCaptor.getValue(), notNullValue());
		assertThat(dateRangeCaptor.getValue().getLowerBound().getValue(),
		    equalTo(DateUtils.truncate(lowerBound.getTime(), Calendar.DATE)));
		assertThat(dateRangeCaptor.getValue().getUpperBound().getValue(),
		    equalTo(DateUtils.truncate(upperBound.getTime(), Calendar.DATE)));
	}
	
	@Test
	public void shouldGetPersonByCity() throws Exception {
		verifyUri(String.format("/Person/?address-city=%s", ADDRESS_FIELD));
		
		verify(personService).searchForPeople(isNull(), isNull(), isNull(), stringAndListCaptor.capture(), isNull(),
		    isNull(), isNull(), isNull(), isNull(), isNull(), isNull());
		
		assertThat(stringAndListCaptor.getValue(), notNullValue());
		assertThat(stringAndListCaptor.getValue().getValuesAsQueryTokens(), not(empty()));
		assertThat(stringAndListCaptor.getValue().getValuesAsQueryTokens().get(0).getValuesAsQueryTokens().get(0).getValue(),
		    equalTo(ADDRESS_FIELD));
	}
	
	@Test
	public void shouldGetPersonByState() throws Exception {
		verifyUri(String.format("/Person/?address-state=%s", ADDRESS_FIELD));
		
		verify(personService).searchForPeople(isNull(), isNull(), isNull(), isNull(), stringAndListCaptor.capture(),
		    isNull(), isNull(), isNull(), isNull(), isNull(), isNull());
		
		assertThat(stringAndListCaptor.getValue(), notNullValue());
		assertThat(stringAndListCaptor.getValue().getValuesAsQueryTokens(), not(empty()));
		assertThat(stringAndListCaptor.getValue().getValuesAsQueryTokens().get(0).getValuesAsQueryTokens().get(0).getValue(),
		    equalTo(ADDRESS_FIELD));
	}
	
	@Test
	public void shouldGetPersonByPostalCode() throws Exception {
		verifyUri(String.format("/Person/?address-postalcode=%s", POSTAL_CODE));
		
		verify(personService).searchForPeople(isNull(), isNull(), isNull(), isNull(), isNull(),
		    stringAndListCaptor.capture(), isNull(), isNull(), isNull(), isNull(), isNull());
		
		assertThat(stringAndListCaptor.getValue(), notNullValue());
		assertThat(stringAndListCaptor.getValue().getValuesAsQueryTokens(), not(empty()));
		assertThat(stringAndListCaptor.getValue().getValuesAsQueryTokens().get(0).getValuesAsQueryTokens().get(0).getValue(),
		    equalTo(POSTAL_CODE));
	}
	
	@Test
	public void shouldGetPersonByCountry() throws Exception {
		verifyUri(String.format("/Person/?address-country=%s", ADDRESS_FIELD));
		
		verify(personService).searchForPeople(isNull(), isNull(), isNull(), isNull(), isNull(), isNull(),
		    stringAndListCaptor.capture(), isNull(), isNull(), isNull(), isNull());
		
		assertThat(stringAndListCaptor.getValue(), notNullValue());
		assertThat(stringAndListCaptor.getValue().getValuesAsQueryTokens(), not(empty()));
		assertThat(stringAndListCaptor.getValue().getValuesAsQueryTokens().get(0).getValuesAsQueryTokens().get(0).getValue(),
		    equalTo(ADDRESS_FIELD));
	}
	
	@Test
	public void shouldGetPersonByUUID() throws Exception {
		verifyUri(String.format("/Person?_id=%s", PERSON_UUID));
		
		verify(personService).searchForPeople(isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(),
		    tokenAndListCaptor.capture(), isNull(), isNull(), isNull());
		
		assertThat(tokenAndListCaptor.getValue(), notNullValue());
		assertThat(tokenAndListCaptor.getValue().getValuesAsQueryTokens(), not(empty()));
		assertThat(tokenAndListCaptor.getValue().getValuesAsQueryTokens().get(0).getValuesAsQueryTokens().get(0).getValue(),
		    equalTo(PERSON_UUID));
	}
	
	@Test
	public void shouldGetPersonByLastUpdatedDate() throws Exception {
		verifyUri(String.format("/Person?_lastUpdated=%s", LAST_UPDATED_DATE));
		
		verify(personService).searchForPeople(isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(),
		    dateRangeCaptor.capture(), isNull(), isNull());
		
		assertThat(dateRangeCaptor.getValue(), notNullValue());
		
		Calendar calendar = Calendar.getInstance();
		calendar.set(2020, Calendar.SEPTEMBER, 3);
		
		assertThat(dateRangeCaptor.getValue().getLowerBound().getValue(),
		    equalTo(DateUtils.truncate(calendar.getTime(), Calendar.DATE)));
		assertThat(dateRangeCaptor.getValue().getUpperBound().getValue(),
		    equalTo(DateUtils.truncate(calendar.getTime(), Calendar.DATE)));
	}
	
	@Test
	public void shouldGetPersonByComplexQuery() throws Exception {
		verifyUri(String.format("/Person/?name=%s&gender=%s&birthdate=eq1975-02-02", PERSON_NAME, PERSON_GENDER));
		
		verify(personService).searchForPeople(stringAndListCaptor.capture(), tokenAndListCaptor.capture(),
		    dateRangeCaptor.capture(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull());
		
		assertThat(stringAndListCaptor.getValue(), notNullValue());
		assertThat(stringAndListCaptor.getValue().getValuesAsQueryTokens(), not(empty()));
		assertThat(stringAndListCaptor.getValue().getValuesAsQueryTokens().get(0).getValuesAsQueryTokens().get(0).getValue(),
		    equalTo(PERSON_NAME));
		
		assertThat(tokenAndListCaptor.getValue(), notNullValue());
		assertThat(tokenAndListCaptor.getValue().getValuesAsQueryTokens(), not(empty()));
		assertThat(tokenAndListCaptor.getValue().getValuesAsQueryTokens().get(0).getValuesAsQueryTokens().get(0).getValue(),
		    equalTo(PERSON_GENDER));
		
		assertThat(dateRangeCaptor.getValue(), notNullValue());
		
		Calendar calendar = Calendar.getInstance();
		calendar.set(1975, Calendar.FEBRUARY, 2);
		
		assertThat(dateRangeCaptor.getValue().getLowerBound().getValue(),
		    equalTo(DateUtils.truncate(calendar.getTime(), Calendar.DATE)));
		assertThat(dateRangeCaptor.getValue().getUpperBound().getValue(),
		    equalTo(DateUtils.truncate(calendar.getTime(), Calendar.DATE)));
	}
	
	@Test
	public void shouldAddPatientsToResultListWhenIncluded() throws Exception {
		verifyUri("/Person?_include=Person:patient");
		
		verify(personService).searchForPeople(isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(),
		    isNull(), isNull(), includeArgumentCaptor.capture());
		
		assertThat(includeArgumentCaptor.getValue(), notNullValue());
		assertThat(includeArgumentCaptor.getValue().size(), equalTo(1));
		assertThat(includeArgumentCaptor.getValue().iterator().next().getParamName(),
		    equalTo(FhirConstants.INCLUDE_PATIENT_PARAM));
		assertThat(includeArgumentCaptor.getValue().iterator().next().getParamType(), equalTo(FhirConstants.PERSON));
	}
	
	@Test
	public void shouldAddLinksToResultListWhenIncluded() throws Exception {
		verifyUri("/Person?_include=Person:link:Patient");
		
		verify(personService).searchForPeople(isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(),
		    isNull(), isNull(), includeArgumentCaptor.capture());
		
		assertThat(includeArgumentCaptor.getValue(), notNullValue());
		assertThat(includeArgumentCaptor.getValue().size(), equalTo(1));
		assertThat(includeArgumentCaptor.getValue().iterator().next().getParamName(),
		    equalTo(FhirConstants.INCLUDE_LINK_PARAM));
		assertThat(includeArgumentCaptor.getValue().iterator().next().getParamType(), equalTo(FhirConstants.PERSON));
		assertThat(includeArgumentCaptor.getValue().iterator().next().getParamTargetType(), equalTo(FhirConstants.PATIENT));
	}
	
	private void verifyUri(String uri) throws Exception {
		Person person = new Person();
		person.setId(PERSON_UUID);
		when(personService.searchForPeople(any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any()))
		        .thenReturn(new MockIBundleProvider<>(Collections.singletonList(person), 10, 1));
		
		MockHttpServletResponse response = get(uri).accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), equalTo(FhirMediaTypes.JSON.toString()));
		
		Bundle results = readBundleResponse(response);
		assertThat(results.hasEntry(), is(true));
		assertThat(results.getEntry().get(0).getResource(), notNullValue());
		assertThat(results.getEntry().get(0).getResource().getIdElement().getIdPart(), equalTo(PERSON_UUID));
	}
	
	@Test
	public void createPerson_shouldCreatePerson() throws Exception {
		String jsonPerson;
		try (InputStream is = this.getClass().getClassLoader().getResourceAsStream(JSON_CREATE_PERSON_PATH)) {
			Objects.requireNonNull(is);
			jsonPerson = IOUtils.toString(is, StandardCharsets.UTF_8);
		}
		
		org.hl7.fhir.r4.model.Person person = new org.hl7.fhir.r4.model.Person();
		person.setId(PERSON_UUID);
		
		when(personService.create(any(org.hl7.fhir.r4.model.Person.class))).thenReturn(person);
		
		MockHttpServletResponse response = post("/Person").jsonContent(jsonPerson).accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isCreated());
	}
	
	@Test
	public void updatePerson_shouldUpdateExistingPerson() throws Exception {
		String jsonPerson;
		try (InputStream is = this.getClass().getClassLoader().getResourceAsStream(JSON_UPDATE_PERSON_PATH)) {
			Objects.requireNonNull(is);
			jsonPerson = IOUtils.toString(is, StandardCharsets.UTF_8);
		}
		
		org.hl7.fhir.r4.model.Person person = new org.hl7.fhir.r4.model.Person();
		person.setId(PERSON_UUID);
		
		when(personService.update(anyString(), any(org.hl7.fhir.r4.model.Person.class))).thenReturn(person);
		
		MockHttpServletResponse response = put("/Person/" + PERSON_UUID).jsonContent(jsonPerson).accept(FhirMediaTypes.JSON)
		        .go();
		
		assertThat(response, isOk());
	}
	
	@Test
	public void updatePerson_shouldThrowErrorForNoId() throws Exception {
		String jsonPerson;
		try (InputStream is = this.getClass().getClassLoader().getResourceAsStream(JSON_UPDATE_PERSON_NO_ID_PATH)) {
			Objects.requireNonNull(is);
			jsonPerson = IOUtils.toString(is, StandardCharsets.UTF_8);
		}
		
		MockHttpServletResponse response = put("/Person/" + PERSON_UUID).jsonContent(jsonPerson).accept(FhirMediaTypes.JSON)
		        .go();
		
		assertThat(response, isBadRequest());
		assertThat(response.getContentAsString(), containsStringIgnoringCase("body must contain an ID element for update"));
	}
	
	@Test
	public void updatePerson_shouldThrowErrorForIdMissMatch() throws Exception {
		String jsonPerson;
		try (InputStream is = this.getClass().getClassLoader().getResourceAsStream(JSON_UPDATE_PERSON_WRONG_ID_PATH)) {
			Objects.requireNonNull(is);
			jsonPerson = IOUtils.toString(is, StandardCharsets.UTF_8);
		}
		
		MockHttpServletResponse response = put("/Person/" + WRONG_PERSON_UUID).jsonContent(jsonPerson)
		        .accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isBadRequest());
		assertThat(response.getContentAsString(),
		    containsStringIgnoringCase("body must contain an ID element which matches the request URL"));
	}
	
	@Test
	public void deletePerson_shouldDeletePerson() throws Exception {
		OperationOutcome retVal = new OperationOutcome();
		retVal.setId(PERSON_UUID);
		retVal.getText().setDivAsString("Deleted successfully");
		
		org.hl7.fhir.r4.model.Person person = new org.hl7.fhir.r4.model.Person();
		person.setId(PERSON_UUID);
		
		when(personService.delete(PERSON_UUID)).thenReturn(person);
		
		MockHttpServletResponse response = delete("/Person/" + PERSON_UUID).accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), equalTo(FhirMediaTypes.JSON.toString()));
	}
}
