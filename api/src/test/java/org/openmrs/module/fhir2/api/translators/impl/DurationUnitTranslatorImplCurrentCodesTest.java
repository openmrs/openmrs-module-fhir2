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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openmrs.Concept;
import org.openmrs.ConceptMap;
import org.openmrs.ConceptMapType;
import org.openmrs.ConceptReferenceTerm;
import org.openmrs.ConceptSource;
import org.openmrs.Duration;
import org.openmrs.api.ConceptService;
import org.openmrs.module.fhir2.BaseFhirContextSensitiveTest;
import org.openmrs.module.fhir2.api.translators.DurationUnitTranslator;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Covers dictionaries that carry the duration unit mappings current dictionaries ship: the SNOMED
 * CT minute code that is active since the 2021-07-31 release, and UCUM mappings. Unlike
 * {@link DurationUnitTranslatorImplTest}, the dataset here deliberately carries no legacy SNOMED CT
 * mappings, so these tests prove the fallbacks rather than the legacy path. Concepts whose mapping
 * sets the dataset must not carry, such as the current CIEL shape with both minute codes and
 * concepts whose SNOMED CT and UCUM mappings disagree, are built in memory instead, since
 * persisting a legacy minute mapping would change what {@code toOpenmrsType(MIN)} resolves.
 */
public class DurationUnitTranslatorImplCurrentCodesTest extends BaseFhirContextSensitiveTest {
	
	private static final String CURRENT_CODES_DATA = "org/openmrs/module/fhir2/mapping/FhirDurationUnitTranslatorTest_current_codes_data.xml";
	
	private static final String CURRENT_SNOMED_CT_MINUTE_CODE = "1156209001";
	
	private static final String MINUTES_CURRENT_CODE_UUID = "917330aa-b67d-11ec-8065-0242ac110002";
	
	private static final String UCUM_HOURS_UUID = "918220aa-b67d-11ec-8065-0242ac110002";
	
	private static final String NAME_ONLY_SNOMED_DAYS_UUID = "910720aa-b67d-11ec-8065-0242ac110002";
	
	@Autowired
	private DurationUnitTranslator durationUnitTranslator;
	
	@Autowired
	ConceptService conceptService;
	
	@BeforeEach
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
	
	@Test
	public void toFhirResource_shouldTranslateConceptWhoseSnomedCtSourceIsIdentifiedByNameOnly() {
		Concept concept = conceptService.getConceptByUuid(NAME_ONLY_SNOMED_DAYS_UUID);
		
		Timing.UnitsOfTime result = durationUnitTranslator.toFhirResource(concept);
		
		assertThat(result, equalTo(Timing.UnitsOfTime.D));
	}
	
	@Test
	public void toOpenmrsType_shouldResolveDaysThroughTheNameIdentifiedSnomedCtSource() {
		Concept result = durationUnitTranslator.toOpenmrsType(Timing.UnitsOfTime.D);
		
		assertThat(result, notNullValue());
		assertThat(result.getUuid(), equalTo(NAME_ONLY_SNOMED_DAYS_UUID));
	}
	
	@Test
	public void toFhirResource_shouldTranslateConceptCarryingBothTheLegacyAndTheCurrentSnomedCtMinuteCode() {
		Concept concept = conceptMappedSameAsTo(term(snomedCt(), Duration.SNOMED_CT_MINUTES_CODE),
		    term(snomedCt(), CURRENT_SNOMED_CT_MINUTE_CODE));
		
		Timing.UnitsOfTime result = durationUnitTranslator.toFhirResource(concept);
		
		assertThat(result, equalTo(Timing.UnitsOfTime.MIN));
	}
	
	@Test
	public void toFhirResource_shouldPreferSnomedCtWhenAUcumMappingDenotesADifferentUnit() {
		Concept concept = conceptMappedSameAsTo(term(snomedCt(), Duration.SNOMED_CT_MINUTES_CODE), term(ucum(), "h"));
		
		Timing.UnitsOfTime result = durationUnitTranslator.toFhirResource(concept);
		
		assertThat(result, equalTo(Timing.UnitsOfTime.MIN));
	}
	
	@Test
	public void toFhirResource_shouldFallBackToUcumWhenNoSnomedCtMappingCarriesAKnownCode() {
		Concept concept = conceptMappedSameAsTo(term(snomedCt(), "99999999"), term(ucum(), "wk"));
		
		Timing.UnitsOfTime result = durationUnitTranslator.toFhirResource(concept);
		
		assertThat(result, equalTo(Timing.UnitsOfTime.WK));
	}
	
	private static Concept conceptMappedSameAsTo(ConceptReferenceTerm... terms) {
		ConceptMapType sameAs = new ConceptMapType();
		sameAs.setUuid(ConceptMapType.SAME_AS_MAP_TYPE_UUID);
		Concept concept = new Concept();
		for (ConceptReferenceTerm term : terms) {
			concept.addConceptMapping(new ConceptMap(term, sameAs));
		}
		return concept;
	}
	
	private static ConceptReferenceTerm term(ConceptSource source, String code) {
		ConceptReferenceTerm term = new ConceptReferenceTerm();
		term.setConceptSource(source);
		term.setCode(code);
		return term;
	}
	
	private static ConceptSource snomedCt() {
		ConceptSource source = new ConceptSource();
		source.setName("SNOMED CT");
		source.setHl7Code("SCT");
		return source;
	}
	
	private static ConceptSource ucum() {
		ConceptSource source = new ConceptSource();
		source.setName("UCUM");
		return source;
	}
}
