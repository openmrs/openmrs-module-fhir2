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

import javax.annotation.Nonnull;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import java.util.Optional;

import lombok.AccessLevel;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.SessionFactory;
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
	
	@Override
	public String getUrlByPatientIdentifierType(PatientIdentifierType patientIdentifierType) {
		EntityManager em = sessionFactory.getCurrentSession();
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<String> cq = cb.createQuery(String.class);
		Root<FhirPatientIdentifierSystem> root = cq.from(FhirPatientIdentifierSystem.class);
		
		cq.where(cb.equal(root.join("patientIdentifierType").get("patientIdentifierTypeId"), patientIdentifierType.getId()));
		cq.select(root.get("url"));
		
		try {
			return em.createQuery(cq).getSingleResult();
		}
		catch (NoResultException e) {
			return null;
		}
	}
	
	@Override
	public PatientIdentifierType getPatientIdentifierTypeByUrl(String url) {
		EntityManager em = sessionFactory.getCurrentSession();
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<PatientIdentifierType> cq = cb.createQuery(PatientIdentifierType.class);
		Root<FhirPatientIdentifierSystem> root = cq.from(FhirPatientIdentifierSystem.class);
		
		cq.where(cb.equal(root.get("url"), url));
		cq.select(root.get("patientIdentifierType"));
		
		try {
			return em.createQuery(cq).getSingleResult();
		}
		catch (NoResultException e) {
			return null;
		}
	}
	
	@Override
	public Optional<FhirPatientIdentifierSystem> getFhirPatientIdentifierSystem(
	        @Nonnull PatientIdentifierType patientIdentifierType) {
		OpenmrsFhirCriteriaContext<FhirPatientIdentifierSystem,FhirPatientIdentifierSystem> criteriaContext = openmrsFhirCriteriaContext();
		
		criteriaContext.getCriteriaQuery().where(criteriaContext.getCriteriaBuilder()
		        .equal(criteriaContext.getRoot().get("patientIdentifierType"), patientIdentifierType));
		return criteriaContext.getEntityManager().createQuery(criteriaContext.getCriteriaQuery()).getResultList().stream()
		        .findFirst();
	}
	
	@Override
	public FhirPatientIdentifierSystem saveFhirPatientIdentifierSystem(
	        @Nonnull FhirPatientIdentifierSystem fhirPatientIdentifierSystem) {
		sessionFactory.getCurrentSession().saveOrUpdate(fhirPatientIdentifierSystem);
		return fhirPatientIdentifierSystem;
	}
	
	private OpenmrsFhirCriteriaContext<FhirPatientIdentifierSystem,FhirPatientIdentifierSystem> openmrsFhirCriteriaContext() {
		EntityManager em = sessionFactory.getCurrentSession();
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<FhirPatientIdentifierSystem> cq = cb.createQuery(FhirPatientIdentifierSystem.class);
		Root<FhirPatientIdentifierSystem> root = cq.from(FhirPatientIdentifierSystem.class);
		
		return new OpenmrsFhirCriteriaContext<>(em, cb, cq, root);
	}
}
