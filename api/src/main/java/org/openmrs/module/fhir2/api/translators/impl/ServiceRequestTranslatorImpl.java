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

import javax.inject.Inject;
import javax.validation.constraints.NotNull;

import java.util.Collection;

import lombok.AccessLevel;
import lombok.Setter;
import org.hl7.fhir.r4.model.ServiceRequest;
import org.hl7.fhir.r4.model.Task;
import org.openmrs.TestOrder;
import org.openmrs.module.fhir2.api.FhirTaskService;
import org.openmrs.module.fhir2.api.translators.ConceptTranslator;
import org.openmrs.module.fhir2.api.translators.ServiceRequestTranslator;
import org.springframework.stereotype.Component;

@Component
@Setter(AccessLevel.PACKAGE)
public class ServiceRequestTranslatorImpl implements ServiceRequestTranslator {
	
	@Inject
	ConceptTranslator conceptTranslator;
	
	@Inject
	FhirTaskService taskService;
	
	@Override
	public ServiceRequest toFhirResource(@NotNull TestOrder order) {
		ServiceRequest serviceRequest = new ServiceRequest();
		
		if (order != null) {
			serviceRequest.setId(order.getUuid());
			
			serviceRequest.setStatus(determineServiceRequestStatus(order.getUuid()));
			serviceRequest.setCode(conceptTranslator.toFhirResource(order.getConcept()));
			serviceRequest.setIntent(ServiceRequest.ServiceRequestIntent.ORDER);
		}
		
		return serviceRequest;
	}
	
	@Override
	public TestOrder toOpenmrsType(ServiceRequest serviceRequest) {
		return null;
	}
	
	@Override
	public TestOrder toOpenmrsType(TestOrder currentOrder, ServiceRequest serviceRequest) {
		return null;
	}
	
	private ServiceRequest.ServiceRequestStatus determineServiceRequestStatus(String orderUuid) {
		Collection<Task> serviceRequestTasks = taskService.getTasksByBasedOn(ServiceRequest.class, orderUuid);
		
		ServiceRequest.ServiceRequestStatus serviceRequestStatus = ServiceRequest.ServiceRequestStatus.UNKNOWN;
		
		if (serviceRequestTasks.size() != 1)
			return serviceRequestStatus;
		
		Task serviceRequestTask = serviceRequestTasks.iterator().next();
		
		if (serviceRequestTask.getStatus() != null) {
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
}
