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

import org.apache.commons.lang3.NotImplementedException;
import org.hl7.fhir.r4.model.Appointment;
import org.openmrs.module.fhir2.api.FhirAppointmentService;
import org.springframework.stereotype.Component;

@Component
public class FhirAppointmentServiceImpl implements FhirAppointmentService {
	
	@Override
	public Appointment getAppointmentByUuid(String uuid) {
		throw new NotImplementedException("NotImplementedException");
	}
}
