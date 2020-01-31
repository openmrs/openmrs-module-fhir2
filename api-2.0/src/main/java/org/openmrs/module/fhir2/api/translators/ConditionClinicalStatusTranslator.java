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

public interface ConditionClinicalStatusTranslator extends ToOpenmrsTranslator<org.openmrs.module.emrapi.conditionslist.Condition.Status, CodeableConcept>, ToFhirTranslator<org.openmrs.module.emrapi.conditionslist.Condition.Status, CodeableConcept> {
	
	/**
	 * Maps an {@link org.openmrs.module.emrapi.conditionslist.Condition.Status} to a
	 * {@link org.hl7.fhir.r4.model.CodeableConcept} clinicalStatus
	 *
	 * @param status the OpenMRS Condition status to translate
	 * @return the corresponding FHIR Condition clinical status resource
	 */
	@Override
	CodeableConcept toFhirResource(org.openmrs.module.emrapi.conditionslist.Condition.Status status);
	
	/**
	 * Maps a {@link org.hl7.fhir.r4.model.CodeableConcept} clinical status to
	 * {@link org.openmrs.module.emrapi.conditionslist.Condition.Status}
	 *
	 * @param codeableConcept the FHIR codeableConcept to translate
	 * @return the corresponding OpenMRS codeableConcept status
	 */
	@Override
	org.openmrs.module.emrapi.conditionslist.Condition.Status toOpenmrsType(CodeableConcept codeableConcept);
}
