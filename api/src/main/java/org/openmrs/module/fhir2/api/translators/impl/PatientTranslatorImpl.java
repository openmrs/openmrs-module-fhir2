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

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import lombok.AccessLevel;
import lombok.Setter;
import org.hl7.fhir.exceptions.FHIRException;
import org.hl7.fhir.r4.model.Address;
import org.hl7.fhir.r4.model.BooleanType;
import org.hl7.fhir.r4.model.ContactPoint;
import org.hl7.fhir.r4.model.DateTimeType;
import org.hl7.fhir.r4.model.HumanName;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Patient;
import org.openmrs.BaseOpenmrsData;
import org.openmrs.PatientIdentifier;
import org.openmrs.PersonAddress;
import org.openmrs.PersonAttribute;
import org.openmrs.PersonName;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.api.FhirGlobalPropertyService;
import org.openmrs.module.fhir2.api.dao.FhirPersonDao;
import org.openmrs.module.fhir2.api.translators.GenderTranslator;
import org.openmrs.module.fhir2.api.translators.PatientIdentifierTranslator;
import org.openmrs.module.fhir2.api.translators.PatientTranslator;
import org.openmrs.module.fhir2.api.translators.PersonAddressTranslator;
import org.openmrs.module.fhir2.api.translators.PersonNameTranslator;
import org.openmrs.module.fhir2.api.translators.ProvenanceTranslator;
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
	private PersonAddressTranslator addressTranslator;
	
	@Autowired
	private FhirGlobalPropertyService globalPropertyService;
	
	@Autowired
	private FhirPersonDao fhirPersonDao;
	
	@Autowired
	private TelecomTranslator<BaseOpenmrsData> telecomTranslator;
	
	@Autowired
	private ProvenanceTranslator<org.openmrs.Patient> provenanceTranslator;
	
	@Override
	public Patient toFhirResource(org.openmrs.Patient openmrsPatient) {
		notNull(openmrsPatient, "The Openmrs Patient object should not be null");
		
		Patient patient = new Patient();
		patient.setId(openmrsPatient.getUuid());
		patient.setBirthDate(openmrsPatient.getBirthdate());
		patient.setActive(!openmrsPatient.getVoided());
		
		if (openmrsPatient.getDead()) {
			if (openmrsPatient.getDeathDate() != null) {
				patient.setDeceased(new DateTimeType(openmrsPatient.getDeathDate()));
			} else {
				patient.setDeceased(new BooleanType(true));
			}
		} else {
			patient.setDeceased(new BooleanType(false));
		}
		
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
		patient.setTelecom(getPatientContactDetails(openmrsPatient));
		patient.getMeta().setLastUpdated(openmrsPatient.getDateChanged());
		patient.addContained(provenanceTranslator.getCreateProvenance(openmrsPatient));
		patient.addContained(provenanceTranslator.getUpdateProvenance(openmrsPatient));
		
		return patient;
	}
	
	public List<ContactPoint> getPatientContactDetails(@NotNull org.openmrs.Patient patient) {
		return fhirPersonDao
		        .getActiveAttributesByPersonAndAttributeTypeUuid(patient,
		            globalPropertyService.getGlobalProperty(FhirConstants.PERSON_CONTACT_ATTRIBUTE_TYPE))
		        .stream().map(telecomTranslator::toFhirResource).collect(Collectors.toList());
	}
	
	@Override
	public org.openmrs.Patient toOpenmrsType(Patient fhirPatient) {
		notNull(fhirPatient, "The Patient object should not be null");
		return toOpenmrsType(new org.openmrs.Patient(), fhirPatient);
	}
	
	@Override
	public org.openmrs.Patient toOpenmrsType(org.openmrs.Patient currentPatient, Patient patient) {
		notNull(currentPatient, "The existing Openmrs Patient object should not be null");
		notNull(patient, "The Patient object should not be null");
		
		currentPatient.setUuid(patient.getId());
		currentPatient.setBirthdate(patient.getBirthDate());
		
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
		
		for (Identifier identifier : patient.getIdentifier()) {
			currentPatient.addIdentifier(identifierTranslator.toOpenmrsType(identifier));
		}
		
		for (HumanName name : patient.getName()) {
			currentPatient.addName(nameTranslator.toOpenmrsType(name));
		}
		
		if (patient.hasGender()) {
			currentPatient.setGender(genderTranslator.toOpenmrsType(patient.getGender()));
		}
		
		for (Address address : patient.getAddress()) {
			currentPatient.addAddress(addressTranslator.toOpenmrsType(address));
		}
		
		patient.getTelecom().stream()
		        .map(contactPoint -> (PersonAttribute) telecomTranslator.toOpenmrsType(new PersonAttribute(), contactPoint))
		        .distinct().filter(Objects::nonNull).forEach(currentPatient::addAttribute);
		
		return currentPatient;
	}
}
