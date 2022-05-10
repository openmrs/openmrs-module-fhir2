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

import javax.annotation.Nonnull;

import lombok.AccessLevel;
import lombok.Setter;
import org.openmrs.MedicationDispense;
import org.openmrs.annotation.OpenmrsProfile;
import org.openmrs.module.fhir2.api.translators.MedicationDispenseTranslator;
import org.openmrs.module.fhir2.api.translators.PatientReferenceTranslator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Setter(AccessLevel.PACKAGE)
@Component
@OpenmrsProfile(openmrsPlatformVersion = "2.6.* - 2.*")
public class MedicationDispenseTranslatorImpl_2_6 implements MedicationDispenseTranslator<MedicationDispense> {
	
	@Autowired
	private PatientReferenceTranslator patientReferenceTranslator;
	
	@Override
	public org.hl7.fhir.r4.model.MedicationDispense toFhirResource(@Nonnull MedicationDispense dispenseData) {
		notNull(dispenseData, "The Openmrs MedicationDispense object should not be null");
		
		org.hl7.fhir.r4.model.MedicationDispense fhirObject = new org.hl7.fhir.r4.model.MedicationDispense();
		fhirObject.setId(dispenseData.getUuid());
		fhirObject.setSubject(patientReferenceTranslator.toFhirResource(dispenseData.getPatient()));
		
		fhirObject.getMeta().setLastUpdated(getLastUpdated(dispenseData));
		
		return fhirObject;
	}
	
	@Override
	public MedicationDispense toOpenmrsType(@Nonnull org.hl7.fhir.r4.model.MedicationDispense medicationDispense) {
		notNull(medicationDispense, "The MedicationDispense object should not be null");
		return this.toOpenmrsType(new MedicationDispense(), medicationDispense);
	}
	
	@Override
	public MedicationDispense toOpenmrsType(@Nonnull MedicationDispense dispenseData,
	        @Nonnull org.hl7.fhir.r4.model.MedicationDispense medicationDispense) {
		notNull(dispenseData, "The existing Openmrs MedicationDispense object should not be null");
		notNull(medicationDispense, "The FHIR MedicationDispense object should not be null");
		
		dispenseData.setUuid(medicationDispense.getIdElement().getIdPart());
		dispenseData.setPatient(patientReferenceTranslator.toOpenmrsType(medicationDispense.getSubject()));
		
		return dispenseData;
	}
}
