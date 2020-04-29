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
import org.openmrs.module.fhir2.api.dao.FhirGlobalPropertyDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional
@Setter(AccessLevel.PACKAGE)
public class FhirGlobalPropertyServiceImpl implements FhirGlobalPropertyService {
	
	@Autowired
	private FhirGlobalPropertyDao dao;
	
	@Override
	@Transactional(readOnly = true)
	public String getGlobalProperty(String property) throws APIException {
		return dao.getGlobalProperty(property);
	}
	
	@Override
	@Transactional(readOnly = true)
	public Integer getGlobalProperty(String property, Integer defaultValue) {
		try {
			return Integer.valueOf(getGlobalProperty(property, String.valueOf(defaultValue)));
		}
		catch (NumberFormatException e) {
			return defaultValue;
		}
	}
	
	@Override
	@Transactional(readOnly = true)
	public String getGlobalProperty(String property, String defaultValue) {
		return this.getGlobalProperty(property) == null ? defaultValue : this.getGlobalProperty(property);
	}
	
	@Override
	@Transactional(readOnly = true)
	public Map<String, String> getGlobalProperties(String... properties) {
		return dao.getGlobalProperties(properties);
	}
}
