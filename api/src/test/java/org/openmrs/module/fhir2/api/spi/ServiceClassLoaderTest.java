/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.fhir2.api.spi;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;

import java.util.Set;

import org.junit.Test;
import org.openmrs.module.fhir2.api.FhirService;
import org.openmrs.module.fhir2.api.impl.FhirObservationServiceImpl;

public class ServiceClassLoaderTest {
	
	@Test
	public void shouldLoadDefinedService() {
		ServiceClassLoader<FhirService> fhirServiceServiceClassLoader = new ServiceClassLoader<>(FhirService.class);
		
		Set<Class<? extends FhirService>> services = fhirServiceServiceClassLoader.load();
		
		assertThat(services, hasSize(greaterThanOrEqualTo(1)));
		assertThat(services, hasItem(equalTo(FhirObservationServiceImpl.class)));
	}
}
