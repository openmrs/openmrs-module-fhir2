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

import java.util.Collection;

import ca.uhn.fhir.rest.param.ReferenceAndListParam;
import ca.uhn.fhir.rest.param.TokenAndListParam;
import ca.uhn.fhir.rest.param.TokenOrListParam;
import org.openmrs.Allergy;

public interface FhirAllergyIntoleranceDao extends FhirDao<Allergy> {
	
	Collection<Allergy> searchForAllergies(ReferenceAndListParam patientReference, TokenOrListParam category,
	        TokenAndListParam allergen, TokenOrListParam severity, TokenAndListParam manifestationCode,
	        TokenOrListParam clinicalStatus);
}
