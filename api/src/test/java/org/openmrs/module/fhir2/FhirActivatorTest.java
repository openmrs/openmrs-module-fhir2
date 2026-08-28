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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import org.junit.Test;
import org.openmrs.module.fhir2.api.spi.ModuleLifecycleListener;

public class FhirActivatorTest {
	
	@Test
	public void willRefreshContext_shouldNotifyEveryListenerWhenOneThrows() {
		FhirActivator activator = new FhirActivator();
		boolean[] reached = new boolean[1];
		
		activator.addModuleLifecycleListener(new ModuleLifecycleListener() {
			
			@Override
			public void willRefresh() {
				throw new IllegalStateException("a contributed bean could not be supplied");
			}
		});
		activator.addModuleLifecycleListener(new ModuleLifecycleListener() {
			
			@Override
			public void willRefresh() {
				reached[0] = true;
			}
		});
		
		activator.willRefreshContext();
		
		// both FHIR servlets register as listeners, so one failing must not leave the other bound to a
		// context that has been closed
		assertThat(reached[0], is(true));
	}
}
