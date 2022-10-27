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

import java.util.HashSet;

import ca.uhn.fhir.model.api.Include;
import ca.uhn.fhir.rest.api.SortSpec;
import ca.uhn.fhir.rest.param.DateRangeParam;
import ca.uhn.fhir.rest.param.HasAndListParam;
import ca.uhn.fhir.rest.param.ReferenceAndListParam;
import ca.uhn.fhir.rest.param.TokenAndListParam;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.openmrs.module.fhir2.FhirConstants;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class EncounterSearchParams extends BaseResourceSearchParams {
	
	private DateRangeParam date;
	
	private ReferenceAndListParam location;
	
	private ReferenceAndListParam participant;
	
	private ReferenceAndListParam subject;
	
	private TokenAndListParam encounterType;
	
	private TokenAndListParam tag;
	
	private HasAndListParam hasAndListParam;
	
	@Builder
	public EncounterSearchParams(DateRangeParam date, ReferenceAndListParam location, ReferenceAndListParam participant,
	    ReferenceAndListParam subject, TokenAndListParam encounterType, TokenAndListParam tag,
	    HasAndListParam hasAndListParam, TokenAndListParam id, DateRangeParam lastUpdated, SortSpec sort,
	    HashSet<Include> includes, HashSet<Include> revIncludes) {
		
		super(id, lastUpdated, sort, includes, revIncludes);
		
		this.date = date;
		this.location = location;
		this.participant = participant;
		this.subject = subject;
		this.encounterType = encounterType;
		this.tag = tag;
		this.hasAndListParam = hasAndListParam;
	}
	
	@Override
	public SearchParameterMap toSearchParameterMap() {
		return baseSearchParameterMap().addParameter(FhirConstants.DATE_RANGE_SEARCH_HANDLER, getDate())
		        .addParameter(FhirConstants.LOCATION_REFERENCE_SEARCH_HANDLER, getLocation())
		        .addParameter(FhirConstants.PARTICIPANT_REFERENCE_SEARCH_HANDLER, getParticipant())
		        .addParameter(FhirConstants.PATIENT_REFERENCE_SEARCH_HANDLER, getSubject())
		        .addParameter(FhirConstants.ENCOUNTER_TYPE_REFERENCE_SEARCH_HANDLER, getEncounterType())
		        .addParameter(FhirConstants.HAS_SEARCH_HANDLER, getHasAndListParam());
	}
}
