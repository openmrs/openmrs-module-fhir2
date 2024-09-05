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

import static org.openmrs.module.fhir2.api.translators.impl.ReferenceHandlingTranslator.createPatientReference;
import static org.openmrs.module.fhir2.api.translators.impl.ReferenceHandlingTranslator.getReferenceId;
import static org.openmrs.module.fhir2.api.translators.impl.ReferenceHandlingTranslator.getReferenceType;

import javax.annotation.Nonnull;

import lombok.AccessLevel;
import lombok.Setter;
import org.hl7.fhir.r4.model.Reference;
import org.openmrs.Patient;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.api.dao.FhirPatientDao;
import org.openmrs.module.fhir2.api.translators.PatientReferenceTranslator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Setter(AccessLevel.PACKAGE)
public class PatientReferenceTranslatorImpl implements PatientReferenceTranslator {
	
	@Autowired
	private FhirPatientDao patientDao;
	
	@Override
	public Reference toFhirResource(@Nonnull Patient patient) {
		if (patient == null) {
			return null;
		}
		
		return createPatientReference(patient);
	}
	
	@Override
	public Patient toOpenmrsType(@Nonnull Reference patient) {
		if (patient == null || !patient.hasReference()) {
			return null;
		}
		
		if (getReferenceType(patient).map(ref -> !ref.equals(FhirConstants.PATIENT)).orElse(true)) {
			throw new IllegalArgumentException(
			        "Reference must be to an Patient not a " + getReferenceType(patient).orElse(""));
		}
		
		return getReferenceId(patient).map(uuid -> patientDao.get(uuid)).orElse(null);
	}
}
