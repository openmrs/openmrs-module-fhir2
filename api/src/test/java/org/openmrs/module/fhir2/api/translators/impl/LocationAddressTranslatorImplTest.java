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
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.openmrs.Location;

import java.util.ArrayList;
import java.util.List;
import static org.hamcrest.Matchers.greaterThan;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class LocationAddressTranslatorImplTest {
	
	private static final String CITY = "Test city";
	
	private static final String STATE_PROVINCE = "Test province";
	
	private static final String COUNTRY = "Test country";
	
	private static final String POSTAL_CODE = "Test postal code";
	
	private static final String ADDRESS_1 = "Address 1";
	
	private static final String ADDRESS_2 = "Address 2";
	
	private static final String ADDRESS_3 = "Address 3";
	
	private static final String ADDRESS_4 = "Address 4";
	
	private static final String ADDRESS_5 = "Address 5";
	
	private static final String ADDRESS_6 = "Address 6";
	
	private static final String ADDRESS_7 = "Address 7";
	
	private static final String ADDRESS_8 = "Address 8";
	
	private static final String ADDRESS_9 = "Address 9";
	
	private static final String ADDRESS_10 = "Address 10";
	
	private static final String ADDRESS_11 = "Address 11";
	
	private static final String ADDRESS_12 = "Address 12";
	
	private static final String ADDRESS_13 = "Address 13";
	
	private static final String ADDRESS_14 = "Address 14";
	
	private static final String ADDRESS_15 = "Address 15";
	
	private LocationAddressTranslatorImpl translator;
	
	private Location omrsLocation;
	
	@Before
	public void setUp() {
		translator = new LocationAddressTranslatorImpl();
		omrsLocation = new Location();
		omrsLocation.setAddress1(ADDRESS_1);
		omrsLocation.setAddress2(ADDRESS_2);
		omrsLocation.setAddress3(ADDRESS_3);
		omrsLocation.setAddress4(ADDRESS_4);
		omrsLocation.setAddress5(ADDRESS_5);
		omrsLocation.setAddress6(ADDRESS_6);
		omrsLocation.setAddress7(ADDRESS_7);
		omrsLocation.setAddress8(ADDRESS_8);
		omrsLocation.setAddress9(ADDRESS_9);
		omrsLocation.setAddress10(ADDRESS_10);
		omrsLocation.setAddress11(ADDRESS_11);
		omrsLocation.setAddress12(ADDRESS_12);
		omrsLocation.setAddress13(ADDRESS_13);
		omrsLocation.setAddress14(ADDRESS_14);
		omrsLocation.setAddress15(ADDRESS_15);
	}
	
	@Test
	public void shouldTranslateLocationCityVillageToAddressCity() {
		omrsLocation.setCityVillage(CITY);
		org.hl7.fhir.r4.model.Address address = translator.toFhirResource(omrsLocation);
		assertNotNull(address);
		assertNotNull(address.getCity());
		assertEquals(address.getCity(), CITY);
	}
	
	@Test
	public void shouldTranslateLocationProvinceToAddressProvince() {
		omrsLocation.setStateProvince(STATE_PROVINCE);
		org.hl7.fhir.r4.model.Address address = translator.toFhirResource(omrsLocation);
		assertNotNull(address);
		assertNotNull(address.getState());
		assertEquals(address.getState(), STATE_PROVINCE);
		
	}
	
	@Test
	public void shouldTranslateLocationCountryToAddressCountry() {
		omrsLocation.setCountry(COUNTRY);
		org.hl7.fhir.r4.model.Address address = translator.toFhirResource(omrsLocation);
		assertNotNull(address);
		assertNotNull(address.getCountry());
		assertEquals(address.getCountry(), COUNTRY);
		
	}
	
	@Test
	public void shouldTranslateLocationPostalCodeToAddressCode() {
		omrsLocation.setPostalCode(POSTAL_CODE);
		org.hl7.fhir.r4.model.Address address = translator.toFhirResource(omrsLocation);
		assertNotNull(address);
		assertNotNull(address.getPostalCode());
		assertEquals(address.getPostalCode(), POSTAL_CODE);
		
	}
	
	@Test
	public void toFhirResource_shouldReturnNullIfCalledWithoutLocation() {
		Address address = translator.toFhirResource(null);
		assertNull(address.getCity());
		assertNull(address.getState());
		assertNull(address.getPostalCode());
		assertNull(address.getCountry());
	}
	
	@Test
	public void shouldTranslateAddressCityToLocationCity() {
		org.hl7.fhir.r4.model.Address address = new Address();
		address.setCity(CITY);
		omrsLocation = translator.toOpenmrsType(address);
		assertNotNull(omrsLocation);
		assertNotNull(omrsLocation.getCityVillage());
		assertEquals(omrsLocation.getCityVillage(), CITY);
	}
	
	@Test
	public void shouldTranslateAddressStateToLocationState() {
		org.hl7.fhir.r4.model.Address address = new Address();
		address.setState(STATE_PROVINCE);
		omrsLocation = translator.toOpenmrsType(address);
		assertNotNull(omrsLocation);
		assertNotNull(omrsLocation.getStateProvince());
		assertEquals(omrsLocation.getStateProvince(), STATE_PROVINCE);
	}
	
	@Test
	public void shouldTranslateAddressCountryToLocationCountry() {
		org.hl7.fhir.r4.model.Address address = new Address();
		address.setCountry(COUNTRY);
		omrsLocation = translator.toOpenmrsType(address);
		assertNotNull(omrsLocation);
		assertNotNull(omrsLocation.getCountry());
		assertEquals(omrsLocation.getCountry(), COUNTRY);
	}
	
	@Test
	public void shouldTranslateAddressPostalCodeToLocationPostalCode() {
		org.hl7.fhir.r4.model.Address address = new Address();
		address.setPostalCode(POSTAL_CODE);
		omrsLocation = translator.toOpenmrsType(address);
		assertNotNull(omrsLocation);
		assertNotNull(omrsLocation.getPostalCode());
		assertEquals(omrsLocation.getPostalCode(), POSTAL_CODE);
	}
	
	@Test
	public void getAddressLine_shouldAddAddressesCorrectly() {
		List<StringType> line = translator.getAddressLine(omrsLocation);
		assertThat(line.size(), greaterThan(0));
		assertEquals(line.get(0).toString(), ADDRESS_1);
		assertEquals(line.get(1).toString(), ADDRESS_2);
		assertEquals(line.get(2).toString(), ADDRESS_3);
		assertEquals(line.get(3).toString(), ADDRESS_4);
		assertEquals(line.get(4).toString(), ADDRESS_5);
		assertEquals(line.get(5).toString(), ADDRESS_6);
		assertEquals(line.get(6).toString(), ADDRESS_7);
		assertEquals(line.get(7).toString(), ADDRESS_8);
		assertEquals(line.get(8).toString(), ADDRESS_9);
		assertEquals(line.get(9).toString(), ADDRESS_10);
		assertEquals(line.get(10).toString(), ADDRESS_11);
		assertEquals(line.get(11).toString(), ADDRESS_12);
		assertEquals(line.get(12).toString(), ADDRESS_13);
		assertEquals(line.get(13).toString(), ADDRESS_14);
		assertEquals(line.get(14).toString(), ADDRESS_15);
		
	}
	
	@Test
	public void setLocationAddress_shouldSetLocationAddressesCorrectly(){
		Location omrsLocation = new Location();
		Address address = new Address();
		List<StringType> line = new ArrayList<>();
		line.add(new StringType(ADDRESS_1));
		line.add(new StringType(ADDRESS_2));
		line.add(new StringType(ADDRESS_3));
		line.add(new StringType(ADDRESS_4));
		line.add(new StringType(ADDRESS_5));
		line.add(new StringType(ADDRESS_6));
		line.add(new StringType(ADDRESS_7));
		line.add(new StringType(ADDRESS_8));
		line.add(new StringType(ADDRESS_9));
		line.add(new StringType(ADDRESS_10));
		line.add(new StringType(ADDRESS_11));
		line.add(new StringType(ADDRESS_12));
		line.add(new StringType(ADDRESS_13));
		line.add(new StringType(ADDRESS_14));
		line.add(new StringType(ADDRESS_15));
		address.setLine(line);
		translator.setLocationAddress(omrsLocation, address);

		assertEquals(omrsLocation.getAddress1(), ADDRESS_1);
		assertEquals(omrsLocation.getAddress2(), ADDRESS_2);
		assertEquals(omrsLocation.getAddress3(), ADDRESS_3);
		assertEquals(omrsLocation.getAddress4(), ADDRESS_4);
		assertEquals(omrsLocation.getAddress5(), ADDRESS_5);
		assertEquals(omrsLocation.getAddress6(), ADDRESS_6);
		assertEquals(omrsLocation.getAddress7(), ADDRESS_7);
		assertEquals(omrsLocation.getAddress8(), ADDRESS_8);
		assertEquals(omrsLocation.getAddress9(), ADDRESS_9);
		assertEquals(omrsLocation.getAddress10(), ADDRESS_10);
		assertEquals(omrsLocation.getAddress11(), ADDRESS_11);
		assertEquals(omrsLocation.getAddress12(), ADDRESS_12);
		assertEquals(omrsLocation.getAddress13(), ADDRESS_13);
		assertEquals(omrsLocation.getAddress14(), ADDRESS_14);
		assertEquals(omrsLocation.getAddress15(), ADDRESS_15);
	}
	
	@Test
	public void setLocationAddress_shouldNotSetLocationAddressesIfNoAddressIsProvided(){
		List<StringType> line = new ArrayList<>();
		Location omrsLocation = new Location();
		Address address = new Address();
		address.setLine(line);
		translator.setLocationAddress(omrsLocation, address);

		assertNull(omrsLocation.getAddress1());
		assertNull(omrsLocation.getAddress2());
		assertNull(omrsLocation.getAddress3());
		assertNull(omrsLocation.getAddress4());
		assertNull(omrsLocation.getAddress5());
		assertNull(omrsLocation.getAddress6());
		assertNull(omrsLocation.getAddress7());
		assertNull(omrsLocation.getAddress8());
		assertNull(omrsLocation.getAddress9());
		assertNull(omrsLocation.getAddress10());
		assertNull(omrsLocation.getAddress11());
		assertNull(omrsLocation.getAddress12());
		assertNull(omrsLocation.getAddress13());
		assertNull(omrsLocation.getAddress14());
		assertNull(omrsLocation.getAddress15());
		assertEquals(line.size(), 0);
	}
}
