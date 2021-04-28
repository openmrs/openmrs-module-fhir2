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

import ca.uhn.fhir.model.api.Include;
import ca.uhn.fhir.rest.api.server.IBundleProvider;
import ca.uhn.fhir.rest.param.DateRangeParam;
import ca.uhn.fhir.rest.param.ReferenceAndListParam;
import ca.uhn.fhir.rest.param.TokenAndListParam;
import ca.uhn.fhir.rest.server.exceptions.InvalidRequestException;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.hl7.fhir.r4.model.Encounter;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.api.FhirEncounterService;
import org.openmrs.module.fhir2.api.dao.FhirEncounterDao;
import org.openmrs.module.fhir2.api.search.SearchQuery;
import org.openmrs.module.fhir2.api.search.SearchQueryInclude;
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
	private FhirVisitServiceImpl visitService;
	
	@Autowired
	private SearchQueryInclude<Encounter> searchQueryInclude;
	
	@Autowired
	private SearchQuery<org.openmrs.Encounter, Encounter, FhirEncounterDao, EncounterTranslator<org.openmrs.Encounter>, SearchQueryInclude<Encounter>> searchQuery;
	
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
	public Encounter delete(@Nonnull String uuid) {
		
		if (uuid == null) {
			throw new InvalidRequestException("Uuid cannot be null.");
		}
		
		Encounter result;
		try {
			result = super.delete(uuid);
		}
		catch (ResourceNotFoundException e) {
			result = visitService.delete(uuid);
		}
		
		return result;
	}
	
	@Override
	@Transactional(readOnly = true)
	public IBundleProvider searchForEncounters(DateRangeParam date, ReferenceAndListParam location,
	        ReferenceAndListParam participant, ReferenceAndListParam subject, TokenAndListParam id,
	        DateRangeParam lastUpdated, HashSet<Include> includes, HashSet<Include> revIncludes) {
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.DATE_RANGE_SEARCH_HANDLER, date)
		        .addParameter(FhirConstants.LOCATION_REFERENCE_SEARCH_HANDLER, location)
		        .addParameter(FhirConstants.PARTICIPANT_REFERENCE_SEARCH_HANDLER, participant)
		        .addParameter(FhirConstants.PATIENT_REFERENCE_SEARCH_HANDLER, subject)
		        .addParameter(FhirConstants.COMMON_SEARCH_HANDLER, FhirConstants.ID_PROPERTY, id)
		        .addParameter(FhirConstants.COMMON_SEARCH_HANDLER, FhirConstants.LAST_UPDATED_PROPERTY, lastUpdated)
		        .addParameter(FhirConstants.INCLUDE_SEARCH_HANDLER, includes)
		        .addParameter(FhirConstants.REVERSE_INCLUDE_SEARCH_HANDLER, revIncludes);
		return searchQuery.getQueryResults(theParams, dao, translator, searchQueryInclude);
	}
}
