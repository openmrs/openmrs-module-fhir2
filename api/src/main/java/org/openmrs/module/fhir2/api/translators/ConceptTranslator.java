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

import org.hl7.fhir.r4.model.CodeableConcept;
import org.openmrs.Concept;

public interface ConceptTranslator extends OpenmrsFhirTranslator<Concept, CodeableConcept> {
	
	/**
	 * Maps a {@link Concept} to a {@link CodeableConcept}
	 * 
	 * @param concept the concept to translate
	 * @return the corresponding codeable concept
	 */
	@Override
	CodeableConcept toFhirResource(Concept concept);
	
	/**
	 * Maps a FHIR resource to an OpenMRS data element
	 * 
	 * @param concept the codeable concept to translate
	 * @return the corresponding concept
	 */
	@Override
	Concept toOpenmrsType(CodeableConcept concept);
}
