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

import org.openmrs.module.fhir2.api.FhirGlobalPropertyService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
import org.springframework.context.annotation.Primary;

@Configuration
@ImportResource({ "classpath:applicationContext-service.xml", "classpath*:moduleApplicationContext.xml",
		"classpath*:webModuleApplicationContext.xml" })
public class WebTestFhirSpringConfiguration {

	@Bean
	@Primary
	FhirGlobalPropertyService getFhirGlobalPropertyService() {
		return property -> {
			switch (property) {
				case FhirConstants.OPENMRS_FHIR_DEFAULT_PAGE_SIZE:
					return "10";
				case FhirConstants.OPENMRS_FHIR_MAXIMUM_PAGE_SIZE:
					return "100";
				default:
					return null;
			}
		};
	}
}
