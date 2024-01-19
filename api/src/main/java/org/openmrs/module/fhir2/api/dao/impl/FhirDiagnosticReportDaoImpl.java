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

import javax.persistence.criteria.Join;

import java.util.Optional;

import ca.uhn.fhir.rest.param.DateRangeParam;
import ca.uhn.fhir.rest.param.ReferenceAndListParam;
import ca.uhn.fhir.rest.param.TokenAndListParam;
import lombok.AccessLevel;
import lombok.NonNull;
import lombok.Setter;
import org.hl7.fhir.r4.model.DiagnosticReport;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.api.dao.FhirDiagnosticReportDao;
import org.openmrs.module.fhir2.api.search.param.SearchParameterMap;
import org.openmrs.module.fhir2.model.FhirDiagnosticReport;
import org.springframework.stereotype.Component;

@Component
@Setter(AccessLevel.PACKAGE)
public class FhirDiagnosticReportDaoImpl extends BaseFhirDao<FhirDiagnosticReport> implements FhirDiagnosticReportDao {
	
	@Override
	protected <U> void setupSearchParams(OpenmrsFhirCriteriaContext<FhirDiagnosticReport,U> criteriaContext,
	        SearchParameterMap theParams) {
		theParams.getParameters().forEach(entry -> {
			switch (entry.getKey()) {
				case FhirConstants.ENCOUNTER_REFERENCE_SEARCH_HANDLER:
					entry.getValue().forEach(
					    param -> handleEncounterReference(criteriaContext, (ReferenceAndListParam) param.getParam(), "e"));
					break;
				case FhirConstants.PATIENT_REFERENCE_SEARCH_HANDLER:
					entry.getValue().forEach(param -> handlePatientReference(criteriaContext,
					    (ReferenceAndListParam) param.getParam(), "subject"));
					break;
				case FhirConstants.CODED_SEARCH_HANDLER:
					entry.getValue()
					        .forEach(param -> handleCodedConcept(criteriaContext, (TokenAndListParam) param.getParam()));
					break;
				case FhirConstants.DATE_RANGE_SEARCH_HANDLER:
					entry.getValue()
					        .forEach(param -> handleDateRange(criteriaContext, "issued", (DateRangeParam) param.getParam())
					                .ifPresent(criteriaContext::addPredicate));
					criteriaContext.finalizeQuery();
					break;
				case FhirConstants.RESULT_SEARCH_HANDLER:
					entry.getValue().forEach(
					    param -> handleObservationReference(criteriaContext, (ReferenceAndListParam) param.getParam()));
					break;
				case FhirConstants.COMMON_SEARCH_HANDLER:
					handleCommonSearchParameters(criteriaContext, entry.getValue()).ifPresent(criteriaContext::addPredicate);
					criteriaContext.finalizeQuery();
					break;
			}
		});
	}
	
	private <U> void handleCodedConcept(OpenmrsFhirCriteriaContext<FhirDiagnosticReport,U> criteriaContext,
	        TokenAndListParam code) {
		if (code != null) {
			if (!criteriaContext.getJoin("c").isPresent()) {
				criteriaContext.addJoin("code", "c");
			}
			handleCodeableConcept(criteriaContext, code, "c", "cm", "crt").ifPresent(criteriaContext::addPredicate);
			criteriaContext.finalizeQuery();
		}
	}
	
	private <U> void handleObservationReference(OpenmrsFhirCriteriaContext<FhirDiagnosticReport,U> criteriaContext,
	        ReferenceAndListParam result) {
		if (result != null) {
			Join<?, ?> resultsJoin = criteriaContext.addJoin("results", "obs");
			
			handleAndListParam(criteriaContext.getCriteriaBuilder(), result,
			    token -> Optional.of(criteriaContext.getCriteriaBuilder().equal(resultsJoin.get("uuid"), token.getIdPart())))
			            .ifPresent(criteriaContext::addPredicate);
			criteriaContext.finalizeQuery();
		}
	}
	
	@Override
	protected <V,U> String paramToProp(OpenmrsFhirCriteriaContext<V,U> criteriaContext, @NonNull String param) {
		if (DiagnosticReport.SP_ISSUED.equals(param)) {
			return "issued";
		}
		
		return super.paramToProp(criteriaContext, param);
	}
}
