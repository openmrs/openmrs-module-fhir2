/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.fhir2.api.impl;

import java.util.Optional;

import lombok.AccessLevel;
import lombok.Setter;
import org.openmrs.Concept;
import org.openmrs.ConceptMap;
import org.openmrs.ConceptMapType;
import org.openmrs.ConceptSource;
import org.openmrs.module.fhir2.api.FhirConceptService;
import org.openmrs.module.fhir2.api.dao.FhirConceptDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional
@Setter(AccessLevel.PACKAGE)
public class FhirConceptServiceImpl implements FhirConceptService {
	
	@Autowired
	private FhirConceptDao dao;
	
	@Override
	@Transactional(readOnly = true)
	public Concept get(String uuid) {
		return dao.get(uuid);
	}
	
	@Override
	@Transactional(readOnly = true)
	public Optional<Concept> getConceptWithSameAsMappingInSource(ConceptSource conceptSource, String mappingCode) {
		return dao.getConceptWithSameAsMappingInSource(conceptSource, mappingCode);
	}
	
	@Override
	@Transactional(readOnly = true)
	public Optional<String> getSameAsMappingForConceptInSource(ConceptSource source, Concept concept) {
		if (source != null && concept != null) {
			for (ConceptMap mapping : concept.getConceptMappings()) {
				if (source.equals(mapping.getConceptReferenceTerm().getConceptSource())) {
					ConceptMapType mapType = mapping.getConceptMapType();
					if (mapType != null) {
						if (mapType.getUuid().equals(ConceptMapType.SAME_AS_MAP_TYPE_UUID)
						        || mapType.getName().equalsIgnoreCase("SAME-AS")) {
							return Optional.of(mapping.getConceptReferenceTerm().getCode());
						}
					}
				}
			}
		}
		
		return Optional.empty();
	}
	
	@Override
	@Transactional(readOnly = true)
	public Optional<Concept> getConceptWithAnyMappingInSource(ConceptSource conceptSource, String mappingCode) {
		return dao.getConceptWithAnyMappingInSource(conceptSource, mappingCode);
	}
}
