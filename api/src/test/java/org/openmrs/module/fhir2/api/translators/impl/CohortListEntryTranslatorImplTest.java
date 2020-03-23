/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.fhir2.api.translators.impl;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

import java.util.ArrayList;
import java.util.List;

import org.hl7.fhir.r4.model.ListResource;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.openmrs.Cohort;

@RunWith(MockitoJUnitRunner.class)
public class CohortListEntryTranslatorImplTest {
	
	private CohortListEntryTranslatorImpl cohortListEntryTranslator;
	
	@Before
	public void setup() {
		cohortListEntryTranslator = new CohortListEntryTranslatorImpl();
	}
	
	@Test
	public void toFhirResource_shouldReturnNull() {
		List<ListResource.ListEntryComponent> list = cohortListEntryTranslator.toFhirResource(new Cohort());
		assertThat(list, nullValue());
	}
	
	@Test
	public void toOpenmrsType_shouldReturnCohortAsItWasPassed() {
		Cohort cohort = new Cohort();
		cohort.setId(1);
		
		Cohort result = cohortListEntryTranslator.toOpenmrsType(cohort, new ArrayList<>());
		assertThat(result, notNullValue());
		assertThat(result, equalTo(cohort));
		assertThat(result.getId(), equalTo(cohort.getId()));
	}
}

