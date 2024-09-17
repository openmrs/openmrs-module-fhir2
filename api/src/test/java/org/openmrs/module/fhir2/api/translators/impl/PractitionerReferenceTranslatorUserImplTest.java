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

import org.hamcrest.Matchers;
import org.hl7.fhir.r4.model.Practitioner;
import org.hl7.fhir.r4.model.Reference;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openmrs.User;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.api.dao.FhirUserDao;

@RunWith(MockitoJUnitRunner.class)
public class PractitionerReferenceTranslatorUserImplTest {
	
	private static final String USER_UUID = "2ffb1a5f-bcd3-4243-8f40-78edc2642789";
	
	private static final String PRACTITIONER_UUID = "2ffb1a5f-bcd3-4243-8f40-78edc2642789";
	
	@Mock
	private FhirUserDao userDao;
	
	private PractitionerReferenceTranslatorUserImpl practitionerReferenceTranslatorUser;
	
	@Before
	public void setup() {
		practitionerReferenceTranslatorUser = new PractitionerReferenceTranslatorUserImpl();
		practitionerReferenceTranslatorUser.setUserDao(userDao);
		
	}
	
	@Test
	public void shouldConvertUserToFhirPractitionerReference() {
		User user = new User();
		user.setUuid(USER_UUID);
		
		Reference result = practitionerReferenceTranslatorUser.toFhirResource(user);
		assertThat(result, notNullValue());
		assertThat(result.getType(), equalTo(FhirConstants.PRACTITIONER));
		assertThat(practitionerReferenceTranslatorUser.getReferenceId(result).orElse(null), equalTo(USER_UUID));
	}
	
	@Test
	public void shouldReturnNullIfCreatorIsNull() {
		assertThat(practitionerReferenceTranslatorUser.toFhirResource(null), nullValue());
	}
	
	@Test
	public void toOpenmrsType_shouldConvertReferenceToUser() {
		Reference creatorReference = new Reference().setReference(FhirConstants.PRACTITIONER + "/" + USER_UUID)
		        .setType(FhirConstants.PRACTITIONER);
		User user = new User();
		user.setUuid(USER_UUID);
		
		Practitioner practitioner = new Practitioner();
		practitioner.setId(PRACTITIONER_UUID);
		
		when(userDao.get(USER_UUID)).thenReturn(user);
		
		User result = practitionerReferenceTranslatorUser.toOpenmrsType(creatorReference);
		
		assertThat(result, Matchers.notNullValue());
		assertThat(result.getUuid(), Matchers.equalTo(USER_UUID));
	}
	
	@Test
	public void toOpenmrsType_shouldReturnNullIfReferenceNull() {
		User result = practitionerReferenceTranslatorUser.toOpenmrsType(null);
		
		assertThat(result, Matchers.nullValue());
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void toOpenmrsType_shouldThrowExceptionIfReferenceIsntForCreator() {
		Reference reference = new Reference().setReference("Unknown" + "/" + USER_UUID).setType("Unknown");
		
		practitionerReferenceTranslatorUser.toOpenmrsType(reference);
	}
}
