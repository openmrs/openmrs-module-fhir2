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

import ca.uhn.fhir.rest.api.server.IBundleProvider;
import ca.uhn.fhir.rest.param.InternalCodingDt;
import ca.uhn.fhir.rest.param.TokenAndListParam;
import ca.uhn.fhir.rest.server.SimpleBundleProvider;
import ca.uhn.fhir.rest.server.exceptions.InvalidRequestException;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.hl7.fhir.r4.model.Condition;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.api.FhirConditionService;
import org.openmrs.module.fhir2.api.FhirDiagnosisService;
import org.openmrs.module.fhir2.api.FhirGlobalPropertyService;
import org.openmrs.module.fhir2.api.dao.FhirConditionDao;
import org.openmrs.module.fhir2.api.search.SearchQuery;
import org.openmrs.module.fhir2.api.search.SearchQueryInclude;
import org.openmrs.module.fhir2.api.search.TwoSearchQueryBundleProvider;
import org.openmrs.module.fhir2.api.search.param.ConditionSearchParams;
import org.openmrs.module.fhir2.api.search.param.SearchParameterMap;
import org.openmrs.module.fhir2.api.translators.ConditionTranslator;
import org.openmrs.module.fhir2.api.util.FhirUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import java.util.Collections;

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
	@Setter(value = AccessLevel.PACKAGE, onMethod_ = @Autowired)
	private FhirGlobalPropertyService globalPropertyService;

	@Getter(value = AccessLevel.PROTECTED)
	@Setter(value = AccessLevel.PROTECTED, onMethod_ = @Autowired)
	private SearchQuery<org.openmrs.Condition, Condition, FhirConditionDao, ConditionTranslator<org.openmrs.Condition>, SearchQueryInclude<Condition>> searchQuery;

	@Getter(value = AccessLevel.PROTECTED)
	@Setter(value = AccessLevel.PACKAGE, onMethod_ = @Autowired)
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
			throw new InvalidRequestException("Invalid type of request");
		}

		if (result.equals(FhirUtils.OpenmrsConditionType.CONDITION)) {
			return super.create(condition);
		}

		if (result.equals(FhirUtils.OpenmrsConditionType.DIAGNOSIS)) {
			return diagnosisService.create(condition);
		}

		throw new InvalidRequestException("Invalid type of request");
	}

	@Override
	public Condition update(@Nonnull String uuid, @Nonnull Condition condition) {

		if (uuid == null) {
			throw new InvalidRequestException("Uuid cannot be null.");
		}

		FhirUtils.OpenmrsConditionType result = FhirUtils.getOpenmrsConditionType(condition).orElse(null);

		if (result == null) {
			throw new InvalidRequestException("Invalid type of request");
		}

		if (result.equals(FhirUtils.OpenmrsConditionType.CONDITION)) {
			return super.update(uuid, condition);
		}

		if (result.equals(FhirUtils.OpenmrsConditionType.DIAGNOSIS)) {
			return diagnosisService.update(uuid, condition);
		}

		throw new InvalidRequestException("Invalid type of request");
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

		IBundleProvider diagnosisBundle = null;
		IBundleProvider conditionBundle = null;

		if (shouldSearchExplicitlyFor(conditionSearchParams.getTag(), "diagnosis")) {
			diagnosisBundle = diagnosisService.searchDiagnoses(theParams);
		}

		if (shouldSearchExplicitlyFor(conditionSearchParams.getTag(), "condition")) {
			conditionBundle = searchQuery.getQueryResults(theParams, dao, translator, searchQueryInclude);
		}

		if (conditionBundle != null && diagnosisBundle != null) {
			return new TwoSearchQueryBundleProvider(diagnosisBundle, conditionBundle, globalPropertyService);
		} else if (conditionBundle == null && diagnosisBundle != null) {
			return diagnosisBundle;
		}

		return conditionBundle == null ? new SimpleBundleProvider() : conditionBundle;
	}

	/**
	 * @return true if the given tokenAndListParam contains the matching OpenMRS condition type tag.
	 */
	protected boolean shouldSearchExplicitlyFor(TokenAndListParam tokenAndListParam, @Nonnull String valueToCheck) {
		if (tokenAndListParam == null || tokenAndListParam.size() == 0 || valueToCheck.isEmpty()) {
			return true;
		}

		return tokenAndListParam.getValuesAsQueryTokens().stream()
				.anyMatch(tokenOrListParam -> tokenOrListParam.doesCodingListMatch(Collections
						.singletonList(new InternalCodingDt(FhirConstants.OPENMRS_FHIR_EXT_CONDITION_TAG, valueToCheck))));
	}
	
}
