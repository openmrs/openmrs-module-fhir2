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

import javax.validation.constraints.NotNull;

import java.util.Collection;
import java.util.Optional;

import ca.uhn.fhir.model.api.annotation.ResourceDef;
import ca.uhn.fhir.rest.api.SortSpec;
import ca.uhn.fhir.rest.param.ReferenceParam;
import ca.uhn.fhir.rest.param.TokenAndListParam;
import lombok.AccessLevel;
import lombok.Setter;
import org.hibernate.Criteria;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;
import org.hl7.fhir.r4.model.DomainResource;
import org.openmrs.api.db.DAOException;
import org.openmrs.module.fhir2.FhirTask;
import org.openmrs.module.fhir2.api.dao.FhirTaskDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
@Setter(AccessLevel.PACKAGE)
public class FhirTaskDaoImpl extends BaseDao implements FhirTaskDao {
	
	@Autowired
	@Qualifier("sessionFactory")
	SessionFactory sessionFactory;
	
	@Override
	public FhirTask saveTask(FhirTask task) throws DAOException {
		// TODO: Refactor - and figure out why CascadeType.ALL does not take care of this.
		if (task.getOwnerReference() != null && task.getOwnerReference().getReference() != null) {
			sessionFactory.getCurrentSession().saveOrUpdate(task.getOwnerReference());
		}
		
		sessionFactory.getCurrentSession().saveOrUpdate(task);
		
		return task;
	}
	
	@Override
	public FhirTask getTaskByUuid(String uuid) {
		return (FhirTask) sessionFactory.getCurrentSession().createCriteria(FhirTask.class).add(eq("uuid", uuid))
		        .uniqueResult();
	}
	
	@Override
	public Collection<FhirTask> getTasksByBasedOnUuid(Class<? extends DomainResource> clazz, String uuid) {
		return (Collection<FhirTask>) sessionFactory.getCurrentSession().createCriteria(FhirTask.class)
		        .createAlias("basedOnReferences", "bo").add(eq("bo.type", clazz.getAnnotation(ResourceDef.class).name()))
		        .add(eq("bo.reference", uuid)).list();
	}
	
	@Override
	public Collection<FhirTask> searchForTasks(ReferenceParam basedOnReference, ReferenceParam ownerReference,
	        TokenAndListParam status, SortSpec sort) {
		
		Criteria criteria = sessionFactory.getCurrentSession().createCriteria(FhirTask.class);
		
		// TODO: Refactor with BaseDaoImpl search support
		// TODO: Handle optional params
		// Task.basedOn
		if (validReferenceParam(basedOnReference)) {
			criteria.createAlias("basedOnReferences", "bo")
			        .add(Restrictions.eq("bo.reference", basedOnReference.getIdPart()))
			        .add(Restrictions.eq("bo.type", basedOnReference.getResourceType()));
		}
		
		// Task.owner
		if (validReferenceParam(ownerReference)) {
			criteria.createAlias("ownerReference", "o").add(Restrictions.eq("o.reference", ownerReference.getIdPart()))
			        .add(Restrictions.eq("o.type", ownerReference.getResourceType()));
		}
		
		// Task.status
		handleStatus(status).ifPresent(criteria::add);
		
		handleSort(criteria, sort);
		
		return criteria.list();
	}
	
	@Override
	protected String paramToProp(@NotNull String paramName) {
		switch (paramName) {
			case "date":
				return "dateChanged";
			default:
				return null;
		}
	}
	
	private Boolean validReferenceParam(ReferenceParam ref) {
		return (ref != null && ref.getIdPart() != null && ref.getResourceType() != null);
	}
	
	private Optional<Criterion> handleStatus(TokenAndListParam tokenAndListParam) {
		if (tokenAndListParam == null) {
			return Optional.empty();
		}
		
		return handleAndListParam(tokenAndListParam, token -> {
			if (token.getValue() != null) {
				return Optional.of(eq("status", FhirTask.TaskStatus.valueOf(token.getValue())));
			}
			
			return Optional.empty();
		});
	}
}
