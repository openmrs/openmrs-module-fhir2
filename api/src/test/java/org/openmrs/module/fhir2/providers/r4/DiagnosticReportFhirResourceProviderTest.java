/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.fhir2.providers.r4;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.mockito.ArgumentMatchers.any;
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
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.DiagnosticReport;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.Patient;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.api.FhirDiagnosticReportService;

@RunWith(MockitoJUnitRunner.class)
public class DiagnosticReportFhirResourceProviderTest {
	
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
		when(service.create(diagnosticReport)).thenReturn(diagnosticReport);
		
		MethodOutcome result = resourceProvider.createDiagnosticReport(diagnosticReport);
		
		assertThat(result, notNullValue());
		assertThat(result.getResource(), equalTo(diagnosticReport));
	}
	
	@Test
	public void updateDiagnosticReport_shouldUpdateExistingDiagnosticReport() {
		when(service.update(UUID, diagnosticReport)).thenReturn(diagnosticReport);
		
		MethodOutcome result = resourceProvider.updateDiagnosticReport(new IdType().setValue(UUID), diagnosticReport);
		
		assertThat(result, notNullValue());
		assertThat(result.getResource(), equalTo(diagnosticReport));
	}
	
	@Test(expected = InvalidRequestException.class)
	public void updateDiagnosticReport_shouldThrowInvalidRequestForUuidMismatch() {
		when(service.update(WRONG_UUID, diagnosticReport)).thenThrow(InvalidRequestException.class);
		
		resourceProvider.updateDiagnosticReport(new IdType().setValue(WRONG_UUID), diagnosticReport);
	}
	
	@Test(expected = InvalidRequestException.class)
	public void updateDiagnosticReport_shouldThrowInvalidRequestForMissingId() {
		DiagnosticReport noIdDiagnostiReport = new DiagnosticReport();
		
		when(service.update(UUID, noIdDiagnostiReport)).thenThrow(InvalidRequestException.class);
		
		resourceProvider.updateDiagnosticReport(new IdType().setValue(UUID), noIdDiagnostiReport);
	}
	
	@Test(expected = MethodNotAllowedException.class)
	public void updateDiagnosticReport_shouldThrowMethodNotAllowedIfDoesNotExist() {
		DiagnosticReport wrongDiagnosticReport = new DiagnosticReport();
		
		wrongDiagnosticReport.setId(WRONG_UUID);
		
		when(service.update(WRONG_UUID, wrongDiagnosticReport)).thenThrow(MethodNotAllowedException.class);
		
		resourceProvider.updateDiagnosticReport(new IdType().setValue(WRONG_UUID), wrongDiagnosticReport);
	}
	
	@Test
	public void findDiagnosticReports_shouldReturnMatchingBundleOfDiagnosticReports() {
		when(service.searchForDiagnosticReports(any(), any(), any(), any(), any())).thenReturn(
		    new MockIBundleProvider<>(Collections.singletonList(diagnosticReport), PREFERRED_PAGE_SIZE, COUNT));
		
		IBundleProvider results = resourceProvider.searchForDiagnosticReports(null, null, null, null, null, null);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList.size(), greaterThanOrEqualTo(1));
		assertThat(resultList.get(0).fhirType(), equalTo(FhirConstants.DIAGNOSTIC_REPORT));
		assertThat(((DiagnosticReport) resultList.iterator().next()).getId(), equalTo(UUID));
	}
	
	@Test
	public void findDiagnosticReports_shouldReturnMatchingBundleOfDiagnosticReportsWhenSubjectIsSpecified() {
		
		ReferenceAndListParam subject = new ReferenceAndListParam();
		subject.addValue(new ReferenceOrListParam().add(new ReferenceParam().setChain(Patient.SP_NAME)));
		
		when(service.searchForDiagnosticReports(any(), any(), any(), any(), any())).thenReturn(
		    new MockIBundleProvider<>(Collections.singletonList(diagnosticReport), PREFERRED_PAGE_SIZE, COUNT));
		
		IBundleProvider results = resourceProvider.searchForDiagnosticReports(null, null, subject, null, null, null);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList.size(), greaterThanOrEqualTo(1));
		assertThat(resultList.get(0).fhirType(), equalTo(FhirConstants.DIAGNOSTIC_REPORT));
		assertThat(((DiagnosticReport) resultList.iterator().next()).getId(), equalTo(UUID));
	}
}
