/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.fhir2.api.impl;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hl7.fhir.r4.model.Patient.SP_IDENTIFIER;
import static org.mockito.Mockito.when;
import static org.openmrs.module.fhir2.FhirConstants.ENCOUNTER;
import static org.openmrs.module.fhir2.FhirConstants.PATIENT;
import static org.openmrs.module.fhir2.FhirConstants.PRACTITIONER;
import static org.openmrs.module.fhir2.api.translators.impl.ImmunizationTranslatorImpl.CIEL_1410;
import static org.openmrs.module.fhir2.api.translators.impl.ImmunizationTranslatorImpl.CIEL_1418;
import static org.openmrs.module.fhir2.api.translators.impl.ImmunizationTranslatorImpl.CIEL_1419;
import static org.openmrs.module.fhir2.api.translators.impl.ImmunizationTranslatorImpl.CIEL_1420;
import static org.openmrs.module.fhir2.api.translators.impl.ImmunizationTranslatorImpl.CIEL_165907;
import static org.openmrs.module.fhir2.api.translators.impl.ImmunizationTranslatorImpl.CIEL_984;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import ca.uhn.fhir.rest.api.server.IBundleProvider;
import ca.uhn.fhir.rest.param.ReferenceAndListParam;
import ca.uhn.fhir.rest.param.ReferenceOrListParam;
import ca.uhn.fhir.rest.param.ReferenceParam;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.DateTimeType;
import org.hl7.fhir.r4.model.DateType;
import org.hl7.fhir.r4.model.Immunization;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.Obs;
import org.openmrs.Provider;
import org.openmrs.api.ObsService;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.TestFhirSpringConfiguration;
import org.openmrs.module.fhir2.api.FhirGlobalPropertyService;
import org.openmrs.module.fhir2.api.util.ImmunizationObsGroupHelper;
import org.openmrs.test.BaseModuleContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

@ContextConfiguration(classes = TestFhirSpringConfiguration.class, inheritLocations = false)
public class FhirImmunizationServiceImplTest extends BaseModuleContextSensitiveTest {
	
	private static final String IMMUNIZATIONS_METADATA_XML = "org/openmrs/module/fhir2/Immunization_metadata.xml";
	
	private static final String IMMUNIZATIONS_INITIAL_DATA_XML = "org/openmrs/module/fhir2/api/services/impl/FhirImmunizationService_initial_data.xml";
	
	private static final String PRACTITIONER_INITIAL_DATA_XML = "org/openmrs/module/fhir2/api/dao/impl/FhirPractitionerDaoImplTest_initial_data.xml";
	
	@Autowired
	private FhirImmunizationServiceImpl service;
	
	@Autowired
	private ObsService obsService;
	
	@Autowired
	private FhirGlobalPropertyService globalPropertyService;
	
	@Autowired
	private ImmunizationObsGroupHelper helper;
	
	@Before
	public void setup() throws Exception {
		when(globalPropertyService.getGlobalProperty(FhirConstants.IMMUNIZATIONS_ENCOUNTER_TYPE_PROPERTY))
		        .thenReturn("29c02aff-9a93-46c9-bf6f-48b552fcb1fa");
		when(globalPropertyService.getGlobalProperty(FhirConstants.ADMINISTERING_ENCOUNTER_ROLE_PROPERTY))
		        .thenReturn("546cce2d-6d58-4097-ba92-206c1a2a0462");
		
		executeDataSet(IMMUNIZATIONS_METADATA_XML);
		executeDataSet(IMMUNIZATIONS_INITIAL_DATA_XML);
		executeDataSet(PRACTITIONER_INITIAL_DATA_XML);
	}
	
	/**
	 * Asserts the commons parts between the grouping obs and its members.
	 *
	 * @param obs The obs to assert.
	 * @param patientUuid The UUID of the patient to be verified.
	 * @param visitUuid The UUID of the visit to be verified.
	 * @param providerUuid The UUID of the provider to be verified.
	 */
	private void assertObsCommons(Obs obs, String patientUuid, String visitUuid, String providerUuid) {
		assertThat(obs.getPerson().getUuid(), is(patientUuid));
		assertThat(obs.getEncounter().getEncounterType(), is(helper.getImmunizationsEncounterType()));
		assertThat(obs.getEncounter().getVisit().getUuid(), is(visitUuid));
		
		Set<Provider> providers = obs.getEncounter().getProvidersByRole(helper.getAdministeringEncounterRole());
		
		assertThat(providers.size(), is(1));
		assertThat(providers.iterator().next().getUuid(), is(providerUuid));
	}
	
	@Test
	public void saveImmunization_shouldCreateEncounterAndObsGroupWhenNewImmunization() {
		// setup
		FhirContext ctx = FhirContext.forR4();
		IParser parser = ctx.newJsonParser();
		Immunization newImmunization = parser.parseResource(Immunization.class, "{\n"
		        + "  \"resourceType\": \"Immunization\",\n" + "  \"status\": \"completed\",\n" + "  \"vaccineCode\": {\n"
		        + "    \"coding\": [\n" + "      {\n" + "        \"code\": \"15f83cd6-64e9-4e06-a5f9-364d3b14a43d\",\n"
		        + "        \"display\": \"Aspirin as a vaccine\"\n" + "      }\n" + "    ]\n" + "  },\n"
		        + "  \"patient\": {\n" + "    \"reference\": \"Patient/a7e04421-525f-442f-8138-05b619d16def\",\n"
		        + "    \"type\": \"Patient\"\n" + "  },\n" + "  \"encounter\": {\n"
		        + "    \"reference\": \"Encounter/7d8c1980-6b78-11e0-93c3-18a905e044dc\",\n"
		        + "    \"type\": \"Encounter\"\n" + "  },\n" + "  \"occurrenceDateTime\": \"2020-07-08T18:30:00.000Z\",\n"
		        + "  \"manufacturer\": {\n" + "    \"display\": \"Acme\"\n" + "  },\n" + "  \"lotNumber\": \"FOO1234\",\n"
		        + "  \"expirationDate\": \"2022-07-31T18:30:00.000Z\",\n" + "  \"performer\": [\n" + "    {\n"
		        + "      \"actor\": {\n" + "        \"reference\": \"Practitioner/f9badd80-ab76-11e2-9e96-0800200c9a66\",\n"
		        + "        \"type\": \"Practitioner\"\n" + "      }\n" + "    }\n" + "  ],\n" + "  \"protocolApplied\": [\n"
		        + "    {\n" + "      \"doseNumberPositiveInt\": 2,\n" + "      \"series\": \"Dose 2\"\n" + "    }\n"
		        + "  ]\n" + "}");
		
		// replay
		Immunization savedImmunization = service.create(newImmunization);
		Obs obs = obsService.getObsByUuid(savedImmunization.getId());
		
		// verify
		helper.validateImmunizationObsGroup(obs);
		assertObsCommons(obs, "a7e04421-525f-442f-8138-05b619d16def", "7d8c1980-6b78-11e0-93c3-18a905e044dc",
		    "f9badd80-ab76-11e2-9e96-0800200c9a66");
		
		obs.getGroupMembers().forEach(o -> assertObsCommons(o, "a7e04421-525f-442f-8138-05b619d16def",
		    "7d8c1980-6b78-11e0-93c3-18a905e044dc", "f9badd80-ab76-11e2-9e96-0800200c9a66"));
		
		Map<String, Obs> members = helper.getObsMembersMap(obs);
		assertThat(members.get(CIEL_984).getValueCoded().getUuid(), is("15f83cd6-64e9-4e06-a5f9-364d3b14a43d"));
		assertThat(members.get(CIEL_1410).getValueDatetime(),
		    equalTo(new DateTimeType("2020-07-08T18:30:00.000Z").getValue()));
		assertThat(members.get(CIEL_1418).getValueNumeric(), equalTo(2.0));
		assertThat(members.get(CIEL_1419).getValueText(), is("Acme"));
		assertThat(members.get(CIEL_1420).getValueText(), is("FOO1234"));
		assertThat(members.get(CIEL_165907).getValueDatetime(),
		    equalTo(new DateTimeType("2022-07-31T18:30:00.000Z").getValue()));
	}
	
	@Test
	public void updateImmunization_shouldUpdateImmunizationAccordingly() {
		// setup
		FhirContext ctx = FhirContext.forR4();
		IParser parser = ctx.newJsonParser();
		Immunization updatedImmunization = parser.parseResource(Immunization.class,
		    "{\n" + "  \"resourceType\": \"Immunization\",\n" + "  \"id\": \"9353776b-dead-4588-8723-d687197d8438\",\n"
		            + "  \"status\": \"completed\",\n" + "  \"vaccineCode\": {\n" + "    \"coding\": [\n" + "      {\n"
		            + "        \"code\": \"d144d24f-6913-4b63-9660-a9108c2bebef\",\n"
		            + "        \"display\": \"STAVUDINE LAMIVUDINE AND NEVIRAPINE\"\n" + "      }\n" + "    ]\n" + "  },\n"
		            + "  \"patient\": {\n" + "    \"reference\": \"Patient/a7e04421-525f-442f-8138-05b619d16def\",\n"
		            + "    \"type\": \"Patient\"\n" + "  },\n" + "  \"encounter\": {\n"
		            + "    \"reference\": \"Encounter/7d8c1980-6b78-11e0-93c3-18a905e044dc\",\n"
		            + "    \"type\": \"Encounter\"\n" + "  },\n"
		            + "  \"occurrenceDateTime\": \"2020-07-08T20:30:00+02:00\",\n" + "  \"manufacturer\": {\n"
		            + "    \"display\": \"Pharma Inc.\"\n" + "  },\n" + "  \"lotNumber\": \"YU765YT-1\",\n"
		            + "  \"expirationDate\": \"2020-10-08\",\n" + "  \"performer\": [\n" + "    {\n" + "      \"actor\": {\n"
		            + "        \"reference\": \"Practitioner/f9badd80-ab76-11e2-9e96-0800200c9a66\",\n"
		            + "        \"type\": \"Practitioner\"\n" + "      }\n" + "    }\n" + "  ],\n"
		            + "  \"protocolApplied\": [\n" + "    {\n" + "      \"doseNumberPositiveInt\": 4\n" + "    }\n" + "  ]\n"
		            + "}");
		
		// replay
		Immunization savedImmunization = service.update("9353776b-dead-4588-8723-d687197d8438", updatedImmunization);
		Obs obs = obsService.getObsByUuid(savedImmunization.getId());
		
		// verify
		helper.validateImmunizationObsGroup(obs);
		assertObsCommons(obs, "a7e04421-525f-442f-8138-05b619d16def", "7d8c1980-6b78-11e0-93c3-18a905e044dc",
		    "f9badd80-ab76-11e2-9e96-0800200c9a66");
		
		obs.getGroupMembers().forEach(o -> assertObsCommons(o, "a7e04421-525f-442f-8138-05b619d16def",
		    "7d8c1980-6b78-11e0-93c3-18a905e044dc", "f9badd80-ab76-11e2-9e96-0800200c9a66"));
		
		Map<String, Obs> members = helper.getObsMembersMap(obs);
		assertThat(members.get(CIEL_984).getValueCoded().getUuid(), is("d144d24f-6913-4b63-9660-a9108c2bebef"));
		assertThat(new DateTimeType(members.get(CIEL_1410).getValueDatetime()).toHumanDisplayLocalTimezone(),
		    equalTo(new DateTimeType("2020-07-08T20:30:00+02:00").toHumanDisplayLocalTimezone()));
		assertThat(members.get(CIEL_1418).getValueNumeric(), equalTo(4.0));
		assertThat(members.get(CIEL_1419).getValueText(), is("Pharma Inc."));
		assertThat(members.get(CIEL_1420).getValueText(), is("YU765YT-1"));
		assertThat(members.get(CIEL_165907).getValueDate(), equalTo(new DateType("2020-10-08").getValue()));
	}
	
	@Test
	public void searchImmunizations_shouldFetchImmunizationsByPatientIdentifier() {
		// setup
		ReferenceAndListParam param = new ReferenceAndListParam();
		param.addValue(new ReferenceOrListParam().add(new ReferenceParam(SP_IDENTIFIER, "12345K")));
		
		// replay
		List<Immunization> immunizations = get(service.searchImmunizations(param, null));
		
		// verify (in chronological order)
		assertThat(immunizations.size(), is(2));
		
		{
			Immunization immunization = immunizations.get(0);
			Coding coding = immunization.getVaccineCode().getCoding().get(0);
			assertThat(coding.getDisplay(), is("STAVUDINE LAMIVUDINE AND NEVIRAPINE"));
			assertThat(coding.getCode(), is("d144d24f-6913-4b63-9660-a9108c2bebef"));
			assertThat(immunization.getPatient().getReference(), is("Patient/a7e04421-525f-442f-8138-05b619d16def"));
			assertThat(immunization.getPatient().getType(), is(PATIENT));
			assertThat(immunization.getEncounter().getReference(), is("Encounter/7d8c1980-6b78-11e0-93c3-18a905e044dc"));
			assertThat(immunization.getEncounter().getType(), is(ENCOUNTER));
			assertThat(immunization.getOccurrenceDateTimeType().getValue().toString(), is("2020-07-08 20:30:00.0"));
			assertThat(immunization.getManufacturer().getDisplay(), is("Pharma Inc."));
			assertThat(immunization.getLotNumber(), is("YU765YT"));
			assertThat(immunization.getExpirationDate().toString(), is("2022-01-01 12:00:00.0"));
			assertThat(immunization.getPerformer().size(), is(1));
			assertThat(immunization.getPerformer().get(0).getActor().getReference(),
			    is("Practitioner/f9badd80-ab76-11e2-9e96-0800200c9a66"));
			assertThat(immunization.getPerformer().get(0).getActor().getType(), is(PRACTITIONER));
			assertThat(immunization.getProtocolApplied().size(), is(1));
			assertThat(immunization.getProtocolApplied().get(0).getDoseNumberPositiveIntType().getValue(), is(3));
		}
		
		{
			Immunization immunization = immunizations.get(1);
			Coding coding = immunization.getVaccineCode().getCoding().get(0);
			assertThat(coding.getDisplay(), is("COUGH SYRUP"));
			assertThat(coding.getCode(), is("0cbe2ed3-cd5f-4f46-9459-26127c9265ab"));
			assertThat(immunization.getPatient().getReference(), is("Patient/a7e04421-525f-442f-8138-05b619d16def"));
			assertThat(immunization.getPatient().getType(), is(PATIENT));
			assertThat(immunization.getEncounter().getReference(), is("Encounter/7d8c1980-6b78-11e0-93c3-18a905e044dc"));
			assertThat(immunization.getEncounter().getType(), is(ENCOUNTER));
			assertThat(immunization.getOccurrenceDateTimeType().getValue().toString(), is("2020-06-08 20:30:00.0"));
			assertThat(immunization.getManufacturer().getDisplay(), is("Biotech Ltd"));
			assertThat(immunization.getLotNumber(), is("WV654XU"));
			assertThat(immunization.getExpirationDate().toString(), is("2023-01-01 12:00:00.0"));
			assertThat(immunization.getPerformer().size(), is(1));
			assertThat(immunization.getPerformer().get(0).getActor().getReference(),
			    is("Practitioner/f9badd80-ab76-11e2-9e96-0800200c9a66"));
			assertThat(immunization.getPerformer().get(0).getActor().getType(), is(PRACTITIONER));
			assertThat(immunization.getProtocolApplied().size(), is(1));
			assertThat(immunization.getProtocolApplied().get(0).getDoseNumberPositiveIntType().getValue(), is(11));
		}
	}
	
	private List<Immunization> get(IBundleProvider results) {
		return results.getResources(0, results.size()).stream().filter(it -> it instanceof Immunization)
		        .map(it -> (Immunization) it).collect(Collectors.toList());
	}
	
}
