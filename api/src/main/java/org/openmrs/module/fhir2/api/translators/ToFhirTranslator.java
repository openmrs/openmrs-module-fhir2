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

import static java.util.stream.Collectors.toMap;

import javax.annotation.Nonnull;

import java.util.Collection;
import java.util.Map;
import java.util.function.Function;

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
	 * Maps OpenMRS data elements to FHIR resources.
	 *
	 * @param data the collection of OpenMRS data elements to translate
	 * @return the mapping of OpenMRS data element to corresponding FHIR resource
	 */
	default Map<T, U> toFhirResources(Collection<T> data) {
		return data.stream().distinct().collect(toMap(Function.identity(), this::toFhirResource));
	}
}
