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

import static org.hl7.fhir.r4.model.Patient.SP_IDENTIFIER;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import ca.uhn.fhir.rest.api.SortSpec;
import ca.uhn.fhir.rest.param.ReferenceAndListParam;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.hl7.fhir.r4.model.Immunization;
import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.api.EncounterService;
import org.openmrs.api.ObsService;
import org.openmrs.api.PatientService;
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
	
	private PatientService patientService;
	
	@Autowired
	public void setPatientService(PatientService patientService) {
		this.patientService = patientService;
	}
	
	@Override
	public Immunization getImmunizationByUuid(String uuid) {
		Obs obs = obsService.getObsByUuid(uuid);
		return translator.toFhirResource(obs);
	}
	
	@Override
	public Immunization createImmunization(Immunization newImmunization) {
		Obs obs = translator.toOpenmrsType(newImmunization);
		if (obs.getEncounter().getId() == null) {
			encounterService.saveEncounter(obs.getEncounter());
		}
		obs = obsService.saveObs(obs, "Created when translating a FHIR Immunization resource.");
		newImmunization = translator.toFhirResource(obs);
		return newImmunization;
	}
	
	@Override
	public Immunization updateImmunization(String uuid, Immunization updatedImmunization) {
		Obs existingObs = obsService.getObsByUuid(uuid);
		Obs obs = translator.toOpenmrsType(existingObs, updatedImmunization);
		obs = obsService.saveObs(obs, "Updated when translating a FHIR Immunization resource.");
		return translator.toFhirResource(obs);
	}
	
	public Patient getPatient(ReferenceAndListParam patientParam) {
		
		String[] ids = new String[2]; // an array of two: [patient identifier, patient UUID]
		
		patientParam.getValuesAsQueryTokens().stream()
		        .forEach(refOrListParam -> refOrListParam.getValuesAsQueryTokens().stream().forEach(refParam -> {
			        
			        if (StringUtils.equals(refParam.getChain(), SP_IDENTIFIER)) {
				        ids[0] = refParam.getValue();
			        }
			        if (StringUtils.isEmpty(refParam.getChain())) {
				        ids[1] = refParam.getValue();
			        }
			        
		        }));
		
		final String identifier = ids[0];
		final String uuid = ids[1];
		
		Patient patient = null;
		
		if (!StringUtils.isEmpty(identifier)) {
			
			List<Patient> patients = patientService.getPatients(identifier, false, 0, 1);
			if (CollectionUtils.isEmpty(patients)) {
				throw new IllegalArgumentException(
				        "No patient could be found for the following OpenMRS identifier: '" + identifier + "'.");
			}
			patient = patients.get(0);
			if (!StringUtils.isEmpty(uuid) && !uuid.equals(patient.getUuid())) {
				throw new IllegalArgumentException(
				        "The provided UUID '" + uuid + "' is not that of the patient identified by '" + identifier + "'.");
			}
		} else if (!StringUtils.isEmpty(uuid)) {
			patient = patientService.getPatientByUuid(uuid);
			if (patient == null) {
				throw new IllegalArgumentException(
				        "No patient could be found for the following OpenMRS UUID: '" + uuid + "'.");
			}
		}
		
		return patient;
	}
	
	@Override
	public Collection<Immunization> searchImmunizations(ReferenceAndListParam patientParam, SortSpec sort) {
		final Patient patient = getPatient(patientParam);
		return obsService
		        .getObservations(Collections.singletonList(patient.getPerson()), null,
		            Collections.singletonList(translator.getOpenmrsImmunizationConcept()), null, null, null,
		            Collections.singletonList("obsDatetime"), null, null, null, null, false)
		        .stream().map(obs -> translator.toFhirResource(obs)).collect(Collectors.toList());
	}
	
}
