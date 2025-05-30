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

import static lombok.AccessLevel.PROTECTED;
import static org.apache.commons.lang3.Validate.notNull;
import static org.openmrs.module.fhir2.api.translators.impl.FhirTranslatorUtils.getLastUpdated;
import static org.openmrs.module.fhir2.api.translators.impl.FhirTranslatorUtils.getVersionId;

import javax.annotation.Nonnull;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.hl7.fhir.r4.model.Address;
import org.hl7.fhir.r4.model.Extension;
import org.hl7.fhir.r4.model.HumanName;
import org.openmrs.BaseOpenmrsData;
import org.openmrs.Person;
import org.openmrs.PersonAddress;
import org.openmrs.PersonAttribute;
import org.openmrs.PersonName;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.api.dao.FhirPatientDao;
import org.openmrs.module.fhir2.api.translators.BirthDateTranslator;
import org.openmrs.module.fhir2.api.translators.GenderTranslator;
import org.openmrs.module.fhir2.api.translators.PatientReferenceTranslator;
import org.openmrs.module.fhir2.api.translators.PersonAddressTranslator;
import org.openmrs.module.fhir2.api.translators.PersonAttributeTranslator;
import org.openmrs.module.fhir2.api.translators.PersonNameTranslator;
import org.openmrs.module.fhir2.api.translators.PersonTranslator;
import org.openmrs.module.fhir2.api.translators.TelecomTranslator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class PersonTranslatorImpl implements PersonTranslator {
	
	@Getter(PROTECTED)
	@Setter(value = PROTECTED, onMethod_ = @Autowired)
	private PersonNameTranslator nameTranslator;
	
	@Getter(PROTECTED)
	@Setter(value = PROTECTED, onMethod_ = @Autowired)
	private GenderTranslator genderTranslator;
	
	@Getter(PROTECTED)
	@Setter(value = PROTECTED, onMethod_ = @Autowired)
	private BirthDateTranslator birthDateTranslator;
	
	@Getter(PROTECTED)
	@Setter(value = PROTECTED, onMethod_ = @Autowired)
	private PersonAddressTranslator addressTranslator;
	
	@Getter(PROTECTED)
	@Setter(value = PROTECTED, onMethod_ = @Autowired)
	private TelecomTranslator<BaseOpenmrsData> telecomTranslator;
	
	@Getter(PROTECTED)
	@Setter(value = PROTECTED, onMethod_ = @Autowired)
	private PatientReferenceTranslator patientReferenceTranslator;
	
	@Getter(PROTECTED)
	@Setter(value = PROTECTED, onMethod_ = @Autowired)
	private FhirPatientDao patientDao;
	
	@Getter(PROTECTED)
	@Setter(value = PROTECTED, onMethod_ = @Autowired)
	private PersonAttributeTranslator personAttributeTranslator;
	
	@Override
	public org.hl7.fhir.r4.model.Person toFhirResource(@Nonnull Person openmrsPerson) {
		notNull(openmrsPerson, "The Openmrs Person object should not be null");
		
		org.hl7.fhir.r4.model.Person person = new org.hl7.fhir.r4.model.Person();
		person.setId(openmrsPerson.getUuid());
		person.setActive(true);
		
		for (PersonName name : openmrsPerson.getNames()) {
			person.addName(nameTranslator.toFhirResource(name));
		}
		
		if (openmrsPerson.getGender() != null) {
			person.setGender(genderTranslator.toFhirResource(openmrsPerson.getGender()));
		}
		
		person.setBirthDateElement(birthDateTranslator.toFhirResource(openmrsPerson));
		
		for (PersonAddress address : openmrsPerson.getAddresses()) {
			person.addAddress(addressTranslator.toFhirResource(address));
		}
		
		person.addTelecom(telecomTranslator.toFhirResource(openmrsPerson));
		
		if (openmrsPerson.getIsPatient()) {
			person.addLink(new org.hl7.fhir.r4.model.Person.PersonLinkComponent()
			        .setTarget(patientReferenceTranslator.toFhirResource(patientDao.get(openmrsPerson.getUuid()))));
		}
		
		Set<PersonAttribute> attributeSet = openmrsPerson.getAttributes();
		
		for (PersonAttribute personAttribute : attributeSet) {
			Extension personAttributeExtension = personAttributeTranslator.toFhirResource(personAttribute);
			if (personAttributeExtension != null) {
				person.addExtension(personAttributeExtension);
			}
		}
		
		person.getMeta().setLastUpdated(getLastUpdated(openmrsPerson));
		person.getMeta().setVersionId(getVersionId(openmrsPerson));
		
		return person;
	}
	
	@Override
	public Person toOpenmrsType(@Nonnull org.hl7.fhir.r4.model.Person person) {
		notNull(person, "The Person object should not be null");
		return toOpenmrsType(new Person(), person);
	}
	
	@Override
	public Person toOpenmrsType(@Nonnull Person openmrsPerson, @Nonnull org.hl7.fhir.r4.model.Person person) {
		notNull(openmrsPerson, "The existing Openmrs Person object should not be null");
		notNull(person, "The Person object should not be null");
		
		if (person.hasId()) {
			openmrsPerson.setUuid(person.getIdElement().getIdPart());
		}
		
		for (HumanName name : person.getName()) {
			openmrsPerson.addName(nameTranslator.toOpenmrsType(name));
		}
		
		if (person.hasGender()) {
			openmrsPerson.setGender(genderTranslator.toOpenmrsType(person.getGender()));
		}
		
		if (person.hasBirthDateElement()) {
			birthDateTranslator.toOpenmrsType(openmrsPerson, person.getBirthDateElement());
		}
		
		for (Address address : person.getAddress()) {
			openmrsPerson.addAddress(addressTranslator.toOpenmrsType(address));
		}
		
		person.getTelecom().stream()
		        .map(contactPoint -> (PersonAttribute) telecomTranslator.toOpenmrsType(new PersonAttribute(), contactPoint))
		        .distinct().filter(Objects::nonNull).forEach(openmrsPerson::addAttribute);
		
		List<Extension> personAttributeExtensions = person.getExtension().stream()
		        .filter(extension -> extension.getUrl().equals(FhirConstants.OPENMRS_FHIR_EXT_PERSON_ATTRIBUTE))
		        .collect(Collectors.toList());
		
		for (Extension extension : personAttributeExtensions) {
			PersonAttribute personAttribute = personAttributeTranslator.toOpenmrsType(extension);
			if (personAttribute != null) {
				openmrsPerson.addAttribute(personAttribute);
			}
		}
		
		return openmrsPerson;
	}
}
