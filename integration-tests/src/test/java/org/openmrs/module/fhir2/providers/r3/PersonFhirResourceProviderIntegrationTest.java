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
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.equalToIgnoringCase;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.startsWith;
import static org.openmrs.module.fhir2.api.util.GeneralUtils.inputStreamToString;

import java.io.InputStream;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import lombok.AccessLevel;
import lombok.Getter;
import org.apache.commons.lang3.time.DateUtils;
import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.dstu3.model.Enumerations;
import org.hl7.fhir.dstu3.model.Extension;
import org.hl7.fhir.dstu3.model.OperationOutcome;
import org.hl7.fhir.dstu3.model.Person;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.module.fhir2.FhirConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletResponse;

public class PersonFhirResourceProviderIntegrationTest extends BaseFhirR3IntegrationTest<PersonFhirResourceProvider, Person> {
	
	private static final String PERSON_SEARCH_DATA_FILES = "org/openmrs/module/fhir2/api/dao/impl/FhirPersonDaoImplTest_initial_data.xml";
	
	private static final String JSON_CREATE_PERSON = "org/openmrs/module/fhir2/providers/PersonWebTest_create.json";
	
	private static final String XML_CREATE_PERSON = "org/openmrs/module/fhir2/providers/PersonWebTest_create.xml";
	
	private static final String PERSON_UUID = "5c521595-4e12-46b0-8248-b8f2d3697766";
	
	private static final String WRONG_PERSON_UUID = "f090747b-459b-4a13-8c1b-c0567d8aeb63";
	
	@Getter(AccessLevel.PUBLIC)
	@Autowired
	private PersonFhirResourceProvider resourceProvider;
	
	@Before
	@Override
	public void setup() throws Exception {
		super.setup();
		executeDataSet(PERSON_SEARCH_DATA_FILES);
	}
	
	@Test
	public void shouldReturnExistingPersonAsJson() throws Exception {
		MockHttpServletResponse response = get("/Person/" + PERSON_UUID).accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), is(FhirMediaTypes.JSON.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		Person person = readResponse(response);
		
		assertThat(person, notNullValue());
		assertThat(person.getIdElement().getIdPart(), equalTo(PERSON_UUID));
		assertThat(person, validResource());
	}
	
	@Test
	public void shouldThrow404ForNonExistingPersonAsJson() throws Exception {
		MockHttpServletResponse response = get("/Person/" + WRONG_PERSON_UUID).accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isNotFound());
		assertThat(response.getContentType(), is(FhirMediaTypes.JSON.toString()));
		assertThat(response.getContentAsString(), notNullValue());
	}
	
	@Test
	public void shouldReturnExistingPersonAsXML() throws Exception {
		MockHttpServletResponse response = get("/Person/" + PERSON_UUID).accept(FhirMediaTypes.XML).go();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), is(FhirMediaTypes.XML.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		Person person = readResponse(response);
		
		assertThat(person, notNullValue());
		assertThat(person.getIdElement().getIdPart(), equalTo(PERSON_UUID));
		assertThat(person, validResource());
	}
	
	@Test
	public void shouldThrow404ForNonExistingPersonAsXml() throws Exception {
		MockHttpServletResponse response = get("/Person/" + WRONG_PERSON_UUID).accept(FhirMediaTypes.XML).go();
		
		assertThat(response, isNotFound());
		assertThat(response.getContentType(), is(FhirMediaTypes.XML.toString()));
		assertThat(response.getContentAsString(), notNullValue());
	}
	
	@Test
	public void shouldReturnPersonAttributesAsExtensions() throws Exception {
		MockHttpServletResponse response = get("/Person/" + PERSON_UUID).accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), is(FhirMediaTypes.JSON.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		Person person = readResponse(response);
		
		assertThat(person.hasExtension(), is(true));
		assertThat(person, hasProperty("extension"));
		
		Extension personAttributeExtension = person.getExtension().get(0);
		
		assertThat(personAttributeExtension, hasProperty("url"));
		assertThat(personAttributeExtension.getUrl(), equalTo(FhirConstants.OPENMRS_FHIR_EXT_PERSON_ATTRIBUTE));
		assertThat(personAttributeExtension, hasProperty("extension"));
		assertThat(personAttributeExtension.getExtension(), hasSize(2));
		assertThat(personAttributeExtension.getExtensionsByUrl(FhirConstants.OPENMRS_FHIR_EXT_PERSON_ATTRIBUTE_TYPE),
		    notNullValue());
		assertThat(personAttributeExtension.getExtensionsByUrl(FhirConstants.OPENMRS_FHIR_EXT_PERSON_ATTRIBUTE_VALUE),
		    notNullValue());
		
		//Filtering for extensions of PersonAttributes
		List<Extension> personAttributeExtensions = person.getExtension().stream()
		        .filter(ext -> ext.getUrl().equals(FhirConstants.OPENMRS_FHIR_EXT_PERSON_ATTRIBUTE))
		        .collect(Collectors.toList());
		
		assertThat(personAttributeExtensions, hasSize(4));
	}
	
	@Test
	public void shouldCreateNewPersonAsJson() throws Exception {
		// read JSON record
		String jsonPerson;
		try (InputStream is = this.getClass().getClassLoader().getResourceAsStream(JSON_CREATE_PERSON)) {
			Objects.requireNonNull(is);
			jsonPerson = inputStreamToString(is, UTF_8);
		}
		
		// create person
		MockHttpServletResponse response = post("/Person").accept(FhirMediaTypes.JSON).jsonContent(jsonPerson).go();
		
		// verify created correctly
		assertThat(response, isCreated());
		assertThat(response.getHeader("Location"), containsString("/Person/"));
		assertThat(response.getContentType(), is(FhirMediaTypes.JSON.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		Person person = readResponse(response);
		
		assertThat(person, notNullValue());
		assertThat(person.getIdElement().getIdPart(), notNullValue());
		assertThat(person.getName().get(0).getGiven().get(0).toString(), equalToIgnoringCase("Adam"));
		assertThat(person.getName().get(0).getFamily(), equalToIgnoringCase("John"));
		assertThat(person.getGender(), equalTo(Enumerations.AdministrativeGender.MALE));
		
		Date birthDate = Date.from(LocalDate.of(2004, 8, 12).atStartOfDay(ZoneId.systemDefault()).toInstant());
		assertThat(person.getBirthDate(), equalTo(birthDate));
		
		assertThat(person.getAddress().get(0).getCity(), equalTo("Kampala"));
		assertThat(person.getAddress().get(0).getState(), equalTo("Mukono"));
		assertThat(person.getAddress().get(0).getCountry(), equalTo("Uganda"));
		assertThat(person, validResource());
		
		// try to get new person
		response = get("/Person/" + person.getIdElement().getIdPart()).accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isOk());
		
		Person newPerson = readResponse(response);
		
		assertThat(newPerson.getId(), equalTo(person.getId()));
	}
	
	@Test
	public void shouldCreateNewPersonAsXML() throws Exception {
		// read XML record
		String xmlPerson;
		try (InputStream is = this.getClass().getClassLoader().getResourceAsStream(XML_CREATE_PERSON)) {
			Objects.requireNonNull(is);
			xmlPerson = inputStreamToString(is, UTF_8);
		}
		
		// create person
		MockHttpServletResponse response = post("/Person").accept(FhirMediaTypes.XML).xmlContent(xmlPerson).go();
		
		// verify created correctly
		assertThat(response, isCreated());
		assertThat(response.getHeader("Location"), containsString("/Person/"));
		assertThat(response.getContentType(), is(FhirMediaTypes.XML.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		Person person = readResponse(response);
		
		assertThat(person, notNullValue());
		assertThat(person.getIdElement().getIdPart(), notNullValue());
		assertThat(person.getName().get(0).getGiven().get(0).toString(), equalToIgnoringCase("Adam"));
		assertThat(person.getName().get(0).getFamily(), equalToIgnoringCase("John"));
		assertThat(person.getGender(), equalTo(Enumerations.AdministrativeGender.MALE));
		
		Date birthDate = Date.from(LocalDate.of(2004, 8, 12).atStartOfDay(ZoneId.systemDefault()).toInstant());
		assertThat(person.getBirthDate(), equalTo(birthDate));
		
		assertThat(person.getAddress().get(0).getCity(), equalTo("Kampala"));
		assertThat(person.getAddress().get(0).getState(), equalTo("Mukono"));
		assertThat(person.getAddress().get(0).getCountry(), equalTo("Uganda"));
		assertThat(person, validResource());
		
		// try to get new person
		response = get("/Person/" + person.getIdElement().getIdPart()).accept(FhirMediaTypes.XML).go();
		
		assertThat(response, isOk());
		
		Person newPerson = readResponse(response);
		
		assertThat(newPerson.getId(), equalTo(person.getId()));
	}
	
	@Test
	public void shouldUpdateExistingPersonAsJson() throws Exception {
		// get the existing record
		MockHttpServletResponse response = get("/Person/" + PERSON_UUID).accept(FhirMediaTypes.JSON).go();
		Person person = readResponse(response);
		
		// update the existing record
		Date birthDate = DateUtils.truncate(new Date(), Calendar.DATE);
		person.setBirthDate(birthDate);
		
		// send the update to the server
		response = put("/Person/" + PERSON_UUID).jsonContent(toJson(person)).accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), is(FhirMediaTypes.JSON.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		// read the updated record
		Person updatedPerson = readResponse(response);
		
		assertThat(updatedPerson, notNullValue());
		assertThat(updatedPerson.getIdElement().getIdPart(), equalTo(PERSON_UUID));
		assertThat(updatedPerson.getBirthDate(), equalTo(birthDate));
		assertThat(person, validResource());
		
		// double-check the record returned via get
		response = get("/Person/" + PERSON_UUID).accept(FhirMediaTypes.JSON).go();
		Person reReadPerson = readResponse(response);
		
		assertThat(reReadPerson.getBirthDate(), equalTo(birthDate));
	}
	
	@Test
	public void shouldReturnBadRequestWhenDocumentIdDoesNotMatchPersonIdAsJson() throws Exception {
		// get the existing record
		MockHttpServletResponse response = get("/Person/" + PERSON_UUID).accept(FhirMediaTypes.JSON).go();
		Person person = readResponse(response);
		
		// update the existing record
		person.setId(WRONG_PERSON_UUID);
		
		// send the update to the server
		response = put("/Person/" + PERSON_UUID).jsonContent(toJson(person)).accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isBadRequest());
		assertThat(response.getContentType(), is(FhirMediaTypes.JSON.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		OperationOutcome operationOutcome = readOperationOutcome(response);
		
		assertThat(operationOutcome, notNullValue());
		assertThat(operationOutcome.hasIssue(), is(true));
	}
	
	@Test
	public void shouldReturnNotFoundWhenUpdatingNonExistentPersonAsJson() throws Exception {
		// get the existing record
		MockHttpServletResponse response = get("/Person/" + PERSON_UUID).accept(FhirMediaTypes.JSON).go();
		Person person = readResponse(response);
		
		// update the existing record
		person.setId(WRONG_PERSON_UUID);
		
		// send the update to the server
		response = put("/Person/" + WRONG_PERSON_UUID).jsonContent(toJson(person)).accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isNotFound());
		assertThat(response.getContentType(), is(FhirMediaTypes.JSON.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		OperationOutcome operationOutcome = readOperationOutcome(response);
		
		assertThat(operationOutcome, notNullValue());
		assertThat(operationOutcome.hasIssue(), is(true));
	}
	
	@Test
	public void shouldUpdateExistingPersonAsXML() throws Exception {
		// get the existing record
		MockHttpServletResponse response = get("/Person/" + PERSON_UUID).accept(FhirMediaTypes.XML).go();
		Person person = readResponse(response);
		
		// update the existing record
		Date birthDate = DateUtils.truncate(new Date(), Calendar.DATE);
		person.setBirthDate(birthDate);
		
		// send the update to the server
		response = put("/Person/" + PERSON_UUID).xmlContent(toXML(person)).accept(FhirMediaTypes.XML).go();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), is(FhirMediaTypes.XML.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		// read the updated record
		Person updatedPerson = readResponse(response);
		
		assertThat(updatedPerson, notNullValue());
		assertThat(updatedPerson.getIdElement().getIdPart(), equalTo(PERSON_UUID));
		assertThat(updatedPerson.getBirthDate(), equalTo(birthDate));
		assertThat(person, validResource());
		
		// double-check the record returned via get
		response = get("/Person/" + PERSON_UUID).accept(FhirMediaTypes.XML).go();
		Person reReadPerson = readResponse(response);
		
		assertThat(reReadPerson.getBirthDate(), equalTo(birthDate));
	}
	
	@Test
	public void shouldReturnBadRequestWhenDocumentIdDoesNotMatchPersonIdAsXML() throws Exception {
		// get the existing record
		MockHttpServletResponse response = get("/Person/" + PERSON_UUID).accept(FhirMediaTypes.XML).go();
		Person person = readResponse(response);
		
		// update the existing record
		person.setId(WRONG_PERSON_UUID);
		
		// send the update to the server
		response = put("/Person/" + PERSON_UUID).xmlContent(toXML(person)).accept(FhirMediaTypes.XML).go();
		
		assertThat(response, isBadRequest());
		assertThat(response.getContentType(), is(FhirMediaTypes.XML.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		OperationOutcome operationOutcome = readOperationOutcome(response);
		
		assertThat(operationOutcome, notNullValue());
		assertThat(operationOutcome.hasIssue(), is(true));
	}
	
	@Test
	public void shouldDeleteExistingPerson() throws Exception {
		MockHttpServletResponse response = delete("/Person/" + PERSON_UUID).accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isOk());
		
		response = get("/Person/" + PERSON_UUID).accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, statusEquals(HttpStatus.GONE));
	}
	
	@Test
	public void shouldReturnNotFoundWhenDeletingNonExistentPerson() throws Exception {
		MockHttpServletResponse response = delete("/Person/" + WRONG_PERSON_UUID).accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isNotFound());
		assertThat(response.getContentType(), is(FhirMediaTypes.JSON.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		OperationOutcome operationOutcome = readOperationOutcome(response);
		
		assertThat(operationOutcome, notNullValue());
		assertThat(operationOutcome.hasIssue(), is(true));
	}
	
	@Test
	public void shouldSearchForAllPersonsAsJson() throws Exception {
		MockHttpServletResponse response = get("/Person").accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), is(FhirMediaTypes.JSON.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		Bundle results = readBundleResponse(response);
		
		assertThat(results, notNullValue());
		assertThat(results.getType(), equalTo(Bundle.BundleType.SEARCHSET));
		assertThat(results.hasEntry(), is(true));
		
		List<Bundle.BundleEntryComponent> entries = results.getEntry();
		
		assertThat(entries, everyItem(hasProperty("fullUrl", startsWith("http://localhost/ws/fhir2/R3/Person/"))));
		assertThat(entries, everyItem(hasResource(instanceOf(Person.class))));
		assertThat(entries, everyItem(hasResource(validResource())));
	}
	
	@Test
	public void shouldReturnSortedAndFilteredSearchResultsForPersonsAsJson() throws Exception {
		MockHttpServletResponse response = get("/Person?name=voided&_sort=given").accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), is(FhirMediaTypes.JSON.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		Bundle results = readBundleResponse(response);
		
		assertThat(results, notNullValue());
		assertThat(results.getType(), equalTo(Bundle.BundleType.SEARCHSET));
		assertThat(results.hasEntry(), is(true));
		
		List<Bundle.BundleEntryComponent> entries = results.getEntry();
		
		assertThat(entries,
		    everyItem(hasResource(hasProperty("nameFirstRep", hasProperty("family", startsWith("voided"))))));
		assertThat(entries, containsInRelativeOrder(
		    hasResource(hasProperty("nameFirstRep", hasProperty("givenAsSingleString", containsString("I"))))));
		assertThat(entries, everyItem(hasResource(validResource())));
	}
	
	@Test
	public void shouldSearchForAllPersonsAsXML() throws Exception {
		MockHttpServletResponse response = get("/Person").accept(FhirMediaTypes.XML).go();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), is(FhirMediaTypes.XML.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		Bundle results = readBundleResponse(response);
		
		assertThat(results, notNullValue());
		assertThat(results.getType(), equalTo(Bundle.BundleType.SEARCHSET));
		assertThat(results.hasEntry(), is(true));
		
		List<Bundle.BundleEntryComponent> entries = results.getEntry();
		
		assertThat(entries, everyItem(hasProperty("fullUrl", startsWith("http://localhost/ws/fhir2/R3/Person/"))));
		assertThat(entries, everyItem(hasResource(instanceOf(Person.class))));
		assertThat(entries, everyItem(hasResource(validResource())));
	}
	
	@Test
	public void shouldReturnSortedAndFilteredSearchResultsForPersonsAsXML() throws Exception {
		MockHttpServletResponse response = get("/Person?name=voided&_sort=given").accept(FhirMediaTypes.XML).go();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), is(FhirMediaTypes.XML.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		Bundle results = readBundleResponse(response);
		
		assertThat(results, notNullValue());
		assertThat(results.getType(), equalTo(Bundle.BundleType.SEARCHSET));
		assertThat(results.hasEntry(), is(true));
		
		List<Bundle.BundleEntryComponent> entries = results.getEntry();
		
		assertThat(entries,
		    everyItem(hasResource(hasProperty("nameFirstRep", hasProperty("family", startsWith("voided"))))));
		assertThat(entries, containsInRelativeOrder(
		    hasResource(hasProperty("nameFirstRep", hasProperty("givenAsSingleString", containsString("I"))))));
		assertThat(entries, everyItem(hasResource(validResource())));
	}
	
	@Test
	public void shouldReturnCountForPersonAsJson() throws Exception {
		MockHttpServletResponse response = get("/Person?name=voided&_summary=count").accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), is(FhirMediaTypes.JSON.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		Bundle result = readBundleResponse(response);
		
		assertThat(result, notNullValue());
		assertThat(result.getType(), equalTo(Bundle.BundleType.SEARCHSET));
		assertThat(result, hasProperty("total", equalTo(1)));
	}
	
	@Test
	public void shouldReturnCountForPersonAsXml() throws Exception {
		MockHttpServletResponse response = get("/Person?name=voided&_summary=count").accept(FhirMediaTypes.XML).go();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), is(FhirMediaTypes.XML.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		Bundle result = readBundleResponse(response);
		
		assertThat(result, notNullValue());
		assertThat(result.getType(), equalTo(Bundle.BundleType.SEARCHSET));
		assertThat(result, hasProperty("total", equalTo(1)));
	}
	
	@Test
	public void shouldReturnAnEtagHeaderWhenRetrievingAnExistingPerson() throws Exception {
		MockHttpServletResponse response = get("/Person/" + PERSON_UUID).accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), is(FhirMediaTypes.JSON.toString()));
		
		assertThat(response.getHeader("etag"), notNullValue());
		assertThat(response.getHeader("etag"), startsWith("W/"));
		
		assertThat(response.getContentAsString(), notNullValue());
		
		Person person = readResponse(response);
		
		assertThat(person, notNullValue());
		assertThat(person.getMeta().getVersionId(), notNullValue());
		assertThat(person, validResource());
	}
	
	@Test
	public void shouldReturnNotModifiedWhenRetrievingAnExistingPersonWithAnEtag() throws Exception {
		MockHttpServletResponse response = get("/Person/" + PERSON_UUID).accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), is(FhirMediaTypes.JSON.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		assertThat(response.getHeader("etag"), notNullValue());
		
		String etagValue = response.getHeader("etag");
		
		response = get("/Person/" + PERSON_UUID).accept(FhirMediaTypes.JSON).ifNoneMatchHeader(etagValue).go();
		
		assertThat(response, isOk());
		assertThat(response, statusEquals(HttpStatus.NOT_MODIFIED));
	}
	
	@Test
	public void shouldReturnAnUpdatedPersonWithNewEtagWhenRetrievingAnExistingPersonWithAnEtag() throws Exception {
		MockHttpServletResponse response = get("/Person/" + PERSON_UUID).accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), is(FhirMediaTypes.JSON.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		assertThat(response.getHeader("etag"), notNullValue());
		
		String etagValue = response.getHeader("etag");
		
		Person person = readResponse(response);
		person.setGender(Enumerations.AdministrativeGender.FEMALE);
		
		//send update to the server
		put("/Person/" + PERSON_UUID).jsonContent(toJson(person)).accept(FhirMediaTypes.JSON).go();
		
		//send a new GET request, with the “If-None-Match” header specifying the ETag that we previously stored
		response = get("/Person/" + PERSON_UUID).accept(FhirMediaTypes.JSON).ifNoneMatchHeader(etagValue).go();
		
		assertThat(response, isOk());
		assertThat(response, statusEquals(HttpStatus.OK));
	}
}
