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
import lombok.AccessLevel;
import lombok.Getter;
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
@Getter(AccessLevel.PROTECTED)
public class FhirMedicationServiceImpl extends BaseFhirService<Medication, Drug> implements FhirMedicationService {
	
	@Autowired
	private MedicationTranslator translator;
	
	@Autowired
	private FhirMedicationDao dao;
	
	@Override
	@Transactional(readOnly = true)
	public Collection<Medication> searchForMedications(TokenAndListParam code, TokenAndListParam dosageForm,
	        TokenOrListParam ingredientCode, TokenOrListParam status) {
		
		return dao.searchForMedications(code, dosageForm, ingredientCode, status).stream().map(translator::toFhirResource)
		        .collect(Collectors.toList());
	}
	
}
