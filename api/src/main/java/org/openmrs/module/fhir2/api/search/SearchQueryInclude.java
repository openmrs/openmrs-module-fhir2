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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import ca.uhn.fhir.model.api.Include;
import lombok.NoArgsConstructor;
import org.apache.commons.collections.CollectionUtils;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.AllergyIntolerance;
import org.hl7.fhir.r4.model.Location;
import org.hl7.fhir.r4.model.Observation;
import org.hl7.fhir.r4.model.Reference;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.api.FhirEncounterService;
import org.openmrs.module.fhir2.api.FhirLocationService;
import org.openmrs.module.fhir2.api.FhirObservationService;
import org.openmrs.module.fhir2.api.FhirPatientService;
import org.openmrs.module.fhir2.api.search.param.PropParam;
import org.openmrs.module.fhir2.api.search.param.SearchParameterMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@NoArgsConstructor
public class SearchQueryInclude<U extends IBaseResource> {
	
	@Autowired
	private FhirLocationService locationService;
	
	@Autowired
	private FhirObservationService observationService;
	
	@Autowired
	private FhirEncounterService encounterService;
	
	@Autowired
	private FhirPatientService patientService;
	
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
				case FhirConstants.INCLUDE_ENCOUNTER_PARAM:
					includedResourcesSet.addAll(handleEncounterInclude(resourceList, includeParam.getParamType()));
					break;
				case FhirConstants.INCLUDE_PATIENT_PARAM:
					includedResourcesSet.addAll(handlePatientInclude(resourceList, includeParam.getParamType()));
					break;
				case FhirConstants.INCLUDE_HAS_MEMBER_PARAM:
				case FhirConstants.INCLUDE_RELATED_TYPE_PARAM:
					includedResourcesSet.addAll(handleObsGroupInclude(resourceList, includeParam.getParamType()));
					break;
			}
		});
		
		return includedResourcesSet;
	}
	
	private List<Location> handleParentLocationInclude(List<Location> resourceList) {
		Set<String> uniqueParentLocationUUIDs = resourceList.stream().map(Location::getPartOf)
		        .map(SearchQueryInclude::getIdFromReference).filter(Objects::nonNull).collect(Collectors.toSet());
		
		return locationService.get(uniqueParentLocationUUIDs);
	}
	
	private Set<IBaseResource> handleObsGroupInclude(List<U> resourceList, String paramType) {
		Set<IBaseResource> includedResources = new HashSet<>();
		Set<String> uniqueObservationUUIDs = new HashSet<>();
		
		switch (paramType) {
			case FhirConstants.OBSERVATION:
				resourceList.forEach(resource -> uniqueObservationUUIDs
				        .addAll(getIdsFromReferenceList(((Observation) resource).getHasMember())));
				break;
		}
		
		uniqueObservationUUIDs.removeIf(Objects::isNull);
		includedResources.addAll(observationService.get(uniqueObservationUUIDs));
		
		return includedResources;
	}
	
	private Set<IBaseResource> handlePatientInclude(List<U> resourceList, String paramType) {
		Set<IBaseResource> includedResources = new HashSet<>();
		Set<String> uniquePatientUUIDs = new HashSet<>();
		
		switch (paramType) {
			case FhirConstants.OBSERVATION:
				resourceList.forEach(
				    resource -> uniquePatientUUIDs.add(getIdFromReference(((Observation) resource).getSubject())));
				break;
			case FhirConstants.ALLERGY_INTOLERANCE:
				resourceList.forEach(
				    resource -> uniquePatientUUIDs.add(getIdFromReference(((AllergyIntolerance) resource).getPatient())));
				break;
		}
		
		uniquePatientUUIDs.removeIf(Objects::isNull);
		includedResources.addAll(patientService.get(uniquePatientUUIDs));
		
		return includedResources;
	}
	
	private Set<IBaseResource> handleEncounterInclude(List<U> resourceList, String paramType) {
		Set<IBaseResource> includedResources = new HashSet<>();
		Set<String> uniqueEncounterUUIDs = new HashSet<>();
		
		switch (paramType) {
			case FhirConstants.OBSERVATION:
				resourceList.forEach(
				    resource -> uniqueEncounterUUIDs.add(getIdFromReference(((Observation) resource).getEncounter())));
				break;
		}
		
		uniqueEncounterUUIDs.removeIf(Objects::isNull);
		includedResources.addAll(encounterService.get(uniqueEncounterUUIDs));
		
		return includedResources;
	}
	
	private static List<String> getIdsFromReferenceList(List<Reference> referenceList) {
		List<String> idList = new ArrayList<>();
		
		if (referenceList != null) {
			referenceList.forEach(reference -> idList.add(getIdFromReference(reference)));
		}
		
		return idList;
	}
	
	protected static String getIdFromReference(Reference reference) {
		return reference != null ? reference.getReferenceElement().getIdPart() : null;
	}
}
