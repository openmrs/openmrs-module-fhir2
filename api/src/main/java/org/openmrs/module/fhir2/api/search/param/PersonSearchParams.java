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
public class PersonSearchParams extends BaseResourceSearchParams {
	
	private StringAndListParam name;
	
	private TokenAndListParam gender;
	
	private DateRangeParam birthDate;
	
	private StringAndListParam city;
	
	private StringAndListParam state;
	
	private StringAndListParam postalCode;
	
	private StringAndListParam country;
	
	@Builder
	public PersonSearchParams(StringAndListParam name, TokenAndListParam gender, DateRangeParam birthDate,
	    StringAndListParam city, StringAndListParam state, StringAndListParam postalCode, StringAndListParam country,
	    TokenAndListParam id, DateRangeParam lastUpdated, SortSpec sort, HashSet<Include> includes) {
		
		super(id, lastUpdated, sort, includes, null);
		
		this.name = name;
		this.gender = gender;
		this.birthDate = birthDate;
		this.city = city;
		this.state = state;
		this.postalCode = postalCode;
		this.country = country;
	}
	
	@Override
	public SearchParameterMap toSearchParameterMap() {
		return baseSearchParameterMap()
		        .addParameter(FhirConstants.NAME_SEARCH_HANDLER, FhirConstants.NAME_PROPERTY, getName())
		        .addParameter(FhirConstants.GENDER_SEARCH_HANDLER, getGender())
		        .addParameter(FhirConstants.DATE_RANGE_SEARCH_HANDLER, getBirthDate())
		        .addParameter(FhirConstants.CITY_SEARCH_HANDLER, FhirConstants.CITY_PROPERTY, getCity())
		        .addParameter(FhirConstants.STATE_SEARCH_HANDLER, FhirConstants.STATE_PROPERTY, getState())
		        .addParameter(FhirConstants.POSTALCODE_SEARCH_HANDLER, FhirConstants.POSTAL_CODE_PROPERTY, getPostalCode())
		        .addParameter(FhirConstants.COUNTRY_SEARCH_HANDLER, FhirConstants.COUNTRY_PROPERTY, getCountry());
	}
	
}
