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

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import java.util.ArrayList;
import java.util.List;
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
	
	private List<Predicate> predicateList = new ArrayList<>();
	
	@Override
	public boolean hasDistinctResults() {
		return false;
	}
	
	@Override
	protected void setupSearchParams(CriteriaBuilder criteriaBuilder, SearchParameterMap theParams) {
		EntityManager em = sessionFactory.getCurrentSession();
		criteriaBuilder = em.getCriteriaBuilder();
		CriteriaQuery<TestOrder> criteriaQuery = criteriaBuilder.createQuery(TestOrder.class);
		
		CriteriaBuilder finalCriteriaBuilder = criteriaBuilder;
		theParams.getParameters().forEach(entry -> {
			switch (entry.getKey()) {
				case FhirConstants.ENCOUNTER_REFERENCE_SEARCH_HANDLER:
					entry.getValue().forEach(
					    param -> handleEncounterReference(finalCriteriaBuilder, (ReferenceAndListParam) param.getParam(), "e"));
					break;
				case FhirConstants.PATIENT_REFERENCE_SEARCH_HANDLER:
					entry.getValue().forEach(patientReference -> handlePatientReference(finalCriteriaBuilder,
					    (ReferenceAndListParam) patientReference.getParam(), "patient"));
					break;
				case FhirConstants.CODED_SEARCH_HANDLER:
					entry.getValue()
					        .forEach(code -> handleCodedConcept(finalCriteriaBuilder, (TokenAndListParam) code.getParam()));
					break;
				case FhirConstants.PARTICIPANT_REFERENCE_SEARCH_HANDLER:
					entry.getValue().forEach(participantReference -> handleProviderReference(finalCriteriaBuilder,
					    (ReferenceAndListParam) participantReference.getParam()));
					break;
				case FhirConstants.DATE_RANGE_SEARCH_HANDLER:
					entry.getValue().forEach(dateRangeParam -> handleDateRange((DateRangeParam) dateRangeParam.getParam())
					        .ifPresent(predicateList::add));
					criteriaQuery.distinct(true).where(predicateList.toArray(new Predicate[] {}));
					break;
				case FhirConstants.COMMON_SEARCH_HANDLER:
					handleCommonSearchParameters(entry.getValue()).ifPresent(predicateList::add);
					criteriaQuery.distinct(true).where(predicateList.toArray(new Predicate[] {}));
					break;
			}
		});
	}
	
	private void handleCodedConcept(CriteriaBuilder criteriaBuilder, TokenAndListParam code) {
		EntityManager em = sessionFactory.getCurrentSession();
		criteriaBuilder = em.getCriteriaBuilder();
		CriteriaQuery<TestOrder> criteriaQuery = criteriaBuilder.createQuery(TestOrder.class);
		Root<TestOrder> root = criteriaQuery.from(TestOrder.class);
		
		if (code != null) {
			if (lacksAlias(criteriaBuilder, "c")) {
				root.join("concept").alias("c");
			}
			
			handleCodeableConcept(criteriaBuilder, code, "c", "cm", "crt").ifPresent(predicateList::add);
			criteriaQuery.distinct(true).where(predicateList.toArray(new Predicate[] {}));
		}
	}
	
	private Optional<Predicate> handleDateRange(DateRangeParam dateRangeParam) {
		if (dateRangeParam == null) {
			return Optional.empty();
		}
		EntityManager em = sessionFactory.getCurrentSession();
		CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
		CriteriaQuery<TestOrder> criteriaQuery = criteriaBuilder.createQuery(TestOrder.class);
		
		return Optional.of(criteriaBuilder.and(toCriteriaArray(Stream.of(
		    Optional.of(
		        criteriaBuilder.or(toCriteriaArray(Stream.of(handleDate("scheduledDate", dateRangeParam.getLowerBound()),
		            handleDate("dateActivated", dateRangeParam.getLowerBound()))))),
		    Optional.of(
		        criteriaBuilder.or(toCriteriaArray(Stream.of(handleDate("dateStopped", dateRangeParam.getUpperBound()),
		            handleDate("autoExpireDate", dateRangeParam.getUpperBound())))))))));
	}
	
}
