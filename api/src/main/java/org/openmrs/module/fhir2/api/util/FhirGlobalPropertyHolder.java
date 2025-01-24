/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.fhir2.api.util;

import static org.openmrs.module.fhir2.FhirConstants.FHIR2_MODULE_ID;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import lombok.extern.slf4j.Slf4j;
import org.openmrs.GlobalProperty;
import org.openmrs.api.APIException;
import org.openmrs.api.GlobalPropertyListener;
import org.openmrs.api.context.Context;
import org.openmrs.util.PrivilegeConstants;

@Slf4j
public class FhirGlobalPropertyHolder implements GlobalPropertyListener {
	
	private static final Map<String, String> globalPropertyCache = new ConcurrentHashMap<>(1 << 4 << 4);
	
	// the idea of missing keys is essentially to store "cache misses" so we avoid needing to do unnecessary lookups
	private static final Set<String> missingKeys = ConcurrentHashMap.newKeySet();
	
	public static String getGlobalProperty(String globalProperty) {
		if (missingKeys.contains(globalProperty)) {
			return null;
		} else {
			if (!supportsProperty(globalProperty)) {
				return null;
			}
			
			return globalPropertyCache.computeIfAbsent(globalProperty, (gp) -> {
				String gpValue = null;
				
				boolean hasUserContext = false;
				try {
					Context.addProxyPrivilege(PrivilegeConstants.GET_GLOBAL_PROPERTIES);
					hasUserContext = true;
				}
				catch (APIException ignored) {}
				
				try {
					gpValue = Context.getAdministrationService().getGlobalProperty(gp);
				}
				catch (APIException ignored) {
					
				}
				finally {
					if (hasUserContext) {
						Context.removeProxyPrivilege(PrivilegeConstants.GET_GLOBAL_PROPERTIES);
					}
				}
				
				if (gpValue == null || gpValue.isEmpty()) {
					missingKeys.add(gp);
					return null;
				}
				
				return gpValue;
			});
		}
	}
	
	public static String getGlobalProperty(String globalProperty, String defaultValue) {
		return Optional.ofNullable(getGlobalProperty(globalProperty)).orElse(defaultValue);
	}
	
	public static int getGlobalPropertyAsInteger(String globalProperty, int defaultValue) {
		String globalPropertyValue = getGlobalProperty(globalProperty);
		
		if (globalPropertyValue == null || globalPropertyValue.isEmpty()) {
			return defaultValue;
		}
		
		try {
			return Integer.parseInt(globalPropertyValue);
		}
		catch (NumberFormatException e) {
			log.error("Error converting global property {} with value '{}' to an integer", globalProperty,
			    globalPropertyValue, e);
			return defaultValue;
		}
	}
	
	public static Map<String, String> getGlobalProperties(String... globalProperties) {
		Map<String, String> result = new HashMap<>(globalProperties.length);
		for (String globalProperty : globalProperties) {
			String globalPropertyValue = getGlobalProperty(globalProperty);
			if (globalPropertyValue != null && !globalPropertyValue.isEmpty()) {
				result.put(globalProperty, globalPropertyValue);
			}
		}
		
		return result;
	}
	
	public static void reset() {
		globalPropertyCache.clear();
		missingKeys.clear();
	}
	
	@Override
	public boolean supportsPropertyName(String globalProperty) {
		return supportsProperty(globalProperty);
	}
	
	@Override
	public void globalPropertyChanged(GlobalProperty globalProperty) {
		globalPropertyCache.put(globalProperty.getProperty(), globalProperty.getPropertyValue());
		missingKeys.remove(globalProperty.getProperty());
	}
	
	@Override
	public void globalPropertyDeleted(String globalProperty) {
		globalPropertyCache.remove(globalProperty);
		missingKeys.add(globalProperty);
	}
	
	private static boolean supportsProperty(String globalProperty) {
		return globalProperty != null
		        && (globalProperty.startsWith(FHIR2_MODULE_ID) || globalProperty.startsWith("allergy"));
	}
}
