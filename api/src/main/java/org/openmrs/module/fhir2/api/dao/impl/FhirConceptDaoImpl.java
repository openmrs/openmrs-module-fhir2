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
import javax.persistence.criteria.Root;

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
		OpenmrsFhirCriteriaContext<Concept> criteriaContext = openmrsFhirCriteriaContext();
		createConceptMapCriteriaBuilder(conceptSource, mappingCode);
		criteriaContext.addPredicate(criteriaContext.getCriteriaBuilder().or(criteriaContext.getCriteriaBuilder().equal(criteriaContext.getRoot().get("uuid"), ConceptMapType.SAME_AS_MAP_TYPE_UUID),
				criteriaContext.getCriteriaBuilder().equal(criteriaContext.getRoot().get("name"), "SAME-AS")));
		
		criteriaContext.addOrder(criteriaContext.getCriteriaBuilder().asc(criteriaContext.getRoot().get("retired")));
		criteriaContext.finalizeQuery();
		
		return Optional.ofNullable(criteriaContext.getEntityManager().createQuery(criteriaContext.getCriteriaQuery()).getSingleResult());
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
	protected void setupSearchParams(OpenmrsFhirCriteriaContext<Concept> criteriaContext, SearchParameterMap theParams) {
		criteriaContext.getCriteriaBuilder().and(criteriaContext.getCriteriaBuilder().equal(criteriaContext.getRoot().get("set"), true));
		theParams.getParameters().forEach(entry -> {
			switch (entry.getKey()) {
				case TITLE_SEARCH_HANDLER:
					entry.getValue().forEach(param -> handleTitle(criteriaContext, (StringAndListParam) param.getParam()));
					break;
			}
		});
	}
	
	protected void handleTitle(OpenmrsFhirCriteriaContext<Concept> criteriaContext, StringAndListParam titlePattern) {
		criteriaContext.getRoot().join("names").alias("cn");
		handleAndListParam(titlePattern, (title) -> propertyLike("cn.name", title)).ifPresent(criteriaContext::addPredicate);
		criteriaContext.finalizeQuery();
	}
	
	protected void createConceptMapCriteriaBuilder(@Nonnull ConceptSource conceptSource, String mappingCode) {
		openmrsFhirCriteriaContext().getCriteriaQuery().select(openmrsFhirCriteriaContext().getRoot().get("concept"));
		
		Join<ConceptMap, ConceptReferenceTerm> term = openmrsFhirCriteriaContext().getRoot().join("conceptReferenceTerm");
		@SuppressWarnings("unused") Join<ConceptMap, ConceptMapType> mapType = openmrsFhirCriteriaContext().getRoot().join("conceptMapType");
		@SuppressWarnings("unused") Join<ConceptMap, Concept> concept = openmrsFhirCriteriaContext().getRoot().join("concept");
	
		if (Context.getAdministrationService().isDatabaseStringComparisonCaseSensitive()) {
			openmrsFhirCriteriaContext().addPredicate(openmrsFhirCriteriaContext().getCriteriaBuilder().equal(openmrsFhirCriteriaContext().getCriteriaBuilder().lower(term.get("code")), mappingCode.toLowerCase()));
		} else {
			openmrsFhirCriteriaContext().addPredicate(openmrsFhirCriteriaContext().getCriteriaBuilder().equal(term.get("code"), mappingCode));
		}
		
		openmrsFhirCriteriaContext().addPredicate(openmrsFhirCriteriaContext().getCriteriaBuilder().equal(term.get("conceptSource"), conceptSource));
		openmrsFhirCriteriaContext().finalizeQuery();
	}
	
	private OpenmrsFhirCriteriaContext<Concept> openmrsFhirCriteriaContext () {
		EntityManager em = sessionFactory.getCurrentSession();
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<Concept> cq = cb.createQuery(Concept.class);
		Root<Concept> root = cq.from(Concept.class);
		
		return new OpenmrsFhirCriteriaContext<>(em, cb, cq, root);
	}
	
}
