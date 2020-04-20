/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.fhir2.providers;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;

import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import org.hl7.fhir.r4.model.Appointment;
import org.hl7.fhir.r4.model.IdType;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openmrs.module.fhir2.api.FhirAppointmentService;
import org.openmrs.module.fhir2.web.servlet.BaseFhirProvenanceResourceTest;

@RunWith(MockitoJUnitRunner.class)
public class AppointmentFhirResourceProviderTest extends BaseFhirProvenanceResourceTest<Appointment> {
	
	private static final String APPOINTMENT_UUID = "1085AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
	
	private static final String WRONG_APPOINTMENT_UUID = "1010AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
	
	@Mock
	private FhirAppointmentService appointmentService;
	
	private Appointment appointment;
	
	private AppointmentFhirResourceProvider resourceProvider;
	
	@Before
	public void setUp() {
		resourceProvider = new AppointmentFhirResourceProvider();
		resourceProvider.setAppointmentService(appointmentService);
	}
	
	@Before
	public void initAppointment() {
		appointment = new Appointment();
		appointment.setId(APPOINTMENT_UUID);
	}
	
	@Test
	public void getResourceType_shouldReturnResourceType() {
		assertThat(resourceProvider.getResourceType(), equalTo(Appointment.class));
		assertThat(resourceProvider.getResourceType().getName(), equalTo(Appointment.class.getName()));
	}
	
	@Test
	public void getAppointmentByUuid_shouldReturnMatchingAppointment() {
		when(appointmentService.getAppointmentByUuid(APPOINTMENT_UUID)).thenReturn(appointment);
		IdType id = new IdType();
		id.setValue(APPOINTMENT_UUID);
		Appointment appointment = resourceProvider.getAppointmentByUuid(id);
		assertThat(appointment, notNullValue());
		assertThat(appointment.getId(), notNullValue());
		assertThat(appointment.getId(), equalTo(APPOINTMENT_UUID));
	}
	
	@Test(expected = ResourceNotFoundException.class)
	public void getAppointmentWithWrongUuid_shouldThrowResourceNotFoundException() {
		IdType id = new IdType();
		id.setValue(WRONG_APPOINTMENT_UUID);
		Appointment result = resourceProvider.getAppointmentByUuid(id);
		assertThat(result, nullValue());
	}
}
