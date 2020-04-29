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

import lombok.AccessLevel;
import lombok.Setter;
import org.hl7.fhir.r4.model.Reference;
import org.openmrs.Encounter;
import org.openmrs.module.fhir2.api.dao.FhirEncounterDao;
import org.openmrs.module.fhir2.api.translators.EncounterReferenceTranslator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Setter(AccessLevel.PACKAGE)
public class EncounterReferenceTranslatorImpl extends BaseReferenceHandlingTranslator implements EncounterReferenceTranslator {
	
	@Autowired
	private FhirEncounterDao encounterDao;
	
	@Override
	public Reference toFhirResource(Encounter encounter) {
		if (encounter == null) {
			return null;
		}
		
		return createEncounterReference(encounter);
	}
	
	@Override
	public Encounter toOpenmrsType(Reference encounter) {
		if (encounter == null) {
			return null;
		}
		
		if (!encounter.getType().equals("Encounter")) {
			throw new IllegalArgumentException("Reference must be to an Encounter not a " + encounter.getType());
		}
		
		String uuid = getReferenceId(encounter);
		if (uuid == null) {
			return null;
		}
		
		return encounterDao.getEncounterByUuid(uuid);
	}
}
