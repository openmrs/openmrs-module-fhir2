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
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
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
import ca.uhn.fhir.rest.param.TokenOrListParam;
import ca.uhn.fhir.rest.param.TokenParam;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.Encounter;
import org.hl7.fhir.r4.model.Medication;
import org.hl7.fhir.r4.model.MedicationRequest;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.Practitioner;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.DrugOrder;
import org.openmrs.module.fhir2.BaseFhirContextSensitiveTest;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.FhirTestConstants;
import org.openmrs.module.fhir2.api.dao.FhirMedicationRequestDao;
import org.openmrs.module.fhir2.api.search.param.SearchParameterMap;
import org.openmrs.module.fhir2.api.translators.MedicationRequestTranslator;
import org.springframework.beans.factory.annotation.Autowired;

/*
 * Notes on test data:
 * 		standardTestDataset has 10 drug orders, of which:
 * 			0 are voided
 * 			2 are discontinue orders
 * 			3 of the non-voided, non-discontinue orders are stopped
 * 			0 of the non-voided, non-discontinue orders are expired
 * 		FhirMedicationRequestDaoImp has 5 drug orders, of which:
 * 			1 is voided
 * 			1 is discontinue order
 * 			0 of the non-voided, non-discontinue orders are stopped
 * 			3 of the non-voided, non-discontinue orders are expired
 *
 * 		Total numbers of non-voided, non-discontinued orders = (10 - 2) + (5 - 2) = 11
 * 		Total number of active orders (non-voided, non-discontunue, not stopped and not expired) = (10 - 5) + (5 - 5) = 5
 */
public class MedicationRequestSearchQueryTest extends BaseFhirContextSensitiveTest {
	
	private static final String MEDICATION_REQUEST_DATA_XMLS = "org/openmrs/module/fhir2/api/dao/impl/FhirMedicationRequestDaoImpl_initial_data.xml";
	
	private static final String DATA_XMLS = "org/openmrs/module/fhir2/api/dao/impl/FhirEncounterDaoImpl_2_2Test_initial_data.xml";
	
	private static final String MEDICATION_REQUEST_UUID = "6d0ae116-707a-4629-9850-f15206e63ab0";
	
	private static final String DISCONTINUE_ORDER_UUID = "b951a436-c775-4dfc-9432-e19446d18c28";
	
	private static final String NON_ACTIVE_ORDER_UUID = "dfca4077-493c-496b-8312-856ee5d1cc26";
	
	private static final String DRUG_ORDER_WITH_FULFILLER_STATUS_COMPLETED_UUID = "ac463525-9b1a-44f2-99f1-0d6a10d5b60d";
	
	private static final String PATIENT_UUID = "86526ed5-3c11-11de-a0ba-001e3766667a";
	
	private static final String PATIENT_GIVEN_NAME = "Moody";
	
	private static final String PATIENT_FAMILY_NAME = "Oregon";
	
	private static final String PATIENT_IDENTIFIER = "MO-2";
	
	private static final String ENCOUNTER_UUID = "bb0af6222-707a-9029-9859-f15206e63ab1";
	
	private static final String PARTICIPANT_UUID = "c2299800-cca9-11e0-9572-0800200c9a66";
	
	private static final String WRONG_UUID = "c2299800-cca9-11e0-9572-abcdef0c9a66";
	
	private static final String PARTICIPANT_GIVEN_NAME = "Super";
	
	private static final String WRONG_GIVEN_NAME = "Wrong given name";
	
	private static final String PARTICIPANT_FAMILY_NAME = "User";
	
	private static final String WRONG_FAMILY_NAME = "Wrong family name";
	
	private static final String PARTICIPANT_IDENTIFIER = "Test";
	
	private static final String WRONG_IDENTIFIER = "Wrong identifier";
	
	private static final String WRONG_NAME = "Wrong name";
	
	private static final String MEDICATION_UUID = "42f00b94-26fe-102b-80cb-0017a47871b2";
	
	private static final String MEDICATION_REQUEST_CONCEPT_ID = "4020";
	
	private static final String MEDICATION_REQUEST_CONCEPT_UUID = "d102c80f-1yz9-4da3-bb88-8122ce889090";
	
	private static final String MEDICATION_REQUEST_LOINC_CODE = "2343253";
	
	private static final String[] LOINC_TB_DRUG_CODES = new String[] { "2343253", "2343256" };
	
	private static final String CIEL_RIFAMPICIN = "2343257";
	
	private static final String LOINC_BENDAQUILINE = "2343253";
	
	private static final String RIFAMPICIN_CONCEPT_ID = "4022";
	
	private static final String DATE_CREATED = "2016-08-19";
	
	private static final String DATE_VOIDED = "2008-11-20";
	
	private static final String STATUS = "ACTIVE";
	
	@Autowired
	private MedicationRequestTranslator translator;
	
	@Autowired
	private FhirMedicationRequestDao dao;
	
	@Autowired
	private SearchQueryInclude<MedicationRequest> searchQueryInclude;
	
	@Autowired
	private SearchQuery<DrugOrder, MedicationRequest, FhirMedicationRequestDao, MedicationRequestTranslator, SearchQueryInclude<MedicationRequest>> searchQuery;
	
	@Before
	public void setup() throws Exception {
		executeDataSet(MEDICATION_REQUEST_DATA_XMLS);
	}
	
	@Test
	public void searchForMedicationRequest_shouldReturnMedicationRequestByPatientUuid() {
		ReferenceAndListParam patientReference = new ReferenceAndListParam()
		        .addAnd(new ReferenceOrListParam().add(new ReferenceParam().setValue(PATIENT_UUID)));
		
		SearchParameterMap theParams = new SearchParameterMap();
		theParams.addParameter(FhirConstants.PATIENT_REFERENCE_SEARCH_HANDLER, patientReference);
		
		IBundleProvider results = search(theParams);
		
		assertThat(results, notNullValue());
		
		List<MedicationRequest> resultList = get(results);
		
		assertThat(resultList, not(empty()));
		assertThat(resultList, hasItem(hasProperty("id", equalTo(MEDICATION_REQUEST_UUID))));
	}
	
	// see: https://issues.openmrs.org/browse/FM2-532
	@Test
	public void searchForMedicationRequest_shouldNotIncludeDiscontinuedOrder() {
		ReferenceAndListParam patientReference = new ReferenceAndListParam()
		        .addAnd(new ReferenceOrListParam().add(new ReferenceParam().setValue(PATIENT_UUID)));
		
		SearchParameterMap theParams = new SearchParameterMap();
		theParams.addParameter(FhirConstants.PATIENT_REFERENCE_SEARCH_HANDLER, patientReference);
		
		IBundleProvider results = search(theParams);
		
		assertThat(results, notNullValue());
		
		List<MedicationRequest> resultList = get(results);
		
		assertThat(resultList, not(empty()));
		assertThat(resultList, not(hasItem(hasProperty("id", equalTo(DISCONTINUE_ORDER_UUID)))));
	}
	
	@Test
	public void searchForMedicationRequests_shouldSearchForMedicationRequestsByMultiplePatientUuidOr() {
		ReferenceAndListParam referenceParam = new ReferenceAndListParam();
		ReferenceParam patient = new ReferenceParam();
		
		patient.setValue(PATIENT_UUID);
		
		ReferenceParam badPatient = new ReferenceParam();
		
		badPatient.setValue(WRONG_UUID);
		
		referenceParam.addValue(new ReferenceOrListParam().add(patient).add(badPatient));
		
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.PATIENT_REFERENCE_SEARCH_HANDLER,
		    referenceParam);
		
		IBundleProvider results = search(theParams);
		
		List<MedicationRequest> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList, not(empty()));
		assertThat(resultList, hasSize(greaterThanOrEqualTo(1)));
		assertThat(resultList, hasItem(hasProperty("id", equalTo(MEDICATION_REQUEST_UUID))));
	}
	
	@Test
	public void searchForMedicationRequests_shouldReturnEmptyListOfMedicationRequestsByMultiplePatientUuidAnd() {
		ReferenceAndListParam referenceParam = new ReferenceAndListParam();
		ReferenceParam patient = new ReferenceParam();
		
		patient.setValue(PATIENT_UUID);
		
		ReferenceParam badPatient = new ReferenceParam();
		
		badPatient.setValue(WRONG_UUID);
		
		referenceParam.addValue(new ReferenceOrListParam().add(patient)).addAnd(new ReferenceOrListParam().add(badPatient));
		
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.PATIENT_REFERENCE_SEARCH_HANDLER,
		    referenceParam);
		IBundleProvider results = search(theParams);
		
		List<MedicationRequest> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList, empty());
	}
	
	@Test
	public void searchForMedicationRequest_shouldReturnMedicationRequestByPatientGivenName() {
		ReferenceAndListParam patientReference = new ReferenceAndListParam().addAnd(
		    new ReferenceOrListParam().add(new ReferenceParam().setValue(PATIENT_GIVEN_NAME).setChain(Patient.SP_GIVEN)));
		
		SearchParameterMap theParams = new SearchParameterMap();
		theParams.addParameter(FhirConstants.PATIENT_REFERENCE_SEARCH_HANDLER, patientReference);
		
		IBundleProvider results = search(theParams);
		
		assertThat(results, notNullValue());
		
		List<MedicationRequest> resultList = get(results);
		
		assertThat(resultList, not(empty()));
		assertThat(resultList, hasItem(hasProperty("id", equalTo(MEDICATION_REQUEST_UUID))));
	}
	
	@Test
	public void searchForMedicationRequests_shouldSearchForMedicationRequestsByMultiplePatientGivenNameOr() {
		ReferenceAndListParam referenceParam = new ReferenceAndListParam();
		ReferenceParam patient = new ReferenceParam();
		
		patient.setValue(PATIENT_GIVEN_NAME);
		patient.setChain(Patient.SP_GIVEN);
		
		ReferenceParam badPatient = new ReferenceParam();
		
		badPatient.setValue(WRONG_GIVEN_NAME);
		badPatient.setChain(Patient.SP_GIVEN);
		
		referenceParam.addValue(new ReferenceOrListParam().add(patient).add(badPatient));
		
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.PATIENT_REFERENCE_SEARCH_HANDLER,
		    referenceParam);
		IBundleProvider results = search(theParams);
		
		List<MedicationRequest> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList, not(empty()));
		assertThat(resultList, hasSize(greaterThanOrEqualTo(1)));
	}
	
	@Test
	public void searchForMedicationRequests_shouldReturnEmptyListOfMedicationRequestsByMultiplePatientGivenNameAnd() {
		ReferenceAndListParam referenceParam = new ReferenceAndListParam();
		ReferenceParam patient = new ReferenceParam();
		
		patient.setValue(PATIENT_GIVEN_NAME);
		patient.setChain(Patient.SP_GIVEN);
		
		ReferenceParam badPatient = new ReferenceParam();
		
		badPatient.setValue(WRONG_GIVEN_NAME);
		badPatient.setChain(Patient.SP_GIVEN);
		
		referenceParam.addValue(new ReferenceOrListParam().add(patient)).addAnd(new ReferenceOrListParam().add(badPatient));
		
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.PATIENT_REFERENCE_SEARCH_HANDLER,
		    referenceParam);
		IBundleProvider results = search(theParams);
		
		List<MedicationRequest> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList, empty());
	}
	
	@Test
	public void searchForMedicationRequest_shouldReturnMedicationRequestByPatientFamilyName() {
		ReferenceAndListParam patientReference = new ReferenceAndListParam().addAnd(
		    new ReferenceOrListParam().add(new ReferenceParam().setValue(PATIENT_FAMILY_NAME).setChain(Patient.SP_FAMILY)));
		
		SearchParameterMap theParams = new SearchParameterMap();
		theParams.addParameter(FhirConstants.PATIENT_REFERENCE_SEARCH_HANDLER, patientReference);
		
		IBundleProvider results = search(theParams);
		assertThat(results, notNullValue());
		
		List<MedicationRequest> resultList = get(results);
		
		assertThat(resultList, not(empty()));
		assertThat(resultList, hasItem(hasProperty("id", equalTo(MEDICATION_REQUEST_UUID))));
	}
	
	@Test
	public void searchForMedicationRequests_shouldSearchForMedicationRequestsByMultiplePatientFamilyNameOr() {
		ReferenceAndListParam referenceParam = new ReferenceAndListParam();
		ReferenceParam patient = new ReferenceParam();
		
		patient.setValue(PATIENT_FAMILY_NAME);
		patient.setChain(Patient.SP_FAMILY);
		
		ReferenceParam badPatient = new ReferenceParam();
		
		badPatient.setValue(WRONG_FAMILY_NAME);
		badPatient.setChain(Patient.SP_FAMILY);
		
		referenceParam.addValue(new ReferenceOrListParam().add(patient).add(badPatient));
		
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.PATIENT_REFERENCE_SEARCH_HANDLER,
		    referenceParam);
		IBundleProvider results = search(theParams);
		
		List<MedicationRequest> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList, not(empty()));
		assertThat(resultList, hasSize(greaterThanOrEqualTo(1)));
	}
	
	@Test
	public void searchForMedicationRequests_shouldReturnEmptyListOfMedicationRequestsByMultiplePatientFamilyNameAnd() {
		ReferenceAndListParam referenceParam = new ReferenceAndListParam();
		ReferenceParam patient = new ReferenceParam();
		
		patient.setValue(PATIENT_FAMILY_NAME);
		patient.setChain(Patient.SP_FAMILY);
		
		ReferenceParam badPatient = new ReferenceParam();
		
		badPatient.setValue(WRONG_FAMILY_NAME);
		badPatient.setChain(Patient.SP_FAMILY);
		
		referenceParam.addValue(new ReferenceOrListParam().add(patient)).addAnd(new ReferenceOrListParam().add(badPatient));
		
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.PATIENT_REFERENCE_SEARCH_HANDLER,
		    referenceParam);
		IBundleProvider results = search(theParams);
		
		List<MedicationRequest> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList, empty());
	}
	
	@Test
	public void searchForMedicationRequest_shouldReturnMedicationRequestByPatientName() {
		ReferenceAndListParam patientReference = new ReferenceAndListParam().addAnd(new ReferenceOrListParam().add(
		    new ReferenceParam().setValue(PATIENT_GIVEN_NAME + " " + PATIENT_FAMILY_NAME).setChain(Patient.SP_NAME)));
		
		SearchParameterMap theParams = new SearchParameterMap();
		theParams.addParameter(FhirConstants.PATIENT_REFERENCE_SEARCH_HANDLER, patientReference);
		
		IBundleProvider results = search(theParams);
		
		assertThat(results, notNullValue());
		
		List<MedicationRequest> resultList = get(results);
		
		assertThat(resultList, not(empty()));
		assertThat(resultList, hasItem(hasProperty("id", equalTo(MEDICATION_REQUEST_UUID))));
	}
	
	@Test
	public void searchForMedicationRequests_shouldSearchForMedicationRequestsByMultiplePatientNameOr() {
		ReferenceAndListParam referenceParam = new ReferenceAndListParam();
		ReferenceParam patient = new ReferenceParam();
		
		patient.setValue(PATIENT_GIVEN_NAME + " " + PATIENT_FAMILY_NAME);
		patient.setChain(Patient.SP_NAME);
		
		ReferenceParam badPatient = new ReferenceParam();
		
		badPatient.setValue(WRONG_NAME);
		badPatient.setChain(Patient.SP_NAME);
		
		referenceParam.addValue(new ReferenceOrListParam().add(patient).add(badPatient));
		
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.PATIENT_REFERENCE_SEARCH_HANDLER,
		    referenceParam);
		IBundleProvider results = search(theParams);
		
		List<MedicationRequest> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList, not(empty()));
		assertThat(resultList, hasSize(greaterThanOrEqualTo(1)));
	}
	
	@Test
	public void searchForMedicationRequests_shouldReturnEmptyListOfMedicationRequestsByMultiplePatientNameAnd() {
		ReferenceAndListParam referenceParam = new ReferenceAndListParam();
		ReferenceParam patient = new ReferenceParam();
		
		patient.setValue(PATIENT_GIVEN_NAME + " " + PATIENT_FAMILY_NAME);
		patient.setChain(Patient.SP_NAME);
		
		ReferenceParam badPatient = new ReferenceParam();
		
		badPatient.setValue(WRONG_NAME);
		badPatient.setChain(Patient.SP_NAME);
		
		referenceParam.addValue(new ReferenceOrListParam().add(patient)).addAnd(new ReferenceOrListParam().add(badPatient));
		
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.PATIENT_REFERENCE_SEARCH_HANDLER,
		    referenceParam);
		IBundleProvider results = search(theParams);
		
		List<MedicationRequest> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList, empty());
	}
	
	@Test
	public void searchForMedicationRequest_shouldReturnMedicationRequestByPatientIdentifier() {
		ReferenceAndListParam patientReference = new ReferenceAndListParam().addAnd(new ReferenceOrListParam()
		        .add(new ReferenceParam().setValue(PATIENT_IDENTIFIER).setChain(Patient.SP_IDENTIFIER)));
		SearchParameterMap theParams = new SearchParameterMap();
		theParams.addParameter(FhirConstants.PATIENT_REFERENCE_SEARCH_HANDLER, patientReference);
		
		IBundleProvider results = search(theParams);
		
		assertThat(results, notNullValue());
		
		List<MedicationRequest> resultList = get(results);
		
		assertThat(resultList, not(empty()));
		assertThat(resultList, hasItem(hasProperty("id", equalTo(MEDICATION_REQUEST_UUID))));
	}
	
	@Test
	public void searchForMedicationRequests_shouldSearchForMedicationRequestsByMultiplePatientIdentifierOr() {
		ReferenceAndListParam referenceParam = new ReferenceAndListParam();
		ReferenceParam patient = new ReferenceParam();
		
		patient.setValue(PATIENT_IDENTIFIER);
		patient.setChain(Patient.SP_IDENTIFIER);
		
		ReferenceParam badPatient = new ReferenceParam();
		
		badPatient.setValue(WRONG_IDENTIFIER);
		badPatient.setChain(Patient.SP_IDENTIFIER);
		
		referenceParam.addValue(new ReferenceOrListParam().add(patient).add(badPatient));
		
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.PATIENT_REFERENCE_SEARCH_HANDLER,
		    referenceParam);
		
		IBundleProvider results = search(theParams);
		
		List<MedicationRequest> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList, not(empty()));
		assertThat(resultList, hasSize(greaterThanOrEqualTo(1)));
	}
	
	@Test
	public void searchForMedicationRequests_shouldReturnEmptyListOfMedicationRequestsByMultiplePatientIdentifierAnd() {
		ReferenceAndListParam referenceParam = new ReferenceAndListParam();
		ReferenceParam patient = new ReferenceParam();
		
		patient.setValue(PATIENT_IDENTIFIER);
		patient.setChain(Patient.SP_IDENTIFIER);
		
		ReferenceParam badPatient = new ReferenceParam();
		
		badPatient.setValue(WRONG_IDENTIFIER);
		badPatient.setChain(Patient.SP_IDENTIFIER);
		
		referenceParam.addValue(new ReferenceOrListParam().add(patient)).addAnd(new ReferenceOrListParam().add(badPatient));
		
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.PATIENT_REFERENCE_SEARCH_HANDLER,
		    referenceParam);
		IBundleProvider results = search(theParams);
		
		List<MedicationRequest> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList, empty());
	}
	
	@Test
	public void searchForMedicationRequest_shouldReturnMedicationRequestByEncounter() {
		ReferenceAndListParam encounterReference = new ReferenceAndListParam()
		        .addAnd(new ReferenceOrListParam().add(new ReferenceParam().setValue(ENCOUNTER_UUID)));
		
		SearchParameterMap theParams = new SearchParameterMap();
		theParams.addParameter(FhirConstants.ENCOUNTER_REFERENCE_SEARCH_HANDLER, encounterReference);
		
		IBundleProvider results = search(theParams);
		
		assertThat(results, notNullValue());
		
		List<MedicationRequest> resultList = get(results);
		
		assertThat(resultList, notNullValue());
		assertThat(resultList, hasItem(hasProperty("id", equalTo(MEDICATION_REQUEST_UUID))));
	}
	
	@Test
	public void searchForMedicationRequest_shouldReturnMedicationRequestByParticipantUuid() {
		ReferenceAndListParam participantReference = new ReferenceAndListParam()
		        .addAnd(new ReferenceOrListParam().add(new ReferenceParam().setValue(PARTICIPANT_UUID)));
		
		SearchParameterMap theParams = new SearchParameterMap();
		theParams.addParameter(FhirConstants.PARTICIPANT_REFERENCE_SEARCH_HANDLER, participantReference);
		
		IBundleProvider results = search(theParams);
		
		assertThat(results, notNullValue());
		List<MedicationRequest> resultList = get(results);
		
		assertThat(resultList, notNullValue());
		assertThat(resultList, hasSize(equalTo(11)));
	}
	
	@Test
	public void searchForMedicationRequest_shouldSearchForMedicationRequestByMultipleParticipantUuidOr() {
		ReferenceAndListParam participantReference = new ReferenceAndListParam();
		ReferenceParam participant = new ReferenceParam();
		
		participant.setValue(PARTICIPANT_UUID);
		
		ReferenceParam badParticipant = new ReferenceParam();
		
		badParticipant.setValue(WRONG_UUID);
		
		participantReference.addValue(new ReferenceOrListParam().add(participant).add(badParticipant));
		
		SearchParameterMap theParams = new SearchParameterMap()
		        .addParameter(FhirConstants.PARTICIPANT_REFERENCE_SEARCH_HANDLER, participantReference);
		
		IBundleProvider results = search(theParams);
		
		assertThat(results, notNullValue());
		List<MedicationRequest> resultList = get(results);
		
		assertThat(resultList, not(empty()));
		assertThat(resultList, hasSize(equalTo(11)));
	}
	
	@Test
	public void searchForMedicationRequest_shouldReturnEmptyListOfMedicationRequestByMultipleParticipantUuidAnd() {
		ReferenceAndListParam participantReference = new ReferenceAndListParam();
		ReferenceParam participant = new ReferenceParam();
		
		participant.setValue(PARTICIPANT_UUID);
		
		ReferenceParam badParticipant = new ReferenceParam();
		
		badParticipant.setValue(WRONG_UUID);
		
		participantReference.addValue(new ReferenceOrListParam().add(participant))
		        .addAnd(new ReferenceOrListParam().add(badParticipant));
		
		SearchParameterMap theParams = new SearchParameterMap()
		        .addParameter(FhirConstants.PARTICIPANT_REFERENCE_SEARCH_HANDLER, participantReference);
		IBundleProvider results = search(theParams);
		
		List<MedicationRequest> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList, empty());
	}
	
	@Test
	public void searchForMedicationRequest_shouldReturnMedicationRequestByParticipantGivenName() {
		ReferenceAndListParam participantReference = new ReferenceAndListParam().addAnd(new ReferenceOrListParam()
		        .add(new ReferenceParam().setValue(PARTICIPANT_GIVEN_NAME).setChain(Practitioner.SP_GIVEN)));
		
		SearchParameterMap theParams = new SearchParameterMap();
		theParams.addParameter(FhirConstants.PARTICIPANT_REFERENCE_SEARCH_HANDLER, participantReference);
		
		IBundleProvider results = search(theParams);
		
		assertThat(results, notNullValue());
		List<MedicationRequest> resultList = get(results);
		
		assertThat(resultList, notNullValue());
		assertThat(resultList, hasSize(equalTo(11)));
	}
	
	@Test
	public void searchForMedicationRequests_shouldSearchForMedicationRequestsByMultipleParticipantGivenNameOr() {
		ReferenceAndListParam participantReference = new ReferenceAndListParam();
		ReferenceParam participant = new ReferenceParam();
		
		participant.setValue(PARTICIPANT_GIVEN_NAME);
		participant.setChain(Practitioner.SP_GIVEN);
		
		ReferenceParam badParticipant = new ReferenceParam();
		
		badParticipant.setValue(WRONG_GIVEN_NAME);
		badParticipant.setChain(Practitioner.SP_GIVEN);
		
		participantReference.addValue(new ReferenceOrListParam().add(participant).add(badParticipant));
		
		SearchParameterMap theParams = new SearchParameterMap()
		        .addParameter(FhirConstants.PARTICIPANT_REFERENCE_SEARCH_HANDLER, participantReference);
		
		IBundleProvider results = search(theParams);
		
		assertThat(results, notNullValue());
		List<MedicationRequest> resultList = get(results);
		
		assertThat(resultList, not(empty()));
		assertThat(resultList, hasSize(equalTo(11)));
	}
	
	@Test
	public void searchForMedicationRequests_shouldReturnEmptyListOfMedicationRequestsByMultipleParticipantGivenNameAnd() {
		ReferenceAndListParam participantReference = new ReferenceAndListParam();
		ReferenceParam participant = new ReferenceParam();
		
		participant.setValue(PARTICIPANT_GIVEN_NAME);
		participant.setChain(Practitioner.SP_GIVEN);
		
		ReferenceParam badParticipant = new ReferenceParam();
		
		badParticipant.setValue(WRONG_GIVEN_NAME);
		badParticipant.setChain(Practitioner.SP_GIVEN);
		
		participantReference.addValue(new ReferenceOrListParam().add(participant))
		        .addAnd(new ReferenceOrListParam().add(badParticipant));
		
		SearchParameterMap theParams = new SearchParameterMap()
		        .addParameter(FhirConstants.PARTICIPANT_REFERENCE_SEARCH_HANDLER, participantReference);
		IBundleProvider results = search(theParams);
		
		List<MedicationRequest> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList, empty());
	}
	
	@Test
	public void searchForMedicationRequest_shouldReturnMedicationRequestByParticipantFamilyName() {
		ReferenceAndListParam participantReference = new ReferenceAndListParam().addAnd(new ReferenceOrListParam()
		        .add(new ReferenceParam().setValue(PARTICIPANT_FAMILY_NAME).setChain(Practitioner.SP_FAMILY)));
		
		SearchParameterMap theParams = new SearchParameterMap();
		theParams.addParameter(FhirConstants.PARTICIPANT_REFERENCE_SEARCH_HANDLER, participantReference);
		
		IBundleProvider results = search(theParams);
		
		assertThat(results, notNullValue());
		List<MedicationRequest> resultList = get(results);
		
		assertThat(resultList, notNullValue());
		assertThat(resultList, hasSize(equalTo(11)));
	}
	
	@Test
	public void searchForMedicationRequests_shouldSearchForMedicationRequestsByMultipleParticipantFamilyNameOr() {
		ReferenceAndListParam participantReference = new ReferenceAndListParam();
		ReferenceParam participant = new ReferenceParam();
		
		participant.setValue(PARTICIPANT_FAMILY_NAME);
		participant.setChain(Practitioner.SP_FAMILY);
		
		ReferenceParam badParticipant = new ReferenceParam();
		
		badParticipant.setValue(WRONG_FAMILY_NAME);
		badParticipant.setChain(Practitioner.SP_FAMILY);
		
		participantReference.addValue(new ReferenceOrListParam().add(participant).add(badParticipant));
		
		SearchParameterMap theParams = new SearchParameterMap()
		        .addParameter(FhirConstants.PARTICIPANT_REFERENCE_SEARCH_HANDLER, participantReference);
		
		IBundleProvider results = search(theParams);
		
		assertThat(results, notNullValue());
		List<MedicationRequest> resultList = get(results);
		
		assertThat(resultList, not(empty()));
		assertThat(resultList, hasSize(equalTo(11)));
	}
	
	@Test
	public void searchForMedicationRequests_shouldReturnEmptyListOfMedicationRequestsByMultipleParticipantFamilyNameAnd() {
		ReferenceAndListParam participantReference = new ReferenceAndListParam();
		ReferenceParam participant = new ReferenceParam();
		
		participant.setValue(PARTICIPANT_FAMILY_NAME);
		participant.setChain(Practitioner.SP_FAMILY);
		
		ReferenceParam badParticipant = new ReferenceParam();
		
		badParticipant.setValue(WRONG_FAMILY_NAME);
		badParticipant.setChain(Practitioner.SP_FAMILY);
		
		participantReference.addValue(new ReferenceOrListParam().add(participant))
		        .addAnd(new ReferenceOrListParam().add(badParticipant));
		
		SearchParameterMap theParams = new SearchParameterMap()
		        .addParameter(FhirConstants.PARTICIPANT_REFERENCE_SEARCH_HANDLER, participantReference);
		IBundleProvider results = search(theParams);
		
		List<MedicationRequest> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(results.size(), equalTo(0));
		assertThat(resultList, empty());
	}
	
	@Test
	public void searchForMedicationRequest_shouldReturnMedicationRequestByParticipantName() {
		ReferenceAndListParam participantReference = new ReferenceAndListParam()
		        .addAnd(new ReferenceOrListParam().add(new ReferenceParam()
		                .setValue(PATIENT_GIVEN_NAME + " " + PARTICIPANT_FAMILY_NAME).setChain(Practitioner.SP_NAME)));
		
		SearchParameterMap theParams = new SearchParameterMap();
		theParams.addParameter(FhirConstants.PARTICIPANT_REFERENCE_SEARCH_HANDLER, participantReference);
		
		IBundleProvider results = search(theParams);
		
		assertThat(results, notNullValue());
		List<MedicationRequest> resultList = get(results);
		
		assertThat(resultList, not(empty()));
		assertThat(resultList, hasSize(equalTo(11)));
	}
	
	@Test
	public void searchForMedicationRequests_shouldSearchForMedicationRequestsByMultipleParticipantNameOr() {
		ReferenceAndListParam participantReference = new ReferenceAndListParam();
		ReferenceParam participant = new ReferenceParam();
		
		participant.setValue(PATIENT_GIVEN_NAME + " " + PARTICIPANT_FAMILY_NAME);
		participant.setChain(Practitioner.SP_NAME);
		
		ReferenceParam badParticipant = new ReferenceParam();
		
		badParticipant.setValue(WRONG_NAME);
		badParticipant.setChain(Practitioner.SP_NAME);
		
		participantReference.addValue(new ReferenceOrListParam().add(participant).add(badParticipant));
		
		SearchParameterMap theParams = new SearchParameterMap()
		        .addParameter(FhirConstants.PARTICIPANT_REFERENCE_SEARCH_HANDLER, participantReference);
		IBundleProvider results = search(theParams);
		
		assertThat(results, notNullValue());
		List<MedicationRequest> resultList = get(results);
		
		assertThat(resultList, not(empty()));
		assertThat(resultList, hasSize(equalTo(11)));
	}
	
	@Test
	public void searchForMedicationRequests_shouldReturnEmptyListOfMedicationRequestsByMultipleParticipantNameAnd() {
		ReferenceAndListParam participantReference = new ReferenceAndListParam();
		ReferenceParam participant = new ReferenceParam();
		
		participant.setValue(PATIENT_GIVEN_NAME + " " + PARTICIPANT_FAMILY_NAME);
		participant.setChain(Practitioner.SP_NAME);
		
		ReferenceParam badParticipant = new ReferenceParam();
		
		badParticipant.setValue(WRONG_NAME);
		badParticipant.setChain(Practitioner.SP_NAME);
		
		participantReference.addValue(new ReferenceOrListParam().add(participant))
		        .addAnd(new ReferenceOrListParam().add(badParticipant));
		
		SearchParameterMap theParams = new SearchParameterMap()
		        .addParameter(FhirConstants.PARTICIPANT_REFERENCE_SEARCH_HANDLER, participantReference);
		IBundleProvider results = search(theParams);
		
		List<MedicationRequest> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(results.size(), equalTo(0));
		assertThat(resultList, empty());
	}
	
	@Test
	public void searchForMedicationRequest_shouldReturnMedicationRequestByParticipantIdentifier() {
		ReferenceAndListParam participantReference = new ReferenceAndListParam().addAnd(new ReferenceOrListParam()
		        .add(new ReferenceParam().setValue(PARTICIPANT_IDENTIFIER).setChain(Practitioner.SP_IDENTIFIER)));
		SearchParameterMap theParams = new SearchParameterMap();
		theParams.addParameter(FhirConstants.PARTICIPANT_REFERENCE_SEARCH_HANDLER, participantReference);
		
		IBundleProvider results = search(theParams);
		
		assertThat(results, notNullValue());
		List<MedicationRequest> resultList = get(results);
		
		assertThat(resultList, not(empty()));
		assertThat(resultList, hasSize(equalTo(11)));
	}
	
	@Test
	public void searchForMedicationRequests_shouldSearchForMedicationRequestsByMultipleParticipantIdentifierOr() {
		ReferenceAndListParam participantReference = new ReferenceAndListParam();
		ReferenceParam participant = new ReferenceParam();
		
		participant.setValue(PARTICIPANT_IDENTIFIER);
		participant.setChain(Practitioner.SP_IDENTIFIER);
		
		ReferenceParam badParticipant = new ReferenceParam();
		
		badParticipant.setValue(WRONG_IDENTIFIER);
		badParticipant.setChain(Practitioner.SP_IDENTIFIER);
		
		participantReference.addValue(new ReferenceOrListParam().add(participant).add(badParticipant));
		
		SearchParameterMap theParams = new SearchParameterMap()
		        .addParameter(FhirConstants.PARTICIPANT_REFERENCE_SEARCH_HANDLER, participantReference);
		IBundleProvider results = search(theParams);
		
		assertThat(results, notNullValue());
		List<MedicationRequest> resultList = get(results);
		
		assertThat(resultList, not(empty()));
		assertThat(resultList, hasSize(equalTo(11)));
		
		assertThat(resultList.get(0).getRequester().getIdentifier().getValue(), equalTo(PARTICIPANT_IDENTIFIER));
	}
	
	@Test
	public void searchForMedicationRequests_shouldReturnEmptyListOfMedicationRequestsByMultipleParticipantIdentifierAnd() {
		ReferenceAndListParam participantReference = new ReferenceAndListParam();
		ReferenceParam participant = new ReferenceParam();
		
		participant.setValue(PARTICIPANT_IDENTIFIER);
		participant.setChain(Practitioner.SP_IDENTIFIER);
		
		ReferenceParam badParticipant = new ReferenceParam();
		
		badParticipant.setValue(WRONG_IDENTIFIER);
		badParticipant.setChain(Practitioner.SP_IDENTIFIER);
		
		participantReference.addValue(new ReferenceOrListParam().add(participant))
		        .addAnd(new ReferenceOrListParam().add(badParticipant));
		
		SearchParameterMap theParams = new SearchParameterMap()
		        .addParameter(FhirConstants.PARTICIPANT_REFERENCE_SEARCH_HANDLER, participantReference);
		IBundleProvider results = search(theParams);
		
		List<MedicationRequest> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(results.size(), equalTo(0));
		assertThat(resultList, empty());
	}
	
	@Test
	public void searchForMedicationRequest_shouldReturnMedicationRequestByMedication() {
		ReferenceAndListParam medicationReference = new ReferenceAndListParam()
		        .addAnd(new ReferenceOrListParam().add(new ReferenceParam().setValue(MEDICATION_UUID)));
		
		SearchParameterMap theParams = new SearchParameterMap();
		theParams.addParameter(FhirConstants.MEDICATION_REFERENCE_SEARCH_HANDLER, medicationReference);
		
		IBundleProvider results = search(theParams);
		
		assertThat(results, notNullValue());
		
		List<MedicationRequest> resultList = get(results);
		
		assertThat(resultList, notNullValue());
		assertThat(resultList, hasItem(hasProperty("id", equalTo(MEDICATION_REQUEST_UUID))));
	}
	
	@Test
	public void searchForMedicationRequest_shouldSearchForMedicationRequestByConceptId() {
		TokenAndListParam code = new TokenAndListParam();
		TokenParam codingToken = new TokenParam();
		codingToken.setValue(MEDICATION_REQUEST_CONCEPT_ID);
		code.addAnd(codingToken);
		
		SearchParameterMap theParams = new SearchParameterMap();
		theParams.addParameter(FhirConstants.CODED_SEARCH_HANDLER, code);
		IBundleProvider results = search(theParams);
		
		assertThat(results, notNullValue());
		assertThat(results.size(), equalTo(1));
		
		List<MedicationRequest> resultList = get(results);
		
		assertThat(resultList, not(empty()));
		assertThat(resultList, hasItem(hasProperty("id", equalTo(MEDICATION_REQUEST_UUID))));
	}
	
	@Test
	public void searchForMedicationRequest_shouldSearchForMedicationRequestByConceptUuid() {
		TokenAndListParam code = new TokenAndListParam();
		TokenParam codingToken = new TokenParam();
		codingToken.setValue(MEDICATION_REQUEST_CONCEPT_UUID);
		code.addAnd(codingToken);
		SearchParameterMap theParams = new SearchParameterMap();
		theParams.addParameter(FhirConstants.CODED_SEARCH_HANDLER, code);
		
		IBundleProvider results = search(theParams);
		
		assertThat(results, notNullValue());
		assertThat(results.size(), equalTo(1));
		
		List<MedicationRequest> resultList = get(results);
		
		assertThat(resultList, not(empty()));
		assertThat(resultList, hasItem(hasProperty("id", equalTo(MEDICATION_REQUEST_UUID))));
	}
	
	@Test
	public void searchForMedicationRequest_shouldReturnMedicationRequestionByConceptMapping() {
		TokenAndListParam code = new TokenAndListParam();
		TokenParam codingToken = new TokenParam();
		codingToken.setSystem(FhirTestConstants.LOINC_SYSTEM_URL);
		codingToken.setValue(MEDICATION_REQUEST_LOINC_CODE);
		code.addAnd(codingToken);
		
		SearchParameterMap theParams = new SearchParameterMap();
		theParams.addParameter(FhirConstants.CODED_SEARCH_HANDLER, code);
		IBundleProvider results = search(theParams);
		
		assertThat(results, notNullValue());
		assertThat(results.size(), equalTo(1));
		
		List<MedicationRequest> resultList = get(results);
		
		assertThat(resultList, not(empty()));
		assertThat(resultList, hasItem(hasProperty("id", equalTo(MEDICATION_REQUEST_UUID))));
	}
	
	@Test
	public void searchForMedicationRequest_shouldReturnMultipleMedicationRequestByConceptMapping() {
		TokenAndListParam code = new TokenAndListParam();
		TokenOrListParam orListParam = new TokenOrListParam();
		code.addAnd(orListParam);
		
		for (String coding : LOINC_TB_DRUG_CODES) {
			TokenParam codingToken = new TokenParam();
			codingToken.setSystem(FhirTestConstants.LOINC_SYSTEM_URL);
			codingToken.setValue(coding);
			orListParam.addOr(codingToken);
		}
		
		SearchParameterMap theParams = new SearchParameterMap();
		theParams.addParameter(FhirConstants.CODED_SEARCH_HANDLER, code);
		IBundleProvider results = search(theParams);
		
		assertThat(results, notNullValue());
		assertThat(results.size(), equalTo(2));
		
		List<MedicationRequest> resultList = get(results);
		
		assertThat(resultList, notNullValue());
		assertThat(resultList, hasSize(equalTo(2)));
	}
	
	@Test
	public void searchForMedicationRequest_shouldReturnFromMultipleConceptMappings() {
		TokenAndListParam code = new TokenAndListParam().addAnd(
		    new TokenParam().setSystem(FhirTestConstants.LOINC_SYSTEM_URL).setValue(LOINC_BENDAQUILINE),
		    new TokenParam().setSystem(FhirTestConstants.CIEL_SYSTEM_URN).setValue(CIEL_RIFAMPICIN));
		
		SearchParameterMap theParams = new SearchParameterMap();
		theParams.addParameter(FhirConstants.CODED_SEARCH_HANDLER, code);
		
		IBundleProvider results = search(theParams);
		
		assertThat(results, notNullValue());
		assertThat(results.size(), equalTo(2));
		
		List<MedicationRequest> resultList = get(results);
		
		assertThat(resultList, notNullValue());
		assertThat(resultList, hasSize(equalTo(2)));
	}
	
	@Test
	public void searchForMedicationRequest_shouldSupportMappedAndUnmappedConcepts() {
		TokenAndListParam code = new TokenAndListParam().addAnd(
		    new TokenParam().setSystem(FhirTestConstants.LOINC_SYSTEM_URL).setValue(LOINC_BENDAQUILINE),
		    new TokenParam().setValue(RIFAMPICIN_CONCEPT_ID));
		
		SearchParameterMap theParams = new SearchParameterMap();
		theParams.addParameter(FhirConstants.CODED_SEARCH_HANDLER, code);
		
		IBundleProvider results = search(theParams);
		
		assertThat(results, notNullValue());
		assertThat(results.size(), equalTo(2));
		
		List<MedicationRequest> resultList = get(results);
		
		assertThat(resultList, notNullValue());
		assertThat(resultList, hasSize(equalTo(2)));
	}
	
	@Test
	public void searchForMedicationRequests_shouldSearchForMedicationRequestsByUuid() {
		TokenAndListParam uuid = new TokenAndListParam().addAnd(new TokenParam(MEDICATION_REQUEST_UUID));
		
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.COMMON_SEARCH_HANDLER,
		    FhirConstants.ID_PROPERTY, uuid);
		
		IBundleProvider results = search(theParams);
		
		assertThat(results, notNullValue());
		assertThat(results.size(), equalTo(1));
		
		List<MedicationRequest> resultList = get(results);
		
		assertThat(resultList, hasSize(equalTo(1)));
		assertThat(resultList.get(0).getIdElement().getIdPart(), equalTo(MEDICATION_REQUEST_UUID));
	}
	
	@Test
	public void searchForMedicationRequests_shouldSearchForMedicationRequestsByStatus() {
		TokenAndListParam status = new TokenAndListParam().addAnd(new TokenParam(STATUS));
		
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.STATUS_SEARCH_HANDLER,
		    MedicationRequest.SP_STATUS, status);
		
		IBundleProvider results = search(theParams);
		
		assertThat(results, notNullValue());
		List<MedicationRequest> resultList = get(results);
		assertThat(resultList, hasSize(equalTo(5)));
		assertThat(resultList, not(hasItem(hasProperty("id", equalTo(NON_ACTIVE_ORDER_UUID)))));
	}
	
	@Test
	public void searchForMedicationRequests_shouldSearchForMedicationRequestsByLastUpdatedDateCreated() {
		DateRangeParam lastUpdated = new DateRangeParam().setUpperBound(DATE_CREATED).setLowerBound(DATE_CREATED);
		
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.COMMON_SEARCH_HANDLER,
		    FhirConstants.LAST_UPDATED_PROPERTY, lastUpdated);
		
		IBundleProvider results = search(theParams);
		
		assertThat(results, notNullValue());
		assertThat(results.size(), equalTo(3));
		
		List<MedicationRequest> resultList = get(results);
		
		assertThat(resultList.size(), equalTo(3));
	}
	
	@Test
	public void searchForMedicationRequests_shouldSearchForMedicationRequestsByMatchingUuidAndLastUpdated() {
		TokenAndListParam uuid = new TokenAndListParam().addAnd(new TokenParam(MEDICATION_REQUEST_UUID));
		DateRangeParam lastUpdated = new DateRangeParam().setUpperBound(DATE_CREATED).setLowerBound(DATE_CREATED);
		
		SearchParameterMap theParams = new SearchParameterMap()
		        .addParameter(FhirConstants.COMMON_SEARCH_HANDLER, FhirConstants.ID_PROPERTY, uuid)
		        .addParameter(FhirConstants.COMMON_SEARCH_HANDLER, FhirConstants.LAST_UPDATED_PROPERTY, lastUpdated);
		
		IBundleProvider results = search(theParams);
		
		assertThat(results, notNullValue());
		assertThat(results.size(), equalTo(1));
		
		List<MedicationRequest> resultList = get(results);
		
		assertThat(resultList, hasSize(equalTo(1)));
		assertThat(resultList.get(0).getIdElement().getIdPart(), equalTo(MEDICATION_REQUEST_UUID));
	}
	
	@Test
	public void searchForMedicationRequests_shouldReturnEmptyListByMismatchingUuidAndLastUpdated() {
		TokenAndListParam uuid = new TokenAndListParam().addAnd(new TokenParam(MEDICATION_REQUEST_UUID));
		DateRangeParam lastUpdated = new DateRangeParam().setUpperBound(DATE_VOIDED).setLowerBound(DATE_VOIDED);
		
		SearchParameterMap theParams = new SearchParameterMap()
		        .addParameter(FhirConstants.COMMON_SEARCH_HANDLER, FhirConstants.ID_PROPERTY, uuid)
		        .addParameter(FhirConstants.COMMON_SEARCH_HANDLER, FhirConstants.LAST_UPDATED_PROPERTY, lastUpdated);
		
		IBundleProvider results = search(theParams);
		
		assertThat(results, notNullValue());
		assertThat(results.size(), equalTo(0));
		
		List<MedicationRequest> resultList = get(results);
		
		assertThat(resultList, empty());
	}
	
	@Test
	public void searchForMedicationRequests_shouldAddPractitionersForRequesterInclude() {
		TokenAndListParam uuid = new TokenAndListParam().addAnd(new TokenParam(MEDICATION_REQUEST_UUID));
		HashSet<Include> includes = new HashSet<>();
		includes.add(new Include("MedicationRequest:requester"));
		
		SearchParameterMap theParams = new SearchParameterMap()
		        .addParameter(FhirConstants.COMMON_SEARCH_HANDLER, FhirConstants.ID_PROPERTY, uuid)
		        .addParameter(FhirConstants.INCLUDE_SEARCH_HANDLER, includes);
		
		IBundleProvider results = search(theParams);
		assertThat(results.size(), equalTo(1));
		assertThat(results, notNullValue());
		
		List<IBaseResource> resultList = results.getResources(0, 10);
		
		assertThat(resultList, not(empty()));
		assertThat(resultList.size(), equalTo(2)); // included resource added as part of the result list
		assertThat(((MedicationRequest) resultList.iterator().next()).getIdElement().getIdPart(),
		    equalTo(MEDICATION_REQUEST_UUID));
		
		MedicationRequest returnedMedicationRequest = (MedicationRequest) resultList.iterator().next();
		assertThat(resultList, hasItem(allOf(is(instanceOf(Practitioner.class)),
		    hasProperty("id", equalTo(returnedMedicationRequest.getRequester().getReferenceElement().getIdPart())))));
	}
	
	@Test
	public void searchForMedicationRequests_shouldAddMedicationForMedicationInclude() {
		TokenAndListParam uuid = new TokenAndListParam().addAnd(new TokenParam(MEDICATION_REQUEST_UUID));
		HashSet<Include> includes = new HashSet<>();
		includes.add(new Include("MedicationRequest:medication"));
		
		SearchParameterMap theParams = new SearchParameterMap()
		        .addParameter(FhirConstants.COMMON_SEARCH_HANDLER, FhirConstants.ID_PROPERTY, uuid)
		        .addParameter(FhirConstants.INCLUDE_SEARCH_HANDLER, includes);
		
		IBundleProvider results = search(theParams);
		assertThat(results.size(), equalTo(1));
		assertThat(results, notNullValue());
		
		List<IBaseResource> resultList = results.getResources(0, 10);
		
		assertThat(resultList, not(empty()));
		assertThat(resultList.size(), equalTo(2)); // included resource added as part of the result list
		assertThat(((MedicationRequest) resultList.iterator().next()).getIdElement().getIdPart(),
		    equalTo(MEDICATION_REQUEST_UUID));
		
		MedicationRequest returnedMedicationRequest = (MedicationRequest) resultList.iterator().next();
		assertThat(resultList, hasItem(allOf(is(instanceOf(Medication.class)), hasProperty("id",
		    equalTo((returnedMedicationRequest.getMedicationReference().getReferenceElement().getIdPart()))))));
	}
	
	@Test
	public void searchForMedicationRequests_shouldAddPatientsForPatientInclude() {
		TokenAndListParam uuid = new TokenAndListParam().addAnd(new TokenParam(MEDICATION_REQUEST_UUID));
		HashSet<Include> includes = new HashSet<>();
		includes.add(new Include("MedicationRequest:patient"));
		
		SearchParameterMap theParams = new SearchParameterMap()
		        .addParameter(FhirConstants.COMMON_SEARCH_HANDLER, FhirConstants.ID_PROPERTY, uuid)
		        .addParameter(FhirConstants.INCLUDE_SEARCH_HANDLER, includes);
		
		IBundleProvider results = search(theParams);
		assertThat(results.size(), equalTo(1));
		assertThat(results, notNullValue());
		
		List<IBaseResource> resultList = results.getResources(0, 10);
		
		assertThat(resultList, not(empty()));
		assertThat(resultList.size(), equalTo(2)); // included resource added as part of the result list
		assertThat(((MedicationRequest) resultList.iterator().next()).getIdElement().getIdPart(),
		    equalTo(MEDICATION_REQUEST_UUID));
		
		MedicationRequest returnedMedicationRequest = (MedicationRequest) resultList.iterator().next();
		assertThat(resultList, hasItem(allOf(is(instanceOf(Patient.class)),
		    hasProperty("id", equalTo((returnedMedicationRequest.getSubject().getReferenceElement().getIdPart()))))));
	}
	
	@Test
	public void searchForMedicationRequests_shouldAddEncountersForEncounterInclude() {
		TokenAndListParam uuid = new TokenAndListParam().addAnd(new TokenParam(MEDICATION_REQUEST_UUID));
		HashSet<Include> includes = new HashSet<>();
		includes.add(new Include("MedicationRequest:encounter"));
		
		SearchParameterMap theParams = new SearchParameterMap()
		        .addParameter(FhirConstants.COMMON_SEARCH_HANDLER, FhirConstants.ID_PROPERTY, uuid)
		        .addParameter(FhirConstants.INCLUDE_SEARCH_HANDLER, includes);
		
		IBundleProvider results = search(theParams);
		assertThat(results.size(), equalTo(1));
		assertThat(results, notNullValue());
		
		List<IBaseResource> resultList = results.getResources(0, 10);
		
		assertThat(resultList, not(empty()));
		assertThat(resultList.size(), equalTo(2)); // included resource added as part of the result list
		assertThat(((MedicationRequest) resultList.iterator().next()).getIdElement().getIdPart(),
		    equalTo(MEDICATION_REQUEST_UUID));
		
		MedicationRequest returnedMedicationRequest = (MedicationRequest) resultList.iterator().next();
		assertThat(resultList, hasItem(allOf(is(instanceOf(Encounter.class)),
		    hasProperty("id", equalTo((returnedMedicationRequest.getEncounter().getReferenceElement().getIdPart()))))));
	}
	
	@Test
	public void searchForMedicationRequests_shouldAddEncountersForEncounterIncludeR3() {
		TokenAndListParam uuid = new TokenAndListParam().addAnd(new TokenParam(MEDICATION_REQUEST_UUID));
		HashSet<Include> includes = new HashSet<>();
		includes.add(new Include("MedicationRequest:context"));
		
		SearchParameterMap theParams = new SearchParameterMap()
		        .addParameter(FhirConstants.COMMON_SEARCH_HANDLER, FhirConstants.ID_PROPERTY, uuid)
		        .addParameter(FhirConstants.INCLUDE_SEARCH_HANDLER, includes);
		
		IBundleProvider results = search(theParams);
		assertThat(results.size(), equalTo(1));
		assertThat(results, notNullValue());
		
		List<IBaseResource> resultList = results.getResources(0, 10);
		
		assertThat(resultList, not(empty()));
		assertThat(resultList.size(), equalTo(2)); // included resource added as part of the result list
		assertThat(((MedicationRequest) resultList.iterator().next()).getIdElement().getIdPart(),
		    equalTo(MEDICATION_REQUEST_UUID));
		
		MedicationRequest returnedMedicationRequest = (MedicationRequest) resultList.iterator().next();
		assertThat(resultList, hasItem(allOf(is(instanceOf(Encounter.class)),
		    hasProperty("id", equalTo((returnedMedicationRequest.getEncounter().getReferenceElement().getIdPart()))))));
	}
	
	@Test
	public void searchForMedicationRequests_shouldHandleMultipleIncludes() {
		TokenAndListParam uuid = new TokenAndListParam().addAnd(new TokenParam(MEDICATION_REQUEST_UUID));
		HashSet<Include> includes = new HashSet<>();
		includes.add(new Include("MedicationRequest:medication"));
		includes.add(new Include("MedicationRequest:requester"));
		
		SearchParameterMap theParams = new SearchParameterMap()
		        .addParameter(FhirConstants.COMMON_SEARCH_HANDLER, FhirConstants.ID_PROPERTY, uuid)
		        .addParameter(FhirConstants.INCLUDE_SEARCH_HANDLER, includes);
		
		IBundleProvider results = search(theParams);
		assertThat(results.size(), equalTo(1));
		assertThat(results, notNullValue());
		
		List<IBaseResource> resultList = results.getResources(0, 10);
		
		assertThat(resultList, not(empty()));
		assertThat(resultList.size(), equalTo(3)); // included resources (medication + requester) added as part of the result list
		assertThat(((MedicationRequest) resultList.iterator().next()).getIdElement().getIdPart(),
		    equalTo(MEDICATION_REQUEST_UUID));
		
		MedicationRequest returnedMedicationRequest = (MedicationRequest) resultList.iterator().next();
		assertThat(resultList, hasItem(allOf(is(instanceOf(Medication.class)), hasProperty("id",
		    equalTo((returnedMedicationRequest.getMedicationReference().getReferenceElement().getIdPart()))))));
		assertThat(resultList, hasItem(allOf(is(instanceOf(Practitioner.class)),
		    hasProperty("id", equalTo(returnedMedicationRequest.getRequester().getReferenceElement().getIdPart())))));
	}
	
	private IBundleProvider search(SearchParameterMap theParams) {
		return searchQuery.getQueryResults(theParams, dao, translator, searchQueryInclude);
	}
	
	private List<MedicationRequest> get(IBundleProvider results) {
		return results.getAllResources().stream().filter(it -> it instanceof MedicationRequest)
		        .map(it -> (MedicationRequest) it).collect(Collectors.toList());
	}
	
	@Test
	public void searchForMedicationRequests_shouldSearchForMedicationRequestsByFulfillerStatus() {
		executeDataSet(DATA_XMLS);
		TokenAndListParam fulfillerStatus = new TokenAndListParam().addAnd(new TokenParam("received"));
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.FULFILLER_STATUS_SEARCH_HANDLER,
		    "fulfillerStatus", fulfillerStatus);
		IBundleProvider results = search(theParams);
		
		assertThat(results, notNullValue());
		List<MedicationRequest> resultList = get(results);
		assertThat(resultList, hasSize(equalTo(4)));
		
		// there is one tests in the test data set with "completed" fulfiller status
		fulfillerStatus = new TokenAndListParam().addAnd(new TokenParam("completed"));
		theParams = new SearchParameterMap().addParameter(FhirConstants.FULFILLER_STATUS_SEARCH_HANDLER, "fulfillerStatus",
		    fulfillerStatus);
		results = search(theParams);
		
		assertThat(results, notNullValue());
		resultList = get(results);
		assertThat(resultList, hasSize(equalTo(1)));
		assertThat(resultList.get(0).getId(), equalTo(DRUG_ORDER_WITH_FULFILLER_STATUS_COMPLETED_UUID));
	}
	
}
