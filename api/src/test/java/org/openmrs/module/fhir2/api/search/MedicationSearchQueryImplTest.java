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

import java.util.List;

import ca.uhn.fhir.rest.api.server.IBundleProvider;
import ca.uhn.fhir.rest.param.TokenAndListParam;
import ca.uhn.fhir.rest.param.TokenOrListParam;
import ca.uhn.fhir.rest.param.TokenParam;
import org.hl7.fhir.instance.model.api.IBaseResource;
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
public class MedicationSearchQueryImplTest extends BaseModuleContextSensitiveTest {
	
	private static final String CONCEPT_UUID = "0f97e14e-cdc2-49ac-9255-b5126f8a5147";
	
	private static final String WRONG_CONCEPT_UUID = "0f97e14e-gdsh-49ac-9255-b5126f8a5147";
	
	private static final String DOSAGE_FORM_UUID = "e10ffe54-5184-4efe-8960-cd565ec1cdf8";
	
	private static final String WRONG_DOSAGE_FORM_UUID = "e10ffe54-5184-4efe-8960-cd565ds1cdf8";
	
	private static final String INGREDIENT_CODE_UUID = "d198bec0-d9c5-11e3-9c1a-0800200c9a66";
	
	private static final String WRONG_INGREDIENT_CODE_UUID = "d198bec0-d9c5-11e3-9c1a-dsh0200c9a66";
	
	private static final String MEDICATION_INITIAL_DATA_XML = "org/openmrs/module/fhir2/api/dao/impl/FhirMedicationDaoImplTest_initial_data.xml";
	
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
	
	private List<IBaseResource> get(IBundleProvider results) {
		return results.getResources(START_INDEX, END_INDEX);
	}
	
	@Test
	public void searchForMedications_shouldSearchMedicationsByCode() {
		TokenAndListParam code = new TokenAndListParam();
		code.addAnd(new TokenOrListParam().addOr(new TokenParam().setValue(CONCEPT_UUID)));
		
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.CODED_SEARCH_HANDLER, code);
		
		IBundleProvider result = search(theParams);
		
		List<IBaseResource> resultList = get(result);
		
		assertThat(result, notNullValue());
		assertThat(resultList.size(), greaterThanOrEqualTo(1));
		assertThat(((Medication) resultList.iterator().next()).getCode().getCodingFirstRep().getCode(),
		    equalTo(CONCEPT_UUID));
	}
	
	@Test
	public void searchForMedications_shouldSearchMedicationsByWrongCode() {
		TokenAndListParam code = new TokenAndListParam();
		code.addAnd(new TokenOrListParam().addOr(new TokenParam().setValue(WRONG_CONCEPT_UUID)));
		
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.CODED_SEARCH_HANDLER, code);
		
		IBundleProvider result = search(theParams);
		
		List<IBaseResource> resultList = get(result);
		
		assertThat(result, notNullValue());
		assertThat(resultList.size(), equalTo(0));
	}
	
	@Test
	public void searchForMedications_shouldSearchMedicationsByDosageForm() {
		TokenAndListParam dosageForm = new TokenAndListParam();
		dosageForm.addAnd(new TokenOrListParam().addOr(new TokenParam().setValue(DOSAGE_FORM_UUID)));
		
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.DOSAGE_FORM_SEARCH_HANDLER,
		    dosageForm);
		
		IBundleProvider result = search(theParams);
		
		List<IBaseResource> resultList = get(result);
		
		assertThat(result, notNullValue());
		assertThat(resultList.size(), greaterThanOrEqualTo(1));
		assertThat(((Medication) resultList.iterator().next()).getForm().getCodingFirstRep().getCode(),
		    equalTo(DOSAGE_FORM_UUID));
	}
	
	@Test
	public void searchForMedications_shouldSearchMedicationsByWrongDosageForm() {
		TokenAndListParam dosageForm = new TokenAndListParam();
		dosageForm.addAnd(new TokenOrListParam().addOr(new TokenParam().setValue(WRONG_DOSAGE_FORM_UUID)));
		
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.DOSAGE_FORM_SEARCH_HANDLER,
		    dosageForm);
		
		IBundleProvider result = search(theParams);
		
		List<IBaseResource> resultList = get(result);
		
		assertThat(result, notNullValue());
		assertThat(resultList.size(), equalTo(0));
	}
	
	@Test
	public void searchForMedications_shouldSearchMedicationsByIngredientCode() {
		TokenAndListParam ingredientCode = new TokenAndListParam();
		ingredientCode.addAnd(new TokenOrListParam().addOr(new TokenParam().setValue(INGREDIENT_CODE_UUID)));
		
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.INGREDIENT_SEARCH_HANDLER,
		    ingredientCode);
		
		IBundleProvider result = search(theParams);
		
		List<IBaseResource> resultList = get(result);
		
		assertThat(result, notNullValue());
		assertThat(resultList.size(), greaterThanOrEqualTo(1));
		assertThat(((Medication) resultList.iterator().next()).getIngredientFirstRep().getItemCodeableConcept()
		        .getCodingFirstRep().getCode(),
		    equalTo(INGREDIENT_CODE_UUID));
	}
	
	@Test
	public void searchForMedications_shouldSearchMedicationsByWrongIngredientCode() {
		TokenAndListParam ingredientCode = new TokenAndListParam();
		ingredientCode.addAnd(new TokenOrListParam().addOr(new TokenParam().setValue(WRONG_INGREDIENT_CODE_UUID)));
		
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.INGREDIENT_SEARCH_HANDLER,
		    ingredientCode);
		
		IBundleProvider result = search(theParams);
		
		List<IBaseResource> resultList = get(result);
		
		assertThat(result, notNullValue());
		assertThat(resultList.size(), equalTo(0));
	}
	
	@Test
	public void searchForMedications_shouldSearchMedicationsByActiveStatus() {
		TokenAndListParam status = new TokenAndListParam().addAnd(new TokenParam("active"));
		
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.BOOLEAN_SEARCH_HANDLER, status);
		
		IBundleProvider result = search(theParams);
		
		assertThat(result, notNullValue());
		assertThat(result.size(), equalTo(3));
	}
	
	@Test
	public void searchForMedications_shouldSearchMedicationsByInactiveStatus() {
		TokenAndListParam status = new TokenAndListParam().addAnd(new TokenParam("inactive"));
		
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.BOOLEAN_SEARCH_HANDLER, status);
		
		IBundleProvider result = search(theParams);
		
		assertThat(result, notNullValue());
		assertThat(result.size(), equalTo(3));
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
		
		List<IBaseResource> resultList = get(result);
		
		assertThat(result, notNullValue());
		assertThat(resultList.size(), greaterThanOrEqualTo(1));
		assertThat(((Medication) resultList.iterator().next()).getCode().getCodingFirstRep().getCode(),
		    equalTo(CONCEPT_UUID));
		assertThat(((Medication) resultList.iterator().next()).getForm().getCodingFirstRep().getCode(),
		    equalTo(DOSAGE_FORM_UUID));
	}
	
}
