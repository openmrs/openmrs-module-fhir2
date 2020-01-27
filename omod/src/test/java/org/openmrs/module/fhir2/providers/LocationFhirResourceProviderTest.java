/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.fhir2.providers;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.mockito.Mockito.when;

import java.util.Collections;

import ca.uhn.fhir.rest.param.StringParam;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import org.hl7.fhir.r4.model.Address;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.Location;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openmrs.module.fhir2.api.FhirLocationService;

@RunWith(MockitoJUnitRunner.class)
public class LocationFhirResourceProviderTest {

	private static final String LOCATION_UUID = "123xx34-623hh34-22hj89-23hjy5";

	private static final String WRONG_LOCATION_UUID = "c3467w-hi4jer83-56hj34-23hjy5";

	private static final String LOCATION_NAME = "chulaimbo";

	private static final String WRONG_LOCATION_NAME = "wrong location name";

	private static final String CITY = "kakamega";

	private static final String WRONG_CITY = "kakamega";

	private static final String COUNTRY = "Kenya";

	private static final String WRONG_COUNTRY = "wrong country";

	private static final String STATE = "Pan villa";

	private static final String WRONG_STATE = "wrong  state";

	private static final String POSTAL_CODE = "234-30100";

	private static final String WRONG_POSTAL_CODE = "wrong postal code";

	@Mock
	private FhirLocationService locationService;

	private LocationFhirResourceProvider resourceProvider;

	private Location location;

	@Before
	public void setup() {
		resourceProvider = new LocationFhirResourceProvider();
		resourceProvider.setFhirLocationService(locationService);
	}

	@Before
	public void initLocation() {
		Address address = new Address();
		address.setCity(CITY);
		address.setCountry(COUNTRY);
		address.setState(STATE);
		address.setPostalCode(POSTAL_CODE);

		location = new Location();
		location.setId(LOCATION_UUID);
		location.setName(LOCATION_NAME);
		location.setAddress(address);
	}

	@Test
	public void getResourceType_shouldReturnResourceType() {
		assertThat(resourceProvider.getResourceType(), equalTo(Location.class));
		assertThat(resourceProvider.getResourceType().getName(), equalTo(Location.class.getName()));
	}

	@Test
	public void getLocationByUuid_shouldReturnMatchingLocation() {
		when(locationService.getLocationByUuid(LOCATION_UUID)).thenReturn(location);
		IdType id = new IdType();
		id.setValue(LOCATION_UUID);
		Location result = resourceProvider.getLocationById(id);
		assertThat(result, notNullValue());
		assertThat(result.getId(), notNullValue());
		assertThat(result.getId(), equalTo(LOCATION_UUID));
	}

	@Test(expected = ResourceNotFoundException.class)
	public void getLocationWithWrongUuid_shouldThrowResourceNotFoundException() {
		IdType id = new IdType();
		id.setValue(WRONG_LOCATION_UUID);
		Location result = resourceProvider.getLocationById(id);
		assertThat(result, nullValue());
	}

	@Test
	public void findLocationsByName_shouldReturnMatchingBundleOfLocations() {
		when(locationService.findLocationByName(LOCATION_NAME)).thenReturn(Collections.singletonList(location));
		StringParam param = new StringParam();
		param.setValue(LOCATION_NAME);

		Bundle results = resourceProvider.findLocationByName(param);
		assertThat(results, notNullValue());
		assertThat(results.isResource(), is(true));
		assertThat(results.getEntry().size(), greaterThanOrEqualTo(1));
	}

	@Test
	public void findLocationsByWrongLocationName_shouldReturnBundleWithEmptyEntries() {
		StringParam param = new StringParam();
		param.setValue(WRONG_LOCATION_NAME);
		Bundle results = resourceProvider.findLocationByName(param);
		assertThat(results, notNullValue());
		assertThat(results.isResource(), is(true));
		assertThat(results.getEntry(), is(empty()));
	}

	@Test
	public void findLocationsByCity_shouldReturnMatchingBundleOfLocations() {
		when(locationService.findLocationsByCity(CITY)).thenReturn(Collections.singletonList(location));
		StringParam param = new StringParam();
		param.setValue(CITY);

		Bundle results = resourceProvider.findLocationByCity(param);
		assertThat(results, notNullValue());
		assertThat(results.isResource(), is(true));
		assertThat(results.getEntry().size(), greaterThanOrEqualTo(1));
	}

	@Test
	public void findLocationsByWrongCityName_shouldReturnBundleWithEmptyEntries() {
		StringParam param = new StringParam();
		param.setValue(WRONG_CITY);
		Bundle results = resourceProvider.findLocationByCity(param);
		assertThat(results, notNullValue());
		assertThat(results.isResource(), is(true));
		assertThat(results.getEntry(), is(empty()));
	}

	@Test
	public void findLocationsByCountry_shouldReturnMatchingBundleOfLocations() {
		when(locationService.findLocationsByCountry(COUNTRY)).thenReturn(Collections.singletonList(location));
		StringParam param = new StringParam();
		param.setValue(COUNTRY);

		Bundle results = resourceProvider.findLocationByCountry(param);
		assertThat(results, notNullValue());
		assertThat(results.isResource(), is(true));
		assertThat(results.getEntry().size(), greaterThanOrEqualTo(1));
	}

	@Test
	public void findLocationsByWrongCountryName_shouldReturnBundleWithEmptyEntries() {
		StringParam param = new StringParam();
		param.setValue(WRONG_COUNTRY);
		Bundle results = resourceProvider.findLocationByCountry(param);
		assertThat(results, notNullValue());
		assertThat(results.isResource(), is(true));
		assertThat(results.getEntry(), is(empty()));
	}

	@Test
	public void findLocationsByState_shouldReturnMatchingBundleOfLocations() {
		when(locationService.findLocationsByState(STATE)).thenReturn(Collections.singletonList(location));
		StringParam param = new StringParam();
		param.setValue(STATE);

		Bundle results = resourceProvider.findLocationByState(param);
		assertThat(results, notNullValue());
		assertThat(results.isResource(), is(true));
		assertThat(results.getEntry().size(), greaterThanOrEqualTo(1));
	}

	@Test
	public void findLocationsByWrongStateName_shouldReturnBundleWithEmptyEntries() {
		StringParam param = new StringParam();
		param.setValue(WRONG_STATE);
		Bundle results = resourceProvider.findLocationByState(param);
		assertThat(results, notNullValue());
		assertThat(results.isResource(), is(true));
		assertThat(results.getEntry(), is(empty()));
	}

	@Test
	public void findLocationsByPostalCode_shouldReturnMatchingBundleOfLocations() {
		when(locationService.findLocationsByPostalCode(POSTAL_CODE)).thenReturn(Collections.singletonList(location));
		StringParam param = new StringParam();
		param.setValue(POSTAL_CODE);

		Bundle results = resourceProvider.findLocationByPostalCode(param);
		assertThat(results, notNullValue());
		assertThat(results.isResource(), is(true));
		assertThat(results.getEntry().size(), greaterThanOrEqualTo(1));
	}

	@Test
	public void findLocationsByWrongPostalCode_shouldReturnBundleWithEmptyEntries() {
		StringParam param = new StringParam();
		param.setValue(WRONG_POSTAL_CODE);
		Bundle results = resourceProvider.findLocationByPostalCode(param);
		assertThat(results, notNullValue());
		assertThat(results.isResource(), is(true));
		assertThat(results.getEntry(), is(empty()));
	}

}
