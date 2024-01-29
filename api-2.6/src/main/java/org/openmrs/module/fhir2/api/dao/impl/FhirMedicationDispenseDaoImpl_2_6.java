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
import javax.persistence.criteria.Predicate;

import java.util.List;
import java.util.Optional;

import ca.uhn.fhir.rest.param.DateRangeParam;
import ca.uhn.fhir.rest.param.ReferenceAndListParam;
import lombok.AccessLevel;
import lombok.NonNull;
import lombok.Setter;
import org.openmrs.MedicationDispense;
import org.openmrs.annotation.Authorized;
import org.openmrs.annotation.OpenmrsProfile;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.api.dao.FhirMedicationDispenseDao;
import org.openmrs.module.fhir2.api.search.param.SearchParameterMap;
import org.openmrs.util.PrivilegeConstants;
import org.springframework.stereotype.Component;

@Component
@Setter(AccessLevel.PROTECTED)
@OpenmrsProfile(openmrsPlatformVersion = "2.6.* - 2.*")
public class FhirMedicationDispenseDaoImpl_2_6 extends BaseFhirDao<MedicationDispense> implements FhirMedicationDispenseDao<MedicationDispense> {
	
	@Override
	@Authorized(PrivilegeConstants.GET_MEDICATION_DISPENSE)
	public MedicationDispense get(@Nonnull String uuid) {
		return super.get(uuid);
	}
	
	@Override
	@Authorized(PrivilegeConstants.EDIT_MEDICATION_DISPENSE)
	public MedicationDispense createOrUpdate(@Nonnull MedicationDispense newEntry) {
		return super.createOrUpdate(newEntry);
	}
	
	@Override
	@Authorized(PrivilegeConstants.DELETE_MEDICATION_DISPENSE)
	public MedicationDispense delete(@Nonnull String uuid) {
		return super.delete(uuid);
	}
	
	@Override
	@Authorized(PrivilegeConstants.GET_MEDICATION_DISPENSE)
	public List<MedicationDispense> getSearchResults(@Nonnull SearchParameterMap theParams) {
		return super.getSearchResults(theParams);
	}
	
	@Override
	protected <U> void setupSearchParams(OpenmrsFhirCriteriaContext<MedicationDispense, U> criteriaContext,
	        SearchParameterMap theParams) {
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
					entry.getValue().forEach(e -> handleMedicationRequestReference(criteriaContext, "drugOrder",
					    (ReferenceAndListParam) e.getParam()).ifPresent(c -> {
						    criteriaContext.addJoin("drugOrder", "drugOrder");
						    criteriaContext.addPredicate(c);
						    criteriaContext.finalizeQuery();
					    }));
					break;
				case FhirConstants.COMMON_SEARCH_HANDLER:
					handleCommonSearchParameters(criteriaContext, entry.getValue()).ifPresent(criteriaContext::addPredicate);
					criteriaContext.finalizeQuery();
					break;
			}
		});
	}
	
	@Override
	protected <T, U> Optional<Predicate> handleLastUpdated(OpenmrsFhirCriteriaContext<T, U> criteriaContext,
	        DateRangeParam param) {
		return super.handleLastUpdatedImmutable(criteriaContext, param);
	}
	
	@Override
	protected <V, U> String paramToProp(OpenmrsFhirCriteriaContext<V, U> criteriaContext, @NonNull String param) {
		return super.paramToProp(criteriaContext, param);
	}
}
