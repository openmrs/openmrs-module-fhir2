/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.fhir2.api.impl;

import org.hl7.fhir.r4.model.Practitioner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.openmrs.Provider;
import org.openmrs.module.fhir2.api.dao.FhirPractitionerDao;
import org.openmrs.module.fhir2.api.translators.PractitionerTranslator;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class FhirPractitionerServiceImplTest {
	
	private static final String PRACTITIONER_UUID = "28923n23-nmkn23-23923-23sd";
	
	@Mock
	private PractitionerTranslator practitionerTranslator;
	
	@Mock
	private FhirPractitionerDao practitionerDao;
	
	private FhirPractitionerServiceImpl practitionerService;
	
	private Provider provider;
	
	private Practitioner practitioner;
	
	@Before
	public void setUp() {
		practitionerService = new FhirPractitionerServiceImpl();
		practitionerService.setDao(practitionerDao);
		practitionerService.setTranslator(practitionerTranslator);
		
		provider = new Provider();
		provider.setUuid(PRACTITIONER_UUID);
		
		practitioner = new Practitioner();
		practitioner.setId(PRACTITIONER_UUID);
	}
	
	@Test
	public void shouldRetrievePractitionerByUuid() {
		when(practitionerDao.getProviderByUuid(PRACTITIONER_UUID)).thenReturn(provider);
		when(practitionerTranslator.toFhirResource(provider)).thenReturn(practitioner);
		
		Practitioner result = practitionerService.getPractitionerByUuid(PRACTITIONER_UUID);
		assertThat(result, notNullValue());
		assertThat(result.getId(), notNullValue());
		assertThat(result.getId(), equalTo(PRACTITIONER_UUID));
	}
}
