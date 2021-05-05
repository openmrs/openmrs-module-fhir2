/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.fhir2.api.translators.impl;

import static org.apache.commons.lang3.Validate.notNull;

import javax.annotation.Nonnull;

import lombok.AccessLevel;
import lombok.Setter;
import org.hl7.fhir.r4.model.Observation;
import org.openmrs.Obs;
import org.openmrs.module.fhir2.api.translators.ConceptTranslator;
import org.openmrs.module.fhir2.api.translators.ObservationComponentTranslator;
import org.openmrs.module.fhir2.api.translators.ObservationValueTranslator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Setter(AccessLevel.PACKAGE)
public class ObservationComponentTranslatorImpl implements ObservationComponentTranslator {
	
	@Autowired
	private ObservationValueTranslator observationValueTranslator;
	
	@Autowired
	private ConceptTranslator conceptTranslator;
	
	@Override
	public Observation.ObservationComponentComponent toFhirResource(@Nonnull Obs obs) {
		if (obs == null) {
			return null;
		}
		
		Observation.ObservationComponentComponent component = new Observation.ObservationComponentComponent();
		component.setId(obs.getUuid());
		component.setCode(conceptTranslator.toFhirResource(obs.getConcept()));
		component.setValue(observationValueTranslator.toFhirResource(obs));
		
		return component;
	}
	
	@Override
	public Obs toOpenmrsType(@Nonnull Obs obs, @Nonnull Observation.ObservationComponentComponent observationComponent) {
		notNull(obs, "The existing Obs object should not be null");
		notNull(observationComponent, "The ObservationComponentComponent object should not be null");
		
		obs.setUuid(observationComponent.getId());
		obs.setConcept(conceptTranslator.toOpenmrsType(observationComponent.getCode()));
		observationValueTranslator.toOpenmrsType(obs, observationComponent.getValue());
		
		return obs;
	}
}
