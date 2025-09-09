/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.fhir2.api.search;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.exparity.hamcrest.date.DateMatchers;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.instance.model.api.IPrimitiveType;
import org.hl7.fhir.r4.model.Observation;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openmrs.Obs;
import org.openmrs.module.fhir2.api.FhirGlobalPropertyService;
import org.openmrs.module.fhir2.api.dao.FhirObservationDao;
import org.openmrs.module.fhir2.api.search.param.SearchParameterMap;
import org.openmrs.module.fhir2.api.translators.ObservationTranslator;

@RunWith(MockitoJUnitRunner.class)
public class SearchQueryBundleProviderTest {
	
	@Mock
	private ObservationTranslator translator;
	
	@Mock
	private FhirObservationDao observationDao;
	
	@Mock
	private FhirGlobalPropertyService globalPropertyService;
	
	@Mock
	private SearchQueryInclude<Observation> searchQueryInclude;
	
	private SearchQueryBundleProvider<Obs, Observation> searchQueryBundleProvider;
	
	@Before
	public void setup() {
		searchQueryBundleProvider = new SearchQueryBundleProvider<>(new SearchParameterMap(), observationDao, translator,
		        globalPropertyService, searchQueryInclude);
	}
	
	@Test
	public void shouldReturnPreferredPageSize() {
		when(globalPropertyService.getGlobalPropertyAsInteger(anyString(), anyInt())).thenReturn(10);
		
		assertThat(searchQueryBundleProvider.preferredPageSize(), notNullValue());
		assertThat(searchQueryBundleProvider.preferredPageSize(), equalTo(10));
	}
	
	@Test
	public void shouldGetDatePublished() {
		IPrimitiveType<Date> result = searchQueryBundleProvider.getPublished();
		assertThat(result, notNullValue());
		assertThat(result.getValue(), DateMatchers.sameDay(new Date()));
	}
	
	@Test
	public void shouldReturnEmptyListWhenNoResults() {
		when(observationDao.getSearchResults(any())).thenReturn(Collections.emptyList());
		List<IBaseResource> resources = searchQueryBundleProvider.getResources(0, 10);
		assertThat(resources, empty());
	}
	
	@Test
	public void shouldReturnDifferentUuid() {
		assertThat(searchQueryBundleProvider.getUuid(), notNullValue());
		assertThat(searchQueryBundleProvider.getUuid(), not(equalTo(new SearchQueryBundleProvider<>(new SearchParameterMap(),
		        observationDao, translator, globalPropertyService, searchQueryInclude).getUuid())));
	}
}
