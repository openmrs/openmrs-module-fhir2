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

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInRelativeOrder;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.startsWith;
import static org.openmrs.module.fhir2.api.util.GeneralUtils.inputStreamToString;

import java.io.InputStream;
import java.sql.Date;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

import lombok.AccessLevel;
import lombok.Getter;
import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.dstu3.model.Condition;
import org.hl7.fhir.dstu3.model.OperationOutcome;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletResponse;

public class ConditionFhirResourceProviderIntegrationTest extends BaseFhirR3IntegrationTest<ConditionFhirResourceProvider, Condition> {
	
	private static final String CONDITION_DATA_SET_FILE = "org/openmrs/module/fhir2/api/dao/impl/FhirObsConditionDaoImplTest_initial_data.xml";
	
	private static final String JSON_CREATE_CONDITION_DOCUMENT = "org/openmrs/module/fhir2/providers/ConditionWebTest_create_r3.json";
	
	private static final String XML_CREATE_CONDITION_DOCUMENT = "org/openmrs/module/fhir2/providers/ConditionWebTest_create_r3.xml";
	
	private static final String CONDITION_UUID = "86sgf-1f7d-4394-a316-0a458edf28c4";
	
	private static final String WRONG_CONDITION_UUID = "950d965d-a935-429f-945f-75a502a90188";
	
	private static final String CONDITION_SUBJECT_UUID = "da7f524f-27ce-4bb2-86d6-6d1d05312bd5";
	
	private static final String EXISTING_OBS_CONDITION_SUBJECT_UUID = "5946f880-b197-400b-9caa-a3c661d23041";
	
	@Getter(AccessLevel.PUBLIC)
	@Autowired
	private ConditionFhirResourceProvider resourceProvider;
	
	@Before
	@Override
	public void setup() throws Exception {
		super.setup();
		executeDataSet(CONDITION_DATA_SET_FILE);
	}
	
	@Test
	public void shouldReturnConditionAsJson() throws Exception {
		MockHttpServletResponse response = get("/Condition/" + CONDITION_UUID).accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), is(FhirMediaTypes.JSON.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		Condition condition = readResponse(response);
		
		assertThat(condition, notNullValue());
		assertThat(condition.getIdElement().getIdPart(), equalTo(CONDITION_UUID));
		
		assertThat(condition.getOnsetDateTimeType().getValue(),
		    equalTo(Date.from(LocalDateTime.of(2008, 07, 1, 0, 0, 0).atZone(ZoneId.systemDefault()).toInstant())));
		
		assertThat(condition.hasSubject(), is(true));
		assertThat(condition.getSubject().getReference(), equalTo("Patient/" + EXISTING_OBS_CONDITION_SUBJECT_UUID));
		
		assertThat(condition, validResource());
	}
	
	@Test
	public void shouldReturnNotFoundWhenConditionNotFoundAsJson() throws Exception {
		MockHttpServletResponse response = get("/Condition/" + WRONG_CONDITION_UUID).accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isNotFound());
		assertThat(response.getContentType(), is(FhirMediaTypes.JSON.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		OperationOutcome operationOutcome = readOperationOutcome(response);
		
		assertThat(operationOutcome, notNullValue());
		assertThat(operationOutcome.hasIssue(), is(true));
	}
	
	@Test
	public void shouldReturnConditionAsXML() throws Exception {
		MockHttpServletResponse response = get("/Condition/" + CONDITION_UUID).accept(FhirMediaTypes.XML).go();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), is(FhirMediaTypes.XML.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		Condition condition = readResponse(response);
		
		assertThat(condition, notNullValue());
		assertThat(condition.getIdElement().getIdPart(), equalTo(CONDITION_UUID));
		
		assertThat(condition.getOnsetDateTimeType().getValue(),
		    equalTo(Date.from(LocalDateTime.of(2008, 07, 01, 0, 0, 0).atZone(ZoneId.systemDefault()).toInstant())));
		
		assertThat(condition.hasSubject(), is(true));
		assertThat(condition.getSubject().getReference(), equalTo("Patient/" + EXISTING_OBS_CONDITION_SUBJECT_UUID));
		
		assertThat(condition, validResource());
	}
	
	@Test
	public void shouldReturnNotFoundWhenConditionNotFoundAsXML() throws Exception {
		MockHttpServletResponse response = get("/Condition/" + WRONG_CONDITION_UUID).accept(FhirMediaTypes.XML).go();
		
		assertThat(response, isNotFound());
		assertThat(response.getContentType(), is(FhirMediaTypes.XML.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		OperationOutcome operationOutcome = readOperationOutcome(response);
		
		assertThat(operationOutcome, notNullValue());
		assertThat(operationOutcome.hasIssue(), is(true));
	}
	
	@Test
	public void shouldCreateNewConditionAsJson() throws Exception {
		String jsonCondition;
		try (InputStream is = this.getClass().getClassLoader().getResourceAsStream(JSON_CREATE_CONDITION_DOCUMENT)) {
			assertThat(is, notNullValue());
			jsonCondition = inputStreamToString(is, UTF_8);
			assertThat(jsonCondition, notNullValue());
		}
		
		MockHttpServletResponse response = post("/Condition").accept(FhirMediaTypes.JSON).jsonContent(jsonCondition).go();
		
		assertThat(response, isCreated());
		assertThat(response.getHeader("Location"), containsString("/Condition/"));
		assertThat(response.getContentType(), is(FhirMediaTypes.JSON.toString()));
		assertThat(response.getContentType(), notNullValue());
		
		Condition condition = readResponse(response);
		
		assertThat(condition, notNullValue());
		assertThat(condition.getIdElement().getIdPart(), notNullValue());
		assertThat(condition.getCode(), notNullValue());
		assertThat(condition.getCode().getCoding(),
		    hasItem(hasProperty("code", equalTo("116128AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA"))));
		assertThat(condition.getSubject(), notNullValue());
		assertThat(condition.getSubject().getReference(), endsWith(CONDITION_SUBJECT_UUID));
		
		assertThat(condition, validResource());
		
		response = get("/Condition/" + condition.getIdElement().getIdPart()).accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isOk());
		
		Condition newCondition = readResponse(response);
		
		assertThat(newCondition.getId(), equalTo(condition.getId()));
	}
	
	@Test
	public void shouldCreateNewConditionAsXML() throws Exception {
		String xmlCondition;
		try (InputStream is = this.getClass().getClassLoader().getResourceAsStream(XML_CREATE_CONDITION_DOCUMENT)) {
			assertThat(is, notNullValue());
			xmlCondition = inputStreamToString(is, UTF_8);
			assertThat(xmlCondition, notNullValue());
		}
		
		MockHttpServletResponse response = post("/Condition").accept(FhirMediaTypes.XML).xmlContent(xmlCondition).go();
		
		assertThat(response, isCreated());
		assertThat(response.getHeader("Location"), containsString("/Condition/"));
		assertThat(response.getContentType(), is(FhirMediaTypes.XML.toString()));
		assertThat(response.getContentType(), notNullValue());
		
		Condition condition = readResponse(response);
		
		assertThat(condition, notNullValue());
		assertThat(condition.getIdElement().getIdPart(), notNullValue());
		assertThat(condition.getOnsetDateTimeType(), notNullValue());
		assertThat(condition.getCode().getCoding(),
		    hasItem(hasProperty("code", equalTo("116128AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA"))));
		assertThat(condition.getSubject().getReference(), endsWith(CONDITION_SUBJECT_UUID));
		
		assertThat(condition, validResource());
		
		response = get("/Condition/" + condition.getIdElement().getIdPart()).accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isOk());
		
		Condition newCondition = readResponse(response);
		
		assertThat(newCondition.getId(), equalTo(condition.getId()));
	}
	
	@Test
	public void shouldReturnBadRequestWhenDocumentIdDoesNotMatchConditionIdAsJson() throws Exception {
		MockHttpServletResponse response = get("/Condition/" + CONDITION_UUID).accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), equalTo(FhirMediaTypes.JSON.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		Condition condition = readResponse(response);
		
		condition.setId(WRONG_CONDITION_UUID);
		
		response = put("/Condition/" + CONDITION_UUID).jsonContent(toJson(condition)).accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isBadRequest());
		assertThat(response.getContentType(), is(FhirMediaTypes.JSON.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		OperationOutcome operationOutcome = readOperationOutcome(response);
		
		assertThat(operationOutcome, notNullValue());
		assertThat(operationOutcome.hasIssue(), is(true));
	}
	
	@Test
	public void shouldReturnNotFoundWhenUpdatingNonExistentConditionAsJson() throws Exception {
		MockHttpServletResponse response = get("/Condition/" + CONDITION_UUID).accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), equalTo(FhirMediaTypes.JSON.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		Condition condition = readResponse(response);
		
		condition.setId(WRONG_CONDITION_UUID);
		
		response = put("/Condition/" + WRONG_CONDITION_UUID).jsonContent(toJson(condition)).accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isNotFound());
		assertThat(response.getContentType(), is(FhirMediaTypes.JSON.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		OperationOutcome operationOutcome = readOperationOutcome(response);
		
		assertThat(operationOutcome, notNullValue());
		assertThat(operationOutcome.hasIssue(), is(true));
	}
	
	@Test
	public void shouldReturnBadRequestWhenDocumentIdDoesNotMatchConditionIdAsXML() throws Exception {
		MockHttpServletResponse response = get("/Condition/" + CONDITION_UUID).accept(FhirMediaTypes.XML).go();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), equalTo(FhirMediaTypes.XML.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		Condition condition = readResponse(response);
		
		condition.setId(WRONG_CONDITION_UUID);
		
		response = put("/Condition/" + CONDITION_UUID).xmlContent(toXML(condition)).accept(FhirMediaTypes.XML).go();
		
		assertThat(response, isBadRequest());
		assertThat(response.getContentType(), is(FhirMediaTypes.XML.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		OperationOutcome operationOutcome = readOperationOutcome(response);
		
		assertThat(operationOutcome, notNullValue());
		assertThat(operationOutcome.hasIssue(), is(true));
	}
	
	@Test
	public void shouldReturnNotFoundWhenUpdatingNonExistentConditionAsXML() throws Exception {
		MockHttpServletResponse response = get("/Condition/" + CONDITION_UUID).accept(FhirMediaTypes.XML).go();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), equalTo(FhirMediaTypes.XML.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		Condition condition = readResponse(response);
		
		condition.setId(WRONG_CONDITION_UUID);
		
		response = put("/Condition/" + WRONG_CONDITION_UUID).xmlContent(toXML(condition)).accept(FhirMediaTypes.XML).go();
		
		assertThat(response, isNotFound());
		assertThat(response.getContentType(), is(FhirMediaTypes.XML.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		OperationOutcome operationOutcome = readOperationOutcome(response);
		
		assertThat(operationOutcome, notNullValue());
		assertThat(operationOutcome.hasIssue(), is(true));
	}
	
	@Test
	public void shouldDeleteExistingCondition() throws Exception {
		MockHttpServletResponse response = delete("/Condition/" + CONDITION_UUID).accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isOk());
		
		response = get("/Condition/" + CONDITION_UUID).accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, statusEquals(HttpStatus.GONE));
	}
	
	@Test
	public void shouldReturnNotFoundWhenDeletingNonExistentCondition() throws Exception {
		MockHttpServletResponse response = delete("/Condition/" + WRONG_CONDITION_UUID).accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isNotFound());
		assertThat(response.getContentType(), is(FhirMediaTypes.JSON.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		OperationOutcome operationOutcome = readOperationOutcome(response);
		
		assertThat(operationOutcome, notNullValue());
		assertThat(operationOutcome.hasIssue(), is(true));
	}
	
	@Test
	public void shouldSearchForAllConditionsAsJson() throws Exception {
		MockHttpServletResponse response = get("/Condition").accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), is(FhirMediaTypes.JSON.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		Bundle results = readBundleResponse(response);
		
		assertThat(results, notNullValue());
		assertThat(results.getType(), equalTo(Bundle.BundleType.SEARCHSET));
		assertThat(results.hasEntry(), is(true));
		
		List<Bundle.BundleEntryComponent> entries = results.getEntry();
		
		assertThat(entries, everyItem(hasProperty("fullUrl", startsWith("http://localhost/ws/fhir2/R3/Condition/"))));
		assertThat(entries, everyItem(hasResource(instanceOf(Condition.class))));
		assertThat(entries, everyItem(hasResource(validResource())));
		assertThat(entries.size(), equalTo(2));
	}
	
	@Test
	public void shouldReturnSortedAndFilteredSearchResultsForConditionsAsJson() throws Exception {
		MockHttpServletResponse response = get("/Condition?clinical-status=active&onset-date=2008&_sort=-onset-date")
		        .accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), is(FhirMediaTypes.JSON.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		Bundle results = readBundleResponse(response);
		
		assertThat(results, notNullValue());
		assertThat(results.getType(), equalTo(Bundle.BundleType.SEARCHSET));
		assertThat(results.hasEntry(), is(true));
		
		List<Bundle.BundleEntryComponent> entries = results.getEntry();
		
		assertThat(entries.size(), equalTo(2));
		assertThat(entries,
		    containsInRelativeOrder(
		        hasResource(hasProperty(
		            "onsetDateTimeType",
		            hasProperty(
		                "value",
		                equalTo(
		                    Date.from(LocalDateTime.of(2008, 7, 1, 0, 0, 0).atZone(ZoneId.systemDefault()).toInstant()))))),
		        hasResource(hasProperty("onsetDateTimeType", hasProperty("value", equalTo(
		            Date.from(LocalDateTime.of(2008, 7, 1, 0, 0, 0).atZone(ZoneId.systemDefault()).toInstant())))))));
	}
	
	@Test
	public void shouldSearchForAllConditionsAsXML() throws Exception {
		MockHttpServletResponse response = get("/Condition").accept(FhirMediaTypes.XML).go();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), is(FhirMediaTypes.XML.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		Bundle results = readBundleResponse(response);
		
		assertThat(results, notNullValue());
		assertThat(results.getType(), equalTo(Bundle.BundleType.SEARCHSET));
		assertThat(results.hasEntry(), is(true));
		
		List<Bundle.BundleEntryComponent> entries = results.getEntry();
		
		assertThat(entries, everyItem(hasProperty("fullUrl", startsWith("http://localhost/ws/fhir2/R3/Condition/"))));
		assertThat(entries, everyItem(hasResource(instanceOf(Condition.class))));
		assertThat(entries, everyItem(hasResource(validResource())));
		assertThat(entries.size(), equalTo(2));
	}
	
	@Test
	public void shouldReturnSortedAndFilteredSearchResultsForConditionsAsXML() throws Exception {
		MockHttpServletResponse response = get("/Condition?clinical-status=active&onset-date=2008&_sort=-onset-date")
		        .accept(FhirMediaTypes.XML).go();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), is(FhirMediaTypes.XML.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		Bundle results = readBundleResponse(response);
		
		assertThat(results, notNullValue());
		assertThat(results.getType(), equalTo(Bundle.BundleType.SEARCHSET));
		assertThat(results.hasEntry(), is(true));
		
		List<Bundle.BundleEntryComponent> entries = results.getEntry();
		
		assertThat(entries.size(), equalTo(2));
		assertThat(entries,
		    containsInRelativeOrder(
		        hasResource(hasProperty(
		            "onsetDateTimeType",
		            hasProperty(
		                "value",
		                equalTo(
		                    Date.from(LocalDateTime.of(2008, 7, 1, 0, 0, 0).atZone(ZoneId.systemDefault()).toInstant()))))),
		        hasResource(hasProperty("onsetDateTimeType", hasProperty("value", equalTo(
		            Date.from(LocalDateTime.of(2008, 7, 1, 0, 0, 0).atZone(ZoneId.systemDefault()).toInstant())))))));
	}
	
	@Test
	public void shouldReturnCountForConditionAsJson() throws Exception {
		MockHttpServletResponse response = get("/Condition?_summary=count").accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), is(FhirMediaTypes.JSON.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		Bundle result = readBundleResponse(response);
		
		assertThat(result, notNullValue());
		assertThat(result.getType(), equalTo(Bundle.BundleType.SEARCHSET));
		assertThat(result, hasProperty("total", equalTo(2)));
	}
	
	@Test
	public void shouldReturnCountForConditionAsXml() throws Exception {
		MockHttpServletResponse response = get("/Condition?_summary=count").accept(FhirMediaTypes.XML).go();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), is(FhirMediaTypes.XML.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		Bundle result = readBundleResponse(response);
		
		assertThat(result, notNullValue());
		assertThat(result.getType(), equalTo(Bundle.BundleType.SEARCHSET));
		assertThat(result, hasProperty("total", equalTo(2)));
	}
}
