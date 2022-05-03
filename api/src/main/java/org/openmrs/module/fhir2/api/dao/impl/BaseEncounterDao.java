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

import java.util.Date;
import java.util.Optional;

import ca.uhn.fhir.rest.param.DateRangeParam;
import ca.uhn.fhir.rest.param.HasAndListParam;
import ca.uhn.fhir.rest.param.ReferenceAndListParam;
import ca.uhn.fhir.rest.param.TokenAndListParam;
import org.apache.commons.lang.StringUtils;
import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;
import org.hl7.fhir.r4.model.MedicationRequest;
import org.openmrs.Auditable;
import org.openmrs.DrugOrder;
import org.openmrs.Encounter;
import org.openmrs.OpenmrsObject;
import org.openmrs.Order;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.api.search.param.SearchParameterMap;

public abstract class BaseEncounterDao<T extends OpenmrsObject & Auditable> extends BaseFhirDao<T> {
	
	@Override
	protected void setupSearchParams(Criteria criteria, SearchParameterMap theParams) {
		theParams.getParameters().forEach(entry -> {
			switch (entry.getKey()) {
				case FhirConstants.DATE_RANGE_SEARCH_HANDLER:
					entry.getValue().forEach(param -> handleDate(criteria, (DateRangeParam) param.getParam()));
					break;
				case FhirConstants.LOCATION_REFERENCE_SEARCH_HANDLER:
					entry.getValue().forEach(param -> handleLocationReference("l", (ReferenceAndListParam) param.getParam())
					        .ifPresent(l -> criteria.createAlias("location", "l").add(l)));
					break;
				case FhirConstants.PARTICIPANT_REFERENCE_SEARCH_HANDLER:
					entry.getValue().forEach(param -> handleParticipant(criteria, (ReferenceAndListParam) param.getParam()));
					break;
				case FhirConstants.PATIENT_REFERENCE_SEARCH_HANDLER:
					entry.getValue()
					        .forEach(param -> handlePatientReference(criteria, (ReferenceAndListParam) param.getParam()));
					break;
				case ENCOUNTER_TYPE_REFERENCE_SEARCH_HANDLER:
					entry.getValue().forEach(param -> handleEncounterType(criteria, (TokenAndListParam) param.getParam()));
					break;
				case FhirConstants.COMMON_SEARCH_HANDLER:
					handleCommonSearchParameters(entry.getValue()).ifPresent(criteria::add);
					break;
				case FhirConstants.HAS_SEARCH_HANDLER:
					entry.getValue().forEach(param -> handleHasAndListParam(criteria, (HasAndListParam) param.getParam()));
					break;
			}
		});
	}
	
	/**
	 * TODO: This is a spike / work in progress.  We need to consider how we want to design / handle this
	 */
	protected void handleHasAndListParam(Criteria criteria, HasAndListParam hasAndListParam) {
		if (hasAndListParam != null) {
			handleAndListParam(hasAndListParam, hasParam -> {
				if (hasParam != null) {
					if (FhirConstants.MEDICATION_REQUEST.equals(hasParam.getTargetResourceType())) {
						if (FhirConstants.INCLUDE_ENCOUNTER_PARAM.equals(hasParam.getReferenceFieldName())) {
							if (lacksAlias(criteria, "orders")) {
								if (Encounter.class.isAssignableFrom(typeToken.getRawType())) {
									criteria.createAlias("orders", "orders");
								} else {
									if (lacksAlias(criteria, "en")) {
										criteria.createAlias("encounters", "en");
									}
									criteria.createAlias("en.orders", "orders");
								}
							}
							criteria.add(Restrictions.eq("orders.class", DrugOrder.class));
							criteria.add(Restrictions.eq("orders.voided", false));
							
							// Handle specific medication request properties
							String paramName = hasParam.getParameterName();
							String paramValue = hasParam.getParameterValue();
							if (StringUtils.isNotBlank(paramName) && StringUtils.isNotBlank(paramValue)) {
								if (FhirConstants.INCLUDE_INTENT_PARAM.equals(paramName)) {
									if (MedicationRequest.MedicationRequestIntent.ORDER.toCode().equals(paramValue)) {
										// Do not constrain, all Orders are given this intent
									}
								}
								if (FhirConstants.INCLUDE_STATUS_PARAM.equals(paramName)) {
									Date now = new Date();
									if (MedicationRequest.MedicationRequestStatus.ACTIVE.toCode().equals(paramValue)) {
										criteria.add(Restrictions.ne("orders.action", Order.Action.DISCONTINUE));
										criteria.add(Restrictions.le("orders.dateActivated", now));
										criteria.add(Restrictions.or(Restrictions.isNull("orders.dateStopped"),
										    Restrictions.gt("orders.dateStopped", now)));
										criteria.add(Restrictions.or(Restrictions.isNull("orders.autoExpireDate"),
										    Restrictions.gt("orders.autoExpireDate", now)));
									} else if (MedicationRequest.MedicationRequestStatus.CANCELLED.toCode()
									        .equals(paramValue)) {
										criteria.add(Restrictions.le("orders.dateActivated", now));
										criteria.add(Restrictions.le("orders.dateStopped", now));
									}
								}
							}
						}
					}
				}
				return Optional.empty();
			}).ifPresent(criteria::add);
		}
	}
	
	protected abstract void handleDate(Criteria criteria, DateRangeParam dateRangeParam);
	
	protected abstract void handleEncounterType(Criteria criteria, TokenAndListParam tokenAndListParam);
	
	protected abstract void handleParticipant(Criteria criteria, ReferenceAndListParam referenceAndListParam);
	
}
