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

import static org.hibernate.criterion.Restrictions.ne;

import javax.annotation.Nonnull;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Predicate;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import ca.uhn.fhir.rest.param.ReferenceAndListParam;
import ca.uhn.fhir.rest.param.TokenAndListParam;
import lombok.AccessLevel;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.Criteria;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;
import org.hl7.fhir.r4.model.MedicationRequest;
import org.openmrs.DrugOrder;
import org.openmrs.Order;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.api.dao.FhirMedicationRequestDao;
import org.openmrs.module.fhir2.api.search.param.SearchParameterMap;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Setter(AccessLevel.PACKAGE)
public class FhirMedicationRequestDaoImpl extends BaseFhirDao<DrugOrder> implements FhirMedicationRequestDao {
	
	@Override
	@Transactional(readOnly = true)
	public DrugOrder get(@Nonnull String uuid) {
		DrugOrder result = super.get(uuid);
		return result != null && result.getAction() != null && result.getAction() != Order.Action.DISCONTINUE ? result
		        : null;
	}
	
	@Override
	@Transactional(readOnly = true)
	public List<DrugOrder> get(@Nonnull Collection<String> uuids) {
		List<DrugOrder> results = super.get(uuids);
		if (results == null) {
			return results;
		} else {
			return results.stream()
			        .filter(order -> order.getAction() == null || order.getAction() != Order.Action.DISCONTINUE)
			        .collect(Collectors.toList());
		}
	}
	
	@Override
	protected void setupSearchParams(CriteriaBuilder criteriaBuilder, SearchParameterMap theParams) {
		theParams.getParameters().forEach(entry -> {
			switch (entry.getKey()) {
				case FhirConstants.FULFILLER_STATUS_SEARCH_HANDLER:
					entry.getValue().forEach(
					    param -> handleFulfillerStatus((TokenAndListParam) param.getParam()).ifPresent(criteriaBuilder::and));
					break;
				case FhirConstants.ENCOUNTER_REFERENCE_SEARCH_HANDLER:
					entry.getValue()
					        .forEach(e -> handleEncounterReference(criteriaBuilder, (ReferenceAndListParam) e.getParam(), "e"));
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
				case FhirConstants.MEDICATION_REFERENCE_SEARCH_HANDLER:
					entry.getValue().forEach(d -> handleMedicationReference("d", (ReferenceAndListParam) d.getParam())
					        .ifPresent(c -> {root.join("drug").alias("d");
						        criteriaBuilder.and(c);
					        }));
					break;
				case FhirConstants.STATUS_SEARCH_HANDLER:
					entry.getValue()
					        .forEach(param -> handleStatus((TokenAndListParam) param.getParam()).ifPresent(criteriaBuilder::and));
				case FhirConstants.COMMON_SEARCH_HANDLER:
					handleCommonSearchParameters(entry.getValue()).ifPresent(criteriaBuilder::and);
					break;
			}
		});
		
		excludeDiscontinueOrders(criteriaBuilder);
	}
	
	private Optional<Predicate> handleStatus(TokenAndListParam tokenAndListParam) {
		return handleAndListParam(tokenAndListParam, token -> {
			if (token.getValue() != null) {
				try {
					// currently only handles "ACTIVE"
					if (MedicationRequest.MedicationRequestStatus.ACTIVE.toString().equals(token.getValue().toUpperCase())) {
						return Optional.of(generateActiveOrderQuery());
					}
				}
				catch (IllegalArgumentException e) {
					return Optional.empty();
				}
			}
			
			return Optional.empty();
		});
	}
	
	private Optional<Predicate> handleFulfillerStatus(TokenAndListParam tokenAndListParam) {
		return handleAndListParam(tokenAndListParam, token -> {
			if (token.getValue() != null) {
				return Optional.of(
				    generateFulfillerStatusRestriction(Order.FulfillerStatus.valueOf(token.getValue().toUpperCase())));
			}
			return Optional.empty();
		});
	}
	
	protected Predicate generateFulfillerStatusRestriction(Order.FulfillerStatus fulfillerStatus) {
		return generateFulfillerStatusRestriction("", fulfillerStatus);
	}
	
	protected Predicate generateFulfillerStatusRestriction(String path, Order.FulfillerStatus fulfillerStatus) {
		if (StringUtils.isNotBlank(path)) {
			path = path + ".";
		}
		
		return criteriaBuilder.equal(root.get(path + "fulfillerStatus"), fulfillerStatus);
	}
	
	private void handleCodedConcept(CriteriaBuilder criteriaBuilder, TokenAndListParam code) {
		if (code != null) {
			if (lacksAlias(criteriaBuilder, "c")) {
				root.join("concept").alias("c");
			}
			
			handleCodeableConcept(criteriaBuilder, code, "c", "cm", "crt").ifPresent(criteriaBuilder::and);
		}
	}
	
	private void excludeDiscontinueOrders(CriteriaBuilder criteriaBuilder) {
		// exclude "discontinue" orders, see: https://issues.openmrs.org/browse/FM2-532
		criteriaBuilder.and(criteriaBuilder.notEqual(root.get("action"), Order.Action.DISCONTINUE));
	}
}
