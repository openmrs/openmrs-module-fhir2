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
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasProperty;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import ca.uhn.fhir.rest.api.server.IBundleProvider;
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
public class AllergyIntoleranceSearchQueryImplTest extends BaseModuleContextSensitiveTest {
	
	private static final String ALLERGY_INTOLERANCE_INITIAL_DATA_XML = "org/openmrs/module/fhir2/api/dao/impl/FhirAllergyIntoleranceDaoImplTest_initial_data.xml";
	
	private static final String ALLERGY_UUID = "1084AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
	
	private static final String CODED_ALLERGEN_UUID = "a09ab2c5-878e-4905-b25d-5784167d0216";
	
	private static final String SEVERITY_MILD_CONCEPT_UUID = "5088AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
	
	private static final String SEVERITY_MODERATE_CONCEPT_UUID = "5089AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
	
	private static final String SEVERITY_SEVERE_CONCEPT_UUID = "7088AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
	
	private static final String SEVERITY_NULL_CONCEPT_UUID = "8088AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
	
	private static final String CODED_REACTION_UUID = "5087AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
	
	private static final String PATIENT_UUID = "8adf539e-4b5a-47aa-80c0-ba1025c957fa";
	
	private static final String PATIENT_IDENTIFIER = "7TU-8";
	
	private static final String PATIENT_GIVEN_NAME = "Anet";
	
	private static final String PATIENT_FAMILY_NAME = "Oloo";
	
	private static final String PATIENT_NAME = "Anet Oloo";
	
	private static final String CATEGORY_FOOD = "food";
	
	private static final String CATEGORY_ENVIRONMENT = "environment";
	
	private static final String CATEGORY_MEDICATION = "medication";
	
	private static final String SEVERITY_MILD = "mild";
	
	private static final String SEVERITY_MODERATE = "moderate";
	
	private static final String SEVERITY_SEVERE = "severe";
	
	private static final String STATUS = "active";
	
	private static final int START_INDEX = 0;
	
	private static final int END_INDEX = 10;
	
	private static final Map<String, String> SEVERITY_CONCEPT_UUIDS = new HashMap<>();
	
	@Autowired
	private FhirAllergyIntoleranceDao allergyDao;
	
	@Autowired
	private AllergyIntoleranceTranslator translator;
	
	@Autowired
	private SearchQuery<Allergy, AllergyIntolerance, FhirAllergyIntoleranceDao, AllergyIntoleranceTranslator> searchQuery;
	
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
		return searchQuery.getQueryResults(theParams, allergyDao, translator);
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
		assertThat(resultList.size(), greaterThanOrEqualTo(1));
		assertThat(((AllergyIntolerance) resultList.iterator().next()).getPatient().getIdentifier().getValue(),
		    equalTo(PATIENT_IDENTIFIER));
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
		assertThat(resultList.size(), greaterThanOrEqualTo(1));
		assertThat(((AllergyIntolerance) resultList.iterator().next()).getPatient().getReferenceElement().getIdPart(),
		    equalTo(PATIENT_UUID));
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
		assertThat(resultList.size(), greaterThanOrEqualTo(1));
		assertThat(((AllergyIntolerance) resultList.iterator().next()).getIdElement().getIdPart(), equalTo(ALLERGY_UUID));
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
		assertThat(resultList.size(), greaterThanOrEqualTo(1));
		assertThat(((AllergyIntolerance) resultList.iterator().next()).getIdElement().getIdPart(), equalTo(ALLERGY_UUID));
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
		assertThat(resultList.size(), greaterThanOrEqualTo(1));
		assertThat(((AllergyIntolerance) resultList.iterator().next()).getIdElement().getIdPart(), equalTo(ALLERGY_UUID));
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
		
		referenceParam.addValue(new ReferenceOrListParam().add(allergyParamName).add(allergyParamGiven));
		
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.PATIENT_REFERENCE_SEARCH_HANDLER,
		    referenceParam);
		
		IBundleProvider results = search(theParams);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList.size(), greaterThanOrEqualTo(1));
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
		assertThat(resultList.size(), greaterThanOrEqualTo(1));
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
		assertThat(resultList.size(), greaterThanOrEqualTo(1));
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
		assertThat(resultList.size(), greaterThanOrEqualTo(1));
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
		assertThat(resultList.size(), greaterThanOrEqualTo(1));
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
		assertThat(resultList.size(), greaterThanOrEqualTo(1));
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
		assertThat(resultList.size(), greaterThanOrEqualTo(1));
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
		assertThat(resultList.size(), greaterThanOrEqualTo(1));
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
		assertThat(resultList.size(), greaterThanOrEqualTo(1));
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
		assertThat(resultList.size(), greaterThanOrEqualTo(1));
		assertThat(((AllergyIntolerance) resultList.iterator().next()).getClinicalStatus().getCodingFirstRep().getCode(),
		    equalTo(STATUS));
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
		assertThat(resultList.size(), greaterThanOrEqualTo(1));
		assertThat(((AllergyIntolerance) resultList.iterator().next()).getClinicalStatus().getCodingFirstRep().getCode(),
		    equalTo(STATUS));
		assertThat(((AllergyIntolerance) resultList.iterator().next()).getCategory().get(0).getValue(),
		    equalTo(AllergyIntolerance.AllergyIntoleranceCategory.ENVIRONMENT));
		assertThat(((AllergyIntolerance) resultList.iterator().next()).getPatient().getReferenceElement().getIdPart(),
		    equalTo(PATIENT_UUID));
	}
	
}
