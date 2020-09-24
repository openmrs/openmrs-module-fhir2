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

import org.hl7.fhir.r4.model.Task;
import org.openmrs.module.fhir2.FhirTask;

public interface TaskTranslator extends OpenmrsFhirUpdatableTranslator<FhirTask, Task> {
	
	/**
	 * Maps a {@link FhirTask} to a {@link Task}
	 * 
	 * @param openmrsTask the Task to translate
	 * @return the corresponding FHIR Task
	 */
	Task toFhirResource(@Nonnull FhirTask openmrsTask);
	
	/**
	 * Maps a {@link Task} to a {@link FhirTask}
	 * 
	 * @param fhirTask the FHIR Task to map
	 * @return the corresponding OpenMRS Task
	 */
	FhirTask toOpenmrsType(@Nonnull Task fhirTask);
	
	/**
	 * Maps a {@link Task} to an existing {@link FhirTask}
	 * 
	 * @param currentTask the openMRS Task to update
	 * @param fhirTask the FHIR patient to map
	 * @return the updated OpenMRS Task
	 */
	FhirTask toOpenmrsType(@Nonnull FhirTask currentTask, @Nonnull Task fhirTask);
}
