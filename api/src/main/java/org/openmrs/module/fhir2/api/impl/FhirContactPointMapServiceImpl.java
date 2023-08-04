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
import lombok.Setter;
import org.openmrs.PersonAttributeType;
import org.openmrs.attribute.BaseAttributeType;
import org.openmrs.module.fhir2.api.FhirContactPointMapService;
import org.openmrs.module.fhir2.api.dao.FhirContactPointMapDao;
import org.openmrs.module.fhir2.model.FhirContactPointMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional
@Setter(AccessLevel.PACKAGE)
public class FhirContactPointMapServiceImpl implements FhirContactPointMapService {
	
	@Autowired
	private FhirContactPointMapDao dao;
	
	@Transactional(readOnly = true)
	@Override
	public Optional<FhirContactPointMap> getFhirContactPointMapForPersonAttributeType(PersonAttributeType attributeType) {
		return dao.getFhirContactPointMapForPersonAttributeType(attributeType);
	}
	
	@Transactional(readOnly = true)
	@Override
	public Optional<FhirContactPointMap> getFhirContactPointMapForAttributeType(BaseAttributeType<?> attributeType) {
		return dao.getFhirContactPointMapForAttributeType(attributeType);
	}
	
	@Transactional
	@Override
	public FhirContactPointMap saveFhirContactPointMap(FhirContactPointMap contactPointMap) {
		return dao.saveFhirContactPointMap(contactPointMap);
	}
	
}
