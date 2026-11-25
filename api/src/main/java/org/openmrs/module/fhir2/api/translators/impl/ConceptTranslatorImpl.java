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
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

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
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class ConceptTranslatorImpl implements ConceptTranslator {
	
	private static final String CACHE_NAME = "fhir2ConceptToCodeableConcept";
	
	private static final String SAME_AS = "SAME-AS";
	
	@Getter(PROTECTED)
	@Setter(value = PROTECTED, onMethod_ = @Autowired)
	private FhirConceptService conceptService;
	
	@Getter(PROTECTED)
	@Setter(value = PROTECTED, onMethod_ = @Autowired)
	private FhirConceptSourceService conceptSourceService;
	
	@Setter(value = PROTECTED, onMethod_ = { @Autowired(required = false) })
	private CacheManager cacheManager;
	
	@Override
	public CodeableConcept toFhirResource(@Nonnull Concept concept) {
		if (concept == null) {
			return null;
		}
		
		Cache cache = cacheManager != null ? cacheManager.getCache(CACHE_NAME) : null;
		if (cache != null) {
			CodeableConcept cached = cache.get(concept, CodeableConcept.class);
			if (cached != null) {
				return cached.copy();
			}
		}
		
		CodeableConcept codeableConcept = buildCodeableConcept(concept);
		
		if (cache != null) {
			cache.put(concept, codeableConcept);
		}
		
		return codeableConcept.copy();
	}
	
	private CodeableConcept buildCodeableConcept(Concept concept) {
		Collection<FhirConceptSource> allFhirConceptSources = conceptSourceService.getFhirConceptSources();
		CodeableConcept codeableConcept = new CodeableConcept();
		codeableConcept.setText(concept.getDisplayString());
		addConceptCoding(codeableConcept.addCoding(), null, concept.getUuid(), concept);
		// map of <systemUrl, <mapType, set of codes>>
		// ie { "http://loinc.org" : { "SAME-AS" : ["108-5"], "NARROWER-THAN": ["108-8", "108-9"] }}
		// Sets (not single codes) so that several mappings with the same map type are not overwritten,
		// and so that duplicate mappings of the same code count once.
		Map<String, Map<String, Set<String>>> systemUrlToCodeMap = new LinkedHashMap<>();
		for (ConceptMap mapping : concept.getConceptMappings()) {
			if (mapping.getConceptMapType() != null) {
				ConceptMapType mapType = mapping.getConceptMapType();
				boolean sameAs = mapType.getUuid() != null && mapType.getUuid().equals(ConceptMapType.SAME_AS_MAP_TYPE_UUID);
				sameAs = sameAs || (mapType.getName() != null && mapType.getName().equalsIgnoreCase(SAME_AS));
				ConceptReferenceTerm crt = mapping.getConceptReferenceTerm();
				String sourceUrl = getSourceUrl(crt.getConceptSource(), allFhirConceptSources);
				if (sourceUrl != null) {
					addSystemToCodeMap(systemUrlToCodeMap, sourceUrl, sameAs ? SAME_AS : mapType.getName(), crt.getCode());
				}
			}
		}
		
		for (Map.Entry<String, Map<String, Set<String>>> entry : systemUrlToCodeMap.entrySet()) {
			String systemUrl = entry.getKey();
			Map<String, Set<String>> mapTypeToCodes = entry.getValue();
			
			// Rule 1: if there are SAME-AS mappings, output all of them (and nothing else) for this source
			Set<String> sameAsCodes = mapTypeToCodes.get(SAME_AS);
			if (sameAsCodes != null && !sameAsCodes.isEmpty()) {
				for (String code : sameAsCodes) {
					addConceptCoding(codeableConcept.addCoding(), systemUrl, code, concept);
				}
				continue;
			}
			
			// Rules 2 and 3: no SAME-AS. Count distinct codes across ALL map types.
			// Exactly one -> output it. More than one -> ambiguous, output nothing for this source.
			Set<String> allCodes = new LinkedHashSet<>();
			mapTypeToCodes.values().forEach(allCodes::addAll);
			if (allCodes.size() == 1) {
				addConceptCoding(codeableConcept.addCoding(), systemUrl, allCodes.iterator().next(), concept);
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
								Map<String, Set<Concept>> mapTypeToConcepts = new LinkedHashMap<>();
								addConceptsToMap(mapTypeToConcepts, allMatchingConcepts, conceptSource.get(),
								    coding.getCode());
								
								// SAME-AS match: return it only if exactly one concept has it, otherwise ambiguous
								Set<Concept> sameAsConcepts = mapTypeToConcepts.get(SAME_AS);
								if (sameAsConcepts != null && !sameAsConcepts.isEmpty()) {
									if (sameAsConcepts.size() == 1) {
										return sameAsConcepts.iterator().next();
									}
									continue;
								}
								
								// no SAME-AS: return the concept only if exactly one distinct concept matches
								Set<Concept> allConcepts = new LinkedHashSet<>();
								mapTypeToConcepts.values().forEach(allConcepts::addAll);
								if (allConcepts.size() == 1) {
									return allConcepts.iterator().next();
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
	
	private void addSystemToCodeMap(Map<String, Map<String, Set<String>>> systemUrlToCodeMap, String systemUrl,
	        String mapType, String code) {
		systemUrlToCodeMap.computeIfAbsent(systemUrl, k -> new LinkedHashMap<>())
		        .computeIfAbsent(mapType, k -> new LinkedHashSet<>()).add(code);
	}
	
	private void addConceptsToMap(Map<String, Set<Concept>> mapTypeToConcepts, List<Concept> allMatchingConcepts,
	        ConceptSource conceptSource, String code) {
		allMatchingConcepts.forEach(concept -> {
			for (ConceptMap mapping : concept.getConceptMappings()) {
				ConceptMapType mapType = mapping.getConceptMapType();
				ConceptReferenceTerm crt = mapping.getConceptReferenceTerm();
				if (crt.getCode().equals(code) && crt.getConceptSource().equals(conceptSource)) {
					boolean sameAs = mapType.getUuid() != null
					        && mapType.getUuid().equals(ConceptMapType.SAME_AS_MAP_TYPE_UUID);
					sameAs = sameAs || (mapType.getName() != null && mapType.getName().equalsIgnoreCase(SAME_AS));
					mapTypeToConcepts.computeIfAbsent(sameAs ? SAME_AS : mapType.getName(), k -> new LinkedHashSet<>())
					        .add(concept);
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
