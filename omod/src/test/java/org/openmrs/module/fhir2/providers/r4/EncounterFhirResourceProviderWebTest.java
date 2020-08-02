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
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsStringIgnoringCase;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasProperty;
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
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;

import ca.uhn.fhir.model.api.Include;
import ca.uhn.fhir.rest.param.DateRangeParam;
import ca.uhn.fhir.rest.param.ReferenceAndListParam;
import ca.uhn.fhir.rest.param.ReferenceOrListParam;
import ca.uhn.fhir.rest.param.ReferenceParam;
import ca.uhn.fhir.rest.param.TokenAndListParam;
import lombok.AccessLevel;
import lombok.Getter;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.time.DateUtils;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Encounter;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.Provenance;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.api.FhirEncounterService;
import org.openmrs.module.fhir2.api.util.FhirUtils;
import org.springframework.mock.web.MockHttpServletResponse;

@RunWith(MockitoJUnitRunner.class)
public class EncounterFhirResourceProviderWebTest extends BaseFhirR4ResourceProviderWebTest<EncounterFhirResourceProvider, Encounter> {
	
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
	
	private static final String PARTICIPANT_GIVEN_NAME = "John";
	
	private static final String PARTICIPANT_FAMILY_NAME = "Doe";
	
	private static final String PARTICIPANT_IDENTIFIER = "1000WF";
	
	private static final String LOCATION_UUID = "c36006e5-9fbb-4f20-866b-0ece245615a1";
	
	private static final String PARTICIPANT_UUID = "b566821c-1ad9-473b-836b-9e9c67688e02";
	
	private static final String LAST_UPDATED_DATE = "eq2020-09-03";
	
	private static final String JSON_CREATE_ENCOUNTER_PATH = "org/openmrs/module/fhir2/providers/EncounterWebTest_create.json";
	
	private static final String JSON_UPDATE_ENCOUNTER_PATH = "org/openmrs/module/fhir2/providers/EncounterWebTest_update.json";
	
	private static final String JSON_UPDATE_ENCOUNTER_NO_ID_PATH = "org/openmrs/module/fhir2/providers/EncounterWebTest_updateWithoutId.json";
	
	private static final String JSON_UPDATE_ENCOUNTER_WRONG_ID_PATH = "org/openmrs/module/fhir2/providers/EncounterWebTest_updateWithWrongId.json";
	
	@Mock
	private FhirEncounterService encounterService;
	
	@Getter(AccessLevel.PUBLIC)
	private EncounterFhirResourceProvider resourceProvider;
	
	@Captor
	private ArgumentCaptor<ReferenceAndListParam> locationCaptor;
	
	@Captor
	private ArgumentCaptor<ReferenceAndListParam> participantCaptor;
	
	@Captor
	private ArgumentCaptor<ReferenceAndListParam> subjectCaptor;
	
	@Captor
	private ArgumentCaptor<DateRangeParam> dateRangeCaptor;
	
	@Captor
	private ArgumentCaptor<TokenAndListParam> tokenAndListParamArgumentCaptor;
	
	@Captor
	private ArgumentCaptor<HashSet<Include>> includeArgumentCaptor;
	
	private Encounter encounter;
	
	@Before
	@Override
	public void setup() throws ServletException {
		resourceProvider = new EncounterFhirResourceProvider();
		resourceProvider.setEncounterService(encounterService);
		encounter = new Encounter();
		encounter.setId(ENCOUNTER_UUID);
		super.setup();
	}
	
	@Test
	public void getEncounterByUuid_shouldReturnEncounter() throws Exception {
		Encounter encounter = new Encounter();
		encounter.setId(ENCOUNTER_UUID);
		when(encounterService.get(ENCOUNTER_UUID)).thenReturn(encounter);
		
		MockHttpServletResponse response = get("/Encounter/" + ENCOUNTER_UUID).accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), equalTo(FhirMediaTypes.JSON.toString()));
		assertThat(readResponse(response).getIdElement().getIdPart(), equalTo(ENCOUNTER_UUID));
	}
	
	@Test
	public void getEncounterByWrongUuid_shouldReturn404() throws Exception {
		when(encounterService.get(WRONG_ENCOUNTER_UUID)).thenReturn(null);
		
		MockHttpServletResponse response = get("/Encounter/" + WRONG_ENCOUNTER_UUID).accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isNotFound());
	}
	
	@Test
	public void shouldGetEncountersBySubjectUuid() throws Exception {
		verifyUri(String.format("/Encounter?subject:Patient=%s", PATIENT_UUID));
		
		verify(encounterService).searchForEncounters(isNull(), isNull(), isNull(), subjectCaptor.capture(), isNull(),
		    isNull(), isNull(), isNull());
		assertThat(subjectCaptor.getValue(), notNullValue());
		assertThat(subjectCaptor.getAllValues().iterator().next().getValuesAsQueryTokens().iterator().next()
		        .getValuesAsQueryTokens().iterator().next().getIdPart(),
		    equalTo(PATIENT_UUID));
		assertThat(subjectCaptor.getAllValues().iterator().next().getValuesAsQueryTokens().iterator().next()
		        .getValuesAsQueryTokens().iterator().next().getChain(),
		    equalTo(null));
	}
	
	@Test
	public void shouldGetEncountersByDate() throws Exception {
		verifyUri("/Encounter/?date=ge1975-02-02");
		
		verify(encounterService).searchForEncounters(dateRangeCaptor.capture(), isNull(), isNull(), isNull(), isNull(),
		    isNull(), isNull(), isNull());
		assertThat(dateRangeCaptor.getValue(), notNullValue());
		
		Calendar calendar = Calendar.getInstance();
		calendar.set(1975, Calendar.FEBRUARY, 2);
		
		assertThat(dateRangeCaptor.getValue().getLowerBound().getValue(),
		    equalTo(DateUtils.truncate(calendar.getTime(), Calendar.DATE)));
		assertThat(dateRangeCaptor.getValue().getUpperBound(), nullValue());
	}
	
	@Test
	public void shouldGetEncountersByLocationUUID() throws Exception {
		verifyUri(String.format("/Encounter/?location=%s", LOCATION_UUID));
		
		verify(encounterService).searchForEncounters(isNull(), locationCaptor.capture(), isNull(), isNull(), isNull(),
		    isNull(), isNull(), isNull());
		
		List<ReferenceOrListParam> orListParams = locationCaptor.getValue().getValuesAsQueryTokens();
		ReferenceParam referenceParam = orListParams.get(0).getValuesAsQueryTokens().get(0);
		
		assertThat(locationCaptor.getValue(), notNullValue());
		assertThat(referenceParam.getChain(), equalTo(null));
		assertThat(referenceParam.getValue(), equalTo(LOCATION_UUID));
	}
	
	@Test
	public void shouldGetEncountersByLocationCityVillage() throws Exception {
		verifyUri(String.format("/Encounter/?location.address-city=%s", ENCOUNTER_ADDRESS_CITY));
		
		verify(encounterService).searchForEncounters(isNull(), locationCaptor.capture(), isNull(), isNull(), isNull(),
		    isNull(), isNull(), isNull());
		
		List<ReferenceOrListParam> orListParams = locationCaptor.getValue().getValuesAsQueryTokens();
		ReferenceParam referenceParam = orListParams.get(0).getValuesAsQueryTokens().get(0);
		
		assertThat(locationCaptor.getValue(), notNullValue());
		assertThat(referenceParam.getChain(), equalTo("address-city"));
		assertThat(referenceParam.getValue(), equalTo(ENCOUNTER_ADDRESS_CITY));
	}
	
	@Test
	public void shouldGetEncountersByLocationState() throws Exception {
		verifyUri(String.format("/Encounter/?location.address-state=%s", ENCOUNTER_ADDRESS_STATE));
		
		verify(encounterService).searchForEncounters(isNull(), locationCaptor.capture(), isNull(), isNull(), isNull(),
		    isNull(), isNull(), isNull());
		
		List<ReferenceOrListParam> orListParams = locationCaptor.getValue().getValuesAsQueryTokens();
		ReferenceParam referenceParam = orListParams.get(0).getValuesAsQueryTokens().get(0);
		
		assertThat(locationCaptor.getValue(), notNullValue());
		assertThat(referenceParam.getChain(), equalTo("address-state"));
		assertThat(referenceParam.getValue(), equalTo(ENCOUNTER_ADDRESS_STATE));
	}
	
	@Test
	public void shouldGetEncountersByLocationPostalCode() throws Exception {
		verifyUri(String.format("/Encounter/?location.address-postalcode=%s", ENCOUNTER_POSTALCODE));
		
		verify(encounterService).searchForEncounters(isNull(), locationCaptor.capture(), isNull(), isNull(), isNull(),
		    isNull(), isNull(), isNull());
		
		List<ReferenceOrListParam> orListParams = locationCaptor.getValue().getValuesAsQueryTokens();
		ReferenceParam referenceParam = orListParams.get(0).getValuesAsQueryTokens().get(0);
		
		assertThat(locationCaptor.getValue(), notNullValue());
		assertThat(referenceParam.getChain(), equalTo("address-postalcode"));
		assertThat(referenceParam.getValue(), equalTo(ENCOUNTER_POSTALCODE));
	}
	
	@Test
	public void shouldGetEncountersByLocationCountry() throws Exception {
		verifyUri(String.format("/Encounter/?location.address-country=%s", ENCOUNTER_ADDRESS_COUNTRY));
		
		verify(encounterService).searchForEncounters(isNull(), locationCaptor.capture(), isNull(), isNull(), isNull(),
		    isNull(), isNull(), isNull());
		
		List<ReferenceOrListParam> orListParams = locationCaptor.getValue().getValuesAsQueryTokens();
		ReferenceParam referenceParam = orListParams.get(0).getValuesAsQueryTokens().get(0);
		
		assertThat(locationCaptor.getValue(), notNullValue());
		assertThat(referenceParam.getChain(), equalTo("address-country"));
		assertThat(referenceParam.getValue(), equalTo(ENCOUNTER_ADDRESS_COUNTRY));
	}
	
	@Test
	public void shouldGetEncountersByLocationCountryWithOr() throws Exception {
		verifyUri(String.format("/Encounter/?location.address-country=%s,%s", ENCOUNTER_ADDRESS_COUNTRY, "USA"));
		
		verify(encounterService).searchForEncounters(isNull(), locationCaptor.capture(), isNull(), isNull(), isNull(),
		    isNull(), isNull(), isNull());
		
		List<ReferenceOrListParam> orListParams = locationCaptor.getValue().getValuesAsQueryTokens();
		ReferenceParam referenceParam = orListParams.get(0).getValuesAsQueryTokens().get(0);
		
		assertThat(locationCaptor.getValue(), notNullValue());
		assertThat(referenceParam.getChain(), equalTo("address-country"));
		assertThat(referenceParam.getValue(), equalTo(ENCOUNTER_ADDRESS_COUNTRY));
		assertThat(orListParams.get(0).getValuesAsQueryTokens().size(), equalTo(2));
	}
	
	@Test
	public void shouldGetEncountersByLocationCountryWithAnd() throws Exception {
		verifyUri("/Encounter/?location.address-country=INDIA&location.address-country=USA");
		
		verify(encounterService).searchForEncounters(isNull(), locationCaptor.capture(), isNull(), isNull(), isNull(),
		    isNull(), isNull(), isNull());
		
		List<ReferenceOrListParam> orListParams = locationCaptor.getValue().getValuesAsQueryTokens();
		ReferenceParam referenceParam = orListParams.get(0).getValuesAsQueryTokens().get(0);
		
		assertThat(locationCaptor.getValue(), notNullValue());
		assertThat(referenceParam.getChain(), equalTo("address-country"));
		assertThat(referenceParam.getValue(), equalTo(ENCOUNTER_ADDRESS_COUNTRY));
		assertThat(locationCaptor.getValue().getValuesAsQueryTokens().size(), equalTo(2));
	}
	
	@Test
	public void shouldGetEncountersByParticipantUUID() throws Exception {
		verifyUri(String.format("/Encounter/?participant:Practitioner=%s", PARTICIPANT_UUID));
		
		verify(encounterService).searchForEncounters(isNull(), isNull(), participantCaptor.capture(), isNull(), isNull(),
		    isNull(), isNull(), isNull());
		
		List<ReferenceOrListParam> orListParams = participantCaptor.getValue().getValuesAsQueryTokens();
		ReferenceParam referenceParam = orListParams.get(0).getValuesAsQueryTokens().get(0);
		
		assertThat(participantCaptor.getValue(), notNullValue());
		assertThat(referenceParam.getChain(), equalTo(null));
		assertThat(referenceParam.getValue(), equalTo(PARTICIPANT_UUID));
	}
	
	@Test
	public void shouldGetEncountersByParticipantGivenName() throws Exception {
		verifyUri(String.format("/Encounter/?participant:Practitioner.given=%s", PARTICIPANT_GIVEN_NAME));
		
		verify(encounterService).searchForEncounters(isNull(), isNull(), participantCaptor.capture(), isNull(), isNull(),
		    isNull(), isNull(), isNull());
		
		List<ReferenceOrListParam> orListParams = participantCaptor.getValue().getValuesAsQueryTokens();
		ReferenceParam referenceParam = orListParams.get(0).getValuesAsQueryTokens().get(0);
		
		assertThat(participantCaptor.getValue(), notNullValue());
		assertThat(referenceParam.getChain(), equalTo("given"));
		assertThat(referenceParam.getValue(), equalTo(PARTICIPANT_GIVEN_NAME));
	}
	
	@Test
	public void shouldGetEncountersByParticipantFamilyName() throws Exception {
		verifyUri(String.format("/Encounter/?participant:Practitioner.family=%s", PARTICIPANT_FAMILY_NAME));
		
		verify(encounterService).searchForEncounters(isNull(), isNull(), participantCaptor.capture(), isNull(), isNull(),
		    isNull(), isNull(), isNull());
		
		List<ReferenceOrListParam> orListParams = participantCaptor.getValue().getValuesAsQueryTokens();
		ReferenceParam referenceParam = orListParams.get(0).getValuesAsQueryTokens().get(0);
		
		assertThat(participantCaptor.getValue(), notNullValue());
		assertThat(referenceParam.getChain(), equalTo("family"));
		assertThat(referenceParam.getValue(), equalTo(PARTICIPANT_FAMILY_NAME));
	}
	
	@Test
	public void shouldGetEncountersByParticipantFamilyNameWithOr() throws Exception {
		verifyUri(String.format("/Encounter/?participant:Practitioner.family=%s,%s", PARTICIPANT_FAMILY_NAME, "Vox"));
		
		verify(encounterService).searchForEncounters(isNull(), isNull(), participantCaptor.capture(), isNull(), isNull(),
		    isNull(), isNull(), isNull());
		
		List<ReferenceOrListParam> orListParams = participantCaptor.getValue().getValuesAsQueryTokens();
		ReferenceParam referenceParam = orListParams.get(0).getValuesAsQueryTokens().get(0);
		
		assertThat(participantCaptor.getValue(), notNullValue());
		assertThat(referenceParam.getChain(), equalTo("family"));
		assertThat(referenceParam.getValue(), equalTo(PARTICIPANT_FAMILY_NAME));
		assertThat(orListParams.get(0).getValuesAsQueryTokens().size(), equalTo(2));
	}
	
	@Test
	public void shouldGetEncountersByParticipantFamilyNameWithAnd() throws Exception {
		verifyUri(String.format("/Encounter/?participant:Practitioner.family=%s&participant:Practitioner.family=%s",
		    PARTICIPANT_FAMILY_NAME, "Vox"));
		
		verify(encounterService).searchForEncounters(isNull(), isNull(), participantCaptor.capture(), isNull(), isNull(),
		    isNull(), isNull(), isNull());
		
		List<ReferenceOrListParam> orListParams = participantCaptor.getValue().getValuesAsQueryTokens();
		ReferenceParam referenceParam = orListParams.get(0).getValuesAsQueryTokens().get(0);
		
		assertThat(participantCaptor.getValue(), notNullValue());
		assertThat(referenceParam.getChain(), equalTo("family"));
		assertThat(referenceParam.getValue(), equalTo(PARTICIPANT_FAMILY_NAME));
		assertThat(participantCaptor.getValue().getValuesAsQueryTokens().size(), equalTo(2));
	}
	
	@Test
	public void shouldGetEncountersByParticipantIdentifier() throws Exception {
		verifyUri(String.format("/Encounter/?participant:Practitioner.identifier=%s,%s", PARTICIPANT_IDENTIFIER,
		    "op87yh-34fd-34egs-56h34-34f7"));
		
		verify(encounterService).searchForEncounters(isNull(), isNull(), participantCaptor.capture(), isNull(), isNull(),
		    isNull(), isNull(), isNull());
		
		List<ReferenceOrListParam> orListParams = participantCaptor.getValue().getValuesAsQueryTokens();
		ReferenceParam referenceParam = orListParams.get(0).getValuesAsQueryTokens().get(0);
		
		assertThat(participantCaptor.getValue(), notNullValue());
		assertThat(referenceParam.getChain(), equalTo("identifier"));
		assertThat(referenceParam.getValue(), equalTo(PARTICIPANT_IDENTIFIER));
		assertThat(orListParams.get(0).getValuesAsQueryTokens().size(), equalTo(2));
	}
	
	@Test
	public void shouldGetEncountersBySubjectGivenName() throws Exception {
		verifyUri(String.format("/Encounter/?subject.given=%s", PATIENT_GIVEN_NAME));
		
		verify(encounterService).searchForEncounters(isNull(), isNull(), isNull(), subjectCaptor.capture(), isNull(),
		    isNull(), isNull(), isNull());
		
		List<ReferenceOrListParam> orListParams = subjectCaptor.getValue().getValuesAsQueryTokens();
		ReferenceParam referenceParam = orListParams.get(0).getValuesAsQueryTokens().get(0);
		
		assertThat(subjectCaptor.getValue(), notNullValue());
		assertThat(referenceParam.getChain(), equalTo("given"));
		assertThat(referenceParam.getValue(), equalTo(PATIENT_GIVEN_NAME));
	}
	
	@Test
	public void shouldGetEncountersBySubjectFamilyName() throws Exception {
		verifyUri(String.format("/Encounter?subject.family=%s", PATIENT_FAMILY_NAME));
		
		verify(encounterService).searchForEncounters(isNull(), isNull(), isNull(), subjectCaptor.capture(), isNull(),
		    isNull(), isNull(), isNull());
		
		List<ReferenceOrListParam> orListParams = subjectCaptor.getValue().getValuesAsQueryTokens();
		ReferenceParam referenceParam = orListParams.get(0).getValuesAsQueryTokens().get(0);
		
		assertThat(subjectCaptor.getValue(), notNullValue());
		assertThat(referenceParam.getChain(), equalTo("family"));
		assertThat(referenceParam.getValue(), equalTo(PATIENT_FAMILY_NAME));
	}
	
	@Test
	public void shouldGetEncountersBySubjectIdentifier() throws Exception {
		verifyUri(String.format("/Encounter?subject.identifier=%s", PATIENT_IDENTIFIER));
		
		verify(encounterService).searchForEncounters(isNull(), isNull(), isNull(), subjectCaptor.capture(), isNull(),
		    isNull(), isNull(), isNull());
		
		List<ReferenceOrListParam> orListParams = subjectCaptor.getValue().getValuesAsQueryTokens();
		ReferenceParam referenceParam = orListParams.get(0).getValuesAsQueryTokens().get(0);
		
		assertThat(subjectCaptor.getValue(), notNullValue());
		assertThat(referenceParam.getChain(), equalTo("identifier"));
		assertThat(referenceParam.getValue(), equalTo(PATIENT_IDENTIFIER));
	}
	
	@Test
	public void shouldGetEncountersBySubjectGivenNameAndLocationPostalCode() throws Exception {
		verifyUri("/Encounter?subject.given=Hannibal&location.address-postalcode=248001");
		
		verify(encounterService).searchForEncounters(isNull(), locationCaptor.capture(), isNull(), subjectCaptor.capture(),
		    isNull(), isNull(), isNull(), isNull());
		
		List<ReferenceOrListParam> orListParamsSubject = subjectCaptor.getValue().getValuesAsQueryTokens();
		ReferenceParam referenceParamSubject = orListParamsSubject.get(0).getValuesAsQueryTokens().get(0);
		
		List<ReferenceOrListParam> orListParamsLocation = locationCaptor.getValue().getValuesAsQueryTokens();
		ReferenceParam referenceParamLocation = orListParamsLocation.get(0).getValuesAsQueryTokens().get(0);
		
		assertThat(subjectCaptor.getValue(), notNullValue());
		assertThat(referenceParamSubject.getChain(), equalTo("given"));
		assertThat(referenceParamSubject.getValue(), equalTo(PATIENT_GIVEN_NAME));
		assertThat(locationCaptor.getValue(), notNullValue());
		assertThat(referenceParamLocation.getChain(), equalTo("address-postalcode"));
		assertThat(referenceParamLocation.getValue(), equalTo(ENCOUNTER_POSTALCODE));
	}
	
	@Test
	public void shouldGetEncountersBySubjectGivenNameAndLocationPostalCodeWithOr() throws Exception {
		verifyUri("/Encounter?subject.given=Hannibal&location.address-postalcode=248001,854796");
		
		verify(encounterService).searchForEncounters(isNull(), locationCaptor.capture(), isNull(), subjectCaptor.capture(),
		    isNull(), isNull(), isNull(), isNull());
		
		List<ReferenceOrListParam> orListParamsSubject = subjectCaptor.getValue().getValuesAsQueryTokens();
		ReferenceParam referenceParamSubject = orListParamsSubject.get(0).getValuesAsQueryTokens().get(0);
		
		List<ReferenceOrListParam> orListParamsLocation = locationCaptor.getValue().getValuesAsQueryTokens();
		ReferenceParam referenceParamLocation = orListParamsLocation.get(0).getValuesAsQueryTokens().get(0);
		
		assertThat(subjectCaptor.getValue(), notNullValue());
		assertThat(referenceParamSubject.getChain(), equalTo("given"));
		assertThat(referenceParamSubject.getValue(), equalTo(PATIENT_GIVEN_NAME));
		assertThat(locationCaptor.getValue(), notNullValue());
		assertThat(referenceParamLocation.getChain(), equalTo("address-postalcode"));
		assertThat(referenceParamLocation.getValue(), equalTo(ENCOUNTER_POSTALCODE));
		assertThat(orListParamsLocation.get(0).getValuesAsQueryTokens().size(), equalTo(2));
	}
	
	@Test
	public void shouldGetEncountersBySubjectGivenNameAndLocationPostalCodeWithAnd() throws Exception {
		verifyUri("/Encounter?subject.given=Hannibal&location.address-postalcode=248001&location.address-postalcode=854796");
		
		verify(encounterService).searchForEncounters(isNull(), locationCaptor.capture(), isNull(), subjectCaptor.capture(),
		    isNull(), isNull(), isNull(), isNull());
		
		List<ReferenceOrListParam> orListParamsSubject = subjectCaptor.getValue().getValuesAsQueryTokens();
		ReferenceParam referenceParamSubject = orListParamsSubject.get(0).getValuesAsQueryTokens().get(0);
		
		List<ReferenceOrListParam> orListParamsLocation = locationCaptor.getValue().getValuesAsQueryTokens();
		ReferenceParam referenceParamLocation = orListParamsLocation.get(0).getValuesAsQueryTokens().get(0);
		
		assertThat(subjectCaptor.getValue(), notNullValue());
		assertThat(referenceParamSubject.getChain(), equalTo("given"));
		assertThat(referenceParamSubject.getValue(), equalTo(PATIENT_GIVEN_NAME));
		assertThat(locationCaptor.getValue(), notNullValue());
		assertThat(referenceParamLocation.getChain(), equalTo("address-postalcode"));
		assertThat(referenceParamLocation.getValue(), equalTo(ENCOUNTER_POSTALCODE));
		assertThat(locationCaptor.getValue().getValuesAsQueryTokens().size(), equalTo(2));
	}
	
	@Test
	public void shouldGetEncountersByParticipantIdentifierAndLocationPostalCode() throws Exception {
		verifyUri("/Encounter?participant:Practitioner.identifier=1000WF&location.address-postalcode=248001");
		
		verify(encounterService).searchForEncounters(isNull(), locationCaptor.capture(), participantCaptor.capture(),
		    isNull(), isNull(), isNull(), isNull(), isNull());
		
		List<ReferenceOrListParam> orListParamsParticipant = participantCaptor.getValue().getValuesAsQueryTokens();
		ReferenceParam referenceParamParticipant = orListParamsParticipant.get(0).getValuesAsQueryTokens().get(0);
		
		List<ReferenceOrListParam> orListParamsLocation = locationCaptor.getValue().getValuesAsQueryTokens();
		ReferenceParam referenceParamLocation = orListParamsLocation.get(0).getValuesAsQueryTokens().get(0);
		
		assertThat(participantCaptor.getValue(), notNullValue());
		assertThat(referenceParamParticipant.getChain(), equalTo("identifier"));
		assertThat(referenceParamParticipant.getValue(), equalTo(PARTICIPANT_IDENTIFIER));
		assertThat(locationCaptor.getValue(), notNullValue());
		assertThat(referenceParamLocation.getChain(), equalTo("address-postalcode"));
		assertThat(referenceParamLocation.getValue(), equalTo(ENCOUNTER_POSTALCODE));
	}
	
	@Test
	public void shouldGetEncountersByParticipantIdentifierAndDate() throws Exception {
		verifyUri("/Encounter?participant:Practitioner.identifier=1000WF,670WD&date=ge1975-02-02");
		
		verify(encounterService).searchForEncounters(dateRangeCaptor.capture(), isNull(), participantCaptor.capture(),
		    isNull(), isNull(), isNull(), isNull(), isNull());
		
		List<ReferenceOrListParam> orListParamsParticipant = participantCaptor.getValue().getValuesAsQueryTokens();
		ReferenceParam referenceParamParticipant = orListParamsParticipant.get(0).getValuesAsQueryTokens().get(0);
		
		assertThat(participantCaptor.getValue(), notNullValue());
		assertThat(referenceParamParticipant.getChain(), equalTo("identifier"));
		assertThat(referenceParamParticipant.getValue(), equalTo(PARTICIPANT_IDENTIFIER));
		assertThat(orListParamsParticipant.get(0).getValuesAsQueryTokens().size(), equalTo(2));
		
		Calendar calendar = Calendar.getInstance();
		calendar.set(1975, Calendar.FEBRUARY, 2);
		assertThat(dateRangeCaptor.getValue(), notNullValue());
		assertThat(dateRangeCaptor.getValue().getLowerBound().getValue(),
		    equalTo(DateUtils.truncate(calendar.getTime(), Calendar.DATE)));
		assertThat(dateRangeCaptor.getValue().getUpperBound(), nullValue());
	}
	
	@Test
	public void shouldGetEncountersByUUID() throws Exception {
		verifyUri(String.format("/Encounter?_id=%s", ENCOUNTER_UUID));
		
		verify(encounterService).searchForEncounters(isNull(), isNull(), isNull(), isNull(),
		    tokenAndListParamArgumentCaptor.capture(), isNull(), isNull(), isNull());
		
		assertThat(tokenAndListParamArgumentCaptor.getValue(), notNullValue());
		assertThat(tokenAndListParamArgumentCaptor.getValue().getValuesAsQueryTokens(), not(empty()));
		assertThat(tokenAndListParamArgumentCaptor.getValue().getValuesAsQueryTokens().get(0).getValuesAsQueryTokens().get(0)
		        .getValue(),
		    equalTo(ENCOUNTER_UUID));
	}
	
	@Test
	public void shouldGetEncountersByLastUpdatedDate() throws Exception {
		verifyUri(String.format("/Encounter?_lastUpdated=%s", LAST_UPDATED_DATE));
		
		verify(encounterService).searchForEncounters(isNull(), isNull(), isNull(), isNull(), isNull(),
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
	public void shouldAddPatientsWithReturnedEncounters() throws Exception {
		verifyUri("/Encounter?_include=Encounter:patient");
		
		verify(encounterService).searchForEncounters(isNull(), isNull(), isNull(), isNull(), isNull(), isNull(),
		    includeArgumentCaptor.capture(), isNull());
		
		assertThat(includeArgumentCaptor.getValue(), notNullValue());
		assertThat(includeArgumentCaptor.getValue().size(), equalTo(1));
		assertThat(includeArgumentCaptor.getValue().iterator().next().getParamName(),
		    equalTo(FhirConstants.INCLUDE_PATIENT_PARAM));
		assertThat(includeArgumentCaptor.getValue().iterator().next().getParamType(), equalTo(FhirConstants.ENCOUNTER));
	}
	
	@Test
	public void shouldAddLocationsWithReturnedEncounters() throws Exception {
		verifyUri("/Encounter?_include=Encounter:location");
		
		verify(encounterService).searchForEncounters(isNull(), isNull(), isNull(), isNull(), isNull(), isNull(),
		    includeArgumentCaptor.capture(), isNull());
		
		assertThat(includeArgumentCaptor.getValue(), notNullValue());
		assertThat(includeArgumentCaptor.getValue().size(), equalTo(1));
		assertThat(includeArgumentCaptor.getValue().iterator().next().getParamName(),
		    equalTo(FhirConstants.INCLUDE_LOCATION_PARAM));
		assertThat(includeArgumentCaptor.getValue().iterator().next().getParamType(), equalTo(FhirConstants.ENCOUNTER));
	}
	
	@Test
	public void shouldAddParticipantsWithReturnedEncounters() throws Exception {
		verifyUri("/Encounter?_include=Encounter:participant");
		
		verify(encounterService).searchForEncounters(isNull(), isNull(), isNull(), isNull(), isNull(), isNull(),
		    includeArgumentCaptor.capture(), isNull());
		
		assertThat(includeArgumentCaptor.getValue(), notNullValue());
		assertThat(includeArgumentCaptor.getValue().size(), equalTo(1));
		assertThat(includeArgumentCaptor.getValue().iterator().next().getParamName(),
		    equalTo(FhirConstants.INCLUDE_PARTICIPANT_PARAM));
		assertThat(includeArgumentCaptor.getValue().iterator().next().getParamType(), equalTo(FhirConstants.ENCOUNTER));
	}
	
	@Test
	public void shouldHandleMultipleIncludes() throws Exception {
		verifyUri("/Encounter?_include=Encounter:participant&_include=Encounter:location");
		
		verify(encounterService).searchForEncounters(isNull(), isNull(), isNull(), isNull(), isNull(), isNull(),
		    includeArgumentCaptor.capture(), isNull());
		
		assertThat(includeArgumentCaptor.getValue(), notNullValue());
		assertThat(includeArgumentCaptor.getValue().size(), equalTo(2));
		
		assertThat(includeArgumentCaptor.getValue(),
		    hasItem(allOf(hasProperty("paramName", equalTo(FhirConstants.INCLUDE_LOCATION_PARAM)),
		        hasProperty("paramType", equalTo(FhirConstants.ENCOUNTER)))));
		assertThat(includeArgumentCaptor.getValue(),
		    hasItem(allOf(hasProperty("paramName", equalTo(FhirConstants.INCLUDE_PARTICIPANT_PARAM)),
		        hasProperty("paramType", equalTo(FhirConstants.ENCOUNTER)))));
	}
	
	@Test
	public void shouldAddObservationsWithReturnedEncounters() throws Exception {
		verifyUri("/Encounter?_revinclude=Observation:encounter");
		
		verify(encounterService).searchForEncounters(isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(),
		    includeArgumentCaptor.capture());
		
		assertThat(includeArgumentCaptor.getValue(), notNullValue());
		assertThat(includeArgumentCaptor.getValue().size(), equalTo(1));
		assertThat(includeArgumentCaptor.getValue().iterator().next().getParamName(),
		    equalTo(FhirConstants.INCLUDE_ENCOUNTER_PARAM));
		assertThat(includeArgumentCaptor.getValue().iterator().next().getParamType(), equalTo(FhirConstants.OBSERVATION));
	}
	
	@Test
	public void shouldAddDiagnosticReportsWithReturnedEncounters() throws Exception {
		verifyUri("/Encounter?_revinclude=DiagnosticReport:encounter");
		
		verify(encounterService).searchForEncounters(isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(),
		    includeArgumentCaptor.capture());
		
		assertThat(includeArgumentCaptor.getValue(), notNullValue());
		assertThat(includeArgumentCaptor.getValue().size(), equalTo(1));
		assertThat(includeArgumentCaptor.getValue().iterator().next().getParamName(),
		    equalTo(FhirConstants.INCLUDE_ENCOUNTER_PARAM));
		assertThat(includeArgumentCaptor.getValue().iterator().next().getParamType(),
		    equalTo(FhirConstants.DIAGNOSTIC_REPORT));
	}
	
	@Test
	public void shouldAddMedicationRequestsWithReturnedEncounters() throws Exception {
		verifyUri("/Encounter?_revinclude=MedicationRequest:encounter");
		
		verify(encounterService).searchForEncounters(isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(),
		    includeArgumentCaptor.capture());
		
		assertThat(includeArgumentCaptor.getValue(), notNullValue());
		assertThat(includeArgumentCaptor.getValue().size(), equalTo(1));
		assertThat(includeArgumentCaptor.getValue().iterator().next().getParamName(),
		    equalTo(FhirConstants.INCLUDE_ENCOUNTER_PARAM));
		assertThat(includeArgumentCaptor.getValue().iterator().next().getParamType(),
		    equalTo(FhirConstants.MEDICATION_REQUEST));
	}
	
	@Test
	public void shouldAddServiceRequestsWithReturnedEncounters() throws Exception {
		verifyUri("/Encounter?_revinclude=ServiceRequest:encounter");
		
		verify(encounterService).searchForEncounters(isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(),
		    includeArgumentCaptor.capture());
		
		assertThat(includeArgumentCaptor.getValue(), notNullValue());
		assertThat(includeArgumentCaptor.getValue().size(), equalTo(1));
		assertThat(includeArgumentCaptor.getValue().iterator().next().getParamName(),
		    equalTo(FhirConstants.INCLUDE_ENCOUNTER_PARAM));
		assertThat(includeArgumentCaptor.getValue().iterator().next().getParamType(),
		    equalTo(FhirConstants.SERVICE_REQUEST));
	}
	
	@Test
	public void shouldHandleMultipleReverseIncludes() throws Exception {
		verifyUri("/Encounter?_revinclude=DiagnosticReport:encounter&_revinclude=Observation:encounter");
		
		verify(encounterService).searchForEncounters(isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(),
		    includeArgumentCaptor.capture());
		
		assertThat(includeArgumentCaptor.getValue(), notNullValue());
		assertThat(includeArgumentCaptor.getValue().size(), equalTo(2));
		
		assertThat(includeArgumentCaptor.getValue(),
		    hasItem(allOf(hasProperty("paramName", equalTo(FhirConstants.INCLUDE_ENCOUNTER_PARAM)),
		        hasProperty("paramType", equalTo(FhirConstants.OBSERVATION)))));
		assertThat(includeArgumentCaptor.getValue(),
		    hasItem(allOf(hasProperty("paramName", equalTo(FhirConstants.INCLUDE_ENCOUNTER_PARAM)),
		        hasProperty("paramType", equalTo(FhirConstants.DIAGNOSTIC_REPORT)))));
	}
	
	private void verifyUri(String uri) throws Exception {
		Encounter encounter = new Encounter();
		encounter.setId(ENCOUNTER_UUID);
		when(encounterService.searchForEncounters(any(), any(), any(), any(), any(), any(), any(), any()))
		        .thenReturn(new MockIBundleProvider<>(Collections.singletonList(encounter), 10, 1));
		
		MockHttpServletResponse response = get(uri).accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), equalTo(FhirMediaTypes.JSON.toString()));
		
		Bundle results = readBundleResponse(response);
		assertThat(results.getEntry(), notNullValue());
		assertThat(results.getEntry(), not(empty()));
		assertThat(results.getEntry().get(0).getResource(), notNullValue());
		assertThat(results.getEntry().get(0).getResource().getIdElement().getIdPart(), equalTo(ENCOUNTER_UUID));
	}
	
	@Test
	public void shouldVerifyEncounterHistoryByIdUri() throws Exception {
		Encounter encounter = new Encounter();
		encounter.setId(ENCOUNTER_UUID);
		when(encounterService.get(ENCOUNTER_UUID)).thenReturn(encounter);
		
		MockHttpServletResponse response = getEncounterHistoryRequest();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), equalTo(FhirMediaTypes.JSON.toString()));
	}
	
	@Test
	public void shouldGetEncounterHistoryById() throws IOException, ServletException {
		Provenance provenance = new Provenance();
		provenance.setId(new IdType(FhirUtils.newUuid()));
		provenance.setRecorded(new Date());
		provenance.setActivity(new CodeableConcept().addCoding(
		    new Coding().setCode("CREATE").setSystem(FhirConstants.FHIR_TERMINOLOGY_DATA_OPERATION).setDisplay("create")));
		provenance.addAgent(new Provenance.ProvenanceAgentComponent()
		        .setType(
		            new CodeableConcept().addCoding(new Coding().setCode(FhirConstants.AUT).setDisplay(FhirConstants.AUTHOR)
		                    .setSystem(FhirConstants.FHIR_TERMINOLOGY_PROVENANCE_PARTICIPANT_TYPE)))
		        .addRole(new CodeableConcept().addCoding(
		            new Coding().setCode("").setDisplay("").setSystem(FhirConstants.FHIR_TERMINOLOGY_PARTICIPATION_TYPE))));
		Encounter encounter = new Encounter();
		encounter.setId(ENCOUNTER_UUID);
		encounter.addContained(provenance);
		
		when(encounterService.get(ENCOUNTER_UUID)).thenReturn(encounter);
		
		MockHttpServletResponse response = getEncounterHistoryRequest();
		
		Bundle results = readBundleResponse(response);
		assertThat(results, notNullValue());
		assertThat(results.hasEntry(), is(true));
		assertThat(results.getEntry().get(0).getResource(), notNullValue());
		assertThat(results.getEntry().get(0).getResource().getResourceType().name(),
		    equalTo(Provenance.class.getSimpleName()));
		
	}
	
	@Test
	public void getEncounterHistoryById_shouldReturnBundleWithEmptyEntriesIfPractitionerContainedIsEmpty() throws Exception {
		Encounter encounter = new Encounter();
		encounter.setId(ENCOUNTER_UUID);
		encounter.setContained(new ArrayList<>());
		when(encounterService.get(ENCOUNTER_UUID)).thenReturn(encounter);
		
		MockHttpServletResponse response = getEncounterHistoryRequest();
		Bundle results = readBundleResponse(response);
		assertThat(results.hasEntry(), is(false));
	}
	
	@Test
	public void getEncounterHistoryById_shouldReturn404IfEncounterIdIsWrong() throws Exception {
		MockHttpServletResponse response = get("/Encounter/" + WRONG_ENCOUNTER_UUID + "/_history")
		        .accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isNotFound());
	}
	
	private MockHttpServletResponse getEncounterHistoryRequest() throws IOException, ServletException {
		return get("/Encounter/" + ENCOUNTER_UUID + "/_history").accept(FhirMediaTypes.JSON).go();
	}
	
	@Test
	@Ignore
	public void createEncounter_shouldCreateNewEncounter() throws Exception {
		String encounterJson;
		try (InputStream is = this.getClass().getClassLoader().getResourceAsStream(JSON_CREATE_ENCOUNTER_PATH)) {
			Objects.requireNonNull(is);
			encounterJson = IOUtils.toString(is, StandardCharsets.UTF_8);
		}
		
		when(encounterService.create(any(Encounter.class))).thenReturn(encounter);
		
		MockHttpServletResponse response = post("/Encounter").jsonContent(encounterJson).accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isCreated());
		assertThat(response.getStatus(), is(201));
	}
	
	@Test
	@Ignore
	public void updateEncounter_shouldUpdateRequestedEncounter() throws Exception {
		String encounterJson;
		try (InputStream is = this.getClass().getClassLoader().getResourceAsStream(JSON_UPDATE_ENCOUNTER_PATH)) {
			Objects.requireNonNull(is);
			encounterJson = IOUtils.toString(is, StandardCharsets.UTF_8);
		}
		
		when(encounterService.update(any(String.class), any(Encounter.class))).thenReturn(encounter);
		
		MockHttpServletResponse response = put("/Encounter/" + ENCOUNTER_UUID).jsonContent(encounterJson)
		        .accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isOk());
	}
	
	@Test
	@Ignore
	public void updateEncounter_shouldErrorForNoId() throws Exception {
		String encounterJson;
		try (InputStream is = this.getClass().getClassLoader().getResourceAsStream(JSON_UPDATE_ENCOUNTER_NO_ID_PATH)) {
			Objects.requireNonNull(is);
			encounterJson = IOUtils.toString(is, StandardCharsets.UTF_8);
		}
		
		MockHttpServletResponse response = put("/Encounter/" + ENCOUNTER_UUID).jsonContent(encounterJson)
		        .accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isBadRequest());
		assertThat(response.getContentAsString(), containsStringIgnoringCase("body must contain an ID element for update"));
	}
	
	@Test
	@Ignore
	public void updateEncounter_shouldErrorForIdMissMatch() throws Exception {
		String encounterJson;
		try (InputStream is = this.getClass().getClassLoader().getResourceAsStream(JSON_UPDATE_ENCOUNTER_WRONG_ID_PATH)) {
			Objects.requireNonNull(is);
			encounterJson = IOUtils.toString(is, StandardCharsets.UTF_8);
		}
		
		MockHttpServletResponse response = put("/Encounter/" + WRONG_ENCOUNTER_UUID).jsonContent(encounterJson)
		        .accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isBadRequest());
		assertThat(response.getContentAsString(),
		    containsStringIgnoringCase("body must contain an ID element which matches the request URL"));
	}
	
	@Test
	@Ignore
	public void deleteEncounter_shouldDeleteEncounter() throws Exception {
		when(encounterService.delete(ENCOUNTER_UUID)).thenReturn(encounter);
		
		MockHttpServletResponse response = delete("/Encounter/" + ENCOUNTER_UUID).accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), equalTo(FhirMediaTypes.JSON.toString()));
	}
	
	@Test
	@Ignore
	public void deleteEncounter_shouldReturn404ForNonExistingEncounter() throws Exception {
		when(encounterService.delete(WRONG_ENCOUNTER_UUID)).thenReturn(null);
		
		MockHttpServletResponse response = delete("/Encounter/" + WRONG_ENCOUNTER_UUID).accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isNotFound());
	}
}
