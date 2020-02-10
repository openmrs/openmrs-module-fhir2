/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.fhir2.api.impl;

import ca.uhn.fhir.rest.api.SortSpec;
import ca.uhn.fhir.rest.param.ReferenceParam;
import ca.uhn.fhir.rest.param.TokenAndListParam;
import lombok.AccessLevel;
import lombok.Setter;
import org.hl7.fhir.r4.model.Observation;
import org.openmrs.annotation.OpenmrsProfile;
import org.openmrs.module.fhir2.api.translators.ObservationTranslator;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import javax.transaction.Transactional;
import java.util.Collection;

@Primary
@Component
@Transactional
@Setter(AccessLevel.PACKAGE)
@OpenmrsProfile(openmrsPlatformVersion = "2.1.* - 2.3.*")
public class FhirObservationServiceImpl_2_1 extends FhirObservationServiceImpl{

    @Inject
    ObservationTranslator observationTranslator;

    @Override
    public Observation getObservationByUuid(String uuid) {
        return super.getObservationByUuid(uuid);
    }

    @Override
    public Collection<Observation> searchForObservations(ReferenceParam encounterReference, ReferenceParam patientReference, TokenAndListParam code, SortSpec sort) {
        return super.searchForObservations(encounterReference, patientReference, code, sort);
    }
}
