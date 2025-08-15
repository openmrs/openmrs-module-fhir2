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

import static lombok.AccessLevel.PROTECTED;
import static org.apache.commons.lang3.Validate.notNull;
import static org.openmrs.module.fhir2.api.translators.impl.FhirTranslatorUtils.getLastUpdated;
import static org.openmrs.module.fhir2.api.translators.impl.FhirTranslatorUtils.getVersionId;

import javax.annotation.Nonnull;

import java.util.Optional;

import lombok.Getter;
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
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.api.translators.ConceptTranslator;
import org.openmrs.module.fhir2.api.translators.ConditionClinicalStatusTranslator;
import org.openmrs.module.fhir2.api.translators.ConditionTranslator;
import org.openmrs.module.fhir2.api.translators.ConditionVerificationStatusTranslator;
import org.openmrs.module.fhir2.api.translators.PatientReferenceTranslator;
import org.openmrs.module.fhir2.api.translators.PractitionerReferenceTranslator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ConditionTranslatorImpl implements ConditionTranslator<Condition> {
	
	@Getter(PROTECTED)
	@Setter(value = PROTECTED, onMethod_ = @Autowired)
	private PatientReferenceTranslator patientReferenceTranslator;
	
	@Getter(PROTECTED)
	@Setter(value = PROTECTED, onMethod_ = @Autowired)
	private ConditionClinicalStatusTranslator<ConditionClinicalStatus> clinicalStatusTranslator;
	
	@Getter(PROTECTED)
	@Setter(value = PROTECTED, onMethod_ = @Autowired)
	private ConditionVerificationStatusTranslator<ConditionVerificationStatus> verificationStatusTranslator;
	
	@Getter(PROTECTED)
	@Setter(value = PROTECTED, onMethod_ = @Autowired)
	private PractitionerReferenceTranslator<User> practitionerReferenceTranslator;
	
	@Getter(PROTECTED)
	@Setter(value = PROTECTED, onMethod_ = @Autowired)
	private ConceptTranslator conceptTranslator;
	
	@Override
	public org.hl7.fhir.r4.model.Condition toFhirResource(@Nonnull Condition condition) {
		notNull(condition, "The Openmrs Condition object should not be null");
		
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
		if (condition.getEndDate() != null) {
			fhirCondition.setAbatement(new DateTimeType().setValue(condition.getEndDate()));
		}
		
		if (condition.getAdditionalDetail() != null) {
			fhirCondition.addNote().setText(condition.getAdditionalDetail());
		}
		
		fhirCondition.setRecorder(practitionerReferenceTranslator.toFhirResource(condition.getCreator()));
		fhirCondition.setRecordedDate(condition.getDateCreated());
		
		CodeableConcept category = new CodeableConcept();
		category.addCoding().setSystem(FhirConstants.CONDITION_CATEGORY_SYSTEM_URI)
		        .setCode(FhirConstants.CONDITION_CATEGORY_CODE_CONDITION).setDisplay("Problem List Item");
		fhirCondition.addCategory(category);
		
		fhirCondition.getMeta().setLastUpdated(getLastUpdated(condition));
		fhirCondition.getMeta().setVersionId(getVersionId(condition));
		
		return fhirCondition;
	}
	
	@Override
	public Condition toOpenmrsType(@Nonnull org.hl7.fhir.r4.model.Condition condition) {
		notNull(condition, "The Condition object should not be null");
		return this.toOpenmrsType(new Condition(), condition);
	}
	
	@Override
	public Condition toOpenmrsType(@Nonnull Condition existingCondition,
	        @Nonnull org.hl7.fhir.r4.model.Condition condition) {
		notNull(existingCondition, "The existing Openmrs Condition object should not be null");
		notNull(condition, "The Condition object should not be null");
		
		if (condition.hasId()) {
			existingCondition.setUuid(condition.getIdElement().getIdPart());
		}
		
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
		
		if (condition.hasNote()) {
			existingCondition.setAdditionalDetail(condition.getNoteFirstRep().getText());
		}
		
		if (condition.hasOnsetDateTimeType()) {
			existingCondition.setOnsetDate(condition.getOnsetDateTimeType().getValue());
		} else if (condition.hasOnsetPeriod()) {
			existingCondition.setOnsetDate(condition.getOnsetPeriod().getStart());
		}
		
		if (condition.hasAbatementDateTimeType()) {
			existingCondition.setEndDate(condition.getAbatementDateTimeType().getValue());
		} else if (condition.hasAbatementPeriod()) {
			existingCondition.setEndDate(condition.getAbatementPeriod().getEnd());
		}
		
		existingCondition.setCreator(practitionerReferenceTranslator.toOpenmrsType(condition.getRecorder()));
		
		return existingCondition;
	}
}
