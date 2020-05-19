/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.fhir2.providers;

import javax.validation.constraints.NotNull;

import ca.uhn.fhir.rest.annotation.Create;
import ca.uhn.fhir.rest.annotation.IdParam;
import ca.uhn.fhir.rest.annotation.OptionalParam;
import ca.uhn.fhir.rest.annotation.Read;
import ca.uhn.fhir.rest.annotation.ResourceParam;
import ca.uhn.fhir.rest.annotation.Search;
import ca.uhn.fhir.rest.annotation.Sort;
import ca.uhn.fhir.rest.annotation.Update;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.api.SortSpec;
import ca.uhn.fhir.rest.param.DateRangeParam;
import ca.uhn.fhir.rest.param.ReferenceAndListParam;
import ca.uhn.fhir.rest.param.TokenAndListParam;
import ca.uhn.fhir.rest.server.IResourceProvider;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import lombok.AccessLevel;
import lombok.Setter;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.DiagnosticReport;
import org.hl7.fhir.r4.model.Encounter;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.Patient;
import org.openmrs.module.fhir2.api.FhirDiagnosticReportService;
import org.openmrs.module.fhir2.util.FhirServerUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
@Qualifier("fhirResources")
@Setter(AccessLevel.PACKAGE)
public class DiagnosticReportFhirResourceProvider implements IResourceProvider {
	
	@Autowired
	private FhirDiagnosticReportService service;
	
	@Override
	public Class<? extends IBaseResource> getResourceType() {
		return DiagnosticReport.class;
	}
	
	@Read
	public DiagnosticReport getDiagnosticReportById(@IdParam @NotNull IdType id) {
		DiagnosticReport diagnosticReport = service.get(id.getIdPart());
		
		if (diagnosticReport == null) {
			throw new ResourceNotFoundException("Could not find Diagnostic Report with Id " + id.getIdPart());
		}
		
		return diagnosticReport;
	}
	
	@Create
	public MethodOutcome createDiagnosticReport(@ResourceParam DiagnosticReport diagnosticReport) {
		return FhirServerUtils.buildCreate(service.create(diagnosticReport));
	}
	
	@Update
	public MethodOutcome updateDiagnosticReport(@IdParam IdType id, @ResourceParam DiagnosticReport diagnosticReport) {
		String idPart = null;
		
		if (id != null) {
			idPart = id.getIdPart();
		}
		
		return FhirServerUtils.buildUpdate(service.update(idPart, diagnosticReport));
	}
	
	@Search
	public Bundle searchForDiagnosticReports(
	        @OptionalParam(name = DiagnosticReport.SP_ENCOUNTER, chainWhitelist = {
	                "" }, targetTypes = Encounter.class) ReferenceAndListParam encounterReference,
	        @OptionalParam(name = DiagnosticReport.SP_PATIENT, chainWhitelist = { "", Patient.SP_IDENTIFIER,
	                Patient.SP_GIVEN, Patient.SP_FAMILY,
	                Patient.SP_NAME }, targetTypes = Patient.class) ReferenceAndListParam patientReference,
	        @OptionalParam(name = DiagnosticReport.SP_ISSUED) DateRangeParam issueDate,
	        @OptionalParam(name = DiagnosticReport.SP_CODE) TokenAndListParam code, @Sort SortSpec sort) {
		return FhirServerUtils.convertSearchResultsToBundle(
		    service.searchForDiagnosticReports(encounterReference, patientReference, issueDate, code, sort));
	}
}
