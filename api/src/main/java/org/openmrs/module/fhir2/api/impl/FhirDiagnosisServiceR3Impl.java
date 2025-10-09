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

import org.openmrs.module.fhir2.api.FhirDiagnosisService;
import org.openmrs.module.fhir2.api.annotations.R3Provider;
import org.openmrs.module.fhir2.api.translators.DiagnosisTranslator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("fhirDiagnosisServiceR3")
@R3Provider
public class FhirDiagnosisServiceR3Impl extends FhirDiagnosisServiceImpl implements FhirDiagnosisService {
	
	@Override
	@Autowired
	protected void setTranslator(DiagnosisTranslator translator) {
		super.setTranslator(translator);
	}
}
