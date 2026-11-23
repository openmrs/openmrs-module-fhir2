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
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.openmrs.module.fhir2.api.util.GeneralUtils.inputStreamToString;

import java.io.InputStream;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import lombok.AccessLevel;
import lombok.Getter;
import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.dstu3.model.Practitioner;
import org.hl7.fhir.dstu3.model.Resource;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.api.ProviderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletResponse;

public class PractitionerProfileRoutingIntegrationTest extends BaseProfileRoutingR3IntegrationTest<PractitionerFhirResourceProvider, Practitioner> {
	
	private static final String PROVIDER_PROFILE = openmrsProfile("openmrs-provider");
	
	private static final String USER_PROFILE = openmrsProfile("openmrs-user");
	
	private static final String PRACTITIONER_INITIAL_DATA_XML = "org/openmrs/module/fhir2/api/dao/impl/FhirPractitionerDaoImplTest_initial_data.xml";
	
	private static final String JSON_CREATE_PRACTITIONER_DOCUMENT = "org/openmrs/module/fhir2/providers/PractitionerWebTest_create.json";
	
	private static final String PROVIDER_UUID = "f9badd80-ab76-11e2-9e96-0800200c9a66";
	
	private static final String USER_UUID = "1010d442-e134-11de-babe-001e378eb67e"; // the admin user
	
	@Getter(AccessLevel.PUBLIC)
	@Autowired
	private PractitionerFhirResourceProvider resourceProvider;
	
	@Autowired
	private ProviderService providerService;
	
	@Before
	@Override
	public void setup() throws Exception {
		super.setup();
		executeDataSet(PRACTITIONER_INITIAL_DATA_XML);
	}
	
	@Test
	public void shouldStampProviderProfileOnProviderBackedPractitioner() throws Exception {
		// when
		MockHttpServletResponse response = get("/Practitioner/" + PROVIDER_UUID).accept(FhirMediaTypes.JSON).go();
		
		// then
		assertThat(response, isOk());
		assertThat(profilesOf(readResponse(response)), contains(PROVIDER_PROFILE));
	}
	
	@Test
	public void shouldStampUserProfileOnUserBackedPractitioner() throws Exception {
		// when
		MockHttpServletResponse response = get("/Practitioner/" + USER_UUID).accept(FhirMediaTypes.JSON).go();
		
		// then
		assertThat(response, isOk());
		assertThat(profilesOf(readResponse(response)), contains(USER_PROFILE));
	}
	
	@Test
	public void shouldRouteSearchToUserHandlerWhenProfileIsUserProfile() throws Exception {
		// given
		List<Resource> all = searchAllPages("/Practitioner");
		assertThat(idsWithProfile(all, PROVIDER_PROFILE), is(not(empty())));
		
		// when
		List<Resource> results = searchAllPages("/Practitioner?_profile=" + USER_PROFILE);
		
		// then
		assertThat(results, is(not(empty())));
		assertThat(results, everyItem(hasProfiles(contains(USER_PROFILE))));
		assertThat(idsOf(results), hasItem(USER_UUID));
		assertThat(idsOf(results), equalTo(idsWithProfile(all, USER_PROFILE)));
	}
	
	@Test
	public void shouldRouteSearchToProviderHandlerWhenProfileIsProviderProfile() throws Exception {
		// given
		List<Resource> all = searchAllPages("/Practitioner");
		assertThat(idsWithProfile(all, USER_PROFILE), is(not(empty())));
		
		// when
		List<Resource> results = searchAllPages("/Practitioner?_profile=" + PROVIDER_PROFILE);
		
		// then
		assertThat(results, is(not(empty())));
		assertThat(results, everyItem(hasProfiles(contains(PROVIDER_PROFILE))));
		assertThat(idsOf(results), hasItem(PROVIDER_UUID));
		assertThat(idsOf(results), equalTo(idsWithProfile(all, PROVIDER_PROFILE)));
	}
	
	@Test
	public void shouldRouteSearchToBothHandlersWhenBothProfilesAreRequested() throws Exception {
		// given
		Set<String> expected = idsOf(searchAllPages("/Practitioner"));
		
		// when
		List<Resource> results = searchAllPages("/Practitioner?_profile=" + PROVIDER_PROFILE + "," + USER_PROFILE);
		
		// then
		assertThat(idsOf(results), equalTo(expected));
		assertThat(idsWithProfile(results, PROVIDER_PROFILE), hasItem(PROVIDER_UUID));
		assertThat(idsWithProfile(results, USER_PROFILE), hasItem(USER_UUID));
	}
	
	@Test
	public void shouldFanOutAsUsualWhenProfileMatchesNoHandler() throws Exception {
		// given
		Set<String> expected = idsOf(searchAllPages("/Practitioner"));
		
		// when
		List<Resource> results = searchAllPages("/Practitioner?_profile=" + UNKNOWN_PROFILE);
		
		// then
		assertThat(idsOf(results), equalTo(expected));
	}
	
	@Test
	public void shouldKeepRoutingToProviderHandlerAcrossPages() throws Exception {
		// given
		int pageSize = 2;
		Set<String> expected = idsWithProfile(searchAllPages("/Practitioner"), PROVIDER_PROFILE);
		assertThat(expected.size(), greaterThan(pageSize));
		
		// when
		Bundle firstPage = searchPage("/Practitioner?_count=" + pageSize + "&_profile=" + PROVIDER_PROFILE);
		List<Resource> results = searchAllPages("/Practitioner?_count=" + pageSize + "&_profile=" + PROVIDER_PROFILE);
		
		// then
		assertThat(firstPage.getTotal(), equalTo(expected.size()));
		assertThat(firstPage.getLink(Bundle.LINK_NEXT), notNullValue());
		assertThat(results, everyItem(hasProfiles(contains(PROVIDER_PROFILE))));
		assertThat(results, hasSize(expected.size()));
		assertThat(idsOf(results), equalTo(expected));
	}
	
	@Test
	public void shouldCreateProviderWhenMetaProfileIsProviderProfile() throws Exception {
		// given
		Practitioner practitioner = readDocument();
		practitioner.getMeta().addProfile(PROVIDER_PROFILE);
		
		// when
		MockHttpServletResponse response = post("/Practitioner").accept(FhirMediaTypes.JSON)
		        .jsonContent(toJson(practitioner)).go();
		
		// then
		assertThat(response, isCreated());
		Practitioner created = readResponse(response);
		assertThat(profilesOf(created), contains(PROVIDER_PROFILE));
		assertThat(providerService.getProviderByUuid(created.getIdElement().getIdPart()), notNullValue());
	}
	
	@Test
	public void shouldRejectCreateWhenMetaProfileIsReadOnlyUserProfile() throws Exception {
		// given
		Practitioner practitioner = readDocument();
		practitioner.getMeta().addProfile(USER_PROFILE);
		
		// when
		MockHttpServletResponse response = post("/Practitioner").accept(FhirMediaTypes.JSON)
		        .jsonContent(toJson(practitioner)).go();
		
		// then
		assertThat(response, isMethodNotAllowed());
	}
	
	private Practitioner readDocument() throws Exception {
		String json;
		try (InputStream is = getClass().getClassLoader().getResourceAsStream(JSON_CREATE_PRACTITIONER_DOCUMENT)) {
			Objects.requireNonNull(is);
			json = inputStreamToString(is, UTF_8);
		}
		
		return getFhirContext().newJsonParser().parseResource(Practitioner.class, json);
	}
}
