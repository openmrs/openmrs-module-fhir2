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

import ca.uhn.fhir.rest.param.ReferenceAndListParam;
import ca.uhn.fhir.rest.param.TokenOrListParam;
import lombok.AccessLevel;
import lombok.Setter;
import org.hl7.fhir.r4.model.AllergyIntolerance;
import org.openmrs.module.fhir2.api.FhirAllergyIntoleranceService;
import org.openmrs.module.fhir2.api.dao.FhirAllergyIntoleranceDao;
import org.openmrs.module.fhir2.api.translators.AllergyIntoleranceTranslator;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional
@Setter(AccessLevel.PACKAGE)
public class FhirAllergyIntoleranceServiceImpl implements FhirAllergyIntoleranceService {
	
	@Inject
	private AllergyIntoleranceTranslator allergyIntoleranceTranslator;
	
	@Inject
	private FhirAllergyIntoleranceDao allergyIntoleranceDao;
	
	@Override
	@Transactional
	public AllergyIntolerance getAllergyIntoleranceByUuid(String uuid) {
		return allergyIntoleranceTranslator.toFhirResource(allergyIntoleranceDao.getAllergyIntoleranceByUuid(uuid));
	}
	
	@Override
	@Transactional(readOnly = true)
	public Collection<AllergyIntolerance> searchForAllergies(ReferenceAndListParam patientReference,
	        TokenOrListParam category, TokenOrListParam allergen, TokenOrListParam severity,
	        TokenOrListParam manifestationCode, TokenOrListParam clinicalStatus) {
		return allergyIntoleranceDao
		        .searchForAllergies(patientReference, category, allergen, severity, manifestationCode, clinicalStatus)
		        .stream().map(allergyIntoleranceTranslator::toFhirResource).collect(Collectors.toList());
	}
}
