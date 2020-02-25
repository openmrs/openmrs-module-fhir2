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

import lombok.AccessLevel;
import lombok.Setter;
import org.hl7.fhir.r4.model.ServiceRequest;
import org.openmrs.TestOrder;
import org.openmrs.module.fhir2.api.translators.ConceptTranslator;
import org.openmrs.module.fhir2.api.translators.ServiceRequestTranslator;
import org.springframework.stereotype.Component;

@Component
@Setter(AccessLevel.PACKAGE)
public class ServiceRequestTranslatorImpl extends BaseServiceRequestTranslatorImpl implements ServiceRequestTranslator<TestOrder> {
	
	@Inject
	private ConceptTranslator conceptTranslator;
	
	@Override
	public ServiceRequest toFhirResource(TestOrder order) {
		if (order == null) {
			return null;
		}
		
		ServiceRequest serviceRequest = new ServiceRequest();
		
		serviceRequest.setId(order.getUuid());
		
		serviceRequest.setStatus(determineServiceRequestStatus(order.getUuid()));
		serviceRequest.setCode(conceptTranslator.toFhirResource(order.getConcept()));
		serviceRequest.setIntent(ServiceRequest.ServiceRequestIntent.ORDER);
		serviceRequest.getMeta().setLastUpdated(order.getDateChanged());
		
		return serviceRequest;
	}
}
