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

public interface TelecomTranslator<T> extends OpenmrsFhirUpdatableTranslator<T, ContactPoint> {
	
	/**
	 * Maps a {@link org.hl7.fhir.r4.model.ContactPoint } to an existing
	 * {@link org.openmrs.PersonAttribute}
	 * 
	 * @param whichAttributeOrExistingAttribute the Attribute to update Or to determine which, can
	 *            be personAttribute or locationAttribute etc.
	 * @param contactPoint the contactPoint to map
	 * @return an updated or mapped version of the openMrs attribute
	 */
	@Override
	T toOpenmrsType(T whichAttributeOrExistingAttribute, ContactPoint contactPoint);
	
	/**
	 * Maps an {@link java.lang.Object} It can be personAttribute, providerAttribute or
	 * locationAttribute etc to a {@link org.hl7.fhir.r4.model.ContactPoint}
	 * 
	 * @param attribute the OpenMRS attribute element to translate
	 * @return the corresponding FHIR ContactPoint resource
	 */
	@Override
	ContactPoint toFhirResource(T attribute);
	
	/**
	 * Maps a {@link org.hl7.fhir.r4.model.ContactPoint} to an OpenMRS {@link java.lang.Object}
	 * 
	 * @param contactPoint the FHIR contactPoint resource to translate
	 * @return the corresponding OpenMRS Object attribute
	 */
	@Override
	T toOpenmrsType(ContactPoint contactPoint);
}
