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

import java.util.Collection;

import ca.uhn.fhir.rest.param.DateRangeParam;
import ca.uhn.fhir.rest.param.ReferenceAndListParam;
import lombok.AccessLevel;
import lombok.Setter;
import org.hibernate.Criteria;
import org.openmrs.Encounter;
import org.openmrs.module.fhir2.api.dao.FhirEncounterDao;
import org.springframework.stereotype.Component;

@Component
@Setter(AccessLevel.PACKAGE)
public class FhirEncounterDaoImpl extends BaseFhirDaoImpl<Encounter> implements FhirEncounterDao {
	
	@Override
	public Encounter get(String uuid) {
		return super.get(uuid);
	}
	
	@Override
	public Collection<Encounter> searchForEncounters(DateRangeParam date, ReferenceAndListParam location,
	        ReferenceAndListParam participant, ReferenceAndListParam subject) {
		
		Criteria criteria = getSessionFactory().getCurrentSession().createCriteria(Encounter.class);
		
		handleDateRange("encounterDatetime", date).ifPresent(criteria::add);
		handleLocationReference("l", location).ifPresent(l -> criteria.createAlias("location", "l").add(l));
		handleParticipantReference(criteria, participant);
		handlePatientReference(criteria, subject);
		
		return criteria.list();
	}
	
}
