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
public class MedicationReferenceTranslatorImpl extends BaseReferenceHandlingTranslator implements MedicationReferenceTranslator {
	
	@Autowired
	private FhirMedicationDao medicationDao;
	
	@Override
	public Reference toFhirResource(@Nonnull Drug drug) {
		if (drug == null) {
			return null;
		}
		return createMedicationReference(drug);
	}
	
	@Override
	public Drug toOpenmrsType(@Nonnull Reference reference) {
		if (reference == null || !reference.hasReference()) {
			return null;
		}
		
		if (getReferenceType(reference).map(ref -> !ref.equals(FhirConstants.MEDICATION)).orElse(true)) {
			throw new IllegalArgumentException(
			        "Reference must be a Medication not a " + getReferenceType(reference).orElse(""));
		}
		
		return getReferenceId(reference).map(uuid -> medicationDao.get(uuid)).orElse(null);
	}
}
