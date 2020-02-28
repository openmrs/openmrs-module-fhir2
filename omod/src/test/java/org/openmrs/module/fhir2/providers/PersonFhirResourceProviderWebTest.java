/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.fhir2.providers;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import javax.servlet.ServletException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;

import ca.uhn.fhir.rest.param.DateRangeParam;
import ca.uhn.fhir.rest.param.StringOrListParam;
import ca.uhn.fhir.rest.param.TokenOrListParam;
import lombok.AccessLevel;
import lombok.Getter;
import org.apache.commons.lang3.time.DateUtils;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.Person;
import org.hl7.fhir.r4.model.Provenance;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.api.FhirPersonService;
import org.openmrs.module.fhir2.api.util.FhirUtils;
import org.openmrs.module.fhir2.web.servlet.BaseFhirResourceProviderTest;
import org.springframework.mock.web.MockHttpServletResponse;

@RunWith(MockitoJUnitRunner.class)
public class PersonFhirResourceProviderWebTest extends BaseFhirResourceProviderTest<PersonFhirResourceProvider, Person> {
	
	private static final String PERSON_NAME = "Hannibal Lector";
	
	private static final String PERSON_GENDER = "male";
	
	private static final String PERSON_UUID = "8a849d5e-6011-4279-a124-40ada5a687de";
	
	private static final String WRONG_PERSON_UUID = "9bf0d1ac-62a8-4440-a5a1-eb1015a7cc65";
	
	private static final String ADDRESS_FIELD = "Washington";
	
	private static final String POSTAL_CODE = "98136";
	
	private static final String AUTHOR = "author";
	
	private static final String AUT = "AUT";
	
	@Mock
	private FhirPersonService personService;
	
	@Getter(AccessLevel.PUBLIC)
	private PersonFhirResourceProvider resourceProvider;
	
	@Captor
	private ArgumentCaptor<StringOrListParam> stringOrListCaptor;
	
	@Captor
	private ArgumentCaptor<TokenOrListParam> tokenOrListCaptor;
	
	@Captor
	private ArgumentCaptor<DateRangeParam> dateRangeCaptor;
	
	@Before
	@Override
	public void setup() throws Exception {
		resourceProvider = new PersonFhirResourceProvider();
		resourceProvider.setFhirPersonService(personService);
		super.setup();
	}
	
	@Test
	public void shouldReturnPersonByUuid() throws Exception {
		Person person = new Person();
		person.setId(PERSON_UUID);
		when(personService.getPersonByUuid(PERSON_UUID)).thenReturn(person);
		
		MockHttpServletResponse response = get("/Person/" + PERSON_UUID).accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), equalTo(FhirMediaTypes.JSON.toString()));
		
		Person resource = readResponse(response);
		assertThat(resource.getIdElement().getIdPart(), equalTo(PERSON_UUID));
	}
	
	@Test
	public void shouldReturn404IfPersonNotFound() throws Exception {
		when(personService.getPersonByUuid(WRONG_PERSON_UUID)).thenReturn(null);
		
		MockHttpServletResponse response = get("/Person/" + WRONG_PERSON_UUID).accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isNotFound());
	}
	
	@Test
	public void shouldGetPersonByName() throws Exception {
		verifyUri(String.format("/Person/?name=%s", PERSON_NAME));
		
		verify(personService).searchForPeople(stringOrListCaptor.capture(), isNull(), isNull(), isNull(), isNull(), isNull(),
		    isNull(), isNull());
		
		assertThat(stringOrListCaptor.getValue(), notNullValue());
		assertThat(stringOrListCaptor.getValue().getValuesAsQueryTokens(), not(empty()));
		assertThat(stringOrListCaptor.getValue().getValuesAsQueryTokens().get(0).getValue(), equalTo(PERSON_NAME));
	}
	
	@Test
	public void shouldGetPersonByGender() throws Exception {
		verifyUri(String.format("/Person/?gender=%s", PERSON_GENDER));
		
		verify(personService).searchForPeople(isNull(), tokenOrListCaptor.capture(), isNull(), isNull(), isNull(), isNull(),
		    isNull(), isNull());
		
		assertThat(tokenOrListCaptor.getValue(), notNullValue());
		assertThat(tokenOrListCaptor.getValue().getValuesAsQueryTokens(), not(empty()));
		assertThat(tokenOrListCaptor.getValue().getValuesAsQueryTokens().get(0).getValue(), equalTo(PERSON_GENDER));
	}
	
	@Test
	public void shouldGetPersonByBirthDate() throws Exception {
		verifyUri("/Person/?birthdate=eq1975-02-02");
		
		verify(personService).searchForPeople(isNull(), isNull(), dateRangeCaptor.capture(), isNull(), isNull(), isNull(),
		    isNull(), isNull());
		assertThat(dateRangeCaptor.getValue(), notNullValue());
		
		Calendar calendar = Calendar.getInstance();
		calendar.set(1975, 1, 2);
		
		assertThat(dateRangeCaptor.getValue().getLowerBound().getValue(),
		    equalTo(DateUtils.truncate(calendar.getTime(), Calendar.DATE)));
		assertThat(dateRangeCaptor.getValue().getUpperBound().getValue(),
		    equalTo(DateUtils.truncate(calendar.getTime(), Calendar.DATE)));
	}
	
	@Test
	public void shouldGetPersonByBirthDateGreaterThanOrEqualTo() throws Exception {
		verifyUri("/Person/?birthdate=ge1975-02-02");
		
		verify(personService).searchForPeople(isNull(), isNull(), dateRangeCaptor.capture(), isNull(), isNull(), isNull(),
		    isNull(), isNull());
		assertThat(dateRangeCaptor.getValue(), notNullValue());
		
		Calendar calendar = Calendar.getInstance();
		calendar.set(1975, 1, 2);
		
		assertThat(dateRangeCaptor.getValue().getLowerBound().getValue(),
		    equalTo(DateUtils.truncate(calendar.getTime(), Calendar.DATE)));
		assertThat(dateRangeCaptor.getValue().getUpperBound(), nullValue());
	}
	
	@Test
	public void shouldGetPersonByBirthDateGreaterThan() throws Exception {
		verifyUri("/Person/?birthdate=gt1975-02-02");
		
		verify(personService).searchForPeople(isNull(), isNull(), dateRangeCaptor.capture(), isNull(), isNull(), isNull(),
		    isNull(), isNull());
		assertThat(dateRangeCaptor.getValue(), notNullValue());
		
		Calendar calendar = Calendar.getInstance();
		calendar.set(1975, 1, 2);
		
		assertThat(dateRangeCaptor.getValue().getLowerBound().getValue(),
		    equalTo(DateUtils.truncate(calendar.getTime(), Calendar.DATE)));
		assertThat(dateRangeCaptor.getValue().getUpperBound(), nullValue());
	}
	
	@Test
	public void shouldGetPersonByBirthDateLessThanOrEqualTo() throws Exception {
		verifyUri("/Person/?birthdate=le1975-02-02");
		
		verify(personService).searchForPeople(isNull(), isNull(), dateRangeCaptor.capture(), isNull(), isNull(), isNull(),
		    isNull(), isNull());
		assertThat(dateRangeCaptor.getValue(), notNullValue());
		
		Calendar calendar = Calendar.getInstance();
		calendar.set(1975, 1, 2);
		
		assertThat(dateRangeCaptor.getValue().getLowerBound(), nullValue());
		assertThat(dateRangeCaptor.getValue().getUpperBound().getValue(),
		    equalTo(DateUtils.truncate(calendar.getTime(), Calendar.DATE)));
	}
	
	@Test
	public void shouldGetPersonByBirthDateLessThan() throws Exception {
		verifyUri("/Person/?birthdate=lt1975-02-02");
		
		verify(personService).searchForPeople(isNull(), isNull(), dateRangeCaptor.capture(), isNull(), isNull(), isNull(),
		    isNull(), isNull());
		assertThat(dateRangeCaptor.getValue(), notNullValue());
		
		Calendar calendar = Calendar.getInstance();
		calendar.set(1975, 1, 2);
		
		assertThat(dateRangeCaptor.getValue().getLowerBound(), nullValue());
		assertThat(dateRangeCaptor.getValue().getUpperBound().getValue(),
		    equalTo(DateUtils.truncate(calendar.getTime(), Calendar.DATE)));
	}
	
	@Test
	public void shouldGetPersonByBirthDateBetween() throws Exception {
		verifyUri("/Person/?birthdate=ge1975-02-02&birthdate=le1980-02-02");
		
		verify(personService).searchForPeople(isNull(), isNull(), dateRangeCaptor.capture(), isNull(), isNull(), isNull(),
		    isNull(), isNull());
		
		Calendar lowerBound = Calendar.getInstance();
		lowerBound.set(1975, 1, 2);
		Calendar upperBound = Calendar.getInstance();
		upperBound.set(1980, 1, 2);
		
		assertThat(dateRangeCaptor.getValue(), notNullValue());
		assertThat(dateRangeCaptor.getValue().getLowerBound().getValue(),
		    equalTo(DateUtils.truncate(lowerBound.getTime(), Calendar.DATE)));
		assertThat(dateRangeCaptor.getValue().getUpperBound().getValue(),
		    equalTo(DateUtils.truncate(upperBound.getTime(), Calendar.DATE)));
	}
	
	@Test
	public void shouldGetPersonByCity() throws Exception {
		verifyUri(String.format("/Person/?address-city=%s", ADDRESS_FIELD));
		
		verify(personService).searchForPeople(isNull(), isNull(), isNull(), stringOrListCaptor.capture(), isNull(), isNull(),
		    isNull(), isNull());
		
		assertThat(stringOrListCaptor.getValue(), notNullValue());
		assertThat(stringOrListCaptor.getValue().getValuesAsQueryTokens(), not(empty()));
		assertThat(stringOrListCaptor.getValue().getValuesAsQueryTokens().get(0).getValue(), equalTo(ADDRESS_FIELD));
	}
	
	@Test
	public void shouldGetPersonByState() throws Exception {
		verifyUri(String.format("/Person/?address-state=%s", ADDRESS_FIELD));
		
		verify(personService).searchForPeople(isNull(), isNull(), isNull(), isNull(), stringOrListCaptor.capture(), isNull(),
		    isNull(), isNull());
		
		assertThat(stringOrListCaptor.getValue(), notNullValue());
		assertThat(stringOrListCaptor.getValue().getValuesAsQueryTokens(), not(empty()));
		assertThat(stringOrListCaptor.getValue().getValuesAsQueryTokens().get(0).getValue(), equalTo(ADDRESS_FIELD));
	}
	
	@Test
	public void shouldGetPersonByPostalCode() throws Exception {
		verifyUri(String.format("/Person/?address-postalcode=%s", POSTAL_CODE));
		
		verify(personService).searchForPeople(isNull(), isNull(), isNull(), isNull(), isNull(), stringOrListCaptor.capture(),
		    isNull(), isNull());
		
		assertThat(stringOrListCaptor.getValue(), notNullValue());
		assertThat(stringOrListCaptor.getValue().getValuesAsQueryTokens(), not(empty()));
		assertThat(stringOrListCaptor.getValue().getValuesAsQueryTokens().get(0).getValue(), equalTo(POSTAL_CODE));
	}
	
	@Test
	public void shouldGetPersonByCountry() throws Exception {
		verifyUri(String.format("/Person/?address-country=%s", ADDRESS_FIELD));
		
		verify(personService).searchForPeople(isNull(), isNull(), isNull(), isNull(), isNull(), isNull(),
		    stringOrListCaptor.capture(), isNull());
		
		assertThat(stringOrListCaptor.getValue(), notNullValue());
		assertThat(stringOrListCaptor.getValue().getValuesAsQueryTokens(), not(empty()));
		assertThat(stringOrListCaptor.getValue().getValuesAsQueryTokens().get(0).getValue(), equalTo(ADDRESS_FIELD));
	}
	
	@Test
	public void shouldGetPersonByComplexQuery() throws Exception {
		verifyUri(String.format("/Person/?name=%s&gender=%s&birthdate=eq1975-02-02", PERSON_NAME, PERSON_GENDER));
		
		verify(personService).searchForPeople(stringOrListCaptor.capture(), tokenOrListCaptor.capture(),
		    dateRangeCaptor.capture(), isNull(), isNull(), isNull(), isNull(), isNull());
		
		assertThat(stringOrListCaptor.getValue(), notNullValue());
		assertThat(stringOrListCaptor.getValue().getValuesAsQueryTokens(), not(empty()));
		assertThat(stringOrListCaptor.getValue().getValuesAsQueryTokens().get(0).getValue(), equalTo(PERSON_NAME));
		
		assertThat(tokenOrListCaptor.getValue(), notNullValue());
		assertThat(tokenOrListCaptor.getValue().getValuesAsQueryTokens(), not(empty()));
		assertThat(tokenOrListCaptor.getValue().getValuesAsQueryTokens().get(0).getValue(), equalTo(PERSON_GENDER));
		
		assertThat(dateRangeCaptor.getValue(), notNullValue());
		
		Calendar calendar = Calendar.getInstance();
		calendar.set(1975, 1, 2);
		
		assertThat(dateRangeCaptor.getValue().getLowerBound().getValue(),
		    equalTo(DateUtils.truncate(calendar.getTime(), Calendar.DATE)));
		assertThat(dateRangeCaptor.getValue().getUpperBound().getValue(),
		    equalTo(DateUtils.truncate(calendar.getTime(), Calendar.DATE)));
	}
	
	private void verifyUri(String uri) throws Exception {
		Person person = new Person();
		person.setId(PERSON_UUID);
		when(personService.searchForPeople(any(), any(), any(), any(), any(), any(), any(), any()))
		        .thenReturn(Collections.singletonList(person));
		
		MockHttpServletResponse response = get(uri).accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), equalTo(FhirMediaTypes.JSON.toString()));
		
		Bundle results = readBundleResponse(response);
		assertThat(results.hasEntry(), is(true));
		assertThat(results.getEntry().get(0).getResource(), notNullValue());
		assertThat(results.getEntry().get(0).getResource().getIdElement().getIdPart(), equalTo(PERSON_UUID));
	}
	
	@Test
	public void shouldVerifyGetPersonHistoryByIdUri() throws Exception {
		Person person = new Person();
		person.setId(PERSON_UUID);
		when(personService.getPersonByUuid(PERSON_UUID)).thenReturn(person);
		
		MockHttpServletResponse response = getPersonHistoryByIdRequest();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), equalTo(BaseFhirResourceProviderTest.FhirMediaTypes.JSON.toString()));
	}
	
	@Test
	public void shouldGetPersonHistoryById() throws IOException, ServletException {
		Provenance provenance = new Provenance();
		provenance.setId(new IdType(FhirUtils.uniqueUuid()));
		provenance.setRecorded(new Date());
		provenance.setActivity(new CodeableConcept().addCoding(
		    new Coding().setCode("CREATE").setSystem(FhirConstants.FHIR_TERMINOLOGY_DATA_OPERATION).setDisplay("create")));
		provenance.addAgent(new Provenance.ProvenanceAgentComponent()
		        .setType(new CodeableConcept().addCoding(new Coding().setCode(AUT).setDisplay(AUTHOR)
		                .setSystem(FhirConstants.FHIR_TERMINOLOGY_PROVENANCE_PARTICIPANT_TYPE)))
		        .addRole(new CodeableConcept().addCoding(
		            new Coding().setCode("").setDisplay("").setSystem(FhirConstants.FHIR_TERMINOLOGY_PARTICIPATION_TYPE))));
		Person person = new Person();
		person.setId(PERSON_UUID);
		person.addContained(provenance);
		
		when(personService.getPersonByUuid(PERSON_UUID)).thenReturn(person);
		
		MockHttpServletResponse response = getPersonHistoryByIdRequest();
		
		Bundle results = readBundleResponse(response);
		assertThat(results, notNullValue());
		assertThat(results.hasEntry(), is(true));
		assertThat(results.getEntry().get(0).getResource(), notNullValue());
		assertThat(results.getEntry().get(0).getResource().getResourceType().name(),
		    equalTo(Provenance.class.getSimpleName()));
		
	}
	
	@Test
	public void getPersonHistoryById_shouldReturnBundleWithEmptyEntriesIfPersonContainedIsEmpty() throws Exception {
		Person person = new Person();
		person.setId(PERSON_UUID);
		person.setContained(new ArrayList<>());
		when(personService.getPersonByUuid(PERSON_UUID)).thenReturn(person);
		
		MockHttpServletResponse response = getPersonHistoryByIdRequest();
		Bundle results = readBundleResponse(response);
		assertThat(results.hasEntry(), is(false));
	}
	
	@Test
	public void getPersonHistoryById_shouldReturn404IfObservationIdIsWrong() throws Exception {
		MockHttpServletResponse response = get("/Person/" + WRONG_PERSON_UUID + "/_history")
		        .accept(BaseFhirResourceProviderTest.FhirMediaTypes.JSON).go();
		
		assertThat(response, isNotFound());
	}
	
	private MockHttpServletResponse getPersonHistoryByIdRequest() throws IOException, ServletException {
		return get("/Person/" + PERSON_UUID + "/_history").accept(BaseFhirResourceProviderTest.FhirMediaTypes.JSON).go();
	}
	
}
