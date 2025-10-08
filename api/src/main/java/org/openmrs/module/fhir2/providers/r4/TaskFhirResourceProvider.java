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
import static lombok.AccessLevel.PROTECTED;

import java.util.HashSet;

import ca.uhn.fhir.model.api.Include;
import ca.uhn.fhir.rest.annotation.Create;
import ca.uhn.fhir.rest.annotation.Delete;
import ca.uhn.fhir.rest.annotation.IdParam;
import ca.uhn.fhir.rest.annotation.IncludeParam;
import ca.uhn.fhir.rest.annotation.OptionalParam;
import ca.uhn.fhir.rest.annotation.Patch;
import ca.uhn.fhir.rest.annotation.Read;
import ca.uhn.fhir.rest.annotation.ResourceParam;
import ca.uhn.fhir.rest.annotation.Search;
import ca.uhn.fhir.rest.annotation.Sort;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.api.PatchTypeEnum;
import ca.uhn.fhir.rest.api.SortSpec;
import ca.uhn.fhir.rest.api.server.IBundleProvider;
import ca.uhn.fhir.rest.api.server.RequestDetails;
import ca.uhn.fhir.rest.param.DateRangeParam;
import ca.uhn.fhir.rest.param.ReferenceAndListParam;
import ca.uhn.fhir.rest.param.TokenAndListParam;
import ca.uhn.fhir.rest.server.exceptions.InvalidRequestException;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.collections.CollectionUtils;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.OperationOutcome;
import org.hl7.fhir.r4.model.Task;
import org.openmrs.module.fhir2.api.FhirTaskService;
import org.openmrs.module.fhir2.api.annotations.R4Provider;
import org.openmrs.module.fhir2.api.search.param.TaskSearchParams;
import org.openmrs.module.fhir2.providers.util.FhirProviderUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("taskFhirR4ResourceProvider")
@R4Provider
public class TaskFhirResourceProvider extends BaseUpsertFhirResourceProvider<Task> {
	
	@Getter(PROTECTED)
	@Setter(value = PACKAGE, onMethod_ = @Autowired)
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
	
	@Create
	public MethodOutcome createTask(@ResourceParam Task newTask) {
		return FhirProviderUtils.buildCreate(service.create(newTask));
	}
	
	@Override
	protected MethodOutcome doUpsert(IdType id, Task task, RequestDetails requestDetails, boolean createIfNotExists) {
		return FhirProviderUtils.buildUpdate(service.update(id.getIdPart(), task, requestDetails, createIfNotExists));
	}
	
	@Patch
	public MethodOutcome patchTask(@IdParam IdType id, PatchTypeEnum patchType, @ResourceParam String body,
	        RequestDetails requestDetails) {
		if (id == null || id.getIdPart() == null) {
			throw new InvalidRequestException("id must be specified to patch Task resource");
		}
		
		Task task = service.patch(id.getIdPart(), patchType, body, requestDetails);
		
		return FhirProviderUtils.buildPatch(task);
	}
	
	@Delete
	public OperationOutcome deleteTask(@IdParam IdType id) {
		service.delete(id.getIdPart());
		return FhirProviderUtils.buildDeleteR4();
	}
	
	@Search
	public IBundleProvider searchTasks(
	        @OptionalParam(name = Task.SP_BASED_ON, chainWhitelist = { "" }) ReferenceAndListParam basedOnReference,
	        @OptionalParam(name = Task.SP_OWNER, chainWhitelist = { "" }) ReferenceAndListParam ownerReference,
	        @OptionalParam(name = Task.SP_SUBJECT, chainWhitelist = { "" }) ReferenceAndListParam forReference,
	        @OptionalParam(name = Task.SP_CODE) TokenAndListParam taskCode,
	        @OptionalParam(name = Task.SP_STATUS) TokenAndListParam status,
	        @OptionalParam(name = Task.SP_RES_ID) TokenAndListParam id,
	        @OptionalParam(name = "_lastUpdated") DateRangeParam lastUpdated, @Sort SortSpec sort,
	        @IncludeParam(allow = { "Task:" + Task.SP_PATIENT, "Task:" + Task.SP_OWNER, "Task:" + Task.SP_BASED_ON,
	                "Task:" + Task.SP_ENCOUNTER }) HashSet<Include> includes) {
		if (CollectionUtils.isEmpty(includes)) {
			includes = null;
		}
		return service.searchForTasks(new TaskSearchParams(basedOnReference, ownerReference, forReference, taskCode, status,
		        id, lastUpdated, sort, includes));
	}
}
