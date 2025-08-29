/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.fhir2.api;

import java.util.List;
import java.util.Optional;

import org.openmrs.Concept;
import org.openmrs.ConceptSource;

public interface FhirConceptService {
	
	Concept get(Integer id);
	
	Concept get(String uuid);
	
	Optional<Concept> getConceptWithSameAsMappingInSource(ConceptSource conceptSource, String mappingCode);
	
	Optional<String> getSameAsMappingForConceptInSource(ConceptSource source, Concept concept);
	
	List<Concept> getConceptsWithAnyMappingInSource(ConceptSource conceptSource, String mappingCode);
}
