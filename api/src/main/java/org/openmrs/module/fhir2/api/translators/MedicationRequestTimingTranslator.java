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

import org.hl7.fhir.r4.model.Timing;
import org.openmrs.DrugOrder;

public interface MedicationRequestTimingTranslator extends ToFhirTranslator<DrugOrder, Timing> {
	
	/**
	 * Maps an {@link org.openmrs.DrugOrder} to a {@link org.hl7.fhir.r4.model.Timing} FHIR Type
	 *
	 * @param drugOrder the OpenMRS drugOrder to translate
	 * @return the corresponding FHIR Timing type
	 */
	@Override
	Timing toFhirResource(@Nonnull DrugOrder drugOrder);
}
