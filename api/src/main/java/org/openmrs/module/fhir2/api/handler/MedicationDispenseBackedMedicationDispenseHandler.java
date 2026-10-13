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

import static lombok.AccessLevel.PACKAGE;
import static lombok.AccessLevel.PROTECTED;
import static org.openmrs.module.fhir2.FhirConstants.OPENMRS_FHIR_STRUCTURE_DEFINITION_PREFIX;

import javax.annotation.Nonnull;

import ca.uhn.fhir.rest.api.server.IBundleProvider;
import lombok.Getter;
import lombok.Setter;
import org.hl7.fhir.r4.model.MedicationDispense;
import org.openmrs.module.fhir2.api.dao.FhirMedicationDispenseDao;
import org.openmrs.module.fhir2.api.search.SearchQuery;
import org.openmrs.module.fhir2.api.search.SearchQueryInclude;
import org.openmrs.module.fhir2.api.search.param.SearchParameterMap;
import org.openmrs.module.fhir2.api.translators.MedicationDispenseTranslator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Default handler for the FHIR {@link MedicationDispense} resource, mapping onto the OpenMRS
 * {@code MedicationDispense} table. Single backing today — claims every incoming
 * MedicationDispense. Carries the CRUD wiring lifted from the original
 * {@code FhirMedicationDispenseServiceImpl}.
 */
@Component
@Order(Ordered.LOWEST_PRECEDENCE)
public class MedicationDispenseBackedMedicationDispenseHandler extends BaseFhirResourceHandler<MedicationDispense, org.openmrs.MedicationDispense> implements FhirResourceHandler<MedicationDispense> {
	
	private static final String IMPLICIT_PROFILE = OPENMRS_FHIR_STRUCTURE_DEFINITION_PREFIX + "/openmrs-medicationdispense";
	
	@Getter(PROTECTED)
	@Setter(value = PACKAGE, onMethod_ = @Autowired)
	private FhirMedicationDispenseDao<org.openmrs.MedicationDispense> dao;
	
	@Getter(PROTECTED)
	@Setter(value = PACKAGE, onMethod_ = @Autowired)
	private MedicationDispenseTranslator<org.openmrs.MedicationDispense> translator;
	
	@Getter(PROTECTED)
	@Setter(value = PACKAGE, onMethod_ = @Autowired)
	private SearchQueryInclude<MedicationDispense> searchQueryInclude;
	
	@Getter(PROTECTED)
	@Setter(value = PACKAGE, onMethod_ = @Autowired)
	private SearchQuery<org.openmrs.MedicationDispense, MedicationDispense, FhirMedicationDispenseDao<org.openmrs.MedicationDispense>, MedicationDispenseTranslator<org.openmrs.MedicationDispense>, SearchQueryInclude<MedicationDispense>> searchQuery;
	
	@Nonnull
	@Override
	public String getImplicitProfile() {
		return IMPLICIT_PROFILE;
	}
	
	@Override
	public boolean canHandle(@Nonnull MedicationDispense resource) {
		return true;
	}
	
	@Override
	public IBundleProvider search(@Nonnull SearchParameterMap params) {
		return searchQuery.getQueryResults(params, dao, translator, searchQueryInclude);
	}
}
