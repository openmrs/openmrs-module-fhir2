/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.fhir2.web.util;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.when;

import javax.servlet.http.HttpServletRequest;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openmrs.module.fhir2.web.util.FhirVersionUtils.FhirVersion;

@RunWith(MockitoJUnitRunner.class)
public class FhirVersionUtilsTest {
	
	@Mock
	private HttpServletRequest httpServletRequest;
	
	@Test
	public void shouldReturnR4ForWs() {
		when(httpServletRequest.getRequestURI()).thenReturn("/openmrs/ws/fhir2/R4/Patient");
		when(httpServletRequest.getContextPath()).thenReturn("/openmrs");
		FhirVersion version = FhirVersionUtils.getFhirVersion(httpServletRequest);
		assertThat(version, equalTo(FhirVersion.R4));
	}
	
	@Test
	public void shouldReturnR3ForWs() {
		when(httpServletRequest.getRequestURI()).thenReturn("/openmrs/ws/fhir2/R3/Patient");
		when(httpServletRequest.getContextPath()).thenReturn("/openmrs");
		FhirVersion version = FhirVersionUtils.getFhirVersion(httpServletRequest);
		assertThat(version, equalTo(FhirVersion.R3));
	}

	@Test
	public void shouldReturnUnknowForWs() {
		when(httpServletRequest.getRequestURI()).thenReturn("/openmrs/ws/fhir2/Patient");
		when(httpServletRequest.getContextPath()).thenReturn("/openmrs");
		FhirVersion version = FhirVersionUtils.getFhirVersion(httpServletRequest);
		assertThat(version, equalTo(FhirVersion.UNKNOWN));
	}

	@Test
	public void shouldReturnR4NoSlash() {
		when(httpServletRequest.getRequestURI()).thenReturn("/openmrs/ws/fhir2/R4");
		when(httpServletRequest.getContextPath()).thenReturn("/openmrs");
		FhirVersion version = FhirVersionUtils.getFhirVersion(httpServletRequest);
		assertThat(version, equalTo(FhirVersion.R4));
	}

	@Test
	public void shouldReturnR4QuestionMark() {
		when(httpServletRequest.getRequestURI()).thenReturn("/openmrs/ws/fhir2/R4?param=12");
		when(httpServletRequest.getContextPath()).thenReturn("/openmrs");
		FhirVersion version = FhirVersionUtils.getFhirVersion(httpServletRequest);
		assertThat(version, equalTo(FhirVersion.R4));
	}
	
	@Test
	public void shouldReturnR4ForMs() {
		when(httpServletRequest.getRequestURI()).thenReturn("/openmrs/ms/fhir2Servlet/Patient");
		when(httpServletRequest.getContextPath()).thenReturn("/openmrs");
		FhirVersion version = FhirVersionUtils.getFhirVersion(httpServletRequest);
		assertThat(version, equalTo(FhirVersion.R4));
	}
	
	@Test
	public void shouldReturnR3ForMs() {
		when(httpServletRequest.getRequestURI()).thenReturn("/openmrs/ms/fhir2R3Servlet/Patient");
		when(httpServletRequest.getContextPath()).thenReturn("/openmrs");
		FhirVersion version = FhirVersionUtils.getFhirVersion(httpServletRequest);
		assertThat(version, equalTo(FhirVersion.R3));
	}
}
