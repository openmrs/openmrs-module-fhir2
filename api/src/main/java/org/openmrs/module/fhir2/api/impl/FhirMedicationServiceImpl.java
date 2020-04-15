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

import java.util.Collection;
import java.util.stream.Collectors;

import ca.uhn.fhir.rest.param.TokenAndListParam;
import ca.uhn.fhir.rest.param.TokenOrListParam;
import ca.uhn.fhir.rest.server.exceptions.InvalidRequestException;
import ca.uhn.fhir.rest.server.exceptions.MethodNotAllowedException;
import lombok.AccessLevel;
import lombok.Setter;
import org.hl7.fhir.r4.model.Medication;
import org.openmrs.Drug;
import org.openmrs.module.fhir2.api.FhirMedicationService;
import org.openmrs.module.fhir2.api.dao.FhirMedicationDao;
import org.openmrs.module.fhir2.api.translators.MedicationTranslator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional
@Setter(AccessLevel.PACKAGE)
public class FhirMedicationServiceImpl implements FhirMedicationService {
	
	@Autowired
	private MedicationTranslator medicationTranslator;
	
	@Autowired
	private FhirMedicationDao medicationDao;
	
	@Override
	@Transactional(readOnly = true)
	public Medication getMedicationByUuid(String uuid) {
		return medicationTranslator.toFhirResource(medicationDao.get(uuid));
	}
	
	@Override
	public Medication saveMedication(Medication medication) {
		return medicationTranslator
		        .toFhirResource(medicationDao.createOrUpdate(medicationTranslator.toOpenmrsType(new Drug(), medication)));
	}
	
	@Override
	public Medication updateMedication(Medication medication, String uuid) {
		if (uuid == null) {
			throw new InvalidRequestException("Uuid cannot be null.");
		}
		
		if (!medication.getId().equals(uuid)) {
			throw new InvalidRequestException("Medication id and provided id do not match.");
		}
		
		Drug drug = medicationDao.get(uuid);
		
		if (drug == null) {
			throw new MethodNotAllowedException("No Medication found to update.");
		}
		
		return medicationTranslator
		        .toFhirResource(medicationDao.createOrUpdate(medicationTranslator.toOpenmrsType(drug, medication)));
	}
	
	@Override
	@Transactional(readOnly = true)
	public Collection<Medication> searchForMedications(TokenAndListParam code, TokenAndListParam dosageForm,
	        TokenOrListParam ingredientCode, TokenOrListParam status) {
		
		return medicationDao.searchForMedications(code, dosageForm, ingredientCode, status).stream()
		        .map(medicationTranslator::toFhirResource).collect(Collectors.toList());
	}
	
	@Override
	public Medication deleteMedication(String uuid) {
		return medicationTranslator.toFhirResource(medicationDao.delete(uuid));
	}
}
