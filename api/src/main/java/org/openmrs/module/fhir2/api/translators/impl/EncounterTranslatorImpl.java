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

import javax.inject.Inject;

import lombok.AccessLevel;
import lombok.Setter;
import org.hl7.fhir.r4.model.Encounter;
import org.hl7.fhir.r4.model.Reference;
import org.openmrs.Patient;
import org.openmrs.PersonName;
import org.openmrs.api.PatientService;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.api.translators.EncounterTranslator;
import org.openmrs.module.fhir2.api.util.FhirReferenceUtils;
import org.springframework.stereotype.Component;

import static org.apache.commons.lang.Validate.notNull;

@Component
@Setter(AccessLevel.PACKAGE)
public class EncounterTranslatorImpl implements EncounterTranslator {
	
	@Inject
	PatientService patientService;
	
	@Override
	public Encounter toFhirResource(org.openmrs.Encounter openMrsEncounter) {
		Encounter encounter = new Encounter();
		encounter.setId(openMrsEncounter.getUuid());
		encounter.setStatus(Encounter.EncounterStatus.UNKNOWN);
		
		encounter.setSubject(FhirReferenceUtils.addPatientReference(openMrsEncounter.getPatient()));
		
		return encounter;
	}
	
	@Override
	public org.openmrs.Encounter toOpenmrsType(Encounter fhirEncounter) {
		return this.toOpenmrsType(new org.openmrs.Encounter(), fhirEncounter);
	}
	
	@Override
	public org.openmrs.Encounter toOpenmrsType(org.openmrs.Encounter existingEncounter, Encounter encounter) {
		notNull(existingEncounter, "Existing encounter cannot be null");
		
		if (encounter == null) {
			return existingEncounter;
		}
		existingEncounter.setUuid(encounter.getId());
		String patientUuid = FhirReferenceUtils.extractUuid(encounter.getSubject().getReference());
		existingEncounter.setPatient(patientService.getPatientByUuid(patientUuid));
		
		return existingEncounter;
	}
}
