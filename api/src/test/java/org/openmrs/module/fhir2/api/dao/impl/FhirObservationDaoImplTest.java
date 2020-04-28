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

import static org.exparity.hamcrest.date.DateMatchers.sameOrAfter;
import static org.exparity.hamcrest.date.DateMatchers.sameOrBefore;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import ca.uhn.fhir.rest.api.SortOrderEnum;
import ca.uhn.fhir.rest.api.SortSpec;
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
import org.apache.commons.lang3.math.NumberUtils;
import org.hl7.fhir.r4.model.Observation;
import org.hl7.fhir.r4.model.Patient;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.Obs;
import org.openmrs.module.fhir2.FhirTestConstants;
import org.openmrs.module.fhir2.TestFhirSpringConfiguration;
import org.openmrs.test.BaseModuleContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

@ContextConfiguration(classes = TestFhirSpringConfiguration.class, inheritLocations = false)
public class FhirObservationDaoImplTest extends BaseModuleContextSensitiveTest {
	
	private static final String OBS_DATA_XML = "org/openmrs/module/fhir2/api/dao/impl/FhirObservationDaoImplTest_initial_data_suppl.xml";
	
	private static final String OBS_UUID = "39fb7f47-e80a-4056-9285-bd798be13c63";
	
	private static final String OBS_WITH_DATE_UUID = "be48cdcb-6a76-47e3-9f2e-2635032f3a9a";
	
	private static final String OBS_WITH_VALUE_DATE_UUID = "99b92980-db62-40cd-8bca-733357c48126";
	
	private static final String BAD_OBS_UUID = "121b73a6-e1a4-4424-8610-d5765bf2fdf7";
	
	private static final String OBS_MINUTE_UUID = "942ec003-a55d-43c4-ac7a-bd6d1ba63381";
	
	private static final String OBS_CONCEPT_ID = "5089";
	
	private static final String VALUE_CONCEPT_ID = "5242";
	
	private static final String OBS_VALUE_CONCEPT_UUID = "785li1f8-bdbc-4950-833b-002244e9fa2b";
	
	private static final String OBS_DATE = "2008-08-01";
	
	private static final String OBS_MONTH = "2008-08";
	
	private static final String OBS_MINUTE = "2008-07-01T10:00";
	
	private static final String VALUE_QUANTITY = "134.0";
	
	private static final String VALUE_STRING = "AFH56";
	
	private static final String VALUE_DATE = "2008-08-14";
	
	private static final String OBS_CONCEPT_UUID = "c607c80f-1ea9-4da3-bb88-6276ce8868dd";
	
	private static final String PATIENT_UUID = "5946f880-b197-400b-9caa-a3c661d23041";
	
	private static final String PATIENT_GIVEN_NAME = "Collet";
	
	private static final String PATIENT_FAMILY_NAME = "Chebaskwony";
	
	private static final String PATIENT_IDENTIFIER = "6TS-4";
	
	private static final String ENCOUNTER_UUID = "6519d653-393b-4118-9c83-a3715b82d4ac";
	
	private static final String ENCOUNTER_UUID_TWO = "6519d653-393b-4118-9c83-a3715b82d4ac";
	
	private static final String MEMBER_UUID = "744b91f8-bdbc-4950-833b-002244e9fa2b";
	
	private static final String OBS_MEMBER_UUID = "4efa62d2-6b8b-4803-a8fa-3f32ee54db4f";
	
	private static final String SNOMED_SYSTEM_URI = "http://snomed.info/sct";
	
	private static final String OBS_SNOMED_CODE = "2332523";
	
	private static final String[] CIEL_VITAL_CODES = new String[] { "5085", "5086", "5087", "5088", "5089", "5090", "5092",
	        "5242" };
	
	private static final String CIEL_DIASTOLIC_BP = "5086";
	
	private static final String LOINC_SYSTOLIC_BP = "8480-6";
	
	@Autowired
	FhirObservationDaoImpl dao;
	
	@Before
	public void setup() throws Exception {
		executeDataSet(OBS_DATA_XML);
	}
	
	@Test
	public void getObsByUuid_shouldGetObsByUuid() {
		Obs result = dao.get(OBS_UUID);
		
		assertThat(result, notNullValue());
		assertThat(result.getUuid(), equalTo(OBS_UUID));
	}
	
	@Test
	public void getObsByUuid_shouldReturnNullIfObsNotFoundByUuid() {
		Obs result = dao.get(BAD_OBS_UUID);
		
		assertThat(result, nullValue());
	}
	
	@Test
	public void searchForObs_shouldSearchForObsByConceptId() {
		TokenAndListParam code = new TokenAndListParam();
		TokenParam codingToken = new TokenParam();
		codingToken.setValue(OBS_CONCEPT_ID);
		code.addAnd(codingToken);
		
		Collection<Obs> results = dao.searchForObservations(null, null, null, null, null, null, null, null, code, null);
		
		assertThat(results, notNullValue());
		assertThat(results, not(empty()));
		assertThat(results, hasItem(hasProperty("uuid", equalTo(OBS_UUID))));
	}
	
	@Test
	public void searchForObs_shouldSearchForObsByConceptUuid() {
		TokenAndListParam code = new TokenAndListParam();
		TokenParam codingToken = new TokenParam();
		codingToken.setValue(OBS_CONCEPT_UUID);
		code.addAnd(codingToken);
		
		Collection<Obs> results = dao.searchForObservations(null, null, null, null, null, null, null, null, code, null);
		
		assertThat(results, notNullValue());
		assertThat(results, not(empty()));
		assertThat(results, hasItem(hasProperty("uuid", equalTo(OBS_UUID))));
	}
	
	@Test
	public void searchForObs_shouldReturnObsByConceptMapping() {
		TokenAndListParam code = new TokenAndListParam();
		TokenParam codingToken = new TokenParam();
		codingToken.setSystem(SNOMED_SYSTEM_URI);
		codingToken.setValue(OBS_SNOMED_CODE);
		code.addAnd(codingToken);
		
		Collection<Obs> results = dao.searchForObservations(null, null, null, null, null, null, null, null, code, null);
		
		assertThat(results, notNullValue());
		assertThat(results, not(empty()));
		assertThat(results, hasItem(hasProperty("uuid", equalTo(OBS_UUID))));
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
		
		Collection<Obs> results = dao.searchForObservations(null, null, null, null, null, null, null, null, code, null);
		
		assertThat(results, notNullValue());
		assertThat(results, not(empty()));
		assertThat(results, hasSize(greaterThan(1)));
		for (String coding : CIEL_VITAL_CODES) {
			assertThat(results,
			    hasItem(hasProperty("concept", hasProperty("conceptId", equalTo(NumberUtils.toInt(coding))))));
		}
	}
	
	@Test
	public void searchForObs_shouldReturnFromMultipleConceptMappings() {
		TokenAndListParam code = new TokenAndListParam();
		TokenOrListParam orListParam = new TokenOrListParam();
		code.addAnd(orListParam);
		
		TokenParam codingToken1 = new TokenParam();
		codingToken1.setSystem(FhirTestConstants.LOINC_SYSTEM_URL);
		codingToken1.setValue(LOINC_SYSTOLIC_BP);
		orListParam.addOr(codingToken1);
		
		TokenParam codingToken2 = new TokenParam();
		codingToken2.setSystem(FhirTestConstants.CIEL_SYSTEM_URN);
		codingToken2.setValue(CIEL_DIASTOLIC_BP);
		orListParam.addOr(codingToken2);
		
		Collection<Obs> results = dao.searchForObservations(null, null, null, null, null, null, null, null, code, null);
		
		assertThat(results, notNullValue());
		assertThat(results, not(empty()));
		assertThat(results, hasSize(greaterThan(1)));
		assertThat(results, hasItem(hasProperty("concept", hasProperty("conceptId", equalTo(5085)))));
		assertThat(results, hasItem(hasProperty("concept", hasProperty("conceptId", equalTo(5086)))));
	}
	
	@Test
	public void searchForObs_shouldSupportMappedAndUnmappedConcepts() {
		TokenAndListParam code = new TokenAndListParam();
		TokenOrListParam orListParam = new TokenOrListParam();
		code.addAnd(orListParam);
		
		TokenParam codingToken1 = new TokenParam();
		codingToken1.setSystem(FhirTestConstants.LOINC_SYSTEM_URL);
		codingToken1.setValue(LOINC_SYSTOLIC_BP);
		orListParam.addOr(codingToken1);
		
		TokenParam codingToken2 = new TokenParam();
		codingToken2.setValue(CIEL_DIASTOLIC_BP);
		orListParam.addOr(codingToken2);
		
		Collection<Obs> results = dao.searchForObservations(null, null, null, null, null, null, null, null, code, null);
		
		assertThat(results, notNullValue());
		assertThat(results, not(empty()));
		assertThat(results, hasSize(greaterThan(1)));
		assertThat(results, hasItem(hasProperty("concept", hasProperty("conceptId", equalTo(5085)))));
		assertThat(results, hasItem(hasProperty("concept", hasProperty("conceptId", equalTo(5086)))));
	}
	
	@Test
	public void searchForObs_shouldReturnObsByPatientUuid() {
		ReferenceAndListParam patientReference = new ReferenceAndListParam();
		ReferenceParam patient = new ReferenceParam();
		
		patient.setValue(PATIENT_UUID);
		
		patientReference.addValue(new ReferenceOrListParam().add(patient));
		
		Collection<Obs> results = dao.searchForObservations(null, patientReference, null, null, null, null, null, null, null,
		    null);
		
		assertThat(results, notNullValue());
		assertThat(results, not(empty()));
		assertThat(results, hasItem(hasProperty("uuid", equalTo(OBS_UUID))));
	}
	
	@Test
	public void searchForObs_shouldReturnObsByPatientGivenName() {
		ReferenceAndListParam patientReference = new ReferenceAndListParam();
		ReferenceParam patient = new ReferenceParam();
		
		patient.setValue(PATIENT_GIVEN_NAME);
		patient.setChain(Patient.SP_GIVEN);
		
		patientReference.addValue(new ReferenceOrListParam().add(patient));
		
		Collection<Obs> results = dao.searchForObservations(null, patientReference, null, null, null, null, null, null, null,
		    null);
		
		assertThat(results, notNullValue());
		assertThat(results, not(empty()));
		assertThat(results, hasItem(hasProperty("uuid", equalTo(OBS_UUID))));
	}
	
	@Test
	public void searchForObs_shouldReturnObsByPatientFamilyName() {
		ReferenceAndListParam patientReference = new ReferenceAndListParam();
		ReferenceParam patient = new ReferenceParam();
		
		patient.setValue(PATIENT_FAMILY_NAME);
		patient.setChain(Patient.SP_FAMILY);
		
		patientReference.addValue(new ReferenceOrListParam().add(patient));
		
		Collection<Obs> results = dao.searchForObservations(null, patientReference, null, null, null, null, null, null, null,
		    null);
		
		assertThat(results, notNullValue());
		assertThat(results, not(empty()));
		assertThat(results, hasItem(hasProperty("uuid", equalTo(OBS_UUID))));
	}
	
	@Test
	public void searchForObs_shouldReturnObsByPatientName() {
		ReferenceAndListParam patientReference = new ReferenceAndListParam();
		ReferenceParam patient = new ReferenceParam();
		
		patient.setValue(PATIENT_GIVEN_NAME + " " + PATIENT_FAMILY_NAME);
		patient.setChain(Patient.SP_NAME);
		
		patientReference.addValue(new ReferenceOrListParam().add(patient));
		
		Collection<Obs> results = dao.searchForObservations(null, patientReference, null, null, null, null, null, null, null,
		    null);
		
		assertThat(results, notNullValue());
		assertThat(results, not(empty()));
		assertThat(results, hasItem(hasProperty("uuid", equalTo(OBS_UUID))));
	}
	
	@Test
	public void searchForObs_shouldReturnObsByPatientIdentifier() {
		ReferenceAndListParam patientReference = new ReferenceAndListParam();
		ReferenceParam patient = new ReferenceParam();
		
		patient.setValue(PATIENT_IDENTIFIER);
		patient.setChain(Patient.SP_IDENTIFIER);
		
		patientReference.addValue(new ReferenceOrListParam().add(patient));
		
		Collection<Obs> results = dao.searchForObservations(null, patientReference, null, null, null, null, null, null, null,
		    null);
		
		assertThat(results, notNullValue());
		assertThat(results, not(empty()));
		assertThat(results, hasItem(hasProperty("uuid", equalTo(OBS_UUID))));
	}
	
	@Test
	public void searchForObs_shouldReturnObsByEncounter() {
		ReferenceAndListParam encounterReference = new ReferenceAndListParam();
		encounterReference.addValue(new ReferenceOrListParam().add(new ReferenceParam().setValue(ENCOUNTER_UUID)));
		
		Collection<Obs> results = dao.searchForObservations(encounterReference, null, null, null, null, null, null, null,
		    null, null);
		
		assertThat(results, notNullValue());
		assertThat(results, not(empty()));
		assertThat(results, hasItem(hasProperty("uuid", equalTo(OBS_UUID))));
	}
	
	@Test
	public void searchForObs_shouldSortObsAsRequested() {
		SortSpec sort = new SortSpec();
		sort.setParamName("date");
		sort.setOrder(SortOrderEnum.ASC);
		
		Collection<Obs> results = dao.searchForObservations(null, null, null, null, null, null, null, null, null, sort);
		
		assertThat(results, notNullValue());
		assertThat(results, not(empty()));
		assertThat(results.size(), greaterThan(1));
		
		List<Obs> resultsList = new ArrayList<>(results);
		// pair-wise compare of all obs by date
		for (int i = 1; i < resultsList.size(); i++) {
			assertThat(resultsList.get(i - 1).getObsDatetime(), sameOrBefore(resultsList.get(i).getObsDatetime()));
		}
		
		sort.setOrder(SortOrderEnum.DESC);
		
		results = dao.searchForObservations(null, null, null, null, null, null, null, null, null, sort);
		
		assertThat(results, notNullValue());
		assertThat(results, not(empty()));
		assertThat(results.size(), greaterThan(1));
		
		resultsList = new ArrayList<>(results);
		// pair-wise compare of all obs by date
		for (int i = 1; i < resultsList.size(); i++) {
			assertThat(resultsList.get(i - 1).getObsDatetime(), sameOrAfter(resultsList.get(i).getObsDatetime()));
		}
	}
	
	@Test
	public void searchForObs_shouldIgnoreSearchByUnknownProperty() {
		SortSpec sort = new SortSpec();
		sort.setParamName("date");
		sort.setOrder(SortOrderEnum.DESC);
		
		Collection<Obs> baselineObs = dao.searchForObservations(null, null, null, null, null, null, null, null, null, sort);
		
		assertThat(baselineObs, notNullValue());
		assertThat(baselineObs, not(empty()));
		
		SortSpec subSort = new SortSpec();
		sort.setChain(subSort);
		subSort.setParamName("dummy");
		subSort.setOrder(SortOrderEnum.ASC);
		
		Collection<Obs> results = dao.searchForObservations(null, null, null, null, null, null, null, null, null, sort);
		
		assertThat(results, notNullValue());
		assertThat(results, not(empty()));
		assertThat(results, equalTo(baselineObs));
	}
	
	@Test
	public void searchForObs_shouldReturnObsByPatientUuidAndPatientGivenName() {
		ReferenceAndListParam patientReference = new ReferenceAndListParam();
		ReferenceParam patientOne = new ReferenceParam();
		ReferenceParam patientTwo = new ReferenceParam();
		
		patientOne.setValue(PATIENT_FAMILY_NAME);
		patientOne.setChain(Patient.SP_FAMILY);
		
		patientTwo.setValue(PATIENT_GIVEN_NAME);
		patientTwo.setChain(Patient.SP_GIVEN);
		
		patientReference.addValue(new ReferenceOrListParam().add(patientOne).add(patientTwo));
		
		Collection<Obs> results = dao.searchForObservations(null, patientReference, null, null, null, null, null, null, null,
		    null);
		
		assertThat(results, notNullValue());
		assertThat(results, not(empty()));
		assertThat(results.iterator().next().getUuid(), equalTo(OBS_UUID));
	}
	
	@Test
	public void searchForObs_shouldReturnObsByEncounters() {
		ReferenceAndListParam encounterReference = new ReferenceAndListParam();
		encounterReference.addValue(new ReferenceOrListParam().add(new ReferenceParam().setValue(ENCOUNTER_UUID))
		        .add(new ReferenceParam().setValue(ENCOUNTER_UUID_TWO)));
		
		Collection<Obs> results = dao.searchForObservations(encounterReference, null, null, null, null, null, null, null,
		    null, null);
		
		assertThat(results, notNullValue());
		assertThat(results, not(empty()));
		assertThat(results, hasItem(hasProperty("uuid", equalTo(OBS_UUID))));
	}
	
	@Test
	public void searchForObs_shouldHandleComplexQuery() {
		TokenAndListParam code = new TokenAndListParam();
		TokenOrListParam orListParam = new TokenOrListParam();
		code.addAnd(orListParam);
		
		for (String coding : CIEL_VITAL_CODES) {
			TokenParam codingToken = new TokenParam();
			codingToken.setSystem(FhirTestConstants.CIEL_SYSTEM_URN);
			codingToken.setValue(coding);
			orListParam.addOr(codingToken);
		}
		
		ReferenceAndListParam patientReference = new ReferenceAndListParam();
		ReferenceParam patient = new ReferenceParam();
		
		patient.setValue(PATIENT_UUID);
		
		patientReference.addValue(new ReferenceOrListParam().add(patient));
		
		SortSpec sort = new SortSpec();
		sort.setParamName("date");
		sort.setOrder(SortOrderEnum.DESC);
		
		Collection<Obs> results = dao.searchForObservations(null, patientReference, null, null, null, null, null, null, code,
		    sort);
		
		assertThat(results, notNullValue());
		assertThat(results, not(empty()));
		assertThat(results, hasSize(greaterThan(1)));
		for (String coding : CIEL_VITAL_CODES) {
			assertThat(results,
			    hasItem(hasProperty("concept", hasProperty("conceptId", equalTo(NumberUtils.toInt(coding))))));
		}
		
		List<Obs> resultsList = new ArrayList<>(results);
		// pair-wise compare of all obs by date
		for (int i = 1; i < resultsList.size(); i++) {
			assertThat(resultsList.get(i - 1).getObsDatetime(), sameOrAfter(resultsList.get(i).getObsDatetime()));
		}
	}
	
	@Test
	public void searchForObs_shouldReturnObsByMemberReference() {
		ReferenceParam memberReference = new ReferenceParam();
		
		memberReference.setValue(MEMBER_UUID);
		memberReference.setChain("");
		
		Collection<Obs> results = dao.searchForObservations(null, null, memberReference, null, null, null, null, null, null,
		    null);
		
		assertThat(results, notNullValue());
		assertThat(results, not(empty()));
		assertThat(results.iterator().next().getUuid(), equalTo(OBS_MEMBER_UUID));
		assertThat(results.iterator().next().getGroupMembers().iterator().next().getUuid(), equalTo(MEMBER_UUID));
	}
	
	@Test
	public void searchForObs_shouldReturnObsByMemberReferenceConceptId() {
		ReferenceParam memberReference = new ReferenceParam();
		
		memberReference.setValue(VALUE_CONCEPT_ID);
		memberReference.setChain(Observation.SP_CODE);
		
		Collection<Obs> results = dao.searchForObservations(null, null, memberReference, null, null, null, null, null, null,
		    null);
		
		assertThat(results, notNullValue());
		assertThat(results, not(empty()));
		assertThat(results.iterator().next().getUuid(), equalTo(OBS_MEMBER_UUID));
		assertThat(results.iterator().next().getGroupMembers().iterator().next().getConcept().getConceptId().toString(),
		    equalTo(VALUE_CONCEPT_ID));
	}
	
	@Test
	public void searchForObs_shouldSearchForObsByValueConceptId() {
		TokenAndListParam code = new TokenAndListParam();
		TokenParam codingToken = new TokenParam();
		codingToken.setValue(VALUE_CONCEPT_ID);
		code.addAnd(codingToken);
		
		Collection<Obs> results = dao.searchForObservations(null, null, null, code, null, null, null, null, null, null);
		
		assertThat(results, notNullValue());
		assertThat(results, not(empty()));
		assertThat(results.iterator().next().getUuid(), equalTo(OBS_VALUE_CONCEPT_UUID));
	}
	
	@Test
	public void searchForObs_shouldSearchForObsByValueDate() {
		Collection<Obs> results = dao.searchForObservations(null, null, null, null,
		    new DateRangeParam(new DateParam(VALUE_DATE)), null, null, null, null, null);
		
		assertThat(results, notNullValue());
		assertThat(results, hasSize(1));
		assertThat(results, hasItem(hasProperty("uuid", equalTo(OBS_WITH_VALUE_DATE_UUID))));
	}
	
	@Test
	public void searchForObs_shouldSearchForObsByDate() {
		Collection<Obs> results = dao.searchForObservations(null, null, null, null, null, null, null,
		    new DateRangeParam(new DateParam(OBS_DATE)), null, null);
		
		assertThat(results, notNullValue());
		assertThat(results, hasSize(1));
		assertThat(results, hasItem(hasProperty("uuid", equalTo(OBS_WITH_DATE_UUID))));
	}
	
	@Test
	public void searchForObs_shouldSearchForObsByMonth() {
		Collection<Obs> results = dao.searchForObservations(null, null, null, null, null, null, null,
		    new DateRangeParam(new DateParam(OBS_MONTH)), null, null);
		
		assertThat(results, notNullValue());
		assertThat(results, hasSize(8));
		assertThat(results, hasItem(hasProperty("uuid", equalTo(OBS_WITH_DATE_UUID))));
	}
	
	@Test
	public void searchForObs_shouldSearchForObsByMinute() {
		Collection<Obs> results = dao.searchForObservations(null, null, null, null, null, null, null,
		    new DateRangeParam(new DateParam(OBS_MINUTE)), null, null);
		
		assertThat(results, notNullValue());
		assertThat(results, hasSize(1));
		assertThat(results, hasItem(hasProperty("uuid", equalTo(OBS_MINUTE_UUID))));
	}
	
	@Test
	public void searchForObs_shouldSearchForObsByValueQuantityWithoutPrefixAndDecimalValue() {
		QuantityAndListParam quantityAndListParam = new QuantityAndListParam();
		
		QuantityOrListParam quantityOrListParam = new QuantityOrListParam();
		
		QuantityParam quantityParam = new QuantityParam();
		quantityParam.setValue("100.00");
		quantityAndListParam.addAnd(quantityOrListParam.add(quantityParam));
		
		Collection<Obs> results = dao.searchForObservations(null, null, null, null, null, quantityAndListParam, null, null,
		    null, null);
		
		assertThat(results, notNullValue());
		assertThat(results, not(empty()));
		assertThat(results, hasItem(hasProperty("uuid", equalTo("89fg071-1f7d-4394-a316-0a458edf28c3"))));
	}
	
	@Test
	public void searchForObs_shouldSearchForObsByValueQuantityWithoutPrefixAndEValue() {
		QuantityAndListParam quantityAndListParam = new QuantityAndListParam();
		
		QuantityOrListParam quantityOrListParam = new QuantityOrListParam();
		
		QuantityParam quantityParam = new QuantityParam();
		quantityParam.setValue("1e2");
		quantityAndListParam.addAnd(quantityOrListParam.add(quantityParam));
		
		Collection<Obs> results = dao.searchForObservations(null, null, null, null, null, quantityAndListParam, null, null,
		    null, null);
		
		assertThat(results, notNullValue());
		assertThat(results, not(empty()));
		assertThat(results, hasItem(hasProperty("uuid", equalTo("89fg071-1f7d-4394-a316-0a458edf28c3"))));
	}
	
	@Test
	public void searchForObs_shouldSearchForObsByValueQuantityWithoutPrefixAndNegativeEValue() {
		QuantityAndListParam quantityAndListParam = new QuantityAndListParam();
		
		QuantityOrListParam quantityOrListParam = new QuantityOrListParam();
		
		QuantityParam quantityParam = new QuantityParam();
		quantityParam.setValue("1e-2");
		quantityAndListParam.addAnd(quantityOrListParam.add(quantityParam));
		
		Collection<Obs> results = dao.searchForObservations(null, null, null, null, null, quantityAndListParam, null, null,
		    null, null);
		
		assertThat(results, notNullValue());
		assertThat(results, not(empty()));
		assertThat(results, hasItem(hasProperty("uuid", equalTo("56htgf-1f7d-4394-a316-0a458edf28c3"))));
	}
	
	@Test
	public void searchForObs_shouldSearchForObsByValueQuantityWithoutPrefix() {
		QuantityAndListParam quantityAndListParam = new QuantityAndListParam();
		
		QuantityOrListParam quantityOrListParam = new QuantityOrListParam();
		
		QuantityParam quantityParam = new QuantityParam();
		quantityParam.setValue("188");
		quantityAndListParam.addAnd(quantityOrListParam.add(quantityParam));
		
		Collection<Obs> results = dao.searchForObservations(null, null, null, null, null, quantityAndListParam, null, null,
		    null, null);
		
		assertThat(results, notNullValue());
		assertThat(results, not(empty()));
		assertThat(results, hasItem(hasProperty("uuid", equalTo("30ba0383-9377-46e9-aab3-5fee12e5ed0a"))));
	}
	
	@Test
	public void searchForObs_shouldSearchForObsByValueQuantityWithPrefixEq() {
		QuantityAndListParam quantityAndListParam = new QuantityAndListParam();
		
		QuantityOrListParam quantityOrListParam = new QuantityOrListParam();
		
		QuantityParam quantityParam = new QuantityParam();
		quantityParam.setValue("100");
		quantityParam.setPrefix(ParamPrefixEnum.EQUAL);
		quantityAndListParam.addAnd(quantityOrListParam.add(quantityParam));
		
		Collection<Obs> results = dao.searchForObservations(null, null, null, null, null, quantityAndListParam, null, null,
		    null, null);
		
		assertThat(results, notNullValue());
		assertThat(results, not(empty()));
		assertThat(results.size(), equalTo(1));
		assertThat(results, hasItem(hasProperty("uuid", equalTo("86sgf-1f7d-4394-a316-0a458edf28c3"))));
	}
	
	@Test
	public void searchForObs_shouldSearchForObsByValueQuantityWithPrefixNe() {
		QuantityAndListParam quantityAndListParam = new QuantityAndListParam();
		
		QuantityOrListParam quantityOrListParam = new QuantityOrListParam();
		
		QuantityParam quantityParam = new QuantityParam();
		quantityParam.setValue("100");
		quantityParam.setPrefix(ParamPrefixEnum.NOT_EQUAL);
		quantityAndListParam.addAnd(quantityOrListParam.add(quantityParam));
		
		Collection<Obs> results = dao.searchForObservations(null, null, null, null, null, quantityAndListParam, null, null,
		    null, null);
		
		assertThat(results, notNullValue());
		assertThat(results, not(empty()));
		assertThat(results.size(), equalTo(16));
	}
	
	@Test
	public void searchForObs_shouldSearchForObsByValueQuantityWithPrefixLe() {
		QuantityAndListParam quantityAndListParam = new QuantityAndListParam();
		
		QuantityOrListParam quantityOrListParam = new QuantityOrListParam();
		
		QuantityParam quantityParam = new QuantityParam();
		quantityParam.setValue("100");
		quantityParam.setPrefix(ParamPrefixEnum.LESSTHAN_OR_EQUALS);
		quantityAndListParam.addAnd(quantityOrListParam.add(quantityParam));
		
		Collection<Obs> results = dao.searchForObservations(null, null, null, null, null, quantityAndListParam, null, null,
		    null, null);
		
		assertThat(results, notNullValue());
		assertThat(results, not(empty()));
		assertThat(results.size(), equalTo(11));
	}
	
	@Test
	public void searchForObs_shouldSearchForObsByValueQuantityWithPrefixLt() {
		QuantityAndListParam quantityAndListParam = new QuantityAndListParam();
		
		QuantityOrListParam quantityOrListParam = new QuantityOrListParam();
		
		QuantityParam quantityParam = new QuantityParam();
		quantityParam.setValue("100");
		quantityParam.setPrefix(ParamPrefixEnum.LESSTHAN);
		quantityAndListParam.addAnd(quantityOrListParam.add(quantityParam));
		
		Collection<Obs> results = dao.searchForObservations(null, null, null, null, null, quantityAndListParam, null, null,
		    null, null);
		
		assertThat(results, notNullValue());
		assertThat(results, not(empty()));
		assertThat(results.size(), equalTo(10));
	}
	
	@Test
	public void searchForObs_shouldSearchForObsByValueQuantityWithPrefixGe() {
		QuantityAndListParam quantityAndListParam = new QuantityAndListParam();
		
		QuantityOrListParam quantityOrListParam = new QuantityOrListParam();
		
		QuantityParam quantityParam = new QuantityParam();
		quantityParam.setValue("100");
		quantityParam.setPrefix(ParamPrefixEnum.GREATERTHAN_OR_EQUALS);
		quantityAndListParam.addAnd(quantityOrListParam.add(quantityParam));
		
		Collection<Obs> results = dao.searchForObservations(null, null, null, null, null, quantityAndListParam, null, null,
		    null, null);
		
		assertThat(results, notNullValue());
		assertThat(results, not(empty()));
		assertThat(results.size(), equalTo(7));
	}
	
	@Test
	public void searchForObs_shouldSearchForObsByValueQuantityWithPrefixGt() {
		QuantityAndListParam quantityAndListParam = new QuantityAndListParam();
		
		QuantityOrListParam quantityOrListParam = new QuantityOrListParam();
		
		QuantityParam quantityParam = new QuantityParam();
		quantityParam.setValue("100");
		quantityParam.setPrefix(ParamPrefixEnum.GREATERTHAN);
		quantityAndListParam.addAnd(quantityOrListParam.add(quantityParam));
		
		Collection<Obs> results = dao.searchForObservations(null, null, null, null, null, quantityAndListParam, null, null,
		    null, null);
		
		assertThat(results, notNullValue());
		assertThat(results, not(empty()));
		assertThat(results.size(), equalTo(6));
	}
	
	@Test
	public void searchForObs_shouldSearchForObsByValueQuantityWithPrefixAp() {
		QuantityAndListParam quantityAndListParam = new QuantityAndListParam();
		
		QuantityOrListParam quantityOrListParam = new QuantityOrListParam();
		
		QuantityParam quantityParam = new QuantityParam();
		quantityParam.setValue("36");
		quantityParam.setPrefix(ParamPrefixEnum.APPROXIMATE);
		quantityAndListParam.addAnd(quantityOrListParam.add(quantityParam));
		
		Collection<Obs> results = dao.searchForObservations(null, null, null, null, null, quantityAndListParam, null, null,
		    null, null);
		
		assertThat(results, notNullValue());
		assertThat(results, not(empty()));
		assertThat(results, hasItem(hasProperty("uuid", equalTo("7e77d071-1f7d-4394-a316-0a458edf28c3"))));
	}
	
	@Test
	public void searchForObs_shouldSearchForObsByValueString() {
		StringAndListParam stringAndListParam = new StringAndListParam();
		StringParam stringParam = new StringParam();
		stringParam.setValue(VALUE_STRING);
		stringAndListParam.addAnd(stringParam);
		
		Collection<Obs> results = dao.searchForObservations(null, null, null, null, null, null, stringAndListParam, null,
		    null, null);
		
		assertThat(results, notNullValue());
		assertThat(results, not(empty()));
		assertThat(results, hasItem(hasProperty("uuid", equalTo(OBS_VALUE_CONCEPT_UUID))));
	}
	
}
