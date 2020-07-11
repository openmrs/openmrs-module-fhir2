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

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.TimeZone;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import org.apache.commons.io.IOUtils;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.openmrs.module.fhir2.FhirConstants;

public class BaseFhirNarrativeTest {
	
	protected static TimeZone defaultTimeZone;
	
	protected final FhirContext ctx = FhirContext.forR4();
	
	protected IParser parser;
	
	@BeforeClass
	public static void setupTimeZone() {
		defaultTimeZone = TimeZone.getDefault();
		TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
	}
	
	@Before
	public void setup() {
		ctx.setNarrativeGenerator(new OpenMRSThymeleafNarrativeGenerator(FhirConstants.HAPI_NARRATIVES_PROPERTY_FILE,
		        FhirConstants.OPENMRS_NARRATIVES_PROPERTY_FILE));
		parser = ctx.newJsonParser();
	}
	
	protected String readNarrativeFile(String resource) throws IOException {
		return IOUtils.resourceToString(resource, StandardCharsets.UTF_8, getClass().getClassLoader()).trim();
	}
	
	@AfterClass
	public static void resetTimeZone() {
		TimeZone.setDefault(defaultTimeZone);
	}
}
