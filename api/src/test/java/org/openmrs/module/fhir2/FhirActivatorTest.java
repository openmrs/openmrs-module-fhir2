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
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;

import java.util.ArrayList;
import java.util.List;

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
		
		assertThat(reached[0], is(true));
	}
	
	/**
	 * A servlet drops itself from destroy(), which can land while a notification is part way through
	 * the list. Self-removal is the deterministic stand-in for that race.
	 */
	@Test
	public void willRefreshContext_shouldNotifyEveryListenerWhenOneRemovesItself() {
		FhirActivator activator = new FhirActivator();
		boolean[] reached = new boolean[1];
		
		ModuleLifecycleListener selfRemoving = new ModuleLifecycleListener() {
			
			@Override
			public void willRefresh() {
				activator.removeModuleLifecycleLister(this);
			}
		};
		
		activator.addModuleLifecycleListener(selfRemoving);
		activator.addModuleLifecycleListener(new ModuleLifecycleListener() {
			
			@Override
			public void willRefresh() {
				reached[0] = true;
			}
		});
		
		activator.willRefreshContext();
		
		assertThat(reached[0], is(true));
	}
	
	/**
	 * Each lifecycle method passes its own method reference, so a listener wired to the wrong event
	 * would leave a servlet holding a stale context with nothing else to catch it.
	 */
	@Test
	public void stopped_shouldNotifyListenersOfStoppedAndNothingElse() {
		FhirActivator activator = new FhirActivator();
		List<String> events = new ArrayList<>();
		
		activator.addModuleLifecycleListener(new ModuleLifecycleListener() {
			
			@Override
			public void willStop() {
				events.add("willStop");
			}
			
			@Override
			public void stopped() {
				events.add("stopped");
			}
		});
		
		activator.stopped();
		
		assertThat(events, contains("stopped"));
	}
	
	@Test
	public void willRefreshContext_shouldNotifyEveryListenerWhenOneThrowsAnError() {
		FhirActivator activator = new FhirActivator();
		boolean[] reached = new boolean[1];
		
		activator.addModuleLifecycleListener(new ModuleLifecycleListener() {
			
			@Override
			public void willRefresh() {
				// a module classloader swap surfaces as a LinkageError, not an exception
				throw new NoClassDefFoundError("org/example/AContributedInterceptor");
			}
		});
		activator.addModuleLifecycleListener(new ModuleLifecycleListener() {
			
			@Override
			public void willRefresh() {
				reached[0] = true;
			}
		});
		
		activator.willRefreshContext();
		
		assertThat(reached[0], is(true));
	}
}
