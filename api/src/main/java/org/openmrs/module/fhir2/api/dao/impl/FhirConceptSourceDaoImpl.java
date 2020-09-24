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
import java.util.Optional;

import lombok.AccessLevel;
import lombok.Setter;
import org.hibernate.SessionFactory;
import org.openmrs.module.fhir2.FhirConceptSource;
import org.openmrs.module.fhir2.api.dao.FhirConceptSourceDao;
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
		return Optional.ofNullable((FhirConceptSource) sessionFactory.getCurrentSession()
		        .createCriteria(FhirConceptSource.class).add(eq("url", url)).add(eq("retired", false)).uniqueResult());
	}
	
	@Override
	public Optional<FhirConceptSource> getFhirConceptSourceByConceptSourceName(@Nonnull String sourceName) {
		return Optional
		        .ofNullable((FhirConceptSource) sessionFactory.getCurrentSession().createCriteria(FhirConceptSource.class)
		                .createAlias("conceptSource", "conceptSource").add(eq("conceptSource.name", sourceName))
		                .add(eq("conceptSource.retired", false)).add(eq("retired", false)).uniqueResult());
	}
}
