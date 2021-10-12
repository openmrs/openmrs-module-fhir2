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
import org.hl7.fhir.r4.model.Address;
import org.hl7.fhir.r4.model.HumanName;
import org.hl7.fhir.r4.model.Practitioner;
import org.openmrs.Person;
import org.openmrs.PersonAddress;
import org.openmrs.PersonName;
import org.openmrs.module.fhir2.api.translators.BirthDateTranslator;
import org.openmrs.module.fhir2.api.translators.GenderTranslator;
import org.openmrs.module.fhir2.api.translators.PersonAddressTranslator;
import org.openmrs.module.fhir2.api.translators.PersonNameTranslator;
import org.springframework.beans.factory.annotation.Autowired;

@Setter(AccessLevel.PACKAGE)
public class BasePractitionerTranslator {
	
	@Autowired
	private PersonNameTranslator nameTranslator;
	
	@Autowired
	private GenderTranslator genderTranslator;
	
	@Autowired
	private BirthDateTranslator birthDateTranslator;
	
	@Autowired
	private PersonAddressTranslator addressTranslator;
	
	protected void personToPractitioner(Person person, Practitioner practitioner) {
		if (person == null) {
			return;
		}
		
		if (practitioner == null) {
			return;
		}
		
		practitioner.setBirthDateElement(birthDateTranslator.toFhirResource(person));
		
		practitioner.setGender(genderTranslator.toFhirResource(person.getGender()));
		for (PersonName name : person.getNames()) {
			practitioner.addName(nameTranslator.toFhirResource(name));
		}
		
		for (PersonAddress address : person.getAddresses()) {
			practitioner.addAddress(addressTranslator.toFhirResource(address));
		}
	}
	
	protected void practitionerToPerson(Practitioner practitioner, Person person) {
		if (practitioner == null) {
			return;
		}
		
		if (person == null) {
			return;
		}
		
		for (HumanName name : practitioner.getName()) {
			person.addName(nameTranslator.toOpenmrsType(name));
		}
		
		for (Address address : practitioner.getAddress()) {
			person.addAddress(addressTranslator.toOpenmrsType(address));
		}
		
		if (practitioner.hasBirthDateElement()) {
			birthDateTranslator.toOpenmrsType(person, practitioner.getBirthDateElement());
		}
		
		person.setGender(genderTranslator.toOpenmrsType(practitioner.getGender()));
	}
	
}
