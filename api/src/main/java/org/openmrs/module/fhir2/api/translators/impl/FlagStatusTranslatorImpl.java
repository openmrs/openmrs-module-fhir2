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

import javax.annotation.Nonnull;

import org.hl7.fhir.r4.model.Flag;
import org.openmrs.module.fhir2.api.translators.FlagStatusTranslator;
import org.openmrs.module.fhir2.model.FhirFlag;

public class FlagStatusTranslatorImpl implements FlagStatusTranslator {
	
	@Override
	public Flag.FlagStatus toFhirResource(@Nonnull FhirFlag.FlagStatus status) {
		switch (status) {
			case ACTIVE:
				return Flag.FlagStatus.ACTIVE;
			case INACTIVE:
				return Flag.FlagStatus.INACTIVE;
			case ENTERED_IN_ERROR:
				return Flag.FlagStatus.ENTEREDINERROR;
			default:
				return Flag.FlagStatus.NULL;
		}
	}
	
	@Override
	public FhirFlag.FlagStatus toOpenmrsType(@Nonnull Flag.FlagStatus flagStatus) {
		switch (flagStatus) {
			case ACTIVE:
				return FhirFlag.FlagStatus.ACTIVE;
			case INACTIVE:
				return FhirFlag.FlagStatus.INACTIVE;
			case ENTEREDINERROR:
				return FhirFlag.FlagStatus.ENTERED_IN_ERROR;
			default:
				return FhirFlag.FlagStatus.NULL;
		}
	}
}
