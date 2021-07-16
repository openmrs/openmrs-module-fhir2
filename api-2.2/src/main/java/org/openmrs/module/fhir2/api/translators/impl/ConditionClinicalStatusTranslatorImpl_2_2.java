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

import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.openmrs.ConditionClinicalStatus;
import org.openmrs.annotation.OpenmrsProfile;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.api.translators.ConditionClinicalStatusTranslator;
import org.springframework.stereotype.Component;

@Component
@OpenmrsProfile(openmrsPlatformVersion = "2.2.* - 2.*")
public class ConditionClinicalStatusTranslatorImpl_2_2 implements ConditionClinicalStatusTranslator<ConditionClinicalStatus> {
	
	@Override
	public CodeableConcept toFhirResource(@Nonnull ConditionClinicalStatus clinicalStatus) {
		if (clinicalStatus == null) {
			return null;
		}
		
		CodeableConcept codeableConcept = new CodeableConcept();
		switch (clinicalStatus) {
			case ACTIVE:
				codeableConcept.addCoding().setCode(clinicalStatus.toString().toLowerCase()).setDisplay("Active")
				        .setSystem(FhirConstants.CONDITION_CLINICAL_STATUS_SYSTEM_URI);
				break;
			case INACTIVE:
				codeableConcept.addCoding().setCode(clinicalStatus.toString().toLowerCase()).setDisplay("Inactive")
				        .setSystem(FhirConstants.CONDITION_CLINICAL_STATUS_SYSTEM_URI);
				break;
			default:
				codeableConcept.addCoding().setCode("inactive").setDisplay("Inactive")
				        .setSystem(FhirConstants.CONDITION_CLINICAL_STATUS_SYSTEM_URI);
				break;
		}
		
		return codeableConcept;
	}
	
	@Override
	public ConditionClinicalStatus toOpenmrsType(@Nonnull CodeableConcept codeableConcept) {
		if (codeableConcept == null) {
			return null;
		}
		
		return codeableConcept.getCoding().stream()
		        .filter(coding -> coding.getSystem().equals(FhirConstants.CONDITION_CLINICAL_STATUS_SYSTEM_URI))
		        .map(this::getClinicalStatus).findFirst().orElse(null);
	}
	
	private ConditionClinicalStatus getClinicalStatus(Coding coding) {
		if (coding.getCode() == null) {
			return ConditionClinicalStatus.INACTIVE;
		}
		
		switch (coding.getCode().trim().toLowerCase()) {
			case "active":
				return ConditionClinicalStatus.ACTIVE;
			case "inactive":
			default:
				return ConditionClinicalStatus.INACTIVE;
		}
	}
}
