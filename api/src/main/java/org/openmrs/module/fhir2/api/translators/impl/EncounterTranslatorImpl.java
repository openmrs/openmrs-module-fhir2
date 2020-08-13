/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.fhir2.api.translators.impl;

import static org.apache.commons.lang3.Validate.notNull;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.stream.Collectors;

import lombok.AccessLevel;
import lombok.Setter;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Encounter;
import org.openmrs.EncounterProvider;
import org.openmrs.Location;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.api.mappings.EncounterClassMap;
import org.openmrs.module.fhir2.api.translators.EncounterLocationTranslator;
import org.openmrs.module.fhir2.api.translators.EncounterParticipantTranslator;
import org.openmrs.module.fhir2.api.translators.EncounterTranslator;
import org.openmrs.module.fhir2.api.translators.PatientReferenceTranslator;
import org.openmrs.module.fhir2.api.translators.ProvenanceTranslator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Setter(AccessLevel.PACKAGE)
public class EncounterTranslatorImpl implements EncounterTranslator {
	
	@Autowired
	private EncounterParticipantTranslator participantTranslator;
	
	@Autowired
	private EncounterLocationTranslator encounterLocationTranslator;
	
	@Autowired
	private PatientReferenceTranslator patientReferenceTranslator;
	
	@Autowired
	private ProvenanceTranslator<org.openmrs.Encounter> provenanceTranslator;
	
	@Autowired
	private EncounterClassMap encounterClassMap;
	
	private Coding mapLocationToClass(Location location) {
		Coding coding = new Coding();
		coding.setSystem(FhirConstants.ENCOUNTER_CLASS_VALUE_SET_URI);
		// The default code for anything that cannot be matched with FHIR codes.
		coding.setCode("AMB");
		if (location == null) {
			return coding;
		}
		String classCode = encounterClassMap.getFhirClass(location.getUuid());
		if (classCode != null) {
			coding.setCode(classCode);
		}
		return coding;
	}
	
	@Override
	public Encounter toFhirResource(org.openmrs.Encounter openMrsEncounter) {
		notNull(openMrsEncounter, "The Openmrs Encounter object should not be null");
		
		Encounter encounter = new Encounter();
		encounter.setId(openMrsEncounter.getUuid());
		encounter.setStatus(Encounter.EncounterStatus.UNKNOWN);
		
		encounter.setSubject(patientReferenceTranslator.toFhirResource(openMrsEncounter.getPatient()));
		encounter.setParticipant(openMrsEncounter.getEncounterProviders().stream().map(participantTranslator::toFhirResource)
		        .collect(Collectors.toList()));
		
		if (openMrsEncounter.getLocation() != null) {
			encounter.setLocation(
			    Collections.singletonList(encounterLocationTranslator.toFhirResource(openMrsEncounter.getLocation())));
		}
		
		encounter.getMeta().setLastUpdated(openMrsEncounter.getDateChanged());
		encounter.addContained(provenanceTranslator.getCreateProvenance(openMrsEncounter));
		encounter.addContained(provenanceTranslator.getUpdateProvenance(openMrsEncounter));
		encounter.setClass_(mapLocationToClass(openMrsEncounter.getLocation()));
		
		return encounter;
	}
	
	@Override
	public org.openmrs.Encounter toOpenmrsType(Encounter fhirEncounter) {
		notNull(fhirEncounter, "The Encounter object should not be null");
		return this.toOpenmrsType(new org.openmrs.Encounter(), fhirEncounter);
	}
	
	@Override
	public org.openmrs.Encounter toOpenmrsType(org.openmrs.Encounter existingEncounter, Encounter encounter) {
		notNull(existingEncounter, "The existing Openmrs Encounter object should not be null");
		notNull(encounter, "The Encounter object should not be null");
		
		existingEncounter.setUuid(encounter.getId());
		
		existingEncounter.setPatient(patientReferenceTranslator.toOpenmrsType(encounter.getSubject()));
		existingEncounter.setEncounterProviders(encounter
		        .getParticipant().stream().map(encounterParticipantComponent -> participantTranslator
		                .toOpenmrsType(new EncounterProvider(), encounterParticipantComponent))
		        .collect(Collectors.toCollection(LinkedHashSet::new)));
		existingEncounter.setLocation(encounterLocationTranslator.toOpenmrsType(encounter.getLocationFirstRep()));
		
		return existingEncounter;
	}
}
