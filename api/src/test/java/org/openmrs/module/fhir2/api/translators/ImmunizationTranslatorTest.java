/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.fhir2.api.translators;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import org.hl7.fhir.r4.model.Immunization;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.Obs;
import org.openmrs.api.ConceptService;
import org.openmrs.module.fhir2.TestFhirSpringConfiguration;
import org.openmrs.test.BaseModuleContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

@ContextConfiguration(classes = TestFhirSpringConfiguration.class, inheritLocations = false)
public class ImmunizationTranslatorTest extends BaseModuleContextSensitiveTest {
	
	private static final String IMMUNIZATIONS_METADATA_XML = "org/openmrs/module/fhir2/api/translators/ImmunizationTranslator_metadata.xml";
	
	private static final String PRACTITIONER_INITIAL_DATA_XML = "org/openmrs/module/fhir2/api/dao/impl/FhirPractitionerDaoImplTest_initial_data.xml";
	
	@Autowired
	private ConceptService conceptService;
	
	@Autowired
	private ImmunizationTranslator translator;
	
	@Before
	public void setup() throws Exception {
		executeDataSet(IMMUNIZATIONS_METADATA_XML);
		executeDataSet(PRACTITIONER_INITIAL_DATA_XML);
	}
	
	@Test
	public void toOpenmrsType_shouldCreateImmunizationObsGroup() {
		
		// setup
		FhirContext ctx = FhirContext.forR4();
		IParser parser = ctx.newJsonParser();
		
		String immunizationJson = "{\n" + "  \"resourceType\": \"Immunization\",\n" + "  \"status\": \"completed\",\n"
		        + "  \"vaccineCode\": {\n" + "    \"coding\": [\n" + "      {\n" + "        \"system\": \"\",\n"
		        + "        \"code\": \"c3922d6a-ddd8-41b9-84e1-550255b75492\",\n" + "        \"display\": \"Cholera\"\n"
		        + "      }\n" + "    ]\n" + "  },\n" + "  \"patient\": {\n" + "    \"type\": \"Patient\",\n"
		        + "    \"reference\": \"Patient/da7f524f-27ce-4bb2-86d6-6d1d05312bd5\"\n" + "  },\n" + "  \"encounter\": {\n"
		        + "    \"type\": \"Encounter\",\n"
		        + "    \"reference\": \"Encounter/6a2dcada-3972-4c4b-a1ae-6d6c2646fe5b\"\n" + "  },\n"
		        + "  \"occurrenceDateTime\": \"2020-06-27T18:30:00.000Z\",\n"
		        + "  \"expirationDate\": \"2020-07-21T18:30:00.000Z\",\n" + "  \"location\": {\n"
		        + "    \"type\": \"Location\",\n" + "    \"reference\": \"Location/8d6c993e-c2cc-11de-8d13-0010c6dffd0f\"\n"
		        + "  },\n" + "  \"performer\": [\n" + "    {\n" + "      \"actor\": {\n"
		        + "        \"type\": \"Practitioner\",\n"
		        + "        \"reference\": \"Practitioner/f9badd80-ab76-11e2-9e96-0800200c9a66\"\n" + "      }\n" + "    }\n"
		        + "  ],\n" + "  \"manufacturer\": {\n" + "    \"display\": \"XYTZZ\"\n" + "  },\n"
		        + "  \"lotNumber\": \"12345\",\n" + "  \"protocolApplied\": [\n" + "    {\n"
		        + "      \"doseNumberPositiveInt\": \"2\",\n" + "      \"series\": \"5 Months\"\n" + "    }\n" + "  ]\n"
		        + "}";
		Immunization immunization = parser.parseResource(Immunization.class, immunizationJson);
		
		// replay
		Obs obs = translator.toOpenmrsType(immunization);
		
	}
	
}
