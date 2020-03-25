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

import javax.inject.Inject;

import java.util.Collection;
import java.util.stream.Collectors;

import ca.uhn.fhir.rest.param.TokenAndListParam;
import ca.uhn.fhir.rest.param.TokenOrListParam;
import lombok.AccessLevel;
import lombok.Setter;
import org.hl7.fhir.r4.model.Medication;
import org.openmrs.module.fhir2.api.FhirMedicationService;
import org.openmrs.module.fhir2.api.dao.FhirMedicationDao;
import org.openmrs.module.fhir2.api.translators.MedicationTranslator;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional
@Setter(AccessLevel.PACKAGE)
public class FhirMedicationServiceImpl implements FhirMedicationService {
	
	@Inject
	private MedicationTranslator medicationTranslator;
	
	@Inject
	private FhirMedicationDao medicationDao;
	
	@Override
	@Transactional(readOnly = true)
	public Medication getMedicationByUuid(String uuid) {
		return medicationTranslator.toFhirResource(medicationDao.getMedicationByUuid(uuid));
	}
	
	@Override
	@Transactional(readOnly = true)
	public Collection<Medication> searchForMedications(TokenAndListParam code, TokenAndListParam dosageForm,
	        TokenOrListParam ingredientCode, TokenOrListParam status) {
		
		return medicationDao.searchForMedications(code, dosageForm, ingredientCode, status).stream()
		        .map(medicationTranslator::toFhirResource).collect(Collectors.toList());
	}
}
