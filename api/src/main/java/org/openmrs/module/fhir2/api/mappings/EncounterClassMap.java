/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.fhir2.api.mappings;

import javax.validation.constraints.NotNull;

import org.springframework.stereotype.Component;

// A class for mapping OpenMRS location names to FHIR Encounter classes. In the properties file the
// location names are keys and FHIR class names are values; it is a many to one map. Note going from
// FHIR classes to OpenMRS locations is not well-defined hence we do not expose the inverse map.
@Component
public class EncounterClassMap extends BaseMapping {
	
	public EncounterClassMap() {
		super("encounterClassMap.properties");
	}
	
	public String getFhirClass(@NotNull String locationUuid) {
		return getValue(locationUuid).orElse(null);
	}
}
