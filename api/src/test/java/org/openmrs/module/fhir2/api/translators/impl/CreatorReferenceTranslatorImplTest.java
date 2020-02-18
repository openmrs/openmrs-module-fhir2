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
import org.hl7.fhir.r4.model.Reference;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openmrs.User;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.api.FhirUserService;

@RunWith(MockitoJUnitRunner.class)
public class CreatorReferenceTranslatorImplTest {
	
	private static final String CREATOR_UUID = "2ffb1a5f-bcd3-4243-8f40-78edc2642789";
	
	@Mock
	private FhirUserService userService;
	
	private CreatorReferenceTranslatorImpl creatorReferenceTranslator;
	
	@Before
	public void setup() {
		creatorReferenceTranslator = new CreatorReferenceTranslatorImpl();
		creatorReferenceTranslator.setUserService(userService);
	}
	
	@Test
	public void shouldConvertCreatorToReference() {
		User user = new User();
		user.setUuid(CREATOR_UUID);
		
		Reference result = creatorReferenceTranslator.toFhirResource(user);
		assertThat(result, notNullValue());
		assertThat(result.getType(), equalTo(FhirConstants.CREATOR));
		assertThat(creatorReferenceTranslator.getReferenceId(result), equalTo(CREATOR_UUID));
	}
	
	@Test
	public void shouldReturnNullIfCreatorIsNull() {
		assertThat(creatorReferenceTranslator.toFhirResource(null), nullValue());
	}
	
	@Test
	public void toOpenmrsType_shouldConvertReferenceToCreator() {
		Reference creatorReference = new Reference().setReference(FhirConstants.CREATOR + "/" + CREATOR_UUID)
		        .setType(FhirConstants.CREATOR);
		User user = new User();
		user.setUuid(CREATOR_UUID);
		when(userService.getUserByUuid(CREATOR_UUID)).thenReturn(user);
		
		User result = creatorReferenceTranslator.toOpenmrsType(creatorReference);
		
		assertThat(result, Matchers.notNullValue());
		assertThat(result.getUuid(), Matchers.equalTo(CREATOR_UUID));
	}
	
	@Test
	public void toOpenmrsType_shouldReturnNullIfReferenceNull() {
		User result = creatorReferenceTranslator.toOpenmrsType(null);
		
		assertThat(result, Matchers.nullValue());
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void toOpenmrsType_shouldThrowExceptionIfReferenceIsntForCreator() {
		Reference reference = new Reference().setReference("Unknown" + "/" + CREATOR_UUID).setType("Unknown");
		
		creatorReferenceTranslator.toOpenmrsType(reference);
	}
}
