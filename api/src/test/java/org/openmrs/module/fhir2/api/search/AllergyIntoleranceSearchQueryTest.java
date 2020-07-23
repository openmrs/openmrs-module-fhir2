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
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.hamcrest.Matchers.not;
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
import ca.uhn.fhir.rest.param.DateRangeParam;
import ca.uhn.fhir.rest.param.ReferenceAndListParam;
import ca.uhn.fhir.rest.param.ReferenceOrListParam;
import ca.uhn.fhir.rest.param.ReferenceParam;
import ca.uhn.fhir.rest.param.TokenAndListParam;
import ca.uhn.fhir.rest.param.TokenOrListParam;
import ca.uhn.fhir.rest.param.TokenParam;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.AllergyIntolerance;
import org.hl7.fhir.r4.model.Patient;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.Allergy;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.TestFhirSpringConfiguration;
import org.openmrs.module.fhir2.api.FhirGlobalPropertyService;
import org.openmrs.module.fhir2.api.dao.FhirAllergyIntoleranceDao;
import org.openmrs.module.fhir2.api.search.param.SearchParameterMap;
import org.openmrs.module.fhir2.api.translators.AllergyIntoleranceTranslator;
import org.openmrs.test.BaseModuleContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

@ContextConfiguration(classes = { TestFhirSpringConfiguration.class }, inheritLocations = false)
public class AllergyIntoleranceSearchQueryTest extends BaseModuleContextSensitiveTest {
	
	private static final String ALLERGY_INTOLERANCE_INITIAL_DATA_XML = "org/openmrs/module/fhir2/api/dao/impl/FhirAllergyIntoleranceDaoImplTest_initial_data.xml";
	
	private static final String ALLERGY_UUID = "1084AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
	
	private static final String CODED_ALLERGEN_UUID = "a09ab2c5-878e-4905-b25d-5784167d0216";
	
	private static final String SEVERITY_MILD_CONCEPT_UUID = "5090AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
	
	private static final String SEVERITY_MODERATE_CONCEPT_UUID = "5091AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
	
	private static final String SEVERITY_SEVERE_CONCEPT_UUID = "5092AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
	
	private static final String SEVERITY_NULL_CONCEPT_UUID = "5093AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
	
	private static final String CODED_REACTION_UUID = "5087AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
	
	private static final String PATIENT_UUID = "8adf539e-4b5a-47aa-80c0-ba1025c957fa";
	
	private static final String PATIENT_WRONG_UUID = "c2299800-cca9-11e0-9572-abcdef0c9a66";
	
	private static final String PATIENT_IDENTIFIER = "7TU-8";
	
	private static final String PATIENT_WRONG_IDENTIFIER = "Wrong identifier";
	
	private static final String PATIENT_GIVEN_NAME = "Anet";
	
	private static final String PATIENT_WRONG_GIVEN_NAME = "Wrong given name";
	
	private static final String PATIENT_FAMILY_NAME = "Oloo";
	
	private static final String PATIENT_WRONG_FAMILY_NAME = "Wrong family name";
	
	private static final String PATIENT_NAME = "Anet Oloo";
	
	private static final String PATIENT_WRONG_NAME = "Wrong name";
	
	private static final String CATEGORY_FOOD = "food";
	
	private static final String CATEGORY_ENVIRONMENT = "environment";
	
	private static final String CATEGORY_MEDICATION = "medication";
	
	private static final String SEVERITY_MILD = "mild";
	
	private static final String SEVERITY_MODERATE = "moderate";
	
	private static final String SEVERITY_SEVERE = "severe";
	
	private static final String STATUS = "active";
	
	private static final String DATE_CREATED = "2005-01-01";
	
	private static final String DATE_CHANGED = "2010-03-31";
	
	private static final String DATE_VOIDED = "2010-09-03";
	
	private static final int START_INDEX = 0;
	
	private static final int END_INDEX = 10;
	
	private static final Map<String, String> SEVERITY_CONCEPT_UUIDS = new HashMap<>();
	
	@Autowired
	private FhirAllergyIntoleranceDao allergyDao;
	
	@Autowired
	private AllergyIntoleranceTranslator translator;
	
	@Autowired
	private SearchQueryInclude<AllergyIntolerance> searchQueryInclude;
	
	@Autowired
	private SearchQuery<Allergy, AllergyIntolerance, FhirAllergyIntoleranceDao, AllergyIntoleranceTranslator, SearchQueryInclude<AllergyIntolerance>> searchQuery;
	
	@Autowired
	private FhirGlobalPropertyService globalPropertyService;
	
	@Before
	public void setup() throws Exception {
		executeDataSet(ALLERGY_INTOLERANCE_INITIAL_DATA_XML);
	}
	
	@Before
	public void setupMocks() {
		initSeverityData();
		
		when(globalPropertyService.getGlobalProperties(FhirConstants.GLOBAL_PROPERTY_MILD,
		    FhirConstants.GLOBAL_PROPERTY_MODERATE, FhirConstants.GLOBAL_PROPERTY_SEVERE,
		    FhirConstants.GLOBAL_PROPERTY_OTHER)).thenReturn(SEVERITY_CONCEPT_UUIDS);
	}
	
	private void initSeverityData() {
		SEVERITY_CONCEPT_UUIDS.put(FhirConstants.GLOBAL_PROPERTY_MILD, SEVERITY_MILD_CONCEPT_UUID);
		SEVERITY_CONCEPT_UUIDS.put(FhirConstants.GLOBAL_PROPERTY_MODERATE, SEVERITY_MODERATE_CONCEPT_UUID);
		SEVERITY_CONCEPT_UUIDS.put(FhirConstants.GLOBAL_PROPERTY_SEVERE, SEVERITY_SEVERE_CONCEPT_UUID);
		SEVERITY_CONCEPT_UUIDS.put(FhirConstants.GLOBAL_PROPERTY_OTHER, SEVERITY_NULL_CONCEPT_UUID);
	}
	
	private List<IBaseResource> get(IBundleProvider results) {
		return results.getResources(START_INDEX, END_INDEX);
	}
	
	private IBundleProvider search(SearchParameterMap theParams) {
		return searchQuery.getQueryResults(theParams, allergyDao, translator, searchQueryInclude);
	}
	
	@Test
	public void searchForAllergies_shouldSearchForAllergiesByIdentifier() {
		ReferenceAndListParam referenceParam = new ReferenceAndListParam();
		ReferenceParam allergyParam = new ReferenceParam();
		
		allergyParam.setValue(PATIENT_IDENTIFIER);
		allergyParam.setChain(Patient.SP_IDENTIFIER);
		
		referenceParam.addValue(new ReferenceOrListParam().add(allergyParam));
		
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.PATIENT_REFERENCE_SEARCH_HANDLER,
		    referenceParam);
		
		IBundleProvider results = search(theParams);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList, hasSize(greaterThanOrEqualTo(1)));
		assertThat(((AllergyIntolerance) resultList.iterator().next()).getPatient().getReference(), endsWith(PATIENT_UUID));
	}
	
	@Test
	public void searchForAllergies_shouldSearchForAllergiesByMultiplePatientIdentifierOr() {
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
		assertThat(((AllergyIntolerance) resultList.iterator().next()).getPatient().getReference(), endsWith(PATIENT_UUID));
	}
	
	@Test
	public void searchForAllergies_shouldReturnEmptyListOfAllergiesByMultiplePatientIdentifierAnd() {
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
	public void searchForAllergies_shouldSearchForAllergiesByPatientUUID() {
		ReferenceAndListParam referenceParam = new ReferenceAndListParam();
		ReferenceParam allergyParam = new ReferenceParam();
		
		allergyParam.setValue(PATIENT_UUID);
		allergyParam.setChain(null);
		
		referenceParam.addValue(new ReferenceOrListParam().add(allergyParam));
		
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.PATIENT_REFERENCE_SEARCH_HANDLER,
		    referenceParam);
		
		IBundleProvider results = search(theParams);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList, hasSize(greaterThanOrEqualTo(1)));
		assertThat(((AllergyIntolerance) resultList.iterator().next()).getPatient().getReferenceElement().getIdPart(),
		    equalTo(PATIENT_UUID));
	}
	
	@Test
	public void searchForAllergies_shouldSearchForAllergiesByMultiplePatientUuidOr() {
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
		assertThat(((AllergyIntolerance) resultList.iterator().next()).getPatient().getReferenceElement().getIdPart(),
		    equalTo(PATIENT_UUID));
	}
	
	@Test
	public void searchForAllergies_shouldReturnEmptyListOfAllergiesByMultiplePatientUuidAnd() {
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
	public void searchForAllergies_shouldSearchForAllergiesByPatientGivenName() {
		ReferenceAndListParam referenceParam = new ReferenceAndListParam();
		ReferenceParam allergyParam = new ReferenceParam();
		
		allergyParam.setValue(PATIENT_GIVEN_NAME);
		allergyParam.setChain(Patient.SP_GIVEN);
		
		referenceParam.addValue(new ReferenceOrListParam().add(allergyParam));
		
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.PATIENT_REFERENCE_SEARCH_HANDLER,
		    referenceParam);
		
		IBundleProvider results = search(theParams);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList, hasSize(greaterThanOrEqualTo(1)));
		assertThat(((AllergyIntolerance) resultList.iterator().next()).getIdElement().getIdPart(), equalTo(ALLERGY_UUID));
	}
	
	@Test
	public void searchForAllergies_shouldSearchForAllergiesByMultiplePatientGivenNameOr() {
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
		assertThat(resultList, hasSize(greaterThanOrEqualTo(1)));
	}
	
	@Test
	public void searchForAllergies_shouldReturnEmptyListOfAllergiesByMultiplePatientGivenNameAnd() {
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
	public void searchForAllergies_shouldSearchForAllergiesByPatientFamilyName() {
		ReferenceAndListParam referenceParam = new ReferenceAndListParam();
		ReferenceParam allergyParam = new ReferenceParam();
		
		allergyParam.setValue(PATIENT_FAMILY_NAME);
		allergyParam.setChain(Patient.SP_FAMILY);
		
		referenceParam.addValue(new ReferenceOrListParam().add(allergyParam));
		
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.PATIENT_REFERENCE_SEARCH_HANDLER,
		    referenceParam);
		
		IBundleProvider results = search(theParams);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList, hasSize(greaterThanOrEqualTo(1)));
		assertThat(((AllergyIntolerance) resultList.iterator().next()).getIdElement().getIdPart(), equalTo(ALLERGY_UUID));
	}
	
	@Test
	public void searchForAllergies_shouldSearchForAllergiesByMultiplePatientFamilyNameOr() {
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
	}
	
	@Test
	public void searchForAllergies_shouldReturnEmptyListOfAllergiesByMultiplePatientFamilyNameAnd() {
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
	public void searchForAllergies_shouldSearchForAllergiesByPatientName() {
		ReferenceAndListParam referenceParam = new ReferenceAndListParam();
		ReferenceParam allergyParam = new ReferenceParam();
		
		allergyParam.setValue(PATIENT_NAME);
		allergyParam.setChain(Patient.SP_NAME);
		
		referenceParam.addValue(new ReferenceOrListParam().add(allergyParam));
		
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.PATIENT_REFERENCE_SEARCH_HANDLER,
		    referenceParam);
		
		IBundleProvider results = search(theParams);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList, hasSize(greaterThanOrEqualTo(1)));
		assertThat(((AllergyIntolerance) resultList.iterator().next()).getIdElement().getIdPart(), equalTo(ALLERGY_UUID));
	}
	
	@Test
	public void searchForAllergies_shouldSearchForAllergiesByMultiplePatientNameOr() {
		ReferenceAndListParam referenceParam = new ReferenceAndListParam();
		ReferenceParam patient = new ReferenceParam();
		
		patient.setValue(PATIENT_NAME);
		patient.setChain(Patient.SP_NAME);
		
		ReferenceParam badPatient = new ReferenceParam();
		
		badPatient.setValue(PATIENT_WRONG_NAME);
		badPatient.setChain(Patient.SP_NAME);
		
		referenceParam.addValue(new ReferenceOrListParam().add(patient).add(badPatient));
		
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.PATIENT_REFERENCE_SEARCH_HANDLER,
		    referenceParam);
		IBundleProvider results = search(theParams);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList, not(empty()));
		assertThat(resultList, hasSize(greaterThanOrEqualTo(1)));
	}
	
	@Test
	public void searchForAllergies_shouldReturnEmptyListOfAllergiesByMultiplePatientNameAnd() {
		ReferenceAndListParam referenceParam = new ReferenceAndListParam();
		ReferenceParam patient = new ReferenceParam();
		
		patient.setValue(PATIENT_NAME);
		patient.setChain(Patient.SP_NAME);
		
		ReferenceParam badPatient = new ReferenceParam();
		
		badPatient.setValue(PATIENT_WRONG_NAME);
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
	public void searchForAllergies_shouldSearchForAllergiesByPatientFamilyNameAndGivenName() {
		ReferenceAndListParam referenceParam = new ReferenceAndListParam();
		ReferenceParam allergyParamName = new ReferenceParam();
		ReferenceParam allergyParamGiven = new ReferenceParam();
		
		allergyParamName.setValue(PATIENT_FAMILY_NAME);
		allergyParamName.setChain(Patient.SP_FAMILY);
		
		allergyParamGiven.setValue(PATIENT_GIVEN_NAME);
		allergyParamGiven.setChain(Patient.SP_GIVEN);
		
		referenceParam.addValue(new ReferenceOrListParam().add(allergyParamName))
		        .addAnd(new ReferenceOrListParam().add(allergyParamGiven));
		
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.PATIENT_REFERENCE_SEARCH_HANDLER,
		    referenceParam);
		
		IBundleProvider results = search(theParams);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList, hasSize(greaterThanOrEqualTo(1)));
		assertThat(((AllergyIntolerance) resultList.iterator().next()).getIdElement().getIdPart(), equalTo(ALLERGY_UUID));
	}
	
	@Test
	public void searchForAllergies_shouldSearchForAllergiesByCategoryFood() {
		TokenAndListParam category = new TokenAndListParam();
		category.addAnd(new TokenOrListParam().addOr(new TokenParam().setValue(CATEGORY_FOOD)));
		
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.CATEGORY_SEARCH_HANDLER,
		    category);
		
		IBundleProvider results = search(theParams);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList, hasSize(greaterThanOrEqualTo(1)));
		assertThat(((AllergyIntolerance) resultList.iterator().next()).getCategory().get(0).getValue(),
		    equalTo(AllergyIntolerance.AllergyIntoleranceCategory.FOOD));
	}
	
	@Test
	public void searchForAllergies_shouldSearchForAllergiesByCategoryMedicine() {
		TokenAndListParam category = new TokenAndListParam();
		category.addAnd(new TokenOrListParam().addOr(new TokenParam().setValue(CATEGORY_MEDICATION)));
		
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.CATEGORY_SEARCH_HANDLER,
		    category);
		
		IBundleProvider results = search(theParams);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList, hasSize(greaterThanOrEqualTo(1)));
		assertThat(((AllergyIntolerance) resultList.iterator().next()).getCategory().get(0).getValue(),
		    equalTo(AllergyIntolerance.AllergyIntoleranceCategory.MEDICATION));
		
	}
	
	@Test
	public void searchForAllergies_shouldSearchForAllergiesByCategoryEnvironment() {
		TokenAndListParam category = new TokenAndListParam();
		category.addAnd(new TokenOrListParam().addOr(new TokenParam().setValue(CATEGORY_ENVIRONMENT)));
		
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.CATEGORY_SEARCH_HANDLER,
		    category);
		
		IBundleProvider results = search(theParams);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList, hasSize(greaterThanOrEqualTo(1)));
		assertThat(((AllergyIntolerance) resultList.iterator().next()).getCategory().get(0).getValue(),
		    equalTo(AllergyIntolerance.AllergyIntoleranceCategory.ENVIRONMENT));
		
	}
	
	@Test
	public void searchForAllergies_shouldSearchForMultipleAllergiesByCategory() {
		TokenAndListParam category = new TokenAndListParam();
		category.addAnd(new TokenOrListParam().addOr(new TokenParam().setValue(CATEGORY_FOOD))
		        .addOr(new TokenParam().setValue(CATEGORY_MEDICATION)));
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.CATEGORY_SEARCH_HANDLER,
		    category);
		
		IBundleProvider results = search(theParams);
		
		List<AllergyIntolerance> resultList = get(results).stream().map(p -> (AllergyIntolerance) p)
		        .collect(Collectors.toList());
		
		assertThat(results, notNullValue());
		assertThat(resultList.size(), equalTo(2));
		assertThat(resultList, hasItem(hasProperty("category",
		    hasItem(hasProperty("value", equalTo(AllergyIntolerance.AllergyIntoleranceCategory.FOOD))))));
		assertThat(resultList, hasItem(hasProperty("category",
		    hasItem(hasProperty("value", equalTo(AllergyIntolerance.AllergyIntoleranceCategory.MEDICATION))))));
	}
	
	@Test
	public void searchForAllergies_shouldSearchForAllergiesByAllergen() {
		TokenAndListParam allergen = new TokenAndListParam();
		allergen.addAnd(new TokenOrListParam().addOr(new TokenParam().setValue(CODED_ALLERGEN_UUID)));
		
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.ALLERGEN_SEARCH_HANDLER,
		    allergen);
		
		IBundleProvider results = search(theParams);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList, hasSize(greaterThanOrEqualTo(1)));
		assertThat(((AllergyIntolerance) resultList.iterator().next()).getCode().getCodingFirstRep().getCode(),
		    equalTo(CODED_ALLERGEN_UUID));
	}
	
	@Test
	public void searchForAllergies_shouldSearchForAllergiesBySeverityMild() {
		TokenAndListParam severity = new TokenAndListParam();
		severity.addAnd(new TokenOrListParam().addOr(new TokenParam().setValue(SEVERITY_MILD)));
		
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.SEVERITY_SEARCH_HANDLER,
		    severity);
		
		IBundleProvider results = search(theParams);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList, hasSize(greaterThanOrEqualTo(1)));
		assertThat(((AllergyIntolerance) resultList.iterator().next()).getReactionFirstRep().getSeverity(),
		    equalTo(AllergyIntolerance.AllergyIntoleranceSeverity.MILD));
	}
	
	@Test
	public void searchForAllergies_shouldSearchForAllergiesBySeverityModerate() {
		TokenAndListParam severity = new TokenAndListParam();
		severity.addAnd(new TokenOrListParam().addOr(new TokenParam().setValue(SEVERITY_MODERATE)));
		
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.SEVERITY_SEARCH_HANDLER,
		    severity);
		
		IBundleProvider results = search(theParams);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList, hasSize(greaterThanOrEqualTo(1)));
		assertThat(((AllergyIntolerance) resultList.iterator().next()).getReactionFirstRep().getSeverity(),
		    equalTo(AllergyIntolerance.AllergyIntoleranceSeverity.MODERATE));
	}
	
	@Test
	public void searchForAllergies_shouldSearchForAllergiesBySeveritySevere() {
		TokenAndListParam severity = new TokenAndListParam();
		severity.addAnd(new TokenOrListParam().addOr(new TokenParam().setValue(SEVERITY_SEVERE)));
		
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.SEVERITY_SEARCH_HANDLER,
		    severity);
		
		IBundleProvider results = search(theParams);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList, hasSize(greaterThanOrEqualTo(1)));
		assertThat(((AllergyIntolerance) resultList.iterator().next()).getReactionFirstRep().getSeverity(),
		    equalTo(AllergyIntolerance.AllergyIntoleranceSeverity.SEVERE));
	}
	
	@Test
	public void searchForAllergies_shouldSearchForAllergiesByManifestation() {
		TokenAndListParam manifestation = new TokenAndListParam();
		manifestation.addAnd(new TokenOrListParam().addOr(new TokenParam().setValue(CODED_REACTION_UUID)));
		
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.CODED_SEARCH_HANDLER,
		    manifestation);
		
		IBundleProvider results = search(theParams);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList, hasSize(greaterThanOrEqualTo(1)));
		assertThat(((AllergyIntolerance) resultList.iterator().next()).getReactionFirstRep().getManifestation(),
		    hasItem(hasProperty("coding", hasItem(hasProperty("code", equalTo(CODED_REACTION_UUID))))));
	}
	
	@Test
	public void searchForAllergies_shouldSearchForAllergiesByClinicalStatus() {
		TokenAndListParam status = new TokenAndListParam();
		status.addAnd(new TokenOrListParam().addOr(new TokenParam().setValue(STATUS)));
		
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.BOOLEAN_SEARCH_HANDLER, status);
		
		IBundleProvider results = search(theParams);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList, hasSize(greaterThanOrEqualTo(1)));
		assertThat(((AllergyIntolerance) resultList.iterator().next()).getClinicalStatus().getCodingFirstRep().getCode(),
		    equalTo(STATUS));
	}
	
	@Test
	public void searchForAllergies_shouldSearchForAllergiesByUuid() {
		TokenAndListParam uuid = new TokenAndListParam().addAnd(new TokenParam(ALLERGY_UUID));
		
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.COMMON_SEARCH_HANDLER,
		    FhirConstants.ID_PROPERTY, uuid);
		
		IBundleProvider results = search(theParams);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList, not(empty()));
		assertThat(resultList, hasSize(equalTo(1)));
		assertThat(((AllergyIntolerance) resultList.iterator().next()).getIdElement().getIdPart(), equalTo(ALLERGY_UUID));
	}
	
	@Test
	public void searchForAllergies_shouldSearchForAllergiesByLastUpdatedDateCreated() {
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
	public void searchForAllergies_shouldSearchForAllergiesByLastUpdatedDateChanged() {
		DateRangeParam lastUpdated = new DateRangeParam().setUpperBound(DATE_CHANGED).setLowerBound(DATE_CHANGED);
		
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.COMMON_SEARCH_HANDLER,
		    FhirConstants.LAST_UPDATED_PROPERTY, lastUpdated);
		
		IBundleProvider results = search(theParams);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList, not(empty()));
		assertThat(resultList, hasSize(equalTo(1)));
	}
	
	@Test
	public void searchForAllergies_shouldSearchForAllergiesByMatchingUuidAndLastUpdated() {
		TokenAndListParam uuid = new TokenAndListParam().addAnd(new TokenParam(ALLERGY_UUID));
		DateRangeParam lastUpdated = new DateRangeParam().setUpperBound(DATE_CREATED).setLowerBound(DATE_CREATED);
		
		SearchParameterMap theParams = new SearchParameterMap()
		        .addParameter(FhirConstants.COMMON_SEARCH_HANDLER, FhirConstants.ID_PROPERTY, uuid)
		        .addParameter(FhirConstants.COMMON_SEARCH_HANDLER, FhirConstants.LAST_UPDATED_PROPERTY, lastUpdated);
		
		IBundleProvider results = search(theParams);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList, not(empty()));
		assertThat(resultList, hasSize(equalTo(1)));
		assertThat(((AllergyIntolerance) resultList.iterator().next()).getIdElement().getIdPart(), equalTo(ALLERGY_UUID));
	}
	
	@Test
	public void searchForAllergies_shouldReturnEmptyListByMismatchingUuidAndLastUpdated() {
		TokenAndListParam uuid = new TokenAndListParam().addAnd(new TokenParam(ALLERGY_UUID));
		DateRangeParam lastUpdated = new DateRangeParam().setUpperBound(DATE_VOIDED).setLowerBound(DATE_VOIDED);
		
		SearchParameterMap theParams = new SearchParameterMap()
		        .addParameter(FhirConstants.COMMON_SEARCH_HANDLER, FhirConstants.ID_PROPERTY, uuid)
		        .addParameter(FhirConstants.COMMON_SEARCH_HANDLER, FhirConstants.LAST_UPDATED_PROPERTY, lastUpdated);
		
		IBundleProvider results = search(theParams);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList, empty());
	}
	
	@Test
	public void searchForAllergies_shouldAddNotNullPatientToReturnedResults() {
		TokenAndListParam uuid = new TokenAndListParam().addAnd(new TokenParam(ALLERGY_UUID));
		HashSet<Include> includes = new HashSet<>();
		includes.add(new Include("AllergyIntolerance:patient"));
		
		SearchParameterMap theParams = new SearchParameterMap()
		        .addParameter(FhirConstants.COMMON_SEARCH_HANDLER, FhirConstants.ID_PROPERTY, uuid)
		        .addParameter(FhirConstants.INCLUDE_SEARCH_HANDLER, includes);
		
		IBundleProvider results = search(theParams);
		assertThat(results.size(), equalTo(1));
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList, not(empty()));
		assertThat(resultList.size(), equalTo(2)); // included resource added as part of the result list
		
		AllergyIntolerance returnedAllergy = (AllergyIntolerance) resultList.iterator().next();
		assertThat(resultList, hasItem(allOf(is(instanceOf(Patient.class)),
		    hasProperty("id", equalTo(returnedAllergy.getPatient().getReferenceElement().getIdPart())))));
	}
	
	@Test
	public void searchForAllergies_shouldHandleComplexQuery() {
		ReferenceAndListParam referenceParam = new ReferenceAndListParam();
		ReferenceParam allergyParam = new ReferenceParam();
		allergyParam.setValue(PATIENT_UUID);
		allergyParam.setChain(null);
		referenceParam.addValue(new ReferenceOrListParam().add(allergyParam));
		
		TokenAndListParam status = new TokenAndListParam();
		status.addAnd(new TokenOrListParam().addOr(new TokenParam().setValue(STATUS)));
		
		TokenAndListParam category = new TokenAndListParam();
		category.addAnd(new TokenOrListParam().addOr(new TokenParam().setValue(CATEGORY_ENVIRONMENT)));
		
		SearchParameterMap theParams = new SearchParameterMap()
		        .addParameter(FhirConstants.PATIENT_REFERENCE_SEARCH_HANDLER, referenceParam)
		        .addParameter(FhirConstants.BOOLEAN_SEARCH_HANDLER, status)
		        .addParameter(FhirConstants.CATEGORY_SEARCH_HANDLER, category);
		
		IBundleProvider results = search(theParams);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList, hasSize(greaterThanOrEqualTo(1)));
		assertThat(((AllergyIntolerance) resultList.iterator().next()).getClinicalStatus().getCodingFirstRep().getCode(),
		    equalTo(STATUS));
		assertThat(((AllergyIntolerance) resultList.iterator().next()).getCategory().get(0).getValue(),
		    equalTo(AllergyIntolerance.AllergyIntoleranceCategory.ENVIRONMENT));
		assertThat(((AllergyIntolerance) resultList.iterator().next()).getPatient().getReferenceElement().getIdPart(),
		    equalTo(PATIENT_UUID));
	}
	
	@Test
	public void shouldReturnCollectionOfAllergiesSortedBySeveritySevere() {
		
		SortSpec sort = new SortSpec();
		sort.setParamName(AllergyIntolerance.SP_SEVERITY);
		sort.setOrder(SortOrderEnum.ASC);
		
		IBundleProvider results = search(new SearchParameterMap().addParameter(FhirConstants.SEVERITY_SEARCH_HANDLER,
		    new TokenAndListParam().addAnd(new TokenParam(SEVERITY_SEVERE))).setSortSpec(sort));
		
		List<AllergyIntolerance> fullResults = get(results).stream().map(p -> (AllergyIntolerance) p)
		        .collect(Collectors.toList());
		for (int i = 1; i < fullResults.size(); i++) {
			
			assertThat((fullResults.get(i - 1)).getReactionFirstRep().getSeverity(),
			    lessThanOrEqualTo((fullResults.get(i)).getReactionFirstRep().getSeverity()));
		}
		
		sort.setOrder(SortOrderEnum.DESC);
		results = search(new SearchParameterMap().addParameter(FhirConstants.SEVERITY_SEARCH_HANDLER,
		    new TokenAndListParam().addAnd(new TokenParam(SEVERITY_SEVERE))).setSortSpec(sort));
		
		for (int i = 1; i < get(results).size(); i++) {
			
			assertThat((fullResults.get(i - 1)).getReactionFirstRep().getSeverity(),
			    greaterThanOrEqualTo((fullResults.get(i)).getReactionFirstRep().getSeverity()));
		}
	}
	
	@Test
	public void shouldReturnCollectionOfAllergiesSortedBySeverityMild() {
		
		SortSpec sort = new SortSpec();
		sort.setParamName(AllergyIntolerance.SP_SEVERITY);
		sort.setOrder(SortOrderEnum.ASC);
		
		IBundleProvider results = search(new SearchParameterMap().addParameter(FhirConstants.SEVERITY_SEARCH_HANDLER,
		    new TokenAndListParam().addAnd(new TokenParam(SEVERITY_MILD))).setSortSpec(sort));
		List<AllergyIntolerance> fullResults = get(results).stream().map(p -> (AllergyIntolerance) p)
		        .collect(Collectors.toList());
		
		for (int i = 1; i < fullResults.size(); i++) {
			
			assertThat((fullResults.get(i - 1)).getReactionFirstRep().getSeverity(),
			    lessThanOrEqualTo((fullResults.get(i)).getReactionFirstRep().getSeverity()));
		}
		
		sort.setOrder(SortOrderEnum.DESC);
		
		results = search(new SearchParameterMap().addParameter(FhirConstants.SEVERITY_SEARCH_HANDLER,
		    new TokenAndListParam().addAnd(new TokenParam(SEVERITY_MILD))).setSortSpec(sort));
		
		for (int i = 1; i < get(results).size(); i++) {
			
			assertThat((fullResults.get(i - 1)).getReactionFirstRep().getSeverity(),
			    greaterThanOrEqualTo((fullResults.get(i)).getReactionFirstRep().getSeverity()));
		}
	}
	
	@Test
	public void shouldReturnCollectionOfAllergiesSortedBySeverityModerate() {
		
		SortSpec sort = new SortSpec();
		sort.setParamName(AllergyIntolerance.SP_SEVERITY);
		sort.setOrder(SortOrderEnum.ASC);
		
		IBundleProvider results = search(new SearchParameterMap().addParameter(FhirConstants.SEVERITY_SEARCH_HANDLER,
		    new TokenAndListParam().addAnd(new TokenParam(SEVERITY_MODERATE))).setSortSpec(sort));
		List<AllergyIntolerance> fullResults = get(results).stream().map(p -> (AllergyIntolerance) p)
		        .collect(Collectors.toList());
		
		for (int i = 1; i < fullResults.size(); i++) {
			
			assertThat((fullResults.get(i - 1)).getReactionFirstRep().getSeverity(),
			    lessThanOrEqualTo((fullResults.get(i)).getReactionFirstRep().getSeverity()));
		}
		sort.setOrder(SortOrderEnum.DESC);
		
		results = search(new SearchParameterMap().addParameter(FhirConstants.SEVERITY_SEARCH_HANDLER,
		    new TokenAndListParam().addAnd(new TokenParam(SEVERITY_MODERATE))).setSortSpec(sort));
		
		for (int i = 1; i < get(results).size(); i++) {
			
			assertThat((fullResults.get(i - 1)).getReactionFirstRep().getSeverity(),
			    greaterThanOrEqualTo((fullResults.get(i)).getReactionFirstRep().getSeverity()));
		}
	}
	
}
