/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.fhir2.api.search;

import java.util.List;
import java.util.Set;

import org.hl7.fhir.instance.model.api.IBaseResource;
import org.openmrs.module.fhir2.api.search.param.SearchParameterMap;

public interface SearchQueryInclude<U extends IBaseResource> {
	
	/**
	 * Fetches any resources meant to be included or revIncluded in the resulting query
	 *
	 * @param resourceList A list of resources that are the result of the FHIR Search query being run
	 * @param theParams The {@link SearchParameterMap} for the FHIR Search query being run
	 * @return A {@link Set} of resources to be included
	 */
	Set<IBaseResource> getIncludedResources(List<U> resourceList, SearchParameterMap theParams);
}
