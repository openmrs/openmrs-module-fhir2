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
import static org.openmrs.module.fhir2.api.translators.impl.ImmunizationTranslatorImpl.immunizationGroupingConcept;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import ca.uhn.fhir.rest.api.SortSpec;
import ca.uhn.fhir.rest.param.ReferenceParam;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.hl7.fhir.r4.model.Immunization;
import org.openmrs.Concept;
import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.api.ConceptService;
import org.openmrs.api.EncounterService;
import org.openmrs.api.ObsService;
import org.openmrs.api.PatientService;
import org.openmrs.module.fhir2.api.FhirImmunizationService;
import org.openmrs.module.fhir2.api.translators.ImmunizationTranslator;
import org.openmrs.module.fhir2.api.translators.impl.ImmunizationObsGroupHelper;
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
	
	private ImmunizationObsGroupHelper helper;
	
	@Autowired
	public void setPatientService(PatientService patientService) {
		this.patientService = patientService;
	}
	
	@Autowired
	public void setImmunizationHelper(ConceptService conceptService) {
		this.helper = new ImmunizationObsGroupHelper(conceptService);
	}
	
	@Override
	public Immunization get(String uuid) {
		Obs obs = obsService.getObsByUuid(uuid);
		return translator.toFhirResource(obs);
	}
	
	@Override
	public Immunization create(Immunization newImmunization) {
		Obs obs = translator.toOpenmrsType(newImmunization);
		if (obs.getEncounter().getId() == null) {
			encounterService.saveEncounter(obs.getEncounter());
		}
		obs = obsService.saveObs(obs, "Created when translating a FHIR Immunization resource.");
		newImmunization = translator.toFhirResource(obs);
		return newImmunization;
	}
	
	@Override
	public Immunization update(String uuid, Immunization updatedImmunization) {
		Obs existingObs = obsService.getObsByUuid(uuid);
		Obs obs = translator.toOpenmrsType(existingObs, updatedImmunization);
		obs = obsService.saveObs(obs, "Updated when translating a FHIR Immunization resource.");
		return translator.toFhirResource(obs);
	}
	
	@Override
	public Immunization delete(String uuid) {
		// TODO Simply void the underlying obs
		throw new UnsupportedOperationException("Deleting a FHIR Immunization resource is currently not supported.");
	}
	
	@Override
	public Concept getOpenmrsImmunizationConcept() {
		return helper.concept(immunizationGroupingConcept);
	}
	
	public Patient getPatient(ReferenceParam patientParam) {
		
		Patient patient = null;
		
		if (StringUtils.equals(patientParam.getChain(), SP_IDENTIFIER)) {
			final String identifier = patientParam.getValue();
			
			List<Patient> patients = patientService.getPatients(identifier, false, 0, 1);
			if (CollectionUtils.isEmpty(patients)) {
				throw new IllegalArgumentException(
				        "No patient could be found for the following OpenMRS identifier: '" + identifier + "'.");
			}
			if (CollectionUtils.size(patients) > 1) {
				throw new IllegalArgumentException(
				        "Multiple patients were found for the following OpenMRS identifier: '" + identifier + "'.");
			}
			patient = patients.get(0);
		} else if (StringUtils.isEmpty(patientParam.getChain())) {
			final String uuid = patientParam.getValue();
			patient = patientService.getPatientByUuid(uuid);
			if (patient == null) {
				throw new IllegalArgumentException(
				        "No patient could be found for the following OpenMRS UUID: '" + uuid + "'.");
			}
		}
		
		return patient;
	}
	
	@Override
	public Collection<Immunization> searchImmunizations(ReferenceParam patientParam, SortSpec sort) {
		final Patient patient = getPatient(patientParam);
		return obsService
		        .getObservations(Collections.singletonList(patient.getPerson()), null,
		            Collections.singletonList(getOpenmrsImmunizationConcept()), null, null, null,
		            Collections.singletonList("obsDatetime"), null, null, null, null, false)
		        .stream().map(obs -> translator.toFhirResource(obs)).collect(Collectors.toList());
	}
	
}
