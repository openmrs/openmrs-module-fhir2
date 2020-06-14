/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.fhir2.providers.r4;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;

import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.api.SortSpec;
import ca.uhn.fhir.rest.api.server.IBundleProvider;
import ca.uhn.fhir.rest.param.DateRangeParam;
import ca.uhn.fhir.rest.param.QuantityAndListParam;
import ca.uhn.fhir.rest.param.QuantityOrListParam;
import ca.uhn.fhir.rest.param.QuantityParam;
import ca.uhn.fhir.rest.param.ReferenceAndListParam;
import ca.uhn.fhir.rest.param.ReferenceOrListParam;
import ca.uhn.fhir.rest.param.ReferenceParam;
import ca.uhn.fhir.rest.param.TokenAndListParam;
import ca.uhn.fhir.rest.param.TokenOrListParam;
import ca.uhn.fhir.rest.param.TokenParam;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import org.hamcrest.Matchers;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.Condition;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.Provenance;
import org.hl7.fhir.r4.model.Resource;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.api.FhirConditionService;
import org.openmrs.module.fhir2.providers.BaseFhirProvenanceResourceTest;
import org.openmrs.module.fhir2.providers.MockIBundleProvider;

@RunWith(MockitoJUnitRunner.class)
public class ConditionFhirResourceProviderTest extends BaseFhirProvenanceResourceTest<Condition> {
	
	private static final String CONDITION_UUID = "23f620c3-2ecb-4d80-aea8-44fa1c5ff978";
	
	private static final String WRONG_CONDITION_UUID = "ca0dfd38-ee20-41a6-909e-7d84247ca192";
	
	private static final int START_INDEX = 0;
	
	private static final int END_INDEX = 10;
	
	@Mock
	private FhirConditionService conditionService;
	
	private Condition condition;
	
	private ConditionFhirResourceProvider resourceProvider;
	
	@Before
	public void setUp() {
		resourceProvider = new ConditionFhirResourceProvider();
		resourceProvider.setConditionService(conditionService);
	}
	
	@Before
	public void initCondition() {
		condition = new Condition();
		condition.setId(CONDITION_UUID);
		setProvenanceResources(condition);
	}
	
	private List<IBaseResource> get(IBundleProvider results) {
		return results.getResources(START_INDEX, END_INDEX);
	}
	
	@Test
	public void getResourceType_shouldReturnResourceType() {
		assertThat(resourceProvider.getResourceType(), equalTo(Condition.class));
		assertThat(resourceProvider.getResourceType().getName(), equalTo(Condition.class.getName()));
	}
	
	@Test
	public void getConditionByUuid_shouldReturnMatchingEncounter() {
		when(conditionService.get(CONDITION_UUID)).thenReturn(condition);
		IdType id = new IdType();
		id.setValue(CONDITION_UUID);
		Condition condition = resourceProvider.getConditionByUuid(id);
		assertThat(condition, notNullValue());
		assertThat(condition.getId(), notNullValue());
		assertThat(condition.getId(), equalTo(CONDITION_UUID));
	}
	
	@Test(expected = ResourceNotFoundException.class)
	public void getConditionWithWrongUuid_shouldThrowResourceNotFoundException() {
		IdType id = new IdType();
		id.setValue(WRONG_CONDITION_UUID);
		Condition result = resourceProvider.getConditionByUuid(id);
		assertThat(result, nullValue());
	}
	
	@Test
	public void getConditionHistory_shouldReturnProvenanceResources() {
		IdType id = new IdType();
		id.setValue(CONDITION_UUID);
		when(conditionService.get(CONDITION_UUID)).thenReturn(condition);
		
		List<Resource> resources = resourceProvider.getConditionHistoryById(id);
		assertThat(resources, not(empty()));
		assertThat(resources.stream().findAny().isPresent(), is(true));
		assertThat(resources.stream().findAny().get().getResourceType().name(),
		    Matchers.equalTo(Provenance.class.getSimpleName()));
	}
	
	@Test(expected = ResourceNotFoundException.class)
	public void getConditionHistoryByWithWrongId_shouldThrowResourceNotFoundException() {
		IdType idType = new IdType();
		idType.setValue(WRONG_CONDITION_UUID);
		assertThat(resourceProvider.getConditionHistoryById(idType).isEmpty(), is(true));
		assertThat(resourceProvider.getConditionHistoryById(idType).size(), Matchers.equalTo(0));
	}
	
	@Test
	public void shouldCreateNewCondition() {
		when(conditionService.saveCondition(condition)).thenReturn(condition);
		
		MethodOutcome result = resourceProvider.createCondition(condition);
		assertThat(result, notNullValue());
		assertThat(result.getCreated(), is(true));
		assertThat(result.getResource(), equalTo(condition));
	}
	
	@Test
	public void searchConditions_shouldReturnConditionReturnedByService() {
		ReferenceAndListParam patientReference = new ReferenceAndListParam();
		patientReference.addValue(new ReferenceOrListParam().add(new ReferenceParam(Patient.SP_GIVEN, "patient name")));
		
		ReferenceAndListParam subjectReference = new ReferenceAndListParam();
		subjectReference.addValue(new ReferenceOrListParam().add(new ReferenceParam(Patient.SP_GIVEN, "subject name")));
		
		TokenAndListParam codeList = new TokenAndListParam();
		codeList.addValue(new TokenOrListParam().add(new TokenParam("test code")));
		
		TokenAndListParam clinicalList = new TokenAndListParam();
		clinicalList.addValue(new TokenOrListParam().add(new TokenParam("test clinical")));
		
		DateRangeParam onsetDate = new DateRangeParam().setLowerBound("lower date").setUpperBound("upper date");
		
		QuantityAndListParam onsetAge = new QuantityAndListParam();
		onsetAge.addValue(new QuantityOrListParam().add(new QuantityParam(12)));
		
		DateRangeParam recordDate = new DateRangeParam().setLowerBound("lower record date")
		        .setUpperBound("upper record date");
		
		SortSpec sort = new SortSpec("sort param");
		
		when(conditionService.searchConditions(patientReference, codeList, clinicalList, onsetDate, onsetAge, recordDate,
		    sort)).thenReturn(new MockIBundleProvider<>(Collections.singletonList(condition), 10, 1));
		
		IBundleProvider result = resourceProvider.searchConditions(patientReference, subjectReference, codeList,
		    clinicalList, onsetDate, onsetAge, recordDate, sort);
		
		List<IBaseResource> resultList = get(result);
		
		assertThat(result, notNullValue());
		assertThat(resultList.size(), greaterThanOrEqualTo(1));
		assertThat(resultList.iterator().next().fhirType(), equalTo(FhirConstants.CONDITION));
	}

	@Test
	public void searchConditions_shouldReturnConditionReturnedByServiceWhenPatientIsNull() {
		ReferenceAndListParam subjectReference = new ReferenceAndListParam();
		subjectReference.addValue(new ReferenceOrListParam().add(new ReferenceParam(Patient.SP_GIVEN, "subject name")));

		TokenAndListParam codeList = new TokenAndListParam();
		codeList.addValue(new TokenOrListParam().add(new TokenParam("test code")));

		TokenAndListParam clinicalList = new TokenAndListParam();
		clinicalList.addValue(new TokenOrListParam().add(new TokenParam("test clinical")));

		DateRangeParam onsetDate = new DateRangeParam().setLowerBound("lower date").setUpperBound("upper date");

		QuantityAndListParam onsetAge = new QuantityAndListParam();
		onsetAge.addValue(new QuantityOrListParam().add(new QuantityParam(12)));

		DateRangeParam recordDate = new DateRangeParam().setLowerBound("lower record date")
				.setUpperBound("upper record date");

		SortSpec sort = new SortSpec("sort param");

		when(conditionService.searchConditions(subjectReference, codeList, clinicalList, onsetDate, onsetAge, recordDate,
				sort)).thenReturn(new MockIBundleProvider<>(Collections.singletonList(condition), 10, 1));

		IBundleProvider result = resourceProvider.searchConditions(null, subjectReference, codeList,
				clinicalList, onsetDate, onsetAge, recordDate, sort);

		List<IBaseResource> resultList = get(result);

		assertThat(result, notNullValue());
		assertThat(resultList.size(), greaterThanOrEqualTo(1));
		assertThat(resultList.iterator().next().fhirType(), equalTo(FhirConstants.CONDITION));
	}
}
