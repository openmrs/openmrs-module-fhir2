/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.fhir2.api.translators.impl;

import lombok.AccessLevel;
import lombok.Setter;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Observation;
import org.openmrs.Obs;
import org.openmrs.annotation.OpenmrsProfile;
import org.openmrs.module.fhir2.api.translators.ObservationInterpretationTranslator;
import org.openmrs.module.fhir2.api.translators.ObservationStatusTranslator;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.util.Collections;
import java.util.function.Supplier;

@Primary
@Component
@Setter(AccessLevel.PACKAGE)
@OpenmrsProfile(openmrsPlatformVersion = "2.1.* - 2.3.*")
public class ObservationTranslatorImpl_2_1 extends ObservationTranslatorImpl {

    @Inject
    private ObservationStatusTranslator observationStatusTranslator;

    @Inject
    private ObservationInterpretationTranslator interpretationTranslator;

    @Override
    public Observation toFhirResource(Obs obs) {
        Observation observation = super.toFhirResource(obs);
        observation.setStatus(observationStatusTranslator.toFhirResource(obs));
        observation.setInterpretation(Collections.singletonList(interpretationTranslator.toFhirResource(obs)));
        return observation;
    }

    @Override
    public Obs toOpenmrsType(Obs existingObs, Observation observation, Supplier<Obs> groupedObsFactory) {
        Obs obs =  super.toOpenmrsType(existingObs, observation, groupedObsFactory);
        observationStatusTranslator.toOpenmrsType(obs, observation.getStatus());
        interpretationTranslator.toOpenmrsType(existingObs, new CodeableConcept().setText(observation.getInterpretation().toString()));
        return obs;
    }
}
