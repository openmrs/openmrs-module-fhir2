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

import javax.annotation.Nonnull;
import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
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
import org.hl7.fhir.r4.model.DiagnosticReport;
import org.openmrs.Encounter;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.api.dao.FhirDiagnosticReportDao;
import org.openmrs.module.fhir2.api.search.param.SearchParameterMap;
import org.openmrs.module.fhir2.model.FhirDiagnosticReport;
import org.springframework.stereotype.Component;

@Component
@Setter(AccessLevel.PACKAGE)
public class FhirDiagnosticReportDaoImpl extends BaseFhirDao<FhirDiagnosticReport> implements FhirDiagnosticReportDao {
	
	@Override
	protected void setupSearchParams(CriteriaBuilder criteriaBuilder, SearchParameterMap theParams) {
		EntityManager em = sessionFactory.getCurrentSession();
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<FhirDiagnosticReport> criteriaQuery = cb.createQuery(FhirDiagnosticReport.class);
		
		List<Predicate> predicates = new ArrayList<>();
		theParams.getParameters().forEach(entry -> {
			switch (entry.getKey()) {
				case FhirConstants.ENCOUNTER_REFERENCE_SEARCH_HANDLER:
					entry.getValue().forEach(
					    param -> handleEncounterReference(criteriaBuilder, (ReferenceAndListParam) param.getParam(), "e"));
					break;
				case FhirConstants.PATIENT_REFERENCE_SEARCH_HANDLER:
					entry.getValue().forEach(param -> handlePatientReference(criteriaBuilder,
					    (ReferenceAndListParam) param.getParam(), "subject"));
					break;
				case FhirConstants.CODED_SEARCH_HANDLER:
					entry.getValue()
					        .forEach(param -> handleCodedConcept(criteriaBuilder, (TokenAndListParam) param.getParam()));
					break;
				case FhirConstants.DATE_RANGE_SEARCH_HANDLER:
					entry.getValue().forEach(
					    param -> handleDateRange("issued", (DateRangeParam) param.getParam()).ifPresent(predicates::add));
					criteriaQuery.distinct(true).where(predicates.toArray(new Predicate[] {}));
					break;
				case FhirConstants.RESULT_SEARCH_HANDLER:
					entry.getValue().forEach(
					    param -> handleObservationReference(criteriaBuilder, (ReferenceAndListParam) param.getParam()));
					break;
				case FhirConstants.COMMON_SEARCH_HANDLER:
					handleCommonSearchParameters(entry.getValue()).ifPresent(predicates::add);
					criteriaQuery.distinct(true).where(predicates.toArray(new Predicate[] {}));
					break;
			}
		});
	}
	
	private void handleCodedConcept(CriteriaBuilder criteriaBuilder, TokenAndListParam code) {
		EntityManager em = sessionFactory.getCurrentSession();
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<FhirDiagnosticReport> criteriaQuery = cb.createQuery(FhirDiagnosticReport.class);
		Root<FhirDiagnosticReport> root = criteriaQuery.from(FhirDiagnosticReport.class);
		
		List<Predicate> predicates = new ArrayList<>();
		if (code != null) {
			if (lacksAlias(criteriaBuilder, "c")) {
				root.join("code");
			}
			handleCodeableConcept(criteriaBuilder, code, "c", "cm", "crt").ifPresent(predicates::add);
			criteriaQuery.distinct(true).where(predicates.toArray(new Predicate[] {}));
		}
	}
	
	private void handleObservationReference(CriteriaBuilder criteriaBuilder, ReferenceAndListParam result) {
		EntityManager em = sessionFactory.getCurrentSession();
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<FhirDiagnosticReport> criteriaQuery = cb.createQuery(FhirDiagnosticReport.class);
		Root<FhirDiagnosticReport> root = criteriaQuery.from(FhirDiagnosticReport.class);
		
		List<Predicate> predicates = new ArrayList<>();
		if (result != null) {
			if (lacksAlias(criteriaBuilder, "obs")) {
				root.join("results");
			}
			
			handleAndListParam(result, token -> Optional.of(criteriaBuilder.equal(root.get("obs.uuid"), token.getIdPart())))
			        .ifPresent(predicates::add);
			criteriaQuery.distinct(true).where(predicates.toArray(new Predicate[] {}));
		}
	}
	
	@Override
	protected String paramToProp(@Nonnull String param) {
		if (DiagnosticReport.SP_ISSUED.equals(param)) {
			return "issued";
		}
		
		return super.paramToProp(param);
	}
	
}
