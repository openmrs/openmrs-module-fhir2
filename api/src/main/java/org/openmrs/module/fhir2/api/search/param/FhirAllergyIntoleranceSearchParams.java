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
public class FhirAllergyIntoleranceSearchParams extends BaseResourceSearchParams {
	
	private ReferenceAndListParam patientReference;
	
	private TokenAndListParam category;
	
	private TokenAndListParam allergen;
	
	private TokenAndListParam severity;
	
	private TokenAndListParam manifestationCode;
	
	private TokenAndListParam clinicalStatus;
	
	@Builder
	public FhirAllergyIntoleranceSearchParams(ReferenceAndListParam patientReference, TokenAndListParam category,
	    TokenAndListParam allergen, TokenAndListParam severity, TokenAndListParam manifestationCode,
	    TokenAndListParam clinicalStatus, TokenAndListParam id, DateRangeParam lastUpdated, SortSpec sort,
	    HashSet<Include> includes) {
		
		super(id, lastUpdated, sort, includes, null);
		
		this.patientReference = patientReference;
		this.category = category;
		this.allergen = allergen;
		this.severity = severity;
		this.manifestationCode = manifestationCode;
		this.clinicalStatus = clinicalStatus;
	}
	
	@Override
	public SearchParameterMap toSearchParameterMap() {
		return baseSearchParameterMap().addParameter(FhirConstants.PATIENT_REFERENCE_SEARCH_HANDLER, getPatientReference())
		        .addParameter(FhirConstants.CATEGORY_SEARCH_HANDLER, getCategory())
		        .addParameter(FhirConstants.ALLERGEN_SEARCH_HANDLER, getAllergen())
		        .addParameter(FhirConstants.SEVERITY_SEARCH_HANDLER, getSeverity())
		        .addParameter(FhirConstants.CODED_SEARCH_HANDLER, getManifestationCode())
		        .addParameter(FhirConstants.BOOLEAN_SEARCH_HANDLER, getClinicalStatus());
	}
}
