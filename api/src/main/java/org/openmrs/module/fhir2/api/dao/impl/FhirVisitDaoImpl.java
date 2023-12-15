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

import static org.hl7.fhir.r4.model.Encounter.SP_DATE;

import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;

import java.util.Optional;

import ca.uhn.fhir.rest.param.DateRangeParam;
import ca.uhn.fhir.rest.param.ReferenceAndListParam;
import ca.uhn.fhir.rest.param.TokenAndListParam;
import lombok.AccessLevel;
import lombok.NonNull;
import lombok.Setter;
import org.openmrs.Encounter;
import org.openmrs.Visit;
import org.openmrs.module.fhir2.api.dao.FhirVisitDao;
import org.springframework.stereotype.Component;

@Component
@Setter(AccessLevel.PACKAGE)
public class FhirVisitDaoImpl extends BaseEncounterDao<Visit> implements FhirVisitDao {
	
	@Override
	protected void handleDate(OpenmrsFhirCriteriaContext<Visit> criteriaContext, DateRangeParam dateRangeParam) {
		handleDateRange(criteriaContext, "startDatetime", dateRangeParam);
		criteriaContext.finalizeQuery();
	}
	
	@Override
	protected void handleEncounterType(OpenmrsFhirCriteriaContext<Visit> criteriaContext,
	        TokenAndListParam tokenAndListParam) {
		handleAndListParam(tokenAndListParam,
		    t -> Optional
		            .of(criteriaContext.getCriteriaBuilder().equal(criteriaContext.getRoot().get("vt.uuid"), t.getValue())))
		                    .ifPresent(t -> {
			                    criteriaContext.getRoot().join("visitType");
			                    criteriaContext.addPredicate(t);
			                    criteriaContext.finalizeQuery();
		                    });
	}
	
	@Override
	protected void handleParticipant(OpenmrsFhirCriteriaContext<Visit> criteriaContext,
	        ReferenceAndListParam referenceAndListParam) {
		Join<Visit, Encounter> encounterJoin = criteriaContext.getRoot().join("encounters", JoinType.INNER);
		encounterJoin.join("encounterProviders", JoinType.INNER);
		handleParticipantReference(criteriaContext, referenceAndListParam);
	}
	
	@Override
	protected <V> String paramToProp(OpenmrsFhirCriteriaContext<V> criteriaContext, @NonNull String param) {
		switch (param) {
			case SP_DATE:
				return "startDatetime";
			default:
				return null;
		}
	}
}
