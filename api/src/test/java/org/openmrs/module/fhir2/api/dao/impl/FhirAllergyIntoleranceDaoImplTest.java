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

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.when;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import ca.uhn.fhir.rest.param.ReferenceAndListParam;
import ca.uhn.fhir.rest.param.ReferenceOrListParam;
import ca.uhn.fhir.rest.param.ReferenceParam;
import ca.uhn.fhir.rest.param.TokenAndListParam;
import ca.uhn.fhir.rest.param.TokenOrListParam;
import ca.uhn.fhir.rest.param.TokenParam;
import org.hibernate.SessionFactory;
import org.hl7.fhir.r4.model.Patient;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.openmrs.AllergenType;
import org.openmrs.Allergy;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.TestFhirSpringConfiguration;
import org.openmrs.module.fhir2.api.FhirGlobalPropertyService;
import org.openmrs.test.BaseModuleContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;

@ContextConfiguration(classes = TestFhirSpringConfiguration.class, inheritLocations = false)
public class FhirAllergyIntoleranceDaoImplTest extends BaseModuleContextSensitiveTest {
	
	private static final String ALLERGY_INTOLERANCE_INITIAL_DATA_XML = "org/openmrs/module/fhir2/api/dao/impl/FhirAllergyIntoleranceDaoImplTest_initial_data.xml";
	
	private static final String ALLERGY_UUID = "1085AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
	
	private static final String UNKNOWN_ALLERGY_UUID = "9999AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
	
	private static final String CODED_ALLERGEN_UUID = "5085AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
	
	private static final String SEVERITY_MILD_CONCEPT_UUID = "5088AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
	
	private static final String SEVERITY_MODERATE_CONCEPT_UUID = "5089AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
	
	private static final String SEVERITY_SEVERE_CONCEPT_UUID = "7088AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
	
	private static final String SEVERITY_NULL_CONCEPT_UUID = "8088AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
	
	private static final String MODERATE_CONCEPT_UUID = "5089AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
	
	private static final String CODED_REACTION_UUID = "5087AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
	
	@Autowired
	@Qualifier("sessionFactory")
	private SessionFactory sessionFactory;
	
	@Mock
	private FhirGlobalPropertyService globalPropertyService;
	
	private FhirAllergyIntoleranceDaoImpl allergyDao;
	
	private Map<String, String> severityConceptUuids = new HashMap<>();
	
	@Before
	public void setup() throws Exception {
		allergyDao = new FhirAllergyIntoleranceDaoImpl();
		allergyDao.setSessionFactory(sessionFactory);
		allergyDao.setGlobalPropertyService(globalPropertyService);
		executeDataSet(ALLERGY_INTOLERANCE_INITIAL_DATA_XML);
	}
	
	public void initSeverityData() {
		severityConceptUuids.put(FhirConstants.GLOBAL_PROPERTY_MILD, SEVERITY_MILD_CONCEPT_UUID);
		severityConceptUuids.put(FhirConstants.GLOBAL_PROPERTY_MODERATE, SEVERITY_MODERATE_CONCEPT_UUID);
		severityConceptUuids.put(FhirConstants.GLOBAL_PROPERTY_SEVERE, SEVERITY_SEVERE_CONCEPT_UUID);
		severityConceptUuids.put(FhirConstants.GLOBAL_PROPERTY_OTHER, SEVERITY_NULL_CONCEPT_UUID);
	}
	
	@Test
	public void getAllergyIntoleranceByUuid_shouldGetByUuid() {
		Allergy allergy = allergyDao.getAllergyIntoleranceByUuid(ALLERGY_UUID);
		assertThat(allergy, notNullValue());
		assertThat(allergy.getUuid(), notNullValue());
		assertThat(allergy.getUuid(), equalTo(ALLERGY_UUID));
	}
	
	@Test
	public void getAllergyIntoleranceByUuid_shouldReturnNullWhenCalledWithUnknownUuid() {
		Allergy allergy = allergyDao.getAllergyIntoleranceByUuid(UNKNOWN_ALLERGY_UUID);
		assertThat(allergy, nullValue());
	}
	
	@Test
	public void searchForAllergies_shouldSearchForAllergiesByIdentifier() {
		ReferenceAndListParam referenceParam = new ReferenceAndListParam();
		ReferenceParam allergyParam = new ReferenceParam();
		
		allergyParam.setValue("M4001-1");
		allergyParam.setChain(Patient.SP_IDENTIFIER);
		
		referenceParam.addValue(new ReferenceOrListParam().add(allergyParam));
		
		Collection<Allergy> result = allergyDao.searchForAllergies(referenceParam, null, null, null, null, null);
		assertThat(result, notNullValue());
		assertThat(result.size(), greaterThanOrEqualTo(1));
		assertThat(result.iterator().next().getPatient().getIdentifiers().iterator().next().getIdentifier(),
		    equalTo("M4001-1"));
	}
	
	@Test
	public void searchForAllergies_shouldSearchForAllergiesByPatientGivenName() {
		ReferenceAndListParam referenceParam = new ReferenceAndListParam();
		ReferenceParam allergyParam = new ReferenceParam();
		
		allergyParam.setValue("John");
		allergyParam.setChain(Patient.SP_GIVEN);
		
		referenceParam.addValue(new ReferenceOrListParam().add(allergyParam));
		
		Collection<Allergy> result = allergyDao.searchForAllergies(referenceParam, null, null, null, null, null);
		assertThat(result, notNullValue());
		assertThat(result.size(), greaterThanOrEqualTo(1));
		assertThat(result.iterator().next().getPatient().getGivenName(), equalTo("John"));
	}
	
	@Test
	public void searchForAllergies_shouldSearchForAllergiesByPatientFamilyName() {
		ReferenceAndListParam referenceParam = new ReferenceAndListParam();
		ReferenceParam allergyParam = new ReferenceParam();
		
		allergyParam.setValue("Doe");
		allergyParam.setChain(Patient.SP_FAMILY);
		
		referenceParam.addValue(new ReferenceOrListParam().add(allergyParam));
		
		Collection<Allergy> result = allergyDao.searchForAllergies(referenceParam, null, null, null, null, null);
		assertThat(result, notNullValue());
		assertThat(result.size(), greaterThanOrEqualTo(1));
		assertThat(result.iterator().next().getPatient().getFamilyName(), equalTo("Doe"));
	}
	
	@Test
	public void searchForAllergies_shouldSearchForAllergiesByPatientName() {
		ReferenceAndListParam referenceParam = new ReferenceAndListParam();
		ReferenceParam allergyParam = new ReferenceParam();
		
		allergyParam.setValue("John Doe");
		allergyParam.setChain(Patient.SP_NAME);
		
		referenceParam.addValue(new ReferenceOrListParam().add(allergyParam));
		
		Collection<Allergy> result = allergyDao.searchForAllergies(referenceParam, null, null, null, null, null);
		assertThat(result, notNullValue());
		assertThat(result.size(), greaterThanOrEqualTo(1));
	}
	
	@Test
	public void searchForAllergies_shouldSearchForAllergiesByPatientFamilyNameAndGivenName() {
		ReferenceAndListParam referenceParam = new ReferenceAndListParam();
		ReferenceParam allergyParamName = new ReferenceParam();
		ReferenceParam allergyParamGiven = new ReferenceParam();
		
		allergyParamName.setValue("Doe");
		allergyParamName.setChain(Patient.SP_FAMILY);
		
		allergyParamGiven.setValue("John");
		allergyParamGiven.setChain(Patient.SP_GIVEN);
		
		referenceParam.addValue(new ReferenceOrListParam().add(allergyParamName).add(allergyParamGiven));
		
		Collection<Allergy> result = allergyDao.searchForAllergies(referenceParam, null, null, null, null, null);
		assertThat(result, notNullValue());
		assertThat(result.size(), greaterThanOrEqualTo(1));
		assertThat(result.iterator().next().getUuid(), equalTo(ALLERGY_UUID));
		assertThat(result.iterator().next().getPatient().getFamilyName(), equalTo("Doe"));
	}
	
	@Test
	public void searchForAllergies_shouldSearchForAllergiesByCategoryFood() {
		TokenOrListParam category = new TokenOrListParam();
		category.addOr(new TokenParam().setValue("food"));
		
		Collection<Allergy> result = allergyDao.searchForAllergies(null, category, null, null, null, null);
		assertThat(result, notNullValue());
		assertThat(result.size(), greaterThanOrEqualTo(1));
		assertThat(result.iterator().next().getAllergenType(), equalTo(AllergenType.FOOD));
	}
	
	@Test
	public void searchForAllergies_shouldSearchForAllergiesByCategoryMedicine() {
		TokenOrListParam category = new TokenOrListParam();
		category.addOr(new TokenParam().setValue("medication"));
		
		Collection<Allergy> result = allergyDao.searchForAllergies(null, category, null, null, null, null);
		assertThat(result, notNullValue());
		assertThat(result.size(), greaterThanOrEqualTo(1));
		assertThat(result.iterator().next().getAllergenType(), equalTo(AllergenType.DRUG));
	}
	
	@Test
	public void searchForAllergies_shouldSearchForAllergiesByCategoryEnvironment() {
		TokenOrListParam category = new TokenOrListParam();
		category.addOr(new TokenParam().setValue("environment"));
		
		Collection<Allergy> result = allergyDao.searchForAllergies(null, category, null, null, null, null);
		assertThat(result, notNullValue());
		assertThat(result.size(), greaterThanOrEqualTo(1));
		assertThat(result.iterator().next().getAllergenType(), equalTo(AllergenType.ENVIRONMENT));
	}
	
	@Test
	public void searchForAllergies_shouldSearchForAllergiesByCategoryOther() {
		TokenOrListParam category = new TokenOrListParam();
		category.addOr(new TokenParam().setValue("null"));
		
		Collection<Allergy> result = allergyDao.searchForAllergies(null, null, null, null, null, null);
		assertThat(result, notNullValue());
		assertThat(result.size(), greaterThanOrEqualTo(1));
	}
	
	@Test
	public void searchForAllergies_shouldSearchForMultipleAllergiesByCategory() {
		TokenOrListParam category = new TokenOrListParam();
		category.addOr(new TokenParam(null, "food")).addOr(new TokenParam(null, "medication"));
		Collection<Allergy> result = allergyDao.searchForAllergies(null, category, null, null, null, null);
		assertThat(result, notNullValue());
		assertThat(result, hasSize(2));
		assertThat(result, hasItem(hasProperty("allergenType", equalTo(AllergenType.FOOD))));
		assertThat(result, hasItem(hasProperty("allergenType", equalTo(AllergenType.DRUG))));
	}
	
	@Test
	public void searchForAllergies_shouldSearchForAllergiesByAllergen() {
		TokenAndListParam allergen = new TokenAndListParam();
		allergen.addAnd(new TokenOrListParam().addOr(new TokenParam().setValue(CODED_ALLERGEN_UUID)));
		
		Collection<Allergy> result = allergyDao.searchForAllergies(null, null, allergen, null, null, null);
		assertThat(result, notNullValue());
		assertThat(result.size(), greaterThanOrEqualTo(1));
		assertThat(result.iterator().next().getAllergen().getCodedAllergen().getUuid(), equalTo(CODED_ALLERGEN_UUID));
	}
	
	@Test
	public void searchForAllergies_shouldSearchForAllergiesBySeverityMild() {
		initSeverityData();
		
		TokenOrListParam severity = new TokenOrListParam();
		severity.addOr(new TokenParam().setValue("mild"));
		
		when(globalPropertyService.getGlobalProperties(FhirConstants.GLOBAL_PROPERTY_MILD,
		    FhirConstants.GLOBAL_PROPERTY_MODERATE, FhirConstants.GLOBAL_PROPERTY_SEVERE,
		    FhirConstants.GLOBAL_PROPERTY_OTHER)).thenReturn(severityConceptUuids);
		
		Collection<Allergy> result = allergyDao.searchForAllergies(null, null, null, severity, null, null);
		assertThat(result, notNullValue());
		assertThat(result.size(), greaterThanOrEqualTo(1));
		assertThat(result.iterator().next().getSeverity().getUuid(), equalTo(SEVERITY_MILD_CONCEPT_UUID));
	}
	
	@Test
	public void searchForAllergies_shouldSearchForAllergiesBySeverityModerate() {
		initSeverityData();
		
		TokenOrListParam severity = new TokenOrListParam();
		severity.addOr(new TokenParam().setValue("moderate"));
		
		when(globalPropertyService.getGlobalProperties(FhirConstants.GLOBAL_PROPERTY_MILD,
		    FhirConstants.GLOBAL_PROPERTY_MODERATE, FhirConstants.GLOBAL_PROPERTY_SEVERE,
		    FhirConstants.GLOBAL_PROPERTY_OTHER)).thenReturn(severityConceptUuids);
		
		Collection<Allergy> result = allergyDao.searchForAllergies(null, null, null, severity, null, null);
		assertThat(result, notNullValue());
		assertThat(result.size(), greaterThanOrEqualTo(1));
		assertThat(result.iterator().next().getSeverity().getUuid(), equalTo(SEVERITY_MODERATE_CONCEPT_UUID));
	}
	
	@Test
	public void searchForAllergies_shouldSearchForAllergiesBySeveritySevere() {
		initSeverityData();
		
		TokenOrListParam severity = new TokenOrListParam();
		severity.addOr(new TokenParam().setValue("severe"));
		
		when(globalPropertyService.getGlobalProperties(FhirConstants.GLOBAL_PROPERTY_MILD,
		    FhirConstants.GLOBAL_PROPERTY_MODERATE, FhirConstants.GLOBAL_PROPERTY_SEVERE,
		    FhirConstants.GLOBAL_PROPERTY_OTHER)).thenReturn(severityConceptUuids);
		
		Collection<Allergy> result = allergyDao.searchForAllergies(null, null, null, severity, null, null);
		assertThat(result, notNullValue());
		assertThat(result.size(), greaterThanOrEqualTo(1));
		assertThat(result.iterator().next().getSeverity().getUuid(), equalTo(SEVERITY_SEVERE_CONCEPT_UUID));
	}
	
	@Test
	public void searchForAllergies_shouldSearchForAllergiesBySeverityOther() {
		initSeverityData();
		
		TokenOrListParam severity = new TokenOrListParam();
		severity.addOr(new TokenParam().setValue("null"));
		
		when(globalPropertyService.getGlobalProperties(FhirConstants.GLOBAL_PROPERTY_MILD,
		    FhirConstants.GLOBAL_PROPERTY_MODERATE, FhirConstants.GLOBAL_PROPERTY_SEVERE,
		    FhirConstants.GLOBAL_PROPERTY_OTHER)).thenReturn(severityConceptUuids);
		
		Collection<Allergy> result = allergyDao.searchForAllergies(null, null, null, severity, null, null);
		assertThat(result, notNullValue());
		assertThat(result.size(), greaterThanOrEqualTo(1));
	}
	
	@Test
	public void searchForAllergies_shouldSearchForAllergiesByManifestation() {
		TokenAndListParam manifestation = new TokenAndListParam();
		manifestation.addAnd(new TokenOrListParam().addOr(new TokenParam().setValue(CODED_REACTION_UUID)));
		
		Collection<Allergy> result = allergyDao.searchForAllergies(null, null, null, null, manifestation, null);
		assertThat(result, notNullValue());
		assertThat(result.size(), greaterThanOrEqualTo(1));
		assertThat(result.iterator().next().getReactions().get(0).getReaction().getUuid(), equalTo(CODED_REACTION_UUID));
	}
	
	@Test
	public void searchForAllergies_shouldSearchForAllergiesByClinicalStatus() {
		TokenOrListParam status = new TokenOrListParam();
		status.addOr(new TokenParam().setValue("active"));
		
		Collection<Allergy> result = allergyDao.searchForAllergies(null, null, null, null, null, status);
		assertThat(result, notNullValue());
		assertThat(result.size(), greaterThanOrEqualTo(1));
		assertThat(result.iterator().next().getVoided(), equalTo(false));
	}
	
}
