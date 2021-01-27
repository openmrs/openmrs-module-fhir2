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
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.hl7.fhir.r4.model.Group;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.module.fhir2.BaseFhirIntegrationTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletResponse;

@Slf4j
public class GroupFhirResourceProviderIntegrationTest extends BaseFhirR4IntegrationTest<org.openmrs.module.fhir2.providers.r4.GroupFhirResourceProvider, Group> {
	
	private static final String COHORT_UUID = "1d64befb-3b2e-48e5-85f5-353d43e23e46";
	
	private static final String BAD_COHORT_UUID = "5c9d032b-6092-4052-93d2-a04202b98462";
	
	private static final String COHORT_DATA_XML = "org/openmrs/module/fhir2/api/dao/impl/FhirCohortDaoImplTest_initial_data.xml";
	
	@Autowired
	@Getter(AccessLevel.PUBLIC)
	private org.openmrs.module.fhir2.providers.r4.GroupFhirResourceProvider resourceProvider;
	
	@Before
	@Override
	public void setup() throws Exception {
		super.setup();
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
}
