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

import javax.inject.Inject;

import lombok.AccessLevel;
import lombok.Setter;
import org.hl7.fhir.r4.model.ServiceRequest;
import org.openmrs.TestOrder;
import org.openmrs.module.fhir2.api.FhirServiceRequestService;
import org.openmrs.module.fhir2.api.dao.FhirServiceRequestDao;
import org.openmrs.module.fhir2.api.translators.ServiceRequestTranslator;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional
@Setter(AccessLevel.PACKAGE)
public class FhirServiceRequestServiceImpl implements FhirServiceRequestService {
	
	@Inject
	private ServiceRequestTranslator<TestOrder> translator;
	
	@Inject
	private FhirServiceRequestDao<TestOrder> dao;
	
	@Transactional(readOnly = true)
	public ServiceRequest getServiceRequestByUuid(String uuid) {
		TestOrder openmrsOrder = dao.getServiceRequestByUuid(uuid);
		
		return translator.toFhirResource(openmrsOrder);
	}
}
