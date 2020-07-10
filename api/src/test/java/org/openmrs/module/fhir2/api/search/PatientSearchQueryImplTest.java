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
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hl7.fhir.r4.model.Patient.SP_FAMILY;
import static org.hl7.fhir.r4.model.Patient.SP_GIVEN;

import java.util.List;

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
import org.hl7.fhir.r4.model.Enumerations;
import org.hl7.fhir.r4.model.Patient;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.TestFhirSpringConfiguration;
import org.openmrs.module.fhir2.api.dao.FhirPatientDao;
import org.openmrs.module.fhir2.api.search.param.SearchParameterMap;
import org.openmrs.module.fhir2.api.translators.PatientTranslator;
import org.openmrs.test.BaseModuleContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

@ContextConfiguration(classes = TestFhirSpringConfiguration.class, inheritLocations = false)
public class PatientSearchQueryImplTest extends BaseModuleContextSensitiveTest {
	
	private static final String[] PATIENT_SEARCH_DATA_FILES = {
	        "org/openmrs/api/include/PatientServiceTest-findPatients.xml",
	        "org/openmrs/module/fhir2/api/dao/impl/FhirPatientDaoImplTest_address_data.xml" };
	
	private static final String PATIENT_GIVEN_NAME = "Jeannette";
	
	private static final String PATIENT_UUID = "54569b76-6862-11ea-bc55-0242ac130003";
	
	private static final String PATIENT_PARTIAL_GIVEN_NAME = "Jean";
	
	private static final String PATIENT_FAMILY_NAME = "Claudent";
	
	private static final String PATIENT_PARTIAL_FAMILY_NAME = "Claud";
	
	private static final String PATIENT_NOT_FOUND_NAME = "Igor";
	
	private static final String PATIENT_IDENTIFIER = "563422";
	
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
	
	private static final String DATE_VOIDED = "2010-09-03";
	
	@Autowired
	private PatientTranslator translator;
	
	@Autowired
	private FhirPatientDao dao;
	
	@Autowired
	private SearchQueryInclude<Patient> searchQueryInclude;
	
	@Autowired
	private SearchQuery<org.openmrs.Patient, Patient, FhirPatientDao, PatientTranslator, SearchQueryInclude<Patient>> searchQuery;
	
	private static final int START_INDEX = 0;
	
	private static final int END_INDEX = 10;
	
	@Before
	public void setup() throws Exception {
		for (String search_data : PATIENT_SEARCH_DATA_FILES) {
			executeDataSet(search_data);
		}
	}
	
	private IBundleProvider search(SearchParameterMap theParams) {
		return searchQuery.getQueryResults(theParams, dao, translator, searchQueryInclude);
	}
	
	private List<IBaseResource> get(IBundleProvider results) {
		return results.getResources(START_INDEX, END_INDEX);
	}
	
	@Test
	public void searchForPatients_shouldSearchForPatientsByName() {
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.NAME_SEARCH_HANDLER, "name",
		    new StringAndListParam().addAnd(new StringParam(PATIENT_GIVEN_NAME)));
		IBundleProvider results = search(theParams);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList, not(empty()));
		assertThat(resultList, hasSize(greaterThan(1)));
	}
	
	@Test
	public void searchForPatients_shouldReturnMultiplePatientsForPartialMatch() {
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.NAME_SEARCH_HANDLER, "name",
		    new StringAndListParam().addAnd(new StringParam(PATIENT_PARTIAL_GIVEN_NAME)));
		IBundleProvider results = search(theParams);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList, not(empty()));
		assertThat(results.size(), greaterThan(1));
	}
	
	@Test
	public void searchForPatients_shouldReturnEmptyCollectionWhenPatientNameNotMatched() {
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.NAME_SEARCH_HANDLER,
		    FhirConstants.NAME_PROPERTY, new StringAndListParam().addAnd(new StringParam(PATIENT_NOT_FOUND_NAME)));
		IBundleProvider results = search(theParams);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList, empty());
	}
	
	@Test
	public void searchForPatients_shouldSearchForPatientsByGivenName() {
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.NAME_SEARCH_HANDLER,
		    FhirConstants.GIVEN_PROPERTY, new StringAndListParam().addAnd(new StringParam(PATIENT_GIVEN_NAME)));
		IBundleProvider results = search(theParams);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList, not(empty()));
		assertThat(resultList, hasItems());
	}
	
	@Test
	public void searchForPatients_shouldReturnMultiplePatientsForPartialMatchOnGivenName() {
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.NAME_SEARCH_HANDLER,
		    FhirConstants.GIVEN_PROPERTY, new StringAndListParam().addAnd(new StringParam(PATIENT_PARTIAL_GIVEN_NAME)));
		IBundleProvider results = search(theParams);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList, not(empty()));
		assertThat(results.size(), greaterThan(1));
	}
	
	@Test
	public void searchForPatients_shouldReturnEmptyCollectionWhenPatientGivenNameNotMatched() {
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.NAME_SEARCH_HANDLER,
		    FhirConstants.GIVEN_PROPERTY, new StringAndListParam().addAnd(new StringParam(PATIENT_NOT_FOUND_NAME)));
		IBundleProvider results = search(theParams);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList, is(empty()));
	}
	
	@Test
	public void searchForPatients_shouldSearchForPatientsByFamilyName() {
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.NAME_SEARCH_HANDLER,
		    FhirConstants.FAMILY_PROPERTY, new StringAndListParam().addAnd(new StringParam(PATIENT_FAMILY_NAME)));
		IBundleProvider results = search(theParams);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList, not(empty()));
		assertThat(resultList, hasSize(greaterThanOrEqualTo(1)));
	}
	
	@Test
	public void searchForPatients_shouldReturnMultiplePatientsForPartialMatchOnFamilyName() {
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.NAME_SEARCH_HANDLER,
		    FhirConstants.FAMILY_PROPERTY, new StringAndListParam().addAnd(new StringParam(PATIENT_PARTIAL_FAMILY_NAME)));
		IBundleProvider results = search(theParams);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList, not(empty()));
		assertThat(results.size(), greaterThan(1));
	}
	
	@Test
	public void searchForPatients_shouldReturnEmptyCollectionWhenPatientFamilyNameNotMatched() {
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.NAME_SEARCH_HANDLER,
		    FhirConstants.FAMILY_PROPERTY, new StringAndListParam().addAnd(new StringParam(PATIENT_NOT_FOUND_NAME)));
		IBundleProvider results = search(theParams);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList, empty());
	}
	
	@Test
	public void searchForPatients_shouldSearchForPatientsByIdentifier() {
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.IDENTIFIER_SEARCH_HANDLER,
		    new TokenAndListParam().addAnd(new TokenParam(null, PATIENT_IDENTIFIER)));
		IBundleProvider results = search(theParams);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList, not(empty()));
		assertThat(resultList, hasSize(greaterThanOrEqualTo(1)));
	}
	
	@Test
	public void searchForPatients_shouldSearchForPatientsByIdentifierWithType() {
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.IDENTIFIER_SEARCH_HANDLER,
		    new TokenAndListParam().addAnd(new TokenParam(PATIENT_IDENTIFIER_TYPE, PATIENT_IDENTIFIER)));
		IBundleProvider results = search(theParams);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList, not(empty()));
		assertThat(resultList, hasSize(greaterThanOrEqualTo(1)));
	}
	
	@Test
	public void searchForPatients_shouldReturnEmptyCollectionWhenIdentifierNotMatched() {
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.IDENTIFIER_SEARCH_HANDLER,
		    new TokenAndListParam().addAnd(new TokenParam(BAD_PATIENT_IDENTIFIER_TYPE, BAD_PATIENT_IDENTIFIER)));
		IBundleProvider results = search(theParams);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList, is(empty()));
	}
	
	@Test
	public void searchForPatients_shouldReturnEmptyCollectionWhenIdentifierTypeNotMatched() {
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.IDENTIFIER_SEARCH_HANDLER,
		    new TokenAndListParam().addAnd(new TokenParam(BAD_PATIENT_IDENTIFIER_TYPE, PATIENT_IDENTIFIER)));
		IBundleProvider results = search(theParams);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList, is(empty()));
	}
	
	@Test
	public void searchForPatients_shouldReturnPatientsByGender() {
		final String GENDER_PROPERTY = "gender";
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.GENDER_SEARCH_HANDLER, "gender",
		    new TokenAndListParam().addAnd(new TokenParam(PATIENT_MALE_GENDER)));
		
		IBundleProvider results = search(theParams);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList, not(empty()));
		assertThat(resultList, hasItems(hasProperty(GENDER_PROPERTY, equalTo(Enumerations.AdministrativeGender.MALE))));
		
		results = search(new SearchParameterMap().addParameter(FhirConstants.GENDER_SEARCH_HANDLER, "gender",
		    new TokenAndListParam().addAnd(new TokenParam(PATIENT_FEMALE_GENDER))));
		
		resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList, not(empty()));
		assertThat(resultList, hasItems(hasProperty(GENDER_PROPERTY, equalTo(Enumerations.AdministrativeGender.FEMALE))));
	}
	
	@Test
	public void searchForPatients_shouldReturnEmptyCollectionWhenGenderNotMatched() {
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.GENDER_SEARCH_HANDLER, "gender",
		    new TokenAndListParam().addAnd(new TokenParam(null, PATIENT_WRONG_GENDER)));
		IBundleProvider results = search(theParams);
		
		assertThat(results, notNullValue());
		assertThat(results.isEmpty(), is(true));
	}
	
	@Test
	public void searchForPatients_shouldSearchForPatientsByBirthDate() {
		IBundleProvider results = search(new SearchParameterMap().addParameter(FhirConstants.DATE_RANGE_SEARCH_HANDLER,
		    "birthdate", new DateRangeParam(new DateParam(PATIENT_BIRTHDATE))));
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList, not(empty()));
		assertThat(resultList, hasItem(hasProperty("id", equalTo(PATIENT_BIRTHDATE_PATIENT_UUID))));
	}
	
	@Test
	public void searchForPatients_shouldSearchForPatientsByBirthDateWithLowerBound() {
		IBundleProvider results = search(new SearchParameterMap().addParameter(FhirConstants.DATE_RANGE_SEARCH_HANDLER,
		    "birthdate", new DateRangeParam().setLowerBound(PATIENT_BIRTHDATE)));
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList, not(empty()));
		assertThat(resultList, hasSize(greaterThan(1)));
		assertThat(resultList, hasItem(hasProperty("id", equalTo(PATIENT_BIRTHDATE_PATIENT_UUID))));
	}
	
	@Test
	public void searchForPatients_shouldSearchForPatientsByBirthDateWithUpperBound() {
		IBundleProvider results = search(new SearchParameterMap().addParameter(FhirConstants.DATE_RANGE_SEARCH_HANDLER,
		    "birthdate", new DateRangeParam().setUpperBound(PATIENT_BIRTHDATE)));
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList, not(empty()));
		assertThat(resultList, hasSize(greaterThan(1)));
		assertThat(resultList, hasItem(hasProperty("id", equalTo(PATIENT_BIRTHDATE_PATIENT_UUID))));
	}
	
	@Test
	public void searchForPatients_shouldSearchForPatientsByBirthDateWithinBoundaries() {
		IBundleProvider results = search(
		    new SearchParameterMap().addParameter(FhirConstants.DATE_RANGE_SEARCH_HANDLER, "birthdate",
		        new DateRangeParam().setLowerBound(PATIENT_BIRTHDATE_LOWER_BOUND).setUpperBound(PATIENT_BIRTHDATE)));
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList, not(empty()));
		assertThat(resultList, hasSize(greaterThan(1)));
		assertThat(resultList, hasItem(hasProperty("id", equalTo(PATIENT_BIRTHDATE_PATIENT_UUID))));
	}
	
	@Test
	public void searchForPatients_shouldSearchForPatientsByCity() {
		IBundleProvider results = search(new SearchParameterMap().addParameter(FhirConstants.ADDRESS_SEARCH_HANDLER,
		    FhirConstants.CITY_PROPERTY, new StringAndListParam().addAnd(new StringParam(PATIENT_ADDRESS_CITY))));
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList, not(empty()));
		assertThat(resultList.iterator().next().getIdElement().getIdPart(), equalTo(PATIENT_ADDRESS_PATIENT_UUID));
	}
	
	@Test
	public void searchForPatients_shouldSearchForPatientsByState() {
		IBundleProvider results = search(new SearchParameterMap().addParameter(FhirConstants.ADDRESS_SEARCH_HANDLER,
		    FhirConstants.STATE_PROPERTY, new StringAndListParam().addAnd(new StringParam(PATIENT_ADDRESS_STATE))));
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList, not(empty()));
		assertThat(resultList.iterator().next().getIdElement().getIdPart(), equalTo(PATIENT_ADDRESS_PATIENT_UUID));
	}
	
	@Test
	public void searchForPatients_shouldSearchForPatientsByCountry() {
		IBundleProvider results = search(new SearchParameterMap().addParameter(FhirConstants.ADDRESS_SEARCH_HANDLER,
		    FhirConstants.COUNTRY_PROPERTY, new StringAndListParam().addAnd(new StringParam(PATIENT_ADDRESS_COUNTRY))));
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList, not(empty()));
		assertThat(resultList.iterator().next().getIdElement().getIdPart(), equalTo(PATIENT_ADDRESS_PATIENT_UUID));
	}
	
	@Test
	public void searchForPatients_shouldSearchForPatientsByPostalCode() {
		IBundleProvider results = search(
		    new SearchParameterMap().addParameter(FhirConstants.ADDRESS_SEARCH_HANDLER, FhirConstants.POSTAL_CODE_PROPERTY,
		        new StringAndListParam().addAnd(new StringParam(PATIENT_ADDRESS_POSTAL_CODE))));
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList, not(empty()));
		assertThat(resultList.iterator().next().getIdElement().getIdPart(), equalTo(PATIENT_ADDRESS_PATIENT_UUID));
	}
	
	@Test
	public void searchForPatients_shouldSearchForPatientsByUuid() {
		TokenAndListParam uuid = new TokenAndListParam().addAnd(new TokenParam(PATIENT_UUID));
		
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.COMMON_SEARCH_HANDLER,
		    FhirConstants.ID_PROPERTY, uuid);
		
		IBundleProvider results = search(theParams);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList, not(empty()));
		assertThat(resultList.size(), equalTo(1));
		assertThat(((Patient) resultList.iterator().next()).getIdElement().getIdPart(), equalTo(PATIENT_UUID));
	}
	
	@Test
	public void searchForPatients_shouldSearchForPatientsByLastUpdatedDateCreated() {
		DateRangeParam lastUpdated = new DateRangeParam().setUpperBound(DATE_CREATED).setLowerBound(DATE_CREATED);
		
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.COMMON_SEARCH_HANDLER,
		    FhirConstants.LAST_UPDATED_PROPERTY, lastUpdated);
		
		IBundleProvider results = search(theParams);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList, not(empty()));
		assertThat(resultList.size(), equalTo(5));
	}
	
	@Test
	public void searchForPatients_shouldSearchForPatientsByLastUpdatedDateChanged() {
		DateRangeParam lastUpdated = new DateRangeParam().setUpperBound(DATE_CHANGED).setLowerBound(DATE_CHANGED);
		
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.COMMON_SEARCH_HANDLER,
		    FhirConstants.LAST_UPDATED_PROPERTY, lastUpdated);
		
		IBundleProvider results = search(theParams);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList, not(empty()));
		assertThat(resultList.size(), equalTo(6));
	}
	
	@Test
	public void searchForPatients_shouldSearchForPatientsByLastUpdatedDateVoided() {
		DateRangeParam lastUpdated = new DateRangeParam().setUpperBound(DATE_VOIDED).setLowerBound(DATE_VOIDED);
		
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.COMMON_SEARCH_HANDLER,
		    FhirConstants.LAST_UPDATED_PROPERTY, lastUpdated);
		
		IBundleProvider results = search(theParams);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList, not(empty()));
		assertThat(resultList.size(), equalTo(1));
	}
	
	@Test
	public void searchForPatients_shouldSearchForPatientsByMatchingUuidAndLastUpdated() {
		TokenAndListParam uuid = new TokenAndListParam().addAnd(new TokenParam(PATIENT_UUID));
		DateRangeParam lastUpdated = new DateRangeParam().setUpperBound(DATE_VOIDED).setLowerBound(DATE_VOIDED);
		
		SearchParameterMap theParams = new SearchParameterMap()
		        .addParameter(FhirConstants.COMMON_SEARCH_HANDLER, FhirConstants.ID_PROPERTY, uuid)
		        .addParameter(FhirConstants.COMMON_SEARCH_HANDLER, FhirConstants.LAST_UPDATED_PROPERTY, lastUpdated);
		
		IBundleProvider results = search(theParams);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList, not(empty()));
		assertThat(resultList.size(), equalTo(1));
		assertThat(((Patient) resultList.iterator().next()).getIdElement().getIdPart(), equalTo(PATIENT_UUID));
	}
	
	@Test
	public void searchForPatients_shouldReturnEmptyListByMismatchingUuidAndLastUpdated() {
		TokenAndListParam uuid = new TokenAndListParam().addAnd(new TokenParam());
		DateRangeParam lastUpdated = new DateRangeParam().setUpperBound(DATE_CHANGED).setLowerBound(DATE_CHANGED);
		
		SearchParameterMap theParams = new SearchParameterMap()
		        .addParameter(FhirConstants.COMMON_SEARCH_HANDLER, FhirConstants.ID_PROPERTY, uuid)
		        .addParameter(FhirConstants.COMMON_SEARCH_HANDLER, FhirConstants.LAST_UPDATED_PROPERTY, lastUpdated);
		
		IBundleProvider results = search(theParams);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList, empty());
	}
	
	@Test
	public void shouldReturnCollectionOfPatientsSortedByName() {
		SortSpec sort = new SortSpec();
		sort.setParamName("name");
		sort.setOrder(SortOrderEnum.ASC);
		
		IBundleProvider results = search(
		    new SearchParameterMap().addParameter(FhirConstants.NAME_SEARCH_HANDLER, FhirConstants.FAMILY_PROPERTY,
		        new StringAndListParam().addAnd(new StringParam(PATIENT_PARTIAL_FAMILY_NAME))).setSortSpec(sort));
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList, not(empty()));
		
		for (int i = 1; i < resultList.size(); i++) {
			assertThat(((Patient) resultList.get(i - 1)).getName().get(0).getNameAsSingleString(),
			    lessThanOrEqualTo(((Patient) resultList.get(i)).getName().get(0).getNameAsSingleString()));
		}
		
		sort.setOrder(SortOrderEnum.DESC);
		
		results = search(
		    new SearchParameterMap().addParameter(FhirConstants.NAME_SEARCH_HANDLER, FhirConstants.FAMILY_PROPERTY,
		        new StringAndListParam().addAnd(new StringParam(PATIENT_PARTIAL_FAMILY_NAME))).setSortSpec(sort));
		
		resultList = get(results);
		
		for (int i = 1; i < resultList.size(); i++) {
			assertThat(((Patient) resultList.get(i - 1)).getName().get(0).getNameAsSingleString(),
			    greaterThanOrEqualTo(((Patient) resultList.get(i)).getName().get(0).getNameAsSingleString()));
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
		
		List<IBaseResource> resultList = get(results);
		
		for (int i = 1; i < resultList.size(); i++) {
			assertThat(((Patient) resultList.get(i - 1)).getName().get(0).getGivenAsSingleString(),
			    lessThanOrEqualTo(((Patient) resultList.get(i)).getName().get(0).getGivenAsSingleString()));
		}
		
		sort.setOrder(SortOrderEnum.DESC);
		
		results = search(
		    new SearchParameterMap().addParameter(FhirConstants.NAME_SEARCH_HANDLER, FhirConstants.GIVEN_PROPERTY,
		        new StringAndListParam().addAnd(new StringParam(PATIENT_PARTIAL_GIVEN_NAME))).setSortSpec(sort));
		
		resultList = get(results);
		
		for (int i = 1; i < resultList.size(); i++) {
			assertThat(((Patient) resultList.get(i - 1)).getName().get(0).getGivenAsSingleString(),
			    greaterThanOrEqualTo(((Patient) resultList.get(i)).getName().get(0).getGivenAsSingleString()));
		}
	}
	
	@Test
	public void shouldReturnCollectionOfPatientsSortedByFamilyName() {
		SortSpec sort = new SortSpec();
		sort.setParamName(SP_FAMILY);
		sort.setOrder(SortOrderEnum.ASC);
		
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.NAME_SEARCH_HANDLER,
		    FhirConstants.FAMILY_PROPERTY, new StringAndListParam().addAnd(new StringParam(PATIENT_FAMILY_NAME)))
		        .setSortSpec(sort);
		
		IBundleProvider results = search(theParams);
		
		List<IBaseResource> resultList = get(results);
		
		for (int i = 1; i < resultList.size(); i++) {
			assertThat(((Patient) resultList.get(i - 1)).getName().get(0).getFamily(),
			    lessThanOrEqualTo(((Patient) resultList.get(i)).getName().get(0).getFamily()));
		}
		
		sort.setOrder(SortOrderEnum.DESC);
		
		results = search(
		    new SearchParameterMap().addParameter(FhirConstants.NAME_SEARCH_HANDLER, FhirConstants.FAMILY_PROPERTY,
		        new StringAndListParam().addAnd(new StringParam(PATIENT_FAMILY_NAME))).setSortSpec(sort));
		
		resultList = get(results);
		
		for (int i = 1; i < resultList.size(); i++) {
			assertThat(((Patient) resultList.get(i - 1)).getName().get(0).getFamily(),
			    greaterThanOrEqualTo(((Patient) resultList.get(i)).getName().get(0).getFamily()));
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
		
		List<IBaseResource> resultList = get(results);
		
		for (int i = 1; i < resultList.size(); i++) {
			assertThat(((Patient) resultList.get(i - 1)).getBirthDate(),
			    lessThanOrEqualTo(((Patient) resultList.get(i)).getBirthDate()));
		}
		
		sort.setOrder(SortOrderEnum.DESC);
		
		results = search(new SearchParameterMap().addParameter(FhirConstants.DATE_RANGE_SEARCH_HANDLER, "birthdate",
		    new DateRangeParam().setLowerBound(PATIENT_BIRTHDATE_LOWER_BOUND)).setSortSpec(sort));
		
		resultList = get(results);
		
		for (int i = 1; i < resultList.size(); i++) {
			assertThat(((Patient) resultList.get(i - 1)).getBirthDate(),
			    greaterThanOrEqualTo(((Patient) resultList.get(i)).getBirthDate()));
		}
	}
	
	@Test
	public void shouldReturnCollectionOfPatientsSortedByCity() {
		SortSpec sort = new SortSpec();
		sort.setParamName("address-city");
		sort.setOrder(SortOrderEnum.ASC);
		
		IBundleProvider results = search(
		    new SearchParameterMap().addParameter(FhirConstants.ADDRESS_SEARCH_HANDLER, FhirConstants.CITY_PROPERTY,
		        new StringAndListParam().addAnd(new StringParam(PATIENT_ADDRESS_CITY))).setSortSpec(sort));
		
		List<IBaseResource> resultList = get(results);
		
		for (int i = 1; i < resultList.size(); i++) {
			assertThat(((Patient) resultList.get(i - 1)).getAddressFirstRep().getCity(),
			    lessThanOrEqualTo(((Patient) resultList.get(i)).getAddressFirstRep().getCity()));
		}
		
		sort.setOrder(SortOrderEnum.DESC);
		
		results = search(
		    new SearchParameterMap().addParameter(FhirConstants.ADDRESS_SEARCH_HANDLER, FhirConstants.CITY_PROPERTY,
		        new StringAndListParam().addAnd(new StringParam(PATIENT_ADDRESS_CITY))).setSortSpec(sort));
		
		resultList = get(results);
		
		for (int i = 1; i < resultList.size(); i++) {
			assertThat(((Patient) resultList.get(i - 1)).getAddressFirstRep().getCity(),
			    greaterThanOrEqualTo(((Patient) resultList.get(i)).getAddressFirstRep().getCity()));
		}
	}
	
	@Test
	public void shouldReturnCollectionOfPatientsSortedByState() {
		SortSpec sort = new SortSpec();
		sort.setParamName("address-state");
		sort.setOrder(SortOrderEnum.ASC);
		
		IBundleProvider results = search(
		    new SearchParameterMap().addParameter(FhirConstants.ADDRESS_SEARCH_HANDLER, FhirConstants.STATE_PROPERTY,
		        new StringAndListParam().addAnd(new StringParam(PATIENT_ADDRESS_STATE))).setSortSpec(sort));
		
		List<IBaseResource> resultList = get(results);
		
		for (int i = 1; i < resultList.size(); i++) {
			assertThat(((Patient) resultList.get(i - 1)).getAddressFirstRep().getState(),
			    lessThanOrEqualTo(((Patient) resultList.get(i)).getAddressFirstRep().getState()));
		}
		
		sort.setOrder(SortOrderEnum.DESC);
		
		results = search(
		    new SearchParameterMap().addParameter(FhirConstants.ADDRESS_SEARCH_HANDLER, FhirConstants.STATE_PROPERTY,
		        new StringAndListParam().addAnd(new StringParam(PATIENT_ADDRESS_STATE))).setSortSpec(sort));
		
		resultList = get(results);
		
		for (int i = 1; i < resultList.size(); i++) {
			assertThat(((Patient) resultList.get(i - 1)).getAddressFirstRep().getState(),
			    greaterThanOrEqualTo(((Patient) resultList.get(i)).getAddressFirstRep().getState()));
		}
	}
	
	@Test
	public void shouldReturnCollectionOfPatientsSortedByPostalCode() {
		SortSpec sort = new SortSpec();
		sort.setParamName("address-postalcode");
		sort.setOrder(SortOrderEnum.ASC);
		
		IBundleProvider results = search(
		    new SearchParameterMap().addParameter(FhirConstants.ADDRESS_SEARCH_HANDLER, FhirConstants.POSTAL_CODE_PROPERTY,
		        new StringAndListParam().addAnd(new StringParam(PATIENT_ADDRESS_POSTAL_CODE))).setSortSpec(sort));
		
		List<IBaseResource> resultList = get(results);
		
		for (int i = 1; i < resultList.size(); i++) {
			assertThat(((Patient) resultList.get(i - 1)).getAddressFirstRep().getPostalCode(),
			    lessThanOrEqualTo(((Patient) resultList.get(i)).getAddressFirstRep().getPostalCode()));
		}
		
		sort.setOrder(SortOrderEnum.DESC);
		
		results = search(
		    new SearchParameterMap().addParameter(FhirConstants.ADDRESS_SEARCH_HANDLER, FhirConstants.POSTAL_CODE_PROPERTY,
		        new StringAndListParam().addAnd(new StringParam(PATIENT_ADDRESS_POSTAL_CODE))).setSortSpec(sort));
		
		resultList = get(results);
		
		for (int i = 1; i < resultList.size(); i++) {
			assertThat(((Patient) resultList.get(i - 1)).getAddressFirstRep().getPostalCode(),
			    greaterThanOrEqualTo(((Patient) resultList.get(i)).getAddressFirstRep().getPostalCode()));
		}
	}
	
	@Test
	public void shouldReturnCollectionOfPatientsSortedByCountry() {
		SortSpec sort = new SortSpec();
		sort.setParamName("address-country");
		sort.setOrder(SortOrderEnum.ASC);
		
		IBundleProvider results = search(
		    new SearchParameterMap().addParameter(FhirConstants.ADDRESS_SEARCH_HANDLER, FhirConstants.COUNTRY_PROPERTY,
		        new StringAndListParam().addAnd(new StringParam(PATIENT_ADDRESS_COUNTRY))).setSortSpec(sort));
		
		List<IBaseResource> resultList = get(results);
		
		for (int i = 1; i < resultList.size(); i++) {
			assertThat(((Patient) resultList.get(i - 1)).getAddressFirstRep().getCountry(),
			    lessThanOrEqualTo(((Patient) resultList.get(i)).getAddressFirstRep().getCountry()));
		}
		
		sort.setOrder(SortOrderEnum.DESC);
		
		results = search(
		    new SearchParameterMap().addParameter(FhirConstants.ADDRESS_SEARCH_HANDLER, FhirConstants.COUNTRY_PROPERTY,
		        new StringAndListParam().addAnd(new StringParam(PATIENT_ADDRESS_COUNTRY))).setSortSpec(sort));
		
		resultList = get(results);
		
		for (int i = 1; i < resultList.size(); i++) {
			assertThat(((Patient) resultList.get(i - 1)).getAddressFirstRep().getCountry(),
			    greaterThanOrEqualTo(((Patient) resultList.get(i)).getAddressFirstRep().getCountry()));
		}
	}
	
}
