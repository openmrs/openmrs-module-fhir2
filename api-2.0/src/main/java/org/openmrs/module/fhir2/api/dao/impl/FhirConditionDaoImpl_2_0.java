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

import java.util.Collection;
import java.util.Date;

import ca.uhn.fhir.rest.api.SortSpec;
import ca.uhn.fhir.rest.param.DateParam;
import ca.uhn.fhir.rest.param.QuantityParam;
import ca.uhn.fhir.rest.param.ReferenceAndListParam;
import ca.uhn.fhir.rest.param.TokenOrListParam;
import lombok.AccessLevel;
import lombok.Setter;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.openmrs.annotation.OpenmrsProfile;
import org.openmrs.module.emrapi.conditionslist.Condition;
import org.openmrs.module.fhir2.api.dao.FhirConditionDao;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

@Primary
@Component
@Setter(AccessLevel.PACKAGE)
@OpenmrsProfile(openmrsPlatformVersion = "2.0.* - 2.1.*")
public class FhirConditionDaoImpl_2_0 implements FhirConditionDao<Condition> {
	
	@Inject
	@Named("sessionFactory")
	private SessionFactory sessionFactory;
	
	@Override
	public Condition getConditionByUuid(String uuid) {
		return (Condition) sessionFactory.getCurrentSession()
		        .createCriteria(org.openmrs.module.emrapi.conditionslist.Condition.class).add(eq("uuid", uuid))
		        .uniqueResult();
	}
	
	@Override
	public Condition saveCondition(Condition condition) {
		Session session = sessionFactory.getCurrentSession();
		Date endDate = condition.getEndDate() != null ? condition.getEndDate() : new Date();
		if (condition.getEndReason() != null) {
			condition.setEndDate(endDate);
		}
		
		Condition existingCondition = getConditionByUuid(condition.getUuid());
		if (condition.equals(existingCondition)) {
			return existingCondition;
		}
		if (existingCondition == null) {
			session.saveOrUpdate(condition);
			return condition;
		}
		
		condition = Condition.newInstance(condition);
		condition.setPreviousCondition(existingCondition);
		
		if (existingCondition.getStatus().equals(condition.getStatus())) {
			existingCondition.setVoided(true);
			session.saveOrUpdate(existingCondition);
			session.saveOrUpdate(condition);
			return condition;
		}
		Date onSetDate = condition.getOnsetDate() != null ? condition.getOnsetDate() : new Date();
		existingCondition.setEndDate(onSetDate);
		session.saveOrUpdate(existingCondition);
		condition.setOnsetDate(onSetDate);
		session.saveOrUpdate(condition);
		
		return condition;
	}
	
	@Override
	public Collection<Condition> searchForConditions(ReferenceAndListParam patientParam, ReferenceAndListParam subjectParam,
	        TokenOrListParam code, TokenOrListParam clinicalStatus, DateParam onsetDate, QuantityParam onsetAge,
	        DateParam recordedDate, SortSpec sort) {
		return null; // TODO
	}
}
