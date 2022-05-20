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

import java.util.Collections;
import java.util.HashSet;

import ca.uhn.fhir.model.api.Include;
import ca.uhn.fhir.rest.api.server.IBundleProvider;
import ca.uhn.fhir.rest.param.InternalCodingDt;
import ca.uhn.fhir.rest.param.TokenAndListParam;
import ca.uhn.fhir.rest.param.TokenParam;
import ca.uhn.fhir.rest.server.SimpleBundleProvider;
import ca.uhn.fhir.rest.server.exceptions.InvalidRequestException;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.hl7.fhir.r4.model.Encounter;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.api.FhirEncounterService;
import org.openmrs.module.fhir2.api.FhirGlobalPropertyService;
import org.openmrs.module.fhir2.api.FhirVisitService;
import org.openmrs.module.fhir2.api.dao.FhirEncounterDao;
import org.openmrs.module.fhir2.api.search.SearchQuery;
import org.openmrs.module.fhir2.api.search.SearchQueryInclude;
import org.openmrs.module.fhir2.api.search.TwoSearchQueryBundleProvider;
import org.openmrs.module.fhir2.api.search.param.EncounterSearchParams;
import org.openmrs.module.fhir2.api.search.param.SearchParameterMap;
import org.openmrs.module.fhir2.api.translators.EncounterTranslator;
import org.openmrs.module.fhir2.api.util.FhirUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional
@Setter(AccessLevel.PACKAGE)
@Getter(AccessLevel.PROTECTED)
public class FhirEncounterServiceImpl extends BaseFhirService<Encounter, org.openmrs.Encounter> implements FhirEncounterService {
	
	@Autowired
	private FhirEncounterDao dao;
	
	@Autowired
	private EncounterTranslator<org.openmrs.Encounter> translator;
	
	@Autowired
	private SearchQueryInclude<Encounter> searchQueryInclude;
	
	@Autowired
	private FhirGlobalPropertyService globalPropertyService;
	
	@Autowired
	private SearchQuery<org.openmrs.Encounter, Encounter, FhirEncounterDao, EncounterTranslator<org.openmrs.Encounter>, SearchQueryInclude<Encounter>> searchQuery;
	
	@Autowired
	private FhirVisitService visitService;
	
	@Override
	public Encounter get(@Nonnull String uuid) {
		
		Encounter result;
		try {
			result = super.get(uuid);
		}
		catch (ResourceNotFoundException e) {
			result = visitService.get(uuid);
		}
		
		return result;
	}
	
	@Override
	public Encounter create(@Nonnull Encounter encounter) {
		
		if (encounter == null) {
			throw new InvalidRequestException("Encounter cannot be null");
		}
		
		FhirUtils.OpenmrsEncounterType result = FhirUtils.getOpenmrsEncounterType(encounter).orElse(null);
		
		if (result == null) {
			throw new InvalidRequestException("Invalid type of request");
		}
		
		if (result.equals(FhirUtils.OpenmrsEncounterType.ENCOUNTER)) {
			return super.create(encounter);
		}
		
		if (result.equals(FhirUtils.OpenmrsEncounterType.VISIT)) {
			return visitService.create(encounter);
		}
		
		throw new InvalidRequestException("Invalid type of request");
	}
	
	@Override
	public Encounter update(@Nonnull String uuid, @Nonnull Encounter encounter) {
		
		if (uuid == null) {
			throw new InvalidRequestException("Uuid cannot be null.");
		}
		
		FhirUtils.OpenmrsEncounterType result = FhirUtils.getOpenmrsEncounterType(encounter).orElse(null);
		
		if (result == null) {
			throw new InvalidRequestException("Invalid type of request");
		}
		
		if (result.equals(FhirUtils.OpenmrsEncounterType.ENCOUNTER)) {
			return super.update(uuid, encounter);
		}
		
		if (result.equals(FhirUtils.OpenmrsEncounterType.VISIT)) {
			return visitService.update(uuid, encounter);
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
			visitService.delete(uuid);
		}
	}
	
	@Override
	@Transactional(readOnly = true)
	public IBundleProvider searchForEncounters(EncounterSearchParams searchParameters) {
		SearchParameterMap theParams = new SearchParameterMap()
		        .addParameter(FhirConstants.DATE_RANGE_SEARCH_HANDLER, searchParameters.getDate())
		        .addParameter(FhirConstants.LOCATION_REFERENCE_SEARCH_HANDLER, searchParameters.getLocation())
		        .addParameter(FhirConstants.PARTICIPANT_REFERENCE_SEARCH_HANDLER, searchParameters.getParticipant())
		        .addParameter(FhirConstants.PATIENT_REFERENCE_SEARCH_HANDLER, searchParameters.getSubject())
		        .addParameter(FhirConstants.ENCOUNTER_TYPE_REFERENCE_SEARCH_HANDLER, searchParameters.getEncounterType())
		        .addParameter(FhirConstants.COMMON_SEARCH_HANDLER, FhirConstants.ID_PROPERTY, searchParameters.getId())
		        .addParameter(FhirConstants.COMMON_SEARCH_HANDLER, FhirConstants.LAST_UPDATED_PROPERTY,
		            searchParameters.getLastUpdated())
		        .addParameter(FhirConstants.INCLUDE_SEARCH_HANDLER, searchParameters.getIncludes())
		        .addParameter(FhirConstants.REVERSE_INCLUDE_SEARCH_HANDLER, searchParameters.getRevIncludes())
		        .addParameter(FhirConstants.HAS_SEARCH_HANDLER, searchParameters.getHasAndListParam())
		        .setSortSpec(searchParameters.getSort());
		
		IBundleProvider visitBundle = null;
		IBundleProvider encounterBundle = null;
		
		if (shouldSearchExplicitlyFor(searchParameters.getTag(), "visit")) {
			visitBundle = visitService.searchForVisits(theParams);
		}
		
		if (shouldSearchExplicitlyFor(searchParameters.getTag(), "encounter")) {
			encounterBundle = searchQuery.getQueryResults(theParams, dao, translator, searchQueryInclude);
		}
		
		if (encounterBundle != null && visitBundle != null) {
			return new TwoSearchQueryBundleProvider(visitBundle, encounterBundle, globalPropertyService);
		} else if (encounterBundle == null && visitBundle != null) {
			return visitBundle;
		}
		
		return encounterBundle == null ? new SimpleBundleProvider() : encounterBundle;
	}
	
	/**
	 * @return true if the given tokenAndListParam contains the matching OpenMRS encounter type tag.
	 */
	protected boolean shouldSearchExplicitlyFor(TokenAndListParam tokenAndListParam, @Nonnull String valueToCheck) {
		if (tokenAndListParam == null || tokenAndListParam.size() == 0 || valueToCheck.isEmpty()) {
			return true;
		}
		
		return tokenAndListParam.getValuesAsQueryTokens().stream()
		        .anyMatch(tokenOrListParam -> tokenOrListParam.doesCodingListMatch(Collections
		                .singletonList(new InternalCodingDt(FhirConstants.OPENMRS_FHIR_EXT_ENCOUNTER_TAG, valueToCheck))));
	}
	
	@Override
	@Transactional(readOnly = true)
	public IBundleProvider getEncounterEverything(TokenParam encounterId) {
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.EVERYTHING_SEARCH_HANDLER, "")
		        .addParameter(FhirConstants.COMMON_SEARCH_HANDLER, FhirConstants.ID_PROPERTY,
		            new TokenAndListParam().addAnd(encounterId));
		
		populateReverseIncludeForEverythingOperationParams(theParams);
		populateIncludeForEverythingOperationParams(theParams);
		
		IBundleProvider visitBundle = visitService.searchForVisits(theParams);
		IBundleProvider encounterBundle = searchQuery.getQueryResults(theParams, dao, translator, searchQueryInclude);
		
		if (!encounterBundle.isEmpty() && !visitBundle.isEmpty()) {
			return new TwoSearchQueryBundleProvider(encounterBundle, visitBundle, globalPropertyService);
		} else if (encounterBundle.isEmpty() && !visitBundle.isEmpty()) {
			return visitBundle;
		}
		
		return encounterBundle;
	}
	
	private void populateReverseIncludeForEverythingOperationParams(SearchParameterMap theParams) {
		HashSet<Include> revIncludes = new HashSet<>();
		
		revIncludes.add(new Include(FhirConstants.OBSERVATION + ":" + FhirConstants.INCLUDE_ENCOUNTER_PARAM));
		revIncludes.add(new Include(FhirConstants.DIAGNOSTIC_REPORT + ":" + FhirConstants.INCLUDE_ENCOUNTER_PARAM));
		revIncludes.add(new Include(FhirConstants.MEDICATION_REQUEST + ":" + FhirConstants.INCLUDE_ENCOUNTER_PARAM));
		revIncludes.add(new Include(FhirConstants.SERVICE_REQUEST + ":" + FhirConstants.INCLUDE_ENCOUNTER_PARAM));
		
		theParams.addParameter(FhirConstants.REVERSE_INCLUDE_SEARCH_HANDLER, revIncludes);
	}
	
	private void populateIncludeForEverythingOperationParams(SearchParameterMap theParams) {
		HashSet<Include> includes = new HashSet<>();
		
		includes.add(new Include(FhirConstants.ENCOUNTER + ":" + FhirConstants.INCLUDE_PATIENT_PARAM));
		includes.add(new Include(FhirConstants.ENCOUNTER + ":" + FhirConstants.INCLUDE_LOCATION_PARAM));
		includes.add(new Include(FhirConstants.ENCOUNTER + ":" + FhirConstants.INCLUDE_PARTICIPANT_PARAM));
		
		theParams.addParameter(FhirConstants.INCLUDE_SEARCH_HANDLER, includes);
	}
}
