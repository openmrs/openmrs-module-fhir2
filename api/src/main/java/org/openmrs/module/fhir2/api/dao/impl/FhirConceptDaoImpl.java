/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.fhir2.api.dao.impl;

import javax.validation.constraints.NotNull;

import java.util.Optional;

import lombok.AccessLevel;
import lombok.Setter;
import org.openmrs.Concept;
import org.openmrs.api.ConceptService;
import org.openmrs.module.fhir2.api.dao.FhirConceptDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Setter(AccessLevel.PACKAGE)
public class FhirConceptDaoImpl extends BaseFhirDao<Concept> implements FhirConceptDao {
	
	@Autowired
	private ConceptService conceptService;
	
	@Override
	public Concept get(@NotNull String uuid) {
		return conceptService.getConceptByUuid(uuid);
	}
	
	@Override
	public Optional<Concept> getConceptBySourceNameAndCode(String sourceName, String code) {
		return Optional.ofNullable(conceptService.getConceptByMapping(code, sourceName, false));
	}
}
