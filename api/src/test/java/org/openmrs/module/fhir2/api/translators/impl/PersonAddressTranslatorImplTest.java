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
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.hl7.fhir.r4.model.Address;
import org.hl7.fhir.r4.model.Extension;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.PersonAddress;
import org.openmrs.module.fhir2.FhirConstants;

public class PersonAddressTranslatorImplTest {
	
	private static final String PERSON_ADDRESS_UUID = "123456-abcdef-123456";
	
	private static final String CITY = "Maputo";
	
	private static final String PROVINCE = "Maputo";
	
	private static final String COUNTRY = "MOÇAMBIQUE";
	
	private static final String POSTAL_CODE = "1100";
	
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
	
	private PersonAddressTranslatorImpl addressTranslator;
	
	@Before
	public void setup() {
		addressTranslator = new PersonAddressTranslatorImpl();
	}
	
	@Test
	public void shouldConvertPersonAddressToAddress() {
		PersonAddress address = new PersonAddress();
		address.setUuid(PERSON_ADDRESS_UUID);
		
		Address result = addressTranslator.toFhirResource(address);
		assertThat(result, notNullValue());
		assertThat(result.getId(), equalTo(PERSON_ADDRESS_UUID));
	}
	
	@Test
	public void shouldConvertCityVillageToCity() {
		PersonAddress address = new PersonAddress();
		address.setCityVillage(CITY);
		
		assertThat(addressTranslator.toFhirResource(address).getCity(), equalTo(CITY));
	}
	
	@Test
	public void shouldConvertStateProvinceToState() {
		PersonAddress address = new PersonAddress();
		address.setStateProvince(PROVINCE);
		
		assertThat(addressTranslator.toFhirResource(address).getState(), equalTo(PROVINCE));
	}
	
	@Test
	public void shouldConvertOpenmrsCountryToCountry() {
		PersonAddress address = new PersonAddress();
		address.setCountry(COUNTRY);
		
		assertThat(addressTranslator.toFhirResource(address).getCountry(), equalTo(COUNTRY));
	}
	
	@Test
	public void shouldConvertOpenmrsPostalCodeToPostalCode() {
		PersonAddress address = new PersonAddress();
		address.setPostalCode(POSTAL_CODE);
		
		assertThat(addressTranslator.toFhirResource(address).getPostalCode(), equalTo(POSTAL_CODE));
	}
	
	@Test
	public void shouldSetUseToHomeIfPreferred() {
		PersonAddress address = new PersonAddress();
		address.setPreferred(true);
		
		assertThat(addressTranslator.toFhirResource(address).getUse(), equalTo(Address.AddressUse.HOME));
	}
	
	@Test
	public void shouldSetUseToOldIfNotPreferred() {
		PersonAddress address = new PersonAddress();
		address.setPreferred(false);
		
		assertThat(addressTranslator.toFhirResource(address).getUse(), equalTo(Address.AddressUse.OLD));
	}
	
	@Test
	public void shouldConvertAddressToPersonAddress() {
		Address address = new Address();
		address.setId(PERSON_ADDRESS_UUID);
		
		PersonAddress result = addressTranslator.toOpenmrsType(address);
		assertThat(result, notNullValue());
		assertThat(result.getUuid(), equalTo(PERSON_ADDRESS_UUID));
	}
	
	@Test
	public void shouldConvertCityToCityVillage() {
		Address address = new Address();
		address.setCity(CITY);
		
		assertThat(addressTranslator.toOpenmrsType(address).getCityVillage(), equalTo(CITY));
	}
	
	@Test
	public void shouldConvertStateToStateProvince() {
		Address address = new Address();
		address.setState(PROVINCE);
		
		assertThat(addressTranslator.toOpenmrsType(address).getStateProvince(), equalTo(PROVINCE));
	}
	
	@Test
	public void shouldConvertFhirCountryToCountry() {
		Address address = new Address();
		address.setCountry(COUNTRY);
		
		assertThat(addressTranslator.toOpenmrsType(address).getCountry(), equalTo(COUNTRY));
	}
	
	@Test
	public void shouldConvertFhirPostalCodeToPostalCode() {
		Address address = new Address();
		address.setPostalCode(POSTAL_CODE);
		
		assertThat(addressTranslator.toOpenmrsType(address).getPostalCode(), equalTo(POSTAL_CODE));
	}
	
	@Test
	public void shouldConvertHomeAddressToPreferred() {
		Address address = new Address();
		address.setUse(Address.AddressUse.HOME);
		
		assertThat(addressTranslator.toOpenmrsType(address).getPreferred(), is(true));
	}
	
	@Test
	public void shouldConvertOldAddressToNotPreferred() {
		Address address = new Address();
		address.setUse(Address.AddressUse.OLD);
		
		assertThat(addressTranslator.toOpenmrsType(address).getPreferred(), is(false));
	}
	
	@Test
	public void shouldAddExtensionForAddress1() {
		PersonAddress personAddress = new PersonAddress();
		personAddress.setAddress1(ADDRESS_1);
		
		assertThat(
		    addressTranslator.toFhirResource(personAddress).getExtensionByUrl(FhirConstants.OPENMRS_FHIR_EXT_ADDRESS)
		            .getExtensionByUrl(FhirConstants.OPENMRS_FHIR_EXT_ADDRESS + "#address1"),
		    hasProperty("value", hasProperty("value", equalTo(ADDRESS_1))));
	}
	
	@Test
	public void shouldAddExtensionForAddress2() {
		PersonAddress personAddress = new PersonAddress();
		personAddress.setAddress2(ADDRESS_2);
		
		assertThat(
		    addressTranslator.toFhirResource(personAddress).getExtensionByUrl(FhirConstants.OPENMRS_FHIR_EXT_ADDRESS)
		            .getExtensionByUrl(FhirConstants.OPENMRS_FHIR_EXT_ADDRESS + "#address2"),
		    hasProperty("value", hasProperty("value", equalTo(ADDRESS_2))));
	}
	
	@Test
	public void shouldAddExtensionForAddress3() {
		PersonAddress personAddress = new PersonAddress();
		personAddress.setAddress3(ADDRESS_3);
		
		assertThat(
		    addressTranslator.toFhirResource(personAddress).getExtensionByUrl(FhirConstants.OPENMRS_FHIR_EXT_ADDRESS)
		            .getExtensionByUrl(FhirConstants.OPENMRS_FHIR_EXT_ADDRESS + "#address3"),
		    hasProperty("value", hasProperty("value", equalTo(ADDRESS_3))));
	}
	
	@Test
	public void shouldAddExtensionForAddress4() {
		PersonAddress personAddress = new PersonAddress();
		personAddress.setAddress4(ADDRESS_4);
		
		assertThat(
		    addressTranslator.toFhirResource(personAddress).getExtensionByUrl(FhirConstants.OPENMRS_FHIR_EXT_ADDRESS)
		            .getExtensionByUrl(FhirConstants.OPENMRS_FHIR_EXT_ADDRESS + "#address4"),
		    hasProperty("value", hasProperty("value", equalTo(ADDRESS_4))));
	}
	
	@Test
	public void shouldAddExtensionForAddress5() {
		PersonAddress personAddress = new PersonAddress();
		personAddress.setAddress5(ADDRESS_5);
		
		assertThat(
		    addressTranslator.toFhirResource(personAddress).getExtensionByUrl(FhirConstants.OPENMRS_FHIR_EXT_ADDRESS)
		            .getExtensionByUrl(FhirConstants.OPENMRS_FHIR_EXT_ADDRESS + "#address5"),
		    hasProperty("value", hasProperty("value", equalTo(ADDRESS_5))));
	}
	
	@Test
	public void shouldAddExtensionForAddress6() {
		PersonAddress personAddress = new PersonAddress();
		personAddress.setAddress6(ADDRESS_6);
		
		assertThat(
		    addressTranslator.toFhirResource(personAddress).getExtensionByUrl(FhirConstants.OPENMRS_FHIR_EXT_ADDRESS)
		            .getExtensionByUrl(FhirConstants.OPENMRS_FHIR_EXT_ADDRESS + "#address6"),
		    hasProperty("value", hasProperty("value", equalTo(ADDRESS_6))));
	}
	
	@Test
	public void shouldAddExtensionForAddress7() {
		PersonAddress personAddress = new PersonAddress();
		personAddress.setAddress7(ADDRESS_7);
		
		assertThat(
		    addressTranslator.toFhirResource(personAddress).getExtensionByUrl(FhirConstants.OPENMRS_FHIR_EXT_ADDRESS)
		            .getExtensionByUrl(FhirConstants.OPENMRS_FHIR_EXT_ADDRESS + "#address7"),
		    hasProperty("value", hasProperty("value", equalTo(ADDRESS_7))));
	}
	
	@Test
	public void shouldAddExtensionForAddress8() {
		PersonAddress personAddress = new PersonAddress();
		personAddress.setAddress8(ADDRESS_8);
		
		assertThat(
		    addressTranslator.toFhirResource(personAddress).getExtensionByUrl(FhirConstants.OPENMRS_FHIR_EXT_ADDRESS)
		            .getExtensionByUrl(FhirConstants.OPENMRS_FHIR_EXT_ADDRESS + "#address8"),
		    hasProperty("value", hasProperty("value", equalTo(ADDRESS_8))));
	}
	
	@Test
	public void shouldAddExtensionForAddress9() {
		PersonAddress personAddress = new PersonAddress();
		personAddress.setAddress9(ADDRESS_9);
		
		assertThat(
		    addressTranslator.toFhirResource(personAddress).getExtensionByUrl(FhirConstants.OPENMRS_FHIR_EXT_ADDRESS)
		            .getExtensionByUrl(FhirConstants.OPENMRS_FHIR_EXT_ADDRESS + "#address9"),
		    hasProperty("value", hasProperty("value", equalTo(ADDRESS_9))));
	}
	
	@Test
	public void shouldAddExtensionForAddress10() {
		PersonAddress personAddress = new PersonAddress();
		personAddress.setAddress10(ADDRESS_10);
		
		assertThat(
		    addressTranslator.toFhirResource(personAddress).getExtensionByUrl(FhirConstants.OPENMRS_FHIR_EXT_ADDRESS)
		            .getExtensionByUrl(FhirConstants.OPENMRS_FHIR_EXT_ADDRESS + "#address10"),
		    hasProperty("value", hasProperty("value", equalTo(ADDRESS_10))));
	}
	
	@Test
	public void shouldAddExtensionForAddress11() {
		PersonAddress personAddress = new PersonAddress();
		personAddress.setAddress11(ADDRESS_11);
		
		assertThat(
		    addressTranslator.toFhirResource(personAddress).getExtensionByUrl(FhirConstants.OPENMRS_FHIR_EXT_ADDRESS)
		            .getExtensionByUrl(FhirConstants.OPENMRS_FHIR_EXT_ADDRESS + "#address11"),
		    hasProperty("value", hasProperty("value", equalTo(ADDRESS_11))));
	}
	
	@Test
	public void shouldAddExtensionForAddress12() {
		PersonAddress personAddress = new PersonAddress();
		personAddress.setAddress12(ADDRESS_12);
		
		assertThat(
		    addressTranslator.toFhirResource(personAddress).getExtensionByUrl(FhirConstants.OPENMRS_FHIR_EXT_ADDRESS)
		            .getExtensionByUrl(FhirConstants.OPENMRS_FHIR_EXT_ADDRESS + "#address12"),
		    hasProperty("value", hasProperty("value", equalTo(ADDRESS_12))));
	}
	
	@Test
	public void shouldAddExtensionForAddress13() {
		PersonAddress personAddress = new PersonAddress();
		personAddress.setAddress13(ADDRESS_13);
		
		assertThat(
		    addressTranslator.toFhirResource(personAddress).getExtensionByUrl(FhirConstants.OPENMRS_FHIR_EXT_ADDRESS)
		            .getExtensionByUrl(FhirConstants.OPENMRS_FHIR_EXT_ADDRESS + "#address13"),
		    hasProperty("value", hasProperty("value", equalTo(ADDRESS_13))));
	}
	
	@Test
	public void shouldAddExtensionForAddress14() {
		PersonAddress personAddress = new PersonAddress();
		personAddress.setAddress14(ADDRESS_14);
		
		assertThat(
		    addressTranslator.toFhirResource(personAddress).getExtensionByUrl(FhirConstants.OPENMRS_FHIR_EXT_ADDRESS)
		            .getExtensionByUrl(FhirConstants.OPENMRS_FHIR_EXT_ADDRESS + "#address14"),
		    hasProperty("value", hasProperty("value", equalTo(ADDRESS_14))));
	}
	
	@Test
	public void shouldAddExtensionForAddress15() {
		PersonAddress personAddress = new PersonAddress();
		personAddress.setAddress15(ADDRESS_15);
		
		assertThat(
		    addressTranslator.toFhirResource(personAddress).getExtensionByUrl(FhirConstants.OPENMRS_FHIR_EXT_ADDRESS)
		            .getExtensionByUrl(FhirConstants.OPENMRS_FHIR_EXT_ADDRESS + "#address15"),
		    hasProperty("value", hasProperty("value", equalTo(ADDRESS_15))));
	}
	
	@Test
	public void addAddressExtension_shouldWorkAsExpected() {
		PersonAddress personAddress = new PersonAddress();
		
		personAddress.setAddress1(ADDRESS_1);
		personAddress.setAddress2(ADDRESS_2);
		personAddress.setAddress3(ADDRESS_3);
		
		Extension extension = addressTranslator.toFhirResource(personAddress)
		        .getExtensionByUrl(FhirConstants.OPENMRS_FHIR_EXT_ADDRESS);
		assertThat(extension.getExtension().size(), greaterThan(1));
		assertThat(extension.getExtension().size(), equalTo(3));
	}
	
	@Test
	public void addAddressComponent_shouldSetAddressesCorrectly() {
		PersonAddress personAddress = new PersonAddress();
		
		addressTranslator.addAddressComponent(personAddress, FhirConstants.OPENMRS_FHIR_EXT_ADDRESS + "#address1",
		    ADDRESS_1);
		assertNotNull(personAddress.getAddress1());
		assertEquals(personAddress.getAddress1(), ADDRESS_1);
		
		addressTranslator.addAddressComponent(personAddress, FhirConstants.OPENMRS_FHIR_EXT_ADDRESS + "#address2",
		    ADDRESS_2);
		assertNotNull(personAddress.getAddress2());
		assertEquals(personAddress.getAddress2(), ADDRESS_2);
		
		addressTranslator.addAddressComponent(personAddress, FhirConstants.OPENMRS_FHIR_EXT_ADDRESS + "#address3",
		    ADDRESS_3);
		assertNotNull(personAddress.getAddress3());
		assertEquals(personAddress.getAddress3(), ADDRESS_3);
		
		addressTranslator.addAddressComponent(personAddress, FhirConstants.OPENMRS_FHIR_EXT_ADDRESS + "#address4",
		    ADDRESS_4);
		assertNotNull(personAddress.getAddress4());
		assertEquals(personAddress.getAddress4(), ADDRESS_4);
		
		addressTranslator.addAddressComponent(personAddress, FhirConstants.OPENMRS_FHIR_EXT_ADDRESS + "#address5",
		    ADDRESS_5);
		assertNotNull(personAddress.getAddress5());
		assertEquals(personAddress.getAddress5(), ADDRESS_5);
		
		addressTranslator.addAddressComponent(personAddress, FhirConstants.OPENMRS_FHIR_EXT_ADDRESS + "#address6",
		    ADDRESS_6);
		assertNotNull(personAddress.getAddress6());
		assertEquals(personAddress.getAddress6(), ADDRESS_6);
		
		addressTranslator.addAddressComponent(personAddress, FhirConstants.OPENMRS_FHIR_EXT_ADDRESS + "#address7",
		    ADDRESS_7);
		assertNotNull(personAddress.getAddress7());
		assertEquals(personAddress.getAddress7(), ADDRESS_7);
		
		addressTranslator.addAddressComponent(personAddress, FhirConstants.OPENMRS_FHIR_EXT_ADDRESS + "#address8",
		    ADDRESS_8);
		assertNotNull(personAddress.getAddress8());
		assertEquals(personAddress.getAddress8(), ADDRESS_8);
		
		addressTranslator.addAddressComponent(personAddress, FhirConstants.OPENMRS_FHIR_EXT_ADDRESS + "#address9",
		    ADDRESS_9);
		assertNotNull(personAddress.getAddress9());
		assertEquals(personAddress.getAddress9(), ADDRESS_9);
		
		addressTranslator.addAddressComponent(personAddress, FhirConstants.OPENMRS_FHIR_EXT_ADDRESS + "#address10",
		    ADDRESS_10);
		assertNotNull(personAddress.getAddress10());
		assertEquals(personAddress.getAddress10(), ADDRESS_10);
		
		addressTranslator.addAddressComponent(personAddress, FhirConstants.OPENMRS_FHIR_EXT_ADDRESS + "#address11",
		    ADDRESS_11);
		assertNotNull(personAddress.getAddress11());
		assertEquals(personAddress.getAddress11(), ADDRESS_11);
		
		addressTranslator.addAddressComponent(personAddress, FhirConstants.OPENMRS_FHIR_EXT_ADDRESS + "#address12",
		    ADDRESS_12);
		assertNotNull(personAddress.getAddress12());
		assertEquals(personAddress.getAddress12(), ADDRESS_12);
		
		addressTranslator.addAddressComponent(personAddress, FhirConstants.OPENMRS_FHIR_EXT_ADDRESS + "#address13",
		    ADDRESS_13);
		assertNotNull(personAddress.getAddress13());
		assertEquals(personAddress.getAddress13(), ADDRESS_13);
		
		addressTranslator.addAddressComponent(personAddress, FhirConstants.OPENMRS_FHIR_EXT_ADDRESS + "#address14",
		    ADDRESS_14);
		assertNotNull(personAddress.getAddress14());
		assertEquals(personAddress.getAddress14(), ADDRESS_14);
		
		addressTranslator.addAddressComponent(personAddress, FhirConstants.OPENMRS_FHIR_EXT_ADDRESS + "#address15",
		    ADDRESS_15);
		assertNotNull(personAddress.getAddress15());
		assertEquals(personAddress.getAddress15(), ADDRESS_15);
		
	}
}
