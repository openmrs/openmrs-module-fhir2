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

import java.util.HashSet;

import ca.uhn.fhir.model.api.Include;
import ca.uhn.fhir.rest.api.SortSpec;
import ca.uhn.fhir.rest.api.server.IBundleProvider;
import ca.uhn.fhir.rest.param.DateRangeParam;
import ca.uhn.fhir.rest.param.HasAndListParam;
import ca.uhn.fhir.rest.param.ReferenceAndListParam;
import ca.uhn.fhir.rest.param.TokenAndListParam;
import ca.uhn.fhir.rest.param.TokenParam;
import org.hl7.fhir.r4.model.Encounter;

public interface FhirEncounterService extends FhirService<Encounter> {
	
	IBundleProvider searchForEncounters(DateRangeParam date, ReferenceAndListParam location,
	        ReferenceAndListParam participant, ReferenceAndListParam subject, TokenAndListParam encounterType,
	        TokenAndListParam id, DateRangeParam lastUpdated, SortSpec sort, HashSet<Include> includes,
	        HashSet<Include> revIncludes, HasAndListParam hasMedicationRequestParam);
	
	IBundleProvider getEncounterEverything(TokenParam identifier);
}
