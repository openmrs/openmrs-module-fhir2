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

import java.util.Optional;

import lombok.AccessLevel;
import lombok.Setter;
import org.hl7.fhir.exceptions.FHIRException;
import org.hl7.fhir.r4.model.MedicationDispense;
import org.openmrs.Concept;
import org.openmrs.ConceptSource;
import org.openmrs.module.fhir2.api.FhirConceptService;
import org.openmrs.module.fhir2.api.FhirConceptSourceService;
import org.openmrs.module.fhir2.api.translators.MedicationDispenseStatusTranslator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Setter(AccessLevel.PACKAGE)
public class MedicationDispenseStatusTranslatorImpl implements MedicationDispenseStatusTranslator {
	
	public static final String CONCEPT_SOURCE_URI = "http://terminology.hl7.org/CodeSystem/medicationdispense-status";
	
	@Autowired
	FhirConceptSourceService conceptSourceService;
	
	@Autowired
	FhirConceptService conceptService;
	
	@Override
	public MedicationDispense.MedicationDispenseStatus toFhirResource(@Nonnull Concept concept) {
		Optional<ConceptSource> conceptSource = conceptSourceService.getConceptSourceByUrl(CONCEPT_SOURCE_URI);
		if (conceptSource.isPresent()) {
			Optional<String> conceptCode = conceptService.getSameAsMappingForConceptInSource(conceptSource.get(), concept);
			if (conceptCode.isPresent()) {
				try {
					return MedicationDispense.MedicationDispenseStatus.fromCode(conceptCode.get());
				}
				catch (FHIRException ignored) {
					// unknown status code
				}
			}
		}
		
		return null;
	}
	
	@Override
	public Concept toOpenmrsType(@Nonnull MedicationDispense.MedicationDispenseStatus status) {
		Optional<Concept> concept = Optional.empty();
		
		if (status != null) {
			Optional<ConceptSource> conceptSource = conceptSourceService.getConceptSourceByUrl(CONCEPT_SOURCE_URI);
			if (conceptSource.isPresent()) {
				concept = conceptService.getConceptWithSameAsMappingInSource(conceptSource.get(), status.toCode());
			}
		}
		
		return concept.orElse(null);
	}
}
