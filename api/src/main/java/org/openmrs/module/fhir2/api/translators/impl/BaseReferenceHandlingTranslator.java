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

import static org.openmrs.module.fhir2.api.util.FhirUtils.getMetadataTranslation;

import javax.annotation.Nonnull;

import java.util.Optional;

import lombok.AccessLevel;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Reference;
import org.openmrs.Concept;
import org.openmrs.Drug;
import org.openmrs.DrugOrder;
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
import org.openmrs.TestOrder;
import org.openmrs.User;
import org.openmrs.Visit;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.api.util.FhirUtils;

@Setter(AccessLevel.PACKAGE)
@Slf4j
public abstract class BaseReferenceHandlingTranslator {
	
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
	
	protected Reference createValueSetReference(@Nonnull Concept concept) {
		if (concept == null || !concept.getSet()) {
			return null;
		}
		return new Reference().setReference(FhirConstants.VALUESET + "/" + concept.getUuid())
		        .setType(FhirConstants.VALUESET);
	}
	
	protected Reference createMedicationReference(@Nonnull Drug drug) {
		return new Reference().setReference(FhirConstants.MEDICATION + "/" + drug.getUuid())
		        .setType(FhirConstants.MEDICATION).setDisplay(drug.getDisplayName());
	}
	
	protected Reference createObservationReference(@Nonnull Obs obs) {
		return new Reference().setReference(FhirConstants.OBSERVATION + "/" + obs.getUuid())
		        .setType(FhirConstants.OBSERVATION);
	}
	
	protected Reference createLocationReferenceByUuid(@Nonnull String uuid) {
		return new Reference().setReference(FhirConstants.LOCATION + "/" + uuid).setType(FhirConstants.LOCATION);
	}
	
	protected Reference createLocationReference(@Nonnull Location location) {
		return new Reference().setReference(FhirConstants.LOCATION + "/" + location.getUuid())
		        .setType(FhirConstants.LOCATION).setDisplay(getMetadataTranslation(location));
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
				sb.append(getMetadataTranslation(identifierType)).append(": ");
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
			}
			
			reference.setDisplay(sb.toString());
		}
		
		return reference;
	}
	
	protected Reference createOrderReference(@Nonnull Order order) {
		if (order == null) {
			return null;
		}
		
		if (order instanceof TestOrder) {
			return new Reference().setReference(FhirConstants.SERVICE_REQUEST + "/" + order.getUuid())
			        .setType(FhirConstants.SERVICE_REQUEST);
		} else if (order instanceof DrugOrder) {
			return createDrugOrderReference((DrugOrder) order);
		} else {
			log.warn("Could not determine order type for order {}", order);
			return null;
		}
	}
	
	protected Reference createDrugOrderReference(@Nonnull DrugOrder drugOrder) {
		if (drugOrder == null) {
			return null;
		}
		return new Reference().setReference(FhirConstants.MEDICATION_REQUEST + "/" + drugOrder.getUuid())
		        .setType(FhirConstants.MEDICATION_REQUEST);
	}
	
	protected Optional<String> getReferenceType(Reference reference) {
		return FhirUtils.getReferenceType(reference);
	}
	
	protected Optional<String> getReferenceId(Reference reference) {
		return FhirUtils.referenceToId(reference.getReference());
	}
}
