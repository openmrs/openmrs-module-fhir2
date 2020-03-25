/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.fhir2.api.dao;

import javax.validation.constraints.NotNull;

import java.util.Collection;

import ca.uhn.fhir.rest.api.SortSpec;
import ca.uhn.fhir.rest.param.DateRangeParam;
import ca.uhn.fhir.rest.param.StringOrListParam;
import ca.uhn.fhir.rest.param.TokenOrListParam;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifierType;

public interface FhirPatientDao {
	
	Patient getPatientById(@NotNull Integer id);
	
	Patient getPatientByUuid(@NotNull String uuid);
	
	PatientIdentifierType getPatientIdentifierTypeByNameOrUuid(String name, String uuid);
	
	Collection<Patient> searchForPatients(StringOrListParam name, StringOrListParam given, StringOrListParam family,
	        TokenOrListParam identifier, TokenOrListParam gender, DateRangeParam birthDate, DateRangeParam deathDate,
	        TokenOrListParam deceased, StringOrListParam city, StringOrListParam state, StringOrListParam postalCode,
	        StringOrListParam country, SortSpec sort);
}
