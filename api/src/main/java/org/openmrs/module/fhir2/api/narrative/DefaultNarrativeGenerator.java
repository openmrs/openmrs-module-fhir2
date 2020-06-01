/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.fhir2.api.narrative;

import java.util.Arrays;
import java.util.List;

import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.Narrative;

public interface DefaultNarrativeGenerator {
	
	List<String> resourcesWithHAPIDefaultNarratives = Arrays.asList("DiagnosticReport", "Medication",
	    "MedicationPrescription", "OperationOutcome", "Patient");
	
	Narrative generateDefaultNarrative(IBaseResource iBaseResource);
	
	Narrative generateHAPIDefaultNarrative(IBaseResource iBaseResource);
}
