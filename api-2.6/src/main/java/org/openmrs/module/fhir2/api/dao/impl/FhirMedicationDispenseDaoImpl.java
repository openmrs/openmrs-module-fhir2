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
import javax.persistence.criteria.From;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;

import java.util.Optional;

import ca.uhn.fhir.rest.param.DateRangeParam;
import ca.uhn.fhir.rest.param.ReferenceAndListParam;
import org.openmrs.MedicationDispense;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.api.dao.FhirMedicationDispenseDao;
import org.openmrs.module.fhir2.api.dao.internals.OpenmrsFhirCriteriaContext;
import org.openmrs.module.fhir2.api.search.param.SearchParameterMap;
import org.springframework.stereotype.Component;

@Component
public class FhirMedicationDispenseDaoImpl extends BaseFhirDao<MedicationDispense> implements FhirMedicationDispenseDao<MedicationDispense> {
	
	@Override
	protected <U> void setupSearchParams(@Nonnull OpenmrsFhirCriteriaContext<MedicationDispense, U> criteriaContext,
	        @Nonnull SearchParameterMap theParams) {
		theParams.getParameters().forEach(entry -> {
			switch (entry.getKey()) {
				case FhirConstants.PATIENT_REFERENCE_SEARCH_HANDLER:
					entry.getValue().forEach(
					    param -> handlePatientReference(criteriaContext, (ReferenceAndListParam) param.getParam()));
					break;
				case FhirConstants.ENCOUNTER_REFERENCE_SEARCH_HANDLER:
					entry.getValue().forEach(
					    e -> handleEncounterReference(criteriaContext, (ReferenceAndListParam) e.getParam(), "e"));
					break;
				case FhirConstants.MEDICATION_REQUEST_REFERENCE_SEARCH_HANDLER:
					From<?, ?> drugOrder = criteriaContext.addJoin("drugOrder", "drugOrder");
					entry.getValue().forEach(e -> handleMedicationRequestReference(criteriaContext, drugOrder,
					    (ReferenceAndListParam) e.getParam()).ifPresent(criteriaContext::addPredicate));
					break;
				case FhirConstants.COMMON_SEARCH_HANDLER:
					handleCommonSearchParameters(criteriaContext, entry.getValue()).ifPresent(criteriaContext::addPredicate);
					criteriaContext.finalizeQuery();
					break;
			}
		});
	}
	
	@Override
	protected <T, U> Optional<Predicate> handleLastUpdated(@Nonnull OpenmrsFhirCriteriaContext<T, U> criteriaContext,
	        DateRangeParam param) {
		return super.handleLastUpdatedImmutable(criteriaContext, param);
	}
	
	@Override
	protected <V, U> Path<?> paramToProp(@Nonnull OpenmrsFhirCriteriaContext<V, U> criteriaContext, @Nonnull String param) {
		return super.paramToProp(criteriaContext, param);
	}
}
