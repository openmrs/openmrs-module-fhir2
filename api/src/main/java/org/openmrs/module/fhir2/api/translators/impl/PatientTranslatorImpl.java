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
import static org.openmrs.module.fhir2.api.translators.impl.FhirTranslatorUtils.getLastUpdated;
import static org.openmrs.module.fhir2.api.translators.impl.FhirTranslatorUtils.getVersionId;

import javax.annotation.Nonnull;

import java.util.*;
import java.util.stream.Collectors;

import lombok.AccessLevel;
import lombok.Setter;
import org.hl7.fhir.exceptions.FHIRException;
import org.hl7.fhir.r4.model.*;
import org.openmrs.BaseOpenmrsData;
import org.openmrs.PatientIdentifier;
import org.openmrs.PersonAddress;
import org.openmrs.PersonAttribute;
import org.openmrs.PersonName;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.api.FhirGlobalPropertyService;
import org.openmrs.module.fhir2.api.dao.FhirPersonDao;
import org.openmrs.module.fhir2.api.translators.BirthDateTranslator;
import org.openmrs.module.fhir2.api.translators.GenderTranslator;
import org.openmrs.module.fhir2.api.translators.PatientIdentifierTranslator;
import org.openmrs.module.fhir2.api.translators.PatientTranslator;
import org.openmrs.module.fhir2.api.translators.PersonAddressTranslator;
import org.openmrs.module.fhir2.api.translators.PersonNameTranslator;
import org.openmrs.module.fhir2.api.translators.TelecomTranslator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Setter(AccessLevel.PACKAGE)
public class PatientTranslatorImpl implements PatientTranslator {
	
	@Autowired
	private PatientIdentifierTranslator identifierTranslator;
	
	@Autowired
	private PersonNameTranslator nameTranslator;
	
	@Autowired
	private GenderTranslator genderTranslator;
	
	@Autowired
	private BirthDateTranslator birthDateTranslator;
	
	@Autowired
	private PersonAddressTranslator addressTranslator;
	
	@Autowired
	private FhirGlobalPropertyService globalPropertyService;
	
	@Autowired
	private FhirPersonDao fhirPersonDao;
	
	@Autowired
	private TelecomTranslator<BaseOpenmrsData> telecomTranslator;

	@Autowired
	private PersonAttributeTranslatorImpl personAttributeTranslator;
	
	@Override
	public Patient toFhirResource(@Nonnull org.openmrs.Patient openmrsPatient) {
		notNull(openmrsPatient, "The Openmrs Patient object should not be null");
		
		Patient patient = new Patient();
		patient.setId(openmrsPatient.getUuid());
		patient.setActive(!openmrsPatient.getVoided());
		
		for (PatientIdentifier identifier : openmrsPatient.getActiveIdentifiers()) {
			patient.addIdentifier(identifierTranslator.toFhirResource(identifier));
		}
		
		for (PersonName name : openmrsPatient.getNames()) {
			patient.addName(nameTranslator.toFhirResource(name));
		}
		
		if (openmrsPatient.getGender() != null) {
			patient.setGender(genderTranslator.toFhirResource(openmrsPatient.getGender()));
		}
		
		patient.setBirthDateElement(birthDateTranslator.toFhirResource(openmrsPatient));
		
		if (openmrsPatient.getDead()) {
			if (openmrsPatient.getDeathDate() != null) {
				patient.setDeceased(new DateTimeType(openmrsPatient.getDeathDate()));
			} else {
				patient.setDeceased(new BooleanType(true));
			}
		} else {
			patient.setDeceased(new BooleanType(false));
		}
		
		for (PersonAddress address : openmrsPatient.getAddresses()) {
			patient.addAddress(addressTranslator.toFhirResource(address));
		}

		if (!openmrsPatient.getAttributes().isEmpty()) {
			patient.setExtension(getPersonAttributeExtensions(openmrsPatient));
		}
		
		patient.setTelecom(getPatientContactDetails(openmrsPatient));
		patient.getMeta().setLastUpdated(getLastUpdated(openmrsPatient));
		patient.getMeta().setVersionId(getVersionId(openmrsPatient));
		
		return patient;
	}
	
	public List<ContactPoint> getPatientContactDetails(@Nonnull org.openmrs.Patient patient) {
		String personContactAttributeType = globalPropertyService
		        .getGlobalProperty(FhirConstants.PERSON_CONTACT_POINT_ATTRIBUTE_TYPE);
		
		if (personContactAttributeType == null || personContactAttributeType.isEmpty()) {
			return Collections.emptyList();
		}
		
		return fhirPersonDao.getActiveAttributesByPersonAndAttributeTypeUuid(patient, personContactAttributeType).stream()
		        .map(telecomTranslator::toFhirResource).collect(Collectors.toList());
	}

	public List<Extension> getPersonAttributeExtensions(@Nonnull org.openmrs.Patient openmrsPatient) {
		List<Extension> personAttributeExtensions = new ArrayList<>();
		Set<PersonAttribute> personAttributes = openmrsPatient.getAttributes();

		for(PersonAttribute personAttribute : personAttributes) {
			personAttributeExtensions.add(personAttributeTranslator.toFhirResource(personAttribute));
		}

		return personAttributeExtensions;
	}
	
	@Override
	public org.openmrs.Patient toOpenmrsType(@Nonnull Patient fhirPatient) {
		notNull(fhirPatient, "The Patient object should not be null");
		return toOpenmrsType(new org.openmrs.Patient(), fhirPatient);
	}
	
	@Override
	public org.openmrs.Patient toOpenmrsType(@Nonnull org.openmrs.Patient currentPatient, @Nonnull Patient patient) {
		notNull(currentPatient, "The existing Openmrs Patient object should not be null");
		notNull(patient, "The Patient object should not be null");
		
		if (patient.hasId()) {
			currentPatient.setUuid(patient.getIdElement().getIdPart());
		}
		
		for (Identifier identifier : patient.getIdentifier()) {
			PatientIdentifier omrsIdentifier = identifierTranslator.toOpenmrsType(identifier);
			if (omrsIdentifier != null) {
				currentPatient.addIdentifier(omrsIdentifier);
			}
		}
		
		for (HumanName name : patient.getName()) {
			PersonName existingName = null;
			if (name.hasId()) {
				existingName = currentPatient.getNames().stream().filter(n -> n.getUuid().equals(name.getId())).findFirst()
				        .orElse(null);
			}
			
			PersonName pn = nameTranslator.toOpenmrsType(existingName != null ? existingName : new PersonName(), name);
			currentPatient.addName(pn);
		}
		
		if (patient.hasGender()) {
			currentPatient.setGender(genderTranslator.toOpenmrsType(patient.getGender()));
		}
		
		if (patient.hasBirthDateElement()) {
			birthDateTranslator.toOpenmrsType(currentPatient, patient.getBirthDateElement());
		}
		
		if (patient.hasDeceased()) {
			try {
				patient.getDeceasedBooleanType();
				
				currentPatient.setDead(patient.getDeceasedBooleanType().booleanValue());
			}
			catch (FHIRException ignored) {}
			
			try {
				patient.getDeceasedDateTimeType();
				
				currentPatient.setDead(true);
				currentPatient.setDeathDate(patient.getDeceasedDateTimeType().getValue());
			}
			catch (FHIRException ignored) {}
		}
		
		for (Address address : patient.getAddress()) {
			currentPatient.addAddress(addressTranslator.toOpenmrsType(address));
		}
		
		patient.getTelecom().stream()
		        .map(contactPoint -> (PersonAttribute) telecomTranslator.toOpenmrsType(new PersonAttribute(), contactPoint))
		        .distinct().filter(Objects::nonNull).forEach(currentPatient::addAttribute);

		List<Extension> patientAttributeExtensions = patient.getExtensionsByUrl(FhirConstants.OPENMRS_FHIR_EXT_PERSON_ATTRIBUTE);

		if (patientAttributeExtensions != null) {
			for(Extension patientAttributeExtension : patientAttributeExtensions) {
				currentPatient.addAttribute(personAttributeTranslator.toOpenmrsType(patientAttributeExtension));
			}
		}

		return currentPatient;
	}
}
