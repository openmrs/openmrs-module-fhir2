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
import org.openmrs.DrugOrder;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.api.dao.FhirMedicationRequestDao;
import org.openmrs.module.fhir2.api.translators.MedicationRequestReferenceTranslator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Setter(AccessLevel.PACKAGE)
public class MedicationRequestReferenceTranslatorImpl extends BaseReferenceHandlingTranslator implements MedicationRequestReferenceTranslator {
	
	@Autowired
	private FhirMedicationRequestDao medicationRequestDao;
	
	@Override
	public Reference toFhirResource(@Nonnull DrugOrder drugOrder) {
		if (drugOrder == null) {
			return null;
		}
		return createDrugOrderReference(drugOrder);
	}
	
	@Override
	public DrugOrder toOpenmrsType(@Nonnull Reference medicationRequest) {
		if (medicationRequest == null || !medicationRequest.hasReference()) {
			return null;
		}
		if (getReferenceType(medicationRequest).map(ref -> !ref.equals(FhirConstants.MEDICATION_REQUEST)).orElse(true)) {
			throw new IllegalArgumentException(
			        "Reference must be to a MedicationRequest not a " + getReferenceType(medicationRequest).orElse(""));
		}
		return getReferenceId(medicationRequest).map(uuid -> medicationRequestDao.get(uuid)).orElse(null);
	}
}
