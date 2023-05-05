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

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.openmrs.test.OpenmrsMatchers.hasId;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import ca.uhn.fhir.rest.param.TokenAndListParam;
import ca.uhn.fhir.rest.param.TokenParam;
import org.hibernate.SessionFactory;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.DrugOrder;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.TestFhirSpringConfiguration;
import org.openmrs.module.fhir2.api.search.param.SearchParameterMap;
import org.openmrs.test.BaseModuleContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;

@ContextConfiguration(classes = TestFhirSpringConfiguration.class, inheritLocations = false)
public class FhirMedicationRequestDaoImplTest extends BaseModuleContextSensitiveTest {
	
	private static final String DRUG_ORDER_UUID = "6d0ae116-707a-4629-9850-f15206e63ab0";
	
	private static final String BAD_DRUG_ORDER_UUID = "uie3b9a2-4de5-4b12-ac40-jk90sdh";
	
	private static final Integer DISCONTINUE_ORDER_ID = 1008;
	
	private static final String DISCONTINUE_ORDER_UUID = "b951a436-c775-4dfc-9432-e19446d18c28";
	
	private static final String MEDICATION_REQUEST_INITIAL_DATA_XML = "org/openmrs/module/fhir2/api/dao/impl/FhirMedicationRequestDaoImpl_initial_data.xml";
	
	private static final String MEDICATION_REQUEST_CONCEPT_ID = "4020";
	
	@Autowired
	@Qualifier("sessionFactory")
	private SessionFactory sessionFactory;
	
	private FhirMedicationRequestDaoImpl medicationRequestDao;
	
	@Before
	public void setup() throws Exception {
		medicationRequestDao = new FhirMedicationRequestDaoImpl();
		medicationRequestDao.setSessionFactory(sessionFactory);
		executeDataSet(MEDICATION_REQUEST_INITIAL_DATA_XML);
	}
	
	@Test
	public void getMedicationRequestByUuid_shouldGetByUuid() {
		DrugOrder drugOrder = medicationRequestDao.get(DRUG_ORDER_UUID);
		assertThat(drugOrder, notNullValue());
		assertThat(drugOrder.getUuid(), notNullValue());
		assertThat(drugOrder.getUuid(), equalTo(DRUG_ORDER_UUID));
	}
	
	@Test
	public void getMedicationRequestByUuid_shouldReturnNullWhenCalledWithBadUuid() {
		DrugOrder drugOrder = medicationRequestDao.get(BAD_DRUG_ORDER_UUID);
		assertThat(drugOrder, nullValue());
	}
	
	@Test
	public void getMedicationRequestByUuids_shouldReturnEmptyListWhenCalledWithBadUuid() {
		List<DrugOrder> drugOrders = medicationRequestDao.get(Collections.singletonList(BAD_DRUG_ORDER_UUID));
		assertThat(drugOrders.size(), is(0));
	}
	
	@Test
	public void getMedicationRequestByUuid_shouldReturnNullWhenRequestingDiscontinueOrder() {
		DrugOrder drugOrder = medicationRequestDao.get(DISCONTINUE_ORDER_UUID);
		assertThat(drugOrder, nullValue());
	}
	
	@Test
	public void getMedicationRequestsByUuid_shouldNotReturnDiscontinueOrders() {
		List<DrugOrder> drugOrders = medicationRequestDao.get(Arrays.asList(DRUG_ORDER_UUID, DISCONTINUE_ORDER_UUID));
		assertThat(drugOrders.size(), is(1));
		assertThat(drugOrders.get(0).getUuid(), is(DRUG_ORDER_UUID));
	}
	
	@Test
	public void getMedicationRequestsBySearchResults_shouldNotReturnDiscontinuedOrders() {
		SearchParameterMap theParams = new SearchParameterMap();
		theParams.setToIndex(20);
		List<DrugOrder> drugOrders = medicationRequestDao.getSearchResults(theParams);
		assertThat(drugOrders, not(hasItems(hasId(DISCONTINUE_ORDER_ID))));
		assertThat(drugOrders, hasSize(11));
	}
	
	@Test
	public void search_shouldReturnSearchQuery() {
		TokenAndListParam code = new TokenAndListParam();
		TokenParam codingToken = new TokenParam();
		codingToken.setValue(MEDICATION_REQUEST_CONCEPT_ID);
		code.addAnd(codingToken);
		
		SearchParameterMap theParams = new SearchParameterMap();
		theParams.addParameter(FhirConstants.CODED_SEARCH_HANDLER, code);
		
		Collection<DrugOrder> drugOrders = medicationRequestDao.getSearchResults(theParams);
		
		assertThat(drugOrders, notNullValue());
		assertThat(drugOrders, hasSize(greaterThanOrEqualTo(1)));
	}
	
}
