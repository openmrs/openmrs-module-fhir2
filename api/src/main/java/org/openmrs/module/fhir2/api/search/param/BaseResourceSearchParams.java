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

import java.io.Serializable;
import java.util.HashSet;

import ca.uhn.fhir.model.api.Include;
import ca.uhn.fhir.rest.api.SortSpec;
import ca.uhn.fhir.rest.param.DateRangeParam;
import ca.uhn.fhir.rest.param.TokenAndListParam;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.openmrs.module.fhir2.FhirConstants;

/**
 * Class containing shared search (and search result) parameters that apply to all resources See
 * https://www.hl7.org/fhir/search.html#Summary for a comprehensive description of these parameters
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public abstract class BaseResourceSearchParams implements Serializable {
	
	protected TokenAndListParam id;
	
	protected DateRangeParam lastUpdated;
	
	protected SortSpec sort;
	
	protected HashSet<Include> includes;
	
	protected HashSet<Include> revIncludes;
	
	protected final SearchParameterMap baseSearchParameterMap() {
		return new SearchParameterMap().addParameter(FhirConstants.COMMON_SEARCH_HANDLER, FhirConstants.ID_PROPERTY, getId())
		        .addParameter(FhirConstants.COMMON_SEARCH_HANDLER, FhirConstants.LAST_UPDATED_PROPERTY, getLastUpdated())
		        .addParameter(FhirConstants.INCLUDE_SEARCH_HANDLER, getIncludes())
		        .addParameter(FhirConstants.REVERSE_INCLUDE_SEARCH_HANDLER, getRevIncludes()).setSortSpec(getSort());
	}
	
	public abstract SearchParameterMap toSearchParameterMap();
}
