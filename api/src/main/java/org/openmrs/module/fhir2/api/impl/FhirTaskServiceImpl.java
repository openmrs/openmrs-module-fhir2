/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.fhir2.api.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Collectors;

import ca.uhn.fhir.rest.api.SortSpec;
import ca.uhn.fhir.rest.param.ReferenceParam;
import ca.uhn.fhir.rest.param.TokenOrListParam;
import ca.uhn.fhir.rest.server.exceptions.InvalidRequestException;
import ca.uhn.fhir.rest.server.exceptions.MethodNotAllowedException;
import lombok.AccessLevel;
import lombok.Setter;
import org.hl7.fhir.r4.model.DomainResource;
import org.hl7.fhir.r4.model.ServiceRequest;
import org.hl7.fhir.r4.model.Task;
import org.openmrs.module.fhir2.FhirTask;
import org.openmrs.module.fhir2.api.FhirTaskService;
import org.openmrs.module.fhir2.api.dao.FhirTaskDao;
import org.openmrs.module.fhir2.api.translators.TaskTranslator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional
@Setter(AccessLevel.PACKAGE)
public class FhirTaskServiceImpl implements FhirTaskService {
	
	@Autowired
	private FhirTaskDao dao;
	
	@Autowired
	private TaskTranslator translator;
	
	/**
	 * Get task by the UUID
	 * 
	 * @param uuid
	 * @return task with given internal identifier
	 */
	@Override
	@Transactional(readOnly = true)
	public Task getTaskByUuid(String uuid) {
		return translator.toFhirResource(dao.getTaskByUuid(uuid));
	}
	
	/**
	 * Save task to the DB
	 * 
	 * @param task the task to save
	 * @return the saved task
	 */
	@Override
	public Task saveTask(Task task) {
		return translator.toFhirResource(dao.saveTask(translator.toOpenmrsType(task)));
	}
	
	/**
	 * Save task to the DB, or update task if one exists with given UUID
	 * 
	 * @param uuid the uuid of the task to update
	 * @param task the task to save
	 * @return the saved task
	 */
	@Override
	public Task updateTask(String uuid, Task task) {
		if (task.getId() == null) {
			throw new InvalidRequestException("Task resource is missing id.");
		}
		
		if (task.getId() != uuid) {
			throw new InvalidRequestException("Task id and provided uuid do not match");
		}
		
		FhirTask openmrsTask = null;
		
		if (uuid != null) {
			openmrsTask = dao.getTaskByUuid(task.getId());
		}
		
		if (openmrsTask == null) {
			throw new MethodNotAllowedException("No Task found to update. Use Post to create new Tasks.");
		}
		
		return translator.toFhirResource(dao.saveTask(translator.toOpenmrsType(openmrsTask, task)));
	}
	
	/**
	 * Get a list of Tasks associated with the given Resource with the given Uuid through the basedOn
	 * relation
	 *
	 * @param uuid the uuid of the associated resource
	 * @param clazz the class of the associated resource
	 * @return the saved task
	 */
	@Override
	public Collection<Task> getTasksByBasedOn(Class<? extends DomainResource> clazz, String uuid) {
		Collection<Task> associatedTasks = new ArrayList<>();
		
		if (clazz == ServiceRequest.class) {
			associatedTasks = dao.getTasksByBasedOnUuid(clazz, uuid).stream().map(translator::toFhirResource)
			        .collect(Collectors.toList());
		}
		
		return associatedTasks;
	}
	
	/**
	 * Get a list of Tasks that match the given search and sort criteria
	 *
	 * @param basedOnReference A reference to a basedOn resource
	 * @param ownerReference A reference to an owner resource
	 * @param status The list of statuses for requested Tasks
	 * @param sort
	 * @return the saved task
	 */
	@Override
	@Transactional(readOnly = true)
	public Collection<Task> searchForTasks(ReferenceParam basedOnReference, ReferenceParam ownerReference,
	        TokenOrListParam status, SortSpec sort) {
		return dao.searchForTasks(basedOnReference, ownerReference, status, sort).stream().map(translator::toFhirResource)
		        .collect(Collectors.toList());
	}
}
