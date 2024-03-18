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
public class ServiceRequestSearchParams extends BaseResourceSearchParams {
	
	private ReferenceAndListParam patientReference;
	
	private TokenAndListParam code;
	
	private ReferenceAndListParam encounterReference;
	
	private ReferenceAndListParam participantReference;
	
	private DateRangeParam occurrence;
	
	private HasAndListParam hasAndListParam;
	
	@Builder
	public ServiceRequestSearchParams(ReferenceAndListParam patientReference, TokenAndListParam code,
	    ReferenceAndListParam encounterReference, ReferenceAndListParam participantReference, DateRangeParam occurrence,
	    HasAndListParam hasAndListParam, TokenAndListParam id, DateRangeParam lastUpdated, SortSpec sort,
	    HashSet<Include> includes) {
		
		super(id, lastUpdated, sort, includes, null);
		
		this.patientReference = patientReference;
		this.code = code;
		this.encounterReference = encounterReference;
		this.participantReference = participantReference;
		this.occurrence = occurrence;
		this.hasAndListParam = hasAndListParam;
	}
	
	@Override
	public SearchParameterMap toSearchParameterMap() {
		return baseSearchParameterMap().addParameter(FhirConstants.PATIENT_REFERENCE_SEARCH_HANDLER, getPatientReference())
		        .addParameter(FhirConstants.CODED_SEARCH_HANDLER, getCode())
		        .addParameter(FhirConstants.ENCOUNTER_REFERENCE_SEARCH_HANDLER, getEncounterReference())
		        .addParameter(FhirConstants.PARTICIPANT_REFERENCE_SEARCH_HANDLER, getParticipantReference())
		        .addParameter(FhirConstants.DATE_RANGE_SEARCH_HANDLER, FhirConstants.STATE_PROPERTY, getOccurrence())
		        .addParameter(FhirConstants.HAS_SEARCH_HANDLER, FhirConstants.POSTAL_CODE_PROPERTY, getHasAndListParam());
	}
}
