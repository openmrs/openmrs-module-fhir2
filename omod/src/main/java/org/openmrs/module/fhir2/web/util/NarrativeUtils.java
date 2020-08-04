/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.fhir2.web.util;

import javax.validation.constraints.NotNull;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class NarrativeUtils {
	
	private static boolean validatePropertiesFilePath(@NotNull String path) {
		
		if (path.startsWith("file:")) {
			String filepath = path.substring("file:".length());
			
			if (filepath == null || filepath.trim().isEmpty()) {
				log.error("Properties File path must not be empty");
				return false;
			}
			
			if (!filepath.endsWith(".properties")) {
				log.error("Properties File must have extension '.properties'");
				return false;
			}
		} else if (path.startsWith("classpath:")) {
			String classpath = path.substring("classpath:".length());
			
			if (classpath == null || classpath.trim().isEmpty()) {
				log.error("Properties File classpath must not be empty");
				return false;
			}
		} else if (path.startsWith("openmrs:")) {
			String openmrsRelativePath = path.substring("openmrs:".length());
			
			if (openmrsRelativePath == null || openmrsRelativePath.trim().isEmpty()) {
				log.error("Properties File OpenMRS relative path must not be empty");
				return false;
			}
		}
		
		return true;
	}
	
	public static String getValidatedPropertiesFilePath(String path) {
		if (path == null || path.isEmpty()) {
			return null;
		}
		
		// add "file:" prefix if the path doesn't start with "classpath:" or "file:" or "openmrs:"
		// since OpenMRSNarrativeTemplateManifest.loadResource() method requires one of these prefixes
		if (!(path.startsWith("file:") || path.startsWith("classpath:") || path.startsWith("openmrs:"))) {
			path = "file:" + path;
		}
		
		if (validatePropertiesFilePath(path)) {
			return path;
		}
		
		return null;
	}
	
}
