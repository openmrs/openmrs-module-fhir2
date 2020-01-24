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

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import org.hl7.fhir.r4.model.Enumerations;
import org.junit.Before;
import org.junit.Test;

public class GenderTranslatorImplTest {
	
	private GenderTranslatorImpl genderTranslator;
	
	@Before
	public void setup() {
		this.genderTranslator = new GenderTranslatorImpl();
	}
	
	@Test
	public void shouldConvertMToMale() {
		assertThat(genderTranslator.toFhirResource("M"), is(Enumerations.AdministrativeGender.MALE));
	}
	
	@Test
	public void shouldConvertFToFemale() {
		assertThat(genderTranslator.toFhirResource("F"), is(Enumerations.AdministrativeGender.FEMALE));
	}
	
	@Test
	public void shouldConvertUToUnknown() {
		assertThat(genderTranslator.toFhirResource("U"), is(Enumerations.AdministrativeGender.UNKNOWN));
	}
	
	@Test
	public void shouldConvertOToOther() {
		assertThat(genderTranslator.toFhirResource("O"), is(Enumerations.AdministrativeGender.OTHER));
	}
	
	@Test
	public void shouldConvertNullToNullGender() {
		assertThat(genderTranslator.toFhirResource(null), is(Enumerations.AdministrativeGender.NULL));
	}
	
	@Test
	public void shouldConvertUnknownValueToNullGender() {
		assertThat(genderTranslator.toFhirResource("DUMMY VALUE"), is(Enumerations.AdministrativeGender.NULL));
	}
	
	@Test
	public void shouldConvertMaleToM() {
		assertThat(genderTranslator.toOpenmrsType(Enumerations.AdministrativeGender.MALE), equalTo("M"));
	}
	
	@Test
	public void shouldConvertFemaleToF() {
		assertThat(genderTranslator.toOpenmrsType(Enumerations.AdministrativeGender.FEMALE), equalTo("F"));
	}
	
	@Test
	public void shouldConvertUnknownToU() {
		assertThat(genderTranslator.toOpenmrsType(Enumerations.AdministrativeGender.UNKNOWN), equalTo("U"));
	}
	
	@Test
	public void shouldConvertOtherToO() {
		assertThat(genderTranslator.toOpenmrsType(Enumerations.AdministrativeGender.OTHER), equalTo("O"));
	}
	
	@Test
	public void shouldConvertNullGenderToNull() {
		assertThat(genderTranslator.toOpenmrsType(Enumerations.AdministrativeGender.NULL), nullValue());
	}
	
	@Test
	public void shouldReturnNullWhenGenderIsNull() {
		assertThat(genderTranslator.toOpenmrsType(null), nullValue());
	}
	
}
