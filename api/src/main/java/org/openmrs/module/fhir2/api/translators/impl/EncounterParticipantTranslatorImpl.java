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

import lombok.AccessLevel;
import lombok.Setter;
import org.hl7.fhir.r4.model.Encounter;
import org.openmrs.EncounterProvider;
import org.openmrs.Provider;
import org.openmrs.module.fhir2.api.FhirPractitionerService;
import org.openmrs.module.fhir2.api.translators.EncounterParticipantTranslator;
import org.openmrs.module.fhir2.api.translators.PractitionerTranslator;
import org.springframework.stereotype.Component;

@Component
@Setter(AccessLevel.PACKAGE)
public class EncounterParticipantTranslatorImpl extends AbstractReferenceHandlingTranslator implements EncounterParticipantTranslator {
	
	@Inject
	FhirPractitionerService practitionerService;
	
	@Inject
	PractitionerTranslator practitionerTranslator;
	
	@Override
	public Encounter.EncounterParticipantComponent toFhirResource(EncounterProvider encounter) {
		Encounter.EncounterParticipantComponent participantComponent = new Encounter.EncounterParticipantComponent();
		participantComponent.setIndividual(createPractitionerReference(encounter.getProvider()));
		return participantComponent;
	}
	
	@Override
	public EncounterProvider toOpenmrsType(EncounterProvider encounterProvider,
	        Encounter.EncounterParticipantComponent encounterParticipantComponent) {
		if (encounterParticipantComponent == null) {
			return encounterProvider;
		}
		String practitionerUuid = getReferenceId(encounterParticipantComponent.getIndividual());
		Provider provider = practitionerTranslator
		        .toOpenmrsType(practitionerService.getPractitionerByUuid(practitionerUuid));
		encounterProvider.setProvider(provider);
		return encounterProvider;
	}
}
