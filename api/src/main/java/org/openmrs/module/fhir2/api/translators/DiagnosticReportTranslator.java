/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.fhir2.api.translators;

import org.hl7.fhir.r4.model.DiagnosticReport;
import org.openmrs.Obs;
import org.openmrs.module.fhir2.FhirDiagnosticReport;

public interface DiagnosticReportTranslator extends OpenmrsFhirUpdatableTranslator<FhirDiagnosticReport, DiagnosticReport> {
	
	/**
	 * Maps a {@link Obs} to a {@link DiagnosticReport}
	 *
	 * @param fhirDiagnosticReport the FhirDiagnosticReport object to translate
	 * @return the corresponding FHIR DiagnosticReport
	 */
	@Override
	DiagnosticReport toFhirResource(FhirDiagnosticReport fhirDiagnosticReport);
	
	/**
	 * Maps {@link DiagnosticReport} to {@link FhirDiagnosticReport}
	 *
	 * @param diagnosticReport the FHIR diagnostic report to translate
	 * @return the corresponding OpenMRS FhirDiagnosticReport
	 */
	@Override
	FhirDiagnosticReport toOpenmrsType(DiagnosticReport diagnosticReport);
	
	/**
	 * Maps a {@link DiagnosticReport} to an existing {@link FhirDiagnosticReport}
	 *
	 * @param existingDiagnosticReport the existing FhirDiagnosticReport to update
	 * @param diagnosticReport the diagnostic report to map
	 * @return an updated version of the existingObs
	 */
	@Override
	FhirDiagnosticReport toOpenmrsType(FhirDiagnosticReport existingDiagnosticReport, DiagnosticReport diagnosticReport);
}
