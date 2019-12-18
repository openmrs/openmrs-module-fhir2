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

import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

import org.hl7.fhir.r4.model.HumanName;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.PersonName;

public class PersonNameTranslatorImplTest {
	
	private static final String PERSON_NAME_UUID = "123456-abcdef-123456";
	
	private static final String PERSON_GIVEN_NAME = "Jean Claude";
	
	private static final String PERSON_MIDDLE_NAME = "Wilhelm";
	
	private static final String PERSON_MIDDLE_NAME_2 = "Friedrich";
	
	private static final String PERSON_FAMILY_NAME = "van Damme";
	
	private PersonNameTranslatorImpl personNameTranslator;
	
	@Before
	public void setup() {
		personNameTranslator = new PersonNameTranslatorImpl();
	}
	
	@Test
	public void shouldConvertPersonNameToHumanName() {
		PersonName name = new PersonName();
		name.setUuid(PERSON_NAME_UUID);
		
		HumanName result = personNameTranslator.toFhirResource(name);
		assertThat(result, notNullValue());
		assertThat(result.getId(), equalTo(PERSON_NAME_UUID));
	}
	
	@Test
	public void shouldConvertGivenNameToGivenName() {
		PersonName name = new PersonName();
		name.setGivenName(PERSON_GIVEN_NAME);
		
		HumanName result = personNameTranslator.toFhirResource(name);
		assertThat(result.getGiven(), notNullValue());
		assertThat(result.getGiven(), not(empty()));
		assertThat(result.getGiven().get(0).getValue(), equalTo(PERSON_GIVEN_NAME));
	}
	
	@Test
	public void shouldConvertMiddleNameToGivenName() {
		PersonName name = new PersonName();
		name.setGivenName(PERSON_GIVEN_NAME);
		name.setMiddleName(PERSON_MIDDLE_NAME);
		
		HumanName result = personNameTranslator.toFhirResource(name);
		assertThat(result.getGiven(), notNullValue());
		assertThat(result.getGiven(), not(empty()));
		assertThat(result.getGiven().get(1).getValue(), equalTo(PERSON_MIDDLE_NAME));
	}
	
	@Test
	public void shouldConvertOpenmrsFamilyNameToFamilyName() {
		PersonName name = new PersonName();
		name.setFamilyName(PERSON_FAMILY_NAME);
		
		assertThat(personNameTranslator.toFhirResource(name).getFamily(), equalTo(PERSON_FAMILY_NAME));
	}
	
	@Test
	public void shouldConvertHumanNameToPersonName() {
		HumanName name = new HumanName();
		name.setId(PERSON_NAME_UUID);
		
		PersonName result = personNameTranslator.toOpenmrsType(name);
		assertThat(result, notNullValue());
		assertThat(result.getUuid(), equalTo(PERSON_NAME_UUID));
	}
	
	@Test
	public void shouldConvertFirstGivenNameToGivenName() {
		HumanName name = new HumanName();
		name.addGiven(PERSON_GIVEN_NAME);
		
		assertThat(personNameTranslator.toOpenmrsType(name).getGivenName(), equalTo(PERSON_GIVEN_NAME));
	}
	
	@Test
	public void shouldConvertOtherGivenNamesToMiddleName() {
		// note that handling a middle name requires a first name
		HumanName name = new HumanName();
		name.addGiven(PERSON_GIVEN_NAME);
		name.addGiven(PERSON_MIDDLE_NAME);
		name.addGiven(PERSON_MIDDLE_NAME_2);
		
		assertThat(personNameTranslator.toOpenmrsType(name).getMiddleName(), equalTo(PERSON_MIDDLE_NAME + " "
		        + PERSON_MIDDLE_NAME_2));
	}
	
	@Test
	public void shouldConvertFhirFamilyNameToFamilyName() {
		HumanName name = new HumanName();
		name.setFamily(PERSON_FAMILY_NAME);
		
		assertThat(personNameTranslator.toOpenmrsType(name).getFamilyName(), equalTo(PERSON_FAMILY_NAME));
	}
	
}
