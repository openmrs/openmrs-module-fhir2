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
public class LocationSearchParams extends BaseResourceSearchParams {
	
	private StringAndListParam name;
	
	private StringAndListParam city;
	
	private StringAndListParam country;
	
	private StringAndListParam postalCode;
	
	private StringAndListParam state;
	
	private TokenAndListParam tag;
	
	private ReferenceAndListParam parent;
	
	private ReferenceAndListParam ancestor;
	
	@Builder
	public LocationSearchParams(StringAndListParam name, StringAndListParam city, StringAndListParam country,
	    StringAndListParam postalCode, StringAndListParam state, TokenAndListParam tag, ReferenceAndListParam parent,
	    ReferenceAndListParam ancestor, TokenAndListParam id, DateRangeParam lastUpdated, SortSpec sort,
	    HashSet<Include> includes, HashSet<Include> revIncludes) {
		
		super(id, lastUpdated, sort, includes, revIncludes);
		
		this.name = name;
		this.city = city;
		this.country = country;
		this.postalCode = postalCode;
		this.state = state;
		this.tag = tag;
		this.parent = parent;
		this.ancestor = ancestor;
	}
	
	@Override
	public SearchParameterMap toSearchParameterMap() {
		return baseSearchParameterMap().addParameter(FhirConstants.NAME_SEARCH_HANDLER, getName())
		        .addParameter(FhirConstants.CITY_SEARCH_HANDLER, getCity())
		        .addParameter(FhirConstants.COUNTRY_SEARCH_HANDLER, getCountry())
		        .addParameter(FhirConstants.POSTALCODE_SEARCH_HANDLER, getPostalCode())
		        .addParameter(FhirConstants.STATE_SEARCH_HANDLER, getState())
		        .addParameter(FhirConstants.TAG_SEARCH_HANDLER, getTag())
		        .addParameter(FhirConstants.LOCATION_REFERENCE_SEARCH_HANDLER, getParent())
		        .addParameter(FhirConstants.LOCATION_ANCESTOR_SEARCH_HANDLER, getAncestor());
	}
	
}
