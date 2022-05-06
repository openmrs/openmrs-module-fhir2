/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.fhir2.api.search.param;

import java.io.Serializable;
import java.util.HashSet;

import ca.uhn.fhir.model.api.Include;
import ca.uhn.fhir.rest.api.SortSpec;
import ca.uhn.fhir.rest.param.DateRangeParam;
import ca.uhn.fhir.rest.param.HasAndListParam;
import ca.uhn.fhir.rest.param.ReferenceAndListParam;
import ca.uhn.fhir.rest.param.TokenAndListParam;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EncounterSearchParams implements Serializable {
	
	private DateRangeParam date;
	
	private ReferenceAndListParam location;
	
	private ReferenceAndListParam participant;
	
	private ReferenceAndListParam subject;
	
	private TokenAndListParam encounterType;
	
	private TokenAndListParam id;
	
	private DateRangeParam lastUpdated;
	
	private SortSpec sort;
	
	private HashSet<Include> includes;
	
	private HashSet<Include> revIncludes;
	
	private HasAndListParam hasAndListParam;
	
	private TokenAndListParam tag;
}
