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
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;

import java.util.List;
import java.util.stream.Collectors;

import ca.uhn.fhir.rest.api.SortOrderEnum;
import ca.uhn.fhir.rest.api.SortSpec;
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
import ca.uhn.fhir.rest.param.StringAndListParam;
import ca.uhn.fhir.rest.param.StringParam;
import ca.uhn.fhir.rest.param.TokenAndListParam;
import ca.uhn.fhir.rest.param.TokenOrListParam;
import ca.uhn.fhir.rest.param.TokenParam;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.instance.model.api.IIdType;
import org.hl7.fhir.r4.model.Observation;
import org.hl7.fhir.r4.model.Patient;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.Obs;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.FhirTestConstants;
import org.openmrs.module.fhir2.TestFhirSpringConfiguration;
import org.openmrs.module.fhir2.api.dao.FhirObservationDao;
import org.openmrs.module.fhir2.api.search.param.SearchParameterMap;
import org.openmrs.module.fhir2.api.translators.ObservationTranslator;
import org.openmrs.test.BaseModuleContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

@ContextConfiguration(classes = TestFhirSpringConfiguration.class, inheritLocations = false)
public class ObservationSearchQueryImplTest extends BaseModuleContextSensitiveTest {
	
	private static final String OBS_DATA_XML = "org/openmrs/module/fhir2/api/dao/impl/FhirObservationDaoImplTest_initial_data_suppl.xml";
	
	private static final String OBS_UUID = "39fb7f47-e80a-4056-9285-bd798be13c63";
	
	private static final String OBS_GROUP_UUID = "4efa62d2-6b8b-4803-a8fa-3f32ee54db4f";
	
	private static final String OBS_CONCEPT_ID = "5089";
	
	private static final String OBS_CONCEPT_UUID = "c607c80f-1ea9-4da3-bb88-6276ce8868dd";
	
	private static final String VALUE_CONCEPT_ID = "5242";
	
	private static final String OBS_VALUE_CONCEPT_UUID = "785li1f8-bdbc-4950-833b-002244e9fa2b";
	
	private static final String VALUE_DATE = "1976-08-25";
	
	private static final String VALUE_STRING = "AFH56";
	
	private static final String VALUE_DATE_AND_TIME = "1976-08-25T13:44:57.0";
	
	private static final String PATIENT_UUID = "5946f880-b197-400b-9caa-a3c661d23041";
	
	private static final String PATIENT_WRONG_UUID = "c2299800-cca9-11e0-9572-abcdef0c9a66";
	
	private static final String PATIENT_GIVEN_NAME = "Collet";
	
	private static final String PATIENT_WRONG_GIVEN_NAME = "Wrong given name";
	
	private static final String PATIENT_FAMILY_NAME = "Chebaskwony";
	
	private static final String PATIENT_WRONG_FAMILY_NAME = "Wrong family name";
	
	private static final String PATIENT_IDENTIFIER = "6TS-4";
	
	private static final String PATIENT_WRONG_IDENTIFIER = "Wrong identifier";
	
	private static final String ENCOUNTER_UUID = "6519d653-393b-4118-9c83-a3715b82d4ac";
	
	private static final String ENCOUNTER_UUID_TWO = "6519d653-393b-4118-9c83-a3715b82d4ac";
	
	private static final String MEMBER_UUID = "744b91f8-bdbc-4950-833b-002244e9fa2b";
	
	private static final String SNOMED_SYSTEM_URI = "http://snomed.info/sct";
	
	private static final String OBS_SNOMED_CODE = "2332523";
	
	private static final String[] CIEL_VITAL_CODES = new String[] { "5085", "5086", "5087", "5088", "5089", "5090", "5092",
	        "5242" };
	
	private static final String CIEL_DIASTOLIC_BP = "5086";
	
	private static final String LOINC_SYSTOLIC_BP = "8480-6";
	
	private static final int START_INDEX = 0;
	
	private static final int END_INDEX = 10;
	
	@Autowired
	private ObservationTranslator translator;
	
	@Autowired
	private FhirObservationDao dao;
	
	@Autowired
	private SearchQuery<Obs, Observation, FhirObservationDao, ObservationTranslator> searchQuery;
	
	@Before
	public void setup() throws Exception {
		executeDataSet(OBS_DATA_XML);
	}
	
	@Test
	public void searchForObs_shouldSearchForObsByConceptId() {
		TokenAndListParam code = new TokenAndListParam();
		TokenParam codingToken = new TokenParam();
		codingToken.setValue(OBS_CONCEPT_ID);
		code.addAnd(codingToken);
		
		SearchParameterMap theParams = new SearchParameterMap();
		theParams.addParameter(FhirConstants.CODED_SEARCH_HANDLER, code);
		IBundleProvider results = search(theParams);
		
		assertThat(results, notNullValue());
		assertThat(results.size(), equalTo(3));
		
		assertThat(get(results), not(empty()));
		assertThat(get(results), hasItem(hasProperty("id", equalTo(OBS_UUID))));
	}
	
	@Test
	public void searchForObs_shouldSearchForObsByConceptUuid() {
		TokenAndListParam code = new TokenAndListParam();
		TokenParam codingToken = new TokenParam();
		codingToken.setValue(OBS_CONCEPT_UUID);
		code.addAnd(codingToken);
		SearchParameterMap theParams = new SearchParameterMap();
		theParams.addParameter(FhirConstants.CODED_SEARCH_HANDLER, code);
		
		IBundleProvider results = search(theParams);
		
		assertThat(results, notNullValue());
		assertThat(results.size(), equalTo(3));
		
		assertThat(get(results), not(empty()));
		assertThat(get(results), hasSize(greaterThan(1)));
		assertThat(get(results), hasItem(hasProperty("id", equalTo(OBS_UUID))));
	}
	
	@Test
	public void searchForObs_shouldReturnObsByConceptMapping() {
		TokenAndListParam code = new TokenAndListParam();
		TokenParam codingToken = new TokenParam();
		codingToken.setSystem(SNOMED_SYSTEM_URI);
		codingToken.setValue(OBS_SNOMED_CODE);
		code.addAnd(codingToken);
		
		SearchParameterMap theParams = new SearchParameterMap();
		theParams.addParameter(FhirConstants.CODED_SEARCH_HANDLER, code);
		IBundleProvider results = search(theParams);
		
		assertThat(results, notNullValue());
		assertThat(results.size(), equalTo(3));
		
		assertThat(get(results), not(empty()));
		assertThat(get(results), hasSize(greaterThan(1)));
		assertThat(get(results), hasItem(hasProperty("id", equalTo(OBS_UUID))));
	}
	
	@Test
	public void searchForObs_shouldReturnMultipleObsByConceptMapping() {
		TokenAndListParam code = new TokenAndListParam();
		TokenOrListParam orListParam = new TokenOrListParam();
		code.addAnd(orListParam);
		
		for (String coding : CIEL_VITAL_CODES) {
			TokenParam codingToken = new TokenParam();
			codingToken.setSystem(FhirTestConstants.CIEL_SYSTEM_URN);
			codingToken.setValue(coding);
			orListParam.addOr(codingToken);
		}
		
		SearchParameterMap theParams = new SearchParameterMap();
		theParams.addParameter(FhirConstants.CODED_SEARCH_HANDLER, code);
		IBundleProvider results = search(theParams);
		
		assertThat(results, notNullValue());
		assertThat(results.size(), equalTo(15));
		
		List<IBaseResource> resources = get(results);
		
		assertThat(resources, notNullValue());
		assertThat(resources, hasSize(equalTo(10)));
	}
	
	@Test
	public void searchForObs_shouldReturnFromMultipleConceptMappings() {
		TokenAndListParam code = new TokenAndListParam().addAnd(
		    new TokenParam().setSystem(FhirTestConstants.LOINC_SYSTEM_URL).setValue(LOINC_SYSTOLIC_BP),
		    new TokenParam().setSystem(FhirTestConstants.CIEL_SYSTEM_URN).setValue(CIEL_DIASTOLIC_BP));
		
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
	public void searchForObs_shouldSupportMappedAndUnmappedConcepts() {
		TokenAndListParam code = new TokenAndListParam().addAnd(
		    new TokenParam().setSystem(FhirTestConstants.LOINC_SYSTEM_URL).setValue(LOINC_SYSTOLIC_BP),
		    new TokenParam().setValue(CIEL_DIASTOLIC_BP));
		
		SearchParameterMap theParams = new SearchParameterMap();
		theParams.addParameter(FhirConstants.CODED_SEARCH_HANDLER, code);
		
		IBundleProvider results = search(theParams);
		
		assertThat(results, notNullValue());
		assertThat(results.size(), greaterThan(1));
		
		List<IBaseResource> resources = get(results);
		
		assertThat(resources, notNullValue());
		assertThat(resources, hasSize(greaterThan(1)));
	}
	
	@Test
	public void searchForObs_shouldReturnObsByPatientUuid() {
		ReferenceAndListParam patientReference = new ReferenceAndListParam()
		        .addAnd(new ReferenceOrListParam().add(new ReferenceParam().setValue(PATIENT_UUID)));
		
		SearchParameterMap theParams = new SearchParameterMap();
		theParams.addParameter(FhirConstants.PATIENT_REFERENCE_SEARCH_HANDLER, patientReference);
		
		IBundleProvider results = search(theParams);
		
		assertThat(results, notNullValue());
		
		List<IBaseResource> resources = get(results);
		
		assertThat(resources, not(empty()));
		assertThat(resources, hasItem(hasProperty("id", equalTo(OBS_UUID))));
	}
	
	@Test
	public void searchForObs_shouldSearchForObsByMultiplePatientUuidOr() {
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
		assertThat(resultList.size(), greaterThanOrEqualTo(1));
	}
	
	@Test
	public void searchForObs_shouldReturnEmptyListOfObsByMultiplePatientUuidAnd() {
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
	public void searchForObs_shouldReturnObsByPatientGivenName() {
		ReferenceAndListParam patientReference = new ReferenceAndListParam().addAnd(
		    new ReferenceOrListParam().add(new ReferenceParam().setValue(PATIENT_GIVEN_NAME).setChain(Patient.SP_GIVEN)));
		
		SearchParameterMap theParams = new SearchParameterMap();
		theParams.addParameter(FhirConstants.PATIENT_REFERENCE_SEARCH_HANDLER, patientReference);
		
		IBundleProvider results = search(theParams);
		
		assertThat(results, notNullValue());
		
		List<IBaseResource> resources = get(results);
		
		assertThat(resources, not(empty()));
		assertThat(resources, hasItem(hasProperty("id", equalTo(OBS_UUID))));
	}
	
	@Test
	public void searchForObs_shouldSearchForObsByMultiplePatientGivenNameOr() {
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
		assertThat(resultList.size(), greaterThanOrEqualTo(1));
	}
	
	@Test
	public void searchForObs_shouldReturnEmptyListOfObsByMultiplePatientGivenNameAnd() {
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
	public void searchForObs_shouldReturnObsByPatientFamilyName() {
		ReferenceAndListParam patientReference = new ReferenceAndListParam().addAnd(
		    new ReferenceOrListParam().add(new ReferenceParam().setValue(PATIENT_FAMILY_NAME).setChain(Patient.SP_FAMILY)));
		
		SearchParameterMap theParams = new SearchParameterMap();
		theParams.addParameter(FhirConstants.PATIENT_REFERENCE_SEARCH_HANDLER, patientReference);
		
		IBundleProvider results = search(theParams);
		
		assertThat(results, notNullValue());
		
		List<IBaseResource> resources = get(results);
		
		assertThat(resources, not(empty()));
		assertThat(resources, hasItem(hasProperty("id", equalTo(OBS_UUID))));
	}
	
	@Test
	public void searchForObs_shouldSearchForObsByMultiplePatientFamilyNameOr() {
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
		assertThat(resultList.size(), greaterThanOrEqualTo(1));
	}
	
	@Test
	public void searchForObs_shouldReturnEmptyListOfObsByMultiplePatientFamilyNameAnd() {
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
	public void searchForObs_shouldReturnObsByPatientName() {
		ReferenceAndListParam patientReference = new ReferenceAndListParam().addAnd(new ReferenceOrListParam().add(
		    new ReferenceParam().setValue(PATIENT_GIVEN_NAME + " " + PATIENT_FAMILY_NAME).setChain(Patient.SP_NAME)));
		
		SearchParameterMap theParams = new SearchParameterMap();
		theParams.addParameter(FhirConstants.PATIENT_REFERENCE_SEARCH_HANDLER, patientReference);
		
		IBundleProvider results = search(theParams);
		
		assertThat(results, notNullValue());
		
		List<IBaseResource> resources = get(results);
		
		assertThat(resources, not(empty()));
		assertThat(resources, hasItem(hasProperty("id", equalTo(OBS_UUID))));
	}
	
	@Test
	public void searchForObs_shouldSearchForObsByMultiplePatientNameOr() {
		ReferenceAndListParam referenceParam = new ReferenceAndListParam();
		ReferenceParam patient = new ReferenceParam();
		
		patient.setValue(PATIENT_GIVEN_NAME + " " + PATIENT_FAMILY_NAME);
		patient.setChain(Patient.SP_NAME);
		
		ReferenceParam badPatient = new ReferenceParam();
		
		badPatient.setValue(PATIENT_WRONG_GIVEN_NAME + " " + PATIENT_WRONG_FAMILY_NAME);
		badPatient.setChain(Patient.SP_NAME);
		
		referenceParam.addValue(new ReferenceOrListParam().add(patient).add(badPatient));
		
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.PATIENT_REFERENCE_SEARCH_HANDLER,
		    referenceParam);
		IBundleProvider results = search(theParams);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList, not(empty()));
		assertThat(resultList.size(), greaterThanOrEqualTo(1));
	}
	
	@Test
	public void searchForObs_shouldReturnEmptyListOfObsByMultiplePatientNameAnd() {
		ReferenceAndListParam referenceParam = new ReferenceAndListParam();
		ReferenceParam patient = new ReferenceParam();
		
		patient.setValue(PATIENT_GIVEN_NAME + " " + PATIENT_FAMILY_NAME);
		patient.setChain(Patient.SP_NAME);
		
		ReferenceParam badPatient = new ReferenceParam();
		
		badPatient.setValue(PATIENT_WRONG_GIVEN_NAME + " " + PATIENT_WRONG_FAMILY_NAME);
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
	public void searchForObs_shouldReturnObsByPatientIdentifier() {
		ReferenceAndListParam patientReference = new ReferenceAndListParam().addAnd(new ReferenceOrListParam()
		        .add(new ReferenceParam().setValue(PATIENT_IDENTIFIER).setChain(Patient.SP_IDENTIFIER)));
		SearchParameterMap theParams = new SearchParameterMap();
		theParams.addParameter(FhirConstants.PATIENT_REFERENCE_SEARCH_HANDLER, patientReference);
		
		IBundleProvider results = search(theParams);
		
		assertThat(results, notNullValue());
		
		List<IBaseResource> resources = get(results);
		
		assertThat(resources, not(empty()));
		assertThat(resources, hasItem(hasProperty("id", equalTo(OBS_UUID))));
		
		// obs.getSubject().getIdentifier().getValue() == PATIENT_IDENTIFIER
		assertThat(resources,
		    everyItem(hasProperty("subject", hasProperty("identifier", hasProperty("value", equalTo(PATIENT_IDENTIFIER))))));
	}
	
	@Test
	public void searchForObs_shouldSearchForObsByMultiplePatientIdentifierOr() {
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
		assertThat(resultList.size(), greaterThanOrEqualTo(1));
		assertThat(resultList,
		    everyItem(hasProperty("subject", hasProperty("identifier", hasProperty("value", equalTo(PATIENT_IDENTIFIER))))));
	}
	
	@Test
	public void searchForObs_shouldReturnEmptyListOfObsByMultiplePatientIdentifierAnd() {
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
	public void searchForObs_shouldReturnObsByEncounter() {
		ReferenceAndListParam encounterReference = new ReferenceAndListParam()
		        .addAnd(new ReferenceOrListParam().add(new ReferenceParam().setValue(ENCOUNTER_UUID)));
		
		SearchParameterMap theParams = new SearchParameterMap();
		theParams.addParameter(FhirConstants.ENCOUNTER_REFERENCE_SEARCH_HANDLER, encounterReference);
		
		IBundleProvider results = search(theParams);
		
		assertThat(results, notNullValue());
		
		List<IBaseResource> resources = get(results);
		
		assertThat(resources, notNullValue());
		assertThat(resources, hasItem(hasProperty("id", equalTo(OBS_UUID))));
	}
	
	@Test
	public void searchForObs_shouldReturnObsByCategory() {
		TokenAndListParam categories = new TokenAndListParam().addAnd(new TokenParam().setValue("laboratory"));
		
		SearchParameterMap theParams = new SearchParameterMap();
		theParams.addParameter(FhirConstants.CATEGORY_SEARCH_HANDLER, categories);
		
		IBundleProvider results = search(theParams);
		
		assertThat(results, notNullValue());
		
		List<IBaseResource> resources = get(results);
		
		assertThat(resources, not(empty()));
		assertThat(resources, hasItem(hasProperty("id", equalTo(OBS_UUID))));
		
		// obs.getCategoryFirstRep().getCodingFirstRep().getCode() == "laboratory"
		assertThat(resources, everyItem(
		    hasProperty("categoryFirstRep", hasProperty("codingFirstRep", hasProperty("code", equalTo("laboratory"))))));
	}
	
	@Test
	public void searchForObs_shouldSortObsAsRequested() {
		SortSpec sort = new SortSpec();
		sort.setParamName("date");
		sort.setOrder(SortOrderEnum.ASC);
		
		SearchParameterMap theParams = new SearchParameterMap();
		theParams.setSortSpec(sort);
		
		IBundleProvider results = search(theParams);
		
		assertThat(results, notNullValue());
		assertThat(get(results), not(empty()));
		assertThat(get(results), hasSize(greaterThan(1)));
		
		sort.setOrder(SortOrderEnum.DESC);
		theParams.setSortSpec(sort);
		results = search(theParams);
		
		assertThat(results, notNullValue());
		assertThat(get(results), not(empty()));
		assertThat(get(results), hasSize(greaterThan(1)));
	}
	
	@Test
	public void searchForObs_shouldIgnoreSortByUnknownProperty() {
		SortSpec sort = new SortSpec();
		sort.setParamName("date");
		sort.setOrder(SortOrderEnum.DESC);
		
		SearchParameterMap theParams = new SearchParameterMap();
		theParams.setSortSpec(sort);
		
		IBundleProvider baselineObs = search(theParams);
		
		assertThat(baselineObs, notNullValue());
		assertThat(get(baselineObs), not(empty()));
		
		SortSpec subSort = new SortSpec();
		sort.setChain(subSort);
		subSort.setParamName("dummy");
		subSort.setOrder(SortOrderEnum.ASC);
		theParams.setSortSpec(sort);
		
		IBundleProvider results = search(theParams);
		
		assertThat(results, notNullValue());
		assertThat(get(results), not(empty()));
		
		List<String> baselineSortedUuids = get(baselineObs).stream().map(IBaseResource::getIdElement).map(IIdType::getIdPart)
		        .collect(Collectors.toList());
		List<String> resultSortedUuids = get(results).stream().map(IBaseResource::getIdElement).map(IIdType::getIdPart)
		        .collect(Collectors.toList());
		
		assertThat(resultSortedUuids, equalTo(baselineSortedUuids));
	}
	
	@Test
	public void searchForObs_shouldReturnObsByPatientUuidAndPatientGivenName() {
		ReferenceAndListParam patientReference = new ReferenceAndListParam().addAnd(
		    new ReferenceOrListParam().add(new ReferenceParam().setValue(PATIENT_FAMILY_NAME).setChain(Patient.SP_FAMILY))
		            .add(new ReferenceParam().setValue(PATIENT_GIVEN_NAME).setChain(Patient.SP_GIVEN)));
		
		SearchParameterMap theParams = new SearchParameterMap();
		theParams.addParameter(FhirConstants.PATIENT_REFERENCE_SEARCH_HANDLER, patientReference);
		
		IBundleProvider results = search(theParams);
		
		assertThat(results, notNullValue());
		assertThat(results.size(), equalTo(21));
		
		List<IBaseResource> resources = get(results);
		
		assertThat(resources, not(empty()));
		assertThat(resources.get(0).getIdElement().getIdPart(), equalTo(OBS_UUID));
	}
	
	@Test
	public void searchForObs_shouldReturnObsByEncounters() {
		ReferenceAndListParam encounterReference = new ReferenceAndListParam().addAnd(new ReferenceOrListParam()
		        .add(new ReferenceParam().setValue(ENCOUNTER_UUID)).add(new ReferenceParam().setValue(ENCOUNTER_UUID_TWO)));
		
		SearchParameterMap theParams = new SearchParameterMap();
		theParams.addParameter(FhirConstants.ENCOUNTER_REFERENCE_SEARCH_HANDLER, encounterReference);
		
		IBundleProvider results = search(theParams);
		
		assertThat(results, notNullValue());
		assertThat(results.size(), equalTo(14));
		
		List<IBaseResource> resources = get(results);
		
		assertThat(resources, not(empty()));
		assertThat(resources, hasSize(equalTo(10)));
		assertThat(resources, hasItem(hasProperty("id", equalTo(OBS_UUID))));
	}
	
	@Test
	public void searchForObs_shouldHandleComplexQuery() {
		TokenAndListParam code = new TokenAndListParam();
		TokenOrListParam orListParam = new TokenOrListParam();
		code.addAnd(orListParam);
		
		for (String coding : CIEL_VITAL_CODES) {
			orListParam.addOr(new TokenParam().setSystem(FhirTestConstants.CIEL_SYSTEM_URN).setValue(coding));
		}
		
		ReferenceAndListParam patientReference = new ReferenceAndListParam()
		        .addAnd(new ReferenceOrListParam().add(new ReferenceParam().setValue(PATIENT_UUID)));
		
		SearchParameterMap theParams = new SearchParameterMap();
		theParams.addParameter(FhirConstants.PATIENT_REFERENCE_SEARCH_HANDLER, patientReference);
		theParams.addParameter(FhirConstants.CODED_SEARCH_HANDLER, code);
		theParams.setSortSpec(new SortSpec().setParamName("date").setOrder(SortOrderEnum.DESC));
		
		IBundleProvider results = this.search(theParams);
		
		assertThat(results, notNullValue());
		assertThat(results.size(), equalTo(15));
		
		List<IBaseResource> resources = get(results);
		
		assertThat(resources, hasSize(equalTo(10)));
	}
	
	@Test
	public void searchForObs_shouldReturnObsByMemberReference() {
		ReferenceParam memberReference = new ReferenceParam().setValue(MEMBER_UUID).setChain("");
		
		SearchParameterMap theParams = new SearchParameterMap();
		theParams.addParameter(FhirConstants.HAS_MEMBER_SEARCH_HANDLER, memberReference);
		
		IBundleProvider results = search(theParams);
		
		assertThat(results, notNullValue());
		assertThat(results.size(), equalTo(1));
		
		assertThat(get(results), not(empty()));
		assertThat(get(results), hasSize(equalTo(1)));
		assertThat(get(results).iterator().next().getIdElement().getIdPart(), equalTo(OBS_GROUP_UUID));
	}
	
	@Test
	public void searchForObs_shouldReturnObsByMemberReferenceConceptId() {
		ReferenceParam memberReference = new ReferenceParam();
		
		memberReference.setValue(VALUE_CONCEPT_ID);
		memberReference.setChain(Observation.SP_CODE);
		
		SearchParameterMap theParams = new SearchParameterMap();
		theParams.addParameter(FhirConstants.HAS_MEMBER_SEARCH_HANDLER, memberReference);
		IBundleProvider results = search(theParams);
		
		assertThat(results, notNullValue());
		assertThat(results.size(), equalTo(1));
		
		assertThat(get(results), not(empty()));
		assertThat(get(results).iterator().next().getIdElement().getIdPart(), equalTo(OBS_GROUP_UUID));
	}
	
	@Test
	public void searchForObs_shouldSearchForObsByValueConceptId() {
		TokenAndListParam code = new TokenAndListParam();
		TokenParam codingToken = new TokenParam();
		codingToken.setValue(VALUE_CONCEPT_ID);
		code.addAnd(codingToken);
		
		SearchParameterMap theParams = new SearchParameterMap();
		theParams.addParameter(FhirConstants.CODED_SEARCH_HANDLER, code);
		IBundleProvider results = search(theParams);
		
		assertThat(results, notNullValue());
		assertThat(results.size(), equalTo(6));
		
		List<IBaseResource> resources = get(results);
		
		assertThat(resources, notNullValue());
		assertThat(resources, hasSize(greaterThan(1)));
		assertThat(resources, hasItem(hasProperty("id", equalTo(OBS_VALUE_CONCEPT_UUID))));
	}
	
	@Test
	public void searchForObs_shouldSearchForObsByValueDate() {
		SearchParameterMap theParams = new SearchParameterMap();
		theParams.addParameter(FhirConstants.DATE_RANGE_SEARCH_HANDLER, new DateRangeParam(new DateParam(VALUE_DATE)));
		IBundleProvider results = search(theParams);
		
		assertThat(results, notNullValue());
		assertThat(results.size(), equalTo(21));
		
		assertThat(get(results), not(empty()));
		assertThat(get(results), hasItem(hasProperty("id", equalTo(OBS_UUID))));
	}
	
	@Test
	public void searchForObs_shouldSearchForObsByValueDateAndTime() {
		SearchParameterMap theParams = new SearchParameterMap();
		theParams.addParameter(FhirConstants.DATE_RANGE_SEARCH_HANDLER,
		    new DateRangeParam(new DateParam(VALUE_DATE_AND_TIME)));
		IBundleProvider results = search(theParams);
		
		assertThat(results, notNullValue());
		assertThat(results.size(), equalTo(21));
		
		assertThat(get(results), not(empty()));
		assertThat(get(results), hasItem(hasProperty("id", equalTo(OBS_UUID))));
	}
	
	@Test
	public void searchForObs_shouldSearchForObsByValueQuantityWithoutPrefixAndDecimalValue() {
		QuantityAndListParam quantityAndListParam = new QuantityAndListParam();
		
		QuantityOrListParam quantityOrListParam = new QuantityOrListParam();
		
		QuantityParam quantityParam = new QuantityParam();
		quantityParam.setValue("100.00");
		quantityAndListParam.addAnd(quantityOrListParam.add(quantityParam));
		
		SearchParameterMap theParams = new SearchParameterMap();
		theParams.addParameter(FhirConstants.QUANTITY_SEARCH_HANDLER, quantityAndListParam);
		IBundleProvider results = search(theParams);
		
		assertThat(results, notNullValue());
		assertThat(results.size(), equalTo(21));
		
		assertThat(get(results), not(empty()));
		assertThat(get(results), hasSize(equalTo(10)));
	}
	
	@Test
	public void searchForObs_shouldSearchForObsByValueQuantityWithoutPrefixAndEValue() {
		QuantityAndListParam quantityAndListParam = new QuantityAndListParam();
		
		QuantityOrListParam quantityOrListParam = new QuantityOrListParam();
		
		QuantityParam quantityParam = new QuantityParam();
		quantityParam.setValue("1e2");
		quantityAndListParam.addAnd(quantityOrListParam.add(quantityParam));
		
		SearchParameterMap theParams = new SearchParameterMap();
		theParams.addParameter(FhirConstants.QUANTITY_SEARCH_HANDLER, quantityAndListParam);
		IBundleProvider results = search(theParams);
		
		assertThat(results, notNullValue());
		assertThat(results.size(), equalTo(21));
		
		assertThat(get(results), not(empty()));
		assertThat(get(results), hasSize(greaterThan(1)));
	}
	
	@Test
	public void searchForObs_shouldSearchForObsByValueQuantityWithoutPrefixAndNegativeEValue() {
		QuantityAndListParam quantityAndListParam = new QuantityAndListParam();
		
		QuantityOrListParam quantityOrListParam = new QuantityOrListParam();
		
		QuantityParam quantityParam = new QuantityParam();
		quantityParam.setValue("1e-2");
		quantityAndListParam.addAnd(quantityOrListParam.add(quantityParam));
		
		SearchParameterMap theParams = new SearchParameterMap();
		theParams.addParameter(FhirConstants.QUANTITY_SEARCH_HANDLER, quantityAndListParam);
		IBundleProvider results = search(theParams);
		
		assertThat(results, notNullValue());
		assertThat(results.size(), equalTo(21));
		
		assertThat(get(results), not(empty()));
		assertThat(get(results), hasSize(greaterThan(1)));
	}
	
	@Test
	public void searchForObs_shouldSearchForObsByValueQuantityWithoutPrefix() {
		QuantityAndListParam quantityAndListParam = new QuantityAndListParam();
		
		QuantityOrListParam quantityOrListParam = new QuantityOrListParam();
		
		QuantityParam quantityParam = new QuantityParam();
		quantityParam.setValue("188");
		quantityAndListParam.addAnd(quantityOrListParam.add(quantityParam));
		
		SearchParameterMap theParams = new SearchParameterMap();
		theParams.addParameter(FhirConstants.QUANTITY_SEARCH_HANDLER, quantityAndListParam);
		IBundleProvider results = search(theParams);
		
		assertThat(results, notNullValue());
		assertThat(results.size(), equalTo(21));
		
		assertThat(get(results), not(empty()));
		assertThat(get(results), hasSize(greaterThan(1)));
	}
	
	@Test
	public void searchForObs_shouldSearchForObsByValueQuantityWithPrefixEq() {
		QuantityAndListParam quantityAndListParam = new QuantityAndListParam()
		        .addAnd(new QuantityOrListParam().add(new QuantityParam().setValue("100").setPrefix(ParamPrefixEnum.EQUAL)));
		
		SearchParameterMap theParams = new SearchParameterMap();
		theParams.addParameter(FhirConstants.QUANTITY_SEARCH_HANDLER, quantityAndListParam);
		
		IBundleProvider results = search(theParams);
		
		assertThat(results, notNullValue());
		assertThat(results.size(), equalTo(21));
		
		assertThat(get(results), not(empty()));
		assertThat(get(results), hasSize(equalTo(10)));
		assertThat(get(results), hasSize(greaterThan(1)));
	}
	
	@Test
	public void searchForObs_shouldSearchForObsByValueQuantityWithPrefixNe() {
		QuantityAndListParam quantityAndListParam = new QuantityAndListParam().addAnd(
		    new QuantityOrListParam().add(new QuantityParam().setValue("100").setPrefix(ParamPrefixEnum.NOT_EQUAL)));
		
		SearchParameterMap theParams = new SearchParameterMap();
		theParams.addParameter(FhirConstants.QUANTITY_SEARCH_HANDLER, quantityAndListParam);
		
		IBundleProvider results = search(theParams);
		
		assertThat(results, notNullValue());
		assertThat(results.size(), equalTo(21));
		
		assertThat(get(results), not(empty()));
		assertThat(get(results), hasSize(equalTo(10)));
	}
	
	@Test
	public void searchForObs_shouldSearchForObsByValueQuantityWithPrefixLe() {
		QuantityAndListParam quantityAndListParam = new QuantityAndListParam().addAnd(new QuantityOrListParam()
		        .add(new QuantityParam().setValue("100").setPrefix(ParamPrefixEnum.LESSTHAN_OR_EQUALS)));
		
		SearchParameterMap theParams = new SearchParameterMap();
		theParams.addParameter(FhirConstants.QUANTITY_SEARCH_HANDLER, quantityAndListParam);
		
		IBundleProvider results = search(theParams);
		
		assertThat(results, notNullValue());
		assertThat(results.size(), equalTo(21));
		
		assertThat(get(results), not(empty()));
		assertThat(get(results), hasSize(equalTo(10)));
	}
	
	@Test
	public void searchForObs_shouldSearchForObsByValueQuantityWithPrefixLt() {
		QuantityAndListParam quantityAndListParam = new QuantityAndListParam().addAnd(
		    new QuantityOrListParam().add(new QuantityParam().setValue("100").setPrefix(ParamPrefixEnum.LESSTHAN)));
		
		SearchParameterMap theParams = new SearchParameterMap();
		theParams.addParameter(FhirConstants.QUANTITY_SEARCH_HANDLER, quantityAndListParam);
		
		IBundleProvider results = search(theParams);
		
		assertThat(results, notNullValue());
		assertThat(results.size(), equalTo(21));
		
		assertThat(get(results), not(empty()));
		assertThat(get(results), hasSize(equalTo(10)));
	}
	
	@Test
	public void searchForObs_shouldSearchForObsByValueQuantityWithPrefixGe() {
		QuantityAndListParam quantityAndListParam = new QuantityAndListParam().addAnd(new QuantityOrListParam()
		        .add(new QuantityParam().setValue("100").setPrefix(ParamPrefixEnum.GREATERTHAN_OR_EQUALS)));
		
		SearchParameterMap theParams = new SearchParameterMap();
		theParams.addParameter(FhirConstants.QUANTITY_SEARCH_HANDLER, quantityAndListParam);
		
		IBundleProvider results = search(theParams);
		
		assertThat(results, notNullValue());
		assertThat(results.size(), equalTo(21));
		
		assertThat(get(results), not(empty()));
		assertThat(get(results), hasSize(equalTo(10)));
	}
	
	@Test
	public void searchForObs_shouldSearchForObsByValueQuantityWithPrefixGt() {
		QuantityAndListParam quantityAndListParam = new QuantityAndListParam().addAnd(
		    new QuantityOrListParam().add(new QuantityParam().setValue("100").setPrefix(ParamPrefixEnum.GREATERTHAN)));
		
		SearchParameterMap theParams = new SearchParameterMap();
		theParams.addParameter(FhirConstants.QUANTITY_SEARCH_HANDLER, quantityAndListParam);
		
		IBundleProvider results = search(theParams);
		
		assertThat(results, notNullValue());
		assertThat(results.size(), equalTo(21));
		
		assertThat(get(results), not(empty()));
		assertThat(get(results), hasSize(equalTo(10)));
	}
	
	@Test
	public void searchForObs_shouldSearchForObsByValueQuantityWithPrefixAp() {
		QuantityAndListParam quantityAndListParam = new QuantityAndListParam().addAnd(
		    new QuantityOrListParam().add(new QuantityParam().setValue("36").setPrefix(ParamPrefixEnum.APPROXIMATE)));
		
		SearchParameterMap theParams = new SearchParameterMap();
		theParams.addParameter(FhirConstants.QUANTITY_SEARCH_HANDLER, quantityAndListParam);
		
		IBundleProvider results = search(theParams);
		
		assertThat(results, notNullValue());
		assertThat(results.size(), equalTo(21));
		
		assertThat(get(results), not(empty()));
		assertThat(get(results), hasSize(greaterThan(1)));
	}
	
	@Test
	public void searchForObs_shouldSearchForObsByValueString() {
		StringAndListParam stringAndListParam = new StringAndListParam().addAnd(new StringParam().setValue(VALUE_STRING));
		
		SearchParameterMap theParams = new SearchParameterMap();
		theParams.addParameter(FhirConstants.VALUE_STRING_SEARCH_HANDLER, stringAndListParam);
		
		IBundleProvider results = search(theParams);
		
		assertThat(results, notNullValue());
		assertThat(results.size(), equalTo(21));
		
		assertThat(get(results), not(empty()));
		assertThat(get(results), hasSize(greaterThan(1)));
	}
	
	@Test
	public void searchForObs_shouldSearchForObsByValueCoded() {
		TokenAndListParam tokenAndListParam = new TokenAndListParam().addAnd(new TokenParam().setValue(VALUE_CONCEPT_ID));
		
		SearchParameterMap theParams = new SearchParameterMap();
		theParams.addParameter(FhirConstants.VALUE_CODED_SEARCH_HANDLER, tokenAndListParam);
		
		IBundleProvider results = search(theParams);
		
		assertThat(results, notNullValue());
		assertThat(results.size(), equalTo(1));
		
		List<IBaseResource> resources = get(results);
		
		assertThat(resources, notNullValue());
		assertThat(resources, hasSize(equalTo(1)));
		assertThat(resources, hasItem(hasProperty("id", equalTo(OBS_VALUE_CONCEPT_UUID))));
	}
	
	private IBundleProvider search(SearchParameterMap theParams) {
		return searchQuery.getQueryResults(theParams, dao, translator);
	}
	
	private List<IBaseResource> get(IBundleProvider results) {
		return results.getResources(START_INDEX, END_INDEX);
	}
}
