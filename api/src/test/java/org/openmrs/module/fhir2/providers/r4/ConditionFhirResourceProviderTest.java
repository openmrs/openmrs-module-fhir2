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
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import ca.uhn.fhir.model.api.Include;
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
import org.hl7.fhir.r4.model.OperationOutcome;
import org.hl7.fhir.r4.model.Patient;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.api.FhirConditionService;
import org.openmrs.module.fhir2.api.search.param.ConditionSearchParams;
import org.openmrs.module.fhir2.providers.BaseFhirProvenanceResourceTest;

@RunWith(MockitoJUnitRunner.class)
public class ConditionFhirResourceProviderTest extends BaseFhirProvenanceResourceTest<Condition> {
	
	private static final String CONDITION_UUID = "23f620c3-2ecb-4d80-aea8-44fa1c5ff978";
	
	private static final String WRONG_CONDITION_UUID = "ca0dfd38-ee20-41a6-909e-7d84247ca192";
	
	private static final String LAST_UPDATED_DATE = "2020-09-03";
	
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
	public void createCondition_shouldCreateNewCondition() {
		when(conditionService.create(condition)).thenReturn(condition);
		
		MethodOutcome result = resourceProvider.createCondition(condition);
		
		assertThat(result, notNullValue());
		assertThat(result.getCreated(), is(true));
		assertThat(result.getResource(), equalTo(condition));
	}
	
	@Test
	public void updateCondition_shouldUpdateRequestedCondition() {
		when(conditionService.update(CONDITION_UUID, condition)).thenReturn(condition);
		
		MethodOutcome result = resourceProvider.updateCondition(new IdType().setValue(CONDITION_UUID), condition);
		
		assertThat(result, notNullValue());
		assertThat(result.getResource(), equalTo(condition));
	}
	
	@Test
	public void deleteCondition_shouldDeleteCondition() {
		OperationOutcome result = resourceProvider.deleteCondition(new IdType().setValue(CONDITION_UUID));
		
		assertThat(result, Matchers.notNullValue());
		assertThat(result.getIssueFirstRep().getSeverity(), Matchers.equalTo(OperationOutcome.IssueSeverity.INFORMATION));
		assertThat(result.getIssueFirstRep().getDetails().getCodingFirstRep().getCode(), Matchers.equalTo("MSG_DELETED"));
		assertThat(result.getIssueFirstRep().getDetails().getCodingFirstRep().getDisplay(),
		    Matchers.equalTo("This resource has been deleted"));
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
		
		DateRangeParam onsetDate = new DateRangeParam().setLowerBound("gt2020-05-01").setUpperBound("lt2021-05-01");
		
		QuantityAndListParam onsetAge = new QuantityAndListParam();
		onsetAge.addValue(new QuantityOrListParam().add(new QuantityParam(12)));
		
		DateRangeParam recordDate = new DateRangeParam().setLowerBound("gt2020-05-01").setUpperBound("lt2021-05-01");
		
		TokenAndListParam uuid = new TokenAndListParam().addAnd(new TokenParam(CONDITION_UUID));
		
		TokenAndListParam category = new TokenAndListParam().addAnd(new TokenOrListParam()
		        .add(FhirConstants.CONDITION_CATEGORY_SYSTEM_URI, FhirConstants.CONDITION_CATEGORY_CODE_CONDITION));
		
		DateRangeParam lastUpdated = new DateRangeParam().setLowerBound(LAST_UPDATED_DATE).setUpperBound(LAST_UPDATED_DATE);
		
		SortSpec sort = new SortSpec("sort param");
		
		HashSet<Include> includes = new HashSet<>();
		
		when(conditionService.searchConditions(new ConditionSearchParams(patientReference, codeList, clinicalList, onsetDate,
		        onsetAge, recordDate, category, uuid, lastUpdated, sort, null)))
		                .thenReturn(new MockIBundleProvider<>(Collections.singletonList(condition), 10, 1));
		
		IBundleProvider result = resourceProvider.searchConditions(patientReference, subjectReference, codeList,
		    clinicalList, onsetDate, onsetAge, recordDate, category, uuid, lastUpdated, sort, includes);
		
		List<IBaseResource> resultList = get(result);
		
		assertThat(result, notNullValue());
		assertThat(resultList, hasSize(greaterThanOrEqualTo(1)));
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
		
		DateRangeParam onsetDate = new DateRangeParam().setLowerBound("gt2020-05-01").setUpperBound("lt2021-05-01");
		
		QuantityAndListParam onsetAge = new QuantityAndListParam();
		onsetAge.addValue(new QuantityOrListParam().add(new QuantityParam(12)));
		
		DateRangeParam recordDate = new DateRangeParam().setLowerBound("gt2020-05-01").setUpperBound("lt2021-05-01");
		
		TokenAndListParam category = new TokenAndListParam().addAnd(new TokenOrListParam()
		        .add(FhirConstants.CONDITION_CATEGORY_SYSTEM_URI, FhirConstants.CONDITION_CATEGORY_CODE_CONDITION));
		
		TokenAndListParam uuid = new TokenAndListParam().addAnd(new TokenParam(CONDITION_UUID));
		
		DateRangeParam lastUpdated = new DateRangeParam().setLowerBound(LAST_UPDATED_DATE).setUpperBound(LAST_UPDATED_DATE);
		
		SortSpec sort = new SortSpec("sort param");
		
		HashSet<Include> includes = new HashSet<>();
		
		when(conditionService.searchConditions(new ConditionSearchParams(subjectReference, codeList, clinicalList, onsetDate,
		        onsetAge, recordDate, category, uuid, lastUpdated, sort, null)))
		                .thenReturn(new MockIBundleProvider<>(Collections.singletonList(condition), 10, 1));
		
		IBundleProvider result = resourceProvider.searchConditions(subjectReference, subjectReference, codeList,
		    clinicalList, onsetDate, onsetAge, recordDate, category, uuid, lastUpdated, sort, includes);
		
		List<IBaseResource> resultList = get(result);
		
		assertThat(result, notNullValue());
		assertThat(resultList, hasSize(greaterThanOrEqualTo(1)));
		assertThat(resultList.iterator().next().fhirType(), equalTo(FhirConstants.CONDITION));
	}
	
	@Test
	public void searchDiagnoses_shouldReturnDiagnosisReturnedByService() {
		ReferenceAndListParam patientReference = new ReferenceAndListParam();
		patientReference.addValue(new ReferenceOrListParam().add(new ReferenceParam(Patient.SP_GIVEN, "patient name")));
		
		ReferenceAndListParam subjectReference = new ReferenceAndListParam();
		subjectReference.addValue(new ReferenceOrListParam().add(new ReferenceParam(Patient.SP_GIVEN, "subject name")));
		
		TokenAndListParam codeList = new TokenAndListParam();
		codeList.addValue(new TokenOrListParam().add(new TokenParam("test code")));
		
		TokenAndListParam clinicalList = new TokenAndListParam();
		clinicalList.addValue(new TokenOrListParam().add(new TokenParam("test clinical")));
		
		DateRangeParam onsetDate = new DateRangeParam().setLowerBound("gt2020-05-01").setUpperBound("lt2021-05-01");
		
		QuantityAndListParam onsetAge = new QuantityAndListParam();
		onsetAge.addValue(new QuantityOrListParam().add(new QuantityParam(12)));
		
		DateRangeParam recordDate = new DateRangeParam().setLowerBound("gt2020-05-01").setUpperBound("lt2021-05-01");
		
		TokenAndListParam category = new TokenAndListParam().addAnd(new TokenOrListParam()
		        .add(FhirConstants.CONDITION_CATEGORY_SYSTEM_URI, FhirConstants.CONDITION_CATEGORY_CODE_CONDITION));
		
		TokenAndListParam uuid = new TokenAndListParam().addAnd(new TokenParam(CONDITION_UUID));
		
		DateRangeParam lastUpdated = new DateRangeParam().setLowerBound(LAST_UPDATED_DATE).setUpperBound(LAST_UPDATED_DATE);
		
		SortSpec sort = new SortSpec("sort param");
		
		HashSet<Include> includes = new HashSet<>();
		
		when(conditionService.searchConditions(new ConditionSearchParams(patientReference, codeList, clinicalList, onsetDate,
		        onsetAge, recordDate, category, uuid, lastUpdated, sort, null)))
		                .thenReturn(new MockIBundleProvider<>(Collections.singletonList(condition), 10, 1));
		
		IBundleProvider result = resourceProvider.searchConditions(patientReference, subjectReference, codeList,
		    clinicalList, onsetDate, onsetAge, recordDate, category, uuid, lastUpdated, sort, includes);
		
		List<IBaseResource> resultList = get(result);
		
		assertThat(result, notNullValue());
		assertThat(resultList, hasSize(greaterThanOrEqualTo(1)));
		assertThat(resultList.iterator().next().fhirType(), equalTo(FhirConstants.CONDITION));
	}
	
	@Test
	public void searchDiagnoses_shouldReturnDiagnosisReturnedByServiceWhenPatientIsNull() {
		ReferenceAndListParam subjectReference = new ReferenceAndListParam();
		subjectReference.addValue(new ReferenceOrListParam().add(new ReferenceParam(Patient.SP_GIVEN, "subject name")));
		
		TokenAndListParam codeList = new TokenAndListParam();
		codeList.addValue(new TokenOrListParam().add(new TokenParam("test code")));
		
		TokenAndListParam clinicalList = new TokenAndListParam();
		clinicalList.addValue(new TokenOrListParam().add(new TokenParam("test clinical")));
		
		DateRangeParam onsetDate = new DateRangeParam().setLowerBound("gt2020-05-01").setUpperBound("lt2021-05-01");
		
		QuantityAndListParam onsetAge = new QuantityAndListParam();
		onsetAge.addValue(new QuantityOrListParam().add(new QuantityParam(12)));
		
		DateRangeParam recordDate = new DateRangeParam().setLowerBound("gt2020-05-01").setUpperBound("lt2021-05-01");
		
		TokenAndListParam category = new TokenAndListParam().addAnd(new TokenOrListParam()
		        .add(FhirConstants.CONDITION_CATEGORY_SYSTEM_URI, FhirConstants.CONDITION_CATEGORY_CODE_DIAGNOSIS));
		
		TokenAndListParam uuid = new TokenAndListParam().addAnd(new TokenParam(CONDITION_UUID));
		
		DateRangeParam lastUpdated = new DateRangeParam().setLowerBound(LAST_UPDATED_DATE).setUpperBound(LAST_UPDATED_DATE);
		
		SortSpec sort = new SortSpec("sort param");
		
		HashSet<Include> includes = new HashSet<>();
		
		when(conditionService.searchConditions(new ConditionSearchParams(subjectReference, codeList, clinicalList, onsetDate,
		        onsetAge, recordDate, category, uuid, lastUpdated, sort, null)))
		                .thenReturn(new MockIBundleProvider<>(Collections.singletonList(condition), 10, 1));
		
		IBundleProvider result = resourceProvider.searchConditions(subjectReference, subjectReference, codeList,
		    clinicalList, onsetDate, onsetAge, recordDate, category, uuid, lastUpdated, sort, includes);
		
		List<IBaseResource> resultList = get(result);
		
		assertThat(result, notNullValue());
		assertThat(resultList, hasSize(greaterThanOrEqualTo(1)));
		assertThat(resultList.iterator().next().fhirType(), equalTo(FhirConstants.CONDITION));
	}
}
