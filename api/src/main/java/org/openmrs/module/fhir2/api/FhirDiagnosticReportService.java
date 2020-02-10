/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.fhir2.api;

import org.hl7.fhir.r4.model.DiagnosticReport;

public interface FhirDiagnosticReportService {
	
	/**
	 * Get diagnostic report by the UUID
	 *
	 * @param uuid Identifier for requested diagnostic report
	 * @return diagnostic report with given internal identifier
	 */
	DiagnosticReport getDiagnosticReportByUuid(String uuid);
	
	/**
	 * Get diagnostic report by the UUID
	 *
	 * @return Created diagnostic report
	 */
	DiagnosticReport saveDiagnosticReport(DiagnosticReport diagnosticReport);
	
	/**
	 * Get diagnostic report by the UUID
	 *
	 * @param uuid Target DiagnosticReport identifier
	 * @param diagnosticReport DiagnosticReport to update
	 * @return Updated diagnostic report
	 */
	DiagnosticReport updateDiagnosticReport(String uuid, DiagnosticReport diagnosticReport);
}
