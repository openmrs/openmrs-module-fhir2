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

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;

import java.util.Collection;

import ca.uhn.fhir.rest.param.ReferenceParam;
import ca.uhn.fhir.rest.param.TokenOrListParam;
import ca.uhn.fhir.rest.param.TokenParam;
import org.hibernate.SessionFactory;
import org.hl7.fhir.r4.model.Patient;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.AllergenType;
import org.openmrs.Allergy;
import org.openmrs.module.fhir2.TestFhirSpringConfiguration;
import org.openmrs.module.fhir2.api.FhirGlobalPropertyService;
import org.openmrs.test.BaseModuleContextSensitiveTest;
import org.springframework.test.context.ContextConfiguration;

@ContextConfiguration(classes = TestFhirSpringConfiguration.class, inheritLocations = false)
public class FhirAllergyIntoleranceDaoImplTest extends BaseModuleContextSensitiveTest {
	
	private static final String ALLERGY_INTOLERANCE_INITIAL_DATA_XML = "org/openmrs/module/fhir2/api/dao/impl/FhirAllergyIntoleranceDaoImplTest_initial_data.xml";
	
	private static final String ALLERGY_UUID = "1085AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
	
	private static final String UNKNOWN_ALLERGY_UUID = "9999AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
	
	private static final String CODED_ALLERGEN_UUID = "5085AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
	
	private static final String SEVERITY_CONCEPT_UUID = "5088AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
	
	private static final String CODED_REACTION_UUID = "5087AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
	
	@Inject
	@Named("sessionFactory")
	private Provider<SessionFactory> sessionFactoryProvider;
	
	@Inject
	private FhirGlobalPropertyService globalPropertyService;
	
	private FhirAllergyIntoleranceDaoImpl allergyDao;
	
	@Before
	public void setup() throws Exception {
		allergyDao = new FhirAllergyIntoleranceDaoImpl();
		allergyDao.setSessionFactory(sessionFactoryProvider.get());
		allergyDao.setGlobalPropertyService(globalPropertyService);
		executeDataSet(ALLERGY_INTOLERANCE_INITIAL_DATA_XML);
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
		ReferenceParam referenceParam = new ReferenceParam();
		referenceParam.setChain(Patient.SP_IDENTIFIER);
		referenceParam.setValue("M4001-1");
		
		Collection<Allergy> result = allergyDao.searchForAllergies(referenceParam, null, null, null, null, null);
		assertThat(result, notNullValue());
		assertThat(result.size(), greaterThanOrEqualTo(1));
		assertThat(result.iterator().next().getPatient().getIdentifiers().iterator().next().getIdentifier(),
		    equalTo("M4001-1"));
	}
	
	@Test
	public void searchForAllergies_shouldSearchForAllergiesByPatientGivenName() {
		ReferenceParam referenceParam = new ReferenceParam();
		referenceParam.setChain(Patient.SP_GIVEN);
		referenceParam.setValue("John");
		
		Collection<Allergy> result = allergyDao.searchForAllergies(referenceParam, null, null, null, null, null);
		assertThat(result, notNullValue());
		assertThat(result.size(), greaterThanOrEqualTo(1));
		assertThat(result.iterator().next().getPatient().getGivenName(), equalTo("John"));
	}
	
	@Test
	public void searchForAllergies_shouldSearchForAllergiesByPatientFamilyName() {
		ReferenceParam referenceParam = new ReferenceParam();
		referenceParam.setChain(Patient.SP_FAMILY);
		referenceParam.setValue("Doe");
		
		Collection<Allergy> result = allergyDao.searchForAllergies(referenceParam, null, null, null, null, null);
		assertThat(result, notNullValue());
		assertThat(result.size(), greaterThanOrEqualTo(1));
		assertThat(result.iterator().next().getPatient().getFamilyName(), equalTo("Doe"));
	}
	
	@Test
	public void searchForAllergies_shouldSearchForAllergiesByPatientName() {
		ReferenceParam referenceParam = new ReferenceParam();
		referenceParam.setChain(Patient.SP_NAME);
		referenceParam.setValue("John Doe");
		
		Collection<Allergy> result = allergyDao.searchForAllergies(referenceParam, null, null, null, null, null);
		assertThat(result, notNullValue());
		assertThat(result.size(), greaterThanOrEqualTo(1));
	}
	
	@Test
	public void searchForAllergies_shouldSearchForAllergiesByCategory() {
		TokenOrListParam category = new TokenOrListParam();
		category.addOr(new TokenParam().setValue("food"));
		
		Collection<Allergy> result = allergyDao.searchForAllergies(null, category, null, null, null, null);
		assertThat(result, notNullValue());
		assertThat(result.size(), greaterThanOrEqualTo(1));
		assertThat(result.iterator().next().getAllergenType(), equalTo(AllergenType.FOOD));
	}
	
	@Test
	public void searchForAllergies_shouldSearchForAllergiesByAllergen() {
		TokenOrListParam allergen = new TokenOrListParam();
		allergen.addOr(new TokenParam().setValue(CODED_ALLERGEN_UUID));
		
		Collection<Allergy> result = allergyDao.searchForAllergies(null, null, allergen, null, null, null);
		assertThat(result, notNullValue());
		assertThat(result.size(), greaterThanOrEqualTo(1));
		assertThat(result.iterator().next().getAllergen().getCodedAllergen().getUuid(), equalTo(CODED_ALLERGEN_UUID));
	}
	
	@Test
	public void searchForAllergies_shouldSearchForAllergiesBySeverity() {
		TokenOrListParam severity = new TokenOrListParam();
		severity.addOr(new TokenParam().setValue(SEVERITY_CONCEPT_UUID));
		
		Collection<Allergy> result = allergyDao.searchForAllergies(null, null, null, severity, null, null);
		assertThat(result, notNullValue());
		assertThat(result.size(), greaterThanOrEqualTo(1));
		assertThat(result.iterator().next().getSeverity().getUuid(), equalTo(SEVERITY_CONCEPT_UUID));
	}
	
	@Test
	public void searchForAllergies_shouldSearchForAllergiesByManifestation() {
		TokenOrListParam manifestation = new TokenOrListParam();
		manifestation.addOr(new TokenParam().setValue(CODED_REACTION_UUID));
		
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
