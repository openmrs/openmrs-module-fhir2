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
import static org.openmrs.module.fhir2.FhirConstants.CODED_SEARCH_HANDLER;
import static org.openmrs.module.fhir2.FhirConstants.OPENMRS_FHIR_STRUCTURE_DEFINITION_PREFIX;
import static org.openmrs.module.fhir2.api.translators.impl.ImmunizationTranslatorImpl.IMMUNIZATION_GROUPING_CONCEPT;

import javax.annotation.Nonnull;

import java.util.List;
import java.util.Map;

import ca.uhn.fhir.rest.api.server.IBundleProvider;
import ca.uhn.fhir.rest.param.TokenAndListParam;
import ca.uhn.fhir.rest.param.TokenParam;
import ca.uhn.fhir.rest.server.exceptions.InvalidRequestException;
import lombok.Getter;
import lombok.Setter;
import org.hl7.fhir.r4.model.Immunization;
import org.openmrs.Obs;
import org.openmrs.api.EncounterService;
import org.openmrs.api.ObsService;
import org.openmrs.module.fhir2.api.dao.FhirObservationDao;
import org.openmrs.module.fhir2.api.search.SearchQuery;
import org.openmrs.module.fhir2.api.search.SearchQueryInclude;
import org.openmrs.module.fhir2.api.search.param.PropParam;
import org.openmrs.module.fhir2.api.search.param.SearchParameterMap;
import org.openmrs.module.fhir2.api.translators.ImmunizationTranslator;
import org.openmrs.module.fhir2.api.util.ImmunizationObsGroupHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Default handler for the FHIR {@link Immunization} resource, mapping onto the OpenMRS {@code Obs}
 * table as an obs group. Single backing today — claims every incoming Immunization. Carries the
 * CRUD wiring and the create/delete/update overrides lifted from the original
 * {@code FhirImmunizationServiceImpl}.
 * <p>
 * Immunizations are stored as obs groups under a designated grouping concept, so
 * {@link #search(SearchParameterMap)} narrows every search to that concept. That filter lives here
 * rather than in the orchestrator because it is a detail of <em>this</em> backing: another module
 * mapping Immunization onto a different OpenMRS table must not inherit an obs concept restriction.
 */
@Component
@Order(Ordered.LOWEST_PRECEDENCE)
public class ObsBackedImmunizationHandler extends BaseFhirResourceHandler<Immunization, Obs> implements FhirResourceHandler<Immunization> {
	
	private static final String IMPLICIT_PROFILE = OPENMRS_FHIR_STRUCTURE_DEFINITION_PREFIX + "/openmrs-immunization";
	
	@Getter(value = PROTECTED)
	@Setter(value = PROTECTED, onMethod_ = @Autowired)
	private FhirObservationDao dao;
	
	@Getter(value = PROTECTED)
	@Setter(value = PROTECTED, onMethod_ = @Autowired)
	private ImmunizationTranslator translator;
	
	@Getter(value = PROTECTED)
	@Setter(value = PROTECTED, onMethod_ = @Autowired)
	private ObsService obsService;
	
	@Getter(value = PROTECTED)
	@Setter(value = PROTECTED, onMethod_ = @Autowired)
	private EncounterService encounterService;
	
	@Getter(value = PROTECTED)
	@Setter(value = PROTECTED, onMethod_ = @Autowired)
	private ImmunizationObsGroupHelper helper;
	
	@Getter(value = PROTECTED)
	@Setter(value = PROTECTED, onMethod_ = @Autowired)
	private SearchQueryInclude<Immunization> searchQueryInclude;
	
	@Getter(value = PROTECTED)
	@Setter(value = PROTECTED, onMethod_ = @Autowired)
	private SearchQuery<Obs, Immunization, FhirObservationDao, ImmunizationTranslator, SearchQueryInclude<Immunization>> searchQuery;
	
	@Nonnull
	@Override
	public String getImplicitProfile() {
		return IMPLICIT_PROFILE;
	}
	
	@Override
	public boolean canHandle(@Nonnull Immunization resource) {
		return true;
	}
	
	@Override
	public IBundleProvider search(@Nonnull SearchParameterMap params) {
		return searchQuery.getQueryResults(withGroupingConcept(params), dao, translator, searchQueryInclude);
	}
	
	@Override
	public Immunization create(@Nonnull Immunization newImmunization) {
		if (newImmunization == null) {
			throw new InvalidRequestException("A resource of type Immunization must be supplied");
		}
		
		Obs obs = translator.toOpenmrsType(newImmunization);
		
		if (obs.getEncounter().getId() == null) {
			encounterService.saveEncounter(obs.getEncounter());
		}
		
		validateObject(obs);
		
		obs = obsService.saveObs(obs, "Created when translating a FHIR Immunization resource.");
		
		return translator.toFhirResource(obs);
	}
	
	@Override
	public void delete(@Nonnull String uuid) {
		if (uuid == null) {
			throw new InvalidRequestException("Uuid cannot be null.");
		}
		
		Obs obs = dao.get(uuid);
		
		if (obs == null) {
			throw resourceNotFound(uuid);
		}
		
		obsService.voidObs(obs, "Voided via FHIR API");
	}
	
	@Override
	protected Immunization applyUpdate(Obs existingObject, Immunization updatedResource) {
		// Obs in OpenMRS are conceptually immutable — ObsService.saveObs voids the previous row and
		// writes a new one, which is what callers expect for FHIR Immunization PUT semantics.
		ImmunizationTranslator translator = getTranslator();
		Obs updatedObject = translator.toOpenmrsType(existingObject, updatedResource);
		
		validateObject(updatedObject);
		
		return translator.toFhirResource(obsService.saveObs(updatedObject, "Updated via the FHIR2 API"));
	}
	
	@Override
	protected void validateObject(Obs obs) {
		super.validateObject(obs);
		helper.validateImmunizationObsGroup(obs);
	}
	
	/**
	 * Returns a copy of {@code params} additionally restricted to the immunization grouping concept.
	 * The orchestrator hands the same map to every handler in a fan-out search, so this must not mutate
	 * the caller's instance.
	 */
	private SearchParameterMap withGroupingConcept(SearchParameterMap params) {
		SearchParameterMap copy = new SearchParameterMap();
		copy.setSortSpec(params.getSortSpec());
		copy.setFromIndex(params.getFromIndex());
		copy.setToIndex(params.getToIndex());
		
		for (Map.Entry<String, List<PropParam<?>>> entry : params.getParameters()) {
			for (PropParam<?> prop : entry.getValue()) {
				copy.addParameter(entry.getKey(), prop.getPropertyName(), prop.getParam());
			}
		}
		
		TokenAndListParam conceptParam = new TokenAndListParam();
		TokenParam token = new TokenParam();
		token.setValue(Integer.toString(helper.concept(IMMUNIZATION_GROUPING_CONCEPT).getId()));
		conceptParam.addAnd(token);
		
		copy.addParameter(CODED_SEARCH_HANDLER, conceptParam);
		
		return copy;
	}
}
