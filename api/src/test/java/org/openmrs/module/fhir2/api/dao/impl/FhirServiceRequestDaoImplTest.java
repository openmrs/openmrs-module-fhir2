/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.fhir2.api.dao.impl;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertEquals;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import ca.uhn.fhir.rest.param.HasAndListParam;
import ca.uhn.fhir.rest.param.HasOrListParam;
import ca.uhn.fhir.rest.param.HasParam;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.hibernate.SessionFactory;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.OrderType;
import org.openmrs.TestOrder;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.TestFhirSpringConfiguration;
import org.openmrs.module.fhir2.api.search.param.SearchParameterMap;
import org.openmrs.test.BaseModuleContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;

@ContextConfiguration(classes = TestFhirSpringConfiguration.class, inheritLocations = false)
public class FhirServiceRequestDaoImplTest extends BaseModuleContextSensitiveTest {
	
	private static final String TEST_ORDER_UUID = "7d96f25c-4949-4f72-9931-d808fbc226de";
	
	private static final String OTHER_ORDER_UUID = "02b9d1e4-7619-453e-bd6b-c32286f861df";
	
	private static final String ORDER_WITH_OBSERVATION_CATEGORY_LABORATORY_UUID = "4364996e-450c-42dc-84a7-1c205f34b10b";
	
	private static final String ORDER_WITH_OBSERVATION_CATEGORY_PROCEDURE_UUID = "6233f0f9-17eb-471c-8131-440a20dc25c4";
	
	private static final String ORDER_WITH_CODED_VALUE_UUID = "b5bb2ba7-b17d-473e-907b-de7ad5374804";
	
	private static final String ORDER_WITH_DATETIME_VALUE_UUID = "a5a0f4a2-8d32-4527-ae8e-2be8cb76a899";
	
	private static final String ORDER_WITH_TEXT_VALUE_UUID = "fb5e9e5f-1800-4c96-b18e-0d89cf20219";
	
	private static final String ORDER_WITH_NUMERIC_VALUE_UUID = "6b870cea-03c2-487d-918c-057a6bdce8c";
	
	private static final String ORDER_WITH_PROVIDER_UUID = "7992afb9-437a-4609-bcaa-81d6cb2fe40";
	
	private static final String ORDER_WITH_STATUS_UUID = "4bd2b016-a773-4d76-b9e1-1c70a80e6a83";
	
	private static final String ORDER_WITH_GROUP_MEMBERS_UUID = "fe00bbbc-5e31-475d-95a1-2facb63bcdae";
	
	private static final String WRONG_UUID = "38c53063-450a-47be-802c-e6270d2e6c69";
	
	private static final String TEST_ORDER_INITIAL_DATA = "org/openmrs/module/fhir2/api/dao/impl/FhirServiceRequestDaoImplTest_initial_data.xml";
	
	private FhirServiceRequestDaoImpl dao;
	
	@Autowired
	@Qualifier("sessionFactory")
	private SessionFactory sessionFactory;
	
	@Before
	public void setup() throws Exception {
		executeDataSet(TEST_ORDER_INITIAL_DATA);
		
		dao = new FhirServiceRequestDaoImpl();
		dao.setSessionFactory(sessionFactory);
	}
	
	@Test
	public void shouldRetrieveTestOrderByUuid() {
		TestOrder result = dao.get(TEST_ORDER_UUID);
		
		assertThat(result, notNullValue());
		assertThat(result, instanceOf(TestOrder.class));
		assertThat(result.getOrderType().getUuid(), equalTo(OrderType.TEST_ORDER_TYPE_UUID));
		assertThat(result.getUuid(), equalTo(TEST_ORDER_UUID));
	}
	
	@Test
	public void shouldReturnNullIfUuidNotFound() {
		TestOrder result = dao.get(WRONG_UUID);
		assertThat(result, nullValue());
	}
	
	@Test
	public void shouldReturnNullIfUuidIsNotValidTestOrder() {
		TestOrder result = dao.get(OTHER_ORDER_UUID);
		assertThat(result, nullValue());
	}
	
	@Test
	public void shouldApplyHasConstraintsForObservationsBasedOnAnyCorrectly() {
		runHasObservationBasedOnTest(
		    null, null, new String[] { ORDER_WITH_OBSERVATION_CATEGORY_LABORATORY_UUID,
		            ORDER_WITH_OBSERVATION_CATEGORY_PROCEDURE_UUID, ORDER_WITH_TEXT_VALUE_UUID },
		    new String[] { WRONG_UUID });
	}
	
	@Test
	public void shouldApplyHasConstraintsForObservationsBasedOnAnyCategoryCorrectly() {
		runHasObservationBasedOnTest("category", null,
		    new String[] { ORDER_WITH_OBSERVATION_CATEGORY_LABORATORY_UUID, ORDER_WITH_OBSERVATION_CATEGORY_PROCEDURE_UUID },
		    new String[] { WRONG_UUID });
	}
	
	@Test
	public void shouldApplyHasConstraintsForObservationsBasedOnLaboratoryCategoryCorrectly() {
		runHasObservationBasedOnTest("category", "laboratory",
		    new String[] { ORDER_WITH_OBSERVATION_CATEGORY_LABORATORY_UUID },
		    new String[] { WRONG_UUID, ORDER_WITH_OBSERVATION_CATEGORY_PROCEDURE_UUID });
	}
	
	@Test
	public void shouldApplyHasConstraintsForObservationsBasedOnUnknownCategoryCorrectly() {
		runHasObservationBasedOnTest("category", "__UnknownCategory", new String[] {}, new String[] { WRONG_UUID,
		        ORDER_WITH_OBSERVATION_CATEGORY_PROCEDURE_UUID, ORDER_WITH_OBSERVATION_CATEGORY_LABORATORY_UUID });
	}
	
	@Test
	public void shouldApplyHasConstraintsForObservationsBasedOnAnyDateCorrectly() {
		runHasObservationBasedOnTest("date", null, new String[] { ORDER_WITH_OBSERVATION_CATEGORY_LABORATORY_UUID },
		    new String[] { WRONG_UUID });
	}
	
	@Test
	public void shouldApplyHasConstraintsForObservationsBasedOnSpecificDateCorrectly() {
		runHasObservationBasedOnTest("date", toFhirDateFormat("1998-07-01 00:00:00.0"),
		    new String[] { ORDER_WITH_OBSERVATION_CATEGORY_LABORATORY_UUID }, new String[] { WRONG_UUID });
	}
	
	@Test
	public void shouldApplyHasConstraintsForObservationsBasedOnAnySubjectCorrectly() {
		runHasObservationBasedOnTest("subject", null, new String[] { ORDER_WITH_OBSERVATION_CATEGORY_LABORATORY_UUID },
		    new String[] { WRONG_UUID });
	}
	
	@Test
	public void shouldApplyHasConstraintsForObservationsBasedOnSpecificSubjectCorrectly() {
		runHasObservationBasedOnTest("subject", "5946f880-b197-400b-9caa-a3c661d23041",
		    new String[] { ORDER_WITH_OBSERVATION_CATEGORY_LABORATORY_UUID }, new String[] { WRONG_UUID });
	}
	
	@Test
	public void shouldApplyHasConstraintsForObservationsBasedOnAnyPatientCorrectly() {
		runHasObservationBasedOnTest("patient", null, new String[] { ORDER_WITH_OBSERVATION_CATEGORY_LABORATORY_UUID },
		    new String[] { WRONG_UUID });
	}
	
	@Test
	public void shouldApplyHasConstraintsForObservationsBasedOnSpecificPatientCorrectly() {
		runHasObservationBasedOnTest("patient", "5946f880-b197-400b-9caa-a3c661d23041",
		    new String[] { ORDER_WITH_OBSERVATION_CATEGORY_LABORATORY_UUID }, new String[] { WRONG_UUID });
	}
	
	@Test
	public void shouldApplyHasConstraintsForObservationsBasedOnAnyValueConceptCorrectly() {
		runHasObservationBasedOnTest("value-concept", null, new String[] { ORDER_WITH_CODED_VALUE_UUID },
		    new String[] { WRONG_UUID, ORDER_WITH_DATETIME_VALUE_UUID });
	}
	
	@Test
	public void shouldApplyHasConstraintsForObservationsBasedOnSpecificValueConceptCorrectly() {
		runHasObservationBasedOnTest("value-concept", "8d492026-c2cc-11de-8d13-0010c6dffd0f",
		    new String[] { ORDER_WITH_CODED_VALUE_UUID }, new String[] { WRONG_UUID, ORDER_WITH_DATETIME_VALUE_UUID });
	}
	
	@Test
	public void shouldApplyHasConstraintsForObservationsBasedOnAnyValueDateCorrectly() {
		runHasObservationBasedOnTest("value-date", null, new String[] { ORDER_WITH_DATETIME_VALUE_UUID },
		    new String[] { WRONG_UUID, ORDER_WITH_CODED_VALUE_UUID });
	}
	
	@Test
	public void shouldApplyHasConstraintsForObservationsBasedOnSpecificValueDateCorrectly() {
		runHasObservationBasedOnTest("value-date", toFhirDateFormat("1998-07-01 00:00:00.0"),
		    new String[] { ORDER_WITH_DATETIME_VALUE_UUID }, new String[] { WRONG_UUID, ORDER_WITH_CODED_VALUE_UUID });
	}
	
	@Test
	public void shouldApplyHasConstraintsForObservationsBasedOnAnyHasMemberCorrectly() {
		runHasObservationBasedOnTest("has-member", null, new String[] { ORDER_WITH_GROUP_MEMBERS_UUID },
		    new String[] { WRONG_UUID, ORDER_WITH_CODED_VALUE_UUID });
	}
	
	@Test
	public void shouldApplyHasConstraintsForObservationsBasedOnSpecificHasMemerCorrectly() {
		runHasObservationBasedOnTest("has-member", "a600eee3-ec06-4edb-b371-18876269ae72",
		    new String[] { ORDER_WITH_GROUP_MEMBERS_UUID }, new String[] { WRONG_UUID, ORDER_WITH_CODED_VALUE_UUID });
	}
	
	@Test
	public void shouldApplyHasConstraintsForObservationsBasedOnAnyValueStringCorrectly() {
		runHasObservationBasedOnTest("value-string", null, new String[] { ORDER_WITH_TEXT_VALUE_UUID },
		    new String[] { WRONG_UUID, ORDER_WITH_CODED_VALUE_UUID });
	}
	
	@Test
	public void shouldApplyHasConstraintsForObservationsBasedOnSpecificValueStringCorrectly() {
		runHasObservationBasedOnTest("value-string",
		    // TODO: what would be a realistic value here? Are spaces allowed?
		    "value_text", new String[] { ORDER_WITH_TEXT_VALUE_UUID },
		    new String[] { WRONG_UUID, ORDER_WITH_CODED_VALUE_UUID });
	}
	
	@Test
	public void shouldApplyHasConstraintsForObservationsBasedOnAnyIdentifierCorrectly() {
		// this test should return ALL observations (it basically checks obs.uuid != null)
		runHasObservationBasedOnTest("identifier", null, new String[] { ORDER_WITH_TEXT_VALUE_UUID }, new String[] {});
	}
	
	@Test
	public void shouldApplyHasConstraintsForObservationsBasedOnSpecificIdentifierCorrectly() {
		runHasObservationBasedOnTest("identifier", "9354bbf7-df39-4153-a07c-e2a27ee4a11f",
		    new String[] { ORDER_WITH_TEXT_VALUE_UUID },
		    new String[] { WRONG_UUID, ORDER_WITH_OBSERVATION_CATEGORY_LABORATORY_UUID });
	}
	
	@Test
	public void shouldApplyHasConstraintsForObservationsBasedOnAnyEncounterCorrectly() {
		// this test should return ALL observations (it basically checks obs.uuid != null)
		runHasObservationBasedOnTest("encounter", null,
		    new String[] { ORDER_WITH_TEXT_VALUE_UUID, ORDER_WITH_OBSERVATION_CATEGORY_LABORATORY_UUID },
		    new String[] { WRONG_UUID });
	}
	
	@Test
	public void shouldApplyHasConstraintsForObservationsBasedOnSpecificEncounterCorrectly() {
		runHasObservationBasedOnTest("encounter", "6cbdb2ef-ef1f-4def-8313-c71763434cd4",
		    new String[] { ORDER_WITH_PROVIDER_UUID }, new String[] { WRONG_UUID, ORDER_WITH_TEXT_VALUE_UUID });
	}
	
	@Test
	public void shouldApplyHasConstraintsForObservationsBasedOnAnyStatusCorrectly() {
		// this test should return ALL observations (it basically checks obs.uuid != null)
		runHasObservationBasedOnTest("status", null,
		    new String[] { ORDER_WITH_TEXT_VALUE_UUID, ORDER_WITH_OBSERVATION_CATEGORY_LABORATORY_UUID },
		    new String[] { WRONG_UUID });
	}
	
	@Test
	public void shouldApplyHasConstraintsForObservationsBasedOnSpecificStatusCorrectly() {
		runHasObservationBasedOnTest("status", "AMENDED", new String[] { ORDER_WITH_STATUS_UUID },
		    new String[] { WRONG_UUID, ORDER_WITH_TEXT_VALUE_UUID });
	}
	
	@Test
	public void shouldApplyHasConstraintsForObservationsBasedOnAnyValueQuantityCorrectly() {
		// this test should return ALL observations (it basically checks obs.uuid != null)
		runHasObservationBasedOnTest("value-quantity", null, new String[] { ORDER_WITH_NUMERIC_VALUE_UUID },
		    new String[] { WRONG_UUID });
	}
	
	@Test
	public void shouldApplyHasConstraintsForObservationsBasedOnSpecificValueQuantityCorrectly() {
		runHasObservationBasedOnTest("value-quantity", "12.345678", new String[] { ORDER_WITH_NUMERIC_VALUE_UUID },
		    new String[] { WRONG_UUID, ORDER_WITH_TEXT_VALUE_UUID });
	}
	
	@Test
	public void shouldApplyHasConstraintsForObservationsBasedOnAnyPerformerCorrectly() {
		// this test should return ALL observations (it basically checks obs.uuid != null)
		runHasObservationBasedOnTest("performer", null, new String[] { ORDER_WITH_PROVIDER_UUID },
		    new String[] { WRONG_UUID });
	}
	
	@Test
	public void shouldApplyHasConstraintsForObservationsBasedOnSpecificPerformerCorrectly() {
		runHasObservationBasedOnTest("performer", "3c08ebc2-6d9d-46c6-bdc4-e8a1871d7a7d",
		    new String[] { ORDER_WITH_PROVIDER_UUID }, new String[] { WRONG_UUID, ORDER_WITH_TEXT_VALUE_UUID });
	}
	
	@Test
	public void shouldReturnNoResultsForInvalidSearchParameter() {
		runHasObservationBasedOnTest("__NotPerformer", "3c08ebc2-6d9d-46c6-bdc4-e8a1871d7a7d", new String[] {},
		    new String[] { WRONG_UUID, ORDER_WITH_TEXT_VALUE_UUID, ORDER_WITH_PROVIDER_UUID });
	}
	
	@Test
	public void shouldReturnNoResultsForMalformattedNumeric() {
		runHasObservationBasedOnTest("value-quantity", "twelve", new String[] {},
		    new String[] { WRONG_UUID, ORDER_WITH_TEXT_VALUE_UUID, ORDER_WITH_NUMERIC_VALUE_UUID });
	}
	
	@Test
	public void shouldReturnNoResultsForMalformattedDate() {
		runHasObservationBasedOnTest("value-date", "yesterday", new String[] {},
		    new String[] { WRONG_UUID, ORDER_WITH_TEXT_VALUE_UUID, ORDER_WITH_NUMERIC_VALUE_UUID });
	}
	
	@Test
	public void shouldReturnNoResultsForUnknownReferencesForHasSearch() {
		HasOrListParam hasOrListParam = new HasOrListParam();
		hasOrListParam.add(new HasParam("Observation", "__notBased-on", "status", null));
		HasAndListParam hasAndListParam = new HasAndListParam();
		hasAndListParam.addAnd(hasOrListParam);
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.HAS_SEARCH_HANDLER,
		    hasAndListParam);
		
		List<TestOrder> results = dao.getSearchResults(theParams);
		
		assertThat(results, notNullValue());
	}
	
	@Test
	public void shouldReturnNoEntriesForUnsupportedSearchParameters() {
		String[] unsupportedSearchParameters = { "focus", "derived-from", "method", "data-absent-reason", "device",
		        "component-data-absent-reason", "component-code-value-quantity", "component-value-quantity",
		        "component-code-value-concept", "component-value-concept", "component-code", "combo-data-absent-reason",
		        "combo-code", "combo-code-value-quantity", "combo-code-value-concept", "combo-value-quantity",
		        "combo-value-concept", };
		
		for (String unsupportedSearchParameter : unsupportedSearchParameters) {
			runHasObservationBasedOnTest(unsupportedSearchParameter, null, new String[] {},
			    new String[] { WRONG_UUID, ORDER_WITH_TEXT_VALUE_UUID });
		}
	}
	
	private void runHasObservationBasedOnTest(String searchParameter, String value, String[] expectedUuids,
	        String[] unexpectedUuids) {
		HasOrListParam hasOrListParam = new HasOrListParam();
		hasOrListParam.add(new HasParam("Observation", "based-on", searchParameter, value));
		HasAndListParam hasAndListParam = new HasAndListParam();
		hasAndListParam.addAnd(hasOrListParam);
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.HAS_SEARCH_HANDLER,
		    hasAndListParam);
		
		List<TestOrder> results = dao.getSearchResults(theParams);
		
		assertThat(results, notNullValue());
		
		if (expectedUuids.length > 0) {
			assertThat("No service requests returned", results.size() > 0);
		}
		
		for (String expectedUuid : expectedUuids) {
			assertThat("Result did not contain expected entry", containsUuid(results, expectedUuid));
		}
		
		for (String unexpectedUuid : unexpectedUuids) {
			assertThat("Result did contain unexpected entry", !containsUuid(results, unexpectedUuid));
		}
	}
	
	private boolean containsUuid(Collection<TestOrder> collection, String uuid) {
		Predicate predicate = new Predicate() {
			
			public boolean evaluate(Object sample) {
				return ((TestOrder) sample).getUuid().equals(uuid);
			}
		};
		return CollectionUtils.exists(collection, predicate);
	}
	
	private String toFhirDateFormat(String dateString) {
		
		try {
			Date dateValue = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss.S").parse(dateString);
			
			// TODO: what is the correct DateFormat for FHIR?
			return DateFormat.getDateTimeInstance().format(dateValue);
		}
		catch (Exception e) {
			assertEquals("Exception during test setup - check format of input value", dateString,
			    dateString + " should match yyyy-MM-dd hh:mm:ss.S");
			return null;
		}
	}
}
