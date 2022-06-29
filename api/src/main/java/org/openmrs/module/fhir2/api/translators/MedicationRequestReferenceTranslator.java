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

import org.hl7.fhir.r4.model.Reference;
import org.openmrs.DrugOrder;

public interface MedicationRequestReferenceTranslator extends OpenmrsFhirTranslator<DrugOrder, Reference> {
	
	/**
	 * Maps an {@link DrugOrder} to a FHIR MedicationRequest reference
	 *
	 * @param drugOrder the encounter to translate
	 * @return the corresponding FHIR reference
	 */
	@Override
	Reference toFhirResource(@Nonnull DrugOrder drugOrder);
	
	/**
	 * Maps a FHIR MedicationRequest reference to an {@link DrugOrder}
	 *
	 * @param medicationRequest the FHIR reference to translate
	 * @return the corresponding OpenMRS {@link DrugOrder}
	 */
	@Override
	DrugOrder toOpenmrsType(@Nonnull Reference medicationRequest);
}
