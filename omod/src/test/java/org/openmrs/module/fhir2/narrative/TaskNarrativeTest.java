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
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

import java.io.IOException;

import org.hl7.fhir.r4.model.Task;
import org.junit.Test;

/**
 * Test for Task resource narrative generation
 */
public class TaskNarrativeTest extends BaseFhirNarrativeTest {
	
	private static final String EXAMPLE_RESOURCE_PATH = "org/openmrs/module/fhir2/narrative/example/task-example.json";
	
	private static final String EXPECTED_NARRATIVE_PATH = "org/openmrs/module/fhir2/narrative/expected/task-expected.html";
	
	/**
	 * Check that the expected narrative is generated for some example Task resource
	 * 
	 * @throws IOException
	 */
	@Test
	public void shouldGenerateTaskNarrative() throws IOException {
		Task given = parser.parseResource(Task.class,
		    getClass().getClassLoader().getResourceAsStream(EXAMPLE_RESOURCE_PATH));
		
		Task result = parser.parseResource(Task.class, parser.encodeResourceToString(given));
		
		assertThat(result, notNullValue());
		assertThat(result.getText(), notNullValue());
		assertThat(result.getText().getStatusAsString(), equalTo("generated"));
		assertThat(result.getText().getDivAsString(), equalTo(readNarrativeFile(EXPECTED_NARRATIVE_PATH)));
	}
}
