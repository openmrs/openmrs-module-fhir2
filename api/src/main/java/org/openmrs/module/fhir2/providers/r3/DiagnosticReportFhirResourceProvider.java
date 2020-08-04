/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.fhir2.providers.r3;

import javax.validation.constraints.NotNull;

import ca.uhn.fhir.rest.annotation.Create;
import ca.uhn.fhir.rest.annotation.Delete;
import ca.uhn.fhir.rest.annotation.IdParam;
import ca.uhn.fhir.rest.annotation.OptionalParam;
import ca.uhn.fhir.rest.annotation.Read;
import ca.uhn.fhir.rest.annotation.ResourceParam;
import ca.uhn.fhir.rest.annotation.Search;
import ca.uhn.fhir.rest.annotation.Sort;
import ca.uhn.fhir.rest.annotation.Update;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.api.SortSpec;
import ca.uhn.fhir.rest.api.server.IBundleProvider;
import ca.uhn.fhir.rest.param.DateRangeParam;
import ca.uhn.fhir.rest.param.ReferenceAndListParam;
import ca.uhn.fhir.rest.param.TokenAndListParam;
import ca.uhn.fhir.rest.server.IResourceProvider;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import lombok.AccessLevel;
import lombok.Setter;
import org.hl7.fhir.convertors.conv30_40.DiagnosticReport30_40;
import org.hl7.fhir.dstu3.model.DiagnosticReport;
import org.hl7.fhir.dstu3.model.Encounter;
import org.hl7.fhir.dstu3.model.IdType;
import org.hl7.fhir.dstu3.model.OperationOutcome;
import org.hl7.fhir.dstu3.model.Patient;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.openmrs.module.fhir2.api.FhirDiagnosticReportService;
import org.openmrs.module.fhir2.providers.util.FhirProviderUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component("diagnosticReportFhirR3ResourceProvider")
@Qualifier("fhirR3Resources")
@Setter(AccessLevel.PACKAGE)
public class DiagnosticReportFhirResourceProvider implements IResourceProvider {
	
	@Autowired
	private FhirDiagnosticReportService diagnosticReportService;
	
	@Override
	public Class<? extends IBaseResource> getResourceType() {
		return DiagnosticReport.class;
	}
	
	@Read
	@SuppressWarnings("unused")
	public DiagnosticReport getDiagnosticReportById(@IdParam @NotNull IdType id) {
		org.hl7.fhir.r4.model.DiagnosticReport diagnosticReport = diagnosticReportService.get(id.getIdPart());
		if (diagnosticReport == null) {
			throw new ResourceNotFoundException("Could not find diagnosticReport with Id " + id.getIdPart());
		}
		
		return DiagnosticReport30_40.convertDiagnosticReport(diagnosticReport);
	}
	
	@Create
	@SuppressWarnings("unused")
	public MethodOutcome createDiagnosticReport(@ResourceParam DiagnosticReport diagnosticReport) {
		return FhirProviderUtils.buildCreate(DiagnosticReport30_40.convertDiagnosticReport(
		    diagnosticReportService.create(DiagnosticReport30_40.convertDiagnosticReport(diagnosticReport))));
	}
	
	@Update
	@SuppressWarnings("unused")
	public MethodOutcome updateDiagnosticReport(@IdParam IdType id, @ResourceParam DiagnosticReport diagnosticReport) {
		String idPart = null;
		
		if (id != null) {
			idPart = id.getIdPart();
		}
		
		return FhirProviderUtils.buildUpdate(DiagnosticReport30_40.convertDiagnosticReport(
		    diagnosticReportService.update(idPart, DiagnosticReport30_40.convertDiagnosticReport(diagnosticReport))));
	}
	
	@Delete
	@SuppressWarnings("unused")
	public OperationOutcome deleteDiagnosticReport(@IdParam @NotNull IdType id) {
		org.hl7.fhir.r4.model.DiagnosticReport diagnosticReport = diagnosticReportService.delete(id.getIdPart());
		if (diagnosticReport == null) {
			throw new ResourceNotFoundException("Could not find medication to delete with id " + id.getIdPart());
		}
		
		return FhirProviderUtils.buildDelete(DiagnosticReport30_40.convertDiagnosticReport(diagnosticReport));
	}
	
	@Search
	public IBundleProvider searchForDiagnosticReports(
	        @OptionalParam(name = DiagnosticReport.SP_ENCOUNTER, chainWhitelist = {
	                "" }, targetTypes = Encounter.class) ReferenceAndListParam encounterReference,
	        @OptionalParam(name = DiagnosticReport.SP_PATIENT, chainWhitelist = { "", Patient.SP_IDENTIFIER,
	                Patient.SP_GIVEN, Patient.SP_FAMILY,
	                Patient.SP_NAME }, targetTypes = Patient.class) ReferenceAndListParam patientReference,
	        @OptionalParam(name = DiagnosticReport.SP_SUBJECT, chainWhitelist = { "", Patient.SP_IDENTIFIER, Patient.SP_NAME,
	                Patient.SP_GIVEN, Patient.SP_FAMILY }) ReferenceAndListParam subjectReference,
	        @OptionalParam(name = DiagnosticReport.SP_ISSUED) DateRangeParam issueDate,
	        @OptionalParam(name = DiagnosticReport.SP_CODE) TokenAndListParam code,
	        @OptionalParam(name = DiagnosticReport.SP_RESULT) ReferenceAndListParam result,
	        @OptionalParam(name = DiagnosticReport.SP_RES_ID) TokenAndListParam id,
	        @OptionalParam(name = "_lastUpdated") DateRangeParam lastUpdated, @Sort SortSpec sort) {
		if (patientReference == null) {
			patientReference = subjectReference;
		}
		return diagnosticReportService.searchForDiagnosticReports(encounterReference, patientReference, issueDate, code,
		    result, id, lastUpdated, sort);
	}
}
