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

import javax.inject.Inject;
import javax.validation.constraints.NotNull;

import java.util.List;
import java.util.Set;
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
	private PersonAddressTranslator addressTranslator;
	
	@Inject
	private FhirGlobalPropertyService globalPropertyService;
	
	@Inject
	private FhirPersonDao fhirPersonDao;
	
	@Inject
	private TelecomTranslator<Object> telecomTranslator;
	
	@Inject
	private ProvenanceTranslator<org.openmrs.Patient> provenanceTranslator;
	
	@Override
	public Patient toFhirResource(org.openmrs.Patient openmrsPatient) {
		Patient patient = new Patient();
		if (openmrsPatient != null) {
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
		}
		
		return patient;
	}
	
	public List<ContactPoint> getPatientContactDetails(@NotNull org.openmrs.Patient patient) {
		return fhirPersonDao
		        .getActiveAttributesByPersonAndAttributeTypeUuid(patient,
		            globalPropertyService.getGlobalProperty(FhirConstants.PERSON_ATTRIBUTE_TYPE_PROPERTY))
		        .stream().map(telecomTranslator::toFhirResource).collect(Collectors.toList());
	}
	
	@Override
	public org.openmrs.Patient toOpenmrsType(Patient fhirPatient) {
		return toOpenmrsType(new org.openmrs.Patient(), fhirPatient);
	}
	
	@Override
	public org.openmrs.Patient toOpenmrsType(org.openmrs.Patient currentPatient, Patient patient) {
		notNull(currentPatient, "currentPatient cannot be null");
		
		if (patient == null) {
			return currentPatient;
		}
		
		currentPatient.setUuid(patient.getId());
		currentPatient.setBirthdate(patient.getBirthDate());
		
		if (!patient.getActive()) {
			currentPatient.setVoided(true);
			currentPatient.setVoidReason("Voided by FHIR module");
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
		Set<PersonAttribute> attributes = patient.getTelecom().stream()
		        .map(contactPoint -> (PersonAttribute) telecomTranslator.toOpenmrsType(new PersonAttribute(), contactPoint))
		        .collect(Collectors.toSet());
		currentPatient.setAttributes(attributes);
		currentPatient.setDateChanged(patient.getMeta().getLastUpdated());
		
		return currentPatient;
	}
}
