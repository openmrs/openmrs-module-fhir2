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
import ca.uhn.fhir.rest.param.QuantityAndListParam;
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
public class DiagnosisSearchParams extends BaseResourceSearchParams {
	
	private ReferenceAndListParam patientParam;
	
	private TokenAndListParam code;
	
	private TokenAndListParam clinicalStatus;
	
	private DateRangeParam onsetDate;
	
	private QuantityAndListParam onsetAge;
	
	private DateRangeParam recordedDate;
	
	private TokenAndListParam category;
	
	@Builder
	public DiagnosisSearchParams(ReferenceAndListParam patientParam, TokenAndListParam code,
	    TokenAndListParam clinicalStatus, DateRangeParam onsetDate, QuantityAndListParam onsetAge,
	    DateRangeParam recordedDate, TokenAndListParam category, TokenAndListParam id, DateRangeParam lastUpdated,
	    SortSpec sort, HashSet<Include> includes, HashSet<Include> revIncludes) {
		
		super(id, lastUpdated, sort, includes, revIncludes);
		
		this.patientParam = patientParam;
		this.code = code;
		this.clinicalStatus = clinicalStatus;
		this.onsetDate = onsetDate;
		this.onsetAge = onsetAge;
		this.recordedDate = recordedDate;
		this.category = category;
	}
	
	@Override
	public SearchParameterMap toSearchParameterMap() {
		return baseSearchParameterMap().addParameter(FhirConstants.PATIENT_REFERENCE_SEARCH_HANDLER, getPatientParam())
		        .addParameter(FhirConstants.CODED_SEARCH_HANDLER, getCode())
		        .addParameter(FhirConstants.CONDITION_CLINICAL_STATUS_HANDLER, getClinicalStatus())
		        .addParameter(FhirConstants.DATE_RANGE_SEARCH_HANDLER, "onsetDate", getOnsetDate())
		        .addParameter(FhirConstants.QUANTITY_SEARCH_HANDLER, getOnsetAge())
		        .addParameter(FhirConstants.DATE_RANGE_SEARCH_HANDLER, "dateCreated", getRecordedDate());
	}
}
