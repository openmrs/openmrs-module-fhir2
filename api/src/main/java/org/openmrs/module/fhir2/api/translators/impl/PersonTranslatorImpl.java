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

import static org.apache.commons.lang3.Validate.notNull;

import javax.validation.constraints.NotNull;

import java.util.ArrayList;
import java.util.List;

import lombok.AccessLevel;
import lombok.Setter;
import org.hl7.fhir.r4.model.Address;
import org.hl7.fhir.r4.model.HumanName;
import org.hl7.fhir.r4.model.Reference;
import org.openmrs.Person;
import org.openmrs.PersonAddress;
import org.openmrs.PersonName;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.api.translators.GenderTranslator;
import org.openmrs.module.fhir2.api.translators.PersonAddressTranslator;
import org.openmrs.module.fhir2.api.translators.PersonNameTranslator;
import org.openmrs.module.fhir2.api.translators.PersonTelecomTranslator;
import org.openmrs.module.fhir2.api.translators.PersonTranslator;
import org.openmrs.module.fhir2.api.translators.ProvenanceTranslator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Setter(AccessLevel.PACKAGE)
public class PersonTranslatorImpl implements PersonTranslator {
	
	@Autowired
	private PersonNameTranslator nameTranslator;
	
	@Autowired
	private PersonAddressTranslator addressTranslator;
	
	@Autowired
	private GenderTranslator genderTranslator;
	
	@Autowired
	private PersonTelecomTranslator telecomTranslator;
	
	@Autowired
	private ProvenanceTranslator<Person> provenanceTranslator;
	
	@Override
	public org.hl7.fhir.r4.model.Person toFhirResource(@NotNull Person openmrsPerson) {
		org.hl7.fhir.r4.model.Person person = new org.hl7.fhir.r4.model.Person();
		if (openmrsPerson != null) {
			person.setId(openmrsPerson.getUuid());
			person.setActive(!openmrsPerson.getVoided());
			person.setBirthDate(openmrsPerson.getBirthdate());
			
			if (openmrsPerson.getGender() != null) {
				person.setGender(genderTranslator.toFhirResource(openmrsPerson.getGender()));
			}
			
			for (PersonName name : openmrsPerson.getNames()) {
				person.addName(nameTranslator.toFhirResource(name));
			}
			
			for (PersonAddress address : openmrsPerson.getAddresses()) {
				person.addAddress(addressTranslator.toFhirResource(address));
			}
			person.setTelecom(telecomTranslator.toFhirResource(openmrsPerson));
			
			buildPersonLinks(openmrsPerson, person);
			person.getMeta().setLastUpdated(openmrsPerson.getDateChanged());
			person.addContained(provenanceTranslator.getCreateProvenance(openmrsPerson));
			person.addContained(provenanceTranslator.getUpdateProvenance(openmrsPerson));
		}
		return person;
	}
	
	/**
	 * TODO Find a better way to implement this generically and maybe move to different package
	 */
	private void buildPersonLinks(@NotNull Person openmrsPerson, org.hl7.fhir.r4.model.Person person) {
		if (openmrsPerson.getIsPatient()) {
			List<org.hl7.fhir.r4.model.Person.PersonLinkComponent> links = new ArrayList<>();
			org.hl7.fhir.r4.model.Person.PersonLinkComponent linkComponent = new org.hl7.fhir.r4.model.Person.PersonLinkComponent();
			String uri = FhirConstants.PATIENT + "/" + openmrsPerson.getUuid();
			Reference patientReference = new Reference();
			PersonName name = openmrsPerson.getPersonName();
			patientReference.setDisplay(name.getFullName());
			patientReference.setId(uri);
			linkComponent.setTarget(patientReference);
			links.add(linkComponent);
			
			person.setLink(links);
		}
	}
	
	@Override
	public Person toOpenmrsType(org.hl7.fhir.r4.model.Person person) {
		return toOpenmrsType(new Person(), person);
	}
	
	@Override
	public Person toOpenmrsType(Person openmrsPerson, org.hl7.fhir.r4.model.Person person) {
		notNull(openmrsPerson, "openmrsPerson cannot be null");
		
		if (person == null) {
			return openmrsPerson;
		}
		
		openmrsPerson.setUuid(person.getId());
		openmrsPerson.setVoided(person.getActive());
		openmrsPerson.setBirthdate(person.getBirthDate());
		
		if (person.hasGender()) {
			openmrsPerson.setGender(genderTranslator.toOpenmrsType(person.getGender()));
		}
		
		for (HumanName name : person.getName()) {
			openmrsPerson.addName(nameTranslator.toOpenmrsType(name));
		}
		
		for (Address address : person.getAddress()) {
			openmrsPerson.addAddress(addressTranslator.toOpenmrsType(address));
		}
		openmrsPerson.setAttributes(telecomTranslator.toOpenmrsType(person.getTelecom()));
		
		return openmrsPerson;
	}
}
