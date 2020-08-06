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
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;

import java.util.List;
import java.util.stream.Collectors;

import ca.uhn.fhir.rest.api.server.IBundleProvider;
import ca.uhn.fhir.rest.param.DateRangeParam;
import ca.uhn.fhir.rest.param.TokenAndListParam;
import ca.uhn.fhir.rest.param.TokenOrListParam;
import ca.uhn.fhir.rest.param.TokenParam;
import org.hl7.fhir.r4.model.Medication;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.Drug;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.TestFhirSpringConfiguration;
import org.openmrs.module.fhir2.api.dao.FhirMedicationDao;
import org.openmrs.module.fhir2.api.search.param.SearchParameterMap;
import org.openmrs.module.fhir2.api.translators.MedicationTranslator;
import org.openmrs.test.BaseModuleContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

@ContextConfiguration(classes = TestFhirSpringConfiguration.class, inheritLocations = false)
public class MedicationSearchQueryTest extends BaseModuleContextSensitiveTest {
	
	private static final String MEDICATION_INITIAL_DATA_XML = "org/openmrs/module/fhir2/api/dao/impl/FhirMedicationDaoImplTest_initial_data.xml";
	
	private static final String MEDICATION_UUID = "1087AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
	
	private static final String CONCEPT_UUID = "11716f9c-1434-4f8d-b9fc-9aa14c4d6129";
	
	private static final String WRONG_CONCEPT_UUID = "0f97e14e-gdsh-49ac-9255-b5126f8a5147";
	
	private static final String DOSAGE_FORM_UUID = "95312123-e0c2-466d-b6b1-cb6e990d0d65";
	
	private static final String WRONG_DOSAGE_FORM_UUID = "e10ffe54-5184-4efe-8960-cd565ds1cdf8";
	
	private static final String INGREDIENT_CODE_UUID = "d198bec0-d9c5-11e3-9c1a-0800200c9a66";
	
	private static final String WRONG_INGREDIENT_CODE_UUID = "d198bec0-d9c5-11e3-9c1a-dsh0200c9a66";
	
	private static final String DATE_CREATED = "2005-01-01";
	
	private static final String DATE_CHANGED = "2010-03-31";
	
	private static final String WRONG_DATE_CHANGED = "2012-05-01";
	
	private static final int START_INDEX = 0;
	
	private static final int END_INDEX = 10;
	
	@Autowired
	private FhirMedicationDao dao;
	
	@Autowired
	private MedicationTranslator translator;
	
	@Autowired
	private SearchQuery<Drug, Medication, FhirMedicationDao, MedicationTranslator> searchQuery;
	
	@Before
	public void setup() throws Exception {
		executeDataSet(MEDICATION_INITIAL_DATA_XML);
	}
	
	private IBundleProvider search(SearchParameterMap theParams) {
		return searchQuery.getQueryResults(theParams, dao, translator);
	}
	
	private List<Medication> get(IBundleProvider results) {
		return results.getResources(START_INDEX, END_INDEX).stream().filter(it -> it instanceof Medication)
		        .map(it -> (Medication) it).collect(Collectors.toList());
	}
	
	@Test
	public void searchForMedications_shouldSearchMedicationsByCode() {
		TokenAndListParam code = new TokenAndListParam();
		code.addAnd(new TokenOrListParam().addOr(new TokenParam().setValue(CONCEPT_UUID)));
		
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.CODED_SEARCH_HANDLER, code);
		
		IBundleProvider result = search(theParams);
		
		List<Medication> resultList = get(result);
		
		assertThat(result, notNullValue());
		assertThat(resultList, hasSize(greaterThanOrEqualTo(1)));
		assertThat(resultList.get(0).getCode().getCodingFirstRep().getCode(), equalTo(CONCEPT_UUID));
	}
	
	@Test
	public void searchForMedications_shouldSearchMedicationsByWrongCode() {
		TokenAndListParam code = new TokenAndListParam();
		code.addAnd(new TokenOrListParam().addOr(new TokenParam().setValue(WRONG_CONCEPT_UUID)));
		
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.CODED_SEARCH_HANDLER, code);
		
		IBundleProvider result = search(theParams);
		
		List<Medication> resultList = get(result);
		
		assertThat(result, notNullValue());
		assertThat(resultList, empty());
	}
	
	@Test
	public void searchForMedications_shouldSearchMedicationsByDosageForm() {
		TokenAndListParam dosageForm = new TokenAndListParam();
		dosageForm.addAnd(new TokenOrListParam().addOr(new TokenParam().setValue(DOSAGE_FORM_UUID)));
		
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.DOSAGE_FORM_SEARCH_HANDLER,
		    dosageForm);
		
		IBundleProvider result = search(theParams);
		
		assertThat(result, notNullValue());
		
		List<Medication> resultList = get(result);
		
		assertThat(resultList, hasSize(greaterThanOrEqualTo(1)));
		assertThat(resultList.get(0).getForm().getCodingFirstRep().getCode(), equalTo(DOSAGE_FORM_UUID));
	}
	
	@Test
	public void searchForMedications_shouldSearchMedicationsByWrongDosageForm() {
		TokenAndListParam dosageForm = new TokenAndListParam();
		dosageForm.addAnd(new TokenOrListParam().addOr(new TokenParam().setValue(WRONG_DOSAGE_FORM_UUID)));
		
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.DOSAGE_FORM_SEARCH_HANDLER,
		    dosageForm);
		
		IBundleProvider result = search(theParams);
		
		List<Medication> resultList = get(result);
		
		assertThat(result, notNullValue());
		assertThat(resultList, empty());
	}
	
	@Test
	public void searchForMedications_shouldSearchMedicationsByIngredientCode() {
		TokenAndListParam ingredientCode = new TokenAndListParam();
		ingredientCode.addAnd(new TokenOrListParam().addOr(new TokenParam().setValue(INGREDIENT_CODE_UUID)));
		
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.INGREDIENT_SEARCH_HANDLER,
		    ingredientCode);
		
		IBundleProvider result = search(theParams);
		
		List<Medication> resultList = get(result);
		
		assertThat(result, notNullValue());
		assertThat(resultList, hasSize(greaterThanOrEqualTo(1)));
		assertThat(resultList.get(0).getIngredientFirstRep().getItemCodeableConcept().getCodingFirstRep().getCode(),
		    equalTo(INGREDIENT_CODE_UUID));
	}
	
	@Test
	public void searchForMedications_shouldSearchMedicationsByWrongIngredientCode() {
		TokenAndListParam ingredientCode = new TokenAndListParam();
		ingredientCode.addAnd(new TokenOrListParam().addOr(new TokenParam().setValue(WRONG_INGREDIENT_CODE_UUID)));
		
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.INGREDIENT_SEARCH_HANDLER,
		    ingredientCode);
		
		IBundleProvider result = search(theParams);
		
		assertThat(result, notNullValue());
		assertThat(result.size(), equalTo(0));
		
		List<Medication> resultList = get(result);
		
		assertThat(resultList, empty());
	}
	
	@Test
	public void searchForMedications_shouldSearchForMedicationsByUuid() {
		TokenAndListParam uuid = new TokenAndListParam().addAnd(new TokenParam(MEDICATION_UUID));
		
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.COMMON_SEARCH_HANDLER,
		    FhirConstants.ID_PROPERTY, uuid);
		
		IBundleProvider result = search(theParams);
		
		assertThat(result, notNullValue());
		
		List<Medication> resultList = get(result);
		
		assertThat(resultList, hasSize(equalTo(1)));
		assertThat(resultList.get(0).getIdElement().getIdPart(), equalTo(MEDICATION_UUID));
	}
	
	@Test
	public void searchForMedications_shouldSearchForMedicationsByLastUpdatedDateCreated() {
		DateRangeParam lastUpdated = new DateRangeParam().setUpperBound(DATE_CREATED).setLowerBound(DATE_CREATED);
		
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.COMMON_SEARCH_HANDLER,
		    FhirConstants.LAST_UPDATED_PROPERTY, lastUpdated);
		
		IBundleProvider results = search(theParams);
		
		assertThat(results, notNullValue());
		assertThat(results.size(), equalTo(1));
	}
	
	@Test
	public void searchForMedications_shouldSearchForMedicationsByLastUpdatedDateChanged() {
		DateRangeParam lastUpdated = new DateRangeParam().setUpperBound(DATE_CHANGED).setLowerBound(DATE_CHANGED);
		
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.COMMON_SEARCH_HANDLER,
		    FhirConstants.LAST_UPDATED_PROPERTY, lastUpdated);
		
		IBundleProvider results = search(theParams);
		
		assertThat(results, notNullValue());
		assertThat(results.size(), equalTo(1));
	}
	
	@Test
	public void searchForMedications_shouldSearchForMedicationsByMatchingUuidAndLastUpdated() {
		TokenAndListParam uuid = new TokenAndListParam().addAnd(new TokenParam(MEDICATION_UUID));
		DateRangeParam lastUpdated = new DateRangeParam().setUpperBound(DATE_CHANGED).setLowerBound(DATE_CHANGED);
		
		SearchParameterMap theParams = new SearchParameterMap()
		        .addParameter(FhirConstants.COMMON_SEARCH_HANDLER, FhirConstants.ID_PROPERTY, uuid)
		        .addParameter(FhirConstants.COMMON_SEARCH_HANDLER, FhirConstants.LAST_UPDATED_PROPERTY, lastUpdated);
		
		IBundleProvider result = search(theParams);
		
		assertThat(result, notNullValue());
		assertThat(result.size(), equalTo(1));
		
		List<Medication> resultList = get(result);
		
		assertThat(resultList, hasSize(equalTo(1)));
		assertThat(resultList.get(0).getIdElement().getIdPart(), equalTo(MEDICATION_UUID));
	}
	
	@Test
	public void searchForMedications_shouldReturnEmptyListByMismatchingUuidAndLastUpdated() {
		TokenAndListParam uuid = new TokenAndListParam().addAnd(new TokenParam(MEDICATION_UUID));
		DateRangeParam lastUpdated = new DateRangeParam().setUpperBound(WRONG_DATE_CHANGED)
		        .setLowerBound(WRONG_DATE_CHANGED);
		
		SearchParameterMap theParams = new SearchParameterMap()
		        .addParameter(FhirConstants.COMMON_SEARCH_HANDLER, FhirConstants.ID_PROPERTY, uuid)
		        .addParameter(FhirConstants.COMMON_SEARCH_HANDLER, FhirConstants.LAST_UPDATED_PROPERTY, lastUpdated);
		
		IBundleProvider results = search(theParams);
		
		assertThat(results, notNullValue());
		assertThat(results.size(), equalTo(0));
		
		List<Medication> resultList = get(results);
		
		assertThat(resultList, empty());
	}
	
	@Test
	public void searchForMedications_shouldHandleComplexQuery() {
		TokenAndListParam code = new TokenAndListParam();
		code.addAnd(new TokenOrListParam().addOr(new TokenParam().setValue(CONCEPT_UUID)));
		
		TokenAndListParam dosageForm = new TokenAndListParam();
		dosageForm.addAnd(new TokenOrListParam().addOr(new TokenParam().setValue(DOSAGE_FORM_UUID)));
		
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.CODED_SEARCH_HANDLER, code)
		        .addParameter(FhirConstants.DOSAGE_FORM_SEARCH_HANDLER, dosageForm);
		
		IBundleProvider result = search(theParams);
		
		assertThat(result, notNullValue());
		assertThat(result.size(), greaterThanOrEqualTo(1));
		
		List<Medication> resultList = get(result);
		
		assertThat(resultList, hasSize(greaterThanOrEqualTo(1)));
		assertThat(resultList.get(0).getCode().getCodingFirstRep().getCode(), equalTo(CONCEPT_UUID));
		assertThat(resultList.get(0).getForm().getCodingFirstRep().getCode(), equalTo(DOSAGE_FORM_UUID));
	}
	
}
