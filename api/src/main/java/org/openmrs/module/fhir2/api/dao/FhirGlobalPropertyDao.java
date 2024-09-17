/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.fhir2.api.dao;

import java.util.Map;

import org.openmrs.GlobalProperty;
import org.openmrs.annotation.Authorized;
import org.openmrs.api.APIException;
import org.openmrs.util.PrivilegeConstants;

public interface FhirGlobalPropertyDao {
	
	@Authorized(PrivilegeConstants.GET_GLOBAL_PROPERTIES)
	String getGlobalProperty(String property) throws APIException;
	
	@Authorized(PrivilegeConstants.GET_GLOBAL_PROPERTIES)
	GlobalProperty getGlobalPropertyObject(String property);
	
	@Authorized(PrivilegeConstants.GET_GLOBAL_PROPERTIES)
	Map<String, String> getGlobalProperties(String... properties);
}
