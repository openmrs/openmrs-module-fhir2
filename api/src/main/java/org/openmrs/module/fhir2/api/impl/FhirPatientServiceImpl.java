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

import ca.uhn.fhir.rest.api.SortSpec;
import ca.uhn.fhir.rest.param.DateRangeParam;
import ca.uhn.fhir.rest.param.StringOrListParam;
import ca.uhn.fhir.rest.param.TokenOrListParam;
import lombok.AccessLevel;
import lombok.Setter;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Patient;
import org.openmrs.PatientIdentifierType;
import org.openmrs.module.fhir2.api.FhirPatientService;
import org.openmrs.module.fhir2.api.dao.FhirPatientDao;
import org.openmrs.module.fhir2.api.translators.PatientTranslator;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional
@Setter(AccessLevel.PACKAGE)
public class FhirPatientServiceImpl implements FhirPatientService {
	
	@Inject
	private PatientTranslator translator;
	
	@Inject
	private FhirPatientDao dao;
	
	@Override
	@Transactional(readOnly = true)
	public Patient getPatientByUuid(String uuid) {
		return translator.toFhirResource(dao.getPatientByUuid(uuid));
	}
	
	@Override
	@Transactional(readOnly = true)
	public PatientIdentifierType getPatientIdentifierTypeByIdentifier(Identifier identifier) {
		return dao.getPatientIdentifierTypeByNameOrUuid(identifier.getSystem(), null);
	}
	
	@Override
	@Transactional(readOnly = true)
	public Collection<Patient> searchForPatients(StringOrListParam name, StringOrListParam given, StringOrListParam family,
	        TokenOrListParam identifier, TokenOrListParam gender, DateRangeParam birthDate, DateRangeParam deathDate,
	        TokenOrListParam deceased, StringOrListParam city, StringOrListParam state, StringOrListParam postalCode,
	        SortSpec sort) {
		return dao.searchForPatients(name, given, family, identifier, gender, birthDate, deathDate, deceased, city, state,
		    postalCode, sort).stream().map(translator::toFhirResource).collect(Collectors.toList());
	}
}
