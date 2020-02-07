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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

import javax.inject.Inject;
import javax.inject.Provider;

import org.hibernate.SessionFactory;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.Obs;
import org.openmrs.module.fhir2.TestFhirSpringConfiguration;
import org.openmrs.test.BaseModuleContextSensitiveTest;
import org.springframework.test.context.ContextConfiguration;

@ContextConfiguration(classes = TestFhirSpringConfiguration.class, inheritLocations = false)
public class FhirDiagnosticReportDaoImplTest extends BaseModuleContextSensitiveTest {
	
	private static final String DATA_XML = "org/openmrs/module/fhir2/api/dao/impl/FhirDiagnosticReportDaoImplTest_initial_data.xml";
	
	private static final String UUID = "d899333c-5bd4-45cc-b1e7-2f9542dbcbf6";
	
	private FhirDiagnosticReportDaoImpl dao;
	
	@Inject
	private Provider<SessionFactory> sessionFactoryProvider;
	
	@Before
	public void setup() throws Exception {
		dao = new FhirDiagnosticReportDaoImpl();
		dao.setSessionFactory(sessionFactoryProvider.get());
		executeDataSet(DATA_XML);
	}
	
	@Test
	public void shouldRetrieveObsGroupByUuid() {
		Obs result = dao.getObsGroupByUuid(UUID);
		
		assertThat(result, notNullValue());
		assertThat(result.getUuid(), equalTo(UUID));
	}
}
