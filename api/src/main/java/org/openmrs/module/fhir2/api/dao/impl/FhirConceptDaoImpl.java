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
import javax.persistence.TypedQuery;
import javax.persistence.criteria.Join;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import ca.uhn.fhir.rest.param.StringAndListParam;
import com.google.common.annotations.VisibleForTesting;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.openmrs.Concept;
import org.openmrs.ConceptMap;
import org.openmrs.ConceptMapType;
import org.openmrs.ConceptSource;
import org.openmrs.api.ConceptService;
import org.openmrs.api.context.Context;
import org.openmrs.module.fhir2.api.dao.FhirConceptDao;
import org.openmrs.module.fhir2.api.dao.internals.OpenmrsFhirCriteriaContext;
import org.openmrs.module.fhir2.api.search.param.SearchParameterMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class FhirConceptDaoImpl extends BaseFhirDao<Concept> implements FhirConceptDao {
	
	@Getter(value = AccessLevel.PROTECTED)
	@Setter(value = AccessLevel.PUBLIC, onMethod = @__({ @Autowired, @VisibleForTesting }))
	private ConceptService conceptService;
	
	@Override
	@Transactional(readOnly = true)
	public Concept get(@Nonnull Integer id) {
		return conceptService.getConcept(id);
	}
	
	@Override
	@Transactional(readOnly = true)
	public Concept get(@Nonnull String uuid) {
		return conceptService.getConceptByUuid(uuid);
	}
	
	@Override
	@Transactional(readOnly = true)
	public Optional<Concept> getConceptWithSameAsMappingInSource(@Nonnull ConceptSource conceptSource,
	        @Nonnull String mappingCode) {
		if (conceptSource == null || mappingCode == null) {
			return Optional.empty();
		}
		
		OpenmrsFhirCriteriaContext<ConceptMap, Concept> criteriaContext = createConceptMapCriteriaBuilder(conceptSource,
		    mappingCode);
		Optional<Join<?, ?>> conceptMapTypeJoin = criteriaContext.getJoin("mapType");
		Optional<Join<?, ?>> conceptJoin = criteriaContext.getJoin("concept");
		if (!conceptMapTypeJoin.isPresent() || !conceptJoin.isPresent()) {
			return Optional.empty();
		}
		
		criteriaContext.addPredicate(criteriaContext.getCriteriaBuilder()
		        .or(criteriaContext.getCriteriaBuilder().equal(conceptMapTypeJoin.get().get("uuid"),
		            ConceptMapType.SAME_AS_MAP_TYPE_UUID),
		            criteriaContext.getCriteriaBuilder().equal(conceptMapTypeJoin.get().get("name"), "SAME-AS")));
		
		criteriaContext.addOrder(criteriaContext.getCriteriaBuilder().asc(conceptJoin.get().get("retired")));
		
		TypedQuery<Concept> query = criteriaContext.getEntityManager().createQuery(criteriaContext.finalizeQuery());
		query.setMaxResults(1);
		
		List<Concept> results = query.getResultList();
		return !results.isEmpty() ? Optional.of(results.get(0)) : Optional.empty();
	}
	
	@Override
	public List<Concept> getConceptsWithAnyMappingInSource(@Nonnull ConceptSource conceptSource,
	        @Nonnull String mappingCode) {
		if (conceptSource == null || mappingCode == null) {
			return Collections.emptyList();
		}
		
		OpenmrsFhirCriteriaContext<ConceptMap, Concept> criteriaContext = createConceptMapCriteriaBuilder(conceptSource,
		    mappingCode);
		Optional<Join<?, ?>> conceptJoin = criteriaContext.getJoin("concept");
		if (!conceptJoin.isPresent()) {
			return Collections.emptyList();
		}
		
		criteriaContext.addOrder(criteriaContext.getCriteriaBuilder().asc(conceptJoin.get().get("retired")));
		criteriaContext.getCriteriaQuery().distinct(true);
		
		return criteriaContext.getEntityManager().createQuery(criteriaContext.finalizeQuery()).getResultList();
	}
	
	@Override
	protected <U> void setupSearchParams(@Nonnull OpenmrsFhirCriteriaContext<Concept, U> criteriaContext,
	        @Nonnull SearchParameterMap theParams) {
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
	
	protected OpenmrsFhirCriteriaContext<ConceptMap, Concept> createConceptMapCriteriaBuilder(
	        @Nonnull ConceptSource conceptSource, String mappingCode) {
		OpenmrsFhirCriteriaContext<ConceptMap, Concept> criteriaContext = createCriteriaContext(ConceptMap.class,
		    Concept.class);
		criteriaContext.addJoin("conceptMapType", "mapType");
		Join<?, ?> conceptJoin = criteriaContext.addJoin("concept", "concept");
		criteriaContext.getCriteriaQuery().select(conceptJoin.as(Concept.class));
		
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
		
		return criteriaContext;
	}
}
