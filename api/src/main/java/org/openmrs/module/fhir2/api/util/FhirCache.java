/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.fhir2.api.util;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.stereotype.Component;

@Component
public class FhirCache {
	
	private final Cache<String, ? super Object> fhirResourceCache;
	
	public FhirCache() {
		// @formatter:off
		fhirResourceCache = Caffeine.newBuilder().initialCapacity(5_000).maximumSize(100_000)
				.expireAfterAccess(2, TimeUnit.MINUTES).build();
		// @formatter:on
	}
	
	@Nullable
	public Object get(@Nonnull String s, @Nonnull Function<? super String, ?> function) {
		if (s != null) {
			return fhirResourceCache.get(s, function);
		}
		
		return function.apply(null);
	}
	
	public void put(@Nonnull String s, @Nonnull Object o) {
		if (s != null) {
			fhirResourceCache.put(s, o);
		}
	}
	
	public void invalidate(@Nonnull String key) {
		if (key != null) {
			fhirResourceCache.invalidate(key);
		}
	}
	
	public void invalidateAll() {
		fhirResourceCache.invalidateAll();
	}
}
