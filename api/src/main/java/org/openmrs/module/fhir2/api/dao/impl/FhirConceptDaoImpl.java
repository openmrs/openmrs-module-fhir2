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

import static org.openmrs.module.fhir2.FhirConstants.TITLE_SEARCH_HANDLER;

import javax.annotation.Nonnull;
import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import ca.uhn.fhir.rest.param.StringAndListParam;
import lombok.AccessLevel;
import lombok.Setter;
import org.openmrs.Concept;
import org.openmrs.ConceptMap;
import org.openmrs.ConceptMapType;
import org.openmrs.ConceptReferenceTerm;
import org.openmrs.ConceptSource;
import org.openmrs.api.ConceptService;
import org.openmrs.api.context.Context;
import org.openmrs.module.fhir2.api.dao.FhirConceptDao;
import org.openmrs.module.fhir2.api.search.param.SearchParameterMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Setter(AccessLevel.PACKAGE)
public class FhirConceptDaoImpl extends BaseFhirDao<Concept> implements FhirConceptDao {
	
	@Autowired
	private ConceptService conceptService;
	
	@Override
	public Concept get(@Nonnull String uuid) {
		return conceptService.getConceptByUuid(uuid);
	}
	
	@Override
	public Optional<Concept> getConceptWithSameAsMappingInSource(@Nonnull ConceptSource conceptSource,
	        @Nonnull String mappingCode) {
		EntityManager em = sessionFactory.getCurrentSession();
		CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
		CriteriaQuery<Concept> criteriaQuery = criteriaBuilder.createQuery(Concept.class);
		Root<Concept> root = criteriaQuery.from(Concept.class);
		
		criteriaBuilder = createConceptMapCriteriaBuilder(conceptSource, mappingCode);
		
		List<Predicate> predicates = new ArrayList<>();
		predicates.add(criteriaBuilder.or(criteriaBuilder.equal(root.get("uuid"), ConceptMapType.SAME_AS_MAP_TYPE_UUID),
		        criteriaBuilder.equal(root.get("name"), "SAME-AS")));
		
		criteriaQuery.orderBy(criteriaBuilder.asc(root.get("retired"))).distinct(true).where(predicates.toArray(new Predicate[]{}));
		
		return Optional.ofNullable(em.createQuery(criteriaQuery).getSingleResult());
	}
	
	@Override
	public List<Concept> getConceptsWithAnyMappingInSource(@Nonnull ConceptSource conceptSource,
	        @Nonnull String mappingCode) {
		if (conceptSource == null || mappingCode == null) {
			return Collections.emptyList();
		}
		
		EntityManager em = sessionFactory.getCurrentSession();
		CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
		CriteriaQuery<Concept> criteriaQuery = criteriaBuilder.createQuery(Concept.class);
		Root<Concept> root = criteriaQuery.from(Concept.class);
		
		createConceptMapCriteriaBuilder(conceptSource, mappingCode);
		criteriaQuery.orderBy(criteriaBuilder.asc(root.get("retired"))).distinct(true);
		
		return em.createQuery(criteriaQuery).getResultList();
	}
	
	@Override
	protected void setupSearchParams(CriteriaBuilder criteriaBuilder, SearchParameterMap theParams) {
		EntityManager em = sessionFactory.getCurrentSession();
		criteriaBuilder = em.getCriteriaBuilder();
		CriteriaQuery<Concept> criteriaQuery = criteriaBuilder.createQuery(Concept.class);
		Root<Concept> root = criteriaQuery.from(Concept.class);
		
		criteriaBuilder.and(criteriaBuilder.equal(root.get("set"), true));
		CriteriaBuilder finalCriteriaBuilder = criteriaBuilder;
		theParams.getParameters().forEach(entry -> {
			switch (entry.getKey()) {
				case TITLE_SEARCH_HANDLER:
					entry.getValue().forEach(param -> handleTitle(finalCriteriaBuilder, (StringAndListParam) param.getParam()));
					break;
			}
		});
	}
	
	protected void handleTitle(CriteriaBuilder criteriaBuilder, StringAndListParam titlePattern) {
		EntityManager em = sessionFactory.getCurrentSession();
		criteriaBuilder = em.getCriteriaBuilder();
		CriteriaQuery<Concept> criteriaQuery = criteriaBuilder.createQuery(Concept.class);
		Root<Concept> root = criteriaQuery.from(Concept.class);
		
		List<Predicate> predicates = new ArrayList<>();
		root.join("names").alias("cn");
		handleAndListParam(titlePattern, (title) -> propertyLike("cn.name", title)).ifPresent(predicates::add);
		criteriaQuery.where(predicates.toArray(new Predicate[] {}));
	}
	
	protected CriteriaBuilder createConceptMapCriteriaBuilder(@Nonnull ConceptSource conceptSource, String mappingCode) {
		EntityManager em = sessionFactory.getCurrentSession();
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<Concept> cq = cb.createQuery(Concept.class);
		Root<ConceptMap> conceptMapRoot = cq.from(ConceptMap.class);
		
		cq.select(conceptMapRoot.get("concept"));
		
		Join<ConceptMap, ConceptReferenceTerm> term = conceptMapRoot.join("conceptReferenceTerm");
		@SuppressWarnings("unused") Join<ConceptMap, ConceptMapType> mapType = conceptMapRoot.join("conceptMapType");
		@SuppressWarnings("unused") Join<ConceptMap, Concept> concept = conceptMapRoot.join("concept");
		
		List<Predicate> predicates = new ArrayList<>();
		if (Context.getAdministrationService().isDatabaseStringComparisonCaseSensitive()) {
			predicates.add(cb.equal(cb.lower(term.get("code")), mappingCode.toLowerCase()));
		} else {
			predicates.add(cb.equal(term.get("code"), mappingCode));
		}
		
		predicates.add(cb.equal(term.get("conceptSource"), conceptSource));
		
		cq.where(predicates.toArray(new Predicate[]{}));
		
		return cb;
	}
	
}
