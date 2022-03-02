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

import java.util.Locale;

import org.hl7.fhir.r4.model.Provenance;
import org.openmrs.OpenmrsObject;
import org.openmrs.module.fhir2.api.util.FhirCache;

/**
 * Generic interface for a translator between OpenMRS data and FHIR provenance resources
 *
 * @param <T> OpenMRS data type
 */

public interface ProvenanceTranslator<T> {
	
	/**
	 * Maps an OpenMRS Object to a {@link org.hl7.fhir.r4.model.Provenance} resource
	 *
	 * @param openMrsObject the OpenMRS object to translate
	 * @return the corresponding {@link org.hl7.fhir.r4.model.Provenance} resource
	 */
	Provenance getCreateProvenance(T openMrsObject);
	
	default Provenance getCreateProvenance(T openMrsObject, FhirCache cache) {
		if (cache != null) {
			String cacheKey = getCacheKey(openMrsObject, "create-provenance-");
			if (cacheKey != null) {
				try {
					@SuppressWarnings("unchecked")
					Provenance cached = (Provenance) cache.get(((OpenmrsObject) openMrsObject).getUuid(),
					    (k) -> getCreateProvenance(openMrsObject));
					return cached;
				}
				catch (ClassCastException e) {
					// we shouldn't really get here, but...
					return getCreateProvenance(openMrsObject);
				}
			}
		}
		
		return getCreateProvenance(openMrsObject);
	}
	
	/**
	 * Maps an OpenMRS Object to a {@link org.hl7.fhir.r4.model.Provenance} resource
	 *
	 * @param openMrsObject the OpenMRS object to translate
	 * @return the corresponding {@link org.hl7.fhir.r4.model.Provenance} resource
	 */
	Provenance getUpdateProvenance(T openMrsObject);
	
	default Provenance getUpdateProvenance(T openMrsObject, FhirCache cache) {
		if (cache != null) {
			String cacheKey = getCacheKey(openMrsObject, "update-provenance-");
			if (cacheKey != null) {
				try {
					@SuppressWarnings("unchecked")
					Provenance cached = (Provenance) cache.get(((OpenmrsObject) openMrsObject).getUuid(),
					    (k) -> getUpdateProvenance(openMrsObject));
					return cached;
				}
				catch (ClassCastException e) {
					// we shouldn't really get here, but...
					return getUpdateProvenance(openMrsObject);
				}
			}
		}
		
		return getUpdateProvenance(openMrsObject);
	}
	
	default String getCacheKey(@Nonnull T data, String prefix) {
		if (data instanceof OpenmrsObject) {
			return prefix + data.getClass().getSimpleName().toLowerCase(Locale.ROOT) + "-"
			        + ((OpenmrsObject) data).getUuid();
		}
		
		return null;
	}
}
