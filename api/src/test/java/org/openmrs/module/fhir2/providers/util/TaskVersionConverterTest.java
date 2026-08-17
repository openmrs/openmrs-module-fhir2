/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.fhir2.providers.util;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

import org.hl7.fhir.dstu3.model.Reference;
import org.hl7.fhir.dstu3.model.Task;
import org.hl7.fhir.exceptions.FHIRException;
import org.junit.Test;

public class TaskVersionConverterTest {
	
	@Test
	public void convertTask_shouldMapDstu3FocusAndContextToR4FocusAndEncounterRespectively() throws FHIRException {
		//given
		Task src = new Task();
		src.setStatus(Task.TaskStatus.REQUESTED);
		src.setIntent(Task.TaskIntent.ORDER);
		src.setFocus(new Reference("Observation/obs-uuid-1111"));
		src.setContext(new Reference("Encounter/enc-uuid-2222"));
		
		//when
		org.hl7.fhir.r4.model.Task result = TaskVersionConverter.convertTask(src);
		
		//then
		assertThat(result.getFocus(), notNullValue());
		assertThat(result.getFocus().getReference(), equalTo("Observation/obs-uuid-1111"));
		assertThat(result.getEncounter(), notNullValue());
		assertThat(result.getEncounter().getReference(), equalTo("Encounter/enc-uuid-2222"));
	}
	
	@Test
	public void convertTask_shouldMapDstu3ContextToR4EncounterWhenFocusIsAbsent() throws FHIRException {
		//given
		Task src = new Task();
		src.setStatus(Task.TaskStatus.REQUESTED);
		src.setIntent(Task.TaskIntent.ORDER);
		src.setContext(new Reference("Encounter/enc-uuid-2222"));
		
		//when
		org.hl7.fhir.r4.model.Task result = TaskVersionConverter.convertTask(src);
		
		//then
		assertThat(result.hasFocus(), equalTo(false));
		assertThat(result.getEncounter(), notNullValue());
		assertThat(result.getEncounter().getReference(), equalTo("Encounter/enc-uuid-2222"));
	}
}
