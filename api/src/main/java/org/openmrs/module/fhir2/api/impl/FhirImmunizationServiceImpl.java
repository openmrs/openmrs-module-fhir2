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

import java.util.Collection;

import ca.uhn.fhir.rest.api.SortSpec;
import ca.uhn.fhir.rest.param.ReferenceAndListParam;
import org.hl7.fhir.r4.model.Immunization;
import org.openmrs.Obs;
import org.openmrs.api.EncounterService;
import org.openmrs.api.ObsService;
import org.openmrs.module.fhir2.api.FhirImmunizationService;
import org.openmrs.module.fhir2.api.translators.ImmunizationTranslator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class FhirImmunizationServiceImpl implements FhirImmunizationService {
	
	@Autowired
	private ImmunizationTranslator translator;
	
	@Autowired
	private ObsService obsService;
	
	@Autowired
	private EncounterService encounterService;
	
	@Override
	public Immunization getImmunizationByUuid(String uuid) {
		Obs obs = obsService.getObsByUuid(uuid);
		return translator.toFhirResource(obs);
	}
	
	@Override
	public Immunization saveImmunization(Immunization immunization) {
		Obs obs = translator.toOpenmrsType(immunization);
		if (obs.getEncounter().getId() == null) {
			encounterService.saveEncounter(obs.getEncounter());
		}
		obs = obsService.saveObs(obs, "Created/updated when translating a FHIR Immunization resource.");
		immunization = translator.toFhirResource(obs);
		return immunization;
	}
	
	@Override
	public Collection<Immunization> searchImmunizations(ReferenceAndListParam patientParam, SortSpec sort) {
		// TODO Auto-generated method stub
		return null;
	}
	
}
