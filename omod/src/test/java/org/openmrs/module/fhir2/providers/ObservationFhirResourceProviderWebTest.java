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
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

import ca.uhn.fhir.rest.param.DateRangeParam;
import ca.uhn.fhir.rest.param.QuantityAndListParam;
import ca.uhn.fhir.rest.param.ReferenceAndListParam;
import ca.uhn.fhir.rest.param.ReferenceOrListParam;
import ca.uhn.fhir.rest.param.ReferenceParam;
import ca.uhn.fhir.rest.param.StringAndListParam;
import ca.uhn.fhir.rest.param.TokenAndListParam;
import ca.uhn.fhir.rest.param.TokenOrListParam;
import lombok.AccessLevel;
import lombok.Getter;
import org.apache.commons.lang.time.DateUtils;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Observation;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openmrs.module.fhir2.api.FhirObservationService;
import org.openmrs.module.fhir2.web.servlet.BaseFhirResourceProviderTest;
import org.springframework.mock.web.MockHttpServletResponse;

@RunWith(MockitoJUnitRunner.class)
public class ObservationFhirResourceProviderWebTest extends BaseFhirResourceProviderTest<ObservationFhirResourceProvider, Observation> {
	
	private static final String OBS_UUID = "39fb7f47-e80a-4056-9285-bd798be13c63";
	
	private static final String BAD_OBS_UUID = "121b73a6-e1a4-4424-8610-d5765bf2fdf7";
	
	private static final String PATIENT_UUID = "d9bc6c12-6adc-4ca6-8bde-441ec1a1c344";
	
	private static final String MEMBER_UUID = "d9bc6c12-6adc-4ca6-8bde-441ec1a1c344";
	
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
	private ArgumentCaptor<ReferenceParam> memberCaptor;
	
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
	
	@Before
	@Override
	public void setup() throws Exception {
		resourceProvider = new ObservationFhirResourceProvider();
		resourceProvider.setObservationService(observationService);
		super.setup();
	}
	
	@Test
	public void shouldGetObservationByUuid() throws Exception {
		Observation observation = new Observation();
		observation.setId(OBS_UUID);
		when(observationService.get(OBS_UUID)).thenReturn(observation);
		
		MockHttpServletResponse response = get("/Observation/" + OBS_UUID).accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), equalTo(FhirMediaTypes.JSON.toString()));
		
		Observation resource = readResponse(response);
		assertThat(resource.getIdElement().getIdPart(), equalTo(OBS_UUID));
	}
	
	@Test
	public void shouldReturn404IfObservationNotFound() throws Exception {
		MockHttpServletResponse response = get("/Observation/" + BAD_OBS_UUID).accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isNotFound());
	}
	
	@Test
	public void shouldGetObservationsBySubjectUuid() throws Exception {
		verifyUri("/Observation?subject=" + PATIENT_UUID);
		
		verify(observationService).searchForObservations(isNull(), patientCaptor.capture(), isNull(), isNull(), isNull(),
		    isNull(), isNull(), isNull(), isNull(), isNull());
		
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
		    isNull(), isNull(), isNull(), isNull(), isNull());
		
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
		    isNull(), isNull(), isNull(), isNull(), isNull());
		
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
		    isNull(), isNull(), isNull(), isNull(), isNull());
		
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
		    isNull(), isNull(), isNull(), isNull(), isNull());
		
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
		    isNull(), isNull(), isNull(), isNull(), isNull());
		
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
		    isNull(), isNull(), isNull(), isNull(), isNull());
		
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
		    isNull(), isNull(), isNull(), isNull(), isNull());
		
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
		    isNull(), isNull(), isNull(), isNull(), isNull());
		
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
		    isNull(), isNull(), isNull(), isNull(), isNull());
		
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
		    isNull(), isNull(), isNull(), isNull(), isNull());
		
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
		    isNull(), isNull(), isNull(), isNull(), isNull());
		
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
		    isNull(), isNull(), isNull(), isNull(), isNull());
		
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
		    isNull(), isNull(), isNull(), isNull(), isNull());
		
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
		    isNull(), isNull(), isNull(), isNull(), isNull());
		
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
		    isNull(), isNull(), codeCaptor.capture(), isNull());
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
	public void shouldGetObservationsByValueConceptId() throws Exception {
		verifyUri("/Observation?value-concept=5098");
		
		verify(observationService).searchForObservations(isNull(), isNull(), isNull(), valueCodeCaptor.capture(), isNull(),
		    isNull(), isNull(), isNull(), isNull(), isNull());
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
	public void shouldGetObservationsByConceptAndSystem() throws Exception {
		verifyUri("/Observation?code=" + URL_ENCODED_CIEL_URN + "|5098");
		
		verify(observationService).searchForObservations(isNull(), isNull(), isNull(), isNull(), isNull(), isNull(),
		    isNull(), isNull(), codeCaptor.capture(), isNull());
		assertThat(codeCaptor.getValue(), notNullValue());
		assertThat(codeCaptor.getValue().getValuesAsQueryTokens(), notNullValue());
		assertThat(codeCaptor.getValue().getValuesAsQueryTokens().size(), equalTo(1));
		
		TokenOrListParam orListParam = codeCaptor.getValue().getValuesAsQueryTokens().get(0);
		assertThat(orListParam.getValuesAsQueryTokens(), notNullValue());
		assertThat(orListParam.getValuesAsQueryTokens().size(), equalTo(1));
		assertThat(orListParam.getValuesAsQueryTokens().get(0).getSystem(), equalTo(CIEL_URN));
		assertThat(orListParam.getValuesAsQueryTokens().get(0).getValue(), equalTo("5098"));
	}
	
	@Test
	public void shouldGetObservationsByConceptsAndSystem() throws Exception {
		verifyUri("/Observation?code=" + URL_ENCODED_CIEL_URN + "|5098," + URL_ENCODED_CIEL_URN + "|5001");
		
		verify(observationService).searchForObservations(isNull(), isNull(), isNull(), isNull(), isNull(), isNull(),
		    isNull(), isNull(), codeCaptor.capture(), isNull());
		assertThat(codeCaptor.getValue(), notNullValue());
		assertThat(codeCaptor.getValue().getValuesAsQueryTokens(), notNullValue());
		assertThat(codeCaptor.getValue().getValuesAsQueryTokens().size(), equalTo(1));
		
		TokenOrListParam orListParam = codeCaptor.getValue().getValuesAsQueryTokens().get(0);
		assertThat(orListParam.getValuesAsQueryTokens(), notNullValue());
		assertThat(orListParam.getValuesAsQueryTokens().size(), equalTo(2));
		assertThat(orListParam.getValuesAsQueryTokens().get(0).getSystem(), equalTo(CIEL_URN));
		assertThat(orListParam.getValuesAsQueryTokens().get(0).getValue(), equalTo("5098"));
		assertThat(orListParam.getValuesAsQueryTokens().get(1).getSystem(), equalTo(CIEL_URN));
		assertThat(orListParam.getValuesAsQueryTokens().get(1).getValue(), equalTo("5001"));
	}
	
	@Test
	public void shouldGetObservationsByValueConceptsAndSystem() throws Exception {
		verifyUri("/Observation?value-concept=" + URL_ENCODED_CIEL_URN + "|5098," + URL_ENCODED_CIEL_URN + "|5001");
		
		verify(observationService).searchForObservations(isNull(), isNull(), isNull(), valueCodeCaptor.capture(), isNull(),
		    isNull(), isNull(), isNull(), isNull(), isNull());
		assertThat(valueCodeCaptor.getValue(), notNullValue());
		assertThat(valueCodeCaptor.getValue().getValuesAsQueryTokens(), notNullValue());
		assertThat(valueCodeCaptor.getValue().getValuesAsQueryTokens().size(), equalTo(1));
		
		TokenOrListParam orListParam = valueCodeCaptor.getValue().getValuesAsQueryTokens().get(0);
		assertThat(orListParam.getValuesAsQueryTokens(), notNullValue());
		assertThat(orListParam.getValuesAsQueryTokens().size(), equalTo(2));
		assertThat(orListParam.getValuesAsQueryTokens().get(0).getSystem(), equalTo(CIEL_URN));
		assertThat(orListParam.getValuesAsQueryTokens().get(0).getValue(), equalTo("5098"));
		assertThat(orListParam.getValuesAsQueryTokens().get(1).getSystem(), equalTo(CIEL_URN));
		assertThat(orListParam.getValuesAsQueryTokens().get(1).getValue(), equalTo("5001"));
	}
	
	@Test
	public void shouldGetObservationsByPatientAndConcept() throws Exception {
		verifyUri("/Observation?code=" + URL_ENCODED_CIEL_URN + "|5098&subject:Patient=" + PATIENT_UUID);
		
		verify(observationService).searchForObservations(isNull(), patientCaptor.capture(), isNull(), isNull(), isNull(),
		    isNull(), isNull(), isNull(), codeCaptor.capture(), isNull());
		
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
		assertThat(orListParam.getValuesAsQueryTokens().get(0).getSystem(), equalTo(CIEL_URN));
		assertThat(orListParam.getValuesAsQueryTokens().get(0).getValue(), equalTo("5098"));
	}
	
	@Test
	public void shouldGetObservationsByMemberAndConcept() throws Exception {
		verifyUri("/Observation?code=" + URL_ENCODED_CIEL_URN + "|5098&has-member=" + MEMBER_UUID);
		
		verify(observationService).searchForObservations(isNull(), isNull(), memberCaptor.capture(), isNull(), isNull(),
		    isNull(), isNull(), isNull(), codeCaptor.capture(), isNull());
		
		// verify member parameter
		assertThat(memberCaptor.getValue(), notNullValue());
		assertThat(memberCaptor.getValue().getIdPart(), equalTo(MEMBER_UUID));
		
		// verify code parameter
		assertThat(codeCaptor.getValue(), notNullValue());
		assertThat(codeCaptor.getValue().getValuesAsQueryTokens(), notNullValue());
		assertThat(codeCaptor.getValue().getValuesAsQueryTokens().size(), equalTo(1));
		
		TokenOrListParam orListParam = codeCaptor.getValue().getValuesAsQueryTokens().get(0);
		assertThat(orListParam.getValuesAsQueryTokens(), notNullValue());
		assertThat(orListParam.getValuesAsQueryTokens().size(), equalTo(1));
		assertThat(orListParam.getValuesAsQueryTokens().get(0).getSystem(), equalTo(CIEL_URN));
		assertThat(orListParam.getValuesAsQueryTokens().get(0).getValue(), equalTo("5098"));
	}
	
	@Test
	public void shouldGetObservationsByMemberCode() throws Exception {
		verifyUri("/Observation?has-member.code=5098");
		
		verify(observationService).searchForObservations(isNull(), isNull(), memberCaptor.capture(), isNull(), isNull(),
		    isNull(), isNull(), isNull(), isNull(), isNull());
		
		assertThat(memberCaptor.getValue(), notNullValue());
		assertThat(memberCaptor.getValue().getChain(), equalTo(Observation.SP_CODE));
		assertThat(memberCaptor.getValue().getValue(), equalTo("5098"));
	}
	
	@Test
	public void shouldGetObservationsByValueDate() throws Exception {
		verifyUri("/Observation?value-date=ge1975-02-02");
		
		verify(observationService).searchForObservations(isNull(), isNull(), isNull(), isNull(), valueDateCaptor.capture(),
		    isNull(), isNull(), isNull(), isNull(), isNull());
		
		Calendar calendar = Calendar.getInstance();
		calendar.set(1975, 1, 2);
		
		assertThat(valueDateCaptor.getValue().getLowerBound().getValue(),
		    equalTo(DateUtils.truncate(calendar.getTime(), Calendar.DATE)));
		assertThat(valueDateCaptor.getValue().getUpperBound(), nullValue());
	}
	
	@Test
	public void shouldGetObservationsByValueQuantity() throws Exception {
		verifyUri("/Observation?value-quantity=134.0");
		
		verify(observationService).searchForObservations(isNull(), isNull(), isNull(), isNull(), isNull(),
		    valueQuantityCaptor.capture(), isNull(), isNull(), isNull(), isNull());
		
		assertThat(valueQuantityCaptor.getValue(), notNullValue());
		assertThat(valueQuantityCaptor.getValue().getValuesAsQueryTokens(), not(empty()));
		assertThat(valueQuantityCaptor.getValue().getValuesAsQueryTokens().get(0).getValuesAsQueryTokens().get(0).getValue(),
		    equalTo(BigDecimal.valueOf(134.0)));
	}
	
	@Test
	public void shouldGetObservationsByValueString() throws Exception {
		verifyUri("/Observation?value-string=AFH56");
		
		verify(observationService).searchForObservations(isNull(), isNull(), isNull(), isNull(), isNull(), isNull(),
		    stringAndListCaptor.capture(), isNull(), isNull(), isNull());
		
		assertThat(stringAndListCaptor.getValue(), notNullValue());
		assertThat(stringAndListCaptor.getValue().getValuesAsQueryTokens(), not(empty()));
		assertThat(stringAndListCaptor.getValue().getValuesAsQueryTokens().get(0).getValuesAsQueryTokens().get(0).getValue(),
		    equalTo("AFH56"));
	}
	
	@Test
	public void shouldGetObservationsByDate() throws Exception {
		verifyUri("/Observation?date=ge1975-02-02");
		
		verify(observationService).searchForObservations(isNull(), isNull(), isNull(), isNull(), isNull(), isNull(),
		    isNull(), dateCaptor.capture(), isNull(), isNull());
		
		Calendar calendar = Calendar.getInstance();
		calendar.set(1975, 1, 2);
		
		assertThat(dateCaptor.getValue().getLowerBound().getValue(),
		    equalTo(DateUtils.truncate(calendar.getTime(), Calendar.DATE)));
		assertThat(dateCaptor.getValue().getUpperBound(), nullValue());
	}
	
	private void verifyUri(String uri) throws Exception {
		Observation observation = new Observation();
		observation.setId(OBS_UUID);
		when(observationService.searchForObservations(any(), any(), any(), any(), any(), any(), any(), any(), any(), any()))
		        .thenReturn(new BaseFhirIBundleResourceProviderTest<>(Collections.singletonList(observation), 10, 1));
		
		MockHttpServletResponse response = get(uri).accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), equalTo(FhirMediaTypes.JSON.toString()));
		
		Bundle results = readBundleResponse(response);
		assertThat(results.getEntry(), notNullValue());
		assertThat(results.getEntry(), not(empty()));
		assertThat(results.getEntry().get(0).getResource(), notNullValue());
		assertThat(results.getEntry().get(0).getResource().getIdElement().getIdPart(), equalTo(OBS_UUID));
	}
}
