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

import ca.uhn.fhir.rest.api.SortSpec;
import ca.uhn.fhir.rest.api.server.IBundleProvider;
import ca.uhn.fhir.rest.param.DateRangeParam;
import ca.uhn.fhir.rest.param.QuantityAndListParam;
import ca.uhn.fhir.rest.param.ReferenceAndListParam;
import ca.uhn.fhir.rest.param.ReferenceParam;
import ca.uhn.fhir.rest.param.StringAndListParam;
import ca.uhn.fhir.rest.param.TokenAndListParam;
import org.hl7.fhir.r4.model.Observation;

public interface FhirObservationService {
	
	Observation getObservationByUuid(@NotNull String uuid);
	
	IBundleProvider searchForObservations(ReferenceAndListParam encounterReference, ReferenceAndListParam patientReference,
	        ReferenceParam hasMemberReference, TokenAndListParam valueConcept, DateRangeParam valueDateParam,
	        QuantityAndListParam valueQuantityParam, StringAndListParam valueStringParam, DateRangeParam date,
	        TokenAndListParam code, SortSpec sort);
}
