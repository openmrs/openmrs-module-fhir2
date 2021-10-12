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
import java.util.stream.Collectors;

import ca.uhn.fhir.rest.api.server.IBundleProvider;
import ca.uhn.fhir.rest.param.ReferenceAndListParam;
import ca.uhn.fhir.rest.param.ReferenceOrListParam;
import ca.uhn.fhir.rest.param.ReferenceParam;
import lombok.AccessLevel;
import lombok.Setter;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.Task;
import org.openmrs.module.fhir2.api.FhirTaskService;
import org.springframework.beans.factory.annotation.Autowired;

@Setter(AccessLevel.PROTECTED)
public abstract class BaseServiceRequestTranslator {
	
	private static final int START_INDEX = 0;
	
	private static final int END_INDEX = 10;
	
	@Autowired
	private FhirTaskService taskService;
	
	protected Reference determineServiceRequestPerformer(String orderUuid) {
		IBundleProvider results = taskService.searchForTasks(
		    new ReferenceAndListParam()
		            .addAnd(new ReferenceOrListParam().add(new ReferenceParam("ServiceRequest", null, orderUuid))),
		    null, null, null, null, null, null);
		
		Collection<Task> serviceRequestTasks = results.getResources(START_INDEX, END_INDEX).stream().map(p -> (Task) p)
		        .collect(Collectors.toList());
		
		if (serviceRequestTasks.size() != 1) {
			return null;
		}
		
		return serviceRequestTasks.iterator().next().getOwner();
	}
}
