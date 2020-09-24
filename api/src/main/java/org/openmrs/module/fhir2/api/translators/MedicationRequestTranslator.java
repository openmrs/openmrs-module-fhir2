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

import org.hl7.fhir.r4.model.MedicationRequest;
import org.openmrs.DrugOrder;

public interface MedicationRequestTranslator extends ToFhirTranslator<DrugOrder, MedicationRequest>, OpenmrsFhirUpdatableTranslator<DrugOrder, MedicationRequest> {
	
	/**
	 * Maps a {@link org.openmrs.DrugOrder} to a {@link org.hl7.fhir.r4.model.MedicationRequest}
	 * resource
	 *
	 * @param drugOrder the OpenMRS drugOrder to translate
	 * @return the corresponding FHIR MedicationRequest resource
	 */
	@Override
	MedicationRequest toFhirResource(@Nonnull DrugOrder drugOrder);
	
	/**
	 * Maps a {@link org.hl7.fhir.r4.model.MedicationRequest} medicationRequest to an existing
	 * {@link org.openmrs.DrugOrder}
	 *
	 * @param existingDrugOrder the existingDrugOrder to update
	 * @param medicationRequest the medicationRequest to map
	 * @return an updated version of the existingDrugOrder
	 */
	@Override
	DrugOrder toOpenmrsType(@Nonnull DrugOrder existingDrugOrder, @Nonnull MedicationRequest medicationRequest);
	
	/**
	 * Maps a {@link org.hl7.fhir.r4.model.MedicationRequest} medicationRequest to an existing
	 * {@link org.openmrs.DrugOrder}
	 *
	 * @param medicationRequest the medicationRequest to map
	 * @return an updated version of the existingDrugOrder
	 */
	@Override
	DrugOrder toOpenmrsType(@Nonnull MedicationRequest medicationRequest);
	
}
