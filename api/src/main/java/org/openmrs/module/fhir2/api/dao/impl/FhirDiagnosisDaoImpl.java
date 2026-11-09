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
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Predicate;

import ca.uhn.fhir.rest.param.DateRangeParam;
import ca.uhn.fhir.rest.param.QuantityAndListParam;
import ca.uhn.fhir.rest.param.ReferenceAndListParam;
import ca.uhn.fhir.rest.param.TokenAndListParam;
import ca.uhn.fhir.rest.param.TokenOrListParam;
import ca.uhn.fhir.rest.param.TokenParam;
import ca.uhn.fhir.rest.param.TokenParamModifier;
import org.openmrs.Diagnosis;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.api.dao.FhirDiagnosisDao;
import org.openmrs.module.fhir2.api.dao.internals.OpenmrsFhirCriteriaContext;
import org.openmrs.module.fhir2.api.search.param.SearchParameterMap;
import org.springframework.stereotype.Component;

@Component
public class FhirDiagnosisDaoImpl extends BaseFhirDao<Diagnosis> implements FhirDiagnosisDao {
	
	/**
	 * Diagnoses are always exposed as FHIR Conditions with a clinicalStatus of "unknown" (see
	 * DiagnosisTranslatorImpl), so this is the only status a diagnosis can match.
	 */
	private static final String DIAGNOSIS_CLINICAL_STATUS = "unknown";
	
	private static final String ONSET_DATE_PROPERTY = "onsetDate";
	
	@Override
	protected <U> void setupSearchParams(@Nonnull OpenmrsFhirCriteriaContext<Diagnosis, U> criteriaContext,
	        @Nonnull SearchParameterMap theParams) {
		theParams.getParameters().forEach(entry -> {
			switch (entry.getKey()) {
				case FhirConstants.PATIENT_REFERENCE_SEARCH_HANDLER:
					entry.getValue().forEach(param -> getSearchQueryHelper().handlePatientReference(criteriaContext,
					    (ReferenceAndListParam) param.getParam(), "patient"));
					break;
				case FhirConstants.ENCOUNTER_REFERENCE_SEARCH_HANDLER:
					entry.getValue().forEach(param -> getSearchQueryHelper().handleEncounterReference(criteriaContext,
					    (ReferenceAndListParam) param.getParam(), "encounter"));
					break;
				case FhirConstants.CODED_SEARCH_HANDLER:
					entry.getValue()
					        .forEach(param -> handleDiagnosisCode(criteriaContext, (TokenAndListParam) param.getParam()));
					break;
				case FhirConstants.CONDITION_CLINICAL_STATUS_HANDLER:
					entry.getValue()
					        .forEach(param -> handleClinicalStatus(criteriaContext, (TokenAndListParam) param.getParam()));
					break;
				case FhirConstants.DATE_RANGE_SEARCH_HANDLER:
					entry.getValue().forEach(param -> handleDateRangeParam(criteriaContext, param.getPropertyName(),
					    (DateRangeParam) param.getParam()));
					break;
				case FhirConstants.QUANTITY_SEARCH_HANDLER:
					entry.getValue()
					        .forEach(param -> handleOnsetAge(criteriaContext, (QuantityAndListParam) param.getParam()));
					break;
				case FhirConstants.COMMON_SEARCH_HANDLER:
					handleCommonSearchParameters(criteriaContext, entry.getValue()).ifPresent(criteriaContext::addPredicate);
					break;
			}
		});
	}
	
	protected <U> void handleDiagnosisCode(@Nonnull OpenmrsFhirCriteriaContext<Diagnosis, U> context,
	        TokenAndListParam code) {
		if (code != null && code.size() > 0) {
			Join<?, ?> codedOrFreeTextJoin = context.addJoin("diagnosis", "diag");
			Join<?, ?> codedJoin = context.addJoin(codedOrFreeTextJoin, "coded", "dc");
			getSearchQueryHelper().handleCodeableConcept(context, code, codedJoin, "dmap", "dterm")
			        .ifPresent(context::addPredicate);
		}
	}
	
	/**
	 * Diagnoses have no persisted clinical status; they are always reported as "unknown". The filter is
	 * therefore evaluated against that fixed value: either every diagnosis matches, or none do.
	 */
	protected <U> void handleClinicalStatus(@Nonnull OpenmrsFhirCriteriaContext<Diagnosis, U> context,
	        TokenAndListParam status) {
		if (status == null || status.size() == 0) {
			return;
		}
		
		if (!matchesDiagnosisClinicalStatus(status)) {
			context.addPredicate(matchNothing(context));
		}
	}
	
	private boolean matchesDiagnosisClinicalStatus(TokenAndListParam status) {
		// AND list: every OR list must match
		for (TokenOrListParam orList : status.getValuesAsQueryTokens()) {
			boolean orMatches = false;
			// OR list: at least one token must match
			for (TokenParam token : orList.getValuesAsQueryTokens()) {
				if (tokenMatchesDiagnosisStatus(token)) {
					orMatches = true;
					break;
				}
			}
			if (!orMatches) {
				return false;
			}
		}
		return true;
	}
	
	private boolean tokenMatchesDiagnosisStatus(TokenParam token) {
		boolean systemMatches = token.getSystem() == null || token.getSystem().isEmpty()
		        || FhirConstants.CONDITION_CLINICAL_SYSTEM_URI.equals(token.getSystem());
		boolean valueMatches = token.getValue() == null || token.getValue().isEmpty()
		        || DIAGNOSIS_CLINICAL_STATUS.equalsIgnoreCase(token.getValue());
		boolean matches = systemMatches && valueMatches;
		
		return TokenParamModifier.NOT.equals(token.getModifier()) ? !matches : matches;
	}
	
	/**
	 * Diagnoses carry no onset information, so an onset-date filter can never match one. The recorded
	 * date maps to the date the diagnosis was created.
	 */
	protected <U> void handleDateRangeParam(@Nonnull OpenmrsFhirCriteriaContext<Diagnosis, U> context, String propertyName,
	        DateRangeParam dateRange) {
		if (dateRange == null || (dateRange.getLowerBound() == null && dateRange.getUpperBound() == null)) {
			return;
		}
		
		if (ONSET_DATE_PROPERTY.equals(propertyName)) {
			context.addPredicate(matchNothing(context));
		} else {
			getSearchQueryHelper().handleDateRange(context, propertyName, dateRange).ifPresent(context::addPredicate);
		}
	}
	
	/**
	 * Diagnoses carry no onset information, so an onset-age filter can never match one.
	 */
	protected <U> void handleOnsetAge(@Nonnull OpenmrsFhirCriteriaContext<Diagnosis, U> context,
	        QuantityAndListParam onsetAge) {
		if (onsetAge != null && onsetAge.size() > 0) {
			context.addPredicate(matchNothing(context));
		}
	}
	
	private <U> Predicate matchNothing(OpenmrsFhirCriteriaContext<Diagnosis, U> context) {
		return context.getCriteriaBuilder().disjunction();
	}
}
