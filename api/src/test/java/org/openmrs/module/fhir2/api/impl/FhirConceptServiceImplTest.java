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
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openmrs.Concept;
import org.openmrs.ConceptSource;
import org.openmrs.module.fhir2.api.dao.FhirConceptDao;

@RunWith(MockitoJUnitRunner.class)
public class FhirConceptServiceImplTest {
	
	private static final String CONCEPT_UUID = "12345-abcdef-12345";
	
	private static final String BAD_CONCEPT_UUID = "no concept";
	
	@Mock
	private FhirConceptDao conceptDao;
	
	private FhirConceptServiceImpl fhirConceptService;
	
	@Before
	public void setup() {
		fhirConceptService = new FhirConceptServiceImpl();
		fhirConceptService.setDao(conceptDao);
	}
	
	@Test
	public void getConceptByUuid_shouldGetConceptByUuid() {
		Concept concept = new Concept();
		concept.setUuid(CONCEPT_UUID);
		when(conceptDao.get(CONCEPT_UUID)).thenReturn(concept);
		
		Concept result = fhirConceptService.get(CONCEPT_UUID);
		
		assertThat(result, notNullValue());
		assertThat(result.getUuid(), equalTo(CONCEPT_UUID));
	}
	
	@Test
	public void getConceptByUuid_shouldReturnNullWhenConceptUuidNotFound() {
		when(conceptDao.get(BAD_CONCEPT_UUID)).thenReturn(null);
		
		Concept result = fhirConceptService.get(BAD_CONCEPT_UUID);
		
		assertThat(result, nullValue());
	}
	
	@Test
	public void getConceptWithSameAsMappingInSource_shouldGetConceptBySourceNameAndCode() {
		ConceptSource loinc = new ConceptSource();
		Concept concept = new Concept();
		concept.setUuid(CONCEPT_UUID);
		when(conceptDao.getConceptWithSameAsMappingInSource(loinc, "1000-1")).thenReturn(concept);
		
		Concept result = fhirConceptService.getConceptWithSameAsMappingInSource(loinc, "1000-1");
		assertThat(result, notNullValue());
		assertThat(result, equalTo(concept));
	}
	
	@Test
	public void getConceptWithSameAsMappingInSource_shouldReturnNullIfSourceIsNull() {
		Concept result = fhirConceptService.getConceptWithSameAsMappingInSource(null, "1000-1");
		assertThat(result, nullValue());
	}
}
