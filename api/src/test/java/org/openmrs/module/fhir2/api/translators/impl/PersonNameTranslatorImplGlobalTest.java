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

import java.util.Arrays;
import java.util.Collections;

import org.hl7.fhir.r4.model.HumanName;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.PersonName;
import org.openmrs.api.context.ServiceContext;
import org.openmrs.layout.name.NameSupport;
import org.openmrs.layout.name.NameTemplate;
import org.openmrs.module.fhir2.TestFhirSpringConfiguration;
import org.openmrs.serialization.SerializationException;
import org.openmrs.serialization.SimpleXStreamSerializer;
import org.openmrs.test.BaseModuleContextSensitiveTest;
import org.springframework.test.context.ContextConfiguration;

@ContextConfiguration(classes = TestFhirSpringConfiguration.class, inheritLocations = false)
public class PersonNameTranslatorImplGlobalTest extends BaseModuleContextSensitiveTest {
	
	private static final String PERSON_GIVEN_NAME = "Jean Claude";
	
	private static final String PERSON_MIDDLE_NAME = "Wilhelm";
	
	private static final String PERSON_FAMILY_NAME = "van Damme";
	
	private static final String NAME_FORMAT_LAYOUT_PROPERTY_NAME = "layout.name.format";
	
	private PersonNameTranslatorImpl personNameTranslator;
	
	private String originalNameFormatLayoutPropertyValue;
	
	@Before
	public void setup() {
		personNameTranslator = new PersonNameTranslatorImpl();
		originalNameFormatLayoutPropertyValue = ServiceContext.getInstance().getAdministrationService()
		        .getGlobalProperty(NAME_FORMAT_LAYOUT_PROPERTY_NAME);
		ServiceContext.getInstance().getAdministrationService().setGlobalProperty(NAME_FORMAT_LAYOUT_PROPERTY_NAME, "test");
	}
	
	@After
	public void teardown() {
		ServiceContext.getInstance().getAdministrationService().setGlobalProperty(NAME_FORMAT_LAYOUT_PROPERTY_NAME,
		    originalNameFormatLayoutPropertyValue);
	}
	
	@Test
	public void shouldUseDefaultNameTemplateToSetNameText() throws SerializationException {
		NameSupport nameSupportInstance = new NameSupport();
		NameTemplate customNameTemplate = new SimpleXStreamSerializer()
		        .deserialize("<org.openmrs.layout.name.NameTemplate>\n" + "  <codeName>test</codeName>\n"
		                + "  <displayName>Test Name Format</displayName>\n" + "  <nameMappings class=\"properties\">\n"
		                + "    <property name=\"givenName\" value=\"PersonName.givenName\"/>\n"
		                + "    <property name=\"middleName\" value=\"PersonName.middleName\"/>\n"
		                + "    <property name=\"familyName\" value=\"PersonName.familyName\"/>\n" + "  </nameMappings>\n"
		                + "  <sizeMappings class=\"properties\">\n" + "    <property name=\"givenName\" value=\"25\"/>\n"
		                + "    <property name=\"middleName\" value=\"25\"/>\n"
		                + "    <property name=\"familyName\" value=\"25\"/>\n" + "  </sizeMappings>\n"
		                + "  <lineByLineFormat>\n" + "    <string>givenName</string>\n" + "    <string>familyName</string>\n"
		                + "    <string>middleName</string>\n" + "  </lineByLineFormat>\n" + "  <requiredElements>\n"
		                + "    <string>givenName</string>\n" + "    <string>familyName</string>\n"
		                + "  </requiredElements>\n" + "</org.openmrs.layout.name.NameTemplate>",
		            NameTemplate.class);
		nameSupportInstance.setLayoutTemplates(Collections.singletonList(customNameTemplate));
		nameSupportInstance.setSpecialTokens(Arrays.asList("prefix", "givenName", "middleName", "familyNamePrefix",
		    "familyNameSuffix", "familyName2", "familyName", "degree"));
		
		PersonName name = new PersonName();
		name.setGivenName(PERSON_GIVEN_NAME);
		name.setMiddleName(PERSON_MIDDLE_NAME);
		name.setFamilyName(PERSON_FAMILY_NAME);
		
		HumanName fhirName = personNameTranslator.toFhirResource(name);
		
		assertThat(fhirName, notNullValue());
		assertThat(fhirName.getTextElement(), notNullValue());
		assertThat(fhirName.getTextElement().getValue(),
		    equalTo(PERSON_GIVEN_NAME + " " + PERSON_FAMILY_NAME + " " + PERSON_MIDDLE_NAME));
	}
}
