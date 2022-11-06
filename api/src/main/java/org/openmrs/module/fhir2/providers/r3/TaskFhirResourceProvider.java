/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.fhir2.providers.r3;

import static lombok.AccessLevel.PACKAGE;

import javax.annotation.Nonnull;

import java.util.HashSet;

import ca.uhn.fhir.model.api.Include;
import ca.uhn.fhir.rest.annotation.Create;
import ca.uhn.fhir.rest.annotation.Delete;
import ca.uhn.fhir.rest.annotation.IdParam;
import ca.uhn.fhir.rest.annotation.IncludeParam;
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
import org.apache.commons.collections.CollectionUtils;
import org.hl7.fhir.dstu3.model.IdType;
import org.hl7.fhir.dstu3.model.OperationOutcome;
import org.hl7.fhir.dstu3.model.Task;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.openmrs.module.fhir2.api.FhirTaskService;
import org.openmrs.module.fhir2.api.annotations.R3Provider;
import org.openmrs.module.fhir2.api.search.SearchQueryBundleProviderR3Wrapper;
import org.openmrs.module.fhir2.api.search.param.TaskSearchParams;
import org.openmrs.module.fhir2.providers.util.FhirProviderUtils;
import org.openmrs.module.fhir2.providers.util.TaskVersionConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("taskFhirR3ResourceProvider")
@R3Provider
@Setter(PACKAGE)
public class TaskFhirResourceProvider implements IResourceProvider {
	
	@Autowired
	private FhirTaskService fhirTaskService;
	
	@Override
	public Class<? extends IBaseResource> getResourceType() {
		return Task.class;
	}
	
	@Read
	public Task getTaskById(@IdParam @Nonnull IdType id) {
		org.hl7.fhir.r4.model.Task task = fhirTaskService.get(id.getIdPart());
		if (task == null) {
			throw new ResourceNotFoundException("Could not find task with Id " + id.getIdPart());
		}
		
		return TaskVersionConverter.convertTask(task);
	}
	
	@Create
	@SuppressWarnings("unused")
	public MethodOutcome createTask(@ResourceParam Task newTask) {
		return FhirProviderUtils.buildCreate(
		    TaskVersionConverter.convertTask(fhirTaskService.create(TaskVersionConverter.convertTask(newTask))));
	}
	
	@Update
	@SuppressWarnings("unused")
	public MethodOutcome updateTask(@IdParam IdType id, @ResourceParam Task task) {
		return FhirProviderUtils.buildUpdate(TaskVersionConverter
		        .convertTask(fhirTaskService.update(id.getIdPart(), TaskVersionConverter.convertTask(task))));
	}
	
	@Delete
	public OperationOutcome deleteTask(@IdParam IdType id) {
		fhirTaskService.delete(id.getIdPart());
		return FhirProviderUtils.buildDeleteR3();
	}
	
	@Search
	public IBundleProvider searchTasks(
	        @OptionalParam(name = Task.SP_BASED_ON, chainWhitelist = { "" }) ReferenceAndListParam basedOnReference,
	        @OptionalParam(name = Task.SP_OWNER, chainWhitelist = { "" }) ReferenceAndListParam ownerReference,
	        @OptionalParam(name = Task.SP_STATUS) TokenAndListParam status,
	        @OptionalParam(name = Task.SP_RES_ID) TokenAndListParam id,
	        @OptionalParam(name = "_lastUpdated") DateRangeParam lastUpdated, @Sort SortSpec sort,
	        @IncludeParam(allow = { "Task:" + Task.SP_PATIENT, "Task:" + Task.SP_OWNER, "Task:" + Task.SP_BASED_ON,
	                "Task:" + Task.SP_CONTEXT }) HashSet<Include> includes) {
		
		if (CollectionUtils.isEmpty(includes)) {
			includes = null;
		}
		return new SearchQueryBundleProviderR3Wrapper(
		        fhirTaskService.searchForTasks(new TaskSearchParams(basedOnReference, ownerReference, status, id, lastUpdated, sort, includes)));
	}
}
