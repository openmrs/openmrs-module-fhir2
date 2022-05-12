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
import static org.hibernate.criterion.Restrictions.or;

import javax.annotation.Nonnull;

import lombok.AccessLevel;
import lombok.Setter;
import org.hibernate.Criteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.transform.DistinctRootEntityResultTransformer;
import org.openmrs.Concept;
import org.openmrs.ConceptMap;
import org.openmrs.ConceptMapType;
import org.openmrs.ConceptSource;
import org.openmrs.api.ConceptService;
import org.openmrs.api.context.Context;
import org.openmrs.module.fhir2.api.dao.FhirConceptDao;
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
	public Concept getConceptWithSameAsMappingInSource(@Nonnull ConceptSource conceptSource, @Nonnull String mappingCode) {
		if (conceptSource == null || mappingCode == null) {
			return null;
		}
		Criteria criteria = getSessionFactory().getCurrentSession().createCriteria(ConceptMap.class);
		criteria.setProjection(Projections.property("concept"));
		criteria.createAlias("conceptReferenceTerm", "term");
		criteria.createAlias("conceptMapType", "mapType");
		criteria.createAlias("concept", "concept");
		if (Context.getAdministrationService().isDatabaseStringComparisonCaseSensitive()) {
			criteria.add(Restrictions.eq("term.code", mappingCode).ignoreCase());
		} else {
			criteria.add(Restrictions.eq("term.code", mappingCode));
		}
		criteria.add(eq("term.conceptSource", conceptSource));
		criteria.add(or(eq("mapType.uuid", ConceptMapType.SAME_AS_MAP_TYPE_UUID), eq("mapType.name", "SAME-AS")));
		criteria.addOrder(Order.asc("concept.retired"));
		criteria.setResultTransformer(DistinctRootEntityResultTransformer.INSTANCE);
		return (Concept) criteria.uniqueResult();
	}
}
