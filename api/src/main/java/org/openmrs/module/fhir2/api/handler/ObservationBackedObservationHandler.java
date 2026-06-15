/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.fhir2.api.handler;

import static org.openmrs.module.fhir2.FhirConstants.OPENMRS_FHIR_STRUCTURE_DEFINITION_PREFIX;
import static org.openmrs.module.fhir2.FhirConstants.OPENMRS_HANDLER_OBSERVATION_OBSERVATION_BACKING_KEY;

import javax.annotation.Nonnull;

import ca.uhn.fhir.rest.api.server.IBundleProvider;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.hl7.fhir.r4.model.Observation;
import org.openmrs.Obs;
import org.openmrs.api.ObsService;
import org.openmrs.module.fhir2.api.dao.FhirObservationDao;
import org.openmrs.module.fhir2.api.search.SearchQuery;
import org.openmrs.module.fhir2.api.search.SearchQueryInclude;
import org.openmrs.module.fhir2.api.search.param.SearchParameterMap;
import org.openmrs.module.fhir2.api.translators.ObservationTranslator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Default handler for the FHIR {@link Observation} resource, mapping onto the OpenMRS {@code Obs}
 * table. Single backing today — claims every incoming Observation. Carries the CRUD wiring lifted
 * from the original {@code FhirObservationServiceImpl} and the {@code applyUpdate} override that
 * routes saves through {@code ObsService} to handle OpenMRS's obs-immutability semantics.
 */
@Component
@Order(Ordered.LOWEST_PRECEDENCE)
public class ObservationBackedObservationHandler extends BaseFhirResourceHandler<Observation, Obs> implements FhirResourceHandler<Observation> {
	
	private static final String IMPLICIT_PROFILE = OPENMRS_FHIR_STRUCTURE_DEFINITION_PREFIX + "/openmrs-observation";
	
	private static final String BACKING_KEY = OPENMRS_HANDLER_OBSERVATION_OBSERVATION_BACKING_KEY;
	
	@Getter(value = AccessLevel.PROTECTED)
	@Setter(value = AccessLevel.PACKAGE, onMethod_ = @Autowired)
	private FhirObservationDao dao;
	
	@Getter(value = AccessLevel.PROTECTED)
	@Setter(value = AccessLevel.PACKAGE, onMethod_ = @Autowired)
	private ObservationTranslator translator;
	
	@Getter(value = AccessLevel.PROTECTED)
	@Setter(value = AccessLevel.PACKAGE, onMethod_ = @Autowired)
	private SearchQueryInclude<Observation> searchQueryInclude;
	
	@Getter(value = AccessLevel.PROTECTED)
	@Setter(value = AccessLevel.PACKAGE, onMethod_ = @Autowired)
	private SearchQuery<Obs, Observation, FhirObservationDao, ObservationTranslator, SearchQueryInclude<Observation>> searchQuery;
	
	@Getter(value = AccessLevel.PROTECTED)
	@Setter(value = AccessLevel.PACKAGE, onMethod_ = @Autowired)
	private ObsService obsService;
	
	@Nonnull
	@Override
	public String getImplicitProfile() {
		return IMPLICIT_PROFILE;
	}
	
	@Nonnull
	@Override
	public String getBackingKey() {
		return BACKING_KEY;
	}
	
	@Override
	public boolean canHandle(@Nonnull Observation resource) {
		return true;
	}
	
	@Override
	public IBundleProvider search(@Nonnull SearchParameterMap params) {
		return searchQuery.getQueryResults(params, dao, translator, searchQueryInclude);
	}
	
	@Override
	protected Observation applyUpdate(Obs existingObject, Observation updatedResource) {
		// Obs in OpenMRS are conceptually immutable — ObsService.saveObs voids the previous row and
		// writes a new one, which is what callers expect for FHIR Observation PUT semantics.
		ObservationTranslator translator = getTranslator();
		Obs updatedObject = translator.toOpenmrsType(existingObject, updatedResource);
		
		validateObject(updatedObject);
		
		return translator.toFhirResource(obsService.saveObs(updatedObject, "Updated via the FHIR2 API"));
	}
}
