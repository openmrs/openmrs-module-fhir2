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

import javax.annotation.Nonnull;

import org.hl7.fhir.r4.model.Coding;
import org.openmrs.Concept;
import org.openmrs.ConceptMap;
import org.openmrs.ConceptMapType;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.api.FhirConceptSourceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * This is an implementation of Coding Translator that maps a Medication Quantity Concept to/from a
 * Coding in FHIR with a preferred set of systems and codes to prioritize. This will first favor
 * using RxNorm coding with SAME-AS mapping. If this is not present on the OpenMRS concept, it will
 * next favor SNOMED-CT, also with SAME-AS mapping. Finally, if neither are present, it will favor
 * the Concept UUID with a null system.
 */
@Component
public class MedicationQuantityCodingTranslatorImpl extends BaseCodingTranslator {
	
	@Autowired
	private FhirConceptSourceService conceptSourceService;
	
	@Override
	public Coding toFhirResource(@Nonnull Concept concept) {
		
		Coding coding = null;
		
		if (concept.getConceptMappings() != null && !concept.getConceptMappings().isEmpty()) {
			coding = getSameAsCodingForSystem(concept, FhirConstants.RX_NORM_SYSTEM_URI);
			if (coding == null) {
				coding = getSameAsCodingForSystem(concept, FhirConstants.SNOMED_SYSTEM_URI);
			}
		}
		
		if (coding == null) {
			coding = createConceptCoding(null, concept.getUuid(), concept);
		}
		
		return coding;
	}
	
	private Coding getSameAsCodingForSystem(Concept concept, String system) {
		for (ConceptMap conceptMap : concept.getConceptMappings()) {
			String conceptSourceUrl = conceptSourceService
			        .getUrlForConceptSource(conceptMap.getConceptReferenceTerm().getConceptSource());
			if (conceptSourceUrl != null && conceptSourceUrl.equals(system)
			        && conceptMap.getConceptMapType().getUuid().equals(ConceptMapType.SAME_AS_MAP_TYPE_UUID)) {
				return createConceptCoding(system, conceptMap.getConceptReferenceTerm().getCode(), concept);
			}
		}
		return null;
	}
	
	private Coding createConceptCoding(String system, String code, Concept concept) {
		Coding coding = new Coding();
		coding.setSystem(system);
		coding.setCode(code);
		coding.setDisplay(concept.getDisplayString());
		return coding;
	}
	
	public void setConceptSourceService(FhirConceptSourceService conceptSourceService) {
		this.conceptSourceService = conceptSourceService;
	}
}
