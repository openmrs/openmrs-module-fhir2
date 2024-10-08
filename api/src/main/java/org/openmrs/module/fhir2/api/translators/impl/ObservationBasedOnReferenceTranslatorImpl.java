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

import static org.openmrs.module.fhir2.api.translators.impl.ReferenceHandlingTranslator.createOrderReference;
import static org.openmrs.module.fhir2.api.translators.impl.ReferenceHandlingTranslator.getReferenceId;
import static org.openmrs.module.fhir2.api.translators.impl.ReferenceHandlingTranslator.getReferenceType;

import javax.annotation.Nonnull;

import lombok.AccessLevel;
import lombok.Setter;
import org.hl7.fhir.r4.model.Reference;
import org.openmrs.Order;
import org.openmrs.TestOrder;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.api.dao.FhirMedicationRequestDao;
import org.openmrs.module.fhir2.api.dao.FhirServiceRequestDao;
import org.openmrs.module.fhir2.api.translators.ObservationBasedOnReferenceTranslator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Setter(AccessLevel.PACKAGE)
public class ObservationBasedOnReferenceTranslatorImpl implements ObservationBasedOnReferenceTranslator {
	
	@Autowired
	private FhirServiceRequestDao<TestOrder> serviceRequestDao;
	
	@Autowired
	private FhirMedicationRequestDao medicationRequestDao;
	
	@Override
	public Reference toFhirResource(@Nonnull Order order) {
		if (order == null) {
			return null;
		}
		
		return createOrderReference(order);
	}
	
	@Override
	public Order toOpenmrsType(@Nonnull Reference reference) {
		if (reference == null) {
			return null;
		}
		
		if (getReferenceType(reference)
		        .map(ref -> !(ref.equals(FhirConstants.SERVICE_REQUEST) || ref.equals(FhirConstants.MEDICATION_REQUEST)))
		        .orElse(true)) {
			throw new IllegalArgumentException("Reference must be to a ServiceRequest or MedicationRequest");
		}
		
		return getReferenceId(reference).map(uuid -> {
			switch (reference.getType()) {
				case FhirConstants.MEDICATION_REQUEST:
					return medicationRequestDao.get(uuid);
				case FhirConstants.SERVICE_REQUEST:
					return serviceRequestDao.get(uuid);
				default:
					return null;
			}
		}).orElse(null);
	}
}
