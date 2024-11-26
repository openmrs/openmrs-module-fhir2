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
import javax.persistence.criteria.Predicate;

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

@Component
@Setter(AccessLevel.PACKAGE)
public class FhirServiceRequestDaoImpl extends BaseFhirDao<TestOrder> implements FhirServiceRequestDao<TestOrder> {
	
	@Override
	public boolean hasDistinctResults() {
		return false;
	}
	
	@Override
	protected <U> void setupSearchParams(@Nonnull OpenmrsFhirCriteriaContext<TestOrder, U> criteriaContext,
										 @Nonnull SearchParameterMap theParams) {
		theParams.getParameters().forEach(entry -> {
			switch (entry.getKey()) {
				case FhirConstants.ENCOUNTER_REFERENCE_SEARCH_HANDLER:
					entry.getValue().forEach(
					    param -> handleEncounterReference(criteriaContext, (ReferenceAndListParam) param.getParam(), "e"));
					break;
				case FhirConstants.PATIENT_REFERENCE_SEARCH_HANDLER:
					entry.getValue().forEach(patientReference -> handlePatientReference(criteriaContext,
					    (ReferenceAndListParam) patientReference.getParam(), "patient"));
					break;
				case FhirConstants.CODED_SEARCH_HANDLER:
					entry.getValue()
					        .forEach(code -> handleCodedConcept(criteriaContext, (TokenAndListParam) code.getParam()));
					break;
				case FhirConstants.PARTICIPANT_REFERENCE_SEARCH_HANDLER:
					entry.getValue().forEach(participantReference -> handleProviderReference(criteriaContext,
					    (ReferenceAndListParam) participantReference.getParam()));
					break;
				case FhirConstants.DATE_RANGE_SEARCH_HANDLER:
					entry.getValue().forEach(
					    dateRangeParam -> handleDateRange(criteriaContext, (DateRangeParam) dateRangeParam.getParam())
					            .ifPresent(d -> criteriaContext.addPredicate(d).finalizeQuery()));
					break;
				case FhirConstants.COMMON_SEARCH_HANDLER:
					handleCommonSearchParameters(criteriaContext, entry.getValue()).ifPresent(criteriaContext::addPredicate);
					break;
			}
		});
	}
	
	private <U> void handleCodedConcept(OpenmrsFhirCriteriaContext<TestOrder, U> criteriaContext, TokenAndListParam code) {
		if (code != null) {
			From<?, ?> conceptJoin = criteriaContext.addJoin("concept", "c");
			handleCodeableConcept(criteriaContext, code, conceptJoin, "cm", "crt").ifPresent(criteriaContext::addPredicate);
		}
	}
	
	private <T, U> Optional<Predicate> handleDateRange(OpenmrsFhirCriteriaContext<T, U> criteriaContext,
	        DateRangeParam dateRangeParam) {
		if (dateRangeParam == null) {
			return Optional.empty();
		}
		
		return Optional
		        .of(criteriaContext.getCriteriaBuilder()
		                .and(
		                    toCriteriaArray(
		                        Stream.of(
		                            Optional.of(criteriaContext.getCriteriaBuilder()
		                                    .or(toCriteriaArray(Stream.of(
		                                        handleDate(criteriaContext, "scheduledDate", dateRangeParam.getLowerBound()),
		                                        handleDate(criteriaContext, "dateActivated",
		                                            dateRangeParam.getLowerBound()))))),
		                            Optional.of(criteriaContext.getCriteriaBuilder()
		                                    .or(toCriteriaArray(Stream.of(
		                                        handleDate(criteriaContext, "dateStopped", dateRangeParam.getUpperBound()),
		                                        handleDate(criteriaContext, "autoExpireDate",
		                                            dateRangeParam.getUpperBound())))))))));
	}
	
}
