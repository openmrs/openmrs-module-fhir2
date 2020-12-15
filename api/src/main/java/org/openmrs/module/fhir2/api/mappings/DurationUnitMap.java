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
import org.hibernate.NonUniqueResultException;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Projections;
import org.hl7.fhir.r4.model.Timing;
import org.openmrs.module.fhir2.model.FhirDurationUnitMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class DurationUnitMap {
	
	@Autowired
	@Qualifier("sessionFactory")
	private SessionFactory sessionFactory;
	
	public Timing.UnitsOfTime getDurationUnit(@Nonnull String conceptUuid) {
		
		try {
			return (Timing.UnitsOfTime) sessionFactory.getCurrentSession().createCriteria(FhirDurationUnitMap.class)
			        .createAlias("concept", "c").add(eq("c.uuid", conceptUuid))
			        .setProjection(Projections.property("unit_of_time")).uniqueResult();
		}
		catch (NonUniqueResultException e) {
			log.error("Exception caught while trying to load DurationUnit for concept '{}'", conceptUuid, e);
		}
		return null;
	}
}
