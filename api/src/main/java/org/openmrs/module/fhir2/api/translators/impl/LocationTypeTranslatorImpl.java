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

import static lombok.AccessLevel.PROTECTED;

import javax.annotation.Nonnull;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import lombok.Getter;
import lombok.Setter;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.openmrs.Concept;
import org.openmrs.Location;
import org.openmrs.LocationAttribute;
import org.openmrs.LocationAttributeType;
import org.openmrs.annotation.OpenmrsProfile;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.api.FhirGlobalPropertyService;
import org.openmrs.module.fhir2.api.dao.FhirConceptDao;
import org.openmrs.module.fhir2.api.dao.FhirLocationDao;
import org.openmrs.module.fhir2.api.translators.ConceptTranslator;
import org.openmrs.module.fhir2.api.translators.LocationTypeTranslator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@OpenmrsProfile(openmrsPlatformVersion = "2.* - 2.4.*")
public class LocationTypeTranslatorImpl implements LocationTypeTranslator {
	
	@Getter(PROTECTED)
	@Setter(value = PROTECTED, onMethod_ = @Autowired)
	private FhirConceptDao conceptDao;
	
	@Getter(PROTECTED)
	@Setter(value = PROTECTED, onMethod_ = @Autowired)
	private FhirLocationDao locationDao;
	
	@Getter(PROTECTED)
	@Setter(value = PROTECTED, onMethod_ = @Autowired)
	private ConceptTranslator conceptTranslator;
	
	@Getter(PROTECTED)
	@Setter(value = PROTECTED, onMethod_ = @Autowired)
	private FhirGlobalPropertyService globalPropertyService;
	
	@Override
	public List<CodeableConcept> toFhirResource(@Nonnull Location location) {
		String attributeTypeUuid = globalPropertyService.getGlobalProperty(FhirConstants.LOCATION_TYPE_ATTRIBUTE_TYPE);
		
		if (!(attributeTypeUuid == null || attributeTypeUuid.isEmpty())) {
			LocationAttributeType typeAttributeType = locationDao.getLocationAttributeTypeByUuid(attributeTypeUuid);
			Optional<LocationAttribute> existingAttributeQuery;
			
			if (typeAttributeType != null) {
				existingAttributeQuery = location.getAttributes().stream()
				        .filter(a -> a.getAttributeType() == typeAttributeType).findFirst();
				
				if (existingAttributeQuery.isPresent()) {
					LocationAttribute typeAttribute = existingAttributeQuery.get();
					
					CodeableConcept type = conceptTranslator
					        .toFhirResource(conceptDao.get(typeAttribute.getValue().toString()));
					
					if (type != null) {
						return Collections.singletonList(type);
					} else {
						return Collections.emptyList();
					}
				}
			}
		}
		
		return Collections.emptyList();
	}
	
	@Override
	public Location toOpenmrsType(@Nonnull Location location, @Nonnull List<CodeableConcept> types) {
		LocationAttributeType typeAttributeType = null;
		
		String locationAttributeType = globalPropertyService.getGlobalProperty(FhirConstants.LOCATION_TYPE_ATTRIBUTE_TYPE);
		if (locationAttributeType != null && !locationAttributeType.isEmpty()) {
			typeAttributeType = locationDao.getLocationAttributeTypeByUuid(locationAttributeType);
		}
		
		if (typeAttributeType != null) {
			Optional<Concept> typeConcept = types.stream().filter(Objects::nonNull).filter(CodeableConcept::hasCoding)
			        .map(conceptTranslator::toOpenmrsType).findFirst();
			
			if (typeConcept.isPresent()) {
				Optional<LocationAttribute> typeAttributeQuery = locationDao
				        .getActiveAttributesByLocationAndAttributeTypeUuid(location, typeAttributeType.getUuid()).stream()
				        .findFirst();
				LocationAttribute typeAttribute;
				
				if (typeAttributeQuery.isPresent()) {
					typeAttribute = typeAttributeQuery.get();
				} else {
					typeAttribute = new LocationAttribute();
					typeAttribute.setAttributeType(typeAttributeType);
					location.addAttribute(typeAttribute);
				}
				typeAttribute.setValue(typeConcept.get().getUuid());
			}
		}
		
		return location;
	}
}
