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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hl7.fhir.r4.model.Address;
import org.hl7.fhir.r4.model.StringType;
import org.openmrs.Location;
import org.openmrs.module.fhir2.api.translators.LocationAddressTranslator;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

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
			address.setLine(getAddressLine(omrsLocation));
			
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
		setLocationAddress(omrsLocation, address);
		
		return omrsLocation;
	}
	
	public List<StringType> getAddressLine(Location omrsLocation){
		List<StringType> theLine = new ArrayList<>();

		theLine.add(new StringType(omrsLocation.getAddress1()));
		theLine.add(new StringType(omrsLocation.getAddress2()));
		theLine.add(new StringType(omrsLocation.getAddress3()));
		theLine.add(new StringType(omrsLocation.getAddress4()));
		theLine.add(new StringType(omrsLocation.getAddress5()));
		theLine.add(new StringType(omrsLocation.getAddress6()));
		theLine.add(new StringType(omrsLocation.getAddress7()));
		theLine.add(new StringType(omrsLocation.getAddress8()));
		theLine.add(new StringType(omrsLocation.getAddress9()));
		theLine.add(new StringType(omrsLocation.getAddress10()));
		theLine.add(new StringType(omrsLocation.getAddress11()));
		theLine.add(new StringType(omrsLocation.getAddress12()));
		theLine.add(new StringType(omrsLocation.getAddress13()));
		theLine.add(new StringType(omrsLocation.getAddress14()));
		theLine.add(new StringType(omrsLocation.getAddress15()));

		return theLine;
	}
	
	public void setLocationAddress(Location omrsLocation, Address address) {
		if (address.getLine().size() > 0) {
			omrsLocation.setAddress1(address.getLine().get(0).toString());
			omrsLocation.setAddress2(address.getLine().get(1).toString());
			omrsLocation.setAddress3(address.getLine().get(2).toString());
			omrsLocation.setAddress4(address.getLine().get(3).toString());
			omrsLocation.setAddress5(address.getLine().get(4).toString());
			omrsLocation.setAddress6(address.getLine().get(5).toString());
			omrsLocation.setAddress7(address.getLine().get(6).toString());
			omrsLocation.setAddress8(address.getLine().get(7).toString());
			omrsLocation.setAddress9(address.getLine().get(8).toString());
			omrsLocation.setAddress10(address.getLine().get(9).toString());
			omrsLocation.setAddress11(address.getLine().get(10).toString());
			omrsLocation.setAddress12(address.getLine().get(11).toString());
			omrsLocation.setAddress13(address.getLine().get(12).toString());
			omrsLocation.setAddress14(address.getLine().get(13).toString());
			omrsLocation.setAddress15(address.getLine().get(14).toString());
		}
	}
}
