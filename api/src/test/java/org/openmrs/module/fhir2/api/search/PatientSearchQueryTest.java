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
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.startsWith;
import static org.hl7.fhir.r4.model.Patient.SP_FAMILY;
import static org.hl7.fhir.r4.model.Patient.SP_GIVEN;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import ca.uhn.fhir.model.api.Include;
import ca.uhn.fhir.rest.api.SortOrderEnum;
import ca.uhn.fhir.rest.api.SortSpec;
import ca.uhn.fhir.rest.api.server.IBundleProvider;
import ca.uhn.fhir.rest.param.DateParam;
import ca.uhn.fhir.rest.param.DateRangeParam;
import ca.uhn.fhir.rest.param.StringAndListParam;
import ca.uhn.fhir.rest.param.StringParam;
import ca.uhn.fhir.rest.param.TokenAndListParam;
import ca.uhn.fhir.rest.param.TokenParam;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.AllergyIntolerance;
import org.hl7.fhir.r4.model.DiagnosticReport;
import org.hl7.fhir.r4.model.Encounter;
import org.hl7.fhir.r4.model.Enumerations;
import org.hl7.fhir.r4.model.MedicationRequest;
import org.hl7.fhir.r4.model.Observation;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.Procedure;
import org.hl7.fhir.r4.model.ServiceRequest;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.TestFhirSpringConfiguration;
import org.openmrs.module.fhir2.api.FhirGlobalPropertyService;
import org.openmrs.module.fhir2.api.dao.FhirPatientDao;
import org.openmrs.module.fhir2.api.search.param.SearchParameterMap;
import org.openmrs.module.fhir2.api.translators.PatientTranslator;
import org.openmrs.test.BaseModuleContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

@ContextConfiguration(classes = TestFhirSpringConfiguration.class, inheritLocations = false)
public class PatientSearchQueryTest extends BaseModuleContextSensitiveTest {
	
	private static final String[] PATIENT_SEARCH_DATA_FILES = {
	        "org/openmrs/module/fhir2/api/dao/impl/FhirPatientDaoImplTest_initial_data.xml",
	        "org/openmrs/module/fhir2/api/dao/impl/FhirPatientDaoImplTest_address_data.xml" };
	
	private static final String SEVERITY_SEVERE_CONCEPT_UUID = "c607c80f-1ea9-4da3-bb88-6276ce8868dd";
	
	private static final String PATIENT_GIVEN_NAME = "Jeannette";
	
	private static final String PATIENT_OTHER1_UUID = "f363b024-7ff2-4882-9f5b-d9bce5d10a9e";
	
	private static final String PATIENT_OTHER2_UUID = "ca17fcc5-ec96-487f-b9ea-42973c8973e3";
	
	private static final String PATIENT_OTHER3_UUID = "61b38324-e2fd-4feb-95b7-9e9a2a4400df";
	
	private static final String PATIENT_UUID = "f363b024-7ff2-4882-9f5b-d9bce5d10a9e";
	
	private static final String PATIENT_PARTIAL_GIVEN_NAME = "Jean";
	
	private static final String PATIENT_FAMILY_NAME = "Claudent";
	
	private static final String PATIENT_PARTIAL_FAMILY_NAME = "Claud";
	
	private static final String PATIENT_NOT_FOUND_NAME = "Igor";
	
	private static final String PATIENT_IDENTIFIER = "563422-5";
	
	private static final String BAD_PATIENT_IDENTIFIER = "9999X9999";
	
	private static final String PATIENT_IDENTIFIER_TYPE = "Test Identifier Type";
	
	private static final String BAD_PATIENT_IDENTIFIER_TYPE = "Non-Existent Identifier";
	
	private static final String PATIENT_MALE_GENDER = "male";
	
	private static final String PATIENT_FEMALE_GENDER = "female";
	
	private static final String PATIENT_WRONG_GENDER = "wrong-gender";
	
	private static final String PATIENT_BIRTHDATE = "1976-08-25";
	
	private static final String PATIENT_BIRTHDATE_LOWER_BOUND = "1975-04-08";
	
	private static final String PATIENT_BIRTHDATE_PATIENT_UUID = "ca17fcc5-ec96-487f-b9ea-42973c8973e3";
	
	private static final String PATIENT_ADDRESS_CITY = "Indianapolis";
	
	private static final String PATIENT_ADDRESS_STATE = "IN";
	
	private static final String PATIENT_ADDRESS_COUNTRY = "USA";
	
	private static final String PATIENT_ADDRESS_POSTAL_CODE = "46202";
	
	private static final String PATIENT_ADDRESS_PATIENT_UUID = "61b38324-e2fd-4feb-95b7-9e9a2a4400df";
	
	private static final String DATE_CREATED = "2005-01-01";
	
	private static final String DATE_CHANGED = "2008-08-18";
	
	private static final int START_INDEX = 0;
	
	private static final int END_INDEX = 10;
	
	private static final Map<String, String> SEVERITY_CONCEPT_UUIDS = new HashMap<>();
	
	@Autowired
	private PatientTranslator translator;
	
	@Autowired
	private FhirPatientDao dao;
	
	@Autowired
	private SearchQueryInclude<Patient> searchQueryInclude;
	
	@Autowired
	private FhirGlobalPropertyService globalPropertyService;
	
	@Autowired
	private SearchQuery<org.openmrs.Patient, Patient, FhirPatientDao, PatientTranslator, SearchQueryInclude<Patient>> searchQuery;
	
	@Before
	public void setup() throws Exception {
		for (String search_data : PATIENT_SEARCH_DATA_FILES) {
			executeDataSet(search_data);
		}
	}
	
	@Before
	public void setupMocks() {
		SEVERITY_CONCEPT_UUIDS.put(FhirConstants.GLOBAL_PROPERTY_SEVERE, SEVERITY_SEVERE_CONCEPT_UUID);
		
		when(globalPropertyService.getGlobalProperties(FhirConstants.GLOBAL_PROPERTY_MILD,
		    FhirConstants.GLOBAL_PROPERTY_MODERATE, FhirConstants.GLOBAL_PROPERTY_SEVERE,
		    FhirConstants.GLOBAL_PROPERTY_OTHER)).thenReturn(SEVERITY_CONCEPT_UUIDS);
	}
	
	private IBundleProvider search(SearchParameterMap theParams) {
		return searchQuery.getQueryResults(theParams, dao, translator, searchQueryInclude);
	}
	
	private List<Patient> get(IBundleProvider results) {
		return results.getResources(START_INDEX, END_INDEX).stream().filter(it -> it instanceof Patient)
		        .map(it -> (Patient) it).collect(Collectors.toList());
	}
	
	private List<IBaseResource> getAllResources(IBundleProvider results) {
		return results.getAllResources();
	}
	
	@Test
	public void searchForPatients_shouldSearchForPatientsByName() {
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.NAME_SEARCH_HANDLER, "name",
		    new StringAndListParam().addAnd(new StringParam(PATIENT_GIVEN_NAME)));
		
		IBundleProvider results = search(theParams);
		
		assertThat(results, notNullValue());
		assertThat(results.size(), greaterThan(1));
		
		List<Patient> resultList = get(results);
		
		assertThat(resultList, not(empty()));
		assertThat(resultList, hasSize(greaterThan(1)));
	}
	
	@Test
	public void searchForPatients_shouldReturnMultiplePatientsForPartialMatch() {
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.NAME_SEARCH_HANDLER, "name",
		    new StringAndListParam().addAnd(new StringParam(PATIENT_PARTIAL_GIVEN_NAME)));
		
		IBundleProvider results = search(theParams);
		
		assertThat(results, notNullValue());
		assertThat(results.size(), greaterThan(1));
		
		List<Patient> resultList = get(results);
		
		assertThat(resultList, not(empty()));
		assertThat(results.size(), greaterThan(1));
	}
	
	@Test
	public void searchForPatients_shouldReturnEmptyCollectionWhenPatientNameNotMatched() {
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.NAME_SEARCH_HANDLER,
		    FhirConstants.NAME_PROPERTY, new StringAndListParam().addAnd(new StringParam(PATIENT_NOT_FOUND_NAME)));
		
		IBundleProvider results = search(theParams);
		
		assertThat(results, notNullValue());
		assertThat(results.size(), equalTo(0));
		
		List<Patient> resultList = get(results);
		
		assertThat(resultList, empty());
	}
	
	@Test
	public void searchForPatients_shouldSearchForPatientsByGivenName() {
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.NAME_SEARCH_HANDLER,
		    FhirConstants.GIVEN_PROPERTY, new StringAndListParam().addAnd(new StringParam(PATIENT_GIVEN_NAME)));
		
		IBundleProvider results = search(theParams);
		
		assertThat(results, notNullValue());
		assertThat(results.size(), greaterThanOrEqualTo(1));
		
		List<Patient> resultList = get(results);
		
		assertThat(resultList, not(empty()));
		assertThat(resultList.get(0).getNameFirstRep().getGiven().get(0).toString(), startsWith(PATIENT_GIVEN_NAME));
	}
	
	@Test
	public void searchForPatients_shouldReturnMultiplePatientsForPartialMatchOnGivenName() {
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.NAME_SEARCH_HANDLER,
		    FhirConstants.GIVEN_PROPERTY, new StringAndListParam().addAnd(new StringParam(PATIENT_PARTIAL_GIVEN_NAME)));
		
		IBundleProvider results = search(theParams);
		
		assertThat(results, notNullValue());
		assertThat(results.size(), greaterThan(1));
		
		List<Patient> resultList = get(results);
		
		assertThat(resultList, hasSize(greaterThan(1)));
		assertThat(resultList.get(0).getNameFirstRep().getGiven().get(0).toString(), startsWith(PATIENT_PARTIAL_GIVEN_NAME));
	}
	
	@Test
	public void searchForPatients_shouldReturnEmptyCollectionWhenPatientGivenNameNotMatched() {
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.NAME_SEARCH_HANDLER,
		    FhirConstants.GIVEN_PROPERTY, new StringAndListParam().addAnd(new StringParam(PATIENT_NOT_FOUND_NAME)));
		
		IBundleProvider results = search(theParams);
		
		assertThat(results, notNullValue());
		assertThat(results.size(), equalTo(0));
		
		List<Patient> resultList = get(results);
		
		assertThat(resultList, is(empty()));
	}
	
	@Test
	public void searchForPatients_shouldSearchForPatientsByFamilyName() {
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.NAME_SEARCH_HANDLER,
		    FhirConstants.FAMILY_PROPERTY, new StringAndListParam().addAnd(new StringParam(PATIENT_FAMILY_NAME)));
		IBundleProvider results = search(theParams);
		
		assertThat(results, notNullValue());
		assertThat(results.size(), greaterThanOrEqualTo(1));
		
		List<Patient> resultList = get(results);
		
		assertThat(resultList, not(empty()));
		assertThat(resultList.get(0).getNameFirstRep().getFamily(), startsWith(PATIENT_FAMILY_NAME));
	}
	
	@Test
	public void searchForPatients_shouldReturnMultiplePatientsForPartialMatchOnFamilyName() {
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.NAME_SEARCH_HANDLER,
		    FhirConstants.FAMILY_PROPERTY, new StringAndListParam().addAnd(new StringParam(PATIENT_PARTIAL_FAMILY_NAME)));
		IBundleProvider results = search(theParams);
		
		List<Patient> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList, not(empty()));
		assertThat(results.size(), greaterThan(1));
	}
	
	@Test
	public void searchForPatients_shouldReturnEmptyCollectionWhenPatientFamilyNameNotMatched() {
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.NAME_SEARCH_HANDLER,
		    FhirConstants.FAMILY_PROPERTY, new StringAndListParam().addAnd(new StringParam(PATIENT_NOT_FOUND_NAME)));
		IBundleProvider results = search(theParams);
		
		List<Patient> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList, empty());
	}
	
	@Test
	public void searchForPatients_shouldSearchForPatientsByIdentifier() {
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.IDENTIFIER_SEARCH_HANDLER,
		    new TokenAndListParam().addAnd(new TokenParam(null, PATIENT_IDENTIFIER)));
		
		IBundleProvider results = search(theParams);
		
		assertThat(results, notNullValue());
		assertThat(results.size(), equalTo(1));
		
		List<Patient> resultList = get(results);
		
		assertThat(resultList, hasSize(equalTo(1)));
		assertThat(resultList.get(0).getIdentifierFirstRep().getValue(), equalTo(PATIENT_IDENTIFIER));
	}
	
	@Test
	public void searchForPatients_shouldSearchForPatientsByIdentifierWithType() {
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.IDENTIFIER_SEARCH_HANDLER,
		    new TokenAndListParam().addAnd(new TokenParam(PATIENT_IDENTIFIER_TYPE, PATIENT_IDENTIFIER)));
		
		IBundleProvider results = search(theParams);
		
		assertThat(results, notNullValue());
		assertThat(results.size(), equalTo(1));
		
		List<Patient> resultList = get(results);
		
		assertThat(resultList, hasSize(equalTo(1)));
	}
	
	@Test
	public void searchForPatients_shouldReturnEmptyCollectionWhenIdentifierNotMatched() {
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.IDENTIFIER_SEARCH_HANDLER,
		    new TokenAndListParam().addAnd(new TokenParam(BAD_PATIENT_IDENTIFIER_TYPE, BAD_PATIENT_IDENTIFIER)));
		IBundleProvider results = search(theParams);
		
		assertThat(results, notNullValue());
		assertThat(results.size(), equalTo(0));
		
		List<Patient> resultList = get(results);
		
		assertThat(resultList, empty());
	}
	
	@Test
	public void searchForPatients_shouldReturnEmptyCollectionWhenIdentifierTypeNotMatched() {
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.IDENTIFIER_SEARCH_HANDLER,
		    new TokenAndListParam().addAnd(new TokenParam(BAD_PATIENT_IDENTIFIER_TYPE, PATIENT_IDENTIFIER)));
		IBundleProvider results = search(theParams);
		
		assertThat(results, notNullValue());
		assertThat(results.size(), equalTo(0));
		
		List<Patient> resultList = get(results);
		
		assertThat(resultList, empty());
	}
	
	@Test
	public void searchForPatients_shouldReturnPatientsByGender() {
		final String GENDER_PROPERTY = "gender";
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.GENDER_SEARCH_HANDLER, "gender",
		    new TokenAndListParam().addAnd(new TokenParam(PATIENT_MALE_GENDER)));
		
		IBundleProvider results = search(theParams);
		
		assertThat(results, notNullValue());
		
		List<Patient> resultList = get(results);
		
		assertThat(resultList, not(empty()));
		assertThat(resultList, everyItem(hasProperty(GENDER_PROPERTY, equalTo(Enumerations.AdministrativeGender.MALE))));
		
		results = search(new SearchParameterMap().addParameter(FhirConstants.GENDER_SEARCH_HANDLER, "gender",
		    new TokenAndListParam().addAnd(new TokenParam(PATIENT_FEMALE_GENDER))));
		
		assertThat(results, notNullValue());
		
		resultList = get(results);
		
		assertThat(resultList, not(empty()));
		assertThat(resultList, everyItem(hasProperty(GENDER_PROPERTY, equalTo(Enumerations.AdministrativeGender.FEMALE))));
	}
	
	@Test
	public void searchForPatients_shouldReturnEmptyCollectionWhenGenderNotMatched() {
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.GENDER_SEARCH_HANDLER, "gender",
		    new TokenAndListParam().addAnd(new TokenParam(null, PATIENT_WRONG_GENDER)));
		
		IBundleProvider results = search(theParams);
		
		assertThat(results, notNullValue());
		assertThat(results.size(), equalTo(0));
		
		List<Patient> resultList = get(results);
		
		assertThat(resultList, empty());
	}
	
	@Test
	public void searchForPatients_shouldSearchForPatientsByBirthDate() {
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.DATE_RANGE_SEARCH_HANDLER,
		    "birthdate", new DateRangeParam(new DateParam(PATIENT_BIRTHDATE)));
		
		IBundleProvider results = search(theParams);
		
		assertThat(results, notNullValue());
		assertThat(results.size(), greaterThanOrEqualTo(1));
		
		List<Patient> resultList = get(results);
		
		assertThat(resultList, not(empty()));
		assertThat(resultList, hasItem(hasProperty("id", equalTo(PATIENT_BIRTHDATE_PATIENT_UUID))));
	}
	
	@Test
	public void searchForPatients_shouldSearchForPatientsByBirthDateWithLowerBound() {
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.DATE_RANGE_SEARCH_HANDLER,
		    "birthdate", new DateRangeParam().setLowerBound(PATIENT_BIRTHDATE));
		
		IBundleProvider results = search(theParams);
		
		assertThat(results, notNullValue());
		assertThat(results.size(), greaterThanOrEqualTo(1));
		
		List<Patient> resultList = get(results);
		
		assertThat(resultList, not(empty()));
		assertThat(resultList, hasItem(hasProperty("id", equalTo(PATIENT_BIRTHDATE_PATIENT_UUID))));
	}
	
	@Test
	public void searchForPatients_shouldSearchForPatientsByBirthDateWithUpperBound() {
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.DATE_RANGE_SEARCH_HANDLER,
		    "birthdate", new DateRangeParam().setUpperBound(PATIENT_BIRTHDATE));
		
		IBundleProvider results = search(theParams);
		
		assertThat(results, notNullValue());
		assertThat(results.size(), greaterThan(1));
		
		List<Patient> resultList = get(results);
		
		assertThat(resultList, not(empty()));
		assertThat(resultList, hasSize(greaterThan(1)));
		assertThat(resultList, hasItem(hasProperty("id", equalTo(PATIENT_BIRTHDATE_PATIENT_UUID))));
	}
	
	@Test
	public void searchForPatients_shouldSearchForPatientsByBirthDateWithinBoundaries() {
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.DATE_RANGE_SEARCH_HANDLER,
		    "birthdate", new DateRangeParam().setLowerBound(PATIENT_BIRTHDATE_LOWER_BOUND).setUpperBound(PATIENT_BIRTHDATE));
		
		IBundleProvider results = search(theParams);
		
		assertThat(results, notNullValue());
		assertThat(results.size(), greaterThan(1));
		
		List<Patient> resultList = get(results);
		
		assertThat(resultList, not(empty()));
		assertThat(resultList, hasSize(greaterThan(1)));
		assertThat(resultList, hasItem(hasProperty("id", equalTo(PATIENT_BIRTHDATE_PATIENT_UUID))));
	}
	
	@Test
	public void searchForPatients_shouldSearchForPatientsByCity() {
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.ADDRESS_SEARCH_HANDLER,
		    FhirConstants.CITY_PROPERTY, new StringAndListParam().addAnd(new StringParam(PATIENT_ADDRESS_CITY)));
		
		IBundleProvider results = search(theParams);
		
		assertThat(results, notNullValue());
		assertThat(results.size(), greaterThanOrEqualTo(1));
		
		List<Patient> resultList = get(results);
		
		assertThat(resultList, not(empty()));
		assertThat(resultList.get(0).getIdElement().getIdPart(), equalTo(PATIENT_ADDRESS_PATIENT_UUID));
	}
	
	@Test
	public void searchForPatients_shouldSearchForPatientsByState() {
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.ADDRESS_SEARCH_HANDLER,
		    FhirConstants.STATE_PROPERTY, new StringAndListParam().addAnd(new StringParam(PATIENT_ADDRESS_STATE)));
		
		IBundleProvider results = search(theParams);
		
		assertThat(results, notNullValue());
		assertThat(results.size(), greaterThanOrEqualTo(1));
		
		List<Patient> resultList = get(results);
		
		assertThat(resultList, not(empty()));
		assertThat(resultList.get(0).getIdElement().getIdPart(), equalTo(PATIENT_ADDRESS_PATIENT_UUID));
	}
	
	@Test
	public void searchForPatients_shouldSearchForPatientsByCountry() {
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.ADDRESS_SEARCH_HANDLER,
		    FhirConstants.COUNTRY_PROPERTY, new StringAndListParam().addAnd(new StringParam(PATIENT_ADDRESS_COUNTRY)));
		
		IBundleProvider results = search(theParams);
		
		assertThat(results, notNullValue());
		assertThat(results.size(), greaterThanOrEqualTo(1));
		
		List<Patient> resultList = get(results);
		
		assertThat(resultList, not(empty()));
		assertThat(resultList.get(0).getIdElement().getIdPart(), equalTo(PATIENT_ADDRESS_PATIENT_UUID));
	}
	
	@Test
	public void searchForPatients_shouldSearchForPatientsByPostalCode() {
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.ADDRESS_SEARCH_HANDLER,
		    FhirConstants.POSTAL_CODE_PROPERTY,
		    new StringAndListParam().addAnd(new StringParam(PATIENT_ADDRESS_POSTAL_CODE)));
		
		IBundleProvider results = search(theParams);
		
		assertThat(results, notNullValue());
		assertThat(results.size(), greaterThanOrEqualTo(1));
		
		List<Patient> resultList = get(results);
		
		assertThat(resultList, not(empty()));
		assertThat(resultList.get(0).getIdElement().getIdPart(), equalTo(PATIENT_ADDRESS_PATIENT_UUID));
	}
	
	@Test
	public void searchForPatients_shouldSearchForPatientsByUuid() {
		TokenAndListParam uuid = new TokenAndListParam().addAnd(new TokenParam(PATIENT_UUID));
		
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.COMMON_SEARCH_HANDLER,
		    FhirConstants.ID_PROPERTY, uuid);
		
		IBundleProvider results = search(theParams);
		
		assertThat(results, notNullValue());
		assertThat(results.size(), equalTo(1));
		
		List<Patient> resultList = get(results);
		
		assertThat(resultList, hasSize(equalTo(1)));
		assertThat(resultList.get(0).getIdElement().getIdPart(), equalTo(PATIENT_UUID));
	}
	
	@Test
	public void searchForPatients_shouldSearchForPatientsByLastUpdatedDateCreated() {
		DateRangeParam lastUpdated = new DateRangeParam().setUpperBound(DATE_CREATED).setLowerBound(DATE_CREATED);
		
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.COMMON_SEARCH_HANDLER,
		    FhirConstants.LAST_UPDATED_PROPERTY, lastUpdated);
		
		IBundleProvider results = search(theParams);
		
		assertThat(results, notNullValue());
		assertThat(results.size(), equalTo(5));
		
		List<Patient> resultList = get(results);
		
		assertThat(resultList, not(empty()));
		assertThat(resultList.size(), equalTo(5));
	}
	
	@Test
	public void searchForPatients_shouldSearchForPatientsByLastUpdatedDateChanged() {
		DateRangeParam lastUpdated = new DateRangeParam().setUpperBound(DATE_CHANGED).setLowerBound(DATE_CHANGED);
		
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.COMMON_SEARCH_HANDLER,
		    FhirConstants.LAST_UPDATED_PROPERTY, lastUpdated);
		
		IBundleProvider results = search(theParams);
		
		assertThat(results, notNullValue());
		assertThat(results.size(), equalTo(4));
		
		List<Patient> resultList = get(results);
		
		assertThat(resultList, hasSize(equalTo(4)));
	}
	
	@Test
	public void searchForPatients_shouldSearchForPatientsByMatchingUuidAndLastUpdated() {
		TokenAndListParam uuid = new TokenAndListParam().addAnd(new TokenParam(PATIENT_UUID));
		DateRangeParam lastUpdated = new DateRangeParam().setUpperBound(DATE_CREATED).setLowerBound(DATE_CREATED);
		
		SearchParameterMap theParams = new SearchParameterMap()
		        .addParameter(FhirConstants.COMMON_SEARCH_HANDLER, FhirConstants.ID_PROPERTY, uuid)
		        .addParameter(FhirConstants.COMMON_SEARCH_HANDLER, FhirConstants.LAST_UPDATED_PROPERTY, lastUpdated);
		
		IBundleProvider results = search(theParams);
		
		assertThat(results, notNullValue());
		assertThat(results.size(), equalTo(1));
		
		List<Patient> resultList = get(results);
		
		assertThat(resultList, hasSize(equalTo(1)));
		assertThat(resultList.get(0).getIdElement().getIdPart(), equalTo(PATIENT_UUID));
	}
	
	@Test
	public void searchForPatients_shouldReturnEmptyListByMismatchingUuidAndLastUpdated() {
		TokenAndListParam uuid = new TokenAndListParam().addAnd(new TokenParam());
		DateRangeParam lastUpdated = new DateRangeParam().setUpperBound(DATE_CHANGED).setLowerBound(DATE_CHANGED);
		
		SearchParameterMap theParams = new SearchParameterMap()
		        .addParameter(FhirConstants.COMMON_SEARCH_HANDLER, FhirConstants.ID_PROPERTY, uuid)
		        .addParameter(FhirConstants.COMMON_SEARCH_HANDLER, FhirConstants.LAST_UPDATED_PROPERTY, lastUpdated);
		
		IBundleProvider results = search(theParams);
		
		assertThat(results, notNullValue());
		assertThat(results.size(), equalTo(0));
		
		List<Patient> resultList = get(results);
		
		assertThat(resultList, empty());
	}
	
	@Test
	public void searchForPatients_shouldReverseIncludeAllergiesWithReturnedResults() {
		TokenAndListParam uuid = new TokenAndListParam().addAnd(new TokenParam(PATIENT_OTHER1_UUID));
		HashSet<Include> revIncludes = new HashSet<>();
		revIncludes.add(new Include("AllergyIntolerance:patient"));
		
		SearchParameterMap theParams = new SearchParameterMap()
		        .addParameter(FhirConstants.COMMON_SEARCH_HANDLER, FhirConstants.ID_PROPERTY, uuid)
		        .addParameter(FhirConstants.REVERSE_INCLUDE_SEARCH_HANDLER, revIncludes);
		
		IBundleProvider results = search(theParams);
		
		assertThat(results, notNullValue());
		assertThat(results.size(), equalTo(1));
		
		List<IBaseResource> resultList = results.getResources(START_INDEX, END_INDEX);
		
		assertThat(results, notNullValue());
		assertThat(resultList.size(), equalTo(2)); // reverse included resource added as part of the result list
		assertThat(resultList.get(1), allOf(is(instanceOf(AllergyIntolerance.class)),
		    hasProperty("patient", hasProperty("referenceElement", hasProperty("idPart", equalTo(PATIENT_OTHER1_UUID))))));
	}
	
	@Test
	public void searchForPatients_shouldReverseIncludeDiagnosticReportsWithReturnedResults() {
		TokenAndListParam uuid = new TokenAndListParam().addAnd(new TokenParam(PATIENT_OTHER1_UUID));
		HashSet<Include> revIncludes = new HashSet<>();
		revIncludes.add(new Include("DiagnosticReport:patient"));
		
		SearchParameterMap theParams = new SearchParameterMap()
		        .addParameter(FhirConstants.COMMON_SEARCH_HANDLER, FhirConstants.ID_PROPERTY, uuid)
		        .addParameter(FhirConstants.REVERSE_INCLUDE_SEARCH_HANDLER, revIncludes);
		
		IBundleProvider results = search(theParams);
		
		assertThat(results, notNullValue());
		assertThat(results.size(), equalTo(1));
		
		List<IBaseResource> resultList = results.getResources(START_INDEX, END_INDEX);
		
		assertThat(results, notNullValue());
		assertThat(resultList.size(), equalTo(2)); // reverse included resource added as part of the result list
		assertThat(resultList.get(1), allOf(is(instanceOf(DiagnosticReport.class)),
		    hasProperty("subject", hasProperty("referenceElement", hasProperty("idPart", equalTo(PATIENT_OTHER1_UUID))))));
	}
	
	@Test
	public void searchForPatients_shouldReverseIncludeEncountersWithReturnedResults() {
		TokenAndListParam uuid = new TokenAndListParam().addAnd(new TokenParam(PATIENT_OTHER2_UUID));
		HashSet<Include> revIncludes = new HashSet<>();
		revIncludes.add(new Include("Encounter:patient"));
		
		SearchParameterMap theParams = new SearchParameterMap()
		        .addParameter(FhirConstants.COMMON_SEARCH_HANDLER, FhirConstants.ID_PROPERTY, uuid)
		        .addParameter(FhirConstants.REVERSE_INCLUDE_SEARCH_HANDLER, revIncludes);
		
		IBundleProvider results = search(theParams);
		
		assertThat(results, notNullValue());
		assertThat(results.size(), equalTo(1));
		
		List<IBaseResource> resultList = results.getResources(START_INDEX, END_INDEX);
		
		assertThat(results, notNullValue());
		assertThat(resultList.size(), equalTo(4)); // reverse included resources added as part of the result list
		assertThat(resultList.subList(1, 4), everyItem(allOf(is(instanceOf(Encounter.class)),
		    hasProperty("subject", hasProperty("referenceElement", hasProperty("idPart", equalTo(PATIENT_OTHER2_UUID)))))));
	}
	
	@Test
	public void searchForPatients_shouldReverseIncludeObservationsWithReturnedResults() {
		TokenAndListParam uuid = new TokenAndListParam().addAnd(new TokenParam(PATIENT_OTHER2_UUID));
		HashSet<Include> revIncludes = new HashSet<>();
		revIncludes.add(new Include("Observation:patient"));
		
		SearchParameterMap theParams = new SearchParameterMap()
		        .addParameter(FhirConstants.COMMON_SEARCH_HANDLER, FhirConstants.ID_PROPERTY, uuid)
		        .addParameter(FhirConstants.REVERSE_INCLUDE_SEARCH_HANDLER, revIncludes);
		
		IBundleProvider results = search(theParams);
		
		assertThat(results, notNullValue());
		assertThat(results.size(), equalTo(1));
		
		List<IBaseResource> resultList = results.getResources(START_INDEX, END_INDEX);
		
		assertThat(results, notNullValue());
		assertThat(resultList.size(), equalTo(10)); // reverse included resources added as part of the result list
		assertThat(resultList.subList(1, 10), everyItem(allOf(is(instanceOf(Observation.class)),
		    hasProperty("subject", hasProperty("referenceElement", hasProperty("idPart", equalTo(PATIENT_OTHER2_UUID)))))));
	}
	
	@Test
	public void searchForPatients_shouldReverseIncludeMedicationRequestsWithReturnedResults() {
		TokenAndListParam uuid = new TokenAndListParam().addAnd(new TokenParam(PATIENT_OTHER3_UUID));
		HashSet<Include> revIncludes = new HashSet<>();
		revIncludes.add(new Include("MedicationRequest:patient"));
		
		SearchParameterMap theParams = new SearchParameterMap()
		        .addParameter(FhirConstants.COMMON_SEARCH_HANDLER, FhirConstants.ID_PROPERTY, uuid)
		        .addParameter(FhirConstants.REVERSE_INCLUDE_SEARCH_HANDLER, revIncludes);
		
		IBundleProvider results = search(theParams);
		
		assertThat(results, notNullValue());
		assertThat(results.size(), equalTo(1));
		
		List<IBaseResource> resultList = results.getResources(START_INDEX, END_INDEX);
		
		assertThat(results, notNullValue());
		assertThat(resultList.size(), equalTo(9)); // reverse included resources added as part of the result list
		assertThat(resultList.subList(1, 9), everyItem(allOf(is(instanceOf(MedicationRequest.class)),
		    hasProperty("subject", hasProperty("referenceElement", hasProperty("idPart", equalTo(PATIENT_OTHER3_UUID)))))));
	}
	
	@Test
	public void searchForPatients_shouldReverseIncludeServiceRequestsWithReturnedResults() {
		TokenAndListParam uuid = new TokenAndListParam().addAnd(new TokenParam(PATIENT_OTHER3_UUID));
		HashSet<Include> revIncludes = new HashSet<>();
		revIncludes.add(new Include("ServiceRequest:patient"));
		
		SearchParameterMap theParams = new SearchParameterMap()
		        .addParameter(FhirConstants.COMMON_SEARCH_HANDLER, FhirConstants.ID_PROPERTY, uuid)
		        .addParameter(FhirConstants.REVERSE_INCLUDE_SEARCH_HANDLER, revIncludes);
		
		IBundleProvider results = search(theParams);
		
		assertThat(results, notNullValue());
		assertThat(results.size(), equalTo(1));
		
		List<IBaseResource> resultList = results.getResources(START_INDEX, END_INDEX);
		
		assertThat(results, notNullValue());
		assertThat(resultList.size(), equalTo(4)); // reverse included resources added as part of the result list
		assertThat(resultList.subList(1, 4), everyItem(allOf(is(instanceOf(ServiceRequest.class)),
		    hasProperty("subject", hasProperty("referenceElement", hasProperty("idPart", equalTo(PATIENT_OTHER3_UUID)))))));
	}
	
	@Test
	public void searchForPatients_shouldReverseIncludeProcedureRequestsWithReturnedResults() {
		TokenAndListParam uuid = new TokenAndListParam().addAnd(new TokenParam(PATIENT_OTHER3_UUID));
		HashSet<Include> revIncludes = new HashSet<>();
		revIncludes.add(new Include("ProcedureRequest:patient"));
		
		SearchParameterMap theParams = new SearchParameterMap()
		        .addParameter(FhirConstants.COMMON_SEARCH_HANDLER, FhirConstants.ID_PROPERTY, uuid)
		        .addParameter(FhirConstants.REVERSE_INCLUDE_SEARCH_HANDLER, revIncludes);
		
		IBundleProvider results = search(theParams);
		
		assertThat(results, notNullValue());
		assertThat(results.size(), equalTo(1));
		
		List<IBaseResource> resultList = results.getResources(START_INDEX, END_INDEX);
		
		assertThat(results, notNullValue());
		assertThat(resultList.size(), equalTo(4)); // reverse included resources added as part of the result list
		assertThat(resultList.subList(1, 4), everyItem(allOf(is(instanceOf(ServiceRequest.class)),
		    hasProperty("subject", hasProperty("referenceElement", hasProperty("idPart", equalTo(PATIENT_OTHER3_UUID)))))));
	}
	
	@Test
	public void searchForPatients_shouldHandleMultipleReverseIncludes() {
		TokenAndListParam uuid = new TokenAndListParam().addAnd(new TokenParam(PATIENT_OTHER2_UUID));
		HashSet<Include> revIncludes = new HashSet<>();
		revIncludes.add(new Include("Encounter:patient"));
		revIncludes.add(new Include("Observation:patient"));
		
		SearchParameterMap theParams = new SearchParameterMap()
		        .addParameter(FhirConstants.COMMON_SEARCH_HANDLER, FhirConstants.ID_PROPERTY, uuid)
		        .addParameter(FhirConstants.REVERSE_INCLUDE_SEARCH_HANDLER, revIncludes);
		
		IBundleProvider results = search(theParams);
		
		assertThat(results, notNullValue());
		assertThat(results.size(), equalTo(1));
		
		List<IBaseResource> resultList = results.getResources(START_INDEX, END_INDEX);
		
		assertThat(results, notNullValue());
		assertThat(resultList.size(), equalTo(13)); // reverse included resources (3 encounters + 9 observations) added as part of the result list
		assertThat(resultList.subList(1, 13), everyItem(allOf(
		    anyOf(is(instanceOf(Encounter.class)), is(instanceOf(Observation.class))),
		    hasProperty("subject", hasProperty("referenceElement", hasProperty("idPart", equalTo(PATIENT_OTHER2_UUID)))))));
	}
	
	@Test
	public void shouldReturnCollectionOfPatientsSortedByName() {
		SortSpec sort = new SortSpec();
		sort.setParamName("name");
		sort.setOrder(SortOrderEnum.ASC);
		
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.NAME_SEARCH_HANDLER,
		    FhirConstants.FAMILY_PROPERTY, new StringAndListParam().addAnd(new StringParam(PATIENT_PARTIAL_FAMILY_NAME)))
		        .setSortSpec(sort);
		
		IBundleProvider results = search(theParams);
		
		assertThat(results, notNullValue());
		assertThat(results.size(), greaterThan(1));
		
		List<Patient> resultList = get(results);
		
		assertThat(resultList, hasSize(greaterThan(1)));
		
		for (int i = 1; i < resultList.size(); i++) {
			assertThat(resultList.get(i - 1).getName().get(0).getNameAsSingleString(),
			    lessThanOrEqualTo(resultList.get(i).getName().get(0).getNameAsSingleString()));
		}
		
		sort.setOrder(SortOrderEnum.DESC);
		
		theParams.setSortSpec(sort);
		
		results = search(theParams);
		
		assertThat(results, notNullValue());
		assertThat(results.size(), greaterThan(1));
		
		resultList = get(results);
		
		assertThat(resultList, hasSize(greaterThan(1)));
		
		for (int i = 1; i < resultList.size(); i++) {
			assertThat(resultList.get(i - 1).getName().get(0).getNameAsSingleString(),
			    greaterThanOrEqualTo(resultList.get(i).getName().get(0).getNameAsSingleString()));
		}
	}
	
	@Test
	public void shouldReturnCollectionOfPatientsSortedByGivenName() {
		SortSpec sort = new SortSpec();
		sort.setParamName(SP_GIVEN);
		sort.setOrder(SortOrderEnum.ASC);
		
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.NAME_SEARCH_HANDLER,
		    FhirConstants.GIVEN_PROPERTY, new StringAndListParam().addAnd(new StringParam(PATIENT_PARTIAL_GIVEN_NAME)))
		        .setSortSpec(sort);
		
		IBundleProvider results = search(theParams);
		
		assertThat(results, notNullValue());
		assertThat(results.size(), greaterThan(1));
		
		List<Patient> resultList = get(results);
		
		assertThat(resultList, hasSize(greaterThan(1)));
		
		for (int i = 1; i < resultList.size(); i++) {
			assertThat(resultList.get(i - 1).getName().get(0).getGivenAsSingleString(),
			    lessThanOrEqualTo(resultList.get(i).getName().get(0).getGivenAsSingleString()));
		}
		
		sort.setOrder(SortOrderEnum.DESC);
		
		theParams.setSortSpec(sort);
		
		results = search(theParams);
		
		assertThat(results, notNullValue());
		assertThat(results.size(), greaterThan(1));
		
		resultList = get(results);
		
		assertThat(resultList, hasSize(greaterThan(1)));
		
		for (int i = 1; i < resultList.size(); i++) {
			assertThat(resultList.get(i - 1).getName().get(0).getGivenAsSingleString(),
			    greaterThanOrEqualTo(resultList.get(i).getName().get(0).getGivenAsSingleString()));
		}
	}
	
	@Test
	public void shouldReturnCollectionOfPatientsSortedByFamilyName() {
		SortSpec sort = new SortSpec();
		sort.setParamName(SP_FAMILY);
		sort.setOrder(SortOrderEnum.ASC);
		
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.NAME_SEARCH_HANDLER,
		    FhirConstants.FAMILY_PROPERTY, new StringAndListParam().addAnd(new StringParam(PATIENT_PARTIAL_FAMILY_NAME)))
		        .setSortSpec(sort);
		
		IBundleProvider results = search(theParams);
		
		assertThat(results, notNullValue());
		assertThat(results.size(), greaterThan(1));
		
		List<Patient> resultList = get(results);
		
		assertThat(resultList, hasSize(greaterThan(1)));
		
		for (int i = 1; i < resultList.size(); i++) {
			assertThat(resultList.get(i - 1).getName().get(0).getFamily(),
			    lessThanOrEqualTo(resultList.get(i).getName().get(0).getFamily()));
		}
		
		sort.setOrder(SortOrderEnum.DESC);
		
		theParams.setSortSpec(sort);
		
		results = search(theParams);
		
		assertThat(results, notNullValue());
		assertThat(results.size(), greaterThan(1));
		
		resultList = get(results);
		
		assertThat(resultList, hasSize(greaterThan(1)));
		
		for (int i = 1; i < resultList.size(); i++) {
			assertThat(resultList.get(i - 1).getName().get(0).getFamily(),
			    greaterThanOrEqualTo(resultList.get(i).getName().get(0).getFamily()));
		}
	}
	
	@Test
	public void shouldReturnCollectionOfPatientsSortedByBirthDate() {
		SortSpec sort = new SortSpec();
		sort.setParamName("birthdate");
		sort.setOrder(SortOrderEnum.ASC);
		
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.DATE_RANGE_SEARCH_HANDLER,
		    "birthdate", new DateRangeParam().setLowerBound(PATIENT_BIRTHDATE_LOWER_BOUND)).setSortSpec(sort);
		
		IBundleProvider results = search(theParams);
		
		assertThat(results, notNullValue());
		assertThat(results.size(), greaterThan(1));
		
		List<Patient> resultList = get(results);
		
		assertThat(resultList, hasSize(greaterThan(1)));
		
		for (int i = 1; i < resultList.size(); i++) {
			assertThat(resultList.get(i - 1).getBirthDate(), lessThanOrEqualTo(resultList.get(i).getBirthDate()));
		}
		
		sort.setOrder(SortOrderEnum.DESC);
		
		theParams.setSortSpec(sort);
		
		results = search(theParams);
		
		assertThat(results, notNullValue());
		assertThat(results.size(), greaterThan(1));
		
		resultList = get(results);
		
		assertThat(resultList, hasSize(greaterThan(1)));
		
		for (int i = 1; i < resultList.size(); i++) {
			assertThat(resultList.get(i - 1).getBirthDate(), greaterThanOrEqualTo(resultList.get(i).getBirthDate()));
		}
	}
	
	@Test
	public void shouldReturnCollectionOfPatientsSortedByCity() {
		SortSpec sort = new SortSpec();
		sort.setParamName("address-city");
		sort.setOrder(SortOrderEnum.ASC);
		
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.ADDRESS_SEARCH_HANDLER,
		    FhirConstants.CITY_PROPERTY, new StringAndListParam().addAnd(new StringParam("City"))).setSortSpec(sort);
		
		IBundleProvider results = search(theParams);
		
		assertThat(results, notNullValue());
		assertThat(results.size(), greaterThan(1));
		
		List<Patient> resultList = get(results);
		
		assertThat(resultList, hasSize(greaterThan(1)));
		
		for (int i = 1; i < resultList.size(); i++) {
			assertThat(resultList.get(i - 1).getAddressFirstRep().getCity(),
			    lessThanOrEqualTo(resultList.get(i).getAddressFirstRep().getCity()));
		}
		
		sort.setOrder(SortOrderEnum.DESC);
		
		theParams.setSortSpec(sort);
		
		results = search(theParams);
		
		assertThat(results, notNullValue());
		assertThat(results.size(), greaterThan(1));
		
		resultList = get(results);
		
		assertThat(resultList, hasSize(greaterThan(1)));
		
		for (int i = 1; i < resultList.size(); i++) {
			assertThat(resultList.get(i - 1).getAddressFirstRep().getCity(),
			    greaterThanOrEqualTo(resultList.get(i).getAddressFirstRep().getCity()));
		}
	}
	
	@Test
	public void shouldReturnCollectionOfPatientsSortedByState() {
		SortSpec sort = new SortSpec();
		sort.setParamName("address-state");
		sort.setOrder(SortOrderEnum.ASC);
		
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.ADDRESS_SEARCH_HANDLER,
		    FhirConstants.STATE_PROPERTY, new StringAndListParam().addAnd(new StringParam("M"))).setSortSpec(sort);
		
		IBundleProvider results = search(theParams);
		
		assertThat(results, notNullValue());
		assertThat(results.size(), greaterThan(1));
		
		List<Patient> resultList = get(results);
		
		assertThat(resultList, hasSize(greaterThan(1)));
		
		for (int i = 1; i < resultList.size(); i++) {
			assertThat(resultList.get(i - 1).getAddressFirstRep().getState(),
			    lessThanOrEqualTo(resultList.get(i).getAddressFirstRep().getState()));
		}
		
		sort.setOrder(SortOrderEnum.DESC);
		
		theParams.setSortSpec(sort);
		
		results = search(theParams);
		
		assertThat(results, notNullValue());
		assertThat(results.size(), greaterThan(1));
		
		resultList = get(results);
		
		assertThat(resultList, hasSize(greaterThan(1)));
		
		for (int i = 1; i < resultList.size(); i++) {
			assertThat(resultList.get(i - 1).getAddressFirstRep().getState(),
			    greaterThanOrEqualTo(resultList.get(i).getAddressFirstRep().getState()));
		}
	}
	
	@Test
	public void shouldReturnCollectionOfPatientsSortedByPostalCode() {
		SortSpec sort = new SortSpec();
		sort.setParamName("address-postalcode");
		sort.setOrder(SortOrderEnum.ASC);
		
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.ADDRESS_SEARCH_HANDLER,
		    FhirConstants.POSTAL_CODE_PROPERTY, new StringAndListParam().addAnd(new StringParam("0"))).setSortSpec(sort);
		
		IBundleProvider results = search(theParams);
		
		assertThat(results, notNullValue());
		assertThat(results.size(), greaterThan(1));
		
		List<Patient> resultList = get(results);
		
		assertThat(resultList, hasSize(greaterThan(1)));
		
		for (int i = 1; i < resultList.size(); i++) {
			assertThat(resultList.get(i - 1).getAddressFirstRep().getPostalCode(),
			    lessThanOrEqualTo(resultList.get(i).getAddressFirstRep().getPostalCode()));
		}
		
		sort.setOrder(SortOrderEnum.DESC);
		
		theParams.setSortSpec(sort);
		
		results = search(theParams);
		
		assertThat(results, notNullValue());
		assertThat(results.size(), greaterThan(1));
		
		resultList = get(results);
		
		assertThat(resultList, hasSize(greaterThan(1)));
		
		for (int i = 1; i < resultList.size(); i++) {
			assertThat(resultList.get(i - 1).getAddressFirstRep().getPostalCode(),
			    greaterThanOrEqualTo(resultList.get(i).getAddressFirstRep().getPostalCode()));
		}
	}
	
	@Test
	public void shouldReturnCollectionOfPatientsSortedByCountry() {
		SortSpec sort = new SortSpec();
		sort.setParamName("address-country");
		sort.setOrder(SortOrderEnum.ASC);
		
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.ADDRESS_SEARCH_HANDLER,
		    FhirConstants.COUNTRY_PROPERTY, new StringAndListParam().addAnd(new StringParam("Fake"))).setSortSpec(sort);
		
		IBundleProvider results = search(theParams);
		
		assertThat(results, notNullValue());
		assertThat(results.size(), greaterThan(1));
		
		List<Patient> resultList = get(results);
		
		assertThat(resultList, hasSize(greaterThan(1)));
		
		for (int i = 1; i < resultList.size(); i++) {
			assertThat(resultList.get(i - 1).getAddressFirstRep().getCountry(),
			    lessThanOrEqualTo(resultList.get(i).getAddressFirstRep().getCountry()));
		}
		
		sort.setOrder(SortOrderEnum.DESC);
		
		theParams.setSortSpec(sort);
		
		results = search(theParams);
		
		assertThat(results, notNullValue());
		assertThat(results.size(), greaterThan(1));
		
		resultList = get(results);
		
		assertThat(resultList, hasSize(greaterThan(1)));
		
		for (int i = 1; i < resultList.size(); i++) {
			assertThat(resultList.get(i - 1).getAddressFirstRep().getCountry(),
			    greaterThanOrEqualTo(resultList.get(i).getAddressFirstRep().getCountry()));
		}
	}
	
	@Test
	public void searchForPatient_shouldReturnPatientEverything() {
		TokenAndListParam patientId = new TokenAndListParam().addAnd(new TokenParam().setValue(PATIENT_OTHER2_UUID));
		
		SearchParameterMap theParams = new SearchParameterMap()
		        .addParameter(FhirConstants.EVERYTHING_SEARCH_HANDLER, new StringParam())
		        .addParameter(FhirConstants.COMMON_SEARCH_HANDLER, FhirConstants.ID_PROPERTY, patientId);
		
		HashSet<Include> revIncludes = new HashSet<>();
		
		revIncludes.add(new Include("Observation:" + Observation.SP_PATIENT));
		revIncludes.add(new Include("AllergyIntolerance:" + AllergyIntolerance.SP_PATIENT));
		revIncludes.add(new Include("DiagnosticReport:" + DiagnosticReport.SP_PATIENT));
		revIncludes.add(new Include("Encounter:" + Encounter.SP_PATIENT));
		revIncludes.add(new Include("MedicationRequest:" + MedicationRequest.SP_PATIENT));
		revIncludes.add(new Include("ServiceRequest:" + ServiceRequest.SP_PATIENT));
		revIncludes.add(new Include("ProcedureRequest:" + Procedure.SP_PATIENT));
		
		theParams.addParameter(FhirConstants.REVERSE_INCLUDE_SEARCH_HANDLER, revIncludes);
		
		IBundleProvider results = search(theParams);
		
		assertThat(results, notNullValue());
		assertThat(results.size(), equalTo(15));
		
		List<IBaseResource> resultList = getAllResources(results);
		
		assertThat(resultList.size(), equalTo(15));
	}
	
}
