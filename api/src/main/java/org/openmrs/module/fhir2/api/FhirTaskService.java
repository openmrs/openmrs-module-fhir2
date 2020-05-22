/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.fhir2.api;

import java.util.Collection;

import ca.uhn.fhir.rest.api.SortSpec;
import ca.uhn.fhir.rest.param.ReferenceParam;
import ca.uhn.fhir.rest.param.TokenAndListParam;
import org.hl7.fhir.r4.model.Task;

/**
 * Contains methods pertaining to creating/updating/voiding Tasks
 */
public interface FhirTaskService extends FhirService<Task> {
	
	/**
	 * Get task by the UUID
	 * 
	 * @param uuid
	 * @return task with given internal identifier
	 */
	Task get(String uuid);
	
	/**
	 * Save task to the DB
	 * 
	 * @param task the task to save
	 * @return the saved task
	 */
	Task create(Task task);
	
	/**
	 * Save task to the DB
	 * 
	 * @param task the task to save
	 * @return the saved task
	 */
	Task update(String uuid, Task task);
	
	/**
	 * Get list of tasks that reference the object type/UUID combo provided
	 *
	 * @param basedOnReference
	 * @param ownerReference
	 * @param status
	 * @param sort
	 * @return the collection of Tasks that match the search parameters
	 */
	Collection<Task> searchForTasks(ReferenceParam basedOnReference, ReferenceParam ownerReference, TokenAndListParam status,
	        SortSpec sort);
}
