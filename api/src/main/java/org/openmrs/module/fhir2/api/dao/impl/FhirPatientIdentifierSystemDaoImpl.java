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

import javax.annotation.Nonnull;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import java.util.Optional;

import lombok.AccessLevel;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Projections;
import org.openmrs.PatientIdentifierType;
import org.openmrs.module.fhir2.api.dao.FhirPatientIdentifierSystemDao;
import org.openmrs.module.fhir2.model.FhirPatientIdentifierSystem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@Setter(AccessLevel.PUBLIC)
public class FhirPatientIdentifierSystemDaoImpl implements FhirPatientIdentifierSystemDao {
	
	@Autowired
	@Qualifier("sessionFactory")
	private SessionFactory sessionFactory;
	
	// TODO: TASK
	@Override
	public String getUrlByPatientIdentifierType(PatientIdentifierType patientIdentifierType) {
		return (String) sessionFactory.getCurrentSession().createCriteria(FhirPatientIdentifierSystem.class)
		        .add(eq("patientIdentifierType.patientIdentifierTypeId", patientIdentifierType.getId()))
		        .setProjection(Projections.property("url")).uniqueResult();
	}
	
	// TODO: TASK
	@Override
	public PatientIdentifierType getPatientIdentifierTypeByUrl(String url) {
		return (PatientIdentifierType) sessionFactory.getCurrentSession().createCriteria(FhirPatientIdentifierSystem.class)
		        .add(eq("url", url)).setProjection(Projections.property("patientIdentifierType")).uniqueResult();
	}
	
	@Override
	public Optional<FhirPatientIdentifierSystem> getFhirPatientIdentifierSystem(
	        @Nonnull PatientIdentifierType patientIdentifierType) {
		EntityManager em = sessionFactory.getCurrentSession();
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<FhirPatientIdentifierSystem> cq = cb.createQuery(FhirPatientIdentifierSystem.class);
		Root<FhirPatientIdentifierSystem> root = cq.from(FhirPatientIdentifierSystem.class);
		
		cq.where(cb.equal(root.get("patientIdentifierType"), patientIdentifierType));
		
		TypedQuery<FhirPatientIdentifierSystem> query = em.createQuery(cq);
		return query.getResultList().stream().findFirst();
	}
	
	@Override
	public FhirPatientIdentifierSystem saveFhirPatientIdentifierSystem(
	        @Nonnull FhirPatientIdentifierSystem fhirPatientIdentifierSystem) {
		sessionFactory.getCurrentSession().saveOrUpdate(fhirPatientIdentifierSystem);
		return fhirPatientIdentifierSystem;
	}
}
