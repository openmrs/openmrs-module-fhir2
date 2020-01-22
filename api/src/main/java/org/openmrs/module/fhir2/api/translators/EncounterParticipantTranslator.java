/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.fhir2.api.translators;

import org.openmrs.EncounterProvider;
import org.hl7.fhir.r4.model.Encounter;

public interface EncounterParticipantTranslator extends ToFhirTranslator<EncounterProvider, Encounter.EncounterParticipantComponent>, UpdatableOpenmrsTranslator<EncounterProvider, Encounter.EncounterParticipantComponent> {
	
	/**
	 * Maps an {@link org.openmrs.EncounterProvider} to an
	 * {@link org.hl7.fhir.r4.model.Encounter.EncounterParticipantComponent}
	 * 
	 * @param encounter the OpenMRS encounter to translate
	 * @return the corresponding FHIR Encounter.EncounterParticipantComponent resource
	 */
	@Override
	Encounter.EncounterParticipantComponent toFhirResource(EncounterProvider encounter);
	
	/**
	 * Maps an {@link org.hl7.fhir.r4.model.Encounter.EncounterParticipantComponent} to an existing
	 * {@link org.openmrs.EncounterProvider}
	 * 
	 * @param encounterProvider the encounterProvider to update
	 * @param encounterParticipantComponent the encounterParticipantComponent to map
	 * @return an updated version of the encounterProvider
	 */
	@Override
	EncounterProvider toOpenmrsType(EncounterProvider encounterProvider,
	        Encounter.EncounterParticipantComponent encounterParticipantComponent);
}
