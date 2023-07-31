/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 * 
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.fhir2.api.translators.impl;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import org.hl7.fhir.r4.model.ContactPoint;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.openmrs.module.fhir2.model.FhirContactPoint;

@RunWith(MockitoJUnitRunner.class)
public class TelecomTranslatorImplTest {
	
	private static final String CONTACT_POINT_ID = "e2323we23-323j34-23k23-23m23";
	
	private static final String CONTACT_POINT_VALUE = "254237283723723";
	
	private TelecomTranslatorImpl telecomTranslator;
	
	private ContactPoint fhirContactPoint;
	
	private FhirContactPoint openmrsContactPoint;
	
	@Before
	public void setUp() {
		telecomTranslator = new TelecomTranslatorImpl();
		fhirContactPoint = new ContactPoint();
		openmrsContactPoint = new FhirContactPoint();
	}
	
	@Test
	public void toFhirResource_shouldTranslateOpenmrsContactPointUuidToFhirContactPointId() {
		openmrsContactPoint.setUuid(CONTACT_POINT_ID);
		ContactPoint contactPoint = telecomTranslator.toFhirResource(openmrsContactPoint);
		assertThat(contactPoint, notNullValue());
		assertThat(contactPoint.getId(), equalTo(CONTACT_POINT_ID));
	}
	
	@Test
	public void toOpenmrsType_shouldTranslateFhirContactPointIdToOpenmrsContactPointUuid() {
		fhirContactPoint.setId(CONTACT_POINT_ID);
		FhirContactPoint result = telecomTranslator.toOpenmrsType(new FhirContactPoint(), fhirContactPoint);
		assertThat(result, notNullValue());
		assertThat(result.getUuid(), equalTo(CONTACT_POINT_ID));
	}
	
	@Test
	public void toFhirResource_shouldTranslateOpenmrsContactPointValueToFhirContactPointValue() {
		openmrsContactPoint.setValue(CONTACT_POINT_VALUE);
		ContactPoint contactPoint = telecomTranslator.toFhirResource(openmrsContactPoint);
		assertThat(contactPoint, notNullValue());
		assertThat(contactPoint.getValue(), equalTo(CONTACT_POINT_VALUE));
	}
	
	@Test
	public void toOpenmrsType_shouldTranslateFhirContactPointValueToOpenmrsContactPointValue() {
		fhirContactPoint.setValue(CONTACT_POINT_VALUE);
		FhirContactPoint result = telecomTranslator.toOpenmrsType(new FhirContactPoint(), fhirContactPoint);
		assertThat(result, notNullValue());
		assertThat(result.getValue(), equalTo(CONTACT_POINT_VALUE));
	}
	
	@Test
	public void toFhirResource_shouldTranslateOpenmrsContactPointSystemToFhirContactPointSystem() {
		openmrsContactPoint.setSystem(ContactPoint.ContactPointSystem.EMAIL);
		ContactPoint contactPoint = telecomTranslator.toFhirResource(openmrsContactPoint);
		assertThat(contactPoint, notNullValue());
		assertThat(contactPoint.getSystem(), equalTo(ContactPoint.ContactPointSystem.EMAIL));
	}
	
	@Test
	public void toOpenmrsType_shouldTranslateFhirContactPointSystemToOpenmrsContactPointSystem() {
		fhirContactPoint.setSystem(ContactPoint.ContactPointSystem.PHONE);
		FhirContactPoint result = telecomTranslator.toOpenmrsType(new FhirContactPoint(), fhirContactPoint);
		assertThat(result, notNullValue());
		assertThat(result.getSystem(), equalTo(ContactPoint.ContactPointSystem.PHONE));
	}
	
	@Test
	public void toFhirResource_shouldTranslateOpenmrsContactPointUseToFhirContactPointUse() {
		openmrsContactPoint.setUse(ContactPoint.ContactPointUse.HOME);
		ContactPoint contactPoint = telecomTranslator.toFhirResource(openmrsContactPoint);
		assertThat(contactPoint, notNullValue());
		assertThat(contactPoint.getUse(), equalTo(ContactPoint.ContactPointUse.HOME));
	}
	
	@Test
	public void toOpenmrsType_shouldTranslateFhirContactPointUseToOpenmrsContactPointUse() {
		fhirContactPoint.setUse(ContactPoint.ContactPointUse.MOBILE);
		FhirContactPoint result = telecomTranslator.toOpenmrsType(new FhirContactPoint(), fhirContactPoint);
		assertThat(result, notNullValue());
		assertThat(result.getUse(), equalTo(ContactPoint.ContactPointUse.MOBILE));
	}
}
