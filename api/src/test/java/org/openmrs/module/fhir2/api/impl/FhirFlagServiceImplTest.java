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

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.when;

import ca.uhn.fhir.rest.server.exceptions.InvalidRequestException;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import org.hl7.fhir.r4.model.Flag;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openmrs.module.fhir2.api.dao.FhirFlagDao;
import org.openmrs.module.fhir2.api.translators.FlagTranslator;
import org.openmrs.module.fhir2.model.FhirFlag;

@RunWith(MockitoJUnitRunner.class)
public class FhirFlagServiceImplTest {
	
	private static final String FLAG_UUID = "1359f03d-55d9-4961-b8f8-9a59eddc1f59";
	
	private static final String BAD_FLAG_UUID = "02ed36f0-6167-4372-a641-d27b92f7deae";
	
	@Mock
	private FhirFlagDao dao;
	
	@Mock
	private FlagTranslator translator;
	
	private FhirFlagServiceImpl flagService;
	
	private FhirFlag openmrsFlag;
	
	private Flag fhirFlag;
	
	@Before
	public void setup() {
		flagService = new FhirFlagServiceImpl() {
			
			@Override
			protected void validateObject(FhirFlag object) {
			}
		};
		
		flagService.setDao(dao);
		flagService.setTranslator(translator);
		
		openmrsFlag = new FhirFlag();
		openmrsFlag.setUuid(FLAG_UUID);
		
		fhirFlag = new Flag();
		fhirFlag.setId(FLAG_UUID);
	}
	
	@Test
	public void getByUuid_shouldGetFlagByUuid() {
		when(dao.get(FLAG_UUID)).thenReturn(openmrsFlag);
		when(translator.toFhirResource(openmrsFlag)).thenReturn(fhirFlag);
		
		Flag flag = flagService.get(FLAG_UUID);
		assertThat(flag, notNullValue());
		assertThat(flag.getId(), notNullValue());
		assertThat(flag.getId(), equalTo(FLAG_UUID));
	}
	
	@Test
	public void getByUuid_shouldThrowResourceNotFoundWhenCalledWithBadUuid() {
		assertThrows(ResourceNotFoundException.class, () -> flagService.get(BAD_FLAG_UUID));
	}
	
	@Test
	public void create_shouldCreateNewFlag() {
		Flag flag = new Flag();
		flag.setId(FLAG_UUID);
		flag.setStatus(Flag.FlagStatus.ACTIVE);
		
		when(translator.toFhirResource(openmrsFlag)).thenReturn(flag);
		when(translator.toOpenmrsType(flag)).thenReturn(openmrsFlag);
		when(dao.createOrUpdate(openmrsFlag)).thenReturn(openmrsFlag);
		
		Flag result = flagService.create(flag);
		assertThat(result, notNullValue());
		assertThat(result.getStatus(), is(Flag.FlagStatus.ACTIVE));
	}
	
	@Test(expected = InvalidRequestException.class)
	public void update_shouldThrowInvalidRequestExceptionIfIdIsNull() {
		Flag flag = new Flag();
		flag.setId(FLAG_UUID);
		
		flagService.update(null, flag);
	}
	
	@Test(expected = InvalidRequestException.class)
	public void update_shouldThrowInvalidRequestExceptionIfIdIsBad() {
		Flag flag = new Flag();
		flag.setId(FLAG_UUID);
		
		flagService.update(BAD_FLAG_UUID, flag);
	}
	
	@Test(expected = ResourceNotFoundException.class)
	public void update_shouldThrowResourceNotFoundException() {
		Flag flag = new Flag();
		flag.setId(FLAG_UUID);
		
		flagService.update(FLAG_UUID, flag);
	}
	
	@Test
	public void update_shouldUpdateExistingFlagAccordingly() {
		FhirFlag fhirFlag = new FhirFlag();
		fhirFlag.setUuid(FLAG_UUID);
		fhirFlag.setStatus(FhirFlag.FlagStatus.INACTIVE);
		fhirFlag.setRetired(false);
		
		Flag flag = new Flag();
		flag.setId(FLAG_UUID);
		flag.setStatus(Flag.FlagStatus.ACTIVE);
		
		when(dao.get(FLAG_UUID)).thenReturn(fhirFlag);
		when(translator.toFhirResource(fhirFlag)).thenReturn(flag);
		when(translator.toOpenmrsType(fhirFlag, flag)).thenReturn(fhirFlag);
		when(dao.createOrUpdate(fhirFlag)).thenReturn(fhirFlag);
		
		Flag result = flagService.update(FLAG_UUID, flag);
		assertThat(result, notNullValue());
		assertThat(result.getStatus(), is(Flag.FlagStatus.ACTIVE));
	}
	
	@Test
	public void delete_shouldDeleteTheSpecifiedFlag() {
		Flag flag = new Flag();
		flag.setId(FLAG_UUID);
		
		when(dao.delete(FLAG_UUID)).thenReturn(openmrsFlag);
		when(translator.toFhirResource(openmrsFlag)).thenReturn(flag);
		
		Flag result = flagService.delete(FLAG_UUID);
		assertThat(result, notNullValue());
		assertThat(result.getId(), equalTo(FLAG_UUID));
	}
	
}
