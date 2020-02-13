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

public interface ConditionVerificationStatusTranslator<T> extends ToFhirTranslator<T, CodeableConcept>, ToOpenmrsTranslator<T, CodeableConcept> {
	
	/**
	 * Maps an OpenMRS generic <T> verificationStatus to a {@link org.hl7.fhir.r4.model.CodeableConcept}
	 *
	 * @param verificationStatus the OpenMRS verificationStatus to translate
	 * @return the corresponding FHIR CodeableConcept
	 */
	@Override
	CodeableConcept toFhirResource(T verificationStatus);
	
	/**
	 * Maps a {@link org.hl7.fhir.r4.model.CodeableConcept} to an OpenMRS generic <T> verificationStatus
	 *
	 * @param codeableConcept the FHIR codeableConcept to translate
	 * @return the corresponding OpenMRS generic <T> verificationStatus
	 */
	@Override
	T toOpenmrsType(CodeableConcept codeableConcept);
}
