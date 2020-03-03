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
import org.openmrs.Drug;
import org.openmrs.module.fhir2.TestFhirSpringConfiguration;
import org.openmrs.test.BaseModuleContextSensitiveTest;
import org.springframework.test.context.ContextConfiguration;

@ContextConfiguration(classes = TestFhirSpringConfiguration.class, inheritLocations = false)
public class FhirMedicationDaoImplTest extends BaseModuleContextSensitiveTest {
	
	private static final String MEDICATION_UUID = "1085AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
	
	private static final String WRONG_MEDICATION_UUID = "9085AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
	
	private static final String MEDICATION_INITIAL_DATA_XML = "org/openmrs/module/fhir2/api/dao/impl/FhirMedicationDaoImplTest_initial_data.xml";
	
	@Inject
	@Named("sessionFactory")
	private Provider<SessionFactory> sessionFactoryProvider;
	
	private FhirMedicationDaoImpl medicationDao;
	
	@Before
	public void setup() throws Exception {
		medicationDao = new FhirMedicationDaoImpl();
		medicationDao.setSessionFactory(sessionFactoryProvider.get());
		executeDataSet(MEDICATION_INITIAL_DATA_XML);
	}
	
	@Test
	public void getMedicationByUuid_shouldGetByUuid() {
		Drug medication = medicationDao.getMedicationByUuid(MEDICATION_UUID);
		assertThat(medication, notNullValue());
		assertThat(medication.getUuid(), notNullValue());
		assertThat(medication.getUuid(), equalTo(MEDICATION_UUID));
	}
	
	@Test
	public void getMedicationByUuid_shouldReturnNullWhenCalledWithUnknownUuid() {
		Drug medication = medicationDao.getMedicationByUuid(WRONG_MEDICATION_UUID);
		assertThat(medication, nullValue());
	}
}
