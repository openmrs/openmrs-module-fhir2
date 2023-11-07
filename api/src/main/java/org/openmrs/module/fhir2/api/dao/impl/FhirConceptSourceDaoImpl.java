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
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import lombok.AccessLevel;
import lombok.Setter;
import org.hibernate.SessionFactory;
import org.openmrs.ConceptSource;
import org.openmrs.api.context.Context;
import org.openmrs.module.fhir2.api.dao.FhirConceptSourceDao;
import org.openmrs.module.fhir2.model.FhirConceptSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Setter(AccessLevel.PACKAGE)
public class FhirConceptSourceDaoImpl implements FhirConceptSourceDao {
	
	@Autowired
	@Qualifier("sessionFactory")
	private SessionFactory sessionFactory;
	
	private EntityManager entityManager;
	
	private CriteriaBuilder criteriaBuilder;
	
	private CriteriaQuery<FhirConceptSource> criteriaQuery;
	
	private Root<FhirConceptSource> fhirConceptSourceRoot;
	
	private TypedQuery<FhirConceptSource> fhirConceptSourceTypedQuery;
	
	public FhirConceptSourceDaoImpl() {
		entityManager = sessionFactory.getCurrentSession();
		criteriaBuilder = entityManager.getCriteriaBuilder();
		criteriaQuery = criteriaBuilder.createQuery(FhirConceptSource.class);
		fhirConceptSourceRoot = criteriaQuery.from(FhirConceptSource.class);
		fhirConceptSourceTypedQuery = entityManager.createQuery(criteriaQuery);
	}
	
	@Override
	@Transactional(readOnly = true)
	public Collection<FhirConceptSource> getFhirConceptSources() {
		return fhirConceptSourceTypedQuery.getResultList();
	}
	
	@Override
	public Optional<FhirConceptSource> getFhirConceptSourceByUrl(@Nonnull String url) {
		criteriaBuilder.and(criteriaBuilder.equal(fhirConceptSourceRoot.get("url"), url));
		criteriaBuilder.and(criteriaBuilder.equal(fhirConceptSourceRoot.get("retired"), false));
		return Optional.ofNullable(fhirConceptSourceTypedQuery.getSingleResult());
	}
	
	@Override
	public Optional<FhirConceptSource> getFhirConceptSourceByConceptSourceName(@Nonnull String sourceName) {
		fhirConceptSourceRoot.join("conceptSource").alias("conceptSource");
		criteriaQuery.where(criteriaBuilder.and(criteriaBuilder.equal(fhirConceptSourceRoot.get("conceptSource.name"), sourceName),
						criteriaBuilder.equal(fhirConceptSourceRoot.get("conceptSource.retired"), false),criteriaBuilder.equal(fhirConceptSourceRoot.get("retired"), false)));
		fhirConceptSourceTypedQuery = entityManager.createQuery(criteriaQuery);
		return Optional.ofNullable(fhirConceptSourceTypedQuery.getSingleResult());
	}
	
	@Override
	public Optional<FhirConceptSource> getFhirConceptSourceByConceptSource(@Nonnull ConceptSource conceptSource) {
		criteriaBuilder.and(criteriaBuilder.equal(fhirConceptSourceRoot.get("conceptSource"), conceptSource));
		return Optional.ofNullable(fhirConceptSourceTypedQuery.getSingleResult());
	}
	
	@Override
	public Optional<ConceptSource> getConceptSourceByHl7Code(@Nonnull String hl7Code) {
		CriteriaQuery<ConceptSource> criteriaQuery = criteriaBuilder.createQuery(ConceptSource.class);
		TypedQuery<ConceptSource> fhirConceptSourceTypedQuery = entityManager.createQuery(criteriaQuery);
		
		if (Context.getAdministrationService().isDatabaseStringComparisonCaseSensitive()) {
			criteriaBuilder.and(criteriaBuilder.equal(fhirConceptSourceRoot.get("hl7Code".toLowerCase()), hl7Code.toLowerCase()));
		} else {
			criteriaBuilder.and(criteriaBuilder.equal(fhirConceptSourceRoot.get("hl7Code"), hl7Code));
		}
		criteriaQuery.orderBy(criteriaBuilder.asc(fhirConceptSourceRoot.get("retired")));
		
		List<ConceptSource> matchingSources = fhirConceptSourceTypedQuery.getResultList();
		if (matchingSources.isEmpty()) {
			return Optional.empty();
		}
		
		return Optional.ofNullable(matchingSources.get(0));
	}
	
	@Override
	public FhirConceptSource saveFhirConceptSource(@Nonnull FhirConceptSource fhirConceptSource) {
		sessionFactory.getCurrentSession().saveOrUpdate(fhirConceptSource);
		return fhirConceptSource;
	}
}
