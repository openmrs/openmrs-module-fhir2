/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.fhir2.providers.r4;

import static lombok.AccessLevel.PACKAGE;

import java.util.List;

import ca.uhn.fhir.rest.annotation.Create;
import ca.uhn.fhir.rest.annotation.Delete;
import ca.uhn.fhir.rest.annotation.History;
import ca.uhn.fhir.rest.annotation.IdParam;
import ca.uhn.fhir.rest.annotation.OptionalParam;
import ca.uhn.fhir.rest.annotation.Read;
import ca.uhn.fhir.rest.annotation.ResourceParam;
import ca.uhn.fhir.rest.annotation.Search;
import ca.uhn.fhir.rest.annotation.Sort;
import ca.uhn.fhir.rest.annotation.Update;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.api.SortSpec;
import ca.uhn.fhir.rest.api.server.IBundleProvider;
import ca.uhn.fhir.rest.param.DateRangeParam;
import ca.uhn.fhir.rest.param.ReferenceAndListParam;
import ca.uhn.fhir.rest.param.TokenAndListParam;
import ca.uhn.fhir.rest.server.IResourceProvider;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import lombok.Setter;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.OperationOutcome;
import org.hl7.fhir.r4.model.Resource;
import org.hl7.fhir.r4.model.Task;
import org.openmrs.module.fhir2.api.FhirTaskService;
import org.openmrs.module.fhir2.api.annotations.R4Provider;
import org.openmrs.module.fhir2.providers.util.FhirProviderUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("taskFhirR4ResourceProvider")
@R4Provider
@Setter(PACKAGE)
public class TaskFhirResourceProvider implements IResourceProvider {
	
	@Autowired
	private FhirTaskService service;
	
	@Override
	public Class<? extends IBaseResource> getResourceType() {
		return Task.class;
	}
	
	@Read
	public Task getTaskById(@IdParam IdType id) {
		Task task = service.get(id.getIdPart());
		if (task == null) {
			throw new ResourceNotFoundException("Could not find Task with Id " + id.getIdPart());
		}
		return task;
	}
	
	@History
	public List<Resource> getTaskHistoryById(@IdParam IdType id) {
		Task task = service.get(id.getIdPart());
		if (task == null) {
			throw new ResourceNotFoundException("Could not find Task with Id " + id.getIdPart());
		}
		return task.getContained();
	}
	
	@Create
	public MethodOutcome createTask(@ResourceParam Task newTask) {
		return FhirProviderUtils.buildCreate(service.create(newTask));
	}
	
	@Update
	public MethodOutcome updateTask(@IdParam IdType id, @ResourceParam Task task) {
		return FhirProviderUtils.buildUpdate(service.update(id.getIdPart(), task));
	}
	
	@Delete
	public OperationOutcome deleteTask(@IdParam IdType id) {
		Task task = service.delete(id.getIdPart());
		if (task == null) {
			throw new ResourceNotFoundException("Could not find task resource with id " + id.getIdPart() + "to delete");
		}
		return FhirProviderUtils.buildDelete(task);
	}
	
	@Search
	public IBundleProvider searchTasks(
	        @OptionalParam(name = Task.SP_BASED_ON, chainWhitelist = { "" }) ReferenceAndListParam basedOnReference,
	        @OptionalParam(name = Task.SP_OWNER, chainWhitelist = { "" }) ReferenceAndListParam ownerReference,
	        @OptionalParam(name = Task.SP_STATUS) TokenAndListParam status,
	        @OptionalParam(name = Task.SP_RES_ID) TokenAndListParam id,
	        @OptionalParam(name = "_lastUpdated") DateRangeParam lastUpdated, @Sort SortSpec sort) {
		return service.searchForTasks(basedOnReference, ownerReference, status, id, lastUpdated, sort);
	}
}
