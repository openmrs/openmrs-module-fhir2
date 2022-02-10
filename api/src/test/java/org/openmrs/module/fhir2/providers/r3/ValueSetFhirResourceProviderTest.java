/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.fhir2.providers.r3;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import ca.uhn.fhir.rest.api.server.IBundleProvider;
import ca.uhn.fhir.rest.param.StringAndListParam;
import ca.uhn.fhir.rest.param.StringOrListParam;
import ca.uhn.fhir.rest.param.StringParam;
import org.hl7.fhir.dstu3.model.IdType;
import org.hl7.fhir.dstu3.model.ValueSet;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openmrs.module.fhir2.api.FhirValueSetService;

@RunWith(MockitoJUnitRunner.class)
public class ValueSetFhirResourceProviderTest {
	
	private static final String ROOT_CONCEPT_UUID = "0f97e14e-cdc2-49ac-9255-b5126f8a5147";
	
	private static final String ROOT_CONCEPT_NAME = "FOOD CONSTRUCT";
	
	private static final int PREFERRED_SIZE = 10;
	
	private static final int COUNT = 1;
	
	private static final int START_INDEX = 0;
	
	private static final int END_INDEX = 10;
	
	@Mock
	private FhirValueSetService fhirValueSetService;
	
	private ValueSetFhirResourceProvider valueSetFhirResourceProvider;
	
	private ValueSet valueSet;

	private org.hl7.fhir.r4.model.ValueSet valueSet4;
	
	private List<IBaseResource> get(IBundleProvider results) {
		return results.getResources(START_INDEX, END_INDEX);
	}
	
	@Before
	public void setup() {
		valueSetFhirResourceProvider = new ValueSetFhirResourceProvider();
		valueSetFhirResourceProvider.setFhirValueSetService(fhirValueSetService);
		
		valueSet = new ValueSet();
		valueSet.setId(ROOT_CONCEPT_UUID);

		valueSet4 = new org.hl7.fhir.r4.model.ValueSet();
		valueSet4.setId(ROOT_CONCEPT_UUID);
	}

	@Test
	public void getValueSetByUuid_shouldReturnMatchingValueSet() {
		IdType id = new IdType();
		id.setValue(ROOT_CONCEPT_UUID);
		when(fhirValueSetService.get(ROOT_CONCEPT_UUID)).thenReturn(valueSet4);
		ValueSet result = valueSetFhirResourceProvider.getValueSetByUuid(id);
		assertThat(result, notNullValue());
		assertThat(result.getId(), notNullValue());
		assertThat(result.getId(), equalTo(ROOT_CONCEPT_UUID));
	}
	
	@Test
	public void searchValueSets_shouldReturnMatchingConceptSetsWhenTitleParamIsSpecified() {
		List<ValueSet> valueSets = new ArrayList<>();
		valueSets.add(valueSet);
		when(fhirValueSetService.searchForValueSets(any()))
		        .thenReturn(new MockIBundleProvider<>(valueSets, PREFERRED_SIZE, COUNT));
		
		StringAndListParam titleParam = new StringAndListParam()
		        .addAnd(new StringOrListParam().add(new StringParam(ROOT_CONCEPT_NAME)));
		
		IBundleProvider results = valueSetFhirResourceProvider.searchValueSets(titleParam);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList, hasSize(greaterThanOrEqualTo(1)));
		assertThat(((ValueSet) resultList.iterator().next()).getId(), equalTo(ROOT_CONCEPT_UUID));
	}
	
}
