/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.fhir2.api.translators;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import java.util.Locale;

import org.openmrs.OpenmrsObject;
import org.openmrs.module.fhir2.api.util.FhirCache;

/**
 * Generic interface for a translator between OpenMRS data and FHIR resources
 * 
 * @param <T> OpenMRS data type
 * @param <U> FHIR resource type
 */
public interface ToFhirTranslator<T, U> extends FhirTranslator {
	
	/**
	 * Maps an OpenMRS data element to a FHIR resource
	 *
	 * @param data the OpenMRS data element to translate
	 * @return the corresponding FHIR resource
	 */
	U toFhirResource(@Nonnull T data);
	
	/**
	 * Maps an OpenMRS data element to a FHIR resource
	 * 
	 * @param data the OpenMRS data element to translate
	 * @param cache contextual cache
	 * @return the corresponding FHIR resource
	 */
	default U toFhirResource(@Nonnull T data, @Nullable FhirCache cache) {
		if (cache != null) {
			String cacheKey = getCacheKey(data);
			if (cacheKey != null) {
				try {
					@SuppressWarnings("unchecked")
					U cached = (U) cache.get(cacheKey, k -> toFhirResource(data));
					return cached;
				}
				catch (ClassCastException e) {
					// we shouldn't really get here, but...
					return toFhirResource(data);
				}
			}
		}
		
		return toFhirResource(data);
	}
	
	default void invalidate(@Nonnull T data, FhirCache fhirCache) {
		fhirCache.invalidate(getCacheKey(data));
	}
	
	default String getCacheKey(@Nonnull T data) {
		if (data == null) {
			return null;
		}
		
		if (data instanceof OpenmrsObject) {
			return this.getClass().getSimpleName().toLowerCase(Locale.ROOT) + "-"
			        + data.getClass().getSimpleName().toLowerCase(Locale.ROOT) + "-" + ((OpenmrsObject) data).getUuid();
		}
		
		return null;
	}
}
