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

import org.hl7.fhir.r4.model.CodeableConcept;
import org.openmrs.Obs;
import org.openmrs.annotation.OpenmrsProfile;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.api.translators.ConditionClinicalStatusTranslator;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

@Primary
@Component
@OpenmrsProfile(openmrsPlatformVersion = "2.0.5 - 2.1.*")
public class ConditionClinicalStatusTranslatorImpl implements ConditionClinicalStatusTranslator<Obs> {
	
	@Override
	public CodeableConcept toFhirResource(Obs clinicalStatus) {
		CodeableConcept codeableConcept = new CodeableConcept();
		codeableConcept.addCoding().setCode("active").setDisplay("active")
		        .setSystem(FhirConstants.CONDITION_CLINICAL_STATUS_SYSTEM_URI);
		return codeableConcept;
	}
	
	@Override
	public Obs toOpenmrsType(CodeableConcept codeableConcept) {
		return null;
	}
	
}
