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

import javax.annotation.Nonnull;

import java.util.HashSet;

import ca.uhn.fhir.model.api.Include;
import ca.uhn.fhir.rest.api.SortSpec;
import ca.uhn.fhir.rest.api.server.IBundleProvider;
import ca.uhn.fhir.rest.param.DateRangeParam;
import ca.uhn.fhir.rest.param.ReferenceAndListParam;
import ca.uhn.fhir.rest.param.StringAndListParam;
import ca.uhn.fhir.rest.param.TokenAndListParam;
import org.hl7.fhir.r4.model.Observation;

public interface FhirMediaService extends FhirService<Observation> {
	
	Observation get(@Nonnull String uuid);
	
	IBundleProvider searchForMedia(TokenAndListParam status, TokenAndListParam type, ReferenceAndListParam subject,
	        ReferenceAndListParam encounterReference, DateRangeParam createdDateTime, TokenAndListParam contentType,
	        StringAndListParam contentDataType, StringAndListParam contentTitle, DateRangeParam contentCreated,
	        DateRangeParam lastUpdated, HashSet<Include> includes, HashSet<Include> revIncludes, SortSpec sort);
}
