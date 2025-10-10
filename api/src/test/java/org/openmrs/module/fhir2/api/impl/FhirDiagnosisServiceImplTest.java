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
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import ca.uhn.fhir.rest.api.server.IBundleProvider;
import org.hl7.fhir.r4.model.Condition;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openmrs.Diagnosis;
import org.openmrs.module.fhir2.api.dao.FhirDiagnosisDao;
import org.openmrs.module.fhir2.api.search.SearchQuery;
import org.openmrs.module.fhir2.api.search.SearchQueryInclude;
import org.openmrs.module.fhir2.api.search.param.DiagnosisSearchParams;
import org.openmrs.module.fhir2.api.search.param.SearchParameterMap;
import org.openmrs.module.fhir2.api.translators.DiagnosisTranslator;

@RunWith(MockitoJUnitRunner.class)
public class FhirDiagnosisServiceImplTest {
	
	@Mock
	private FhirDiagnosisDao dao;
	
	@Mock
	private DiagnosisTranslator translator;
	
	@Mock
	private SearchQueryInclude<Condition> searchQueryInclude;
	
	@Mock
	private SearchQuery<Diagnosis, Condition, FhirDiagnosisDao, DiagnosisTranslator, SearchQueryInclude<Condition>> searchQuery;
	
	private FhirDiagnosisServiceImpl diagnosisService;
	
	@Before
	public void setup() {
		diagnosisService = new FhirDiagnosisServiceImpl() {
			
			@Override
			protected void validateObject(Diagnosis object) {
			}
		};
		diagnosisService.setDao(dao);
		diagnosisService.setTranslator(translator);
		diagnosisService.setSearchQuery(searchQuery);
		diagnosisService.setSearchQueryInclude(searchQueryInclude);
	}
	
	@Test
	public void searchDiagnoses_shouldDelegateToSearchQuery() {
		DiagnosisSearchParams params = spy(DiagnosisSearchParams.builder().build());
		SearchParameterMap expectedParams = new SearchParameterMap();
		doReturn(expectedParams).when(params).toSearchParameterMap();
		IBundleProvider provider = mock(IBundleProvider.class);
		when(searchQuery.getQueryResults(expectedParams, dao, translator, searchQueryInclude)).thenReturn(provider);
		
		IBundleProvider result = diagnosisService.searchDiagnoses(params);
		assertThat(result, equalTo(provider));
	}
}
