/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.fhir2.providers.r4;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.openmrs.module.fhir2.api.util.GeneralUtils.inputStreamToString;

import java.io.InputStream;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import lombok.AccessLevel;
import lombok.Getter;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Condition;
import org.hl7.fhir.r4.model.Resource;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.api.ConditionService;
import org.openmrs.api.DiagnosisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletResponse;

public class ConditionProfileRoutingIntegrationTest extends BaseProfileRoutingR4IntegrationTest<ConditionFhirResourceProvider, Condition> {
	
	private static final String CONDITION_PROFILE = openmrsProfile("openmrs-condition");
	
	private static final String DIAGNOSIS_PROFILE = openmrsProfile("openmrs-diagnosis");
	
	private static final String CONDITION_DATA_SET_FILE = "org/openmrs/module/fhir2/api/dao/impl/FhirConditionDaoImplTest_initial_data.xml";
	
	private static final String DIAGNOSIS_DATA_SET_FILE = "org/openmrs/module/fhir2/api/dao/impl/FhirDiagnosisDaoImplTest_initial_dataset.xml";
	
	private static final String JSON_CREATE_CONDITION_DOCUMENT = "org/openmrs/module/fhir2/providers/ConditionWebTest_create.json";
	
	private static final String JSON_CREATE_DIAGNOSIS_DOCUMENT = "org/openmrs/module/fhir2/providers/ConditionDiagnosis_create.json";
	
	private static final String CONDITION_UUID = "2cc6880e-2c46-11e4-9138-a6c5e4d20fb7";
	
	private static final String DIAGNOSIS_UUID = "9f1e6734-4935-4abb-9dda-ace8331d4d2f";
	
	@Getter(AccessLevel.PUBLIC)
	@Autowired
	private ConditionFhirResourceProvider resourceProvider;
	
	@Autowired
	private ConditionService conditionService;
	
	@Autowired
	private DiagnosisService diagnosisService;
	
	@Before
	@Override
	public void setup() throws Exception {
		super.setup();
		executeDataSet(CONDITION_DATA_SET_FILE);
		executeDataSet(DIAGNOSIS_DATA_SET_FILE);
	}
	
	@Test
	public void shouldStampConditionProfileOnConditionBackedCondition() throws Exception {
		// when
		MockHttpServletResponse response = get("/Condition/" + CONDITION_UUID).accept(FhirMediaTypes.JSON).go();
		
		// then
		assertThat(response, isOk());
		assertThat(profilesOf(readResponse(response)), contains(CONDITION_PROFILE));
	}
	
	@Test
	public void shouldStampDiagnosisProfileOnDiagnosisBackedCondition() throws Exception {
		// when
		MockHttpServletResponse response = get("/Condition/" + DIAGNOSIS_UUID).accept(FhirMediaTypes.JSON).go();
		
		// then
		assertThat(response, isOk());
		assertThat(profilesOf(readResponse(response)), contains(DIAGNOSIS_PROFILE));
	}
	
	@Test
	public void shouldRouteSearchToDiagnosisHandlerWhenProfileIsDiagnosisProfile() throws Exception {
		// given
		List<Resource> all = searchAllPages("/Condition");
		assertThat(idsWithProfile(all, CONDITION_PROFILE), is(not(empty())));
		
		// when
		List<Resource> results = searchAllPages("/Condition?_profile=" + DIAGNOSIS_PROFILE);
		
		// then
		assertThat(results, everyItem(hasProfiles(contains(DIAGNOSIS_PROFILE))));
		assertThat(idsOf(results), contains(DIAGNOSIS_UUID));
		assertThat(idsOf(results), equalTo(idsWithProfile(all, DIAGNOSIS_PROFILE)));
	}
	
	@Test
	public void shouldRouteSearchToConditionHandlerWhenProfileIsConditionProfile() throws Exception {
		// given
		Set<String> expected = idsWithProfile(searchAllPages("/Condition"), CONDITION_PROFILE);
		
		// when
		List<Resource> results = searchAllPages("/Condition?_profile=" + CONDITION_PROFILE);
		
		// then
		assertThat(results, is(not(empty())));
		assertThat(results, everyItem(hasProfiles(contains(CONDITION_PROFILE))));
		assertThat(idsOf(results), hasItem(CONDITION_UUID));
		assertThat(idsOf(results), equalTo(expected));
	}
	
	@Test
	public void shouldRouteSearchToBothHandlersWhenBothProfilesAreRequested() throws Exception {
		// given
		Set<String> expected = idsOf(searchAllPages("/Condition"));
		
		// when
		List<Resource> results = searchAllPages("/Condition?_profile=" + CONDITION_PROFILE + "," + DIAGNOSIS_PROFILE);
		
		// then
		assertThat(idsOf(results), equalTo(expected));
		assertThat(idsWithProfile(results, CONDITION_PROFILE), hasItem(CONDITION_UUID));
		assertThat(idsWithProfile(results, DIAGNOSIS_PROFILE), contains(DIAGNOSIS_UUID));
	}
	
	@Test
	public void shouldFanOutAsUsualWhenProfileMatchesNoHandler() throws Exception {
		// given
		Set<String> expected = idsOf(searchAllPages("/Condition"));
		
		// when
		List<Resource> results = searchAllPages("/Condition?_profile=" + UNKNOWN_PROFILE);
		
		// then
		assertThat(idsOf(results), equalTo(expected));
	}
	
	@Test
	public void shouldKeepRoutingToConditionHandlerAcrossPages() throws Exception {
		// given
		int pageSize = 2;
		Set<String> expected = idsWithProfile(searchAllPages("/Condition"), CONDITION_PROFILE);
		assertThat(expected.size(), greaterThan(pageSize));
		
		// when
		Bundle firstPage = searchPage("/Condition?_count=" + pageSize + "&_profile=" + CONDITION_PROFILE);
		List<Resource> results = searchAllPages("/Condition?_count=" + pageSize + "&_profile=" + CONDITION_PROFILE);
		
		// then
		assertThat(firstPage.getTotal(), equalTo(expected.size()));
		assertThat(firstPage.getLink(Bundle.LINK_NEXT), notNullValue());
		assertThat(results, everyItem(hasProfiles(contains(CONDITION_PROFILE))));
		assertThat(results, hasSize(expected.size()));
		assertThat(idsOf(results), equalTo(expected));
	}
	
	@Test
	public void shouldCreateDiagnosisWhenMetaProfileIsDiagnosisProfile() throws Exception {
		// given
		Condition condition = readDocument(JSON_CREATE_DIAGNOSIS_DOCUMENT);
		condition.getMeta().addProfile(DIAGNOSIS_PROFILE);
		
		// when
		MockHttpServletResponse response = post("/Condition").accept(FhirMediaTypes.JSON).jsonContent(toJson(condition))
		        .go();
		
		// then
		assertThat(response, isCreated());
		Condition created = readResponse(response);
		String uuid = created.getIdElement().getIdPart();
		assertThat(profilesOf(created), contains(DIAGNOSIS_PROFILE));
		assertThat(diagnosisService.getDiagnosisByUuid(uuid), notNullValue());
		assertThat(conditionService.getConditionByUuid(uuid), nullValue());
	}
	
	@Test
	public void shouldCreateConditionWhenMetaProfileIsConditionProfile() throws Exception {
		// given
		Condition condition = readDocument(JSON_CREATE_CONDITION_DOCUMENT);
		condition.setId((String) null);
		condition.getMeta().addProfile(CONDITION_PROFILE);
		
		// when
		MockHttpServletResponse response = post("/Condition").accept(FhirMediaTypes.JSON).jsonContent(toJson(condition))
		        .go();
		
		// then
		assertThat(response, isCreated());
		Condition created = readResponse(response);
		String uuid = created.getIdElement().getIdPart();
		assertThat(profilesOf(created), contains(CONDITION_PROFILE));
		assertThat(conditionService.getConditionByUuid(uuid), notNullValue());
		assertThat(diagnosisService.getDiagnosisByUuid(uuid), nullValue());
	}
	
	@Test
	public void shouldCreateConditionFromDiagnosisShapedBodyWhenMetaProfileIsConditionProfile() throws Exception {
		// given
		Condition condition = readDocument(JSON_CREATE_DIAGNOSIS_DOCUMENT);
		condition.getMeta().addProfile(CONDITION_PROFILE);
		
		// when
		MockHttpServletResponse response = post("/Condition").accept(FhirMediaTypes.JSON).jsonContent(toJson(condition))
		        .go();
		
		// then
		assertThat(response, isCreated());
		Condition created = readResponse(response);
		String uuid = created.getIdElement().getIdPart();
		assertThat(profilesOf(created), contains(CONDITION_PROFILE));
		assertThat(conditionService.getConditionByUuid(uuid), notNullValue());
		assertThat(diagnosisService.getDiagnosisByUuid(uuid), nullValue());
	}
	
	private Condition readDocument(String path) throws Exception {
		String json;
		try (InputStream is = getClass().getClassLoader().getResourceAsStream(path)) {
			Objects.requireNonNull(is);
			json = inputStreamToString(is, UTF_8);
		}
		
		return getFhirContext().newJsonParser().parseResource(Condition.class, json);
	}
}
