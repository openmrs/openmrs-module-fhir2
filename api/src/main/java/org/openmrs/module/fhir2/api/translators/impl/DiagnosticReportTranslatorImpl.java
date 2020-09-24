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
import java.util.stream.Collectors;

import lombok.AccessLevel;
import lombok.Setter;
import org.hl7.fhir.r4.model.DiagnosticReport;
import org.openmrs.Concept;
import org.openmrs.Encounter;
import org.openmrs.Obs;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.FhirDiagnosticReport;
import org.openmrs.module.fhir2.api.translators.ConceptTranslator;
import org.openmrs.module.fhir2.api.translators.DiagnosticReportTranslator;
import org.openmrs.module.fhir2.api.translators.EncounterReferenceTranslator;
import org.openmrs.module.fhir2.api.translators.ObservationReferenceTranslator;
import org.openmrs.module.fhir2.api.translators.PatientReferenceTranslator;
import org.openmrs.module.fhir2.api.util.FhirUtils;
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
	private ObservationReferenceTranslator observationReferenceTranslator;
	
	@Autowired
	private ConceptTranslator conceptTranslator;
	
	@Override
	public DiagnosticReport toFhirResource(@Nonnull FhirDiagnosticReport fhirDiagnosticReport) {
		notNull(fhirDiagnosticReport, "The diagnostic report should not be null");
		
		DiagnosticReport diagnosticReport = new DiagnosticReport();
		
		diagnosticReport.setId(fhirDiagnosticReport.getUuid());
		
		if (fhirDiagnosticReport.getDateChanged() != null) {
			diagnosticReport.getMeta().setLastUpdated(fhirDiagnosticReport.getDateChanged());
		} else {
			diagnosticReport.getMeta().setLastUpdated(fhirDiagnosticReport.getDateCreated());
		}
		
		if (fhirDiagnosticReport.getStatus() != null) {
			diagnosticReport
			        .setStatus(DiagnosticReport.DiagnosticReportStatus.valueOf(fhirDiagnosticReport.getStatus().toString()));
		} else {
			diagnosticReport.setStatus(DiagnosticReport.DiagnosticReportStatus.UNKNOWN);
		}
		
		if (fhirDiagnosticReport.getEncounter() != null) {
			diagnosticReport.setEncounter(encounterReferenceTranslator.toFhirResource(fhirDiagnosticReport.getEncounter()));
		}
		
		if (fhirDiagnosticReport.getSubject() != null) {
			diagnosticReport.setSubject(patientReferenceTranslator.toFhirResource(fhirDiagnosticReport.getSubject()));
		}
		
		Concept code = fhirDiagnosticReport.getCode();
		if (code != null) {
			diagnosticReport.setCode(conceptTranslator.toFhirResource(code));
		}
		
		diagnosticReport.addCategory().addCoding().setSystem(FhirConstants.DIAGNOSTIC_REPORT_SERVICE_SYSTEM_URI)
		        .setCode(FhirConstants.DIAGNOSTIC_REPORT_CATEGORY_LAB);
		
		diagnosticReport.setIssued(fhirDiagnosticReport.getIssued());
		
		for (Obs obs : fhirDiagnosticReport.getResults()) {
			diagnosticReport.addResult(observationReferenceTranslator.toFhirResource(obs));
		}
		
		return diagnosticReport;
	}
	
	@Override
	public FhirDiagnosticReport toOpenmrsType(@Nonnull DiagnosticReport diagnosticReport) {
		return toOpenmrsType(new FhirDiagnosticReport(), diagnosticReport);
	}
	
	@Override
	public FhirDiagnosticReport toOpenmrsType(@Nonnull FhirDiagnosticReport existingDiagnosticReport,
	        @Nonnull DiagnosticReport diagnosticReport) {
		notNull(existingDiagnosticReport, "The existing Obs should not be null");
		notNull(diagnosticReport, "The DiagnosticReport object should not be null");
		
		if (diagnosticReport.hasId() && existingDiagnosticReport.getUuid() == null) {
			existingDiagnosticReport.setUuid(diagnosticReport.getId());
		}
		
		if (diagnosticReport.hasStatus()) {
			FhirDiagnosticReport.DiagnosticReportStatus status;
			try {
				status = FhirDiagnosticReport.DiagnosticReportStatus.valueOf(diagnosticReport.getStatus().toString());
			}
			catch (IllegalArgumentException | NullPointerException ignored) {
				status = FhirDiagnosticReport.DiagnosticReportStatus.UNKNOWN;
			}
			existingDiagnosticReport.setStatus(status);
		}
		
		if (diagnosticReport.hasEncounter()) {
			existingDiagnosticReport
			        .setEncounter(encounterReferenceTranslator.toOpenmrsType(diagnosticReport.getEncounter()));
		}
		
		if (diagnosticReport.hasSubject()) {
			FhirUtils.getReferenceType(diagnosticReport.getSubject()).ifPresent(t -> {
				if (FhirConstants.PATIENT.equals(t)) {
					existingDiagnosticReport
					        .setSubject(patientReferenceTranslator.toOpenmrsType(diagnosticReport.getSubject()));
				}
			});
			
		}
		
		if (diagnosticReport.hasCode()) {
			existingDiagnosticReport.setCode(conceptTranslator.toOpenmrsType(diagnosticReport.getCode()));
		}
		
		if (diagnosticReport.hasIssued()) {
			existingDiagnosticReport.setIssued(diagnosticReport.getIssued());
		} else if (existingDiagnosticReport.getIssued() == null) {
			existingDiagnosticReport.setIssued(new Date());
		}
		
		existingDiagnosticReport.setResults(diagnosticReport.getResult().stream()
		        .map(observationReferenceTranslator::toOpenmrsType).collect(Collectors.toSet()));
		
		return existingDiagnosticReport;
	}
}
