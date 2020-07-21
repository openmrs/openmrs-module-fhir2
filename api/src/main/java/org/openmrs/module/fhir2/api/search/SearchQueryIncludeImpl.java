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

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import ca.uhn.fhir.model.api.Include;
import lombok.NoArgsConstructor;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.Location;
import org.hl7.fhir.r4.model.Reference;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.api.FhirLocationService;
import org.openmrs.module.fhir2.api.search.param.PropParam;
import org.openmrs.module.fhir2.api.search.param.SearchParameterMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

@Component
@NoArgsConstructor
public class SearchQueryIncludeImpl<U extends IBaseResource> implements SearchQueryInclude<U> {
	
	@Autowired
	private FhirLocationService locationService;
	
	@Override
	@SuppressWarnings("unchecked")
	public Set<IBaseResource> getIncludedResources(List<U> resourceList, SearchParameterMap theParams) {
		Set<IBaseResource> includedResourcesSet = new HashSet<>();
		
		List<PropParam<?>> includeParamList = theParams.getParameters(FhirConstants.INCLUDE_SEARCH_HANDLER);
		
		if (CollectionUtils.isEmpty(includeParamList)) {
			return includedResourcesSet;
		}
		
		Set<Include> includeSet = (HashSet<Include>) includeParamList.get(0).getParam();
		includeSet.forEach(includeParam -> {
			switch (includeParam.getParamName()) {
				case FhirConstants.INCLUDE_PART_OF_PARAM:
					includedResourcesSet.addAll(handleParentLocationInclude((List<Location>) resourceList));
					break;
			}
		});
		
		return includedResourcesSet;
	}
	
	private List<Location> handleParentLocationInclude(List<Location> resourceList) {
		Set<String> uniqueParentLocationUUIDs = resourceList.stream().map(Location::getPartOf)
		        .map(SearchQueryIncludeImpl::getIdFromReference).filter(Objects::nonNull).collect(Collectors.toSet());
		
		return locationService.get(uniqueParentLocationUUIDs);
	}
	
	private static String getIdFromReference(Reference reference) {
		return reference != null ? reference.getReferenceElement().getIdPart() : null;
	}
	
}
