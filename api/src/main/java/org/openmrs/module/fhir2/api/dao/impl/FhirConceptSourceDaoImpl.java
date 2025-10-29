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
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.SessionFactory;
import org.openmrs.ConceptSource;
import org.openmrs.api.context.Context;
import org.openmrs.module.fhir2.api.dao.FhirConceptSourceDao;
import org.openmrs.module.fhir2.api.dao.internals.OpenmrsFhirCriteriaContext;
import org.openmrs.module.fhir2.model.FhirConceptSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class FhirConceptSourceDaoImpl implements FhirConceptSourceDao {
	
	@Getter(value = AccessLevel.PROTECTED)
	@Setter(value = AccessLevel.PROTECTED, onMethod = @__({ @Autowired, @Qualifier("sessionFactory") }))
	private SessionFactory sessionFactory;
	
	@Override
	@Transactional(readOnly = true)
	public Collection<FhirConceptSource> getFhirConceptSources() {
		OpenmrsFhirCriteriaContext<FhirConceptSource, FhirConceptSource> criteriaContext = openmrsFhirCriteriaContext();
		
		criteriaContext.getCriteriaQuery().select(criteriaContext.getRoot());
		return criteriaContext.getEntityManager().createQuery(criteriaContext.getCriteriaQuery()).getResultList();
	}
	
	@Override
	@Transactional(readOnly = true)
	public Optional<FhirConceptSource> getFhirConceptSourceByUrl(@Nonnull String url) {
		OpenmrsFhirCriteriaContext<FhirConceptSource, FhirConceptSource> criteriaContext = openmrsFhirCriteriaContext();
		criteriaContext.getCriteriaQuery().select(criteriaContext.getRoot());
		
		criteriaContext.addPredicate(criteriaContext.getCriteriaBuilder().and(
		    criteriaContext.getCriteriaBuilder().equal(criteriaContext.getRoot().get("url"), url),
		    criteriaContext.getCriteriaBuilder().equal(criteriaContext.getRoot().get("retired"), false)));
		
		try {
			return Optional.ofNullable(
			    criteriaContext.getEntityManager().createQuery(criteriaContext.finalizeQuery()).getSingleResult());
		}
		catch (NoResultException e) {
			return Optional.empty();
		}
	}
	
	@Override
	@Transactional(readOnly = true)
	public Optional<FhirConceptSource> getFhirConceptSourceByConceptSourceName(@Nonnull String sourceName) {
		
		OpenmrsFhirCriteriaContext<FhirConceptSource, FhirConceptSource> criteriaContext = openmrsFhirCriteriaContext();
		criteriaContext.getCriteriaQuery().select(criteriaContext.getRoot());
		
		Join<FhirConceptSource, ConceptSource> conceptSourceJoin = criteriaContext.getRoot().join("conceptSource");
		
		criteriaContext.addPredicate(criteriaContext.getCriteriaBuilder().and(
		    criteriaContext.getCriteriaBuilder().equal(conceptSourceJoin.get("name"), sourceName),
		    criteriaContext.getCriteriaBuilder().equal(conceptSourceJoin.get("retired"), false),
		    criteriaContext.getCriteriaBuilder().equal(criteriaContext.getRoot().get("retired"), false)));
		
		try {
			return Optional.ofNullable(
			    criteriaContext.getEntityManager().createQuery(criteriaContext.finalizeQuery()).getSingleResult());
		}
		catch (NoResultException e) {
			return Optional.empty();
		}
	}
	
	@Override
	@Transactional(readOnly = true)
	public Optional<FhirConceptSource> getFhirConceptSourceByConceptSource(@Nonnull ConceptSource conceptSource) {
		EntityManager em = sessionFactory.getCurrentSession();
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<FhirConceptSource> cq = cb.createQuery(FhirConceptSource.class);
		Root<FhirConceptSource> root = cq.from(FhirConceptSource.class);
		
		List<Predicate> predicates = new ArrayList<>();
		predicates.add(cb.equal(root.get("conceptSource"), conceptSource));
		
		cq.distinct(true).where(predicates.toArray(new Predicate[] {}));
		TypedQuery<FhirConceptSource> query = em.createQuery(cq);
		query.setMaxResults(1);
		
		FhirConceptSource result = query.getResultList().stream().findFirst().orElse(null);
		return Optional.ofNullable(result);
	}
	
	@Override
	@Transactional(readOnly = true)
	public Optional<ConceptSource> getConceptSourceByHl7Code(@Nonnull String hl7Code) {
		EntityManager entityManager = sessionFactory.getCurrentSession();
		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<ConceptSource> criteriaQuery = criteriaBuilder.createQuery(ConceptSource.class);
		Root<ConceptSource> sourceRoot = criteriaQuery.from(ConceptSource.class);
		
		List<Predicate> predicates = new ArrayList<>();
		
		if (Context.getAdministrationService().isDatabaseStringComparisonCaseSensitive()) {
			predicates.add(criteriaBuilder.equal(criteriaBuilder.lower(sourceRoot.get("hl7Code")), hl7Code.toLowerCase()));
		} else {
			predicates.add(criteriaBuilder.equal(sourceRoot.get("hl7Code"), hl7Code));
		}
		
		criteriaQuery.orderBy(criteriaBuilder.asc(sourceRoot.get("retired"))).where(predicates.toArray(new Predicate[] {}));
		
		TypedQuery<ConceptSource> fhirConceptSourceTypedQuery = entityManager.createQuery(criteriaQuery);
		List<ConceptSource> matchingSources = fhirConceptSourceTypedQuery.getResultList();
		if (matchingSources.isEmpty()) {
			return Optional.empty();
		}
		
		return Optional.ofNullable(matchingSources.get(0));
	}
	
	@Override
	@Transactional
	public FhirConceptSource saveFhirConceptSource(@Nonnull FhirConceptSource fhirConceptSource) {
		sessionFactory.getCurrentSession().saveOrUpdate(fhirConceptSource);
		return fhirConceptSource;
	}
	
	private OpenmrsFhirCriteriaContext<FhirConceptSource, FhirConceptSource> openmrsFhirCriteriaContext() {
		EntityManager em = sessionFactory.getCurrentSession();
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<FhirConceptSource> cq = cb.createQuery(FhirConceptSource.class);
		Root<FhirConceptSource> root = cq.from(FhirConceptSource.class);
		
		return new OpenmrsFhirCriteriaContext<>(em, cb, cq, root);
		
	}
}
