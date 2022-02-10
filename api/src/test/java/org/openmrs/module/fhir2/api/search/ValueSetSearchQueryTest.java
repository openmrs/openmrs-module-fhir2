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
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;

import java.util.List;

import ca.uhn.fhir.rest.api.server.IBundleProvider;
import ca.uhn.fhir.rest.param.StringAndListParam;
import ca.uhn.fhir.rest.param.StringOrListParam;
import ca.uhn.fhir.rest.param.StringParam;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.ValueSet;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.Concept;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.TestFhirSpringConfiguration;
import org.openmrs.module.fhir2.api.dao.FhirConceptDao;
import org.openmrs.module.fhir2.api.search.param.SearchParameterMap;
import org.openmrs.module.fhir2.api.translators.ValueSetTranslator;
import org.openmrs.test.BaseModuleContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

@ContextConfiguration(classes = TestFhirSpringConfiguration.class, inheritLocations = false)
public class ValueSetSearchQueryTest extends BaseModuleContextSensitiveTest {
	
	private static final String CONCEPT_INITIAL_DATA_XML = "org/openmrs/module/fhir2/api/dao/impl/FhirConceptDaoImplTest_initial_data.xml";
	
	private static final String ROOT_CONCEPT_UUID = "0f97e14e-cdc2-49ac-9255-b5126f8a5147";
	
	private static final String ROOT_CONCEPT_NAME = "FOOD CONSTRUCT";
	
	private static final int START_INDEX = 0;
	
	private static final int END_INDEX = 10;
	
	@Autowired
	private FhirConceptDao dao;
	
	@Autowired
	private ValueSetTranslator translator;
	
	@Autowired
	private SearchQueryInclude<ValueSet> searchQueryInclude;
	
	@Autowired
	SearchQuery<Concept, ValueSet, FhirConceptDao, ValueSetTranslator, SearchQueryInclude<ValueSet>> searchQuery;
	
	private List<IBaseResource> get(IBundleProvider results) {
		return results.getResources(START_INDEX, END_INDEX);
	}
	
	private IBundleProvider search(SearchParameterMap theParams) {
		return searchQuery.getQueryResults(theParams, dao, translator, searchQueryInclude);
	}
	
	@Before
	public void setup() throws Exception {
		executeDataSet(CONCEPT_INITIAL_DATA_XML);
	}
	
	@Test
	public void searchForConceptSets_shouldSearchForConceptSetsByTitle() {
		StringAndListParam root_concept = new StringAndListParam()
		        .addAnd(new StringOrListParam().add(new StringParam(ROOT_CONCEPT_NAME)));
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.TITLE_SEARCH_HANDLER,
		    root_concept);
		IBundleProvider results = search(theParams);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList, not(empty()));
		assertThat(resultList, hasSize(greaterThanOrEqualTo(1)));
		assertThat(((ValueSet) resultList.iterator().next()).getId(), equalTo(ROOT_CONCEPT_UUID));
	}
	
	@Test
	public void searchForConceptSets_shouldReturnEmptyListOfValueSet() {
		StringAndListParam root_concept = new StringAndListParam()
		        .addAnd(new StringOrListParam().add(new StringParam("wrong")));
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.TITLE_SEARCH_HANDLER,
		    root_concept);
		IBundleProvider results = search(theParams);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList, empty());
	}
}
