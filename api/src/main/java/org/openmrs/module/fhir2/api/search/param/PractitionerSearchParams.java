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
import ca.uhn.fhir.rest.param.StringOrListParam;
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
	
	private StringOrListParam providerRole;
	
	private TokenAndListParam tag;
	
	@Builder
	public PractitionerSearchParams(TokenAndListParam identifier, StringAndListParam name, StringAndListParam given,
	    StringAndListParam family, StringAndListParam city, StringAndListParam state, StringAndListParam postalCode,
	    StringAndListParam country, TokenAndListParam id, StringOrListParam providerRole, TokenAndListParam tag,
	    DateRangeParam lastUpdated, HashSet<Include> revIncludes) {
		
		super(id, lastUpdated, null, null, revIncludes);
		
		this.identifier = identifier;
		this.name = name;
		this.given = given;
		this.family = family;
		this.city = city;
		this.state = state;
		this.postalCode = postalCode;
		this.country = country;
		this.providerRole = providerRole;
		this.tag = tag;
	}
	
	@Override
	public SearchParameterMap toSearchParameterMap() {
		return baseSearchParameterMap().addParameter(FhirConstants.IDENTIFIER_SEARCH_HANDLER, getIdentifier())
		        .addParameter(FhirConstants.NAME_SEARCH_HANDLER, getName())
		        .addParameter(FhirConstants.NAME_SEARCH_HANDLER, getGiven())
		        .addParameter(FhirConstants.NAME_SEARCH_HANDLER, getFamily())
		        .addParameter(FhirConstants.CITY_SEARCH_HANDLER, getCity())
		        .addParameter(FhirConstants.STATE_SEARCH_HANDLER, getState())
		        .addParameter(FhirConstants.POSTALCODE_SEARCH_HANDLER, getPostalCode())
		        .addParameter(FhirConstants.COUNTRY_SEARCH_HANDLER, getCountry())
		        .addParameter(FhirConstants.PROVIDER_ROLE_SEARCH_HANDLER, getProviderRole())
		        .addParameter(FhirConstants.TAG_SEARCH_HANDLER, getTag());
	}
}
