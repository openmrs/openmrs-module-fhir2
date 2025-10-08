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

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
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
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.openmrs.module.fhir2.api.util.GeneralUtils.inputStreamToString;

import javax.servlet.ServletException;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import ca.uhn.fhir.rest.param.DateRangeParam;
import ca.uhn.fhir.rest.param.HasAndListParam;
import ca.uhn.fhir.rest.param.HasOrListParam;
import ca.uhn.fhir.rest.param.ReferenceAndListParam;
import ca.uhn.fhir.rest.param.ReferenceOrListParam;
import ca.uhn.fhir.rest.param.ReferenceParam;
import ca.uhn.fhir.rest.param.TokenAndListParam;
import ca.uhn.fhir.rest.param.TokenOrListParam;
import ca.uhn.fhir.rest.param.TokenParam;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
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
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.api.FhirEncounterService;
import org.openmrs.module.fhir2.api.search.param.EncounterSearchParams;
import org.springframework.mock.web.MockHttpServletResponse;

@RunWith(MockitoJUnitRunner.class)
public class EncounterFhirResourceProviderWebTest extends BaseFhirR4ResourceProviderWebTest<EncounterFhirResourceProvider, Encounter> {
	
	private static final String ENCOUNTER_UUID = "6519d653-393b-4118-9c83-a3715b82d4ac"; // encounter 3 from standard test dataset
	
	private static final String ENCOUNTER_TYPE_UUID = "07000be2-26b6-4cce-8b40-866d8435b613";
	
	private static final String WRONG_ENCOUNTER_UUID = "9bf0d1ac-62a8-4440-a5a1-eb1015a7cc65";
	
	private static final String PATIENT_IDENTIFIER = "h43489-h";
	
	private static final String PATIENT_UUID = "5946f880-b197-400b-9caa-a3c661d23041"; // patient 7 from the standard test dataset
	
	private static final String PATIENT_GIVEN_NAME = "Hannibal";
	
	private static final String PATIENT_FAMILY_NAME = "Sid";
	
	private static final String ENCOUNTER_ADDRESS_CITY = "Boston";
	
	private static final String ENCOUNTER_ADDRESS_COUNTRY = "INDIA";
	
	private static final String ENCOUNTER_ADDRESS_STATE = "MA";
	
	private static final String ENCOUNTER_POSTALCODE = "248001";
	
	private static final String PARTICIPANT_GIVEN_NAME = "John";
	
	private static final String PARTICIPANT_FAMILY_NAME = "Doe";
	
	private static final String PARTICIPANT_IDENTIFIER = "1000WF";
	
	private static final String LOCATION_UUID = "9356400c-a5a2-4532-8f2b-2361b3446eb8"; // location 2 from the standard test dataset
	
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
	private ArgumentCaptor<EncounterSearchParams> paramCaptor;
	
	@Captor
	private ArgumentCaptor<TokenParam> tokenCaptor;
	
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
		assertThat(response.getContentType(), containsString(FhirMediaTypes.JSON.toString()));
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
		verify(encounterService).searchForEncounters(paramCaptor.capture());
		ReferenceAndListParam p = paramCaptor.getValue().getSubject();
		assertThat(p, notNullValue());
		assertThat(p.getValuesAsQueryTokens().get(0).getValuesAsQueryTokens().get(0).getIdPart(), equalTo(PATIENT_UUID));
	}
	
	@Test
	public void shouldGetEncountersByDate() throws Exception {
		verifyUri("/Encounter/?date=ge1975-02-02");
		verify(encounterService).searchForEncounters(paramCaptor.capture());
		
		DateRangeParam p = paramCaptor.getValue().getDate();
		assertThat(p, notNullValue());
		
		Calendar calendar = Calendar.getInstance();
		calendar.set(1975, Calendar.FEBRUARY, 2);
		
		assertThat(p.getLowerBound().getValue(), equalTo(DateUtils.truncate(calendar.getTime(), Calendar.DATE)));
		assertThat(p.getUpperBound(), nullValue());
	}
	
	@Test
	public void shouldGetEncountersByLocationUUID() throws Exception {
		verifyUri(String.format("/Encounter/?location=%s", LOCATION_UUID));
		
		verify(encounterService).searchForEncounters(paramCaptor.capture());
		
		List<ReferenceOrListParam> orListParams = paramCaptor.getValue().getLocation().getValuesAsQueryTokens();
		ReferenceParam referenceParam = orListParams.get(0).getValuesAsQueryTokens().get(0);
		
		assertThat(paramCaptor.getValue().getLocation(), notNullValue());
		assertThat(referenceParam.getChain(), equalTo(null));
		assertThat(referenceParam.getValue(), equalTo(LOCATION_UUID));
	}
	
	@Test
	public void shouldGetEncountersByLocationCityVillage() throws Exception {
		verifyUri(String.format("/Encounter/?location.address-city=%s", ENCOUNTER_ADDRESS_CITY));
		
		verify(encounterService).searchForEncounters(paramCaptor.capture());
		
		ReferenceAndListParam listParam = paramCaptor.getValue().getLocation();
		assertThat(listParam, notNullValue());
		ReferenceParam firstParam = listParam.getValuesAsQueryTokens().get(0).getValuesAsQueryTokens().get(0);
		assertThat(firstParam.getChain(), equalTo("address-city"));
		assertThat(firstParam.getValue(), equalTo(ENCOUNTER_ADDRESS_CITY));
	}
	
	@Test
	public void shouldGetEncountersByLocationState() throws Exception {
		verifyUri(String.format("/Encounter/?location.address-state=%s", ENCOUNTER_ADDRESS_STATE));
		
		verify(encounterService).searchForEncounters(paramCaptor.capture());
		
		ReferenceAndListParam listParam = paramCaptor.getValue().getLocation();
		assertThat(listParam, notNullValue());
		ReferenceParam firstParam = listParam.getValuesAsQueryTokens().get(0).getValuesAsQueryTokens().get(0);
		assertThat(firstParam.getChain(), equalTo("address-state"));
		assertThat(firstParam.getValue(), equalTo(ENCOUNTER_ADDRESS_STATE));
	}
	
	@Test
	public void shouldGetEncountersByLocationPostalCode() throws Exception {
		verifyUri(String.format("/Encounter/?location.address-postalcode=%s", ENCOUNTER_POSTALCODE));
		
		verify(encounterService).searchForEncounters(paramCaptor.capture());
		
		ReferenceAndListParam listParam = paramCaptor.getValue().getLocation();
		assertThat(listParam, notNullValue());
		ReferenceParam firstParam = listParam.getValuesAsQueryTokens().get(0).getValuesAsQueryTokens().get(0);
		assertThat(firstParam.getChain(), equalTo("address-postalcode"));
		assertThat(firstParam.getValue(), equalTo(ENCOUNTER_POSTALCODE));
	}
	
	@Test
	public void shouldGetEncountersByLocationCountry() throws Exception {
		verifyUri(String.format("/Encounter/?location.address-country=%s", ENCOUNTER_ADDRESS_COUNTRY));
		
		verify(encounterService).searchForEncounters(paramCaptor.capture());
		
		ReferenceAndListParam listParam = paramCaptor.getValue().getLocation();
		assertThat(listParam, notNullValue());
		ReferenceParam firstParam = listParam.getValuesAsQueryTokens().get(0).getValuesAsQueryTokens().get(0);
		assertThat(firstParam.getChain(), equalTo("address-country"));
		assertThat(firstParam.getValue(), equalTo(ENCOUNTER_ADDRESS_COUNTRY));
	}
	
	@Test
	public void shouldGetEncountersByLocationCountryWithOr() throws Exception {
		verifyUri(String.format("/Encounter/?location.address-country=%s,%s", ENCOUNTER_ADDRESS_COUNTRY, "USA"));
		
		verify(encounterService).searchForEncounters(paramCaptor.capture());
		
		ReferenceAndListParam andParams = paramCaptor.getValue().getLocation();
		assertThat(andParams, notNullValue());
		assertThat(andParams.getValuesAsQueryTokens().size(), equalTo(1));
		
		List<ReferenceParam> orParams = andParams.getValuesAsQueryTokens().get(0).getValuesAsQueryTokens();
		assertThat(orParams.size(), equalTo(2));
		assertThat(orParams.get(0).getChain(), equalTo("address-country"));
		assertThat(orParams.get(0).getValue(), equalTo(ENCOUNTER_ADDRESS_COUNTRY));
		assertThat(orParams.get(1).getChain(), equalTo("address-country"));
		assertThat(orParams.get(1).getValue(), equalTo("USA"));
	}
	
	@Test
	public void shouldGetEncountersByLocationCountryWithAnd() throws Exception {
		verifyUri("/Encounter/?location.address-country=INDIA&location.address-country=USA");
		
		verify(encounterService).searchForEncounters(paramCaptor.capture());
		
		ReferenceAndListParam andParams = paramCaptor.getValue().getLocation();
		assertThat(andParams, notNullValue());
		assertThat(andParams.getValuesAsQueryTokens().size(), equalTo(2));
		
		ReferenceOrListParam param1 = andParams.getValuesAsQueryTokens().get(0);
		assertThat(param1.getValuesAsQueryTokens().size(), equalTo(1));
		assertThat(param1.getValuesAsQueryTokens().get(0).getChain(), equalTo("address-country"));
		assertThat(param1.getValuesAsQueryTokens().get(0).getValue(), equalTo(ENCOUNTER_ADDRESS_COUNTRY));
		
		ReferenceOrListParam param2 = andParams.getValuesAsQueryTokens().get(1);
		assertThat(param2.getValuesAsQueryTokens().size(), equalTo(1));
		assertThat(param2.getValuesAsQueryTokens().get(0).getChain(), equalTo("address-country"));
		assertThat(param2.getValuesAsQueryTokens().get(0).getValue(), equalTo("USA"));
	}
	
	@Test
	public void shouldGetEncountersByParticipantGivenName() throws Exception {
		verifyUri(String.format("/Encounter/?participant:Practitioner.given=%s", PARTICIPANT_GIVEN_NAME));
		
		verify(encounterService).searchForEncounters(paramCaptor.capture());
		
		List<ReferenceOrListParam> orListParams = paramCaptor.getValue().getParticipant().getValuesAsQueryTokens();
		ReferenceParam referenceParam = orListParams.get(0).getValuesAsQueryTokens().get(0);
		
		assertThat(paramCaptor.getValue().getParticipant(), notNullValue());
		assertThat(referenceParam.getChain(), equalTo("given"));
		assertThat(referenceParam.getValue(), equalTo(PARTICIPANT_GIVEN_NAME));
	}
	
	@Test
	public void shouldGetEncountersByParticipantFamilyName() throws Exception {
		verifyUri(String.format("/Encounter/?participant:Practitioner.family=%s", PARTICIPANT_FAMILY_NAME));
		
		verify(encounterService).searchForEncounters(paramCaptor.capture());
		
		List<ReferenceOrListParam> orListParams = paramCaptor.getValue().getParticipant().getValuesAsQueryTokens();
		ReferenceParam referenceParam = orListParams.get(0).getValuesAsQueryTokens().get(0);
		
		assertThat(paramCaptor.getValue().getParticipant(), notNullValue());
		assertThat(referenceParam.getChain(), equalTo("family"));
		assertThat(referenceParam.getValue(), equalTo(PARTICIPANT_FAMILY_NAME));
	}
	
	@Test
	public void shouldGetEncountersByParticipantFamilyNameWithOr() throws Exception {
		verifyUri(String.format("/Encounter/?participant:Practitioner.family=%s,%s", PARTICIPANT_FAMILY_NAME, "Vox"));
		
		verify(encounterService).searchForEncounters(paramCaptor.capture());
		
		List<ReferenceOrListParam> orListParams = paramCaptor.getValue().getParticipant().getValuesAsQueryTokens();
		ReferenceParam referenceParam = orListParams.get(0).getValuesAsQueryTokens().get(0);
		
		assertThat(paramCaptor.getValue().getParticipant(), notNullValue());
		assertThat(referenceParam.getChain(), equalTo("family"));
		assertThat(referenceParam.getValue(), equalTo(PARTICIPANT_FAMILY_NAME));
		assertThat(orListParams.get(0).getValuesAsQueryTokens().size(), equalTo(2));
	}
	
	@Test
	public void shouldGetEncountersByParticipantFamilyNameWithAnd() throws Exception {
		verifyUri(String.format("/Encounter/?participant:Practitioner.family=%s&participant:Practitioner.family=%s",
		    PARTICIPANT_FAMILY_NAME, "Vox"));
		
		verify(encounterService).searchForEncounters(paramCaptor.capture());
		
		List<ReferenceOrListParam> orListParams = paramCaptor.getValue().getParticipant().getValuesAsQueryTokens();
		ReferenceParam referenceParam = orListParams.get(0).getValuesAsQueryTokens().get(0);
		
		assertThat(paramCaptor.getValue().getParticipant(), notNullValue());
		assertThat(referenceParam.getChain(), equalTo("family"));
		assertThat(referenceParam.getValue(), equalTo(PARTICIPANT_FAMILY_NAME));
		assertThat(paramCaptor.getValue().getParticipant().getValuesAsQueryTokens().size(), equalTo(2));
	}
	
	@Test
	public void shouldGetEncountersByParticipantIdentifier() throws Exception {
		verifyUri(String.format("/Encounter/?participant:Practitioner.identifier=%s,%s", PARTICIPANT_IDENTIFIER,
		    "op87yh-34fd-34egs-56h34-34f7"));
		
		verify(encounterService).searchForEncounters(paramCaptor.capture());
		
		List<ReferenceOrListParam> orListParams = paramCaptor.getValue().getParticipant().getValuesAsQueryTokens();
		ReferenceParam referenceParam = orListParams.get(0).getValuesAsQueryTokens().get(0);
		
		assertThat(paramCaptor.getValue().getParticipant(), notNullValue());
		assertThat(referenceParam.getChain(), equalTo("identifier"));
		assertThat(referenceParam.getValue(), equalTo(PARTICIPANT_IDENTIFIER));
		assertThat(orListParams.get(0).getValuesAsQueryTokens().size(), equalTo(2));
	}
	
	@Test
	public void shouldGetEncountersBySubjectGivenName() throws Exception {
		verifyUri(String.format("/Encounter/?subject.given=%s", PATIENT_GIVEN_NAME));
		
		verify(encounterService).searchForEncounters(paramCaptor.capture());
		
		List<ReferenceOrListParam> orListParams = paramCaptor.getValue().getSubject().getValuesAsQueryTokens();
		ReferenceParam referenceParam = orListParams.get(0).getValuesAsQueryTokens().get(0);
		
		assertThat(paramCaptor.getValue().getSubject(), notNullValue());
		assertThat(referenceParam.getChain(), equalTo("given"));
		assertThat(referenceParam.getValue(), equalTo(PATIENT_GIVEN_NAME));
	}
	
	@Test
	public void shouldGetEncountersBySubjectFamilyName() throws Exception {
		verifyUri(String.format("/Encounter?subject.family=%s", PATIENT_FAMILY_NAME));
		
		verify(encounterService).searchForEncounters(paramCaptor.capture());
		
		List<ReferenceOrListParam> orListParams = paramCaptor.getValue().getSubject().getValuesAsQueryTokens();
		ReferenceParam referenceParam = orListParams.get(0).getValuesAsQueryTokens().get(0);
		
		assertThat(paramCaptor.getValue().getSubject(), notNullValue());
		assertThat(referenceParam.getChain(), equalTo("family"));
		assertThat(referenceParam.getValue(), equalTo(PATIENT_FAMILY_NAME));
	}
	
	@Test
	public void shouldGetEncountersBySubjectIdentifier() throws Exception {
		verifyUri(String.format("/Encounter?subject.identifier=%s", PATIENT_IDENTIFIER));
		
		verify(encounterService).searchForEncounters(paramCaptor.capture());
		
		List<ReferenceOrListParam> orListParams = paramCaptor.getValue().getSubject().getValuesAsQueryTokens();
		ReferenceParam referenceParam = orListParams.get(0).getValuesAsQueryTokens().get(0);
		
		assertThat(paramCaptor.getValue().getSubject(), notNullValue());
		assertThat(referenceParam.getChain(), equalTo("identifier"));
		assertThat(referenceParam.getValue(), equalTo(PATIENT_IDENTIFIER));
	}
	
	@Test
	public void shouldGetEncountersBySubjectGivenNameAndLocationPostalCode() throws Exception {
		verifyUri("/Encounter?subject.given=Hannibal&location.address-postalcode=248001");
		
		verify(encounterService).searchForEncounters(paramCaptor.capture());
		
		List<ReferenceOrListParam> orListParamsSubject = paramCaptor.getValue().getSubject().getValuesAsQueryTokens();
		ReferenceParam referenceParamSubject = orListParamsSubject.get(0).getValuesAsQueryTokens().get(0);
		
		List<ReferenceOrListParam> orListParamsLocation = paramCaptor.getValue().getLocation().getValuesAsQueryTokens();
		ReferenceParam referenceParamLocation = orListParamsLocation.get(0).getValuesAsQueryTokens().get(0);
		
		assertThat(paramCaptor.getValue().getSubject(), notNullValue());
		assertThat(referenceParamSubject.getChain(), equalTo("given"));
		assertThat(referenceParamSubject.getValue(), equalTo(PATIENT_GIVEN_NAME));
		assertThat(paramCaptor.getValue().getLocation(), notNullValue());
		assertThat(referenceParamLocation.getChain(), equalTo("address-postalcode"));
		assertThat(referenceParamLocation.getValue(), equalTo(ENCOUNTER_POSTALCODE));
	}
	
	@Test
	public void shouldGetEncountersBySubjectGivenNameAndLocationPostalCodeWithOr() throws Exception {
		verifyUri("/Encounter?subject.given=Hannibal&location.address-postalcode=248001,854796");
		
		verify(encounterService).searchForEncounters(paramCaptor.capture());
		
		List<ReferenceOrListParam> orListParamsSubject = paramCaptor.getValue().getSubject().getValuesAsQueryTokens();
		ReferenceParam referenceParamSubject = orListParamsSubject.get(0).getValuesAsQueryTokens().get(0);
		
		List<ReferenceOrListParam> orListParamsLocation = paramCaptor.getValue().getLocation().getValuesAsQueryTokens();
		ReferenceParam referenceParamLocation = orListParamsLocation.get(0).getValuesAsQueryTokens().get(0);
		
		assertThat(paramCaptor.getValue().getSubject(), notNullValue());
		assertThat(referenceParamSubject.getChain(), equalTo("given"));
		assertThat(referenceParamSubject.getValue(), equalTo(PATIENT_GIVEN_NAME));
		assertThat(paramCaptor.getValue().getLocation(), notNullValue());
		assertThat(referenceParamLocation.getChain(), equalTo("address-postalcode"));
		assertThat(referenceParamLocation.getValue(), equalTo(ENCOUNTER_POSTALCODE));
		assertThat(orListParamsLocation.get(0).getValuesAsQueryTokens().size(), equalTo(2));
	}
	
	@Test
	public void shouldGetEncountersBySubjectGivenNameAndLocationPostalCodeWithAnd() throws Exception {
		verifyUri("/Encounter?subject.given=Hannibal&location.address-postalcode=248001&location.address-postalcode=854796");
		
		verify(encounterService).searchForEncounters(paramCaptor.capture());
		
		List<ReferenceOrListParam> orListParamsSubject = paramCaptor.getValue().getSubject().getValuesAsQueryTokens();
		ReferenceParam referenceParamSubject = orListParamsSubject.get(0).getValuesAsQueryTokens().get(0);
		
		List<ReferenceOrListParam> orListParamsLocation = paramCaptor.getValue().getLocation().getValuesAsQueryTokens();
		ReferenceParam referenceParamLocation = orListParamsLocation.get(0).getValuesAsQueryTokens().get(0);
		
		assertThat(paramCaptor.getValue().getSubject(), notNullValue());
		assertThat(referenceParamSubject.getChain(), equalTo("given"));
		assertThat(referenceParamSubject.getValue(), equalTo(PATIENT_GIVEN_NAME));
		assertThat(paramCaptor.getValue().getLocation(), notNullValue());
		assertThat(referenceParamLocation.getChain(), equalTo("address-postalcode"));
		assertThat(referenceParamLocation.getValue(), equalTo(ENCOUNTER_POSTALCODE));
		assertThat(paramCaptor.getValue().getLocation().getValuesAsQueryTokens().size(), equalTo(2));
	}
	
	@Test
	public void shouldGetEncountersByParticipantIdentifierAndLocationPostalCode() throws Exception {
		verifyUri("/Encounter?participant:Practitioner.identifier=1000WF&location.address-postalcode=248001");
		
		verify(encounterService).searchForEncounters(paramCaptor.capture());
		
		List<ReferenceOrListParam> orListParamsParticipant = paramCaptor.getValue().getParticipant()
		        .getValuesAsQueryTokens();
		ReferenceParam referenceParamParticipant = orListParamsParticipant.get(0).getValuesAsQueryTokens().get(0);
		
		List<ReferenceOrListParam> orListParamsLocation = paramCaptor.getValue().getLocation().getValuesAsQueryTokens();
		ReferenceParam referenceParamLocation = orListParamsLocation.get(0).getValuesAsQueryTokens().get(0);
		
		assertThat(paramCaptor.getValue().getParticipant(), notNullValue());
		assertThat(referenceParamParticipant.getChain(), equalTo("identifier"));
		assertThat(referenceParamParticipant.getValue(), equalTo(PARTICIPANT_IDENTIFIER));
		assertThat(paramCaptor.getValue().getLocation(), notNullValue());
		assertThat(referenceParamLocation.getChain(), equalTo("address-postalcode"));
		assertThat(referenceParamLocation.getValue(), equalTo(ENCOUNTER_POSTALCODE));
	}
	
	@Test
	public void shouldGetEncountersByParticipantIdentifierAndDate() throws Exception {
		verifyUri("/Encounter?participant:Practitioner.identifier=1000WF,670WD&date=ge1975-02-02");
		
		verify(encounterService).searchForEncounters(paramCaptor.capture());
		
		List<ReferenceOrListParam> orListParamsParticipant = paramCaptor.getValue().getParticipant()
		        .getValuesAsQueryTokens();
		ReferenceParam referenceParamParticipant = orListParamsParticipant.get(0).getValuesAsQueryTokens().get(0);
		
		assertThat(paramCaptor.getValue().getParticipant(), notNullValue());
		assertThat(referenceParamParticipant.getChain(), equalTo("identifier"));
		assertThat(referenceParamParticipant.getValue(), equalTo(PARTICIPANT_IDENTIFIER));
		assertThat(orListParamsParticipant.get(0).getValuesAsQueryTokens().size(), equalTo(2));
		
		Calendar calendar = Calendar.getInstance();
		calendar.set(1975, Calendar.FEBRUARY, 2);
		assertThat(paramCaptor.getValue().getDate(), notNullValue());
		assertThat(paramCaptor.getValue().getDate().getLowerBound().getValue(),
		    equalTo(DateUtils.truncate(calendar.getTime(), Calendar.DATE)));
		assertThat(paramCaptor.getValue().getDate().getUpperBound(), nullValue());
	}
	
	@Test
	public void shouldGetEncountersByUUID() throws Exception {
		verifyUri(String.format("/Encounter?_id=%s", ENCOUNTER_UUID));
		
		verify(encounterService).searchForEncounters(paramCaptor.capture());
		
		assertThat(paramCaptor.getValue().getId(), notNullValue());
		assertThat(paramCaptor.getValue().getId().getValuesAsQueryTokens(), not(empty()));
		assertThat(paramCaptor.getValue().getId().getValuesAsQueryTokens().get(0).getValuesAsQueryTokens().get(0).getValue(),
		    equalTo(ENCOUNTER_UUID));
	}
	
	@Test
	public void shouldGetEncountersByTypeUUID() throws Exception {
		verifyUri(String.format("/Encounter?type=%s", ENCOUNTER_TYPE_UUID));
		
		verify(encounterService).searchForEncounters(paramCaptor.capture());
		
		assertThat(paramCaptor.getValue().getEncounterType(), notNullValue());
		assertThat(paramCaptor.getValue().getEncounterType().getValuesAsQueryTokens(), not(empty()));
		assertThat(paramCaptor.getValue().getEncounterType().getValuesAsQueryTokens().get(0).getValuesAsQueryTokens().get(0)
		        .getValue(),
		    equalTo(ENCOUNTER_TYPE_UUID));
	}
	
	@Test
	public void shouldGetEncountersByLastUpdatedDate() throws Exception {
		verifyUri(String.format("/Encounter?_lastUpdated=%s", LAST_UPDATED_DATE));
		
		verify(encounterService).searchForEncounters(paramCaptor.capture());
		
		assertThat(paramCaptor.getValue().getLastUpdated(), notNullValue());
		
		Calendar calendar = Calendar.getInstance();
		calendar.set(2020, Calendar.SEPTEMBER, 3);
		
		assertThat(paramCaptor.getValue().getLastUpdated().getLowerBound().getValue(),
		    equalTo(org.apache.commons.lang3.time.DateUtils.truncate(calendar.getTime(), Calendar.DATE)));
		assertThat(paramCaptor.getValue().getLastUpdated().getUpperBound().getValue(),
		    equalTo(org.apache.commons.lang3.time.DateUtils.truncate(calendar.getTime(), Calendar.DATE)));
	}
	
	@Test
	public void shouldAddPatientsWithReturnedEncounters() throws Exception {
		verifyUri("/Encounter?_include=Encounter:patient");
		
		verify(encounterService).searchForEncounters(paramCaptor.capture());
		
		assertThat(paramCaptor.getValue().getIncludes(), notNullValue());
		assertThat(paramCaptor.getValue().getIncludes().size(), equalTo(1));
		assertThat(paramCaptor.getValue().getIncludes().iterator().next().getParamName(),
		    equalTo(FhirConstants.INCLUDE_PATIENT_PARAM));
		assertThat(paramCaptor.getValue().getIncludes().iterator().next().getParamType(), equalTo(FhirConstants.ENCOUNTER));
	}
	
	@Test
	public void shouldAddLocationsWithReturnedEncounters() throws Exception {
		verifyUri("/Encounter?_include=Encounter:location");
		
		verify(encounterService).searchForEncounters(paramCaptor.capture());
		
		assertThat(paramCaptor.getValue().getIncludes(), notNullValue());
		assertThat(paramCaptor.getValue().getIncludes().size(), equalTo(1));
		assertThat(paramCaptor.getValue().getIncludes().iterator().next().getParamName(),
		    equalTo(FhirConstants.INCLUDE_LOCATION_PARAM));
		assertThat(paramCaptor.getValue().getIncludes().iterator().next().getParamType(), equalTo(FhirConstants.ENCOUNTER));
	}
	
	@Test
	public void shouldAddParticipantsWithReturnedEncounters() throws Exception {
		verifyUri("/Encounter?_include=Encounter:participant");
		
		verify(encounterService).searchForEncounters(paramCaptor.capture());
		
		assertThat(paramCaptor.getValue().getIncludes(), notNullValue());
		assertThat(paramCaptor.getValue().getIncludes().size(), equalTo(1));
		assertThat(paramCaptor.getValue().getIncludes().iterator().next().getParamName(),
		    equalTo(FhirConstants.INCLUDE_PARTICIPANT_PARAM));
		assertThat(paramCaptor.getValue().getIncludes().iterator().next().getParamType(), equalTo(FhirConstants.ENCOUNTER));
	}
	
	@Test
	public void shouldHandleMultipleIncludes() throws Exception {
		verifyUri("/Encounter?_include=Encounter:participant&_include=Encounter:location");
		
		verify(encounterService).searchForEncounters(paramCaptor.capture());
		
		assertThat(paramCaptor.getValue().getIncludes(), notNullValue());
		assertThat(paramCaptor.getValue().getIncludes().size(), equalTo(2));
		
		assertThat(paramCaptor.getValue().getIncludes(),
		    hasItem(allOf(hasProperty("paramName", equalTo(FhirConstants.INCLUDE_LOCATION_PARAM)),
		        hasProperty("paramType", equalTo(FhirConstants.ENCOUNTER)))));
		assertThat(paramCaptor.getValue().getIncludes(),
		    hasItem(allOf(hasProperty("paramName", equalTo(FhirConstants.INCLUDE_PARTICIPANT_PARAM)),
		        hasProperty("paramType", equalTo(FhirConstants.ENCOUNTER)))));
	}
	
	@Test
	public void shouldAddObservationsWithReturnedEncounters() throws Exception {
		verifyUri("/Encounter?_revinclude=Observation:encounter");
		
		verify(encounterService).searchForEncounters(paramCaptor.capture());
		
		assertThat(paramCaptor.getValue().getRevIncludes(), notNullValue());
		assertThat(paramCaptor.getValue().getRevIncludes().size(), equalTo(1));
		assertThat(paramCaptor.getValue().getRevIncludes().iterator().next().getParamName(),
		    equalTo(FhirConstants.INCLUDE_ENCOUNTER_PARAM));
		assertThat(paramCaptor.getValue().getRevIncludes().iterator().next().getParamType(),
		    equalTo(FhirConstants.OBSERVATION));
	}
	
	@Test
	public void shouldAddDiagnosticReportsWithReturnedEncounters() throws Exception {
		verifyUri("/Encounter?_revinclude=DiagnosticReport:encounter");
		
		verify(encounterService).searchForEncounters(paramCaptor.capture());
		
		assertThat(paramCaptor.getValue().getRevIncludes(), notNullValue());
		assertThat(paramCaptor.getValue().getRevIncludes().size(), equalTo(1));
		assertThat(paramCaptor.getValue().getRevIncludes().iterator().next().getParamName(),
		    equalTo(FhirConstants.INCLUDE_ENCOUNTER_PARAM));
		assertThat(paramCaptor.getValue().getRevIncludes().iterator().next().getParamType(),
		    equalTo(FhirConstants.DIAGNOSTIC_REPORT));
	}
	
	@Test
	public void shouldAddMedicationRequestsWithReturnedEncounters() throws Exception {
		verifyUri("/Encounter?_revinclude=MedicationRequest:encounter");
		
		verify(encounterService).searchForEncounters(paramCaptor.capture());
		
		assertThat(paramCaptor.getValue().getRevIncludes(), notNullValue());
		assertThat(paramCaptor.getValue().getRevIncludes().size(), equalTo(1));
		assertThat(paramCaptor.getValue().getRevIncludes().iterator().next().getParamName(),
		    equalTo(FhirConstants.INCLUDE_ENCOUNTER_PARAM));
		assertThat(paramCaptor.getValue().getRevIncludes().iterator().next().getParamType(),
		    equalTo(FhirConstants.MEDICATION_REQUEST));
	}
	
	@Test
	public void shouldAddServiceRequestsWithReturnedEncounters() throws Exception {
		verifyUri("/Encounter?_revinclude=ServiceRequest:encounter");
		
		verify(encounterService).searchForEncounters(paramCaptor.capture());
		
		assertThat(paramCaptor.getValue().getRevIncludes(), notNullValue());
		assertThat(paramCaptor.getValue().getRevIncludes().size(), equalTo(1));
		assertThat(paramCaptor.getValue().getRevIncludes().iterator().next().getParamName(),
		    equalTo(FhirConstants.INCLUDE_ENCOUNTER_PARAM));
		assertThat(paramCaptor.getValue().getRevIncludes().iterator().next().getParamType(),
		    equalTo(FhirConstants.SERVICE_REQUEST));
	}
	
	@Test
	public void shouldHandleMultipleReverseIncludes() throws Exception {
		verifyUri("/Encounter?_revinclude=DiagnosticReport:encounter&_revinclude=Observation:encounter");
		
		verify(encounterService).searchForEncounters(paramCaptor.capture());
		
		assertThat(paramCaptor.getValue().getRevIncludes(), notNullValue());
		assertThat(paramCaptor.getValue().getRevIncludes().size(), equalTo(2));
		
		assertThat(paramCaptor.getValue().getRevIncludes(),
		    hasItem(allOf(hasProperty("paramName", equalTo(FhirConstants.INCLUDE_ENCOUNTER_PARAM)),
		        hasProperty("paramType", equalTo(FhirConstants.OBSERVATION)))));
		assertThat(paramCaptor.getValue().getRevIncludes(),
		    hasItem(allOf(hasProperty("paramName", equalTo(FhirConstants.INCLUDE_ENCOUNTER_PARAM)),
		        hasProperty("paramType", equalTo(FhirConstants.DIAGNOSTIC_REPORT)))));
	}
	
	@Test
	public void shouldHandleIterativeMedicationDispenseReverseInclude() throws Exception {
		verifyUri("/Encounter?_revinclude=MedicationRequest:encounter&_revinclude:iterate=MedicationDispense:prescription");
		
		verify(encounterService).searchForEncounters(paramCaptor.capture());
		
		assertThat(paramCaptor.getValue().getRevIncludes(), notNullValue());
		assertThat(paramCaptor.getValue().getRevIncludes().size(), equalTo(2));
		
		assertThat(paramCaptor.getValue().getRevIncludes(),
		    hasItem(allOf(hasProperty("paramName", equalTo(FhirConstants.INCLUDE_ENCOUNTER_PARAM)),
		        hasProperty("paramType", equalTo(FhirConstants.MEDICATION_REQUEST)))));
		assertThat(paramCaptor.getValue().getRevIncludes(),
		    hasItem(allOf(hasProperty("paramName", equalTo(FhirConstants.INCLUDE_PRESCRIPTION_PARAM)),
		        hasProperty("paramType", equalTo(FhirConstants.MEDICATION_DISPENSE)))));
	}
	
	@Test
	public void shouldHandleHasAndListParameter() throws Exception {
		verifyUri(
		    "/Encounter?_has:MedicationRequest:encounter:intent=order&_has:MedicationRequest:encounter:status=active,draft");
		
		verify(encounterService).searchForEncounters(paramCaptor.capture());
		
		HasAndListParam hasAndListParam = paramCaptor.getValue().getHasAndListParam();
		assertThat(hasAndListParam, notNullValue());
		assertThat(hasAndListParam.size(), equalTo(2));
		
		List<HasOrListParam> hasOrListParams = hasAndListParam.getValuesAsQueryTokens();
		assertThat(hasOrListParams.size(), equalTo(2));
		
		List<String> valuesFound = new ArrayList<>();
		
		for (HasOrListParam hasOrListParam : hasOrListParams) {
			hasOrListParam.getValuesAsQueryTokens().forEach(hasParam -> {
				assertThat(hasParam.getTargetResourceType(), equalTo(FhirConstants.MEDICATION_REQUEST));
				assertThat(hasParam.getReferenceFieldName(), equalTo("encounter"));
				valuesFound.add(hasParam.getParameterName() + "=" + hasParam.getParameterValue());
			});
		}
		Collections.sort(valuesFound);
		
		assertThat(valuesFound.size(), equalTo(3));
		assertThat(valuesFound.get(0), equalTo("intent=order"));
		assertThat(valuesFound.get(1), equalTo("status=active"));
		assertThat(valuesFound.get(2), equalTo("status=draft"));
	}
	
	@Test
	public void shouldHandleTagParameter() throws Exception {
		verifyUri("/Encounter?_tag=http://fhir.openmrs.org/ext/encounter-tag|encounter");
		
		verify(encounterService).searchForEncounters(paramCaptor.capture());
		
		TokenAndListParam tagParam = paramCaptor.getValue().getTag();
		assertThat(tagParam, notNullValue());
		List<TokenOrListParam> tokenOrListParams = tagParam.getValuesAsQueryTokens();
		assertThat(tokenOrListParams.size(), equalTo(1));
		TokenOrListParam tokenOrListParam = tokenOrListParams.get(0);
		assertThat(tokenOrListParam.getValuesAsQueryTokens().size(), equalTo(1));
		TokenParam tokenParam = tokenOrListParam.getValuesAsQueryTokens().get(0);
		assertThat(tokenParam.getSystem(), equalTo(FhirConstants.OPENMRS_FHIR_EXT_ENCOUNTER_TAG));
		assertThat(tokenParam.getValue(), equalTo("encounter"));
	}
	
	private void verifyUri(String uri) throws Exception {
		Encounter encounter = new Encounter();
		encounter.setId(ENCOUNTER_UUID);
		when(encounterService.searchForEncounters(any()))
		        .thenReturn(new MockIBundleProvider<>(Collections.singletonList(encounter), 10, 1));
		
		MockHttpServletResponse response = get(uri).accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), containsString(FhirMediaTypes.JSON.toString()));
		
		Bundle results = readBundleResponse(response);
		assertThat(results.getEntry(), notNullValue());
		assertThat(results.getEntry(), not(empty()));
		assertThat(results.getEntry().get(0).getResource(), notNullValue());
		assertThat(results.getEntry().get(0).getResource().getIdElement().getIdPart(), equalTo(ENCOUNTER_UUID));
	}
	
	@Test
	public void createEncounter_shouldCreateNewEncounter() throws Exception {
		String encounterJson;
		try (InputStream is = this.getClass().getClassLoader().getResourceAsStream(JSON_CREATE_ENCOUNTER_PATH)) {
			Objects.requireNonNull(is);
			encounterJson = inputStreamToString(is, UTF_8);
		}
		
		when(encounterService.create(any(Encounter.class))).thenReturn(encounter);
		
		MockHttpServletResponse response = post("/Encounter").jsonContent(encounterJson).accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isCreated());
		assertThat(response.getStatus(), is(201));
	}
	
	@Test
	public void updateEncounter_shouldUpdateRequestedEncounter() throws Exception {
		String encounterJson;
		try (InputStream is = this.getClass().getClassLoader().getResourceAsStream(JSON_UPDATE_ENCOUNTER_PATH)) {
			Objects.requireNonNull(is);
			encounterJson = inputStreamToString(is, UTF_8);
		}
		
		when(encounterService.update(any(String.class), any(Encounter.class))).thenReturn(encounter);
		
		MockHttpServletResponse response = put("/Encounter/" + ENCOUNTER_UUID).jsonContent(encounterJson)
		        .accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isOk());
	}
	
	@Test
	public void updateEncounter_shouldErrorForNoId() throws Exception {
		String encounterJson;
		try (InputStream is = this.getClass().getClassLoader().getResourceAsStream(JSON_UPDATE_ENCOUNTER_NO_ID_PATH)) {
			Objects.requireNonNull(is);
			encounterJson = inputStreamToString(is, UTF_8);
		}
		
		MockHttpServletResponse response = put("/Encounter/" + ENCOUNTER_UUID).jsonContent(encounterJson)
		        .accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isBadRequest());
		assertThat(response.getContentAsString(), containsStringIgnoringCase("body must contain an ID element for update"));
	}
	
	@Test
	public void updateEncounter_shouldErrorForIdMissMatch() throws Exception {
		String encounterJson;
		try (InputStream is = this.getClass().getClassLoader().getResourceAsStream(JSON_UPDATE_ENCOUNTER_WRONG_ID_PATH)) {
			Objects.requireNonNull(is);
			encounterJson = inputStreamToString(is, UTF_8);
		}
		
		MockHttpServletResponse response = put("/Encounter/" + WRONG_ENCOUNTER_UUID).jsonContent(encounterJson)
		        .accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isBadRequest());
		assertThat(response.getContentAsString(),
		    containsStringIgnoringCase("body must contain an ID element which matches the request URL"));
	}
	
	@Test
	public void deleteEncounter_shouldDeleteEncounter() throws Exception {
		MockHttpServletResponse response = delete("/Encounter/" + ENCOUNTER_UUID).accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), containsString(FhirMediaTypes.JSON.toString()));
	}
	
	@Test
	public void deleteEncounter_shouldReturn404ForNonExistingEncounter() throws Exception {
		doThrow(new ResourceNotFoundException("")).when(encounterService).delete(WRONG_ENCOUNTER_UUID);
		
		MockHttpServletResponse response = delete("/Encounter/" + WRONG_ENCOUNTER_UUID).accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isNotFound());
	}
	
	@Test
	public void getEncounterEverything_shouldHandleEncounterId() throws Exception {
		verifyEverythingOperation("/Encounter/" + ENCOUNTER_UUID + "/$everything?");
		
		verify(encounterService).getEncounterEverything(tokenCaptor.capture());
		
		assertThat(tokenCaptor.getValue(), notNullValue());
		assertThat(tokenCaptor.getValue().getValue(), equalTo(ENCOUNTER_UUID));
	}
	
	private void verifyEverythingOperation(String uri) throws Exception {
		Encounter encounter = new Encounter();
		encounter.setId(ENCOUNTER_UUID);
		
		when(encounterService.getEncounterEverything(any()))
		        .thenReturn(new MockIBundleProvider<>(Collections.singletonList(encounter), 10, 1));
		
		MockHttpServletResponse response = get(uri).accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), containsString(FhirMediaTypes.JSON.toString()));
		
		Bundle results = readBundleResponse(response);
		assertThat(results.getEntry(), notNullValue());
		assertThat(results.getEntry(), not(empty()));
		assertThat(results.getEntry().get(0).getResource(), notNullValue());
		assertThat(results.getEntry().get(0).getResource().getIdElement().getIdPart(), equalTo(ENCOUNTER_UUID));
	}
}
