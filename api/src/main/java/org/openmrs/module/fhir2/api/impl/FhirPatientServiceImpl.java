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

import ca.uhn.fhir.rest.api.SortSpec;
import ca.uhn.fhir.rest.param.DateRangeParam;
import ca.uhn.fhir.rest.param.StringAndListParam;
import ca.uhn.fhir.rest.param.TokenAndListParam;
import lombok.AccessLevel;
import lombok.Setter;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Patient;
import org.openmrs.PatientIdentifierType;
import org.openmrs.module.fhir2.api.FhirPatientService;
import org.openmrs.module.fhir2.api.dao.FhirPatientDao;
import org.openmrs.module.fhir2.api.translators.PatientTranslator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional
@Setter(AccessLevel.PACKAGE)
public class FhirPatientServiceImpl implements FhirPatientService {
	
	@Autowired
	private PatientTranslator translator;
	
	@Autowired
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
	public Collection<Patient> searchForPatients(StringAndListParam name, StringAndListParam given,
	        StringAndListParam family, TokenAndListParam identifier, TokenAndListParam gender, DateRangeParam birthDate,
	        DateRangeParam deathDate, TokenAndListParam deceased, StringAndListParam city, StringAndListParam state,
	        StringAndListParam postalCode, StringAndListParam country, SortSpec sort) {
		return dao.searchForPatients(name, given, family, identifier, gender, birthDate, deathDate, deceased, city, state,
		    postalCode, country, sort).stream().map(translator::toFhirResource).collect(Collectors.toList());
	}
}
