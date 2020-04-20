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
import static org.hamcrest.Matchers.nullValue;

import org.apache.commons.lang3.NotImplementedException;
import org.hl7.fhir.r4.model.Appointment;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class FhirAppointmentServiceImplTest {
	
	private static final String APPOINTMENT_UUID = "1085AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
	
	private FhirAppointmentServiceImpl appointmentService;
	
	private Appointment appointment;
	
	@Before
	public void setup() {
		appointmentService = new FhirAppointmentServiceImpl();
		
		appointment = new Appointment();
		appointment.setId(APPOINTMENT_UUID);
	}
	
	@Test(expected = NotImplementedException.class)
	public void getAppointmentByUuid_shouldThrowNotImplementedException() {
		assertThat(appointmentService.getAppointmentByUuid(APPOINTMENT_UUID), nullValue());
	}
}
