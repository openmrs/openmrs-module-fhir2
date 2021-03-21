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
import static org.hamcrest.Matchers.containsInRelativeOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.startsWith;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;

import lombok.AccessLevel;
import lombok.Getter;
import org.apache.commons.io.IOUtils;
import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.dstu3.model.Location;
import org.hl7.fhir.dstu3.model.OperationOutcome;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletResponse;

public class LocationFhirResourceProviderIntegrationTest extends BaseFhirR3IntegrationTest<LocationFhirResourceProvider, Location> {
	
	private static final String LOCATION_INITIAL_DATA_XML = "org/openmrs/module/fhir2/api/dao/impl/FhirLocationDaoImplTest_initial_data.xml";
	
	private static final String LOCATION_UUID = "c0938432-1691-11df-97a5-7038c432";
	
	private static final String UNKNOWN_LOCATION_UUID = "8516d594-9c31-4bd3-bfec-b42b2f8a8444";
	
	private static final String PARENT_LOCATION_UUID = "76cd2d30-2411-44ef-84ea-8b7473256a6a";
	
	private static final String JSON_CREATE_LOCATION_DOCUMENT = "org/openmrs/module/fhir2/providers/LocationWebTest_create.json";
	
	private static final String XML_CREATE_LOCATION_DOCUMENT = "org/openmrs/module/fhir2/providers/LocationWebTest_create.xml";
	
	@Getter(AccessLevel.PUBLIC)
	@Autowired
	private LocationFhirResourceProvider resourceProvider;
	
	@Before
	@Override
	public void setup() throws Exception {
		super.setup();
		executeDataSet(LOCATION_INITIAL_DATA_XML);
	}
	
	@Test
	public void shouldReturnExistingLocationAsJson() throws Exception {
		MockHttpServletResponse response = get("/Location/" + LOCATION_UUID).accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), is(FhirMediaTypes.JSON.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		Location location = readResponse(response);
		
		assertThat(location, notNullValue());
		assertThat(location.getIdElement().getIdPart(), equalTo(LOCATION_UUID));
		assertThat(location, validResource());
	}
	
	@Test
	public void shouldThrow404ForNonExistingLocationAsJson() throws Exception {
		MockHttpServletResponse response = get("/Location/" + UNKNOWN_LOCATION_UUID).accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isNotFound());
		assertThat(response.getContentType(), is(FhirMediaTypes.JSON.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		OperationOutcome operationOutcome = readOperationOutcome(response);
		
		assertThat(operationOutcome, notNullValue());
		assertThat(operationOutcome.hasIssue(), is(true));
	}
	
	@Test
	public void shouldReturnExistingLocationAsXML() throws Exception {
		MockHttpServletResponse response = get("/Location/" + LOCATION_UUID).accept(FhirMediaTypes.XML).go();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), is(FhirMediaTypes.XML.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		Location location = readResponse(response);
		
		assertThat(location, notNullValue());
		assertThat(location.getIdElement().getIdPart(), equalTo(LOCATION_UUID));
		assertThat(location, validResource());
	}
	
	@Test
	public void shouldThrow404ForNonExistingLocationAsXML() throws Exception {
		MockHttpServletResponse response = get("/Location/" + UNKNOWN_LOCATION_UUID).accept(FhirMediaTypes.XML).go();
		
		assertThat(response, isNotFound());
		assertThat(response.getContentType(), is(FhirMediaTypes.XML.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		OperationOutcome operationOutcome = readOperationOutcome(response);
		
		assertThat(operationOutcome, notNullValue());
		assertThat(operationOutcome.hasIssue(), is(true));
	}
	
	@Test
	public void shouldCreateNewLocationAsJson() throws Exception {
		// read JSON record
		String jsonLocation;
		try (InputStream is = this.getClass().getClassLoader().getResourceAsStream(JSON_CREATE_LOCATION_DOCUMENT)) {
			Objects.requireNonNull(is);
			jsonLocation = IOUtils.toString(is, StandardCharsets.UTF_8);
		}
		
		// create location
		MockHttpServletResponse response = post("/Location").accept(FhirMediaTypes.JSON).jsonContent(jsonLocation).go();
		
		// verify created correctly
		assertThat(response, isCreated());
		assertThat(response.getContentType(), is(FhirMediaTypes.JSON.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		Location location = readResponse(response);
		
		assertThat(location, notNullValue());
		assertThat(location.getName(), equalTo("Test location"));
		assertThat(location.getAddress().getCity(), equalTo("kampala"));
		assertThat(location.getAddress().getCountry(), equalTo("uganda"));
		assertThat(location.getAddress().getState(), equalTo("MI"));
		assertThat(location.getAddress().getPostalCode(), equalTo("9105 PZ"));
		assertThat(location, validResource());
		
		// try to get new location
		response = get("/Location/" + location.getIdElement().getIdPart()).accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isOk());
		
		Location newLocation = readResponse(response);
		
		assertThat(newLocation.getId(), equalTo(location.getId()));
	}
	
	@Test
	public void shouldCreateNewLocationAsXML() throws Exception {
		// read XML record
		String xmlLocation;
		try (InputStream is = this.getClass().getClassLoader().getResourceAsStream(XML_CREATE_LOCATION_DOCUMENT)) {
			Objects.requireNonNull(is);
			xmlLocation = IOUtils.toString(is, StandardCharsets.UTF_8);
		}
		
		// create location
		MockHttpServletResponse response = post("/Location").accept(FhirMediaTypes.XML).xmlContext(xmlLocation).go();
		
		// verify created correctly
		assertThat(response, isCreated());
		assertThat(response.getContentType(), is(FhirMediaTypes.XML.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		Location location = readResponse(response);
		
		assertThat(location, notNullValue());
		assertThat(location.getName(), equalTo("Test location"));
		assertThat(location.getAddress().getCity(), equalTo("kampala"));
		assertThat(location.getAddress().getCountry(), equalTo("uganda"));
		assertThat(location.getAddress().getState(), equalTo("MI"));
		assertThat(location.getAddress().getPostalCode(), equalTo("9105 PZ"));
		assertThat(location, validResource());
		
		// try to get new location
		response = get("/Location/" + location.getIdElement().getIdPart()).accept(FhirMediaTypes.XML).go();
		
		assertThat(response, isOk());
		
		Location newLocation = readResponse(response);
		
		assertThat(newLocation.getId(), equalTo(location.getId()));
	}
	
	@Test
	public void shouldUpdateExistingLocationAsJson() throws Exception {
		// get the existing record
		MockHttpServletResponse response = get("/Location/" + LOCATION_UUID).accept(FhirMediaTypes.JSON).go();
		Location location = readResponse(response);
		
		// update the existing record
		location.getAddress().setCountry("France");
		
		// send the update to the server
		response = put("/Location/" + LOCATION_UUID).jsonContent(toJson(location)).accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), is(FhirMediaTypes.JSON.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		// read the updated record
		Location updatedLocation = readResponse(response);
		
		assertThat(updatedLocation, notNullValue());
		assertThat(updatedLocation.getIdElement().getIdPart(), equalTo(LOCATION_UUID));
		assertThat(updatedLocation.getAddress().getCountry(), equalTo("France"));
		assertThat(updatedLocation, validResource());
		
		// double-check the record returned via get
		response = get("/Location/" + LOCATION_UUID).accept(FhirMediaTypes.JSON).go();
		Location reReadLocation = readResponse(response);
		
		assertThat(reReadLocation.getAddress().getCountry(), equalTo("France"));
	}
	
	@Test
	public void shouldReturnBadRequestWhenDocumentIdDoesNotMatchLocationIdAsJson() throws Exception {
		// get the existing record
		MockHttpServletResponse response = get("/Location/" + LOCATION_UUID).accept(FhirMediaTypes.JSON).go();
		Location location = readResponse(response);
		
		// update the existing record
		location.setId(UNKNOWN_LOCATION_UUID);
		
		// send the update to the server
		response = put("/Location/" + LOCATION_UUID).jsonContent(toJson(location)).accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isBadRequest());
		assertThat(response.getContentType(), is(FhirMediaTypes.JSON.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		OperationOutcome operationOutcome = readOperationOutcome(response);
		
		assertThat(operationOutcome, notNullValue());
		assertThat(operationOutcome.hasIssue(), is(true));
	}
	
	@Test
	public void shouldReturnNotFoundWhenUpdatingNonExistentLocationAsJson() throws Exception {
		// get the existing record
		MockHttpServletResponse response = get("/Location/" + LOCATION_UUID).accept(FhirMediaTypes.JSON).go();
		Location location = readResponse(response);
		
		// update the existing record
		location.setId(UNKNOWN_LOCATION_UUID);
		
		// send the update to the server
		response = put("/Location/" + UNKNOWN_LOCATION_UUID).jsonContent(toJson(location)).accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isNotFound());
		assertThat(response.getContentType(), is(FhirMediaTypes.JSON.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		OperationOutcome operationOutcome = readOperationOutcome(response);
		
		assertThat(operationOutcome, notNullValue());
		assertThat(operationOutcome.hasIssue(), is(true));
	}
	
	@Test
	public void shouldUpdateExistingLocationAsXML() throws Exception {
		// get the existing record
		MockHttpServletResponse response = get("/Location/" + LOCATION_UUID).accept(FhirMediaTypes.XML).go();
		Location location = readResponse(response);
		
		// update the existing record
		location.getAddress().setCountry("France");
		
		// send the update to the server
		response = put("/Location/" + LOCATION_UUID).xmlContext(toXML(location)).accept(FhirMediaTypes.XML).go();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), is(FhirMediaTypes.XML.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		// read the updated record
		Location updatedLocation = readResponse(response);
		
		assertThat(updatedLocation, notNullValue());
		assertThat(updatedLocation.getIdElement().getIdPart(), equalTo(LOCATION_UUID));
		assertThat(updatedLocation.getAddress().getCountry(), equalTo("France"));
		assertThat(updatedLocation, validResource());
		
		// double-check the record returned via get
		response = get("/Location/" + LOCATION_UUID).accept(FhirMediaTypes.XML).go();
		Location reReadLocation = readResponse(response);
		
		assertThat(reReadLocation.getAddress().getCountry(), equalTo("France"));
	}
	
	@Test
	public void shouldReturnBadRequestWhenDocumentIdDoesNotMatchLocationIdAsXML() throws Exception {
		// get the existing record
		MockHttpServletResponse response = get("/Location/" + LOCATION_UUID).accept(FhirMediaTypes.XML).go();
		Location location = readResponse(response);
		
		// update the existing record
		location.setId(UNKNOWN_LOCATION_UUID);
		
		// send the update to the server
		response = put("/Location/" + LOCATION_UUID).xmlContext(toXML(location)).accept(FhirMediaTypes.XML).go();
		
		assertThat(response, isBadRequest());
		assertThat(response.getContentType(), is(FhirMediaTypes.XML.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		OperationOutcome operationOutcome = readOperationOutcome(response);
		
		assertThat(operationOutcome, notNullValue());
		assertThat(operationOutcome.hasIssue(), is(true));
	}
	
	@Test
	public void shouldReturnNotFoundWhenUpdatingNonExistentLocationAsXML() throws Exception {
		// get the existing record
		MockHttpServletResponse response = get("/Location/" + LOCATION_UUID).accept(FhirMediaTypes.XML).go();
		Location location = readResponse(response);
		
		// update the existing record
		location.setId(UNKNOWN_LOCATION_UUID);
		
		// send the update to the server
		response = put("/Location/" + UNKNOWN_LOCATION_UUID).xmlContext(toXML(location)).accept(FhirMediaTypes.XML).go();
		
		assertThat(response, isNotFound());
		assertThat(response.getContentType(), is(FhirMediaTypes.XML.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		OperationOutcome operationOutcome = readOperationOutcome(response);
		
		assertThat(operationOutcome, notNullValue());
		assertThat(operationOutcome.hasIssue(), is(true));
	}
	
	@Test
	public void shouldDeleteExistingLocationAsJson() throws Exception {
		MockHttpServletResponse response = delete("/Location/" + LOCATION_UUID).accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isOk());
		
		response = get("/Location/" + LOCATION_UUID).accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, statusEquals(HttpStatus.GONE));
	}
	
	@Test
	public void shouldReturnNotFoundWhenDeletingNonExistentLocationAsJson() throws Exception {
		MockHttpServletResponse response = delete("/Location/" + UNKNOWN_LOCATION_UUID).accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isNotFound());
		assertThat(response.getContentType(), is(FhirMediaTypes.JSON.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		OperationOutcome operationOutcome = readOperationOutcome(response);
		
		assertThat(operationOutcome, notNullValue());
		assertThat(operationOutcome.hasIssue(), is(true));
	}
	
	@Test
	public void shouldDeleteExistingLocationAsXML() throws Exception {
		MockHttpServletResponse response = delete("/Location/" + LOCATION_UUID).accept(FhirMediaTypes.XML).go();
		
		assertThat(response, isOk());
		
		response = get("/Location/" + LOCATION_UUID).accept(FhirMediaTypes.XML).go();
		
		assertThat(response, statusEquals(HttpStatus.GONE));
	}
	
	@Test
	public void shouldReturnNotFoundWhenDeletingNonExistentLocationAsXML() throws Exception {
		MockHttpServletResponse response = delete("/Location/" + UNKNOWN_LOCATION_UUID).accept(FhirMediaTypes.XML).go();
		
		assertThat(response, isNotFound());
		assertThat(response.getContentType(), is(FhirMediaTypes.XML.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		OperationOutcome operationOutcome = readOperationOutcome(response);
		
		assertThat(operationOutcome, notNullValue());
		assertThat(operationOutcome.hasIssue(), is(true));
	}
	
	@Test
	public void shouldSearchForExistingLocationsAsJson() throws Exception {
		MockHttpServletResponse response = get("/Location").accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), is(FhirMediaTypes.JSON.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		Bundle results = readBundleResponse(response);
		
		assertThat(results, notNullValue());
		assertThat(results.getType(), equalTo(Bundle.BundleType.SEARCHSET));
		assertThat(results.hasEntry(), is(true));
		
		List<Bundle.BundleEntryComponent> entries = results.getEntry();
		
		assertThat(entries, everyItem(hasProperty("fullUrl", startsWith("http://localhost/ws/fhir2/R3/Location/"))));
		assertThat(entries, everyItem(hasResource(instanceOf(Location.class))));
		assertThat(entries, everyItem(hasResource(validResource())));
		
		response = get("/Location?address-city=Kerio&partof=" + PARENT_LOCATION_UUID + "&_sort=name")
		        .accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), is(FhirMediaTypes.JSON.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		results = readBundleResponse(response);
		
		assertThat(results, notNullValue());
		assertThat(results.getType(), equalTo(Bundle.BundleType.SEARCHSET));
		assertThat(results.hasEntry(), is(true));
		
		entries = results.getEntry();
		
		assertThat(entries, everyItem(hasResource(hasProperty("address", hasProperty("city", equalTo("Kerio"))))));
		assertThat(entries, everyItem(hasResource(
		    hasProperty("partOf", hasProperty("referenceElement", hasProperty("idPart", equalTo(PARENT_LOCATION_UUID)))))));
		assertThat(entries, containsInRelativeOrder(hasResource(hasProperty("name", equalTo("Test location 6"))),
		    hasResource(hasProperty("name", equalTo("Test location 8")))));
		assertThat(entries, everyItem(hasResource(validResource())));
	}
	
	@Test
	public void shouldSearchForExistingLocationsAsXML() throws Exception {
		MockHttpServletResponse response = get("/Location").accept(FhirMediaTypes.XML).go();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), is(FhirMediaTypes.XML.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		Bundle results = readBundleResponse(response);
		
		assertThat(results, notNullValue());
		assertThat(results.getType(), equalTo(Bundle.BundleType.SEARCHSET));
		assertThat(results.hasEntry(), is(true));
		
		List<Bundle.BundleEntryComponent> entries = results.getEntry();
		
		assertThat(entries, everyItem(hasProperty("fullUrl", startsWith("http://localhost/ws/fhir2/R3/Location/"))));
		assertThat(entries, everyItem(hasResource(instanceOf(Location.class))));
		assertThat(entries, everyItem(hasResource(validResource())));
		
		response = get("/Location?address-city=Kerio&partof=" + PARENT_LOCATION_UUID + "&_sort=name")
		        .accept(FhirMediaTypes.XML).go();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), is(FhirMediaTypes.XML.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		results = readBundleResponse(response);
		
		assertThat(results, notNullValue());
		assertThat(results.getType(), equalTo(Bundle.BundleType.SEARCHSET));
		assertThat(results.hasEntry(), is(true));
		
		entries = results.getEntry();
		
		assertThat(entries, everyItem(hasResource(hasProperty("address", hasProperty("city", equalTo("Kerio"))))));
		assertThat(entries, everyItem(hasResource(
		    hasProperty("partOf", hasProperty("referenceElement", hasProperty("idPart", equalTo(PARENT_LOCATION_UUID)))))));
		assertThat(entries, containsInRelativeOrder(hasResource(hasProperty("name", equalTo("Test location 6"))),
		    hasResource(hasProperty("name", equalTo("Test location 8")))));
		assertThat(entries, everyItem(hasResource(validResource())));
	}
	
	@Test
	public void shouldReturnCountForLocationAsJson() throws Exception {
		MockHttpServletResponse response = get("/Location?_summary=count").accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), is(FhirMediaTypes.JSON.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		Bundle result = readBundleResponse(response);
		
		assertThat(result, notNullValue());
		assertThat(result.getType(), equalTo(Bundle.BundleType.SEARCHSET));
		
		assertThat(result, hasProperty("total", equalTo(8)));
	}
	
	@Test
	public void shouldReturnCountForLocationAsXml() throws Exception {
		MockHttpServletResponse response = get("/Location?_summary=count").accept(FhirMediaTypes.XML).go();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), is(FhirMediaTypes.XML.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		Bundle result = readBundleResponse(response);
		
		assertThat(result, notNullValue());
		assertThat(result.getType(), equalTo(Bundle.BundleType.SEARCHSET));
		
		assertThat(result, hasProperty("total", equalTo(8)));
	}
}
