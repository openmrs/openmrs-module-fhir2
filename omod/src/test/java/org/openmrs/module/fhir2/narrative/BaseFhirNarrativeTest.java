/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.fhir2.narrative;

import static org.openmrs.module.fhir2.api.util.GeneralUtils.resourceToString;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.TimeZone;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.openmrs.module.fhir2.FhirConstants;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;

/**
 * Base class for narrative generation tests
 */
public class BaseFhirNarrativeTest {
	
	protected static TimeZone defaultTimeZone;
	
	protected static Locale defaultLocale;
	
	protected final FhirContext ctx = FhirContext.forR4();
	
	protected IParser parser;
	
	protected ReloadableResourceBundleMessageSource messageSource;
	
	/**
	 * Setup a common timezone and locale before all the tests: the expected narratives are in UTC and
	 * US English, and dates are formatted in the default timezone and locale, so on a machine whose
	 * locale writes "pm" rather than "PM" (en_GB, en_UG, ...) the narrative generation tests would fail
	 */
	@BeforeClass
	public static void setupTimeZoneAndLocale() {
		defaultTimeZone = TimeZone.getDefault();
		TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
		defaultLocale = Locale.getDefault();
		Locale.setDefault(Locale.US);
	}
	
	/**
	 * Set OpenMRSThymeleafNarrativeGenerator as the narrative generator for the FhirContext and
	 * initialize the parser
	 */
	@Before
	public void setup() {
		messageSource = new ReloadableResourceBundleMessageSource();
		messageSource.setBasename("classpath:messages");
		
		ctx.setNarrativeGenerator(new OpenmrsThymeleafNarrativeGenerator(messageSource,
		        FhirConstants.OPENMRS_NARRATIVES_PROPERTY_FILE, FhirConstants.HAPI_NARRATIVES_PROPERTY_FILE));
		parser = ctx.newJsonParser();
	}
	
	/**
	 * A utility to read expected narrative files in tests
	 * 
	 * @param resource
	 * @return narrative file content as a String
	 * @throws IOException
	 */
	protected String readNarrativeFile(String resource) throws IOException {
		return resourceToString(resource, StandardCharsets.UTF_8, getClass().getClassLoader()).trim();
	}
	
	/**
	 * Reset the timezone and locale to defaultTimeZone and defaultLocale after all tests are run
	 */
	@AfterClass
	public static void resetTimeZoneAndLocale() {
		TimeZone.setDefault(defaultTimeZone);
		Locale.setDefault(defaultLocale);
	}
}
