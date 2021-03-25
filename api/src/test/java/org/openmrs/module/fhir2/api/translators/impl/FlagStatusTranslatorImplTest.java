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

import org.hl7.fhir.r4.model.Flag;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.openmrs.module.fhir2.model.FhirFlag;

@RunWith(MockitoJUnitRunner.class)
public class FlagStatusTranslatorImplTest {
	
	private FlagStatusTranslatorImpl flagStatusTranslator;
	
	@Before
	public void setup() {
		flagStatusTranslator = new FlagStatusTranslatorImpl();
	}
	
	@Test
	public void shouldTranslateActiveStatus() {
		//toFhirResource
		assertThat(flagStatusTranslator.toFhirResource(FhirFlag.FlagStatus.ACTIVE), is(Flag.FlagStatus.ACTIVE));
		//Translating back to OpenMrsType
		assertThat(flagStatusTranslator.toOpenmrsType(Flag.FlagStatus.ACTIVE), is(FhirFlag.FlagStatus.ACTIVE));
	}
	
	@Test
	public void shouldTranslateInActiveStatus() {
		//toFhirResource
		assertThat(flagStatusTranslator.toFhirResource(FhirFlag.FlagStatus.INACTIVE), is(Flag.FlagStatus.INACTIVE));
		//Translating back to OpenMrsType
		assertThat(flagStatusTranslator.toOpenmrsType(Flag.FlagStatus.INACTIVE), is(FhirFlag.FlagStatus.INACTIVE));
	}
	
	@Test
	public void shouldTranslateEnteredInErrorStatus() {
		//toFhirResource
		assertThat(flagStatusTranslator.toFhirResource(FhirFlag.FlagStatus.ENTERED_IN_ERROR),
		    is(Flag.FlagStatus.ENTEREDINERROR));
		//Translating back to OpenMrsType
		assertThat(flagStatusTranslator.toOpenmrsType(Flag.FlagStatus.ENTEREDINERROR),
		    is(FhirFlag.FlagStatus.ENTERED_IN_ERROR));
	}
	
	@Test
	public void shouldTranslateNullStatus() {
		//toFhirResource
		assertThat(flagStatusTranslator.toFhirResource(FhirFlag.FlagStatus.NULL), is(Flag.FlagStatus.NULL));
		//Translating back to OpenMrsType
		assertThat(flagStatusTranslator.toOpenmrsType(Flag.FlagStatus.NULL), is(FhirFlag.FlagStatus.NULL));
	}
	
}
