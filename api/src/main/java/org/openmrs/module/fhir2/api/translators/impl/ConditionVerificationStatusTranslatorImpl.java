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

import javax.annotation.Nonnull;

import java.util.Optional;

import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.openmrs.ConditionVerificationStatus;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.api.translators.ConditionVerificationStatusTranslator;
import org.springframework.stereotype.Component;

@Component
public class ConditionVerificationStatusTranslatorImpl implements ConditionVerificationStatusTranslator<ConditionVerificationStatus> {
	
	@Override
	public CodeableConcept toFhirResource(@Nonnull ConditionVerificationStatus verificationStatus) {
		if (verificationStatus == null) {
			return null;
		}
		
		return new CodeableConcept().addCoding(addVerificationStatusCoding(verificationStatus.toString()));
	}
	
	@Override
	public ConditionVerificationStatus toOpenmrsType(@Nonnull CodeableConcept codeableConcept) {
		if (codeableConcept == null || codeableConcept.getCoding().isEmpty()) {
			return null;
		}
		
		Optional<Coding> verificationStatus = codeableConcept.getCoding().stream()
		        .filter(c -> c.getSystem().equals(FhirConstants.CONDITION_VERIFICATION_STATUS_SYSTEM_URI)).findFirst();
		
		Coding verificationStatusCode = verificationStatus.orElse(null);
		
		if (verificationStatusCode == null) {
			return null;
		}
		
		switch (verificationStatusCode.getCode()) {
			case "confirmed":
				return ConditionVerificationStatus.CONFIRMED;
			case "provisional":
				return ConditionVerificationStatus.PROVISIONAL;
			default:
				return null;
		}
	}
	
	private Coding addVerificationStatusCoding(String verificationStatus) {
		Coding coding = new Coding();
		coding.setSystem(FhirConstants.CONDITION_VERIFICATION_STATUS_SYSTEM_URI);
		coding.setCode(verificationStatus.toLowerCase());
		coding.setDisplay(verificationStatus.replaceAll("_", "-").toUpperCase());
		return coding;
	}
}
