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

import lombok.AccessLevel;
import lombok.Setter;
import org.hibernate.proxy.HibernateProxy;
import org.hl7.fhir.r4.model.DiagnosticReport;
import org.hl7.fhir.r4.model.Reference;
import org.openmrs.Concept;
import org.openmrs.Encounter;
import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.Person;
import org.openmrs.api.db.hibernate.HibernateUtil;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.api.translators.ConceptTranslator;
import org.openmrs.module.fhir2.api.translators.DiagnosticReportTranslator;
import org.openmrs.module.fhir2.api.translators.EncounterReferenceTranslator;
import org.openmrs.module.fhir2.api.translators.ObservationReferenceTranslator;
import org.openmrs.module.fhir2.api.translators.PatientReferenceTranslator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Setter(AccessLevel.PACKAGE)
public class DiagnosticReportTranslatorImpl implements DiagnosticReportTranslator {
	
	@Autowired
	private EncounterReferenceTranslator<Encounter> encounterReferenceTranslator;
	
	@Autowired
	private PatientReferenceTranslator patientReferenceTranslator;
	
	@Autowired
	private ConceptTranslator conceptTranslator;
	
	@Autowired
	private ObservationReferenceTranslator observationReferenceTranslator;
	
	@Override
	public DiagnosticReport toFhirResource(Obs obsGroup) {
		notNull(obsGroup, "The Obs object should not be null");
		
		if (!obsGroup.isObsGrouping()) {
			throw new IllegalArgumentException("Obs object must be an Obs group.");
		}
		
		DiagnosticReport diagnosticReport = new DiagnosticReport();
		
		setResourceElements(obsGroup, diagnosticReport);
		
		diagnosticReport.getMeta().setLastUpdated(obsGroup.getDateChanged());
		
		return diagnosticReport;
	}
	
	@Override
	public Obs toOpenmrsType(DiagnosticReport diagnosticReport) {
		notNull(diagnosticReport, "The DiagnosticReport object should not be null");
		
		if (!diagnosticReport.hasResult()) {
			throw new IllegalArgumentException("Diagnostic Report must have at least one result");
		}
		
		Obs translatedObs = new Obs();
		
		setOpenmrsFields(diagnosticReport, translatedObs);
		
		return translatedObs;
	}
	
	@Override
	public Obs toOpenmrsType(Obs existingObs, DiagnosticReport diagnosticReport) {
		notNull(existingObs, "The existing Obs should not be null");
		notNull(diagnosticReport, "The DiagnosticReport object should not be null");
		
		if (!diagnosticReport.hasResult()) {
			throw new IllegalArgumentException("Diagnostic Report must have at least one result");
		}
		
		setOpenmrsFields(diagnosticReport, existingObs);
		
		return existingObs;
	}
	
	private void setResourceElements(Obs obsGroup, DiagnosticReport diagnosticReport) {
		// DiagnosticReport.id
		diagnosticReport.setId(obsGroup.getUuid());
		
		// DiagnosticReport.status
		diagnosticReport.setStatus(DiagnosticReport.DiagnosticReportStatus.UNKNOWN);
		
		// DiagnosticReport.encounter
		Encounter encounter = obsGroup.getEncounter();
		if (encounter != null) {
			diagnosticReport.setEncounter(encounterReferenceTranslator.toFhirResource(encounter));
		}
		
		// DiagnosticReport.subject
		Person subject = obsGroup.getPerson();
		if (subject != null) {
			if (subject instanceof HibernateProxy) {
				subject = HibernateUtil.getRealObjectFromProxy(subject);
			}
			
			if (subject instanceof Patient) {
				diagnosticReport.setSubject(patientReferenceTranslator.toFhirResource((Patient) subject));
			}
		}
		
		// DiagnosticReport.code
		Concept code = obsGroup.getConcept();
		if (code != null) {
			diagnosticReport.setCode(conceptTranslator.toFhirResource(code));
		}
		
		// DiagnosticReport.category
		diagnosticReport.addCategory().addCoding().setSystem(FhirConstants.DIAGNOSTIC_SERVICE_SECTIONS_VALUE_SET_URI)
		        .setCode(FhirConstants.DIAGNOSTIC_REPORT_CATEGORY_LAB);
		
		// DiagnosticReport.issued
		diagnosticReport.setIssued(obsGroup.getDateCreated());
		
		// DiagnosticReport.result
		for (Obs obs : obsGroup.getGroupMembers()) {
			diagnosticReport.addResult(observationReferenceTranslator.toFhirResource(obs));
		}
	}
	
	private void setOpenmrsFields(DiagnosticReport diagnosticReport, Obs translatedObs) {
		
		// DiagnosticReport.id
		if (translatedObs.getUuid() != null) {
			translatedObs.setUuid(diagnosticReport.getId());
		}
		
		// DiagnosticReport.status (Constant)
		
		// DiagnosticReport.encounter
		if (diagnosticReport.hasEncounter()) {
			translatedObs.setEncounter(encounterReferenceTranslator.toOpenmrsType(diagnosticReport.getEncounter()));
		}
		
		// DiagnosticReport.subject
		if (diagnosticReport.hasSubject()) {
			translatedObs.setPerson(patientReferenceTranslator.toOpenmrsType(diagnosticReport.getSubject()));
		}
		
		// DiagnosticReport.code
		if (diagnosticReport.hasCode()) {
			translatedObs.setConcept(conceptTranslator.toOpenmrsType(diagnosticReport.getCode()));
		}
		
		// DiagnosticReport.category (Constant)
		
		// DiagnosticReport.result
		for (Reference observationReference : diagnosticReport.getResult()) {
			translatedObs.addGroupMember(observationReferenceTranslator.toOpenmrsType(observationReference));
		}
		
		// Tag as diagnostic report
		// TODO: distinguish between DianosticReport and Observation mapping for a given Obs
		translatedObs.setComment("mapped DiagnosticReport");
		
	}
}
