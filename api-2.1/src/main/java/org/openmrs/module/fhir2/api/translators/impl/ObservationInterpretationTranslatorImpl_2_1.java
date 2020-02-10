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
import org.openmrs.Obs;
import org.openmrs.annotation.OpenmrsProfile;
import org.openmrs.module.fhir2.api.translators.ObservationInterpretationTranslator;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

@Primary
@Component
@Setter(AccessLevel.PACKAGE)
@OpenmrsProfile(openmrsPlatformVersion = "2.1.* - 2.3.*")
public class ObservationInterpretationTranslatorImpl_2_1 implements ObservationInterpretationTranslator {

    @Override
    public CodeableConcept toFhirResource(Obs obs) {
        CodeableConcept interpretation = new CodeableConcept();
        if (obs.getInterpretation() != null){
            interpretation.setText(obs.getInterpretation().toString());
        }
        return interpretation;
    }

    @Override
    public Obs toOpenmrsType(Obs existingObject, CodeableConcept interpretation) {
        existingObject.setInterpretation(Obs.Interpretation.valueOf(interpretation.getText()));
        return existingObject;
    }
}
