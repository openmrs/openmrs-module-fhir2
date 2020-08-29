/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.fhir2.api;

import java.util.Collection;

import ca.uhn.fhir.rest.annotation.Sort;
import ca.uhn.fhir.rest.api.SortSpec;
import ca.uhn.fhir.rest.param.ReferenceParam;
import org.hl7.fhir.r4.model.Immunization;
import org.openmrs.Concept;

public interface FhirImmunizationService extends FhirService<Immunization> {
	
	/**
	 * Fetches the OpenMRS concept used to capture immunization obs groups.
	 * 
	 * @return The OpenMRS immunization concept.
	 */
	Concept getOpenmrsImmunizationConcept();
	
	Collection<Immunization> searchImmunizations(ReferenceParam patientParam, @Sort SortSpec sort);
	
}
