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

import org.hl7.fhir.r4.model.AllergyIntolerance;
import org.openmrs.Allergy;

public interface AllergyIntoleranceTranslator extends ToFhirTranslator<Allergy, AllergyIntolerance>, OpenmrsFhirUpdatableTranslator<Allergy, AllergyIntolerance> {
	
	/**
	 * Maps {@link org.openmrs.Allergy} to {@link org.hl7.fhir.r4.model.AllergyIntolerance}
	 * 
	 * @param allergy the OpenMRS data element to translate
	 * @return the corresponding FHIR AllergyIntolerance resource
	 */
	@Override
	AllergyIntolerance toFhirResource(Allergy allergy);
	
	/**
	 * Maps {@link org.hl7.fhir.r4.model.AllergyIntolerance} to {@link org.openmrs.Allergy}
	 *
	 * @param allergy the existing OpenMRS allergy to update
	 * @param allergyIntolerance the FHIR AllergyIntolerance resource to update the existing Allergy
	 *            with
	 * @return the updated OpenMrs Allergy object
	 */
	@Override
	Allergy toOpenmrsType(Allergy allergy, AllergyIntolerance allergyIntolerance);
}
