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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

import ca.uhn.fhir.context.ConfigurationException;
import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.narrative2.INarrativeTemplate;
import ca.uhn.fhir.narrative2.TemplateTypeEnum;
import ca.uhn.fhir.rest.server.exceptions.InternalErrorException;
import org.hl7.fhir.r4.model.BaseResource;
import org.hl7.fhir.r4.model.Patient;
import org.junit.Test;
import org.openmrs.util.OpenmrsUtil;

public class NarrativeGeneratorTest {
	
	protected final FhirContext ctx = FhirContext.forR4();
	
	protected final BaseResource dummyResource = new Patient();
	
	@Test
	public void shouldThrowIOExcpetionWhenNoValidPrefixInPath() {
		String givenPath = "some/random/path/without/prefix.properties";
		ctx.setNarrativeGenerator(new OpenMRSThymeleafNarrativeGenerator(givenPath));
		
		String expectedErrorMessage = "Invalid resource name: '" + givenPath
		        + "' (must start with classpath: or file: or openmrs:)";
		
		Throwable e = assertThrows(InternalErrorException.class,
		    () -> ctx.getNarrativeGenerator().populateResourceNarrative(ctx, dummyResource));
		assertEquals(e.getMessage(), expectedErrorMessage);
	}
	
	@Test
	public void shouldThrowIOExcpetionForIncorrectClassPath() {
		String givenPath = "classpath:some/random/class/path.properties";
		ctx.setNarrativeGenerator(new OpenMRSThymeleafNarrativeGenerator(givenPath));
		
		String expectedErrorMessage = "Can not find '" + givenPath.substring("classpath:".length()) + "' on classpath";
		
		Throwable e = assertThrows(InternalErrorException.class,
		    () -> ctx.getNarrativeGenerator().populateResourceNarrative(ctx, dummyResource));
		assertEquals(e.getMessage(), expectedErrorMessage);
	}
	
	@Test
	public void shouldThrowIOExcpetionForIncorrectFilePath() {
		String givenPath = "file:some/random/file/path.properties";
		ctx.setNarrativeGenerator(new OpenMRSThymeleafNarrativeGenerator(givenPath));
		
		File file = new File(givenPath.substring("file:".length()));
		String expectedErrorMessage = "File not found: " + file.getAbsolutePath();
		
		Throwable e = assertThrows(InternalErrorException.class,
		    () -> ctx.getNarrativeGenerator().populateResourceNarrative(ctx, dummyResource));
		assertEquals(e.getMessage(), expectedErrorMessage);
	}
	
	@Test
	public void shouldThrowIOExcpetionForIncorrectOpenmrsPath() {
		String givenPath = "openmrs:some/random/openmrs/path.properties";
		ctx.setNarrativeGenerator(new OpenMRSThymeleafNarrativeGenerator(givenPath));
		
		File file = new File(OpenmrsUtil.getApplicationDataDirectory(), givenPath.substring("openmrs:".length()));
		String expectedErrorMessage = "File not found: " + file.getAbsolutePath();
		
		Throwable e = assertThrows(InternalErrorException.class,
		    () -> ctx.getNarrativeGenerator().populateResourceNarrative(ctx, dummyResource));
		assertEquals(e.getMessage(), expectedErrorMessage);
	}
	
	@Test
	public void shouldMapValuesFromPropertiesFileToTemplate() throws IOException {
		String testNarrativePropFile = "classpath:org/openmrs/module/fhir2/narrative/testNarratives.properties";
		Set<String> expectedProfiles = new HashSet<>();
		expectedProfiles.add("testProfile");
		Set<String> expectedResourceType = new HashSet<>();
		expectedResourceType.add("testResourceType");
		TemplateTypeEnum expectedTemplateType = TemplateTypeEnum.THYMELEAF;
		String expectedContextPath = "testContextPath";
		String expectedNarrative = "<div>testNarrativeContent</div>\n";
		
		OpenMRSNarrativeTemplateManifest manifest = OpenMRSNarrativeTemplateManifest
		        .forManifestFileLocation(Arrays.asList(testNarrativePropFile));
		INarrativeTemplate template = manifest.getTemplateByName(ctx, EnumSet.of(TemplateTypeEnum.THYMELEAF), "testRes")
		        .get(0);
		
		assertEquals(template.getAppliesToProfiles(), expectedProfiles);
		assertEquals(template.getAppliesToResourceTypes(), expectedResourceType);
		assertEquals(template.getTemplateType(), expectedTemplateType);
		assertEquals(template.getContextPath(), expectedContextPath);
		assertEquals(template.getTemplateText(), expectedNarrative);
	}
	
	@Test
	public void shouldThrowConfigurationExceptionWhenPropertyNameInvalid() {
		String testNarrativePropFile = "classpath:org/openmrs/module/fhir2/narrative/testNarrativesWithInvalidProperty.properties";
		String expectedErrorMessage = "Invalid property name: testRes.invalidPropertyName"
		        + " - the key must end in one of the expected extensions "
		        + "'.profile', '.resourceType', '.dataType', '.style', '.contextPath', '.narrative', '.title'";
		
		Throwable e = assertThrows(ConfigurationException.class,
		    () -> OpenMRSNarrativeTemplateManifest.forManifestFileLocation(Arrays.asList(testNarrativePropFile)));
		assertEquals(e.getMessage(), expectedErrorMessage);
	}
}
