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

import lombok.AccessLevel;
import lombok.Setter;
import org.hl7.fhir.r4.model.Encounter;
import org.openmrs.EncounterProvider;
import org.openmrs.module.fhir2.api.dao.FhirPractitionerDao;
import org.openmrs.module.fhir2.api.translators.EncounterParticipantTranslator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Setter(AccessLevel.PACKAGE)
public class EncounterParticipantTranslatorImpl extends BaseReferenceHandlingTranslator implements EncounterParticipantTranslator {
	
	@Autowired
	private FhirPractitionerDao practitionerDao;
	
	@Override
	public Encounter.EncounterParticipantComponent toFhirResource(EncounterProvider encounterProvider) {
		if (encounterProvider == null || encounterProvider.getVoided()) {
			return null;
		}
		
		Encounter.EncounterParticipantComponent participantComponent = new Encounter.EncounterParticipantComponent();
		participantComponent.setIndividual(createPractitionerReference(encounterProvider.getProvider()));
		return participantComponent;
	}
	
	@Override
	public EncounterProvider toOpenmrsType(EncounterProvider encounterProvider,
	        Encounter.EncounterParticipantComponent encounterParticipantComponent) {
		notNull(encounterProvider, "The existing EncounterProvider object should not be null");
		notNull(encounterParticipantComponent, "The EncounterParticipantComponent object should not be null");
		
		getReferenceId(encounterParticipantComponent.getIndividual())
		        .map(practitionerUuid -> practitionerDao.get(practitionerUuid)).ifPresent(encounterProvider::setProvider);
		
		return encounterProvider;
	}
}
