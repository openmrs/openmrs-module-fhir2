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
import org.openmrs.module.fhir2.model.FhirTaskOutput;

public interface TaskOutputTranslator extends ToFhirTranslator<FhirTaskOutput, Task.TaskOutputComponent>, ToOpenmrsTranslator<FhirTaskOutput, Task.TaskOutputComponent> {
	
	/**
	 * Maps an {@link org.openmrs.module.fhir2.model.FhirTaskOutput} to a corresponding
	 * {@link org.hl7.fhir.r4.model.Task.TaskOutputComponent}
	 * 
	 * @param openmrsOutput the Openmrs Task output to translate
	 * @return the corresponding FHIR Task output
	 */
	@Override
	Task.TaskOutputComponent toFhirResource(@Nonnull FhirTaskOutput openmrsTaskOutput);
	
	/**
	 * Maps an {@link org.hl7.fhir.r4.model.Task.TaskOutputComponent} to a existing
	 * {@link org.openmrs.module.fhir2.model.FhirTaskOutput}
	 * 
	 * @param fhirOutput FHIR Task output to translate
	 * @return corresponding Openmrs Task output
	 */
	@Override
	FhirTaskOutput toOpenmrsType(@Nonnull Task.TaskOutputComponent fhirTaskOutput);
}
