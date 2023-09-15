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

import org.hl7.fhir.r4.model.Task;
import org.openmrs.module.fhir2.model.FhirTaskInput;

public interface TaskInputTranslator extends ToFhirTranslator<FhirTaskInput, Task.ParameterComponent>, ToOpenmrsTranslator<FhirTaskInput, Task.ParameterComponent> {
	
	/**
	 * Maps an {@link org.openmrs.module.fhir2.model.FhirTaskInput} to a corresponding
	 * {@link org.hl7.fhir.r4.model.Task.ParameterComponent}
	 * 
	 * @param openmrsTaskInput the Openmrs Task input to translate
	 * @return the corresponding FHIR Task input
	 */
	@Override
	Task.ParameterComponent toFhirResource(@Nonnull FhirTaskInput openmrsTaskInput);
	
	/**
	 * Maps an {@link org.hl7.fhir.r4.model.Task.ParameterComponent} to a existing
	 * {@link org.openmrs.module.fhir2.model.FhirTaskInput}
	 * 
	 * @param fhirTaskInput FHIR Task input to translate
	 * @return corresponding Openmrs Task input
	 */
	@Override
	FhirTaskInput toOpenmrsType(@Nonnull Task.ParameterComponent fhirTaskInput);
}
