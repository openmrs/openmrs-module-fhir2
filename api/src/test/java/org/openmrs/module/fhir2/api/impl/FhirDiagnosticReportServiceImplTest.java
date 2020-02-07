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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

import org.hl7.fhir.r4.model.DiagnosticReport;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openmrs.Obs;
import org.openmrs.module.fhir2.api.dao.FhirDiagnosticReportDao;
import org.openmrs.module.fhir2.api.translators.DiagnosticReportTranslator;

@RunWith(MockitoJUnitRunner.class)
public class FhirDiagnosticReportServiceImplTest {
	
	private static final String UUID = "249b9094-b812-4b0c-a204-0052a05c657f";
	
	private static final DiagnosticReport.DiagnosticReportStatus INITIAL_STATUS = DiagnosticReport.DiagnosticReportStatus.PRELIMINARY;
	
	private static final DiagnosticReport.DiagnosticReportStatus FINAL_STATUS = DiagnosticReport.DiagnosticReportStatus.FINAL;
	
	@Mock
	private FhirDiagnosticReportDao dao;
	
	@Mock
	private DiagnosticReportTranslator translator;
	
	private FhirDiagnosticReportServiceImpl service;
	
	@Before
	public void setUp() throws Exception {
		service = new FhirDiagnosticReportServiceImpl();
		service.setTranslator(translator);
		service.setDao(dao);
	}
	
	@Test
	public void shouldRetrieveDiagnosticReportByUuid() {
		Obs obsGroup = new Obs();
		DiagnosticReport diagnosticReport = new DiagnosticReport();
		
		obsGroup.setUuid(UUID);
		diagnosticReport.setId(UUID);
		
		when(dao.getObsGroupByUuid(UUID)).thenReturn(obsGroup);
		when(translator.toFhirResource(obsGroup)).thenReturn(diagnosticReport);
		
		DiagnosticReport result = service.getDiagnosticReportByUuid(UUID);
		assertNotNull(result);
		assertThat(result, equalTo(diagnosticReport));
		
	}
}
