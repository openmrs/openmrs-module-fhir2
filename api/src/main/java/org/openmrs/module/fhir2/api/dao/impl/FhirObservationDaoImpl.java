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

import javax.inject.Inject;
import javax.inject.Named;

import lombok.AccessLevel;
import lombok.Setter;
import org.hibernate.SessionFactory;
import org.openmrs.Obs;
import org.openmrs.module.fhir2.api.dao.FhirObservationDao;
import org.springframework.stereotype.Component;

import java.util.Collection;

@Component
@Setter(AccessLevel.PACKAGE)
public class FhirObservationDaoImpl implements FhirObservationDao {
	
	@Inject
	@Named("sessionFactory")
	SessionFactory sessionFactory;

	@Override
	public Collection<Obs> getAllObs() {
		return sessionFactory.getCurrentSession().createCriteria(Obs.class).list();
	}

	@Override
	public Obs getObsByUuid(String uuid) {
		return (Obs) sessionFactory.getCurrentSession().createCriteria(Obs.class).add(eq("uuid", uuid)).uniqueResult();
	}
}
