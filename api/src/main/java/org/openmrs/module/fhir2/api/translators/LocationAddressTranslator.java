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

import org.hl7.fhir.r4.model.Address;
import org.openmrs.Location;

public interface LocationAddressTranslator extends OpenmrsFhirUpdatableTranslator<Location, Address> {
	
	/**
	 * Maps an {@link org.openmrs.Location} to a {@link org.hl7.fhir.r4.model.Address}
	 * 
	 * @param omrsLocation the location to translate
	 * @return the corresponding FHIR address resource
	 */
	@Override
	Address toFhirResource(Location omrsLocation);
	
	/**
	 * Maps an {@link org.openmrs.Location} to a {@link org.hl7.fhir.r4.model.Address}
	 * 
	 * @param address the location to translate
	 * @return the corresponding FHIR location resource with address properties
	 */
	@Override
	Location toOpenmrsType(Address address);
	
	/**
	 * Maps an {@link org.openmrs.Location} to a {@link org.hl7.fhir.r4.model.Address}
	 * 
	 * @param location the location resource to update
	 * @param address the location to translate
	 * @return the updated OpenMRS location address
	 */
	@Override
	Location toOpenmrsType(Location location, Address address);
}
