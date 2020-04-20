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

import javax.validation.constraints.NotNull;

import ca.uhn.fhir.rest.annotation.IdParam;
import ca.uhn.fhir.rest.annotation.Read;
import ca.uhn.fhir.rest.server.IResourceProvider;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import lombok.AccessLevel;
import lombok.Setter;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.Appointment;
import org.hl7.fhir.r4.model.IdType;
import org.openmrs.module.fhir2.api.FhirAppointmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
@Qualifier("fhirResources")
@Setter(AccessLevel.PACKAGE)
public class AppointmentFhirResourceProvider implements IResourceProvider {
	
	@Autowired
	private FhirAppointmentService appointmentService;
	
	@Override
	public Class<? extends IBaseResource> getResourceType() {
		return Appointment.class;
	}
	
	@Read
	@SuppressWarnings("unused")
	public Appointment getAppointmentByUuid(@IdParam @NotNull IdType id) {
		Appointment appointment = appointmentService.getAppointmentByUuid(id.getIdPart());
		if (appointment == null) {
			throw new ResourceNotFoundException("Could not find Appointment with Id " + id.getIdPart());
		}
		return appointment;
	}
}
