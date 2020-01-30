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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

import javax.inject.Inject;
import javax.inject.Provider;

import java.util.Collection;

import org.hibernate.SessionFactory;
import org.hl7.fhir.r4.model.ServiceRequest;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.module.fhir2.Task;
import org.openmrs.module.fhir2.TestFhirSpringConfiguration;
import org.openmrs.test.BaseModuleContextSensitiveTest;
import org.springframework.test.context.ContextConfiguration;

@ContextConfiguration(classes = TestFhirSpringConfiguration.class, inheritLocations = false)
public class FhirTaskDaoImplTest extends BaseModuleContextSensitiveTest {
	
	private static final String TASK_DATA_XML = "org/openmrs/module/fhir2/api/dao/impl/FhirTaskDaoImplTest_initial_data.xml";
	
	private static final String TASK_UUID = "d899333c-5bd4-45cc-b1e7-2f9542dbcbf6";
	
	private static final String BASED_ON_TASK_UUID = "3dc9f4a7-44dc-4b29-adfd-a8b297a41f33";
	
	private static final String BASED_ON_ORDER_UUID = "7d96f25c-4949-4f72-9931-d808fbc226de";
	
	private static final String OTHER_ORDER_UUID = "cbcb84f3-4576-452f-ba74-7cdeaa9aa602";
	
	private static final Task.TaskStatus TASK_STATUS = Task.TaskStatus.REQUESTED;
	
	private static final Task.TaskStatus NEW_STATUS = Task.TaskStatus.ACCEPTED;
	
	private static final Task.TaskIntent TASK_INTENT = Task.TaskIntent.ORDER;
	
	private FhirTaskDaoImpl dao;
	
	@Inject
	private Provider<SessionFactory> sessionFactoryProvider;
	
	@Before
	public void setup() throws Exception {
		dao = new FhirTaskDaoImpl();
		dao.setSessionFactory(sessionFactoryProvider.get());
		executeDataSet(TASK_DATA_XML);
	}
	
	@Test
	public void shouldRetrieveTaskByUuid() {
		Task result = dao.getTaskByUuid(TASK_UUID);
		
		assertThat(result, notNullValue());
		assertThat(result.getUuid(), equalTo(TASK_UUID));
	}
	
	@Test
	public void shouldUpdateTaskStatus() {
		Task toUpdate = dao.getTaskByUuid(TASK_UUID);
		toUpdate.setStatus(NEW_STATUS);
		
		dao.saveTask(toUpdate);
		
		assertThat(dao.getTaskByUuid(TASK_UUID).getStatus(), equalTo(NEW_STATUS));
	}
	
	@Test
	public void shouldRetrieveTasksByBasedOn() {
		Collection<Task> results = dao.getTasksByBasedOnUuid(ServiceRequest.class, BASED_ON_ORDER_UUID);
		assertThat(results, notNullValue());
		assertThat(results.size(), equalTo(1));
		assertThat(results.iterator().next().getUuid(), equalTo(BASED_ON_TASK_UUID));
	}
	
	@Test
	public void shouldReturnEmptyTaskListForOrderWithNoTask() {
		Collection<Task> results = dao.getTasksByBasedOnUuid(ServiceRequest.class, OTHER_ORDER_UUID);
		assertThat(results, notNullValue());
		assertThat(results, empty());
	}
}
