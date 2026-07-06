/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.fhir2.api.translators.impl;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

import org.hl7.fhir.r4.model.Timing;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.Concept;
import org.openmrs.api.ConceptService;
import org.openmrs.module.fhir2.BaseFhirContextSensitiveTest;
import org.openmrs.module.fhir2.api.translators.DurationUnitTranslator;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Covers dictionaries that carry the duration unit mappings current dictionaries ship: the SNOMED
 * CT minute code that is active since the 2021-07-31 release, and UCUM mappings. Unlike
 * {@link DurationUnitTranslatorImplTest}, the dataset here deliberately carries no legacy SNOMED CT
 * mappings, so these tests prove the fallbacks rather than the legacy path.
 */
public class DurationUnitTranslatorImplCurrentCodesTest extends BaseFhirContextSensitiveTest {
	
	private static final String CURRENT_CODES_DATA = "org/openmrs/module/fhir2/mapping/FhirDurationUnitTranslatorTest_current_codes_data.xml";
	
	private static final String MINUTES_CURRENT_CODE_UUID = "917330aa-b67d-11ec-8065-0242ac110002";
	
	private static final String UCUM_HOURS_UUID = "918220aa-b67d-11ec-8065-0242ac110002";
	
	@Autowired
	private DurationUnitTranslator durationUnitTranslator;
	
	@Autowired
	ConceptService conceptService;
	
	@Before
	public void setup() throws Exception {
		executeDataSet(CURRENT_CODES_DATA);
	}
	
	@Test
	public void toFhirResource_shouldTranslateConceptMappedToTheCurrentSnomedCtMinuteCode() {
		Concept concept = conceptService.getConceptByUuid(MINUTES_CURRENT_CODE_UUID);
		
		Timing.UnitsOfTime result = durationUnitTranslator.toFhirResource(concept);
		
		assertThat(result, equalTo(Timing.UnitsOfTime.MIN));
	}
	
	@Test
	public void toFhirResource_shouldTranslateConceptMappedOnlyToUcum() {
		Concept concept = conceptService.getConceptByUuid(UCUM_HOURS_UUID);
		
		Timing.UnitsOfTime result = durationUnitTranslator.toFhirResource(concept);
		
		assertThat(result, equalTo(Timing.UnitsOfTime.H));
	}
	
	@Test
	public void toOpenmrsType_shouldResolveMinutesThroughTheCurrentSnomedCtCodeWhenTheLegacyMappingIsAbsent() {
		Concept result = durationUnitTranslator.toOpenmrsType(Timing.UnitsOfTime.MIN);
		
		assertThat(result, notNullValue());
		assertThat(result.getUuid(), equalTo(MINUTES_CURRENT_CODE_UUID));
	}
	
	@Test
	public void toOpenmrsType_shouldResolveHoursThroughTheUcumMappingWhenNoSnomedCtMappingExists() {
		Concept result = durationUnitTranslator.toOpenmrsType(Timing.UnitsOfTime.H);
		
		assertThat(result, notNullValue());
		assertThat(result.getUuid(), equalTo(UCUM_HOURS_UUID));
	}
}
