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

import javax.inject.Inject;

import java.util.Collection;
import java.util.stream.Collectors;

import lombok.AccessLevel;
import lombok.Setter;
import org.hl7.fhir.r4.model.Task;
import org.openmrs.module.fhir2.api.FhirTaskService;
import org.openmrs.module.fhir2.api.dao.FhirTaskDao;
import org.openmrs.module.fhir2.api.translators.TaskTranslator;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional
@Setter(AccessLevel.PACKAGE)
public class FhirTaskServiceImpl implements FhirTaskService {
	
	@Inject
	private FhirTaskDao dao;
	
	@Inject
	private TaskTranslator translator;

	@Override
	@Transactional(readOnly = true)
	public Collection<Task> getAllTasks() {
		return dao.getAllTasks().stream()
				.map(translator::toFhirResource)
				.collect(Collectors.toList());
	}

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
		org.openmrs.module.fhir2.Task openmrsTask = null;
		
		if (uuid != null) {
			openmrsTask = dao.getTaskByUuid(task.getId());
		}
		
		return translator.toFhirResource(dao.saveTask(translator.toOpenmrsType(openmrsTask, task)));
	}
	
}
