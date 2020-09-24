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
import org.hl7.fhir.r4.model.Task;
import org.openmrs.module.fhir2.model.FhirReference;

public interface ReferenceTranslator extends OpenmrsFhirUpdatableTranslator<FhirReference, Reference> {
	
	/**
	 * Maps a {@link FhirReference} to a {@link Reference}
	 * 
	 * @param openmrsReference the Task to translate
	 * @return the corresponding FHIR Task
	 */
	Reference toFhirResource(@Nonnull FhirReference openmrsReference);
	
	/**
	 * Maps a {@link Reference} to a {@link FhirReference}
	 * 
	 * @param fhirReference the FHIR Task to map
	 * @return the corresponding OpenMRS Task
	 */
	FhirReference toOpenmrsType(@Nonnull Reference fhirReference);
	
	/**
	 * Maps a {@link Task} to an existing {@link FhirReference}
	 * 
	 * @param currentReference the openMRS Task to update
	 * @param fhirReference the FHIR patient to map
	 * @return the updated OpenMRS Task
	 */
	FhirReference toOpenmrsType(@Nonnull FhirReference currentReference, @Nonnull Reference fhirReference);
}
