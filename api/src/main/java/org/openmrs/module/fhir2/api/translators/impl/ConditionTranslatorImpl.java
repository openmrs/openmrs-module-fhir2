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

import static org.apache.commons.lang3.Validate.notNull;

import javax.annotation.Nonnull;

import java.util.Date;

import ca.uhn.fhir.rest.server.exceptions.InternalErrorException;
import lombok.AccessLevel;
import lombok.Setter;
import org.hibernate.proxy.HibernateProxy;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.DateTimeType;
import org.openmrs.Concept;
import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.Person;
import org.openmrs.User;
import org.openmrs.annotation.OpenmrsProfile;
import org.openmrs.api.ConceptService;
import org.openmrs.api.db.hibernate.HibernateUtil;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.api.translators.ConceptTranslator;
import org.openmrs.module.fhir2.api.translators.ConditionClinicalStatusTranslator;
import org.openmrs.module.fhir2.api.translators.ConditionTranslator;
import org.openmrs.module.fhir2.api.translators.PatientReferenceTranslator;
import org.openmrs.module.fhir2.api.translators.PractitionerReferenceTranslator;
import org.openmrs.module.fhir2.api.translators.ProvenanceTranslator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Setter(AccessLevel.PACKAGE)
@Component
@OpenmrsProfile(openmrsPlatformVersion = "2.0.5 - 2.1.*")
public class ConditionTranslatorImpl implements ConditionTranslator<Obs> {
	
	@Autowired
	private PatientReferenceTranslator patientReferenceTranslator;
	
	@Autowired
	private PractitionerReferenceTranslator<User> practitionerReferenceTranslator;
	
	@Autowired
	private ConceptTranslator conceptTranslator;
	
	@Autowired
	private ProvenanceTranslator<Obs> provenanceTranslator;
	
	@Autowired
	private ConceptService conceptService;
	
	@Autowired
	private ConditionClinicalStatusTranslator<Obs> conditionClinicalStatusTranslator;
	
	@Override
	public org.hl7.fhir.r4.model.Condition toFhirResource(@Nonnull Obs obsCondition) {
		notNull(obsCondition, "The Openmrs Condition object should not be null");
		
		org.hl7.fhir.r4.model.Condition fhirCondition = new org.hl7.fhir.r4.model.Condition();
		fhirCondition.setId(obsCondition.getUuid());
		
		Person obsPerson = obsCondition.getPerson();
		if (obsPerson != null) {
			if (obsPerson instanceof HibernateProxy) {
				obsPerson = HibernateUtil.getRealObjectFromProxy(obsPerson);
			}
			
			if (obsPerson instanceof Patient) {
				fhirCondition.setSubject(patientReferenceTranslator.toFhirResource((Patient) obsPerson));
			}
		}
		if (obsCondition.getValueCoded() != null) {
			fhirCondition.setCode(conceptTranslator.toFhirResource(obsCondition.getValueCoded()));
		}
		fhirCondition.setClinicalStatus(conditionClinicalStatusTranslator.toFhirResource(obsCondition));
		fhirCondition.setOnset(new DateTimeType().setValue(obsCondition.getObsDatetime()));
		fhirCondition.setRecorder(practitionerReferenceTranslator.toFhirResource(obsCondition.getCreator()));
		fhirCondition.setRecordedDate(obsCondition.getDateCreated());
		fhirCondition.getMeta().setLastUpdated(obsCondition.getDateChanged());
		fhirCondition.addContained(provenanceTranslator.getCreateProvenance(obsCondition));
		fhirCondition.addContained(provenanceTranslator.getUpdateProvenance(obsCondition));
		
		return fhirCondition;
	}
	
	@Override
	public Obs toOpenmrsType(@Nonnull org.hl7.fhir.r4.model.Condition condition) {
		notNull(condition, "The Condition object should not be null");
		return this.toOpenmrsType(new Obs(), condition);
	}
	
	@Override
	public Obs toOpenmrsType(@Nonnull Obs existingObsCondition, @Nonnull org.hl7.fhir.r4.model.Condition condition) {
		notNull(existingObsCondition, "The existing Openmrs Obs Condition object should not be null");
		notNull(condition, "The Condition object should not be null");
		existingObsCondition.setUuid(condition.getIdElement().getIdPart());
		CodeableConcept codeableConcept = condition.getCode();
		existingObsCondition.setValueCoded(conceptTranslator.toOpenmrsType(codeableConcept));
		existingObsCondition.setPerson(patientReferenceTranslator.toOpenmrsType(condition.getSubject()));
		Concept problemList = conceptService.getConcept(FhirConstants.CONDITION_OBSERVATION_CONCEPT_ID);
		if (problemList != null) {
			existingObsCondition.setConcept(problemList);
		} else {
			throw new InternalErrorException(
			        "Concept " + FhirConstants.CONDITION_OBSERVATION_CONCEPT_ID + " ProblemList Not found");
		}
		Date onsetTime = condition.getOnsetDateTimeType().getValue();
		Date recordTime = condition.getRecordedDateElement().getValue();
		if (onsetTime != null) {
			existingObsCondition.setObsDatetime(onsetTime);
		} else if (recordTime != null) {
			existingObsCondition.setObsDatetime(recordTime);
		}
		existingObsCondition.setCreator(practitionerReferenceTranslator.toOpenmrsType(condition.getRecorder()));
		
		return existingObsCondition;
	}
}
