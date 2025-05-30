/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.fhir2.api.translators;

import javax.annotation.Nonnull;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.hl7.fhir.r4.model.CodeableConcept;
import org.openmrs.Concept;
import org.openmrs.module.fhir2.api.context.ConceptTranslatorContext;
import org.springframework.cache.annotation.Cacheable;

public interface ConceptTranslator extends OpenmrsFhirTranslator<Concept, CodeableConcept> {
	
	/**
	 * Maps a {@link Concept} to a {@link CodeableConcept}
	 * 
	 * @param concept the concept to translate
	 * @return the corresponding codeable concept
	 */
	@Override
	CodeableConcept toFhirResource(@Nonnull Concept concept);
	
	/**
	 * Maps a collection of {@link org.openmrs.Concept}s to a
	 * {@link org.hl7.fhir.r4.model.CodeableConcept}
	 *
	 * @param openmrsConcepts the collection of OpenMRS concepts to translate
	 * @return the mapping of OpenMRS concept to corresponding FHIR concept resource
	 * @since 2.6.0
	 */
	List<CodeableConcept> toFhirResources(Collection<Concept> openmrsConcepts);
	
	/**
	 * Maps a collection of {@link org.openmrs.Concept}s to
	 * {@link org.hl7.fhir.r4.model.CodeableConcept}.
	 *
	 * @param openmrsConcepts the collection of OpenMRS concepts to translate
	 * @return map of OpenMRS concept to corresponding FHIR concept
	 * @since 2.6.0
	 */
	Map<Concept, CodeableConcept> toFhirResourcesMap(Collection<Concept> openmrsConcepts);
	
	/**
	 * Maps a FHIR resource to an OpenMRS data element
	 * 
	 * @param concept the codeable concept to translate
	 * @return the corresponding concept
	 */
	@Override
	Concept toOpenmrsType(@Nonnull CodeableConcept concept);
	
	/**
	 * Creates {@link ConceptTranslatorContext}. Used for cache data to improve performance in
	 * subsequent request calls.
	 *
	 * @return concept translator context
	 */
	@Cacheable
	ConceptTranslatorContext getConceptTranslatorContext();
}
