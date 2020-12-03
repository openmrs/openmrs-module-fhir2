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
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertEquals;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ca.uhn.fhir.rest.api.server.IBundleProvider;
import ca.uhn.fhir.rest.param.DateParam;
import ca.uhn.fhir.rest.param.DateRangeParam;
import ca.uhn.fhir.rest.param.ReferenceAndListParam;
import ca.uhn.fhir.rest.param.ReferenceOrListParam;
import ca.uhn.fhir.rest.param.ReferenceParam;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.Condition;
import org.hl7.fhir.r4.model.Patient;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.api.PatientService;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.TestFhirSpringConfiguration;
import org.openmrs.module.fhir2.api.dao.FhirConditionDao;
import org.openmrs.module.fhir2.api.search.param.SearchParameterMap;
import org.openmrs.module.fhir2.api.translators.ConditionTranslator;
import org.openmrs.module.fhir2.api.util.LocalDateTimeFactory;
import org.openmrs.test.BaseModuleContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

@ContextConfiguration(classes = TestFhirSpringConfiguration.class, inheritLocations = false)
public class ConditionSearchQueryTest extends BaseModuleContextSensitiveTest {
	
	private static final String OBS_CONDITION_INITIAL_DATA_XML = "org/openmrs/module/fhir2/api/dao/impl/FhirObsConditionDaoImplTest_initial_data.xml";
	
	private static final int START_INDEX = 0;
	
	private static final int END_INDEX = 10;
	
	private static final String PATIENT_IDENTIFIER = "6TS-4";
	
	private static final String PATIENT_WRONG_IDENTIFIER = "Wrong Identifier";
	
	private static final String PATIENT_UUID = "5946f880-b197-400b-9caa-a3c661d23041";
	
	private static final String PATIENT_WRONG_UUID = "c2299800-cca9-11e0-9572-abcdef0c9a66";
	
	private static final String PATIENT_GIVEN_NAME = "Collet";
	
	private static final String PATIENT_WRONG_GIVEN_NAME = "Wrong given name";
	
	private static final String PATIENT_PARTIAL_NAME = "Test";
	
	private static final String PATIENT_FAMILY_NAME = "Chebaskwony";
	
	private static final String PATIENT_WRONG_FAMILY_NAME = "Wrong family name";
	
	private static final String PATIENT_NOT_FOUND_NAME = "Igor";
	
	private static final String ONSET_DATE_TIME = "2008-07-01T00:00:00";
	
	private static final String ONSET_START_DATE = "2008-05-01T00:00:00";
	
	private static final String ONSET_END_DATE = "2008-08-01T00:00:00";
	
	private static final String ONSET_DATE = "2008-07-01";
	
	private static final String RECORDED_DATE_TIME = "2008-08-18T14:09:35.0";
	
	private static final String RECORDED_DATE = "2008-08-18";
	
	private static final String EXISTING_OBS_CONDITION_UUID = "86sgf-1f7d-4394-a316-0a458edf28c4";
	
	//
	
	@Autowired
	private FhirConditionDao<org.openmrs.Obs> dao;
	
	@Autowired
	private ConditionTranslator<org.openmrs.Obs> translator;
	
	@Autowired
	private SearchQueryInclude<Condition> searchQueryInclude;
	
	@Autowired
	private SearchQuery<org.openmrs.Obs, Condition, FhirConditionDao<org.openmrs.Obs>, ConditionTranslator<org.openmrs.Obs>, SearchQueryInclude<Condition>> searchQuery;
	
	@Autowired
	private LocalDateTimeFactory localDateTimeFactory;
	
	@Autowired
	PatientService patientService;
	
	@Before
	public void setup() throws Exception {
		executeDataSet(OBS_CONDITION_INITIAL_DATA_XML);
	}
	
	private IBundleProvider search(SearchParameterMap theParams) {
		return searchQuery.getQueryResults(theParams, dao, translator, searchQueryInclude);
	}
	
	private List<IBaseResource> get(IBundleProvider results) {
		return results.getResources(START_INDEX, END_INDEX);
	}
	
	@Test
	public void searchForObsConditions_shouldReturnConditionByPatientIdentifier() {
		ReferenceParam patientReference = new ReferenceParam(Patient.SP_IDENTIFIER, PATIENT_IDENTIFIER);
		ReferenceAndListParam patientList = new ReferenceAndListParam();
		patientList.addAnd(new ReferenceOrListParam().addOr(patientReference));
		
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.PATIENT_REFERENCE_SEARCH_HANDLER,
		    patientList);
		
		IBundleProvider results = search(theParams);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList, not(empty()));
		assertThat(resultList, hasSize(greaterThanOrEqualTo(1)));
		assertEquals(resultList.size(), 2);
		assertThat(((org.hl7.fhir.r4.model.Condition) resultList.iterator().next()).getSubject().getReference(),
		    endsWith(PATIENT_UUID));
	}
	
	@Test
	public void searchForObsConditions_shouldSearchForConditionsByMultiplePatientIdentifierOr() {
		ReferenceAndListParam referenceParam = new ReferenceAndListParam();
		ReferenceParam patient = new ReferenceParam();
		
		patient.setValue(PATIENT_IDENTIFIER);
		patient.setChain(Patient.SP_IDENTIFIER);
		
		ReferenceParam badPatient = new ReferenceParam();
		
		badPatient.setValue(PATIENT_WRONG_IDENTIFIER);
		badPatient.setChain(Patient.SP_IDENTIFIER);
		
		referenceParam.addValue(new ReferenceOrListParam().add(patient).add(badPatient));
		
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.PATIENT_REFERENCE_SEARCH_HANDLER,
		    referenceParam);
		
		IBundleProvider results = search(theParams);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList, not(empty()));
		assertThat(resultList, hasSize(greaterThanOrEqualTo(1)));
		assertEquals(resultList.size(), 2);
		assertThat(((org.hl7.fhir.r4.model.Condition) resultList.iterator().next()).getSubject().getReference(),
		    endsWith(PATIENT_UUID));
	}
	
	@Test
	public void searchForObsConditions_shouldReturnEmptyListOfConditionsByMultiplePatientIdentifierAnd() {
		ReferenceAndListParam referenceParam = new ReferenceAndListParam();
		ReferenceParam patient = new ReferenceParam();
		
		patient.setValue(PATIENT_IDENTIFIER);
		patient.setChain(Patient.SP_IDENTIFIER);
		
		ReferenceParam badPatient = new ReferenceParam();
		
		badPatient.setValue(PATIENT_WRONG_IDENTIFIER);
		badPatient.setChain(Patient.SP_IDENTIFIER);
		
		referenceParam.addValue(new ReferenceOrListParam().add(patient)).addAnd(new ReferenceOrListParam().add(badPatient));
		
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.PATIENT_REFERENCE_SEARCH_HANDLER,
		    referenceParam);
		IBundleProvider results = search(theParams);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList, empty());
	}
	
	@Test
	public void searchForObsConditions_shouldReturnConditionByPatientUuid() {
		ReferenceParam patientReference = new ReferenceParam(null, PATIENT_UUID);
		ReferenceAndListParam patientList = new ReferenceAndListParam();
		patientList.addAnd(new ReferenceOrListParam().addOr(patientReference));
		
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.PATIENT_REFERENCE_SEARCH_HANDLER,
		    patientList);
		
		IBundleProvider results = search(theParams);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList, not(empty()));
		assertThat(resultList, hasSize(greaterThanOrEqualTo(1)));
		assertEquals(resultList.size(), 2);
		assertThat(
		    ((org.hl7.fhir.r4.model.Condition) resultList.iterator().next()).getSubject().getReferenceElement().getIdPart(),
		    equalTo(PATIENT_UUID));
	}
	
	@Test
	public void searchForObsConditions_shouldSearchForConditionsByMultiplePatientUuidOr() {
		ReferenceAndListParam referenceParam = new ReferenceAndListParam();
		ReferenceParam patient = new ReferenceParam();
		
		patient.setValue(PATIENT_UUID);
		
		ReferenceParam badPatient = new ReferenceParam();
		
		badPatient.setValue(PATIENT_WRONG_UUID);
		
		referenceParam.addValue(new ReferenceOrListParam().add(patient).add(badPatient));
		
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.PATIENT_REFERENCE_SEARCH_HANDLER,
		    referenceParam);
		
		IBundleProvider results = search(theParams);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList, not(empty()));
		assertThat(resultList, hasSize(greaterThanOrEqualTo(1)));
		assertEquals(resultList.size(), 2);
		assertThat(
		    ((org.hl7.fhir.r4.model.Condition) resultList.iterator().next()).getSubject().getReferenceElement().getIdPart(),
		    equalTo(PATIENT_UUID));
	}
	
	@Test
	public void searchForObsConditions_shouldReturnEmptyListOfConditionsByMultiplePatientUuidAnd() {
		ReferenceAndListParam referenceParam = new ReferenceAndListParam();
		ReferenceParam patient = new ReferenceParam();
		
		patient.setValue(PATIENT_UUID);
		
		ReferenceParam badPatient = new ReferenceParam();
		
		badPatient.setValue(PATIENT_WRONG_UUID);
		
		referenceParam.addValue(new ReferenceOrListParam().add(patient)).addAnd(new ReferenceOrListParam().add(badPatient));
		
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.PATIENT_REFERENCE_SEARCH_HANDLER,
		    referenceParam);
		IBundleProvider results = search(theParams);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList, empty());
	}
	
	@Test
	public void searchForObsConditions_shouldReturnConditionByPatientGivenName() {
		ReferenceParam patientReference = new ReferenceParam(Patient.SP_GIVEN, PATIENT_GIVEN_NAME);
		ReferenceAndListParam patientList = new ReferenceAndListParam();
		patientList.addValue(new ReferenceOrListParam().add(patientReference));
		
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.PATIENT_REFERENCE_SEARCH_HANDLER,
		    patientList);
		
		IBundleProvider results = search(theParams);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList, not(empty()));
		assertThat(resultList, hasSize(greaterThanOrEqualTo(1)));
		assertEquals(resultList.size(), 2);
		assertThat(
		    ((org.hl7.fhir.r4.model.Condition) resultList.iterator().next()).getSubject().getReferenceElement().getIdPart(),
		    equalTo(PATIENT_UUID));
	}
	
	@Test
	public void searchForObsConditions_shouldReturnUniqueConditionsByPatientGivenName() {
		ReferenceParam patientReference = new ReferenceParam(Patient.SP_GIVEN, PATIENT_GIVEN_NAME);
		ReferenceAndListParam patientList = new ReferenceAndListParam();
		patientList.addValue(new ReferenceOrListParam().add(patientReference));
		
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.PATIENT_REFERENCE_SEARCH_HANDLER,
		    patientList);
		
		IBundleProvider results = search(theParams);
		
		assertThat(results, notNullValue());
		assertThat(results.size(), equalTo(2));
		
		Set<String> resultSet = new HashSet<>(dao.getSearchResultUuids(theParams));
		assertThat(resultSet.size(), equalTo(2));
	}
	
	@Test
	public void searchForObsConditions_shouldSearchForConditionsByMultiplePatientGivenNameOr() {
		ReferenceAndListParam referenceParam = new ReferenceAndListParam();
		ReferenceParam patient = new ReferenceParam();
		
		patient.setValue(PATIENT_GIVEN_NAME);
		patient.setChain(Patient.SP_GIVEN);
		
		ReferenceParam badPatient = new ReferenceParam();
		
		badPatient.setValue(PATIENT_WRONG_GIVEN_NAME);
		badPatient.setChain(Patient.SP_GIVEN);
		
		referenceParam.addValue(new ReferenceOrListParam().add(patient).add(badPatient));
		
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.PATIENT_REFERENCE_SEARCH_HANDLER,
		    referenceParam);
		IBundleProvider results = search(theParams);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList, not(empty()));
		assertEquals(resultList.size(), 2);
		assertThat(resultList, hasSize(greaterThanOrEqualTo(1)));
	}
	
	@Test
	public void searchForConditions_shouldReturnEmptyListOfConditionsByMultiplePatientGivenNameAnd() {
		ReferenceAndListParam referenceParam = new ReferenceAndListParam();
		ReferenceParam patient = new ReferenceParam();
		
		patient.setValue(PATIENT_GIVEN_NAME);
		patient.setChain(Patient.SP_GIVEN);
		
		ReferenceParam badPatient = new ReferenceParam();
		
		badPatient.setValue(PATIENT_WRONG_GIVEN_NAME);
		badPatient.setChain(Patient.SP_GIVEN);
		
		referenceParam.addValue(new ReferenceOrListParam().add(patient)).addAnd(new ReferenceOrListParam().add(badPatient));
		
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.PATIENT_REFERENCE_SEARCH_HANDLER,
		    referenceParam);
		IBundleProvider results = search(theParams);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList, empty());
	}
	
	@Test
	public void searchForConditions_shouldReturnEmptyListOfConditionByPatientNotFoundName() {
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
		assertThat(resultList, hasSize(greaterThanOrEqualTo(1)));
		assertThat(resultList.size(), equalTo(2));
		assertThat(
		    ((org.hl7.fhir.r4.model.Condition) resultList.iterator().next()).getSubject().getReferenceElement().getIdPart(),
		    equalTo(PATIENT_UUID));
	}
	
	@Test
	public void searchForObsConditions_shouldReturnUniqueConditionsByPatientFamilyName() {
		ReferenceParam patientReference = new ReferenceParam(Patient.SP_FAMILY, PATIENT_FAMILY_NAME);
		ReferenceAndListParam patientList = new ReferenceAndListParam();
		patientList.addValue(new ReferenceOrListParam().add(patientReference));
		
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.PATIENT_REFERENCE_SEARCH_HANDLER,
		    patientList);
		
		IBundleProvider results = search(theParams);
		
		assertThat(results, notNullValue());
		assertThat(results.size(), equalTo(2));
		
		Set<String> resultSet = new HashSet<>(dao.getSearchResultUuids(theParams));
		assertThat(resultSet.size(), equalTo(2));
	}
	
	@Test
	public void searchForConditions_shouldSearchForConditionsByMultiplePatientFamilyNameOr() {
		ReferenceAndListParam referenceParam = new ReferenceAndListParam();
		ReferenceParam patient = new ReferenceParam();
		
		patient.setValue(PATIENT_FAMILY_NAME);
		patient.setChain(Patient.SP_FAMILY);
		
		ReferenceParam badPatient = new ReferenceParam();
		
		badPatient.setValue(PATIENT_WRONG_FAMILY_NAME);
		badPatient.setChain(Patient.SP_FAMILY);
		
		referenceParam.addValue(new ReferenceOrListParam().add(patient).add(badPatient));
		
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.PATIENT_REFERENCE_SEARCH_HANDLER,
		    referenceParam);
		IBundleProvider results = search(theParams);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList, not(empty()));
		assertThat(resultList, hasSize(greaterThanOrEqualTo(1)));
		assertThat(resultList.size(), equalTo(2));
	}
	
	@Test
	public void searchForConditions_shouldReturnEmptyListOfConditionsByMultiplePatientFamilyNameAnd() {
		ReferenceAndListParam referenceParam = new ReferenceAndListParam();
		ReferenceParam patient = new ReferenceParam();
		
		patient.setValue(PATIENT_FAMILY_NAME);
		patient.setChain(Patient.SP_FAMILY);
		
		ReferenceParam badPatient = new ReferenceParam();
		
		badPatient.setValue(PATIENT_WRONG_FAMILY_NAME);
		badPatient.setChain(Patient.SP_FAMILY);
		
		referenceParam.addValue(new ReferenceOrListParam().add(patient)).addAnd(new ReferenceOrListParam().add(badPatient));
		
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.PATIENT_REFERENCE_SEARCH_HANDLER,
		    referenceParam);
		IBundleProvider results = search(theParams);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList, empty());
	}
	
	@Test
	public void searchForObsConditions_shouldReturnConditionByPatientName() {
		ReferenceParam patientReference = new ReferenceParam(Patient.SP_NAME, PATIENT_PARTIAL_NAME);
		ReferenceAndListParam patientList = new ReferenceAndListParam();
		patientList.addValue(new ReferenceOrListParam().add(patientReference));
		
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.PATIENT_REFERENCE_SEARCH_HANDLER,
		    patientList);
		
		IBundleProvider results = search(theParams);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList, not(empty()));
		assertThat(resultList, hasSize(greaterThanOrEqualTo(1)));
		assertThat(
		    ((org.hl7.fhir.r4.model.Condition) resultList.iterator().next()).getSubject().getReferenceElement().getIdPart(),
		    equalTo(PATIENT_UUID));
		assertThat(resultList.size(), equalTo(2));
	}
	
	@Test
	public void searchForConditions_shouldReturnUniqueConditionsByPatientName() {
		ReferenceParam patientReference = new ReferenceParam(Patient.SP_NAME, PATIENT_PARTIAL_NAME);
		ReferenceAndListParam patientList = new ReferenceAndListParam();
		patientList.addValue(new ReferenceOrListParam().add(patientReference));
		
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.PATIENT_REFERENCE_SEARCH_HANDLER,
		    patientList);
		
		IBundleProvider results = search(theParams);
		
		assertThat(results, notNullValue());
		assertThat(results.size(), equalTo(2));
		
		List<String> resultSet = dao.getSearchResultUuids(theParams);
		
		assertThat(resultSet.size(), equalTo(2));
	}
	
	@Test
	public void searchForConditions_shouldSearchForConditionsByMultiplePatientNameOr() {
		ReferenceAndListParam referenceParam = new ReferenceAndListParam();
		ReferenceParam patient = new ReferenceParam();
		
		patient.setValue(PATIENT_PARTIAL_NAME);
		patient.setChain(Patient.SP_NAME);
		
		ReferenceParam badPatient = new ReferenceParam();
		
		badPatient.setValue(PATIENT_NOT_FOUND_NAME);
		badPatient.setChain(Patient.SP_NAME);
		
		referenceParam.addValue(new ReferenceOrListParam().add(patient).add(badPatient));
		
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.PATIENT_REFERENCE_SEARCH_HANDLER,
		    referenceParam);
		IBundleProvider results = search(theParams);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList, not(empty()));
		assertThat(resultList, hasSize(greaterThanOrEqualTo(1)));
		assertThat(resultList.size(), equalTo(2));
	}
	
	@Test
	public void searchForConditions_shouldReturnEmptyListOfConditionsByMultiplePatientNameAnd() {
		ReferenceAndListParam referenceParam = new ReferenceAndListParam();
		ReferenceParam patient = new ReferenceParam();
		
		patient.setValue(PATIENT_PARTIAL_NAME);
		patient.setChain(Patient.SP_NAME);
		
		ReferenceParam badPatient = new ReferenceParam();
		
		badPatient.setValue(PATIENT_NOT_FOUND_NAME);
		badPatient.setChain(Patient.SP_NAME);
		
		referenceParam.addValue(new ReferenceOrListParam().add(patient)).addAnd(new ReferenceOrListParam().add(badPatient));
		
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.PATIENT_REFERENCE_SEARCH_HANDLER,
		    referenceParam);
		IBundleProvider results = search(theParams);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList, empty());
	}
	
	@Test
	public void searchForObsConditions_shouldReturnConditionByOnsetDate() {
		DateRangeParam onsetDate = new DateRangeParam(new DateParam("eq" + ONSET_DATE_TIME));
		
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.DATE_RANGE_SEARCH_HANDLER,
		    "obsDatetime", onsetDate);
		
		IBundleProvider results = search(theParams);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList, not(empty()));
		assertThat(resultList, hasSize(greaterThanOrEqualTo(1)));
		assertThat(
		    ((org.hl7.fhir.r4.model.Condition) resultList.iterator().next()).getOnsetDateTimeType().getValue().toString(),
		    containsString(ONSET_DATE));
		assertThat(resultList.size(), equalTo(2));
	}
	
	@Test
	public void searchForConditions_shouldReturnConditionByOnsetDateRange() {
		DateRangeParam onsetDate = new DateRangeParam(new DateParam(ONSET_START_DATE), new DateParam(ONSET_END_DATE));
		
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.DATE_RANGE_SEARCH_HANDLER,
		    "obsDatetime", onsetDate);
		
		IBundleProvider results = search(theParams);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList, not(empty()));
		assertThat(resultList, hasSize(greaterThanOrEqualTo(1)));
		assertThat(
		    ((org.hl7.fhir.r4.model.Condition) resultList.iterator().next()).getOnsetDateTimeType().getValue().toString(),
		    containsString(ONSET_DATE));
		assertThat(resultList.size(), equalTo(2));
	}
	
	@Test
	public void searchForConditions_shouldReturnConditionByUnboundedOnsetDate() {
		DateRangeParam onsetDate = new DateRangeParam(new DateParam("gt" + ONSET_START_DATE));
		
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.DATE_RANGE_SEARCH_HANDLER,
		    "obsDatetime", onsetDate);
		
		IBundleProvider results = search(theParams);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList, not(empty()));
		assertThat(resultList, hasSize(greaterThanOrEqualTo(1)));
		assertThat(resultList.size(), equalTo(2));
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
		assertThat(resultList, hasSize(greaterThanOrEqualTo(1)));
		assertThat(((org.hl7.fhir.r4.model.Condition) resultList.iterator().next()).getRecordedDate().toString(),
		    containsString(RECORDED_DATE));
		assertThat(resultList.size(), equalTo(2));
	}
	
	@Test
	public void check() {
		//System.out.println(patientService.getPatient(7).getPatientIdentifier());
		//System.out.println(patientService.getPatient(7).getUuid());
		//System.out.println(patientService.getPatient(7).getGivenName());
		//System.out.println(patientService.getPatient(7).getFamilyName());
		System.out.println(patientService.getPatient(7).getMiddleName());
	}
	
}
