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

import java.util.Optional;
import java.util.stream.Stream;

import ca.uhn.fhir.rest.param.DateRangeParam;
import ca.uhn.fhir.rest.param.ReferenceAndListParam;
import ca.uhn.fhir.rest.param.TokenAndListParam;
import lombok.AccessLevel;
import lombok.Setter;
import org.openmrs.TestOrder;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.api.dao.FhirServiceRequestDao;
import org.openmrs.module.fhir2.api.search.param.SearchParameterMap;
import org.springframework.stereotype.Component;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Predicate;

@Component
@Setter(AccessLevel.PACKAGE)
public class FhirServiceRequestDaoImpl extends BaseFhirDao<TestOrder> implements FhirServiceRequestDao<TestOrder> {
	
	@Override
	public boolean hasDistinctResults() {
		return false;
	}
	
	@Override
	protected void setupSearchParams(CriteriaBuilder criteriaBuilder, SearchParameterMap theParams) {
		theParams.getParameters().forEach(entry -> {
			switch (entry.getKey()) {
				case FhirConstants.ENCOUNTER_REFERENCE_SEARCH_HANDLER:
					entry.getValue().forEach(
					    param -> handleEncounterReference(criteriaBuilder, (ReferenceAndListParam) param.getParam(), "e"));
					break;
				case FhirConstants.PATIENT_REFERENCE_SEARCH_HANDLER:
					entry.getValue().forEach(patientReference -> handlePatientReference(criteriaBuilder,
					    (ReferenceAndListParam) patientReference.getParam(), "patient"));
					break;
				case FhirConstants.CODED_SEARCH_HANDLER:
					entry.getValue().forEach(code -> handleCodedConcept(criteriaBuilder, (TokenAndListParam) code.getParam()));
					break;
				case FhirConstants.PARTICIPANT_REFERENCE_SEARCH_HANDLER:
					entry.getValue().forEach(participantReference -> handleProviderReference(criteriaBuilder,
					    (ReferenceAndListParam) participantReference.getParam()));
					break;
				case FhirConstants.DATE_RANGE_SEARCH_HANDLER:
					entry.getValue().forEach(dateRangeParam -> handleDateRange((DateRangeParam) dateRangeParam.getParam())
					        .ifPresent(criteriaBuilder::and));
					break;
				case FhirConstants.COMMON_SEARCH_HANDLER:
					handleCommonSearchParameters(entry.getValue()).ifPresent(criteriaBuilder::and);
					break;
			}
		});
	}
	
	private void handleCodedConcept(CriteriaBuilder criteriaBuilder, TokenAndListParam code) {
		if (code != null) {
			if (lacksAlias(criteriaBuilder, "c")) {
				root.join("concept").alias("c");
			}
			
			handleCodeableConcept(criteriaBuilder, code, "c", "cm", "crt").ifPresent(criteriaBuilder::and);
		}
	}
	
	private Optional<Predicate> handleDateRange(DateRangeParam dateRangeParam) {
		if (dateRangeParam == null) {
			return Optional.empty();
		}
		
		return Optional.of(criteriaBuilder.and(toCriteriaArray(Stream.of(
		    Optional.of(criteriaBuilder.or(toCriteriaArray(Stream.of(handleDate("scheduledDate", dateRangeParam.getLowerBound()),
		        handleDate("dateActivated", dateRangeParam.getLowerBound()))))),
		    Optional.of(criteriaBuilder.or(toCriteriaArray(Stream.of(handleDate("dateStopped", dateRangeParam.getUpperBound()),
		        handleDate("autoExpireDate", dateRangeParam.getUpperBound())))))))));
	}
	
}
