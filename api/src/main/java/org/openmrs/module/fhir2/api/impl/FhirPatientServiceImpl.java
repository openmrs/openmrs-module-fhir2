/**
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

import lombok.AccessLevel;
import lombok.Setter;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Patient;
import org.openmrs.PatientIdentifierType;
import org.openmrs.module.fhir2.api.FhirPatientService;
import org.openmrs.module.fhir2.api.dao.FhirPatientDao;
import org.openmrs.module.fhir2.api.translators.PatientTranslator;
import org.springframework.stereotype.Component;

@Component
@Setter(AccessLevel.PACKAGE)
public class FhirPatientServiceImpl implements FhirPatientService {
	
	@Inject
	private PatientTranslator translator;
	
	@Inject
	private FhirPatientDao dao;
	
	@Override
	public Patient getPatientByUuid(String uuid) {
		return translator.toFhirResource(dao.getPatientByUuid(uuid));
	}
	
	@Override
	public PatientIdentifierType getPatientIdentifierTypeByIdentifier(Identifier identifier) {
		return dao.getPatientIdentifierTypeByNameOrUuid(identifier.getSystem(), null);
	}
	
	@Override
	public Collection<Patient> findPatientsByName(String name) {
		return dao.findPatientsByName(name).stream().map(translator::toFhirResource).collect(Collectors.toList());
	}
	
	@Override
	public Collection<Patient> findPatientsByGivenName(String given) {
		return dao.findPatientsByGivenName(given).stream().map(translator::toFhirResource).collect(Collectors.toList());
	}
	
	@Override
	public Collection<Patient> findPatientsByFamilyName(String family) {
		return dao.findPatientsByFamilyName(family).stream().map(translator::toFhirResource).collect(Collectors.toList());
	}
}
