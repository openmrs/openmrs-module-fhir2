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

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import lombok.Getter;
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
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class ConceptTranslatorImpl implements ConceptTranslator {
	
	@Getter(PROTECTED)
	@Setter(value = PROTECTED, onMethod_ = @Autowired)
	private FhirConceptService conceptService;
	
	@Getter(PROTECTED)
	@Setter(value = PROTECTED, onMethod_ = @Autowired)
	private FhirConceptSourceService conceptSourceService;
	
	@Override
	@Cacheable(value = "fhir2ConceptToCodeableConcept")
	public CodeableConcept toFhirResource(@Nonnull Concept concept) {
		if (concept == null) {
			return null;
		}
		
		Collection<FhirConceptSource> allFhirConceptSources = conceptSourceService.getFhirConceptSources();
		CodeableConcept codeableConcept = new CodeableConcept();
		codeableConcept.setText(concept.getDisplayString());
		addConceptCoding(codeableConcept.addCoding(), null, concept.getUuid(), concept);
		//map of <systemUrl ,<mapType , code>> ie { "http://loinc.org” : { "SAME-AS" : "108-5", "NARROWER-THAN": "108-8" }}
		Map<String, Map<String, String>> systemUrlToCodeMap = new HashMap<>();
		for (ConceptMap mapping : concept.getConceptMappings()) {
			if (mapping.getConceptMapType() != null) {
				ConceptMapType mapType = mapping.getConceptMapType();
				boolean sameAs = mapType.getUuid() != null && mapType.getUuid().equals(ConceptMapType.SAME_AS_MAP_TYPE_UUID);
				sameAs = sameAs || (mapType.getName() != null && mapType.getName().equalsIgnoreCase("SAME-AS"));
				ConceptReferenceTerm crt = mapping.getConceptReferenceTerm();
				String sourceUrl = getSourceUrl(crt.getConceptSource(), allFhirConceptSources);
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
							List<Concept> allMatchingConcepts = conceptService
							        .getConceptsWithAnyMappingInSource(conceptSource.get(), coding.getCode());
							if (!allMatchingConcepts.isEmpty()) {
								Map<String, Concept> mapTypeToConceptMap = new HashMap<>();
								addConceptsToMap(mapTypeToConceptMap, allMatchingConcepts, conceptSource.get(),
								    coding.getCode());
								if (mapTypeToConceptMap.size() == 1) {
									for (String mapType : mapTypeToConceptMap.keySet()) {
										return mapTypeToConceptMap.get(mapType);
									}
								} else if (mapTypeToConceptMap.size() > 1 && mapTypeToConceptMap.containsKey("SAME-AS")) {
									return mapTypeToConceptMap.get("SAME-AS");
								}
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
		if (system == null) {
			coding.setDisplay(concept.getDisplayString());
		}
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
	
	private void addConceptsToMap(Map<String, Concept> mapTypeToConceptMap, List<Concept> allMatchingConcepts,
	        ConceptSource conceptSource, String code) {
		allMatchingConcepts.forEach(concept -> {
			for (ConceptMap mapping : concept.getConceptMappings()) {
				ConceptMapType mapType = mapping.getConceptMapType();
				ConceptReferenceTerm crt = mapping.getConceptReferenceTerm();
				if (crt.getCode().equals(code) && crt.getConceptSource().equals(conceptSource)) {
					boolean sameAs = mapType.getUuid() != null
					        && mapType.getUuid().equals(ConceptMapType.SAME_AS_MAP_TYPE_UUID);
					sameAs = sameAs || (mapType.getName() != null && mapType.getName().equalsIgnoreCase("SAME-AS"));
					if (sameAs) {
						mapTypeToConceptMap.put("SAME-AS", concept);
					} else {
						mapTypeToConceptMap.put(mapType.getName(), concept);
					}
				}
			}
		});
	}
	
	private String getSourceUrl(ConceptSource conceptSource, Collection<FhirConceptSource> fhirConceptSources) {
		String sourceUrl = null;
		if (conceptSource != null) {
			FhirConceptSource fhirConceptSource = fhirConceptSources.stream().filter(fcs -> fcs.getConceptSource() != null)
			        .filter(fcs -> fcs.getConceptSource().getUuid().equals(conceptSource.getUuid())).findFirst()
			        .orElse(null);
			
			if (fhirConceptSource != null && fhirConceptSource.getUrl() != null) {
				sourceUrl = fhirConceptSource.getUrl();
			} else {
				sourceUrl = Duration.SNOMED_CT_CONCEPT_SOURCE_HL7_CODE.equals(conceptSource.getHl7Code())
				        ? FhirConstants.SNOMED_SYSTEM_URI
				        : null;
			}
		}
		
		return sourceUrl;
	}
}
