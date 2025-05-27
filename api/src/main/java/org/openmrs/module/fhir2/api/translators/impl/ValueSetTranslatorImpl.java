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
import static org.openmrs.module.fhir2.api.translators.impl.FhirTranslatorUtils.getLastUpdated;
import static org.openmrs.module.fhir2.api.translators.impl.FhirTranslatorUtils.getVersionId;

import javax.annotation.Nonnull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import lombok.Getter;
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
public class ValueSetTranslatorImpl implements ValueSetTranslator {
	
	@Getter(PROTECTED)
	@Setter(value = PROTECTED, onMethod_ = @Autowired)
	private FhirConceptSourceService conceptSourceService;
	
	@Getter(PROTECTED)
	@Setter(value = PROTECTED, onMethod_ = @Autowired)
	private ValueSetReferenceTranslator valueSetReferenceTranslator;
	
	@Override
	public Concept toOpenmrsType(@Nonnull ValueSet resource) {
		return null;
	}
	
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
		
		Map<String, ValueSet.ConceptSetComponent> sets = new HashMap<>();
		ValueSet.ConceptSetComponent conceptUuidSet = new ValueSet.ConceptSetComponent();
		sets.put("conceptUuid", conceptUuidSet);
		
		Map<ConceptSource, String> conceptSourceCache = new HashMap<>();
		
		for (ConceptSet conceptSet : concept.getConceptSets()) {
			Concept conceptSetMember = conceptSet.getConcept();
			
			if (conceptSetMember != null) {
				// if it's a set, just add a reference to the concept uuid set
				if (conceptSetMember.getSet()) {
					conceptUuidSet.addValueSet(valueSetReferenceTranslator.toFhirResource(conceptSetMember).getReference());
				} else {
					// first, add the uuid and display to the concept uuid set
					ValueSet.ConceptReferenceComponent uuidConceptReferenceComponent = new ValueSet.ConceptReferenceComponent();
					uuidConceptReferenceComponent.setCode(conceptSetMember.getUuid());
					uuidConceptReferenceComponent.setDisplay(conceptSetMember.getDisplayString());
					conceptUuidSet.addConcept(uuidConceptReferenceComponent);
					
					// now iterate through all the mappings
					if (conceptSetMember.getConceptMappings() != null) {
						for (ConceptMap conceptMapping : conceptSetMember.getConceptMappings()) {
							ConceptReferenceTerm crt = conceptMapping.getConceptReferenceTerm();
							String sourceUrl = conceptSourceCache.computeIfAbsent(crt.getConceptSource(),
							    this::conceptSourceToURL);
							// only add sources that we have urls for (provided by Fhir Concept Source table)
							if (sourceUrl != null) {
								ValueSet.ConceptSetComponent conceptSetComponent;
								if (sets.containsKey(sourceUrl)) {
									conceptSetComponent = sets.get(sourceUrl);
								} else {
									conceptSetComponent = new ValueSet.ConceptSetComponent();
									conceptSetComponent.setSystem(sourceUrl);
									sets.put(sourceUrl, conceptSetComponent);
								}
								// set the code and the display string
								ValueSet.ConceptReferenceComponent conceptReferenceComponent = new ValueSet.ConceptReferenceComponent();
								conceptReferenceComponent.setCode(crt.getCode());
								conceptReferenceComponent.setDisplay(conceptSetMember.getDisplayString());
								conceptSetComponent.addConcept(conceptReferenceComponent);
								
							}
						}
					}
				}
			}
		}
		
		compose.setInclude(new ArrayList<>(sets.values()));
		valueSet.setCompose(compose);
		
		valueSet.getMeta().setLastUpdated(getLastUpdated(concept));
		valueSet.getMeta().setVersionId(getVersionId(concept));
		return valueSet;
	}
	
	private String conceptSourceToURL(ConceptSource conceptSource) {
		return conceptSourceService.getFhirConceptSource(conceptSource).map(FhirConceptSource::getUrl).orElse(null);
	}
}
