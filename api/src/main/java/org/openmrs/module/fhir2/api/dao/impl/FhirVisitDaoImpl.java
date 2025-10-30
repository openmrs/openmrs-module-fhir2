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

import javax.annotation.Nonnull;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;

import java.util.Optional;

import ca.uhn.fhir.rest.param.DateRangeParam;
import ca.uhn.fhir.rest.param.ReferenceAndListParam;
import ca.uhn.fhir.rest.param.TokenAndListParam;
import org.openmrs.Visit;
import org.openmrs.module.fhir2.api.dao.FhirVisitDao;
import org.openmrs.module.fhir2.api.dao.internals.OpenmrsFhirCriteriaContext;
import org.springframework.stereotype.Component;

@Component
public class FhirVisitDaoImpl extends BaseEncounterDao<Visit> implements FhirVisitDao {
	
	@Override
	protected <U> Optional<Predicate> handleDate(@Nonnull OpenmrsFhirCriteriaContext<Visit, U> criteriaContext,
	        DateRangeParam dateRangeParam) {
		return getSearchQueryHelper().handleDateRange(criteriaContext, "startDatetime", dateRangeParam);
	}
	
	@Override
	protected <U> Optional<Predicate> handleEncounterType(@Nonnull OpenmrsFhirCriteriaContext<Visit, U> criteriaContext,
	        TokenAndListParam tokenAndListParam) {
		Join<?, ?> visitTypeJoin = criteriaContext.addJoin("visitType", "vt");
		return handleAndListParam(criteriaContext.getCriteriaBuilder(), tokenAndListParam,
		    t -> Optional.of(criteriaContext.getCriteriaBuilder().equal(visitTypeJoin.get("uuid"), t.getValue())));
	}
	
	@Override
	protected <U> Optional<Predicate> handleParticipant(OpenmrsFhirCriteriaContext<Visit, U> criteriaContext,
	        ReferenceAndListParam referenceAndListParam) {
		if (referenceAndListParam == null || referenceAndListParam.size() == 0) {
			return Optional.empty();
		}
		
		Join<?, ?> encounterJoin = criteriaContext.addJoin("encounters", "en");
		From<?, ?> epJoin = criteriaContext.addJoin(encounterJoin, "encounterProviders", "ep");
		return getSearchQueryHelper().handleParticipantReference(criteriaContext, referenceAndListParam, epJoin);
	}
	
	@Override
	protected <V, U> Path<?> paramToProp(@Nonnull OpenmrsFhirCriteriaContext<V, U> criteriaContext, @Nonnull String param) {
		switch (param) {
			case SP_DATE:
				return criteriaContext.getRoot().get("startDatetime");
			default:
				return null;
		}
	}
}
