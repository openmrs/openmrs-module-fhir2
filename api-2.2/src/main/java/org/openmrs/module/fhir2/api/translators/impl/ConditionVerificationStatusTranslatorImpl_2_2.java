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
import org.openmrs.ConditionVerificationStatus;
import org.openmrs.annotation.OpenmrsProfile;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.api.translators.ConditionVerificationStatusTranslator;
import org.springframework.stereotype.Component;

@Component
@OpenmrsProfile(openmrsPlatformVersion = "2.2.* - 2.3.*")
public class ConditionVerificationStatusTranslatorImpl_2_2 implements ConditionVerificationStatusTranslator<ConditionVerificationStatus> {
	
	@Override
	public CodeableConcept toFhirResource(ConditionVerificationStatus verificationStatus) {
		if (verificationStatus == null) {
			return null;
		}
		return this.setVerificationStatus(verificationStatus.toString());
	}
	
	@Override
	public ConditionVerificationStatus toOpenmrsType(CodeableConcept codeableConcept) {
		return codeableConcept.getCoding().stream().filter(coding -> coding.getSystem().equals(FhirConstants.OPENMRS_URI))
		        .map(this::getVerificationStatus).findFirst().get();
	}
	
	private CodeableConcept setVerificationStatus(String text) {
		CodeableConcept codeableConcept = new CodeableConcept();
		codeableConcept.addCoding().setCode(text).setDisplay(text).setSystem(FhirConstants.OPENMRS_URI);
		return codeableConcept;
	}
	
	private ConditionVerificationStatus getVerificationStatus(Coding coding) {
		switch (coding.getCode().trim().toLowerCase()) {
			case "confirmed":
				return ConditionVerificationStatus.CONFIRMED;
			case "provisional":
			default:
				return ConditionVerificationStatus.PROVISIONAL;
		}
	}
}
