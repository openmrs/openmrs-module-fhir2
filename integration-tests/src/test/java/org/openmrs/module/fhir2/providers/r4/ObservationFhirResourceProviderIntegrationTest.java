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

import static org.exparity.hamcrest.date.DateMatchers.sameOrAfter;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
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
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.startsWith;

import java.io.InputStream;
import java.math.BigDecimal;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import lombok.AccessLevel;
import lombok.Getter;
import org.apache.commons.io.IOUtils;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Observation;
import org.hl7.fhir.r4.model.OperationOutcome;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.module.fhir2.api.dao.FhirEncounterDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletResponse;

public class ObservationFhirResourceProviderIntegrationTest extends BaseFhirR4IntegrationTest<ObservationFhirResourceProvider, Observation> {
	
	private static final String OBS_DATA_XML = "org/openmrs/module/fhir2/api/dao/impl/FhirObservationDaoImplTest_initial_data_suppl.xml";
	
	private static final String JSON_CREATE_OBS_DOCUMENT = "org/openmrs/module/fhir2/providers/ObservationWebTest_create.json";
	
	private static final String XML_CREATE_OBS_DOCUMENT = "org/openmrs/module/fhir2/providers/ObservationWebTest_create.xml";
	
	private static final String OBS_UUID = "39fb7f47-e80a-4056-9285-bd798be13c63";
	
	private static final String OBS_CONCEPT_UUID = "5089AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
	
	private static final String OBS_CONCEPT_DISPLAY_NAME = "Weight";
	
	private static final String CIEL_SYSTEM_URN = "https://openconceptlab.org/orgs/CIEL/sources/CIEL";
	
	private static final String OBS_CONCEPT_CIEL_ID = "5089";
	
	private static final BigDecimal OBS_CONCEPT_VALUE = BigDecimal.valueOf(50.0);
	
	private static final BigDecimal OBS_LOW_REFERENCE_RANGE = BigDecimal.valueOf(0.0);
	
	private static final BigDecimal OBS_HIGH_REFERENCE_RANGE = BigDecimal.valueOf(250.0);
	
	private static final String OBS_PATIENT_UUID = "5946f880-b197-400b-9caa-a3c661d23041";
	
	private static final String OBS_ENCOUNTER_UUID = "6519d653-393b-4118-9c83-a3715b82d4ac";
	
	private static final String WRONG_OBS_UUID = "121b73a6-e1a4-4424-8610-d5765bf2fdf7";
	
	@Autowired
	@Getter(AccessLevel.PUBLIC)
	private ObservationFhirResourceProvider resourceProvider;
	
	@Autowired
	private FhirEncounterDao encounterDao;
	
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
		    allOf(hasProperty("system", equalTo(CIEL_SYSTEM_URN)), hasProperty("code", equalTo(OBS_CONCEPT_CIEL_ID)))));
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
		assertThat(observation.getSubject().getType(), equalTo("Patient"));
		assertThat(observation.getSubject().getReference(), endsWith("/" + OBS_PATIENT_UUID));
		
		//verify expected encounter
		assertThat(observation.getEncounter(), notNullValue());
		assertThat(observation.getEncounter().getReference(), endsWith("/" + OBS_ENCOUNTER_UUID));
		
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
		    allOf(hasProperty("system", equalTo(CIEL_SYSTEM_URN)), hasProperty("code", equalTo(OBS_CONCEPT_CIEL_ID)))));
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
		assertThat(observation.getSubject().getType(), equalTo("Patient"));
		assertThat(observation.getSubject().getReference(), endsWith("/" + OBS_PATIENT_UUID));
		
		//verify expected encounter
		assertThat(observation.getEncounter(), notNullValue());
		assertThat(observation.getEncounter().getReference(), endsWith("/" + OBS_ENCOUNTER_UUID));
		
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
		assertThat(observation.getEncounter(), notNullValue());
		assertThat(observation.getEncounter().getReference(), endsWith("6519d653-393b-4118-9c83-a3715b82d4ac"));
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
		MockHttpServletResponse response = post("/Observation").accept(FhirMediaTypes.XML).xmlContent(xmlObs).go();
		
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
		assertThat(observation.getEncounter(), notNullValue());
		assertThat(observation.getEncounter().getReference(), endsWith("6519d653-393b-4118-9c83-a3715b82d4ac"));
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
		
		assertThat(entries, everyItem(hasProperty("fullUrl", startsWith("http://localhost/ws/fhir2/R4/Observation/"))));
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
		
		assertThat(entries, everyItem(hasProperty("fullUrl", startsWith("http://localhost/ws/fhir2/R4/Observation/"))));
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
		
		assertThat(entries, everyItem(hasProperty("fullUrl", startsWith("http://localhost/ws/fhir2/R4/Observation/"))));
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
		
		assertThat(entries, everyItem(hasProperty("fullUrl", startsWith("http://localhost/ws/fhir2/R4/Observation/"))));
		assertThat(entries, everyItem(hasResource(instanceOf(Observation.class))));
		assertThat(entries,
		    everyItem(hasResource(hasProperty("subject", hasProperty("reference", endsWith(OBS_PATIENT_UUID))))));
		assertThat(entries, everyItem(hasResource(validResource())));
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
	
	@Test
	public void shouldReturnCountForObservationAsJson() throws Exception {
		MockHttpServletResponse response = get("/Observation?subject.name=Chebaskwony&_summary=count")
		        .accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), is(FhirMediaTypes.JSON.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		Bundle result = readBundleResponse(response);
		
		assertThat(result, notNullValue());
		assertThat(result.getType(), equalTo(Bundle.BundleType.SEARCHSET));
		assertThat(result, hasProperty("total", equalTo(21)));
		
	}
	
	@Test
	public void shouldReturnCountForObservationAsXml() throws Exception {
		MockHttpServletResponse response = get("/Observation?subject.name=Chebaskwony&_summary=count")
		        .accept(FhirMediaTypes.XML).go();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), is(FhirMediaTypes.XML.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		Bundle result = readBundleResponse(response);
		
		assertThat(result, notNullValue());
		assertThat(result.getType(), equalTo(Bundle.BundleType.SEARCHSET));
		assertThat(result, hasProperty("total", equalTo(21)));
		
	}
	
	@Test
	public void shouldReturnLastnObservationsAsJson() throws Exception {
		MockHttpServletResponse response = get(
		    "Observation/$lastn?max=2&subject=" + OBS_PATIENT_UUID + "&category=laboratory&code=5242")
		            .accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), is(FhirMediaTypes.JSON.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		Bundle results = readBundleResponse(response);
		
		assertThat(results, notNullValue());
		assertThat(results.getType(), equalTo(Bundle.BundleType.COLLECTION));
		assertThat(results.hasEntry(), is(true));
		assertThat(results, hasProperty("total", equalTo(7)));
		
		List<Bundle.BundleEntryComponent> entries = results.getEntry();
		
		assertThat(entries, everyItem(hasProperty("fullUrl", startsWith("http://localhost/ws/fhir2/R4/Observation/"))));
		assertThat(entries, everyItem(hasResource(instanceOf(Observation.class))));
		assertThat(entries, everyItem(hasResource(validResource())));
		assertThat(entries,
		    everyItem(hasResource(hasProperty("subject", hasProperty("reference", endsWith(OBS_PATIENT_UUID))))));
		assertThat(entries, everyItem(hasResource(
		    hasProperty("code", hasProperty("coding", everyItem(hasProperty("display", equalTo("Respiratory rate"))))))));
		assertThat(entries, everyItem(hasResource(hasProperty("category",
		    everyItem(hasProperty("coding", everyItem(hasProperty("code", equalTo("laboratory")))))))));
		assertThat(entries, isSortedAndWithinMax(2));
	}
	
	@Test
	public void shouldReturnLastnObservationsWhenMaxIsMissingAsJson() throws Exception {
		MockHttpServletResponse response = get(
		    "Observation/$lastn?subject=" + OBS_PATIENT_UUID + "&category=laboratory&code=5242").accept(FhirMediaTypes.JSON)
		            .go();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), is(FhirMediaTypes.JSON.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		Bundle results = readBundleResponse(response);
		
		assertThat(results, notNullValue());
		assertThat(results.getType(), equalTo(Bundle.BundleType.COLLECTION));
		assertThat(results.hasEntry(), is(true));
		assertThat(results, hasProperty("total", equalTo(2)));
		
		List<Bundle.BundleEntryComponent> entries = results.getEntry();
		
		assertThat(entries, everyItem(hasProperty("fullUrl", startsWith("http://localhost/ws/fhir2/R4/Observation/"))));
		assertThat(entries, everyItem(hasResource(instanceOf(Observation.class))));
		assertThat(entries, everyItem(hasResource(validResource())));
		assertThat(entries,
		    everyItem(hasResource(hasProperty("subject", hasProperty("reference", endsWith(OBS_PATIENT_UUID))))));
		assertThat(entries, everyItem(hasResource(
		    hasProperty("code", hasProperty("coding", everyItem(hasProperty("display", equalTo("Respiratory rate"))))))));
		assertThat(entries, everyItem(hasResource(hasProperty("category",
		    everyItem(hasProperty("coding", everyItem(hasProperty("code", equalTo("laboratory")))))))));
		assertThat(entries, isSortedAndWithinMax(1));
	}
	
	@Test
	public void shouldReturnLastnObservationsWhenPatientReferenceIsPassedInPatientParameterAsJson() throws Exception {
		MockHttpServletResponse response = get(
		    "Observation/$lastn?max=2&patient=" + OBS_PATIENT_UUID + "&category=laboratory&code=5242")
		            .accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), is(FhirMediaTypes.JSON.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		Bundle results = readBundleResponse(response);
		
		assertThat(results, notNullValue());
		assertThat(results.getType(), equalTo(Bundle.BundleType.COLLECTION));
		assertThat(results.hasEntry(), is(true));
		assertThat(results, hasProperty("total", equalTo(7)));
		
		List<Bundle.BundleEntryComponent> entries = results.getEntry();
		
		assertThat(entries, everyItem(hasProperty("fullUrl", startsWith("http://localhost/ws/fhir2/R4/Observation/"))));
		assertThat(entries, everyItem(hasResource(instanceOf(Observation.class))));
		assertThat(entries, everyItem(hasResource(validResource())));
		assertThat(entries,
		    everyItem(hasResource(hasProperty("subject", hasProperty("reference", endsWith(OBS_PATIENT_UUID))))));
		assertThat(entries, everyItem(hasResource(
		    hasProperty("code", hasProperty("coding", everyItem(hasProperty("display", equalTo("Respiratory rate"))))))));
		assertThat(entries, everyItem(hasResource(hasProperty("category",
		    everyItem(hasProperty("coding", everyItem(hasProperty("code", equalTo("laboratory")))))))));
		assertThat(entries, isSortedAndWithinMax(2));
	}
	
	@Test
	public void shouldReturnLastnObservationsWhenPatientReferenceIsMissingAsJson() throws Exception {
		MockHttpServletResponse response = get("Observation/$lastn?max=2&category=laboratory&code=5242")
		        .accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), is(FhirMediaTypes.JSON.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		Bundle results = readBundleResponse(response);
		
		assertThat(results, notNullValue());
		assertThat(results.getType(), equalTo(Bundle.BundleType.COLLECTION));
		assertThat(results.hasEntry(), is(true));
		assertThat(results, hasProperty("total", equalTo(7)));
		
		List<Bundle.BundleEntryComponent> entries = results.getEntry();
		
		assertThat(entries, everyItem(hasProperty("fullUrl", startsWith("http://localhost/ws/fhir2/R4/Observation/"))));
		assertThat(entries, everyItem(hasResource(instanceOf(Observation.class))));
		assertThat(entries, everyItem(hasResource(validResource())));
		assertThat(entries, everyItem(hasResource(
		    hasProperty("code", hasProperty("coding", everyItem(hasProperty("display", equalTo("Respiratory rate"))))))));
		assertThat(entries, everyItem(hasResource(hasProperty("category",
		    everyItem(hasProperty("coding", everyItem(hasProperty("code", equalTo("laboratory")))))))));
		assertThat(entries, isSortedAndWithinMax(2));
	}
	
	@Test
	public void shouldReturnLastnObservationsWhenCategoryIsMissingAsJson() throws Exception {
		MockHttpServletResponse response = get("Observation/$lastn?max=2&patient=" + OBS_PATIENT_UUID + "&code=5242")
		        .accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), is(FhirMediaTypes.JSON.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		Bundle results = readBundleResponse(response);
		
		assertThat(results, notNullValue());
		assertThat(results.getType(), equalTo(Bundle.BundleType.COLLECTION));
		assertThat(results.hasEntry(), is(true));
		assertThat(results, hasProperty("total", equalTo(7)));
		
		List<Bundle.BundleEntryComponent> entries = results.getEntry();
		
		assertThat(entries, everyItem(hasProperty("fullUrl", startsWith("http://localhost/ws/fhir2/R4/Observation/"))));
		assertThat(entries, everyItem(hasResource(instanceOf(Observation.class))));
		assertThat(entries, everyItem(hasResource(validResource())));
		assertThat(entries,
		    everyItem(hasResource(hasProperty("subject", hasProperty("reference", endsWith(OBS_PATIENT_UUID))))));
		assertThat(entries, everyItem(hasResource(
		    hasProperty("code", hasProperty("coding", everyItem(hasProperty("display", equalTo("Respiratory rate"))))))));
		assertThat(entries, isSortedAndWithinMax(2));
	}
	
	@Test
	public void shouldReturnLastnObservationsWhenCodeIsMissingAsJson() throws Exception {
		MockHttpServletResponse response = get(
		    "Observation/$lastn?max=2&subject=" + OBS_PATIENT_UUID + "&category=laboratory").accept(FhirMediaTypes.JSON)
		            .go();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), is(FhirMediaTypes.JSON.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		Bundle results = readBundleResponse(response);
		
		assertThat(results, notNullValue());
		assertThat(results.getType(), equalTo(Bundle.BundleType.COLLECTION));
		assertThat(results.hasEntry(), is(true));
		assertThat(results, hasProperty("total", equalTo(17)));
		
		List<Bundle.BundleEntryComponent> entries = results.getEntry();
		
		assertThat(entries, everyItem(hasProperty("fullUrl", startsWith("http://localhost/ws/fhir2/R4/Observation/"))));
		assertThat(entries, everyItem(hasResource(instanceOf(Observation.class))));
		assertThat(entries, everyItem(hasResource(validResource())));
		assertThat(entries,
		    everyItem(hasResource(hasProperty("subject", hasProperty("reference", endsWith(OBS_PATIENT_UUID))))));
		assertThat(entries, everyItem(hasResource(hasProperty("category",
		    everyItem(hasProperty("coding", everyItem(hasProperty("code", equalTo("laboratory")))))))));
		assertThat(entries, isSortedAndWithinMax(2));
	}
	
	@Test
	public void shouldReturnLastnObservationsWhenNoParamterIsGivenAsJson() throws Exception {
		MockHttpServletResponse response = get("Observation/$lastn?").accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), is(FhirMediaTypes.JSON.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		Bundle results = readBundleResponse(response);
		
		assertThat(results, notNullValue());
		assertThat(results.getType(), equalTo(Bundle.BundleType.COLLECTION));
		assertThat(results.hasEntry(), is(true));
		assertThat(results, hasProperty("total", equalTo(14)));
		
		List<Bundle.BundleEntryComponent> entries = results.getEntry();
		
		assertThat(entries, everyItem(hasProperty("fullUrl", startsWith("http://localhost/ws/fhir2/R4/Observation/"))));
		assertThat(entries, everyItem(hasResource(instanceOf(Observation.class))));
		assertThat(entries, everyItem(hasResource(validResource())));
		assertThat(entries, isSortedAndWithinMax(1));
	}
	
	@Test
	public void shouldReturnLastnObservationsAsXml() throws Exception {
		MockHttpServletResponse response = get(
		    "Observation/$lastn?max=2&subject=" + OBS_PATIENT_UUID + "&category=laboratory&code=5242")
		            .accept(FhirMediaTypes.XML).go();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), is(FhirMediaTypes.XML.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		Bundle results = readBundleResponse(response);
		
		assertThat(results, notNullValue());
		assertThat(results.getType(), equalTo(Bundle.BundleType.COLLECTION));
		assertThat(results.hasEntry(), is(true));
		assertThat(results, hasProperty("total", equalTo(7)));
		
		List<Bundle.BundleEntryComponent> entries = results.getEntry();
		
		assertThat(entries, everyItem(hasProperty("fullUrl", startsWith("http://localhost/ws/fhir2/R4/Observation/"))));
		assertThat(entries, everyItem(hasResource(instanceOf(Observation.class))));
		assertThat(entries, everyItem(hasResource(validResource())));
		assertThat(entries,
		    everyItem(hasResource(hasProperty("subject", hasProperty("reference", endsWith(OBS_PATIENT_UUID))))));
		assertThat(entries, everyItem(hasResource(
		    hasProperty("code", hasProperty("coding", everyItem(hasProperty("display", equalTo("Respiratory rate"))))))));
		assertThat(entries, everyItem(hasResource(hasProperty("category",
		    everyItem(hasProperty("coding", everyItem(hasProperty("code", equalTo("laboratory")))))))));
		assertThat(entries, isSortedAndWithinMax(2));
	}
	
	@Test
	public void shouldReturnLastnObservationsWhenMaxIsMissingAsXml() throws Exception {
		MockHttpServletResponse response = get(
		    "Observation/$lastn?subject=" + OBS_PATIENT_UUID + "&category=laboratory&code=5242").accept(FhirMediaTypes.XML)
		            .go();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), is(FhirMediaTypes.XML.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		Bundle results = readBundleResponse(response);
		
		assertThat(results, notNullValue());
		assertThat(results.getType(), equalTo(Bundle.BundleType.COLLECTION));
		assertThat(results.hasEntry(), is(true));
		assertThat(results, hasProperty("total", equalTo(2)));
		
		List<Bundle.BundleEntryComponent> entries = results.getEntry();
		
		assertThat(entries, everyItem(hasProperty("fullUrl", startsWith("http://localhost/ws/fhir2/R4/Observation/"))));
		assertThat(entries, everyItem(hasResource(instanceOf(Observation.class))));
		assertThat(entries, everyItem(hasResource(validResource())));
		assertThat(entries,
		    everyItem(hasResource(hasProperty("subject", hasProperty("reference", endsWith(OBS_PATIENT_UUID))))));
		assertThat(entries, everyItem(hasResource(
		    hasProperty("code", hasProperty("coding", everyItem(hasProperty("display", equalTo("Respiratory rate"))))))));
		assertThat(entries, everyItem(hasResource(hasProperty("category",
		    everyItem(hasProperty("coding", everyItem(hasProperty("code", equalTo("laboratory")))))))));
		assertThat(entries, isSortedAndWithinMax(1));
	}
	
	@Test
	public void shouldReturnLastnObservationsWhenPatientReferenceIsPassedInPatientParameterAsXml() throws Exception {
		MockHttpServletResponse response = get(
		    "Observation/$lastn?max=2&patient=" + OBS_PATIENT_UUID + "&category=laboratory&code=5242")
		            .accept(FhirMediaTypes.XML).go();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), is(FhirMediaTypes.XML.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		Bundle results = readBundleResponse(response);
		
		assertThat(results, notNullValue());
		assertThat(results.getType(), equalTo(Bundle.BundleType.COLLECTION));
		assertThat(results.hasEntry(), is(true));
		assertThat(results, hasProperty("total", equalTo(7)));
		
		List<Bundle.BundleEntryComponent> entries = results.getEntry();
		
		assertThat(entries, everyItem(hasProperty("fullUrl", startsWith("http://localhost/ws/fhir2/R4/Observation/"))));
		assertThat(entries, everyItem(hasResource(instanceOf(Observation.class))));
		assertThat(entries, everyItem(hasResource(validResource())));
		assertThat(entries,
		    everyItem(hasResource(hasProperty("subject", hasProperty("reference", endsWith(OBS_PATIENT_UUID))))));
		assertThat(entries, everyItem(hasResource(
		    hasProperty("code", hasProperty("coding", everyItem(hasProperty("display", equalTo("Respiratory rate"))))))));
		assertThat(entries, everyItem(hasResource(hasProperty("category",
		    everyItem(hasProperty("coding", everyItem(hasProperty("code", equalTo("laboratory")))))))));
		assertThat(entries, isSortedAndWithinMax(2));
	}
	
	@Test
	public void shouldReturnLastnObservationsWhenPatientReferenceIsMissingAsXml() throws Exception {
		MockHttpServletResponse response = get("Observation/$lastn?max=2&category=laboratory&code=5242")
		        .accept(FhirMediaTypes.XML).go();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), is(FhirMediaTypes.XML.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		Bundle results = readBundleResponse(response);
		
		assertThat(results, notNullValue());
		assertThat(results.getType(), equalTo(Bundle.BundleType.COLLECTION));
		assertThat(results.hasEntry(), is(true));
		assertThat(results, hasProperty("total", equalTo(7)));
		
		List<Bundle.BundleEntryComponent> entries = results.getEntry();
		
		assertThat(entries, everyItem(hasProperty("fullUrl", startsWith("http://localhost/ws/fhir2/R4/Observation/"))));
		assertThat(entries, everyItem(hasResource(instanceOf(Observation.class))));
		assertThat(entries, everyItem(hasResource(validResource())));
		assertThat(entries, everyItem(hasResource(
		    hasProperty("code", hasProperty("coding", everyItem(hasProperty("display", equalTo("Respiratory rate"))))))));
		assertThat(entries, everyItem(hasResource(hasProperty("category",
		    everyItem(hasProperty("coding", everyItem(hasProperty("code", equalTo("laboratory")))))))));
		assertThat(entries, isSortedAndWithinMax(2));
	}
	
	@Test
	public void shouldReturnLastnObservationsWhenCategoryIsMissingAsXml() throws Exception {
		MockHttpServletResponse response = get("Observation/$lastn?max=2&patient=" + OBS_PATIENT_UUID + "&code=5242")
		        .accept(FhirMediaTypes.XML).go();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), is(FhirMediaTypes.XML.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		Bundle results = readBundleResponse(response);
		
		assertThat(results, notNullValue());
		assertThat(results.getType(), equalTo(Bundle.BundleType.COLLECTION));
		assertThat(results.hasEntry(), is(true));
		assertThat(results, hasProperty("total", equalTo(7)));
		
		List<Bundle.BundleEntryComponent> entries = results.getEntry();
		
		assertThat(entries, everyItem(hasProperty("fullUrl", startsWith("http://localhost/ws/fhir2/R4/Observation/"))));
		assertThat(entries, everyItem(hasResource(instanceOf(Observation.class))));
		assertThat(entries, everyItem(hasResource(validResource())));
		assertThat(entries,
		    everyItem(hasResource(hasProperty("subject", hasProperty("reference", endsWith(OBS_PATIENT_UUID))))));
		assertThat(entries, everyItem(hasResource(
		    hasProperty("code", hasProperty("coding", everyItem(hasProperty("display", equalTo("Respiratory rate"))))))));
		assertThat(entries, isSortedAndWithinMax(2));
	}
	
	@Test
	public void shouldReturnLastnObservationsWhenCodeIsMissingAsXml() throws Exception {
		MockHttpServletResponse response = get(
		    "Observation/$lastn?max=2&subject=" + OBS_PATIENT_UUID + "&category=laboratory").accept(FhirMediaTypes.XML).go();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), is(FhirMediaTypes.XML.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		Bundle results = readBundleResponse(response);
		
		assertThat(results, notNullValue());
		assertThat(results.getType(), equalTo(Bundle.BundleType.COLLECTION));
		assertThat(results.hasEntry(), is(true));
		assertThat(results, hasProperty("total", equalTo(17)));
		
		List<Bundle.BundleEntryComponent> entries = results.getEntry();
		
		assertThat(entries, everyItem(hasProperty("fullUrl", startsWith("http://localhost/ws/fhir2/R4/Observation/"))));
		assertThat(entries, everyItem(hasResource(instanceOf(Observation.class))));
		assertThat(entries, everyItem(hasResource(validResource())));
		assertThat(entries,
		    everyItem(hasResource(hasProperty("subject", hasProperty("reference", endsWith(OBS_PATIENT_UUID))))));
		assertThat(entries, everyItem(hasResource(hasProperty("category",
		    everyItem(hasProperty("coding", everyItem(hasProperty("code", equalTo("laboratory")))))))));
		assertThat(entries, isSortedAndWithinMax(2));
	}
	
	@Test
	public void shouldReturnLastnObservationsWhenNoParamterIsGivenAsXml() throws Exception {
		MockHttpServletResponse response = get("Observation/$lastn?").accept(FhirMediaTypes.XML).go();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), is(FhirMediaTypes.XML.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		Bundle results = readBundleResponse(response);
		
		assertThat(results, notNullValue());
		assertThat(results.getType(), equalTo(Bundle.BundleType.COLLECTION));
		assertThat(results.hasEntry(), is(true));
		assertThat(results, hasProperty("total", equalTo(14)));
		
		List<Bundle.BundleEntryComponent> entries = results.getEntry();
		
		assertThat(entries, everyItem(hasProperty("fullUrl", startsWith("http://localhost/ws/fhir2/R4/Observation/"))));
		assertThat(entries, everyItem(hasResource(instanceOf(Observation.class))));
		assertThat(entries, everyItem(hasResource(validResource())));
		assertThat(entries, isSortedAndWithinMax(1));
	}
	
	@Test
	public void shouldReturnLastnEncountersObservationsAsJson() throws Exception {
		MockHttpServletResponse response = get(
		    "Observation/$lastn-encounters?max=2&subject=" + OBS_PATIENT_UUID + "&category=laboratory&code=5089")
		            .accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), is(FhirMediaTypes.JSON.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		Bundle results = readBundleResponse(response);
		
		assertThat(results, notNullValue());
		assertThat(results.getType(), equalTo(Bundle.BundleType.COLLECTION));
		assertThat(results.hasEntry(), is(true));
		assertThat(results, hasProperty("total", equalTo(2)));
		
		List<Bundle.BundleEntryComponent> entries = results.getEntry();
		
		assertThat(entries, everyItem(hasProperty("fullUrl", startsWith("http://localhost/ws/fhir2/R4/Observation/"))));
		assertThat(entries, everyItem(hasResource(instanceOf(Observation.class))));
		assertThat(entries, everyItem(hasResource(validResource())));
		assertThat(entries,
		    everyItem(hasResource(hasProperty("subject", hasProperty("reference", endsWith(OBS_PATIENT_UUID))))));
		assertThat(entries, everyItem(
		    hasResource(hasProperty("code", hasProperty("coding", everyItem(hasProperty("display", equalTo("Weight"))))))));
		assertThat(entries, everyItem(hasResource(hasProperty("category",
		    everyItem(hasProperty("coding", everyItem(hasProperty("code", equalTo("laboratory")))))))));
		assertThat(getDistinctEncounterDatetime(entries), lessThanOrEqualTo(2));
	}
	
	@Test
	public void shouldReturnLastnEncountersObservationsWhenMaxIsMissingAsJson() throws Exception {
		MockHttpServletResponse response = get(
		    "Observation/$lastn-encounters?subject=" + OBS_PATIENT_UUID + "&category=laboratory&code=5089")
		            .accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), is(FhirMediaTypes.JSON.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		Bundle results = readBundleResponse(response);
		
		assertThat(results, notNullValue());
		assertThat(results.getType(), equalTo(Bundle.BundleType.COLLECTION));
		assertThat(results.hasEntry(), is(true));
		assertThat(results, hasProperty("total", equalTo(1)));
		
		List<Bundle.BundleEntryComponent> entries = results.getEntry();
		
		assertThat(entries, everyItem(hasProperty("fullUrl", startsWith("http://localhost/ws/fhir2/R4/Observation/"))));
		assertThat(entries, everyItem(hasResource(instanceOf(Observation.class))));
		assertThat(entries, everyItem(hasResource(validResource())));
		assertThat(entries,
		    everyItem(hasResource(hasProperty("subject", hasProperty("reference", endsWith(OBS_PATIENT_UUID))))));
		assertThat(entries, everyItem(
		    hasResource(hasProperty("code", hasProperty("coding", everyItem(hasProperty("display", equalTo("Weight"))))))));
		assertThat(entries, everyItem(hasResource(hasProperty("category",
		    everyItem(hasProperty("coding", everyItem(hasProperty("code", equalTo("laboratory")))))))));
		assertThat(getDistinctEncounterDatetime(entries), lessThanOrEqualTo(1));
	}
	
	@Test
	public void shouldReturnLastnEncountersObservationsWhenPatientReferenceIsPassedInPatientParameterAsJson()
	        throws Exception {
		MockHttpServletResponse response = get(
		    "Observation/$lastn-encounters?max=2&patient=" + OBS_PATIENT_UUID + "&category=laboratory&code=5089")
		            .accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), is(FhirMediaTypes.JSON.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		Bundle results = readBundleResponse(response);
		
		assertThat(results, notNullValue());
		assertThat(results.getType(), equalTo(Bundle.BundleType.COLLECTION));
		assertThat(results.hasEntry(), is(true));
		assertThat(results, hasProperty("total", equalTo(2)));
		
		List<Bundle.BundleEntryComponent> entries = results.getEntry();
		
		assertThat(entries, everyItem(hasProperty("fullUrl", startsWith("http://localhost/ws/fhir2/R4/Observation/"))));
		assertThat(entries, everyItem(hasResource(instanceOf(Observation.class))));
		assertThat(entries, everyItem(hasResource(validResource())));
		assertThat(entries,
		    everyItem(hasResource(hasProperty("subject", hasProperty("reference", endsWith(OBS_PATIENT_UUID))))));
		assertThat(entries, everyItem(
		    hasResource(hasProperty("code", hasProperty("coding", everyItem(hasProperty("display", equalTo("Weight"))))))));
		assertThat(entries, everyItem(hasResource(hasProperty("category",
		    everyItem(hasProperty("coding", everyItem(hasProperty("code", equalTo("laboratory")))))))));
		assertThat(getDistinctEncounterDatetime(entries), lessThanOrEqualTo(2));
	}
	
	@Test
	public void shouldReturnLastnEncountersObservationsWhenPatientReferenceIsMissingAsJson() throws Exception {
		MockHttpServletResponse response = get("Observation/$lastn-encounters?max=2&category=laboratory&code=5089")
		        .accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), is(FhirMediaTypes.JSON.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		Bundle results = readBundleResponse(response);
		
		assertThat(results, notNullValue());
		assertThat(results.getType(), equalTo(Bundle.BundleType.COLLECTION));
		assertThat(results.hasEntry(), is(true));
		assertThat(results, hasProperty("total", equalTo(2)));
		
		List<Bundle.BundleEntryComponent> entries = results.getEntry();
		
		assertThat(entries, everyItem(hasProperty("fullUrl", startsWith("http://localhost/ws/fhir2/R4/Observation/"))));
		assertThat(entries, everyItem(hasResource(instanceOf(Observation.class))));
		assertThat(entries, everyItem(hasResource(validResource())));
		assertThat(entries, everyItem(
		    hasResource(hasProperty("code", hasProperty("coding", everyItem(hasProperty("display", equalTo("Weight"))))))));
		assertThat(entries, everyItem(hasResource(hasProperty("category",
		    everyItem(hasProperty("coding", everyItem(hasProperty("code", equalTo("laboratory")))))))));
		assertThat(getDistinctEncounterDatetime(entries), lessThanOrEqualTo(2));
	}
	
	@Test
	public void shouldReturnLastnEncountersObservationsWhenCategoryIsMissingAsJson() throws Exception {
		MockHttpServletResponse response = get(
		    "Observation/$lastn-encounters?max=2&patient=" + OBS_PATIENT_UUID + "&code=5089").accept(FhirMediaTypes.JSON)
		            .go();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), is(FhirMediaTypes.JSON.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		Bundle results = readBundleResponse(response);
		
		assertThat(results, notNullValue());
		assertThat(results.getType(), equalTo(Bundle.BundleType.COLLECTION));
		assertThat(results.hasEntry(), is(true));
		assertThat(results, hasProperty("total", equalTo(2)));
		
		List<Bundle.BundleEntryComponent> entries = results.getEntry();
		
		assertThat(entries, everyItem(hasProperty("fullUrl", startsWith("http://localhost/ws/fhir2/R4/Observation/"))));
		assertThat(entries, everyItem(hasResource(instanceOf(Observation.class))));
		assertThat(entries, everyItem(hasResource(validResource())));
		assertThat(entries,
		    everyItem(hasResource(hasProperty("subject", hasProperty("reference", endsWith(OBS_PATIENT_UUID))))));
		assertThat(entries, everyItem(
		    hasResource(hasProperty("code", hasProperty("coding", everyItem(hasProperty("display", equalTo("Weight"))))))));
		assertThat(getDistinctEncounterDatetime(entries), lessThanOrEqualTo(2));
	}
	
	@Test
	public void shouldReturnLastnEncountersObservationsWhenCodeIsMissingAsJson() throws Exception {
		MockHttpServletResponse response = get(
		    "Observation/$lastn-encounters?max=2&subject=" + OBS_PATIENT_UUID + "&category=laboratory")
		            .accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), is(FhirMediaTypes.JSON.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		Bundle results = readBundleResponse(response);
		
		assertThat(results, notNullValue());
		assertThat(results.getType(), equalTo(Bundle.BundleType.COLLECTION));
		assertThat(results.hasEntry(), is(true));
		assertThat(results, hasProperty("total", equalTo(3)));
		
		List<Bundle.BundleEntryComponent> entries = results.getEntry();
		
		assertThat(entries, everyItem(hasProperty("fullUrl", startsWith("http://localhost/ws/fhir2/R4/Observation/"))));
		assertThat(entries, everyItem(hasResource(instanceOf(Observation.class))));
		assertThat(entries, everyItem(hasResource(validResource())));
		assertThat(entries,
		    everyItem(hasResource(hasProperty("subject", hasProperty("reference", endsWith(OBS_PATIENT_UUID))))));
		assertThat(entries, everyItem(hasResource(hasProperty("category",
		    everyItem(hasProperty("coding", everyItem(hasProperty("code", equalTo("laboratory")))))))));
		assertThat(getDistinctEncounterDatetime(entries), lessThanOrEqualTo(2));
	}
	
	@Test
	public void shouldReturnLastnEncountersObservationsWhenNoParamterIsGivenAsJson() throws Exception {
		MockHttpServletResponse response = get("Observation/$lastn-encounters?").accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), is(FhirMediaTypes.JSON.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		Bundle results = readBundleResponse(response);
		
		assertThat(results, notNullValue());
		assertThat(results.getType(), equalTo(Bundle.BundleType.COLLECTION));
		assertThat(results.hasEntry(), is(true));
		assertThat(results, hasProperty("total", equalTo(1)));
		
		List<Bundle.BundleEntryComponent> entries = results.getEntry();
		
		assertThat(entries, everyItem(hasProperty("fullUrl", startsWith("http://localhost/ws/fhir2/R4/Observation/"))));
		assertThat(entries, everyItem(hasResource(instanceOf(Observation.class))));
		assertThat(entries, everyItem(hasResource(validResource())));
		assertThat(getDistinctEncounterDatetime(entries), lessThanOrEqualTo(1));
	}
	
	@Test
	public void shouldReturnLastnEncountersObservationsAsXml() throws Exception {
		MockHttpServletResponse response = get(
		    "Observation/$lastn-encounters?max=2&subject=" + OBS_PATIENT_UUID + "&category=laboratory&code=5089")
		            .accept(FhirMediaTypes.XML).go();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), is(FhirMediaTypes.XML.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		Bundle results = readBundleResponse(response);
		
		assertThat(results, notNullValue());
		assertThat(results.getType(), equalTo(Bundle.BundleType.COLLECTION));
		assertThat(results.hasEntry(), is(true));
		assertThat(results, hasProperty("total", equalTo(2)));
		
		List<Bundle.BundleEntryComponent> entries = results.getEntry();
		
		assertThat(entries, everyItem(hasProperty("fullUrl", startsWith("http://localhost/ws/fhir2/R4/Observation/"))));
		assertThat(entries, everyItem(hasResource(instanceOf(Observation.class))));
		assertThat(entries, everyItem(hasResource(validResource())));
		assertThat(entries,
		    everyItem(hasResource(hasProperty("subject", hasProperty("reference", endsWith(OBS_PATIENT_UUID))))));
		assertThat(entries, everyItem(
		    hasResource(hasProperty("code", hasProperty("coding", everyItem(hasProperty("display", equalTo("Weight"))))))));
		assertThat(entries, everyItem(hasResource(hasProperty("category",
		    everyItem(hasProperty("coding", everyItem(hasProperty("code", equalTo("laboratory")))))))));
		assertThat(getDistinctEncounterDatetime(entries), lessThanOrEqualTo(2));
	}
	
	@Test
	public void shouldReturnLastnEncountersObservationsWhenMaxIsMissingAsXml() throws Exception {
		MockHttpServletResponse response = get(
		    "Observation/$lastn-encounters?subject=" + OBS_PATIENT_UUID + "&category=laboratory&code=5089")
		            .accept(FhirMediaTypes.XML).go();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), is(FhirMediaTypes.XML.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		Bundle results = readBundleResponse(response);
		
		assertThat(results, notNullValue());
		assertThat(results.getType(), equalTo(Bundle.BundleType.COLLECTION));
		assertThat(results.hasEntry(), is(true));
		assertThat(results, hasProperty("total", equalTo(1)));
		
		List<Bundle.BundleEntryComponent> entries = results.getEntry();
		
		assertThat(entries, everyItem(hasProperty("fullUrl", startsWith("http://localhost/ws/fhir2/R4/Observation/"))));
		assertThat(entries, everyItem(hasResource(instanceOf(Observation.class))));
		assertThat(entries, everyItem(hasResource(validResource())));
		assertThat(entries,
		    everyItem(hasResource(hasProperty("subject", hasProperty("reference", endsWith(OBS_PATIENT_UUID))))));
		assertThat(entries, everyItem(
		    hasResource(hasProperty("code", hasProperty("coding", everyItem(hasProperty("display", equalTo("Weight"))))))));
		assertThat(entries, everyItem(hasResource(hasProperty("category",
		    everyItem(hasProperty("coding", everyItem(hasProperty("code", equalTo("laboratory")))))))));
		assertThat(getDistinctEncounterDatetime(entries), lessThanOrEqualTo(1));
	}
	
	@Test
	public void shouldReturnLastnEncountersObservationsWhenPatientReferenceIsPassedInPatientParameterAsXml()
	        throws Exception {
		MockHttpServletResponse response = get(
		    "Observation/$lastn-encounters?max=2&patient=" + OBS_PATIENT_UUID + "&category=laboratory&code=5089")
		            .accept(FhirMediaTypes.XML).go();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), is(FhirMediaTypes.XML.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		Bundle results = readBundleResponse(response);
		
		assertThat(results, notNullValue());
		assertThat(results.getType(), equalTo(Bundle.BundleType.COLLECTION));
		assertThat(results.hasEntry(), is(true));
		assertThat(results, hasProperty("total", equalTo(2)));
		
		List<Bundle.BundleEntryComponent> entries = results.getEntry();
		
		assertThat(entries, everyItem(hasProperty("fullUrl", startsWith("http://localhost/ws/fhir2/R4/Observation/"))));
		assertThat(entries, everyItem(hasResource(instanceOf(Observation.class))));
		assertThat(entries, everyItem(hasResource(validResource())));
		assertThat(entries,
		    everyItem(hasResource(hasProperty("subject", hasProperty("reference", endsWith(OBS_PATIENT_UUID))))));
		assertThat(entries, everyItem(
		    hasResource(hasProperty("code", hasProperty("coding", everyItem(hasProperty("display", equalTo("Weight"))))))));
		assertThat(entries, everyItem(hasResource(hasProperty("category",
		    everyItem(hasProperty("coding", everyItem(hasProperty("code", equalTo("laboratory")))))))));
		assertThat(getDistinctEncounterDatetime(entries), lessThanOrEqualTo(2));
	}
	
	@Test
	public void shouldReturnLastnEncountersObservationsWhenPatientReferenceIsMissingAsXml() throws Exception {
		MockHttpServletResponse response = get("Observation/$lastn-encounters?max=2&category=laboratory&code=5089")
		        .accept(FhirMediaTypes.XML).go();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), is(FhirMediaTypes.XML.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		Bundle results = readBundleResponse(response);
		
		assertThat(results, notNullValue());
		assertThat(results.getType(), equalTo(Bundle.BundleType.COLLECTION));
		assertThat(results.hasEntry(), is(true));
		assertThat(results, hasProperty("total", equalTo(2)));
		
		List<Bundle.BundleEntryComponent> entries = results.getEntry();
		
		assertThat(entries, everyItem(hasProperty("fullUrl", startsWith("http://localhost/ws/fhir2/R4/Observation/"))));
		assertThat(entries, everyItem(hasResource(instanceOf(Observation.class))));
		assertThat(entries, everyItem(hasResource(validResource())));
		assertThat(entries, everyItem(
		    hasResource(hasProperty("code", hasProperty("coding", everyItem(hasProperty("display", equalTo("Weight"))))))));
		assertThat(entries, everyItem(hasResource(hasProperty("category",
		    everyItem(hasProperty("coding", everyItem(hasProperty("code", equalTo("laboratory")))))))));
		assertThat(getDistinctEncounterDatetime(entries), lessThanOrEqualTo(2));
	}
	
	@Test
	public void shouldReturnLastnEncountersObservationsWhenCategoryIsMissingAsXml() throws Exception {
		MockHttpServletResponse response = get(
		    "Observation/$lastn-encounters?max=2&patient=" + OBS_PATIENT_UUID + "&code=5089").accept(FhirMediaTypes.XML)
		            .go();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), is(FhirMediaTypes.XML.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		Bundle results = readBundleResponse(response);
		
		assertThat(results, notNullValue());
		assertThat(results.getType(), equalTo(Bundle.BundleType.COLLECTION));
		assertThat(results.hasEntry(), is(true));
		assertThat(results, hasProperty("total", equalTo(2)));
		
		List<Bundle.BundleEntryComponent> entries = results.getEntry();
		
		assertThat(entries, everyItem(hasProperty("fullUrl", startsWith("http://localhost/ws/fhir2/R4/Observation/"))));
		assertThat(entries, everyItem(hasResource(instanceOf(Observation.class))));
		assertThat(entries, everyItem(hasResource(validResource())));
		assertThat(entries,
		    everyItem(hasResource(hasProperty("subject", hasProperty("reference", endsWith(OBS_PATIENT_UUID))))));
		assertThat(entries, everyItem(
		    hasResource(hasProperty("code", hasProperty("coding", everyItem(hasProperty("display", equalTo("Weight"))))))));
		assertThat(getDistinctEncounterDatetime(entries), lessThanOrEqualTo(2));
	}
	
	@Test
	public void shouldReturnLastnEncountersObservationsWhenCodeIsMissingAsXml() throws Exception {
		MockHttpServletResponse response = get(
		    "Observation/$lastn-encounters?max=2&subject=" + OBS_PATIENT_UUID + "&category=laboratory")
		            .accept(FhirMediaTypes.XML).go();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), is(FhirMediaTypes.XML.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		Bundle results = readBundleResponse(response);
		
		assertThat(results, notNullValue());
		assertThat(results.getType(), equalTo(Bundle.BundleType.COLLECTION));
		assertThat(results.hasEntry(), is(true));
		assertThat(results, hasProperty("total", equalTo(3)));
		
		List<Bundle.BundleEntryComponent> entries = results.getEntry();
		
		assertThat(entries, everyItem(hasProperty("fullUrl", startsWith("http://localhost/ws/fhir2/R4/Observation/"))));
		assertThat(entries, everyItem(hasResource(instanceOf(Observation.class))));
		assertThat(entries, everyItem(hasResource(validResource())));
		assertThat(entries,
		    everyItem(hasResource(hasProperty("subject", hasProperty("reference", endsWith(OBS_PATIENT_UUID))))));
		assertThat(entries, everyItem(hasResource(hasProperty("category",
		    everyItem(hasProperty("coding", everyItem(hasProperty("code", equalTo("laboratory")))))))));
		assertThat(getDistinctEncounterDatetime(entries), lessThanOrEqualTo(2));
	}
	
	@Test
	public void shouldReturnLastnEncountersObservationsWhenNoParamterIsGivenAsXml() throws Exception {
		MockHttpServletResponse response = get("Observation/$lastn-encounters?").accept(FhirMediaTypes.XML).go();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), is(FhirMediaTypes.XML.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		Bundle results = readBundleResponse(response);
		
		assertThat(results, notNullValue());
		assertThat(results.getType(), equalTo(Bundle.BundleType.COLLECTION));
		assertThat(results.hasEntry(), is(true));
		assertThat(results, hasProperty("total", equalTo(1)));
		
		List<Bundle.BundleEntryComponent> entries = results.getEntry();
		
		assertThat(entries, everyItem(hasProperty("fullUrl", startsWith("http://localhost/ws/fhir2/R4/Observation/"))));
		assertThat(entries, everyItem(hasResource(instanceOf(Observation.class))));
		assertThat(entries, everyItem(hasResource(validResource())));
		assertThat(getDistinctEncounterDatetime(entries), lessThanOrEqualTo(1));
	}
	
	private int getDistinctEncounterDatetime(List<Bundle.BundleEntryComponent> resultList) {
		List<Date> results = resultList.stream().map(Bundle.BundleEntryComponent::getResource)
		        .filter(it -> it instanceof Observation).map(result -> encounterDao
		                .get(((Observation) result).getEncounter().getReferenceElement().getIdPart()).getEncounterDatetime())
		        .sorted(Comparator.reverseOrder()).collect(Collectors.toList());
		
		int distinctEncounterDatetime = 0;
		for (int var = 0; var < results.size(); var++) {
			Date currentDatetime = results.get(var);
			distinctEncounterDatetime++;
			
			if (var == results.size() - 1) {
				return distinctEncounterDatetime;
			}
			
			Date nextDatetime = results.get(var + 1);
			
			while (nextDatetime.equals(currentDatetime)) {
				var++;
				
				if (var + 1 == results.size()) {
					return distinctEncounterDatetime;
				}
				nextDatetime = results.get(var + 1);
			}
		}
		
		return distinctEncounterDatetime;
	}
}
