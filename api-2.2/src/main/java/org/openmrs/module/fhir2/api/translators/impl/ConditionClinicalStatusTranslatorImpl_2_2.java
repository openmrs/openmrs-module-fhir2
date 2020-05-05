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
import org.openmrs.ConditionClinicalStatus;
import org.openmrs.annotation.OpenmrsProfile;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.api.translators.ConditionClinicalStatusTranslator;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

@Primary
@Component
@OpenmrsProfile(openmrsPlatformVersion = "2.2.* - 2.*")
public class ConditionClinicalStatusTranslatorImpl_2_2 implements ConditionClinicalStatusTranslator<ConditionClinicalStatus> {
	
	@Override
	public CodeableConcept toFhirResource(ConditionClinicalStatus clinicalStatus) {
		return this.setClinicalStatus(clinicalStatus.toString());
	}
	
	@Override
	public ConditionClinicalStatus toOpenmrsType(CodeableConcept codeableConcept) {
		return codeableConcept.getCoding().stream().filter(coding -> coding.getSystem().equals(FhirConstants.OPENMRS_URI))
		        .map(this::getClinicalStatus).findFirst().orElse(null);
	}
	
	private ConditionClinicalStatus getClinicalStatus(Coding coding) {
		switch (coding.getCode().trim().toLowerCase()) {
			case "active":
				return ConditionClinicalStatus.ACTIVE;
			case "history_of":
				return ConditionClinicalStatus.HISTORY_OF;
			case "inactive":
			default:
				return ConditionClinicalStatus.INACTIVE;
		}
	}
	
	private CodeableConcept setClinicalStatus(String text) {
		CodeableConcept codeableConcept = new CodeableConcept();
		codeableConcept.addCoding().setCode(text).setSystem(FhirConstants.OPENMRS_URI);
		return codeableConcept;
	}
}
