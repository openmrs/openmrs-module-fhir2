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

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasProperty;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.hamcrest.MatcherAssert.assertThat;

import org.hl7.fhir.r4.model.Address;
import org.hl7.fhir.r4.model.Extension;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.openmrs.Location;
import org.openmrs.module.fhir2.FhirConstants;

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
	public void shouldAddExtensionForAddress1() {
		Location location = new Location();
		location.setAddress1(ADDRESS_1);
		
		assertThat(translator.toFhirResource(location).getExtensionByUrl(FhirConstants.OPENMRS_FHIR_EXT_ADDRESS)
		        .getExtensionByUrl(FhirConstants.OPENMRS_FHIR_EXT_ADDRESS + "#address1"),
		    hasProperty("value", hasProperty("value", equalTo(ADDRESS_1))));
	}
	
	@Test
	public void shouldAddExtensionForAddress2() {
		Location location = new Location();
		location.setAddress2(ADDRESS_2);
		
		assertThat(translator.toFhirResource(location).getExtensionByUrl(FhirConstants.OPENMRS_FHIR_EXT_ADDRESS)
		        .getExtensionByUrl(FhirConstants.OPENMRS_FHIR_EXT_ADDRESS + "#address2"),
		    hasProperty("value", hasProperty("value", equalTo(ADDRESS_2))));
	}
	
	@Test
	public void shouldAddExtensionForAddress3() {
		Location location = new Location();
		location.setAddress3(ADDRESS_3);
		
		assertThat(translator.toFhirResource(location).getExtensionByUrl(FhirConstants.OPENMRS_FHIR_EXT_ADDRESS)
		        .getExtensionByUrl(FhirConstants.OPENMRS_FHIR_EXT_ADDRESS + "#address3"),
		    hasProperty("value", hasProperty("value", equalTo(ADDRESS_3))));
	}
	
	@Test
	public void shouldAddExtensionForAddress4() {
		Location location = new Location();
		location.setAddress4(ADDRESS_4);
		
		assertThat(translator.toFhirResource(location).getExtensionByUrl(FhirConstants.OPENMRS_FHIR_EXT_ADDRESS)
		        .getExtensionByUrl(FhirConstants.OPENMRS_FHIR_EXT_ADDRESS + "#address4"),
		    hasProperty("value", hasProperty("value", equalTo(ADDRESS_4))));
	}
	
	@Test
	public void shouldAddExtensionForAddress5() {
		Location location = new Location();
		location.setAddress5(ADDRESS_5);
		
		assertThat(translator.toFhirResource(location).getExtensionByUrl(FhirConstants.OPENMRS_FHIR_EXT_ADDRESS)
		        .getExtensionByUrl(FhirConstants.OPENMRS_FHIR_EXT_ADDRESS + "#address5"),
		    hasProperty("value", hasProperty("value", equalTo(ADDRESS_5))));
	}
	
	@Test
	public void shouldAddExtensionForAddress6() {
		Location location = new Location();
		location.setAddress6(ADDRESS_6);
		
		assertThat(translator.toFhirResource(location).getExtensionByUrl(FhirConstants.OPENMRS_FHIR_EXT_ADDRESS)
		        .getExtensionByUrl(FhirConstants.OPENMRS_FHIR_EXT_ADDRESS + "#address6"),
		    hasProperty("value", hasProperty("value", equalTo(ADDRESS_6))));
	}
	
	@Test
	public void shouldAddExtensionForAddress7() {
		Location location = new Location();
		location.setAddress7(ADDRESS_7);
		
		assertThat(translator.toFhirResource(location).getExtensionByUrl(FhirConstants.OPENMRS_FHIR_EXT_ADDRESS)
		        .getExtensionByUrl(FhirConstants.OPENMRS_FHIR_EXT_ADDRESS + "#address7"),
		    hasProperty("value", hasProperty("value", equalTo(ADDRESS_7))));
	}
	
	@Test
	public void shouldAddExtensionForAddress8() {
		Location location = new Location();
		location.setAddress8(ADDRESS_8);
		
		assertThat(translator.toFhirResource(location).getExtensionByUrl(FhirConstants.OPENMRS_FHIR_EXT_ADDRESS)
		        .getExtensionByUrl(FhirConstants.OPENMRS_FHIR_EXT_ADDRESS + "#address8"),
		    hasProperty("value", hasProperty("value", equalTo(ADDRESS_8))));
	}
	
	@Test
	public void shouldAddExtensionForAddress9() {
		Location location = new Location();
		location.setAddress9(ADDRESS_9);
		
		assertThat(translator.toFhirResource(location).getExtensionByUrl(FhirConstants.OPENMRS_FHIR_EXT_ADDRESS)
		        .getExtensionByUrl(FhirConstants.OPENMRS_FHIR_EXT_ADDRESS + "#address9"),
		    hasProperty("value", hasProperty("value", equalTo(ADDRESS_9))));
	}
	
	@Test
	public void shouldAddExtensionForAddress10() {
		Location location = new Location();
		location.setAddress10(ADDRESS_10);
		
		assertThat(translator.toFhirResource(location).getExtensionByUrl(FhirConstants.OPENMRS_FHIR_EXT_ADDRESS)
		        .getExtensionByUrl(FhirConstants.OPENMRS_FHIR_EXT_ADDRESS + "#address10"),
		    hasProperty("value", hasProperty("value", equalTo(ADDRESS_10))));
	}
	
	@Test
	public void shouldAddExtensionForAddress11() {
		Location location = new Location();
		location.setAddress11(ADDRESS_11);
		
		assertThat(translator.toFhirResource(location).getExtensionByUrl(FhirConstants.OPENMRS_FHIR_EXT_ADDRESS)
		        .getExtensionByUrl(FhirConstants.OPENMRS_FHIR_EXT_ADDRESS + "#address11"),
		    hasProperty("value", hasProperty("value", equalTo(ADDRESS_11))));
	}
	
	@Test
	public void shouldAddExtensionForAddress12() {
		Location location = new Location();
		location.setAddress12(ADDRESS_12);
		
		assertThat(translator.toFhirResource(location).getExtensionByUrl(FhirConstants.OPENMRS_FHIR_EXT_ADDRESS)
		        .getExtensionByUrl(FhirConstants.OPENMRS_FHIR_EXT_ADDRESS + "#address12"),
		    hasProperty("value", hasProperty("value", equalTo(ADDRESS_12))));
	}
	
	@Test
	public void shouldAddExtensionForAddress13() {
		Location location = new Location();
		location.setAddress13(ADDRESS_13);
		
		assertThat(translator.toFhirResource(location).getExtensionByUrl(FhirConstants.OPENMRS_FHIR_EXT_ADDRESS)
		        .getExtensionByUrl(FhirConstants.OPENMRS_FHIR_EXT_ADDRESS + "#address13"),
		    hasProperty("value", hasProperty("value", equalTo(ADDRESS_13))));
	}
	
	@Test
	public void shouldAddExtensionForAddress14() {
		Location location = new Location();
		location.setAddress14(ADDRESS_14);
		
		assertThat(translator.toFhirResource(location).getExtensionByUrl(FhirConstants.OPENMRS_FHIR_EXT_ADDRESS)
		        .getExtensionByUrl(FhirConstants.OPENMRS_FHIR_EXT_ADDRESS + "#address14"),
		    hasProperty("value", hasProperty("value", equalTo(ADDRESS_14))));
	}
	
	@Test
	public void shouldAddExtensionForAddress15() {
		Location location = new Location();
		location.setAddress15(ADDRESS_15);
		
		assertThat(translator.toFhirResource(location).getExtensionByUrl(FhirConstants.OPENMRS_FHIR_EXT_ADDRESS)
		        .getExtensionByUrl(FhirConstants.OPENMRS_FHIR_EXT_ADDRESS + "#address15"),
		    hasProperty("value", hasProperty("value", equalTo(ADDRESS_15))));
	}
	
	@Test
	public void addAddressExtension_shouldWorkAsExpected() {
		omrsLocation.setAddress1(ADDRESS_1);
		omrsLocation.setAddress2(ADDRESS_2);
		omrsLocation.setAddress3(ADDRESS_3);
		
		Extension extension = translator.toFhirResource(omrsLocation).getExtensionByUrl(
		    FhirConstants.OPENMRS_FHIR_EXT_ADDRESS);
		assertThat(extension.getExtension().size(), greaterThan(1));
		assertThat(extension.getExtension().size(), equalTo(3));
	}
	
	@Test
	public void addAddressComponent_shouldSetAddressesCorrectly() {
		translator.addAddressComponent(omrsLocation, "https://fhir.openmrs.org/ext/address#address1", ADDRESS_1);
		assertNotNull(omrsLocation.getAddress1());
		assertEquals(omrsLocation.getAddress1(), ADDRESS_1);
		
		translator.addAddressComponent(omrsLocation, "https://fhir.openmrs.org/ext/address#address2", ADDRESS_2);
		assertNotNull(omrsLocation.getAddress2());
		assertEquals(omrsLocation.getAddress2(), ADDRESS_2);
		
		translator.addAddressComponent(omrsLocation, "https://fhir.openmrs.org/ext/address#address3", ADDRESS_3);
		assertNotNull(omrsLocation.getAddress3());
		assertEquals(omrsLocation.getAddress3(), ADDRESS_3);
		
		translator.addAddressComponent(omrsLocation, "https://fhir.openmrs.org/ext/address#address4", ADDRESS_4);
		assertNotNull(omrsLocation.getAddress4());
		assertEquals(omrsLocation.getAddress4(), ADDRESS_4);
		
		translator.addAddressComponent(omrsLocation, "https://fhir.openmrs.org/ext/address#address5", ADDRESS_5);
		assertNotNull(omrsLocation.getAddress5());
		assertEquals(omrsLocation.getAddress5(), ADDRESS_5);
		
		translator.addAddressComponent(omrsLocation, "https://fhir.openmrs.org/ext/address#address6", ADDRESS_6);
		assertNotNull(omrsLocation.getAddress6());
		assertEquals(omrsLocation.getAddress6(), ADDRESS_6);
		
		translator.addAddressComponent(omrsLocation, "https://fhir.openmrs.org/ext/address#address7", ADDRESS_7);
		assertNotNull(omrsLocation.getAddress7());
		assertEquals(omrsLocation.getAddress7(), ADDRESS_7);
		
		translator.addAddressComponent(omrsLocation, "https://fhir.openmrs.org/ext/address#address8", ADDRESS_8);
		assertNotNull(omrsLocation.getAddress8());
		assertEquals(omrsLocation.getAddress8(), ADDRESS_8);
		
		translator.addAddressComponent(omrsLocation, "https://fhir.openmrs.org/ext/address#address9", ADDRESS_9);
		assertNotNull(omrsLocation.getAddress9());
		assertEquals(omrsLocation.getAddress9(), ADDRESS_9);
		
		translator.addAddressComponent(omrsLocation, "https://fhir.openmrs.org/ext/address#address10", ADDRESS_10);
		assertNotNull(omrsLocation.getAddress10());
		assertEquals(omrsLocation.getAddress10(), ADDRESS_10);
		
		translator.addAddressComponent(omrsLocation, "https://fhir.openmrs.org/ext/address#address11", ADDRESS_11);
		assertNotNull(omrsLocation.getAddress11());
		assertEquals(omrsLocation.getAddress11(), ADDRESS_11);
		
		translator.addAddressComponent(omrsLocation, "https://fhir.openmrs.org/ext/address#address12", ADDRESS_12);
		assertNotNull(omrsLocation.getAddress12());
		assertEquals(omrsLocation.getAddress12(), ADDRESS_12);
		
		translator.addAddressComponent(omrsLocation, "https://fhir.openmrs.org/ext/address#address13", ADDRESS_13);
		assertNotNull(omrsLocation.getAddress13());
		assertEquals(omrsLocation.getAddress13(), ADDRESS_13);
		
		translator.addAddressComponent(omrsLocation, "https://fhir.openmrs.org/ext/address#address14", ADDRESS_14);
		assertNotNull(omrsLocation.getAddress14());
		assertEquals(omrsLocation.getAddress14(), ADDRESS_14);
		
		translator.addAddressComponent(omrsLocation, "https://fhir.openmrs.org/ext/address#address15", ADDRESS_15);
		assertNotNull(omrsLocation.getAddress15());
		assertEquals(omrsLocation.getAddress15(), ADDRESS_15);
		
	}
}
