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
import org.openmrs.Condition;
import org.openmrs.ConditionClinicalStatus;
import org.openmrs.ConditionVerificationStatus;
import org.openmrs.annotation.OpenmrsProfile;
import org.openmrs.module.fhir2.api.translators.ConditionClinicalStatusTranslator;
import org.openmrs.module.fhir2.api.translators.ConditionTranslator;
import org.openmrs.module.fhir2.api.translators.ConditionVerificationStatusTranslator;
import org.openmrs.module.fhir2.api.translators.PatientReferenceTranslator;

@Setter(AccessLevel.PACKAGE)
@OpenmrsProfile(openmrsPlatformVersion = "2.2.* - 2.3.*")
public class ConditionTranslatorImpl_2_2 implements ConditionTranslator<Condition> {
	
	@Inject
	private PatientReferenceTranslator patientReferenceTranslator;
	
	@Inject
	private ConditionClinicalStatusTranslator<ConditionClinicalStatus> clinicalStatusTranslator;
	
	@Inject
	private ConditionVerificationStatusTranslator<ConditionVerificationStatus> verificationStatusTranslator;
	
	@Override
	public org.hl7.fhir.r4.model.Condition toFhirResource(Condition condition) {
		org.hl7.fhir.r4.model.Condition fhirCondition = new org.hl7.fhir.r4.model.Condition();
		fhirCondition.setId(condition.getUuid());
		fhirCondition.setSubject(patientReferenceTranslator.toFhirResource(condition.getPatient()));
		fhirCondition.setClinicalStatus(clinicalStatusTranslator.toFhirResource(condition.getClinicalStatus()));
		fhirCondition.setVerificationStatus(verificationStatusTranslator.toFhirResource(condition.getVerificationStatus()));
		return fhirCondition;
	}
	
	@Override
	public Condition toOpenmrsType(org.hl7.fhir.r4.model.Condition condition) {
		return this.toOpenmrsType(new Condition(), condition);
	}
	
	@Override
	public Condition toOpenmrsType(Condition existingCondition, org.hl7.fhir.r4.model.Condition condition) {
		if (condition == null) {
			return existingCondition;
		}
		existingCondition.setUuid(condition.getId());
		existingCondition.setPatient(patientReferenceTranslator.toOpenmrsType(condition.getSubject()));
		existingCondition.setClinicalStatus(clinicalStatusTranslator.toOpenmrsType(condition.getClinicalStatus()));
		
		existingCondition
		        .setVerificationStatus(verificationStatusTranslator.toOpenmrsType(condition.getVerificationStatus()));
		return existingCondition;
	}
}
