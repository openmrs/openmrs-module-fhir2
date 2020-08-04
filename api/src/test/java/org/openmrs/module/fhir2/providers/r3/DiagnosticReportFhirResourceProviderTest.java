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

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;

import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.api.server.IBundleProvider;
import ca.uhn.fhir.rest.param.ReferenceAndListParam;
import ca.uhn.fhir.rest.param.ReferenceOrListParam;
import ca.uhn.fhir.rest.param.ReferenceParam;
import ca.uhn.fhir.rest.server.exceptions.InvalidRequestException;
import ca.uhn.fhir.rest.server.exceptions.MethodNotAllowedException;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import lombok.AccessLevel;
import lombok.Getter;
import org.hl7.fhir.convertors.conv30_40.DiagnosticReport30_40;
import org.hl7.fhir.dstu3.model.DiagnosticReport;
import org.hl7.fhir.dstu3.model.IdType;
import org.hl7.fhir.dstu3.model.OperationOutcome;
import org.hl7.fhir.dstu3.model.Patient;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.api.FhirDiagnosticReportService;
import org.openmrs.module.fhir2.providers.r4.MockIBundleProvider;

@RunWith(MockitoJUnitRunner.class)
public class DiagnosticReportFhirResourceProviderTest extends BaseFhirR3ProvenanceResourceTest<org.hl7.fhir.r4.model.DiagnosticReport> {
	
	private static final String UUID = "bdd7e368-3d1a-42a9-9538-395391b64adf";
	
	private static final String WRONG_UUID = "df34a1c1-f57b-4c33-bee5-e601b56b9d5b";
	
	private static final int START_INDEX = 0;
	
	private static final int END_INDEX = 10;
	
	private static final int PREFERRED_PAGE_SIZE = 10;
	
	private static final int COUNT = 1;
	
	@Mock
	private FhirDiagnosticReportService service;
	
	@Getter(AccessLevel.PUBLIC)
	private DiagnosticReportFhirResourceProvider resourceProvider;
	
	private org.hl7.fhir.r4.model.DiagnosticReport diagnosticReport;
	
	@Before
	public void setup() {
		resourceProvider = new DiagnosticReportFhirResourceProvider();
		resourceProvider.setDiagnosticReportService(service);
	}
	
	@Before
	public void initDiagnosticReport() {
		diagnosticReport = new org.hl7.fhir.r4.model.DiagnosticReport();
		diagnosticReport.setId(UUID);
	}
	
	private List<IBaseResource> get(IBundleProvider results) {
		return results.getResources(START_INDEX, END_INDEX);
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
		
		when(service.get(UUID)).thenReturn(diagnosticReport);
		
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
		when(service.create(any(org.hl7.fhir.r4.model.DiagnosticReport.class))).thenReturn(diagnosticReport);
		
		MethodOutcome result = resourceProvider
		        .createDiagnosticReport(DiagnosticReport30_40.convertDiagnosticReport(diagnosticReport));
		
		assertThat(result, notNullValue());
		assertThat(result.getResource(), notNullValue());
		assertThat(result.getResource().getIdElement().getIdPart(), equalTo(diagnosticReport.getId()));
	}
	
	@Test
	public void updateDiagnosticReport_shouldUpdateExistingDiagnosticReport() {
		when(service.update(eq(UUID), any(org.hl7.fhir.r4.model.DiagnosticReport.class))).thenReturn(diagnosticReport);
		
		MethodOutcome result = resourceProvider.updateDiagnosticReport(new IdType().setValue(UUID),
		    DiagnosticReport30_40.convertDiagnosticReport(diagnosticReport));
		
		assertThat(result, notNullValue());
		assertThat(result.getResource(), notNullValue());
		assertThat(result.getResource().getIdElement().getIdPart(), equalTo(UUID));
	}
	
	@Test(expected = InvalidRequestException.class)
	public void updateDiagnosticReport_shouldThrowInvalidRequestForUuidMismatch() {
		when(service.update(eq(WRONG_UUID), any(org.hl7.fhir.r4.model.DiagnosticReport.class)))
		        .thenThrow(InvalidRequestException.class);
		
		resourceProvider.updateDiagnosticReport(new IdType().setValue(WRONG_UUID),
		    DiagnosticReport30_40.convertDiagnosticReport(diagnosticReport));
	}
	
	@Test(expected = InvalidRequestException.class)
	public void updateDiagnosticReport_shouldThrowInvalidRequestForMissingId() {
		DiagnosticReport noIdDiagnosticReport = new DiagnosticReport();
		
		when(service.update(eq(UUID), any(org.hl7.fhir.r4.model.DiagnosticReport.class)))
		        .thenThrow(InvalidRequestException.class);
		
		resourceProvider.updateDiagnosticReport(new IdType().setValue(UUID), noIdDiagnosticReport);
	}
	
	@Test(expected = MethodNotAllowedException.class)
	public void updateDiagnosticReport_shouldThrowMethodNotAllowedIfDoesNotExist() {
		DiagnosticReport wrongDiagnosticReport = new DiagnosticReport();
		
		wrongDiagnosticReport.setId(WRONG_UUID);
		
		when(service.update(eq(WRONG_UUID), any(org.hl7.fhir.r4.model.DiagnosticReport.class)))
		        .thenThrow(MethodNotAllowedException.class);
		
		resourceProvider.updateDiagnosticReport(new IdType().setValue(WRONG_UUID), wrongDiagnosticReport);
	}
	
	@Test
	public void findDiagnosticReports_shouldReturnMatchingBundleOfDiagnosticReports() {
		when(service.searchForDiagnosticReports(any(), any(), any(), any(), any(), any(), any(), any())).thenReturn(
		    new MockIBundleProvider<>(Collections.singletonList(diagnosticReport), PREFERRED_PAGE_SIZE, COUNT));
		
		IBundleProvider results = resourceProvider.searchForDiagnosticReports(null, null, null, null, null, null, null, null,
		    null);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList.size(), greaterThanOrEqualTo(1));
		assertThat(resultList.get(0).fhirType(), equalTo(FhirConstants.DIAGNOSTIC_REPORT));
		assertThat(((org.hl7.fhir.r4.model.DiagnosticReport) resultList.iterator().next()).getId(), equalTo(UUID));
	}
	
	@Test
	public void findDiagnosticReports_shouldReturnMatchingBundleOfDiagnosticReportsWhenSubjectIsSpecified() {
		
		ReferenceAndListParam subject = new ReferenceAndListParam();
		subject.addValue(new ReferenceOrListParam().add(new ReferenceParam().setChain(Patient.SP_NAME)));
		
		when(service.searchForDiagnosticReports(any(), any(), any(), any(), any(), any(), any(), any())).thenReturn(
		    new MockIBundleProvider<>(Collections.singletonList(diagnosticReport), PREFERRED_PAGE_SIZE, COUNT));
		
		IBundleProvider results = resourceProvider.searchForDiagnosticReports(null, null, subject, null, null, null, null,
		    null, null);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList.size(), greaterThanOrEqualTo(1));
		assertThat(resultList.get(0).fhirType(), equalTo(FhirConstants.DIAGNOSTIC_REPORT));
		assertThat(((org.hl7.fhir.r4.model.DiagnosticReport) resultList.iterator().next()).getId(), equalTo(UUID));
	}
	
	@Test
	public void deleteDiagnosticReport_shouldDeleteRequestedDiagnosticReport() {
		
		when(service.delete(UUID)).thenReturn(diagnosticReport);
		
		OperationOutcome result = resourceProvider.deleteDiagnosticReport(new IdType().setValue(UUID));
		assertThat(result, notNullValue());
		assertThat(result.getIssue(), notNullValue());
		assertThat(result.getIssueFirstRep().getSeverity(), equalTo(OperationOutcome.IssueSeverity.INFORMATION));
		assertThat(result.getIssueFirstRep().getDetails().getCodingFirstRep().getCode(), equalTo("MSG_DELETED"));
	}
	
	@Test(expected = ResourceNotFoundException.class)
	public void deleteDiagnosticReport_shouldThrowResourceNotFoundExceptionWhenIdRefersToNonExistentDiagnosticReport() {
		when(service.delete(WRONG_UUID)).thenReturn(null);
		resourceProvider.deleteDiagnosticReport(new IdType().setValue(WRONG_UUID));
	}
}
