/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.fhir2;

import static org.mockito.Mockito.when;

import org.mockito.Mockito;
import org.openmrs.module.fhir2.api.FhirGlobalPropertyService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class MockedGlobalPropertyServiceConfiguration {
	
	@Bean
	@Primary
	public FhirGlobalPropertyService getFhirGlobalPropertyService() {
		FhirGlobalPropertyService globalPropertyService = Mockito.mock(FhirGlobalPropertyService.class);
		when(globalPropertyService.getGlobalProperty("default_locale", "en_GB")).thenReturn("en_GB");
		return globalPropertyService;
	}
}
