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
public class DiagnosticReportSearchParams extends BaseResourceSearchParams {
	
	private ReferenceAndListParam encounterReference;
	
	private ReferenceAndListParam patientReference;
	
	private DateRangeParam issueDate;
	
	private TokenAndListParam code;
	
	private ReferenceAndListParam result;
	
	@Builder
	public DiagnosticReportSearchParams(ReferenceAndListParam encounterReference, ReferenceAndListParam patientReference,
	    DateRangeParam issueDate, TokenAndListParam code, ReferenceAndListParam result, TokenAndListParam id,
	    DateRangeParam lastUpdated, SortSpec sort, HashSet<Include> includes) {
		
		super(id, lastUpdated, sort, includes, null);
		
		this.encounterReference = encounterReference;
		this.patientReference = patientReference;
		this.issueDate = issueDate;
		this.code = code;
		this.result = result;
	}
	
	@Override
	public SearchParameterMap toSearchParameterMap() {
		return baseSearchParameterMap()
		        .addParameter(FhirConstants.ENCOUNTER_REFERENCE_SEARCH_HANDLER, getEncounterReference())
		        .addParameter(FhirConstants.PATIENT_REFERENCE_SEARCH_HANDLER, getPatientReference())
		        .addParameter(FhirConstants.DATE_RANGE_SEARCH_HANDLER, getIssueDate())
		        .addParameter(FhirConstants.CODED_SEARCH_HANDLER, getCode())
		        .addParameter(FhirConstants.RESULT_SEARCH_HANDLER, getResult());
	}
}
