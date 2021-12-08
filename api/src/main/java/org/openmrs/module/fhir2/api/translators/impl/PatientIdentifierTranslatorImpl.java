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

import javax.annotation.Nonnull;

import lombok.AccessLevel;
import lombok.Setter;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.Type;
import org.openmrs.PatientIdentifier;
import org.openmrs.PatientIdentifierType;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.api.FhirPatientIdentifierSystemService;
import org.openmrs.module.fhir2.api.FhirPatientService;
import org.openmrs.module.fhir2.api.dao.FhirLocationDao;
import org.openmrs.module.fhir2.api.translators.PatientIdentifierTranslator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Setter(AccessLevel.PACKAGE)
// TODO Create proper "System" value
public class PatientIdentifierTranslatorImpl extends BaseReferenceHandlingTranslator implements PatientIdentifierTranslator {
	
	@Autowired
	private FhirPatientIdentifierSystemService patientIdentifierSystemService;
	
	@Autowired
	private FhirPatientService patientService;
	
	@Autowired
	private FhirLocationDao locationDao;
	
	@Override
	public Identifier toFhirResource(@Nonnull PatientIdentifier identifier) {
		if (identifier == null || identifier.getVoided()) {
			return null;
		}
		
		Identifier patientIdentifier = new Identifier();
		
		patientIdentifier.setValue(identifier.getIdentifier()).setId(identifier.getUuid());
		
		if (identifier.getPreferred() != null) {
			if (identifier.getPreferred()) {
				patientIdentifier.setUse(Identifier.IdentifierUse.OFFICIAL);
			} else {
				patientIdentifier.setUse(Identifier.IdentifierUse.USUAL);
			}
		}
		
		if (identifier.getIdentifierType() != null) {
			patientIdentifier.setType(new CodeableConcept(new Coding().setCode(identifier.getIdentifierType().getUuid()))
			        .setText(identifier.getIdentifierType().getName()));
		}
		
		if (identifier.getLocation() != null) {
			patientIdentifier.addExtension().setUrl(FhirConstants.OPENMRS_FHIR_EXT_PATIENT_IDENTIFIER_LOCATION)
			        .setValue(createLocationReference(identifier.getLocation()));
		}
		
		if (identifier.getIdentifierType() != null) {
			patientIdentifier
			        .setSystem(patientIdentifierSystemService.getUrlByPatientIdentifierType(identifier.getIdentifierType()));
		}
		
		return patientIdentifier;
	}
	
	@Override
	public PatientIdentifier toOpenmrsType(@Nonnull Identifier identifier) {
		if (identifier == null) {
			return null;
		}
		
		return toOpenmrsType(new PatientIdentifier(), identifier);
	}
	
	@Override
	public PatientIdentifier toOpenmrsType(@Nonnull PatientIdentifier patientIdentifier, @Nonnull Identifier identifier) {
		if (patientIdentifier == null || identifier == null) {
			return patientIdentifier;
		}
		
		patientIdentifier.setUuid(identifier.getId());
		
		patientIdentifier.setIdentifier(identifier.getValue());
		
		patientIdentifier.setPreferred(Identifier.IdentifierUse.OFFICIAL.equals(identifier.getUse()));
		
		PatientIdentifierType type = patientService.getPatientIdentifierTypeByIdentifier(identifier);
		if (type == null && patientIdentifier.getIdentifierType() == null) {
			return null;
		}
		
		patientIdentifier.setIdentifierType(type);
		
		if (identifier.hasExtension(FhirConstants.OPENMRS_FHIR_EXT_PATIENT_IDENTIFIER_LOCATION)) {
			Type identifierLocationType = identifier
			        .getExtensionByUrl(FhirConstants.OPENMRS_FHIR_EXT_PATIENT_IDENTIFIER_LOCATION).getValue();
			
			if (identifierLocationType instanceof Reference) {
				getReferenceId((Reference) identifierLocationType).map(uuid -> locationDao.get(uuid))
				        .ifPresent(patientIdentifier::setLocation);
			}
		}
		
		return patientIdentifier;
	}
}
