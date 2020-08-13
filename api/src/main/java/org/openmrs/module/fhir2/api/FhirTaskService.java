/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.fhir2.api;

import ca.uhn.fhir.rest.api.SortSpec;
import ca.uhn.fhir.rest.api.server.IBundleProvider;
import ca.uhn.fhir.rest.param.DateRangeParam;
import ca.uhn.fhir.rest.param.ReferenceAndListParam;
import ca.uhn.fhir.rest.param.TokenAndListParam;
import org.hl7.fhir.r4.model.Task;

/**
 * Contains methods pertaining to creating/updating/voiding/searching Tasks
 */
public interface FhirTaskService extends FhirService<Task> {
	
	/**
	 * Get collection of tasks corresponding to the provided search parameters
	 *
	 * @param basedOnReference A reference list to basedOn resources
	 * @param ownerReference A reference list to owner resources
	 * @param status A list of statuses
	 * @param id The UUID of the requested task
	 * @param lastUpdated A date range corresponding to when the Tasks were last updated
	 * @param sort The sort parameters for the search results
	 * @return the collection of Tasks that match the search parameters
	 */
	IBundleProvider searchForTasks(ReferenceAndListParam basedOnReference, ReferenceAndListParam ownerReference,
	        TokenAndListParam status, TokenAndListParam id, DateRangeParam lastUpdated, SortSpec sort);
}
