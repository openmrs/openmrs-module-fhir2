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

import static org.openmrs.module.fhir2.FhirConstants.ENCOUNTER_TYPE_REFERENCE_SEARCH_HANDLER;

import javax.annotation.Nonnull;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Predicate;

import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;

import ca.uhn.fhir.rest.param.DateRangeParam;
import ca.uhn.fhir.rest.param.HasAndListParam;
import ca.uhn.fhir.rest.param.HasParam;
import ca.uhn.fhir.rest.param.ReferenceAndListParam;
import ca.uhn.fhir.rest.param.TokenAndListParam;
import lombok.extern.slf4j.Slf4j;
import org.hl7.fhir.r4.model.MedicationRequest;
import org.openmrs.Auditable;
import org.openmrs.Encounter;
import org.openmrs.OpenmrsObject;
import org.openmrs.Order;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.api.dao.internals.OpenmrsFhirCriteriaContext;
import org.openmrs.module.fhir2.api.search.param.SearchParameterMap;

@Slf4j
public abstract class BaseEncounterDao<T extends OpenmrsObject & Auditable> extends BaseFhirDao<T> {
	
	@Override
	protected <U> void setupSearchParams(@Nonnull OpenmrsFhirCriteriaContext<T, U> criteriaContext,
	        @Nonnull SearchParameterMap theParams) {
		theParams.getParameters().forEach(entry -> {
			switch (entry.getKey()) {
				case FhirConstants.DATE_RANGE_SEARCH_HANDLER:
					entry.getValue().forEach(param -> handleDate(criteriaContext, (DateRangeParam) param.getParam())
					        .ifPresent(criteriaContext::addPredicate));
					break;
				case FhirConstants.LOCATION_REFERENCE_SEARCH_HANDLER:
					entry.getValue().forEach(param -> {
						From<?, ?> locationAlias = criteriaContext.addJoin("location", "l");
						getSearchQueryHelper().handleLocationReference(criteriaContext, locationAlias,
						    (ReferenceAndListParam) param.getParam()).ifPresent(criteriaContext::addPredicate);
					});
					break;
				case FhirConstants.PARTICIPANT_REFERENCE_SEARCH_HANDLER:
					entry.getValue()
					        .forEach(param -> handleParticipant(criteriaContext, (ReferenceAndListParam) param.getParam())
					                .ifPresent(criteriaContext::addPredicate));
					break;
				case FhirConstants.PATIENT_REFERENCE_SEARCH_HANDLER:
					entry.getValue().forEach(param -> getSearchQueryHelper().handlePatientReference(criteriaContext,
					    (ReferenceAndListParam) param.getParam()));
					break;
				case ENCOUNTER_TYPE_REFERENCE_SEARCH_HANDLER:
					entry.getValue()
					        .forEach(param -> handleEncounterType(criteriaContext, (TokenAndListParam) param.getParam())
					                .ifPresent(criteriaContext::addPredicate));
					break;
				case FhirConstants.COMMON_SEARCH_HANDLER:
					handleCommonSearchParameters(criteriaContext, entry.getValue()).ifPresent(criteriaContext::addPredicate);
					break;
				case FhirConstants.HAS_SEARCH_HANDLER:
					entry.getValue()
					        .forEach(param -> handleHasAndListParam(criteriaContext, (HasAndListParam) param.getParam()));
					break;
			}
		});
	}
	
	/**
	 * Handle _has parameters that are passed in to constrain the Encounter resource on properties of
	 * dependent resources
	 */
	@SuppressWarnings("UnstableApiUsage")
	protected <U> void handleHasAndListParam(OpenmrsFhirCriteriaContext<T, U> criteriaContext,
	        HasAndListParam hasAndListParam) {
		if (hasAndListParam != null) {
			log.debug("Handling hasAndListParam");
			hasAndListParam.getValuesAsQueryTokens().forEach(hasOrListParam -> {
				if (!hasOrListParam.getValuesAsQueryTokens().isEmpty()) {
					
					log.debug("Handling hasOrListParam");
					// Making the assumption that any "orListParams" match everything except for the value
					HasParam hasParam = hasOrListParam.getValuesAsQueryTokens().get(0);
					Set<String> values = new LinkedHashSet<>();
					hasOrListParam.getValuesAsQueryTokens().forEach(orParam -> values.add(orParam.getParameterValue()));
					
					log.debug("Handling hasParam = {} for values in {}", hasParam.getQueryParameterQualifier(), values);
					
					boolean handled = false;
					
					// Support constraining encounter resources to those that contain only certain Medication Requests
					if (FhirConstants.MEDICATION_REQUEST.equals(hasParam.getTargetResourceType())) {
						if (MedicationRequest.SP_ENCOUNTER.equals(hasParam.getReferenceFieldName())) {
							if (!criteriaContext.getJoin("orders").isPresent()) {
								if (Encounter.class.isAssignableFrom(typeToken.getRawType())) {
									criteriaContext.addJoin("orders", "orders");
								} else {
									if (!criteriaContext.getJoin("en").isPresent()) {
										criteriaContext.addJoin("encounters", "en");
									}
									criteriaContext.addJoin("en.orders", "orders");
								}
							}
							
							Optional<Join<?, ?>> ordersJoin = criteriaContext.getJoin("orders");
							if (!ordersJoin.isPresent()) {
								return;
							}
							
							// Constrain only on non-voided Drug Orders
							// TODO Do these criteria still work?
							criteriaContext.addPredicate(
							    criteriaContext.getCriteriaBuilder().equal(ordersJoin.get().get("voided"), false));
							criteriaContext.addPredicate(criteriaContext.getCriteriaBuilder()
							        .notEqual(ordersJoin.get().get("action"), Order.Action.DISCONTINUE));
							
							String paramName = hasParam.getParameterName();
							String paramValue = hasParam.getParameterValue();
							if (MedicationRequest.SP_INTENT.equals(paramName)) {
								if (values.contains(MedicationRequest.MedicationRequestIntent.ORDER.toCode())) {
									// No additional constraints needed, all Orders are given/assumed intent=order
									handled = true;
								}
							} else if (MedicationRequest.SP_STATUS.equals(paramName)) {
								// only supports ACTIVE at this time
								if (paramValue != null) {
									if (MedicationRequest.MedicationRequestStatus.ACTIVE.toString()
									        .equalsIgnoreCase(paramValue)) {
										criteriaContext.addPredicate(criteriaContext.getCriteriaBuilder().and(
										    getSearchQueryHelper().handleQueryForActiveOrders(criteriaContext, "orders")));
									}
								}
								handled = true;
							} else if ((MedicationRequest.SP_STATUS + ":not").equalsIgnoreCase(paramName)) {
								if (paramValue != null) {
									if (MedicationRequest.MedicationRequestStatus.CANCELLED.toString()
									        .equalsIgnoreCase(paramValue)) {
										criteriaContext
										        .addPredicate(criteriaContext.getCriteriaBuilder().and(getSearchQueryHelper()
										                .handleQueryForCancelledOrders(criteriaContext, "orders")));
									}
									if (MedicationRequest.MedicationRequestStatus.COMPLETED.toString()
									        .equalsIgnoreCase(paramValue)) {
										Predicate notCompletedCriterion = generateNotCompletedOrderQuery(criteriaContext,
										    ordersJoin.get());
										if (notCompletedCriterion != null) {
											criteriaContext.addPredicate(
											    criteriaContext.getCriteriaBuilder().and(notCompletedCriterion));
										}
									}
								}
								handled = true;
							} else if ((FhirConstants.SP_FULFILLER_STATUS).equalsIgnoreCase(paramName)) {
								if (paramValue != null) {
									criteriaContext.addPredicate(criteriaContext.getCriteriaBuilder().and(
									    generateFulfillerStatusRestriction(criteriaContext, ordersJoin.get(), paramValue)));
								}
								handled = true;
							} else if ((FhirConstants.SP_FULFILLER_STATUS + ":not").equalsIgnoreCase(paramName)) {
								if (paramValue != null) {
									criteriaContext.addPredicate(
									    criteriaContext.getCriteriaBuilder().and(generateNotFulfillerStatusRestriction(
									        criteriaContext, ordersJoin.get(), paramValue)));
								}
								handled = true;
							}
						}
					}
					if (!handled) {
						log.warn("_has parameter not supported: {}", hasParam.getQueryParameterQualifier());
					}
					
				}
			});
		}
	}
	
	protected abstract <U> Optional<Predicate> handleDate(OpenmrsFhirCriteriaContext<T, U> criteriaContext,
	        DateRangeParam dateRangeParam);
	
	protected abstract <U> Optional<Predicate> handleEncounterType(OpenmrsFhirCriteriaContext<T, U> criteriaContext,
	        TokenAndListParam tokenAndListParam);
	
	protected abstract <U> Optional<Predicate> handleParticipant(OpenmrsFhirCriteriaContext<T, U> criteriaContext,
	        ReferenceAndListParam referenceAndListParam);
	
	protected <V, U> Predicate generateNotCompletedOrderQuery(OpenmrsFhirCriteriaContext<V, U> criteriaContext,
	        Join<?, ?> ordersJoin) {
		// not implemented in Core until 2.2; see override in FhirEncounterDaoImpl_2_2
		return null;
	}
	
	protected <V, U> Predicate generateFulfillerStatusRestriction(OpenmrsFhirCriteriaContext<V, U> criteriaContext,
	        Join<?, ?> ordersJoin, String fulfillerStatus) {
		// not implemented in Core until 2.2; see override in FhirEncounterDaoImpl_2_2
		return null;
	}
	
	protected <V, U> Predicate generateNotFulfillerStatusRestriction(OpenmrsFhirCriteriaContext<V, U> criteriaContext,
	        Join<?, ?> ordersJoin, String fulfillerStatus) {
		// not implemented in Core until 2.2; see override in FhirEncounterDaoImpl_2_2
		return null;
	}
	
}
