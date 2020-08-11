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

import static org.exparity.hamcrest.date.DateMatchers.sameOrAfter;
import static org.exparity.hamcrest.date.DateMatchers.sameOrBefore;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;

import java.util.List;
import java.util.stream.Collectors;

import ca.uhn.fhir.rest.api.SortOrderEnum;
import ca.uhn.fhir.rest.api.SortSpec;
import ca.uhn.fhir.rest.api.server.IBundleProvider;
import ca.uhn.fhir.rest.param.DateRangeParam;
import ca.uhn.fhir.rest.param.ReferenceAndListParam;
import ca.uhn.fhir.rest.param.ReferenceOrListParam;
import ca.uhn.fhir.rest.param.ReferenceParam;
import ca.uhn.fhir.rest.param.TokenAndListParam;
import ca.uhn.fhir.rest.param.TokenOrListParam;
import ca.uhn.fhir.rest.param.TokenParam;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.Task;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.TestFhirSpringConfiguration;
import org.openmrs.module.fhir2.api.dao.FhirTaskDao;
import org.openmrs.module.fhir2.api.search.param.SearchParameterMap;
import org.openmrs.module.fhir2.api.translators.TaskTranslator;
import org.openmrs.module.fhir2.model.FhirTask;
import org.openmrs.test.BaseModuleContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

@ContextConfiguration(classes = TestFhirSpringConfiguration.class, inheritLocations = false)
public class TaskSearchQueryTest extends BaseModuleContextSensitiveTest {
	
	private static final String TASK_DATA_XML = "org/openmrs/module/fhir2/api/dao/impl/FhirTaskDaoImplTest_initial_data.xml";
	
	private static final String TASK_DATA_OWNER_XML = "org/openmrs/module/fhir2/api/dao/impl/FhirTaskDaoImplTest_owner_data.xml";
	
	private static final String TASK_UUID = "d899333c-5bd4-45cc-b1e7-2f9542dbcbf6";
	
	private static final String BASED_ON_TASK_UUID = "3dc9f4a7-44dc-4b29-adfd-a8b297a41f33";
	
	private static final String BASED_ON_ORDER_UUID = "7d96f25c-4949-4f72-9931-d808fbc226de";
	
	private static final String OTHER_ORDER_UUID = "cbcb84f3-4576-452f-ba74-7cdeaa9aa602";
	
	private static final String UNKNOWN_OWNER_UUID = "c1a3af38-c0a9-4c2e-9cc0-8ensade357e5";
	
	private static final String OWNER_TASK_UUID = "c1a3af38-c0a9-4c2e-9cc0-8e0440e357e5";
	
	private static final String OWNER_USER_UUID = "7f8aec9d-8269-4bb4-8bc5-1820bb31092c";
	
	private static final String DATE_CREATED = "2012-03-01";
	
	private static final String DATE_CHANGED = "2012-09-01";
	
	private static final String DATE_RETIRED = "2015-09-03";
	
	private static final int START_INDEX = 0;
	
	private static final int END_INDEX = 10;
	
	@Autowired
	private FhirTaskDao dao;
	
	@Autowired
	private TaskTranslator translator;
	
	@Autowired
	private SearchQueryInclude<Task> searchQueryInclude;
	
	@Autowired
	private SearchQuery<FhirTask, Task, FhirTaskDao, TaskTranslator, SearchQueryInclude<Task>> searchQuery;
	
	@Before
	public void setup() throws Exception {
		executeDataSet(TASK_DATA_XML);
	}
	
	private List<IBaseResource> get(IBundleProvider results) {
		return results.getResources(START_INDEX, END_INDEX);
	}
	
	private IBundleProvider search(SearchParameterMap theParams) {
		return searchQuery.getQueryResults(theParams, dao, translator, searchQueryInclude);
	}
	
	@Test
	public void getTasksByBasedOnUuid_shouldRetrieveTasksByBasedOn() {
		ReferenceAndListParam ref = new ReferenceAndListParam().addAnd(
		    new ReferenceOrListParam().add(new ReferenceParam(FhirConstants.SERVICE_REQUEST, null, BASED_ON_ORDER_UUID)));
		
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.BASED_ON_REFERENCE_SEARCH_HANDLER,
		    ref);
		
		IBundleProvider results = search(theParams);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList, not(empty()));
		assertThat(resultList, hasSize(equalTo(1)));
		assertThat(((Task) resultList.iterator().next()).getIdElement().getIdPart(), equalTo(BASED_ON_TASK_UUID));
	}
	
	@Test
	public void getTasksByBasedOnUuid_shouldReturnEmptyTaskListForOrderWithNoTask() {
		ReferenceAndListParam ref = new ReferenceAndListParam().addAnd(
		    new ReferenceOrListParam().add(new ReferenceParam(FhirConstants.SERVICE_REQUEST, null, OTHER_ORDER_UUID)));
		
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.BASED_ON_REFERENCE_SEARCH_HANDLER,
		    ref);
		
		IBundleProvider results = search(theParams);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList, empty());
	}
	
	@Test
	public void searchForTasks_shouldReturnTasksByBasedOn() {
		ReferenceParam basedOnReference = new ReferenceParam();
		basedOnReference.setValue(FhirConstants.SERVICE_REQUEST + "/" + BASED_ON_ORDER_UUID);
		
		ReferenceAndListParam ref = new ReferenceAndListParam().addAnd(new ReferenceOrListParam().add(basedOnReference));
		
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.BASED_ON_REFERENCE_SEARCH_HANDLER,
		    ref);
		
		IBundleProvider results = search(theParams);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList, not(empty()));
		assertThat(resultList, hasSize(equalTo(1)));
		assertThat(((Task) resultList.iterator().next()).getIdElement().getIdPart(), equalTo(BASED_ON_TASK_UUID));
	}
	
	@Test
	public void getTasksByBasedOnUuid_shouldReturnTasksForMultipleBasedOnReferenceOr() {
		ReferenceParam basedOnReference = new ReferenceParam()
		        .setValue(FhirConstants.SERVICE_REQUEST + "/" + BASED_ON_ORDER_UUID);
		ReferenceParam badReference = new ReferenceParam().setValue(FhirConstants.SERVICE_REQUEST + "/" + OTHER_ORDER_UUID);
		ReferenceAndListParam ref = new ReferenceAndListParam()
		        .addAnd(new ReferenceOrListParam().add(basedOnReference).add(badReference));
		
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.BASED_ON_REFERENCE_SEARCH_HANDLER,
		    ref);
		
		IBundleProvider results = search(theParams);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList, not(empty()));
		assertThat(resultList, hasSize(equalTo(1)));
		assertThat(((Task) resultList.iterator().next()).getIdElement().getIdPart(), equalTo(BASED_ON_TASK_UUID));
	}
	
	@Test
	public void getTasksByBasedOnUuid_shouldReturnEmptyTaskListForMultipleBasedOnReferenceAnd() {
		ReferenceParam basedOnReference = new ReferenceParam()
		        .setValue(FhirConstants.SERVICE_REQUEST + "/" + BASED_ON_ORDER_UUID);
		ReferenceParam badReference = new ReferenceParam().setValue(FhirConstants.SERVICE_REQUEST + "/" + OTHER_ORDER_UUID);
		ReferenceAndListParam ref = new ReferenceAndListParam().addAnd(new ReferenceOrListParam().add(basedOnReference))
		        .addAnd(new ReferenceOrListParam().add(badReference));
		
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.BASED_ON_REFERENCE_SEARCH_HANDLER,
		    ref);
		
		IBundleProvider results = search(theParams);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList, empty());
	}
	
	@Test
	public void searchForTasks_shouldReturnTasksByOwner() throws Exception {
		ReferenceParam ownerReference = new ReferenceParam();
		ownerReference.setValue(FhirConstants.PRACTITIONER + "/" + OWNER_USER_UUID);
		
		ReferenceAndListParam ref = new ReferenceAndListParam().addAnd(new ReferenceOrListParam().add(ownerReference));
		
		executeDataSet(TASK_DATA_OWNER_XML);
		
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.OWNER_REFERENCE_SEARCH_HANDLER,
		    ref);
		
		IBundleProvider results = search(theParams);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList, not(empty()));
		assertThat(resultList.size(), equalTo(3));
		assertThat(resultList, hasItem(hasProperty("id", equalTo(OWNER_TASK_UUID))));
	}
	
	@Test
	public void searchForTasks_shouldReturnTasksByMultipleOwnersOr() throws Exception {
		ReferenceParam ownerReference = new ReferenceParam().setValue(FhirConstants.PRACTITIONER + "/" + OWNER_USER_UUID);
		ReferenceParam badReference = new ReferenceParam().setValue(FhirConstants.PRACTITIONER + "/" + UNKNOWN_OWNER_UUID);
		
		ReferenceAndListParam ref = new ReferenceAndListParam()
		        .addAnd(new ReferenceOrListParam().add(ownerReference).add(badReference));
		
		executeDataSet(TASK_DATA_OWNER_XML);
		
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.OWNER_REFERENCE_SEARCH_HANDLER,
		    ref);
		
		IBundleProvider results = search(theParams);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList, not(empty()));
		assertThat(resultList.size(), equalTo(3));
		assertThat(resultList, hasItem(hasProperty("id", equalTo(OWNER_TASK_UUID))));
	}
	
	@Test
	public void searchForTasks_shouldReturnEmptyTaskListByMultipleOwnersAnd() throws Exception {
		ReferenceParam ownerReference = new ReferenceParam().setValue(FhirConstants.PRACTITIONER + "/" + OWNER_USER_UUID);
		ReferenceParam badReference = new ReferenceParam().setValue(FhirConstants.PRACTITIONER + "/" + UNKNOWN_OWNER_UUID);
		
		ReferenceAndListParam ref = new ReferenceAndListParam().addAnd(new ReferenceOrListParam().add(ownerReference))
		        .addAnd(new ReferenceOrListParam().add(badReference));
		
		executeDataSet(TASK_DATA_OWNER_XML);
		
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.OWNER_REFERENCE_SEARCH_HANDLER,
		    ref);
		
		IBundleProvider results = search(theParams);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList, empty());
	}
	
	@Test
	public void searchForTasks_shouldReturnTasksByStatus() {
		TokenAndListParam status = new TokenAndListParam().addAnd(
		    new TokenOrListParam().add(FhirConstants.TASK_STATUS_VALUE_SET_URI, Task.TaskStatus.ACCEPTED.toString()));
		
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.STATUS_SEARCH_HANDLER, status);
		
		IBundleProvider results = search(theParams);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList, not(empty()));
		assertThat(resultList.size(), equalTo(2));
		assertThat(resultList, hasItem(hasProperty("id", equalTo(TASK_UUID))));
		assertThat(resultList, everyItem(hasProperty("status", equalTo(Task.TaskStatus.ACCEPTED))));
	}
	
	@Test
	public void searchForTasks_shouldReturnTasksByMultipleStatusOr() {
		TokenAndListParam status = new TokenAndListParam().addAnd(
		    new TokenOrListParam().add(FhirConstants.TASK_STATUS_VALUE_SET_URI, Task.TaskStatus.ACCEPTED.toString())
		            .add(FhirConstants.TASK_STATUS_VALUE_SET_URI, Task.TaskStatus.REQUESTED.toString()));
		
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.STATUS_SEARCH_HANDLER, status);
		
		IBundleProvider results = search(theParams);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList, not(empty()));
		assertThat(resultList.size(), equalTo(4));
		assertThat(resultList, hasItem(hasProperty("id", equalTo(TASK_UUID))));
		assertThat(resultList, hasItem(hasProperty("status", equalTo(Task.TaskStatus.ACCEPTED))));
		assertThat(resultList, hasItem(hasProperty("status", equalTo(Task.TaskStatus.REQUESTED))));
		assertThat(resultList, not(hasItem(hasProperty("status", equalTo(Task.TaskStatus.REJECTED)))));
	}
	
	@Test
	public void searchForTasks_shouldReturnEmptyTaskListByMultipleStatusAnd() {
		TokenAndListParam status = new TokenAndListParam()
		        .addAnd(
		            new TokenOrListParam().add(FhirConstants.TASK_STATUS_VALUE_SET_URI, Task.TaskStatus.ACCEPTED.toString()))
		        .addAnd(new TokenOrListParam(FhirConstants.TASK_STATUS_VALUE_SET_URI, Task.TaskStatus.REQUESTED.toString()));
		
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.STATUS_SEARCH_HANDLER, status);
		
		IBundleProvider results = search(theParams);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList, empty());
	}
	
	@Test
	public void searchForTasks_shouldSearchForTasksByUuid() {
		TokenAndListParam uuid = new TokenAndListParam().addAnd(new TokenParam(TASK_UUID));
		
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.COMMON_SEARCH_HANDLER,
		    FhirConstants.ID_PROPERTY, uuid);
		
		IBundleProvider results = search(theParams);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList, not(empty()));
		assertThat(resultList, hasSize(equalTo(1)));
		assertThat(((Task) resultList.iterator().next()).getIdElement().getIdPart(), equalTo(TASK_UUID));
	}
	
	@Test
	public void searchForTasks_shouldSearchForTasksByLastUpdatedDateCreated() {
		DateRangeParam lastUpdated = new DateRangeParam().setUpperBound(DATE_CREATED).setLowerBound(DATE_CREATED);
		
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.COMMON_SEARCH_HANDLER,
		    FhirConstants.LAST_UPDATED_PROPERTY, lastUpdated);
		
		IBundleProvider results = search(theParams);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList, not(empty()));
		assertThat(resultList, hasSize(equalTo(1)));
	}
	
	@Test
	public void searchForTasks_shouldSearchForTasksByLastUpdatedDateChanged() {
		DateRangeParam lastUpdated = new DateRangeParam().setUpperBound(DATE_CHANGED).setLowerBound(DATE_CHANGED);
		
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.COMMON_SEARCH_HANDLER,
		    FhirConstants.LAST_UPDATED_PROPERTY, lastUpdated);
		
		IBundleProvider results = search(theParams);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList, not(empty()));
		assertThat(resultList, hasSize(equalTo(1)));
	}
	
	@Test
	public void searchForTasks_shouldSearchForTasksByMatchingUuidAndLastUpdated() {
		TokenAndListParam uuid = new TokenAndListParam().addAnd(new TokenParam(TASK_UUID));
		DateRangeParam lastUpdated = new DateRangeParam().setUpperBound(DATE_CHANGED).setLowerBound(DATE_CHANGED);
		
		SearchParameterMap theParams = new SearchParameterMap()
		        .addParameter(FhirConstants.COMMON_SEARCH_HANDLER, FhirConstants.ID_PROPERTY, uuid)
		        .addParameter(FhirConstants.COMMON_SEARCH_HANDLER, FhirConstants.LAST_UPDATED_PROPERTY, lastUpdated);
		
		IBundleProvider results = search(theParams);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList, not(empty()));
		assertThat(resultList, hasSize(equalTo(1)));
		assertThat(((Task) resultList.iterator().next()).getIdElement().getIdPart(), equalTo(TASK_UUID));
	}
	
	@Test
	public void searchForTasks_shouldReturnEmptyListByMismatchingUuidAndLastUpdated() {
		TokenAndListParam uuid = new TokenAndListParam().addAnd(new TokenParam(TASK_UUID));
		DateRangeParam lastUpdated = new DateRangeParam().setUpperBound(DATE_RETIRED).setLowerBound(DATE_RETIRED);
		
		SearchParameterMap theParams = new SearchParameterMap()
		        .addParameter(FhirConstants.COMMON_SEARCH_HANDLER, FhirConstants.ID_PROPERTY, uuid)
		        .addParameter(FhirConstants.COMMON_SEARCH_HANDLER, FhirConstants.LAST_UPDATED_PROPERTY, lastUpdated);
		
		IBundleProvider results = search(theParams);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList, empty());
	}
	
	@Test
	public void searchForTasks_shouldSortTasksAsRequested() {
		SortSpec sort = new SortSpec();
		sort.setParamName(FhirConstants.SP_LAST_UPDATED);
		sort.setOrder(SortOrderEnum.ASC);
		
		SearchParameterMap theParams = new SearchParameterMap().setSortSpec(sort);
		
		IBundleProvider results = search(theParams);
		
		List<Task> resultList = get(results).stream().map(p -> (Task) p).collect(Collectors.toList());
		
		assertThat(results, notNullValue());
		assertThat(resultList, not(empty()));
		assertThat(resultList, hasSize(greaterThanOrEqualTo(1)));
		
		// remove tasks with null date changed to allow comparison
		resultList.removeIf(p -> p.getLastModified() == null);
		
		// pair-wise comparison of all tasks by date
		for (int i = 1; i < resultList.size(); i++) {
			assertThat(resultList.get(i - 1).getLastModified(), sameOrBefore(resultList.get(i).getLastModified()));
		}
		
		sort.setOrder(SortOrderEnum.DESC);
		
		theParams = new SearchParameterMap().setSortSpec(sort);
		
		results = search(theParams);
		
		resultList = get(results).stream().map(p -> (Task) p).collect(Collectors.toList());
		
		assertThat(results, notNullValue());
		assertThat(resultList, not(empty()));
		assertThat(resultList, hasSize(greaterThanOrEqualTo(1)));
		
		// remove tasks with null date changed to allow comparison
		resultList.removeIf(p -> p.getLastModified() == null);
		
		// pair-wise comparison of all tasks by date
		for (int i = 1; i < resultList.size(); i++) {
			assertThat(resultList.get(i - 1).getLastModified(), sameOrAfter(resultList.get(i).getLastModified()));
		}
	}
	
	@Test
	public void searchForTasks_shouldHandleAComplexQuery() throws Exception {
		executeDataSet(TASK_DATA_OWNER_XML);
		
		TokenAndListParam status = new TokenAndListParam();
		
		status.addAnd(
		    new TokenOrListParam().add(FhirConstants.TASK_STATUS_VALUE_SET_URI, Task.TaskStatus.ACCEPTED.toString())
		            .add(FhirConstants.TASK_STATUS_VALUE_SET_URI, Task.TaskStatus.REQUESTED.toString()));
		
		ReferenceParam ownerReference = new ReferenceParam();
		ownerReference.setValue(FhirConstants.PRACTITIONER + "/" + OWNER_USER_UUID);
		
		ReferenceAndListParam ref = new ReferenceAndListParam().addAnd(new ReferenceOrListParam().add(ownerReference));
		
		SortSpec sort = new SortSpec();
		sort.setParamName("date");
		sort.setOrder(SortOrderEnum.DESC);
		
		SearchParameterMap theParams = new SearchParameterMap()
		        .addParameter(FhirConstants.OWNER_REFERENCE_SEARCH_HANDLER, ref)
		        .addParameter(FhirConstants.STATUS_SEARCH_HANDLER, status).setSortSpec(sort);
		
		IBundleProvider results = search(theParams);
		
		List<Task> resultList = get(results).stream().map(p -> (Task) p).collect(Collectors.toList());
		
		assertThat(results, notNullValue());
		assertThat(resultList, not(empty()));
		assertThat(resultList.size(), equalTo(2));
		
		assertThat(resultList, hasItem(hasProperty("status", equalTo(Task.TaskStatus.ACCEPTED))));
		assertThat(resultList, hasItem(hasProperty("status", equalTo(Task.TaskStatus.REQUESTED))));
		assertThat(resultList, not(hasItem(hasProperty("status", equalTo(Task.TaskStatus.REJECTED)))));
		assertThat(resultList, hasItem(hasProperty("id", equalTo(OWNER_TASK_UUID))));
		
		// pair-wise comparison of all tasks by date
		for (int i = 1; i < resultList.size(); i++) {
			assertThat(resultList.get(i - 1).getLastModified(), sameOrAfter(resultList.get(i).getLastModified()));
		}
		
	}
	
}
