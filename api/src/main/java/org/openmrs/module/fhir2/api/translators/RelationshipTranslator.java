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
import org.openmrs.RelationshipType;

public interface RelationshipTranslator extends ToFhirTranslator<RelationshipType, CodeableConcept> {
	
	/**
	 * Maps {@link org.openmrs.RelationshipType} to a {@link org.hl7.fhir.r4.model.CodeableConcept}
	 *
	 * @param relationshipType the OpenMRS relationshipType to translate
	 * @return the corresponding FHIR {@link org.hl7.fhir.r4.model.CodeableConcept}
	 */
	@Override
	CodeableConcept toFhirResource(RelationshipType relationshipType);
}
