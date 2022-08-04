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

import org.hl7.fhir.r4.model.MedicationDispense;
import org.openmrs.Concept;

public interface MedicationDispenseStatusTranslator extends ToFhirTranslator<Concept, MedicationDispense.MedicationDispenseStatus>, ToOpenmrsTranslator<Concept, MedicationDispense.MedicationDispenseStatus> {
	
	@Override
	MedicationDispense.MedicationDispenseStatus toFhirResource(@Nonnull Concept concept);
	
	@Override
	Concept toOpenmrsType(@Nonnull MedicationDispense.MedicationDispenseStatus resource);
}