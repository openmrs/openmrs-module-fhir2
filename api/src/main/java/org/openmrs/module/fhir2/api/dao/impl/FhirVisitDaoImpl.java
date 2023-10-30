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
import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import ca.uhn.fhir.rest.param.DateRangeParam;
import ca.uhn.fhir.rest.param.ReferenceAndListParam;
import ca.uhn.fhir.rest.param.TokenAndListParam;
import lombok.AccessLevel;
import lombok.Setter;
import org.openmrs.Encounter;
import org.openmrs.Visit;
import org.openmrs.module.fhir2.api.dao.FhirVisitDao;
import org.springframework.stereotype.Component;

@Component
@Setter(AccessLevel.PACKAGE)
public class FhirVisitDaoImpl extends BaseEncounterDao<Visit> implements FhirVisitDao {
	
	@Override
	protected void handleDate(CriteriaBuilder criteriaBuilder, DateRangeParam dateRangeParam) {
		EntityManager em = sessionFactory.getCurrentSession();
		criteriaBuilder = em.getCriteriaBuilder();
		CriteriaQuery<Visit> criteriaQuery = criteriaBuilder.createQuery(Visit.class);
		
		List<Predicate> predicates = new ArrayList<>();
		handleDateRange("startDatetime", dateRangeParam).ifPresent(predicates::add);
		criteriaQuery.distinct(true).where(predicates.toArray(new Predicate[] {}));
	}
	
	@Override
	protected void handleEncounterType(CriteriaBuilder criteriaBuilder, TokenAndListParam tokenAndListParam) {
		EntityManager em = sessionFactory.getCurrentSession();
		criteriaBuilder = em.getCriteriaBuilder();
		CriteriaQuery<Visit> criteriaQuery = criteriaBuilder.createQuery(Visit.class);
		Root<Visit> root = criteriaQuery.from(Visit.class);
		
		List<Predicate> predicates = new ArrayList<>();
		CriteriaBuilder finalCriteriaBuilder = criteriaBuilder;
		handleAndListParam(tokenAndListParam, t -> Optional.of(finalCriteriaBuilder.equal(root.get("vt.uuid"), t.getValue())))
		        .ifPresent(t -> {
			        root.join("visitType");
			        predicates.add(t);
			        criteriaQuery.distinct(true).where(predicates.toArray(new Predicate[] {}));
		        });
	}
	
	@Override
	protected void handleParticipant(CriteriaBuilder criteriaBuilder, ReferenceAndListParam referenceAndListParam) {
		EntityManager em = sessionFactory.getCurrentSession();
		criteriaBuilder = em.getCriteriaBuilder();
		CriteriaQuery<Visit> criteriaQuery = criteriaBuilder.createQuery(Visit.class);
		Root<Visit> root = criteriaQuery.from(Visit.class);
		
		Join<Visit, Encounter> encounterJoin = root.join("encounters", JoinType.INNER);
		encounterJoin.join("encounterProviders", JoinType.INNER);
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
