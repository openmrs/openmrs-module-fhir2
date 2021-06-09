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
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import javax.servlet.ServletException;

import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;

import ca.uhn.fhir.model.api.Include;
import ca.uhn.fhir.rest.param.DateRangeParam;
import ca.uhn.fhir.rest.param.NumberParam;
import ca.uhn.fhir.rest.param.QuantityAndListParam;
import ca.uhn.fhir.rest.param.ReferenceAndListParam;
import ca.uhn.fhir.rest.param.ReferenceOrListParam;
import ca.uhn.fhir.rest.param.ReferenceParam;
import ca.uhn.fhir.rest.param.StringAndListParam;
import ca.uhn.fhir.rest.param.TokenAndListParam;
import ca.uhn.fhir.rest.param.TokenOrListParam;
import lombok.AccessLevel;
import lombok.Getter;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.time.DateUtils;
import org.hamcrest.MatcherAssert;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Observation;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.api.FhirObservationService;
import org.springframework.mock.web.MockHttpServletResponse;

@RunWith(MockitoJUnitRunner.class)
public class ObservationFhirResourceProviderWebTest extends BaseFhirR4ResourceProviderWebTest<ObservationFhirResourceProvider, Observation> {
	
	private static final String OBS_UUID = "39fb7f47-e80a-4056-9285-bd798be13c63";
	
	private static final String BAD_OBS_UUID = "121b73a6-e1a4-4424-8610-d5765bf2fdf7";
	
	private static final String PATIENT_UUID = "d9bc6c12-6adc-4ca6-8bde-441ec1a1c344";
	
	private static final String MEMBER_UUID = "d9bc6c12-6adc-4ca6-8bde-441ec1a1c344";
	
	private static final String CIEL_URI = "https://openconceptlab.org/orgs/CIEL/sources/CIEL";
	
	private static final String LAST_UPDATED_DATE = "eq2020-09-03";
	
	private static final String URL_ENCODED_CIEL_URI;
	
	private static final String JSON_CREATE_OBSERVATION_PATH = "org/openmrs/module/fhir2/providers/ObservationWebTest_create.json";
	
	static {
		try {
			URL_ENCODED_CIEL_URI = URLEncoder.encode(CIEL_URI, "utf-8");
		}
		catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}
	
	@Getter(AccessLevel.PUBLIC)
	private ObservationFhirResourceProvider resourceProvider;
	
	@Mock
	private FhirObservationService observationService;
	
	@Captor
	private ArgumentCaptor<ReferenceAndListParam> patientCaptor;
	
	@Captor
	private ArgumentCaptor<ReferenceAndListParam> encounterCaptor;
	
	@Captor
	private ArgumentCaptor<TokenAndListParam> codeCaptor;
	
	@Captor
	private ArgumentCaptor<ReferenceAndListParam> memberCaptor;
	
	@Captor
	private ArgumentCaptor<TokenAndListParam> valueCodeCaptor;
	
	@Captor
	private ArgumentCaptor<DateRangeParam> dateCaptor;
	
	@Captor
	private ArgumentCaptor<QuantityAndListParam> valueQuantityCaptor;
	
	@Captor
	private ArgumentCaptor<StringAndListParam> stringAndListCaptor;
	
	@Captor
	private ArgumentCaptor<DateRangeParam> valueDateCaptor;
	
	@Captor
	private ArgumentCaptor<HashSet<Include>> includeArgumentCaptor;
	
	@Captor
	private ArgumentCaptor<NumberParam> maxCaptor;
	
	private Observation observation;
	
	@Before
	@Override
	public void setup() throws ServletException {
		resourceProvider = new ObservationFhirResourceProvider();
		resourceProvider.setObservationService(observationService);
		observation = new Observation();
		observation.setId(OBS_UUID);
		super.setup();
	}
	
	@Test
	public void shouldGetObservationByUuid() throws Exception {
		Observation observation = new Observation();
		observation.setId(OBS_UUID);
		when(observationService.get(OBS_UUID)).thenReturn(observation);
		
		MockHttpServletResponse response = get("/Observation/" + OBS_UUID).accept(FhirMediaTypes.JSON).go();
		
		MatcherAssert.assertThat(response, isOk());
		MatcherAssert.assertThat(response.getContentType(), equalTo(FhirMediaTypes.JSON.toString()));
		
		Observation resource = readResponse(response);
		assertThat(resource.getIdElement().getIdPart(), equalTo(OBS_UUID));
	}
	
	@Test
	public void shouldReturn404IfObservationNotFound() throws Exception {
		MockHttpServletResponse response = get("/Observation/" + BAD_OBS_UUID).accept(FhirMediaTypes.JSON).go();
		
		MatcherAssert.assertThat(response, isNotFound());
	}
	
	@Test
	public void createObservation_shouldCreateNewObservation() throws Exception {
		String observationJson;
		try (InputStream is = this.getClass().getClassLoader().getResourceAsStream(JSON_CREATE_OBSERVATION_PATH)) {
			Objects.requireNonNull(is);
			observationJson = IOUtils.toString(is, StandardCharsets.UTF_8);
		}
		
		when(observationService.create(any(Observation.class))).thenReturn(observation);
		
		MockHttpServletResponse response = post("/Observation").jsonContent(observationJson).accept(FhirMediaTypes.JSON)
		        .go();
		
		assertThat(response, isCreated());
	}
	
	@Test
	public void deleteObservation_shouldDeleteObservation() throws Exception {
		when(observationService.delete(OBS_UUID)).thenReturn(observation);
		
		MockHttpServletResponse response = delete("/Observation/" + OBS_UUID).accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), equalTo(FhirMediaTypes.JSON.toString()));
	}
	
	@Test
	public void deleteObservation_shouldReturn404ForNonExistingObservation() throws Exception {
		when(observationService.delete(BAD_OBS_UUID)).thenReturn(null);
		
		MockHttpServletResponse response = delete("/Observation/" + BAD_OBS_UUID).accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isNotFound());
		assertThat(response.getStatus(), equalTo(404));
	}
	
	@Test
	public void shouldGetObservationsBySubjectUuid() throws Exception {
		verifyUri("/Observation?subject=" + PATIENT_UUID);
		
		verify(observationService).searchForObservations(isNull(), patientCaptor.capture(), isNull(), isNull(), isNull(),
		    isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull());
		
		List<ReferenceOrListParam> orListParams = patientCaptor.getValue().getValuesAsQueryTokens();
		ReferenceParam referenceParam = orListParams.get(0).getValuesAsQueryTokens().get(0);
		
		assertThat(patientCaptor.getValue(), notNullValue());
		assertThat(referenceParam.getIdPart(), equalTo(PATIENT_UUID));
		assertThat(referenceParam.getChain(), equalTo(null));
	}
	
	@Test
	public void shouldGetObservationsByPatientUuid() throws Exception {
		verifyUri("/Observation?subject:Patient=" + PATIENT_UUID);
		
		verify(observationService).searchForObservations(isNull(), patientCaptor.capture(), isNull(), isNull(), isNull(),
		    isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull());
		
		List<ReferenceOrListParam> orListParams = patientCaptor.getValue().getValuesAsQueryTokens();
		ReferenceParam referenceParam = orListParams.get(0).getValuesAsQueryTokens().get(0);
		
		assertThat(patientCaptor.getValue(), notNullValue());
		assertThat(referenceParam.getIdPart(), equalTo(PATIENT_UUID));
		assertThat(referenceParam.getResourceType(), equalTo("Patient"));
		assertThat(referenceParam.getChain(), equalTo(null));
	}
	
	@Test
	public void shouldGetObservationsByPatientIdentifier() throws Exception {
		verifyUri("/Observation?subject.identifier=M4001-1");
		
		verify(observationService).searchForObservations(isNull(), patientCaptor.capture(), isNull(), isNull(), isNull(),
		    isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull());
		
		List<ReferenceOrListParam> orListParams = patientCaptor.getValue().getValuesAsQueryTokens();
		ReferenceParam referenceParam = orListParams.get(0).getValuesAsQueryTokens().get(0);
		
		assertThat(patientCaptor.getValue(), notNullValue());
		assertThat(referenceParam.getChain(), equalTo("identifier"));
		assertThat(referenceParam.getValue(), equalTo("M4001-1"));
	}
	
	@Test
	public void shouldGetObservationsByPatientIdentifierWithOr() throws Exception {
		verifyUri("/Observation?subject.identifier=M4001-1,ABS098,YT56RE,IU23O");
		
		verify(observationService).searchForObservations(isNull(), patientCaptor.capture(), isNull(), isNull(), isNull(),
		    isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull());
		
		List<ReferenceOrListParam> orListParams = patientCaptor.getValue().getValuesAsQueryTokens();
		ReferenceParam referenceParam = orListParams.get(0).getValuesAsQueryTokens().get(0);
		
		assertThat(patientCaptor.getValue(), notNullValue());
		assertThat(patientCaptor.getAllValues().iterator().next().getValuesAsQueryTokens().iterator().next()
		        .getValuesAsQueryTokens().iterator().next().getChain(),
		    equalTo("identifier"));
		assertThat(referenceParam.getValue(), equalTo("M4001-1"));
		assertThat(orListParams.get(0).getValuesAsQueryTokens().size(), equalTo(4));
	}
	
	@Test
	public void shouldGetObservationsByPatientIdentifierWithAnd() throws Exception {
		verifyUri(
		    "/Observation?subject.identifier=M4001-1&subject.identifier=ABS098&subject.identifier=YT56RE&subject.identifier=IU23O");
		
		verify(observationService).searchForObservations(isNull(), patientCaptor.capture(), isNull(), isNull(), isNull(),
		    isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull());
		
		List<ReferenceOrListParam> orListParams = patientCaptor.getValue().getValuesAsQueryTokens();
		ReferenceParam referenceParam = orListParams.get(0).getValuesAsQueryTokens().get(0);
		
		assertThat(patientCaptor.getValue(), notNullValue());
		assertThat(referenceParam.getChain(), equalTo("identifier"));
		assertThat(referenceParam.getValue(), equalTo("M4001-1"));
		assertThat(patientCaptor.getValue().getValuesAsQueryTokens().size(), equalTo(4));
	}
	
	@Test
	public void shouldGetObservationsByPatientName() throws Exception {
		verifyUri("/Observation?subject.name=Hannibal Lector");
		
		verify(observationService).searchForObservations(isNull(), patientCaptor.capture(), isNull(), isNull(), isNull(),
		    isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull());
		
		List<ReferenceOrListParam> orListParams = patientCaptor.getValue().getValuesAsQueryTokens();
		ReferenceParam referenceParam = orListParams.get(0).getValuesAsQueryTokens().get(0);
		
		assertThat(patientCaptor.getValue(), notNullValue());
		assertThat(referenceParam.getChain(), equalTo("name"));
		assertThat(referenceParam.getValue(), equalTo("Hannibal Lector"));
	}
	
	@Test
	public void shouldGetObservationsByPatientGivenName() throws Exception {
		verifyUri("/Observation?subject.given=Hannibal");
		
		verify(observationService).searchForObservations(isNull(), patientCaptor.capture(), isNull(), isNull(), isNull(),
		    isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull());
		
		List<ReferenceOrListParam> orListParams = patientCaptor.getValue().getValuesAsQueryTokens();
		ReferenceParam referenceParam = orListParams.get(0).getValuesAsQueryTokens().get(0);
		
		assertThat(patientCaptor.getValue(), notNullValue());
		assertThat(referenceParam.getChain(), equalTo("given"));
		assertThat(referenceParam.getValue(), equalTo("Hannibal"));
	}
	
	@Test
	public void shouldGetObservationsByPatientGivenNameWithOr() throws Exception {
		verifyUri("/Observation?subject.given=Hannibal,Smith");
		
		verify(observationService).searchForObservations(isNull(), patientCaptor.capture(), isNull(), isNull(), isNull(),
		    isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull());
		
		List<ReferenceOrListParam> orListParams = patientCaptor.getValue().getValuesAsQueryTokens();
		ReferenceParam referenceParam = orListParams.get(0).getValuesAsQueryTokens().get(0);
		
		assertThat(patientCaptor.getValue(), notNullValue());
		assertThat(referenceParam.getChain(), equalTo("given"));
		assertThat(referenceParam.getValue(), equalTo("Hannibal"));
		assertThat(orListParams.get(0).getValuesAsQueryTokens().size(), equalTo(2));
	}
	
	@Test
	public void shouldGetObservationsByPatientGivenNameWithAnd() throws Exception {
		verifyUri("/Observation?subject.given=Hannibal&subject.given=Smith");
		
		verify(observationService).searchForObservations(isNull(), patientCaptor.capture(), isNull(), isNull(), isNull(),
		    isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull());
		
		List<ReferenceOrListParam> orListParams = patientCaptor.getValue().getValuesAsQueryTokens();
		ReferenceParam referenceParam = orListParams.get(0).getValuesAsQueryTokens().get(0);
		
		assertThat(patientCaptor.getValue(), notNullValue());
		assertThat(referenceParam.getChain(), equalTo("given"));
		assertThat(referenceParam.getValue(), equalTo("Hannibal"));
		assertThat(patientCaptor.getValue().getValuesAsQueryTokens().size(), equalTo(2));
	}
	
	@Test
	public void shouldGetObservationsByPatientFamilyName() throws Exception {
		verifyUri("/Observation?subject.family=Lector");
		
		verify(observationService).searchForObservations(isNull(), patientCaptor.capture(), isNull(), isNull(), isNull(),
		    isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull());
		
		List<ReferenceOrListParam> orListParams = patientCaptor.getValue().getValuesAsQueryTokens();
		ReferenceParam referenceParam = orListParams.get(0).getValuesAsQueryTokens().get(0);
		
		assertThat(patientCaptor.getValue(), notNullValue());
		assertThat(referenceParam.getChain(), equalTo("family"));
		assertThat(referenceParam.getValue(), equalTo("Lector"));
	}
	
	@Test
	public void shouldGetObservationsByPatientFamilyNameWithOr() throws Exception {
		verifyUri("/Observation?subject.family=Lector,Rick,Tom");
		
		verify(observationService).searchForObservations(isNull(), patientCaptor.capture(), isNull(), isNull(), isNull(),
		    isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull());
		
		List<ReferenceOrListParam> orListParams = patientCaptor.getValue().getValuesAsQueryTokens();
		ReferenceParam referenceParam = orListParams.get(0).getValuesAsQueryTokens().get(0);
		
		assertThat(patientCaptor.getValue(), notNullValue());
		assertThat(referenceParam.getChain(), equalTo("family"));
		assertThat(referenceParam.getValue(), equalTo("Lector"));
		assertThat(orListParams.get(0).getValuesAsQueryTokens().size(), equalTo(3));
	}
	
	@Test
	public void shouldGetObservationsByPatientFamilyNameWithAnd() throws Exception {
		verifyUri("/Observation?subject.family=Lector&subject.family=Rick&subject.family=Tom");
		
		verify(observationService).searchForObservations(isNull(), patientCaptor.capture(), isNull(), isNull(), isNull(),
		    isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull());
		
		List<ReferenceOrListParam> orListParams = patientCaptor.getValue().getValuesAsQueryTokens();
		ReferenceParam referenceParam = orListParams.get(0).getValuesAsQueryTokens().get(0);
		
		assertThat(patientCaptor.getValue(), notNullValue());
		assertThat(referenceParam.getChain(), equalTo("family"));
		assertThat(referenceParam.getValue(), equalTo("Lector"));
		assertThat(patientCaptor.getValue().getValuesAsQueryTokens().size(), equalTo(3));
	}
	
	@Test
	public void shouldGetObservationsByEncounterUuid() throws Exception {
		verifyUri("/Observation?encounter=c4aa5682-90cf-48e8-87c9-a6066ffd3a3f");
		
		verify(observationService).searchForObservations(encounterCaptor.capture(), isNull(), isNull(), isNull(), isNull(),
		    isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull());
		
		List<ReferenceOrListParam> orListParams = encounterCaptor.getValue().getValuesAsQueryTokens();
		ReferenceParam referenceParam = orListParams.get(0).getValuesAsQueryTokens().get(0);
		
		assertThat(encounterCaptor.getValue(), notNullValue());
		assertThat(referenceParam.getIdPart(), equalTo("c4aa5682-90cf-48e8-87c9-a6066ffd3a3f"));
		assertThat(referenceParam.getChain(), equalTo(null));
	}
	
	@Test
	public void shouldGetObservationsByEncounterUuidWithOr() throws Exception {
		verifyUri("/Observation?encounter=c4aa5682-90cf-48e8-87c9-a6066ffd3a3f,c4aa5682-90cf-48e8-87c9-auyt23ffd3a3f");
		
		verify(observationService).searchForObservations(encounterCaptor.capture(), isNull(), isNull(), isNull(), isNull(),
		    isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull());
		
		List<ReferenceOrListParam> orListParams = encounterCaptor.getValue().getValuesAsQueryTokens();
		ReferenceParam referenceParam = orListParams.get(0).getValuesAsQueryTokens().get(0);
		
		assertThat(encounterCaptor.getValue(), notNullValue());
		assertThat(referenceParam.getIdPart(), equalTo("c4aa5682-90cf-48e8-87c9-a6066ffd3a3f"));
		assertThat(orListParams.get(0).getValuesAsQueryTokens().size(), equalTo(2));
	}
	
	@Test
	public void shouldGetObservationsByEncounterUuidWithAnd() throws Exception {
		verifyUri(
		    "/Observation?encounter=c4aa5682-90cf-48e8-87c9-a6066ffd3a3f&encounter=c4aa5682-90cf-48e8-87c9-auyt23ffd3a3f");
		
		verify(observationService).searchForObservations(encounterCaptor.capture(), isNull(), isNull(), isNull(), isNull(),
		    isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull());
		
		List<ReferenceOrListParam> orListParams = encounterCaptor.getValue().getValuesAsQueryTokens();
		ReferenceParam referenceParam = orListParams.get(0).getValuesAsQueryTokens().get(0);
		
		assertThat(encounterCaptor.getValue(), notNullValue());
		assertThat(referenceParam.getIdPart(), equalTo("c4aa5682-90cf-48e8-87c9-a6066ffd3a3f"));
		assertThat(encounterCaptor.getValue().getValuesAsQueryTokens().size(), equalTo(2));
	}
	
	@Test
	public void shouldGetObservationsByConceptId() throws Exception {
		verifyUri("/Observation?code=5098");
		
		verify(observationService).searchForObservations(isNull(), isNull(), isNull(), isNull(), isNull(), isNull(),
		    isNull(), isNull(), codeCaptor.capture(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull());
		assertThat(codeCaptor.getValue(), notNullValue());
		assertThat(codeCaptor.getValue().getValuesAsQueryTokens(), notNullValue());
		assertThat(codeCaptor.getValue().getValuesAsQueryTokens().size(), equalTo(1));
		
		TokenOrListParam orListParam = codeCaptor.getValue().getValuesAsQueryTokens().get(0);
		assertThat(orListParam.getValuesAsQueryTokens(), notNullValue());
		assertThat(orListParam.getValuesAsQueryTokens().size(), equalTo(1));
		assertThat(orListParam.getValuesAsQueryTokens().get(0).getSystem(), nullValue());
		assertThat(orListParam.getValuesAsQueryTokens().get(0).getValue(), equalTo("5098"));
	}
	
	@Test
	public void shouldGetObservationsByCategory() throws Exception {
		verifyUri("/Observation?category=laboratory");
		
		verify(observationService).searchForObservations(isNull(), isNull(), isNull(), isNull(), isNull(), isNull(),
		    isNull(), isNull(), isNull(), codeCaptor.capture(), isNull(), isNull(), isNull(), isNull(), isNull());
		assertThat(codeCaptor.getValue(), notNullValue());
		assertThat(codeCaptor.getValue().getValuesAsQueryTokens(), notNullValue());
		assertThat(codeCaptor.getValue().getValuesAsQueryTokens().size(), equalTo(1));
		
		TokenOrListParam orListParam = codeCaptor.getValue().getValuesAsQueryTokens().get(0);
		assertThat(orListParam.getValuesAsQueryTokens(), notNullValue());
		assertThat(orListParam.getValuesAsQueryTokens().size(), equalTo(1));
		assertThat(orListParam.getValuesAsQueryTokens().get(0).getSystem(), nullValue());
		assertThat(orListParam.getValuesAsQueryTokens().get(0).getValue(), equalTo("laboratory"));
	}
	
	@Test
	public void shouldGetObservationsByConceptAndSystem() throws Exception {
		verifyUri("/Observation?code=" + URL_ENCODED_CIEL_URI + "|5098");
		
		verify(observationService).searchForObservations(isNull(), isNull(), isNull(), isNull(), isNull(), isNull(),
		    isNull(), isNull(), codeCaptor.capture(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull());
		assertThat(codeCaptor.getValue(), notNullValue());
		assertThat(codeCaptor.getValue().getValuesAsQueryTokens(), notNullValue());
		assertThat(codeCaptor.getValue().getValuesAsQueryTokens().size(), equalTo(1));
		
		TokenOrListParam orListParam = codeCaptor.getValue().getValuesAsQueryTokens().get(0);
		assertThat(orListParam.getValuesAsQueryTokens(), notNullValue());
		assertThat(orListParam.getValuesAsQueryTokens().size(), equalTo(1));
		assertThat(orListParam.getValuesAsQueryTokens().get(0).getSystem(), equalTo(CIEL_URI));
		assertThat(orListParam.getValuesAsQueryTokens().get(0).getValue(), equalTo("5098"));
	}
	
	@Test
	public void shouldGetObservationsByConceptsAndSystem() throws Exception {
		verifyUri("/Observation?code=" + URL_ENCODED_CIEL_URI + "|5098," + URL_ENCODED_CIEL_URI + "|5001");
		
		verify(observationService).searchForObservations(isNull(), isNull(), isNull(), isNull(), isNull(), isNull(),
		    isNull(), isNull(), codeCaptor.capture(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull());
		assertThat(codeCaptor.getValue(), notNullValue());
		assertThat(codeCaptor.getValue().getValuesAsQueryTokens(), notNullValue());
		assertThat(codeCaptor.getValue().getValuesAsQueryTokens().size(), equalTo(1));
		
		TokenOrListParam orListParam = codeCaptor.getValue().getValuesAsQueryTokens().get(0);
		assertThat(orListParam.getValuesAsQueryTokens(), notNullValue());
		assertThat(orListParam.getValuesAsQueryTokens().size(), equalTo(2));
		assertThat(orListParam.getValuesAsQueryTokens().get(0).getSystem(), equalTo(CIEL_URI));
		assertThat(orListParam.getValuesAsQueryTokens().get(0).getValue(), equalTo("5098"));
		assertThat(orListParam.getValuesAsQueryTokens().get(1).getSystem(), equalTo(CIEL_URI));
		assertThat(orListParam.getValuesAsQueryTokens().get(1).getValue(), equalTo("5001"));
	}
	
	@Test
	public void shouldGetObservationsByPatientAndConcept() throws Exception {
		verifyUri("/Observation?code=" + URL_ENCODED_CIEL_URI + "|5098&subject:Patient=" + PATIENT_UUID);
		
		verify(observationService).searchForObservations(isNull(), patientCaptor.capture(), isNull(), isNull(), isNull(),
		    isNull(), isNull(), isNull(), codeCaptor.capture(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull());
		
		List<ReferenceOrListParam> orListParams = patientCaptor.getValue().getValuesAsQueryTokens();
		ReferenceParam referenceParam = orListParams.get(0).getValuesAsQueryTokens().get(0);
		
		// verify patient parameter
		assertThat(patientCaptor.getValue(), notNullValue());
		assertThat(referenceParam.getIdPart(), equalTo(PATIENT_UUID));
		assertThat(referenceParam.getResourceType(), equalTo("Patient"));
		
		// verify code parameter
		assertThat(codeCaptor.getValue(), notNullValue());
		assertThat(codeCaptor.getValue().getValuesAsQueryTokens(), notNullValue());
		assertThat(codeCaptor.getValue().getValuesAsQueryTokens().size(), equalTo(1));
		
		TokenOrListParam orListParam = codeCaptor.getValue().getValuesAsQueryTokens().get(0);
		assertThat(orListParam.getValuesAsQueryTokens(), notNullValue());
		assertThat(orListParam.getValuesAsQueryTokens().size(), equalTo(1));
		assertThat(orListParam.getValuesAsQueryTokens().get(0).getSystem(), equalTo(CIEL_URI));
		assertThat(orListParam.getValuesAsQueryTokens().get(0).getValue(), equalTo("5098"));
	}
	
	@Test
	public void shouldGetObservationsByMemberAndConcept() throws Exception {
		verifyUri("/Observation?code=" + URL_ENCODED_CIEL_URI + "|5098&has-member=" + MEMBER_UUID);
		
		verify(observationService).searchForObservations(isNull(), isNull(), memberCaptor.capture(), isNull(), isNull(),
		    isNull(), isNull(), isNull(), codeCaptor.capture(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull());
		
		// verify member parameter
		assertThat(memberCaptor.getValue(), notNullValue());
		assertThat(memberCaptor.getValue().getValuesAsQueryTokens().get(0).getValuesAsQueryTokens().get(0).getIdPart(),
		    equalTo(MEMBER_UUID));
		
		// verify code parameter
		assertThat(codeCaptor.getValue(), notNullValue());
		assertThat(codeCaptor.getValue().getValuesAsQueryTokens(), notNullValue());
		assertThat(codeCaptor.getValue().getValuesAsQueryTokens().size(), equalTo(1));
		
		TokenOrListParam orListParam = codeCaptor.getValue().getValuesAsQueryTokens().get(0);
		assertThat(orListParam.getValuesAsQueryTokens(), notNullValue());
		assertThat(orListParam.getValuesAsQueryTokens().size(), equalTo(1));
		assertThat(orListParam.getValuesAsQueryTokens().get(0).getSystem(), equalTo(CIEL_URI));
		assertThat(orListParam.getValuesAsQueryTokens().get(0).getValue(), equalTo("5098"));
	}
	
	@Test
	public void shouldGetObservationsByMemberCode() throws Exception {
		verifyUri("/Observation?has-member.code=5098");
		
		verify(observationService).searchForObservations(isNull(), isNull(), memberCaptor.capture(), isNull(), isNull(),
		    isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull());
		
		assertThat(memberCaptor.getValue(), notNullValue());
		assertThat(memberCaptor.getValue().getValuesAsQueryTokens().get(0).getValuesAsQueryTokens().get(0).getChain(),
		    equalTo(Observation.SP_CODE));
		assertThat(memberCaptor.getValue().getValuesAsQueryTokens().get(0).getValuesAsQueryTokens().get(0).getValue(),
		    equalTo("5098"));
	}
	
	@Test
	public void shouldGetObservationsByValueDate() throws Exception {
		verifyUri("/Observation?value-date=ge1975-02-02");
		
		verify(observationService).searchForObservations(isNull(), isNull(), isNull(), isNull(), valueDateCaptor.capture(),
		    isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull());
		
		Calendar calendar = Calendar.getInstance();
		calendar.set(1975, Calendar.FEBRUARY, 2);
		
		assertThat(valueDateCaptor.getValue().getLowerBound().getValue(),
		    equalTo(DateUtils.truncate(calendar.getTime(), Calendar.DATE)));
		assertThat(valueDateCaptor.getValue().getUpperBound(), nullValue());
	}
	
	@Test
	public void shouldGetObservationsByValueQuantity() throws Exception {
		verifyUri("/Observation?value-quantity=134.0");
		
		verify(observationService).searchForObservations(isNull(), isNull(), isNull(), isNull(), isNull(),
		    valueQuantityCaptor.capture(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(),
		    isNull());
		
		assertThat(valueQuantityCaptor.getValue(), notNullValue());
		assertThat(valueQuantityCaptor.getValue().getValuesAsQueryTokens(), not(empty()));
		assertThat(valueQuantityCaptor.getValue().getValuesAsQueryTokens().get(0).getValuesAsQueryTokens().get(0).getValue(),
		    equalTo(BigDecimal.valueOf(134.0)));
	}
	
	@Test
	public void shouldGetObservationsByValueString() throws Exception {
		verifyUri("/Observation?value-string=AFH56");
		
		verify(observationService).searchForObservations(isNull(), isNull(), isNull(), isNull(), isNull(), isNull(),
		    stringAndListCaptor.capture(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull());
		
		assertThat(stringAndListCaptor.getValue(), notNullValue());
		assertThat(stringAndListCaptor.getValue().getValuesAsQueryTokens(), not(empty()));
		assertThat(stringAndListCaptor.getValue().getValuesAsQueryTokens().get(0).getValuesAsQueryTokens().get(0).getValue(),
		    equalTo("AFH56"));
	}
	
	@Test
	public void shouldGetObservationsByDate() throws Exception {
		verifyUri("/Observation?date=ge1975-02-02");
		
		verify(observationService).searchForObservations(isNull(), isNull(), isNull(), isNull(), isNull(), isNull(),
		    isNull(), dateCaptor.capture(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull());
		
		Calendar calendar = Calendar.getInstance();
		calendar.set(1975, Calendar.FEBRUARY, 2);
		
		assertThat(dateCaptor.getValue().getLowerBound().getValue(),
		    equalTo(DateUtils.truncate(calendar.getTime(), Calendar.DATE)));
		assertThat(dateCaptor.getValue().getUpperBound(), nullValue());
	}
	
	@Test
	public void shouldGetObservationsByValueConcept() throws Exception {
		verifyUri("/Observation?value-concept=5098");
		
		verify(observationService).searchForObservations(isNull(), isNull(), isNull(), valueCodeCaptor.capture(), isNull(),
		    isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull());
		assertThat(valueCodeCaptor.getValue(), notNullValue());
		assertThat(valueCodeCaptor.getValue().getValuesAsQueryTokens(), notNullValue());
		assertThat(valueCodeCaptor.getValue().getValuesAsQueryTokens().size(), equalTo(1));
		
		TokenOrListParam orListParam = valueCodeCaptor.getValue().getValuesAsQueryTokens().get(0);
		assertThat(orListParam.getValuesAsQueryTokens(), notNullValue());
		assertThat(orListParam.getValuesAsQueryTokens().size(), equalTo(1));
		assertThat(orListParam.getValuesAsQueryTokens().get(0).getSystem(), nullValue());
		assertThat(orListParam.getValuesAsQueryTokens().get(0).getValue(), equalTo("5098"));
	}
	
	@Test
	public void shouldGetObservationsByValueConceptsAndSystem() throws Exception {
		verifyUri("/Observation?value-concept=" + URL_ENCODED_CIEL_URI + "|5098," + URL_ENCODED_CIEL_URI + "|5001");
		
		verify(observationService).searchForObservations(isNull(), isNull(), isNull(), valueCodeCaptor.capture(), isNull(),
		    isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull());
		assertThat(valueCodeCaptor.getValue(), notNullValue());
		assertThat(valueCodeCaptor.getValue().getValuesAsQueryTokens(), notNullValue());
		assertThat(valueCodeCaptor.getValue().getValuesAsQueryTokens().size(), equalTo(1));
		
		TokenOrListParam orListParam = valueCodeCaptor.getValue().getValuesAsQueryTokens().get(0);
		assertThat(orListParam.getValuesAsQueryTokens(), notNullValue());
		assertThat(orListParam.getValuesAsQueryTokens().size(), equalTo(2));
		assertThat(orListParam.getValuesAsQueryTokens().get(0).getSystem(), equalTo(CIEL_URI));
		assertThat(orListParam.getValuesAsQueryTokens().get(0).getValue(), equalTo("5098"));
		assertThat(orListParam.getValuesAsQueryTokens().get(1).getSystem(), equalTo(CIEL_URI));
		assertThat(orListParam.getValuesAsQueryTokens().get(1).getValue(), equalTo("5001"));
	}
	
	@Test
	public void shouldGetObservationsByUUID() throws Exception {
		verifyUri(String.format("/Observation?_id=%s", OBS_UUID));
		
		verify(observationService).searchForObservations(isNull(), isNull(), isNull(), isNull(), isNull(), isNull(),
		    isNull(), isNull(), isNull(), isNull(), codeCaptor.capture(), isNull(), isNull(), isNull(), isNull());
		
		assertThat(codeCaptor.getValue(), notNullValue());
		assertThat(codeCaptor.getValue().getValuesAsQueryTokens(), not(empty()));
		assertThat(codeCaptor.getValue().getValuesAsQueryTokens().get(0).getValuesAsQueryTokens().get(0).getValue(),
		    equalTo(OBS_UUID));
	}
	
	@Test
	public void shouldGetObservationsByLastUpdatedDate() throws Exception {
		verifyUri(String.format("/Observation?_lastUpdated=%s", LAST_UPDATED_DATE));
		
		verify(observationService).searchForObservations(isNull(), isNull(), isNull(), isNull(), isNull(), isNull(),
		    isNull(), isNull(), isNull(), isNull(), isNull(), dateCaptor.capture(), isNull(), isNull(), isNull());
		
		assertThat(dateCaptor.getValue(), notNullValue());
		
		Calendar calendar = Calendar.getInstance();
		calendar.set(2020, Calendar.SEPTEMBER, 3);
		
		assertThat(dateCaptor.getValue().getLowerBound().getValue(),
		    equalTo(DateUtils.truncate(calendar.getTime(), Calendar.DATE)));
		assertThat(dateCaptor.getValue().getUpperBound().getValue(),
		    equalTo(DateUtils.truncate(calendar.getTime(), Calendar.DATE)));
	}
	
	@Test
	public void shouldIncludeEncounterWithReturnedObservations() throws Exception {
		verifyUri("/Observation?_include=Observation:encounter");
		
		verify(observationService).searchForObservations(isNull(), isNull(), isNull(), isNull(), isNull(), isNull(),
		    isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), includeArgumentCaptor.capture(), isNull());
		
		assertThat(includeArgumentCaptor.getValue(), notNullValue());
		assertThat(includeArgumentCaptor.getValue().size(), equalTo(1));
		assertThat(includeArgumentCaptor.getValue().iterator().next().getParamName(),
		    equalTo(FhirConstants.INCLUDE_ENCOUNTER_PARAM));
		assertThat(includeArgumentCaptor.getValue().iterator().next().getParamType(), equalTo(FhirConstants.OBSERVATION));
	}
	
	@Test
	public void shouldIncludePatientWithReturnedObservations() throws Exception {
		verifyUri("/Observation?_include=Observation:patient");
		
		verify(observationService).searchForObservations(isNull(), isNull(), isNull(), isNull(), isNull(), isNull(),
		    isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), includeArgumentCaptor.capture(), isNull());
		
		assertThat(includeArgumentCaptor.getValue(), notNullValue());
		assertThat(includeArgumentCaptor.getValue().size(), equalTo(1));
		assertThat(includeArgumentCaptor.getValue().iterator().next().getParamName(),
		    equalTo(FhirConstants.INCLUDE_PATIENT_PARAM));
		assertThat(includeArgumentCaptor.getValue().iterator().next().getParamType(), equalTo(FhirConstants.OBSERVATION));
	}
	
	@Test
	public void shouldIncludeObservationGroupMembersWithReturnedObservations() throws Exception {
		verifyUri("/Observation?_include=Observation:has-member");
		
		verify(observationService).searchForObservations(isNull(), isNull(), isNull(), isNull(), isNull(), isNull(),
		    isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), includeArgumentCaptor.capture(), isNull());
		
		assertThat(includeArgumentCaptor.getValue(), notNullValue());
		assertThat(includeArgumentCaptor.getValue().size(), equalTo(1));
		assertThat(includeArgumentCaptor.getValue().iterator().next().getParamName(),
		    equalTo(FhirConstants.INCLUDE_HAS_MEMBER_PARAM));
		assertThat(includeArgumentCaptor.getValue().iterator().next().getParamType(), equalTo(FhirConstants.OBSERVATION));
	}
	
	@Test
	public void shouldHandleMultipleIncludes() throws Exception {
		verifyUri("/Observation?_include=Observation:has-member&_include=Observation:encounter");
		
		verify(observationService).searchForObservations(isNull(), isNull(), isNull(), isNull(), isNull(), isNull(),
		    isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), includeArgumentCaptor.capture(), isNull());
		
		assertThat(includeArgumentCaptor.getValue(), notNullValue());
		assertThat(includeArgumentCaptor.getValue().size(), equalTo(2));
		
		assertThat(includeArgumentCaptor.getValue(),
		    hasItem(allOf(hasProperty("paramName", equalTo(FhirConstants.INCLUDE_HAS_MEMBER_PARAM)),
		        hasProperty("paramType", equalTo(FhirConstants.OBSERVATION)))));
		assertThat(includeArgumentCaptor.getValue(),
		    hasItem(allOf(hasProperty("paramName", equalTo(FhirConstants.INCLUDE_ENCOUNTER_PARAM)),
		        hasProperty("paramType", equalTo(FhirConstants.OBSERVATION)))));
	}
	
	@Test
	public void shouldReverseIncludeObservationsWithReturnedObservations() throws Exception {
		verifyUri("/Observation?_revinclude=Observation:has-member");
		
		verify(observationService).searchForObservations(isNull(), isNull(), isNull(), isNull(), isNull(), isNull(),
		    isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), includeArgumentCaptor.capture());
		
		assertThat(includeArgumentCaptor.getValue(), notNullValue());
		assertThat(includeArgumentCaptor.getValue().size(), equalTo(1));
		assertThat(includeArgumentCaptor.getValue().iterator().next().getParamName(),
		    equalTo(FhirConstants.INCLUDE_HAS_MEMBER_PARAM));
		assertThat(includeArgumentCaptor.getValue().iterator().next().getParamType(), equalTo(FhirConstants.OBSERVATION));
	}
	
	@Test
	public void shouldReverseIncludeDiagnosticReportsWithReturnedObservations() throws Exception {
		verifyUri("/Observation?_revinclude=DiagnosticReport:result");
		
		verify(observationService).searchForObservations(isNull(), isNull(), isNull(), isNull(), isNull(), isNull(),
		    isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), includeArgumentCaptor.capture());
		
		assertThat(includeArgumentCaptor.getValue(), notNullValue());
		assertThat(includeArgumentCaptor.getValue().size(), equalTo(1));
		assertThat(includeArgumentCaptor.getValue().iterator().next().getParamName(),
		    equalTo(FhirConstants.INCLUDE_RESULT_PARAM));
		assertThat(includeArgumentCaptor.getValue().iterator().next().getParamType(),
		    equalTo(FhirConstants.DIAGNOSTIC_REPORT));
	}
	
	@Test
	public void shouldHandleMultipleReverseIncludes() throws Exception {
		verifyUri("/Observation?_revinclude=Observation:has-member&_revinclude=DiagnosticReport:result");
		
		verify(observationService).searchForObservations(isNull(), isNull(), isNull(), isNull(), isNull(), isNull(),
		    isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), includeArgumentCaptor.capture());
		
		assertThat(includeArgumentCaptor.getValue(), notNullValue());
		assertThat(includeArgumentCaptor.getValue().size(), equalTo(2));
		
		assertThat(includeArgumentCaptor.getValue(),
		    hasItem(allOf(hasProperty("paramName", equalTo(FhirConstants.INCLUDE_HAS_MEMBER_PARAM)),
		        hasProperty("paramType", equalTo(FhirConstants.OBSERVATION)))));
		assertThat(includeArgumentCaptor.getValue(),
		    hasItem(allOf(hasProperty("paramName", equalTo(FhirConstants.INCLUDE_RESULT_PARAM)),
		        hasProperty("paramType", equalTo(FhirConstants.DIAGNOSTIC_REPORT)))));
	}
	
	@Test
	public void lastn_shouldHandleRequestWithMaxParameter() throws Exception {
		verifyLastnOperation("/Observation/$lastn?max=3");
		
		verify(observationService).getLastnObservations(maxCaptor.capture(), isNull(), isNull(), isNull());
		
		assertThat(maxCaptor.getValue(), notNullValue());
	}
	
	@Test
	public void lastn_shouldHandleRequestWithPatientId() throws Exception {
		verifyLastnOperation("/Observation/$lastn?subject=" + PATIENT_UUID);
		
		verify(observationService).getLastnObservations(isNull(), patientCaptor.capture(), isNull(), isNull());
		
		List<ReferenceOrListParam> orListParams = patientCaptor.getValue().getValuesAsQueryTokens();
		ReferenceParam referenceParam = orListParams.get(0).getValuesAsQueryTokens().get(0);
		
		assertThat(patientCaptor.getValue(), notNullValue());
		assertThat(referenceParam.getIdPart(), equalTo(PATIENT_UUID));
		assertThat(referenceParam.getChain(), equalTo(null));
	}
	
	@Test
	public void lastn_shouldHandleRequestWithCategory() throws Exception {
		verifyLastnOperation("/Observation/$lastn?category=laboratory");
		
		verify(observationService).getLastnObservations(isNull(), isNull(), codeCaptor.capture(), isNull());
		
		assertThat(codeCaptor.getValue(), notNullValue());
		assertThat(codeCaptor.getValue().getValuesAsQueryTokens(), notNullValue());
		assertThat(codeCaptor.getValue().getValuesAsQueryTokens().size(), equalTo(1));
		
		TokenOrListParam orListParam = codeCaptor.getValue().getValuesAsQueryTokens().get(0);
		assertThat(orListParam.getValuesAsQueryTokens(), notNullValue());
		assertThat(orListParam.getValuesAsQueryTokens().size(), equalTo(1));
		assertThat(orListParam.getValuesAsQueryTokens().get(0).getSystem(), nullValue());
		assertThat(orListParam.getValuesAsQueryTokens().get(0).getValue(), equalTo("laboratory"));
	}
	
	@Test
	public void lastn_shouldHandleRequestWithCode() throws Exception {
		verifyLastnOperation("/Observation/$lastn?code=5085");
		
		verify(observationService).getLastnObservations(isNull(), isNull(), isNull(), codeCaptor.capture());
		
		assertThat(codeCaptor.getValue(), notNullValue());
		assertThat(codeCaptor.getValue().getValuesAsQueryTokens(), notNullValue());
		assertThat(codeCaptor.getValue().getValuesAsQueryTokens().size(), equalTo(1));
		
		TokenOrListParam orListParam = codeCaptor.getValue().getValuesAsQueryTokens().get(0);
		assertThat(orListParam.getValuesAsQueryTokens(), notNullValue());
		assertThat(orListParam.getValuesAsQueryTokens().size(), equalTo(1));
		assertThat(orListParam.getValuesAsQueryTokens().get(0).getSystem(), nullValue());
		assertThat(orListParam.getValuesAsQueryTokens().get(0).getValue(), equalTo("5085"));
	}
	
	@Test
	public void lastn_shouldHandleRequestWithAllParameters() throws Exception {
		verifyLastnOperation("/Observation/$lastn?max=2&subject=" + PATIENT_UUID + "&category=laboratory&code=5085");
		
		verify(observationService).getLastnObservations(maxCaptor.capture(), patientCaptor.capture(), codeCaptor.capture(),
		    codeCaptor.capture());
		
		assertThat(maxCaptor.getValue(), notNullValue());
		assertThat(patientCaptor.getValue(), notNullValue());
		assertThat(codeCaptor.getValue(), notNullValue());
	}
	
	private void verifyLastnOperation(String uri) throws Exception {
		Observation observation = new Observation();
		observation.setId(OBS_UUID);
		
		when(observationService.getLastnObservations(any(), any(), any(), any()))
		        .thenReturn(new MockIBundleProvider<>(Collections.singletonList(observation), 10, 1));
		
		MockHttpServletResponse response = get(uri).accept(FhirMediaTypes.JSON).go();
		
		MatcherAssert.assertThat(response, isOk());
		MatcherAssert.assertThat(response.getContentType(), equalTo(FhirMediaTypes.JSON.toString()));
		
		Bundle results = readBundleResponse(response);
		assertThat(results.getEntry(), notNullValue());
		assertThat(results.getEntry(), not(empty()));
		assertThat(results.getEntry().get(0).getResource(), notNullValue());
		assertThat(results.getEntry().get(0).getResource().getIdElement().getIdPart(), equalTo(OBS_UUID));
	}
	
	private void verifyUri(String uri) throws Exception {
		Observation observation = new Observation();
		observation.setId(OBS_UUID);
		when(observationService.searchForObservations(any(), any(), any(), any(), any(), any(), any(), any(), any(), any(),
		    any(), any(), any(), any(), any()))
		            .thenReturn(new MockIBundleProvider<>(Collections.singletonList(observation), 10, 1));
		
		MockHttpServletResponse response = get(uri).accept(FhirMediaTypes.JSON).go();
		
		MatcherAssert.assertThat(response, isOk());
		MatcherAssert.assertThat(response.getContentType(), equalTo(FhirMediaTypes.JSON.toString()));
		
		Bundle results = readBundleResponse(response);
		assertThat(results.getEntry(), notNullValue());
		assertThat(results.getEntry(), not(empty()));
		assertThat(results.getEntry().get(0).getResource(), notNullValue());
		assertThat(results.getEntry().get(0).getResource().getIdElement().getIdPart(), equalTo(OBS_UUID));
	}
}
