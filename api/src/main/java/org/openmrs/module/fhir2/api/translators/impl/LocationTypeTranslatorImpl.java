/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.fhir2.api.translators.impl;

import javax.annotation.Nonnull;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import lombok.AccessLevel;
import lombok.Setter;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.openmrs.Concept;
import org.openmrs.Location;
import org.openmrs.LocationAttribute;
import org.openmrs.LocationAttributeType;
import org.openmrs.api.ConceptService;
import org.openmrs.api.LocationService;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.api.FhirGlobalPropertyService;
import org.openmrs.module.fhir2.api.translators.ConceptTranslator;
import org.openmrs.module.fhir2.api.translators.LocationTypeTranslator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Setter(AccessLevel.PACKAGE)
public class LocationTypeTranslatorImpl implements LocationTypeTranslator {
	
	@Autowired
	private LocationService locationService;
	
	@Autowired
	private ConceptService conceptService;
	
	@Autowired
	private ConceptTranslator conceptTranslator;
	
	@Autowired
	private FhirGlobalPropertyService globalPropertyService;
	
	@Override
	public List<CodeableConcept> toFhirResource(@Nonnull Location location) {
		LocationAttributeType typeAttributeType = locationService.getLocationAttributeTypeByUuid(
		    globalPropertyService.getGlobalProperty(FhirConstants.LOCATION_TYPE_ATTRIBUTE_TYPE));
		
		Optional<LocationAttribute> existingAttributeQuery = location.getAttributes().stream()
		        .filter(a -> a.getAttributeType() == typeAttributeType).findAny();
		
		if (existingAttributeQuery.isPresent()) {
			LocationAttribute typeAttribute = existingAttributeQuery.get();
			
			CodeableConcept type = conceptTranslator
			        .toFhirResource(conceptService.getConceptByUuid(typeAttribute.getValue().toString()));
			
			return Collections.singletonList(type);
		}
		
		return Collections.emptyList();
	}
	
	@Override
	public Location toOpenmrsType(@Nonnull Location location, @Nonnull List<CodeableConcept> types) {
		LocationAttributeType typeAttributeType = locationService.getLocationAttributeTypeByUuid(
		    globalPropertyService.getGlobalProperty(FhirConstants.LOCATION_TYPE_ATTRIBUTE_TYPE));
		String locationTypeSystem = globalPropertyService.getGlobalProperty(FhirConstants.LOCATION_TYPE_SYSTEM_URL);
		
		Concept typeConcept = null;
		
		// Only save the type from this code system as per mCSD IG: https://terminology.hl7.org/2.1.0/CodeSystem-v3-RoleCode.html
		Optional<CodeableConcept> type = types.stream()
		        .filter(t -> t.hasCoding() && t.getCodingFirstRep().getSystem().equals(locationTypeSystem)).findFirst();
		
		if (type.isPresent()) {
			typeConcept = conceptTranslator.toOpenmrsType(type.get());
		}
		
		if (typeConcept != null) {
			LocationAttribute typeAttribute = new LocationAttribute();
			typeAttribute.setValue(typeConcept.getUuid());
			typeAttribute.setAttributeType(typeAttributeType);
			
			location.addAttribute(typeAttribute);
		}
		
		return location;
	}
}
