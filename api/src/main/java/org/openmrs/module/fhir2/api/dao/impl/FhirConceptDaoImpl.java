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
import javax.persistence.criteria.Join;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import ca.uhn.fhir.rest.param.StringAndListParam;
import lombok.AccessLevel;
import lombok.Setter;
import org.hibernate.transform.DistinctRootEntityResultTransformer;
import org.openmrs.Concept;
import org.openmrs.ConceptMap;
import org.openmrs.ConceptMapType;
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
		if (conceptSource == null || mappingCode == null) {
			return Optional.empty();
		}
		
		OpenmrsFhirCriteriaContext<Concept, Concept> criteriaContext = createCriteriaContext(Concept.class);
		
		createConceptMapCriteriaBuilder(conceptSource, mappingCode);
		Optional<Join<?, ?>> conceptAliasJoin = criteriaContext.getJoin("concept");
		if (!conceptAliasJoin.isPresent()) {
			return Optional.empty();
		}
		Join<?, ?> conceptMapTypeJoin = criteriaContext.addJoin(conceptAliasJoin.get(), "conceptMapType", "mapType");
		
		criteriaContext.addPredicate(criteriaContext.getCriteriaBuilder().or(
		    criteriaContext.getCriteriaBuilder().equal(conceptMapTypeJoin.get("uuid"), ConceptMapType.SAME_AS_MAP_TYPE_UUID),
		    criteriaContext.getCriteriaBuilder().equal(conceptMapTypeJoin.get("name"), "SAME-AS")));
		
		criteriaContext.addOrder(
		    criteriaContext.getCriteriaBuilder().asc(criteriaContext.getRoot().join("concept").get("retired")));
		
		return Optional.ofNullable(
		    criteriaContext.getEntityManager().createQuery(criteriaContext.getCriteriaQuery()).getSingleResult());
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public List<Concept> getConceptsWithAnyMappingInSource(@Nonnull ConceptSource conceptSource,
	        @Nonnull String mappingCode) {
		if (conceptSource == null || mappingCode == null) {
			return Collections.emptyList();
		}
		
		OpenmrsFhirCriteriaContext<ConceptMap, ConceptMap> criteriaContext = createCriteriaContext(ConceptMap.class);
		createConceptMapCriteriaBuilder(conceptSource, mappingCode);
		Join<?, ?> conceptJoin = criteriaContext.addJoin("concept", "concept");
		criteriaContext.addOrder(criteriaContext.getCriteriaBuilder().asc(conceptJoin.get("retired")));
		
		OpenmrsFhirCriteriaContext<Concept, Concept> fhirCriteriaContext = createCriteriaContext(Concept.class);
		
		return fhirCriteriaContext.getEntityManager().createQuery(fhirCriteriaContext.getCriteriaQuery())
		        .unwrap(org.hibernate.query.Query.class).setResultTransformer(DistinctRootEntityResultTransformer.INSTANCE)
		        .getResultList();
	}
	
	@Override
	protected <U> void setupSearchParams(OpenmrsFhirCriteriaContext<Concept, U> criteriaContext,
	        SearchParameterMap theParams) {
		criteriaContext.getCriteriaBuilder()
		        .and(criteriaContext.getCriteriaBuilder().equal(criteriaContext.getRoot().get("set"), true));
		theParams.getParameters().forEach(entry -> {
			switch (entry.getKey()) {
				case TITLE_SEARCH_HANDLER:
					entry.getValue().forEach(param -> handleTitle(criteriaContext, (StringAndListParam) param.getParam()));
					break;
			}
		});
	}
	
	protected <U> void handleTitle(OpenmrsFhirCriteriaContext<Concept, U> criteriaContext, StringAndListParam titlePattern) {
		Join<?, ?> conceptNamesJoin = criteriaContext.addJoin("names", "cn");
		handleAndListParam(criteriaContext.getCriteriaBuilder(), titlePattern,
		    (title) -> propertyLike(criteriaContext, conceptNamesJoin, "name", title))
		            .ifPresent(criteriaContext::addPredicate);
	}
	
	protected <U> void createConceptMapCriteriaBuilder(@Nonnull ConceptSource conceptSource, String mappingCode) {
		OpenmrsFhirCriteriaContext<ConceptMap, ConceptMap> criteriaContext = createCriteriaContext(ConceptMap.class);
		criteriaContext.getCriteriaQuery().select(criteriaContext.getRoot().get("concept"));
		criteriaContext.addJoin("conceptMapType", "mapType");
		criteriaContext.addJoin("concept", "concept");
		
		Join<?, ?> conceptReferenceTermJoin = criteriaContext.addJoin("conceptReferenceTerm", "term");
		if (Context.getAdministrationService().isDatabaseStringComparisonCaseSensitive()) {
			criteriaContext.addPredicate(criteriaContext.getCriteriaBuilder().equal(
			    criteriaContext.getCriteriaBuilder().lower(conceptReferenceTermJoin.get("code")),
			    mappingCode.toLowerCase()));
		} else {
			criteriaContext.addPredicate(
			    criteriaContext.getCriteriaBuilder().equal(conceptReferenceTermJoin.get("code"), mappingCode));
		}
		criteriaContext.addPredicate(
		    criteriaContext.getCriteriaBuilder().equal(conceptReferenceTermJoin.get("conceptSource"), conceptSource));
	}
}
