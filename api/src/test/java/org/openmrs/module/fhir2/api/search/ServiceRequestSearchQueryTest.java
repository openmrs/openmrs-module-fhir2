/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.fhir2.api.search;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;

import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

import ca.uhn.fhir.model.api.Include;
import ca.uhn.fhir.rest.api.server.IBundleProvider;
import ca.uhn.fhir.rest.param.DateRangeParam;
import ca.uhn.fhir.rest.param.ReferenceAndListParam;
import ca.uhn.fhir.rest.param.ReferenceOrListParam;
import ca.uhn.fhir.rest.param.ReferenceParam;
import ca.uhn.fhir.rest.param.TokenAndListParam;
import ca.uhn.fhir.rest.param.TokenParam;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.Encounter;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.Practitioner;
import org.hl7.fhir.r4.model.ServiceRequest;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.TestOrder;
import org.openmrs.api.cache.CacheConfig;
import org.openmrs.module.fhir2.BaseFhirContextSensitiveTest;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.FhirTestConstants;
import org.openmrs.module.fhir2.api.dao.FhirServiceRequestDao;
import org.openmrs.module.fhir2.api.search.param.SearchParameterMap;
import org.openmrs.module.fhir2.api.translators.ServiceRequestTranslator;
import org.springframework.beans.factory.annotation.Autowired;

public class ServiceRequestSearchQueryTest extends BaseFhirContextSensitiveTest {
	
	private static final String TEST_ORDER_INITIAL_DATA = "org/openmrs/module/fhir2/api/dao/impl/FhirServiceRequestTest_initial_data.xml";
	
	private static final String SERVICE_REQUEST_UUID = "7d96f25c-4949-4f72-9931-d808fbc226de";
	
	private static final String DATE_CREATED = "2008-11-19";
	
	private static final String DATE_VOIDED = "2010-09-03";
	
	private static final String TEST_ORDER_CONCEPT_ID = "5497";
	
	private static final String TEST_ORDER_CONCEPT_UUID = "a09ab2c5-878e-4905-b25d-5784167d0216";
	
	private static final String TEST_ORDER_LOINC_CODE = "2343903";
	
	private static final String TEST_ORDER_CIEL_CODE = "2343900";
	
	private static final String PATIENT_UUID = "da7f524f-27ce-4bb2-86d6-6d1d05312bd5";
	
	private static final String PATIENT_WRONG_UUID = "da7f52kiu-27ce-4bb2-86d6-6d1d05312bd5";
	
	private static final String PATIENT_GIVEN_NAME = "Horatio";
	
	private static final String PATIENT_WRONG_GIVEN_NAME = "Wrong given name";
	
	private static final String PATIENT_FAMILY_NAME = "Hornblower";
	
	private static final String PATIENT_WRONG_FAMILY_NAME = "Wrong family name";
	
	private static final String PATIENT_IDENTIFIER = "101-6";
	
	private static final String PATIENT_WRONG_IDENTIFIER = "Wrong identifier";
	
	private static final String ENCOUNTER_UUID = "y403fafb-e5e4-42d0-9d11-4f52e89d123r";
	
	private static final String ENCOUNTER_UUID_TWO = "e403fafb-e5e4-42d0-9d11-4f52e89d148c";
	
	private static final String START_DATE = "2008-01-10";
	
	private static final String END_DATE = "2009-01-10";
	
	private static final String START_DATE_TIME = "2008-01-10T09:24:10.0";
	
	private static final String END_DATE_TIME = "2009-01-10T09:24:10.0";
	
	private static final String PARTICIPANT_UUID = "c2299800-cca9-11e0-9572-0800200c9a66";
	
	private static final String PARTICIPANT_WRONG_UUID = "c2299800-cca9-11e0-9572-abcdef0c9a66";
	
	private static final String PARTICIPANT_GIVEN_NAME = "Super";
	
	private static final String PARTICIPANT_WRONG_GIVEN_NAME = "Wrong given name";
	
	private static final String PARTICIPANT_FAMILY_NAME = "User";
	
	private static final String PARTICIPANT_WRONG_FAMILY_NAME = "Wrong family name";
	
	private static final String PARTICIPANT_IDENTIFIER = "Test";
	
	private static final String PARTICIPANT_WRONG_IDENTIFIER = "Wrong identifier";
	
	private static final int START_INDEX = 0;
	
	private static final int END_INDEX = 10;
	
	@Autowired
	CacheConfig cacheConfig;
	
	@Autowired
	private FhirServiceRequestDao<TestOrder> dao;
	
	@Autowired
	private ServiceRequestTranslator<TestOrder> translator;
	
	@Autowired
	private SearchQueryInclude<ServiceRequest> searchQueryInclude;
	
	@Autowired
	private SearchQuery<TestOrder, ServiceRequest, FhirServiceRequestDao<TestOrder>, ServiceRequestTranslator<TestOrder>, SearchQueryInclude<ServiceRequest>> searchQuery;
	
	@Before
	public void setup() throws Exception {
		// Needed until TRUNK-6299 in place
		cacheConfig.cacheManager().getCacheNames().forEach(name -> cacheConfig.cacheManager().getCache(name).clear());
		executeDataSet(TEST_ORDER_INITIAL_DATA);
	}
	
	private IBundleProvider search(SearchParameterMap theParams) {
		return searchQuery.getQueryResults(theParams, dao, translator, searchQueryInclude);
	}
	
	private List<ServiceRequest> get(IBundleProvider results) {
		return results.getResources(START_INDEX, END_INDEX).stream().filter(it -> it instanceof ServiceRequest)
		        .map(it -> (ServiceRequest) it).collect(Collectors.toList());
	}
	
	@Test
	public void searchForServiceRequests_shouldSearchForServiceRequestsByConceptId() {
		TokenAndListParam code = new TokenAndListParam().addAnd(new TokenParam(TEST_ORDER_CONCEPT_ID));
		
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.CODED_SEARCH_HANDLER, code);
		
		IBundleProvider results = search(theParams);
		
		assertThat(results, notNullValue());
		
		assertThat(results.size(), equalTo(4));
		
		List<ServiceRequest> resultList = get(results);
		
		assertThat(resultList, not(empty()));
		assertThat(resultList.size(), equalTo(4));
		assertThat(resultList, everyItem(
		    hasProperty("code", hasProperty("coding", hasItem(hasProperty("code", equalTo(TEST_ORDER_CONCEPT_UUID)))))));
	}
	
	@Test
	public void searchForServiceRequests_shouldSearchForServiceRequestsByConceptUuid() {
		TokenAndListParam code = new TokenAndListParam().addAnd(new TokenParam(TEST_ORDER_CONCEPT_UUID));
		
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.CODED_SEARCH_HANDLER, code);
		
		IBundleProvider results = search(theParams);
		
		assertThat(results, notNullValue());
		assertThat(results.size(), equalTo(4));
		
		List<ServiceRequest> resultList = get(results);
		
		assertThat(resultList, not(empty()));
		assertThat(resultList, hasSize(equalTo(4)));
		assertThat(resultList, everyItem(
		    hasProperty("code", hasProperty("coding", hasItem(hasProperty("code", equalTo(TEST_ORDER_CONCEPT_UUID)))))));
	}
	
	@Test
	public void searchForServiceRequests_shouldSearchForServiceRequestsByConceptMapping() {
		TokenAndListParam code = new TokenAndListParam()
		        .addAnd(new TokenParam(FhirTestConstants.LOINC_SYSTEM_URL, TEST_ORDER_LOINC_CODE));
		
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.CODED_SEARCH_HANDLER, code);
		
		IBundleProvider results = search(theParams);
		
		assertThat(results, notNullValue());
		assertThat(results.size(), equalTo(4));
		
		List<ServiceRequest> resultList = get(results);
		
		assertThat(resultList, not(empty()));
		assertThat(resultList, hasSize(equalTo(4)));
		assertThat(resultList,
		    everyItem(
		        hasProperty("code", hasProperty("coding", hasItem(allOf(hasProperty("code", equalTo(TEST_ORDER_LOINC_CODE)),
		            hasProperty("system", equalTo(FhirTestConstants.LOINC_SYSTEM_URL))))))));
	}
	
	@Test
	public void searchForServiceRequests_shouldSupportMappedAndUnmappedConcepts() {
		TokenAndListParam code = new TokenAndListParam()
		        .addAnd(new TokenParam().setSystem(FhirTestConstants.LOINC_SYSTEM_URL).setValue(TEST_ORDER_LOINC_CODE))
		        .addAnd(new TokenParam().setValue(TEST_ORDER_CONCEPT_ID));
		
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.CODED_SEARCH_HANDLER, code);
		
		IBundleProvider results = search(theParams);
		
		assertThat(results, notNullValue());
		assertThat(results.size(), equalTo(4));
		
		List<ServiceRequest> resultList = get(results);
		
		assertThat(resultList, not(empty()));
		assertThat(resultList, hasSize(equalTo(4)));
		assertThat(resultList, everyItem(allOf(
		    hasProperty("code",
		        hasProperty("coding",
		            hasItem(allOf(hasProperty("code", equalTo(TEST_ORDER_LOINC_CODE)),
		                hasProperty("system", equalTo(FhirTestConstants.LOINC_SYSTEM_URL)))))),
		    hasProperty("code", hasProperty("coding", hasItem(hasProperty("code", equalTo(TEST_ORDER_CONCEPT_UUID))))))));
	}
	
	@Test
	public void searchForServiceRequests_shouldReturnFromMultipleConceptMappings() {
		TokenAndListParam code = new TokenAndListParam().addAnd(
		    new TokenParam().setSystem(FhirTestConstants.LOINC_SYSTEM_URL).setValue(TEST_ORDER_LOINC_CODE),
		    new TokenParam().setSystem(FhirTestConstants.CIEL_SYSTEM_URN).setValue(TEST_ORDER_CIEL_CODE));
		
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.CODED_SEARCH_HANDLER, code);
		
		IBundleProvider results = search(theParams);
		
		assertThat(results, notNullValue());
		assertThat(results.size(), equalTo(4));
		
		List<ServiceRequest> resultList = get(results);
		
		assertThat(resultList, not(empty()));
		assertThat(resultList, hasSize(equalTo(4)));
		assertThat(resultList,
		    everyItem(anyOf(
		        hasProperty("code",
		            hasProperty("coding",
		                hasItem(allOf(hasProperty("code", equalTo(TEST_ORDER_LOINC_CODE)),
		                    hasProperty("system", equalTo(FhirTestConstants.LOINC_SYSTEM_URL)))))),
		        
		        hasProperty("code", hasProperty("coding", hasItem(allOf(hasProperty("code", equalTo(TEST_ORDER_CIEL_CODE)),
		            hasProperty("system", equalTo(FhirTestConstants.CIEL_SYSTEM_URN)))))))));
	}
	
	@Test
	public void searchForServiceRequests_shouldReturnServiceRequestsByPatientUuid() {
		ReferenceAndListParam patientReference = new ReferenceAndListParam()
		        .addAnd(new ReferenceOrListParam().add(new ReferenceParam().setValue(PATIENT_UUID)));
		
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.PATIENT_REFERENCE_SEARCH_HANDLER,
		    patientReference);
		
		IBundleProvider results = search(theParams);
		
		assertThat(results, notNullValue());
		assertThat(results.size(), equalTo(4));
		
		List<ServiceRequest> resultList = get(results);
		
		assertThat(resultList, not(empty()));
		assertThat(resultList, hasSize(equalTo(4)));
		assertThat(resultList, everyItem(
		    hasProperty("subject", hasProperty("referenceElement", hasProperty("idPart", equalTo(PATIENT_UUID))))));
	}
	
	@Test
	public void searchForServiceRequests_shouldSearchForServiceRequestsByMultiplePatientUuidOr() {
		ReferenceAndListParam patientReference = new ReferenceAndListParam().addAnd(new ReferenceOrListParam()
		        .add(new ReferenceParam().setValue(PATIENT_UUID)).add(new ReferenceParam().setValue(PATIENT_WRONG_UUID)));
		
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.PATIENT_REFERENCE_SEARCH_HANDLER,
		    patientReference);
		
		IBundleProvider results = search(theParams);
		
		assertThat(results, notNullValue());
		assertThat(results.size(), equalTo(4));
		
		List<ServiceRequest> resultList = get(results);
		
		assertThat(resultList, not(empty()));
		assertThat(resultList, hasSize(equalTo(4)));
		assertThat(resultList, everyItem(
		    hasProperty("subject", hasProperty("referenceElement", hasProperty("idPart", equalTo(PATIENT_UUID))))));
	}
	
	@Test
	public void searchForServiceRequests_shouldReturnEmptyListOfServiceRequestsByMultiplePatientUuidAnd() {
		ReferenceAndListParam patientReference = new ReferenceAndListParam()
		        .addAnd(new ReferenceOrListParam().add(new ReferenceParam().setValue(PATIENT_UUID)))
		        .addAnd(new ReferenceOrListParam().add(new ReferenceParam().setValue(PATIENT_WRONG_UUID)));
		
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.PATIENT_REFERENCE_SEARCH_HANDLER,
		    patientReference);
		
		IBundleProvider results = search(theParams);
		
		assertThat(results, notNullValue());
		assertThat(results.size(), equalTo(0));
		
		List<ServiceRequest> resultList = get(results);
		
		assertThat(resultList, notNullValue());
		assertThat(resultList, empty());
	}
	
	@Test
	public void searchForServiceRequests_shouldReturnServiceRequestsByPatientGivenName() {
		ReferenceAndListParam patientReference = new ReferenceAndListParam().addAnd(
		    new ReferenceOrListParam().add(new ReferenceParam().setValue(PATIENT_GIVEN_NAME).setChain(Patient.SP_GIVEN)));
		
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.PATIENT_REFERENCE_SEARCH_HANDLER,
		    patientReference);
		
		IBundleProvider results = search(theParams);
		
		assertThat(results, notNullValue());
		assertThat(results.size(), equalTo(4));
		
		List<ServiceRequest> resources = get(results);
		
		assertThat(resources, not(empty()));
		assertThat(resources, everyItem(
		    hasProperty("subject", hasProperty("referenceElement", hasProperty("idPart", equalTo(PATIENT_UUID))))));
	}
	
	@Test
	public void searchForServiceRequests_shouldReturnServiceRequestsByPatientFamilyName() {
		ReferenceAndListParam patientReference = new ReferenceAndListParam().addAnd(
		    new ReferenceOrListParam().add(new ReferenceParam().setValue(PATIENT_FAMILY_NAME).setChain(Patient.SP_FAMILY)));
		
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.PATIENT_REFERENCE_SEARCH_HANDLER,
		    patientReference);
		
		IBundleProvider results = search(theParams);
		
		assertThat(results, notNullValue());
		assertThat(results.size(), equalTo(4));
		
		List<ServiceRequest> resources = get(results);
		
		assertThat(resources, not(empty()));
		assertThat(resources, everyItem(
		    hasProperty("subject", hasProperty("referenceElement", hasProperty("idPart", equalTo(PATIENT_UUID))))));
	}
	
	@Test
	public void searchForServiceRequests_shouldSearchForServiceRequestsByMultiplePatientGivenNameOr() {
		ReferenceAndListParam patientReference = new ReferenceAndListParam().addAnd(
		    new ReferenceOrListParam().add(new ReferenceParam().setValue(PATIENT_GIVEN_NAME).setChain(Patient.SP_GIVEN))
		            .add(new ReferenceParam().setValue(PATIENT_WRONG_GIVEN_NAME).setChain(Patient.SP_GIVEN)));
		
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.PATIENT_REFERENCE_SEARCH_HANDLER,
		    patientReference);
		
		IBundleProvider results = search(theParams);
		
		assertThat(results, notNullValue());
		assertThat(results.size(), equalTo(4));
		
		List<ServiceRequest> resources = get(results);
		
		assertThat(resources, not(empty()));
		assertThat(resources, everyItem(
		    hasProperty("subject", hasProperty("referenceElement", hasProperty("idPart", equalTo(PATIENT_UUID))))));
	}
	
	@Test
	public void searchForServiceRequests_shouldReturnEmptyListOfServiceRequestsByMultiplePatientGivenNameAnd() {
		ReferenceAndListParam patientReference = new ReferenceAndListParam()
		        .addAnd(new ReferenceOrListParam()
		                .add(new ReferenceParam().setValue(PATIENT_GIVEN_NAME).setChain(Patient.SP_GIVEN)))
		        .addAnd(new ReferenceOrListParam()
		                .add(new ReferenceParam().setValue(PATIENT_WRONG_GIVEN_NAME).setChain(Patient.SP_GIVEN)));
		
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.PATIENT_REFERENCE_SEARCH_HANDLER,
		    patientReference);
		
		IBundleProvider results = search(theParams);
		
		assertThat(results, notNullValue());
		assertThat(results.size(), equalTo(0));
		
		List<ServiceRequest> resultList = get(results);
		
		assertThat(resultList, notNullValue());
		assertThat(resultList, empty());
	}
	
	@Test
	public void searchForServiceRequests_shouldSearchForServiceRequestsByMultiplePatientFamilyNameOr() {
		ReferenceAndListParam patientReference = new ReferenceAndListParam().addAnd(
		    new ReferenceOrListParam().add(new ReferenceParam().setValue(PATIENT_FAMILY_NAME).setChain(Patient.SP_FAMILY))
		            .add(new ReferenceParam().setValue(PATIENT_WRONG_FAMILY_NAME).setChain(Patient.SP_FAMILY)));
		
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.PATIENT_REFERENCE_SEARCH_HANDLER,
		    patientReference);
		
		IBundleProvider results = search(theParams);
		
		assertThat(results, notNullValue());
		assertThat(results.size(), equalTo(4));
		
		List<ServiceRequest> resources = get(results);
		
		assertThat(resources, not(empty()));
		assertThat(resources, everyItem(
		    hasProperty("subject", hasProperty("referenceElement", hasProperty("idPart", equalTo(PATIENT_UUID))))));
	}
	
	@Test
	public void searchForServiceRequests_shouldReturnEmptyListOfServiceRequestsByMultiplePatientFamilyNameAnd() {
		ReferenceAndListParam patientReference = new ReferenceAndListParam()
		        .addAnd(new ReferenceOrListParam()
		                .add(new ReferenceParam().setValue(PATIENT_FAMILY_NAME).setChain(Patient.SP_FAMILY)))
		        .addAnd(new ReferenceOrListParam()
		                .add(new ReferenceParam().setValue(PATIENT_WRONG_FAMILY_NAME).setChain(Patient.SP_FAMILY)));
		
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.PATIENT_REFERENCE_SEARCH_HANDLER,
		    patientReference);
		
		IBundleProvider results = search(theParams);
		
		assertThat(results, notNullValue());
		assertThat(results.size(), equalTo(0));
		
		List<ServiceRequest> resultList = get(results);
		
		assertThat(resultList, notNullValue());
		assertThat(resultList, empty());
	}
	
	@Test
	public void searchForServiceRequests_shouldReturnServiceRequestsByPatientName() {
		ReferenceAndListParam patientReference = new ReferenceAndListParam().addAnd(new ReferenceOrListParam().add(
		    new ReferenceParam().setValue(PATIENT_GIVEN_NAME + " " + PATIENT_FAMILY_NAME).setChain(Patient.SP_NAME)));
		
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.PATIENT_REFERENCE_SEARCH_HANDLER,
		    patientReference);
		
		IBundleProvider results = search(theParams);
		
		assertThat(results, notNullValue());
		assertThat(results.size(), equalTo(4));
		
		List<ServiceRequest> resources = get(results);
		
		assertThat(resources, not(empty()));
		assertThat(resources, everyItem(
		    hasProperty("subject", hasProperty("referenceElement", hasProperty("idPart", equalTo(PATIENT_UUID))))));
	}
	
	@Test
	public void searchForServiceRequests_shouldSearchForServiceRequestsByMultiplePatientNameOr() {
		ReferenceAndListParam patientReference = new ReferenceAndListParam().addAnd(new ReferenceOrListParam()
		        .add(new ReferenceParam().setValue(PATIENT_GIVEN_NAME + " " + PATIENT_FAMILY_NAME).setChain(Patient.SP_NAME))
		        .add(new ReferenceParam().setValue(PATIENT_WRONG_GIVEN_NAME + " " + PATIENT_WRONG_FAMILY_NAME)
		                .setChain(Patient.SP_NAME)));
		
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.PATIENT_REFERENCE_SEARCH_HANDLER,
		    patientReference);
		
		IBundleProvider results = search(theParams);
		
		assertThat(results, notNullValue());
		assertThat(results.size(), equalTo(4));
		
		List<ServiceRequest> resources = get(results);
		
		assertThat(resources, not(empty()));
		assertThat(resources, everyItem(
		    hasProperty("subject", hasProperty("referenceElement", hasProperty("idPart", equalTo(PATIENT_UUID))))));
	}
	
	@Test
	public void searchForServiceRequests_shouldReturnEmptyListOfServiceRequestsByMultiplePatientNameAnd() {
		ReferenceAndListParam patientReference = new ReferenceAndListParam()
		        .addAnd(new ReferenceOrListParam().add(
		            new ReferenceParam().setValue(PATIENT_GIVEN_NAME + " " + PATIENT_FAMILY_NAME).setChain(Patient.SP_NAME)))
		        .addAnd(new ReferenceOrListParam().add(new ReferenceParam()
		                .setValue(PATIENT_WRONG_GIVEN_NAME + " " + PATIENT_WRONG_FAMILY_NAME).setChain(Patient.SP_NAME)));
		
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.PATIENT_REFERENCE_SEARCH_HANDLER,
		    patientReference);
		
		IBundleProvider results = search(theParams);
		
		assertThat(results, notNullValue());
		assertThat(results.size(), equalTo(0));
		
		List<ServiceRequest> resultList = get(results);
		
		assertThat(resultList, notNullValue());
		assertThat(resultList, empty());
	}
	
	@Test
	public void searchForServiceRequests_shouldReturnServiceRequestsByPatientIdentifier() {
		ReferenceAndListParam patientReference = new ReferenceAndListParam().addAnd(new ReferenceOrListParam()
		        .add(new ReferenceParam().setValue(PATIENT_IDENTIFIER).setChain(Patient.SP_IDENTIFIER)));
		
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.PATIENT_REFERENCE_SEARCH_HANDLER,
		    patientReference);
		IBundleProvider results = search(theParams);
		
		assertThat(results, notNullValue());
		assertThat(results.size(), equalTo(4));
		
		List<ServiceRequest> resources = get(results);
		
		assertThat(resources, not(empty()));
		assertThat(resources, hasSize(equalTo(4)));
		assertThat(resources, everyItem(hasProperty("subject", hasProperty("reference", endsWith(PATIENT_UUID)))));
	}
	
	@Test
	public void searchForServiceRequests_shouldSearchForServiceRequestsByMultiplePatientIdentifierOr() {
		ReferenceAndListParam patientReference = new ReferenceAndListParam().addAnd(
		    new ReferenceOrListParam().add(new ReferenceParam().setValue(PATIENT_IDENTIFIER).setChain(Patient.SP_IDENTIFIER))
		            .add(new ReferenceParam().setValue(PATIENT_WRONG_IDENTIFIER).setChain(Patient.SP_IDENTIFIER)));
		
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.PATIENT_REFERENCE_SEARCH_HANDLER,
		    patientReference);
		
		IBundleProvider results = search(theParams);
		
		assertThat(results, notNullValue());
		assertThat(results.size(), equalTo(4));
		
		List<ServiceRequest> resultList = get(results);
		
		assertThat(resultList, not(empty()));
		assertThat(resultList, hasSize(equalTo(4)));
		assertThat(resultList, everyItem(hasProperty("subject", hasProperty("reference", endsWith(PATIENT_UUID)))));
	}
	
	@Test
	public void searchForServiceRequests_shouldReturnEmptyListOfServiceRequestsByMultiplePatientIdentifierAnd() {
		ReferenceAndListParam patientReference = new ReferenceAndListParam()
		        .addAnd(new ReferenceOrListParam()
		                .add(new ReferenceParam().setValue(PATIENT_IDENTIFIER).setChain(Patient.SP_IDENTIFIER)))
		        .addAnd(new ReferenceOrListParam()
		                .add(new ReferenceParam().setValue(PATIENT_WRONG_IDENTIFIER).setChain(Patient.SP_IDENTIFIER)));
		
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.PATIENT_REFERENCE_SEARCH_HANDLER,
		    patientReference);
		
		IBundleProvider results = search(theParams);
		
		assertThat(results, notNullValue());
		assertThat(results.size(), equalTo(0));
		
		List<ServiceRequest> resultList = get(results);
		
		assertThat(resultList, notNullValue());
		assertThat(resultList, empty());
	}
	
	@Test
	public void searchForServiceRequests_shouldReturnServiceRequestsByEncounter() {
		ReferenceAndListParam encounterReference = new ReferenceAndListParam()
		        .addAnd(new ReferenceOrListParam().add(new ReferenceParam().setValue(ENCOUNTER_UUID)));
		
		SearchParameterMap theParams = new SearchParameterMap()
		        .addParameter(FhirConstants.ENCOUNTER_REFERENCE_SEARCH_HANDLER, encounterReference);
		
		IBundleProvider results = search(theParams);
		
		assertThat(results, notNullValue());
		assertThat(results.size(), equalTo(4));
		
		List<ServiceRequest> resources = get(results);
		
		assertThat(resources, not(empty()));
		assertThat(resources, hasSize(equalTo(4)));
		assertThat(resources, everyItem(
		    hasProperty("encounter", hasProperty("referenceElement", hasProperty("idPart", equalTo(ENCOUNTER_UUID))))));
	}
	
	@Test
	public void searchForServiceRequests_shouldReturnServiceRequestsByPatientUuidAndPatientGivenName() {
		ReferenceAndListParam patientReference = new ReferenceAndListParam()
		        .addAnd(new ReferenceOrListParam().add(new ReferenceParam().setValue(PATIENT_UUID)))
		        .addAnd(new ReferenceOrListParam()
		                .add(new ReferenceParam().setValue(PATIENT_GIVEN_NAME).setChain(Patient.SP_GIVEN)));
		
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.PATIENT_REFERENCE_SEARCH_HANDLER,
		    patientReference);
		
		IBundleProvider results = search(theParams);
		
		assertThat(results, notNullValue());
		assertThat(results.size(), equalTo(4));
		
		List<ServiceRequest> resources = get(results);
		
		assertThat(resources, not(empty()));
		assertThat(resources, everyItem(
		    hasProperty("subject", hasProperty("referenceElement", hasProperty("idPart", equalTo(PATIENT_UUID))))));
	}
	
	@Test
	public void searchForServiceRequests_shouldReturnServiceRequestsByMultipleEncountersOr() {
		ReferenceAndListParam encounterReference = new ReferenceAndListParam().addAnd(new ReferenceOrListParam()
		        .add(new ReferenceParam().setValue(ENCOUNTER_UUID)).add(new ReferenceParam().setValue(ENCOUNTER_UUID_TWO)));
		
		SearchParameterMap theParams = new SearchParameterMap()
		        .addParameter(FhirConstants.ENCOUNTER_REFERENCE_SEARCH_HANDLER, encounterReference);
		
		IBundleProvider results = search(theParams);
		
		assertThat(results, notNullValue());
		assertThat(results.size(), equalTo(4));
		
		List<ServiceRequest> resources = get(results);
		
		assertThat(resources, not(empty()));
		assertThat(resources, hasSize(equalTo(4)));
		assertThat(resources, everyItem(anyOf(
		    hasProperty("encounter", hasProperty("referenceElement", hasProperty("idPart", equalTo(ENCOUNTER_UUID)))),
		    hasProperty("encounter", hasProperty("referenceElement", hasProperty("idPart", equalTo(ENCOUNTER_UUID_TWO)))))));
	}
	
	@Test
	public void searchForServiceRequests_shouldSearchByServiceRequestsDate() {
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.DATE_RANGE_SEARCH_HANDLER,
		    new DateRangeParam().setLowerBound(START_DATE).setUpperBound(END_DATE));
		
		IBundleProvider results = search(theParams);
		
		assertThat(results, notNullValue());
		assertThat(results.size(), equalTo(2));
		
		List<ServiceRequest> resultList = get(results);
		
		assertThat(resultList, not(empty()));
		assertThat(resultList.size(), equalTo(2));
		assertThat(resultList,
		    everyItem(allOf(
		        hasProperty("occurrencePeriod",
		            hasProperty("startElement", hasProperty("valueAsString", greaterThanOrEqualTo(START_DATE)))),
		        hasProperty("occurrencePeriod",
		            hasProperty("endElement", hasProperty("valueAsString", lessThanOrEqualTo(END_DATE)))))));
	}
	
	@Test
	public void searchForServiceRequests_shouldSearchByServiceRequestsDateAndTime() {
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.DATE_RANGE_SEARCH_HANDLER,
		    new DateRangeParam().setLowerBound(START_DATE_TIME).setUpperBound(END_DATE_TIME));
		
		IBundleProvider results = search(theParams);
		
		assertThat(results, notNullValue());
		assertThat(results.size(), equalTo(2));
		
		List<ServiceRequest> resultList = get(results);
		
		assertThat(resultList, not(empty()));
		assertThat(resultList.size(), equalTo(2));
		assertThat(resultList,
		    everyItem(allOf(
		        hasProperty("occurrencePeriod",
		            hasProperty("startElement", hasProperty("valueAsString", greaterThanOrEqualTo(START_DATE_TIME)))),
		        hasProperty("occurrencePeriod",
		            hasProperty("endElement", hasProperty("valueAsString", lessThanOrEqualTo(END_DATE_TIME)))))));
	}
	
	@Test
	public void searchForServiceRequests_shouldReturnServiceRequestsByParticipantUuid() {
		ReferenceAndListParam participantReference = new ReferenceAndListParam()
		        .addAnd(new ReferenceOrListParam().add(new ReferenceParam().setValue(PARTICIPANT_UUID)));
		
		SearchParameterMap theParams = new SearchParameterMap()
		        .addParameter(FhirConstants.PARTICIPANT_REFERENCE_SEARCH_HANDLER, participantReference);
		
		IBundleProvider results = search(theParams);
		
		assertThat(results, notNullValue());
		assertThat(results.size(), equalTo(4));
		
		List<ServiceRequest> resources = get(results);
		
		assertThat(resources, notNullValue());
		assertThat(resources, hasSize(equalTo(4)));
		assertThat(resources, everyItem(
		    hasProperty("requester", hasProperty("referenceElement", hasProperty("idPart", equalTo(PARTICIPANT_UUID))))));
	}
	
	@Test
	public void searchForServiceRequests_shouldSearchForServiceRequestsByMultipleParticipantUuidOr() {
		ReferenceAndListParam participantReference = new ReferenceAndListParam()
		        .addAnd(new ReferenceOrListParam().add(new ReferenceParam().setValue(PARTICIPANT_UUID))
		                .add(new ReferenceParam().setValue(PARTICIPANT_WRONG_UUID)));
		
		SearchParameterMap theParams = new SearchParameterMap()
		        .addParameter(FhirConstants.PARTICIPANT_REFERENCE_SEARCH_HANDLER, participantReference);
		
		IBundleProvider results = search(theParams);
		
		assertThat(results, notNullValue());
		assertThat(results.size(), equalTo(4));
		
		List<ServiceRequest> resources = get(results);
		
		assertThat(resources, notNullValue());
		assertThat(resources, hasSize(equalTo(4)));
		assertThat(resources, everyItem(
		    hasProperty("requester", hasProperty("referenceElement", hasProperty("idPart", equalTo(PARTICIPANT_UUID))))));
	}
	
	@Test
	public void searchForServiceRequests_shouldReturnEmptyListOfServiceRequestsByMultipleParticipantUuidAnd() {
		ReferenceAndListParam participantReference = new ReferenceAndListParam()
		        .addAnd(new ReferenceOrListParam().add(new ReferenceParam().setValue(PARTICIPANT_UUID)))
		        .addAnd(new ReferenceOrListParam().add(new ReferenceParam().setValue(PARTICIPANT_WRONG_UUID)));
		
		SearchParameterMap theParams = new SearchParameterMap()
		        .addParameter(FhirConstants.PARTICIPANT_REFERENCE_SEARCH_HANDLER, participantReference);
		
		IBundleProvider results = search(theParams);
		
		assertThat(results, notNullValue());
		assertThat(results.size(), equalTo(0));
		
		List<ServiceRequest> resultList = get(results);
		
		assertThat(resultList, notNullValue());
		assertThat(resultList, empty());
	}
	
	@Test
	public void searchForServiceRequests_shouldReturnServiceRequestsByParticipantGivenName() {
		ReferenceAndListParam participantReference = new ReferenceAndListParam().addAnd(new ReferenceOrListParam()
		        .add(new ReferenceParam().setValue(PARTICIPANT_GIVEN_NAME).setChain(Practitioner.SP_GIVEN)));
		
		SearchParameterMap theParams = new SearchParameterMap()
		        .addParameter(FhirConstants.PARTICIPANT_REFERENCE_SEARCH_HANDLER, participantReference);
		
		IBundleProvider results = search(theParams);
		
		assertThat(results, notNullValue());
		assertThat(results.size(), equalTo(4));
		
		List<ServiceRequest> resources = get(results);
		
		assertThat(resources, notNullValue());
		assertThat(resources, hasSize(equalTo(4)));
		assertThat(resources, everyItem(
		    hasProperty("requester", hasProperty("referenceElement", hasProperty("idPart", equalTo(PARTICIPANT_UUID))))));
	}
	
	@Test
	public void searchForServiceRequests_shouldSearchForServiceRequestsByMultipleParticipantGivenNameOr() {
		ReferenceAndListParam participantReference = new ReferenceAndListParam().addAnd(new ReferenceOrListParam()
		        .add(new ReferenceParam().setValue(PARTICIPANT_GIVEN_NAME).setChain(Practitioner.SP_GIVEN))
		        .add(new ReferenceParam().setValue(PARTICIPANT_WRONG_GIVEN_NAME).setChain(Practitioner.SP_GIVEN)));
		
		SearchParameterMap theParams = new SearchParameterMap()
		        .addParameter(FhirConstants.PARTICIPANT_REFERENCE_SEARCH_HANDLER, participantReference);
		
		IBundleProvider results = search(theParams);
		
		assertThat(results, notNullValue());
		assertThat(results.size(), equalTo(4));
		
		List<ServiceRequest> resources = get(results);
		
		assertThat(resources, notNullValue());
		assertThat(resources, hasSize(equalTo(4)));
		assertThat(resources, everyItem(
		    hasProperty("requester", hasProperty("referenceElement", hasProperty("idPart", equalTo(PARTICIPANT_UUID))))));
	}
	
	@Test
	public void searchForServiceRequests_shouldReturnEmptyListOfServiceRequestsByMultipleParticipantGivenNameAnd() {
		ReferenceAndListParam participantReference = new ReferenceAndListParam()
		        .addAnd(new ReferenceOrListParam()
		                .add(new ReferenceParam().setValue(PARTICIPANT_GIVEN_NAME).setChain(Practitioner.SP_GIVEN)))
		        .addAnd(new ReferenceOrListParam()
		                .add(new ReferenceParam().setValue(PARTICIPANT_WRONG_GIVEN_NAME).setChain(Practitioner.SP_GIVEN)));
		
		SearchParameterMap theParams = new SearchParameterMap()
		        .addParameter(FhirConstants.PARTICIPANT_REFERENCE_SEARCH_HANDLER, participantReference);
		
		IBundleProvider results = search(theParams);
		
		assertThat(results, notNullValue());
		assertThat(results.size(), equalTo(0));
		
		List<ServiceRequest> resultList = get(results);
		
		assertThat(resultList, notNullValue());
		assertThat(resultList, empty());
	}
	
	@Test
	public void searchForServiceRequests_shouldReturnServiceRequestsByParticipantFamilyName() {
		ReferenceAndListParam participantReference = new ReferenceAndListParam().addAnd(new ReferenceOrListParam()
		        .add(new ReferenceParam().setValue(PARTICIPANT_FAMILY_NAME).setChain(Practitioner.SP_FAMILY)));
		
		SearchParameterMap theParams = new SearchParameterMap()
		        .addParameter(FhirConstants.PARTICIPANT_REFERENCE_SEARCH_HANDLER, participantReference);
		
		IBundleProvider results = search(theParams);
		
		assertThat(results, notNullValue());
		assertThat(results.size(), equalTo(4));
		
		List<ServiceRequest> resources = get(results);
		
		assertThat(resources, notNullValue());
		assertThat(resources, hasSize(equalTo(4)));
		assertThat(resources, everyItem(
		    hasProperty("requester", hasProperty("referenceElement", hasProperty("idPart", equalTo(PARTICIPANT_UUID))))));
	}
	
	@Test
	public void searchForServiceRequests_shouldSearchForServiceRequestsByMultipleParticipantFamilyNameOr() {
		ReferenceAndListParam participantReference = new ReferenceAndListParam().addAnd(new ReferenceOrListParam()
		        .add(new ReferenceParam().setValue(PARTICIPANT_FAMILY_NAME).setChain(Practitioner.SP_FAMILY))
		        .add(new ReferenceParam().setValue(PARTICIPANT_WRONG_FAMILY_NAME).setChain(Practitioner.SP_FAMILY)));
		
		SearchParameterMap theParams = new SearchParameterMap()
		        .addParameter(FhirConstants.PARTICIPANT_REFERENCE_SEARCH_HANDLER, participantReference);
		
		IBundleProvider results = search(theParams);
		
		assertThat(results, notNullValue());
		assertThat(results.size(), equalTo(4));
		
		List<ServiceRequest> resources = get(results);
		
		assertThat(resources, notNullValue());
		assertThat(resources, hasSize(equalTo(4)));
		assertThat(resources, everyItem(
		    hasProperty("requester", hasProperty("referenceElement", hasProperty("idPart", equalTo(PARTICIPANT_UUID))))));
	}
	
	@Test
	public void searchForServiceRequests_shouldReturnEmptyListOfServiceRequestsByMultipleParticipantFamilyNameAnd() {
		ReferenceAndListParam participantReference = new ReferenceAndListParam()
		        .addAnd(new ReferenceOrListParam()
		                .add(new ReferenceParam().setValue(PARTICIPANT_FAMILY_NAME).setChain(Practitioner.SP_FAMILY)))
		        .addAnd(new ReferenceOrListParam()
		                .add(new ReferenceParam().setValue(PARTICIPANT_WRONG_FAMILY_NAME).setChain(Practitioner.SP_FAMILY)));
		
		SearchParameterMap theParams = new SearchParameterMap()
		        .addParameter(FhirConstants.PARTICIPANT_REFERENCE_SEARCH_HANDLER, participantReference);
		
		IBundleProvider results = search(theParams);
		
		assertThat(results, notNullValue());
		assertThat(results.size(), equalTo(0));
		
		List<ServiceRequest> resultList = get(results);
		
		assertThat(resultList, notNullValue());
		assertThat(resultList, empty());
	}
	
	@Test
	public void searchForServiceRequests_shouldReturnServiceRequestsByParticipantName() {
		ReferenceAndListParam participantReference = new ReferenceAndListParam()
		        .addAnd(new ReferenceOrListParam().add(new ReferenceParam()
		                .setValue(PARTICIPANT_GIVEN_NAME + " " + PARTICIPANT_FAMILY_NAME).setChain(Practitioner.SP_NAME)));
		
		SearchParameterMap theParams = new SearchParameterMap()
		        .addParameter(FhirConstants.PARTICIPANT_REFERENCE_SEARCH_HANDLER, participantReference);
		
		IBundleProvider results = search(theParams);
		
		assertThat(results, notNullValue());
		assertThat(results.size(), equalTo(4));
		
		List<ServiceRequest> resources = get(results);
		
		assertThat(resources, notNullValue());
		assertThat(resources, hasSize(equalTo(4)));
		assertThat(resources, everyItem(
		    hasProperty("requester", hasProperty("referenceElement", hasProperty("idPart", equalTo(PARTICIPANT_UUID))))));
	}
	
	@Test
	public void searchForServiceRequests_shouldSearchForServiceRequestsByMultipleParticipantNameOr() {
		ReferenceAndListParam participantReference = new ReferenceAndListParam().addAnd(
		    new ReferenceOrListParam()
		            .add(new ReferenceParam().setValue(PARTICIPANT_GIVEN_NAME + " " + PARTICIPANT_FAMILY_NAME)
		                    .setChain(Practitioner.SP_NAME))
		            .add(new ReferenceParam().setValue(PARTICIPANT_WRONG_GIVEN_NAME + " " + PARTICIPANT_WRONG_FAMILY_NAME)
		                    .setChain(Practitioner.SP_NAME)));
		
		SearchParameterMap theParams = new SearchParameterMap()
		        .addParameter(FhirConstants.PARTICIPANT_REFERENCE_SEARCH_HANDLER, participantReference);
		
		IBundleProvider results = search(theParams);
		
		assertThat(results, notNullValue());
		assertThat(results.size(), equalTo(4));
		
		List<ServiceRequest> resources = get(results);
		
		assertThat(resources, notNullValue());
		assertThat(resources, hasSize(equalTo(4)));
		assertThat(resources, everyItem(
		    hasProperty("requester", hasProperty("referenceElement", hasProperty("idPart", equalTo(PARTICIPANT_UUID))))));
	}
	
	@Test
	public void searchForServiceRequests_shouldReturnEmptyListOfServiceRequestsByMultipleParticipantNameAnd() {
		ReferenceAndListParam participantReference = new ReferenceAndListParam()
		        .addAnd(new ReferenceOrListParam().add(new ReferenceParam()
		                .setValue(PARTICIPANT_GIVEN_NAME + " " + PARTICIPANT_FAMILY_NAME).setChain(Practitioner.SP_NAME)))
		        .addAnd(new ReferenceOrListParam().add(
		            new ReferenceParam().setValue(PARTICIPANT_WRONG_GIVEN_NAME + " " + PARTICIPANT_WRONG_FAMILY_NAME)
		                    .setChain(Practitioner.SP_NAME)));
		
		SearchParameterMap theParams = new SearchParameterMap()
		        .addParameter(FhirConstants.PARTICIPANT_REFERENCE_SEARCH_HANDLER, participantReference);
		
		IBundleProvider results = search(theParams);
		
		assertThat(results, notNullValue());
		assertThat(results.size(), equalTo(0));
		
		List<ServiceRequest> resultList = get(results);
		
		assertThat(resultList, notNullValue());
		assertThat(resultList, empty());
	}
	
	@Test
	public void searchForServiceRequests_shouldReturnServiceRequestsByParticipantIdentifier() {
		ReferenceAndListParam participantReference = new ReferenceAndListParam().addAnd(new ReferenceOrListParam()
		        .add(new ReferenceParam().setValue(PARTICIPANT_IDENTIFIER).setChain(Practitioner.SP_IDENTIFIER)));
		
		SearchParameterMap theParams = new SearchParameterMap()
		        .addParameter(FhirConstants.PARTICIPANT_REFERENCE_SEARCH_HANDLER, participantReference);
		
		IBundleProvider results = search(theParams);
		
		assertThat(results, notNullValue());
		assertThat(results.size(), equalTo(4));
		
		List<ServiceRequest> resources = get(results);
		
		assertThat(resources, notNullValue());
		assertThat(resources, hasSize(equalTo(4)));
		assertThat(resources, everyItem(
		    hasProperty("requester", hasProperty("identifier", hasProperty("value", equalTo(PARTICIPANT_IDENTIFIER))))));
	}
	
	@Test
	public void searchForServiceRequests_shouldSearchForServiceRequestsByMultipleParticipantIdentifierOr() {
		ReferenceAndListParam participantReference = new ReferenceAndListParam().addAnd(new ReferenceOrListParam()
		        .add(new ReferenceParam().setValue(PARTICIPANT_IDENTIFIER).setChain(Practitioner.SP_IDENTIFIER))
		        .add(new ReferenceParam().setValue(PARTICIPANT_WRONG_IDENTIFIER).setChain(Practitioner.SP_IDENTIFIER)));
		
		SearchParameterMap theParams = new SearchParameterMap()
		        .addParameter(FhirConstants.PARTICIPANT_REFERENCE_SEARCH_HANDLER, participantReference);
		
		IBundleProvider results = search(theParams);
		
		assertThat(results, notNullValue());
		assertThat(results.size(), equalTo(4));
		
		List<ServiceRequest> resources = get(results);
		
		assertThat(resources, notNullValue());
		assertThat(resources, hasSize(equalTo(4)));
		assertThat(resources, everyItem(
		    hasProperty("requester", hasProperty("identifier", hasProperty("value", equalTo(PARTICIPANT_IDENTIFIER))))));
	}
	
	@Test
	public void searchForServiceRequests_shouldReturnEmptyListOfServiceRequestsByMultipleParticipantIdentifierAnd() {
		ReferenceAndListParam participantReference = new ReferenceAndListParam()
		        .addAnd(new ReferenceOrListParam()
		                .add(new ReferenceParam().setValue(PARTICIPANT_IDENTIFIER).setChain(Practitioner.SP_IDENTIFIER)))
		        .addAnd(new ReferenceOrListParam().add(
		            new ReferenceParam().setValue(PARTICIPANT_WRONG_IDENTIFIER).setChain(Practitioner.SP_IDENTIFIER)));
		
		SearchParameterMap theParams = new SearchParameterMap()
		        .addParameter(FhirConstants.PARTICIPANT_REFERENCE_SEARCH_HANDLER, participantReference);
		
		IBundleProvider results = search(theParams);
		
		assertThat(results, notNullValue());
		assertThat(results.size(), equalTo(0));
		
		List<ServiceRequest> resultList = get(results);
		
		assertThat(resultList, notNullValue());
		assertThat(resultList, empty());
	}
	
	@Test
	public void searchForServiceRequests_shouldSearchForServiceRequestsByUuid() {
		TokenAndListParam uuid = new TokenAndListParam().addAnd(new TokenParam(SERVICE_REQUEST_UUID));
		
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.COMMON_SEARCH_HANDLER,
		    FhirConstants.ID_PROPERTY, uuid);
		
		IBundleProvider results = search(theParams);
		
		assertThat(results, notNullValue());
		assertThat(results.size(), equalTo(1));
		
		List<ServiceRequest> resultList = get(results);
		
		assertThat(resultList, hasSize(equalTo(1)));
		assertThat(resultList.get(0).getIdElement().getIdPart(), equalTo(SERVICE_REQUEST_UUID));
	}
	
	@Test
	public void searchForServiceRequests_shouldSearchForServiceRequestsByLastUpdatedDateCreated() {
		DateRangeParam lastUpdated = new DateRangeParam().setUpperBound(DATE_CREATED).setLowerBound(DATE_CREATED);
		
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.COMMON_SEARCH_HANDLER,
		    FhirConstants.LAST_UPDATED_PROPERTY, lastUpdated);
		
		IBundleProvider results = search(theParams);
		
		assertThat(results, notNullValue());
		assertThat(results.size(), equalTo(1));
		
		List<ServiceRequest> resultList = get(results);
		
		assertThat(resultList, hasSize(equalTo(1)));
	}
	
	@Test
	public void searchForServiceRequests_shouldSearchForServiceRequestsByMatchingUuidAndLastUpdated() {
		TokenAndListParam uuid = new TokenAndListParam().addAnd(new TokenParam(SERVICE_REQUEST_UUID));
		DateRangeParam lastUpdated = new DateRangeParam().setUpperBound(DATE_CREATED).setLowerBound(DATE_CREATED);
		
		SearchParameterMap theParams = new SearchParameterMap()
		        .addParameter(FhirConstants.COMMON_SEARCH_HANDLER, FhirConstants.ID_PROPERTY, uuid)
		        .addParameter(FhirConstants.COMMON_SEARCH_HANDLER, FhirConstants.LAST_UPDATED_PROPERTY, lastUpdated);
		
		IBundleProvider results = search(theParams);
		
		List<ServiceRequest> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList, not(empty()));
		assertThat(resultList, hasSize(equalTo(1)));
		assertThat(resultList.get(0).getIdElement().getIdPart(), equalTo(SERVICE_REQUEST_UUID));
	}
	
	@Test
	public void searchForServiceRequests_shouldReturnEmptyListByMismatchingUuidAndLastUpdated() {
		TokenAndListParam uuid = new TokenAndListParam().addAnd(new TokenParam(SERVICE_REQUEST_UUID));
		DateRangeParam lastUpdated = new DateRangeParam().setUpperBound(DATE_VOIDED).setLowerBound(DATE_VOIDED);
		
		SearchParameterMap theParams = new SearchParameterMap()
		        .addParameter(FhirConstants.COMMON_SEARCH_HANDLER, FhirConstants.ID_PROPERTY, uuid)
		        .addParameter(FhirConstants.COMMON_SEARCH_HANDLER, FhirConstants.LAST_UPDATED_PROPERTY, lastUpdated);
		
		IBundleProvider results = search(theParams);
		
		List<ServiceRequest> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList, empty());
	}
	
	@Test
	public void searchForServiceRequests_shouldAddPatientsToResultListWhenIncluded() {
		TokenAndListParam uuid = new TokenAndListParam().addAnd(new TokenParam(SERVICE_REQUEST_UUID));
		HashSet<Include> includes = new HashSet<>();
		includes.add(new Include("ServiceRequest:patient"));
		
		SearchParameterMap theParams = new SearchParameterMap()
		        .addParameter(FhirConstants.COMMON_SEARCH_HANDLER, FhirConstants.ID_PROPERTY, uuid)
		        .addParameter(FhirConstants.INCLUDE_SEARCH_HANDLER, includes);
		
		IBundleProvider results = search(theParams);
		assertThat(results, notNullValue());
		assertThat(results.size(), equalTo(1));
		
		List<IBaseResource> resultList = results.getResources(START_INDEX, END_INDEX);
		
		assertThat(resultList, not(empty()));
		assertThat(resultList.size(), equalTo(2)); // included resource added as part of result list
		assertThat(((ServiceRequest) resultList.iterator().next()).getIdElement().getIdPart(),
		    equalTo(SERVICE_REQUEST_UUID));
		
		ServiceRequest returnedServiceRequest = (ServiceRequest) resultList.iterator().next();
		assertThat(resultList, hasItem(allOf(is(instanceOf(Patient.class)),
		    hasProperty("id", equalTo(returnedServiceRequest.getSubject().getReferenceElement().getIdPart())))));
	}
	
	@Test
	public void searchForServiceRequests_shouldAddPatientsToResultListWhenIncludedR3() {
		TokenAndListParam uuid = new TokenAndListParam().addAnd(new TokenParam(SERVICE_REQUEST_UUID));
		HashSet<Include> includes = new HashSet<>();
		includes.add(new Include("ProcedureRequest:patient"));
		
		SearchParameterMap theParams = new SearchParameterMap()
		        .addParameter(FhirConstants.COMMON_SEARCH_HANDLER, FhirConstants.ID_PROPERTY, uuid)
		        .addParameter(FhirConstants.INCLUDE_SEARCH_HANDLER, includes);
		
		IBundleProvider results = search(theParams);
		assertThat(results, notNullValue());
		assertThat(results.size(), equalTo(1));
		
		List<IBaseResource> resultList = results.getResources(START_INDEX, END_INDEX);
		
		assertThat(resultList, not(empty()));
		assertThat(resultList.size(), equalTo(2)); // included resource added as part of result list
		assertThat(((ServiceRequest) resultList.iterator().next()).getIdElement().getIdPart(),
		    equalTo(SERVICE_REQUEST_UUID));
		
		ServiceRequest returnedServiceRequest = (ServiceRequest) resultList.iterator().next();
		assertThat(resultList, hasItem(allOf(is(instanceOf(Patient.class)),
		    hasProperty("id", equalTo(returnedServiceRequest.getSubject().getReferenceElement().getIdPart())))));
	}
	
	@Test
	public void searchForServiceRequests_shouldAddRequesterToResultListWhenIncluded() {
		TokenAndListParam uuid = new TokenAndListParam().addAnd(new TokenParam(SERVICE_REQUEST_UUID));
		HashSet<Include> includes = new HashSet<>();
		includes.add(new Include("ServiceRequest:requester"));
		
		SearchParameterMap theParams = new SearchParameterMap()
		        .addParameter(FhirConstants.COMMON_SEARCH_HANDLER, FhirConstants.ID_PROPERTY, uuid)
		        .addParameter(FhirConstants.INCLUDE_SEARCH_HANDLER, includes);
		
		IBundleProvider results = search(theParams);
		assertThat(results, notNullValue());
		assertThat(results.size(), equalTo(1));
		
		List<IBaseResource> resultList = results.getResources(START_INDEX, END_INDEX);
		
		assertThat(resultList, not(empty()));
		assertThat(resultList.size(), equalTo(2)); // included resource added as part of result list
		assertThat(((ServiceRequest) resultList.iterator().next()).getIdElement().getIdPart(),
		    equalTo(SERVICE_REQUEST_UUID));
		
		ServiceRequest returnedServiceRequest = (ServiceRequest) resultList.iterator().next();
		assertThat(resultList, hasItem(allOf(is(instanceOf(Practitioner.class)),
		    hasProperty("id", equalTo(returnedServiceRequest.getRequester().getReferenceElement().getIdPart())))));
	}
	
	@Test
	public void searchForServiceRequests_shouldAddRequesterToResultListWhenIncludedR3() {
		TokenAndListParam uuid = new TokenAndListParam().addAnd(new TokenParam(SERVICE_REQUEST_UUID));
		HashSet<Include> includes = new HashSet<>();
		includes.add(new Include("ProcedureRequest:requester"));
		
		SearchParameterMap theParams = new SearchParameterMap()
		        .addParameter(FhirConstants.COMMON_SEARCH_HANDLER, FhirConstants.ID_PROPERTY, uuid)
		        .addParameter(FhirConstants.INCLUDE_SEARCH_HANDLER, includes);
		
		IBundleProvider results = search(theParams);
		assertThat(results, notNullValue());
		assertThat(results.size(), equalTo(1));
		
		List<IBaseResource> resultList = results.getResources(START_INDEX, END_INDEX);
		
		assertThat(resultList, not(empty()));
		assertThat(resultList.size(), equalTo(2)); // included resource added as part of result list
		assertThat(((ServiceRequest) resultList.iterator().next()).getIdElement().getIdPart(),
		    equalTo(SERVICE_REQUEST_UUID));
		
		ServiceRequest returnedServiceRequest = (ServiceRequest) resultList.iterator().next();
		assertThat(resultList, hasItem(allOf(is(instanceOf(Practitioner.class)),
		    hasProperty("id", equalTo(returnedServiceRequest.getRequester().getReferenceElement().getIdPart())))));
	}
	
	@Test
	public void searchForServiceRequests_shouldAddEncounterToResultListWhenIncluded() {
		TokenAndListParam uuid = new TokenAndListParam().addAnd(new TokenParam(SERVICE_REQUEST_UUID));
		HashSet<Include> includes = new HashSet<>();
		includes.add(new Include("ServiceRequest:encounter"));
		
		SearchParameterMap theParams = new SearchParameterMap()
		        .addParameter(FhirConstants.COMMON_SEARCH_HANDLER, FhirConstants.ID_PROPERTY, uuid)
		        .addParameter(FhirConstants.INCLUDE_SEARCH_HANDLER, includes);
		
		IBundleProvider results = search(theParams);
		assertThat(results, notNullValue());
		assertThat(results.size(), equalTo(1));
		
		List<IBaseResource> resultList = results.getResources(START_INDEX, END_INDEX);
		
		assertThat(resultList, not(empty()));
		assertThat(resultList.size(), equalTo(2)); // included resource added as part of result list
		assertThat(((ServiceRequest) resultList.iterator().next()).getIdElement().getIdPart(),
		    equalTo(SERVICE_REQUEST_UUID));
		
		ServiceRequest returnedServiceRequest = (ServiceRequest) resultList.iterator().next();
		assertThat(resultList, hasItem(allOf(is(instanceOf(Encounter.class)),
		    hasProperty("id", equalTo(returnedServiceRequest.getEncounter().getReferenceElement().getIdPart())))));
	}
	
	@Test
	public void searchForServiceRequests_shouldAddEncounterToResultListWhenIncludedR3() {
		TokenAndListParam uuid = new TokenAndListParam().addAnd(new TokenParam(SERVICE_REQUEST_UUID));
		HashSet<Include> includes = new HashSet<>();
		includes.add(new Include("ProcedureRequest:encounter"));
		
		SearchParameterMap theParams = new SearchParameterMap()
		        .addParameter(FhirConstants.COMMON_SEARCH_HANDLER, FhirConstants.ID_PROPERTY, uuid)
		        .addParameter(FhirConstants.INCLUDE_SEARCH_HANDLER, includes);
		
		IBundleProvider results = search(theParams);
		assertThat(results, notNullValue());
		assertThat(results.size(), equalTo(1));
		
		List<IBaseResource> resultList = results.getResources(START_INDEX, END_INDEX);
		
		assertThat(resultList, not(empty()));
		assertThat(resultList.size(), equalTo(2)); // included resource added as part of result list
		assertThat(((ServiceRequest) resultList.iterator().next()).getIdElement().getIdPart(),
		    equalTo(SERVICE_REQUEST_UUID));
		
		ServiceRequest returnedServiceRequest = (ServiceRequest) resultList.iterator().next();
		assertThat(resultList, hasItem(allOf(is(instanceOf(Encounter.class)),
		    hasProperty("id", equalTo(returnedServiceRequest.getEncounter().getReferenceElement().getIdPart())))));
	}
	
	@Test
	public void searchForServiceRequests_shouldHandleMultipleIncludes() {
		TokenAndListParam uuid = new TokenAndListParam().addAnd(new TokenParam(SERVICE_REQUEST_UUID));
		HashSet<Include> includes = new HashSet<>();
		includes.add(new Include("ServiceRequest:requester"));
		includes.add(new Include("ServiceRequest:patient"));
		
		SearchParameterMap theParams = new SearchParameterMap()
		        .addParameter(FhirConstants.COMMON_SEARCH_HANDLER, FhirConstants.ID_PROPERTY, uuid)
		        .addParameter(FhirConstants.INCLUDE_SEARCH_HANDLER, includes);
		
		IBundleProvider results = search(theParams);
		assertThat(results, notNullValue());
		assertThat(results.size(), equalTo(1));
		
		List<IBaseResource> resultList = results.getResources(START_INDEX, END_INDEX);
		
		assertThat(resultList, not(empty()));
		assertThat(resultList.size(), equalTo(3)); // included resources (patient + requester) added as part of result list
		assertThat(((ServiceRequest) resultList.iterator().next()).getIdElement().getIdPart(),
		    equalTo(SERVICE_REQUEST_UUID));
		
		ServiceRequest returnedServiceRequest = (ServiceRequest) resultList.iterator().next();
		assertThat(resultList, hasItem(allOf(is(instanceOf(Practitioner.class)),
		    hasProperty("id", equalTo(returnedServiceRequest.getRequester().getReferenceElement().getIdPart())))));
		assertThat(resultList, hasItem(allOf(is(instanceOf(Patient.class)),
		    hasProperty("id", equalTo(returnedServiceRequest.getSubject().getReferenceElement().getIdPart())))));
	}
	
	@Test
	public void searchForServiceRequests_shouldHandleMultipleIncludesR3() {
		TokenAndListParam uuid = new TokenAndListParam().addAnd(new TokenParam(SERVICE_REQUEST_UUID));
		HashSet<Include> includes = new HashSet<>();
		includes.add(new Include("ProcedureRequest:requester"));
		includes.add(new Include("ProcedureRequest:patient"));
		
		SearchParameterMap theParams = new SearchParameterMap()
		        .addParameter(FhirConstants.COMMON_SEARCH_HANDLER, FhirConstants.ID_PROPERTY, uuid)
		        .addParameter(FhirConstants.INCLUDE_SEARCH_HANDLER, includes);
		
		IBundleProvider results = search(theParams);
		assertThat(results, notNullValue());
		assertThat(results.size(), equalTo(1));
		
		List<IBaseResource> resultList = results.getResources(START_INDEX, END_INDEX);
		
		assertThat(resultList, not(empty()));
		assertThat(resultList.size(), equalTo(3)); // included resources (patient + requester) added as part of result list
		assertThat(((ServiceRequest) resultList.iterator().next()).getIdElement().getIdPart(),
		    equalTo(SERVICE_REQUEST_UUID));
		
		ServiceRequest returnedServiceRequest = (ServiceRequest) resultList.iterator().next();
		assertThat(resultList, hasItem(allOf(is(instanceOf(Practitioner.class)),
		    hasProperty("id", equalTo(returnedServiceRequest.getRequester().getReferenceElement().getIdPart())))));
		assertThat(resultList, hasItem(allOf(is(instanceOf(Patient.class)),
		    hasProperty("id", equalTo(returnedServiceRequest.getSubject().getReferenceElement().getIdPart())))));
	}
}
