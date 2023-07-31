/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.fhir2.api.impl;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collection;

import com.google.common.collect.Lists;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openmrs.module.fhir2.api.dao.FhirContactPointDao;
import org.openmrs.module.fhir2.model.FhirContactPoint;

@RunWith(MockitoJUnitRunner.class)
public class FhirContactPointServiceImplTest {
	
	@Mock
	private FhirContactPointDao fhirContactPointDao;
	
	private FhirContactPointServiceImpl fhirContactPointService;
	
	@Before
	public void setup() {
		fhirContactPointService = new FhirContactPointServiceImpl();
		fhirContactPointService.setFhirContactPointDao(fhirContactPointDao);
	}
	
	@Test
	public void getContactPoints_shouldReturnContactPoints() {
		Collection<FhirContactPoint> fhirContactPoints = Lists.newArrayList(new FhirContactPoint(), new FhirContactPoint());
		
		when(fhirContactPointDao.getFhirFhirContactPoints()).thenReturn(fhirContactPoints);
		
		Collection<FhirContactPoint> result = fhirContactPointService.getFhirFhirContactPoints();
		
		assertThat(result, notNullValue());
		assertThat(result, not(empty()));
	}
	
	@Test
	public void getContactPoints_shouldReturnEmptyCollectionWhenNoContactPointsFound() {
		when(fhirContactPointDao.getFhirFhirContactPoints()).thenReturn(new ArrayList<>());
		
		Collection<FhirContactPoint> result = fhirContactPointService.getFhirFhirContactPoints();
		
		assertThat(result, notNullValue());
		assertThat(result, empty());
	}
}
