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
import ca.uhn.fhir.rest.param.*;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.openmrs.module.fhir2.FhirConstants;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ObservationSearchParams extends BaseResourceSearchParams {
	
	private ReferenceAndListParam encounter;
	
	private ReferenceAndListParam patient;
	
	private ReferenceAndListParam hasMember;
	
	private TokenAndListParam valueConcept;
	
	private DateRangeParam valueDate;
	
	private QuantityAndListParam valueQuantity;
	
	private StringAndListParam valueString;
	
	private DateRangeParam date;
	
	private TokenAndListParam code;
	
	private TokenAndListParam category;
	
	/**
	 * Custom AllArgsConstructor (instead of using @AllArgsConstructor annotation) that supports super
	 * class instantiation
	 */
	@Builder
	public ObservationSearchParams(ReferenceAndListParam encounterReference, ReferenceAndListParam patientReference,
	    ReferenceAndListParam hasMemberReference, TokenAndListParam valueConcept, DateRangeParam valueDateParam,
	    QuantityAndListParam valueQuantityParam, StringAndListParam valueStringParam, DateRangeParam date,
	    TokenAndListParam code, TokenAndListParam category, TokenAndListParam id, DateRangeParam lastUpdated, SortSpec sort,
	    HashSet<Include> includes, HashSet<Include> revIncludes) {
		
		super(id, lastUpdated, sort, includes, revIncludes);
		
		this.encounter = encounterReference;
		this.patient = patientReference;
		this.hasMember = hasMemberReference;
		this.valueConcept = valueConcept;
		this.valueDate = valueDateParam;
		this.valueQuantity = valueQuantityParam;
		this.valueString = valueStringParam;
		this.date = date;
		this.code = code;
		this.category = category;
	}
	
	@Override
	public SearchParameterMap toSearchParameterMap() {
		return baseSearchParameterMap().addParameter(FhirConstants.ENCOUNTER_REFERENCE_SEARCH_HANDLER, getEncounter())
		        .addParameter(FhirConstants.PATIENT_REFERENCE_SEARCH_HANDLER, getPatient())
		        .addParameter(FhirConstants.CODED_SEARCH_HANDLER, getCode())
		        .addParameter(FhirConstants.CATEGORY_SEARCH_HANDLER, getCategory())
		        .addParameter(FhirConstants.VALUE_CODED_SEARCH_HANDLER, getValueConcept())
		        .addParameter(FhirConstants.HAS_MEMBER_SEARCH_HANDLER, getHasMember())
		        .addParameter(FhirConstants.VALUE_STRING_SEARCH_HANDLER, "valueText", getValueString())
		        .addParameter(FhirConstants.QUANTITY_SEARCH_HANDLER, "valueNumeric", getValueQuantity())
		        .addParameter(FhirConstants.DATE_RANGE_SEARCH_HANDLER, "obsDatetime", getDate())
		        .addParameter(FhirConstants.DATE_RANGE_SEARCH_HANDLER, "valueDatetime", getValueDate());
	}
}
