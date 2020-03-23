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
import org.openmrs.PersonAddress;

public interface PersonAddressTranslator extends OpenmrsFhirUpdatableTranslator<PersonAddress, Address> {
	
	/**
	 * Maps an {@link PersonAddress} to an {@link Address}
	 * 
	 * @param address the address to translate
	 * @return the corresponding FHIR address
	 */
	@Override
	Address toFhirResource(PersonAddress address);
	
	/**
	 * Maps an {@link Address} to an {@link org.openmrs.Address}
	 * 
	 * @param address the address to translate
	 * @return the corresponding OpenMRS address
	 */
	@Override
	PersonAddress toOpenmrsType(Address address);
	
	/**
	 * Maps an {@link Address} to an existing {@link org.openmrs.Address}
	 * 
	 * @param personAddress the person address to update
	 * @param address the resource to translate
	 * @return the updated OpenMRS address
	 */
	@Override
	PersonAddress toOpenmrsType(PersonAddress personAddress, Address address);
}
