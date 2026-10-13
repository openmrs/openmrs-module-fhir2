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

import javax.annotation.Nonnull;

import ca.uhn.fhir.rest.api.server.IBundleProvider;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.hl7.fhir.r4.model.AllergyIntolerance;
import org.openmrs.Allergy;
import org.openmrs.module.fhir2.api.dao.FhirAllergyIntoleranceDao;
import org.openmrs.module.fhir2.api.search.SearchQuery;
import org.openmrs.module.fhir2.api.search.SearchQueryInclude;
import org.openmrs.module.fhir2.api.search.param.SearchParameterMap;
import org.openmrs.module.fhir2.api.translators.AllergyIntoleranceTranslator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Default handler for the FHIR {@link AllergyIntolerance} resource, mapping onto the OpenMRS
 * {@code Allergy} table. Single backing today — claims every incoming AllergyIntolerance. Carries
 * the CRUD wiring lifted from the original {@code FhirAllergyIntoleranceServiceImpl}.
 */
@Component
@Order(Ordered.LOWEST_PRECEDENCE)
public class AllergyBackedAllergyIntoleranceHandler extends BaseFhirResourceHandler<AllergyIntolerance, Allergy> implements FhirResourceHandler<AllergyIntolerance> {
	
	private static final String IMPLICIT_PROFILE = OPENMRS_FHIR_STRUCTURE_DEFINITION_PREFIX + "/openmrs-allergyintolerance";
	
	@Getter(value = AccessLevel.PROTECTED)
	@Setter(value = AccessLevel.PACKAGE, onMethod_ = @Autowired)
	private FhirAllergyIntoleranceDao dao;
	
	@Getter(value = AccessLevel.PROTECTED)
	@Setter(value = AccessLevel.PACKAGE, onMethod_ = @Autowired)
	private AllergyIntoleranceTranslator translator;
	
	@Getter(value = AccessLevel.PROTECTED)
	@Setter(value = AccessLevel.PACKAGE, onMethod_ = @Autowired)
	private SearchQueryInclude<AllergyIntolerance> searchQueryInclude;
	
	@Getter(value = AccessLevel.PROTECTED)
	@Setter(value = AccessLevel.PACKAGE, onMethod_ = @Autowired)
	private SearchQuery<Allergy, AllergyIntolerance, FhirAllergyIntoleranceDao, AllergyIntoleranceTranslator, SearchQueryInclude<AllergyIntolerance>> searchQuery;
	
	@Nonnull
	@Override
	public String getImplicitProfile() {
		return IMPLICIT_PROFILE;
	}
	
	@Override
	public boolean canHandle(@Nonnull AllergyIntolerance resource) {
		return true;
	}
	
	@Override
	public IBundleProvider search(@Nonnull SearchParameterMap params) {
		return searchQuery.getQueryResults(params, dao, translator, searchQueryInclude);
	}
}
