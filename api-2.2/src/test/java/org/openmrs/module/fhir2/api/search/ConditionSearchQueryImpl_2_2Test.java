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

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.not;
import static org.mockito.Mockito.when;

import java.util.Calendar;
import java.util.List;

import ca.uhn.fhir.rest.api.server.IBundleProvider;
import ca.uhn.fhir.rest.param.DateParam;
import ca.uhn.fhir.rest.param.DateRangeParam;
import ca.uhn.fhir.rest.param.ParamPrefixEnum;
import ca.uhn.fhir.rest.param.QuantityAndListParam;
import ca.uhn.fhir.rest.param.QuantityOrListParam;
import ca.uhn.fhir.rest.param.QuantityParam;
import ca.uhn.fhir.rest.param.ReferenceAndListParam;
import ca.uhn.fhir.rest.param.ReferenceOrListParam;
import ca.uhn.fhir.rest.param.ReferenceParam;
import ca.uhn.fhir.rest.param.TokenAndListParam;
import ca.uhn.fhir.rest.param.TokenOrListParam;
import ca.uhn.fhir.rest.param.TokenParam;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.Patient;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.Condition;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.TestFhirSpringConfiguration;
import org.openmrs.module.fhir2.api.dao.FhirConditionDao;
import org.openmrs.module.fhir2.api.search.param.SearchParameterMap;
import org.openmrs.module.fhir2.api.translators.ConditionTranslator;
import org.openmrs.module.fhir2.api.util.CalendarFactory;
import org.openmrs.test.BaseModuleContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

@ContextConfiguration(classes = TestFhirSpringConfiguration.class, inheritLocations = false)
public class ConditionSearchQueryImpl_2_2Test extends BaseModuleContextSensitiveTest {
	
	private static final String CONDITION_UUID = "604953c5-b5c6-4e1e-be95-e37d8f392046";
	
	private static final String CONDITION_INITIAL_DATA_XML = "org/openmrs/module/fhir2/api/dao/impl/FhirConditionDaoImplTest_initial_data.xml";
	
	private static final String PATIENT_UUID = "a7e04421-525f-442f-8138-05b619d16def";
	
	private static final String PATIENT_GIVEN_NAME = "Johnny";
	
	private static final String PATIENT_PARTIAL_NAME = "Johnn";
	
	private static final String PATIENT_FAMILY_NAME = "Doe";
	
	private static final String PATIENT_NOT_FOUND_NAME = "Igor";
	
	private static final String ONSET_DATE_TIME = "2020-03-05T19:00:00";
	
	private static final String ONSET_DATE = "2020-03-05 19:00:00";
	
	private static final String ONSET_START_DATE = "2020-03-03T22:00:00";
	
	private static final String ONSET_END_DATE = "2020-03-08T19:00:00";
	
	private static final String RECORDED_DATE_TIME = "2020-03-14T19:32:34";
	
	private static final String RECORDED_DATE = "2020-03-14 19:32:34";
	
	private static final String RECORDED_START_DATE = "2020-03-12T19:32:34";
	
	private static final String RECORDED_END_DATE = "2020-03-20T19:32:34";
	
	private static final String STATUS_ACTIVE = "active";
	
	private static final String STATUS_INACTIVE = "inactive";
	
	private static final String CODE_SYSTEM_1 = "http://made_up_concepts.info/sct";
	
	private static final String CODE_VALUE_1 = "CD41003";
	
	private static final String CONCEPT_ID_1 = "a09ab2c5-878e-4905-b25d-5784167d0216";
	
	private static final String CODE_SYSTEM_2 = "http://made_up_concepts.info/sct";
	
	private static final String CODE_VALUE_2 = "WGT234";
	
	private static final String CONCEPT_ID_2 = "c607c80f-1ea9-4da3-bb88-6276ce8868dd";
	
	private static final int START_INDEX = 0;
	
	private static final int END_INDEX = 10;
	
	@Autowired
	private FhirConditionDao<Condition> dao;
	
	@Autowired
	private ConditionTranslator<Condition> translator;
	
	@Autowired
	private SearchQuery<Condition, org.hl7.fhir.r4.model.Condition, FhirConditionDao<Condition>, ConditionTranslator<Condition>> searchQuery;
	
	@Autowired
	private CalendarFactory calendarFactory;
	
	@Before
	public void setup() {
		executeDataSet(CONDITION_INITIAL_DATA_XML);
	}
	
	private IBundleProvider search(SearchParameterMap theParams) {
		return searchQuery.getQueryResults(theParams, dao, translator);
	}
	
	private List<IBaseResource> get(IBundleProvider results) {
		return results.getResources(START_INDEX, END_INDEX);
	}
	
	@Test
	public void searchForConditions_shouldReturnConditionByPatientUuid() {
		ReferenceParam patientReference = new ReferenceParam(null, PATIENT_UUID);
		ReferenceAndListParam patientList = new ReferenceAndListParam();
		patientList.addAnd(new ReferenceOrListParam().addOr(patientReference));
		
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.PATIENT_REFERENCE_SEARCH_HANDLER,
		    patientList);
		
		IBundleProvider results = search(theParams);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList, not(empty()));
		assertThat(resultList.size(), greaterThanOrEqualTo(1));
		assertThat(
		    ((org.hl7.fhir.r4.model.Condition) resultList.iterator().next()).getSubject().getReferenceElement().getIdPart(),
		    equalTo(PATIENT_UUID));
	}
	
	@Test
	public void searchForConditions_shouldReturnConditionByPatientGivenName() {
		ReferenceParam patientReference = new ReferenceParam(Patient.SP_GIVEN, PATIENT_GIVEN_NAME);
		ReferenceAndListParam patientList = new ReferenceAndListParam();
		patientList.addValue(new ReferenceOrListParam().add(patientReference));
		
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.PATIENT_REFERENCE_SEARCH_HANDLER,
		    patientList);
		
		IBundleProvider results = search(theParams);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList, not(empty()));
		assertThat(resultList.size(), greaterThanOrEqualTo(1));
		assertThat(
		    ((org.hl7.fhir.r4.model.Condition) resultList.iterator().next()).getSubject().getReferenceElement().getIdPart(),
		    equalTo(PATIENT_UUID));
	}
	
	@Test
	public void searchForConditions_shouldReturnConditionByPatientNotFoundName() {
		ReferenceParam patientReference = new ReferenceParam(Patient.SP_GIVEN, PATIENT_NOT_FOUND_NAME);
		ReferenceAndListParam patientList = new ReferenceAndListParam();
		patientList.addValue(new ReferenceOrListParam().add(patientReference));
		
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.PATIENT_REFERENCE_SEARCH_HANDLER,
		    patientList);
		
		IBundleProvider results = search(theParams);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList, empty());
	}
	
	@Test
	public void searchForConditions_shouldReturnConditionByPatientFamilyName() {
		ReferenceParam patientReference = new ReferenceParam(Patient.SP_FAMILY, PATIENT_FAMILY_NAME);
		ReferenceAndListParam patientList = new ReferenceAndListParam();
		patientList.addValue(new ReferenceOrListParam().add(patientReference));
		
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.PATIENT_REFERENCE_SEARCH_HANDLER,
		    patientList);
		
		IBundleProvider results = search(theParams);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList, not(empty()));
		assertThat(resultList.size(), greaterThanOrEqualTo(1));
		assertThat(
		    ((org.hl7.fhir.r4.model.Condition) resultList.iterator().next()).getSubject().getReferenceElement().getIdPart(),
		    equalTo(PATIENT_UUID));
	}
	
	@Test
	public void searchForConditions_shouldReturnConditionByPatientName() {
		ReferenceParam patientReference = new ReferenceParam(Patient.SP_NAME, PATIENT_PARTIAL_NAME);
		ReferenceAndListParam patientList = new ReferenceAndListParam();
		patientList.addValue(new ReferenceOrListParam().add(patientReference));
		
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.PATIENT_REFERENCE_SEARCH_HANDLER,
		    patientList);
		
		IBundleProvider results = search(theParams);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList, not(empty()));
		assertThat(resultList.size(), greaterThanOrEqualTo(1));
		assertThat(
		    ((org.hl7.fhir.r4.model.Condition) resultList.iterator().next()).getSubject().getReferenceElement().getIdPart(),
		    equalTo(PATIENT_UUID));
	}
	
	@Test
	public void searchForConditions_shouldReturnConditionByOnsetDate() {
		DateRangeParam onsetDate = new DateRangeParam(new DateParam("eq" + ONSET_DATE_TIME));
		
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.DATE_RANGE_SEARCH_HANDLER,
		    "onsetDate", onsetDate);
		
		IBundleProvider results = search(theParams);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList, not(empty()));
		assertThat(resultList.size(), greaterThanOrEqualTo(1));
		assertThat(
		    ((org.hl7.fhir.r4.model.Condition) resultList.iterator().next()).getOnsetDateTimeType().getValue().toString(),
		    containsString(ONSET_DATE));
	}
	
	@Test
	public void searchForConditions_shouldReturnConditionByOnsetDateRange() {
		DateRangeParam onsetDate = new DateRangeParam(new DateParam(ONSET_START_DATE), new DateParam(ONSET_END_DATE));
		
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.DATE_RANGE_SEARCH_HANDLER,
		    "onsetDate", onsetDate);
		
		IBundleProvider results = search(theParams);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList, not(empty()));
		assertThat(resultList.size(), greaterThanOrEqualTo(1));
		assertThat(
		    ((org.hl7.fhir.r4.model.Condition) resultList.iterator().next()).getOnsetDateTimeType().getValue().toString(),
		    containsString(ONSET_DATE));
	}
	
	@Test
	public void searchForConditions_shouldReturnConditionByUnboundedOnsetDate() {
		DateRangeParam onsetDate = new DateRangeParam(new DateParam("gt" + ONSET_START_DATE));
		
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.DATE_RANGE_SEARCH_HANDLER,
		    "onsetDate", onsetDate);
		
		IBundleProvider results = search(theParams);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList, not(empty()));
		assertThat(resultList.size(), greaterThanOrEqualTo(1));
	}
	
	@Test
	public void searchForConditions_shouldReturnConditionByOnsetAgeLessThanHour() {
		QuantityOrListParam orList = new QuantityOrListParam();
		orList.addOr(new QuantityParam(ParamPrefixEnum.LESSTHAN, 1.5, "", "h"));
		QuantityAndListParam onsetAgeParam = new QuantityAndListParam().addAnd(orList);
		
		Calendar calendar = Calendar.getInstance();
		calendar.set(2020, Calendar.MARCH, 13, 19, 10, 0);
		when(calendarFactory.getCalendar()).thenReturn(calendar);
		
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.QUANTITY_SEARCH_HANDLER,
		    onsetAgeParam);
		
		IBundleProvider results = search(theParams);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList.size(), greaterThanOrEqualTo(1));
		assertThat(((org.hl7.fhir.r4.model.Condition) resultList.iterator().next()).getIdElement().getIdPart(),
		    equalTo(CONDITION_UUID));
	}
	
	@Test
	public void searchForConditions_shouldReturnConditionByOnsetAgeEqualHour() {
		QuantityOrListParam orList = new QuantityOrListParam();
		orList.addOr(new QuantityParam(ParamPrefixEnum.EQUAL, 3, "", "h"));
		QuantityAndListParam onsetAgeParam = new QuantityAndListParam().addAnd(orList);
		
		Calendar calendar = Calendar.getInstance();
		calendar.set(2020, Calendar.MARCH, 13, 22, 0, 0);
		when(calendarFactory.getCalendar()).thenReturn(calendar);
		
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.QUANTITY_SEARCH_HANDLER,
		    onsetAgeParam);
		
		IBundleProvider results = search(theParams);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList.size(), greaterThanOrEqualTo(1));
		assertThat(((org.hl7.fhir.r4.model.Condition) resultList.iterator().next()).getIdElement().getIdPart(),
		    equalTo(CONDITION_UUID));
	}
	
	@Test
	public void searchForConditions_shouldReturnConditionByOnsetAgeIntervalDay() {
		QuantityOrListParam orListLower = new QuantityOrListParam();
		QuantityOrListParam orListUpper = new QuantityOrListParam();
		orListLower.addOr(new QuantityParam(ParamPrefixEnum.LESSTHAN, 11, "", "d"));
		orListUpper.addOr(new QuantityParam(ParamPrefixEnum.GREATERTHAN, 8, "", "d"));
		QuantityAndListParam onsetAgeParam = new QuantityAndListParam().addAnd(orListLower).addAnd(orListUpper);
		
		Calendar calendar = Calendar.getInstance();
		calendar.set(2020, Calendar.MARCH, 22, 22, 0, 0);
		when(calendarFactory.getCalendar()).thenReturn((Calendar) calendar.clone()).thenReturn((Calendar) calendar.clone());
		
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.QUANTITY_SEARCH_HANDLER,
		    onsetAgeParam);
		
		IBundleProvider results = search(theParams);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList.size(), greaterThanOrEqualTo(1));
		assertThat(((org.hl7.fhir.r4.model.Condition) resultList.iterator().next()).getIdElement().getIdPart(),
		    equalTo(CONDITION_UUID));
	}
	
	@Test
	public void searchForConditions_shouldReturnConditionByOnsetAgeOrWeekMonthYear() {
		QuantityOrListParam orList = new QuantityOrListParam();
		orList.addOr(new QuantityParam(ParamPrefixEnum.GREATERTHAN, 4, "", "a"));
		orList.addOr(new QuantityParam(ParamPrefixEnum.LESSTHAN, 3, "", "mo"));
		orList.addOr(new QuantityParam(ParamPrefixEnum.LESSTHAN, 2, "", "wk"));
		QuantityAndListParam onsetAgeParam = new QuantityAndListParam().addAnd(orList);
		
		Calendar calendar = Calendar.getInstance();
		calendar.set(2020, Calendar.MARCH, 20, 22, 0, 0);
		when(calendarFactory.getCalendar()).thenReturn((Calendar) calendar.clone()).thenReturn((Calendar) calendar.clone());
		
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.QUANTITY_SEARCH_HANDLER,
		    onsetAgeParam);
		
		IBundleProvider results = search(theParams);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList.size(), greaterThanOrEqualTo(1));
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void searchForConditions_shouldReturnConditionByOnsetAgeExceptionForWrongUnit() {
		QuantityOrListParam orList = new QuantityOrListParam();
		orList.addOr(new QuantityParam(ParamPrefixEnum.LESSTHAN, 1.5, "", "WRONG_UNIT"));
		QuantityAndListParam onsetAgeParam = new QuantityAndListParam().addAnd(orList);
		
		Calendar calendar = Calendar.getInstance();
		calendar.set(2020, Calendar.MARCH, 13, 19, 10, 0);
		when(calendarFactory.getCalendar()).thenReturn(calendar);
		
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.QUANTITY_SEARCH_HANDLER,
		    onsetAgeParam);
		
		IBundleProvider results = search(theParams);
		get(results);
	}
	
	@Test
	public void searchForConditions_shouldReturnConditionByRecordedDate() {
		DateRangeParam recordedDate = new DateRangeParam(new DateParam("eq" + RECORDED_DATE_TIME));
		
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.DATE_RANGE_SEARCH_HANDLER,
		    "dateCreated", recordedDate);
		
		IBundleProvider results = search(theParams);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList, not(empty()));
		assertThat(resultList.size(), greaterThanOrEqualTo(1));
		assertThat(((org.hl7.fhir.r4.model.Condition) resultList.iterator().next()).getRecordedDate().toString(),
		    containsString(RECORDED_DATE));
	}
	
	@Test
	public void searchForConditions_shouldReturnConditionByRecordedDateRange() {
		DateRangeParam onsetDate = new DateRangeParam(new DateParam(RECORDED_START_DATE), new DateParam(RECORDED_END_DATE));
		
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.DATE_RANGE_SEARCH_HANDLER,
		    "dateCreated", onsetDate);
		
		IBundleProvider results = search(theParams);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList, not(empty()));
		assertThat(resultList.size(), greaterThanOrEqualTo(1));
		assertThat(((org.hl7.fhir.r4.model.Condition) resultList.iterator().next()).getRecordedDate().toString(),
		    containsString(RECORDED_DATE));
	}
	
	@Test
	public void searchForConditions_shouldReturnConditionByUnboundedRecordedDate() {
		DateRangeParam onsetDate = new DateRangeParam(new DateParam("gt" + RECORDED_START_DATE));
		
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.DATE_RANGE_SEARCH_HANDLER,
		    "dateCreated", onsetDate);
		
		IBundleProvider results = search(theParams);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList, not(empty()));
		assertThat(resultList.size(), greaterThanOrEqualTo(1));
	}
	
	@Test
	public void searchForConditions_shouldReturnConditionByClinicalStatusActive() {
		TokenAndListParam listParam = new TokenAndListParam();
		listParam.addValue(new TokenOrListParam().add(new TokenParam(STATUS_ACTIVE)));
		
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.STATUS_SEARCH_HANDLER, listParam);
		
		IBundleProvider results = search(theParams);
		
		assertThat(results, notNullValue());
		assertThat(results.size(), greaterThanOrEqualTo(1));
	}
	
	@Test
	public void searchForConditions_shouldReturnConditionByClinicalStatusInactive() {
		TokenAndListParam listParam = new TokenAndListParam();
		listParam.addValue(new TokenOrListParam().add(new TokenParam(STATUS_INACTIVE)));
		
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.STATUS_SEARCH_HANDLER, listParam);
		
		IBundleProvider results = search(theParams);
		
		assertThat(results, notNullValue());
		assertThat(results.size(), greaterThanOrEqualTo(1));
	}
	
	@Test
	public void searchForConditions_shouldReturnConditionByClinicalStatusAll() {
		TokenAndListParam listParam = new TokenAndListParam();
		listParam.addValue(new TokenOrListParam().add(new TokenParam(STATUS_ACTIVE)).add(new TokenParam(STATUS_INACTIVE)));
		
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.STATUS_SEARCH_HANDLER, listParam);
		
		IBundleProvider results = search(theParams);
		
		assertThat(results, notNullValue());
		assertThat(results.size(), equalTo(6));
	}
	
	@Test
	public void searchForConditions_shouldReturnConditionByCode() {
		TokenAndListParam listParam = new TokenAndListParam();
		listParam.addValue(new TokenOrListParam().add(new TokenParam(CODE_SYSTEM_1, CODE_VALUE_1)));
		
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.CODED_SEARCH_HANDLER, listParam);
		
		IBundleProvider results = search(theParams);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList, not(empty()));
		assertThat(resultList.size(), greaterThanOrEqualTo(1));
		assertThat(((org.hl7.fhir.r4.model.Condition) resultList.iterator().next()).getCode().getCodingFirstRep().getCode(),
		    equalTo(CONCEPT_ID_1));
	}
	
	@Test
	public void searchForConditions_shouldReturnMultipleConditionsByCodeList() {
		TokenAndListParam listParam = new TokenAndListParam();
		
		// Adding codes concept_id=5497 and concept_id=5089.
		listParam.addValue(new TokenOrListParam().add(new TokenParam(CODE_SYSTEM_1, CODE_VALUE_1))
		        .add(new TokenParam(CODE_SYSTEM_2, CODE_VALUE_2)));
		
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.CODED_SEARCH_HANDLER, listParam);
		
		IBundleProvider results = search(theParams);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList, not(empty()));
		assertThat(resultList.size(), greaterThanOrEqualTo(1));
	}
	
	@Test
	public void searchForConditions_shouldReturnConditionByCodeAndNoSystem() {
		TokenAndListParam listParam = new TokenAndListParam();
		listParam.addValue(new TokenOrListParam().add(new TokenParam(CONCEPT_ID_1)));
		
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.CODED_SEARCH_HANDLER, listParam);
		
		IBundleProvider results = search(theParams);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList, not(empty()));
		assertThat(resultList.size(), greaterThanOrEqualTo(1));
		assertThat(((org.hl7.fhir.r4.model.Condition) resultList.iterator().next()).getCode().getCodingFirstRep().getCode(),
		    equalTo(CONCEPT_ID_1));
	}
	
	@Test
	public void searchForConditions_shouldReturnMultipleConditionsByCodeListAndNoSystem() {
		TokenAndListParam listParam = new TokenAndListParam();
		listParam.addValue(new TokenOrListParam().add(new TokenParam(CONCEPT_ID_1)).add(new TokenParam(CONCEPT_ID_2)));
		
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.CODED_SEARCH_HANDLER, listParam);
		
		IBundleProvider results = search(theParams);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList, not(empty()));
		assertThat(resultList.size(), greaterThanOrEqualTo(1));
	}
	
}
