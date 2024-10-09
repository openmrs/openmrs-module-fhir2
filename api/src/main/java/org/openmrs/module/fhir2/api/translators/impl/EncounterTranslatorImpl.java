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
import static org.openmrs.module.fhir2.api.translators.impl.FhirTranslatorUtils.getLastUpdated;
import static org.openmrs.module.fhir2.api.translators.impl.FhirTranslatorUtils.getVersionId;

import javax.annotation.Nonnull;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

import lombok.AccessLevel;
import lombok.Setter;
import org.hl7.fhir.r4.model.Encounter;
import org.openmrs.EncounterProvider;
import org.openmrs.EncounterType;
import org.openmrs.Visit;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.api.translators.EncounterLocationTranslator;
import org.openmrs.module.fhir2.api.translators.EncounterParticipantTranslator;
import org.openmrs.module.fhir2.api.translators.EncounterPeriodTranslator;
import org.openmrs.module.fhir2.api.translators.EncounterReferenceTranslator;
import org.openmrs.module.fhir2.api.translators.EncounterTranslator;
import org.openmrs.module.fhir2.api.translators.EncounterTypeTranslator;
import org.openmrs.module.fhir2.api.translators.PatientReferenceTranslator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Setter(AccessLevel.PACKAGE)
public class EncounterTranslatorImpl extends BaseEncounterTranslator implements EncounterTranslator<org.openmrs.Encounter> {
	
	@Autowired
	private EncounterParticipantTranslator participantTranslator;
	
	@Autowired
	private EncounterLocationTranslator encounterLocationTranslator;
	
	@Autowired
	private PatientReferenceTranslator patientReferenceTranslator;
	
	@Autowired
	private EncounterReferenceTranslator<Visit> visitReferenceTranlator;
	
	@Autowired
	private EncounterTypeTranslator<EncounterType> encounterTypeTranslator;
	
	@Autowired
	private EncounterPeriodTranslator<org.openmrs.Encounter> encounterPeriodTranslator;
	
	@Override
	public Encounter toFhirResource(@Nonnull org.openmrs.Encounter openmrsEncounter) {
		notNull(openmrsEncounter, "The Openmrs Encounter object should not be null");
		
		Encounter encounter = new Encounter();
		encounter.setId(openmrsEncounter.getUuid());
		encounter.setStatus(Encounter.EncounterStatus.UNKNOWN);
		encounter.setType(encounterTypeTranslator.toFhirResource(openmrsEncounter.getEncounterType()));
		
		encounter.setSubject(patientReferenceTranslator.toFhirResource(openmrsEncounter.getPatient()));
		encounter.setParticipant(openmrsEncounter.getEncounterProviders().stream().map(participantTranslator::toFhirResource)
		        .collect(Collectors.toList()));
		
		// add visit as part of encounter
		encounter.setPartOf(visitReferenceTranlator.toFhirResource(openmrsEncounter.getVisit()));
		
		if (openmrsEncounter.getLocation() != null) {
			encounter.setLocation(
			    Collections.singletonList(encounterLocationTranslator.toFhirResource(openmrsEncounter.getLocation())));
		}
		
		encounter.setPeriod(encounterPeriodTranslator.toFhirResource(openmrsEncounter));
		
		encounter.getMeta().addTag(FhirConstants.OPENMRS_FHIR_EXT_ENCOUNTER_TAG, "encounter", "Encounter");
		encounter.getMeta().setLastUpdated(getLastUpdated(openmrsEncounter));
		encounter.getMeta().setVersionId(getVersionId(openmrsEncounter));
		encounter.setClass_(mapLocationToClass(openmrsEncounter.getLocation()));
		
		return encounter;
	}
	
	@Override
	public org.openmrs.Encounter toOpenmrsType(@Nonnull Encounter fhirEncounter) {
		notNull(fhirEncounter, "The Encounter object should not be null");
		return this.toOpenmrsType(new org.openmrs.Encounter(), fhirEncounter);
	}
	
	@Override
	public org.openmrs.Encounter toOpenmrsType(@Nonnull org.openmrs.Encounter existingEncounter,
	        @Nonnull Encounter encounter) {
		notNull(existingEncounter, "The existing Openmrs Encounter object should not be null");
		notNull(encounter, "The Encounter object should not be null");
		
		if (encounter.hasId()) {
			existingEncounter.setUuid(encounter.getIdElement().getIdPart());
		}
		
		EncounterType encounterType = encounterTypeTranslator.toOpenmrsType(encounter.getType());
		if (encounterType != null) {
			existingEncounter.setEncounterType(encounterType);
		}
		
		existingEncounter.setPatient(patientReferenceTranslator.toOpenmrsType(encounter.getSubject()));
		
		// TODO Improve this to do actual diffing
		Set<EncounterProvider> existingProviders = existingEncounter.getEncounterProviders();
		if (existingProviders != null && !existingProviders.isEmpty()) {
			existingProviders.clear();
		}
		
		if (existingProviders == null) {
			existingProviders = new LinkedHashSet<>(encounter.getParticipant().size());
		}
		
		existingProviders.addAll(encounter.getParticipant().stream()
		        .map(encounterParticipantComponent -> participantTranslator.toOpenmrsType(new EncounterProvider(),
		            encounterParticipantComponent))
		        .peek(ep -> ep.setEncounter(existingEncounter)).collect(Collectors.toCollection(LinkedHashSet::new)));
		
		existingEncounter.setEncounterProviders(existingProviders);
		
		existingEncounter.setLocation(encounterLocationTranslator.toOpenmrsType(encounter.getLocationFirstRep()));
		existingEncounter.setVisit(visitReferenceTranlator.toOpenmrsType(encounter.getPartOf()));
		
		encounterPeriodTranslator.toOpenmrsType(existingEncounter, encounter.getPeriod());
		
		return existingEncounter;
	}
}
