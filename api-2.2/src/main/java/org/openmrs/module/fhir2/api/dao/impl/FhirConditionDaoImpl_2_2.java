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

import java.util.Collection;
import java.util.Date;
import java.util.Optional;

import ca.uhn.fhir.rest.api.SortSpec;
import ca.uhn.fhir.rest.param.DateRangeParam;
import ca.uhn.fhir.rest.param.QuantityParam;
import ca.uhn.fhir.rest.param.ReferenceAndListParam;
import ca.uhn.fhir.rest.param.TokenAndListParam;
import lombok.AccessLevel;
import lombok.Setter;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.openmrs.Condition;
import org.openmrs.ConditionClinicalStatus;
import org.openmrs.annotation.OpenmrsProfile;
import org.openmrs.module.fhir2.api.dao.FhirConditionDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

@Primary
@Component
@Setter(AccessLevel.PACKAGE)
@OpenmrsProfile(openmrsPlatformVersion = "2.2.* - 2.*")
public class FhirConditionDaoImpl_2_2 extends BaseDaoImpl implements FhirConditionDao<Condition> {
	// TODO: Change the BaseDaoImpl inheritance pattern to one of composition; here and everywhere else.
	
	@Autowired
	@Qualifier("sessionFactory")
	private SessionFactory sessionFactory;
	
	@Override
	public Condition getConditionByUuid(String uuid) {
		return (Condition) sessionFactory.getCurrentSession().createCriteria(Condition.class).add(eq("uuid", uuid))
		        .uniqueResult();
	}
	
	private ConditionClinicalStatus convertStatus(String status) {
		if ("active".equalsIgnoreCase(status)) {
			return ConditionClinicalStatus.ACTIVE;
		}
		if ("inactive".equalsIgnoreCase(status)) {
			return ConditionClinicalStatus.INACTIVE;
		}
		// Note `history_of` is not a valid value in the FHIR spec:
		// http://www.hl7.org/fhir/valueset-condition-clinical.html
		// We are simply following the logic implemented in `ConditionClinicalStatusTranslatorImpl_2_2`.
		return ConditionClinicalStatus.HISTORY_OF;
	}
	
	@Override
	public Collection<Condition> searchForConditions(ReferenceAndListParam patientParam, ReferenceAndListParam subjectParam,
	        TokenAndListParam code, TokenAndListParam clinicalStatus, DateRangeParam onsetDate, QuantityParam onsetAge,
	        DateRangeParam recordedData, SortSpec sort) {
		Criteria criteria = sessionFactory.getCurrentSession().createCriteria(Condition.class);
		
		handlePatientReference(criteria, patientParam);
		if (patientParam == null) {
			handlePatientReference(criteria, subjectParam);
		}
		handleDateRange("onsetDate", onsetDate).ifPresent(criteria::add);
		// TODO: Handle onsetAge as well.
		handleDateRange("dateCreated", recordedData).ifPresent(criteria::add);
		handleAndListParam(clinicalStatus,
		    tokenParam -> Optional.of(eq("clinicalStatus", convertStatus(tokenParam.getValue())))).ifPresent(criteria::add);
		if (code != null) {
			criteria.createAlias("condition.coded", "cd");
			handleCodeableConcept(criteria, code, "cd", "map", "term").ifPresent(criteria::add);
		}
		
		handleSort(criteria, sort);
		
		return criteria.list();
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
		condition.setPreviousVersion(existingCondition);
		
		if (existingCondition.getClinicalStatus().equals(condition.getClinicalStatus())) {
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
}
