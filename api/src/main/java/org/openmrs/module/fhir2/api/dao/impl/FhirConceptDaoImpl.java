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
import javax.persistence.criteria.CriteriaBuilder;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import ca.uhn.fhir.rest.param.StringAndListParam;
import lombok.AccessLevel;
import lombok.Setter;
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
		
		criteriaBuilder = createConceptMapCriteriaBuilder(conceptSource, mappingCode);
		criteriaBuilder.and(criteriaBuilder.or(criteriaBuilder.equal(root.get("mapType.uuid"), ConceptMapType.SAME_AS_MAP_TYPE_UUID),
				criteriaBuilder.equal(root.get("mapType.name"), "SAME-AS")));
		criteriaQuery.orderBy(criteriaBuilder.asc(root.get("concept.retired"))).distinct(true);
		
		return Optional.ofNullable(manager.createQuery(criteriaQuery).getSingleResult());
	}
	
	@Override
	public List<Concept> getConceptsWithAnyMappingInSource(@Nonnull ConceptSource conceptSource,
	        @Nonnull String mappingCode) {
		if (conceptSource == null || mappingCode == null) {
			return Collections.emptyList();
		}
		
		createConceptMapCriteriaBuilder(conceptSource, mappingCode);
		criteriaQuery.orderBy(criteriaBuilder.asc(root.get("concept.retired"))).distinct(true);
		
		return manager.createQuery(criteriaQuery).getResultList();
	}
	
	@Override
	protected void setupSearchParams(CriteriaBuilder criteriaBuilder, SearchParameterMap theParams) {
		criteriaBuilder.and(criteriaBuilder.equal(root.get("set"), true));
		theParams.getParameters().forEach(entry -> {
			switch (entry.getKey()) {
				case TITLE_SEARCH_HANDLER:
					entry.getValue().forEach(param -> handleTitle(criteriaBuilder, (StringAndListParam) param.getParam()));
					break;
			}
		});
	}
	
	protected void handleTitle(CriteriaBuilder criteriaBuilder, StringAndListParam titlePattern) {
		root.join("names").alias("cn");
		handleAndListParam(titlePattern, (title) -> propertyLike("cn.name", title)).ifPresent(criteriaBuilder::and);
	}
	
	protected CriteriaBuilder createConceptMapCriteriaBuilder(@Nonnull ConceptSource conceptSource, @Nonnull String mappingCode) {
		criteriaQuery.select(root.get("concept"));
		root.join("conceptReferenceTerm").alias("term");
		root.join("conceptMapType").alias("mapType");
		root.join("concept").alias("concept");
		
		if (Context.getAdministrationService().isDatabaseStringComparisonCaseSensitive()) {
			criteriaBuilder.and(criteriaBuilder.equal(root.get("term.code".toLowerCase()), mappingCode.toLowerCase()));
		} else {
			criteriaBuilder.and(criteriaBuilder.equal(root.get("term.code"), mappingCode));
		}
		
		criteriaBuilder.and(criteriaBuilder.equal(root.get("term.conceptSource"), conceptSource));
		return criteriaBuilder;
	}
}
