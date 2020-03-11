/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.fhir2.api;

import javax.validation.constraints.NotNull;

import java.util.Collection;

import ca.uhn.fhir.rest.api.SortSpec;
import ca.uhn.fhir.rest.param.ReferenceOrListParam;
import ca.uhn.fhir.rest.param.StringOrListParam;
import ca.uhn.fhir.rest.param.TokenOrListParam;
import org.hl7.fhir.r4.model.Location;

public interface FhirLocationService {
	
	Location getLocationByUuid(@NotNull String uuid);
	
	Collection<Location> searchForLocations(StringOrListParam name, StringOrListParam city, StringOrListParam country,
	        StringOrListParam postalCode, StringOrListParam state, TokenOrListParam tag, ReferenceOrListParam parent,
	        SortSpec sort);
}
