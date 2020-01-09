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

import lombok.AccessLevel;
import lombok.Setter;
import org.openmrs.api.APIException;
import org.openmrs.module.fhir2.api.FhirGlobalPropertyService;
import org.openmrs.module.fhir2.api.dao.FhirGlobalPropertyDao;
import org.springframework.stereotype.Component;

import javax.inject.Inject;

@Component
@Setter(AccessLevel.PACKAGE)
public class FhirGlobalPropertyServiceImpl implements FhirGlobalPropertyService {
	
	@Inject
	private FhirGlobalPropertyDao dao;
	
	@Override
	public String getGlobalProperty(String property) throws APIException {
		return dao.getGlobalProperty(property);
	}
}
