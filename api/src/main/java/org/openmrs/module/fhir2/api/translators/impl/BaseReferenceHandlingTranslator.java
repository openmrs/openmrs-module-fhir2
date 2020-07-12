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

import java.util.Optional;

import lombok.AccessLevel;
import lombok.Setter;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Reference;
import org.openmrs.Drug;
import org.openmrs.Encounter;
import org.openmrs.Location;
import org.openmrs.Obs;
import org.openmrs.OpenmrsObject;
import org.openmrs.Order;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifier;
import org.openmrs.PatientIdentifierType;
import org.openmrs.Person;
import org.openmrs.Provider;
import org.openmrs.User;
import org.openmrs.Visit;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.api.util.FhirUtils;

@Setter(AccessLevel.PACKAGE)
public abstract class BaseReferenceHandlingTranslator {
	
	public static final String DRUG_ORDER_TYPE_UUID = "131168f4-15f5-102d-96e4-000c29c2a5d7";
	
	public static final String TEST_ORDER_TYPE_UUID = "52a447d3-a64a-11e3-9aeb-50e549534c5e";
	
	protected Reference createEncounterReference(@Nonnull Encounter encounter) {
		return createEncounterReference((OpenmrsObject) encounter);
	}
	
	protected Reference createEncounterReference(@Nonnull Visit visit) {
		return createEncounterReference((OpenmrsObject) visit);
	}
	
	private Reference createEncounterReference(@Nonnull OpenmrsObject encounter) {
		return new Reference().setReference(FhirConstants.ENCOUNTER + "/" + encounter.getUuid())
		        .setType(FhirConstants.ENCOUNTER);
	}
	
	protected Reference createVisitReference(@Nonnull Visit visit) {
		return new Reference().setReference(FhirConstants.VISIT + "/" + visit.getUuid()).setType(FhirConstants.ENCOUNTER);
	}
	
	protected Reference createMedicationReference(@Nonnull Drug drug) {
		return new Reference().setReference(FhirConstants.MEDICATION + "/" + drug.getUuid())
		        .setType(FhirConstants.MEDICATION);
	}
	
	protected Reference createObservationReference(@Nonnull Obs obs) {
		return new Reference().setReference(FhirConstants.OBSERVATION + "/" + obs.getUuid())
		        .setType(FhirConstants.OBSERVATION);
	}
	
	protected Reference createLocationReference(@Nonnull Location location) {
		return new Reference().setReference(FhirConstants.LOCATION + "/" + location.getUuid())
		        .setType(FhirConstants.LOCATION).setDisplay(location.getName());
	}
	
	protected Reference createPatientReference(@Nonnull Patient patient) {
		Reference reference = new Reference().setReference(FhirConstants.PATIENT + "/" + patient.getUuid())
		        .setType(FhirConstants.PATIENT);
		
		StringBuilder sb = new StringBuilder();
		if (patient.getPersonName() != null) {
			sb.append(patient.getPersonName().getFullName());
		}
		
		PatientIdentifier identifier = patient.getPatientIdentifier();
		if (identifier != null && identifier.getIdentifier() != null) {
			sb.append(" (");
			
			PatientIdentifierType identifierType = identifier.getIdentifierType();
			if (identifierType != null && identifierType.getName() != null) {
				sb.append(identifierType.getName()).append(": ");
			}
			
			sb.append(identifier.getIdentifier()).append(")");
		}
		
		reference.setDisplay(sb.toString());
		
		return reference;
	}
	
	protected Reference createPractitionerReference(@Nonnull User user) {
		Reference reference = new Reference().setReference(FhirConstants.PRACTITIONER + "/" + user.getUuid())
		        .setType(FhirConstants.PRACTITIONER);
		
		if (user.getPerson() != null) {
			if (user.getPerson().getPersonName() != null) {
				reference.setDisplay(user.getPerson().getPersonName().getFullName());
			}
		}
		
		return reference;
	}
	
	protected Reference createPractitionerReference(@Nonnull Provider provider) {
		Reference reference = new Reference().setReference(FhirConstants.PRACTITIONER + "/" + provider.getUuid())
		        .setType(FhirConstants.PRACTITIONER);
		
		if (provider.getPerson() != null) {
			StringBuilder sb = new StringBuilder();
			
			Person person = provider.getPerson();
			if (person.getPersonName() != null) {
				sb.append(person.getPersonName().getFullName());
			}
			
			if (provider.getIdentifier() != null) {
				reference.setIdentifier(new Identifier().setValue(provider.getIdentifier()));
				sb.append(" (").append(FhirConstants.IDENTIFIER).append(": ").append(provider.getIdentifier()).append(")");
			}
			
			reference.setDisplay(sb.toString());
		}
		
		return reference;
	}
	
	protected Reference createOrderReference(@Nonnull Order order) {
		if (order.getOrderType() == null) {
			return null;
		}
		
		if (order.getOrderType().getUuid().equals(TEST_ORDER_TYPE_UUID)) {
			return new Reference().setReference(FhirConstants.SERVICE_REQUEST + "/" + order.getUuid())
			        .setType(FhirConstants.SERVICE_REQUEST);
		} else if (order.getOrderType().getUuid().equals(DRUG_ORDER_TYPE_UUID)) {
			return new Reference().setReference(FhirConstants.MEDICATION + "/" + order.getUuid())
			        .setType(FhirConstants.MEDICATION);
		} else {
			throw new IllegalArgumentException("Cannot create reference of undetermined order type");
		}
	}
	
	protected Optional<String> getReferenceType(Reference reference) {
		return FhirUtils.getReferenceType(reference);
	}
	
	protected Optional<String> getReferenceId(Reference reference) {
		return FhirUtils.referenceToId(reference.getReference());
	}
}
