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

import org.hl7.fhir.r4.model.ContactPoint;
import org.openmrs.LocationAttribute;

public interface LocationTelecomTranslator extends OpenmrsFhirUpdatableTranslator<LocationAttribute, ContactPoint> {
	
	/**
	 * Maps an {@link org.openmrs.LocationAttribute} to a {@link org.hl7.fhir.r4.model.ContactPoint}
	 * 
	 * @param locationAttribute the locationPoint to translate
	 * @return the corresponding FHIR contact point resource
	 */
	@Override
	ContactPoint toFhirResource(LocationAttribute locationAttribute);
	
	/**
	 * Maps an {@link org.openmrs.LocationAttribute} to a {@link org.hl7.fhir.r4.model.ContactPoint}
	 * 
	 * @param contactPoint the contactPoint to translate
	 * @return the corresponding OpenMRS location attribute
	 */
	@Override
	LocationAttribute toOpenmrsType(ContactPoint contactPoint);
	
	/**
	 * Maps an {@link org.openmrs.LocationAttribute} to a {@link org.hl7.fhir.r4.model.ContactPoint}
	 * 
	 * @param locationAttribute the locationAttribute to update
	 * @param contactPoint the contactPoint to translate
	 * @return the updated OpenMRS location attribute
	 */
	@Override
	LocationAttribute toOpenmrsType(LocationAttribute locationAttribute, ContactPoint contactPoint);
}
