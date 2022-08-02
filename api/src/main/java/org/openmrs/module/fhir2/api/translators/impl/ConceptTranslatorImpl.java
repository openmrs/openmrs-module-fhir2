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

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

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
import org.openmrs.module.fhir2.api.FhirConceptService;
import org.openmrs.module.fhir2.api.FhirConceptSourceService;
import org.openmrs.module.fhir2.api.translators.ConceptTranslator;
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
		codeableConcept.setText(concept.getDisplayString());
		addConceptCoding(codeableConcept.addCoding(), null, concept.getUuid(), concept);
		//map of <systemUrl ,<mapType , code>> ie { "http://loinc.org” : { "SAME-AS" : "108-5", "NAROOWER-THAN": "108-8" }}
		Map<String, Map<String, String>> systemUrlToCodeMap = new HashMap<>();
		for (ConceptMap mapping : concept.getConceptMappings()) {
			if (mapping.getConceptMapType() != null) {
				ConceptMapType mapType = mapping.getConceptMapType();
				boolean sameAs = mapType.getUuid() != null && mapType.getUuid().equals(ConceptMapType.SAME_AS_MAP_TYPE_UUID);
				sameAs = sameAs || (mapType.getName() != null && mapType.getName().equalsIgnoreCase("SAME-AS"));
				ConceptReferenceTerm crt = mapping.getConceptReferenceTerm();
				String sourceUrl = conceptSourceService.getUrlForConceptSource(crt.getConceptSource());
				if (sourceUrl != null) {
					if (sameAs) {
						addSystemToCodeMap(systemUrlToCodeMap, sourceUrl, "SAME-AS", crt.getCode());
					} else {
						addSystemToCodeMap(systemUrlToCodeMap, sourceUrl, mapType.getName(), crt.getCode());
					}
				}
			}
		}
		
		for (String systemUrl : systemUrlToCodeMap.keySet()) {
			Map<String, String> mapTypeToCodeMap = systemUrlToCodeMap.get(systemUrl);
			if (mapTypeToCodeMap != null) {
				if (mapTypeToCodeMap.size() == 1) {
					for (String mapType : mapTypeToCodeMap.keySet()) {
						addConceptCoding(codeableConcept.addCoding(), systemUrl, mapTypeToCodeMap.get(mapType), concept);
					}
				} else if (mapTypeToCodeMap.size() > 1 && mapTypeToCodeMap.containsKey("SAME-AS")) {
					addConceptCoding(codeableConcept.addCoding(), systemUrl, mapTypeToCodeMap.get("SAME-AS"), concept);
				}
			}
		}
		return codeableConcept;
	}
	
	@Override
	public Concept toOpenmrsType(@Nonnull CodeableConcept concept) {
		if (concept != null) {
			for (Coding coding : concept.getCoding()) {
				if (coding.getCode() != null) {
					if (!coding.hasSystem()) {
						Concept c = conceptService.get(coding.getCode());
						if (c != null) {
							return c;
						}
					} else {
						Optional<ConceptSource> conceptSource = conceptSourceService
						        .getConceptSourceByUrl(coding.getSystem());
						if (conceptSource.isPresent()) {
							Optional<Concept> c = conceptService.getConceptWithSameAsMappingInSource(conceptSource.get(),
							    coding.getCode());
							if (c.isPresent()) {
								return c.get();
							}
						}
					}
				}
			}
		}
		
		return null;
	}
	
	private void addConceptCoding(Coding coding, String system, String code, Concept concept) {
		coding.setSystem(system);
		coding.setCode(code);
		coding.setDisplay(concept.getDisplayString());
	}
	
	private void addSystemToCodeMap(Map<String, Map<String, String>> systemUrlToCodeMap, String systemUrl, String mapType,
	        String code) {
		if (systemUrlToCodeMap.containsKey(systemUrl)) {
			systemUrlToCodeMap.get(systemUrl).put(mapType, code);
		} else {
			Map<String, String> mapTypeToCodeMap = new HashMap<>();
			mapTypeToCodeMap.put(mapType, code);
			systemUrlToCodeMap.put(systemUrl, mapTypeToCodeMap);
		}
	}
}
