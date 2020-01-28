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

import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import lombok.AccessLevel;
import lombok.Setter;
import org.hibernate.SessionFactory;
import org.openmrs.Encounter;
import org.openmrs.module.fhir2.api.dao.FhirEncounterDao;
import org.springframework.stereotype.Component;

@Component
@Setter(AccessLevel.PACKAGE)
public class FhirEncounterDaoImpl implements FhirEncounterDao {

	@Inject
	@Named("sessionFactory")
	private SessionFactory sessionFactory;

	@Override
	public Encounter getEncounterByUuid(String uuid) {
		return (Encounter) sessionFactory.getCurrentSession().createCriteria(Encounter.class).add(eq("uuid", uuid))
				.uniqueResult();
	}

	@Override
	@SuppressWarnings("unchecked")
	public List<Encounter> findEncountersByPatientIdentifier(String patientIdentifier) {
		return (List<Encounter>) sessionFactory.getCurrentSession().createCriteria(Encounter.class).createAlias("patient", "p")
				.createAlias("p.identifiers", "pi")
				.add(eq("pi.identifier", patientIdentifier))
				.list();
	}
}
