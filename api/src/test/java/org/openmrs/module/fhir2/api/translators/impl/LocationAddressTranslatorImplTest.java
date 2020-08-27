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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

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
		assertThat(address, notNullValue());
		assertThat(address.getCity(), notNullValue());
		assertThat(address.getCity(), equalTo(CITY));
	}
	
	@Test
	public void shouldTranslateLocationProvinceToAddressProvince() {
		omrsLocation.setStateProvince(STATE_PROVINCE);
		org.hl7.fhir.r4.model.Address address = translator.toFhirResource(omrsLocation);
		assertThat(address, notNullValue());
		assertThat(address.getState(), notNullValue());
		assertThat(address.getState(), equalTo(STATE_PROVINCE));
		
	}
	
	@Test
	public void shouldTranslateLocationCountryToAddressCountry() {
		omrsLocation.setCountry(COUNTRY);
		org.hl7.fhir.r4.model.Address address = translator.toFhirResource(omrsLocation);
		assertThat(address, notNullValue());
		assertThat(address.getCountry(), notNullValue());
		assertThat(address.getCountry(), equalTo(COUNTRY));
		
	}
	
	@Test
	public void shouldTranslateLocationPostalCodeToAddressCode() {
		omrsLocation.setPostalCode(POSTAL_CODE);
		org.hl7.fhir.r4.model.Address address = translator.toFhirResource(omrsLocation);
		assertThat(address, notNullValue());
		assertThat(address.getPostalCode(), notNullValue());
		assertThat(address.getPostalCode(), equalTo(POSTAL_CODE));
		
	}
	
	@Test
	public void toFhirResource_shouldReturnNullIfCalledWithoutLocation() {
		Address address = translator.toFhirResource(null);
		assertThat(address, nullValue());
	}
	
	@Test
	public void shouldTranslateAddressCityToLocationCity() {
		org.hl7.fhir.r4.model.Address address = new Address();
		address.setCity(CITY);
		
		omrsLocation = translator.toOpenmrsType(new Location(), address);
		
		assertThat(omrsLocation, notNullValue());
		assertThat(omrsLocation.getCityVillage(), notNullValue());
		assertThat(omrsLocation.getCityVillage(), equalTo(CITY));
	}
	
	@Test
	public void shouldTranslateAddressStateToLocationState() {
		org.hl7.fhir.r4.model.Address address = new Address();
		address.setState(STATE_PROVINCE);
		
		omrsLocation = translator.toOpenmrsType(new Location(), address);
		
		assertThat(omrsLocation, notNullValue());
		assertThat(omrsLocation.getStateProvince(), notNullValue());
		assertThat(omrsLocation.getStateProvince(), equalTo(STATE_PROVINCE));
	}
	
	@Test
	public void shouldTranslateAddressCountryToLocationCountry() {
		org.hl7.fhir.r4.model.Address address = new Address();
		address.setCountry(COUNTRY);
		
		omrsLocation = translator.toOpenmrsType(new Location(), address);
		
		assertThat(omrsLocation, notNullValue());
		assertThat(omrsLocation.getCountry(), notNullValue());
		assertThat(omrsLocation.getCountry(), equalTo(COUNTRY));
	}
	
	@Test
	public void shouldTranslateAddressPostalCodeToLocationPostalCode() {
		org.hl7.fhir.r4.model.Address address = new Address();
		address.setPostalCode(POSTAL_CODE);
		
		omrsLocation = translator.toOpenmrsType(new Location(), address);
		
		assertThat(omrsLocation, notNullValue());
		assertThat(omrsLocation.getPostalCode(), notNullValue());
		assertThat(omrsLocation.getPostalCode(), equalTo(POSTAL_CODE));
	}
	
	@Test
	public void shouldAddExtensionForAddress1() {
		Location location = new Location();
		location.setAddress1(ADDRESS_1);
		
		assertThat(
		    translator.toFhirResource(location).getExtensionByUrl(FhirConstants.OPENMRS_FHIR_EXT_ADDRESS)
		            .getExtensionByUrl(FhirConstants.OPENMRS_FHIR_EXT_ADDRESS + "#address1"),
		    hasProperty("value", hasProperty("value", equalTo(ADDRESS_1))));
	}
	
	@Test
	public void shouldAddExtensionForAddress2() {
		Location location = new Location();
		location.setAddress2(ADDRESS_2);
		
		assertThat(
		    translator.toFhirResource(location).getExtensionByUrl(FhirConstants.OPENMRS_FHIR_EXT_ADDRESS)
		            .getExtensionByUrl(FhirConstants.OPENMRS_FHIR_EXT_ADDRESS + "#address2"),
		    hasProperty("value", hasProperty("value", equalTo(ADDRESS_2))));
	}
	
	@Test
	public void shouldAddExtensionForAddress3() {
		Location location = new Location();
		location.setAddress3(ADDRESS_3);
		
		assertThat(
		    translator.toFhirResource(location).getExtensionByUrl(FhirConstants.OPENMRS_FHIR_EXT_ADDRESS)
		            .getExtensionByUrl(FhirConstants.OPENMRS_FHIR_EXT_ADDRESS + "#address3"),
		    hasProperty("value", hasProperty("value", equalTo(ADDRESS_3))));
	}
	
	@Test
	public void shouldAddExtensionForAddress4() {
		Location location = new Location();
		location.setAddress4(ADDRESS_4);
		
		assertThat(
		    translator.toFhirResource(location).getExtensionByUrl(FhirConstants.OPENMRS_FHIR_EXT_ADDRESS)
		            .getExtensionByUrl(FhirConstants.OPENMRS_FHIR_EXT_ADDRESS + "#address4"),
		    hasProperty("value", hasProperty("value", equalTo(ADDRESS_4))));
	}
	
	@Test
	public void shouldAddExtensionForAddress5() {
		Location location = new Location();
		location.setAddress5(ADDRESS_5);
		
		assertThat(
		    translator.toFhirResource(location).getExtensionByUrl(FhirConstants.OPENMRS_FHIR_EXT_ADDRESS)
		            .getExtensionByUrl(FhirConstants.OPENMRS_FHIR_EXT_ADDRESS + "#address5"),
		    hasProperty("value", hasProperty("value", equalTo(ADDRESS_5))));
	}
	
	@Test
	public void shouldAddExtensionForAddress6() {
		Location location = new Location();
		location.setAddress6(ADDRESS_6);
		
		assertThat(
		    translator.toFhirResource(location).getExtensionByUrl(FhirConstants.OPENMRS_FHIR_EXT_ADDRESS)
		            .getExtensionByUrl(FhirConstants.OPENMRS_FHIR_EXT_ADDRESS + "#address6"),
		    hasProperty("value", hasProperty("value", equalTo(ADDRESS_6))));
	}
	
	@Test
	public void shouldAddExtensionForAddress7() {
		Location location = new Location();
		location.setAddress7(ADDRESS_7);
		
		assertThat(
		    translator.toFhirResource(location).getExtensionByUrl(FhirConstants.OPENMRS_FHIR_EXT_ADDRESS)
		            .getExtensionByUrl(FhirConstants.OPENMRS_FHIR_EXT_ADDRESS + "#address7"),
		    hasProperty("value", hasProperty("value", equalTo(ADDRESS_7))));
	}
	
	@Test
	public void shouldAddExtensionForAddress8() {
		Location location = new Location();
		location.setAddress8(ADDRESS_8);
		
		assertThat(
		    translator.toFhirResource(location).getExtensionByUrl(FhirConstants.OPENMRS_FHIR_EXT_ADDRESS)
		            .getExtensionByUrl(FhirConstants.OPENMRS_FHIR_EXT_ADDRESS + "#address8"),
		    hasProperty("value", hasProperty("value", equalTo(ADDRESS_8))));
	}
	
	@Test
	public void shouldAddExtensionForAddress9() {
		Location location = new Location();
		location.setAddress9(ADDRESS_9);
		
		assertThat(
		    translator.toFhirResource(location).getExtensionByUrl(FhirConstants.OPENMRS_FHIR_EXT_ADDRESS)
		            .getExtensionByUrl(FhirConstants.OPENMRS_FHIR_EXT_ADDRESS + "#address9"),
		    hasProperty("value", hasProperty("value", equalTo(ADDRESS_9))));
	}
	
	@Test
	public void shouldAddExtensionForAddress10() {
		Location location = new Location();
		location.setAddress10(ADDRESS_10);
		
		assertThat(
		    translator.toFhirResource(location).getExtensionByUrl(FhirConstants.OPENMRS_FHIR_EXT_ADDRESS)
		            .getExtensionByUrl(FhirConstants.OPENMRS_FHIR_EXT_ADDRESS + "#address10"),
		    hasProperty("value", hasProperty("value", equalTo(ADDRESS_10))));
	}
	
	@Test
	public void shouldAddExtensionForAddress11() {
		Location location = new Location();
		location.setAddress11(ADDRESS_11);
		
		assertThat(
		    translator.toFhirResource(location).getExtensionByUrl(FhirConstants.OPENMRS_FHIR_EXT_ADDRESS)
		            .getExtensionByUrl(FhirConstants.OPENMRS_FHIR_EXT_ADDRESS + "#address11"),
		    hasProperty("value", hasProperty("value", equalTo(ADDRESS_11))));
	}
	
	@Test
	public void shouldAddExtensionForAddress12() {
		Location location = new Location();
		location.setAddress12(ADDRESS_12);
		
		assertThat(
		    translator.toFhirResource(location).getExtensionByUrl(FhirConstants.OPENMRS_FHIR_EXT_ADDRESS)
		            .getExtensionByUrl(FhirConstants.OPENMRS_FHIR_EXT_ADDRESS + "#address12"),
		    hasProperty("value", hasProperty("value", equalTo(ADDRESS_12))));
	}
	
	@Test
	public void shouldAddExtensionForAddress13() {
		Location location = new Location();
		location.setAddress13(ADDRESS_13);
		
		assertThat(
		    translator.toFhirResource(location).getExtensionByUrl(FhirConstants.OPENMRS_FHIR_EXT_ADDRESS)
		            .getExtensionByUrl(FhirConstants.OPENMRS_FHIR_EXT_ADDRESS + "#address13"),
		    hasProperty("value", hasProperty("value", equalTo(ADDRESS_13))));
	}
	
	@Test
	public void shouldAddExtensionForAddress14() {
		Location location = new Location();
		location.setAddress14(ADDRESS_14);
		
		assertThat(
		    translator.toFhirResource(location).getExtensionByUrl(FhirConstants.OPENMRS_FHIR_EXT_ADDRESS)
		            .getExtensionByUrl(FhirConstants.OPENMRS_FHIR_EXT_ADDRESS + "#address14"),
		    hasProperty("value", hasProperty("value", equalTo(ADDRESS_14))));
	}
	
	@Test
	public void shouldAddExtensionForAddress15() {
		Location location = new Location();
		location.setAddress15(ADDRESS_15);
		
		assertThat(
		    translator.toFhirResource(location).getExtensionByUrl(FhirConstants.OPENMRS_FHIR_EXT_ADDRESS)
		            .getExtensionByUrl(FhirConstants.OPENMRS_FHIR_EXT_ADDRESS + "#address15"),
		    hasProperty("value", hasProperty("value", equalTo(ADDRESS_15))));
	}
	
	@Test
	public void addAddressExtension_shouldWorkAsExpected() {
		omrsLocation.setAddress1(ADDRESS_1);
		omrsLocation.setAddress2(ADDRESS_2);
		omrsLocation.setAddress3(ADDRESS_3);
		
		Extension extension = translator.toFhirResource(omrsLocation)
		        .getExtensionByUrl(FhirConstants.OPENMRS_FHIR_EXT_ADDRESS);
		assertThat(extension.getExtension().size(), greaterThan(1));
		assertThat(extension.getExtension().size(), equalTo(3));
	}
	
	@Test
	public void addAddressComponent_shouldSetAddressesCorrectly() {
		translator.addAddressComponent(omrsLocation, FhirConstants.OPENMRS_FHIR_EXT_ADDRESS + "#address1", ADDRESS_1);
		assertThat(omrsLocation.getAddress1(), notNullValue());
		assertThat(omrsLocation.getAddress1(), equalTo(ADDRESS_1));
		
		translator.addAddressComponent(omrsLocation, FhirConstants.OPENMRS_FHIR_EXT_ADDRESS + "#address2", ADDRESS_2);
		assertThat(omrsLocation.getAddress2(), notNullValue());
		assertThat(omrsLocation.getAddress2(), equalTo(ADDRESS_2));
		
		translator.addAddressComponent(omrsLocation, FhirConstants.OPENMRS_FHIR_EXT_ADDRESS + "#address3", ADDRESS_3);
		assertThat(omrsLocation.getAddress3(), notNullValue());
		assertThat(omrsLocation.getAddress3(), equalTo(ADDRESS_3));
		
		translator.addAddressComponent(omrsLocation, FhirConstants.OPENMRS_FHIR_EXT_ADDRESS + "#address4", ADDRESS_4);
		assertThat(omrsLocation.getAddress4(), notNullValue());
		assertThat(omrsLocation.getAddress4(), equalTo(ADDRESS_4));
		
		translator.addAddressComponent(omrsLocation, FhirConstants.OPENMRS_FHIR_EXT_ADDRESS + "#address5", ADDRESS_5);
		assertThat(omrsLocation.getAddress5(), notNullValue());
		assertThat(omrsLocation.getAddress5(), equalTo(ADDRESS_5));
		
		translator.addAddressComponent(omrsLocation, FhirConstants.OPENMRS_FHIR_EXT_ADDRESS + "#address6", ADDRESS_6);
		assertThat(omrsLocation.getAddress6(), notNullValue());
		assertThat(omrsLocation.getAddress6(), equalTo(ADDRESS_6));
		
		translator.addAddressComponent(omrsLocation, FhirConstants.OPENMRS_FHIR_EXT_ADDRESS + "#address7", ADDRESS_7);
		assertThat(omrsLocation.getAddress7(), notNullValue());
		assertThat(omrsLocation.getAddress7(), equalTo(ADDRESS_7));
		
		translator.addAddressComponent(omrsLocation, FhirConstants.OPENMRS_FHIR_EXT_ADDRESS + "#address8", ADDRESS_8);
		assertThat(omrsLocation.getAddress8(), notNullValue());
		assertThat(omrsLocation.getAddress8(), equalTo(ADDRESS_8));
		
		translator.addAddressComponent(omrsLocation, FhirConstants.OPENMRS_FHIR_EXT_ADDRESS + "#address9", ADDRESS_9);
		assertThat(omrsLocation.getAddress9(), notNullValue());
		assertThat(omrsLocation.getAddress9(), equalTo(ADDRESS_9));
		
		translator.addAddressComponent(omrsLocation, FhirConstants.OPENMRS_FHIR_EXT_ADDRESS + "#address10", ADDRESS_10);
		assertThat(omrsLocation.getAddress10(), notNullValue());
		assertThat(omrsLocation.getAddress10(), equalTo(ADDRESS_10));
		
		translator.addAddressComponent(omrsLocation, FhirConstants.OPENMRS_FHIR_EXT_ADDRESS + "#address11", ADDRESS_11);
		assertThat(omrsLocation.getAddress11(), notNullValue());
		assertThat(omrsLocation.getAddress11(), equalTo(ADDRESS_11));
		
		translator.addAddressComponent(omrsLocation, FhirConstants.OPENMRS_FHIR_EXT_ADDRESS + "#address12", ADDRESS_12);
		assertThat(omrsLocation.getAddress12(), notNullValue());
		assertThat(omrsLocation.getAddress12(), equalTo(ADDRESS_12));
		
		translator.addAddressComponent(omrsLocation, FhirConstants.OPENMRS_FHIR_EXT_ADDRESS + "#address13", ADDRESS_13);
		assertThat(omrsLocation.getAddress13(), notNullValue());
		assertThat(omrsLocation.getAddress13(), equalTo(ADDRESS_13));
		
		translator.addAddressComponent(omrsLocation, FhirConstants.OPENMRS_FHIR_EXT_ADDRESS + "#address14", ADDRESS_14);
		assertThat(omrsLocation.getAddress14(), notNullValue());
		assertThat(omrsLocation.getAddress14(), equalTo(ADDRESS_14));
		
		translator.addAddressComponent(omrsLocation, FhirConstants.OPENMRS_FHIR_EXT_ADDRESS + "#address15", ADDRESS_15);
		assertThat(omrsLocation.getAddress15(), notNullValue());
		assertThat(omrsLocation.getAddress15(), equalTo(ADDRESS_15));
		
	}
}
