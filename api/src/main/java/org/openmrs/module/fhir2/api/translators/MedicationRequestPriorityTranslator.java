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

import org.hl7.fhir.r4.model.MedicationRequest;
import org.openmrs.DrugOrder;

public interface MedicationRequestPriorityTranslator extends ToFhirTranslator<DrugOrder.Urgency, MedicationRequest.MedicationRequestPriority>, ToOpenmrsTranslator<DrugOrder.Urgency, MedicationRequest.MedicationRequestPriority> {
	
	/**
	 * Maps {@link org.openmrs.DrugOrder.Urgency} to a
	 * {@link org.hl7.fhir.r4.model.MedicationRequest.MedicationRequestPriority}
	 *
	 * @param urgency the OpenMRS drugOrder urgency to translate
	 * @return the corresponding FHIR medicationRequestPriority
	 */
	@Override
	MedicationRequest.MedicationRequestPriority toFhirResource(DrugOrder.Urgency urgency);
	
	/**
	 * Maps a {@link org.hl7.fhir.r4.model.MedicationRequest.MedicationRequestPriority} to an OpenMRS
	 * {@link org.openmrs.DrugOrder.Urgency}
	 *
	 * @param medicationRequestPriority the FHIR medicationRequestPriority to translate
	 * @return the corresponding OpenMRS drugOrder urgency
	 */
	@Override
	DrugOrder.Urgency toOpenmrsType(MedicationRequest.MedicationRequestPriority medicationRequestPriority);
}
