/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.fhir2.api.translators;

import org.hl7.fhir.r4.model.Type;
import org.openmrs.Obs;

public interface ObservationEffectiveDatetimeTranslator extends ToFhirTranslator<Obs, Type>, UpdatableOpenmrsTranslator<Obs, Type> {
	
	/**
	 * Maps an {@link org.openmrs.Obs} to a corresponding {@link org.hl7.fhir.r4.model.Type}
	 *
	 * @param obs the obs to translate
	 * @return the corresponding FHIR type
	 */
	@Override
	Type toFhirResource(Obs obs);
	
	/**
	 * Maps an {@link org.hl7.fhir.r4.model.Type} to a existing {@link org.openmrs.Obs}
	 *
	 * @param obs the obs to update
	 * @param resource the resource to map
	 * @return an updated version of the obs
	 */
	@Override
	Obs toOpenmrsType(Obs obs, Type resource);
}
