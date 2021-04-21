/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.fhir2.providers.r4;

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
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import lombok.AccessLevel;
import lombok.Getter;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.hamcrest.MatcherAssert;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.RelatedPerson;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.api.FhirRelatedPersonService;
import org.springframework.mock.web.MockHttpServletResponse;

@RunWith(MockitoJUnitRunner.class)
public class RelatedPersonFhirResourceProviderWebTest extends BaseFhirR4ResourceProviderWebTest<RelatedPersonFhirResourceProvider, RelatedPerson> {
	
	private static final String JSON_CREATE_RELATED_PERSON_PATH = "org/openmrs/module/fhir2/providers/RelatedPersonWebTest_create.json";
	
	private static final String JSON_UPDATE_RELATED_PERSON_PATH = "org/openmrs/module/fhir2/providers/RelatedPersonWebTest_update.json";
	
	private static final String JSON_UPDATE_RELATED_PERSON_NO_ID_PATH = "org/openmrs/module/fhir2/providers/RelatedPersonWebTest_updateWithoutId.json";
	
	private static final String JSON_UPDATE_RELATED_PERSON_WRONG_ID_PATH = "org/openmrs/module/fhir2/providers/RelatedPersonWebTest_updateWrongId.json";
	
	private static final String RELATED_PERSON_UUID = "8a849d5e-6011-4279-a124-40ada5a687de";
	
	private static final String WRONG_RELATED_PERSON_UUID = "9bf0d1ac-62a8-4440-a5a1-eb1015a7cc65";
	
	private static final String PERSON_NAME = "Hannibal Lector";
	
	private static final String PERSON_GENDER = "male";
	
	private static final String ADDRESS_FIELD = "Washington";
	
	private static final String POSTAL_CODE = "98136";
	
	private static final String LAST_UPDATED_DATE = "eq2020-09-03";
	
	@Mock
	private FhirRelatedPersonService relatedPersonService;
	
	@Getter(AccessLevel.PUBLIC)
	private RelatedPersonFhirResourceProvider resourceProvider;
	
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
		resourceProvider = new RelatedPersonFhirResourceProvider();
		resourceProvider.setRelatedPersonService(relatedPersonService);
		super.setup();
	}
	
	@Test
	public void shouldReturnRelatedPersonById() throws Exception {
		RelatedPerson relatedPerson = new RelatedPerson();
		relatedPerson.setId(RELATED_PERSON_UUID);
		when(relatedPersonService.get(RELATED_PERSON_UUID)).thenReturn(relatedPerson);
		
		MockHttpServletResponse response = get("/RelatedPerson/" + RELATED_PERSON_UUID).accept(FhirMediaTypes.JSON).go();
		
		MatcherAssert.assertThat(response, isOk());
		MatcherAssert.assertThat(response.getContentType(), equalTo(FhirMediaTypes.JSON.toString()));
		
		RelatedPerson resource = readResponse(response);
		assertThat(resource.getIdElement().getIdPart(), equalTo(RELATED_PERSON_UUID));
	}
	
	@Test
	public void shouldReturn404IfRelatedPersonNotFound() throws Exception {
		when(relatedPersonService.get(WRONG_RELATED_PERSON_UUID)).thenReturn(null);
		
		MockHttpServletResponse response = get("/RelatedPerson/" + WRONG_RELATED_PERSON_UUID).accept(FhirMediaTypes.JSON)
		        .go();
		
		MatcherAssert.assertThat(response, isNotFound());
	}
	
	@Test
	public void shouldGetRelatedPersonByName() throws Exception {
		verifyUri(String.format("/RelatedPerson/?name=%s", PERSON_NAME));
		
		verify(relatedPersonService).searchForRelatedPeople(stringAndListCaptor.capture(), isNull(), isNull(), isNull(),
		    isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull());
		
		assertThat(stringAndListCaptor.getValue(), notNullValue());
		assertThat(stringAndListCaptor.getValue().getValuesAsQueryTokens(), not(empty()));
		assertThat(stringAndListCaptor.getValue().getValuesAsQueryTokens().get(0).getValuesAsQueryTokens().get(0).getValue(),
		    equalTo(PERSON_NAME));
	}
	
	@Test
	public void shouldGetRelatedPersonByGender() throws Exception {
		verifyUri(String.format("/RelatedPerson/?gender=%s", PERSON_GENDER));
		
		verify(relatedPersonService).searchForRelatedPeople(isNull(), tokenAndListCaptor.capture(), isNull(), isNull(),
		    isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull());
		
		assertThat(tokenAndListCaptor.getValue(), notNullValue());
		assertThat(tokenAndListCaptor.getValue().getValuesAsQueryTokens(), not(empty()));
		assertThat(tokenAndListCaptor.getValue().getValuesAsQueryTokens().get(0).getValuesAsQueryTokens().get(0).getValue(),
		    equalTo(PERSON_GENDER));
	}
	
	@Test
	public void shouldGetRelatedPersonByBirthDate() throws Exception {
		verifyUri("/RelatedPerson/?birthdate=eq1975-02-02");
		
		verify(relatedPersonService).searchForRelatedPeople(isNull(), isNull(), dateRangeCaptor.capture(), isNull(),
		    isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull());
		assertThat(dateRangeCaptor.getValue(), notNullValue());
		
		Calendar calendar = Calendar.getInstance();
		calendar.set(1975, Calendar.FEBRUARY, 2);
		
		assertThat(dateRangeCaptor.getValue().getLowerBound().getValue(),
		    equalTo(DateUtils.truncate(calendar.getTime(), Calendar.DATE)));
		assertThat(dateRangeCaptor.getValue().getUpperBound().getValue(),
		    equalTo(DateUtils.truncate(calendar.getTime(), Calendar.DATE)));
	}
	
	@Test
	public void shouldGetRelatedPersonByBirthDateGreaterThanOrEqualTo() throws Exception {
		verifyUri("/RelatedPerson/?birthdate=ge1975-02-02");
		
		verify(relatedPersonService).searchForRelatedPeople(isNull(), isNull(), dateRangeCaptor.capture(), isNull(),
		    isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull());
		assertThat(dateRangeCaptor.getValue(), notNullValue());
		
		Calendar calendar = Calendar.getInstance();
		calendar.set(1975, Calendar.FEBRUARY, 2);
		
		assertThat(dateRangeCaptor.getValue().getLowerBound().getValue(),
		    equalTo(DateUtils.truncate(calendar.getTime(), Calendar.DATE)));
		assertThat(dateRangeCaptor.getValue().getUpperBound(), nullValue());
	}
	
	@Test
	public void shouldGetRelatedPersonByBirthDateGreaterThan() throws Exception {
		verifyUri("/RelatedPerson/?birthdate=gt1975-02-02");
		
		verify(relatedPersonService).searchForRelatedPeople(isNull(), isNull(), dateRangeCaptor.capture(), isNull(),
		    isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull());
		assertThat(dateRangeCaptor.getValue(), notNullValue());
		
		Calendar calendar = Calendar.getInstance();
		calendar.set(1975, Calendar.FEBRUARY, 2);
		
		assertThat(dateRangeCaptor.getValue().getLowerBound().getValue(),
		    equalTo(DateUtils.truncate(calendar.getTime(), Calendar.DATE)));
		assertThat(dateRangeCaptor.getValue().getUpperBound(), nullValue());
	}
	
	@Test
	public void shouldGetRelatedPersonByBirthDateLessThanOrEqualTo() throws Exception {
		verifyUri("/RelatedPerson/?birthdate=le1975-02-02");
		
		verify(relatedPersonService).searchForRelatedPeople(isNull(), isNull(), dateRangeCaptor.capture(), isNull(),
		    isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull());
		assertThat(dateRangeCaptor.getValue(), notNullValue());
		
		Calendar calendar = Calendar.getInstance();
		calendar.set(1975, Calendar.FEBRUARY, 2);
		
		assertThat(dateRangeCaptor.getValue().getLowerBound(), nullValue());
		assertThat(dateRangeCaptor.getValue().getUpperBound().getValue(),
		    equalTo(DateUtils.truncate(calendar.getTime(), Calendar.DATE)));
	}
	
	@Test
	public void shouldGetRelatedPersonByBirthDateLessThan() throws Exception {
		verifyUri("/RelatedPerson/?birthdate=lt1975-02-02");
		
		verify(relatedPersonService).searchForRelatedPeople(isNull(), isNull(), dateRangeCaptor.capture(), isNull(),
		    isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull());
		assertThat(dateRangeCaptor.getValue(), notNullValue());
		
		Calendar calendar = Calendar.getInstance();
		calendar.set(1975, Calendar.FEBRUARY, 2);
		
		assertThat(dateRangeCaptor.getValue().getLowerBound(), nullValue());
		assertThat(dateRangeCaptor.getValue().getUpperBound().getValue(),
		    equalTo(DateUtils.truncate(calendar.getTime(), Calendar.DATE)));
	}
	
	@Test
	public void shouldGetRelatedPersonByBirthDateBetween() throws Exception {
		verifyUri("/RelatedPerson/?birthdate=ge1975-02-02&birthdate=le1980-02-02");
		
		verify(relatedPersonService).searchForRelatedPeople(isNull(), isNull(), dateRangeCaptor.capture(), isNull(),
		    isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull());
		
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
	public void shouldGetRelatedPersonByCity() throws Exception {
		verifyUri(String.format("/RelatedPerson/?address-city=%s", ADDRESS_FIELD));
		
		verify(relatedPersonService).searchForRelatedPeople(isNull(), isNull(), isNull(), stringAndListCaptor.capture(),
		    isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull());
		
		assertThat(stringAndListCaptor.getValue(), notNullValue());
		assertThat(stringAndListCaptor.getValue().getValuesAsQueryTokens(), not(empty()));
		assertThat(stringAndListCaptor.getValue().getValuesAsQueryTokens().get(0).getValuesAsQueryTokens().get(0).getValue(),
		    equalTo(ADDRESS_FIELD));
	}
	
	@Test
	public void shouldGetRelatedPersonByState() throws Exception {
		verifyUri(String.format("/RelatedPerson/?address-state=%s", ADDRESS_FIELD));
		
		verify(relatedPersonService).searchForRelatedPeople(isNull(), isNull(), isNull(), isNull(),
		    stringAndListCaptor.capture(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull());
		
		assertThat(stringAndListCaptor.getValue(), notNullValue());
		assertThat(stringAndListCaptor.getValue().getValuesAsQueryTokens(), not(empty()));
		assertThat(stringAndListCaptor.getValue().getValuesAsQueryTokens().get(0).getValuesAsQueryTokens().get(0).getValue(),
		    equalTo(ADDRESS_FIELD));
	}
	
	@Test
	public void shouldGetRelatedPersonByPostalCode() throws Exception {
		verifyUri(String.format("/RelatedPerson/?address-postalcode=%s", POSTAL_CODE));
		
		verify(relatedPersonService).searchForRelatedPeople(isNull(), isNull(), isNull(), isNull(), isNull(),
		    stringAndListCaptor.capture(), isNull(), isNull(), isNull(), isNull(), isNull());
		
		assertThat(stringAndListCaptor.getValue(), notNullValue());
		assertThat(stringAndListCaptor.getValue().getValuesAsQueryTokens(), not(empty()));
		assertThat(stringAndListCaptor.getValue().getValuesAsQueryTokens().get(0).getValuesAsQueryTokens().get(0).getValue(),
		    equalTo(POSTAL_CODE));
	}
	
	@Test
	public void shouldGetRelatedPersonByCountry() throws Exception {
		verifyUri(String.format("/RelatedPerson/?address-country=%s", ADDRESS_FIELD));
		
		verify(relatedPersonService).searchForRelatedPeople(isNull(), isNull(), isNull(), isNull(), isNull(), isNull(),
		    stringAndListCaptor.capture(), isNull(), isNull(), isNull(), isNull());
		
		assertThat(stringAndListCaptor.getValue(), notNullValue());
		assertThat(stringAndListCaptor.getValue().getValuesAsQueryTokens(), not(empty()));
		assertThat(stringAndListCaptor.getValue().getValuesAsQueryTokens().get(0).getValuesAsQueryTokens().get(0).getValue(),
		    equalTo(ADDRESS_FIELD));
	}
	
	@Test
	public void shouldGetRelatedPersonByUUID() throws Exception {
		verifyUri(String.format("/RelatedPerson?_id=%s", RELATED_PERSON_UUID));
		
		verify(relatedPersonService).searchForRelatedPeople(isNull(), isNull(), isNull(), isNull(), isNull(), isNull(),
		    isNull(), tokenAndListCaptor.capture(), isNull(), isNull(), isNull());
		
		assertThat(tokenAndListCaptor.getValue(), notNullValue());
		assertThat(tokenAndListCaptor.getValue().getValuesAsQueryTokens(), not(empty()));
		assertThat(tokenAndListCaptor.getValue().getValuesAsQueryTokens().get(0).getValuesAsQueryTokens().get(0).getValue(),
		    equalTo(RELATED_PERSON_UUID));
	}
	
	@Test
	public void shouldGetRelatedPersonByLastUpdatedDate() throws Exception {
		verifyUri(String.format("/RelatedPerson?_lastUpdated=%s", LAST_UPDATED_DATE));
		
		verify(relatedPersonService).searchForRelatedPeople(isNull(), isNull(), isNull(), isNull(), isNull(), isNull(),
		    isNull(), isNull(), dateRangeCaptor.capture(), isNull(), isNull());
		
		assertThat(dateRangeCaptor.getValue(), notNullValue());
		
		Calendar calendar = Calendar.getInstance();
		calendar.set(2020, Calendar.SEPTEMBER, 3);
		
		assertThat(dateRangeCaptor.getValue().getLowerBound().getValue(),
		    equalTo(DateUtils.truncate(calendar.getTime(), Calendar.DATE)));
		assertThat(dateRangeCaptor.getValue().getUpperBound().getValue(),
		    equalTo(DateUtils.truncate(calendar.getTime(), Calendar.DATE)));
	}
	
	@Test
	public void shouldGetRelatedPersonByComplexQuery() throws Exception {
		verifyUri(String.format("/RelatedPerson/?name=%s&gender=%s&birthdate=eq1975-02-02", PERSON_NAME, PERSON_GENDER));
		
		verify(relatedPersonService).searchForRelatedPeople(stringAndListCaptor.capture(), tokenAndListCaptor.capture(),
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
		verifyUri("/RelatedPerson?_include=RelatedPerson:patient");
		
		verify(relatedPersonService).searchForRelatedPeople(isNull(), isNull(), isNull(), isNull(), isNull(), isNull(),
		    isNull(), isNull(), isNull(), isNull(), includeArgumentCaptor.capture());
		
		assertThat(includeArgumentCaptor.getValue(), notNullValue());
		assertThat(includeArgumentCaptor.getValue().size(), equalTo(1));
		assertThat(includeArgumentCaptor.getValue().iterator().next().getParamName(),
		    equalTo(FhirConstants.INCLUDE_PATIENT_PARAM));
		assertThat(includeArgumentCaptor.getValue().iterator().next().getParamType(), equalTo(FhirConstants.RELATED_PERSON));
	}
	
	private void verifyUri(String uri) throws Exception {
		RelatedPerson relatedPerson = new RelatedPerson();
		relatedPerson.setId(RELATED_PERSON_UUID);
		when(relatedPersonService.searchForRelatedPeople(any(), any(), any(), any(), any(), any(), any(), any(), any(),
		    any(), any())).thenReturn(new MockIBundleProvider<>(Collections.singletonList(relatedPerson), 10, 1));
		
		MockHttpServletResponse response = get(uri).accept(FhirMediaTypes.JSON).go();
		
		MatcherAssert.assertThat(response, isOk());
		MatcherAssert.assertThat(response.getContentType(), equalTo(FhirMediaTypes.JSON.toString()));
		
		Bundle results = readBundleResponse(response);
		assertThat(results.hasEntry(), is(true));
		assertThat(results.getEntry().get(0).getResource(), notNullValue());
		assertThat(results.getEntry().get(0).getResource().getIdElement().getIdPart(), equalTo(RELATED_PERSON_UUID));
	}
	
	@Test
	public void createRelatedPerson_shouldCreateRelatedPerson() throws Exception {
		String jsonRelatedPerson;
		try (InputStream is = this.getClass().getClassLoader().getResourceAsStream(JSON_CREATE_RELATED_PERSON_PATH)) {
			Objects.requireNonNull(is);
			jsonRelatedPerson = IOUtils.toString(is, StandardCharsets.UTF_8);
		}
		
		RelatedPerson relatedPerson = new RelatedPerson();
		relatedPerson.setId(RELATED_PERSON_UUID);
		
		when(relatedPersonService.create(any(RelatedPerson.class))).thenReturn(relatedPerson);
		
		MockHttpServletResponse response = post("RelatedPerson").jsonContent(jsonRelatedPerson).accept(FhirMediaTypes.JSON)
		        .go();
		
		assertThat(response, isCreated());
	}
	
	@Test
	public void updateRelatedPerson_shouldUpdateExistingRelatedPerson() throws Exception {
		String jsonRelatedPerson;
		try (InputStream is = this.getClass().getClassLoader().getResourceAsStream(JSON_UPDATE_RELATED_PERSON_PATH)) {
			Objects.requireNonNull(is);
			jsonRelatedPerson = IOUtils.toString(is, StandardCharsets.UTF_8);
		}
		
		RelatedPerson relatedPerson = new RelatedPerson();
		relatedPerson.setId(RELATED_PERSON_UUID);
		
		when(relatedPersonService.update(anyString(), any(RelatedPerson.class))).thenReturn(relatedPerson);
		
		MockHttpServletResponse response = put("/RelatedPerson/" + RELATED_PERSON_UUID).jsonContent(jsonRelatedPerson)
		        .accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isOk());
	}
	
	@Test
	public void updateRelatedPerson_shouldThrowErrorForNoId() throws Exception {
		String jsonRelatedPerson;
		try (InputStream is = this.getClass().getClassLoader().getResourceAsStream(JSON_UPDATE_RELATED_PERSON_NO_ID_PATH)) {
			Objects.requireNonNull(is);
			jsonRelatedPerson = IOUtils.toString(is, StandardCharsets.UTF_8);
		}
		
		MockHttpServletResponse response = put("/RelatedPerson/" + RELATED_PERSON_UUID).jsonContent(jsonRelatedPerson)
		        .accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isBadRequest());
		assertThat(response.getContentAsString(), containsStringIgnoringCase("body must contain an ID element for update"));
	}
	
	@Test
	public void updateRelatedPerson_shouldThrowErrorForIdMissMatch() throws Exception {
		String jsonRelatedPerson;
		try (InputStream is = this.getClass().getClassLoader()
		        .getResourceAsStream(JSON_UPDATE_RELATED_PERSON_WRONG_ID_PATH)) {
			Objects.requireNonNull(is);
			jsonRelatedPerson = IOUtils.toString(is, StandardCharsets.UTF_8);
		}
		
		MockHttpServletResponse response = put("/RelatedPerson/" + RELATED_PERSON_UUID).jsonContent(jsonRelatedPerson)
		        .accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isBadRequest());
		assertThat(response.getContentAsString(),
		    containsStringIgnoringCase("body must contain an ID element which matches the request URL"));
	}
	
	@Test
	public void deleteRelatedPerson_shouldDeleteRelatedPerson() throws Exception {
		RelatedPerson relatedPerson = new RelatedPerson();
		relatedPerson.setId(RELATED_PERSON_UUID);
		
		when(relatedPersonService.delete(RELATED_PERSON_UUID)).thenReturn(relatedPerson);
		
		MockHttpServletResponse response = delete("/RelatedPerson/" + RELATED_PERSON_UUID).accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), equalTo(FhirMediaTypes.JSON.toString()));
	}
	
	@Test(expected = ResourceNotFoundException.class)
	public void deleteRelatedPerson_shouldThrowResourceNotFoundException() {
		when(relatedPersonService.delete(WRONG_RELATED_PERSON_UUID)).thenReturn(null);
		
		resourceProvider.deleteRelatedPerson(new IdType().setValue(WRONG_RELATED_PERSON_UUID));
	}
}
