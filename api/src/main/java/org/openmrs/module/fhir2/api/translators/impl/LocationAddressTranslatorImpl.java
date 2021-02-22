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

import javax.annotation.Nonnull;

import org.hl7.fhir.r4.model.Address;
import org.hl7.fhir.r4.model.StringType;
import org.openmrs.Location;
import org.openmrs.module.fhir2.api.translators.LocationAddressTranslator;
import org.springframework.stereotype.Component;

@Component
public class LocationAddressTranslatorImpl extends BaseAddressTranslator implements LocationAddressTranslator {
	
	@Override
	public Address toFhirResource(@Nonnull Location omrsLocation) {
		Address address = null;
		if (omrsLocation != null) {
			address = new Address();
			address.setId(null);
			address.setCity(omrsLocation.getCityVillage());
			address.setState(omrsLocation.getStateProvince());
			address.setCountry(omrsLocation.getCountry());
			address.setPostalCode(omrsLocation.getPostalCode());
			
			addAddressExtensions(address, omrsLocation);
		}
		
		return address;
	}
	
	@Override
	public Location toOpenmrsType(@Nonnull Location omrsLocation, @Nonnull Address address) {
		if (omrsLocation == null || address == null) {
			return omrsLocation;
		}
		
		omrsLocation.setCityVillage(address.getCity());
		omrsLocation.setStateProvince(address.getState());
		omrsLocation.setCountry(address.getCountry());
		omrsLocation.setPostalCode(address.getPostalCode());
		
		getOpenmrsAddressExtension(address).ifPresent(ext -> ext.getExtension()
		        .forEach(e -> addAddressComponent(omrsLocation, e.getUrl(), ((StringType) e.getValue()).getValue())));
		return omrsLocation;
	}
	
}
