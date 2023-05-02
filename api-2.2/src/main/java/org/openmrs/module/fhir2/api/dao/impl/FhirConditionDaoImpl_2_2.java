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

import static org.hibernate.criterion.Restrictions.eq;

import javax.annotation.Nonnull;

import java.util.List;
import java.util.Optional;

import ca.uhn.fhir.rest.param.DateRangeParam;
import ca.uhn.fhir.rest.param.QuantityAndListParam;
import ca.uhn.fhir.rest.param.ReferenceAndListParam;
import ca.uhn.fhir.rest.param.TokenAndListParam;
import lombok.AccessLevel;
import lombok.Setter;
import org.hibernate.Criteria;
import org.hibernate.criterion.Criterion;
import org.openmrs.Condition;
import org.openmrs.ConditionClinicalStatus;
import org.openmrs.annotation.Authorized;
import org.openmrs.annotation.OpenmrsProfile;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.api.dao.FhirConditionDao;
import org.openmrs.module.fhir2.api.search.param.SearchParameterMap;
import org.openmrs.util.PrivilegeConstants;
import org.springframework.stereotype.Component;

@Component
@Setter(AccessLevel.PROTECTED)
@OpenmrsProfile(openmrsPlatformVersion = "2.2.* - 2.*")
public class FhirConditionDaoImpl_2_2 extends BaseFhirDao<Condition> implements FhirConditionDao<Condition> {
	
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
	protected void setupSearchParams(Criteria criteria, SearchParameterMap theParams) {
		theParams.getParameters().forEach(entry -> {
			switch (entry.getKey()) {
				case FhirConstants.PATIENT_REFERENCE_SEARCH_HANDLER:
					entry.getValue()
					        .forEach(param -> handlePatientReference(criteria, (ReferenceAndListParam) param.getParam()));
					break;
				case FhirConstants.CODED_SEARCH_HANDLER:
					entry.getValue().forEach(param -> handleCode(criteria, (TokenAndListParam) param.getParam()));
					break;
				case FhirConstants.CONDITION_CLINICAL_STATUS_HANDLER:
					entry.getValue().forEach(param -> handleClinicalStatus(criteria, (TokenAndListParam) param.getParam()));
					break;
				case FhirConstants.DATE_RANGE_SEARCH_HANDLER:
					entry.getValue()
					        .forEach(param -> handleDateRange(param.getPropertyName(), (DateRangeParam) param.getParam())
					                .ifPresent(criteria::add));
					break;
				case FhirConstants.QUANTITY_SEARCH_HANDLER:
					entry.getValue().forEach(param -> handleOnsetAge(criteria, (QuantityAndListParam) param.getParam()));
					break;
				case FhirConstants.COMMON_SEARCH_HANDLER:
					handleCommonSearchParameters(entry.getValue()).ifPresent(criteria::add);
					break;
			}
		});
	}
	
	private void handleCode(Criteria criteria, TokenAndListParam code) {
		if (code != null) {
			criteria.createAlias("condition.coded", "cd");
			handleCodeableConcept(criteria, code, "cd", "map", "term").ifPresent(criteria::add);
		}
	}
	
	private void handleClinicalStatus(Criteria criteria, TokenAndListParam status) {
		handleAndListParam(status, tokenParam -> Optional.of(eq("clinicalStatus", convertStatus(tokenParam.getValue()))))
		        .ifPresent(criteria::add);
	}
	
	private void handleOnsetAge(Criteria criteria, QuantityAndListParam onsetAge) {
		handleAndListParam(onsetAge, onsetAgeParam -> handleAgeByDateProperty("onsetDate", onsetAgeParam))
		        .ifPresent(criteria::add);
	}
	
	@Override
	protected Optional<Criterion> handleLastUpdated(DateRangeParam param) {
		return super.handleLastUpdatedImmutable(param);
	}
	
	@Override
	protected String paramToProp(@Nonnull String param) {
		switch (param) {
			case org.hl7.fhir.r4.model.Condition.SP_ONSET_DATE:
				return "onsetDate";
			case org.hl7.fhir.r4.model.Condition.SP_RECORDED_DATE:
				return "dateCreated";
		}
		
		return super.paramToProp(param);
	}
}
