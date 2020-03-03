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

import org.hl7.fhir.r4.model.Medication;
import org.openmrs.Drug;

public interface MedicationTranslator extends ToFhirTranslator<Drug, Medication>, UpdatableOpenmrsTranslator<Drug, Medication> {
	
	/**
	 * Maps {@link org.openmrs.Drug} to {@link org.hl7.fhir.r4.model.Medication}
	 *
	 * @param drug the OpenMRS data element to translate
	 * @return the corresponding FHIR Medication resource
	 */
	@Override
	Medication toFhirResource(Drug drug);
	
	/**
	 * Maps {@link org.hl7.fhir.r4.model.Medication} to {@link org.openmrs.Drug}
	 *
	 * @param existingDrug the OpenMRS drug object to update
	 * @param medication the resource to map
	 * @return the updated OpenMrs Drug object
	 */
	@Override
	Drug toOpenmrsType(Drug existingDrug, Medication medication);
}
