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
import org.hl7.fhir.r4.model.Group;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openmrs.Cohort;
import org.openmrs.module.fhir2.api.dao.FhirGroupDao;
import org.openmrs.module.fhir2.api.translators.GroupTranslator;

@RunWith(MockitoJUnitRunner.class)
public class FhirGroupServiceImplTest {
	
	private static final String COHORT_UUID = "1359f03d-55d9-4961-b8f8-9a59eddc1f59";
	
	private static final String BAD_COHORT_UUID = "02ed36f0-6167-4372-a641-d27b92f7deae";
	
	@Mock
	private FhirGroupDao dao;
	
	@Mock
	private GroupTranslator translator;
	
	private FhirGroupServiceImpl groupService;
	
	private Group group;
	
	private Cohort cohort;
	
	@Before
	public void setup() {
		groupService = new FhirGroupServiceImpl() {
			
			@Override
			protected void validateObject(Cohort object) {
			}
		};
		groupService.setDao(dao);
		groupService.setTranslator(translator);
		
		group = new Group();
		group.setId(COHORT_UUID);
		
		cohort = new Cohort();
		cohort.setUuid(COHORT_UUID);
	}
	
	@Test
	public void getGroupByUuid_shouldGetGroupByUuid() {
		when(dao.get(COHORT_UUID)).thenReturn(cohort);
		when(translator.toFhirResource(cohort)).thenReturn(group);
		
		Group group = groupService.get(COHORT_UUID);
		assertThat(group, notNullValue());
		assertThat(group.getId(), notNullValue());
		assertThat(group.getId(), equalTo(COHORT_UUID));
	}
	
	@Test
	public void getGroupByUuid_shouldThrowResourceNotFoundWhenCalledWithUnknownUuid() {
		assertThrows(ResourceNotFoundException.class, () -> groupService.get(BAD_COHORT_UUID));
	}
	
	@Test
	public void shouldSaveNewGroup() {
		Cohort cohort = new Cohort();
		cohort.setUuid(COHORT_UUID);
		
		Group group = new Group();
		group.setId(COHORT_UUID);
		
		when(translator.toFhirResource(cohort)).thenReturn(group);
		when(translator.toOpenmrsType(group)).thenReturn(cohort);
		when(dao.createOrUpdate(cohort)).thenReturn(cohort);
		
		Group result = groupService.create(group);
		assertThat(result, notNullValue());
		assertThat(result.getId(), equalTo(COHORT_UUID));
	}
	
	@Test(expected = InvalidRequestException.class)
	public void updateGroupShouldThrowInvalidRequestExceptionIfIdIsNull() {
		Group group = new Group();
		group.setId(COHORT_UUID);
		
		groupService.update(null, group);
	}
	
	@Test(expected = InvalidRequestException.class)
	public void updateGroupShouldThrowInvalidRequestExceptionIfIdIsBad() {
		Group group = new Group();
		group.setId(COHORT_UUID);
		
		groupService.update(BAD_COHORT_UUID, group);
	}
	
	@Test(expected = ResourceNotFoundException.class)
	public void updateGroupShouldThrowResourceNotFoundException() {
		Group group = new Group();
		group.setId(COHORT_UUID);
		
		groupService.update(COHORT_UUID, group);
	}
	
	@Test
	public void shouldUpdateGroup() {
		Cohort cohort = new Cohort();
		cohort.setUuid(COHORT_UUID);
		cohort.setVoided(false);
		
		Group group = new Group();
		group.setId(COHORT_UUID);
		group.setActive(false);
		
		when(dao.get(COHORT_UUID)).thenReturn(cohort);
		when(translator.toFhirResource(cohort)).thenReturn(group);
		when(translator.toOpenmrsType(cohort, group)).thenReturn(cohort);
		when(dao.createOrUpdate(cohort)).thenReturn(cohort);
		
		Group result = groupService.update(COHORT_UUID, group);
		assertThat(result, notNullValue());
		assertThat(result.getActive(), is(false));
	}
	
	@Test
	public void shouldDeleteGroup() {
		Group group = new Group();
		group.setId(COHORT_UUID);
		
		when(dao.delete(COHORT_UUID)).thenReturn(cohort);
		when(translator.toFhirResource(cohort)).thenReturn(group);
		
		Group result = groupService.delete(COHORT_UUID);
		assertThat(result, notNullValue());
		assertThat(result.getId(), equalTo(COHORT_UUID));
		assertThat(result.getActive(), is(false));
	}
}
