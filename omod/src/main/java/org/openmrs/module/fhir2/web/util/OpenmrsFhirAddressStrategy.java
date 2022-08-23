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

import static org.openmrs.module.fhir2.web.util.FhirVersionUtils.FhirVersion.UNKNOWN;

import javax.annotation.PreDestroy;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import ca.uhn.fhir.rest.server.IServerAddressStrategy;
import lombok.AccessLevel;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.openmrs.GlobalProperty;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.GlobalPropertyListener;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.api.FhirGlobalPropertyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Slf4j
@Setter(AccessLevel.PACKAGE)
public class OpenmrsFhirAddressStrategy implements IServerAddressStrategy {
	
	private volatile String gpPrefix;
	
	private final AdministrationService administrationService;
	
	private GlobalPropertyListener globalPropertyListener = new GlobalPropertyListener() {
		
		@Override
		public boolean supportsPropertyName(String propertyName) {
			return FhirConstants.GLOBAL_PROPERTY_URI_PREFIX.equals(propertyName);
		}
		
		@Override
		public void globalPropertyChanged(GlobalProperty newValue) {
			synchronized (OpenmrsFhirAddressStrategy.this) {
				gpPrefix = newValue.getPropertyValue();
			}
		}
		
		@Override
		public void globalPropertyDeleted(String propertyName) {
			synchronized (OpenmrsFhirAddressStrategy.this) {
				gpPrefix = null;
			}
		}
	};
	
	@Autowired
	public OpenmrsFhirAddressStrategy(FhirGlobalPropertyService globalPropertyService,
	    @Qualifier("adminService") AdministrationService administrationService) {
		this.administrationService = administrationService;
		if (administrationService != null) {
			administrationService.addGlobalPropertyListener(globalPropertyListener);
		}
		
		try {
			this.gpPrefix = globalPropertyService.getGlobalProperty(FhirConstants.GLOBAL_PROPERTY_URI_PREFIX);
		}
		catch (Exception e) {
			log.error("An error occurred while trying to read the {} property", FhirConstants.GLOBAL_PROPERTY_URI_PREFIX, e);
			this.gpPrefix = null;
		}
	}
	
	@Override
	@Transactional(readOnly = true)
	public String determineServerBase(ServletContext context, HttpServletRequest request) {
		String gpPrefix;
		
		synchronized (this) {
			gpPrefix = this.gpPrefix;
		}
		
		if (StringUtils.isNotBlank(gpPrefix)) {
			StringBuilder gpUrl = new StringBuilder().append(gpPrefix);
			FhirVersionUtils.FhirVersion fhirVersion = FhirVersionUtils.getFhirVersion(request);
			return gpUrl.append('/').append(fhirVersion == UNKNOWN ? "" : fhirVersion.toString()).toString();
		}
		
		String scheme;
		if (StringUtils.isNotBlank(request.getHeader("X-Forwarded-Proto"))) {
			scheme = request.getHeader("X-Forwarded-Proto");
		} else {
			scheme = request.getScheme();
		}
		
		String host;
		if (StringUtils.isNotBlank(request.getHeader("X-Forwarded-Host"))) {
			host = request.getHeader("X-Forwarded-Host");
		} else {
			host = request.getServerName();
		}
		
		int serverPort = request.getServerPort();
		String port;
		if (StringUtils.isNotBlank(request.getHeader("X-Forwarded-Port"))) {
			port = ":" + request.getHeader("X-Forwarded-Port");
			if (scheme.equalsIgnoreCase("http") && port.equals(":80")) {
				port = "";
			} else if (scheme.equalsIgnoreCase("https") && port.equals(":443")) {
				port = "";
			}
		} else if ("http".equalsIgnoreCase(request.getScheme())) {
			port = serverPort == 80 ? "" : ":" + serverPort;
		} else if ("https".equalsIgnoreCase(request.getScheme())) {
			port = serverPort == 443 ? "" : ":" + serverPort;
		} else {
			port = ":" + serverPort;
		}
		
		FhirVersionUtils.FhirVersion fhirVersion = FhirVersionUtils.getFhirVersion(request);
		
		return scheme + "://" + host + port + StringUtils.defaultString(request.getContextPath()) + "/ws/fhir2/"
		        + (fhirVersion == UNKNOWN ? "" : fhirVersion.toString());
	}
	
	public void setGpPrefix(String gpPrefix) {
		if (StringUtils.isNotBlank(gpPrefix)) {
			if (gpPrefix.endsWith("/")) {
				gpPrefix = gpPrefix.substring(0, gpPrefix.length() - 1);
			}
		}
		
		this.gpPrefix = gpPrefix;
	}
	
	@PreDestroy
	public void preDestroy() {
		if (administrationService != null) {
			administrationService.removeGlobalPropertyListener(globalPropertyListener);
		}
	}
}
