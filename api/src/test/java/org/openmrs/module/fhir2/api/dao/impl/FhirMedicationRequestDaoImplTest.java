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

import org.hibernate.SessionFactory;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.DrugOrder;
import org.openmrs.module.fhir2.TestFhirSpringConfiguration;
import org.openmrs.test.BaseModuleContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;

@ContextConfiguration(classes = TestFhirSpringConfiguration.class, inheritLocations = false)
public class FhirMedicationRequestDaoImplTest extends BaseModuleContextSensitiveTest {
	
	private static final String DRUG_ORDER_UUID = "6d0ae116-707a-4629-9850-f15206e63ab0";
	
	private static final String BAD_DRUG_ORDER_UUID = "uie3b9a2-4de5-4b12-ac40-jk90sdh";
	
	private static final String MEDICATION_REQUEST_INITIAL_DATA_XML = "org/openmrs/module/fhir2/api/dao/impl/FhirMedicationRequestDaoImpl_initial_data.xml";
	
	@Autowired
	@Qualifier("sessionFactory")
	private SessionFactory sessionFactory;
	
	private FhirMedicationRequestDaoImpl medicationRequestDao;
	
	@Before
	public void setup() throws Exception {
		medicationRequestDao = new FhirMedicationRequestDaoImpl();
		medicationRequestDao.setSessionFactory(sessionFactory);
		executeDataSet(MEDICATION_REQUEST_INITIAL_DATA_XML);
	}
	
	@Test
	public void getMedicationRequestByUuid_shouldGetByUuid() {
		DrugOrder drugOrder = medicationRequestDao.getMedicationRequestByUuid(DRUG_ORDER_UUID);
		assertThat(drugOrder, notNullValue());
		assertThat(drugOrder.getUuid(), notNullValue());
		assertThat(drugOrder.getUuid(), equalTo(DRUG_ORDER_UUID));
	}
	
	@Test
	public void getMedicationRequestByUuid_shouldReturnNullWhenCalledWithBadUuid() {
		DrugOrder drugOrder = medicationRequestDao.getMedicationRequestByUuid(BAD_DRUG_ORDER_UUID);
		assertThat(drugOrder, nullValue());
	}
	
}
