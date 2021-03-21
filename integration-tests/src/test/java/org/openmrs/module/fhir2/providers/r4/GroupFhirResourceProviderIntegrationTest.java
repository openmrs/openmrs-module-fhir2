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
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Extension;
import org.hl7.fhir.r4.model.Group;
import org.hl7.fhir.r4.model.OperationOutcome;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.module.fhir2.BaseFhirIntegrationTest;
import org.openmrs.module.fhir2.FhirConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletResponse;

@Slf4j
public class GroupFhirResourceProviderIntegrationTest extends BaseFhirR4IntegrationTest<org.openmrs.module.fhir2.providers.r4.GroupFhirResourceProvider, Group> {
	
	private static final String COHORT_UUID = "1d64befb-3b2e-48e5-85f5-353d43e23e46";
	
	private static final String BAD_COHORT_UUID = "5c9d032b-6092-4052-93d2-a04202b98462";
	
	private static final String COHORT_DATA_XML = "org/openmrs/module/fhir2/api/dao/impl/FhirCohortDaoImplTest_initial_data.xml";
	
	private static final String PATIENT_DATA_XML = "org/openmrs/module/fhir2/api/dao/impl/FhirPatientDaoImplTest_initial_data.xml";
	
	private static final String JSON_CREATE_GROUP_DOCUMENT = "org/openmrs/module/fhir2/providers/GroupWebTest_create.json";
	
	private static final String XML_CREATE_GROUP_DOCUMENT = "org/openmrs/module/fhir2/providers/GroupWebTest_create.xml";
	
	private static final String JSON_UPDATE_GROUP_DOCUMENT = "org/openmrs/module/fhir2/providers/GroupWebTest_update.json";
	
	private static final String XML_UPDATE_GROUP_DOCUMENT = "org/openmrs/module/fhir2/providers/GroupWebTest_update.xml";
	
	@Autowired
	@Getter(AccessLevel.PUBLIC)
	private org.openmrs.module.fhir2.providers.r4.GroupFhirResourceProvider resourceProvider;
	
	@Before
	@Override
	public void setup() throws Exception {
		super.setup();
		executeDataSet(PATIENT_DATA_XML);
		executeDataSet(COHORT_DATA_XML);
	}
	
	@Test
	public void shouldReturnExistingGroupAsJson() throws Exception {
		MockHttpServletResponse response = get("/Group/" + COHORT_UUID).accept(BaseFhirIntegrationTest.FhirMediaTypes.JSON)
		        .go();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), is(BaseFhirIntegrationTest.FhirMediaTypes.JSON.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		Group group = readResponse(response);
		
		assertThat(group, notNullValue());
		assertThat(group.getIdElement().getIdPart(), equalTo(COHORT_UUID));
		assertThat(group, validResource());
	}
	
	@Test
	public void shouldThrow404ForNonExistingGroupAsJson() throws Exception {
		MockHttpServletResponse response = get("/Group/" + BAD_COHORT_UUID)
		        .accept(BaseFhirIntegrationTest.FhirMediaTypes.JSON).go();
		
		assertThat(response, isNotFound());
		assertThat(response.getContentType(), is(BaseFhirIntegrationTest.FhirMediaTypes.JSON.toString()));
		assertThat(response.getContentAsString(), notNullValue());
	}
	
	@Test
	public void shouldReturnExistingGroupAsXML() throws Exception {
		MockHttpServletResponse response = get("/Group/" + COHORT_UUID).accept(BaseFhirIntegrationTest.FhirMediaTypes.XML)
		        .go();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), is(BaseFhirIntegrationTest.FhirMediaTypes.XML.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		Group group = readResponse(response);
		
		assertThat(group, notNullValue());
		assertThat(group.getIdElement().getIdPart(), equalTo(COHORT_UUID));
		assertThat(group, validResource());
	}
	
	@Test
	public void shouldThrow404ForNonExistingGroupAsXML() throws Exception {
		MockHttpServletResponse response = get("/Group/" + BAD_COHORT_UUID)
		        .accept(BaseFhirIntegrationTest.FhirMediaTypes.XML).go();
		
		assertThat(response, isNotFound());
		assertThat(response.getContentType(), is(BaseFhirIntegrationTest.FhirMediaTypes.XML.toString()));
		assertThat(response.getContentAsString(), notNullValue());
	}
	
	@Test
	public void shouldCreateNewGroupAsJson() throws Exception {
		// read JSON record
		String jsonGroup;
		try (InputStream is = this.getClass().getClassLoader().getResourceAsStream(JSON_CREATE_GROUP_DOCUMENT)) {
			Objects.requireNonNull(is);
			jsonGroup = IOUtils.toString(is, StandardCharsets.UTF_8);
		}
		
		// create group
		MockHttpServletResponse response = post("/Group").accept(FhirMediaTypes.JSON).jsonContent(jsonGroup).go();
		
		// verify created correctly
		assertThat(response, isCreated());
		assertThat(response.getContentType(), is(FhirMediaTypes.JSON.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		Group group = readResponse(response);
		assertThat(group, notNullValue());
		assertThat(group.getActive(), is(true));
		assertThat(group.hasMember(), is(true));
		
		// try to get new group
		response = get(group.getId()).accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isOk());
		
		Group newGroup = readResponse(response);
		
		assertThat(newGroup.getId(), equalTo(group.getId()));
		assertThat(newGroup.getActive(), equalTo(true));
		
	}
	
	@Test
	public void shouldCreateNewGroupAsXML() throws Exception {
		// read JSON record
		String xmlGroup;
		try (InputStream is = this.getClass().getClassLoader().getResourceAsStream(XML_CREATE_GROUP_DOCUMENT)) {
			Objects.requireNonNull(is);
			xmlGroup = IOUtils.toString(is, StandardCharsets.UTF_8);
		}
		
		// create group
		MockHttpServletResponse response = post("/Group").accept(FhirMediaTypes.XML).xmlContext(xmlGroup).go();
		
		// verify created correctly
		assertThat(response, isCreated());
		assertThat(response.getContentType(), is(FhirMediaTypes.XML.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		Group group = readResponse(response);
		assertThat(group, notNullValue());
		assertThat(group.getActive(), is(true));
		assertThat(group.hasMember(), is(true));
		
		// try to get new group
		response = get(group.getId()).accept(FhirMediaTypes.XML).go();
		
		assertThat(response, isOk());
		
		Group newGroup = readResponse(response);
		
		assertThat(newGroup.getId(), equalTo(group.getId()));
		assertThat(newGroup.getActive(), equalTo(true));
	}
	
	@Test
	public void shouldUpdateExistingGroupAsJson() throws Exception {
		//Before update
		MockHttpServletResponse response = get("/Group/" + COHORT_UUID).accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), is(FhirMediaTypes.JSON.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		Group group = readResponse(response);
		Extension descExtension = group.getExtensionByUrl(FhirConstants.OPENMRS_FHIR_EXT_GROUP_DESCRIPTION);
		
		assertThat(group, notNullValue());
		assertThat(group, validResource());
		assertThat(group.getIdElement().getIdPart(), equalTo(COHORT_UUID));
		assertThat(descExtension.getValue().toString(), equalTo("Covid19 patients"));
		
		// Get existing group with updated name
		String jsonGroup;
		try (InputStream is = this.getClass().getClassLoader().getResourceAsStream(JSON_UPDATE_GROUP_DOCUMENT)) {
			Objects.requireNonNull(is);
			jsonGroup = IOUtils.toString(is, StandardCharsets.UTF_8);
		}
		
		//Update
		response = put("/Group/" + COHORT_UUID).jsonContent(jsonGroup).accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), is(FhirMediaTypes.JSON.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		// read updated record
		group = readResponse(response);
		descExtension = group.getExtensionByUrl(FhirConstants.OPENMRS_FHIR_EXT_GROUP_DESCRIPTION);
		
		assertThat(group, notNullValue());
		assertThat(group.getIdElement().getIdPart(), equalTo(COHORT_UUID));
		assertThat(group.getActive(), is(true));
		assertThat(descExtension.getValue().toString(), equalTo("Patients with at least one encounter"));
		assertThat(group, validResource());
		
		// Double-check via get
		response = get("/Group/" + COHORT_UUID).accept(FhirMediaTypes.JSON).go();
		
		Group updatedGroup = readResponse(response);
		descExtension = updatedGroup.getExtensionByUrl(FhirConstants.OPENMRS_FHIR_EXT_GROUP_DESCRIPTION);
		
		assertThat(updatedGroup, validResource());
		assertThat(updatedGroup, notNullValue());
		assertThat(updatedGroup.getActive(), is(true));
		assertThat(descExtension.getValue().toString(), equalTo("Patients with at least one encounter"));
	}
	
	@Test
	public void shouldUpdateExistingGroupAsXML() throws Exception {
		//Before update
		MockHttpServletResponse response = get("/Group/" + COHORT_UUID).accept(FhirMediaTypes.XML).go();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), is(FhirMediaTypes.XML.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		Group group = readResponse(response);
		Extension descExtension = group.getExtensionByUrl(FhirConstants.OPENMRS_FHIR_EXT_GROUP_DESCRIPTION);
		
		assertThat(group, notNullValue());
		assertThat(group, validResource());
		assertThat(group.getIdElement().getIdPart(), equalTo(COHORT_UUID));
		assertThat(descExtension.getValue().toString(), equalTo("Covid19 patients"));
		
		// Get existing group with updated name
		String xmlGroup;
		try (InputStream is = this.getClass().getClassLoader().getResourceAsStream(XML_UPDATE_GROUP_DOCUMENT)) {
			Objects.requireNonNull(is);
			xmlGroup = IOUtils.toString(is, StandardCharsets.UTF_8);
		}
		
		//Update
		response = put("/Group/" + COHORT_UUID).xmlContext(xmlGroup).accept(FhirMediaTypes.XML).go();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), is(FhirMediaTypes.XML.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		// read updated record
		group = readResponse(response);
		descExtension = group.getExtensionByUrl(FhirConstants.OPENMRS_FHIR_EXT_GROUP_DESCRIPTION);
		
		assertThat(group, notNullValue());
		assertThat(group.getIdElement().getIdPart(), equalTo(COHORT_UUID));
		assertThat(group.getActive(), is(true));
		assertThat(descExtension.getValue().toString(), equalTo("Patients with at least one encounter"));
		assertThat(group, validResource());
		
		// Double-check via get
		response = get("/Group/" + COHORT_UUID).accept(FhirMediaTypes.XML).go();
		
		Group updatedGroup = readResponse(response);
		descExtension = updatedGroup.getExtensionByUrl(FhirConstants.OPENMRS_FHIR_EXT_GROUP_DESCRIPTION);
		
		assertThat(updatedGroup, validResource());
		assertThat(updatedGroup, notNullValue());
		assertThat(updatedGroup.getActive(), is(true));
		assertThat(descExtension.getValue().toString(), equalTo("Patients with at least one encounter"));
	}
	
	@Test
	public void shouldReturnBadRequestWhenDocumentIdDoesNotMatchGroupIdAsXML() throws Exception {
		// get the existing record
		MockHttpServletResponse response = get("/Group/" + COHORT_UUID).accept(FhirMediaTypes.XML).go();
		Group group = readResponse(response);
		
		// update the existing record
		group.setId(BAD_COHORT_UUID);
		
		// send the update to the server
		response = put("/Group/" + COHORT_UUID).xmlContext(toXML(group)).accept(FhirMediaTypes.XML).go();
		
		assertThat(response, isBadRequest());
		assertThat(response.getContentType(), is(FhirMediaTypes.XML.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		OperationOutcome operationOutcome = readOperationOutcome(response);
		
		assertThat(operationOutcome, notNullValue());
		assertThat(operationOutcome.hasIssue(), is(true));
	}
	
	@Test
	public void shouldReturnNotFoundWhenUpdatingNonExistentGroupAsXML() throws Exception {
		// get the existing record
		MockHttpServletResponse response = get("/Group/" + COHORT_UUID).accept(FhirMediaTypes.XML).go();
		Group group = readResponse(response);
		
		// update the existing record
		group.setId(BAD_COHORT_UUID);
		
		// send the update to the server
		response = put("/Group/" + BAD_COHORT_UUID).xmlContext(toXML(group)).accept(FhirMediaTypes.XML).go();
		
		assertThat(response, isNotFound());
		assertThat(response.getContentType(), is(FhirMediaTypes.XML.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		OperationOutcome operationOutcome = readOperationOutcome(response);
		
		assertThat(operationOutcome, notNullValue());
		assertThat(operationOutcome.hasIssue(), is(true));
	}
	
	@Test
	public void shouldReturnBadRequestWhenDocumentIdDoesNotMatchGroupIdAsJSON() throws Exception {
		// get the existing record
		MockHttpServletResponse response = get("/Group/" + COHORT_UUID).accept(FhirMediaTypes.JSON).go();
		Group group = readResponse(response);
		
		// update the existing record
		group.setId(BAD_COHORT_UUID);
		
		// send the update to the server
		response = put("/Group/" + COHORT_UUID).jsonContent(toJson(group)).accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isBadRequest());
		assertThat(response.getContentType(), is(FhirMediaTypes.JSON.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		OperationOutcome operationOutcome = readOperationOutcome(response);
		
		assertThat(operationOutcome, notNullValue());
		assertThat(operationOutcome.hasIssue(), is(true));
	}
	
	@Test
	public void shouldReturnNotFoundWhenUpdatingNonExistentGroupAsJSON() throws Exception {
		// get the existing record
		MockHttpServletResponse response = get("/Group/" + COHORT_UUID).accept(FhirMediaTypes.JSON).go();
		Group group = readResponse(response);
		
		// update the existing record
		group.setId(BAD_COHORT_UUID);
		
		// send the update to the server
		response = put("/Group/" + BAD_COHORT_UUID).jsonContent(toJson(group)).accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isNotFound());
		assertThat(response.getContentType(), is(FhirMediaTypes.JSON.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		OperationOutcome operationOutcome = readOperationOutcome(response);
		
		assertThat(operationOutcome, notNullValue());
		assertThat(operationOutcome.hasIssue(), is(true));
	}
	
	@Test
	public void shouldDeleteExistingGroup() throws Exception {
		MockHttpServletResponse response = delete("/Group/" + COHORT_UUID).accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isOk());
		
		response = get("/Group/" + COHORT_UUID).accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, statusEquals(HttpStatus.GONE));
	}
	
	@Test
	public void shouldReturnNotFoundWhenDeletingNonExistentGroup() throws Exception {
		MockHttpServletResponse response = delete("/Group/" + BAD_COHORT_UUID).accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isNotFound());
		assertThat(response.getContentType(), is(FhirMediaTypes.JSON.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		OperationOutcome operationOutcome = readOperationOutcome(response);
		
		assertThat(operationOutcome, notNullValue());
		assertThat(operationOutcome.hasIssue(), is(true));
	}
	
	@Test
	public void shouldReturnCountForGroupAsJson() throws Exception {
		MockHttpServletResponse response = get("/Group?_summary=count").accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), is(FhirMediaTypes.JSON.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		Bundle result = readBundleResponse(response);
		
		assertThat(result, notNullValue());
		assertThat(result.getType(), equalTo(Bundle.BundleType.SEARCHSET));
		
		assertThat(result, hasProperty("total", equalTo(1)));
	}
	
	@Test
	public void shouldReturnCountForGroupAsXml() throws Exception {
		MockHttpServletResponse response = get("/Group?_summary=count").accept(FhirMediaTypes.XML).go();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), is(FhirMediaTypes.XML.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		Bundle result = readBundleResponse(response);
		
		assertThat(result, notNullValue());
		assertThat(result.getType(), equalTo(Bundle.BundleType.SEARCHSET));
		
		assertThat(result, hasProperty("total", equalTo(1)));
	}
}
