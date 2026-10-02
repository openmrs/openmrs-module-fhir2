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

import static lombok.AccessLevel.PROTECTED;
import static org.openmrs.module.fhir2.FhirConstants.OPENMRS_FHIR_STRUCTURE_DEFINITION_PREFIX;

import javax.annotation.Nonnull;

import ca.uhn.fhir.rest.api.server.IBundleProvider;
import lombok.Getter;
import lombok.Setter;
import org.hl7.fhir.r4.model.EpisodeOfCare;
import org.openmrs.PatientProgram;
import org.openmrs.module.fhir2.api.dao.FhirEpisodeOfCareDao;
import org.openmrs.module.fhir2.api.search.param.SearchParameterMap;
import org.openmrs.module.fhir2.api.translators.EpisodeOfCareTranslator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Default handler for the FHIR {@link EpisodeOfCare} resource, mapping onto the OpenMRS
 * {@code PatientProgram} table. Single backing today — claims every incoming EpisodeOfCare. Carries
 * the CRUD wiring lifted from the original {@code FhirEpisodeOfCareServiceImpl}.
 * <p>
 * EpisodeOfCare is read-only in this module: its resource provider exposes only {@code @Read}, and
 * no {@code SearchQuery} is wired for this backing. The handler therefore opts out of search
 * fan-out via {@link #acceptsSearch(SearchParameterMap)} rather than pretending to support it.
 */
@Component
@Order(Ordered.LOWEST_PRECEDENCE)
public class PatientProgramBackedEpisodeOfCareHandler extends BaseFhirResourceHandler<EpisodeOfCare, PatientProgram> implements FhirResourceHandler<EpisodeOfCare> {
	
	private static final String IMPLICIT_PROFILE = OPENMRS_FHIR_STRUCTURE_DEFINITION_PREFIX + "/openmrs-episodeofcare";
	
	@Getter(value = PROTECTED)
	@Setter(value = PROTECTED, onMethod_ = @Autowired)
	private FhirEpisodeOfCareDao dao;
	
	@Getter(value = PROTECTED)
	@Setter(value = PROTECTED, onMethod_ = @Autowired)
	private EpisodeOfCareTranslator translator;
	
	@Nonnull
	@Override
	public String getImplicitProfile() {
		return IMPLICIT_PROFILE;
	}
	
	@Override
	public boolean canHandle(@Nonnull EpisodeOfCare resource) {
		return true;
	}
	
	/**
	 * Always {@code false} — this backing has no search wiring, so it never joins a fan-out search.
	 */
	@Override
	public boolean acceptsSearch(@Nonnull SearchParameterMap params) {
		return false;
	}
	
	@Override
	public IBundleProvider search(@Nonnull SearchParameterMap params) {
		throw new UnsupportedOperationException("Searching EpisodeOfCare is not supported");
	}
}
