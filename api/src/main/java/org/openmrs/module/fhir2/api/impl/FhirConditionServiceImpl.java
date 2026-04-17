/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.fhir2.api.impl;

import javax.annotation.Nonnull;

import java.util.HashSet;

import ca.uhn.fhir.rest.api.server.IBundleProvider;
import ca.uhn.fhir.rest.param.TokenAndListParam;
import ca.uhn.fhir.rest.param.TokenOrListParam;
import ca.uhn.fhir.rest.param.TokenParam;
import ca.uhn.fhir.rest.server.exceptions.InvalidRequestException;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.r4.model.Condition;
import org.openmrs.api.APIAuthenticationException;
import org.openmrs.api.context.Context;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.api.FhirConditionService;
import org.openmrs.module.fhir2.api.FhirDiagnosisService;
import org.openmrs.module.fhir2.api.FhirGlobalPropertyService;
import org.openmrs.module.fhir2.api.dao.FhirConditionDao;
import org.openmrs.module.fhir2.api.search.SearchQuery;
import org.openmrs.module.fhir2.api.search.SearchQueryInclude;
import org.openmrs.module.fhir2.api.search.TwoSearchQueryBundleProvider;
import org.openmrs.module.fhir2.api.search.param.ConditionSearchParams;
import org.openmrs.module.fhir2.api.search.param.DiagnosisSearchParams;
import org.openmrs.module.fhir2.api.search.param.SearchParameterMap;
import org.openmrs.module.fhir2.api.translators.ConditionTranslator;
import org.openmrs.module.fhir2.api.util.FhirUtils;
import org.openmrs.util.PrivilegeConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class FhirConditionServiceImpl extends BaseFhirService<Condition, org.openmrs.Condition> implements FhirConditionService {
	
	@Getter(AccessLevel.PROTECTED)
	@Setter(value = AccessLevel.PROTECTED, onMethod_ = @Autowired)
	private FhirConditionDao dao;
	
	@Getter(AccessLevel.PROTECTED)
	@Setter(value = AccessLevel.PROTECTED, onMethod_ = @Autowired)
	private ConditionTranslator<org.openmrs.Condition> translator;
	
	@Getter(value = AccessLevel.PROTECTED)
	@Setter(value = AccessLevel.PROTECTED, onMethod_ = @Autowired)
	private SearchQueryInclude<Condition> searchQueryInclude;
	
	@Getter(value = AccessLevel.PROTECTED)
	@Setter(value = AccessLevel.PROTECTED, onMethod_ = @Autowired)
	private FhirGlobalPropertyService globalPropertyService;
	
	@Getter(value = AccessLevel.PROTECTED)
	@Setter(value = AccessLevel.PROTECTED, onMethod_ = @Autowired)
	private SearchQuery<org.openmrs.Condition, Condition, FhirConditionDao, ConditionTranslator<org.openmrs.Condition>, SearchQueryInclude<Condition>> searchQuery;
	
	@Getter(value = AccessLevel.PROTECTED)
	@Setter(value = AccessLevel.PROTECTED, onMethod_ = @Autowired)
	private FhirDiagnosisService diagnosisService;
	
	@Override
	public Condition get(@Nonnull String uuid) {
		Condition result;
		try {
			result = super.get(uuid);
		}
		catch (ResourceNotFoundException e) {
			result = diagnosisService.get(uuid);
		}
		
		return result;
	}
	
	@Override
	public Condition create(@Nonnull Condition condition) {
		if (condition == null) {
			throw new InvalidRequestException("Condition cannot be null");
		}
		
		FhirUtils.OpenmrsConditionType result = FhirUtils.getOpenmrsConditionType(condition).orElse(null);
		
		if (result == null) {
			throw new InvalidRequestException(
			        "Condition.category provided must be one of problem-list-item or encounter-diagnosis");
		}
		
		if (result.equals(FhirUtils.OpenmrsConditionType.CONDITION)) {
			return super.create(condition);
		} else if (result.equals(FhirUtils.OpenmrsConditionType.DIAGNOSIS)) {
			return diagnosisService.create(condition);
		}
		
		throw new InvalidRequestException("Invalid type of request");
	}
	
	@Override
	public Condition update(@Nonnull String uuid, @Nonnull Condition condition) {
		if (uuid == null) {
			throw new InvalidRequestException("Uuid cannot be null.");
		}
		
		if (condition == null) {
			throw new InvalidRequestException("Condition cannot be null");
		}
		
		FhirUtils.OpenmrsConditionType result = FhirUtils.getOpenmrsConditionType(condition).orElse(null);
		
		if (result.equals(FhirUtils.OpenmrsConditionType.DIAGNOSIS)) {
			return diagnosisService.update(uuid, condition);
		} else {
			return super.update(uuid, condition);
		}
		
	}
	
	@Override
	public void delete(@Nonnull String uuid) {
		if (uuid == null) {
			throw new InvalidRequestException("Uuid cannot be null.");
		}
		
		try {
			super.delete(uuid);
		}
		catch (ResourceNotFoundException e) {
			diagnosisService.delete(uuid);
		}
	}
	
	@Override
	public IBundleProvider searchConditions(ConditionSearchParams conditionSearchParams) {
		SearchParameterMap theParams = conditionSearchParams.toSearchParameterMap();
		
		DiagnosisSearchParams diagnosisSearchParams = DiagnosisSearchParams.builder()
		        .patientParam(conditionSearchParams.getPatientParam()).code(conditionSearchParams.getCode())
		        .clinicalStatus(conditionSearchParams.getClinicalStatus()).onsetDate(conditionSearchParams.getOnsetDate())
		        .onsetAge(conditionSearchParams.getOnsetAge()).recordedDate(conditionSearchParams.getRecordedDate())
		        .category(conditionSearchParams.getCategory()).id(conditionSearchParams.getId())
		        .lastUpdated(conditionSearchParams.getLastUpdated()).sort(conditionSearchParams.getSort())
		        .includes(
		            conditionSearchParams.getIncludes() == null ? null : new HashSet<>(conditionSearchParams.getIncludes()))
		        .revIncludes(conditionSearchParams.getRevIncludes() == null ? null
		                : new HashSet<>(conditionSearchParams.getRevIncludes()))
		        .build();
		
		IBundleProvider diagnosisBundle = null;
		IBundleProvider conditionBundle = null;
		
		boolean isSearchingForDiagnoses = shouldSearchExplicitlyFor(conditionSearchParams.getCategory(),
		    FhirConstants.CONDITION_CATEGORY_CODE_DIAGNOSIS);
		boolean canSearchForDiagnoses = Context.hasPrivilege(PrivilegeConstants.GET_DIAGNOSES);
		boolean isSearchingForConditions = shouldSearchExplicitlyFor(conditionSearchParams.getCategory(),
		    FhirConstants.CONDITION_CATEGORY_CODE_CONDITION);
		boolean canSearchForConditions = Context.hasPrivilege(PrivilegeConstants.GET_CONDITIONS);
		
		if ((isSearchingForDiagnoses && !canSearchForDiagnoses && isSearchingForConditions && !canSearchForConditions)
		        || (!isSearchingForDiagnoses && !isSearchingForConditions && !canSearchForDiagnoses
		                && !canSearchForConditions)) {
			throw new APIAuthenticationException(Context.getMessageSourceService().getMessage("error.privilegesRequired",
			    new Object[] { StringUtils.join(PrivilegeConstants.GET_CONDITIONS, PrivilegeConstants.GET_DIAGNOSES, ',') },
			    Context.getLocale()));
		} else if (isSearchingForDiagnoses && !canSearchForDiagnoses) {
			throw new APIAuthenticationException(Context.getMessageSourceService().getMessage("error.privilegesRequired",
			    new Object[] { PrivilegeConstants.GET_DIAGNOSES }, Context.getLocale()));
		} else if (isSearchingForConditions && !canSearchForConditions) {
			throw new APIAuthenticationException(Context.getMessageSourceService().getMessage("error.privilegesRequired",
			    new Object[] { PrivilegeConstants.GET_CONDITIONS }, Context.getLocale()));
		}
		
		if ((isSearchingForDiagnoses || !isSearchingForConditions) && canSearchForDiagnoses) {
			diagnosisBundle = diagnosisService.searchDiagnoses(diagnosisSearchParams);
		}
		
		if ((isSearchingForConditions || !isSearchingForDiagnoses) && canSearchForConditions) {
			conditionBundle = searchQuery.getQueryResults(theParams, dao, translator, searchQueryInclude);
		}
		
		if (conditionBundle != null && diagnosisBundle != null) {
			return new TwoSearchQueryBundleProvider(conditionBundle, diagnosisBundle, globalPropertyService);
		} else if (conditionBundle == null && diagnosisBundle != null) {
			return diagnosisBundle;
		}
		
		return conditionBundle;
	}
	
	/**
	 * @return true if category param is provided with correct system and code. Also returns true if
	 *         nothing is provided.
	 */
	protected boolean shouldSearchExplicitlyFor(TokenAndListParam tokenAndListParam, @Nonnull String valueToCheck) {
		if (tokenAndListParam == null || tokenAndListParam.size() == 0 || valueToCheck.isEmpty()) {
			return false;
		}
		
		for (TokenOrListParam orList : tokenAndListParam.getValuesAsQueryTokens()) {
			for (TokenParam tp : orList.getValuesAsQueryTokens()) {
				String sys = tp.getSystem();
				String code = tp.getValue();
				if (sys != null && !sys.isEmpty() && FhirConstants.CONDITION_CATEGORY_SYSTEM_URI.equals(sys)
				        && valueToCheck.equals(code)) {
					return true;
				}
			}
		}
		
		return false;
	}
}
