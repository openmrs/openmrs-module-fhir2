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
import org.openmrs.module.fhir2.api.translators.OrderIdentifierTranslator;
import org.openmrs.module.fhir2.api.util.FhirUtils;

@Slf4j
public final class ReferenceHandlingTranslator {
	
	public static Reference createEncounterReference(@Nonnull Encounter encounter) {
		return createEncounterReference((OpenmrsObject) encounter);
	}
	
	public static Reference createEncounterReference(@Nonnull Visit visit) {
		return createEncounterReference((OpenmrsObject) visit);
	}
	
	private static Reference createEncounterReference(@Nonnull OpenmrsObject encounter) {
		return createReferenceOfType(encounter, FhirConstants.ENCOUNTER);
	}
	
	public static Reference createValueSetReference(@Nonnull Concept concept) {
		if (concept == null || !concept.getSet()) {
			return null;
		}
		
		return createReferenceOfType(concept, FhirConstants.VALUESET);
	}
	
	public static Reference createMedicationReference(@Nonnull Drug drug) {
		return createReferenceOfType(drug, FhirConstants.MEDICATION).setDisplay(drug.getDisplayName());
	}
	
	public static Reference createObservationReference(@Nonnull Obs obs) {
		return createReferenceOfType(obs, FhirConstants.OBSERVATION);
	}
	
	public static Reference createLocationReferenceByUuid(@Nonnull String uuid) {
		return createReferenceOfType(uuid, FhirConstants.LOCATION);
	}
	
	public static Reference createLocationReference(@Nonnull Location location) {
		return createReferenceOfType(location, FhirConstants.LOCATION).setDisplay(getMetadataTranslation(location));
	}
	
	public static Reference createPatientReference(@Nonnull Patient patient) {
		Reference reference = createReferenceOfType(patient, FhirConstants.PATIENT);
		
		StringBuilder sb = new StringBuilder();
		if (patient.getPersonName() != null) {
			sb.append(patient.getPersonName().getFullName());
		}
		
		PatientIdentifier identifier = patient.getPatientIdentifier();
		if (identifier != null && identifier.getIdentifier() != null) {
			if (sb.length() > 0) {
				sb.append(" ");
			}
			sb.append("(");
			
			PatientIdentifierType identifierType = identifier.getIdentifierType();
			if (identifierType != null && identifierType.getName() != null) {
				sb.append(getMetadataTranslation(identifierType)).append(": ");
			}
			
			sb.append(identifier.getIdentifier()).append(")");
		}
		
		reference.setDisplay(sb.toString());
		return reference;
	}
	
	public static Reference createPractitionerReference(@Nonnull User user) {
		Reference reference = createReferenceOfType(user, FhirConstants.PRACTITIONER);
		
		if (user.getPerson() != null) {
			if (user.getPerson().getPersonName() != null) {
				reference.setDisplay(user.getPerson().getPersonName().getFullName());
			}
		}
		
		return reference;
	}
	
	public static Reference createPractitionerReference(@Nonnull Provider provider) {
		Reference reference = createReferenceOfType(provider, FhirConstants.PRACTITIONER);
		
		if (provider.getPerson() != null) {
			Person person = provider.getPerson();
			if (person.getPersonName() != null) {
				reference.setDisplay(person.getPersonName().getFullName());
			}
		}
		
		if (provider.getIdentifier() != null) {
			reference.setIdentifier(new Identifier().setValue(provider.getIdentifier()));
		}
		
		return reference;
	}
	
	public static Reference createOrderReference(@Nonnull Order order) {
		return createOrderReference(order, null);
	}
	
	public static Reference createOrderReference(@Nonnull Order order, OrderIdentifierTranslator orderIdentifierTranslator) {
		if (order == null) {
			return null;
		}
		
		if (order instanceof TestOrder) {
			return createTestOrderReference((TestOrder) order, orderIdentifierTranslator);
		} else if (order instanceof DrugOrder) {
			return createDrugOrderReference((DrugOrder) order, orderIdentifierTranslator);
		} else {
			log.warn("Could not determine order type for order {}", order);
			return null;
		}
	}
	
	public static Reference createDrugOrderReference(@Nonnull DrugOrder drugOrder) {
		return createDrugOrderReference(drugOrder, null);
	}
	
	public static Reference createDrugOrderReference(@Nonnull DrugOrder drugOrder,
	        OrderIdentifierTranslator orderIdentifierTranslator) {
		if (drugOrder == null) {
			return null;
		}
		
		Reference reference = createReferenceOfType(drugOrder, FhirConstants.MEDICATION_REQUEST);
		
		if (orderIdentifierTranslator != null) {
			reference.setIdentifier(orderIdentifierTranslator.toFhirResource(drugOrder));
		}
		
		return reference;
	}
	
	public static Reference createTestOrderReference(@Nonnull TestOrder order) {
		return createTestOrderReference(order, null);
	}
	
	public static Reference createTestOrderReference(@Nonnull TestOrder order,
	        OrderIdentifierTranslator orderIdentifierTranslator) {
		if (order == null) {
			return null;
		}
		
		Reference reference = createReferenceOfType(order, FhirConstants.SERVICE_REQUEST);
		
		if (orderIdentifierTranslator != null) {
			reference.setIdentifier(orderIdentifierTranslator.toFhirResource(order));
		}
		
		return reference;
	}
	
	public static Optional<String> getReferenceType(Reference reference) {
		return FhirUtils.getReferenceType(reference);
	}
	
	public static Optional<String> getReferenceId(Reference reference) {
		return FhirUtils.referenceToId(reference.getReference());
	}
	
	private static Reference createReferenceOfType(@Nonnull OpenmrsObject object, @Nonnull String referenceType) {
		return createReferenceOfType(object.getUuid(), referenceType);
	}
	
	private static Reference createReferenceOfType(@Nonnull String uuid, @Nonnull String referenceType) {
		return new Reference().setReference(referenceType + "/" + uuid).setType(referenceType);
	}
}
