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

import java.util.Collection;

import lombok.AccessLevel;
import lombok.Setter;
import org.openmrs.module.fhir2.api.FhirContactPointService;
import org.openmrs.module.fhir2.api.dao.FhirContactPointDao;
import org.openmrs.module.fhir2.model.FhirContactPoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional
@Setter(AccessLevel.PACKAGE)
public class FhirContactPointServiceImpl implements FhirContactPointService {
	
	@Autowired
	FhirContactPointDao fhirContactPointDao;
	
	@Override
	public Collection<FhirContactPoint> getFhirFhirContactPoints() {
		return fhirContactPointDao.getFhirFhirContactPoints();
	}
}
