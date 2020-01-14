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

import org.hl7.fhir.r4.model.Task;

public interface TaskTranslator extends OpenmrsFhirUpdatableTranslator<org.openmrs.module.fhir2.Task, Task> {
	
	/**
	 * Maps a {@link org.openmrs.module.fhir2.Task} to a {@link Task}
	 * 
	 * @param openmrsTask the Task to translate
	 * @return the corresponding FHIR Task
	 */
	Task toFhirResource(org.openmrs.module.fhir2.Task openmrsTask);
	
	/**
	 * Maps a {@link Task} to a {@link org.openmrs.module.fhir2.Task}
	 * 
	 * @param fhirTask the FHIR Task to map
	 * @return the corresponding OpenMRS Task
	 */
	org.openmrs.module.fhir2.Task toOpenmrsType(Task fhirTask);
	
	/**
	 * Maps a {@link Task} to an existing {@link org.openmrs.module.fhir2.Task}
	 * 
	 * @param currentTask the openMRS Task to update
	 * @param fhirTask the FHIR patient to map
	 * @return the updated OpenMRS Task
	 */
	org.openmrs.module.fhir2.Task toOpenmrsType(org.openmrs.module.fhir2.Task currentTask, Task fhirTask);
}
