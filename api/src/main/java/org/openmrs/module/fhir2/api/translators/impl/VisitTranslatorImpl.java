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

import lombok.AccessLevel;
import lombok.Setter;
import org.hl7.fhir.r4.model.Encounter;
import org.openmrs.Visit;
import org.openmrs.module.fhir2.api.translators.EncounterLocationTranslator;
import org.openmrs.module.fhir2.api.translators.EncounterTranslator;
import org.openmrs.module.fhir2.api.translators.PatientReferenceTranslator;
import org.openmrs.module.fhir2.api.translators.ProvenanceTranslator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Setter(AccessLevel.PACKAGE)
public class VisitTranslatorImpl extends BaseEncounterTranslator implements EncounterTranslator<Visit> {
	
	@Autowired
	private PatientReferenceTranslator patientReferenceTranslator;
	
	@Autowired
	private EncounterLocationTranslator encounterLocationTranslator;
	
	@Autowired
	private ProvenanceTranslator<org.openmrs.Visit> provenanceTranslator;
	
	@Override
	public Encounter toFhirResource(Visit visit) {
		notNull(visit, "The OpenMrs Visit object should not be null");
		
		Encounter encounter = new Encounter();
		encounter.setId(visit.getUuid());
		encounter.setStatus(Encounter.EncounterStatus.UNKNOWN);
		
		encounter.setSubject(patientReferenceTranslator.toFhirResource(visit.getPatient()));
		if (visit.getLocation() != null) {
			encounterLocationTranslator.toFhirResource(visit.getLocation());
		}
		encounter.setClass_(mapLocationToClass(visit.getLocation()));
		
		encounter.getMeta().setLastUpdated(visit.getDateChanged());
		encounter.addContained(provenanceTranslator.getCreateProvenance(visit));
		encounter.addContained(provenanceTranslator.getUpdateProvenance(visit));
		
		return encounter;
	}
	
	@Override
	public Visit toOpenmrsType(Encounter encounter) {
		return toOpenmrsType(new Visit(), encounter);
	}
	
	@Override
	public Visit toOpenmrsType(Visit existingVisit, Encounter encounter) {
		notNull(existingVisit, "The existingVisit object should not be null");
		notNull(encounter, "The Encounter object should not be null");
		
		existingVisit.setUuid(encounter.getId());
		existingVisit.setPatient(patientReferenceTranslator.toOpenmrsType(encounter.getSubject()));
		existingVisit.setLocation(encounterLocationTranslator.toOpenmrsType(encounter.getLocationFirstRep()));
		
		return existingVisit;
	}
}
