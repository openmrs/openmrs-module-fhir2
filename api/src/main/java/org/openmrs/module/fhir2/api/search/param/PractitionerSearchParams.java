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
public class PractitionerSearchParams extends BaseResourceSearchParams {
	
	private TokenAndListParam identifier;
	
	private StringAndListParam name;
	
	private StringAndListParam given;
	
	private StringAndListParam family;
	
	private StringAndListParam city;
	
	private StringAndListParam state;
	
	private StringAndListParam postalCode;
	
	private StringAndListParam country;
	
	@Builder
	public PractitionerSearchParams(TokenAndListParam identifier, StringAndListParam name, StringAndListParam given,
	    StringAndListParam family, StringAndListParam city, StringAndListParam state, StringAndListParam postalCode,
	    StringAndListParam country, TokenAndListParam id, DateRangeParam lastUpdated, HashSet<Include> revIncludes) {
		
		super(id, lastUpdated, null, null, revIncludes);
		
		this.identifier = identifier;
		this.name = name;
		this.given = given;
		this.family = family;
		this.city = city;
		this.state = state;
		this.postalCode = postalCode;
		this.country = country;
	}
	
	@Override
	public SearchParameterMap toSearchParameterMap() {
		return baseSearchParameterMap().addParameter(FhirConstants.IDENTIFIER_SEARCH_HANDLER, getIdentifier())
		        .addParameter(FhirConstants.NAME_SEARCH_HANDLER, FhirConstants.NAME_PROPERTY, getName())
		        .addParameter(FhirConstants.NAME_SEARCH_HANDLER, FhirConstants.GIVEN_PROPERTY, getGiven())
		        .addParameter(FhirConstants.NAME_SEARCH_HANDLER, FhirConstants.FAMILY_PROPERTY, getFamily())
		        .addParameter(FhirConstants.ADDRESS_SEARCH_HANDLER, FhirConstants.CITY_PROPERTY, getCity())
		        .addParameter(FhirConstants.ADDRESS_SEARCH_HANDLER, FhirConstants.STATE_PROPERTY, getState())
		        .addParameter(FhirConstants.ADDRESS_SEARCH_HANDLER, FhirConstants.POSTAL_CODE_PROPERTY, getPostalCode())
		        .addParameter(FhirConstants.ADDRESS_SEARCH_HANDLER, FhirConstants.COUNTRY_PROPERTY, getCountry());
	}
}
