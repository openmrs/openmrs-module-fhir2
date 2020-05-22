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
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;

import org.hl7.fhir.r4.model.RelatedPerson;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openmrs.Relationship;
import org.openmrs.module.fhir2.api.dao.FhirRelatedPersonDao;
import org.openmrs.module.fhir2.api.translators.RelatedPersonTranslator;

@RunWith(MockitoJUnitRunner.class)
public class FhirRelatedPersonServiceImplTest {
	
	private static final String RELATED_PERSON_UUID = "5f07c6ff-c483-4e77-815e-44dd650470e7";
	
	private static final String WRONG_RELATED_PERSON_UUID = "1a1d2623-2f67-47de-8fb0-b02f51e378b7";
	
	@Mock
	private FhirRelatedPersonDao dao;
	
	@Mock
	private RelatedPersonTranslator translator;
	
	private FhirRelatedPersonServiceImpl relatedPersonService;
	
	@Before
	public void setup() {
		relatedPersonService = new FhirRelatedPersonServiceImpl();
		relatedPersonService.setDao(dao);
		relatedPersonService.setTranslator(translator);
	}
	
	@Test
	public void shouldGetRelatedPersonById() {
		Relationship relationship = new Relationship();
		relationship.setUuid(RELATED_PERSON_UUID);
		
		RelatedPerson relatedPerson = new RelatedPerson();
		relatedPerson.setId(RELATED_PERSON_UUID);
		
		when(dao.get(RELATED_PERSON_UUID)).thenReturn(relationship);
		when(translator.toFhirResource(relationship)).thenReturn(relatedPerson);
		
		RelatedPerson result = relatedPersonService.get(RELATED_PERSON_UUID);
		assertThat(result, notNullValue());
		assertThat(result.getId(), notNullValue());
		assertThat(result.getId(), equalTo(RELATED_PERSON_UUID));
	}
	
	@Test
	public void shouldReturnNullWhenGetByWrongUuid() {
		when(dao.get(WRONG_RELATED_PERSON_UUID)).thenReturn(null);
		assertThat(relatedPersonService.get(WRONG_RELATED_PERSON_UUID), nullValue());
	}
}
