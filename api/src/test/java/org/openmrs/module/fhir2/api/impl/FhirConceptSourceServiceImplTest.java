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
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;

import com.google.common.collect.Lists;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openmrs.module.fhir2.api.dao.FhirConceptSourceDao;
import org.openmrs.module.fhir2.model.FhirConceptSource;

@RunWith(MockitoJUnitRunner.class)
public class FhirConceptSourceServiceImplTest {
	
	@Mock
	private FhirConceptSourceDao dao;
	
	private FhirConceptSourceServiceImpl fhirConceptSourceService;
	
	@Before
	public void setup() {
		fhirConceptSourceService = new FhirConceptSourceServiceImpl();
		fhirConceptSourceService.setDao(dao);
	}
	
	@Test
	public void getFhirConceptSources_shouldReturnFhirConceptSources() {
		Collection<FhirConceptSource> sources = Lists.newArrayList(new FhirConceptSource(), new FhirConceptSource());
		when(dao.getFhirConceptSources()).thenReturn(sources);
		
		Collection<FhirConceptSource> result = fhirConceptSourceService.getFhirConceptSources();
		
		assertThat(result, notNullValue());
		assertThat(result, not(empty()));
	}
	
	@Test
	public void getFhirConceptSources_shouldReturnEmptyCollectionWhenNoSourcesFound() {
		when(dao.getFhirConceptSources()).thenReturn(new ArrayList<>());
		
		Collection<FhirConceptSource> result = fhirConceptSourceService.getFhirConceptSources();
		
		assertThat(result, notNullValue());
		assertThat(result, empty());
	}
	
	@Test
	public void getFhirConceptSourceByUrl_shouldReturnConceptSourceForUrl() {
		FhirConceptSource source = new FhirConceptSource();
		when(dao.getFhirConceptSourceByUrl("http://www.example.com")).thenReturn(Optional.of(source));
		
		Optional<FhirConceptSource> result = fhirConceptSourceService.getFhirConceptSourceByUrl("http://www.example.com");
		
		assertThat(result.isPresent(), is(true));
		assertThat(result.get(), equalTo(source));
	}
	
	@Test
	public void getFhirConceptSourceByUrl_shouldReturnEmptyWhenNoConceptSourceFound() {
		when(dao.getFhirConceptSourceByUrl("http://www.example.com")).thenReturn(Optional.empty());
		
		Optional<FhirConceptSource> result = fhirConceptSourceService.getFhirConceptSourceByUrl("http://www.example.com");
		
		assertThat(result.isPresent(), is(false));
	}
	
	@Test
	public void getFhirConceptSourceByConceptSourceName_shouldReturnConceptSourceForName() {
		FhirConceptSource source = new FhirConceptSource();
		when(dao.getFhirConceptSourceByConceptSourceName("LOINC")).thenReturn(Optional.of(source));
		
		Optional<FhirConceptSource> result = fhirConceptSourceService.getFhirConceptSourceByConceptSourceName("LOINC", null);
		
		assertThat(result.isPresent(), is(true));
		assertThat(result.get(), equalTo(source));
	}
	
	@Test
	public void getFhirConceptSourceByConceptSourceName_shouldReturnEmptyWhenNoConceptSourceFound() {
		when(dao.getFhirConceptSourceByConceptSourceName("LOINC")).thenReturn(Optional.empty());
		
		Optional<FhirConceptSource> result = fhirConceptSourceService.getFhirConceptSourceByConceptSourceName("LOINC", null);
		
		assertThat(result.isPresent(), is(false));
	}
}
