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

import lombok.AccessLevel;
import lombok.Setter;
import org.hl7.fhir.r4.model.Reference;
import org.openmrs.Patient;
import org.openmrs.module.fhir2.api.dao.FhirPatientDao;
import org.openmrs.module.fhir2.api.translators.PatientReferenceTranslator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Setter(AccessLevel.PACKAGE)
public class PatientReferenceTranslatorImpl extends BaseReferenceHandlingTranslator implements PatientReferenceTranslator {
	
	@Autowired
	private FhirPatientDao patientDao;
	
	@Override
	public Reference toFhirResource(Patient patient) {
		if (patient == null) {
			return null;
		}
		
		return createPatientReference(patient);
	}
	
	@Override
	public Patient toOpenmrsType(Reference patient) {
		if (patient == null) {
			return null;
		}
		
		if (!getReferenceType(patient).equals("Patient")) {
			throw new IllegalArgumentException("Reference must be to an Patient not a " + patient.getType());
		}
		
		String uuid = getReferenceId(patient);
		if (uuid == null) {
			return null;
		}
		
		return patientDao.get(uuid);
	}
}
