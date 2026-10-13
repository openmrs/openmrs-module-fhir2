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
import org.hl7.fhir.r4.model.Medication;
import org.openmrs.Drug;
import org.openmrs.module.fhir2.api.dao.FhirMedicationDao;
import org.openmrs.module.fhir2.api.search.SearchQuery;
import org.openmrs.module.fhir2.api.search.SearchQueryInclude;
import org.openmrs.module.fhir2.api.search.param.SearchParameterMap;
import org.openmrs.module.fhir2.api.translators.MedicationTranslator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Default handler for the FHIR {@link Medication} resource, mapping onto the OpenMRS {@code Drug}
 * table. Single backing today — claims every incoming Medication. Carries the CRUD wiring lifted
 * from the original {@code FhirMedicationServiceImpl}.
 */
@Component
@Order(Ordered.LOWEST_PRECEDENCE)
public class DrugBackedMedicationHandler extends BaseFhirResourceHandler<Medication, Drug> implements FhirResourceHandler<Medication> {
	
	private static final String IMPLICIT_PROFILE = OPENMRS_FHIR_STRUCTURE_DEFINITION_PREFIX + "/openmrs-medication";
	
	@Getter(value = AccessLevel.PROTECTED)
	@Setter(value = AccessLevel.PACKAGE, onMethod_ = @Autowired)
	private FhirMedicationDao dao;
	
	@Getter(value = AccessLevel.PROTECTED)
	@Setter(value = AccessLevel.PACKAGE, onMethod_ = @Autowired)
	private MedicationTranslator translator;
	
	@Getter(value = AccessLevel.PROTECTED)
	@Setter(value = AccessLevel.PACKAGE, onMethod_ = @Autowired)
	private SearchQueryInclude<Medication> searchQueryInclude;
	
	@Getter(value = AccessLevel.PROTECTED)
	@Setter(value = AccessLevel.PACKAGE, onMethod_ = @Autowired)
	private SearchQuery<Drug, Medication, FhirMedicationDao, MedicationTranslator, SearchQueryInclude<Medication>> searchQuery;
	
	@Nonnull
	@Override
	public String getImplicitProfile() {
		return IMPLICIT_PROFILE;
	}
	
	@Override
	public boolean canHandle(@Nonnull Medication resource) {
		return true;
	}
	
	@Override
	public IBundleProvider search(@Nonnull SearchParameterMap params) {
		return searchQuery.getQueryResults(params, dao, translator, searchQueryInclude);
	}
}
