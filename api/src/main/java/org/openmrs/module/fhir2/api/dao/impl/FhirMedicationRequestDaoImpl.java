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

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import ca.uhn.fhir.rest.param.ReferenceAndListParam;
import ca.uhn.fhir.rest.param.TokenAndListParam;
import lombok.AccessLevel;
import lombok.Setter;
import org.hibernate.Criteria;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;
import org.hl7.fhir.r4.model.Medication;
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
	@SuppressWarnings("unchecked")
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
	public List<DrugOrder> getSearchResults(@Nonnull SearchParameterMap theParams, @Nonnull List<Integer> resourceIds) {
		List<DrugOrder> results = super.getSearchResults(theParams, resourceIds);
		if (results == null) {
			return results;
		} else {
			return results.stream()
			        .filter(order -> order.getAction() == null || order.getAction() != Order.Action.DISCONTINUE)
			        .collect(Collectors.toList());
		}
		
	}
	
	@Override
	protected void setupSearchParams(Criteria criteria, SearchParameterMap theParams) {
		theParams.getParameters().forEach(entry -> {
			switch (entry.getKey()) {
				case FhirConstants.ENCOUNTER_REFERENCE_SEARCH_HANDLER:
					entry.getValue()
					        .forEach(e -> handleEncounterReference(criteria, (ReferenceAndListParam) e.getParam(), "e"));
					break;
				case FhirConstants.PATIENT_REFERENCE_SEARCH_HANDLER:
					entry.getValue().forEach(patientReference -> handlePatientReference(criteria,
					    (ReferenceAndListParam) patientReference.getParam(), "patient"));
					break;
				case FhirConstants.CODED_SEARCH_HANDLER:
					entry.getValue().forEach(code -> handleCodedConcept(criteria, (TokenAndListParam) code.getParam()));
					break;
				case FhirConstants.PARTICIPANT_REFERENCE_SEARCH_HANDLER:
					entry.getValue().forEach(participantReference -> handleProviderReference(criteria,
					    (ReferenceAndListParam) participantReference.getParam()));
					break;
				case FhirConstants.MEDICATION_REFERENCE_SEARCH_HANDLER:
					entry.getValue().forEach(d -> handleMedicationReference("d", (ReferenceAndListParam) d.getParam())
					        .ifPresent(c -> criteria.createAlias("drug", "d").add(c)));
					break;
				case FhirConstants.STATUS_SEARCH_HANDLER:
					entry.getValue()
					        .forEach(param -> handleStatus((TokenAndListParam) param.getParam()).ifPresent(criteria::add));
				case FhirConstants.COMMON_SEARCH_HANDLER:
					handleCommonSearchParameters(entry.getValue()).ifPresent(criteria::add);
					break;
			}
		});
		excludeDiscontinueOrders(criteria);
	}
	
	private Optional<Criterion> handleStatus(TokenAndListParam tokenAndListParam) {
		return handleAndListParam(tokenAndListParam, token -> {
			if (token.getValue() != null) {
				try {
					// currently only handles "ACTIVE"
					if (Medication.MedicationStatus.ACTIVE.toString().equals(token.getValue().toUpperCase())) {
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
	
	private void handleCodedConcept(Criteria criteria, TokenAndListParam code) {
		if (code != null) {
			if (lacksAlias(criteria, "c")) {
				criteria.createAlias("concept", "c");
			}
			
			handleCodeableConcept(criteria, code, "c", "cm", "crt").ifPresent(criteria::add);
		}
	}
	
	private void excludeDiscontinueOrders(Criteria criteria) {
		// exclude "discontinue" orders, see: https://issues.openmrs.org/browse/FM2-532
		criteria.add(Restrictions.ne("action", Order.Action.DISCONTINUE));
	}
}
