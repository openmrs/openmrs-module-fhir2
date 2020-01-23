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

import javax.inject.Inject;

import lombok.AccessLevel;
import lombok.Setter;
import org.hl7.fhir.r4.model.Observation;
import org.openmrs.Obs;
import org.openmrs.module.fhir2.api.translators.ConceptTranslator;
import org.openmrs.module.fhir2.api.translators.ObservationComponentTranslator;
import org.openmrs.module.fhir2.api.translators.ObservationValueTranslator;
import org.springframework.stereotype.Component;

@Component
@Setter(AccessLevel.PACKAGE)
public class ObservationComponentTranslatorImpl implements ObservationComponentTranslator {
	
	@Inject
	private ObservationValueTranslator observationValueTranslator;
	
	@Inject
	private ConceptTranslator conceptTranslator;
	
	@Override
	public Observation.ObservationComponentComponent toFhirResource(Obs obs) {
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
	public Obs toOpenmrsType(Obs obs, Observation.ObservationComponentComponent observationComponent) {
		if (obs == null) {
			return null;
		}
		
		if (observationComponent == null) {
			return obs;
		}
		
		obs.setUuid(observationComponent.getId());
		obs.setConcept(conceptTranslator.toOpenmrsType(observationComponent.getCode()));
		observationValueTranslator.toOpenmrsType(obs, observationComponent.getValue());
		
		return obs;
	}
}
