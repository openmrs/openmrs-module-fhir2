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
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.DateTimeType;
import org.openmrs.Concept;
import org.openmrs.annotation.OpenmrsProfile;
import org.openmrs.module.emrapi.conditionslist.Condition;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.api.translators.ConceptTranslator;
import org.openmrs.module.fhir2.api.translators.ConditionClinicalStatusTranslator;
import org.openmrs.module.fhir2.api.translators.ConditionTranslator;
import org.openmrs.module.fhir2.api.translators.CreatorReferenceTranslator;
import org.openmrs.module.fhir2.api.translators.PatientReferenceTranslator;
import org.springframework.stereotype.Component;

@Component
@Setter(AccessLevel.PACKAGE)
@OpenmrsProfile(openmrsPlatformVersion = "2.0.* - 2.1.*")
public class ConditionTranslatorImpl_2_0 implements ConditionTranslator<Condition> {
	
	@Inject
	private PatientReferenceTranslator patientReferenceTranslator;
	
	@Inject
	private ConditionClinicalStatusTranslator<Condition.Status> clinicalStatusTranslator;
	
	@Inject
	private ConceptTranslator conceptTranslator;
	
	@Inject
	private CreatorReferenceTranslator creatorReferenceTranslator;
	
	@Override
	public org.hl7.fhir.r4.model.Condition toFhirResource(Condition condition) {
		if (condition == null) {
			return null;
		}
		org.hl7.fhir.r4.model.Condition fhirCondition = new org.hl7.fhir.r4.model.Condition();
		fhirCondition.setId(condition.getUuid());
		fhirCondition.setSubject(patientReferenceTranslator.toFhirResource(condition.getPatient()));
		fhirCondition.setClinicalStatus(clinicalStatusTranslator.toFhirResource(condition.getStatus()));
		if (condition.getConcept() == null) {
			CodeableConcept codeableConcept = new CodeableConcept();
			codeableConcept
			        .addCoding(new Coding().setCode(condition.getConditionNonCoded()).setSystem(FhirConstants.OPENMRS_URI));
			fhirCondition.setCode(codeableConcept);
		} else {
			fhirCondition.setCode(conceptTranslator.toFhirResource(condition.getConcept()));
		}
		fhirCondition.setOnset(new DateTimeType().setValue(condition.getOnsetDate()));
		fhirCondition.setRecorder(creatorReferenceTranslator.toFhirResource(condition.getCreator()));
		fhirCondition.setRecordedDate(condition.getDateCreated());
		
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
		existingCondition.setStatus(clinicalStatusTranslator.toOpenmrsType(condition.getClinicalStatus()));
		if (!condition.getCode().getCoding().isEmpty()) {
			Concept concept = conceptTranslator.toOpenmrsType(condition.getCode());
			if (concept == null) {
				existingCondition.setConditionNonCoded(condition.getCode().getCoding().get(0).getCode());
			} else {
				existingCondition.setConcept(concept);
			}
		}
		existingCondition.setOnsetDate(condition.getOnsetDateTimeType().getValue());
		existingCondition.setCreator(creatorReferenceTranslator.toOpenmrsType(condition.getRecorder()));
		existingCondition.setDateCreated(condition.getRecordedDate());
		
		return existingCondition;
	}
}
