/**
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
import org.hl7.fhir.r4.model.Address;
import org.hl7.fhir.r4.model.HumanName;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Patient;
import org.openmrs.PatientIdentifier;
import org.openmrs.PersonAddress;
import org.openmrs.PersonName;
import org.openmrs.module.fhir2.api.translators.AddressTranslator;
import org.openmrs.module.fhir2.api.translators.GenderTranslator;
import org.openmrs.module.fhir2.api.translators.PatientIdentifierTranslator;
import org.openmrs.module.fhir2.api.translators.PatientTranslator;
import org.openmrs.module.fhir2.api.translators.PersonNameTranslator;
import org.springframework.stereotype.Component;

@Component
@Setter(AccessLevel.PACKAGE)
public class PatientTranslatorImpl implements PatientTranslator {
	
	@Inject
	private PatientIdentifierTranslator identifierTranslator;
	
	@Inject
	private PersonNameTranslator nameTranslator;
	
	@Inject
	private GenderTranslator genderTranslator;
	
	@Inject
	private AddressTranslator addressTranslator;
	
	@Override
	public Patient toFhirResource(org.openmrs.Patient openmrsPatient) {
		Patient patient = new Patient();
		
		patient.setId(openmrsPatient.getUuid());
		
		for (PatientIdentifier identifier : openmrsPatient.getActiveIdentifiers()) {
			patient.addIdentifier(identifierTranslator.toFhirResource(identifier));
		}
		
		for (PersonName name : openmrsPatient.getNames()) {
			patient.addName(nameTranslator.toFhirResource(name));
		}
		
		if (openmrsPatient.getGender() != null) {
			patient.setGender(genderTranslator.toFhirResource(openmrsPatient.getGender()));
		}
		
		for (PersonAddress address : openmrsPatient.getAddresses()) {
			patient.addAddress(addressTranslator.toFhirResource(address));
		}
		
		return patient;
	}
	
	@Override
	public org.openmrs.Patient toOpenmrsType(Patient fhirPatient) {
		org.openmrs.Patient patient = new org.openmrs.Patient();
		
		patient.setUuid(fhirPatient.getId());
		
		for (Identifier identifier : fhirPatient.getIdentifier()) {
			patient.addIdentifier(identifierTranslator.toOpenmrsType(identifier));
		}
		
		for (HumanName name : fhirPatient.getName()) {
			patient.addName(nameTranslator.toOpenmrsType(name));
		}
		
		if (fhirPatient.getGender() != null) {
			patient.setGender(genderTranslator.toOpenmrsType(fhirPatient.getGender()));
		}
		
		for (Address address : fhirPatient.getAddress()) {
			patient.addAddress(addressTranslator.toOpenmrsType(address));
		}
		
		return patient;
	}
}
