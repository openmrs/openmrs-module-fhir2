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

import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.startsWith;

import java.util.List;
import java.util.stream.Collectors;

import lombok.AccessLevel;
import lombok.Getter;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.OperationOutcome;
import org.hl7.fhir.r4.model.ValueSet;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletResponse;

public class ValueSetFhirResourceProviderIntegrationTest extends BaseFhirR4IntegrationTest<ValueSetFhirResourceProvider, ValueSet> {
	
	private static final String VALUESET_DATA_XML = "org/openmrs/module/fhir2/api/dao/impl/ValueSetFhirResourceProviderIntegrationTest_initial_data.xml";
	
	private static final String FARM_ANIMAL_CONCEPT_SET_UUID = "378e63b1-6c75-46ed-95e3-797b48ddc9f8";
	
	private static final String INVALID_UUID = "0f89e14e-cdc2-49ac-9255-b5126f8a5147";
	
	private static final String CONCEPT_THAT_IS_NOT_SET_UUID = "0cbe2ed3-cd5f-4f46-9459-26127c9265ab"; //CONCEPT WITH IsSet = false
	
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
		
		MockHttpServletResponse response = get("/ValueSet/" + FARM_ANIMAL_CONCEPT_SET_UUID).accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), is(FhirMediaTypes.JSON.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		ValueSet valueSet = readResponse(response);
		
		assertThat(valueSet, notNullValue());
		assertThat(valueSet.getIdElement().getIdPart(), equalTo(FARM_ANIMAL_CONCEPT_SET_UUID));
		assertThat(valueSet, validResource());
		
		assertThat(valueSet.getCompose(), notNullValue());
		List<ValueSet.ConceptSetComponent> includedComponents = valueSet.getCompose().getInclude();
		assertThat(includedComponents.size(), is(3));
		
		// confirm both the includes have the right system
		assertThat(includedComponents, hasItem(hasProperty("system", nullValue())));
		assertThat(includedComponents, hasItem(hasProperty("system", is("http://www.nfacc.ca/"))));
		assertThat(includedComponents, hasItem(hasProperty("system", is("https://www.eaap.org/"))));
		
		ValueSet.ConceptSetComponent uuidSystemSet = includedComponents.stream().filter(element -> {
			return element.getSystem() == null;
		}).collect(Collectors.toList()).get(0);
		ValueSet.ConceptSetComponent farmAnimalCodesSystemSet = includedComponents.stream().filter(element -> {
			return element.getSystemElement().equals("http://www.nfacc.ca/");
		}).collect(Collectors.toList()).get(0);
		ValueSet.ConceptSetComponent spanishCodesSystemSet = includedComponents.stream().filter(element -> {
			return element.getSystemElement().equals("https://www.eaap.org/");
		}).collect(Collectors.toList()).get(0);
		
		// confirm both sets have 3 concepts
		assertThat(uuidSystemSet.getConcept().size(), is(3));
		assertThat(farmAnimalCodesSystemSet.getConcept().size(), is(3));
		assertThat(spanishCodesSystemSet.getConcept().size(), is(3));
		
		assertThat(uuidSystemSet.getConcept(), hasItem(
		    allOf(hasProperty("code", is("bbbf56f8-706e-41ef-95cd-57271567223a")), hasProperty("display", is("Cow")))));
		assertThat(uuidSystemSet.getConcept(), hasItem(
		    allOf(hasProperty("code", is("b150225f-2086-43af-a13a-5ff16464a6f7")), hasProperty("display", is("Pig")))));
		assertThat(uuidSystemSet.getConcept(), hasItem(
		    allOf(hasProperty("code", is("f5608410-6610-4314-84de-6d23a7fdfa99")), hasProperty("display", is("Sheep")))));
		
		assertThat(farmAnimalCodesSystemSet.getConcept(),
		    hasItem(allOf(hasProperty("code", is("MOO")), hasProperty("display", is("Cow")))));
		assertThat(farmAnimalCodesSystemSet.getConcept(),
		    hasItem(allOf(hasProperty("code", is("OINK")), hasProperty("display", is("Pig")))));
		assertThat(farmAnimalCodesSystemSet.getConcept(),
		    hasItem(allOf(hasProperty("code", is("BAA")), hasProperty("display", is("Sheep")))));
		
		assertThat(spanishCodesSystemSet.getConcept(),
		    hasItem(allOf(hasProperty("code", is("VACA")), hasProperty("display", is("Cow")))));
		assertThat(spanishCodesSystemSet.getConcept(),
		    hasItem(allOf(hasProperty("code", is("CERDO")), hasProperty("display", is("Pig")))));
		assertThat(spanishCodesSystemSet.getConcept(),
		    hasItem(allOf(hasProperty("code", is("OVEJA")), hasProperty("display", is("Sheep")))));
		
	}
	
	@Test
	public void shouldReturnNotFoundWhenValueSetNotFoundAsJsonGivenInvalidUUID() throws Exception {
		MockHttpServletResponse response = get("/ValueSet/" + INVALID_UUID).accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isNotFound());
		assertThat(response.getContentType(), is(FhirMediaTypes.JSON.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		OperationOutcome operationOutcome = readOperationOutcome(response);
		
		assertThat(operationOutcome, notNullValue());
		assertThat(operationOutcome.hasIssue(), is(true));
	}
	
	@Test
	public void shouldReturnNotFoundWhenValueSetNotFoundAsJsonGivenConceptUUID() throws Exception {
		MockHttpServletResponse response = get("/ValueSet/" + CONCEPT_THAT_IS_NOT_SET_UUID).accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isNotFound());
		assertThat(response.getContentType(), is(FhirMediaTypes.JSON.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		OperationOutcome operationOutcome = readOperationOutcome(response);
		
		assertThat(operationOutcome, notNullValue());
		assertThat(operationOutcome.hasIssue(), is(true));
	}
	
	@Test
	public void shouldReturnValueSetAsXML() throws Exception {
		MockHttpServletResponse response = get("/ValueSet/" + FARM_ANIMAL_CONCEPT_SET_UUID).accept(FhirMediaTypes.XML).go();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), is(FhirMediaTypes.XML.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		ValueSet valueSet = readResponse(response);
		
		assertThat(valueSet, notNullValue());
		assertThat(valueSet.getIdElement().getIdPart(), equalTo(FARM_ANIMAL_CONCEPT_SET_UUID));
		assertThat(valueSet, validResource());
		
		// TODO add more?
	}
	
	@Test
	public void shouldReturnNotFoundWhenValueSetNotFoundAsXMLGivenInvalidUUID() throws Exception {
		MockHttpServletResponse response = get("/ValueSet/" + INVALID_UUID).accept(FhirMediaTypes.XML).go();
		
		assertThat(response, isNotFound());
		assertThat(response.getContentType(), is(FhirMediaTypes.XML.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		OperationOutcome operationOutcome = readOperationOutcome(response);
		
		assertThat(operationOutcome, notNullValue());
		assertThat(operationOutcome.hasIssue(), is(true));
	}
	
	@Test
	public void shouldReturnNotFoundWhenValueSetNotFoundAsXMLGivenConceptUUID() throws Exception {
		MockHttpServletResponse response = get("/ValueSet/" + CONCEPT_THAT_IS_NOT_SET_UUID).accept(FhirMediaTypes.XML).go();
		
		assertThat(response, isNotFound());
		assertThat(response.getContentType(), is(FhirMediaTypes.XML.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		OperationOutcome operationOutcome = readOperationOutcome(response);
		
		assertThat(operationOutcome, notNullValue());
		assertThat(operationOutcome.hasIssue(), is(true));
	}
	
	@Test
	public void shouldSearchForValueSetsByTitleAsJSON() throws Exception {
		String uri = String.format("/ValueSet/?title=%s", "Farm Animal Set");
		MockHttpServletResponse response = get(uri).accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), is(FhirMediaTypes.JSON.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		Bundle results = readBundleResponse(response);
		
		assertThat(results, notNullValue());
		assertThat(results.getType(), equalTo(Bundle.BundleType.SEARCHSET));
		assertThat(results.hasEntry(), is(true));
		
		List<Bundle.BundleEntryComponent> entries = results.getEntry();
		
		assertThat(entries.isEmpty(), not(true));
		assertThat(entries, everyItem(hasProperty("fullUrl", startsWith("http://localhost/ws/fhir2/R4/ValueSet"))));
		assertThat(entries, everyItem(hasResource(instanceOf(ValueSet.class))));
		assertThat(entries, everyItem(hasResource(validResource())));
		assertThat(entries, everyItem(hasResource(hasProperty("title", equalTo("Farm Animal Set")))));
	}
	
	@Test
	public void shouldSearchForValueSetsByTitleAsXml() throws Exception {
		String uri = String.format("/ValueSet/?title=%s", "Farm Animal Set");
		MockHttpServletResponse response = get(uri).accept(FhirMediaTypes.XML).go();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), is(FhirMediaTypes.XML.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		Bundle results = readBundleResponse(response);
		
		assertThat(results, notNullValue());
		assertThat(results.getType(), equalTo(Bundle.BundleType.SEARCHSET));
		assertThat(results.hasEntry(), is(true));
		
		List<Bundle.BundleEntryComponent> entries = results.getEntry();
		
		assertThat(entries.isEmpty(), not(true));
		assertThat(entries, everyItem(hasProperty("fullUrl", startsWith("http://localhost/ws/fhir2/R4/ValueSet"))));
		assertThat(entries, everyItem(hasResource(instanceOf(ValueSet.class))));
		assertThat(entries, everyItem(hasResource(validResource())));
		assertThat(entries, everyItem(hasResource(hasProperty("title", equalTo("Farm Animal Set")))));
	}
	
}
