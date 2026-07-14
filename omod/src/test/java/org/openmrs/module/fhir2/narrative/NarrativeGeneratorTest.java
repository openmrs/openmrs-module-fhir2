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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

import ca.uhn.fhir.context.ConfigurationException;
import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.narrative2.INarrativeTemplate;
import ca.uhn.fhir.narrative2.NarrativeTemplateManifest;
import ca.uhn.fhir.narrative2.TemplateTypeEnum;
import org.hl7.fhir.r4.model.BaseResource;
import org.hl7.fhir.r4.model.Patient;
import org.junit.Test;
import org.openmrs.util.OpenmrsUtil;

/**
 * Tests for Narrative Generator (OpenMRSThymeleafNarrativeGenerator,
 * OpenMRSNarrativeTemplateManifest and OpenMRSNarrativeTemplate)
 */
public class NarrativeGeneratorTest {
	
	protected final FhirContext ctx = FhirContext.forR4();
	
	protected final BaseResource dummyResource = new Patient();
	
	/**
	 * Check that property values are correctly mapped from property file to the template
	 * 
	 * @throws IOException
	 */
	@Test
	public void shouldMapValuesFromPropertiesFileToTemplate() throws IOException {
		String testNarrativePropFile = "classpath:org/openmrs/module/fhir2/narrative/testNarratives.properties";
		Set<String> expectedProfiles = new HashSet<>();
		expectedProfiles.add("testProfile");
		Set<String> expectedResourceType = new HashSet<>();
		expectedResourceType.add("testResourceType");
		TemplateTypeEnum expectedTemplateType = TemplateTypeEnum.THYMELEAF;
		String expectedContextPath = "testContextPath";
		String expectedNarrative = "<div>testNarrativeContent</div>";
		
		NarrativeTemplateManifest manifest = OpenmrsNarrativeTemplateManifest
		        .forManifestFileLocation(Collections.singletonList(testNarrativePropFile));
		INarrativeTemplate template = manifest.getTemplateByName(ctx, EnumSet.of(TemplateTypeEnum.THYMELEAF), "testRes")
		        .get(0);
		
		assertEquals(template.getAppliesToProfiles(), expectedProfiles);
		assertEquals(template.getAppliesToResourceTypes(), expectedResourceType);
		assertEquals(template.getTemplateType(), expectedTemplateType);
		assertEquals(template.getContextPath(), expectedContextPath);
		assertEquals(template.getTemplateText().trim(), expectedNarrative.trim());
	}
	
	/**
	 * Check that an {@code openmrs:} prefixed path is resolved against the OpenMRS application data
	 * directory before being handed to HAPI (which itself only understands classpath: and file:).
	 */
	@Test
	public void shouldResolveOpenmrsPrefixAgainstApplicationDataDirectory() {
		String givenPath = "openmrs:some/random/openmrs/path.properties";
		File expectedFile = new File(OpenmrsUtil.getApplicationDataDirectory(), "some/random/openmrs/path.properties");

		Throwable e = assertThrows(IOException.class, () -> OpenmrsNarrativeTemplateManifest
		        .forManifestFileLocation(Collections.singletonList(givenPath)));
		assertThat(e.getMessage(), containsString(expectedFile.getAbsolutePath()));
	}

	/**
	 * Check that ConfigurationException is thrown when property name is invalid
	 */
	@Test
	public void shouldThrowConfigurationExceptionWhenPropertyNameInvalid() {
		String testNarrativePropFile = "classpath:org/openmrs/module/fhir2/narrative/testNarrativesWithInvalidProperty.properties";
		
		Throwable e = assertThrows(ConfigurationException.class, () -> OpenmrsNarrativeTemplateManifest
		        .forManifestFileLocation(Collections.singletonList(testNarrativePropFile)));
		assertThat(e.getMessage(), containsString("Invalid property name: testRes.invalidPropertyName"));
	}
}
