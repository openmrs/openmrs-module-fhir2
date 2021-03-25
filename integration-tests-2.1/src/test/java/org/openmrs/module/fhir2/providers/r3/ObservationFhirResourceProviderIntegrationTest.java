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

import static org.exparity.hamcrest.date.DateMatchers.sameOrAfter;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.startsWith;

import java.io.InputStream;
import java.math.BigDecimal;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import lombok.AccessLevel;
import lombok.Getter;
import org.apache.commons.io.IOUtils;
import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.dstu3.model.Observation;
import org.hl7.fhir.dstu3.model.OperationOutcome;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.module.fhir2.FhirConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletResponse;

public class ObservationFhirResourceProviderIntegrationTest extends BaseFhirR3IntegrationTest<ObservationFhirResourceProvider, Observation> {
	
	private static final String OBS_DATA_XML = "org/openmrs/module/fhir2/api/dao/impl/FhirObservationDaoImplTest_initial_data_2.1.xml";
	
	private static final String JSON_CREATE_OBS_DOCUMENT = "org/openmrs/module/fhir2/providers/ObservationWebTest_create_r3.json";
	
	private static final String XML_CREATE_OBS_DOCUMENT = "org/openmrs/module/fhir2/providers/ObservationWebTest_create_r3.xml";
	
	private static final String OBS_UUID = "b0b9c14f-2123-4c0f-9a5c-918e192629f0";
	
	private static final String OBS_CONCEPT_UUID = "5085AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
	
	private static final String OBS_CONCEPT_DISPLAY_NAME = "Systolic blood pressure";
	
	private static final String CIEL_SYSTEM_URI = "https://openconceptlab.org/orgs/CIEL/sources/CIEL";
	
	private static final String OBS_CONCEPT_CIEL_ID = "5085";
	
	private static final BigDecimal OBS_CONCEPT_VALUE = BigDecimal.valueOf(115.0);
	
	private static final BigDecimal OBS_LOW_REFERENCE_RANGE = BigDecimal.valueOf(0.0);
	
	private static final BigDecimal OBS_HIGH_REFERENCE_RANGE = BigDecimal.valueOf(250.0);
	
	private static final String OBS_PATIENT_UUID = "5946f880-b197-400b-9caa-a3c661d23041";
	
	private static final String OBS_ENCOUNTER_UUID = "6519d653-393b-4118-9c83-a3715b82d4ac";
	
	private static final String WRONG_OBS_UUID = "121b73a6-e1a4-4424-8610-d5765bf2fdf7";
	
	@Autowired
	@Getter(AccessLevel.PUBLIC)
	private ObservationFhirResourceProvider resourceProvider;
	
	@Before
	@Override
	public void setup() throws Exception {
		super.setup();
		
		executeDataSet(OBS_DATA_XML);
	}
	
	@Test
	public void shouldReturnExistingObservationAsJson() throws Exception {
		MockHttpServletResponse response = get("/Observation/" + OBS_UUID).accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), is(FhirMediaTypes.JSON.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		Observation observation = readResponse(response);
		
		assertThat(observation, notNullValue());
		assertThat(observation.getIdElement().getIdPart(), equalTo(OBS_UUID));
		
		// verify expected codings
		assertThat(observation.getCode().getCoding(),
		    hasItem(allOf(hasProperty("system", nullValue()), hasProperty("code", equalTo(OBS_CONCEPT_UUID)))));
		assertThat(observation.getCode().getCoding(), hasItem(
		    allOf(hasProperty("system", equalTo(CIEL_SYSTEM_URI)), hasProperty("code", equalTo(OBS_CONCEPT_CIEL_ID)))));
		assertThat(observation.getCode().getCodingFirstRep().getDisplay(), equalTo(OBS_CONCEPT_DISPLAY_NAME));
		
		// verify expected value
		assertThat(observation.getValueQuantity(), notNullValue());
		assertThat(observation.getValueQuantity().getValue(), equalTo(OBS_CONCEPT_VALUE));
		
		// verify reference ranges
		assertThat(observation.getReferenceRange(), notNullValue());
		assertThat(observation.getReferenceRange(), not(empty()));
		assertThat(observation.getReferenceRange(),
		    hasItem(hasProperty("low", hasProperty("value", equalTo(OBS_LOW_REFERENCE_RANGE)))));
		assertThat(observation.getReferenceRange(),
		    hasItem(hasProperty("high", hasProperty("value", equalTo(OBS_HIGH_REFERENCE_RANGE)))));
		
		// verify expected patient
		assertThat(observation.getSubject(), notNullValue());
		assertThat(observation.getSubject().getReference(), containsString("Patient/"));
		assertThat(observation.getSubject().getReference(), endsWith("/" + OBS_PATIENT_UUID));
		
		//verify expected encounter
		assertThat(observation.getContext(), notNullValue());
		assertThat(observation.getContext().getReference(), endsWith("/" + OBS_ENCOUNTER_UUID));
		
		assertThat(observation, validResource());
	}
	
	@Test
	public void shouldReturnNotFoundWhenObservationNotFoundAsJson() throws Exception {
		MockHttpServletResponse response = get("/Observation/" + WRONG_OBS_UUID).accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isNotFound());
		assertThat(response.getContentType(), is(FhirMediaTypes.JSON.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		OperationOutcome operationOutcome = readOperationOutcome(response);
		
		assertThat(operationOutcome, notNullValue());
		assertThat(operationOutcome.getIssue(), hasSize(greaterThanOrEqualTo(1)));
		assertThat(operationOutcome.getIssue(),
		    hasItem(hasProperty("severity", equalTo(OperationOutcome.IssueSeverity.ERROR))));
	}
	
	@Test
	public void shouldReturnExistingObservationAsXML() throws Exception {
		MockHttpServletResponse response = get("/Observation/" + OBS_UUID).accept(FhirMediaTypes.XML).go();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), is(FhirMediaTypes.XML.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		Observation observation = readResponse(response);
		
		assertThat(observation, notNullValue());
		assertThat(observation.getIdElement().getIdPart(), equalTo(OBS_UUID));
		
		// verify expected codings
		assertThat(observation.getCode().getCoding(),
		    hasItem(allOf(hasProperty("system", nullValue()), hasProperty("code", equalTo(OBS_CONCEPT_UUID)))));
		assertThat(observation.getCode().getCoding(), hasItem(
		    allOf(hasProperty("system", equalTo(CIEL_SYSTEM_URI)), hasProperty("code", equalTo(OBS_CONCEPT_CIEL_ID)))));
		assertThat(observation.getCode().getCodingFirstRep().getDisplay(), equalTo(OBS_CONCEPT_DISPLAY_NAME));
		
		// verify expected value
		assertThat(observation.getValueQuantity(), notNullValue());
		assertThat(observation.getValueQuantity().getValue(), equalTo(OBS_CONCEPT_VALUE));
		
		// verify reference ranges
		assertThat(observation.getReferenceRange(), notNullValue());
		assertThat(observation.getReferenceRange(), not(empty()));
		assertThat(observation.getReferenceRange(),
		    hasItem(hasProperty("low", hasProperty("value", equalTo(OBS_LOW_REFERENCE_RANGE)))));
		assertThat(observation.getReferenceRange(),
		    hasItem(hasProperty("high", hasProperty("value", equalTo(OBS_HIGH_REFERENCE_RANGE)))));
		
		// verify expected patient
		assertThat(observation.getSubject(), notNullValue());
		assertThat(observation.getSubject().getReference(), containsString("Patient/"));
		assertThat(observation.getSubject().getReference(), endsWith("/" + OBS_PATIENT_UUID));
		
		//verify expected encounter
		assertThat(observation.getContext(), notNullValue());
		assertThat(observation.getContext().getReference(), endsWith("/" + OBS_ENCOUNTER_UUID));
		
		assertThat(observation, validResource());
	}
	
	@Test
	public void shouldReturnNotFoundWhenObservationNotFoundAsXML() throws Exception {
		MockHttpServletResponse response = get("/Observation/" + WRONG_OBS_UUID).accept(FhirMediaTypes.XML).go();
		
		assertThat(response, isNotFound());
		assertThat(response.getContentType(), is(FhirMediaTypes.XML.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		OperationOutcome operationOutcome = readOperationOutcome(response);
		
		assertThat(operationOutcome, notNullValue());
		assertThat(operationOutcome.getIssue(), hasSize(greaterThanOrEqualTo(1)));
		assertThat(operationOutcome.getIssue(),
		    hasItem(hasProperty("severity", equalTo(OperationOutcome.IssueSeverity.ERROR))));
	}
	
	@Test
	public void shouldCreateNewObservationAsJson() throws Exception {
		// read JSON record
		String jsonObs;
		try (InputStream is = this.getClass().getClassLoader().getResourceAsStream(JSON_CREATE_OBS_DOCUMENT)) {
			Objects.requireNonNull(is);
			jsonObs = IOUtils.toString(is, StandardCharsets.UTF_8);
		}
		
		// create obs
		MockHttpServletResponse response = post("/Observation").accept(FhirMediaTypes.JSON).jsonContent(jsonObs).go();
		
		// verify created correctly
		assertThat(response, isCreated());
		assertThat(response.getHeader("Location"), notNullValue());
		assertThat(response.getContentType(), is(FhirMediaTypes.JSON.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		Observation observation = readResponse(response);
		
		assertThat(observation.getIdElement().getIdPart(), notNullValue());
		assertThat(observation.getCode(), notNullValue());
		assertThat(observation.getCode().getCoding(),
		    hasItem(hasProperty("code", equalTo("5090AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA"))));
		assertThat(observation.getSubject(), notNullValue());
		assertThat(observation.getSubject().getReference(), endsWith("5946f880-b197-400b-9caa-a3c661d23041"));
		assertThat(observation.getContext(), notNullValue());
		assertThat(observation.getContext().getReference(), endsWith("6519d653-393b-4118-9c83-a3715b82d4ac"));
		assertThat(observation.getValue(), notNullValue());
		assertThat(observation.getValueQuantity(), notNullValue());
		assertThat(observation.getValueQuantity().getValue(), equalTo(BigDecimal.valueOf(156.0)));
		assertThat(observation.getValueQuantity().getUnit(), equalTo("cm"));
		assertThat(observation, validResource());
		
		// try to fetch the new observation
		response = get("/Observation/" + observation.getIdElement().getIdPart()).accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isOk());
		
		Observation newObservation = readResponse(response);
		
		assertThat(newObservation.getId(), equalTo(observation.getId()));
	}
	
	@Test
	public void shouldCreateNewObservationAsXML() throws Exception {
		// read JSON record
		String xmlObs;
		try (InputStream is = this.getClass().getClassLoader().getResourceAsStream(XML_CREATE_OBS_DOCUMENT)) {
			Objects.requireNonNull(is);
			xmlObs = IOUtils.toString(is, StandardCharsets.UTF_8);
		}
		
		// create obs
		MockHttpServletResponse response = post("/Observation").accept(FhirMediaTypes.XML).xmlContext(xmlObs).go();
		
		// verify created correctly
		assertThat(response, isCreated());
		assertThat(response.getHeader("Location"), notNullValue());
		assertThat(response.getContentType(), is(FhirMediaTypes.XML.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		Observation observation = readResponse(response);
		
		assertThat(observation.getIdElement().getIdPart(), notNullValue());
		assertThat(observation.getCode(), notNullValue());
		assertThat(observation.getCode().getCoding(),
		    hasItem(hasProperty("code", equalTo("5090AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA"))));
		assertThat(observation.getSubject(), notNullValue());
		assertThat(observation.getSubject().getReference(), endsWith("5946f880-b197-400b-9caa-a3c661d23041"));
		assertThat(observation.getContext(), notNullValue());
		assertThat(observation.getContext().getReference(), endsWith("6519d653-393b-4118-9c83-a3715b82d4ac"));
		assertThat(observation.getValue(), notNullValue());
		assertThat(observation.getValueQuantity(), notNullValue());
		assertThat(observation.getValueQuantity().getValue(), equalTo(BigDecimal.valueOf(156.0)));
		assertThat(observation.getValueQuantity().getUnit(), equalTo("cm"));
		assertThat(observation, validResource());
		
		// try to fetch the new observation
		response = get("/Observation/" + observation.getIdElement().getIdPart()).accept(FhirMediaTypes.XML).go();
		
		assertThat(response, isOk());
		
		Observation newObservation = readResponse(response);
		
		assertThat(newObservation.getId(), equalTo(observation.getId()));
	}
	
	@Test
	public void shouldDeleteExistingObservation() throws Exception {
		MockHttpServletResponse response = delete("/Observation/" + OBS_UUID).accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isOk());
		
		response = get("/Observation/" + OBS_UUID).accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, statusEquals(HttpStatus.GONE));
	}
	
	@Test
	public void shouldReturnNotFoundWhenDeletingNonExistentObservation() throws Exception {
		MockHttpServletResponse response = delete("/Observation/" + WRONG_OBS_UUID).accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isNotFound());
		assertThat(response.getContentType(), is(FhirMediaTypes.JSON.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		OperationOutcome operationOutcome = readOperationOutcome(response);
		
		assertThat(operationOutcome, notNullValue());
		assertThat(operationOutcome.hasIssue(), is(true));
	}
	
	@Test
	public void shouldSearchForExistingObservationsAsJson() throws Exception {
		MockHttpServletResponse response = get("/Observation").accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), is(FhirMediaTypes.JSON.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		Bundle results = readBundleResponse(response);
		
		assertThat(results, notNullValue());
		assertThat(results.getType(), equalTo(Bundle.BundleType.SEARCHSET));
		assertThat(results.hasEntry(), is(true));
		
		List<Bundle.BundleEntryComponent> entries = results.getEntry();
		
		assertThat(entries, everyItem(hasProperty("fullUrl", startsWith("http://localhost/ws/fhir2/R3/Observation/"))));
		assertThat(entries, everyItem(hasResource(instanceOf(Observation.class))));
		assertThat(entries, everyItem(hasResource(validResource())));
	}
	
	@Test
	public void shouldSearchForFilteredObservationsAsJson() throws Exception {
		MockHttpServletResponse response = get("/Observation?subject.name=Chebaskwony&_sort=-date")
		        .accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), is(FhirMediaTypes.JSON.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		Bundle results = readBundleResponse(response);
		
		assertThat(results, notNullValue());
		assertThat(results.getType(), equalTo(Bundle.BundleType.SEARCHSET));
		assertThat(results.hasEntry(), is(true));
		
		List<Bundle.BundleEntryComponent> entries = results.getEntry();
		
		assertThat(entries, everyItem(hasProperty("fullUrl", startsWith("http://localhost/ws/fhir2/R3/Observation/"))));
		assertThat(entries, everyItem(hasResource(instanceOf(Observation.class))));
		assertThat(entries,
		    everyItem(hasResource(hasProperty("subject", hasProperty("reference", endsWith(OBS_PATIENT_UUID))))));
		assertThat(entries, everyItem(hasResource(validResource())));
	}
	
	@Test
	public void shouldSearchForExistingObservationsAsXML() throws Exception {
		MockHttpServletResponse response = get("/Observation").accept(FhirMediaTypes.XML).go();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), is(FhirMediaTypes.XML.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		Bundle results = readBundleResponse(response);
		
		assertThat(results, notNullValue());
		assertThat(results.getType(), equalTo(Bundle.BundleType.SEARCHSET));
		assertThat(results.hasEntry(), is(true));
		
		List<Bundle.BundleEntryComponent> entries = results.getEntry();
		
		assertThat(entries, everyItem(hasProperty("fullUrl", startsWith("http://localhost/ws/fhir2/R3/Observation/"))));
		assertThat(entries, everyItem(hasResource(instanceOf(Observation.class))));
		assertThat(entries, everyItem(hasResource(validResource())));
	}
	
	@Test
	public void shouldSearchForFilteredObservationsAsXML() throws Exception {
		MockHttpServletResponse response = get("/Observation?subject.name=Chebaskwony&_sort=-date")
		        .accept(FhirMediaTypes.XML).go();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), is(FhirMediaTypes.XML.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		Bundle results = readBundleResponse(response);
		
		assertThat(results, notNullValue());
		assertThat(results.getType(), equalTo(Bundle.BundleType.SEARCHSET));
		assertThat(results.hasEntry(), is(true));
		
		List<Bundle.BundleEntryComponent> entries = results.getEntry();
		
		assertThat(entries, everyItem(hasProperty("fullUrl", startsWith("http://localhost/ws/fhir2/R3/Observation/"))));
		assertThat(entries, everyItem(hasResource(instanceOf(Observation.class))));
		assertThat(entries,
		    everyItem(hasResource(hasProperty("subject", hasProperty("reference", endsWith(OBS_PATIENT_UUID))))));
		assertThat(entries, everyItem(hasResource(validResource())));
	}
	
	@Test
	public void shouldReturnCorrectInterpretationAsJson() throws Exception {
		MockHttpServletResponse response = get("/Observation/" + OBS_UUID).accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), is(FhirMediaTypes.JSON.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		Observation observation = readResponse(response);
		
		assertThat(observation, notNullValue());
		assertThat(observation.getIdElement().getIdPart(), equalTo(OBS_UUID));
		
		assertThat(observation.getInterpretation().getCodingFirstRep().getSystem(),
		    equalTo(FhirConstants.INTERPRETATION_SYSTEM_URI));
		assertThat(observation.getInterpretation().getCodingFirstRep().getCode(), equalTo("N"));
		assertThat(observation.getInterpretation().getCodingFirstRep().getDisplay(), equalTo("Normal"));
	}
	
	@Test
	public void shouldReturnCorrectInterpretationAsXML() throws Exception {
		MockHttpServletResponse response = get("/Observation/" + OBS_UUID).accept(FhirMediaTypes.XML).go();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), is(FhirMediaTypes.XML.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		Observation observation = readResponse(response);
		
		assertThat(observation, notNullValue());
		assertThat(observation.getIdElement().getIdPart(), equalTo(OBS_UUID));
		
		assertThat(observation.getInterpretation().getCodingFirstRep().getSystem(),
		    equalTo(FhirConstants.INTERPRETATION_SYSTEM_URI));
		assertThat(observation.getInterpretation().getCodingFirstRep().getCode(), equalTo("N"));
		assertThat(observation.getInterpretation().getCodingFirstRep().getDisplay(), equalTo("Normal"));
	}
	
	@Test
	public void shouldReturnCorrectStatusAsJSON() throws Exception {
		MockHttpServletResponse response = get("/Observation/" + OBS_UUID).accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), is(FhirMediaTypes.JSON.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		Observation observation = readResponse(response);
		
		assertThat(observation, notNullValue());
		assertThat(observation.getIdElement().getIdPart(), equalTo(OBS_UUID));
		assertThat(observation.getStatus(), equalTo(Observation.ObservationStatus.FINAL));
	}
	
	@Test
	public void shouldReturnCorrectStatusAsXML() throws Exception {
		MockHttpServletResponse response = get("/Observation/" + OBS_UUID).accept(FhirMediaTypes.XML).go();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), is(FhirMediaTypes.XML.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		Observation observation = readResponse(response);
		
		assertThat(observation, notNullValue());
		assertThat(observation.getIdElement().getIdPart(), equalTo(OBS_UUID));
		assertThat(observation.getStatus(), equalTo(Observation.ObservationStatus.FINAL));
	}
	
	@Test
	public void shouldSupportMultiplePagesAsJson() throws Exception {
		MockHttpServletResponse response = get("/Observation?subject.name=Chebaskwony&_sort=-date")
		        .accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), is(FhirMediaTypes.JSON.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		Bundle results = readBundleResponse(response);
		
		assertThat(results, notNullValue());
		assertThat(results.getType(), equalTo(Bundle.BundleType.SEARCHSET));
		assertThat(results.hasEntry(), is(true));
		
		List<Observation> observations = results.getEntry().stream().map(Bundle.BundleEntryComponent::getResource)
		        .filter(it -> it instanceof Observation).map(it -> (Observation) it).collect(Collectors.toList());
		
		Bundle.BundleLinkComponent link = results.getLink("next");
		while (link != null) {
			String nextUrl = link.getUrl();
			URL url = new URL(nextUrl);
			
			// NB Because we cannot use the *full* URL, we use the relevant portion, which in this case, is just the query
			// string
			response = get("?" + url.getQuery()).accept(FhirMediaTypes.JSON).go();
			
			assertThat(response, isOk());
			assertThat(response.getContentType(), is(FhirMediaTypes.JSON.toString()));
			assertThat(response.getContentAsString(), notNullValue());
			
			results = readBundleResponse(response);
			
			assertThat(results, notNullValue());
			assertThat(results.getType(), equalTo(Bundle.BundleType.SEARCHSET));
			assertThat(results.hasEntry(), is(true));
			
			observations.addAll(results.getEntry().stream().map(Bundle.BundleEntryComponent::getResource)
			        .filter(it -> it instanceof Observation).map(it -> (Observation) it).collect(Collectors.toList()));
			
			link = results.getLink("next");
		}
		
		assertThat(observations, hasSize(equalTo(results.getTotal())));
		for (int i = 1; i < observations.size(); i++) {
			assertThat(observations.get(i - 1).getEffectiveDateTimeType().getValue(),
			    sameOrAfter(observations.get(i).getEffectiveDateTimeType().getValue()));
		}
	}
	
	@Test
	public void shouldSupportMultiplePagesAsXML() throws Exception {
		MockHttpServletResponse response = get("/Observation?subject.name=Chebaskwony&_sort=-date")
		        .accept(FhirMediaTypes.XML).go();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), is(FhirMediaTypes.XML.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		Bundle results = readBundleResponse(response);
		
		assertThat(results, notNullValue());
		assertThat(results.getType(), equalTo(Bundle.BundleType.SEARCHSET));
		assertThat(results.hasEntry(), is(true));
		
		List<Observation> observations = results.getEntry().stream().map(Bundle.BundleEntryComponent::getResource)
		        .filter(it -> it instanceof Observation).map(it -> (Observation) it).collect(Collectors.toList());
		
		Bundle.BundleLinkComponent link = results.getLink("next");
		while (link != null) {
			String nextUrl = link.getUrl();
			URL url = new URL(nextUrl);
			
			// NB Because we cannot use the *full* URL, we use the relevant portion, which in this case, is just the query
			// string
			response = get("?" + url.getQuery()).accept(FhirMediaTypes.XML).go();
			
			assertThat(response, isOk());
			assertThat(response.getContentType(), is(FhirMediaTypes.XML.toString()));
			assertThat(response.getContentAsString(), notNullValue());
			
			results = readBundleResponse(response);
			
			assertThat(results, notNullValue());
			assertThat(results.getType(), equalTo(Bundle.BundleType.SEARCHSET));
			assertThat(results.hasEntry(), is(true));
			
			observations.addAll(results.getEntry().stream().map(Bundle.BundleEntryComponent::getResource)
			        .filter(it -> it instanceof Observation).map(it -> (Observation) it).collect(Collectors.toList()));
			
			link = results.getLink("next");
		}
		
		assertThat(observations, hasSize(equalTo(results.getTotal())));
		for (int i = 1; i < observations.size(); i++) {
			assertThat(observations.get(i - 1).getEffectiveDateTimeType().getValue(),
			    sameOrAfter(observations.get(i).getEffectiveDateTimeType().getValue()));
		}
	}
}
