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

import static org.hibernate.criterion.Order.asc;
import static org.hibernate.criterion.Projections.property;
import static org.hibernate.criterion.Restrictions.eq;
import static org.hibernate.criterion.Restrictions.or;
import static org.openmrs.module.fhir2.FhirConstants.TITLE_SEARCH_HANDLER;

import javax.annotation.Nonnull;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import ca.uhn.fhir.rest.param.StringAndListParam;
import com.google.common.annotations.VisibleForTesting;
import lombok.AccessLevel;
import lombok.Setter;
import org.hibernate.Criteria;
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
public class FhirConceptDaoImpl extends BaseFhirDao<Concept> implements FhirConceptDao {
	
	@Setter(value = AccessLevel.PUBLIC, onMethod = @__({ @Autowired, @VisibleForTesting }))
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
		
		Criteria criteria = createConceptMapCriteria(conceptSource, mappingCode);
		criteria.add(or(eq("mapType.uuid", ConceptMapType.SAME_AS_MAP_TYPE_UUID), eq("mapType.name", "SAME-AS")));
		criteria.addOrder(asc("concept.retired"));
		criteria.setResultTransformer(DistinctRootEntityResultTransformer.INSTANCE);
		
		return Optional.ofNullable((Concept) criteria.uniqueResult());
	}
	
	@Override
	public List<Concept> getConceptsWithAnyMappingInSource(@Nonnull ConceptSource conceptSource,
	        @Nonnull String mappingCode) {
		if (conceptSource == null || mappingCode == null) {
			return Collections.emptyList();
		}
		
		Criteria criteria = createConceptMapCriteria(conceptSource, mappingCode);
		criteria.addOrder(asc("concept.retired"));
		criteria.setResultTransformer(DistinctRootEntityResultTransformer.INSTANCE);
		
		return criteria.list();
	}
	
	@Override
	protected void setupSearchParams(Criteria criteria, SearchParameterMap theParams) {
		criteria.add(eq("set", true));
		theParams.getParameters().forEach(entry -> {
			switch (entry.getKey()) {
				case TITLE_SEARCH_HANDLER:
					entry.getValue().forEach(param -> handleTitle(criteria, (StringAndListParam) param.getParam()));
					break;
			}
		});
	}
	
	protected void handleTitle(Criteria criteria, StringAndListParam titlePattern) {
		criteria.createAlias("names", "cn");
		handleAndListParam(titlePattern, (title) -> propertyLike("cn.name", title)).ifPresent(criteria::add);
	}
	
	protected Criteria createConceptMapCriteria(@Nonnull ConceptSource conceptSource, @Nonnull String mappingCode) {
		Criteria criteria = getSessionFactory().getCurrentSession().createCriteria(ConceptMap.class);
		criteria.setProjection(property("concept"));
		criteria.createAlias("conceptReferenceTerm", "term");
		criteria.createAlias("conceptMapType", "mapType");
		criteria.createAlias("concept", "concept");
		
		if (Context.getAdministrationService().isDatabaseStringComparisonCaseSensitive()) {
			criteria.add(eq("term.code", mappingCode).ignoreCase());
		} else {
			criteria.add(eq("term.code", mappingCode));
		}
		
		criteria.add(eq("term.conceptSource", conceptSource));
		return criteria;
	}
}
