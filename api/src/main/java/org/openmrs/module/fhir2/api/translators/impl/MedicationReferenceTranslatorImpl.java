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
import org.openmrs.Drug;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.api.dao.FhirMedicationDao;
import org.openmrs.module.fhir2.api.translators.MedicationReferenceTranslator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Setter(AccessLevel.PACKAGE)
public class MedicationReferenceTranslatorImpl extends AbstractReferenceHandlingTranslator implements MedicationReferenceTranslator {
	
	@Autowired
	private FhirMedicationDao medicationDao;
	
	@Override
	public Reference toFhirResource(Drug drug) {
		if (drug == null) {
			return null;
		}
		return createMedicationReference(drug);
	}
	
	@Override
	public Drug toOpenmrsType(Reference reference) {
		if (reference == null) {
			return null;
		}
		
		if (!reference.getType().equals(FhirConstants.MEDICATION)) {
			throw new IllegalArgumentException("Reference must be a Medication not a " + reference.getType());
		}
		
		String uuid = getReferenceId(reference);
		if (uuid == null) {
			return null;
		}
		
		return medicationDao.getMedicationByUuid(uuid);
	}
}
