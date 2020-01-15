/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.fhir2;

import org.springframework.beans.factory.annotation.Value;

public class FhirConstants {
	
	private FhirConstants() {
	}
	
	public static String OPENMRS_FHIR_SERVER_NAME = "OpenMRS FHIR Server";
	
	@Value("${project.version}")
	public static String OPENMRS_FHIR_SERVER_VERSION;
	
	public static String OPENMRS_FHIR_PREFIX = "https://fhir.openmrs.org";
	
	public static String OPENMRS_FHIR_EXT_PREFIX = OPENMRS_FHIR_PREFIX + "/ext";
	
	public static String OPENMRS_FHIR_EXT_NAME = OPENMRS_FHIR_EXT_PREFIX + "/name";
	
	public static String OPENMRS_FHIR_EXT_ADDRESS = OPENMRS_FHIR_EXT_PREFIX + "/address";
	
	public static String OPENMRS_FHIR_DEFAULT_PAGE_SIZE = "fhir2.paging.default";
	
	public static String OPENMRS_FHIR_MAXIMUM_PAGE_SIZE = "fhir2.paging.maximum";
	
	public static String PATIENT = "Patient";
	
	@Value("${project.parent.artifactId}.personAttributeTypeUuid")
	public static String PERSON_ATTRIBUTE_TYPE_PROPERTY;
	
	@Value("${project.parent.artifactId}.locationAttributeTypeUuid")
	public static String LOCATION_ATTRIBUTE_TYPE_PROPERTY;
	
}
