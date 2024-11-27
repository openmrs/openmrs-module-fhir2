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

import java.util.List;
import java.util.Optional;

import ca.uhn.fhir.rest.param.DateRangeParam;
import ca.uhn.fhir.rest.param.QuantityAndListParam;
import ca.uhn.fhir.rest.param.ReferenceAndListParam;
import ca.uhn.fhir.rest.param.TokenAndListParam;
import lombok.AccessLevel;
import lombok.NonNull;
import lombok.Setter;
import org.openmrs.Condition;
import org.openmrs.ConditionClinicalStatus;
import org.openmrs.annotation.Authorized;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.api.dao.FhirConditionDao;
import org.openmrs.module.fhir2.api.dao.internals.OpenmrsFhirCriteriaContext;
import org.openmrs.module.fhir2.api.search.param.SearchParameterMap;
import org.openmrs.util.PrivilegeConstants;
import org.springframework.stereotype.Component;

@Component
@Setter(AccessLevel.PROTECTED)
public class FhirConditionDaoImpl extends BaseFhirDao<Condition> implements FhirConditionDao<Condition> {
	
	@Override
	@Authorized(PrivilegeConstants.GET_CONDITIONS)
	public Condition get(@Nonnull String uuid) {
		return super.get(uuid);
	}
	
	@Override
	@Authorized(PrivilegeConstants.EDIT_CONDITIONS)
	public Condition createOrUpdate(@Nonnull Condition newEntry) {
		return super.createOrUpdate(newEntry);
	}
	
	@Override
	@Authorized(PrivilegeConstants.DELETE_CONDITIONS)
	public Condition delete(@Nonnull String uuid) {
		return super.delete(uuid);
	}
	
	@Override
	@Authorized(PrivilegeConstants.GET_CONDITIONS)
	public List<Condition> getSearchResults(@Nonnull SearchParameterMap theParams) {
		return super.getSearchResults(theParams);
	}
	
	private ConditionClinicalStatus convertStatus(String status) {
		if ("active".equalsIgnoreCase(status)) {
			return ConditionClinicalStatus.ACTIVE;
		}
		return ConditionClinicalStatus.INACTIVE;
	}
	
	@Override
	public boolean hasDistinctResults() {
		return false;
	}
	
	@Override
	protected <U> void setupSearchParams(@Nonnull OpenmrsFhirCriteriaContext<Condition, U> criteriaContext,
	        @Nonnull SearchParameterMap theParams) {
		theParams.getParameters().forEach(entry -> {
			switch (entry.getKey()) {
				case FhirConstants.PATIENT_REFERENCE_SEARCH_HANDLER:
					entry.getValue().forEach(
					    param -> handlePatientReference(criteriaContext, (ReferenceAndListParam) param.getParam()));
					break;
				case FhirConstants.CODED_SEARCH_HANDLER:
					entry.getValue().forEach(param -> handleCode(criteriaContext, (TokenAndListParam) param.getParam()));
					break;
				case FhirConstants.CONDITION_CLINICAL_STATUS_HANDLER:
					entry.getValue()
					        .forEach(param -> handleClinicalStatus(criteriaContext, (TokenAndListParam) param.getParam()));
					break;
				case FhirConstants.DATE_RANGE_SEARCH_HANDLER:
					entry.getValue().forEach(
					    param -> handleDateRange(criteriaContext, param.getPropertyName(), (DateRangeParam) param.getParam())
					            .ifPresent(criteriaContext::addPredicate));
					break;
				case FhirConstants.QUANTITY_SEARCH_HANDLER:
					entry.getValue()
					        .forEach(param -> handleOnsetAge(criteriaContext, (QuantityAndListParam) param.getParam()));
					break;
				case FhirConstants.COMMON_SEARCH_HANDLER:
					handleCommonSearchParameters(criteriaContext, entry.getValue()).ifPresent(criteriaContext::addPredicate);
					break;
			}
		});
	}
	
	private <U> void handleCode(OpenmrsFhirCriteriaContext<Condition, U> criteriaContext, TokenAndListParam code) {
		if (code != null) {
			From<?, ?> conditionJoin = criteriaContext.addJoin("condition", "condition");
			From<?, ?> codedJoin = criteriaContext.addJoin(conditionJoin, "coded", "cd");
			handleCodeableConcept(criteriaContext, code, codedJoin, "map", "term").ifPresent(criteriaContext::addPredicate);
		}
	}
	
	private <U> void handleClinicalStatus(OpenmrsFhirCriteriaContext<Condition, U> criteriaContext,
	        TokenAndListParam status) {
		handleAndListParam(criteriaContext.getCriteriaBuilder(), status,
		    tokenParam -> Optional.of(criteriaContext.getCriteriaBuilder()
		            .equal(criteriaContext.getRoot().get("clinicalStatus"), convertStatus(tokenParam.getValue()))))
		                    .ifPresent(criteriaContext::addPredicate);
	}
	
	private <U> void handleOnsetAge(OpenmrsFhirCriteriaContext<Condition, U> criteriaContext,
	        QuantityAndListParam onsetAge) {
		handleAndListParam(criteriaContext.getCriteriaBuilder(), onsetAge,
		    onsetAgeParam -> handleAgeByDateProperty(criteriaContext, "onsetDate", onsetAgeParam))
		            .ifPresent(criteriaContext::addPredicate);
	}
	
	@Override
	protected <T, U> Optional<Predicate> handleLastUpdated(@Nonnull OpenmrsFhirCriteriaContext<T, U> criteriaContext,
	        DateRangeParam param) {
		return super.handleLastUpdated(criteriaContext, param);
	}
	
	@Override
	protected <V, U> Path<?> paramToProp(@Nonnull OpenmrsFhirCriteriaContext<V, U> criteriaContext, @NonNull String param) {
		switch (param) {
			case org.hl7.fhir.r4.model.Condition.SP_ONSET_DATE:
				return criteriaContext.getRoot().get("onsetDate");
			case org.hl7.fhir.r4.model.Condition.SP_RECORDED_DATE:
				return criteriaContext.getRoot().get("dateCreated");
		}
		return super.paramToProp(criteriaContext, param);
	}
}
