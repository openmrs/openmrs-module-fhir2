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

import lombok.AccessLevel;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.openmrs.Concept;
import org.openmrs.ConceptMap;
import org.openmrs.ConceptMapType;
import org.openmrs.ConceptReferenceTerm;
import org.openmrs.ConceptSource;
import org.openmrs.Duration;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.api.FhirConceptService;
import org.openmrs.module.fhir2.api.FhirConceptSourceService;
import org.openmrs.module.fhir2.api.translators.ConceptTranslator;
import org.openmrs.module.fhir2.model.FhirConceptSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Setter(AccessLevel.PACKAGE)
public class ConceptTranslatorImpl implements ConceptTranslator {
	
	@Autowired
	private FhirConceptService conceptService;
	
	@Autowired
	private FhirConceptSourceService conceptSourceService;
	
	@Override
	public CodeableConcept toFhirResource(@Nonnull Concept concept) {
		if (concept == null) {
			return null;
		}
		
		CodeableConcept codeableConcept = new CodeableConcept();
		addConceptCoding(codeableConcept.addCoding(), null, concept.getUuid(), concept);
		
		for (ConceptMap mapping : concept.getConceptMappings()) {
			if (mapping.getConceptMapType() == null) {
				continue;
			}
			
			ConceptMapType mapType = mapping.getConceptMapType();
			boolean sameAs = mapType.getUuid() != null && mapType.getUuid().equals(ConceptMapType.SAME_AS_MAP_TYPE_UUID);
			sameAs = sameAs || (mapType.getName() != null && mapType.getName().equalsIgnoreCase("SAME-AS"));
			if (!sameAs) {
				continue;
			}
			
			ConceptReferenceTerm crt = mapping.getConceptReferenceTerm();
			
			String sourceUrl = conceptSourceToURL(crt.getConceptSource());
			if (sourceUrl == null) {
				continue;
			}
			
			addConceptCoding(codeableConcept.addCoding(), sourceUrl, crt.getCode(), concept);
		}
		
		return codeableConcept;
	}
	
	@Override
	public Concept toOpenmrsType(@Nonnull CodeableConcept concept) {
		if (concept == null) {
			return null;
		}
		
		Concept concept_ = null;
		
		for (Coding coding : concept.getCoding()) {
			if (!coding.hasSystem()) {
				concept_ = coding.getCode() != null ? conceptService.get(coding.getCode()) : null;
				continue;
			}
			
			String codingSource = conceptURLToSourceNameOrHl7Code(coding.getSystem());
			if (codingSource == null) {
				continue;
			}
			
			Concept codedConcept = coding.getCode() != null
			        ? conceptService.getConceptBySourceNameAndCode(codingSource, coding.getCode()).orElse(null)
			        : null;
			
			if (codedConcept != null) {
				concept_ = codedConcept;
				break;
			}
		}
		
		return concept_;
	}
	
	private void addConceptCoding(Coding coding, String system, String code, Concept concept) {
		coding.setSystem(system);
		coding.setCode(code);
		coding.setDisplay(concept.getDisplayString());
	}
	
	private String conceptSourceToURL(ConceptSource conceptSource) {
		return conceptSourceService.getFhirConceptSourceByConceptSourceName(conceptSource.getName())
		        .map(FhirConceptSource::getUrl)
		        .orElseGet(() -> Duration.SNOMED_CT_CONCEPT_SOURCE_HL7_CODE.equals(conceptSource.getHl7Code())
		                ? FhirConstants.SNOMED_SYSTEM_URI
		                : null);
	}
	
	private String conceptURLToSourceNameOrHl7Code(String url) {
		return conceptSourceService.getFhirConceptSourceByUrl(url).map(cs -> cs.getConceptSource().getName()).orElseGet(
		    () -> FhirConstants.SNOMED_SYSTEM_URI.equals(url) ? Duration.SNOMED_CT_CONCEPT_SOURCE_HL7_CODE : null);
	}
}
