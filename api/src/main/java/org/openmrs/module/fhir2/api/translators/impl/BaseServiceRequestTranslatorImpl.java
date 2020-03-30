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

import java.util.Collection;

import lombok.AccessLevel;
import lombok.Setter;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.ServiceRequest;
import org.hl7.fhir.r4.model.Task;
import org.openmrs.module.fhir2.api.FhirTaskService;
import org.springframework.beans.factory.annotation.Autowired;

@Setter(AccessLevel.PROTECTED)
public class BaseServiceRequestTranslatorImpl {
	
	@Autowired
	private FhirTaskService taskService;
	
	protected ServiceRequest.ServiceRequestStatus determineServiceRequestStatus(String orderUuid) {
		Collection<Task> serviceRequestTasks = taskService.getTasksByBasedOn(ServiceRequest.class, orderUuid);
		
		ServiceRequest.ServiceRequestStatus serviceRequestStatus = ServiceRequest.ServiceRequestStatus.UNKNOWN;
		
		if (serviceRequestTasks.size() != 1) {
			return serviceRequestStatus;
		}
		
		Task serviceRequestTask = serviceRequestTasks.iterator().next();
		
		if (serviceRequestTask.hasStatus()) {
			switch (serviceRequestTask.getStatus()) {
				case ACCEPTED:
				case REQUESTED:
					serviceRequestStatus = ServiceRequest.ServiceRequestStatus.ACTIVE;
					break;
				case REJECTED:
					serviceRequestStatus = ServiceRequest.ServiceRequestStatus.REVOKED;
					break;
				case COMPLETED:
					serviceRequestStatus = ServiceRequest.ServiceRequestStatus.COMPLETED;
					break;
			}
		}
		return serviceRequestStatus;
	}
	
	protected Reference determineServiceRequestPerformer(String orderUuid) {
		Collection<Task> serviceRequestTasks = taskService.getTasksByBasedOn(ServiceRequest.class, orderUuid);
		
		if (serviceRequestTasks.size() != 1) {
			return null;
		}
		
		return serviceRequestTasks.iterator().next().getOwner();
	}
}
