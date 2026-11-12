/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.fhir2.api.impl;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasItem;

import java.util.List;
import java.util.stream.Collectors;

import ca.uhn.fhir.rest.api.server.IBundleProvider;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Condition;
import org.hl7.fhir.r4.model.Encounter;
import org.junit.Test;
import org.openmrs.module.fhir2.BaseFhirContextSensitiveTest;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.api.FhirConditionService;
import org.openmrs.module.fhir2.api.FhirEncounterService;
import org.openmrs.module.fhir2.api.search.param.ConditionSearchParams;
import org.openmrs.module.fhir2.api.search.param.EncounterSearchParams;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * The backings of a multi-backed resource are merged in handler precedence order, which must be
 * fixed by {@code @Order} rather than left to bean registration order. These tests use the real
 * Spring wiring so that they check the handlers' declared precedence.
 */
public class MultiBackingSearchOrderTest extends BaseFhirContextSensitiveTest {
	
	private static final String ENCOUNTER_DATA = "org/openmrs/module/fhir2/api/dao/impl/FhirVisitDaoImplTest_initial_data.xml";
	
	private static final String CONDITION_DATA = "org/openmrs/module/fhir2/api/dao/impl/FhirConditionDaoImplTest_initial_data.xml";
	
	private static final String DIAGNOSIS_DATA = "org/openmrs/module/fhir2/api/dao/impl/FhirDiagnosisDaoImplTest_initial_dataset.xml";
	
	@Autowired
	private FhirEncounterService encounterService;
	
	@Autowired
	private FhirConditionService conditionService;
	
	@Test
	public void searchForEncounters_shouldListEncountersBeforeVisits() throws Exception {
		// given
		executeDataSet(ENCOUNTER_DATA);
		
		// when
		IBundleProvider results = encounterService.searchForEncounters(new EncounterSearchParams());
		List<IBaseResource> firstPage = results.getResources(0, 1);
		
		// then
		List<String> tags = ((Encounter) firstPage.get(0)).getMeta().getTag().stream()
		        .filter(t -> FhirConstants.OPENMRS_FHIR_EXT_ENCOUNTER_TAG.equals(t.getSystem())).map(Coding::getCode)
		        .collect(Collectors.toList());
		assertThat(tags, hasItem("encounter"));
	}
	
	@Test
	public void searchConditions_shouldListConditionsBeforeDiagnoses() throws Exception {
		// given
		executeDataSet(CONDITION_DATA);
		executeDataSet(DIAGNOSIS_DATA);
		
		// when
		IBundleProvider results = conditionService.searchConditions(new ConditionSearchParams());
		List<IBaseResource> firstPage = results.getResources(0, 1);
		
		// then
		assertThat(results.size(), greaterThan(1));
		List<String> categories = ((Condition) firstPage.get(0)).getCategory().stream().flatMap(c -> c.getCoding().stream())
		        .map(Coding::getCode).collect(Collectors.toList());
		assertThat(categories, contains(FhirConstants.CONDITION_CATEGORY_CODE_CONDITION));
	}
}
