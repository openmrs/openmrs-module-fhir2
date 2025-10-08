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
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import javax.servlet.ServletException;

import java.util.Calendar;
import java.util.Collections;
import java.util.HashSet;

import ca.uhn.fhir.model.api.Include;
import ca.uhn.fhir.rest.param.DateRangeParam;
import ca.uhn.fhir.rest.param.StringAndListParam;
import ca.uhn.fhir.rest.param.TokenAndListParam;
import lombok.AccessLevel;
import lombok.Getter;
import org.apache.commons.lang3.time.DateUtils;
import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.dstu3.model.RelatedPerson;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.api.FhirRelatedPersonService;
import org.openmrs.module.fhir2.api.search.param.RelatedPersonSearchParams;
import org.openmrs.module.fhir2.providers.r4.MockIBundleProvider;
import org.springframework.mock.web.MockHttpServletResponse;

@RunWith(MockitoJUnitRunner.class)
public class RelatedPersonFhirResourceProviderWebTest extends BaseFhirR3ResourceProviderWebTest<RelatedPersonFhirResourceProvider, RelatedPerson> {
	
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
	private ArgumentCaptor<RelatedPersonSearchParams> relatedPersonSearchParamsArgumentCaptor;
	
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
		org.hl7.fhir.r4.model.RelatedPerson relatedPerson = new org.hl7.fhir.r4.model.RelatedPerson();
		relatedPerson.setId(RELATED_PERSON_UUID);
		when(relatedPersonService.get(RELATED_PERSON_UUID)).thenReturn(relatedPerson);
		
		MockHttpServletResponse response = get("/RelatedPerson/" + RELATED_PERSON_UUID).accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), containsString(FhirMediaTypes.JSON.toString()));
		
		RelatedPerson resource = readResponse(response);
		assertThat(resource.getIdElement().getIdPart(), equalTo(RELATED_PERSON_UUID));
	}
	
	@Test
	public void shouldReturn404IfRelatedPersonNotFound() throws Exception {
		when(relatedPersonService.get(WRONG_RELATED_PERSON_UUID)).thenReturn(null);
		
		MockHttpServletResponse response = get("/RelatedPerson/" + WRONG_RELATED_PERSON_UUID).accept(FhirMediaTypes.JSON)
		        .go();
		
		assertThat(response, isNotFound());
	}
	
	@Test
	public void shouldGetRelatedPersonByName() throws Exception {
		verifyUri(String.format("/RelatedPerson/?name=%s", PERSON_NAME));
		
		verify(relatedPersonService).searchForRelatedPeople(relatedPersonSearchParamsArgumentCaptor.capture());
		
		assertThat(relatedPersonSearchParamsArgumentCaptor.getValue(), notNullValue());
		assertThat(relatedPersonSearchParamsArgumentCaptor.getValue().getName().getValuesAsQueryTokens(), not(empty()));
		assertThat(relatedPersonSearchParamsArgumentCaptor.getValue().getName().getValuesAsQueryTokens().get(0)
		        .getValuesAsQueryTokens().get(0).getValue(),
		    equalTo(PERSON_NAME));
	}
	
	@Test
	public void shouldGetRelatedPersonByGender() throws Exception {
		verifyUri(String.format("/RelatedPerson/?gender=%s", PERSON_GENDER));
		
		verify(relatedPersonService).searchForRelatedPeople(relatedPersonSearchParamsArgumentCaptor.capture());
		
		assertThat(relatedPersonSearchParamsArgumentCaptor.getValue(), notNullValue());
		assertThat(relatedPersonSearchParamsArgumentCaptor.getValue().getGender().getValuesAsQueryTokens(), not(empty()));
		assertThat(relatedPersonSearchParamsArgumentCaptor.getValue().getGender().getValuesAsQueryTokens().get(0)
		        .getValuesAsQueryTokens().get(0).getValue(),
		    equalTo(PERSON_GENDER));
	}
	
	@Test
	public void shouldGetRelatedPersonByBirthDate() throws Exception {
		verifyUri("/RelatedPerson/?birthdate=eq1975-02-02");
		
		verify(relatedPersonService).searchForRelatedPeople(relatedPersonSearchParamsArgumentCaptor.capture());
		assertThat(relatedPersonSearchParamsArgumentCaptor.getValue(), notNullValue());
		
		Calendar calendar = Calendar.getInstance();
		calendar.set(1975, Calendar.FEBRUARY, 2);
		
		assertThat(relatedPersonSearchParamsArgumentCaptor.getValue().getBirthDate().getLowerBound().getValue(),
		    equalTo(DateUtils.truncate(calendar.getTime(), Calendar.DATE)));
		assertThat(relatedPersonSearchParamsArgumentCaptor.getValue().getBirthDate().getUpperBound().getValue(),
		    equalTo(DateUtils.truncate(calendar.getTime(), Calendar.DATE)));
	}
	
	@Test
	public void shouldGetRelatedPersonByBirthDateGreaterThanOrEqualTo() throws Exception {
		verifyUri("/RelatedPerson/?birthdate=ge1975-02-02");
		
		verify(relatedPersonService).searchForRelatedPeople(relatedPersonSearchParamsArgumentCaptor.capture());
		assertThat(relatedPersonSearchParamsArgumentCaptor.getValue(), notNullValue());
		
		Calendar calendar = Calendar.getInstance();
		calendar.set(1975, Calendar.FEBRUARY, 2);
		
		assertThat(relatedPersonSearchParamsArgumentCaptor.getValue().getBirthDate().getLowerBound().getValue(),
		    equalTo(DateUtils.truncate(calendar.getTime(), Calendar.DATE)));
		assertThat(relatedPersonSearchParamsArgumentCaptor.getValue().getBirthDate().getUpperBound(), nullValue());
	}
	
	@Test
	public void shouldGetPersonByBirthDateGreaterThan() throws Exception {
		verifyUri("/RelatedPerson/?birthdate=gt1975-02-02");
		
		verify(relatedPersonService).searchForRelatedPeople(relatedPersonSearchParamsArgumentCaptor.capture());
		assertThat(relatedPersonSearchParamsArgumentCaptor.getValue(), notNullValue());
		
		Calendar calendar = Calendar.getInstance();
		calendar.set(1975, Calendar.FEBRUARY, 2);
		
		assertThat(relatedPersonSearchParamsArgumentCaptor.getValue().getBirthDate().getLowerBound().getValue(),
		    equalTo(DateUtils.truncate(calendar.getTime(), Calendar.DATE)));
		assertThat(relatedPersonSearchParamsArgumentCaptor.getValue().getBirthDate().getUpperBound(), nullValue());
	}
	
	@Test
	public void shouldGetRelatedPersonByBirthDateLessThanOrEqualTo() throws Exception {
		verifyUri("/RelatedPerson/?birthdate=le1975-02-02");
		
		verify(relatedPersonService).searchForRelatedPeople(relatedPersonSearchParamsArgumentCaptor.capture());
		assertThat(relatedPersonSearchParamsArgumentCaptor.getValue(), notNullValue());
		
		Calendar calendar = Calendar.getInstance();
		calendar.set(1975, Calendar.FEBRUARY, 2);
		
		assertThat(relatedPersonSearchParamsArgumentCaptor.getValue().getBirthDate().getLowerBound(), nullValue());
		assertThat(relatedPersonSearchParamsArgumentCaptor.getValue().getBirthDate().getUpperBound().getValue(),
		    equalTo(DateUtils.truncate(calendar.getTime(), Calendar.DATE)));
	}
	
	@Test
	public void shouldGetRelatedPersonByBirthDateLessThan() throws Exception {
		verifyUri("/RelatedPerson/?birthdate=lt1975-02-02");
		
		verify(relatedPersonService).searchForRelatedPeople(relatedPersonSearchParamsArgumentCaptor.capture());
		assertThat(relatedPersonSearchParamsArgumentCaptor.getValue(), notNullValue());
		
		Calendar calendar = Calendar.getInstance();
		calendar.set(1975, Calendar.FEBRUARY, 2);
		
		assertThat(relatedPersonSearchParamsArgumentCaptor.getValue().getBirthDate().getLowerBound(), nullValue());
		assertThat(relatedPersonSearchParamsArgumentCaptor.getValue().getBirthDate().getUpperBound().getValue(),
		    equalTo(DateUtils.truncate(calendar.getTime(), Calendar.DATE)));
	}
	
	@Test
	public void shouldGetPersonByBirthDateBetween() throws Exception {
		verifyUri("/RelatedPerson/?birthdate=ge1975-02-02&birthdate=le1980-02-02");
		
		verify(relatedPersonService).searchForRelatedPeople(relatedPersonSearchParamsArgumentCaptor.capture());
		
		Calendar lowerBound = Calendar.getInstance();
		lowerBound.set(1975, Calendar.FEBRUARY, 2);
		Calendar upperBound = Calendar.getInstance();
		upperBound.set(1980, Calendar.FEBRUARY, 2);
		
		assertThat(relatedPersonSearchParamsArgumentCaptor.getValue(), notNullValue());
		assertThat(relatedPersonSearchParamsArgumentCaptor.getValue().getBirthDate().getLowerBound().getValue(),
		    equalTo(DateUtils.truncate(lowerBound.getTime(), Calendar.DATE)));
		assertThat(relatedPersonSearchParamsArgumentCaptor.getValue().getBirthDate().getUpperBound().getValue(),
		    equalTo(DateUtils.truncate(upperBound.getTime(), Calendar.DATE)));
	}
	
	@Test
	public void shouldGetRelatedPersonByCity() throws Exception {
		verifyUri(String.format("/RelatedPerson/?address-city=%s", ADDRESS_FIELD));
		
		verify(relatedPersonService).searchForRelatedPeople(relatedPersonSearchParamsArgumentCaptor.capture());
		
		assertThat(relatedPersonSearchParamsArgumentCaptor.getValue(), notNullValue());
		assertThat(relatedPersonSearchParamsArgumentCaptor.getValue().getCity().getValuesAsQueryTokens(), not(empty()));
		assertThat(relatedPersonSearchParamsArgumentCaptor.getValue().getCity().getValuesAsQueryTokens().get(0)
		        .getValuesAsQueryTokens().get(0).getValue(),
		    equalTo(ADDRESS_FIELD));
	}
	
	@Test
	public void shouldGetRelatedPersonByState() throws Exception {
		verifyUri(String.format("/RelatedPerson/?address-state=%s", ADDRESS_FIELD));
		
		verify(relatedPersonService).searchForRelatedPeople(relatedPersonSearchParamsArgumentCaptor.capture());
		
		assertThat(relatedPersonSearchParamsArgumentCaptor.getValue(), notNullValue());
		assertThat(relatedPersonSearchParamsArgumentCaptor.getValue().getState().getValuesAsQueryTokens(), not(empty()));
		assertThat(relatedPersonSearchParamsArgumentCaptor.getValue().getState().getValuesAsQueryTokens().get(0)
		        .getValuesAsQueryTokens().get(0).getValue(),
		    equalTo(ADDRESS_FIELD));
	}
	
	@Test
	public void shouldGetRelatedPersonByPostalCode() throws Exception {
		verifyUri(String.format("/RelatedPerson/?address-postalcode=%s", POSTAL_CODE));
		
		verify(relatedPersonService).searchForRelatedPeople(relatedPersonSearchParamsArgumentCaptor.capture());
		
		assertThat(relatedPersonSearchParamsArgumentCaptor.getValue(), notNullValue());
		assertThat(relatedPersonSearchParamsArgumentCaptor.getValue().getPostalCode().getValuesAsQueryTokens(),
		    not(empty()));
		assertThat(relatedPersonSearchParamsArgumentCaptor.getValue().getPostalCode().getValuesAsQueryTokens().get(0)
		        .getValuesAsQueryTokens().get(0).getValue(),
		    equalTo(POSTAL_CODE));
	}
	
	@Test
	public void shouldGetRelatedPersonByCountry() throws Exception {
		verifyUri(String.format("/RelatedPerson/?address-country=%s", ADDRESS_FIELD));
		
		verify(relatedPersonService).searchForRelatedPeople(relatedPersonSearchParamsArgumentCaptor.capture());
		
		assertThat(relatedPersonSearchParamsArgumentCaptor.getValue(), notNullValue());
		assertThat(relatedPersonSearchParamsArgumentCaptor.getValue().getCountry().getValuesAsQueryTokens(), not(empty()));
		assertThat(relatedPersonSearchParamsArgumentCaptor.getValue().getCountry().getValuesAsQueryTokens().get(0)
		        .getValuesAsQueryTokens().get(0).getValue(),
		    equalTo(ADDRESS_FIELD));
	}
	
	@Test
	public void shouldGetRelatedPersonByUUID() throws Exception {
		verifyUri(String.format("/RelatedPerson?_id=%s", RELATED_PERSON_UUID));
		
		verify(relatedPersonService).searchForRelatedPeople(relatedPersonSearchParamsArgumentCaptor.capture());
		
		assertThat(relatedPersonSearchParamsArgumentCaptor.getValue(), notNullValue());
		assertThat(relatedPersonSearchParamsArgumentCaptor.getValue().getId().getValuesAsQueryTokens(), not(empty()));
		assertThat(relatedPersonSearchParamsArgumentCaptor.getValue().getId().getValuesAsQueryTokens().get(0)
		        .getValuesAsQueryTokens().get(0).getValue(),
		    equalTo(RELATED_PERSON_UUID));
	}
	
	@Test
	public void shouldGetRelatedPersonByLastUpdatedDate() throws Exception {
		verifyUri(String.format("/RelatedPerson?_lastUpdated=%s", LAST_UPDATED_DATE));
		
		verify(relatedPersonService).searchForRelatedPeople(relatedPersonSearchParamsArgumentCaptor.capture());
		
		assertThat(relatedPersonSearchParamsArgumentCaptor.getValue(), notNullValue());
		
		Calendar calendar = Calendar.getInstance();
		calendar.set(2020, Calendar.SEPTEMBER, 3);
		
		assertThat(relatedPersonSearchParamsArgumentCaptor.getValue().getLastUpdated().getLowerBound().getValue(),
		    equalTo(DateUtils.truncate(calendar.getTime(), Calendar.DATE)));
		assertThat(relatedPersonSearchParamsArgumentCaptor.getValue().getLastUpdated().getUpperBound().getValue(),
		    equalTo(DateUtils.truncate(calendar.getTime(), Calendar.DATE)));
	}
	
	@Test
	public void shouldGetPersonByComplexQuery() throws Exception {
		verifyUri(String.format("/RelatedPerson/?name=%s&gender=%s&birthdate=eq1975-02-02", PERSON_NAME, PERSON_GENDER));
		
		verify(relatedPersonService).searchForRelatedPeople(relatedPersonSearchParamsArgumentCaptor.capture());
		
		assertThat(relatedPersonSearchParamsArgumentCaptor.getValue(), notNullValue());
		assertThat(relatedPersonSearchParamsArgumentCaptor.getValue().getName().getValuesAsQueryTokens(), not(empty()));
		assertThat(relatedPersonSearchParamsArgumentCaptor.getValue().getName().getValuesAsQueryTokens().get(0)
		        .getValuesAsQueryTokens().get(0).getValue(),
		    equalTo(PERSON_NAME));
		
		assertThat(relatedPersonSearchParamsArgumentCaptor.getValue(), notNullValue());
		assertThat(relatedPersonSearchParamsArgumentCaptor.getValue().getGender().getValuesAsQueryTokens(), not(empty()));
		assertThat(relatedPersonSearchParamsArgumentCaptor.getValue().getGender().getValuesAsQueryTokens().get(0)
		        .getValuesAsQueryTokens().get(0).getValue(),
		    equalTo(PERSON_GENDER));
		
		assertThat(relatedPersonSearchParamsArgumentCaptor.getValue(), notNullValue());
		
		Calendar calendar = Calendar.getInstance();
		calendar.set(1975, Calendar.FEBRUARY, 2);
		
		assertThat(relatedPersonSearchParamsArgumentCaptor.getValue().getBirthDate().getLowerBound().getValue(),
		    equalTo(DateUtils.truncate(calendar.getTime(), Calendar.DATE)));
		assertThat(relatedPersonSearchParamsArgumentCaptor.getValue().getBirthDate().getUpperBound().getValue(),
		    equalTo(DateUtils.truncate(calendar.getTime(), Calendar.DATE)));
	}
	
	@Test
	public void shouldAddPatientsToResultListWhenIncluded() throws Exception {
		verifyUri("/RelatedPerson?_include=RelatedPerson:patient");
		
		verify(relatedPersonService).searchForRelatedPeople(relatedPersonSearchParamsArgumentCaptor.capture());
		
		assertThat(relatedPersonSearchParamsArgumentCaptor.getValue(), notNullValue());
		assertThat(relatedPersonSearchParamsArgumentCaptor.getValue().getIncludes().size(), equalTo(1));
		assertThat(relatedPersonSearchParamsArgumentCaptor.getValue().getIncludes().iterator().next().getParamName(),
		    equalTo(FhirConstants.INCLUDE_PATIENT_PARAM));
		assertThat(relatedPersonSearchParamsArgumentCaptor.getValue().getIncludes().iterator().next().getParamType(),
		    equalTo(FhirConstants.RELATED_PERSON));
	}
	
	private void verifyUri(String uri) throws Exception {
		RelatedPerson relatedPerson = new RelatedPerson();
		relatedPerson.setId(RELATED_PERSON_UUID);
		when(relatedPersonService.searchForRelatedPeople(any()))
		        .thenReturn(new MockIBundleProvider<>(Collections.singletonList(relatedPerson), 10, 1));
		
		MockHttpServletResponse response = get(uri).accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), containsString(FhirMediaTypes.JSON.toString()));
		
		Bundle results = readBundleResponse(response);
		assertThat(results.hasEntry(), is(true));
		assertThat(results.getEntry().get(0).getResource(), notNullValue());
		assertThat(results.getEntry().get(0).getResource().getIdElement().getIdPart(), equalTo(RELATED_PERSON_UUID));
	}
	
}
