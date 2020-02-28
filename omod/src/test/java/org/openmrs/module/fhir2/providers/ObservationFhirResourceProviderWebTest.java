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
import static org.openmrs.module.fhir2.FhirConstants.AUT;
import static org.openmrs.module.fhir2.FhirConstants.AUTHOR;

import javax.servlet.ServletException;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;

import ca.uhn.fhir.rest.param.ReferenceParam;
import ca.uhn.fhir.rest.param.TokenAndListParam;
import ca.uhn.fhir.rest.param.TokenOrListParam;
import lombok.AccessLevel;
import lombok.Getter;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.Observation;
import org.hl7.fhir.r4.model.Provenance;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.api.FhirObservationService;
import org.openmrs.module.fhir2.api.util.FhirUtils;
import org.openmrs.module.fhir2.web.servlet.BaseFhirResourceProviderTest;
import org.springframework.mock.web.MockHttpServletResponse;

@RunWith(MockitoJUnitRunner.class)
public class ObservationFhirResourceProviderWebTest extends BaseFhirResourceProviderTest<ObservationFhirResourceProvider, Observation> {
	
	private static final String OBS_UUID = "39fb7f47-e80a-4056-9285-bd798be13c63";
	
	private static final String BAD_OBS_UUID = "121b73a6-e1a4-4424-8610-d5765bf2fdf7";
	
	private static final String PATIENT_UUID = "d9bc6c12-6adc-4ca6-8bde-441ec1a1c344";
	
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
	private ArgumentCaptor<ReferenceParam> patientCaptor;
	
	@Captor
	private ArgumentCaptor<ReferenceParam> encounterCaptor;
	
	@Captor
	private ArgumentCaptor<TokenAndListParam> codeCaptor;
	
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
		when(observationService.getObservationByUuid(OBS_UUID)).thenReturn(observation);
		
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
		
		verify(observationService).searchForObservations(isNull(), patientCaptor.capture(), isNull(), isNull());
		assertThat(patientCaptor.getValue(), notNullValue());
		assertThat(patientCaptor.getValue().getIdPart(), equalTo(PATIENT_UUID));
	}
	
	@Test
	public void shouldGetObservationsByPatientUuid() throws Exception {
		verifyUri("/Observation?subject:Patient=" + PATIENT_UUID);
		
		verify(observationService).searchForObservations(isNull(), patientCaptor.capture(), isNull(), isNull());
		assertThat(patientCaptor.getValue(), notNullValue());
		assertThat(patientCaptor.getValue().getIdPart(), equalTo(PATIENT_UUID));
		assertThat(patientCaptor.getValue().getResourceType(), equalTo("Patient"));
	}
	
	@Test
	public void shouldGetObservationsByPatientIdentifier() throws Exception {
		verifyUri("/Observation?subject.identifier=M4001-1");
		
		verify(observationService).searchForObservations(isNull(), patientCaptor.capture(), isNull(), isNull());
		assertThat(patientCaptor.getValue(), notNullValue());
		assertThat(patientCaptor.getValue().getChain(), equalTo("identifier"));
		assertThat(patientCaptor.getValue().getValue(), equalTo("M4001-1"));
	}
	
	@Test
	public void shouldGetObservationsByPatientName() throws Exception {
		verifyUri("/Observation?subject.name=Hannibal Lector");
		
		verify(observationService).searchForObservations(isNull(), patientCaptor.capture(), isNull(), isNull());
		assertThat(patientCaptor.getValue(), notNullValue());
		assertThat(patientCaptor.getValue().getChain(), equalTo("name"));
		assertThat(patientCaptor.getValue().getValue(), equalTo("Hannibal Lector"));
	}
	
	@Test
	public void shouldGetObservationsByPatientGivenName() throws Exception {
		verifyUri("/Observation?subject.given=Hannibal");
		
		verify(observationService).searchForObservations(isNull(), patientCaptor.capture(), isNull(), isNull());
		assertThat(patientCaptor.getValue(), notNullValue());
		assertThat(patientCaptor.getValue().getChain(), equalTo("given"));
		assertThat(patientCaptor.getValue().getValue(), equalTo("Hannibal"));
	}
	
	@Test
	public void shouldGetObservationsByPatientFamilyName() throws Exception {
		verifyUri("/Observation?subject.family=Lector");
		
		verify(observationService).searchForObservations(isNull(), patientCaptor.capture(), isNull(), isNull());
		assertThat(patientCaptor.getValue(), notNullValue());
		assertThat(patientCaptor.getValue().getChain(), equalTo("family"));
		assertThat(patientCaptor.getValue().getValue(), equalTo("Lector"));
	}
	
	@Test
	public void shouldGetObservationsByEncounterUuid() throws Exception {
		verifyUri("/Observation?encounter=c4aa5682-90cf-48e8-87c9-a6066ffd3a3f");
		
		verify(observationService).searchForObservations(encounterCaptor.capture(), isNull(), isNull(), isNull());
		assertThat(encounterCaptor.getValue(), notNullValue());
		assertThat(encounterCaptor.getValue().getIdPart(), equalTo("c4aa5682-90cf-48e8-87c9-a6066ffd3a3f"));
	}
	
	@Test
	public void shouldGetObservationsByConceptId() throws Exception {
		verifyUri("/Observation?code=5098");
		
		verify(observationService).searchForObservations(isNull(), isNull(), codeCaptor.capture(), isNull());
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
	public void shouldGetObservationsByConceptAndSystem() throws Exception {
		verifyUri("/Observation?code=" + URL_ENCODED_CIEL_URN + "|5098");
		
		verify(observationService).searchForObservations(isNull(), isNull(), codeCaptor.capture(), isNull());
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
		
		verify(observationService).searchForObservations(isNull(), isNull(), codeCaptor.capture(), isNull());
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
	public void shouldGetObservationsByPatientAndConcept() throws Exception {
		verifyUri("/Observation?code=" + URL_ENCODED_CIEL_URN + "|5098&subject:Patient=" + PATIENT_UUID);
		
		verify(observationService).searchForObservations(isNull(), patientCaptor.capture(), codeCaptor.capture(), isNull());
		
		// verify patient parameter
		assertThat(patientCaptor.getValue(), notNullValue());
		assertThat(patientCaptor.getValue().getIdPart(), equalTo(PATIENT_UUID));
		assertThat(patientCaptor.getValue().getResourceType(), equalTo("Patient"));
		
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
	
	private void verifyUri(String uri) throws Exception {
		Observation observation = new Observation();
		observation.setId(OBS_UUID);
		when(observationService.searchForObservations(any(), any(), any(), any()))
		        .thenReturn(Collections.singletonList(observation));
		
		MockHttpServletResponse response = get(uri).accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), equalTo(FhirMediaTypes.JSON.toString()));
		
		Bundle results = readBundleResponse(response);
		assertThat(results.getEntry(), notNullValue());
		assertThat(results.getEntry(), not(empty()));
		assertThat(results.getEntry().get(0).getResource(), notNullValue());
		assertThat(results.getEntry().get(0).getResource().getIdElement().getIdPart(), equalTo(OBS_UUID));
	}
	
	@Test
	public void shouldVerifyGetObservationHistoryByIdUri() throws Exception {
		Observation observation = new Observation();
		observation.setId(OBS_UUID);
		when(observationService.getObservationByUuid(OBS_UUID)).thenReturn(observation);
		
		MockHttpServletResponse response = getPatientHistoryRequest();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), equalTo(BaseFhirResourceProviderTest.FhirMediaTypes.JSON.toString()));
	}
	
	@Test
	public void shouldGetObservationHistoryById() throws IOException, ServletException {
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
		Observation observation = new Observation();
		observation.setId(OBS_UUID);
		observation.addContained(provenance);
		
		when(observationService.getObservationByUuid(OBS_UUID)).thenReturn(observation);
		
		MockHttpServletResponse response = getPatientHistoryRequest();
		
		Bundle results = readBundleResponse(response);
		assertThat(results, notNullValue());
		assertThat(results.hasEntry(), is(true));
		assertThat(results.getEntry().get(0).getResource(), notNullValue());
		assertThat(results.getEntry().get(0).getResource().getResourceType().name(),
		    equalTo(Provenance.class.getSimpleName()));
		
	}
	
	@Test
	public void getObservationHistoryById_shouldReturnBundleWithEmptyEntriesIfObservationContainedIsEmpty()
	        throws Exception {
		Observation observation = new Observation();
		observation.setId(OBS_UUID);
		observation.setContained(new ArrayList<>());
		when(observationService.getObservationByUuid(OBS_UUID)).thenReturn(observation);
		
		MockHttpServletResponse response = getPatientHistoryRequest();
		Bundle results = readBundleResponse(response);
		assertThat(results.hasEntry(), is(false));
	}
	
	@Test
	public void getObservationHistoryById_shouldReturn404IfObservationIdIsWrong() throws Exception {
		MockHttpServletResponse response = get("/Observation/" + BAD_OBS_UUID + "/_history")
		        .accept(BaseFhirResourceProviderTest.FhirMediaTypes.JSON).go();
		
		assertThat(response, isNotFound());
	}
	
	private MockHttpServletResponse getPatientHistoryRequest() throws IOException, ServletException {
		return get("/Observation/" + OBS_UUID + "/_history").accept(BaseFhirResourceProviderTest.FhirMediaTypes.JSON).go();
	}
}
