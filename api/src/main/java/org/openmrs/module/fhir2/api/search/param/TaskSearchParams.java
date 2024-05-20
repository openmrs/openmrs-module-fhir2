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
public class TaskSearchParams extends BaseResourceSearchParams {
	
	/**
	 * Get collection of tasks corresponding to the provided search parameters
	 *
	 * @param basedOnReference A reference list to basedOn resources
	 * @param ownerReference A reference list to owner resources
	 * @param status A list of statuses
	 * @param id The UUID of the requested task
	 * @param lastUpdated A date range corresponding to when the Tasks were last updated
	 * @param sort The sort parameters for the search results
	 * @param includes request for specified referenced resources
	 * @return the collection of Tasks that match the search parameters
	 */
	
	private ReferenceAndListParam basedOnReference;
	
	private ReferenceAndListParam ownerReference;
	
	private ReferenceAndListParam forReference;
	
	private TokenAndListParam taskCode;
	
	private TokenAndListParam status;
	
	@Builder
	public TaskSearchParams(ReferenceAndListParam basedOnReference, ReferenceAndListParam ownerReference,
	    ReferenceAndListParam forReference, TokenAndListParam taskCode, TokenAndListParam status, TokenAndListParam id,
	    DateRangeParam lastUpdated, SortSpec sort, HashSet<Include> includes) {
		
		super(id, lastUpdated, sort, includes, null);
		
		this.basedOnReference = basedOnReference;
		this.ownerReference = ownerReference;
		this.forReference = forReference;
		this.taskCode = taskCode;
		this.status = status;
		
	}
	
	@Override
	public SearchParameterMap toSearchParameterMap() {
		return baseSearchParameterMap().addParameter(FhirConstants.BASED_ON_REFERENCE_SEARCH_HANDLER, getBasedOnReference())
		        .addParameter(FhirConstants.OWNER_REFERENCE_SEARCH_HANDLER, getOwnerReference())
		        .addParameter(FhirConstants.FOR_REFERENCE_SEARCH_HANDLER, getForReference())
		        .addParameter(FhirConstants.TASK_CODE_SEARCH_HANDLER, getTaskCode())
		        .addParameter(FhirConstants.STATUS_SEARCH_HANDLER, getStatus());
	}
}
