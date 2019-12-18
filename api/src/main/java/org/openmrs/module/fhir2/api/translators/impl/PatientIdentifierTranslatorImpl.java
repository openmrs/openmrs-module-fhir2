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

import javax.inject.Inject;

import lombok.AccessLevel;
import lombok.Setter;
import org.hl7.fhir.r4.model.Identifier;
import org.openmrs.PatientIdentifier;
import org.openmrs.PatientIdentifierType;
import org.openmrs.api.APIException;
import org.openmrs.module.fhir2.api.FhirPatientService;
import org.openmrs.module.fhir2.api.translators.PatientIdentifierTranslator;
import org.springframework.stereotype.Component;

@Component
@Setter(AccessLevel.PACKAGE)
// TODO Map PatientIdentifierType to Type
// TODO Create proper "System" value
public class PatientIdentifierTranslatorImpl implements PatientIdentifierTranslator {
	
	@Inject
	private FhirPatientService patientService;
	
	@Override
	public Identifier toFhirResource(PatientIdentifier identifier) {
		if (identifier == null) {
			return null;
		}
		
		Identifier patientIdentifier = new Identifier();
		if (identifier.getPreferred()) {
			patientIdentifier.setUse(Identifier.IdentifierUse.OFFICIAL);
		} else {
			patientIdentifier.setUse(Identifier.IdentifierUse.USUAL);
		}
		
		patientIdentifier.setSystem(identifier.getIdentifierType().getName()).setValue(identifier.getIdentifier())
		        .setId(identifier.getUuid());
		
		return patientIdentifier;
	}
	
	@Override
	public PatientIdentifier toOpenmrsType(Identifier identifier) {
		if (identifier == null) {
			return null;
		}
		
		PatientIdentifier patientIdentifier = new PatientIdentifier();
		patientIdentifier.setUuid(identifier.getId());
		patientIdentifier.setIdentifier(identifier.getValue());
		
		if (Identifier.IdentifierUse.OFFICIAL.equals(identifier.getUse())) {
			patientIdentifier.setPreferred(true);
		} else {
			patientIdentifier.setPreferred(false);
		}
		
		PatientIdentifierType type = patientService.getPatientIdentifierTypeByIdentifier(identifier);
		if (type == null) {
			// TODO implement error handling
			throw new APIException("cannot find identifier type for ");
		}
		
		patientIdentifier.setIdentifierType(type);
		
		return patientIdentifier;
	}
}
