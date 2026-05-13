/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.fhir2.api.handler;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ca.uhn.fhir.rest.api.server.IBundleProvider;
import ca.uhn.fhir.rest.param.TokenAndListParam;
import ca.uhn.fhir.rest.param.TokenOrListParam;
import ca.uhn.fhir.rest.param.TokenParam;
import ca.uhn.fhir.rest.server.SimpleBundleProvider;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Encounter;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.api.FhirOpenmrsEncounterService;
import org.openmrs.module.fhir2.api.search.param.SearchParameterMap;

/**
 * Tests the dispatch hooks and the delegation behaviour. The CRUD and search logic lives in
 * {@code FhirOpenmrsEncounterServiceImpl} and is tested in
 * {@code FhirOpenmrsEncounterServiceImplTest}.
 */
@RunWith(MockitoJUnitRunner.class)
public class EncounterBackedEncounterHandlerTest {
	
	private static final String ENCOUNTER_UUID = "344kk343-45hj45-34jk34-34ui33";
	
	@Mock
	private FhirOpenmrsEncounterService encounterService;
	
	private EncounterBackedEncounterHandler handler;
	
	private Encounter fhirEncounter;
	
	@Before
	public void setUp() {
		handler = new EncounterBackedEncounterHandler();
		handler.setEncounterService(encounterService);
		
		fhirEncounter = new Encounter();
		fhirEncounter.setId(ENCOUNTER_UUID);
	}
	
	// ---- dispatch predicates ----
	
	@Test
	public void shouldExposeEncounterImplicitProfile() {
		assertThat(handler.getImplicitProfile(), equalTo("http://fhir.openmrs.org/StructureDefinition/openmrs-encounter"));
	}
	
	@Test
	public void shouldExposeEncounterBackingKey() {
		assertThat(handler.getBackingKey(), equalTo("openmrs.encounter"));
	}
	
	@Test
	public void canHandle_shouldReturnTrueWhenTypeCodingHasEncounterSystem() {
		CodeableConcept type = new CodeableConcept();
		type.addCoding().setSystem(FhirConstants.ENCOUNTER_TYPE_SYSTEM_URI).setCode("1");
		Encounter encounter = new Encounter().addType(type);
		
		assertTrue(handler.canHandle(encounter));
	}
	
	@Test
	public void canHandle_shouldReturnFalseWhenTypeCodingHasOnlyVisitSystem() {
		CodeableConcept type = new CodeableConcept();
		type.addCoding().setSystem(FhirConstants.VISIT_TYPE_SYSTEM_URI).setCode("1");
		Encounter encounter = new Encounter().addType(type);
		
		assertFalse(handler.canHandle(encounter));
	}
	
	@Test
	public void canHandle_shouldReturnFalseWhenNoTypeCodings() {
		assertFalse(handler.canHandle(new Encounter()));
	}
	
	// ---- acceptsSearch (tag-aware opt-out) ----
	
	@Test
	public void acceptsSearch_shouldAcceptUntaggedSearch() {
		assertTrue(handler.acceptsSearch(new SearchParameterMap()));
	}
	
	@Test
	public void acceptsSearch_shouldAcceptEncounterTag() {
		SearchParameterMap params = new SearchParameterMap().addParameter(FhirConstants.TAG_SEARCH_HANDLER,
		    new TokenAndListParam().addAnd(new TokenParam(FhirConstants.OPENMRS_FHIR_EXT_ENCOUNTER_TAG, "encounter")));
		assertTrue(handler.acceptsSearch(params));
	}
	
	@Test
	public void acceptsSearch_shouldOptOutOnVisitTag() {
		SearchParameterMap params = new SearchParameterMap().addParameter(FhirConstants.TAG_SEARCH_HANDLER,
		    new TokenAndListParam().addAnd(new TokenParam(FhirConstants.OPENMRS_FHIR_EXT_ENCOUNTER_TAG, "visit")));
		assertFalse(handler.acceptsSearch(params));
	}
	
	@Test
	public void acceptsSearch_shouldAcceptOrListContainingEncounterCode() {
		TokenOrListParam orList = new TokenOrListParam();
		orList.add(FhirConstants.OPENMRS_FHIR_EXT_ENCOUNTER_TAG, "encounter");
		orList.add(FhirConstants.OPENMRS_FHIR_EXT_ENCOUNTER_TAG, "visit");
		SearchParameterMap params = new SearchParameterMap().addParameter(FhirConstants.TAG_SEARCH_HANDLER,
		    new TokenAndListParam().addAnd(orList));
		
		assertTrue(handler.acceptsSearch(params));
	}
	
	@Test
	public void acceptsSearch_shouldIgnoreTagsInUnrelatedSystems() {
		SearchParameterMap params = new SearchParameterMap().addParameter(FhirConstants.TAG_SEARCH_HANDLER,
		    new TokenAndListParam().addAnd(new TokenParam("http://example.org/some-other-system", "value")));
		assertTrue(handler.acceptsSearch(params));
	}
	
	// ---- delegation ----
	
	@Test
	public void get_shouldDelegateToEncounterService() {
		when(encounterService.get(ENCOUNTER_UUID)).thenReturn(fhirEncounter);
		
		Encounter result = handler.get(ENCOUNTER_UUID);
		
		assertThat(result, sameInstance(fhirEncounter));
		verify(encounterService).get(ENCOUNTER_UUID);
	}
	
	@Test
	public void exists_shouldDelegateToEncounterServiceExists() {
		when(encounterService.exists(ENCOUNTER_UUID)).thenReturn(true);
		
		assertTrue(handler.exists(ENCOUNTER_UUID));
		verify(encounterService).exists(ENCOUNTER_UUID);
		verify(encounterService, never()).get(ENCOUNTER_UUID);
	}
	
	@Test
	public void create_shouldDelegateToEncounterService() {
		when(encounterService.create(fhirEncounter)).thenReturn(fhirEncounter);
		
		Encounter result = handler.create(fhirEncounter);
		
		assertThat(result, sameInstance(fhirEncounter));
		verify(encounterService).create(fhirEncounter);
	}
	
	@Test
	public void update_shouldDelegateToEncounterService() {
		when(encounterService.update(ENCOUNTER_UUID, fhirEncounter)).thenReturn(fhirEncounter);
		
		Encounter result = handler.update(ENCOUNTER_UUID, fhirEncounter);
		
		assertThat(result, sameInstance(fhirEncounter));
		verify(encounterService).update(ENCOUNTER_UUID, fhirEncounter);
	}
	
	@Test
	public void delete_shouldDelegateToEncounterService() {
		handler.delete(ENCOUNTER_UUID);
		verify(encounterService).delete(ENCOUNTER_UUID);
	}
	
	@Test
	public void search_shouldDelegateToEncounterServiceSearchForEncounters() {
		IBundleProvider expected = new SimpleBundleProvider();
		SearchParameterMap params = new SearchParameterMap();
		when(encounterService.searchForEncounters(params)).thenReturn(expected);
		
		IBundleProvider result = handler.search(params);
		
		assertThat(result, is(expected));
		verify(encounterService).searchForEncounters(params);
	}
}
