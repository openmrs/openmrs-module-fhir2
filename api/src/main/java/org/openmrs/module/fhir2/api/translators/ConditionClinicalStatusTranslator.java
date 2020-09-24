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

import org.hl7.fhir.r4.model.CodeableConcept;

public interface ConditionClinicalStatusTranslator<T> extends ToFhirTranslator<T, CodeableConcept>, ToOpenmrsTranslator<T, CodeableConcept> {
	
	/**
	 * Maps <T> an OpenMRS Generic clinicalStatus element to a
	 * {@link org.hl7.fhir.r4.model.CodeableConcept}
	 *
	 * @param clinicalStatus the OpenMRS generic <T> clinicalStatus to translate
	 * @return the corresponding CodeableConcept FHIR resource
	 */
	@Override
	CodeableConcept toFhirResource(@Nonnull T clinicalStatus);
	
	/**
	 * Maps a {@link org.hl7.fhir.r4.model.CodeableConcept} to an OpenMRS <T> generic type
	 *
	 * @param codeableConcept the FHIR codeableConcept to translate
	 * @return the corresponding OpenMRS <T> generic type
	 */
	@Override
	T toOpenmrsType(@Nonnull CodeableConcept codeableConcept);
}
