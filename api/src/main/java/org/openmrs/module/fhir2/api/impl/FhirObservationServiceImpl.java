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

import java.util.Optional;

import ca.uhn.fhir.rest.api.server.IBundleProvider;
import ca.uhn.fhir.rest.param.NumberParam;
import ca.uhn.fhir.rest.param.StringParam;
import ca.uhn.fhir.rest.server.exceptions.InvalidRequestException;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.hl7.fhir.r4.model.Observation;
import org.openmrs.Obs;
import org.openmrs.api.context.Context;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.api.FhirObservationService;
import org.openmrs.module.fhir2.api.dao.FhirObservationDao;
import org.openmrs.module.fhir2.api.search.SearchQuery;
import org.openmrs.module.fhir2.api.search.SearchQueryInclude;
import org.openmrs.module.fhir2.api.search.param.ObservationSearchParams;
import org.openmrs.module.fhir2.api.search.param.SearchParameterMap;
import org.openmrs.module.fhir2.api.translators.ObservationTranslator;
import org.openmrs.module.fhir2.api.translators.OpenmrsFhirTranslator;
import org.openmrs.module.fhir2.api.translators.UpdatableOpenmrsTranslator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nonnull;

@Component
@Transactional
@Getter(AccessLevel.PROTECTED)
@Setter(AccessLevel.PACKAGE)
public class FhirObservationServiceImpl extends BaseFhirService<Observation, org.openmrs.Obs> implements FhirObservationService {
	
	@Autowired
	private FhirObservationDao dao;
	
	@Autowired
	private ObservationTranslator translator;
	
	@Autowired
	private SearchQueryInclude<Observation> searchQueryInclude;
	
	@Autowired
	private SearchQuery<Obs, Observation, FhirObservationDao, ObservationTranslator, SearchQueryInclude<Observation>> searchQuery;
	
	@Override
	@Transactional(readOnly = true)
	public IBundleProvider searchForObservations(ObservationSearchParams observationSearchParams) {
		return searchQuery.getQueryResults(observationSearchParams.toSearchParameterMap(), dao, translator,
		    searchQueryInclude);
	}
	
	@Override
	@Transactional(readOnly = true)
	public IBundleProvider getLastnObservations(NumberParam max, ObservationSearchParams observationSearchParams) {
		
		SearchParameterMap theParams = observationSearchParams.toSearchParameterMap()
		        .addParameter(FhirConstants.LASTN_OBSERVATION_SEARCH_HANDLER, new StringParam())
		        .addParameter(FhirConstants.MAX_SEARCH_HANDLER, Optional.ofNullable(max).orElse(new NumberParam(1)));
		
		return searchQuery.getQueryResults(theParams, dao, translator, searchQueryInclude);
	}
	
	@Override
	@Transactional(readOnly = true)
	public IBundleProvider getLastnEncountersObservations(NumberParam max, ObservationSearchParams observationSearchParams) {
		
		SearchParameterMap theParams = observationSearchParams.toSearchParameterMap()
		        .addParameter(FhirConstants.LASTN_ENCOUNTERS_SEARCH_HANDLER, new StringParam())
		        .addParameter(FhirConstants.MAX_SEARCH_HANDLER, Optional.ofNullable(max).orElse(new NumberParam(1)));
		
		return searchQuery.getQueryResults(theParams, dao, translator, searchQueryInclude);
	}
	
	@Override
	public Observation update(@Nonnull String uuid, @Nonnull Observation updatedObservation) {
		if (uuid == null) {
			throw new InvalidRequestException("Uuid cannot be null.");
		}
		
		if (updatedObservation == null) {
			throw new InvalidRequestException("Resource cannot be null.");
		}
		
		if (updatedObservation.getId() == null) {
			throw new InvalidRequestException("Observation resource is missing id.");
		}
		
		if (!updatedObservation.getIdElement().getIdPart().equals(uuid)) {
			throw new InvalidRequestException("Observation id does not match resource id.");
		}
		
		Obs existingObservation = dao.get(uuid);
		
		if (existingObservation == null) {
			throw resourceNotFound(uuid);
		}
		
		return applyUpdate(existingObservation, updatedObservation);
	}
	
	@Override
	protected Observation applyUpdate(org.openmrs.Obs existingObject, Observation updatedResource) {
		OpenmrsFhirTranslator<Obs, Observation> translator = getTranslator();
		
		org.openmrs.Obs updatedObject;
		if (translator instanceof UpdatableOpenmrsTranslator) {
			UpdatableOpenmrsTranslator<org.openmrs.Obs, Observation> updatableOpenmrsTranslator = (UpdatableOpenmrsTranslator<org.openmrs.Obs, Observation>) translator;
			updatedObject = updatableOpenmrsTranslator.toOpenmrsType(existingObject, updatedResource);
		} else {
			updatedObject = translator.toOpenmrsType(updatedResource);
		}
		
		validateObject(updatedObject);
		
		return translator.toFhirResource(Context.getObsService().saveObs(updatedObject, "Updated via the FHIR2 API"));
	}
}
