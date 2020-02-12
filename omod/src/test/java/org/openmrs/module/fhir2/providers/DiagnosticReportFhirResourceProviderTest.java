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

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;

import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import lombok.AccessLevel;
import lombok.Getter;
import org.hl7.fhir.r4.model.DiagnosticReport;
import org.hl7.fhir.r4.model.IdType;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openmrs.module.fhir2.api.FhirDiagnosticReportService;

@RunWith(MockitoJUnitRunner.class)
public class DiagnosticReportFhirResourceProviderTest {
	
	private static final String UUID = "bdd7e368-3d1a-42a9-9538-395391b64adf";
	
	private static final String WRONG_UUID = "df34a1c1-f57b-4c33-bee5-e601b56b9d5b";
	
	@Mock
	private FhirDiagnosticReportService service;
	
	@Getter(AccessLevel.PUBLIC)
	private DiagnosticReportFhirResourceProvider resourceProvider;
	
	private DiagnosticReport diagnosticReport;
	
	@Before
	public void setup() {
		resourceProvider = new DiagnosticReportFhirResourceProvider();
		resourceProvider.setService(service);
	}
	
	@Before
	public void initDiagnosticReport() {
		diagnosticReport = new DiagnosticReport();
		diagnosticReport.setId(UUID);
	}
	
	@Test
	public void getResourceType_shouldReturnResourceType() {
		assertThat(resourceProvider.getResourceType(), equalTo(DiagnosticReport.class));
		assertThat(resourceProvider.getResourceType().getName(), equalTo(DiagnosticReport.class.getName()));
	}
	
	@Test
	public void getDiagnosticReportById_shouldReturnMatchingDiagnosticReport() {
		IdType id = new IdType();
		id.setValue(UUID);
		
		when(service.getDiagnosticReportByUuid(UUID)).thenReturn(diagnosticReport);
		
		DiagnosticReport result = resourceProvider.getDiagnosticReportById(id);
		
		assertThat(result, notNullValue());
		assertThat(result.isResource(), is(true));
		assertThat(result.getId(), notNullValue());
		assertThat(result.getId(), equalTo(UUID));
	}
	
	@Test(expected = ResourceNotFoundException.class)
	public void getDiagnosticReportByWithWrongId_shouldThrowResourceNotFoundException() {
		IdType idType = new IdType();
		idType.setValue(WRONG_UUID);
		
		assertThat(resourceProvider.getDiagnosticReportById(idType).isResource(), is(true));
		assertThat(resourceProvider.getDiagnosticReportById(idType), nullValue());
	}
	
	@Test
	public void createDiagnosticReport_shouldCreateNewDiagnosticReport() {
		when(service.saveDiagnosticReport(diagnosticReport)).thenReturn(diagnosticReport);
		
		MethodOutcome result = resourceProvider.createDiagnosticReport(diagnosticReport);
		
		assertThat(result, notNullValue());
		assertThat(result.getResource(), equalTo(diagnosticReport));
	}
	
	@Test
	public void updateDiagnosticReport_shouldUpdateExistingDiagnosticReport() {
		when(service.updateDiagnosticReport(UUID, diagnosticReport)).thenReturn(diagnosticReport);
		
		MethodOutcome result = resourceProvider.updateDiagnosticReport(new IdType().setValue(UUID), diagnosticReport);
		
		assertThat(result, notNullValue());
		assertThat(result.getResource(), equalTo(diagnosticReport));
	}
}
