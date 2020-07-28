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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;

import org.junit.Test;

public class NarrativeUtilTest {
	
	@Test
	public void shouldReturnNullWhenPathIsNull() {
		String propFilePathGiven = null;
		
		String propFilePathResult = NarrativeUtils.getValidatedPropertiesFilePath(propFilePathGiven);
		
		assertThat(propFilePathResult, nullValue());
	}
	
	@Test
	public void shouldReturnNullWhenFilePathIsEmpty() {
		String propFilePathGiven = "file:";
		
		String propFilePathResult = NarrativeUtils.getValidatedPropertiesFilePath(propFilePathGiven);
		
		assertThat(propFilePathResult, nullValue());
	}
	
	@Test
	public void shouldReturnNullWhenDoesNotEndWithProperties() {
		String propFilePathGiven = "file:somepath/which/does/not/end/with/properties/extension";
		
		String propFilePathResult = NarrativeUtils.getValidatedPropertiesFilePath(propFilePathGiven);
		
		assertThat(propFilePathResult, nullValue());
	}
	
	@Test
	public void shouldReturnFilePrefixedPathIfPrefixNotPresent() {
		String propFilePathGiven = "somepath/which/does/not/have/file/prefix/filename.properties";
		
		String propFilePathResult = NarrativeUtils.getValidatedPropertiesFilePath(propFilePathGiven);
		
		assertThat(propFilePathResult, equalTo(String.join(":", "file", propFilePathGiven)));
	}
	
	@Test
	public void shouldReturnNullWhenClassPathIsEmpty() {
		String propFilePathGiven = "classpath: ";
		
		String propFilePathResult = NarrativeUtils.getValidatedPropertiesFilePath(propFilePathGiven);
		
		assertThat(propFilePathResult, nullValue());
	}

	@Test
	public void shouldReturnNullWhenOpenmrsRelativePathIsEmpty() {
		String propFilePathGiven = "openmrs: ";

		String propFilePathResult = NarrativeUtils.getValidatedPropertiesFilePath(propFilePathGiven);

		assertThat(propFilePathResult, nullValue());
	}
	
}
