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

import javax.annotation.Nonnull;

import org.hl7.fhir.r4.model.Flag;
import org.openmrs.module.fhir2.model.FhirFlag;

public interface FlagStatusTranslator extends OpenmrsFhirTranslator<FhirFlag.FlagStatus, Flag.FlagStatus> {
	
	/**
	 * Maps an OpenMRS status element to a FHIR resource
	 *
	 * @param status the OpenMRS status element to translate
	 * @return the corresponding FHIR resource
	 */
	@Override
	Flag.FlagStatus toFhirResource(@Nonnull FhirFlag.FlagStatus status);
	
	/**
	 * Maps a FHIR flagStatus to an OpenMRS data element
	 *
	 * @param flagStatus the FHIR flagStatus to translate
	 * @return the corresponding OpenMRS data element
	 */
	@Override
	FhirFlag.FlagStatus toOpenmrsType(@Nonnull Flag.FlagStatus flagStatus);
}
