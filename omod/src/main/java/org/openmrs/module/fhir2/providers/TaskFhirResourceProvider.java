/*
  This Source Code Form is subject to the terms of the Mozilla Public License,
  v. 2.0. If a copy of the MPL was not distributed with this file, You can
  obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
  the terms of the Healthcare Disclaimer located at http://openmrs.org/license.

  Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
  graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.fhir2.providers;

import ca.uhn.fhir.rest.annotation.Create;
import ca.uhn.fhir.rest.annotation.IdParam;
import ca.uhn.fhir.rest.annotation.Read;
import ca.uhn.fhir.rest.annotation.ResourceParam;
import ca.uhn.fhir.rest.annotation.Update;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.server.IResourceProvider;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import lombok.AccessLevel;
import lombok.Setter;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.Task;
import org.openmrs.module.fhir2.api.FhirTaskService;
import org.openmrs.module.fhir2.util.MethodOutComeUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import javax.inject.Inject;

@Component
@Qualifier("fhirResources")
@Setter(AccessLevel.PACKAGE)
public class TaskFhirResourceProvider implements IResourceProvider {
	
	@Inject
	private FhirTaskService service;
	
	@Override
	public Class<? extends IBaseResource> getResourceType() {
		return Task.class;
	}
	
	@Read
	public Task getTaskById(@IdParam IdType id) {
		Task task = service.getTaskByUuid(id.getIdPart());
		if (task == null) {
			throw new ResourceNotFoundException("Could not find Task with Id " + id.getIdPart());
		}
		return task;
	}
	
	@Create
	public MethodOutcome createTask(@ResourceParam Task newTask) {
		return MethodOutComeUtils.buildCreate(service.saveTask(newTask));
	}
	
	@Update
	public MethodOutcome updateTask(@IdParam IdType id, @ResourceParam Task task) {
		Task existingTask = service.getTaskByUuid(id.getIdPart());
		
		if (task == null) {
			throw new ResourceNotFoundException("Could not update Task with Id " + id.getIdPart() + ". Task not found.");
		}
		
		// TODO: Figure out how OpenMRS determines whether to create or save
		
		task.setId(existingTask.getId());
		
		return MethodOutComeUtils.buildUpdate(service.saveTask(task));
		
	}
	
}
