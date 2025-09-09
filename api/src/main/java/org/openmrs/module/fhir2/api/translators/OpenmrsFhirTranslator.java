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

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Generic interface for a translator between OpenMRS data and FHIR resources
 * 
 * @param <T> OpenMRS data type
 * @param <U> FHIR resource type
 */
public interface OpenmrsFhirTranslator<T, U> extends ToFhirTranslator<T, U>, ToOpenmrsTranslator<T, U> {
	
	/**
	 * Maps OpenMRS data elements to FHIR resources.
	 *
	 * @param data the collection of OpenMRS data elements to translate
	 * @return the mapping of OpenMRS data element to corresponding FHIR resource
	 */
	default List<U> toFhirResources(Collection<T> data) {
		return data.stream().distinct().map(this::toFhirResource).collect(Collectors.toList());
	}
}
