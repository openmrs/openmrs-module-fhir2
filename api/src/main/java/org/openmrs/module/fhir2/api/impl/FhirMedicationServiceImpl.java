/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.fhir2.api.impl;

import ca.uhn.fhir.rest.api.server.IBundleProvider;
import org.hl7.fhir.r4.model.Medication;
import org.openmrs.module.fhir2.api.FhirMedicationService;
import org.openmrs.module.fhir2.api.search.param.MedicationSearchParams;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class FhirMedicationServiceImpl extends BaseCompositeFhirService<Medication> implements FhirMedicationService {
	
	@Override
	@Transactional(readOnly = true)
	public IBundleProvider searchForMedications(MedicationSearchParams medicationSearchParams) {
		return doSearch(medicationSearchParams.toSearchParameterMap());
	}
}
