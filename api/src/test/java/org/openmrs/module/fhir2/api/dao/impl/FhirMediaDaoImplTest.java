/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.fhir2.api.dao.impl;

import ca.uhn.fhir.rest.param.StringAndListParam;
import ca.uhn.fhir.rest.param.StringParam;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.Obs;
import org.openmrs.api.ObsService;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.TestFhirSpringConfiguration;
import org.openmrs.module.fhir2.api.dao.FhirMediaDao;
import org.openmrs.module.fhir2.api.search.param.SearchParameterMap;
import org.openmrs.test.BaseModuleContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

import java.util.Collection;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

@ContextConfiguration(classes = TestFhirSpringConfiguration.class, inheritLocations = false)
public class FhirMediaDaoImplTest extends BaseModuleContextSensitiveTest {
	
	private static final String OBS_DATA_XML = "org/openmrs/module/fhir2/api/dao/impl/FhirObservationDaoImplTest_initial_data_suppl.xml";
	
	private static final String OBS_UUID = "759a0d9e-ccf8-4f00-a045-6a94c43fbd6b";
	
	private static final String OBS_CONCEPT_ID = "5242";
	
	private ObsService obsService;
	
	@Autowired
	FhirMediaDao dao;
	
	@Before
	public void setup() throws Exception {
		executeDataSet(OBS_DATA_XML);
		dao = new FhirMediaDaoImpl();
	}
	
	@Test
	public void get_shouldGetComplexObsByUuid() {
		assertThat(dao.get(OBS_UUID), notNullValue());
	}
	
	@Test
	public void get_shouldReturnNullIfObsNotFoundByUuid() {
		Obs obs = dao.get(OBS_UUID);
		
		assertThat(obs, nullValue());
	}
	
	@Test
	public void search_ShouldReturnSearchQuery() {
		StringAndListParam status = new StringAndListParam();
		StringParam codingToken = new StringParam();
		codingToken.setValue(OBS_CONCEPT_ID);
		status.addAnd(codingToken);
		
		SearchParameterMap theParams = new SearchParameterMap();
		theParams.addParameter(FhirConstants.MEDIA_CONTENT_TYPE, status);
		
		List<String> matchingResourceUuids = dao.getSearchResultUuids(theParams);
		Collection<Obs> obs = dao.getSearchResults(theParams, matchingResourceUuids);
		
		assertThat(obs, notNullValue());
	}
}
