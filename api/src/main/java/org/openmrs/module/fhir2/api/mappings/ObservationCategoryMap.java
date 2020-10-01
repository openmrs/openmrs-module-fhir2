/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.fhir2.api.mappings;

import static org.hibernate.criterion.Restrictions.eq;

import javax.annotation.Nonnull;

import lombok.extern.slf4j.Slf4j;
import org.hibernate.HibernateException;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Projections;
import org.openmrs.module.fhir2.model.FhirObservationCategoryMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class ObservationCategoryMap {
	
	@Autowired
	@Qualifier("sessionFactory")
	private SessionFactory sessionFactory;
	
	public String getCategory(@Nonnull String conceptClassUuid) {
		try {
			return (String) sessionFactory.getCurrentSession().createCriteria(FhirObservationCategoryMap.class)
			        .createAlias("conceptClass", "cc").add(eq("cc.uuid", conceptClassUuid))
			        .setProjection(Projections.property("observationCategory")).uniqueResult();
		}
		catch (HibernateException e) {
			log.error("Exception caught while trying to load category for concept class '{}'", conceptClassUuid, e);
		}
		
		return null;
	}
	
	public String getConceptClassUuid(@Nonnull String category) {
		try {
			return (String) sessionFactory.getCurrentSession().createCriteria(FhirObservationCategoryMap.class)
			        .createAlias("conceptClass", "cc").add(eq("observationCategory", category))
			        .setProjection(Projections.property("cc.uuid")).uniqueResult();
		}
		catch (HibernateException e) {
			log.error("Exception caught while trying to load concept class for category '{}'", category, e);
		}
		
		return null;
	}
}
