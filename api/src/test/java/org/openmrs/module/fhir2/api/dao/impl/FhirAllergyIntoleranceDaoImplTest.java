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

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;

import org.hibernate.SessionFactory;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.Allergy;
import org.openmrs.module.fhir2.TestFhirSpringConfiguration;
import org.openmrs.test.BaseModuleContextSensitiveTest;
import org.springframework.test.context.ContextConfiguration;

@ContextConfiguration(classes = TestFhirSpringConfiguration.class, inheritLocations = false)
public class FhirAllergyIntoleranceDaoImplTest extends BaseModuleContextSensitiveTest {
	
	private static final String ALLERGY_INTOLERANCE_INITIAL_DATA_XML = "org/openmrs/module/fhir2/api/dao/impl/FhirAllergyIntoleranceDaoImplTest_initial_data.xml";
	
	private static final String ALLERGY_UUID = "1085AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
	
	private static final String UNKNOWN_ALLERGY_UUID = "9999AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
	
	@Inject
	@Named("sessionFactory")
	private Provider<SessionFactory> sessionFactoryProvider;
	
	private FhirAllergyIntoleranceDaoImpl allergyDao;
	
	@Before
	public void setup() throws Exception {
		allergyDao = new FhirAllergyIntoleranceDaoImpl();
		allergyDao.setSessionFactory(sessionFactoryProvider.get());
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
	
}
