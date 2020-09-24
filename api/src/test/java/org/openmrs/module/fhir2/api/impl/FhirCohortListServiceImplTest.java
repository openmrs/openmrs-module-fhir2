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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.Date;

import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import org.hl7.fhir.r4.model.Annotation;
import org.hl7.fhir.r4.model.ListResource;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openmrs.Cohort;
import org.openmrs.module.fhir2.api.dao.FhirListDao;
import org.openmrs.module.fhir2.api.translators.ListTranslator;

@RunWith(MockitoJUnitRunner.class)
public class FhirCohortListServiceImplTest {
	
	private static final String LIST_UUID = "c0b1f314-1691-11df-97a5-7038c432aab88";
	
	private static final String UNKNOWN_UUID = "c0b1f314-1691-11df-97a5-7038c432aab99";
	
	private static final String TITLE = "Covid19 patients";
	
	private static final String DESCRIPTION = "Covid19 patients";
	
	@Mock
	private FhirListDao<Cohort> listDao;
	
	@Mock
	private ListTranslator<Cohort> listTranslator;
	
	private FhirCohortListServiceImpl fhirCohortListService;
	
	private Cohort cohort;
	
	private ListResource list;
	
	@Before
	public void setup() {
		fhirCohortListService = new FhirCohortListServiceImpl() {
			
			@Override
			protected void validateObject(Cohort object) {
			}
		};
		
		fhirCohortListService.setTranslator(listTranslator);
		fhirCohortListService.setDao(listDao);
		
		cohort = new Cohort();
		cohort.setUuid(LIST_UUID);
		cohort.setName(TITLE);
		cohort.setVoided(false);
		cohort.setDateChanged(new Date());
		cohort.setDescription(DESCRIPTION);
		
		list = new ListResource();
		list.setId(LIST_UUID);
		list.setTitle(TITLE);
		list.setStatus(ListResource.ListStatus.CURRENT);
		list.setDate(new Date());
		list.setNote(Collections.singletonList(new Annotation().setText(DESCRIPTION)));
		list.setMode(ListResource.ListMode.WORKING);
		
	}
	
	@Test
	public void getListByUuid_shouldGetListByUuid() {
		when(listDao.get(LIST_UUID)).thenReturn(cohort);
		when(listTranslator.toFhirResource(cohort)).thenReturn(list);
		
		org.hl7.fhir.r4.model.ListResource result = fhirCohortListService.get(LIST_UUID);
		
		assertThat(result, notNullValue());
		assertThat(result.getId(), equalTo(LIST_UUID));
		assertThat(result.getTitle(), equalTo(TITLE));
		assertThat(result.getStatus(), equalTo(ListResource.ListStatus.CURRENT));
	}
	
	@Test
	public void getListByUuid_shouldThrowResourceNotFoundForUnknownUuid() {
		assertThrows(ResourceNotFoundException.class, () -> fhirCohortListService.get(UNKNOWN_UUID));
	}
}
