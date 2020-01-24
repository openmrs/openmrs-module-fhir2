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

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;

import org.hibernate.SessionFactory;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.Obs;
import org.openmrs.module.fhir2.TestFhirSpringConfiguration;
import org.openmrs.test.BaseModuleContextSensitiveTest;
import org.springframework.test.context.ContextConfiguration;

@ContextConfiguration(classes = TestFhirSpringConfiguration.class, inheritLocations = false)
public class FhirObservationDaoImplTest extends BaseModuleContextSensitiveTest {
	
	private static final String OBS_UUID = "39fb7f47-e80a-4056-9285-bd798be13c63";
	
	private static final String BAD_OBS_UUID = "121b73a6-e1a4-4424-8610-d5765bf2fdf7";

	private FhirObservationDaoImpl fhirObservationDao;
	
	@Inject
	Provider<FhirObservationDaoImpl> daoProvider;

	@Inject
	@Named("sessionFactory")
	private Provider<SessionFactory> sessionFactoryProvider;

	@Before
	public void setup() throws Exception {
		fhirObservationDao = new FhirObservationDaoImpl();
		fhirObservationDao.setSessionFactory(sessionFactoryProvider.get());
	}
	
	@Test
	public void shouldGetObsByUuid() {
		Obs result = fhirObservationDao.getObsByUuid(OBS_UUID);
		
		assertThat(result, notNullValue());
		assertThat(result.getUuid(), equalTo(OBS_UUID));
	}
	
	@Test
	public void shouldReturnNullIfObsNotFoundByUuid() {
		Obs result = fhirObservationDao.getObsByUuid(BAD_OBS_UUID);
		
		assertThat(result, nullValue());
	}
}
