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
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.openmrs.module.fhir2.api.util.GeneralUtils.inputStreamToString;

import java.io.InputStream;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import lombok.AccessLevel;
import lombok.Getter;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Encounter;
import org.hl7.fhir.r4.model.Resource;
import org.junit.Test;
import org.openmrs.api.EncounterService;
import org.openmrs.api.VisitService;
import org.openmrs.module.fhir2.FhirConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletResponse;

public class EncounterProfileRoutingIntegrationTest extends BaseProfileRoutingR4IntegrationTest<EncounterFhirResourceProvider, Encounter> {
	
	private static final String ENCOUNTER_PROFILE = openmrsProfile("openmrs-encounter");
	
	private static final String VISIT_PROFILE = openmrsProfile("openmrs-visit");
	
	private static final String VISIT_JSON_CREATE_ENCOUNTER_PATH = "org/openmrs/module/fhir2/providers/VisitWebTest_create.json";
	
	private static final String ENCOUNTER_UUID = "6519d653-393b-4118-9c83-a3715b82d4ac"; // encounter 3 from the standard test dataset
	
	private static final String VISIT_UUID = "1e5d5d48-6b78-11e0-93c3-18a905e044dc"; // visit 1 from the standard test dataset
	
	private static final String ENCOUNTER_TYPE_UUID = "61ae96f4-6afe-4351-b6f8-cd4fc383cce1"; // encounter type 1 from the standard test dataset
	
	// the standard test dataset has 6 non-voided visits and 4 non-voided encounters
	private static final int VISIT_COUNT = 6;
	
	private static final int ENCOUNTER_COUNT = 4;
	
	@Getter(AccessLevel.PUBLIC)
	@Autowired
	private EncounterFhirResourceProvider resourceProvider;
	
	@Autowired
	private VisitService visitService;
	
	@Autowired
	private EncounterService encounterService;
	
	@Test
	public void shouldStampEncounterProfileOnEncounterBackedEncounter() throws Exception {
		// when
		MockHttpServletResponse response = get("/Encounter/" + ENCOUNTER_UUID).accept(FhirMediaTypes.JSON).go();
		
		// then
		assertThat(response, isOk());
		assertThat(profilesOf(readResponse(response)), contains(ENCOUNTER_PROFILE));
	}
	
	@Test
	public void shouldStampVisitProfileOnVisitBackedEncounter() throws Exception {
		// when
		MockHttpServletResponse response = get("/Encounter/" + VISIT_UUID).accept(FhirMediaTypes.JSON).go();
		
		// then
		assertThat(response, isOk());
		assertThat(profilesOf(readResponse(response)), contains(VISIT_PROFILE));
	}
	
	@Test
	public void shouldStampEachEncounterWithItsHandlersProfileWhenSearchingWithoutProfile() throws Exception {
		// when
		List<Resource> results = searchAllPages("/Encounter");
		
		// then
		assertThat(results, hasSize(VISIT_COUNT + ENCOUNTER_COUNT));
		assertThat(idsWithProfile(results, VISIT_PROFILE), hasSize(VISIT_COUNT));
		assertThat(idsWithProfile(results, ENCOUNTER_PROFILE), hasSize(ENCOUNTER_COUNT));
		assertThat(idsWithProfile(results, VISIT_PROFILE), hasItem(VISIT_UUID));
		assertThat(idsWithProfile(results, ENCOUNTER_PROFILE), hasItem(ENCOUNTER_UUID));
	}
	
	@Test
	public void shouldRouteSearchToVisitHandlerWhenProfileIsVisitProfile() throws Exception {
		// given
		Set<String> expected = idsWithProfile(searchAllPages("/Encounter"), VISIT_PROFILE);
		
		// when
		List<Resource> results = searchAllPages("/Encounter?_profile=" + VISIT_PROFILE);
		
		// then
		assertThat(results, hasSize(VISIT_COUNT));
		assertThat(results, everyItem(hasProfiles(contains(VISIT_PROFILE))));
		assertThat(idsOf(results), equalTo(expected));
	}
	
	@Test
	public void shouldRouteSearchToEncounterHandlerWhenProfileIsEncounterProfile() throws Exception {
		// given
		Set<String> expected = idsWithProfile(searchAllPages("/Encounter"), ENCOUNTER_PROFILE);
		
		// when
		List<Resource> results = searchAllPages("/Encounter?_profile=" + ENCOUNTER_PROFILE);
		
		// then
		assertThat(results, hasSize(ENCOUNTER_COUNT));
		assertThat(results, everyItem(hasProfiles(contains(ENCOUNTER_PROFILE))));
		assertThat(idsOf(results), equalTo(expected));
	}
	
	@Test
	public void shouldRouteSearchToBothHandlersWhenBothProfilesAreRequested() throws Exception {
		// given
		Set<String> expected = idsOf(searchAllPages("/Encounter"));
		
		// when
		List<Resource> results = searchAllPages("/Encounter?_profile=" + VISIT_PROFILE + "," + ENCOUNTER_PROFILE);
		
		// then
		assertThat(results, hasSize(VISIT_COUNT + ENCOUNTER_COUNT));
		assertThat(idsOf(results), equalTo(expected));
		assertThat(idsWithProfile(results, VISIT_PROFILE), hasSize(VISIT_COUNT));
		assertThat(idsWithProfile(results, ENCOUNTER_PROFILE), hasSize(ENCOUNTER_COUNT));
	}
	
	@Test
	public void shouldFanOutAsUsualWhenProfileMatchesNoHandler() throws Exception {
		// given
		Set<String> expected = idsOf(searchAllPages("/Encounter"));
		
		// when
		List<Resource> results = searchAllPages("/Encounter?_profile=" + UNKNOWN_PROFILE);
		
		// then
		assertThat(idsOf(results), equalTo(expected));
	}
	
	@Test
	public void shouldKeepRoutingToVisitHandlerAcrossPages() throws Exception {
		// given
		int pageSize = 2;
		
		// when
		Bundle firstPage = searchPage("/Encounter?_count=" + pageSize + "&_profile=" + VISIT_PROFILE);
		List<Resource> results = searchAllPages("/Encounter?_count=" + pageSize + "&_profile=" + VISIT_PROFILE);
		
		// then
		assertThat(firstPage.getTotal(), equalTo(VISIT_COUNT));
		assertThat(firstPage.getLink(Bundle.LINK_NEXT), notNullValue());
		assertThat(matchedResources(firstPage).size(), lessThanOrEqualTo(pageSize));
		assertThat(results, hasSize(VISIT_COUNT));
		assertThat(results, everyItem(hasProfiles(contains(VISIT_PROFILE))));
		assertThat(idsOf(results), hasSize(VISIT_COUNT));
	}
	
	@Test
	public void shouldCreateEncounterWhenClaimedByBothHandlersAndNoProfileIsGiven() throws Exception {
		// given
		Encounter encounter = ambiguousEncounter();
		
		// when
		MockHttpServletResponse response = post("/Encounter").accept(FhirMediaTypes.JSON).jsonContent(toJson(encounter))
		        .go();
		
		// then
		assertThat(response, isCreated());
		Encounter created = readResponse(response);
		String uuid = created.getIdElement().getIdPart();
		assertThat(profilesOf(created), contains(ENCOUNTER_PROFILE));
		assertThat(encounterService.getEncounterByUuid(uuid), notNullValue());
		assertThat(visitService.getVisitByUuid(uuid), nullValue());
	}
	
	@Test
	public void shouldCreateVisitWhenMetaProfileIsVisitProfile() throws Exception {
		// given
		Encounter encounter = ambiguousEncounter();
		encounter.getMeta().addProfile(VISIT_PROFILE);
		
		// when
		MockHttpServletResponse response = post("/Encounter").accept(FhirMediaTypes.JSON).jsonContent(toJson(encounter))
		        .go();
		
		// then
		assertThat(response, isCreated());
		Encounter created = readResponse(response);
		String uuid = created.getIdElement().getIdPart();
		assertThat(profilesOf(created), contains(VISIT_PROFILE));
		assertThat(visitService.getVisitByUuid(uuid), notNullValue());
		assertThat(encounterService.getEncounterByUuid(uuid), nullValue());
		
		response = get("/Encounter/" + uuid).accept(FhirMediaTypes.JSON).go();
		assertThat(response, isOk());
		assertThat(profilesOf(readResponse(response)), contains(VISIT_PROFILE));
	}
	
	@Test
	public void shouldCreateEncounterWhenMetaProfileIsEncounterProfile() throws Exception {
		// given
		Encounter encounter = ambiguousEncounter();
		encounter.getMeta().addProfile(ENCOUNTER_PROFILE);
		
		// when
		MockHttpServletResponse response = post("/Encounter").accept(FhirMediaTypes.JSON).jsonContent(toJson(encounter))
		        .go();
		
		// then
		assertThat(response, isCreated());
		Encounter created = readResponse(response);
		String uuid = created.getIdElement().getIdPart();
		assertThat(profilesOf(created), contains(ENCOUNTER_PROFILE));
		assertThat(encounterService.getEncounterByUuid(uuid), notNullValue());
		assertThat(visitService.getVisitByUuid(uuid), nullValue());
		
		response = get("/Encounter/" + uuid).accept(FhirMediaTypes.JSON).go();
		assertThat(response, isOk());
		assertThat(profilesOf(readResponse(response)), contains(ENCOUNTER_PROFILE));
	}
	
	/**
	 * A visit create body whose {@code type} also carries an encounter-type coding, so both handlers'
	 * {@code canHandle} claim it and only {@code meta.profile} can pick one.
	 */
	private Encounter ambiguousEncounter() throws Exception {
		String json;
		try (InputStream is = getClass().getClassLoader().getResourceAsStream(VISIT_JSON_CREATE_ENCOUNTER_PATH)) {
			Objects.requireNonNull(is);
			json = inputStreamToString(is, UTF_8);
		}
		
		Encounter encounter = getFhirContext().newJsonParser().parseResource(Encounter.class, json);
		encounter.getMeta().getTag().clear();
		encounter.getTypeFirstRep()
		        .addCoding(new Coding(FhirConstants.ENCOUNTER_TYPE_SYSTEM_URI, ENCOUNTER_TYPE_UUID, "Scheduled"));
		return encounter;
	}
}
