/*
  This Source Code Form is subject to the terms of the Mozilla Public License,
  v. 2.0. If a copy of the MPL was not distributed with this file, You can
  obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
  the terms of the Healthcare Disclaimer located at http://openmrs.org/license.

  Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
  graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.fhir2.api.dao.impl;

import org.junit.Before;
import org.junit.Test;
import org.openmrs.api.ProviderService;
import org.openmrs.module.fhir2.TestFhirSpringConfiguration;
import org.openmrs.test.BaseModuleContextSensitiveTest;
import org.springframework.test.context.ContextConfiguration;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

@ContextConfiguration(classes = TestFhirSpringConfiguration.class, inheritLocations = false)
public class FhirPractitionerDaoImplTest extends BaseModuleContextSensitiveTest {
	
	private static final String PRACTITIONER_INITIAL_DATA_XML = "org/openmrs/module/fhir2/api/dao/impl/FhirPractitionerDaoImplTest_initial_data.xml";
	
	private static final String PRACTITIONER_UUID = "f9badd80-ab76-11e2-9e96-0800200c9a66";
	
	@Inject
	@Named("providerService")
	private Provider<ProviderService> providerServiceProvider;
	
	private FhirPractitionerDaoImpl dao;
	
	@Before
	public void setUp() throws Exception {
		dao = new FhirPractitionerDaoImpl();
		dao.setProviderService(providerServiceProvider.get());
		executeDataSet(PRACTITIONER_INITIAL_DATA_XML);
	}
	
	@Test
	public void shouldRetrievePractitionerByUuid() {
		org.openmrs.Provider provider = dao.getProviderByUuid(PRACTITIONER_UUID);
		assertThat(provider, notNullValue());
		assertThat(provider.getUuid(), notNullValue());
		assertThat(provider.getUuid(), equalTo(PRACTITIONER_UUID));
	}
	
}
