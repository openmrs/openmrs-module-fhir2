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
import ca.uhn.fhir.rest.param.DateRangeParam;
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
public class MedicationRequestSearchParams extends BaseResourceSearchParams {
	
	private ReferenceAndListParam patientReference;
	
	private ReferenceAndListParam encounterReference;
	
	private TokenAndListParam code;
	
	private ReferenceAndListParam participantReference;
	
	private ReferenceAndListParam medicationReference;
	
	private TokenAndListParam status;
	
	private TokenAndListParam fulfillerStatus;
	
	@Builder
	public MedicationRequestSearchParams(ReferenceAndListParam patientReference, ReferenceAndListParam encounterReference,
	    TokenAndListParam code, ReferenceAndListParam participantReference, ReferenceAndListParam medicationReference,
	    TokenAndListParam status, TokenAndListParam fulfillerStatus, TokenAndListParam id, DateRangeParam lastUpdated,
	    HashSet<Include> includes, HashSet<Include> revIncludes) {
		
		super(id, lastUpdated, null, includes, revIncludes);
		
		this.patientReference = patientReference;
		this.encounterReference = encounterReference;
		this.code = code;
		this.participantReference = participantReference;
		this.medicationReference = medicationReference;
		this.status = status;
		this.fulfillerStatus = fulfillerStatus;
	}
	
	@Override
	public SearchParameterMap toSearchParameterMap() {
		return baseSearchParameterMap()
		        .addParameter(FhirConstants.ENCOUNTER_REFERENCE_SEARCH_HANDLER, getEncounterReference())
		        .addParameter(FhirConstants.PATIENT_REFERENCE_SEARCH_HANDLER, getPatientReference())
		        .addParameter(FhirConstants.CODED_SEARCH_HANDLER, getCode())
		        .addParameter(FhirConstants.PARTICIPANT_REFERENCE_SEARCH_HANDLER, getParticipantReference())
		        .addParameter(FhirConstants.MEDICATION_REFERENCE_SEARCH_HANDLER, getMedicationReference())
		        .addParameter(FhirConstants.STATUS_SEARCH_HANDLER, getStatus())
		        .addParameter(FhirConstants.FULFILLER_STATUS_SEARCH_HANDLER, getFulfillerStatus());
	}
}
