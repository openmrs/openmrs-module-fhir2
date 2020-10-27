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

import static org.openmrs.module.fhir2.FhirConstants.CODED_SEARCH_HANDLER;
import static org.openmrs.module.fhir2.FhirConstants.PATIENT_REFERENCE_SEARCH_HANDLER;
import static org.openmrs.module.fhir2.api.translators.impl.ImmunizationTranslatorImpl.immunizationGroupingConcept;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import ca.uhn.fhir.rest.api.SortSpec;
import ca.uhn.fhir.rest.api.server.IBundleProvider;
import ca.uhn.fhir.rest.param.ReferenceAndListParam;
import ca.uhn.fhir.rest.param.TokenAndListParam;
import ca.uhn.fhir.rest.param.TokenParam;
import org.hl7.fhir.r4.model.Immunization;
import org.openmrs.Concept;
import org.openmrs.Obs;
import org.openmrs.api.EncounterService;
import org.openmrs.api.ObsService;
import org.openmrs.module.fhir2.api.FhirImmunizationService;
import org.openmrs.module.fhir2.api.dao.FhirObservationDao;
import org.openmrs.module.fhir2.api.search.SearchQuery;
import org.openmrs.module.fhir2.api.search.SearchQueryInclude;
import org.openmrs.module.fhir2.api.search.param.SearchParameterMap;
import org.openmrs.module.fhir2.api.translators.ImmunizationTranslator;
import org.openmrs.module.fhir2.api.translators.impl.ImmunizationObsGroupHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class FhirImmunizationServiceImpl implements FhirImmunizationService {
	
	@Autowired
	private ImmunizationTranslator translator;
	
	@Autowired
	private FhirObservationDao obsDao;
	
	@Autowired
	private ObsService obsService;
	
	@Autowired
	private EncounterService encounterService;
	
	@Autowired
	private ImmunizationObsGroupHelper helper;
	
	@Autowired
	private SearchQueryInclude<Immunization> searchQueryInclude;
	
	@Autowired
	private SearchQuery<org.openmrs.Obs, Immunization, FhirObservationDao, ImmunizationTranslator, SearchQueryInclude<Immunization>> searchQuery;
	
	@Override
	public Immunization get(String uuid) {
		return translator.toFhirResource(obsDao.get(uuid));
	}
	
	@Override
	public List<Immunization> get(Collection<String> uuids) {
		return uuids.stream().map(uuid -> get(uuid)).collect(Collectors.toList());
	}
	
	@Override
	public Immunization create(Immunization newImmunization) {
		Obs obs = translator.toOpenmrsType(newImmunization);
		if (obs.getEncounter().getId() == null) {
			encounterService.saveEncounter(obs.getEncounter());
		}
		obs = obsService.saveObs(obs, "Created when translating a FHIR Immunization resource.");
		//		obs = obsDao.createOrUpdate(obs);
		return translator.toFhirResource(obs);
	}
	
	@Override
	public Immunization update(String uuid, Immunization updatedImmunization) {
		Obs obs = translator.toOpenmrsType(obsDao.get(uuid), updatedImmunization);
		obs = obsService.saveObs(obs, "Updated when translating a FHIR Immunization resource.");
		//		obs = obsDao.createOrUpdate(obs);
		return translator.toFhirResource(obs);
	}
	
	@Override
	public Immunization delete(String uuid) {
		Obs obs = translator.toOpenmrsType(get(uuid));
		obs = obsService.voidObs(obs, "Voided through deleting via the FHIR Immunization resource.");
		return translator.toFhirResource(obs);
	}
	
	@Override
	public Concept getOpenmrsImmunizationConcept() {
		return helper.concept(immunizationGroupingConcept);
	}
	
	@Override
	public IBundleProvider searchImmunizations(ReferenceAndListParam patientParam, SortSpec sort) {
		
		SearchParameterMap theParams = new SearchParameterMap();
		theParams.addParameter(PATIENT_REFERENCE_SEARCH_HANDLER, patientParam);
		TokenAndListParam conceptParam = new TokenAndListParam();
		TokenParam token = new TokenParam();
		token.setValue(Integer.toString(getOpenmrsImmunizationConcept().getId()));
		conceptParam.addAnd(token);
		theParams.addParameter(CODED_SEARCH_HANDLER, conceptParam);
		
		return searchQuery.getQueryResults(theParams, obsDao, translator, searchQueryInclude);
	}
	
}
