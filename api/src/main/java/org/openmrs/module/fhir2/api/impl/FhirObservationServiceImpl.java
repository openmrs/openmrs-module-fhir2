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

import javax.inject.Inject;

import java.util.Collection;
import java.util.stream.Collectors;

import lombok.AccessLevel;
import lombok.Setter;
import org.hl7.fhir.r4.model.Observation;
import org.openmrs.module.fhir2.api.FhirObservationService;
import org.openmrs.module.fhir2.api.dao.FhirObservationDao;
import org.openmrs.module.fhir2.api.translators.ObservationTranslator;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional
@Setter(AccessLevel.PACKAGE)
public class FhirObservationServiceImpl implements FhirObservationService {
	
	@Inject
	FhirObservationDao dao;
	
	@Inject
	ObservationTranslator observationTranslator;

	@Override
	@Transactional(readOnly = true)
	public Collection<Observation> getAllObservations() {
		return dao.getAllObs().stream()
				.map(observationTranslator::toFhirResource)
				.collect(Collectors.toList());
	}

	@Override
	@Transactional(readOnly = true)
	public Observation getObservationByUuid(String uuid) {
		return observationTranslator.toFhirResource(dao.getObsByUuid(uuid));
	}
}
