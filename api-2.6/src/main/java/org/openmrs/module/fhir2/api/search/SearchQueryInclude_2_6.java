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

import ca.uhn.fhir.model.api.Include;
import lombok.NoArgsConstructor;
import org.apache.commons.collections.CollectionUtils;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.MedicationDispense;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.api.FhirPatientService;
import org.openmrs.module.fhir2.api.search.param.PropParam;
import org.openmrs.module.fhir2.api.search.param.SearchParameterMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@NoArgsConstructor
public class SearchQueryInclude_2_6 extends SearchQueryInclude<MedicationDispense> {
	
	@Autowired
	private FhirPatientService patientService;
	
	@Override
	@SuppressWarnings("unchecked")
	public Set<IBaseResource> getIncludedResources(List<MedicationDispense> resourceList, SearchParameterMap theParams) {
		Set<IBaseResource> includedResourcesSet = new HashSet<>();
		
		List<PropParam<?>> includeParamList = theParams.getParameters(FhirConstants.INCLUDE_SEARCH_HANDLER);
		
		if (CollectionUtils.isEmpty(includeParamList)) {
			return includedResourcesSet;
		}
		
		Set<Include> includeSet = (HashSet<Include>) includeParamList.get(0).getParam();
		includeSet.forEach(includeParam -> {
			switch (includeParam.getParamName()) {
				case FhirConstants.INCLUDE_PATIENT_PARAM:
					includedResourcesSet.addAll(handlePatientInclude(resourceList, includeParam.getParamType()));
					break;
			}
		});
		
		return includedResourcesSet;
	}
	
	private Set<IBaseResource> handlePatientInclude(List<MedicationDispense> resourceList, String paramType) {
		Set<IBaseResource> includedResources = new HashSet<>();
		Set<String> uniquePatientUUIDs = new HashSet<>();
		
		switch (paramType) {
			case FhirConstants.MEDICATION_DISPENSE:
				resourceList.forEach(resource -> uniquePatientUUIDs.add(getIdFromReference(resource.getSubject())));
				break;
		}
		
		uniquePatientUUIDs.removeIf(Objects::isNull);
		includedResources.addAll(patientService.get(uniquePatientUUIDs));
		
		return includedResources;
	}
	
}
