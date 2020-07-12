/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.fhir2;

import lombok.extern.slf4j.Slf4j;
import org.openmrs.EncounterRole;
import org.openmrs.EncounterType;
import org.openmrs.api.EncounterService;
import org.openmrs.api.context.Context;
import org.openmrs.module.BaseModuleActivator;

/**
 * This class contains the logic that is run every time this module is either started or shutdown
 */
@SuppressWarnings("unused")
@Slf4j
public class FhirActivator extends BaseModuleActivator {
	
	/**
	 * @return The immunizations encounter type
	 */
	public static EncounterType getImmunizationsEncounterTypeOrCreateIfMissing() {
		String uuid = Context.getAdministrationService()
		        .getGlobalProperty(FhirConstants.IMMUNIZATIONS_ENCOUNTER_TYPE_PROPERTY);
		EncounterService es = Context.getEncounterService();
		EncounterType encounterType = es.getEncounterTypeByUuid(uuid);
		if (encounterType == null) {
			encounterType = new EncounterType("Immunizations Encounter",
			        "An encounter to which immunizations obs groups are attached.");
			encounterType.setUuid(uuid);
			encounterType = es.saveEncounterType(encounterType);
		}
		return encounterType;
	}
	
	/**
	 * @return The administering encounter role
	 */
	public static EncounterRole getAdministeringEncounterRoleOrCreateIfMissing() {
		String uuid = Context.getAdministrationService()
		        .getGlobalProperty(FhirConstants.ADMINISTERING_ENCOUNTER_ROLE_PROPERTY);
		EncounterService es = Context.getEncounterService();
		EncounterRole encounterRole = es.getEncounterRoleByUuid(uuid);
		if (encounterRole == null) {
			encounterRole = new EncounterRole();
			encounterRole.setName("Administering Encounter Role");
			encounterRole
			        .setDescription("An encounter role to use when administering medications/vaccines during encounters.");
			encounterRole.setUuid(uuid);
			encounterRole = es.saveEncounterRole(encounterRole);
		}
		return encounterRole;
	}
	
	@Override
	public void started() {
		getImmunizationsEncounterTypeOrCreateIfMissing();
		getAdministeringEncounterRoleOrCreateIfMissing();
		log.info("Started FHIR");
	}
	
	@Override
	public void stopped() {
		log.info("Shutdown FHIR");
	}
	
}
