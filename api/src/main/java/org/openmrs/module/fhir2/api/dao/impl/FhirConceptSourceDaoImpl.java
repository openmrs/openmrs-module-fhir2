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

import javax.annotation.Nonnull;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import lombok.AccessLevel;
import lombok.Setter;
import org.hibernate.Criteria;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Order;
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
	
	@Override
	@Transactional(readOnly = true)
	@SuppressWarnings("unchecked")
	public Collection<FhirConceptSource> getFhirConceptSources() {
		return sessionFactory.getCurrentSession().createCriteria(FhirConceptSource.class).list();
	}
	
	@Override
	public Optional<FhirConceptSource> getFhirConceptSourceByUrl(@Nonnull String url) {
		Criteria criteria = sessionFactory.getCurrentSession().createCriteria(FhirConceptSource.class);
		criteria.add(eq("url", url)).add(eq("retired", false));
		return Optional.ofNullable((FhirConceptSource) criteria.uniqueResult());
	}
	
	@Override
	public Optional<FhirConceptSource> getFhirConceptSourceByConceptSource(@Nonnull ConceptSource conceptSource) {
		Criteria criteria = sessionFactory.getCurrentSession().createCriteria(FhirConceptSource.class);
		criteria.add(eq("conceptSource", conceptSource));
		return Optional.ofNullable((FhirConceptSource) criteria.uniqueResult());
	}
	
	@Override
	public Optional<ConceptSource> getConceptSourceByHl7Code(@Nonnull String hl7Code) {
		Criteria criteria = sessionFactory.getCurrentSession().createCriteria(ConceptSource.class);
		if (Context.getAdministrationService().isDatabaseStringComparisonCaseSensitive()) {
			criteria.add(eq("hl7Code", hl7Code).ignoreCase());
		} else {
			criteria.add(eq("hl7Code", hl7Code));
		}
		criteria.addOrder(Order.asc("retired"));
		
		List<ConceptSource> matchingSources = criteria.list();
		if (matchingSources.isEmpty()) {
			return Optional.empty();
		}
		
		return Optional.ofNullable(matchingSources.get(0));
	}
}
