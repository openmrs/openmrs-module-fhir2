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
import org.openmrs.Drug;

public interface MedicationReferenceTranslator extends OpenmrsFhirTranslator<Drug, Reference> {
	
	/**
	 * Maps {@link org.openmrs.Drug} to a FHIR {@link org.hl7.fhir.r4.model.Reference}
	 *
	 * @param drug the drug to translate
	 * @return the corresponding FHIR reference
	 */
	@Override
	Reference toFhirResource(@Nonnull Drug drug);
	
	/**
	 * Maps a {@link org.hl7.fhir.r4.model.Reference} to an {@link org.openmrs.Drug}
	 *
	 * @param reference the reference to translate
	 * @return the corresponding medication
	 */
	@Override
	Drug toOpenmrsType(@Nonnull Reference reference);
}
