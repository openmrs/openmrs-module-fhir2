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
import ca.uhn.fhir.rest.param.StringAndListParam;
import ca.uhn.fhir.rest.param.TokenAndListParam;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.openmrs.module.fhir2.FhirConstants;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class OpenmrsPatientSearchParams extends BaseResourceSearchParams {
	
	private StringAndListParam query;
	
	private TokenAndListParam gender;
	
	private DateRangeParam birthDate;
	
	private DateRangeParam deathDate;
	
	private TokenAndListParam deceased;
	
	private StringAndListParam city;
	
	private StringAndListParam state;
	
	private StringAndListParam postalCode;
	
	private StringAndListParam country;
	
	@Builder
	public OpenmrsPatientSearchParams(StringAndListParam query, TokenAndListParam gender, DateRangeParam birthDate,
	    DateRangeParam deathDate, TokenAndListParam deceased, StringAndListParam city, StringAndListParam state,
	    StringAndListParam postalCode, StringAndListParam country, TokenAndListParam id, DateRangeParam lastUpdated,
	    SortSpec sort, HashSet<Include> revIncludes) {
		
		super(id, lastUpdated, sort, null, revIncludes);
		
		this.query = query;
		this.gender = gender;
		this.birthDate = birthDate;
		this.deathDate = deathDate;
		this.deceased = deceased;
		this.city = city;
		this.state = state;
		this.postalCode = postalCode;
		this.country = country;
	}
	
	@Override
	public SearchParameterMap toSearchParameterMap() {
		return baseSearchParameterMap().addParameter(FhirConstants.QUERY_SEARCH_HANDLER, "query", getQuery())
		        .addParameter(FhirConstants.GENDER_SEARCH_HANDLER, "gender", getGender())
		        .addParameter(FhirConstants.DATE_RANGE_SEARCH_HANDLER, "birthdate", getBirthDate())
		        .addParameter(FhirConstants.DATE_RANGE_SEARCH_HANDLER, "deathDate", getDeathDate())
		        .addParameter(FhirConstants.BOOLEAN_SEARCH_HANDLER, getDeceased())
		        .addParameter(FhirConstants.ADDRESS_SEARCH_HANDLER, FhirConstants.CITY_PROPERTY, getCity())
		        .addParameter(FhirConstants.ADDRESS_SEARCH_HANDLER, FhirConstants.STATE_PROPERTY, getState())
		        .addParameter(FhirConstants.ADDRESS_SEARCH_HANDLER, FhirConstants.POSTAL_CODE_PROPERTY, getPostalCode())
		        .addParameter(FhirConstants.ADDRESS_SEARCH_HANDLER, FhirConstants.COUNTRY_PROPERTY, getCountry());
	}
}
