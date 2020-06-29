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

import org.hl7.fhir.r4.model.ServiceRequest;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.TestOrder;
import org.openmrs.module.fhir2.TestFhirSpringConfiguration;
import org.openmrs.module.fhir2.api.dao.FhirServiceRequestDao;
import org.openmrs.module.fhir2.api.translators.ServiceRequestTranslator;
import org.openmrs.test.BaseModuleContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

@ContextConfiguration(classes = TestFhirSpringConfiguration.class, inheritLocations = false)
public class ServiceRequestSearchQueryImplTest extends BaseModuleContextSensitiveTest {
	
	private static final String TEST_ORDER_INITIAL_DATA = "org/openmrs/module/fhir2/api/dao/impl/FhirServiceRequestTest_initial_data.xml";
	
	@Autowired
	private FhirServiceRequestDao<TestOrder> dao;
	
	@Autowired
	private ServiceRequestTranslator<TestOrder> translator;
	
	@Autowired
	private SearchQuery<TestOrder, ServiceRequest, FhirServiceRequestDao<TestOrder>, ServiceRequestTranslator<TestOrder>> searchQuery;
	
	@Before
	public void setup() throws Exception {
		executeDataSet(TEST_ORDER_INITIAL_DATA);
	}
	
	@Test
	public void searchForServiceRequests_shouldHandleComplexQuery() {
		
	}
}
