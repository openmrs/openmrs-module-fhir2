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

import org.openmrs.module.fhir2.api.FhirConditionService;
import org.openmrs.module.fhir2.api.FhirDiagnosisService;
import org.openmrs.module.fhir2.api.annotations.R3Provider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("fhirConditionServiceR3")
@R3Provider
public class FhirConditionServiceR3Impl extends FhirConditionServiceImpl implements FhirConditionService {
	
	@Override
	@Autowired
	@R3Provider
	void setDiagnosisService(FhirDiagnosisService diagnosisService) {
		super.setDiagnosisService(diagnosisService);
	}
}
