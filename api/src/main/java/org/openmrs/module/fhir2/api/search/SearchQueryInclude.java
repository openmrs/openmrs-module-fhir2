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

/**
 * Generic Interface for _include implementation
 *
 * @param <U> FHIR resource type
 */
public interface SearchQueryInclude<U extends IBaseResource> {
	
	/**
	 * Gets the updated resource with the included resource contained in it
	 *
	 * @param resourceList FHIR resources returned through search
	 * @param theParams search params.
	 * @return the set of unique included resources
	 */
	Set<IBaseResource> getIncludedResources(List<U> resourceList, SearchParameterMap theParams);
	
}
