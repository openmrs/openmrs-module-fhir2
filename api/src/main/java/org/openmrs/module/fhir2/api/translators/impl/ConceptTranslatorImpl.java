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

import java.util.concurrent.atomic.AtomicReference;

import lombok.AccessLevel;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Extension;
import org.hl7.fhir.r4.model.StringType;
import org.openmrs.Concept;
import org.openmrs.ConceptMap;
import org.openmrs.ConceptName;
import org.openmrs.ConceptReferenceTerm;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.api.FhirConceptService;
import org.openmrs.module.fhir2.api.FhirConceptSourceService;
import org.openmrs.module.fhir2.api.FhirUserDefaultProperties;
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
	
	@Autowired
	private FhirUserDefaultProperties userDefaultProperties;
	
	@Override
	public CodeableConcept toFhirResource(Concept concept) {
		if (concept == null) {
			return null;
		}
		
		CodeableConcept codeableConcept = new CodeableConcept();
		// TODO fix this so it refers to a specific system
		addConceptCoding(codeableConcept.addCoding(), null, concept.getUuid(), concept);
		
		for (ConceptMap mapping : concept.getConceptMappings()) {
			ConceptReferenceTerm crt = mapping.getConceptReferenceTerm();
			String sourceUrl = conceptSourceToURL(crt.getConceptSource().getName());
			if (sourceUrl == null) {
				continue;
			}
			
			addConceptCoding(codeableConcept.addCoding(), sourceUrl, crt.getCode(), concept);
		}
		
		return codeableConcept;
	}
	
	@Override
	public Concept toOpenmrsType(CodeableConcept concept) {
		if (concept == null) {
			return null;
		}
		
		Concept concept_ = null;
		
		for (Coding coding : concept.getCoding()) {
			if (!coding.hasSystem()) {
				concept_ = coding.getCode() != null ? conceptService.get(coding.getCode()) : null;
				continue;
			}
			
			String codingSource = conceptURLToSource(coding.getSystem());
			if (codingSource == null) {
				continue;
			}
			
			Concept codedConcept = coding.getCode() != null
			        ? conceptService.getConceptBySourceNameAndCode(codingSource, coding.getCode()).orElse(null)
			        : null;
			if (codedConcept != null) {
				if (concept_ == null) {
					concept_ = codedConcept;
				} else if (!concept_.equals(codedConcept) && "LOINC".equals(codingSource)) {
					concept_ = codedConcept;
				}
			}
		}
		
		return concept_;
	}
	
	private void addConceptCoding(Coding coding, String system, String code, Concept concept) {
		coding.setSystem(system);
		coding.setCode(code);
		ConceptName conceptName = concept.getName(userDefaultProperties.getDefaultLocale());
		if (conceptName == null || conceptName.getName() == null) {
			conceptName = concept.getName();
		}
		
		String display = (conceptName == null || conceptName.getName() == null) ? "" : conceptName.getName();
		coding.setDisplay(display);
		
		for (ConceptName name : concept.getNames()) {
			Extension ext = coding.addExtension().setUrl(FhirConstants.FHIR_EXT_TRANSLATIONS);
			ext.addExtension("lang", new StringType(name.getLocale().toLanguageTag()));
			ext.addExtension("content", new StringType(name.getName()));
		}
	}
	
	private String conceptSourceToURL(String conceptSourceName) {
		final AtomicReference<String> url = new AtomicReference<>(null);
		conceptSourceService.getFhirConceptSourceByConceptSourceName(conceptSourceName)
		        .ifPresent(cs -> url.set(cs.getUrl()));
		return url.get();
	}
	
	private String conceptURLToSource(String url) {
		final AtomicReference<String> sourceName = new AtomicReference<>(null);
		conceptSourceService.getFhirConceptSourceByUrl(url).ifPresent(cs -> sourceName.set(cs.getConceptSource().getName()));
		return sourceName.get();
	}
}
