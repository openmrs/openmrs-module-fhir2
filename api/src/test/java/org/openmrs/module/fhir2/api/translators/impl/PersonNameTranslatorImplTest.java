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
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

import org.hl7.fhir.r4.model.Extension;
import org.hl7.fhir.r4.model.HumanName;
import org.hl7.fhir.r4.model.StringType;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.Person;
import org.openmrs.PersonName;
import org.openmrs.module.fhir2.FhirConstants;

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
	public void shouldAddExtensionForPrefix() {
		PersonName name = new PersonName();
		name.setPrefix(PERSON_MIDDLE_NAME);
		
		assertThat(personNameTranslator.toFhirResource(name).getExtensionByUrl(FhirConstants.OPENMRS_FHIR_EXT_NAME)
		        .getExtensionByUrl(FhirConstants.OPENMRS_FHIR_EXT_NAME + "#prefix"),
		    hasProperty("value", hasProperty("value", equalTo(PERSON_MIDDLE_NAME))));
	}
	
	@Test
	public void shouldAddExtensionForFamilyNamePrefix() {
		PersonName name = new PersonName();
		name.setFamilyNamePrefix(PERSON_MIDDLE_NAME);
		
		assertThat(personNameTranslator.toFhirResource(name).getExtensionByUrl(FhirConstants.OPENMRS_FHIR_EXT_NAME)
		        .getExtensionByUrl(FhirConstants.OPENMRS_FHIR_EXT_NAME + "#familyNamePrefix"),
		    hasProperty("value", hasProperty("value", equalTo(PERSON_MIDDLE_NAME))));
	}
	
	@Test
	public void shouldAddExtensionForFamilyName2() {
		PersonName name = new PersonName();
		name.setFamilyName2(PERSON_MIDDLE_NAME);
		
		assertThat(personNameTranslator.toFhirResource(name).getExtensionByUrl(FhirConstants.OPENMRS_FHIR_EXT_NAME)
		        .getExtensionByUrl(FhirConstants.OPENMRS_FHIR_EXT_NAME + "#familyName2"),
		    hasProperty("value", hasProperty("value", equalTo(PERSON_MIDDLE_NAME))));
	}
	
	@Test
	public void shouldAddExtensionForFamilyNameSuffix() {
		PersonName name = new PersonName();
		name.setFamilyNameSuffix(PERSON_MIDDLE_NAME);
		
		assertThat(personNameTranslator.toFhirResource(name).getExtensionByUrl(FhirConstants.OPENMRS_FHIR_EXT_NAME)
		        .getExtensionByUrl(FhirConstants.OPENMRS_FHIR_EXT_NAME + "#familyNameSuffix"),
		    hasProperty("value", hasProperty("value", equalTo(PERSON_MIDDLE_NAME))));
	}
	
	@Test
	public void shouldAddExtensionForDegree() {
		PersonName name = new PersonName();
		name.setDegree(PERSON_MIDDLE_NAME);
		
		assertThat(personNameTranslator.toFhirResource(name).getExtensionByUrl(FhirConstants.OPENMRS_FHIR_EXT_NAME)
		        .getExtensionByUrl(FhirConstants.OPENMRS_FHIR_EXT_NAME + "#degree"),
		    hasProperty("value", hasProperty("value", equalTo(PERSON_MIDDLE_NAME))));
	}

	@Test
	public void shouldOnlyCreateOneExtensionForExtensibleAttributes() {
		PersonName name = new PersonName();
		name.setFamilyNamePrefix(PERSON_MIDDLE_NAME);
		name.setFamilyNameSuffix(PERSON_MIDDLE_NAME);

		// note that this throws an IllegalArgumentException if more than extension with the same URL occurs
		Extension extension = personNameTranslator.toFhirResource(name).getExtensionByUrl(FhirConstants.OPENMRS_FHIR_EXT_NAME);
		assertThat(extension.getExtension().size(), greaterThan(1));
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
	
	@Test
	public void shouldConvertExtensionToPrefix() {
		HumanName name = new HumanName();
		Extension nameExtension = name.addExtension();
		nameExtension.setUrl(FhirConstants.OPENMRS_FHIR_EXT_NAME);
		nameExtension.addExtension(FhirConstants.OPENMRS_FHIR_EXT_NAME + "#prefix", new StringType(PERSON_MIDDLE_NAME));
		
		assertThat(personNameTranslator.toOpenmrsType(name).getPrefix(), equalTo(PERSON_MIDDLE_NAME));
	}
	
	@Test
	public void shouldConvertExtensionToFamilyNamePrefix() {
		HumanName name = new HumanName();
		Extension nameExtension = name.addExtension();
		nameExtension.setUrl(FhirConstants.OPENMRS_FHIR_EXT_NAME);
		nameExtension.addExtension(FhirConstants.OPENMRS_FHIR_EXT_NAME + "#familyNamePrefix", new StringType(
		        PERSON_MIDDLE_NAME));
		
		assertThat(personNameTranslator.toOpenmrsType(name).getFamilyNamePrefix(), equalTo(PERSON_MIDDLE_NAME));
	}
	
	@Test
	public void shouldConvertExtensionToFamilyName2() {
		HumanName name = new HumanName();
		Extension nameExtension = name.addExtension();
		nameExtension.setUrl(FhirConstants.OPENMRS_FHIR_EXT_NAME);
		nameExtension.addExtension(FhirConstants.OPENMRS_FHIR_EXT_NAME + "#familyName2", new StringType(PERSON_MIDDLE_NAME));
		
		assertThat(personNameTranslator.toOpenmrsType(name).getFamilyName2(), equalTo(PERSON_MIDDLE_NAME));
	}
	
	@Test
	public void shouldConvertExtensionToFamilyNameSuffix() {
		HumanName name = new HumanName();
		Extension nameExtension = name.addExtension();
		nameExtension.setUrl(FhirConstants.OPENMRS_FHIR_EXT_NAME);
		nameExtension.addExtension(FhirConstants.OPENMRS_FHIR_EXT_NAME + "#familyNameSuffix", new StringType(
		        PERSON_MIDDLE_NAME));
		
		assertThat(personNameTranslator.toOpenmrsType(name).getFamilyNameSuffix(), equalTo(PERSON_MIDDLE_NAME));
	}
	
	@Test
	public void shouldConvertExtensionToDegree() {
		HumanName name = new HumanName();
		Extension nameExtension = name.addExtension();
		nameExtension.setUrl(FhirConstants.OPENMRS_FHIR_EXT_NAME);
		nameExtension.addExtension(FhirConstants.OPENMRS_FHIR_EXT_NAME + "#degree", new StringType(PERSON_MIDDLE_NAME));
		
		assertThat(personNameTranslator.toOpenmrsType(name).getDegree(), equalTo(PERSON_MIDDLE_NAME));
	}
	
}
