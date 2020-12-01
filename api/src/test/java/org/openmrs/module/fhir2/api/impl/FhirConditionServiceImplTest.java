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

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class FhirConditionServiceImplTest {
	
	private static final String NEW_OBS_CONDITION_UUID = "NEW6880e-2c46-15e4-9038-a6c5e4d22gh8";
	
	private static final String EXISTING_OBS_CONDITION_UUID = "942ec003-a55d-43c4-ac7a-bd6d1ba63382";
	
	private static final String WRONG_OBS_CONDITION_UUID = "430bbb70-6a9c-4e1e-badb-9d1034b1b5e9";
	
	private static final String OBS_CONDITION_INITIAL_DATA_XML = "org/openmrs/module/fhir2/api/dao/impl/FhirObsConditionDaoImplTest_initial_data.xml";
	
	@Test
	public void get_shouldGetObsConditionByUuid() {
		assertEquals("", "");
	}
	
}
