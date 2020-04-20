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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.when;

import lombok.AccessLevel;
import lombok.Getter;
import org.hl7.fhir.r4.model.Appointment;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openmrs.module.fhir2.api.FhirAppointmentService;
import org.openmrs.module.fhir2.web.servlet.BaseFhirResourceProviderTest;
import org.springframework.mock.web.MockHttpServletResponse;

@RunWith(MockitoJUnitRunner.class)
public class AppointmentFhirResourceProviderWebTest extends BaseFhirResourceProviderTest<AppointmentFhirResourceProvider, Appointment> {
	
	private static final String APPOINTMENT_UUID = "1085AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
	
	private static final String WRONG_APPOINTMENT_UUID = "1010AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
	
	@Mock
	private FhirAppointmentService appointmentService;
	
	@Getter(AccessLevel.PUBLIC)
	private AppointmentFhirResourceProvider resourceProvider;
	
	@Before
	@Override
	public void setup() throws Exception {
		resourceProvider = new AppointmentFhirResourceProvider();
		resourceProvider.setAppointmentService(appointmentService);
		super.setup();
	}
	
	@Test
	public void shouldReturnAppointmentByUuid() throws Exception {
		Appointment appointment = new Appointment();
		appointment.setId(APPOINTMENT_UUID);
		when(appointmentService.getAppointmentByUuid(APPOINTMENT_UUID)).thenReturn(appointment);
		
		MockHttpServletResponse response = get("/Appointment/" + APPOINTMENT_UUID).accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), equalTo(FhirMediaTypes.JSON.toString()));
		
		Appointment result = readResponse(response);
		assertThat(result.getIdElement().getIdPart(), equalTo(APPOINTMENT_UUID));
	}
	
	@Test
	public void shouldReturn404IfAppointmentNotFound() throws Exception {
		when(appointmentService.getAppointmentByUuid(WRONG_APPOINTMENT_UUID)).thenReturn(null);
		
		MockHttpServletResponse response = get("/Appointment/" + WRONG_APPOINTMENT_UUID).accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isNotFound());
	}
}
