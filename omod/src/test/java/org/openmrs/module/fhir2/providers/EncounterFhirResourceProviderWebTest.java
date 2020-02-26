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
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Calendar;
import java.util.Collections;

import ca.uhn.fhir.rest.param.DateRangeParam;
import ca.uhn.fhir.rest.param.ReferenceParam;
import lombok.AccessLevel;
import lombok.Getter;
import org.apache.commons.lang.time.DateUtils;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Encounter;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openmrs.module.fhir2.api.FhirEncounterService;
import org.openmrs.module.fhir2.web.servlet.BaseFhirResourceProviderTest;
import org.springframework.mock.web.MockHttpServletResponse;

@RunWith(MockitoJUnitRunner.class)
public class EncounterFhirResourceProviderWebTest extends BaseFhirResourceProviderTest<EncounterFhirResourceProvider, Encounter> {
	
	private static final String ENCOUNTER_UUID = "8a849d5e-6011-4279-a124-40ada5a687de";
	
	private static final String WRONG_ENCOUNTER_UUID = "9bf0d1ac-62a8-4440-a5a1-eb1015a7cc65";
	
	private static final String PATIENT_IDENTIFIER = "h43489-h";
	
	private static final String PATIENT_UUID = "d9bc6c12-6adc-4ca6-8bde-441ec1a1c344";
	
	private static final String PATIENT_GIVEN_NAME = "Hannibal";
	
	private static final String PATIENT_FAMILY_NAME = "Sid";
	
	private static final String ENCOUNTER_ADDRESS_CITY = "Boston";
	
	private static final String ENCOUNTER_ADDRESS_COUNTRY = "INDIA";
	
	private static final String ENCOUNTER_ADDRESS_STATE = "MA";
	
	private static final String ENCOUNTER_POSTALCODE = "248001";
	
	private static final String CIEL_URN = "urn:oid:2.16.840.1.113883.3.7201";
	
	private static final String URL_ENCODED_CIEL_URN;
	
	static {
		try {
			URL_ENCODED_CIEL_URN = URLEncoder.encode(CIEL_URN, "utf-8");
		}
		catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}
	
	@Mock
	private FhirEncounterService encounterService;
	
	@Getter(AccessLevel.PUBLIC)
	private EncounterFhirResourceProvider resourceProvider;
	
	@Captor
	private ArgumentCaptor<ReferenceParam> locationCaptor;
	
	@Captor
	private ArgumentCaptor<ReferenceParam> participantCaptor;
	
	@Captor
	private ArgumentCaptor<ReferenceParam> subjectCaptor;
	
	@Captor
	private ArgumentCaptor<DateRangeParam> dateRangeCaptor;
	
	@Before
	@Override
	public void setup() throws Exception {
		resourceProvider = new EncounterFhirResourceProvider();
		resourceProvider.setEncounterService(encounterService);
		super.setup();
	}
	
	@Test
	public void getEncounterByUuid_shouldReturnEncounter() throws Exception {
		Encounter encounter = new Encounter();
		encounter.setId(ENCOUNTER_UUID);
		when(encounterService.getEncounterByUuid(ENCOUNTER_UUID)).thenReturn(encounter);
		
		MockHttpServletResponse response = get("/Encounter/" + ENCOUNTER_UUID).accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), equalTo(FhirMediaTypes.JSON.toString()));
		assertThat(readResponse(response).getIdElement().getIdPart(), equalTo(ENCOUNTER_UUID));
	}
	
	@Test
	public void getEncounterByWrongUuid_shouldReturn404() throws Exception {
		when(encounterService.getEncounterByUuid(WRONG_ENCOUNTER_UUID)).thenReturn(null);
		
		MockHttpServletResponse response = get("/Encounter/" + WRONG_ENCOUNTER_UUID).accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isNotFound());
	}
	
	@Test
	public void shouldGetEncountersBySubjectUuid() throws Exception {
		verifyUri(String.format("/Encounter?subject:Patient=%s", PATIENT_UUID));
		
		verify(encounterService).searchForEncounters(isNull(), isNull(), isNull(), subjectCaptor.capture());
		assertThat(subjectCaptor.getValue(), notNullValue());
		assertThat(subjectCaptor.getValue().getIdPart(), equalTo(PATIENT_UUID));
	}
	
	@Test
	public void shouldGetEncountersByDate() throws Exception {
		verifyUri("/Encounter/?date=ge1975-02-02");
		
		verify(encounterService).searchForEncounters(dateRangeCaptor.capture(), isNull(), isNull(), isNull());
		assertThat(dateRangeCaptor.getValue(), notNullValue());
		
		Calendar calendar = Calendar.getInstance();
		calendar.set(1975, 1, 2);
		
		assertThat(dateRangeCaptor.getValue().getLowerBound().getValue(),
		    equalTo(DateUtils.truncate(calendar.getTime(), Calendar.DATE)));
		assertThat(dateRangeCaptor.getValue().getUpperBound(), nullValue());
	}
	
	@Test
	public void shouldGetEncountersByLocationCityVillage() throws Exception {
		verifyUri(String.format("/Encounter/?location.address-city=%s", ENCOUNTER_ADDRESS_CITY));
		
		verify(encounterService).searchForEncounters(isNull(), locationCaptor.capture(), isNull(), isNull());
		assertThat(locationCaptor.getValue(), notNullValue());
		assertThat(locationCaptor.getValue().getChain(), equalTo("address-city"));
		assertThat(locationCaptor.getValue().getValue(), equalTo(ENCOUNTER_ADDRESS_CITY));
	}
	
	@Test
	public void shouldGetEncountersByLocationState() throws Exception {
		verifyUri(String.format("/Encounter/?location.address-state=%s", ENCOUNTER_ADDRESS_STATE));
		
		verify(encounterService).searchForEncounters(isNull(), locationCaptor.capture(), isNull(), isNull());
		assertThat(locationCaptor.getValue(), notNullValue());
		assertThat(locationCaptor.getValue().getChain(), equalTo("address-state"));
		assertThat(locationCaptor.getValue().getValue(), equalTo(ENCOUNTER_ADDRESS_STATE));
	}
	
	@Test
	public void shouldGetEncountersByLocationPostalCode() throws Exception {
		verifyUri(String.format("/Encounter/?location.address-postalcode=%s", ENCOUNTER_POSTALCODE));
		
		verify(encounterService).searchForEncounters(isNull(), locationCaptor.capture(), isNull(), isNull());
		assertThat(locationCaptor.getValue(), notNullValue());
		assertThat(locationCaptor.getValue().getChain(), equalTo("address-postalcode"));
		assertThat(locationCaptor.getValue().getValue(), equalTo(ENCOUNTER_POSTALCODE));
	}
	
	@Test
	public void shouldGetEncountersByLocationCountry() throws Exception {
		verifyUri(String.format("/Encounter/?location.address-country=%s", ENCOUNTER_ADDRESS_COUNTRY));
		
		verify(encounterService).searchForEncounters(isNull(), locationCaptor.capture(), isNull(), isNull());
		assertThat(locationCaptor.getValue(), notNullValue());
		assertThat(locationCaptor.getValue().getChain(), equalTo("address-country"));
		assertThat(locationCaptor.getValue().getValue(), equalTo(ENCOUNTER_ADDRESS_COUNTRY));
	}
	
	@Test
	public void shouldGetEncountersByParticipantGivenName() throws Exception {
		verifyUri(String.format("/Encounter/?participant:Practitioner.given=%s", PATIENT_GIVEN_NAME));
		
		verify(encounterService).searchForEncounters(isNull(), isNull(), participantCaptor.capture(), isNull());
		assertThat(participantCaptor.getValue(), notNullValue());
		assertThat(participantCaptor.getValue().getChain(), equalTo("given"));
		assertThat(participantCaptor.getValue().getValue(), equalTo(PATIENT_GIVEN_NAME));
	}
	
	@Test
	public void shouldGetEncountersByParticipantFamilyName() throws Exception {
		verifyUri(String.format("/Encounter/?participant:Practitioner.family=%s", PATIENT_FAMILY_NAME));
		
		verify(encounterService).searchForEncounters(isNull(), isNull(), participantCaptor.capture(), isNull());
		assertThat(participantCaptor.getValue(), notNullValue());
		assertThat(participantCaptor.getValue().getChain(), equalTo("family"));
		assertThat(participantCaptor.getValue().getValue(), equalTo(PATIENT_FAMILY_NAME));
	}
	
	@Test
	public void shouldGetEncountersByParticipantIdentifier() throws Exception {
		verifyUri(String.format("/Encounter/?participant:Practitioner.identifier=%s", PATIENT_IDENTIFIER));
		
		verify(encounterService).searchForEncounters(isNull(), isNull(), participantCaptor.capture(), isNull());
		assertThat(participantCaptor.getValue(), notNullValue());
		assertThat(participantCaptor.getValue().getChain(), equalTo("identifier"));
		assertThat(participantCaptor.getValue().getValue(), equalTo(PATIENT_IDENTIFIER));
	}
	
	@Test
	public void shouldGetEncountersBySubjectGivenName() throws Exception {
		verifyUri(String.format("/Encounter/?subject.given=%s", PATIENT_GIVEN_NAME));
		
		verify(encounterService).searchForEncounters(isNull(), isNull(), isNull(), subjectCaptor.capture());
		assertThat(subjectCaptor.getValue(), notNullValue());
		assertThat(subjectCaptor.getValue().getChain(), equalTo("given"));
		assertThat(subjectCaptor.getValue().getValue(), equalTo(PATIENT_GIVEN_NAME));
	}
	
	@Test
	public void shouldGetEncountersBySubjectFamilyName() throws Exception {
		verifyUri(String.format("/Encounter?subject.family=%s", PATIENT_FAMILY_NAME));
		
		verify(encounterService).searchForEncounters(isNull(), isNull(), isNull(), subjectCaptor.capture());
		assertThat(subjectCaptor.getValue(), notNullValue());
		assertThat(subjectCaptor.getValue().getChain(), equalTo("family"));
		assertThat(subjectCaptor.getValue().getValue(), equalTo(PATIENT_FAMILY_NAME));
	}
	
	@Test
	public void shouldGetEncountersBySubjectIdentifier() throws Exception {
		verifyUri(String.format("/Encounter?subject.identifier=%s", PATIENT_IDENTIFIER));
		
		verify(encounterService).searchForEncounters(isNull(), isNull(), isNull(), subjectCaptor.capture());
		assertThat(subjectCaptor.getValue(), notNullValue());
		assertThat(subjectCaptor.getValue().getChain(), equalTo("identifier"));
		assertThat(subjectCaptor.getValue().getValue(), equalTo(PATIENT_IDENTIFIER));
	}
	
	@Test
	public void shouldGetEncountersBySubjectGivenNameAndLocationPostalCode() throws Exception {
		verifyUri("/Encounter?subject.given=Hannibal&location.address-postalcode=248001");
		
		verify(encounterService).searchForEncounters(isNull(), locationCaptor.capture(), isNull(), subjectCaptor.capture());
		
		assertThat(subjectCaptor.getValue(), notNullValue());
		assertThat(subjectCaptor.getValue().getChain(), equalTo("given"));
		assertThat(subjectCaptor.getValue().getValue(), equalTo(PATIENT_GIVEN_NAME));
		assertThat(locationCaptor.getValue(), notNullValue());
		assertThat(locationCaptor.getValue().getChain(), equalTo("address-postalcode"));
		assertThat(locationCaptor.getValue().getValue(), equalTo(ENCOUNTER_POSTALCODE));
	}
	
	@Test
	public void shouldGetEncountersByParticipantIdentifierAndLocationPostalCode() throws Exception {
		verifyUri("/Encounter?participant:Practitioner.identifier=h43489-h&location.address-postalcode=248001");
		
		verify(encounterService).searchForEncounters(isNull(), locationCaptor.capture(), participantCaptor.capture(),
		    isNull());
		
		assertThat(participantCaptor.getValue(), notNullValue());
		assertThat(participantCaptor.getValue().getChain(), equalTo("identifier"));
		assertThat(participantCaptor.getValue().getValue(), equalTo(PATIENT_IDENTIFIER));
		assertThat(locationCaptor.getValue(), notNullValue());
		assertThat(locationCaptor.getValue().getChain(), equalTo("address-postalcode"));
		assertThat(locationCaptor.getValue().getValue(), equalTo(ENCOUNTER_POSTALCODE));
	}
	
	@Test
	public void shouldGetEncountersByParticipantIdentifierAndDate() throws Exception {
		verifyUri("/Encounter?participant:Practitioner.identifier=h43489-h&date=ge1975-02-02");
		
		verify(encounterService).searchForEncounters(dateRangeCaptor.capture(), isNull(), participantCaptor.capture(),
		    isNull());
		
		assertThat(participantCaptor.getValue(), notNullValue());
		assertThat(participantCaptor.getValue().getChain(), equalTo("identifier"));
		assertThat(participantCaptor.getValue().getValue(), equalTo(PATIENT_IDENTIFIER));
		
		Calendar calendar = Calendar.getInstance();
		calendar.set(1975, 1, 2);
		assertThat(dateRangeCaptor.getValue(), notNullValue());
		assertThat(dateRangeCaptor.getValue().getLowerBound().getValue(),
		    equalTo(DateUtils.truncate(calendar.getTime(), Calendar.DATE)));
		assertThat(dateRangeCaptor.getValue().getUpperBound(), nullValue());
	}
	
	private void verifyUri(String uri) throws Exception {
		Encounter encounter = new Encounter();
		encounter.setId(ENCOUNTER_UUID);
		when(encounterService.searchForEncounters(any(), any(), any(), any()))
		        .thenReturn(Collections.singletonList(encounter));
		
		MockHttpServletResponse response = get(uri).accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), equalTo(FhirMediaTypes.JSON.toString()));
		
		Bundle results = readBundleResponse(response);
		assertThat(results.getEntry(), notNullValue());
		assertThat(results.getEntry(), not(empty()));
		assertThat(results.getEntry().get(0).getResource(), notNullValue());
		assertThat(results.getEntry().get(0).getResource().getIdElement().getIdPart(), equalTo(ENCOUNTER_UUID));
	}
	
}
