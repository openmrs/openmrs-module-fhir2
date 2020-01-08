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

/**
 * This class should not be used directly. <br>
 * The FhiTaskService should be used for all Task-related database manipulation.
 * 
 * @see org.openmrs.module.fhir2.api.FhirTaskService
 * @see org.openmrs.module.fhir2.api.dao.FhirTaskDao
 */

@Component
@Setter(AccessLevel.PACKAGE)
public class FhirTaskDaoImpl implements FhirTaskDao {
	
	private static final Logger log = LoggerFactory.getLogger(FhirTaskDaoImpl.class);
	
	@Inject
	@Named("sessionFactory")
	SessionFactory sessionFactory;
	
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
