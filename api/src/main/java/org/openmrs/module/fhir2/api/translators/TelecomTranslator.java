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
import org.openmrs.PersonAttribute;

public interface TelecomTranslator extends OpenmrsFhirUpdatableTranslator<PersonAttribute, ContactPoint> {
	
	/**
	 * Maps a {@link org.hl7.fhir.r4.model.ContactPoint } to an existing
	 * {@link org.openmrs.PersonAttribute}
	 * 
	 * @param currentPersonAttribute the currentPersonAttribute to update
	 * @param contactPoint the contactPoint to map
	 * @return an updated version of the currentPersonAttribute
	 */
	@Override
	PersonAttribute toOpenmrsType(PersonAttribute currentPersonAttribute, ContactPoint contactPoint);
	
	/**
	 * Maps an {@link org.openmrs.PersonAttribute} to a {@link org.hl7.fhir.r4.model.ContactPoint}
	 * 
	 * @param personAttribute the OpenMRS personAttribute element to translate
	 * @return the corresponding FHIR resource
	 */
	@Override
	ContactPoint toFhirResource(PersonAttribute personAttribute);
	
	/**
	 * Maps a FHIR {@link org.hl7.fhir.r4.model.ContactPoint} to an
	 * {@link org.openmrs.PersonAttribute}
	 * 
	 * @param contactPoint the FHIR contactPoint to translate
	 * @return the corresponding OpenMRS data element
	 */
	@Override
	PersonAttribute toOpenmrsType(ContactPoint contactPoint);
}
