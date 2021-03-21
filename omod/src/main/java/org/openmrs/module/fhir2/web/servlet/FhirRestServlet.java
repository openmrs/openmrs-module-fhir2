/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.fhir2.web.servlet;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.api.EncodingEnum;
import ca.uhn.fhir.rest.server.BasePagingProvider;
import ca.uhn.fhir.rest.server.FifoMemoryPagingProvider;
import ca.uhn.fhir.rest.server.IResourceProvider;
import ca.uhn.fhir.rest.server.IServerAddressStrategy;
import ca.uhn.fhir.rest.server.RestfulServer;
import ca.uhn.fhir.rest.server.interceptor.LoggingInterceptor;
import lombok.AccessLevel;
import lombok.Setter;
import org.apache.commons.lang3.math.NumberUtils;
import org.openmrs.GlobalProperty;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.GlobalPropertyListener;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.api.FhirGlobalPropertyService;
import org.openmrs.module.fhir2.api.search.interceptor.SummaryInterceptor;
import org.openmrs.module.fhir2.narrative.OpenmrsThymeleafNarrativeGenerator;
import org.openmrs.module.fhir2.web.util.NarrativeUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

@Component
@Setter(AccessLevel.PUBLIC)
public class FhirRestServlet extends RestfulServer {
	
	private static final long serialVersionUID = 2L;
	
	private static final List<String> DEFAULT_NARRATIVE_FILES = Arrays.asList(FhirConstants.OPENMRS_NARRATIVES_PROPERTY_FILE,
	    FhirConstants.HAPI_NARRATIVES_PROPERTY_FILE);
	
	@Autowired
	@Qualifier("adminService")
	private AdministrationService administrationService;
	
	@Autowired
	private FhirGlobalPropertyService globalPropertyService;
	
	@Autowired
	@Qualifier("hapiLoggingInterceptor")
	private LoggingInterceptor loggingInterceptor;
	
	@Autowired
	@Qualifier("hapiSummaryInterceptor")
	private SummaryInterceptor summaryInterceptor;
	
	private MessageSource messageSource;
	
	@Setter(AccessLevel.NONE)
	private BasePagingProvider pagingProvider;
	
	private GlobalPropertyListener fhirRestServletListener = new GlobalPropertyListener() {
		
		@Override
		public boolean supportsPropertyName(String propertyName) {
			return FhirConstants.OPENMRS_FHIR_MAXIMUM_PAGE_SIZE.equals(propertyName)
			        || FhirConstants.OPENMRS_FHIR_DEFAULT_PAGE_SIZE.equals(propertyName);
		}
		
		@Override
		public void globalPropertyChanged(GlobalProperty newValue) {
			int value;
			try {
				value = Integer.parseInt(newValue.getPropertyValue());
			}
			catch (NumberFormatException e) {
				globalPropertyDeleted(newValue.getProperty());
				return;
			}
			
			switch (newValue.getProperty()) {
				case FhirConstants.OPENMRS_FHIR_DEFAULT_PAGE_SIZE:
					pagingProvider.setDefaultPageSize(value);
					break;
				case FhirConstants.OPENMRS_FHIR_MAXIMUM_PAGE_SIZE:
					pagingProvider.setMaximumPageSize(value);
					break;
			}
		}
		
		@Override
		public void globalPropertyDeleted(String propertyName) {
			switch (propertyName) {
				case FhirConstants.OPENMRS_FHIR_DEFAULT_PAGE_SIZE:
					pagingProvider.setDefaultPageSize(10);
					break;
				case FhirConstants.OPENMRS_FHIR_MAXIMUM_PAGE_SIZE:
					pagingProvider.setMaximumPageSize(100);
					break;
			}
		}
	};
	
	// TODO: Why does the formatter screw up only this method?
	//@formatter:off
	@Override
	protected void initialize() {
		// ensure properties for this class are properly injected
		if (globalPropertyService == null) {
			SpringBeanAutowiringSupport.processInjectionBasedOnServletContext(this, getServletContext());
			administrationService.addGlobalPropertyListener(fhirRestServletListener);
		}

		int defaultPageSize = NumberUtils
				.toInt(globalPropertyService.getGlobalProperty(FhirConstants.OPENMRS_FHIR_DEFAULT_PAGE_SIZE), 10);
		int maximumPageSize = NumberUtils
				.toInt(globalPropertyService.getGlobalProperty(FhirConstants.OPENMRS_FHIR_MAXIMUM_PAGE_SIZE), 100);

		pagingProvider = new FifoMemoryPagingProvider(10_000);
		pagingProvider.setDefaultPageSize(defaultPageSize);
		pagingProvider.setMaximumPageSize(maximumPageSize);

		setPagingProvider(pagingProvider);
		setDefaultResponseEncoding(EncodingEnum.JSON);
		registerInterceptor(loggingInterceptor);
		registerInterceptor(summaryInterceptor);

		String narrativesOverridePropertyFile = NarrativeUtils.getValidatedPropertiesFilePath(
				globalPropertyService.getGlobalProperty(FhirConstants.NARRATIVES_OVERRIDE_PROPERTY_FILE, (String) null));

		List<String> narrativePropertiesFiles;
		if (narrativesOverridePropertyFile != null) {
			narrativePropertiesFiles = new ArrayList<>(3);
			narrativePropertiesFiles.add(narrativesOverridePropertyFile);
			narrativePropertiesFiles.addAll(DEFAULT_NARRATIVE_FILES);
		} else {
			narrativePropertiesFiles = DEFAULT_NARRATIVE_FILES;
		}

		getFhirContext()
				.setNarrativeGenerator(new OpenmrsThymeleafNarrativeGenerator(messageSource, narrativePropertiesFiles));
	}
	//@formatter:on
	
	@Override
	protected String createPoweredByHeaderComponentName() {
		return FhirConstants.OPENMRS_FHIR_SERVER_NAME;
	}
	
	@Override
	protected String getRequestPath(String requestFullPath, String servletContextPath, String servletPath) {
		return requestFullPath
		        .substring(escapedLength(servletContextPath) + escapedLength(servletPath) + escapedLength("/fhir2Servlet"));
	}
	
	@Override
	@Autowired
	@Qualifier("fhirR4")
	public void setFhirContext(FhirContext theFhirContext) {
		super.setFhirContext(theFhirContext);
	}
	
	@Override
	@Autowired
	@Qualifier("fhirResources")
	public void setResourceProviders(Collection<IResourceProvider> theProviders) {
		super.setResourceProviders(theProviders);
	}
	
	@Override
	@Autowired
	public void setServerAddressStrategy(IServerAddressStrategy theServerAddressStrategy) {
		super.setServerAddressStrategy(theServerAddressStrategy);
	}
	
	@Autowired
	public void setMessageSource(MessageSource messageSource) {
		this.messageSource = messageSource;
	}
}
