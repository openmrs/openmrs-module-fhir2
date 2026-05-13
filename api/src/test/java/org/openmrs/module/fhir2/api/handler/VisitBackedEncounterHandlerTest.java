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
import static org.mockito.Mockito.verifyNoInteractions;
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
import org.openmrs.module.fhir2.api.FhirVisitService;
import org.openmrs.module.fhir2.api.search.param.SearchParameterMap;

@RunWith(MockitoJUnitRunner.class)
public class VisitBackedEncounterHandlerTest {
	
	private static final String ENCOUNTER_UUID = "344kk343-45hj45-34jk34-34ui33";
	
	@Mock
	private FhirVisitService visitService;
	
	private VisitBackedEncounterHandler handler;
	
	private Encounter fhirEncounter;
	
	@Before
	public void setUp() {
		handler = new VisitBackedEncounterHandler();
		handler.setVisitService(visitService);
		
		fhirEncounter = new Encounter();
		fhirEncounter.setId(ENCOUNTER_UUID);
	}
	
	// ---- dispatch predicates ----
	
	@Test
	public void shouldExposeVisitImplicitProfile() {
		assertThat(handler.getImplicitProfile(), equalTo("http://fhir.openmrs.org/StructureDefinition/openmrs-visit"));
	}
	
	@Test
	public void canHandle_shouldReturnTrueWhenTypeCodingHasVisitSystem() {
		CodeableConcept type = new CodeableConcept();
		type.addCoding().setSystem(FhirConstants.VISIT_TYPE_SYSTEM_URI).setCode("1");
		Encounter encounter = new Encounter().addType(type);
		
		assertTrue(handler.canHandle(encounter));
	}
	
	@Test
	public void canHandle_shouldReturnFalseWhenTypeCodingHasOnlyEncounterSystem() {
		CodeableConcept type = new CodeableConcept();
		type.addCoding().setSystem(FhirConstants.ENCOUNTER_TYPE_SYSTEM_URI).setCode("1");
		Encounter encounter = new Encounter().addType(type);
		
		assertFalse(handler.canHandle(encounter));
	}
	
	// ---- acceptsSearch (tag-aware opt-out) ----
	
	@Test
	public void acceptsSearch_shouldAcceptUntaggedSearch() {
		assertTrue(handler.acceptsSearch(new SearchParameterMap()));
	}
	
	@Test
	public void acceptsSearch_shouldAcceptVisitTag() {
		SearchParameterMap params = new SearchParameterMap().addParameter(FhirConstants.TAG_SEARCH_HANDLER,
		    new TokenAndListParam().addAnd(new TokenParam(FhirConstants.OPENMRS_FHIR_EXT_ENCOUNTER_TAG, "visit")));
		assertTrue(handler.acceptsSearch(params));
	}
	
	@Test
	public void acceptsSearch_shouldOptOutOnEncounterTag() {
		SearchParameterMap params = new SearchParameterMap().addParameter(FhirConstants.TAG_SEARCH_HANDLER,
		    new TokenAndListParam().addAnd(new TokenParam(FhirConstants.OPENMRS_FHIR_EXT_ENCOUNTER_TAG, "encounter")));
		assertFalse(handler.acceptsSearch(params));
	}
	
	@Test
	public void acceptsSearch_shouldAcceptOrListContainingVisitCode() {
		TokenOrListParam orList = new TokenOrListParam();
		orList.add(FhirConstants.OPENMRS_FHIR_EXT_ENCOUNTER_TAG, "encounter");
		orList.add(FhirConstants.OPENMRS_FHIR_EXT_ENCOUNTER_TAG, "visit");
		SearchParameterMap params = new SearchParameterMap().addParameter(FhirConstants.TAG_SEARCH_HANDLER,
		    new TokenAndListParam().addAnd(orList));
		
		assertTrue(handler.acceptsSearch(params));
	}
	
	// ---- delegation ----
	
	@Test
	public void get_shouldDelegateToVisitService() {
		when(visitService.get(ENCOUNTER_UUID)).thenReturn(fhirEncounter);
		
		Encounter result = handler.get(ENCOUNTER_UUID);
		
		assertThat(result, sameInstance(fhirEncounter));
		verify(visitService).get(ENCOUNTER_UUID);
	}
	
	@Test
	public void exists_shouldDelegateToVisitServiceExists() {
		when(visitService.exists(ENCOUNTER_UUID)).thenReturn(true);
		
		assertTrue(handler.exists(ENCOUNTER_UUID));
		// Probe should use the cheap exists() — never call get(), which would translate.
		verify(visitService).exists(ENCOUNTER_UUID);
		verify(visitService, never()).get(ENCOUNTER_UUID);
	}
	
	@Test
	public void create_shouldDelegateToVisitService() {
		when(visitService.create(fhirEncounter)).thenReturn(fhirEncounter);
		
		Encounter result = handler.create(fhirEncounter);
		
		assertThat(result, sameInstance(fhirEncounter));
		verify(visitService).create(fhirEncounter);
	}
	
	@Test
	public void update_shouldDelegateToVisitService() {
		when(visitService.update(ENCOUNTER_UUID, fhirEncounter)).thenReturn(fhirEncounter);
		
		Encounter result = handler.update(ENCOUNTER_UUID, fhirEncounter);
		
		assertThat(result, sameInstance(fhirEncounter));
		verify(visitService).update(ENCOUNTER_UUID, fhirEncounter);
	}
	
	@Test
	public void delete_shouldDelegateToVisitService() {
		handler.delete(ENCOUNTER_UUID);
		verify(visitService).delete(ENCOUNTER_UUID);
	}
	
	@Test
	public void search_shouldDelegateToVisitService() {
		IBundleProvider expected = new SimpleBundleProvider();
		SearchParameterMap params = new SearchParameterMap();
		when(visitService.searchForVisits(params)).thenReturn(expected);
		
		IBundleProvider result = handler.search(params);
		
		assertThat(result, is(expected));
		verify(visitService).searchForVisits(params);
	}
	
	@Test
	public void acceptsSearch_shouldNotInteractWithVisitService() {
		// Acceptance check must be local to the handler, not delegate to visitService.
		handler.acceptsSearch(new SearchParameterMap());
		verifyNoInteractions(visitService);
	}
}
