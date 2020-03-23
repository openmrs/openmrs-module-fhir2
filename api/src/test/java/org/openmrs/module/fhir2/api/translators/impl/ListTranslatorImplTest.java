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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

import java.util.Date;

import org.apache.commons.lang3.time.DateUtils;
import org.exparity.hamcrest.date.DateMatchers;
import org.hl7.fhir.r4.model.ListResource;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.openmrs.Cohort;

@RunWith(MockitoJUnitRunner.class)
public class ListTranslatorImplTest {
	
	private static final String LIST_UUID = "c0b1f314-1691-11df-97a5-7038c432aab88";
	
	private static final String TITLE = "Covid19 patients";
	
	private static final String DESCRIPTION = "Covid19 patients";
	
	private ListTranslatorImpl listTranslator;
	
	private Cohort cohort;
	
	@Before
	public void setup() {
		listTranslator = new ListTranslatorImpl();
		cohort = new Cohort();
	}
	
	@Test
	public void toFhirResource_shouldReturnNullIfCohortIsNull() {
		ListResource list = listTranslator.toFhirResource(null);
		assertThat(list, nullValue());
	}
	
	@Test
	public void toFhirResource_shouldTranslateCohortUuidToListId() {
		cohort.setUuid(LIST_UUID);
		ListResource list = listTranslator.toFhirResource(cohort);
		assertThat(list, notNullValue());
		assertThat(list.getId(), notNullValue());
		assertThat(list.getId(), equalTo(LIST_UUID));
	}
	
	@Test
	public void toFhirResource_shouldSetListModeToWorking() {
		ListResource list = listTranslator.toFhirResource(cohort);
		assertThat(list, notNullValue());
		assertThat(list.getMode(), equalTo(ListResource.ListMode.WORKING));
	}
	
	@Test
	public void toFhirResource_shouldTranslateCohortNameToListTitle() {
		cohort.setName(TITLE);
		ListResource list = listTranslator.toFhirResource(cohort);
		assertThat(list, notNullValue());
		assertThat(list.getTitle(), notNullValue());
		assertThat(list.getTitle(), equalTo(TITLE));
	}
	
	@Test
	public void toFhirResource_shouldTranslateCohortDateToListDate() {
		cohort.setDateCreated(new Date());
		ListResource list = listTranslator.toFhirResource(cohort);
		assertThat(list, notNullValue());
		assertThat(list.getDate(), notNullValue());
		assertThat(list.getDate(), DateMatchers.sameDay(new Date()));
	}
	
	@Test
	public void toFhirResource_shouldTranslateCohortDateChangedToListDate() {
		cohort.setDateCreated(DateUtils.addDays(new Date(), -60));
		cohort.setDateChanged(new Date());
		
		ListResource list = listTranslator.toFhirResource(cohort);
		assertThat(list, notNullValue());
		assertThat(list.getDate(), notNullValue());
		assertThat(list.getDate(), DateMatchers.sameDay(new Date()));
	}
	
	@Test
	public void toFhirResource_shouldTranslateCohortDescriptionToListNote() {
		cohort.setDescription(DESCRIPTION);
		ListResource list = listTranslator.toFhirResource(cohort);
		assertThat(list, notNullValue());
		assertThat(list.getNote().size(), equalTo(1));
		assertThat(list.getNote().get(0).getText(), equalTo(DESCRIPTION));
	}
	
	@Test
	public void toFhirResource_shouldTranslateVoidedToListRetired() {
		cohort.setVoided(true);
		ListResource list = listTranslator.toFhirResource(cohort);
		assertThat(list, notNullValue());
		assertThat(list.getStatus(), notNullValue());
		assertThat(list.getStatus(), equalTo(ListResource.ListStatus.RETIRED));
	}
	
	@Test
	public void toFhirResource_shouldTranslateVoidedFalseToActive() {
		cohort.setVoided(false);
		ListResource list = listTranslator.toFhirResource(cohort);
		assertThat(list, notNullValue());
		assertThat(list.getStatus(), notNullValue());
		assertThat(list.getStatus(), equalTo(ListResource.ListStatus.CURRENT));
	}
	
	@Test
	public void toOpenmrsType_shouldReturnCohortAsIsIfListResourceIsNull() {
		Cohort result = listTranslator.toOpenmrsType(cohort, null);
		
		assertThat(result, equalTo(cohort));
	}
	
	@Test
	public void toOpenmrsType_shouldTranslateListIdToCohortUuid() {
		ListResource list = new ListResource();
		list.setId(LIST_UUID);
		list.setStatus(ListResource.ListStatus.CURRENT);
		
		listTranslator.toOpenmrsType(cohort, list);
		
		assertThat(cohort.getUuid(), notNullValue());
		assertThat(cohort.getUuid(), equalTo(LIST_UUID));
	}
	
	@Test
	public void toOpenmrsType_shouldTranslateListTitleToCohortName() {
		ListResource list = new ListResource();
		list.setTitle(TITLE);
		list.setStatus(ListResource.ListStatus.CURRENT);
		
		listTranslator.toOpenmrsType(cohort, list);
		
		assertThat(cohort.getName(), notNullValue());
		assertThat(cohort.getName(), equalTo(TITLE));
	}
	
	@Test
	public void toOpenmrsType_shouldTranslateListStatusToCohortStatus() {
		ListResource list = new ListResource();
		list.setStatus(ListResource.ListStatus.RETIRED);
		
		listTranslator.toOpenmrsType(cohort, list);
		
		assertThat(cohort.getVoided(), notNullValue());
		assertThat(cohort.getVoided(), equalTo(true));
	}
	
	@Test
	public void toOpenmrsType_shouldTranslateListDateToDateCreated() {
		ListResource list = new ListResource();
		list.setDate(new Date());
		list.setStatus(ListResource.ListStatus.CURRENT);
		
		listTranslator.toOpenmrsType(cohort, list);
		
		assertThat(cohort.getDateCreated(), notNullValue());
		assertThat(cohort.getDateCreated(), DateMatchers.sameDay(new Date()));
	}
	
	@Test
	public void toOpenmrsType_shouldTranslateListDateToDateUpdated() {
		ListResource list = new ListResource();
		list.setDate(new Date());
		list.setStatus(ListResource.ListStatus.CURRENT);
		
		cohort.setDateCreated(DateUtils.addDays(new Date(), -60));
		listTranslator.toOpenmrsType(cohort, list);
		
		assertThat(cohort.getDateCreated(), notNullValue());
		assertThat(cohort.getDateChanged(), notNullValue());
		assertThat(cohort.getDateChanged(), DateMatchers.sameDay(new Date()));
	}
}
