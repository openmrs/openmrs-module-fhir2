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

import static lombok.AccessLevel.PROTECTED;
import static org.apache.commons.lang3.Validate.notNull;
import static org.openmrs.module.fhir2.api.translators.impl.ReferenceHandlingTranslator.createPractitionerReference;
import static org.openmrs.module.fhir2.api.translators.impl.ReferenceHandlingTranslator.getReferenceId;

import javax.annotation.Nonnull;

import lombok.Getter;
import lombok.Setter;
import org.hl7.fhir.r4.model.Encounter;
import org.openmrs.EncounterProvider;
import org.openmrs.EncounterRole;
import org.openmrs.api.EncounterService;
import org.openmrs.module.fhir2.api.FhirGlobalPropertyService;
import org.openmrs.module.fhir2.api.dao.FhirPractitionerDao;
import org.openmrs.module.fhir2.api.translators.EncounterParticipantTranslator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class EncounterParticipantTranslatorImpl implements EncounterParticipantTranslator {
	
	private static final String DEFAULT_ENCOUNTER_ROLE_UUID_PROPERTY = "fhir2.encounterParticipantComponentUuid";
	
	@Getter(PROTECTED)
	@Setter(value = PROTECTED, onMethod_ = @Autowired)
	private FhirPractitionerDao practitionerDao;
	
	@Getter(PROTECTED)
	@Setter(value = PROTECTED, onMethod_ = @Autowired)
	private EncounterService encounterService;
	
	@Getter(PROTECTED)
	@Setter(value = PROTECTED, onMethod_ = @Autowired)
	private FhirGlobalPropertyService globalPropertyService;
	
	@Override
	public Encounter.EncounterParticipantComponent toFhirResource(@Nonnull EncounterProvider encounterProvider) {
		if (encounterProvider == null || encounterProvider.getVoided()) {
			return null;
		}
		
		Encounter.EncounterParticipantComponent participantComponent = new Encounter.EncounterParticipantComponent();
		participantComponent.setIndividual(createPractitionerReference(encounterProvider.getProvider()));
		return participantComponent;
	}
	
	@Override
	public EncounterProvider toOpenmrsType(@Nonnull EncounterProvider encounterProvider,
	        @Nonnull Encounter.EncounterParticipantComponent encounterParticipantComponent) {
		notNull(encounterProvider, "The existing EncounterProvider object should not be null");
		notNull(encounterParticipantComponent, "The EncounterParticipantComponent object should not be null");
		
		getReferenceId(encounterParticipantComponent.getIndividual())
		        .map(practitionerUuid -> practitionerDao.get(practitionerUuid)).ifPresent(encounterProvider::setProvider);
		
		if (encounterProvider.getEncounterRole() == null) {
			encounterProvider.setEncounterRole(getDefaultEncounterRole());
		}
		
		return encounterProvider;
	}
	
	protected EncounterRole getDefaultEncounterRole() {
		String defaultEncounterRoleUuid = globalPropertyService.getGlobalProperty(DEFAULT_ENCOUNTER_ROLE_UUID_PROPERTY);
		
		String encounterRoleUuid = (defaultEncounterRoleUuid != null && !defaultEncounterRoleUuid.isEmpty())
		        ? defaultEncounterRoleUuid
		        : EncounterRole.UNKNOWN_ENCOUNTER_ROLE_UUID;
		
		EncounterRole role = encounterService.getEncounterRoleByUuid(encounterRoleUuid);
		
		if (role == null) {
			throw new IllegalStateException("Default encounter role not found: " + encounterRoleUuid);
		}
		
		return role;
	}
}
