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
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.startsWith;

import java.util.List;

import lombok.AccessLevel;
import lombok.Getter;
import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.dstu3.model.OperationOutcome;
import org.hl7.fhir.dstu3.model.ValueSet;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletResponse;

public class ValueSetFhirResourceProviderIntegrationTest extends BaseFhirR3IntegrationTest<ValueSetFhirResourceProvider, ValueSet> {
	
	private static final String VALUESET_DATA_XML = "org/openmrs/module/fhir2/api/dao/impl/FhirConceptDaoImplTest_initial_data.xml";
	
	private static final String ROOT_CONCEPT_UUID = "0f97e14e-cdc2-49ac-9255-b5126f8a5147";
	
	private static final String UNKNOWN_CONCEPT_UUID = "0f89e14e-cdc2-49ac-9255-b5126f8a5147";
	
	private static final String CONCEPT_UUID = "0cbe2ed3-cd5f-4f46-9459-26127c9265ab"; //CONCEPT WITH IsSet = false
	
	@Getter(AccessLevel.PUBLIC)
	@Autowired
	private ValueSetFhirResourceProvider resourceProvider;
	
	@Before
	@Override
	public void setup() throws Exception {
		super.setup();
		
		executeDataSet(VALUESET_DATA_XML);
	}
	
	@Test
	public void shouldReturnValueSetAsJson() throws Exception {
		MockHttpServletResponse response = get("/ValueSet/" + ROOT_CONCEPT_UUID).accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), startsWith(FhirMediaTypes.JSON.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		ValueSet valueSet = readResponse(response);
		
		assertThat(valueSet, notNullValue());
		assertThat(valueSet.getIdElement().getIdPart(), equalTo(ROOT_CONCEPT_UUID));
		assertThat(valueSet, validResource());
	}
	
	@Test
	public void shouldReturnNotFoundWhenValueSetNotFoundAsJsonGivenInvalidUUID() throws Exception {
		MockHttpServletResponse response = get("/ValueSet/" + UNKNOWN_CONCEPT_UUID).accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isNotFound());
		assertThat(response.getContentType(), startsWith(FhirMediaTypes.JSON.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		OperationOutcome operationOutcome = readOperationOutcome(response);
		
		assertThat(operationOutcome, notNullValue());
		assertThat(operationOutcome.hasIssue(), is(true));
	}
	
	@Test
	public void shouldReturnNotFoundWhenValueSetNotFoundAsJsonGivenConceptUUID() throws Exception {
		MockHttpServletResponse response = get("/ValueSet/" + CONCEPT_UUID).accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isNotFound());
		assertThat(response.getContentType(), startsWith(FhirMediaTypes.JSON.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		OperationOutcome operationOutcome = readOperationOutcome(response);
		
		assertThat(operationOutcome, notNullValue());
		assertThat(operationOutcome.hasIssue(), is(true));
	}
	
	@Test
	public void shouldReturnValueSetAsXML() throws Exception {
		MockHttpServletResponse response = get("/ValueSet/" + ROOT_CONCEPT_UUID).accept(FhirMediaTypes.XML).go();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), startsWith(FhirMediaTypes.XML.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		ValueSet valueSet = readResponse(response);
		
		assertThat(valueSet, notNullValue());
		assertThat(valueSet.getIdElement().getIdPart(), equalTo(ROOT_CONCEPT_UUID));
		assertThat(valueSet, validResource());
	}
	
	@Test
	public void shouldReturnNotFoundWhenValueSetNotFoundAsXMLGivenInvalidUUID() throws Exception {
		MockHttpServletResponse response = get("/ValueSet/" + UNKNOWN_CONCEPT_UUID).accept(FhirMediaTypes.XML).go();
		
		assertThat(response, isNotFound());
		assertThat(response.getContentType(), startsWith(FhirMediaTypes.XML.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		OperationOutcome operationOutcome = readOperationOutcome(response);
		
		assertThat(operationOutcome, notNullValue());
		assertThat(operationOutcome.hasIssue(), is(true));
	}
	
	@Test
	public void shouldReturnNotFoundWhenValueSetNotFoundAsXMLGivenConceptUUID() throws Exception {
		MockHttpServletResponse response = get("/ValueSet/" + UNKNOWN_CONCEPT_UUID).accept(FhirMediaTypes.XML).go();
		
		assertThat(response, isNotFound());
		assertThat(response.getContentType(), startsWith(FhirMediaTypes.XML.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		OperationOutcome operationOutcome = readOperationOutcome(response);
		
		assertThat(operationOutcome, notNullValue());
		assertThat(operationOutcome.hasIssue(), is(true));
	}
	
	@Test
	public void shouldSearchForValueSetsByTitleAsJSON() throws Exception {
		String uri = String.format("/ValueSet/?title=%s", "DEMO");
		MockHttpServletResponse response = get(uri).accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), startsWith(FhirMediaTypes.JSON.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		Bundle results = readBundleResponse(response);
		
		assertThat(results, notNullValue());
		assertThat(results.getType(), equalTo(Bundle.BundleType.SEARCHSET));
		assertThat(results.hasEntry(), is(true));
		
		List<Bundle.BundleEntryComponent> entries = results.getEntry();
		
		assertThat(entries.isEmpty(), not(true));
		assertThat(entries, everyItem(hasProperty("fullUrl", startsWith("http://localhost/ws/fhir2/R3/ValueSet"))));
		assertThat(entries, everyItem(hasResource(instanceOf(ValueSet.class))));
		assertThat(entries, everyItem(hasResource(validResource())));
		assertThat(entries, everyItem(hasResource(hasProperty("title", equalTo("DEMO")))));
	}
	
	@Test
	public void shouldSearchForValueSetsByTitleAsXml() throws Exception {
		String uri = String.format("/ValueSet/?title=%s", "DEMO");
		MockHttpServletResponse response = get(uri).accept(FhirMediaTypes.XML).go();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), startsWith(FhirMediaTypes.XML.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		Bundle results = readBundleResponse(response);
		
		assertThat(results, notNullValue());
		assertThat(results.getType(), equalTo(Bundle.BundleType.SEARCHSET));
		assertThat(results.hasEntry(), is(true));
		
		List<Bundle.BundleEntryComponent> entries = results.getEntry();
		
		assertThat(entries.isEmpty(), not(true));
		assertThat(entries, everyItem(hasProperty("fullUrl", startsWith("http://localhost/ws/fhir2/R3/ValueSet"))));
		assertThat(entries, everyItem(hasResource(instanceOf(ValueSet.class))));
		assertThat(entries, everyItem(hasResource(validResource())));
		assertThat(entries, everyItem(hasResource(hasProperty("title", equalTo("DEMO")))));
	}
	
	@Test
	public void shouldReturnAnEtagHeaderWhenRetrievingAnExistingValueSet() throws Exception {
		MockHttpServletResponse response = get("/ValueSet/" + ROOT_CONCEPT_UUID).accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), startsWith(FhirMediaTypes.JSON.toString()));
		
		assertThat(response.getHeader("etag"), notNullValue());
		assertThat(response.getHeader("etag"), startsWith("W/"));
		
		assertThat(response.getContentAsString(), notNullValue());
		
		ValueSet valueSet = readResponse(response);
		
		assertThat(valueSet, notNullValue());
		assertThat(valueSet.getMeta().getVersionId(), notNullValue());
		assertThat(valueSet, validResource());
	}
	
	@Test
	public void shouldReturnNotModifiedWhenRetrievingAnExistingValueSetWithAnEtag() throws Exception {
		MockHttpServletResponse response = get("/ValueSet/" + ROOT_CONCEPT_UUID).accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), startsWith(FhirMediaTypes.JSON.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		assertThat(response.getHeader("etag"), notNullValue());
		
		String etagValue = response.getHeader("etag");
		
		response = get("/ValueSet/" + ROOT_CONCEPT_UUID).accept(FhirMediaTypes.JSON).ifNoneMatchHeader(etagValue).go();
		
		assertThat(response, isOk());
		assertThat(response, statusEquals(HttpStatus.NOT_MODIFIED));
	}
	
}
