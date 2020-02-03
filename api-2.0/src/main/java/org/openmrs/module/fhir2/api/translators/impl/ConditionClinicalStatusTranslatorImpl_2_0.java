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
import org.hl7.fhir.r4.model.Coding;
import org.openmrs.annotation.OpenmrsProfile;
import org.openmrs.module.emrapi.conditionslist.Condition;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.api.translators.ConditionClinicalStatusTranslator;
import org.springframework.stereotype.Component;

@Component
@OpenmrsProfile(openmrsPlatformVersion = "2.0.* - 2.1.*")
public class ConditionClinicalStatusTranslatorImpl_2_0 implements ConditionClinicalStatusTranslator {
	
	@Override
	public CodeableConcept toFhirResource(org.openmrs.module.emrapi.conditionslist.Condition.Status status) {
		return setClinicalStatus(status.toString());
	}
	
	@Override
	public Condition.Status toOpenmrsType(CodeableConcept codeableConcept) {
		Condition.Status status = codeableConcept.getCoding().stream()
		        .filter(coding -> coding.getSystem().equals(FhirConstants.OPENMRS_URI)).map(this::getClinicalStatus)
		        .findFirst().get();
		return status;
	}
	
	private Condition.Status getClinicalStatus(Coding coding) {
		switch (coding.getDisplay().trim().toLowerCase()) {
			case "active":
				return Condition.Status.ACTIVE;
			case "history_of":
				return Condition.Status.HISTORY_OF;
			case "inactive":
			default:
				return Condition.Status.INACTIVE;
		}
	}
	
	private CodeableConcept setClinicalStatus(String text) {
		CodeableConcept codeableConcept = new CodeableConcept();
		codeableConcept.addCoding().setCode(text).setDisplay(text).setSystem(FhirConstants.OPENMRS_URI);
		return codeableConcept;
	}
}
