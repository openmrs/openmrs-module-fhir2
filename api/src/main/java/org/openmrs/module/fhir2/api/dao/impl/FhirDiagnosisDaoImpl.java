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

import ca.uhn.fhir.rest.param.ReferenceAndListParam;
import ca.uhn.fhir.rest.param.TokenAndListParam;
import org.openmrs.Diagnosis;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.api.dao.FhirDiagnosisDao;
import org.openmrs.module.fhir2.api.dao.internals.OpenmrsFhirCriteriaContext;
import org.openmrs.module.fhir2.api.search.param.SearchParameterMap;
import org.springframework.stereotype.Component;

@Component
public class FhirDiagnosisDaoImpl extends BaseFhirDao<Diagnosis> implements FhirDiagnosisDao {
	
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
}
