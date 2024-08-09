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

import java.util.Map;

import lombok.AccessLevel;
import lombok.Setter;
import org.openmrs.api.APIException;
import org.openmrs.module.fhir2.api.FhirGlobalPropertyService;
import org.openmrs.module.fhir2.api.util.FhirGlobalPropertyHolder;
import org.springframework.stereotype.Component;

@Component
@Setter(AccessLevel.PACKAGE)
public class FhirGlobalPropertyServiceImpl implements FhirGlobalPropertyService {
	
	@Override
	public String getGlobalProperty(String property) throws APIException {
		return FhirGlobalPropertyHolder.getGlobalProperty(property);
	}
	
	@Override
	public int getGlobalPropertyAsInteger(String property, int defaultValue) {
		return FhirGlobalPropertyHolder.getGlobalPropertyAsInteger(property, defaultValue);
	}
	
	@Override
	public String getGlobalProperty(String property, String defaultValue) {
		return FhirGlobalPropertyHolder.getGlobalProperty(property, defaultValue);
	}
	
	@Override
	public Map<String, String> getGlobalProperties(String... properties) {
		return FhirGlobalPropertyHolder.getGlobalProperties(properties);
	}
}
