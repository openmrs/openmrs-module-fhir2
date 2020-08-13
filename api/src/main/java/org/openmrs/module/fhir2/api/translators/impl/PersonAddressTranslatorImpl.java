/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.fhir2.api.translators.impl;

import org.hl7.fhir.r4.model.Address;
import org.hl7.fhir.r4.model.StringType;
import org.openmrs.PersonAddress;
import org.openmrs.module.fhir2.api.translators.PersonAddressTranslator;
import org.springframework.stereotype.Component;

@Component
public class PersonAddressTranslatorImpl extends BaseAddressTranslatorImpl implements PersonAddressTranslator {
	
	@Override
	public Address toFhirResource(PersonAddress address) {
		if (address == null) {
			return null;
		}
		
		Address fhirAddress = new Address();
		fhirAddress.setId(address.getUuid());
		fhirAddress.setCity(address.getCityVillage());
		fhirAddress.setState(address.getStateProvince());
		fhirAddress.setCountry(address.getCountry());
		fhirAddress.setPostalCode(address.getPostalCode());
		
		// TODO is this the right mapping?
		if (address.getPreferred() != null) {
			if (address.getPreferred()) {
				fhirAddress.setUse(Address.AddressUse.HOME);
			} else {
				fhirAddress.setUse(Address.AddressUse.OLD);
			}
		}
		
		addAddressExtensions(fhirAddress, address);
		
		return fhirAddress;
	}
	
	@Override
	public PersonAddress toOpenmrsType(Address address) {
		if (address == null) {
			return null;
		}
		
		return toOpenmrsType(new PersonAddress(), address);
	}
	
	@Override
	public PersonAddress toOpenmrsType(PersonAddress personAddress, Address address) {
		if (personAddress == null || address == null) {
			return personAddress;
		}
		
		personAddress.setUuid(address.getId());
		personAddress.setCityVillage(address.getCity());
		personAddress.setStateProvince(address.getState());
		personAddress.setCountry(address.getCountry());
		personAddress.setPostalCode(address.getPostalCode());
		
		if (Address.AddressUse.HOME.equals(address.getUse())) {
			personAddress.setPreferred(true);
		}
		
		getOpenmrsAddressExtension(address).ifPresent(ext -> ext.getExtension()
		        .forEach(e -> addAddressComponent(personAddress, e.getUrl(), ((StringType) e.getValue()).getValue())));
		
		return personAddress;
	}
}
