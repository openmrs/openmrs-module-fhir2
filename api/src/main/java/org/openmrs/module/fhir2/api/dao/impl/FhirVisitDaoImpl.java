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
import static org.hl7.fhir.r4.model.Encounter.SP_DATE;

import javax.annotation.Nonnull;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.JoinType;

import java.util.Optional;

import ca.uhn.fhir.rest.param.DateRangeParam;
import ca.uhn.fhir.rest.param.ReferenceAndListParam;
import ca.uhn.fhir.rest.param.TokenAndListParam;
import lombok.AccessLevel;
import lombok.Setter;
import org.hibernate.Criteria;
import org.openmrs.Visit;
import org.openmrs.module.fhir2.api.dao.FhirVisitDao;
import org.springframework.stereotype.Component;

@Component
@Setter(AccessLevel.PACKAGE)
public class FhirVisitDaoImpl extends BaseEncounterDao<Visit> implements FhirVisitDao {
	
	@Override
	protected void handleDate(CriteriaBuilder criteriaBuilder, DateRangeParam dateRangeParam) {
		handleDateRange("startDatetime", dateRangeParam).ifPresent(criteriaBuilder::and);
	}
	
	@Override
	protected void handleEncounterType(CriteriaBuilder criteriaBuilder, TokenAndListParam tokenAndListParam) {
		handleAndListParam(tokenAndListParam, t -> Optional.of(criteriaBuilder.equal(root.get("vt.uuid"), t.getValue())))
				.ifPresent(t -> {
					root.join("visitType", JoinType.INNER).alias("vt");
					criteriaBuilder.and(t);
				});
	}
	
	@Override
	protected void handleParticipant(CriteriaBuilder criteriaBuilder, ReferenceAndListParam referenceAndListParam) {
		root.join("encounters", JoinType.INNER).alias("en");
		root.join("en.encounterProviders", JoinType.INNER).alias("ep");
		handleParticipantReference(criteriaBuilder, referenceAndListParam);
	}
	
	@Override
	protected String paramToProp(@Nonnull String param) {
		switch (param) {
			case SP_DATE:
				return "startDatetime";
			default:
				return null;
		}
	}
}
