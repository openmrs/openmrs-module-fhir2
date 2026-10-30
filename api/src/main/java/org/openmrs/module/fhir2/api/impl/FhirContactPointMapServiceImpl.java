/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.fhir2.api.impl;

import java.util.Optional;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.openmrs.LocationAttributeType;
import org.openmrs.PersonAttributeType;
import org.openmrs.ProviderAttributeType;
import org.openmrs.attribute.BaseAttributeType;
import org.openmrs.module.fhir2.api.FhirContactPointMapService;
import org.openmrs.module.fhir2.api.dao.FhirContactPointMapDao;
import org.openmrs.module.fhir2.model.FhirContactPointMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class FhirContactPointMapServiceImpl implements FhirContactPointMapService {
	
	@Getter(value = AccessLevel.PROTECTED)
	@Setter(value = AccessLevel.PROTECTED, onMethod_ = @Autowired)
	private FhirContactPointMapDao dao;
	
	@Override
	public Optional<FhirContactPointMap> getFhirContactPointMapByUuid(String uuid) {
		return dao.getFhirContactPointMapByUuid(uuid);
	}
	
	@Override
	public Optional<FhirContactPointMap> getFhirContactPointMapForPersonAttributeType(PersonAttributeType attributeType) {
		return dao.getFhirContactPointMapForPersonAttributeType(attributeType);
	}
	
	@Override
	public Optional<FhirContactPointMap> getFhirContactPointMapForAttributeType(BaseAttributeType<?> attributeType) {
		// each domain has its own guarded lookup so the matching privilege is checked; other domains have no maps
		if (attributeType instanceof LocationAttributeType) {
			return dao.getFhirContactPointMapForLocationAttributeType((LocationAttributeType) attributeType);
		}
		
		if (attributeType instanceof ProviderAttributeType) {
			return dao.getFhirContactPointMapForProviderAttributeType((ProviderAttributeType) attributeType);
		}
		
		return Optional.empty();
	}
	
	@Override
	public FhirContactPointMap saveFhirContactPointMap(FhirContactPointMap contactPointMap) {
		// saving a map requires the same privilege as saving an attribute type of its domain
		String attributeTypeDomain = contactPointMap.getAttributeTypeDomain();
		if (attributeTypeDomain == null) {
			throw new IllegalArgumentException("A contact point map must specify its attribute type domain");
		}
		
		switch (attributeTypeDomain) {
			case "person":
				return dao.saveFhirContactPointMapForPersonAttributeType(contactPointMap);
			case "location":
				return dao.saveFhirContactPointMapForLocationAttributeType(contactPointMap);
			case "provider":
				return dao.saveFhirContactPointMapForProviderAttributeType(contactPointMap);
			default:
				throw new IllegalArgumentException("No privilege governs contact point maps for the '" + attributeTypeDomain
				        + "' attribute type domain");
		}
	}
	
}
