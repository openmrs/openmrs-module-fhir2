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

import java.util.Optional;

import lombok.AccessLevel;
import lombok.Setter;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.DateTimeType;
import org.hl7.fhir.r4.model.Extension;
import org.hl7.fhir.r4.model.StringType;
import org.openmrs.CodedOrFreeText;
import org.openmrs.Condition;
import org.openmrs.ConditionClinicalStatus;
import org.openmrs.ConditionVerificationStatus;
import org.openmrs.User;
import org.openmrs.annotation.OpenmrsProfile;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.api.translators.ConceptTranslator;
import org.openmrs.module.fhir2.api.translators.ConditionClinicalStatusTranslator;
import org.openmrs.module.fhir2.api.translators.ConditionTranslator;
import org.openmrs.module.fhir2.api.translators.ConditionVerificationStatusTranslator;
import org.openmrs.module.fhir2.api.translators.PatientReferenceTranslator;
import org.openmrs.module.fhir2.api.translators.PractitionerReferenceTranslator;
import org.openmrs.module.fhir2.api.translators.ProvenanceTranslator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Setter(AccessLevel.PACKAGE)
@Component
@OpenmrsProfile(openmrsPlatformVersion = "2.2.* - 2.*")
public class ConditionTranslatorImpl_2_2 implements ConditionTranslator<Condition> {
	
	@Autowired
	private PatientReferenceTranslator patientReferenceTranslator;
	
	@Autowired
	private ConditionClinicalStatusTranslator<ConditionClinicalStatus> clinicalStatusTranslator;
	
	@Autowired
	private ConditionVerificationStatusTranslator<ConditionVerificationStatus> verificationStatusTranslator;
	
	@Autowired
	private PractitionerReferenceTranslator<User> practitionerReferenceTranslator;
	
	@Autowired
	private ConceptTranslator conceptTranslator;
	
	@Autowired
	private ProvenanceTranslator<Condition> provenanceTranslator;
	
	@Override
	public org.hl7.fhir.r4.model.Condition toFhirResource(Condition condition) {
		if (condition == null) {
			return null;
		}
		
		org.hl7.fhir.r4.model.Condition fhirCondition = new org.hl7.fhir.r4.model.Condition();
		fhirCondition.setId(condition.getUuid());
		fhirCondition.setSubject(patientReferenceTranslator.toFhirResource(condition.getPatient()));
		fhirCondition.setClinicalStatus(clinicalStatusTranslator.toFhirResource(condition.getClinicalStatus()));
		fhirCondition.setVerificationStatus(verificationStatusTranslator.toFhirResource(condition.getVerificationStatus()));
		
		CodedOrFreeText codedOrFreeTextCondition = condition.getCondition();
		if (codedOrFreeTextCondition != null) {
			fhirCondition.setCode(conceptTranslator.toFhirResource(codedOrFreeTextCondition.getCoded()));
			if (codedOrFreeTextCondition.getNonCoded() != null) {
				Extension extension = new Extension();
				extension.setUrl(FhirConstants.OPENMRS_FHIR_EXT_NON_CODED_CONDITION);
				extension.setValue(new StringType(codedOrFreeTextCondition.getNonCoded()));
				fhirCondition.addExtension(extension);
			}
		}
		
		fhirCondition.setOnset(new DateTimeType().setValue(condition.getOnsetDate()));
		fhirCondition.setRecorder(practitionerReferenceTranslator.toFhirResource(condition.getCreator()));
		fhirCondition.setRecordedDate(condition.getDateCreated());
		fhirCondition.getMeta().setLastUpdated(condition.getDateChanged());
		fhirCondition.addContained(provenanceTranslator.getCreateProvenance(condition));
		fhirCondition.addContained(provenanceTranslator.getUpdateProvenance(condition));
		
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
		
		CodeableConcept codeableConcept = condition.getCode();
		CodedOrFreeText conditionCodedOrText = new CodedOrFreeText();
		Optional<Extension> extension = Optional
		        .ofNullable(condition.getExtensionByUrl(FhirConstants.OPENMRS_FHIR_EXT_NON_CODED_CONDITION));
		extension.ifPresent(value -> conditionCodedOrText.setNonCoded(String.valueOf(value.getValue())));
		conditionCodedOrText.setCoded(conceptTranslator.toOpenmrsType(codeableConcept));
		existingCondition.setCondition(conditionCodedOrText);
		
		existingCondition.setOnsetDate(condition.getOnsetDateTimeType().getValue());
		existingCondition.setCreator(practitionerReferenceTranslator.toOpenmrsType(condition.getRecorder()));
		
		return existingCondition;
	}
}
