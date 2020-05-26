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

import lombok.AccessLevel;
import lombok.Setter;
import org.hl7.fhir.r4.model.Address;
import org.hl7.fhir.r4.model.HumanName;
import org.openmrs.Person;
import org.openmrs.PersonAddress;
import org.openmrs.PersonName;
import org.openmrs.module.fhir2.api.dao.FhirPatientDao;
import org.openmrs.module.fhir2.api.translators.GenderTranslator;
import org.openmrs.module.fhir2.api.translators.PatientReferenceTranslator;
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
	
	@Autowired
	private PatientReferenceTranslator patientReferenceTranslator;
	
	@Autowired
	private FhirPatientDao patientDao;
	
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
			
			if (openmrsPerson.getIsPatient()) {
				person.addLink(new org.hl7.fhir.r4.model.Person.PersonLinkComponent()
				        .setTarget(patientReferenceTranslator.toFhirResource(patientDao.get(openmrsPerson.getUuid()))));
			}
			person.getMeta().setLastUpdated(openmrsPerson.getDateChanged());
			person.addContained(provenanceTranslator.getCreateProvenance(openmrsPerson));
			person.addContained(provenanceTranslator.getUpdateProvenance(openmrsPerson));
		}
		return person;
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
