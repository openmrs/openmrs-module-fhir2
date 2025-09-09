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

import javax.annotation.Nonnull;

import java.util.Collections;
import java.util.List;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.EpisodeOfCare;
import org.hl7.fhir.r4.model.Period;
import org.openmrs.PatientProgram;
import org.openmrs.module.fhir2.api.translators.ConceptTranslator;
import org.openmrs.module.fhir2.api.translators.EpisodeOfCareTranslator;
import org.openmrs.module.fhir2.api.translators.PatientReferenceTranslator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class EpisodeOfCareTranslatorImpl implements EpisodeOfCareTranslator {
	
	@Getter(PROTECTED)
	@Setter(value = PROTECTED, onMethod_ = @Autowired)
	private PatientReferenceTranslator patientReferenceTranslator;
	
	@Getter(PROTECTED)
	@Setter(value = PROTECTED, onMethod_ = @Autowired)
	private ConceptTranslator conceptTranslator;
	
	@Override
	public EpisodeOfCare toFhirResource(@Nonnull PatientProgram patientProgram) {
		notNull(patientProgram, "The Openmrs PatientProgram object should not be null");
		
		EpisodeOfCare episodeOfCare = new EpisodeOfCare();
		
		episodeOfCare.setId(patientProgram.getUuid());
		episodeOfCare.setPeriod(getPeriod(patientProgram));
		episodeOfCare.setPatient(patientReferenceTranslator.toFhirResource(patientProgram.getPatient()));
		episodeOfCare.setType(getType(patientProgram));
		
		episodeOfCare.setStatus(getStatus(patientProgram));
		
		return episodeOfCare;
	}
	
	@Override
	public PatientProgram toOpenmrsType(@Nonnull EpisodeOfCare episodeOfCare) {
		notNull(episodeOfCare, "The EpisodeOfCare object should not be null");
		return this.toOpenmrsType(new PatientProgram(), episodeOfCare);
	}
	
	@Override
	public PatientProgram toOpenmrsType(@Nonnull PatientProgram patientProgram, @Nonnull EpisodeOfCare episodeOfCare) {
		throw new UnsupportedOperationException(
		        "Translation from FHIR resource EpisodeOfCare to OpenMRS object PatientProgram is not supported.");
	}
	
	private List<CodeableConcept> getType(PatientProgram patientProgram) {
		if (patientProgram.getProgram() == null) {
			return Collections.emptyList();
		}
		
		CodeableConcept codeableConcept = conceptTranslator.toFhirResource(patientProgram.getProgram().getConcept());
		if (codeableConcept == null) {
			return Collections.emptyList();
		}
		return Collections.singletonList(codeableConcept);
	}
	
	private EpisodeOfCare.EpisodeOfCareStatus getStatus(PatientProgram patientProgram) {
		if (patientProgram.getActive()) {
			return EpisodeOfCare.EpisodeOfCareStatus.ACTIVE;
		} else {
			return EpisodeOfCare.EpisodeOfCareStatus.FINISHED;
		}
	}
	
	private Period getPeriod(PatientProgram program) {
		Period period = new Period();
		
		period.setStart(program.getDateEnrolled());
		period.setEnd(program.getDateCompleted());
		
		return period;
	}
}
