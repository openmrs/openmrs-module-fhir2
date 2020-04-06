/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.fhir2.api.search.param;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;

import ca.uhn.fhir.rest.api.SortOrderEnum;
import ca.uhn.fhir.rest.api.SortSpec;
import ca.uhn.fhir.rest.param.ReferenceParam;
import ca.uhn.fhir.rest.param.StringAndListParam;
import ca.uhn.fhir.rest.param.StringOrListParam;
import ca.uhn.fhir.rest.param.StringParam;
import org.junit.Before;
import org.junit.Test;

public class SearchParameterMapTest {
	
	private static final String ENCOUNTER_DATETIME = "encounterDatetime";
	
	private static final String REF_PARAM = "refParam";
	
	private static final String NAME = "name";
	
	private SearchParameterMap searchParam;
	
	@Before
	public void setup() {
		searchParam = new SearchParameterMap();
	}
	
	@Test
	public void shouldAddStringAndListParamIntoTheMap() {
		StringAndListParam andListParam = new StringAndListParam();
		andListParam.addAnd(new StringParam("John"));
		searchParam.addAndParam(NAME, andListParam);
		
		assertThat(searchParam, notNullValue());
		assertThat(searchParam.getAndParams(NAME), not(empty()));
		assertThat(searchParam.getAndParams(NAME), hasSize(is(1)));
	}
	
	@Test
	public void shouldAddStringAndListParamIntoAnExistingMapUsingTheSameKeyValue() {
		StringAndListParam andListParam1 = new StringAndListParam();
		andListParam1.addAnd(new StringParam("John"));
		
		StringAndListParam andListParam2 = new StringAndListParam();
		andListParam2.addAnd(new StringParam("Joe"));
		
		searchParam.addAndParam(NAME, andListParam1);
		searchParam.addAndParam(NAME, andListParam2);
		
		assertThat(searchParam, notNullValue());
		assertThat(searchParam.getAndParams(NAME), not(empty()));
		assertThat(searchParam.getAndParams(NAME), hasSize(is(2)));
	}
	
	@Test
	public void shouldAddStringOrListParamIntoTheMap() {
		StringOrListParam orListParam = new StringOrListParam();
		orListParam.addOr(new StringParam("Clement"));
		searchParam.addOrParam(NAME, orListParam);
		
		assertThat(searchParam, notNullValue());
		assertThat(searchParam.getOrParams(NAME), not(empty()));
		assertThat(searchParam.getOrParams(NAME), hasSize(is(1)));
	}
	
	@Test
	public void shouldAddStringOrListParamIntoAnExistingMapUsingTheSameKeyValue() {
		StringOrListParam orListParam1 = new StringOrListParam();
		orListParam1.addOr(new StringParam("Clement"));
		
		StringOrListParam orListParam2 = new StringOrListParam();
		orListParam2.addOr(new StringParam("Jane"));
		
		searchParam.addOrParam(NAME, orListParam1);
		searchParam.addOrParam(NAME, orListParam2);
		
		assertThat(searchParam, notNullValue());
		assertThat(searchParam.getOrParams(NAME), not(empty()));
		assertThat(searchParam.getOrParams(NAME), hasSize(is(2)));
	}
	
	@Test
	public void shouldAddReferenceParamIntoLinkedHashMap() {
		ReferenceParam referenceParam = new ReferenceParam();
		referenceParam.setValue("referenceParamValue");
		searchParam.addReferenceParam(REF_PARAM, referenceParam);
		
		assertThat(searchParam, notNullValue());
		assertThat(searchParam.getReferenceParams(REF_PARAM), not(empty()));
		assertThat(searchParam.getReferenceParams(REF_PARAM), hasSize(equalTo(1)));
	}
	
	@Test
	public void shouldAddReferenceParamIntoAnExistingLinkedHashMapUsingSameKey() {
		ReferenceParam referenceParam1 = new ReferenceParam();
		referenceParam1.setValue("referenceParamValue");
		ReferenceParam referenceParam2 = new ReferenceParam();
		referenceParam2.setValue("referenceParamValue");
		
		searchParam.addReferenceParam(REF_PARAM, referenceParam1);
		searchParam.addReferenceParam(REF_PARAM, referenceParam2);
		
		assertThat(searchParam, notNullValue());
		assertThat(searchParam.getReferenceParams(REF_PARAM), not(empty()));
		assertThat(searchParam.getReferenceParams(REF_PARAM), hasSize(equalTo(2)));
	}
	
	@Test
	public void shouldSetSortSpec() {
		SortSpec sort = new SortSpec();
		sort.setOrder(SortOrderEnum.ASC);
		sort.setParamName(ENCOUNTER_DATETIME);
		searchParam.setSortSpec(sort);
		
		assertThat(searchParam, notNullValue());
		assertThat(searchParam.getSortSpec(), notNullValue());
		assertThat(searchParam.getSortSpec().getOrder(), equalTo(SortOrderEnum.ASC));
		assertThat(searchParam.getSortSpec().getParamName(), equalTo(ENCOUNTER_DATETIME));
	}
	
	@Test
	public void shouldSetCount() {
		searchParam.setCount(10);
		assertThat(searchParam.getCount(), notNullValue());
		assertThat(searchParam.getCount(), equalTo(10));
	}
}
