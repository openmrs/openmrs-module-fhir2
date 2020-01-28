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

import javax.inject.Inject;

import lombok.AccessLevel;
import lombok.Setter;
import org.hl7.fhir.r4.model.Encounter;
import org.openmrs.module.fhir2.api.FhirEncounterService;
import org.openmrs.module.fhir2.api.dao.FhirEncounterDao;
import org.openmrs.module.fhir2.api.translators.EncounterTranslator;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional
@Setter(AccessLevel.PACKAGE)
public class FhirEncounterServiceImpl implements FhirEncounterService {
	
	@Inject
	FhirEncounterDao dao;
	
	@Inject
	EncounterTranslator translator;
	
	@Override
	@Transactional(readOnly = true)
	public Encounter getEncounterByUuid(String uuid) {
		return translator.toFhirResource(dao.getEncounterByUuid(uuid));
	}

	@Override
	public Collection<Encounter> findEncountersByPatientIdentifier(String patientIdentifier) {
		return dao.findEncountersByPatientIdentifier(patientIdentifier)
				.stream()
				.map(translator::toFhirResource)
				.collect(Collectors.toList());
	}
}
