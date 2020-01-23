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

import javax.validation.constraints.NotNull;

import java.util.Optional;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hl7.fhir.r4.model.Address;
import org.hl7.fhir.r4.model.Extension;
import org.hl7.fhir.r4.model.StringType;
import org.openmrs.Location;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.api.translators.LocationAddressTranslator;
import org.springframework.stereotype.Component;

@Component
public class LocationAddressTranslatorImpl implements LocationAddressTranslator {
	
	private static Log log = LogFactory.getLog(LocationAddressTranslatorImpl.class);
	
	@Override
	public Address toFhirResource(Location omrsLocation) {
		Address address = new Address();
		if (omrsLocation != null) {
			address.setId(null);
			address.setCity(omrsLocation.getCityVillage());
			address.setState(omrsLocation.getStateProvince());
			address.setCountry(omrsLocation.getCountry());
			address.setPostalCode(omrsLocation.getPostalCode());
			
			addAddressExtension(address, "address1", omrsLocation.getAddress1());
			addAddressExtension(address, "address2", omrsLocation.getAddress2());
			addAddressExtension(address, "address3", omrsLocation.getAddress3());
			addAddressExtension(address, "address4", omrsLocation.getAddress4());
			addAddressExtension(address, "address5", omrsLocation.getAddress5());
			addAddressExtension(address, "address6", omrsLocation.getAddress6());
			addAddressExtension(address, "address7", omrsLocation.getAddress7());
			addAddressExtension(address, "address8", omrsLocation.getAddress8());
			addAddressExtension(address, "address9", omrsLocation.getAddress9());
			addAddressExtension(address, "address10", omrsLocation.getAddress10());
			addAddressExtension(address, "address11", omrsLocation.getAddress11());
			addAddressExtension(address, "address12", omrsLocation.getAddress12());
			addAddressExtension(address, "address13", omrsLocation.getAddress13());
			addAddressExtension(address, "address14", omrsLocation.getAddress14());
			addAddressExtension(address, "address15", omrsLocation.getAddress15());
			
		}
		return address;
	}
	
	@Override
	public Location toOpenmrsType(Address address) {
		return toOpenmrsType(new Location(), address);
	}
	
	@Override
    public Location toOpenmrsType(Location omrsLocation, Address address) {
        omrsLocation.setCityVillage(address.getCity());
        omrsLocation.setStateProvince(address.getState());
        omrsLocation.setCountry(address.getCountry());
        omrsLocation.setPostalCode(address.getPostalCode());

        getOpenmrsAddressExtension(address).ifPresent(ext ->
                ext.getExtension().forEach(e -> addAddressComponent(omrsLocation, e.getUrl(), ((StringType) e.getValue()).getValue()))
        );
        return omrsLocation;
    }
	
	private void addAddressExtension(@NotNull Address address, @NotNull String extensionProperty, @NotNull String value) {
        if (value == null) {
            return;
        }

        getOpenmrsAddressExtension(address).orElseGet(() -> address.addExtension().setUrl(FhirConstants.OPENMRS_FHIR_EXT_ADDRESS))
                .addExtension(FhirConstants.OPENMRS_FHIR_EXT_ADDRESS + "#" + extensionProperty, new StringType(value));
    }
	
	public void addAddressComponent(@NotNull Location location, @NotNull String url, @NotNull String value) {
		if (value == null || url == null || !url.startsWith(FhirConstants.OPENMRS_FHIR_EXT_ADDRESS + "#")) {
			return;
		}
		
		String address = url.substring(url.lastIndexOf('#') + 1);
		
		switch (address) {
			case "address1":
				location.setAddress1(value);
				break;
			case "address2":
				location.setAddress2(value);
				break;
			case "address3":
				location.setAddress3(value);
				break;
			case "address4":
				location.setAddress4(value);
				break;
			case "address5":
				location.setAddress5(value);
				break;
			case "address6":
				location.setAddress6(value);
				break;
			case "address7":
				location.setAddress7(value);
				break;
			case "address8":
				location.setAddress8(value);
				break;
			case "address9":
				location.setAddress9(value);
				break;
			case "address10":
				location.setAddress10(value);
				break;
			case "address11":
				location.setAddress11(value);
				break;
			case "address12":
				location.setAddress12(value);
				break;
			case "address13":
				location.setAddress13(value);
				break;
			case "address14":
				location.setAddress14(value);
				break;
			case "address15":
				location.setAddress15(value);
				break;
		
		}
	}
	
	private Optional<Extension> getOpenmrsAddressExtension(@NotNull Address address) {
		return Optional.ofNullable(address.getExtensionByUrl(FhirConstants.OPENMRS_FHIR_EXT_ADDRESS));
		
	}
}
