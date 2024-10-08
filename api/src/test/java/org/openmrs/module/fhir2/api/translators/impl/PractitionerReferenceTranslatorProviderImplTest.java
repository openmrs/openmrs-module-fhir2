/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.fhir2.api.translators.impl;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;
import static org.openmrs.module.fhir2.api.translators.impl.ReferenceHandlingTranslator.getReferenceId;

import org.hamcrest.Matchers;
import org.hl7.fhir.r4.model.Practitioner;
import org.hl7.fhir.r4.model.Reference;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openmrs.Provider;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.api.dao.FhirPractitionerDao;

@RunWith(MockitoJUnitRunner.class)
public class PractitionerReferenceTranslatorProviderImplTest {
	
	private static final String PRACTITIONER_UUID = "2ffb1a5f-bcd3-4243-8f40-78edc2642789";
	
	@Mock
	private FhirPractitionerDao practitionerDao;
	
	private PractitionerReferenceTranslatorProviderImpl referenceTranslatorProvider;
	
	private Provider provider;
	
	@Before
	public void setup() {
		referenceTranslatorProvider = new PractitionerReferenceTranslatorProviderImpl();
		referenceTranslatorProvider.setPractitionerDao(practitionerDao);
		
		provider = new Provider();
		provider.setUuid(PRACTITIONER_UUID);
	}
	
	@Test
	public void shouldConvertProviderToFhirPractitionerReference() {
		
		Reference result = referenceTranslatorProvider.toFhirResource(provider);
		assertThat(result, notNullValue());
		assertThat(result.getType(), equalTo(FhirConstants.PRACTITIONER));
		assertThat(getReferenceId(result).orElse(null), equalTo(PRACTITIONER_UUID));
	}
	
	@Test
	public void shouldReturnNullIfProviderIsNull() {
		assertThat(referenceTranslatorProvider.toFhirResource(null), nullValue());
	}
	
	@Test
	public void shouldConvertReferenceToProvider() {
		Reference practitionerReference = new Reference().setReference(FhirConstants.PRACTITIONER + "/" + PRACTITIONER_UUID)
		        .setType(FhirConstants.PRACTITIONER);
		Practitioner practitioner = new Practitioner();
		practitioner.setId(PRACTITIONER_UUID);
		when(practitionerDao.get(PRACTITIONER_UUID)).thenReturn(provider);
		
		Provider result = referenceTranslatorProvider.toOpenmrsType(practitionerReference);
		
		assertThat(result, Matchers.notNullValue());
		assertThat(result.getUuid(), Matchers.equalTo(PRACTITIONER_UUID));
	}
	
	@Test
	public void shouldReturnNullIfReferenceNull() {
		Provider result = referenceTranslatorProvider.toOpenmrsType(null);
		
		assertThat(result, Matchers.nullValue());
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void shouldThrowExceptionIfReferenceIsNotForPractitioner() {
		Reference reference = new Reference().setReference("Unknown" + "/" + PRACTITIONER_UUID).setType("Unknown");
		
		referenceTranslatorProvider.toOpenmrsType(reference);
	}
}
