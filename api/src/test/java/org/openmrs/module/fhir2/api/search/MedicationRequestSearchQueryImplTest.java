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
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;

import java.util.List;

import ca.uhn.fhir.rest.api.server.IBundleProvider;
import ca.uhn.fhir.rest.param.ReferenceAndListParam;
import ca.uhn.fhir.rest.param.ReferenceOrListParam;
import ca.uhn.fhir.rest.param.ReferenceParam;
import ca.uhn.fhir.rest.param.TokenAndListParam;
import ca.uhn.fhir.rest.param.TokenOrListParam;
import ca.uhn.fhir.rest.param.TokenParam;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.MedicationRequest;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.Practitioner;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.DrugOrder;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.FhirTestConstants;
import org.openmrs.module.fhir2.TestFhirSpringConfiguration;
import org.openmrs.module.fhir2.api.dao.FhirMedicationRequestDao;
import org.openmrs.module.fhir2.api.search.param.SearchParameterMap;
import org.openmrs.module.fhir2.api.translators.MedicationRequestTranslator;
import org.openmrs.test.BaseModuleContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

@ContextConfiguration(classes = TestFhirSpringConfiguration.class, inheritLocations = false)
public class MedicationRequestSearchQueryImplTest extends BaseModuleContextSensitiveTest {
	
	private static final String MEDICATION_REQUEST_DATA_XML = "org/openmrs/module/fhir2/api/dao/impl/FhirMedicationRequestDaoImpl_initial_data.xml";
	
	private static final String MEDICATION_REQUEST_UUID = "6d0ae116-707a-4629-9850-f15206e63ab0";
	
	private static final String PATIENT_UUID = "86526ed5-3c11-11de-a0ba-001e3766667a";
	
	private static final String PATIENT_GIVEN_NAME = "Moody";
	
	private static final String PATIENT_FAMILY_NAME = "Oregon";
	
	private static final String PATIENT_IDENTIFIER = "MO-2";
	
	private static final String ENCOUNTER_UUID = "bb0af6222-707a-9029-9859-f15206e63ab1";
	
	private static final String PARTICIPANT_UUID = "c2299800-cca9-11e0-9572-0800200c9a66";
	
	private static final String PARTICIPANT_GIVEN_NAME = "Super";
	
	private static final String PARTICIPANT_FAMILY_NAME = "User";
	
	private static final String PARTICIPANT_IDENTIFIER = "Test";
	
	private static final String MEDICATION_UUID = "42f00b94-26fe-102b-80cb-0017a47871b2";
	
	private static final String MEDICATION_REQUEST_CONCEPT_ID = "4020";
	
	private static final String MEDICATION_REQUEST_CONCEPT_UUID = "d102c80f-1yz9-4da3-bb88-8122ce889090";
	
	private static final String MEDICATION_REQUEST_LOINC_CODE = "2343253";
	
	private static final String[] LOINC_TB_DRUG_CODES = new String[] { "2343253", "2343256" };
	
	private static final String CIEL_RIFAMPICIN = "2343257";
	
	private static final String LOINC_BENDAQUILINE = "2343253";
	
	private static final String RIFAMPICIN_CONCEPT_ID = "4022";
	
	@Autowired
	private MedicationRequestTranslator translator;
	
	@Autowired
	private FhirMedicationRequestDao dao;
	
	@Autowired
	private SearchQuery<DrugOrder, MedicationRequest, FhirMedicationRequestDao, MedicationRequestTranslator> searchQuery;
	
	@Before
	public void setup() throws Exception {
		executeDataSet(MEDICATION_REQUEST_DATA_XML);
	}
	
	@Test
	public void searchForMedicationRequest_shouldReturnMedicationRequestByPatientUuid() {
		ReferenceAndListParam patientReference = new ReferenceAndListParam()
		        .addAnd(new ReferenceOrListParam().add(new ReferenceParam().setValue(PATIENT_UUID)));
		
		SearchParameterMap theParams = new SearchParameterMap();
		theParams.addParameter(FhirConstants.PATIENT_REFERENCE_SEARCH_HANDLER, patientReference);
		
		IBundleProvider results = search(theParams);
		
		assertThat(results, notNullValue());
		
		List<IBaseResource> resources = get(results);
		
		assertThat(resources, not(empty()));
		assertThat(resources, hasItem(hasProperty("id", equalTo(MEDICATION_REQUEST_UUID))));
	}
	
	@Test
	public void searchForMedicationRequest_shouldReturnMedicationRequestByPatientGivenName() {
		ReferenceAndListParam patientReference = new ReferenceAndListParam().addAnd(
		    new ReferenceOrListParam().add(new ReferenceParam().setValue(PATIENT_GIVEN_NAME).setChain(Patient.SP_GIVEN)));
		
		SearchParameterMap theParams = new SearchParameterMap();
		theParams.addParameter(FhirConstants.PATIENT_REFERENCE_SEARCH_HANDLER, patientReference);
		
		IBundleProvider results = search(theParams);
		
		assertThat(results, notNullValue());
		
		List<IBaseResource> resources = get(results);
		
		assertThat(resources, not(empty()));
		assertThat(resources, hasItem(hasProperty("id", equalTo(MEDICATION_REQUEST_UUID))));
	}
	
	@Test
	public void searchForMedicationRequest_shouldReturnMedicationRequestByPatientFamilyName() {
		ReferenceAndListParam patientReference = new ReferenceAndListParam().addAnd(
		    new ReferenceOrListParam().add(new ReferenceParam().setValue(PATIENT_FAMILY_NAME).setChain(Patient.SP_FAMILY)));
		
		SearchParameterMap theParams = new SearchParameterMap();
		theParams.addParameter(FhirConstants.PATIENT_REFERENCE_SEARCH_HANDLER, patientReference);
		
		IBundleProvider results = search(theParams);
		
		assertThat(results, notNullValue());
		
		List<IBaseResource> resources = get(results);
		
		assertThat(resources, not(empty()));
		assertThat(resources, hasItem(hasProperty("id", equalTo(MEDICATION_REQUEST_UUID))));
	}
	
	@Test
	public void searchForMedicationRequest_shouldReturnMedicationRequestByPatientName() {
		ReferenceAndListParam patientReference = new ReferenceAndListParam().addAnd(new ReferenceOrListParam().add(
		    new ReferenceParam().setValue(PATIENT_GIVEN_NAME + " " + PATIENT_FAMILY_NAME).setChain(Patient.SP_NAME)));
		
		SearchParameterMap theParams = new SearchParameterMap();
		theParams.addParameter(FhirConstants.PATIENT_REFERENCE_SEARCH_HANDLER, patientReference);
		
		IBundleProvider results = search(theParams);
		
		assertThat(results, notNullValue());
		
		List<IBaseResource> resources = get(results);
		
		assertThat(resources, not(empty()));
		assertThat(resources, hasItem(hasProperty("id", equalTo(MEDICATION_REQUEST_UUID))));
	}
	
	@Test
	public void searchForMedicationRequest_shouldReturnMedicationRequestByPatientIdentifier() {
		ReferenceAndListParam patientReference = new ReferenceAndListParam().addAnd(new ReferenceOrListParam()
		        .add(new ReferenceParam().setValue(PATIENT_IDENTIFIER).setChain(Patient.SP_IDENTIFIER)));
		SearchParameterMap theParams = new SearchParameterMap();
		theParams.addParameter(FhirConstants.PATIENT_REFERENCE_SEARCH_HANDLER, patientReference);
		
		IBundleProvider results = search(theParams);
		
		assertThat(results, notNullValue());
		
		List<IBaseResource> resources = get(results);
		
		assertThat(resources, not(empty()));
		assertThat(resources, hasItem(hasProperty("id", equalTo(MEDICATION_REQUEST_UUID))));		
	}
	
	@Test
	public void searchForMedicationRequest_shouldReturnMedicationRequestByEncounter() {
		ReferenceAndListParam encounterReference = new ReferenceAndListParam()
		        .addAnd(new ReferenceOrListParam().add(new ReferenceParam().setValue(ENCOUNTER_UUID)));
		
		SearchParameterMap theParams = new SearchParameterMap();
		theParams.addParameter(FhirConstants.ENCOUNTER_REFERENCE_SEARCH_HANDLER, encounterReference);
		
		IBundleProvider results = search(theParams);
		
		assertThat(results, notNullValue());
		
		List<IBaseResource> resources = get(results);
		
		assertThat(resources, notNullValue());
		assertThat(resources, hasItem(hasProperty("id", equalTo(MEDICATION_REQUEST_UUID))));
	}
	
	@Test
	public void searchForMedicationRequest_shouldReturnMedicationRequestByParticipantUuid() {
		ReferenceAndListParam participantReference = new ReferenceAndListParam()
		        .addAnd(new ReferenceOrListParam().add(new ReferenceParam().setValue(PARTICIPANT_UUID)));
		
		SearchParameterMap theParams = new SearchParameterMap();
		theParams.addParameter(FhirConstants.PARTICIPANT_REFERENCE_SEARCH_HANDLER, participantReference);
		
		IBundleProvider results = search(theParams);
		
		assertThat(results, notNullValue());
		assertThat(results.size(), equalTo(13));
		
		List<IBaseResource> resources = get(results);
		
		assertThat(resources, notNullValue());
		assertThat(resources, hasSize(equalTo(10)));
	}
	
	@Test
	public void searchForMedicationRequest_shouldReturnMedicationRequestByParticipantGivenName() {
		ReferenceAndListParam participantReference = new ReferenceAndListParam().addAnd(new ReferenceOrListParam()
		        .add(new ReferenceParam().setValue(PARTICIPANT_GIVEN_NAME).setChain(Practitioner.SP_GIVEN)));
		
		SearchParameterMap theParams = new SearchParameterMap();
		theParams.addParameter(FhirConstants.PARTICIPANT_REFERENCE_SEARCH_HANDLER, participantReference);
		
		IBundleProvider results = search(theParams);
		
		assertThat(results, notNullValue());
		assertThat(results.size(), equalTo(13));
		
		List<IBaseResource> resources = get(results);
		
		assertThat(resources, notNullValue());
		assertThat(resources, hasSize(equalTo(10)));	
	}
	
	@Test
	public void searchForMedicationRequest_shouldReturnMedicationRequestByParticipantFamilyName() {
		ReferenceAndListParam participantReference = new ReferenceAndListParam().addAnd(new ReferenceOrListParam()
		        .add(new ReferenceParam().setValue(PARTICIPANT_FAMILY_NAME).setChain(Practitioner.SP_FAMILY)));
		
		SearchParameterMap theParams = new SearchParameterMap();
		theParams.addParameter(FhirConstants.PARTICIPANT_REFERENCE_SEARCH_HANDLER, participantReference);
		
		IBundleProvider results = search(theParams);
		
		assertThat(results, notNullValue());
		assertThat(results.size(), equalTo(13));
		
		List<IBaseResource> resources = get(results);
		
		assertThat(resources, notNullValue());
		assertThat(resources, hasSize(equalTo(10)));		
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
		assertThat(results.size(), equalTo(13));
		
		List<IBaseResource> resources = get(results);
		
		assertThat(resources, notNullValue());
		assertThat(resources, hasSize(equalTo(10)));		
	}
	
	@Test
	public void searchForMedicationRequest_shouldReturnMedicationRequestByParticipantIdentifier() {
		ReferenceAndListParam participantReference = new ReferenceAndListParam().addAnd(new ReferenceOrListParam()
		        .add(new ReferenceParam().setValue(PARTICIPANT_IDENTIFIER).setChain(Practitioner.SP_IDENTIFIER)));
		SearchParameterMap theParams = new SearchParameterMap();
		theParams.addParameter(FhirConstants.PARTICIPANT_REFERENCE_SEARCH_HANDLER, participantReference);
		
		IBundleProvider results = search(theParams);
		
		assertThat(results, notNullValue());
		assertThat(results.size(), equalTo(13));
		
		List<IBaseResource> resources = get(results);
		
		assertThat(resources, notNullValue());
		assertThat(resources, hasSize(equalTo(10)));	
	}
	
	@Test
	public void searchForMedicationRequest_shouldReturnMedicationRequestByMedication() {
		ReferenceAndListParam medicationReference = new ReferenceAndListParam()
		        .addAnd(new ReferenceOrListParam().add(new ReferenceParam().setValue(MEDICATION_UUID)));
		
		SearchParameterMap theParams = new SearchParameterMap();
		theParams.addParameter(FhirConstants.MEDICATION_REFERENCE_SEARCH_HANDLER, medicationReference);
		
		IBundleProvider results = search(theParams);
		
		assertThat(results, notNullValue());
		
		List<IBaseResource> resources = get(results);
		
		assertThat(resources, notNullValue());
		assertThat(resources, hasItem(hasProperty("id", equalTo(MEDICATION_REQUEST_UUID))));
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
		
		assertThat(get(results), not(empty()));
		assertThat(get(results), hasItem(hasProperty("id", equalTo(MEDICATION_REQUEST_UUID))));
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
		
		assertThat(get(results), not(empty()));
		assertThat(get(results), hasSize(greaterThan(0)));
		assertThat(get(results), hasItem(hasProperty("id", equalTo(MEDICATION_REQUEST_UUID))));
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
		
		assertThat(get(results), not(empty()));
		assertThat(get(results), hasSize(greaterThan(0)));
		assertThat(get(results), hasItem(hasProperty("id", equalTo(MEDICATION_REQUEST_UUID))));
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
		
		List<IBaseResource> resources = get(results);
		
		assertThat(resources, notNullValue());
		assertThat(resources, hasSize(equalTo(2)));
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
		
		List<IBaseResource> resources = get(results);
		
		assertThat(resources, notNullValue());
		assertThat(resources, hasSize(equalTo(2)));
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
		
		List<IBaseResource> resources = get(results);
		
		assertThat(resources, notNullValue());
		assertThat(results.size(), equalTo(2));
	}
	
	private IBundleProvider search(SearchParameterMap theParams) {
		return searchQuery.getQueryResults(theParams, dao, translator);
	}
	
	private List<IBaseResource> get(IBundleProvider results) {
		return results.getResources(0, 10);
	}
	
}
