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
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThrows;

import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import org.hl7.fhir.r4.model.ContactPoint;
import org.hl7.fhir.r4.model.Location;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.GlobalProperty;
import org.openmrs.LocationAttribute;
import org.openmrs.LocationAttributeType;
import org.openmrs.api.context.Context;
import org.openmrs.customdatatype.datatype.FreeTextDatatype;
import org.openmrs.module.fhir2.BaseFhirContextSensitiveTest;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.api.FhirContactPointMapService;
import org.openmrs.module.fhir2.api.FhirLocationService;
import org.openmrs.module.fhir2.api.util.FhirGlobalPropertyHolder;
import org.openmrs.module.fhir2.model.FhirContactPointMap;
import org.openmrs.util.PrivilegeConstants;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Checks the privileges a composite service requires against the real DAO guards: each operation
 * needs only the privilege of the operation itself, and a missing privilege is reported as not
 * found.
 */
public class CompositeServicePrivilegeTest extends BaseFhirContextSensitiveTest {
	
	private static final String LOCATION_UUID = "8d6c993e-c2cc-11de-8d13-0010c6dffd0f";
	
	private static final String UNKNOWN_UUID = "99999999-9999-9999-9999-999999999999";
	
	@Autowired
	private FhirLocationService locationService;
	
	@Autowired
	private FhirContactPointMapService contactPointMapService;
	
	@Before
	public void setup() {
		Context.logout();
	}
	
	@After
	public void cleanup() {
		Context.removeProxyPrivilege(PrivilegeConstants.MANAGE_LOCATIONS);
		Context.removeProxyPrivilege(PrivilegeConstants.GET_LOCATIONS);
		Context.removeProxyPrivilege(PrivilegeConstants.GET_LOCATION_ATTRIBUTE_TYPES);
		FhirGlobalPropertyHolder.reset();
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
	
	@Test
	public void update_shouldReportNotFoundForBothKnownAndUnknownUuidsWithoutTheWritePrivilege() {
		// given
		Location known = new Location();
		known.setId(LOCATION_UUID);
		Location unknown = new Location();
		unknown.setId(UNKNOWN_UUID);
		
		// when / then
		assertThrows(ResourceNotFoundException.class, () -> locationService.update(LOCATION_UUID, known, null, true));
		assertThrows(ResourceNotFoundException.class, () -> locationService.update(UNKNOWN_UUID, unknown, null, true));
	}
	
	@Test
	public void get_shouldOmitTelecomWithoutTheAttributeTypePrivilege() {
		// given
		givenLocationWithTelecom();
		Context.addProxyPrivilege(PrivilegeConstants.GET_LOCATIONS);
		
		// when
		Location result = locationService.get(LOCATION_UUID);
		
		// then
		assertThat(result.getTelecom(), empty());
	}
	
	@Test
	public void get_shouldIncludeTelecomWithTheAttributeTypePrivilege() {
		// given
		givenLocationWithTelecom();
		Context.addProxyPrivilege(PrivilegeConstants.GET_LOCATIONS);
		Context.addProxyPrivilege(PrivilegeConstants.GET_LOCATION_ATTRIBUTE_TYPES);
		
		// when
		Location result = locationService.get(LOCATION_UUID);
		
		// then
		assertThat(result.getTelecom(), hasSize(1));
		assertThat(result.getTelecom().get(0).getSystem(), equalTo(ContactPoint.ContactPointSystem.PHONE));
	}
	
	private void givenLocationWithTelecom() {
		authenticate();
		try {
			LocationAttributeType type = new LocationAttributeType();
			type.setName("telecom");
			type.setDatatypeClassname(FreeTextDatatype.class.getName());
			type.setMinOccurs(0);
			Context.getLocationService().saveLocationAttributeType(type);
			
			Context.getAdministrationService().saveGlobalProperty(
			    new GlobalProperty(FhirConstants.LOCATION_CONTACT_POINT_ATTRIBUTE_TYPE, type.getUuid()));
			FhirGlobalPropertyHolder.reset();
			
			org.openmrs.Location location = Context.getLocationService().getLocationByUuid(LOCATION_UUID);
			LocationAttribute attribute = new LocationAttribute();
			attribute.setAttributeType(type);
			attribute.setValue("555-1234");
			location.addAttribute(attribute);
			Context.getLocationService().saveLocation(location);
			
			FhirContactPointMap map = new FhirContactPointMap();
			map.setAttributeTypeDomain("location");
			map.setAttributeTypeId(type.getId());
			map.setSystem(ContactPoint.ContactPointSystem.PHONE);
			map.setUse(ContactPoint.ContactPointUse.WORK);
			contactPointMapService.saveFhirContactPointMap(map);
			
			Context.flushSession();
			Context.clearSession();
		}
		finally {
			Context.logout();
		}
	}
}
