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
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hl7.fhir.r4.model.Patient.SP_FAMILY;
import static org.hl7.fhir.r4.model.Patient.SP_GIVEN;
import static org.hl7.fhir.r4.model.Patient.SP_IDENTIFIER;
import static org.hl7.fhir.r4.model.Patient.SP_NAME;
import static org.openmrs.module.fhir2.FhirConstants.RESULT_SEARCH_HANDLER;

import java.util.List;
import java.util.stream.Collectors;

import ca.uhn.fhir.rest.api.SortOrderEnum;
import ca.uhn.fhir.rest.api.SortSpec;
import ca.uhn.fhir.rest.api.server.IBundleProvider;
import ca.uhn.fhir.rest.param.DateParam;
import ca.uhn.fhir.rest.param.DateRangeParam;
import ca.uhn.fhir.rest.param.ReferenceAndListParam;
import ca.uhn.fhir.rest.param.ReferenceOrListParam;
import ca.uhn.fhir.rest.param.ReferenceParam;
import ca.uhn.fhir.rest.param.TokenAndListParam;
import ca.uhn.fhir.rest.param.TokenOrListParam;
import ca.uhn.fhir.rest.param.TokenParam;
import org.hl7.fhir.r4.model.DiagnosticReport;
import org.hl7.fhir.r4.model.Patient;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.Obs;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.TestFhirSpringConfiguration;
import org.openmrs.module.fhir2.api.dao.FhirDiagnosticReportDao;
import org.openmrs.module.fhir2.api.search.param.SearchParameterMap;
import org.openmrs.module.fhir2.api.translators.DiagnosticReportTranslator;
import org.openmrs.test.BaseModuleContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

@ContextConfiguration(classes = TestFhirSpringConfiguration.class, inheritLocations = false)
public class DiagnosticReportSearchQueryTest extends BaseModuleContextSensitiveTest {
	
	private static final String DATA_XML = "org/openmrs/module/fhir2/api/dao/impl/FhirDiagnosticReportDaoImplTest_initial_data.xml";
	
	private static final String DIAGNOSTIC_REPORT_UUID = "dchf6962-1c42-49ea-bed2-97650c66f246";
	
	private static final String ENCOUNTER_UUID = "y403fafb-e5e4-42d0-9d11-4f52e89d123r";
	
	private static final String WRONG_ENCOUNTER_UUID = "6519d653-393b-4118-9c83-a3715b82d4az";
	
	private static final String PATIENT_UUID = "da7f524f-27ce-4bb2-86d6-6d1d05312bd5";
	
	private static final String WRONG_PATIENT_UUID = "5946f880-b197-400b-9caa-a3c661d23043";
	
	private static final String PATIENT_GIVEN_NAME = "Horatio";
	
	private static final String WRONG_PATIENT_GIVEN_NAME = "Colletas";
	
	private static final String PATIENT_FAMILY_NAME = "Hornblower";
	
	private static final String WRONG_PATIENT_FAMILY_NAME = "Chebaskwonyop";
	
	private static final String DIAGNOSTIC_REPORT_DATETIME = "2018-08-18T14:09:35.0";
	
	private static final String DIAGNOSTIC_REPORT_WRONG_DATETIME = "2008-08-18T14:11:15.0";
	
	private static final String DIAGNOSTIC_REPORT_DATE = "2018-08-18 14:09:35.0";
	
	private static final String DIAGNOSTIC_REPORT_CODE = "23";
	
	private static final String CODEABLE_CONCEPT_UUID = "0f97e14e-cdc2-49ac-9255-b5126f8a5147";
	
	private static final String DIAGNOSTIC_REPORT_WRONG_CODE = "5499";
	
	private static final String PATIENT_IDENTIFIER = "101-6";
	
	private static final String WRONG_PATIENT_IDENTIFIER = "6TS-3";
	
	private static final String DATE_CREATED = "2018-08-18";
	
	private static final String WRONG_DATE_CREATED = "2008-08-18";
	
	private static final String OBS_RESULT_UUID = "dc386962-1c42-49ea-bed2-97650c66sd46";
	
	private static final int START_INDEX = 0;
	
	private static final int END_INDEX = 10;
	
	@Autowired
	SearchQuery<Obs, DiagnosticReport, FhirDiagnosticReportDao, DiagnosticReportTranslator> searchQuery;
	
	@Autowired
	private FhirDiagnosticReportDao dao;
	
	@Autowired
	private DiagnosticReportTranslator translator;
	
	@Before
	public void setup() throws Exception {
		executeDataSet(DATA_XML);
	}
	
	private IBundleProvider search(SearchParameterMap theParams) {
		return searchQuery.getQueryResults(theParams, dao, translator);
	}
	
	private List<DiagnosticReport> get(IBundleProvider results) {
		return results.getResources(START_INDEX, END_INDEX).stream().filter(it -> it instanceof DiagnosticReport)
		        .map(it -> (DiagnosticReport) it).collect(Collectors.toList());
	}
	
	@Test
	public void searchForDiagnosticReports_shouldReturnCorrectObsByEncounterUUID() {
		ReferenceAndListParam encounterReference = new ReferenceAndListParam()
		        .addAnd(new ReferenceOrListParam().add(new ReferenceParam().setValue(ENCOUNTER_UUID).setChain(null)));
		
		SearchParameterMap theParams = new SearchParameterMap()
		        .addParameter(FhirConstants.ENCOUNTER_REFERENCE_SEARCH_HANDLER, encounterReference);
		
		IBundleProvider diagnosticReports = search(theParams);
		
		List<DiagnosticReport> resultList = get(diagnosticReports);
		
		assertThat(diagnosticReports, notNullValue());
		assertThat(resultList, hasSize(greaterThanOrEqualTo(1)));
		assertThat(resultList.get(0).getEncounter().getReferenceElement().getIdPart(), equalTo(ENCOUNTER_UUID));
	}
	
	@Test
	public void searchForDiagnosticReports_shouldReturnEmptyCollectionByWrongEncounterUUID() {
		ReferenceAndListParam encounterReference = new ReferenceAndListParam()
		        .addAnd(new ReferenceOrListParam().add(new ReferenceParam().setValue(WRONG_ENCOUNTER_UUID).setChain(null)));
		
		SearchParameterMap theParams = new SearchParameterMap()
		        .addParameter(FhirConstants.ENCOUNTER_REFERENCE_SEARCH_HANDLER, encounterReference);
		
		IBundleProvider diagnosticReports = search(theParams);
		
		List<DiagnosticReport> resultList = get(diagnosticReports);
		
		assertThat(diagnosticReports, notNullValue());
		assertThat(resultList, empty());
	}
	
	@Test
	public void searchForDiagnosticReports_shouldReturnCorrectObsByPatientUUID() {
		ReferenceAndListParam patientReference = new ReferenceAndListParam()
		        .addAnd(new ReferenceOrListParam().add(new ReferenceParam().setValue(PATIENT_UUID).setChain(null)));
		
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.PATIENT_REFERENCE_SEARCH_HANDLER,
		    patientReference);
		
		IBundleProvider diagnosticReports = search(theParams);
		
		assertThat(diagnosticReports, notNullValue());
		assertThat(diagnosticReports.size(), greaterThanOrEqualTo(1));
		
		List<DiagnosticReport> resultList = get(diagnosticReports);
		
		assertThat(resultList, hasSize(greaterThanOrEqualTo(1)));
		assertThat(resultList.get(0).getSubject().getReferenceElement().getIdPart(), equalTo(PATIENT_UUID));
	}
	
	@Test
	public void searchForDiagnosticReports_shouldReturnEmptyCollectionByWrongPatientUUID() {
		ReferenceAndListParam patientReference = new ReferenceAndListParam()
		        .addAnd(new ReferenceOrListParam().add(new ReferenceParam().setValue(WRONG_PATIENT_UUID).setChain(null)));
		
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.PATIENT_REFERENCE_SEARCH_HANDLER,
		    patientReference);
		
		IBundleProvider diagnosticReports = search(theParams);
		
		assertThat(diagnosticReports, notNullValue());
		assertThat(diagnosticReports.size(), equalTo(0));
		
		List<DiagnosticReport> resultList = get(diagnosticReports);
		
		assertThat(resultList, empty());
	}
	
	@Test
	public void searchForDiagnosticReports_shouldSearchForDiagnosticReportsByMultiplePatientUuidOr() {
		ReferenceAndListParam referenceParam = new ReferenceAndListParam();
		ReferenceParam patient = new ReferenceParam();
		
		patient.setValue(PATIENT_UUID);
		
		ReferenceParam badPatient = new ReferenceParam();
		
		badPatient.setValue(WRONG_PATIENT_UUID);
		
		referenceParam.addValue(new ReferenceOrListParam().add(patient).add(badPatient));
		
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.PATIENT_REFERENCE_SEARCH_HANDLER,
		    referenceParam);
		
		IBundleProvider results = search(theParams);
		
		assertThat(results, notNullValue());
		assertThat(results.size(), greaterThanOrEqualTo(1));
		
		List<DiagnosticReport> resultList = get(results);
		
		assertThat(resultList, hasSize(greaterThanOrEqualTo(1)));
	}
	
	@Test
	public void searchForDiagnosticReports_shouldReturnEmptyListOfDiagnosticReportsByMultiplePatientUuidAnd() {
		ReferenceAndListParam referenceParam = new ReferenceAndListParam();
		ReferenceParam patient = new ReferenceParam();
		
		patient.setValue(PATIENT_UUID);
		
		ReferenceParam badPatient = new ReferenceParam();
		
		badPatient.setValue(WRONG_PATIENT_UUID);
		
		referenceParam.addValue(new ReferenceOrListParam().add(patient)).addAnd(new ReferenceOrListParam().add(badPatient));
		
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.PATIENT_REFERENCE_SEARCH_HANDLER,
		    referenceParam);
		IBundleProvider results = search(theParams);
		
		List<DiagnosticReport> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList, empty());
	}
	
	@Test
	public void searchForDiagnosticReports_shouldReturnCorrectObsByPatientIdentifier() {
		ReferenceAndListParam patientReference = new ReferenceAndListParam().addAnd(new ReferenceOrListParam()
		        .add(new ReferenceParam().setValue(PATIENT_IDENTIFIER).setChain(Patient.SP_IDENTIFIER)));
		
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.PATIENT_REFERENCE_SEARCH_HANDLER,
		    patientReference);
		
		IBundleProvider diagnosticReports = search(theParams);
		
		assertThat(diagnosticReports, notNullValue());
		assertThat(diagnosticReports.size(), greaterThanOrEqualTo(1));
		
		List<DiagnosticReport> resultList = get(diagnosticReports);
		
		assertThat(resultList, hasSize(greaterThanOrEqualTo(1)));
		assertThat(resultList.get(0).getSubject().getIdentifier().getValue(), equalTo(PATIENT_IDENTIFIER));
	}
	
	@Test
	public void searchForDiagnosticReports_shouldReturnEmptyCollectionByWrongPatientIdentifier() {
		ReferenceAndListParam patientReference = new ReferenceAndListParam().addAnd(
		    new ReferenceOrListParam().add(new ReferenceParam().setValue(WRONG_PATIENT_IDENTIFIER).setChain(SP_IDENTIFIER)));
		
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.PATIENT_REFERENCE_SEARCH_HANDLER,
		    patientReference);
		
		IBundleProvider diagnosticReports = search(theParams);
		
		assertThat(diagnosticReports, notNullValue());
		assertThat(diagnosticReports.size(), equalTo(0));
		
		List<DiagnosticReport> resultList = get(diagnosticReports);
		
		assertThat(resultList, empty());
	}
	
	@Test
	public void searchForDiagnosticReports_shouldSearchForDiagnosticReportsByMultiplePatientIdentifierOr() {
		ReferenceAndListParam referenceParam = new ReferenceAndListParam();
		ReferenceParam patient = new ReferenceParam();
		
		patient.setValue(PATIENT_IDENTIFIER);
		patient.setChain(Patient.SP_IDENTIFIER);
		
		ReferenceParam badPatient = new ReferenceParam();
		
		badPatient.setValue(WRONG_PATIENT_IDENTIFIER);
		badPatient.setChain(Patient.SP_IDENTIFIER);
		
		referenceParam.addValue(new ReferenceOrListParam().add(patient).add(badPatient));
		
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.PATIENT_REFERENCE_SEARCH_HANDLER,
		    referenceParam);
		
		IBundleProvider results = search(theParams);
		
		assertThat(results, notNullValue());
		assertThat(results.size(), greaterThanOrEqualTo(1));
		
		List<DiagnosticReport> resultList = get(results);
		
		assertThat(resultList, hasSize(greaterThanOrEqualTo(1)));
		assertThat(resultList.get(0).getSubject().getIdentifier().getValue(), equalTo(PATIENT_IDENTIFIER));
	}
	
	@Test
	public void searchForDiagnosticReports_shouldReturnEmptyListOfDiagnosticReportsByMultiplePatientIdentifierAnd() {
		ReferenceAndListParam referenceParam = new ReferenceAndListParam();
		ReferenceParam patient = new ReferenceParam();
		
		patient.setValue(PATIENT_IDENTIFIER);
		patient.setChain(Patient.SP_IDENTIFIER);
		
		ReferenceParam badPatient = new ReferenceParam();
		
		badPatient.setValue(WRONG_PATIENT_IDENTIFIER);
		badPatient.setChain(Patient.SP_IDENTIFIER);
		
		referenceParam.addValue(new ReferenceOrListParam().add(patient)).addAnd(new ReferenceOrListParam().add(badPatient));
		
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.PATIENT_REFERENCE_SEARCH_HANDLER,
		    referenceParam);
		
		IBundleProvider results = search(theParams);
		
		assertThat(results, notNullValue());
		assertThat(results.size(), equalTo(0));
		
		List<DiagnosticReport> resultList = get(results);
		
		assertThat(resultList, empty());
	}
	
	@Test
	public void searchForDiagnosticReports_shouldReturnCorrectObsByPatientName() {
		ReferenceAndListParam patientReference = new ReferenceAndListParam().addAnd(
		    new ReferenceOrListParam().add(new ReferenceParam().setValue(PATIENT_GIVEN_NAME).setChain(Patient.SP_NAME)));
		
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.PATIENT_REFERENCE_SEARCH_HANDLER,
		    patientReference);
		
		IBundleProvider diagnosticReports = search(theParams);
		
		assertThat(diagnosticReports, notNullValue());
		assertThat(diagnosticReports.size(), greaterThanOrEqualTo(1));
		
		List<DiagnosticReport> resultList = get(diagnosticReports);
		
		assertThat(resultList, hasSize(greaterThanOrEqualTo(1)));
		assertThat(resultList.get(0).getIdElement().getIdPart(), equalTo(DIAGNOSTIC_REPORT_UUID));
	}
	
	@Test
	public void searchForDiagnosticReports_shouldSearchForUniqueObsByPatientName() {
		ReferenceParam patientReference = new ReferenceParam(Patient.SP_NAME, "Horatio Hornblower");
		ReferenceAndListParam patientList = new ReferenceAndListParam();
		patientList.addValue(new ReferenceOrListParam().add(patientReference));
		
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.PATIENT_REFERENCE_SEARCH_HANDLER,
		    patientList);
		
		IBundleProvider results = search(theParams);
		
		assertThat(results, notNullValue());
		assertThat(results.size(), equalTo(1));
		
		List<String> resultSet = dao.getSearchResultUuids(theParams);
		
		assertThat(resultSet.size(), equalTo(1)); // 3 with repetitions
	}
	
	@Test
	public void searchForDiagnosticReports_shouldReturnEmptyCollectionByWrongPatientName() {
		ReferenceAndListParam patientReference = new ReferenceAndListParam().addAnd(
		    new ReferenceOrListParam().add(new ReferenceParam().setValue(WRONG_PATIENT_GIVEN_NAME).setChain(SP_NAME)));
		
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.PATIENT_REFERENCE_SEARCH_HANDLER,
		    patientReference);
		
		IBundleProvider diagnosticReports = search(theParams);
		
		assertThat(diagnosticReports, notNullValue());
		assertThat(diagnosticReports.size(), equalTo(0));
		
		List<DiagnosticReport> resultList = get(diagnosticReports);
		
		assertThat(resultList, empty());
	}
	
	@Test
	public void searchForDiagnosticReports_shouldSearchForDiagnosticReportsByMultiplePatientNameOr() {
		ReferenceAndListParam referenceParam = new ReferenceAndListParam();
		ReferenceParam patient = new ReferenceParam();
		
		patient.setValue(PATIENT_GIVEN_NAME);
		patient.setChain(Patient.SP_NAME);
		
		ReferenceParam badPatient = new ReferenceParam();
		
		badPatient.setValue(WRONG_PATIENT_GIVEN_NAME);
		badPatient.setChain(Patient.SP_NAME);
		
		referenceParam.addValue(new ReferenceOrListParam().add(patient).add(badPatient));
		
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.PATIENT_REFERENCE_SEARCH_HANDLER,
		    referenceParam);
		
		IBundleProvider results = search(theParams);
		
		assertThat(results, notNullValue());
		assertThat(results.size(), greaterThanOrEqualTo(1));
		
		List<DiagnosticReport> resultList = get(results);
		
		assertThat(resultList, hasSize(greaterThanOrEqualTo(1)));
		assertThat(resultList.get(0).getIdElement().getIdPart(), equalTo(DIAGNOSTIC_REPORT_UUID));
	}
	
	@Test
	public void searchForDiagnosticReports_shouldReturnEmptyListOfDiagnosticReportsByMultiplePatientNameAnd() {
		ReferenceAndListParam referenceParam = new ReferenceAndListParam();
		ReferenceParam patient = new ReferenceParam();
		
		patient.setValue(PATIENT_GIVEN_NAME);
		patient.setChain(Patient.SP_NAME);
		
		ReferenceParam badPatient = new ReferenceParam();
		
		badPatient.setValue(WRONG_PATIENT_GIVEN_NAME);
		badPatient.setChain(Patient.SP_NAME);
		
		referenceParam.addValue(new ReferenceOrListParam().add(patient)).addAnd(new ReferenceOrListParam().add(badPatient));
		
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.PATIENT_REFERENCE_SEARCH_HANDLER,
		    referenceParam);
		IBundleProvider results = search(theParams);
		
		assertThat(results, notNullValue());
		assertThat(results.size(), equalTo(0));
		
		List<DiagnosticReport> resultList = get(results);
		
		assertThat(resultList, empty());
	}
	
	@Test
	public void searchForDiagnosticReports_shouldReturnCorrectObsByPatientGivenName() {
		ReferenceAndListParam patientReference = new ReferenceAndListParam().addAnd(
		    new ReferenceOrListParam().add(new ReferenceParam().setValue(PATIENT_GIVEN_NAME).setChain(Patient.SP_GIVEN)));
		
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.PATIENT_REFERENCE_SEARCH_HANDLER,
		    patientReference);
		
		IBundleProvider diagnosticReports = search(theParams);
		
		assertThat(diagnosticReports, notNullValue());
		assertThat(diagnosticReports.size(), greaterThanOrEqualTo(1));
		
		List<DiagnosticReport> resultList = get(diagnosticReports);
		
		assertThat(resultList, hasSize(greaterThanOrEqualTo(1)));
		assertThat(resultList.get(0).getIdElement().getIdPart(), equalTo(DIAGNOSTIC_REPORT_UUID));
	}
	
	@Test
	public void searchForDiagnosticReports_shouldSearchForUniqueObsByPatientGivenName() {
		ReferenceParam patientReference = new ReferenceParam(Patient.SP_GIVEN, "Horatio");
		ReferenceAndListParam patientList = new ReferenceAndListParam();
		patientList.addValue(new ReferenceOrListParam().add(patientReference));
		
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.PATIENT_REFERENCE_SEARCH_HANDLER,
		    patientList);
		
		IBundleProvider results = search(theParams);
		
		assertThat(results, notNullValue());
		assertThat(results.size(), equalTo(1));
		
		List<String> resultSet = dao.getSearchResultUuids(theParams);
		
		assertThat(resultSet.size(), equalTo(1)); // 2 with repetitions
	}
	
	@Test
	public void searchForDiagnosticReports_shouldReturnEmptyCollectionByWrongPatientGivenName() {
		ReferenceAndListParam patientReference = new ReferenceAndListParam().addAnd(
		    new ReferenceOrListParam().add(new ReferenceParam().setValue(WRONG_PATIENT_GIVEN_NAME).setChain(SP_GIVEN)));
		
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.PATIENT_REFERENCE_SEARCH_HANDLER,
		    patientReference);
		
		IBundleProvider diagnosticReports = search(theParams);
		
		assertThat(diagnosticReports, notNullValue());
		assertThat(diagnosticReports.size(), equalTo(0));
		
		List<DiagnosticReport> resultList = get(diagnosticReports);
		
		assertThat(resultList, empty());
	}
	
	@Test
	public void searchForDiagnosticReports_shouldSearchForDiagnosticReportsByMultiplePatientGivenNameOr() {
		ReferenceAndListParam referenceParam = new ReferenceAndListParam();
		ReferenceParam patient = new ReferenceParam();
		
		patient.setValue(PATIENT_GIVEN_NAME);
		patient.setChain(Patient.SP_GIVEN);
		
		ReferenceParam badPatient = new ReferenceParam();
		
		badPatient.setValue(WRONG_PATIENT_GIVEN_NAME);
		badPatient.setChain(Patient.SP_GIVEN);
		
		referenceParam.addValue(new ReferenceOrListParam().add(patient).add(badPatient));
		
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.PATIENT_REFERENCE_SEARCH_HANDLER,
		    referenceParam);
		IBundleProvider results = search(theParams);
		
		assertThat(results, notNullValue());
		assertThat(results.size(), greaterThanOrEqualTo(1));
		
		List<DiagnosticReport> resultList = get(results);
		
		assertThat(resultList, hasSize(greaterThanOrEqualTo(1)));
	}
	
	@Test
	public void searchForDiagnosticReports_shouldReturnEmptyListOfDiagnosticReportsByMultiplePatientGivenNameAnd() {
		ReferenceAndListParam referenceParam = new ReferenceAndListParam();
		ReferenceParam patient = new ReferenceParam();
		
		patient.setValue(PATIENT_GIVEN_NAME);
		patient.setChain(Patient.SP_GIVEN);
		
		ReferenceParam badPatient = new ReferenceParam();
		
		badPatient.setValue(WRONG_PATIENT_GIVEN_NAME);
		badPatient.setChain(Patient.SP_GIVEN);
		
		referenceParam.addValue(new ReferenceOrListParam().add(patient)).addAnd(new ReferenceOrListParam().add(badPatient));
		
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.PATIENT_REFERENCE_SEARCH_HANDLER,
		    referenceParam);
		IBundleProvider results = search(theParams);
		
		List<DiagnosticReport> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList, empty());
	}
	
	@Test
	public void searchForDiagnosticReports_shouldReturnCorrectObsByPatientFamilyName() {
		ReferenceAndListParam patientReference = new ReferenceAndListParam().addAnd(
		    new ReferenceOrListParam().add(new ReferenceParam().setValue(PATIENT_FAMILY_NAME).setChain(Patient.SP_FAMILY)));
		
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.PATIENT_REFERENCE_SEARCH_HANDLER,
		    patientReference);
		
		IBundleProvider diagnosticReports = search(theParams);
		
		assertThat(diagnosticReports, notNullValue());
		assertThat(diagnosticReports.size(), greaterThanOrEqualTo(1));
		
		List<DiagnosticReport> resultList = get(diagnosticReports);
		
		assertThat(resultList, hasSize(greaterThanOrEqualTo(1)));
		assertThat(resultList.get(0).getIdElement().getIdPart(), equalTo(DIAGNOSTIC_REPORT_UUID));
	}
	
	@Test
	public void searchForDiagnosticReports_shouldSearchForUniqueObsByPatientFamilyName() {
		ReferenceParam patientReference = new ReferenceParam(Patient.SP_FAMILY, "Hornblower");
		ReferenceAndListParam patientList = new ReferenceAndListParam();
		patientList.addValue(new ReferenceOrListParam().add(patientReference));
		
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.PATIENT_REFERENCE_SEARCH_HANDLER,
		    patientList);
		
		IBundleProvider results = search(theParams);
		
		assertThat(results, notNullValue());
		assertThat(results.size(), equalTo(1));
		
		List<String> resultSet = dao.getSearchResultUuids(theParams);
		
		assertThat(resultSet.size(), equalTo(1)); // 3 with repetitions
	}
	
	@Test
	public void searchForDiagnosticReports_shouldReturnEmptyCollectionByWrongPatientFamilyName() {
		ReferenceAndListParam patientReference = new ReferenceAndListParam().addAnd(
		    new ReferenceOrListParam().add(new ReferenceParam().setValue(WRONG_PATIENT_FAMILY_NAME).setChain(SP_FAMILY)));
		
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.PATIENT_REFERENCE_SEARCH_HANDLER,
		    patientReference);
		
		IBundleProvider diagnosticReports = search(theParams);
		
		assertThat(diagnosticReports, notNullValue());
		assertThat(diagnosticReports.size(), equalTo(0));
		
		List<DiagnosticReport> resultList = get(diagnosticReports);
		
		assertThat(resultList, empty());
	}
	
	@Test
	public void searchForDiagnosticReports_shouldSearchForDiagnosticReportsByMultiplePatientFamilyNameOr() {
		ReferenceAndListParam referenceParam = new ReferenceAndListParam();
		ReferenceParam patient = new ReferenceParam();
		
		patient.setValue(PATIENT_FAMILY_NAME);
		patient.setChain(Patient.SP_FAMILY);
		
		ReferenceParam badPatient = new ReferenceParam();
		
		badPatient.setValue(WRONG_PATIENT_FAMILY_NAME);
		badPatient.setChain(Patient.SP_FAMILY);
		
		referenceParam.addValue(new ReferenceOrListParam().add(patient).add(badPatient));
		
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.PATIENT_REFERENCE_SEARCH_HANDLER,
		    referenceParam);
		
		IBundleProvider results = search(theParams);
		
		assertThat(results, notNullValue());
		assertThat(results.size(), greaterThanOrEqualTo(1));
		
		List<DiagnosticReport> resultList = get(results);
		
		assertThat(resultList, hasSize(greaterThanOrEqualTo(1)));
	}
	
	@Test
	public void searchForDiagnosticReports_shouldReturnEmptyListOfDiagnosticReportsByMultiplePatientFamilyNameAnd() {
		ReferenceAndListParam referenceParam = new ReferenceAndListParam();
		ReferenceParam patient = new ReferenceParam();
		
		patient.setValue(PATIENT_FAMILY_NAME);
		patient.setChain(Patient.SP_FAMILY);
		
		ReferenceParam badPatient = new ReferenceParam();
		
		badPatient.setValue(WRONG_PATIENT_FAMILY_NAME);
		badPatient.setChain(Patient.SP_FAMILY);
		
		referenceParam.addValue(new ReferenceOrListParam().add(patient)).addAnd(new ReferenceOrListParam().add(badPatient));
		
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.PATIENT_REFERENCE_SEARCH_HANDLER,
		    referenceParam);
		
		IBundleProvider results = search(theParams);
		
		assertThat(results, notNullValue());
		assertThat(results.size(), equalTo(0));
		
		List<DiagnosticReport> resultList = get(results);
		
		assertThat(resultList, empty());
	}
	
	@Test
	public void searchForDiagnosticReports_shouldReturnCorrectObsByIssueDate() {
		DateRangeParam issueDate = new DateRangeParam(new DateParam(DIAGNOSTIC_REPORT_DATETIME));
		
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.DATE_RANGE_SEARCH_HANDLER,
		    issueDate);
		
		IBundleProvider diagnosticReports = search(theParams);
		
		List<DiagnosticReport> resultList = get(diagnosticReports);
		
		assertThat(diagnosticReports, notNullValue());
		assertThat(resultList, hasSize(greaterThanOrEqualTo(1)));
		assertThat(resultList.get(0).getIssued().toString(), equalTo(DIAGNOSTIC_REPORT_DATE));
	}
	
	@Test
	public void searchForDiagnosticReports_shouldReturnEmptyCollectionByWrongIssueDate() {
		DateRangeParam issueDate = new DateRangeParam(new DateParam(DIAGNOSTIC_REPORT_WRONG_DATETIME));
		
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.DATE_RANGE_SEARCH_HANDLER,
		    issueDate);
		
		IBundleProvider diagnosticReports = search(theParams);
		
		assertThat(diagnosticReports, notNullValue());
		assertThat(diagnosticReports.size(), equalTo(0));
		
		List<DiagnosticReport> resultList = get(diagnosticReports);
		
		assertThat(resultList, empty());
	}
	
	@Test
	public void searchForDiagnosticReports_shouldReturnCorrectObsByCode() {
		TokenAndListParam code = new TokenAndListParam()
		        .addAnd(new TokenOrListParam().add(new TokenParam(DIAGNOSTIC_REPORT_CODE)));
		
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.CODED_SEARCH_HANDLER, code);
		
		IBundleProvider diagnosticReports = search(theParams);
		
		assertThat(diagnosticReports, notNullValue());
		assertThat(diagnosticReports.size(), greaterThanOrEqualTo(1));
		
		List<DiagnosticReport> resultList = get(diagnosticReports);
		
		assertThat(resultList, hasSize(greaterThanOrEqualTo(1)));
		assertThat(resultList.get(0).getCode().getCodingFirstRep().getCode(), equalTo(CODEABLE_CONCEPT_UUID));
	}
	
	@Test
	public void searchForDiagnosticReports_shouldReturnEmptyCollectionByWrongCode() {
		TokenAndListParam code = new TokenAndListParam()
		        .addAnd(new TokenOrListParam().add(new TokenParam(DIAGNOSTIC_REPORT_WRONG_CODE)));
		
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.CODED_SEARCH_HANDLER, code);
		
		IBundleProvider diagnosticReports = search(theParams);
		
		assertThat(diagnosticReports, notNullValue());
		assertThat(diagnosticReports.size(), equalTo(0));
		
		List<DiagnosticReport> resultList = get(diagnosticReports);
		
		assertThat(resultList, empty());
	}
	
	@Test
	public void searchForDiagnosticReports_shouldReturnCorrectObsByResult() {
		ReferenceAndListParam param = new ReferenceAndListParam()
		        .addAnd(new ReferenceOrListParam().add(new ReferenceParam(OBS_RESULT_UUID)));
		
		SearchParameterMap theParams = new SearchParameterMap().addParameter(RESULT_SEARCH_HANDLER, param);
		
		IBundleProvider diagnosticReports = search(theParams);
		
		assertThat(diagnosticReports, notNullValue());
		assertThat(diagnosticReports.size(), equalTo(1));
		
		List<DiagnosticReport> resultList = get(diagnosticReports);
		
		assertThat(diagnosticReports, notNullValue());
		assertThat(resultList, hasSize(equalTo(1)));
		assertThat(resultList.get(0).getResult(),
		    hasItem(hasProperty("referenceElement", hasProperty("idPart", equalTo(OBS_RESULT_UUID)))));
	}
	
	@Test
	public void searchForDiagnosticReports_shouldReturnEmptyCollectionByWrongObsResult() {
		ReferenceAndListParam param = new ReferenceAndListParam()
		        .addAnd(new ReferenceOrListParam().add(new ReferenceParam(DIAGNOSTIC_REPORT_UUID)));
		
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.RESULT_SEARCH_HANDLER, param);
		
		IBundleProvider diagnosticReports = search(theParams);
		
		assertThat(diagnosticReports, notNullValue());
		assertThat(diagnosticReports.size(), equalTo(0));
		
		List<DiagnosticReport> resultList = get(diagnosticReports);
		
		assertThat(diagnosticReports, notNullValue());
		assertThat(resultList.size(), equalTo(0));
	}
	
	@Test
	public void searchForDiagnosticReports_shouldSearchForObsByUuid() {
		TokenAndListParam uuid = new TokenAndListParam().addAnd(new TokenParam(DIAGNOSTIC_REPORT_UUID));
		
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.COMMON_SEARCH_HANDLER,
		    FhirConstants.ID_PROPERTY, uuid);
		
		IBundleProvider results = search(theParams);
		
		assertThat(results, notNullValue());
		assertThat(results.size(), equalTo(1));
		
		List<DiagnosticReport> resultList = get(results);
		
		assertThat(resultList, hasSize(equalTo(1)));
		assertThat(resultList.get(0).getIdElement().getIdPart(), equalTo(DIAGNOSTIC_REPORT_UUID));
	}
	
	@Test
	public void searchForDiagnosticReports_shouldSearchForObsByLastUpdatedDateCreated() {
		DateRangeParam lastUpdated = new DateRangeParam().setUpperBound(DATE_CREATED).setLowerBound(DATE_CREATED);
		
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.COMMON_SEARCH_HANDLER,
		    FhirConstants.LAST_UPDATED_PROPERTY, lastUpdated);
		
		IBundleProvider results = search(theParams);
		
		assertThat(results, notNullValue());
		assertThat(results.size(), equalTo(1));
		
		List<DiagnosticReport> resultList = get(results);
		
		assertThat(resultList, hasSize(equalTo(1)));
	}
	
	@Test
	public void searchForDiagnosticReports_shouldSearchForObsByMatchingUuidAndLastUpdated() {
		TokenAndListParam uuid = new TokenAndListParam().addAnd(new TokenParam(DIAGNOSTIC_REPORT_UUID));
		DateRangeParam lastUpdated = new DateRangeParam().setUpperBound(DATE_CREATED).setLowerBound(DATE_CREATED);
		
		SearchParameterMap theParams = new SearchParameterMap()
		        .addParameter(FhirConstants.COMMON_SEARCH_HANDLER, FhirConstants.ID_PROPERTY, uuid)
		        .addParameter(FhirConstants.COMMON_SEARCH_HANDLER, FhirConstants.LAST_UPDATED_PROPERTY, lastUpdated);
		
		IBundleProvider results = search(theParams);
		
		assertThat(results, notNullValue());
		assertThat(results.size(), equalTo(1));
		
		List<DiagnosticReport> resultList = get(results);
		
		assertThat(resultList, hasSize(equalTo(1)));
		assertThat(resultList.get(0).getIdElement().getIdPart(), equalTo(DIAGNOSTIC_REPORT_UUID));
	}
	
	@Test
	public void searchForDiagnosticReports_shouldReturnEmptyListByMismatchingUuidAndLastUpdated() {
		TokenAndListParam uuid = new TokenAndListParam().addAnd(new TokenParam(DIAGNOSTIC_REPORT_UUID));
		DateRangeParam lastUpdated = new DateRangeParam().setUpperBound(WRONG_DATE_CREATED)
		        .setLowerBound(WRONG_DATE_CREATED);
		
		SearchParameterMap theParams = new SearchParameterMap()
		        .addParameter(FhirConstants.COMMON_SEARCH_HANDLER, FhirConstants.ID_PROPERTY, uuid)
		        .addParameter(FhirConstants.COMMON_SEARCH_HANDLER, FhirConstants.LAST_UPDATED_PROPERTY, lastUpdated);
		
		IBundleProvider results = search(theParams);
		
		assertThat(results, notNullValue());
		assertThat(results.size(), equalTo(0));
		
		List<DiagnosticReport> resultList = get(results);
		
		assertThat(resultList, empty());
	}
	
	@Test
	public void searchForDiagnosticReports_shouldHandleComplexQuery() {
		TokenAndListParam code = new TokenAndListParam()
		        .addAnd(new TokenOrListParam().add(new TokenParam(DIAGNOSTIC_REPORT_CODE)));
		ReferenceAndListParam patientReference = new ReferenceAndListParam().addAnd(
		    new ReferenceOrListParam().add(new ReferenceParam().setValue(PATIENT_GIVEN_NAME).setChain(Patient.SP_GIVEN)));
		
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.CODED_SEARCH_HANDLER, code)
		        .addParameter(FhirConstants.PATIENT_REFERENCE_SEARCH_HANDLER, patientReference);
		
		IBundleProvider diagnosticReports = search(theParams);
		
		assertThat(diagnosticReports, notNullValue());
		assertThat(diagnosticReports.size(), greaterThanOrEqualTo(1));
		
		List<DiagnosticReport> resultList = get(diagnosticReports);
		
		assertThat(resultList, hasSize(greaterThanOrEqualTo(1)));
		
		DiagnosticReport diagnosticReport = resultList.get(0);
		
		assertThat(diagnosticReport.getCode().getCodingFirstRep().getCode(), equalTo(CODEABLE_CONCEPT_UUID));
		assertThat(diagnosticReport.getIdElement().getIdPart(), equalTo(DIAGNOSTIC_REPORT_UUID));
	}
	
	@Test
	public void searchForDiagnosticReports_shouldSortDiagnosticReportsByIssueDateAsRequested() {
		SortSpec sort = new SortSpec();
		sort.setParamName(DiagnosticReport.SP_ISSUED);
		sort.setOrder(SortOrderEnum.ASC);
		
		List<DiagnosticReport> resultsList = getNonNullObsListForSorting(sort);
		// check if the sorting is indeed correct by ascending order
		for (int i = 1; i < resultsList.size(); i++) {
			assertThat(resultsList.get(i - 1).getIssued(), lessThanOrEqualTo(resultsList.get(i).getIssued()));
		}
		
		sort.setOrder(SortOrderEnum.DESC);
		
		resultsList = getNonNullObsListForSorting(sort);
		// check if the sorting is indeed correct by descending order
		for (int i = 1; i < resultsList.size(); i++) {
			assertThat(resultsList.get(i - 1).getIssued(), greaterThanOrEqualTo(resultsList.get(i).getIssued()));
		}
	}
	
	private List<DiagnosticReport> getNonNullObsListForSorting(SortSpec sort) {
		SearchParameterMap theParams = new SearchParameterMap().setSortSpec(sort);
		
		IBundleProvider diagnosticReports = search(theParams);
		
		assertThat(diagnosticReports, notNullValue());
		assertThat(diagnosticReports.size(), greaterThanOrEqualTo(1));
		
		// collect only those obs which are an obs group
		List<String> matchingResourceUuids = dao.getSearchResultUuids(theParams);
		List<DiagnosticReport> results = dao.getSearchResults(theParams, matchingResourceUuids, START_INDEX, END_INDEX)
		        .stream().filter(Obs::isObsGrouping).map(translator::toFhirResource).collect(Collectors.toList());
		
		assertThat(results, not(empty()));
		assertThat(results.size(), greaterThanOrEqualTo(1));
		
		// Remove diagnostic reports with sort parameter value null, to allow comparison while asserting.
		if (DiagnosticReport.SP_ISSUED.equals(sort.getParamName())) {
			results.removeIf(p -> p.getIssued() == null);
		}
		
		return results;
	}
	
}
