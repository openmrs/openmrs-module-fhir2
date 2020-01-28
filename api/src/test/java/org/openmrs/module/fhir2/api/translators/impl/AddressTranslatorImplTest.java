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
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

import org.hl7.fhir.r4.model.Address;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.PersonAddress;

public class AddressTranslatorImplTest {
	
	private static final String PERSON_ADDRESS_UUID = "123456-abcdef-123456";
	
	private static final String CITY = "Maputo";
	
	private static final String PROVINCE = "Maputo";
	
	private static final String COUNTRY = "MOÇAMBIQUE";
	
	private static final String POSTAL_CODE = "1100";
	
	private AddressTranslatorImpl addressTranslator;
	
	@Before
	public void setup() {
		addressTranslator = new AddressTranslatorImpl();
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
	
}
