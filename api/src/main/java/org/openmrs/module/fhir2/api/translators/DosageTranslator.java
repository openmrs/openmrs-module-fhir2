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

import org.hl7.fhir.r4.model.Dosage;
import org.openmrs.DrugOrder;

public interface DosageTranslator extends ToFhirTranslator<DrugOrder, Dosage>, UpdatableOpenmrsTranslator<DrugOrder, Dosage> {
	
	/**
	 * Maps an {@link DrugOrder} to a {@link Dosage}
	 *
	 * @param drugOrder the OpenMRS drugOrder to translate
	 * @return the corresponding Dosage FHIR resource
	 */
	@Override
	Dosage toFhirResource(@Nonnull DrugOrder drugOrder);
	
	/**
	 * Populates a {@link DrugOrder} with dosing information from a {@link Dosage}
	 *
	 * @param drugOrder the OpenMRS drugOrder to update
	 * @param dosage the Dosage FHIR resource that the drugOrder should be updated with
	 * @return the updated {@link DrugOrder}
	 */
	@Override
	DrugOrder toOpenmrsType(@Nonnull DrugOrder drugOrder, @Nonnull Dosage dosage);
}
