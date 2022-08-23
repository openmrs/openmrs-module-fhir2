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
import org.hl7.fhir.r4.model.Enumerations;
import org.hl7.fhir.r4.model.ValueSet;
import org.openmrs.Concept;
import org.openmrs.ConceptMap;
import org.openmrs.ConceptName;
import org.openmrs.ConceptReferenceTerm;
import org.openmrs.ConceptSet;
import org.openmrs.ConceptSource;
import org.openmrs.module.fhir2.api.FhirConceptSourceService;
import org.openmrs.module.fhir2.api.translators.ValueSetReferenceTranslator;
import org.openmrs.module.fhir2.api.translators.ValueSetTranslator;
import org.openmrs.module.fhir2.model.FhirConceptSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Setter(AccessLevel.PACKAGE)
public class ValueSetTranslatorImpl implements ValueSetTranslator {
	
	@Autowired
	private FhirConceptSourceService conceptSourceService;
	
	@Autowired
	private ValueSetReferenceTranslator valueSetReferenceTranslator;
	
	@Override
	public ValueSet toFhirResource(@Nonnull Concept concept) {
		if (concept == null) {
			return null;
		}
		
		if (!concept.getSet()) {
			return null;
		}
		
		ValueSet valueSet = new ValueSet();
		valueSet.setId(concept.getUuid());
		valueSet.setTitle(Optional.ofNullable(concept.getName()).map(ConceptName::getName).orElse(""));
		valueSet.setStatus(Enumerations.PublicationStatus.ACTIVE);
		
		valueSet.setDate((concept.getDateChanged() != null) ? concept.getDateChanged() : concept.getDateCreated());
		
		String description = (concept.getDescription() == null || concept.getDescription().getDescription() == null) ? ""
		        : concept.getDescription().getDescription();
		valueSet.setDescription(description);
		
		ValueSet.ValueSetComposeComponent compose = new ValueSet.ValueSetComposeComponent();
		
		for (ConceptSet conceptSet : concept.getConceptSets()) {
			Concept conceptSetMember = conceptSet.getConcept();
			
			if (conceptSetMember != null) {
				if (conceptSetMember.getSet()) {
					ValueSet.ConceptSetComponent conceptSetComponent = new ValueSet.ConceptSetComponent();
					conceptSetComponent
					        .addValueSet(valueSetReferenceTranslator.toFhirResource(conceptSetMember).getReference());
					compose.addInclude(conceptSetComponent);
					continue;
				}
				
				if (conceptSetMember.getConceptMappings() != null) {
					Map<ConceptSource, String> conceptSourceCache = new HashMap<>();
					for (ConceptMap mapping : conceptSetMember.getConceptMappings()) {
						ValueSet.ConceptSetComponent conceptSetComponent = new ValueSet.ConceptSetComponent();
						ValueSet.ConceptReferenceComponent conceptReferenceComponent = new ValueSet.ConceptReferenceComponent();
						
						ConceptReferenceTerm crt = mapping.getConceptReferenceTerm();
						if (crt == null || crt.getConceptSource() == null) {
							continue;
						}
						
						String sourceUrl = conceptSourceCache.computeIfAbsent(crt.getConceptSource(),
						    this::conceptSourceToURL);
						if (sourceUrl == null) {
							conceptReferenceComponent.setCode(conceptSetMember.getUuid());
						} else {
							conceptSetComponent.setSystem(sourceUrl);
							conceptReferenceComponent.setCode(crt.getCode());
						}
						
						conceptSetComponent.addConcept(conceptReferenceComponent);
						compose.addInclude(conceptSetComponent);
					}
				}
			}
		}
		
		valueSet.setCompose(compose);
		return valueSet;
	}
	
	private String conceptSourceToURL(ConceptSource conceptSource) {
		return conceptSourceService.getFhirConceptSource(conceptSource).map(FhirConceptSource::getUrl).orElse(null);
	}
}
