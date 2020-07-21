/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.fhir2.api.mappings;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;

import com.google.common.collect.ImmutableMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

public abstract class BaseMapping {
	
	@Autowired
	private ResourceLoader resourceLoader;
	
	private final String resourceLocator;
	
	private volatile Map<String, String> fhirToOpenmrs = null;
	
	private volatile Map<String, String> openmrsToFhir = null;
	
	protected BaseMapping(String resourceLocator) {
		Objects.requireNonNull(resourceLocator);
		this.resourceLocator = resourceLocator;
	}
	
	protected Optional<String> getOpenmrs(String fhirCode) {
		setupDelegates();
		return Optional.ofNullable(fhirToOpenmrs.getOrDefault(fhirCode, null));
	}
	
	protected Optional<String> getFhir(String openmrsCode) {
		setupDelegates();
		return Optional.ofNullable(openmrsToFhir.getOrDefault(openmrsCode, null));
	}
	
	private void setupDelegates() {
		if (fhirToOpenmrs == null) {
			synchronized (this) {
				if (fhirToOpenmrs == null) {
					Resource resource = resourceLoader.getResource("classpath:" + resourceLocator);
					Properties properties = new Properties();
					
					try (InputStream in = resource.getInputStream()) {
						properties.load(in);
					}
					catch (IOException ignored) {}
					
					ImmutableMap.Builder<String, String> fhirToOpenmrsBuilder = ImmutableMap
					        .builderWithExpectedSize(properties.size()),
					        openmrsToFhirBuilder = ImmutableMap.builderWithExpectedSize(properties.size());
					for (Map.Entry<Object, Object> entry : properties.entrySet()) {
						String entryValue = entry.getValue().toString().trim();
						openmrsToFhirBuilder.put(entryValue, entry.getKey().toString());
						fhirToOpenmrsBuilder.put(entry.getKey().toString(), entryValue);
					}
					
					fhirToOpenmrs = fhirToOpenmrsBuilder.build();
					openmrsToFhir = openmrsToFhirBuilder.build();
				}
			}
		}
	}
}
