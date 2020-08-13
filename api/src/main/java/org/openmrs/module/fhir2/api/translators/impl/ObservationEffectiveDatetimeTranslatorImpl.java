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

import lombok.AccessLevel;
import lombok.Setter;
import org.hl7.fhir.r4.model.DateTimeType;
import org.hl7.fhir.r4.model.InstantType;
import org.hl7.fhir.r4.model.Type;
import org.openmrs.Obs;
import org.openmrs.module.fhir2.api.translators.ObservationEffectiveDatetimeTranslator;
import org.springframework.stereotype.Component;

@Component
@Setter(AccessLevel.PACKAGE)
public class ObservationEffectiveDatetimeTranslatorImpl implements ObservationEffectiveDatetimeTranslator {
	
	@Override
	public Type toFhirResource(Obs obs) {
		if (obs == null) {
			return null;
		}
		
		if (obs.getObsDatetime() != null) {
			return (new DateTimeType(obs.getObsDatetime()));
		}
		
		return null;
	}
	
	@Override
	public Obs toOpenmrsType(Obs obs, Type resource) {
		notNull(obs, "The existing Obs object should not be null");
		notNull(resource, "The DateTime object should not be null");
		
		if (resource instanceof DateTimeType) {
			obs.setObsDatetime(((DateTimeType) resource).getValue());
		} else if (resource instanceof InstantType) {
			obs.setObsDatetime(((InstantType) resource).getValue());
		}
		
		return obs;
	}
}
