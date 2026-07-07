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

import static lombok.AccessLevel.PROTECTED;

import javax.annotation.Nonnull;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import lombok.Getter;
import lombok.Setter;
import org.hl7.fhir.r4.model.Timing;
import org.openmrs.Concept;
import org.openmrs.ConceptMap;
import org.openmrs.ConceptMapType;
import org.openmrs.ConceptReferenceTerm;
import org.openmrs.ConceptSource;
import org.openmrs.Duration;
import org.openmrs.api.ConceptService;
import org.openmrs.module.fhir2.api.translators.DurationUnitTranslator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class DurationUnitTranslatorImpl implements DurationUnitTranslator {
	
	@Getter(PROTECTED)
	@Setter(value = PROTECTED, onMethod_ = @Autowired)
	ConceptService conceptService;
	
	/**
	 * The SNOMED CT code for minute that is active since the 2021-07-31 release, in which the legacy
	 * code was inactivated. Dictionaries that track current SNOMED CT releases, such as CIEL, map their
	 * minutes concepts to this code. TODO: replace with Duration.SNOMED_CT_MINUTES_CODE_2021 once the
	 * module depends on a platform release that includes TRUNK-6674.
	 */
	private static final String SNOMED_CT_MINUTES_CODE_2021 = "1156209001";
	
	private static final String UCUM_CONCEPT_SOURCE = "UCUM";
	
	// Matched as a fallback for dictionaries that register SNOMED CT by name without setting its HL7
	// code, mirroring UCUM. TODO: replace with Duration.SNOMED_CT_CONCEPT_SOURCE_NAME once the module
	// depends on a platform release that includes TRUNK-6674.
	private static final String SNOMED_CT_CONCEPT_SOURCE_NAME = "SNOMED CT";
	
	private static final Map<String, Timing.UnitsOfTime> SNOMED_CT_CODE_MAP;
	
	private static final Map<String, Timing.UnitsOfTime> UCUM_CODE_MAP;
	
	static {
		// iteration order matters in toOpenmrsType: legacy codes are tried before newer ones so that
		// dictionaries carrying only legacy mappings keep resolving exactly as before
		Map<String, Timing.UnitsOfTime> snomedCtCodes = new LinkedHashMap<>();
		snomedCtCodes.put(Duration.SNOMED_CT_SECONDS_CODE, Timing.UnitsOfTime.S);
		snomedCtCodes.put(Duration.SNOMED_CT_MINUTES_CODE, Timing.UnitsOfTime.MIN);
		snomedCtCodes.put(SNOMED_CT_MINUTES_CODE_2021, Timing.UnitsOfTime.MIN);
		snomedCtCodes.put(Duration.SNOMED_CT_HOURS_CODE, Timing.UnitsOfTime.H);
		snomedCtCodes.put(Duration.SNOMED_CT_DAYS_CODE, Timing.UnitsOfTime.D);
		snomedCtCodes.put(Duration.SNOMED_CT_WEEKS_CODE, Timing.UnitsOfTime.WK);
		snomedCtCodes.put(Duration.SNOMED_CT_MONTHS_CODE, Timing.UnitsOfTime.MO);
		snomedCtCodes.put(Duration.SNOMED_CT_YEARS_CODE, Timing.UnitsOfTime.A);
		SNOMED_CT_CODE_MAP = Collections.unmodifiableMap(snomedCtCodes);
		
		// the UnitsOfTime codes are the UCUM codes for the units of time
		Map<String, Timing.UnitsOfTime> ucumCodes = new LinkedHashMap<>();
		ucumCodes.put(Timing.UnitsOfTime.S.toCode(), Timing.UnitsOfTime.S);
		ucumCodes.put(Timing.UnitsOfTime.MIN.toCode(), Timing.UnitsOfTime.MIN);
		ucumCodes.put(Timing.UnitsOfTime.H.toCode(), Timing.UnitsOfTime.H);
		ucumCodes.put(Timing.UnitsOfTime.D.toCode(), Timing.UnitsOfTime.D);
		ucumCodes.put(Timing.UnitsOfTime.WK.toCode(), Timing.UnitsOfTime.WK);
		ucumCodes.put(Timing.UnitsOfTime.MO.toCode(), Timing.UnitsOfTime.MO);
		ucumCodes.put(Timing.UnitsOfTime.A.toCode(), Timing.UnitsOfTime.A);
		UCUM_CODE_MAP = Collections.unmodifiableMap(ucumCodes);
	}
	
	@Override
	public Timing.UnitsOfTime toFhirResource(@Nonnull Concept concept) {
		for (ConceptMap conceptMapping : concept.getConceptMappings()) {
			if (!ConceptMapType.SAME_AS_MAP_TYPE_UUID.equals(conceptMapping.getConceptMapType().getUuid())) {
				continue;
			}
			ConceptReferenceTerm term = conceptMapping.getConceptReferenceTerm();
			Timing.UnitsOfTime unitsOfTime = findUnitsOfTime(term.getConceptSource(), term.getCode());
			if (unitsOfTime != null) {
				return unitsOfTime;
			}
		}
		return Timing.UnitsOfTime.NULL;
	}
	
	@Override
	public Concept toOpenmrsType(@Nonnull Timing.UnitsOfTime unitsOfTime) {
		for (Map.Entry<String, Timing.UnitsOfTime> entry : SNOMED_CT_CODE_MAP.entrySet()) {
			if (entry.getValue() == unitsOfTime) {
				Concept concept = conceptService.getConceptByMapping(entry.getKey(),
				    Duration.SNOMED_CT_CONCEPT_SOURCE_HL7_CODE);
				if (concept == null) {
					concept = conceptService.getConceptByMapping(entry.getKey(), SNOMED_CT_CONCEPT_SOURCE_NAME);
				}
				if (concept != null) {
					return concept;
				}
			}
		}
		if (UCUM_CODE_MAP.containsKey(unitsOfTime.toCode())) {
			return conceptService.getConceptByMapping(unitsOfTime.toCode(), UCUM_CONCEPT_SOURCE);
		}
		return null;
	}
	
	private static Timing.UnitsOfTime findUnitsOfTime(ConceptSource conceptSource, String code) {
		if (Duration.SNOMED_CT_CONCEPT_SOURCE_HL7_CODE.equals(conceptSource.getHl7Code())
		        || SNOMED_CT_CONCEPT_SOURCE_NAME.equalsIgnoreCase(conceptSource.getName())) {
			return SNOMED_CT_CODE_MAP.get(code);
		}
		if (UCUM_CONCEPT_SOURCE.equalsIgnoreCase(conceptSource.getName())
		        || UCUM_CONCEPT_SOURCE.equals(conceptSource.getHl7Code())) {
			return UCUM_CODE_MAP.get(code);
		}
		return null;
	}
}
