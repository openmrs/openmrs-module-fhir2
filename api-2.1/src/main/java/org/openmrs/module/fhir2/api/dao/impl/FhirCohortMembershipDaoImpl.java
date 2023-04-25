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

import static org.hibernate.criterion.Projections.projectionList;
import static org.hibernate.criterion.Projections.property;
import static org.hibernate.criterion.Restrictions.eq;
import static org.hibernate.criterion.Restrictions.in;

import java.util.List;
import java.util.stream.Collectors;

import org.hibernate.Criteria;
import org.openmrs.CohortMembership;
import org.openmrs.Patient;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.api.dao.FhirCohortMembershipDao;
import org.openmrs.module.fhir2.api.search.param.SearchParameterMap;
import org.openmrs.module.fhir2.model.IdUuidTuple;
import org.springframework.stereotype.Component;

@Component
public class FhirCohortMembershipDaoImpl extends BaseFhirDao<CohortMembership> implements FhirCohortMembershipDao {
	
	@Override
	protected void setupSearchParams(Criteria criteria, SearchParameterMap theParams) {
		theParams.getParameters().forEach(entry -> {
			if (FhirConstants.GROUP_MEMBERS_SEARCH_HANDLER.equals(entry.getKey())) {
				entry.getValue().forEach(param -> handleGroupMembers(criteria, (String) param.getParam()));
			}
		});
	}
	
	private void handleGroupMembers(Criteria criteria, String groupUuid) {
		if (groupUuid != null) {
			if ((lacksAlias(criteria, "_c"))) {
				criteria.createAlias("cohort", "_c");
			}
			criteria.add(eq("_c.uuid", groupUuid));
		}
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public List<IdUuidTuple> getIdsForUuids(List<String> uuids) {
		List<Object[]> results = getSessionFactory().getCurrentSession().createCriteria(Patient.class).add(in("uuid", uuids))
		        .setProjection(projectionList().add(property("patientId")).add(property("uuid"))).list();
		
		return results.stream().map(r -> new IdUuidTuple((Integer) r[0], (String) r[1])).collect(Collectors.toList());
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public List<IdUuidTuple> getUuidsForIds(List<Integer> ids) {
		List<Object[]> results = getSessionFactory().getCurrentSession().createCriteria(Patient.class)
		        .add(in("patientId", ids)).setProjection(projectionList().add(property("patientId")).add(property("uuid")))
		        .list();
		
		return results.stream().map(r -> new IdUuidTuple((Integer) r[0], (String) r[1])).collect(Collectors.toList());
	}
}
