/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.fhir2.api.dao.impl;

import static org.hibernate.criterion.Restrictions.eq;

import javax.inject.Inject;
import javax.inject.Named;

import lombok.AccessLevel;
import lombok.Setter;
import org.hibernate.SessionFactory;
import org.openmrs.api.db.DAOException;
import org.openmrs.module.fhir2.Task;
import org.openmrs.module.fhir2.api.dao.FhirTaskDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Collection;

@Component
@Setter(AccessLevel.PACKAGE)
public class FhirTaskDaoImpl implements FhirTaskDao {
	
	private static final Logger log = LoggerFactory.getLogger(FhirTaskDaoImpl.class);
	
	@Inject
	@Named("sessionFactory")
	SessionFactory sessionFactory;

	@Override
	public Collection<Task> getAllTasks() {
		return sessionFactory.getCurrentSession().createCriteria(Task.class).list();
	}

	@Override
	public Task saveTask(Task task) throws DAOException {
		sessionFactory.getCurrentSession().saveOrUpdate(task);
		
		return task;
	}
	
	@Override
	public Task getTaskByUuid(String uuid) {
		return (Task) sessionFactory.getCurrentSession().createCriteria(Task.class).add(eq("uuid", uuid)).uniqueResult();
	}
}
