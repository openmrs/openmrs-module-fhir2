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
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.hl7.fhir.r4.model.Group;
import org.hl7.fhir.r4.model.Reference;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openmrs.Cohort;
import org.openmrs.User;
import org.openmrs.module.fhir2.api.translators.PractitionerReferenceTranslator;

@RunWith(MockitoJUnitRunner.class)
public class BaseGroupTranslatorTest {
	
	private static final String COHORT_UUID = "787e12bd-314e-4cc4-9b4d-1cdff9be9545";
	
	private static final String COHORT_NAME = "Patients with VL >= 100.00";
	
	@Mock
	private PractitionerReferenceTranslator<User> practitionerReferenceTranslator;
	
	private BaseGroupTranslator baseGroupTranslator;
	
	private Cohort cohort;
	
	private Group group;
	
	@Before
	public void setup() {
		baseGroupTranslator = new BaseGroupTranslator() {};
		baseGroupTranslator.setPractitionerReferenceTranslator(practitionerReferenceTranslator);
		
		cohort = new Cohort();
		cohort.setUuid(COHORT_UUID);
		cohort.setName(COHORT_NAME);
		
		group = new Group();
		group.setId(COHORT_UUID);
	}
	
	@Test
	public void shouldTranslateManagingEntityToCreatorOpenMRSType() {
		User user = mock(User.class);
		Reference practitionerRef = mock(Reference.class);
		when(practitionerReferenceTranslator.toOpenmrsType(practitionerRef)).thenReturn(user);
		
		group.setManagingEntity(practitionerRef);
		
		Cohort result = baseGroupTranslator.toOpenmrsType(cohort, group);
		assertThat(result, notNullValue());
		assertThat(result.getCreator(), notNullValue());
		assertThat(result.getCreator(), is(user));
		
	}
	
	@Test
	public void shouldTranslateCreatorToManagingEntityFHIRType() {
		User user = mock(User.class);
		Reference practitionerRef = mock(Reference.class);
		when(practitionerReferenceTranslator.toFhirResource(user)).thenReturn(practitionerRef);
		
		cohort.setCreator(user);
		
		Group result = baseGroupTranslator.toFhirResource(cohort);
		assertThat(result, notNullValue());
		assertThat(result.hasManagingEntity(), is(true));
		assertThat(result.getManagingEntity(), is(practitionerRef));
	}
	
}
