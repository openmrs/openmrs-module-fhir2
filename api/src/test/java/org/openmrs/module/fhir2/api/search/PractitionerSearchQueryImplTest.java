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
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;

import java.util.List;
import java.util.stream.Collectors;

import ca.uhn.fhir.rest.api.server.IBundleProvider;
import ca.uhn.fhir.rest.param.StringAndListParam;
import ca.uhn.fhir.rest.param.StringOrListParam;
import ca.uhn.fhir.rest.param.StringParam;
import ca.uhn.fhir.rest.param.TokenAndListParam;
import ca.uhn.fhir.rest.param.TokenOrListParam;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.Practitioner;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.Provider;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.TestFhirSpringConfiguration;
import org.openmrs.module.fhir2.api.dao.FhirPractitionerDao;
import org.openmrs.module.fhir2.api.search.param.SearchParameterMap;
import org.openmrs.module.fhir2.api.translators.PractitionerTranslator;
import org.openmrs.test.BaseModuleContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

@ContextConfiguration(classes = TestFhirSpringConfiguration.class, inheritLocations = false)
public class PractitionerSearchQueryImplTest extends BaseModuleContextSensitiveTest {
	
	private static final String PRACTITIONER_INITIAL_DATA_XML = "org/openmrs/module/fhir2/api/dao/impl/FhirPractitionerDaoImplTest_initial_data.xml";
	
	private static final String PRACTITIONER_UUID = "f9badd80-ab76-11e2-9e96-0800200c9a66";
	
	private static final String PRACTITIONER_NAME = "ricky";
	
	private static final String PRACTITIONER_IDENTIFIER = "347834-gf";
	
	private static final String NOT_FOUND_PRACTITIONER_NAME = "waf";
	
	private static final String NOT_FOUND_PRACTITIONER_IDENTIFIER = "38934-t";
	
	private static final int START_INDEX = 0;
	
	private static final int END_INDEX = 10;
	
	@Autowired
	private FhirPractitionerDao dao;
	
	@Autowired
	private PractitionerTranslator<Provider> translator;
	
	@Autowired
	private SearchQuery<Provider, Practitioner, FhirPractitionerDao, PractitionerTranslator<Provider>> searchQuery;
	
	@Before
	public void setUp() throws Exception {
		executeDataSet(PRACTITIONER_INITIAL_DATA_XML);
	}
	
	private IBundleProvider search(SearchParameterMap theParams) {
		return searchQuery.getQueryResults(theParams, dao, translator);
	}
	
	private List<IBaseResource> get(IBundleProvider results) {
		return results.getResources(START_INDEX, END_INDEX);
	}
	
	@Test
	public void searchForPractitioners_shouldReturnPractitionersByName() {
		StringAndListParam name = new StringAndListParam()
		        .addAnd(new StringOrListParam().add(new StringParam(PRACTITIONER_NAME)));
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.NAME_SEARCH_HANDLER, name);
		
		IBundleProvider results = search(theParams);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList, not(empty()));
		assertThat(resultList.size(), equalTo(1));
		assertThat(resultList.iterator().next().getIdElement().getIdPart(), equalTo(PRACTITIONER_UUID));
	}
	
	@Test
	public void searchForPractitioners_shouldReturnEmptyCollectionWhenNameNotMatched() {
		StringAndListParam name = new StringAndListParam()
		        .addAnd(new StringOrListParam().add(new StringParam(NOT_FOUND_PRACTITIONER_NAME)));
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.NAME_SEARCH_HANDLER, name);
		
		IBundleProvider results = search(theParams);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList, is(empty()));
	}
	
	@Test
	public void searchForPractitioners_shouldReturnPractitionersByIdentifier() {
		TokenAndListParam identifier = new TokenAndListParam().addAnd(new TokenOrListParam().add(PRACTITIONER_IDENTIFIER));
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.IDENTIFIER_SEARCH_HANDLER,
		    identifier);
		
		IBundleProvider results = search(theParams);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList, not(empty()));
		assertThat(resultList.size(), greaterThanOrEqualTo(1));
		assertThat(((Practitioner) resultList.iterator().next()).getIdentifierFirstRep().getValue(),
		    equalTo(PRACTITIONER_IDENTIFIER));
	}
	
	@Test
	public void searchForPractitioners_shouldReturnEmptyCollectionWhenIdentifierNotMatched() {
		TokenAndListParam identifier = new TokenAndListParam()
		        .addAnd(new TokenOrListParam().add(NOT_FOUND_PRACTITIONER_IDENTIFIER));
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.IDENTIFIER_SEARCH_HANDLER,
		    identifier);
		
		IBundleProvider results = search(theParams);
		
		List<Practitioner> resultList = get(results).stream().map(p -> (Practitioner) p).collect(Collectors.toList());
		
		assertThat(results, notNullValue());
		assertThat(resultList, is(empty()));
	}
	
	@Test
	public void searchForPractitioners_shouldHandleComplexQuery() {
		StringAndListParam name = new StringAndListParam()
		        .addAnd(new StringOrListParam().add(new StringParam(PRACTITIONER_NAME)));
		TokenAndListParam identifier = new TokenAndListParam().addAnd(new TokenOrListParam().add(PRACTITIONER_IDENTIFIER));
		
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.NAME_SEARCH_HANDLER, name)
		        .addParameter(FhirConstants.IDENTIFIER_SEARCH_HANDLER, identifier);
		
		IBundleProvider results = search(theParams);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList, not(empty()));
		assertThat(resultList.size(), equalTo(1));
		assertThat(((Practitioner) resultList.iterator().next()).getIdentifierFirstRep().getValue(),
		    equalTo(PRACTITIONER_IDENTIFIER));
		assertThat(resultList.iterator().next().getIdElement().getIdPart(), equalTo(PRACTITIONER_UUID));
	}
	
}
