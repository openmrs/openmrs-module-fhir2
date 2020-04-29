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
import ca.uhn.fhir.rest.param.TokenAndListParam;
import ca.uhn.fhir.rest.param.TokenParam;
import org.junit.Before;
import org.junit.Test;

public class SearchParameterMapTest {
	
	private static final String ENCOUNTER_DATETIME = "encounterDatetime";
	
	private static final String REF_PARAM = "refParam";
	
	private static final String NAME = "name";
	
	private static final String TOKEN = "token";
	
	private SearchParameterMap searchParam;
	
	@Before
	public void setup() {
		searchParam = new SearchParameterMap();
	}
	
	@Test
	public void shouldAddStringAndListParamIntoTheMap() {
		StringAndListParam andListParam = new StringAndListParam();
		andListParam.addAnd(new StringParam("John"));
		searchParam.addParameter(NAME, andListParam);
		
		assertThat(searchParam, notNullValue());
		assertThat(searchParam.getParameters(NAME), not(empty()));
		assertThat(searchParam.getParameters(NAME), hasSize(is(1)));
	}
	
	@Test
	public void shouldAddStringAndListParamIntoAnExistingMapUsingTheSameKeyValue() {
		StringAndListParam andListParam1 = new StringAndListParam();
		andListParam1.addAnd(new StringParam("John"));
		
		StringAndListParam andListParam2 = new StringAndListParam();
		andListParam2.addAnd(new StringParam("Joe"));
		
		searchParam.addParameter(NAME, andListParam1);
		searchParam.addParameter(NAME, andListParam2);
		
		assertThat(searchParam, notNullValue());
		assertThat(searchParam.getParameters(NAME), not(empty()));
		assertThat(searchParam.getParameters(NAME), hasSize(is(2)));
	}
	
	@Test
	public void shouldAddStringOrListParamIntoTheMap() {
		StringOrListParam orListParam = new StringOrListParam();
		orListParam.addOr(new StringParam("Clement"));
		searchParam.addParameter(NAME, orListParam);
		
		assertThat(searchParam, notNullValue());
		assertThat(searchParam.getParameters(NAME), not(empty()));
		assertThat(searchParam.getParameters(NAME), hasSize(is(1)));
	}
	
	@Test
	public void shouldAddStringOrListParamIntoAnExistingMapUsingTheSameKeyValue() {
		StringOrListParam orListParam1 = new StringOrListParam();
		orListParam1.addOr(new StringParam("Clement"));
		
		StringOrListParam orListParam2 = new StringOrListParam();
		orListParam2.addOr(new StringParam("Jane"));
		
		searchParam.addParameter(NAME, orListParam1);
		searchParam.addParameter(NAME, orListParam2);
		
		assertThat(searchParam, notNullValue());
		assertThat(searchParam.getParameters(NAME), not(empty()));
		assertThat(searchParam.getParameters(NAME), hasSize(is(2)));
	}
	
	@Test
	public void shouldAddTokenAndListParamIntoTheMap() {
		TokenAndListParam tokenAndListParam = new TokenAndListParam();
		tokenAndListParam.addAnd(new TokenParam("5089"));
		searchParam.addParameter(TOKEN, tokenAndListParam);
		
		assertThat(searchParam, notNullValue());
		assertThat(searchParam.getParameters(TOKEN), not(empty()));
		assertThat(searchParam.getParameters(TOKEN), hasSize(is(1)));
	}
	
	@Test
	public void shouldAddTokenAddListParamIntoAnExistingMapUsingTheSameKeyValue() {
		TokenAndListParam tokenAndListParam1 = new TokenAndListParam();
		tokenAndListParam1.addAnd(new TokenParam("5089"));
		
		TokenAndListParam tokenAndListParam2 = new TokenAndListParam();
		tokenAndListParam2.addAnd(new TokenParam("5087"));
		
		searchParam.addParameter(TOKEN, tokenAndListParam1);
		searchParam.addParameter(TOKEN, tokenAndListParam2);
		
		assertThat(searchParam, notNullValue());
		assertThat(searchParam.getParameters(TOKEN), not(empty()));
		assertThat(searchParam.getParameters(TOKEN), hasSize(is(2)));
	}
	
	@Test
	public void shouldAddReferenceParamIntoLinkedHashMap() {
		ReferenceParam referenceParam = new ReferenceParam();
		referenceParam.setValue("referenceParamValue");
		searchParam.addParameter(REF_PARAM, referenceParam);
		
		assertThat(searchParam, notNullValue());
		assertThat(searchParam.getParameters(REF_PARAM), not(empty()));
		assertThat(searchParam.getParameters(REF_PARAM), hasSize(equalTo(1)));
	}
	
	@Test
	public void shouldAddReferenceParamIntoAnExistingLinkedHashMapUsingSameKey() {
		ReferenceParam referenceParam1 = new ReferenceParam();
		referenceParam1.setValue("referenceParamValue");
		ReferenceParam referenceParam2 = new ReferenceParam();
		referenceParam2.setValue("referenceParamValue");
		
		searchParam.addParameter(REF_PARAM, referenceParam1);
		searchParam.addParameter(REF_PARAM, referenceParam2);
		
		assertThat(searchParam, notNullValue());
		assertThat(searchParam.getParameters(REF_PARAM), not(empty()));
		assertThat(searchParam.getParameters(REF_PARAM), hasSize(equalTo(2)));
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
}
