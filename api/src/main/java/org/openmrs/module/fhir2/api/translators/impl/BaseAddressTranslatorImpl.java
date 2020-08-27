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

import org.hl7.fhir.r4.model.Address;
import org.hl7.fhir.r4.model.Extension;
import org.hl7.fhir.r4.model.StringType;
import org.openmrs.module.fhir2.FhirConstants;

public abstract class BaseAddressTranslatorImpl {
	
	protected Optional<Extension> getOpenmrsAddressExtension(@NotNull Address address) {
		return Optional.ofNullable(address.getExtensionByUrl(FhirConstants.OPENMRS_FHIR_EXT_ADDRESS));
	}
	
	protected void addAddressExtension(@NotNull Address address, @NotNull String extensionProperty, @NotNull String value) {
		if (value == null) {
			return;
		}
		
		getOpenmrsAddressExtension(address)
		        .orElseGet(() -> address.addExtension().setUrl(FhirConstants.OPENMRS_FHIR_EXT_ADDRESS))
		        .addExtension(FhirConstants.OPENMRS_FHIR_EXT_ADDRESS + "#" + extensionProperty, new StringType(value));
	}
	
	protected void addAddressComponent(@NotNull org.openmrs.Address addressOfResource, @NotNull String url,
	        @NotNull String value) {
		if (value == null || url == null || !url.startsWith(FhirConstants.OPENMRS_FHIR_EXT_ADDRESS + "#")) {
			return;
		}
		
		String address = url.substring(url.lastIndexOf('#') + 1);
		
		switch (address) {
			case "address1":
				addressOfResource.setAddress1(value);
				break;
			case "address2":
				addressOfResource.setAddress2(value);
				break;
			case "address3":
				addressOfResource.setAddress3(value);
				break;
			case "address4":
				addressOfResource.setAddress4(value);
				break;
			case "address5":
				addressOfResource.setAddress5(value);
				break;
			case "address6":
				addressOfResource.setAddress6(value);
				break;
			case "address7":
				addressOfResource.setAddress7(value);
				break;
			case "address8":
				addressOfResource.setAddress8(value);
				break;
			case "address9":
				addressOfResource.setAddress9(value);
				break;
			case "address10":
				addressOfResource.setAddress10(value);
				break;
			case "address11":
				addressOfResource.setAddress11(value);
				break;
			case "address12":
				addressOfResource.setAddress12(value);
				break;
			case "address13":
				addressOfResource.setAddress13(value);
				break;
			case "address14":
				addressOfResource.setAddress14(value);
				break;
			case "address15":
				addressOfResource.setAddress15(value);
				break;
			
		}
	}
	
	protected void addAddressExtensions(Address address, org.openmrs.Address addressOfResource) {
		addAddressExtension(address, "address1", addressOfResource.getAddress1());
		addAddressExtension(address, "address2", addressOfResource.getAddress2());
		addAddressExtension(address, "address3", addressOfResource.getAddress3());
		addAddressExtension(address, "address4", addressOfResource.getAddress4());
		addAddressExtension(address, "address5", addressOfResource.getAddress5());
		addAddressExtension(address, "address6", addressOfResource.getAddress6());
		addAddressExtension(address, "address7", addressOfResource.getAddress7());
		addAddressExtension(address, "address8", addressOfResource.getAddress8());
		addAddressExtension(address, "address9", addressOfResource.getAddress9());
		addAddressExtension(address, "address10", addressOfResource.getAddress10());
		addAddressExtension(address, "address11", addressOfResource.getAddress11());
		addAddressExtension(address, "address12", addressOfResource.getAddress12());
		addAddressExtension(address, "address13", addressOfResource.getAddress13());
		addAddressExtension(address, "address14", addressOfResource.getAddress14());
		addAddressExtension(address, "address15", addressOfResource.getAddress15());
	}
}
