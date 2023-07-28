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

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.startsWith;
import static org.openmrs.module.fhir2.api.util.GeneralUtils.inputStreamToString;

import java.io.InputStream;
import java.util.List;
import java.util.Objects;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Extension;
import org.hl7.fhir.r4.model.Group;
import org.hl7.fhir.r4.model.OperationOutcome;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.module.fhir2.BaseFhirIntegrationTest;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.model.GroupMember;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
public class GroupResourceProviderIntegrationTest extends BaseFhirR4IntegrationTest<GroupFhirResourceProvider, Group> {
	
	private static final String GROUP_UUID = "1d64befb-3b2e-48e5-85f5-353d43e23e46";
	
	private static final String GROUP_WITH_MORE_MEMBERS = "2d64befb-3b2e-48e5-85f5-353d43e23e48";
	
	private static final String BAD_GROUP_UUID = "5c9d032b-6092-4052-93d2-a04202b98462";
	
	private static final String COHORT_DATA_XML = "org/openmrs/module/fhir2/api/dao/impl/FhirCohortMemberDaoImplTest_initial_data.xml";
	
	private static final String PATIENT_DATA_XML = "org/openmrs/module/fhir2/api/dao/impl/FhirPatientDaoImplTest_initial_data.xml";
	
	private static final String JSON_CREATE_GROUP_DOCUMENT = "org/openmrs/module/fhir2/providers/GroupWebTest_create.json";
	
	private static final String XML_CREATE_GROUP_DOCUMENT = "org/openmrs/module/fhir2/providers/GroupWebTest_create.xml";
	
	private static final String JSON_UPDATE_GROUP_DOCUMENT = "org/openmrs/module/fhir2/providers/GroupWebTest_update.json";
	
	private static final String XML_UPDATE_GROUP_DOCUMENT = "org/openmrs/module/fhir2/providers/GroupWebTest_update.xml";
	
	@Autowired
	@Getter(AccessLevel.PUBLIC)
	private GroupFhirResourceProvider resourceProvider;
	
	@Before
	@Override
	public void setup() throws Exception {
		super.setup();
		getFhirContext().registerCustomType(GroupMember.class);
		executeDataSet(PATIENT_DATA_XML);
		executeDataSet(COHORT_DATA_XML);
	}
	
	@Test
	public void shouldReturnExistingGroupAsJson() throws Exception {
		MockHttpServletResponse response = get("/Group/" + GROUP_UUID).accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), is(FhirMediaTypes.JSON.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		Group group = readResponse(response);
		
		assertThat(group, notNullValue());
		assertThat(group.getIdElement().getIdPart(), equalTo(GROUP_UUID));
		assertThat(group, validResource());
		
		assertThat(group.getMember(), notNullValue());
		assertThat(group.getQuantity(), equalTo(1));
		assertThat(group.getMember().size(), equalTo(1));
	}
	
	@Test
	public void shouldThrow404ForNonExistingGroupAsJson() throws Exception {
		MockHttpServletResponse response = get("/Group/" + BAD_GROUP_UUID)
		        .accept(BaseFhirIntegrationTest.FhirMediaTypes.JSON).go();
		
		assertThat(response, isNotFound());
		assertThat(response.getContentType(), is(BaseFhirIntegrationTest.FhirMediaTypes.JSON.toString()));
		assertThat(response.getContentAsString(), notNullValue());
	}
	
	@Test
	public void shouldReturnExistingGroupAsXML() throws Exception {
		MockHttpServletResponse response = get("/Group/" + GROUP_UUID).accept(BaseFhirIntegrationTest.FhirMediaTypes.XML)
		        .go();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), is(BaseFhirIntegrationTest.FhirMediaTypes.XML.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		Group group = readResponse(response);
		
		assertThat(group, notNullValue());
		assertThat(group.getIdElement().getIdPart(), equalTo(GROUP_UUID));
		assertThat(group, validResource());
		
		assertThat(group.getMember(), notNullValue());
		assertThat(group.getQuantity(), equalTo(1));
		assertThat(group.getMember().size(), equalTo(1));
	}
	
	@Test
	public void shouldThrow404ForNonExistingGroupAsXML() throws Exception {
		MockHttpServletResponse response = get("/Group/" + BAD_GROUP_UUID).accept(BaseFhirIntegrationTest.FhirMediaTypes.XML)
		        .go();
		
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
			jsonGroup = inputStreamToString(is, UTF_8);
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
		assertThat(group.getMember(), notNullValue());
		assertThat(group.getQuantity(), equalTo(1));
		
	}
	
	@Test
	public void shouldCreateNewGroupAsXML() throws Exception {
		// read JSON record
		String xmlGroup;
		try (InputStream is = this.getClass().getClassLoader().getResourceAsStream(XML_CREATE_GROUP_DOCUMENT)) {
			Objects.requireNonNull(is);
			xmlGroup = inputStreamToString(is, UTF_8);
		}
		
		// create group
		MockHttpServletResponse response = post("/Group").accept(FhirMediaTypes.XML).xmlContent(xmlGroup).go();
		
		// verify created correctly
		assertThat(response, isCreated());
		assertThat(response.getContentType(), is(FhirMediaTypes.XML.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		Group group = readResponse(response);
		assertThat(group, notNullValue());
		assertThat(group.getActive(), is(true));
		assertThat(group.hasMember(), is(true));
		assertThat(group.getMember(), notNullValue());
		assertThat(group.getQuantity(), equalTo(1));
	}
	
	@Test
	@Transactional(readOnly = true)
	public void shouldUpdateExistingGroupAsJson() throws Exception {
		//Before update
		MockHttpServletResponse response = get("/Group/" + GROUP_UUID).accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), is(FhirMediaTypes.JSON.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		Group group = readResponse(response);
		Extension descExtension = group.getExtensionByUrl(FhirConstants.OPENMRS_FHIR_EXT_GROUP_DESCRIPTION);
		
		assertThat(group, notNullValue());
		assertThat(group, validResource());
		assertThat(group.getIdElement().getIdPart(), equalTo(GROUP_UUID));
		assertThat(group.getMember(), notNullValue());
		assertThat(group.getQuantity(), equalTo(1));
		assertThat(descExtension.getValue().toString(), equalTo("cohort voided"));
		
		// Get existing group with updated name
		String jsonGroup;
		try (InputStream is = this.getClass().getClassLoader().getResourceAsStream(JSON_UPDATE_GROUP_DOCUMENT)) {
			Objects.requireNonNull(is);
			jsonGroup = inputStreamToString(is, UTF_8);
		}
		
		//Update
		response = put("/Group/" + GROUP_UUID).jsonContent(jsonGroup).accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), is(FhirMediaTypes.JSON.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		// read updated record
		group = readResponse(response);
		descExtension = group.getExtensionByUrl(FhirConstants.OPENMRS_FHIR_EXT_GROUP_DESCRIPTION);
		
		assertThat(group, notNullValue());
		assertThat(group.getIdElement().getIdPart(), equalTo(GROUP_UUID));
		assertThat(group.getActive(), is(true));
		assertThat(group.getMember(), notNullValue());
		assertThat(group.getQuantity(), equalTo(1));
		assertThat(descExtension.getValue().toString(), equalTo("Patients with at least one encounter"));
		assertThat(group, validResource());
		
		// Double-check via get
		response = get("/Group/" + GROUP_UUID).accept(FhirMediaTypes.JSON).go();
		
		Group updatedGroup = readResponse(response);
		descExtension = updatedGroup.getExtensionByUrl(FhirConstants.OPENMRS_FHIR_EXT_GROUP_DESCRIPTION);
		
		assertThat(updatedGroup, validResource());
		assertThat(updatedGroup, notNullValue());
		assertThat(updatedGroup.getActive(), is(true));
		assertThat(group.getMember(), notNullValue());
		assertThat(group.getQuantity(), equalTo(1));
		assertThat(descExtension.getValue().toString(), equalTo("Patients with at least one encounter"));
	}
	
	@Test
	@Transactional(readOnly = true)
	public void shouldUpdateExistingGroupAsXML() throws Exception {
		//Before update
		MockHttpServletResponse response = get("/Group/" + GROUP_UUID).accept(FhirMediaTypes.XML).go();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), is(FhirMediaTypes.XML.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		Group group = readResponse(response);
		Extension descExtension = group.getExtensionByUrl(FhirConstants.OPENMRS_FHIR_EXT_GROUP_DESCRIPTION);
		
		assertThat(group, notNullValue());
		assertThat(group, validResource());
		assertThat(group.getIdElement().getIdPart(), equalTo(GROUP_UUID));
		assertThat(descExtension.getValue().toString(), equalTo("cohort voided"));
		
		// Get existing group with updated name
		String xmlGroup;
		try (InputStream is = this.getClass().getClassLoader().getResourceAsStream(XML_UPDATE_GROUP_DOCUMENT)) {
			Objects.requireNonNull(is);
			xmlGroup = inputStreamToString(is, UTF_8);
		}
		
		//Update
		response = put("/Group/" + GROUP_UUID).xmlContent(xmlGroup).accept(FhirMediaTypes.XML).go();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), is(FhirMediaTypes.XML.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		// read updated record
		group = readResponse(response);
		descExtension = group.getExtensionByUrl(FhirConstants.OPENMRS_FHIR_EXT_GROUP_DESCRIPTION);
		
		assertThat(group, notNullValue());
		assertThat(group.getIdElement().getIdPart(), equalTo(GROUP_UUID));
		assertThat(group.getActive(), is(true));
		assertThat(descExtension.getValue().toString(), equalTo("Patients with at least one encounter"));
		assertThat(group, validResource());
		
		// Double-check via get
		response = get("/Group/" + GROUP_UUID).accept(FhirMediaTypes.XML).go();
		
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
		MockHttpServletResponse response = get("/Group/" + GROUP_UUID).accept(FhirMediaTypes.XML).go();
		Group group = readResponse(response);
		
		// update the existing record
		group.setId(BAD_GROUP_UUID);
		
		// send the update to the server
		response = put("/Group/" + GROUP_UUID).xmlContent(toXML(group)).accept(FhirMediaTypes.XML).go();
		
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
		MockHttpServletResponse response = get("/Group/" + GROUP_UUID).accept(FhirMediaTypes.XML).go();
		Group group = readResponse(response);
		
		// update the existing record
		group.setId(BAD_GROUP_UUID);
		
		// send the update to the server
		response = put("/Group/" + BAD_GROUP_UUID).xmlContent(toXML(group)).accept(FhirMediaTypes.XML).go();
		
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
		MockHttpServletResponse response = get("/Group/" + GROUP_UUID).accept(FhirMediaTypes.JSON).go();
		Group group = readResponse(response);
		
		// update the existing record
		group.setId(BAD_GROUP_UUID);
		
		// send the update to the server
		response = put("/Group/" + GROUP_UUID).jsonContent(toJson(group)).accept(FhirMediaTypes.JSON).go();
		
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
		MockHttpServletResponse response = get("/Group/" + GROUP_UUID).accept(FhirMediaTypes.JSON).go();
		Group group = readResponse(response);
		
		// update the existing record
		group.setId(BAD_GROUP_UUID);
		
		// send the update to the server
		response = put("/Group/" + BAD_GROUP_UUID).jsonContent(toJson(group)).accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isNotFound());
		assertThat(response.getContentType(), is(FhirMediaTypes.JSON.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		OperationOutcome operationOutcome = readOperationOutcome(response);
		
		assertThat(operationOutcome, notNullValue());
		assertThat(operationOutcome.hasIssue(), is(true));
	}
	
	@Test
	public void shouldDeleteExistingGroup() throws Exception {
		MockHttpServletResponse response = delete("/Group/" + GROUP_UUID).accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isOk());
		
		response = get("/Group/" + GROUP_UUID).accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, statusEquals(HttpStatus.GONE));
	}
	
	@Test
	public void shouldReturnNotFoundWhenDeletingNonExistentGroup() throws Exception {
		MockHttpServletResponse response = delete("/Group/" + BAD_GROUP_UUID).accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isNotFound());
		assertThat(response.getContentType(), is(FhirMediaTypes.JSON.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		OperationOutcome operationOutcome = readOperationOutcome(response);
		
		assertThat(operationOutcome, notNullValue());
		assertThat(operationOutcome.hasIssue(), is(true));
	}
	
	@Test
	public void shouldReturnPaginatedListOfGroupMembersAsJson() throws Exception {
		MockHttpServletResponse response = get("/Group/" + GROUP_UUID + "/$members").accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), is(BaseFhirIntegrationTest.FhirMediaTypes.JSON.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		Bundle groupMembers = readBundleResponse(response);
		
		assertThat(groupMembers, notNullValue());
		assertThat(groupMembers.getEntry(), not(empty()));
		assertThat(groupMembers.getTotal(), is(1));
		
		List<Bundle.BundleEntryComponent> entries = groupMembers.getEntry();
		
		assertThat(entries, everyItem(hasProperty("fullUrl", startsWith("http://localhost/ws/fhir2/R4/GroupMember/"))));
		assertThat(entries, everyItem(hasResource(instanceOf(GroupMember.class))));
		assertThat(entries, everyItem(hasResource(hasProperty("entity", hasProperty("reference", startsWith("Patient/"))))));
	}
	
	@Test
	public void shouldReturnPaginatedListOfGroupMembersAsXml() throws Exception {
		MockHttpServletResponse response = get("/Group/" + GROUP_UUID + "/$members").accept(FhirMediaTypes.XML).go();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), is(FhirMediaTypes.XML.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		Bundle groupMembers = readBundleResponse(response);
		
		assertThat(groupMembers, notNullValue());
		assertThat(groupMembers.getEntry(), not(empty()));
		assertThat(groupMembers.getTotal(), is(1));
		
		List<Bundle.BundleEntryComponent> entries = groupMembers.getEntry();
		
		assertThat(entries, everyItem(hasProperty("fullUrl", startsWith("http://localhost/ws/fhir2/R4/GroupMember/"))));
		assertThat(entries, everyItem(hasResource(instanceOf(GroupMember.class))));
		assertThat(entries, everyItem(hasResource(hasProperty("entity", hasProperty("reference", startsWith("Patient/"))))));
	}
	
	@Test
	public void shouldReturnPaginatedListOfGroupMembersWithLargeGroupAsJson() throws Exception {
		MockHttpServletResponse response = get("/Group/" + GROUP_WITH_MORE_MEMBERS + "/$members").accept(FhirMediaTypes.JSON)
		        .go();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), is(BaseFhirIntegrationTest.FhirMediaTypes.JSON.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		Bundle groupMembers = readBundleResponse(response);
		
		assertThat(groupMembers, notNullValue());
		assertThat(groupMembers.getEntry(), not(empty()));
		assertThat(groupMembers.getTotal(), is(6));
		
		List<Bundle.BundleEntryComponent> entries = groupMembers.getEntry();
		
		assertThat(entries, everyItem(hasProperty("fullUrl", startsWith("http://localhost/ws/fhir2/R4/GroupMember/"))));
		assertThat(entries, everyItem(hasResource(instanceOf(GroupMember.class))));
		assertThat(entries, everyItem(hasResource(hasProperty("entity", hasProperty("reference", startsWith("Patient/"))))));
	}
	
	@Test
	public void shouldReturnPaginatedListOfGroupMembersWithLargeGroupAsXml() throws Exception {
		MockHttpServletResponse response = get("/Group/" + GROUP_WITH_MORE_MEMBERS + "/$members").accept(FhirMediaTypes.XML)
		        .go();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), is(FhirMediaTypes.XML.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		Bundle groupMembers = readBundleResponse(response);
		
		assertThat(groupMembers, notNullValue());
		assertThat(groupMembers.getEntry(), not(empty()));
		assertThat(groupMembers.getTotal(), is(6));
		
		List<Bundle.BundleEntryComponent> entries = groupMembers.getEntry();
		
		assertThat(entries, everyItem(hasProperty("fullUrl", startsWith("http://localhost/ws/fhir2/R4/GroupMember/"))));
		assertThat(entries, everyItem(hasResource(instanceOf(GroupMember.class))));
		assertThat(entries, everyItem(hasResource(hasProperty("entity", hasProperty("reference", startsWith("Patient/"))))));
	}
	
	@Test
	public void shouldReturnAnEtagHeaderWhenRetrievingAnExistingGroup() throws Exception {
		MockHttpServletResponse response = get("/Group/" + GROUP_UUID).accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), startsWith(FhirMediaTypes.JSON.toString()));
		
		assertThat(response.getHeader("etag"), notNullValue());
		assertThat(response.getHeader("etag"), startsWith("W/"));
		
		assertThat(response.getContentAsString(), notNullValue());
		
		Group group = readResponse(response);
		
		assertThat(group, notNullValue());
		assertThat(group.getMeta().getVersionId(), notNullValue());
		assertThat(group, validResource());
	}
	
	@Test
	public void shouldReturnNotModifiedWhenRetrievingAnExistingGroupWithAnEtag() throws Exception {
		MockHttpServletResponse response = get("/Group/" + GROUP_UUID).accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), startsWith(FhirMediaTypes.JSON.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		assertThat(response.getHeader("etag"), notNullValue());
		
		String etagValue = response.getHeader("etag");
		
		response = get("/Group/" + GROUP_UUID).accept(FhirMediaTypes.JSON).ifNoneMatchHeader(etagValue).go();
		
		assertThat(response, isOk());
		assertThat(response, statusEquals(HttpStatus.NOT_MODIFIED));
	}
	
}
