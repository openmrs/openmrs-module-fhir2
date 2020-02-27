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

import javax.inject.Inject;

import java.util.function.Supplier;

import lombok.AccessLevel;
import lombok.Setter;
import org.hl7.fhir.r4.model.Observation;
import org.hl7.fhir.r4.model.Reference;
import org.openmrs.Concept;
import org.openmrs.ConceptNumeric;
import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.Person;
import org.openmrs.module.fhir2.api.translators.ConceptTranslator;
import org.openmrs.module.fhir2.api.translators.EncounterReferenceTranslator;
import org.openmrs.module.fhir2.api.translators.ObservationInterpretationTranslator;
import org.openmrs.module.fhir2.api.translators.ObservationReferenceRangeTranslator;
import org.openmrs.module.fhir2.api.translators.ObservationReferenceTranslator;
import org.openmrs.module.fhir2.api.translators.ObservationStatusTranslator;
import org.openmrs.module.fhir2.api.translators.ObservationTranslator;
import org.openmrs.module.fhir2.api.translators.ObservationValueTranslator;
import org.openmrs.module.fhir2.api.translators.PatientReferenceTranslator;
import org.springframework.stereotype.Component;

@Component
@Setter(AccessLevel.PACKAGE)
public class ObservationTranslatorImpl implements ObservationTranslator {
	
	@Inject
	private ObservationStatusTranslator observationStatusTranslator;
	
	@Inject
	private ObservationReferenceTranslator observationReferenceTranslator;
	
	@Inject
	private ObservationValueTranslator observationValueTranslator;
	
	@Inject
	private ConceptTranslator conceptTranslator;
	
	@Inject
	private EncounterReferenceTranslator encounterReferenceTranslator;
	
	@Inject
	private PatientReferenceTranslator patientReferenceTranslator;
	
	@Inject
	private ObservationInterpretationTranslator interpretationTranslator;
	
	@Inject
	private ObservationReferenceRangeTranslator referenceRangeTranslator;
	
	@Override
	public Observation toFhirResource(Obs observation) {
		if (observation == null) {
			return null;
		}
		
		Observation obs = new Observation();
		obs.setId(observation.getUuid());
		obs.setStatus(observationStatusTranslator.toFhirResource(observation));
		
		obs.setEncounter(encounterReferenceTranslator.toFhirResource(observation.getEncounter()));
		
		Person obsPerson = observation.getPerson();
		if (obsPerson != null) {
			try {
				obs.setSubject(patientReferenceTranslator.toFhirResource((Patient) observation.getPerson()));
			}
			catch (ClassCastException ignored) {}
		}
		
		obs.setCode(conceptTranslator.toFhirResource(observation.getConcept()));
		
		if (observation.isObsGrouping()) {
			for (Obs groupObs : observation.getGroupMembers()) {
				obs.addHasMember(observationReferenceTranslator.toFhirResource(groupObs));
			}
		}
		
		obs.setValue(observationValueTranslator.toFhirResource(observation));
		
		obs.addInterpretation(interpretationTranslator.toFhirResource(observation));
		
		if (observation.getValueNumeric() != null) {
			Concept concept = observation.getConcept();
			if (concept instanceof ConceptNumeric) {
				obs.setReferenceRange(referenceRangeTranslator.toFhirResource((ConceptNumeric) concept));
			}
			
		}
		obs.getMeta().setLastUpdated(observation.getDateChanged());
		
		return obs;
	}
	
	@Override
	public Obs toOpenmrsType(Obs existingObs, Observation observation, Supplier<Obs> groupedObsFactory) {
		if (existingObs == null) {
			return null;
		}
		
		if (observation == null) {
			return existingObs;
		}
		
		existingObs.setUuid(observation.getId());
		observationStatusTranslator.toOpenmrsType(existingObs, observation.getStatus());
		
		existingObs.setEncounter(encounterReferenceTranslator.toOpenmrsType(observation.getEncounter()));
		existingObs.setPerson(patientReferenceTranslator.toOpenmrsType(observation.getSubject()));
		
		existingObs.setConcept(conceptTranslator.toOpenmrsType(observation.getCode()));
		
		for (Reference reference : observation.getHasMember()) {
			existingObs.addGroupMember(observationReferenceTranslator.toOpenmrsType(reference));
		}
		
		if (observation.getInterpretation().size() > 0) {
			interpretationTranslator.toOpenmrsType(existingObs, observation.getInterpretation().get(0));
		}
		existingObs.setDateChanged(observation.getMeta().getLastUpdated());
		
		return existingObs;
	}
}
