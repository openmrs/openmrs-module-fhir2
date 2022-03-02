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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import java.util.Collections;
import java.util.LinkedHashSet;
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
import org.openmrs.module.fhir2.api.translators.ProvenanceTranslator;
import org.openmrs.module.fhir2.api.util.FhirCache;
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
	private ProvenanceTranslator<org.openmrs.Encounter> provenanceTranslator;
	
	@Autowired
	private EncounterReferenceTranslator<Visit> visitReferenceTranlator;
	
	@Autowired
	private EncounterTypeTranslator<EncounterType> encounterTypeTranslator;
	
	@Autowired
	private EncounterPeriodTranslator<org.openmrs.Encounter> encounterPeriodTranslator;
	
	@Override
	public Encounter toFhirResource(@Nonnull org.openmrs.Encounter omrsEncounter) {
		if (omrsEncounter == null) {
			return null;
		}
		
		return toFhirResourceInternal(omrsEncounter, null);
	}
	
	@Override
	public Encounter toFhirResource(@Nonnull org.openmrs.Encounter omrsEncounter, @Nullable FhirCache cache) {
		if (omrsEncounter == null) {
			return null;
		}
		
		if (cache != null) {
			return (Encounter) cache.get(getCacheKey(omrsEncounter), k -> toFhirResourceInternal(omrsEncounter, cache));
		}
		
		return toFhirResourceInternal(omrsEncounter, null);
	}
	
	protected Encounter toFhirResourceInternal(@Nonnull org.openmrs.Encounter omrsEncounter, @Nullable FhirCache cache) {
		Encounter encounter = new Encounter();
		encounter.setId(omrsEncounter.getUuid());
		encounter.setStatus(Encounter.EncounterStatus.UNKNOWN);
		encounter.setType(encounterTypeTranslator.toFhirResource(omrsEncounter.getEncounterType(), cache));
		
		encounter.setSubject(patientReferenceTranslator.toFhirResource(omrsEncounter.getPatient(), cache));
		encounter.setParticipant(omrsEncounter.getEncounterProviders().stream()
		        .map(p -> participantTranslator.toFhirResource(p, cache)).collect(Collectors.toList()));
		
		// add visit as part of encounter
		encounter.setPartOf(visitReferenceTranlator.toFhirResource(omrsEncounter.getVisit(), cache));
		
		if (omrsEncounter.getLocation() != null) {
			encounter.setLocation(
			    Collections.singletonList(encounterLocationTranslator.toFhirResource(omrsEncounter.getLocation(), cache)));
		}
		
		encounter.setPeriod(encounterPeriodTranslator.toFhirResource(omrsEncounter, cache));
		
		encounter.getMeta().addTag(FhirConstants.OPENMRS_FHIR_EXT_ENCOUNTER_TAG, "encounter", "Encounter");
		encounter.getMeta().setLastUpdated(omrsEncounter.getDateChanged());
		encounter.addContained(provenanceTranslator.getCreateProvenance(omrsEncounter, cache));
		encounter.addContained(provenanceTranslator.getUpdateProvenance(omrsEncounter, cache));
		encounter.setClass_(mapLocationToClass(omrsEncounter.getLocation(), cache));
		
		return encounter;
	}
	
	@Override
	public org.openmrs.Encounter toOpenmrsType(@Nonnull Encounter fhirEncounter) {
		if (fhirEncounter == null) {
			return null;
		}
		
		return this.toOpenmrsType(new org.openmrs.Encounter(), fhirEncounter);
	}
	
	@Override
	public org.openmrs.Encounter toOpenmrsType(@Nonnull org.openmrs.Encounter existingEncounter,
	        @Nonnull Encounter encounter) {
		if (encounter == null) {
			return null;
		}
		
		if (existingEncounter == null) {
			existingEncounter = new org.openmrs.Encounter();
		}
		
		existingEncounter.setUuid(encounter.getId());
		
		EncounterType encounterType = encounterTypeTranslator.toOpenmrsType(encounter.getType());
		if (encounterType != null) {
			existingEncounter.setEncounterType(encounterType);
		}
		
		existingEncounter.setPatient(patientReferenceTranslator.toOpenmrsType(encounter.getSubject()));
		existingEncounter.setEncounterProviders(encounter
		        .getParticipant().stream().map(encounterParticipantComponent -> participantTranslator
		                .toOpenmrsType(new EncounterProvider(), encounterParticipantComponent))
		        .collect(Collectors.toCollection(LinkedHashSet::new)));
		
		existingEncounter.setLocation(encounterLocationTranslator.toOpenmrsType(encounter.getLocationFirstRep()));
		existingEncounter.setVisit(visitReferenceTranlator.toOpenmrsType(encounter.getPartOf()));
		
		encounterPeriodTranslator.toOpenmrsType(existingEncounter, encounter.getPeriod());
		
		return existingEncounter;
	}
}
