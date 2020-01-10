package org.openmrs.module.fhir2.api.impl;

import javax.inject.Inject;

import lombok.AccessLevel;
import lombok.Setter;
import org.hl7.fhir.r4.model.Task;
import org.openmrs.module.fhir2.api.FhirTaskService;

import org.openmrs.module.fhir2.api.dao.FhirTaskDao;
import org.openmrs.module.fhir2.api.translators.TaskTranslator;
import org.springframework.stereotype.Component;

@Component
@Setter(AccessLevel.PACKAGE)
public class FhirTaskServiceImpl implements FhirTaskService {
	
	@Inject
	private FhirTaskDao dao;
	
	@Inject
	private TaskTranslator translator;
	
	/**
	 * Get task by the UUID
	 * 
	 * @param uuid
	 * @return task with given internal identifier
	 */
	@Override
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
	
}
