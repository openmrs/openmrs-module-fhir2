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
import static org.hamcrest.Matchers.containsInRelativeOrder;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.startsWith;

import java.util.List;

import lombok.AccessLevel;
import lombok.Getter;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.RelatedPerson;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletResponse;

public class RelatedPersonFhirResourceProviderIntegrationTest extends BaseFhirR4IntegrationTest<RelatedPersonFhirResourceProvider, RelatedPerson> {
	
	private static final String RELATIONSHIP_UUID = "c3c91630-8563-481b-8efa-48e10c139a3d";
	
	private static final String WRONG_RELATIONSHIP_UUID = "f4d45630-8563-481b-8efa-48e10c139a3d";
	
	private static final String RELATED_PERSON_DATA_FILES = "org/openmrs/module/fhir2/api/dao/impl/FhirRelatedPersonDaoImplTest_initial_data.xml";
	
	@Getter(AccessLevel.PUBLIC)
	@Autowired
	private RelatedPersonFhirResourceProvider resourceProvider;
	
	@Before
	@Override
	public void setup() throws Exception {
		super.setup();
		executeDataSet(RELATED_PERSON_DATA_FILES);
	}
	
	@Test
	public void shouldReturnRelatedPersonAsJson() throws Exception {
		MockHttpServletResponse response = get("/RelatedPerson/" + RELATIONSHIP_UUID).accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), is(FhirMediaTypes.JSON.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		RelatedPerson relatedPerson = readResponse(response);
		
		assertThat(relatedPerson, notNullValue());
		assertThat(relatedPerson.getIdElement().getIdPart(), equalTo(RELATIONSHIP_UUID));
	}
	
	@Test
	public void shouldThrow404ForNonExistingRelationshipAsJson() throws Exception {
		MockHttpServletResponse response = get("/RelatedPerson/" + WRONG_RELATIONSHIP_UUID).accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isNotFound());
		assertThat(response.getContentType(), is(FhirMediaTypes.JSON.toString()));
		assertThat(response.getContentAsString(), notNullValue());
	}
	
	@Test
	public void shouldReturnRelatedPersonAsXML() throws Exception {
		MockHttpServletResponse response = get("/RelatedPerson/" + RELATIONSHIP_UUID).accept(FhirMediaTypes.XML).go();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), is(FhirMediaTypes.XML.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		RelatedPerson relatedPerson = readResponse(response);
		
		assertThat(relatedPerson, notNullValue());
		assertThat(relatedPerson.getIdElement().getIdPart(), equalTo(RELATIONSHIP_UUID));
	}
	
	@Test
	public void shouldThrow404ForNonExistingRelationshipAsXML() throws Exception {
		MockHttpServletResponse response = get("/RelatedPerson/" + WRONG_RELATIONSHIP_UUID).accept(FhirMediaTypes.XML).go();
		
		assertThat(response, isNotFound());
		assertThat(response.getContentType(), is(FhirMediaTypes.XML.toString()));
		assertThat(response.getContentAsString(), notNullValue());
	}
	
	@Test
	public void shouldReturnForAllRelatedPersonAsJson() throws Exception {
		MockHttpServletResponse response = get("/RelatedPerson").accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), is(FhirMediaTypes.JSON.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		Bundle results = readBundleResponse(response);
		
		assertThat(results, notNullValue());
		assertThat(results.getType(), equalTo(Bundle.BundleType.SEARCHSET));
		assertThat(results.hasEntry(), is(true));
		
		List<Bundle.BundleEntryComponent> entries = results.getEntry();
		
		assertThat(entries, everyItem(hasProperty("fullUrl", startsWith("http://localhost/ws/fhir2/R4/RelatedPerson/"))));
		assertThat(entries, everyItem(hasResource(instanceOf(RelatedPerson.class))));
	}
	
	@Test
	public void shouldReturnSortedAndFilterSearchResultsForRelatedPersonAsJson() throws Exception {
		MockHttpServletResponse response = get("/RelatedPerson?name=John&_sort=-date").accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), is(FhirMediaTypes.JSON.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		Bundle results = readBundleResponse(response);
		
		assertThat(results, notNullValue());
		assertThat(results.getType(), equalTo(Bundle.BundleType.SEARCHSET));
		assertThat(results.hasEntry(), is(true));
		
		List<Bundle.BundleEntryComponent> entries = results.getEntry();
		
		assertThat(entries,
		    everyItem(hasResource(hasProperty("nameFirstRep", hasProperty("family", containsString("Doe"))))));
		assertThat(entries, containsInRelativeOrder(
		    hasResource(hasProperty("nameFirstRep", hasProperty("givenAsSingleString", containsString("F"))))));
	}
	
	@Test
	public void shouldReturnForAllRelatedPersonAsXML() throws Exception {
		MockHttpServletResponse response = get("/RelatedPerson").accept(FhirMediaTypes.XML).go();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), is(FhirMediaTypes.XML.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		Bundle results = readBundleResponse(response);
		
		assertThat(results, notNullValue());
		assertThat(results.getType(), equalTo(Bundle.BundleType.SEARCHSET));
		assertThat(results.hasEntry(), is(true));
		
		List<Bundle.BundleEntryComponent> entries = results.getEntry();
		
		assertThat(entries, everyItem(hasProperty("fullUrl", startsWith("http://localhost/ws/fhir2/R4/RelatedPerson/"))));
		assertThat(entries, everyItem(hasResource(instanceOf(RelatedPerson.class))));
	}
	
	@Test
	public void shouldReturnSortedAndFilterSearchResultsForRelatedPersonAsXML() throws Exception {
		MockHttpServletResponse response = get("/RelatedPerson?name=John&_sort=-date").accept(FhirMediaTypes.XML).go();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), is(FhirMediaTypes.XML.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		Bundle results = readBundleResponse(response);
		
		assertThat(results, notNullValue());
		assertThat(results.getType(), equalTo(Bundle.BundleType.SEARCHSET));
		assertThat(results.hasEntry(), is(true));
		
		List<Bundle.BundleEntryComponent> entries = results.getEntry();
		
		assertThat(entries,
		    everyItem(hasResource(hasProperty("nameFirstRep", hasProperty("family", containsString("Doe"))))));
		assertThat(entries, containsInRelativeOrder(
		    hasResource(hasProperty("nameFirstRep", hasProperty("givenAsSingleString", containsString("F"))))));
	}
}
