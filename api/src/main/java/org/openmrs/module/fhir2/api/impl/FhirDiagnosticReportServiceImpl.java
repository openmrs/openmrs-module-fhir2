/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.fhir2.api.impl;

import ca.uhn.fhir.rest.api.server.IBundleProvider;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.hl7.fhir.r4.model.DiagnosticReport;
import org.openmrs.module.fhir2.api.FhirDiagnosticReportService;
import org.openmrs.module.fhir2.api.dao.FhirDiagnosticReportDao;
import org.openmrs.module.fhir2.api.search.SearchQuery;
import org.openmrs.module.fhir2.api.search.SearchQueryInclude;
import org.openmrs.module.fhir2.api.search.param.DiagnosticReportSearchParams;
import org.openmrs.module.fhir2.api.translators.DiagnosticReportTranslator;
import org.openmrs.module.fhir2.model.FhirDiagnosticReport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional
@Setter(AccessLevel.PACKAGE)
@Getter(AccessLevel.PROTECTED)
public class FhirDiagnosticReportServiceImpl extends BaseFhirService<DiagnosticReport, FhirDiagnosticReport> implements FhirDiagnosticReportService {
	
	@Autowired
	private FhirDiagnosticReportDao dao;
	
	@Autowired
	private DiagnosticReportTranslator translator;
	
	@Autowired
	private SearchQueryInclude<DiagnosticReport> searchQueryInclude;
	
	@Autowired
	private SearchQuery<FhirDiagnosticReport, DiagnosticReport, FhirDiagnosticReportDao, DiagnosticReportTranslator, SearchQueryInclude<DiagnosticReport>> searchQuery;
	
	@Override
	public IBundleProvider searchForDiagnosticReports(DiagnosticReportSearchParams diagnosticReportSearchParams) {
		return searchQuery.getQueryResults(diagnosticReportSearchParams.toSearchParameterMap(), dao, translator,
		    searchQueryInclude);
	}
}
