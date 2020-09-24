/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.fhir2.api.validators;

import org.openmrs.annotation.Handler;
import org.openmrs.module.fhir2.model.FhirDiagnosticReport;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Handler(supports = FhirDiagnosticReport.class, order = 50)
public class DiagnosticReportValidator implements Validator {
	
	@Override
	public boolean supports(Class<?> clazz) {
		return FhirDiagnosticReport.class.isAssignableFrom(clazz);
	}
	
	@Override
	public void validate(Object target, Errors errors) {
		if (!(target instanceof FhirDiagnosticReport)) {
			return;
		}
		
		FhirDiagnosticReport diagnosticReport = (FhirDiagnosticReport) target;
		
		if (diagnosticReport.getStatus() == null) {
			errors.rejectValue("status", "diagnosticReport.error.statusRequired");
		}
		
		if (diagnosticReport.getCode() == null) {
			errors.rejectValue("code", "diagnosticReport.error.codeRequired");
		}
	}
}
