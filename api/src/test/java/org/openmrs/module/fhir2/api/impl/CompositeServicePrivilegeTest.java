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
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThrows;

import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.api.context.Context;
import org.openmrs.module.fhir2.BaseFhirContextSensitiveTest;
import org.openmrs.module.fhir2.api.FhirLocationService;
import org.openmrs.util.PrivilegeConstants;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Checks the privileges a composite service requires against the real DAO guards: each operation
 * needs only the privilege of the operation itself, and a missing privilege is reported as not
 * found.
 */
public class CompositeServicePrivilegeTest extends BaseFhirContextSensitiveTest {
	
	private static final String LOCATION_UUID = "8d6c993e-c2cc-11de-8d13-0010c6dffd0f";
	
	@Autowired
	private FhirLocationService locationService;
	
	@Before
	public void setup() {
		Context.logout();
	}
	
	@After
	public void cleanup() {
		Context.removeProxyPrivilege(PrivilegeConstants.MANAGE_LOCATIONS);
		Context.removeProxyPrivilege(PrivilegeConstants.GET_LOCATIONS);
	}
	
	@Test
	public void delete_shouldNotRequireTheReadPrivilege() {
		// given
		Context.addProxyPrivilege(PrivilegeConstants.MANAGE_LOCATIONS);
		
		// when
		locationService.delete(LOCATION_UUID);
		
		// then
		Context.addProxyPrivilege(PrivilegeConstants.GET_LOCATIONS);
		assertThat(Context.getLocationService().getLocationByUuid(LOCATION_UUID).getRetired(), is(true));
	}
	
	@Test
	public void delete_shouldReportNotFoundWithoutTheDeletePrivilege() {
		// given
		Context.addProxyPrivilege(PrivilegeConstants.GET_LOCATIONS);
		
		// when / then
		assertThrows(ResourceNotFoundException.class, () -> locationService.delete(LOCATION_UUID));
	}
	
	@Test
	public void get_shouldReportNotFoundWithoutTheReadPrivilege() {
		// when / then
		assertThrows(ResourceNotFoundException.class, () -> locationService.get(LOCATION_UUID));
	}
}
