/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.fhir2.provider.r4;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.openmrs.module.fhir2.api.util.GeneralUtils.inputStreamToString;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Objects;

import lombok.AccessLevel;
import lombok.Getter;
import org.hl7.fhir.r4.model.DiagnosticReport;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.module.fhir2.providers.r4.BaseFhirR4IntegrationTest;
import org.openmrs.module.fhir2.providers.r4.DiagnosticReportFhirResourceProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletResponse;

public class DiagnosticReportResourceProvider_2_2IntegrationTest extends BaseFhirR4IntegrationTest<DiagnosticReportFhirResourceProvider, DiagnosticReport> {
	
	private static final String REPORT_DATA_XML = "org/openmrs/module/fhir2/api/dao/impl/FhirDiagnosticReport_2_2_intial_data.xml";
	
	private static final String DIAGNOSTIC_REPORT_UUID = "1e589127-f391-4d0c-8e98-e0a158b2be22";
	
	private static final String JSON_PATCH_REPORT_PATH = "org/openmrs/module/fhir2/providers/DiagnosticReport_patch.json";
	
	private static final String JSON_PATCH_REPORT_TEXT = "[\n    { \"op\": \"replace\", \"path\": \"/status\", \"value\": \"registered\" }\n]";
	
	@Getter(AccessLevel.PUBLIC)
	@Autowired
	private DiagnosticReportFhirResourceProvider resourceProvider;
	
	@Before
	@Override
	public void setup() throws Exception {
		super.setup();
		executeDataSet(REPORT_DATA_XML);
	}
	
	@Test
	public void shouldReturnExistingDiagnosticReportAsJson() throws Exception {
		MockHttpServletResponse response = get("/DiagnosticReport/" + DIAGNOSTIC_REPORT_UUID).accept(FhirMediaTypes.JSON)
		        .go();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), is(FhirMediaTypes.JSON.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		DiagnosticReport diagnosticReport = readResponse(response);
		
		assertThat(diagnosticReport, notNullValue());
		assertThat(diagnosticReport.getIdElement().getIdPart(), equalTo(DIAGNOSTIC_REPORT_UUID));
		
		assertThat(diagnosticReport.hasCategory(), is(true));
		assertThat(diagnosticReport.getCategoryFirstRep().getCodingFirstRep().getCode(), equalTo("LAB"));
		
		assertThat(diagnosticReport.hasSubject(), is(true));
		assertThat(diagnosticReport.getSubject().getReference(), equalTo("Patient/5946f880-b197-400b-9caa-a3c661d23041"));
		
		assertThat(diagnosticReport.hasEncounter(), is(true));
		assertThat(diagnosticReport.getEncounter().getReference(),
		    equalTo("Encounter/6519d653-393b-4118-9c83-a3715b82d4ac"));
		
		assertThat(diagnosticReport.hasIssued(), is(true));
		assertThat(diagnosticReport.getIssued(),
		    equalTo(Date.from(LocalDateTime.of(2008, 7, 1, 0, 0, 0).atZone(ZoneId.systemDefault()).toInstant())));
		
		assertThat(diagnosticReport, validResource());
	}
	
	@Test
	public void shouldPatchExistingDiagnosticReportViaJsonMergePatch() throws Exception {
		String jsonDiagnosticReportPatch;
		try (InputStream is = this.getClass().getClassLoader().getResourceAsStream(JSON_PATCH_REPORT_PATH)) {
			Objects.requireNonNull(is);
			jsonDiagnosticReportPatch = inputStreamToString(is, UTF_8);
		}
		
		MockHttpServletResponse response = patch("/DiagnosticReport/" + DIAGNOSTIC_REPORT_UUID)
		        .jsonMergePatch(jsonDiagnosticReportPatch).accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), is(FhirMediaTypes.JSON.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		DiagnosticReport diagnosticReport = readResponse(response);
		
		assertThat(diagnosticReport, notNullValue());
		assertThat(diagnosticReport.getIdElement().getIdPart(), equalTo(DIAGNOSTIC_REPORT_UUID));
		assertThat(diagnosticReport, validResource());
		
		assertThat(diagnosticReport.getStatus(), equalTo(DiagnosticReport.DiagnosticReportStatus.REGISTERED));
	}
	
	
	@Test
	public void shouldPatchExistingDiagnosticReportViaJsonPatch() throws Exception {
		MockHttpServletResponse response = patch("/DiagnosticReport/" + DIAGNOSTIC_REPORT_UUID)
				.jsonPatch(JSON_PATCH_REPORT_TEXT).accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), is(FhirMediaTypes.JSON.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		DiagnosticReport diagnosticReport = readResponse(response);
		
		assertThat(diagnosticReport, notNullValue());
		assertThat(diagnosticReport.getIdElement().getIdPart(), equalTo(DIAGNOSTIC_REPORT_UUID));
		assertThat(diagnosticReport, validResource());
		
		assertThat(diagnosticReport.getStatus(), equalTo(DiagnosticReport.DiagnosticReportStatus.REGISTERED));
	}
	
}
