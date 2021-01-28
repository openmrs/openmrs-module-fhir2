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
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.openmrs.EncounterType;
import org.openmrs.Location;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.api.mappings.EncounterClassMap;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collections;
import java.util.List;

@Setter(AccessLevel.PACKAGE)
public abstract class BaseEncounterTranslator {
	
	@Autowired
	private EncounterClassMap encounterClassMap;
	
	protected Coding mapLocationToClass(Location location) {
		Coding coding = new Coding();
		coding.setSystem(FhirConstants.ENCOUNTER_CLASS_VALUE_SET_URI);
		// The default code for anything that cannot be matched with FHIR codes.
		coding.setCode("AMB");
		if (location == null) {
			return coding;
		}
		String classCode = encounterClassMap.getFhirClass(location.getUuid());
		if (classCode != null) {
			coding.setCode(classCode);
		}
		return coding;
	}

	protected List<CodeableConcept> mapEncounterTypeClass(EncounterType openmrsEncounterType) {
		Coding coding = new Coding();
		coding.setCode(openmrsEncounterType.getUuid());
		coding.setDisplay(openmrsEncounterType.getName());

		CodeableConcept code = new CodeableConcept();
		code.setCoding(Collections.singletonList(coding));

		return Collections.singletonList(code);
	}

	protected EncounterType mapEncounterTypeField(CodeableConcept fhirEncounterType) {
		EncounterType openmrsEncounterType = null;

		if(fhirEncounterType != null && fhirEncounterType.getCoding() != null && !fhirEncounterType.getCoding().isEmpty()) {
			openmrsEncounterType = new EncounterType();
			openmrsEncounterType.setName(fhirEncounterType.getCodingFirstRep().getDisplay());
			openmrsEncounterType.setUuid(fhirEncounterType.getCodingFirstRep().getCode());
		}

		return openmrsEncounterType;
	}

}
