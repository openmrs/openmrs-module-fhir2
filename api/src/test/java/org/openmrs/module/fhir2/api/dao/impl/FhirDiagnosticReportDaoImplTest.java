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
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import ca.uhn.fhir.rest.api.SortOrderEnum;
import ca.uhn.fhir.rest.api.SortSpec;
import ca.uhn.fhir.rest.param.DateParam;
import ca.uhn.fhir.rest.param.DateRangeParam;
import ca.uhn.fhir.rest.param.ReferenceAndListParam;
import ca.uhn.fhir.rest.param.ReferenceOrListParam;
import ca.uhn.fhir.rest.param.ReferenceParam;
import ca.uhn.fhir.rest.param.TokenAndListParam;
import ca.uhn.fhir.rest.param.TokenOrListParam;
import ca.uhn.fhir.rest.param.TokenParam;
import org.hibernate.SessionFactory;
import org.hl7.fhir.r4.model.DiagnosticReport;
import org.hl7.fhir.r4.model.Patient;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.Obs;
import org.openmrs.api.ConceptService;
import org.openmrs.api.ObsService;
import org.openmrs.api.PatientService;
import org.openmrs.module.fhir2.TestFhirSpringConfiguration;
import org.openmrs.test.BaseModuleContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;

@ContextConfiguration(classes = TestFhirSpringConfiguration.class, inheritLocations = false)
public class FhirDiagnosticReportDaoImplTest extends BaseModuleContextSensitiveTest {
	
	private static final String DATA_XML = "org/openmrs/module/fhir2/api/dao/impl/FhirDiagnosticReportDaoImplTest_initial_data.xml";
	
	private static final String UUID = "d899333c-5bd4-45cc-b1e7-2f9542dbcbf6";
	
	private static final String NEW_UUID = "655b64a2-1513-4f07-9d1c-0da7fa80840a";
	
	private static final String CHILD_UUID = "dc386962-1c42-49ea-bed2-97650c66f742";
	
	private static final String ENCOUNTER_UUID = "6519d653-393b-4118-9c83-a3715b82d4ac";
	
	private static final String WRONG_ENCOUNTER_UUID = "6519d653-393b-4118-9c83-a3715b82d4az";
	
	private static final String PATIENT_UUID = "5946f880-b197-400b-9caa-a3c661d23041";
	
	private static final String WRONG_PATIENT_UUID = "5946f880-b197-400b-9caa-a3c661d23043";
	
	private static final String PATIENT_GIVEN_NAME = "Collet";
	
	private static final String WRONG_PATIENT_GIVEN_NAME = "Colletas";
	
	private static final String PATIENT_FAMILY_NAME = "Chebaskwony";
	
	private static final String WRONG_PATIENT_FAMILY_NAME = "Chebaskwonyop";
	
	private static final String DIAGNOSTIC_REPORT_DATETIME = "2008-08-18T14:11:13.0";
	
	private static final String DIAGNOSTIC_REPORT_WRONG_DATETIME = "2008-08-18T14:11:15.0";
	
	private static final String DIAGNOSTIC_REPORT_DATE = "2008-08-18 14:11:13.0";
	
	private static final String DIAGNOSTIC_REPORT_CODE = "5497";
	
	private static final String DIAGNOSTIC_REPORT_WRONG_CODE = "5499";
	
	private static final String PATIENT_IDENTIFIER = "6TS-4";
	
	private static final String WRONG_PATIENT_IDENTIFIER = "6TS-3";
	
	private FhirDiagnosticReportDaoImpl dao;
	
	@Autowired
	private SessionFactory sessionFactory;
	
	@Autowired
	@Qualifier("patientService")
	private PatientService patientService;
	
	@Autowired
	@Qualifier("conceptService")
	private ConceptService conceptService;
	
	@Autowired
	@Qualifier("obsService")
	private ObsService obsService;
	
	@Before
	public void setup() throws Exception {
		dao = new FhirDiagnosticReportDaoImpl();
		dao.setSessionFactory(sessionFactory);
		executeDataSet(DATA_XML);
	}
	
	@Test
	public void getObsGroupByUuid_shouldGetObsGroupByUuid() {
		Obs result = dao.get(UUID);
		
		assertThat(result, notNullValue());
		assertThat(result.getUuid(), equalTo(UUID));
	}
	
	@Test
	public void saveObsGroup_shouldSaveNewObsGroup() {
		Obs newObs = new Obs();
		
		newObs.setUuid(NEW_UUID);
		newObs.setObsDatetime(new Date());
		newObs.setPerson(patientService.getPatient(7));
		newObs.setConcept(conceptService.getConcept(5085));
		newObs.addGroupMember(obsService.getObsByUuid(CHILD_UUID));
		
		Obs result = dao.createOrUpdate(newObs);
		
		assertThat(result, notNullValue());
		assertThat(result.getUuid(), equalTo(NEW_UUID));
		assertThat(result.isObsGrouping(), equalTo(true));
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void saveObsGroup_shouldNotSaveObsWithNoGroupMembers() {
		Obs newObs = new Obs();
		newObs.setUuid(NEW_UUID);
		
		dao.createOrUpdate(newObs);
	}
	
	@Test
	public void saveObsGroup_shouldUpdateExistingObsGroup() {
		Obs newMember = new Obs();
		newMember.setUuid(NEW_UUID);
		
		Obs existingObsGroup = dao.get(UUID);
		existingObsGroup.addGroupMember(newMember);
		
		Obs result = dao.createOrUpdate(existingObsGroup);
		
		assertThat(result, notNullValue());
		assertThat(result.getUuid(), equalTo(UUID));
		assertThat(result.getGroupMembers().size(), equalTo(2));
	}
	
	@Test
	public void searchForDiagnosticReports_shouldReturnCorrectObsByEncounterUUID() {
		ReferenceAndListParam encounterReference = new ReferenceAndListParam()
		        .addAnd(new ReferenceOrListParam().add(new ReferenceParam().setValue(ENCOUNTER_UUID).setChain(null)));
		
		Collection<Obs> diagnosticReports = dao.searchForDiagnosticReports(encounterReference, null, null, null, null);
		
		assertThat(diagnosticReports, notNullValue());
		assertThat(diagnosticReports.size(), greaterThanOrEqualTo(1));
		assertThat(diagnosticReports.iterator().next().getEncounter().getUuid(), equalTo(ENCOUNTER_UUID));
	}
	
	@Test
	public void searchForDiagnosticReports_shouldReturnEmptyCollectionByWrongEncounterUUID() {
		ReferenceAndListParam encounterReference = new ReferenceAndListParam()
		        .addAnd(new ReferenceOrListParam().add(new ReferenceParam().setValue(WRONG_ENCOUNTER_UUID).setChain(null)));
		
		Collection<Obs> diagnosticReports = dao.searchForDiagnosticReports(encounterReference, null, null, null, null);
		
		assertThat(diagnosticReports, notNullValue());
		assertThat(diagnosticReports.size(), equalTo(0));
	}
	
	@Test
	public void searchForDiagnosticReports_shouldReturnCorrectObsByPatientUUID() {
		ReferenceAndListParam patientReference = new ReferenceAndListParam()
		        .addAnd(new ReferenceOrListParam().add(new ReferenceParam().setValue(PATIENT_UUID).setChain(null)));
		
		Collection<Obs> diagnosticReports = dao.searchForDiagnosticReports(null, patientReference, null, null, null);
		
		assertThat(diagnosticReports, notNullValue());
		assertThat(diagnosticReports.size(), greaterThanOrEqualTo(1));
		assertThat(diagnosticReports.iterator().next().getPerson().getUuid(), equalTo(PATIENT_UUID));
	}
	
	@Test
	public void searchForDiagnosticReports_shouldReturnEmptyCollectionByWrongPatientUUID() {
		ReferenceAndListParam patientReference = new ReferenceAndListParam()
		        .addAnd(new ReferenceOrListParam().add(new ReferenceParam().setValue(WRONG_PATIENT_UUID).setChain(null)));
		
		Collection<Obs> diagnosticReports = dao.searchForDiagnosticReports(null, patientReference, null, null, null);
		
		assertThat(diagnosticReports, notNullValue());
		assertThat(diagnosticReports.size(), equalTo(0));
	}
	
	@Test
	public void searchForDiagnosticReports_shouldReturnCorrectObsByPatientIdentifier() {
		ReferenceAndListParam patientReference = new ReferenceAndListParam().addAnd(new ReferenceOrListParam()
		        .add(new ReferenceParam().setValue(PATIENT_IDENTIFIER).setChain(Patient.SP_IDENTIFIER)));
		
		Collection<Obs> diagnosticReports = dao.searchForDiagnosticReports(null, patientReference, null, null, null);
		
		assertThat(diagnosticReports, notNullValue());
		assertThat(diagnosticReports.size(), greaterThanOrEqualTo(1));
		assertThat(diagnosticReports.iterator().next().getPerson().getUuid(), equalTo(PATIENT_UUID));
	}
	
	@Test
	public void searchForDiagnosticReports_shouldReturnEmptyCollectionByWrongPatientIdentifier() {
		ReferenceAndListParam patientReference = new ReferenceAndListParam().addAnd(new ReferenceOrListParam()
		        .add(new ReferenceParam().setValue(WRONG_PATIENT_IDENTIFIER).setChain(Patient.SP_IDENTIFIER)));
		
		Collection<Obs> diagnosticReports = dao.searchForDiagnosticReports(null, patientReference, null, null, null);
		
		assertThat(diagnosticReports, notNullValue());
		assertThat(diagnosticReports.size(), equalTo(0));
	}
	
	@Test
	public void searchForDiagnosticReports_shouldReturnCorrectObsByPatientName() {
		ReferenceAndListParam patientReference = new ReferenceAndListParam().addAnd(
		    new ReferenceOrListParam().add(new ReferenceParam().setValue(PATIENT_GIVEN_NAME).setChain(Patient.SP_NAME)));
		
		Collection<Obs> diagnosticReports = dao.searchForDiagnosticReports(null, patientReference, null, null, null);
		
		assertThat(diagnosticReports, notNullValue());
		assertThat(diagnosticReports.size(), greaterThanOrEqualTo(1));
		
		Obs diagnosticReport = diagnosticReports.iterator().next();
		assertThat(diagnosticReport.getPerson().getGivenName(), equalTo(PATIENT_GIVEN_NAME));
	}
	
	@Test
	public void searchForDiagnosticReports_shouldReturnEmptyCollectionByWrongPatientName() {
		ReferenceAndListParam patientReference = new ReferenceAndListParam().addAnd(new ReferenceOrListParam()
		        .add(new ReferenceParam().setValue(WRONG_PATIENT_GIVEN_NAME).setChain(Patient.SP_NAME)));
		
		Collection<Obs> diagnosticReports = dao.searchForDiagnosticReports(null, patientReference, null, null, null);
		
		assertThat(diagnosticReports, notNullValue());
		assertThat(diagnosticReports.size(), equalTo(0));
	}
	
	@Test
	public void searchForDiagnosticReports_shouldReturnCorrectObsByPatientGivenName() {
		ReferenceAndListParam patientReference = new ReferenceAndListParam().addAnd(
		    new ReferenceOrListParam().add(new ReferenceParam().setValue(PATIENT_GIVEN_NAME).setChain(Patient.SP_GIVEN)));
		
		Collection<Obs> diagnosticReports = dao.searchForDiagnosticReports(null, patientReference, null, null, null);
		
		assertThat(diagnosticReports, notNullValue());
		assertThat(diagnosticReports.size(), greaterThanOrEqualTo(1));
		assertThat(diagnosticReports.iterator().next().getPerson().getGivenName(), equalTo(PATIENT_GIVEN_NAME));
	}
	
	@Test
	public void searchForDiagnosticReports_shouldReturnEmptyCollectionByWrongPatientGivenName() {
		ReferenceAndListParam patientReference = new ReferenceAndListParam().addAnd(new ReferenceOrListParam()
		        .add(new ReferenceParam().setValue(WRONG_PATIENT_GIVEN_NAME).setChain(Patient.SP_GIVEN)));
		
		Collection<Obs> diagnosticReports = dao.searchForDiagnosticReports(null, patientReference, null, null, null);
		
		assertThat(diagnosticReports, notNullValue());
		assertThat(diagnosticReports.size(), equalTo(0));
	}
	
	@Test
	public void searchForDiagnosticReports_shouldReturnCorrectObsByPatientFamilyName() {
		ReferenceAndListParam patientReference = new ReferenceAndListParam().addAnd(
		    new ReferenceOrListParam().add(new ReferenceParam().setValue(PATIENT_FAMILY_NAME).setChain(Patient.SP_FAMILY)));
		
		Collection<Obs> diagnosticReports = dao.searchForDiagnosticReports(null, patientReference, null, null, null);
		
		assertThat(diagnosticReports, notNullValue());
		assertThat(diagnosticReports.size(), greaterThanOrEqualTo(1));
		assertThat(diagnosticReports.iterator().next().getPerson().getFamilyName(), equalTo(PATIENT_FAMILY_NAME));
	}
	
	@Test
	public void searchForDiagnosticReports_shouldReturnEmptyCollectionByWrongPatientFamilyName() {
		ReferenceAndListParam patientReference = new ReferenceAndListParam().addAnd(new ReferenceOrListParam()
		        .add(new ReferenceParam().setValue(WRONG_PATIENT_FAMILY_NAME).setChain(Patient.SP_FAMILY)));
		
		Collection<Obs> diagnosticReports = dao.searchForDiagnosticReports(null, patientReference, null, null, null);
		
		assertThat(diagnosticReports, notNullValue());
		assertThat(diagnosticReports.size(), equalTo(0));
	}
	
	@Test
	public void searchForDiagnosticReports_shouldReturnCorrectObsByIssueDate() {
		DateRangeParam issueDate = new DateRangeParam(new DateParam(DIAGNOSTIC_REPORT_DATETIME));
		
		Collection<Obs> diagnosticReports = dao.searchForDiagnosticReports(null, null, issueDate, null, null);
		
		assertThat(diagnosticReports, notNullValue());
		assertThat(diagnosticReports.size(), greaterThanOrEqualTo(1));
		assertThat(diagnosticReports.iterator().next().getDateCreated().toString(), equalTo(DIAGNOSTIC_REPORT_DATE));
	}
	
	@Test
	public void searchForDiagnosticReports_shouldReturnEmptyCollectionByWrongIssueDate() {
		DateRangeParam issueDate = new DateRangeParam(new DateParam(DIAGNOSTIC_REPORT_WRONG_DATETIME));
		
		Collection<Obs> diagnosticReports = dao.searchForDiagnosticReports(null, null, issueDate, null, null);
		
		assertThat(diagnosticReports, notNullValue());
		assertThat(diagnosticReports.size(), equalTo(0));
	}
	
	@Test
	public void searchForDiagnosticReports_shouldReturnCorrectObsByCode() {
		TokenAndListParam code = new TokenAndListParam()
		        .addAnd(new TokenOrListParam().add(new TokenParam(DIAGNOSTIC_REPORT_CODE)));
		
		Collection<Obs> diagnosticReports = dao.searchForDiagnosticReports(null, null, null, code, null);
		
		assertThat(diagnosticReports, notNullValue());
		assertThat(diagnosticReports.size(), greaterThanOrEqualTo(1));
		assertThat(diagnosticReports.iterator().next().getConcept().getId().toString(), equalTo(DIAGNOSTIC_REPORT_CODE));
	}
	
	@Test
	public void searchForDiagnosticReports_shouldReturnEmptyCollectionByWrongCode() {
		TokenAndListParam code = new TokenAndListParam()
		        .addAnd(new TokenOrListParam().add(new TokenParam(DIAGNOSTIC_REPORT_WRONG_CODE)));
		
		Collection<Obs> diagnosticReports = dao.searchForDiagnosticReports(null, null, null, code, null);
		
		assertThat(diagnosticReports, notNullValue());
		assertThat(diagnosticReports.size(), equalTo(0));
	}
	
	@Test
	public void searchForDiagnosticReports_shouldHandleComplexQuery() {
		TokenAndListParam code = new TokenAndListParam()
		        .addAnd(new TokenOrListParam().add(new TokenParam(DIAGNOSTIC_REPORT_CODE)));
		ReferenceAndListParam patientReference = new ReferenceAndListParam().addAnd(
		    new ReferenceOrListParam().add(new ReferenceParam().setValue(PATIENT_GIVEN_NAME).setChain(Patient.SP_GIVEN)));
		Collection<Obs> diagnosticReports = dao.searchForDiagnosticReports(null, patientReference, null, code, null);
		
		assertThat(diagnosticReports, notNullValue());
		assertThat(diagnosticReports.size(), equalTo(2));
		
		Obs diagnosticReport = diagnosticReports.iterator().next();
		assertThat(diagnosticReport.getConcept().getId().toString(), equalTo(DIAGNOSTIC_REPORT_CODE));
		assertThat(diagnosticReport.getPerson().getGivenName(), equalTo(PATIENT_GIVEN_NAME));
	}
	
	@Test
	public void searchForDiagnosticReports_shouldSortDiagnosticReportsByIssueDateAsRequested() {
		SortSpec sort = new SortSpec();
		sort.setParamName(DiagnosticReport.SP_ISSUED);
		sort.setOrder(SortOrderEnum.ASC);
		
		List<Obs> resultsList = new ArrayList<>(getNonNullObsListForSorting(sort));
		// check if the sorting is indeed correct by ascending order
		for (int i = 1; i < resultsList.size(); i++) {
			assertThat(resultsList.get(i - 1).getDateCreated(), lessThanOrEqualTo(resultsList.get(i).getDateCreated()));
		}
		
		sort.setOrder(SortOrderEnum.DESC);
		
		resultsList = new ArrayList<>(getNonNullObsListForSorting(sort));
		// check if the sorting is indeed correct by descending order
		for (int i = 1; i < resultsList.size(); i++) {
			assertThat(resultsList.get(i - 1).getDateCreated(), greaterThanOrEqualTo(resultsList.get(i).getDateCreated()));
		}
	}
	
	private List<Obs> getNonNullObsListForSorting(SortSpec sort) {
		Collection<Obs> diagnosticReports = dao.searchForDiagnosticReports(null, null, null, null, sort);
		
		assertThat(diagnosticReports, notNullValue());
		assertThat(diagnosticReports, not(empty()));
		assertThat(diagnosticReports.size(), greaterThan(1));
		
		List<Obs> obsList = new ArrayList<>(diagnosticReports);
		// Remove diagnostic reports with sort parameter value null, to allow comparison while asserting.
		switch (sort.getParamName()) {
			case DiagnosticReport.SP_ISSUED:
				obsList.removeIf(p -> p.getDateCreated() == null);
				break;
			case DiagnosticReport.SP_DATE:
				obsList.removeIf(p -> p.getObsDatetime() == null);
				break;
		}
		
		return obsList;
	}
}
