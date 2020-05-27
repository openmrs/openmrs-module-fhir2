/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.fhir2.providers.r4;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;
import static org.mockito.hamcrest.MockitoHamcrest.argThat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ca.uhn.fhir.rest.param.ReferenceAndListParam;
import ca.uhn.fhir.rest.param.ReferenceOrListParam;
import ca.uhn.fhir.rest.param.ReferenceParam;
import ca.uhn.fhir.rest.param.StringAndListParam;
import ca.uhn.fhir.rest.param.StringOrListParam;
import ca.uhn.fhir.rest.param.StringParam;
import ca.uhn.fhir.rest.param.TokenAndListParam;
import ca.uhn.fhir.rest.param.TokenOrListParam;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import org.hamcrest.Matchers;
import org.hl7.fhir.r4.model.Address;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.Location;
import org.hl7.fhir.r4.model.Provenance;
import org.hl7.fhir.r4.model.Resource;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.api.FhirLocationService;
import org.openmrs.module.fhir2.providers.BaseFhirProvenanceResourceTest;

@RunWith(MockitoJUnitRunner.class)
public class LocationFhirResourceProviderTest extends BaseFhirProvenanceResourceTest<Location> {
	
	private static final String LOCATION_UUID = "123xx34-623hh34-22hj89-23hjy5";
	
	private static final String WRONG_LOCATION_UUID = "c3467w-hi4jer83-56hj34-23hjy5";
	
	private static final String LOCATION_NAME = "chulaimbo";
	
	private static final String CITY = "kakamega";
	
	private static final String COUNTRY = "Kenya";
	
	private static final String STATE = "Pan villa";
	
	private static final String POSTAL_CODE = "234-30100";
	
	private static final String LOGIN_LOCATION_TAG_NAME = "login";
	
	private static final String LOGIN_LOCATION_TAG_DESCRIPTION = "Identify login locations";
	
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
		location.getMeta().addTag(new Coding(FhirConstants.OPENMRS_FHIR_EXT_LOCATION_TAG, LOGIN_LOCATION_TAG_NAME,
		        LOGIN_LOCATION_TAG_DESCRIPTION));
		setProvenanceResources(location);
	}
	
	@Test
	public void getResourceType_shouldReturnResourceType() {
		assertThat(resourceProvider.getResourceType(), equalTo(Location.class));
		assertThat(resourceProvider.getResourceType().getName(), equalTo(Location.class.getName()));
	}
	
	@Test
	public void getLocationByUuid_shouldReturnMatchingLocation() {
		when(locationService.get(LOCATION_UUID)).thenReturn(location);
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
		StringAndListParam nameParam = new StringAndListParam()
		        .addAnd(new StringOrListParam().add(new StringParam(LOCATION_NAME)));
		when(locationService.searchForLocations(argThat(Matchers.is(nameParam)), isNull(), isNull(), isNull(), isNull(),
		    isNull(), isNull(), isNull())).thenReturn(Collections.singletonList(location));
		
		Bundle results = resourceProvider.searchLocations(nameParam, null, null, null, null, null, null, null);
		
		assertThat(results, notNullValue());
		assertThat(results.isResource(), is(true));
		assertThat(results.getEntry().size(), greaterThanOrEqualTo(1));
	}
	
	@Test
	public void findLocationsByCity_shouldReturnMatchingBundleOfLocations() {
		StringAndListParam cityParam = new StringAndListParam().addAnd(new StringOrListParam().add(new StringParam(CITY)));
		when(locationService.searchForLocations(isNull(), argThat(Matchers.is(cityParam)), isNull(), isNull(), isNull(),
		    isNull(), isNull(), isNull())).thenReturn(Collections.singletonList(location));
		
		Bundle results = resourceProvider.searchLocations(null, cityParam, null, null, null, null, null, null);
		
		assertThat(results, Matchers.notNullValue());
		assertThat(results.isResource(), Matchers.is(true));
		assertThat(results.getEntry().size(), greaterThanOrEqualTo(1));
	}
	
	@Test
	public void findLocationsByCountry_shouldReturnMatchingBundleOfLocations() {
		StringAndListParam countryParam = new StringAndListParam()
		        .addAnd(new StringOrListParam().add(new StringParam(COUNTRY)));
		when(locationService.searchForLocations(isNull(), isNull(), argThat(Matchers.is(countryParam)), isNull(), isNull(),
		    isNull(), isNull(), isNull())).thenReturn(Collections.singletonList(location));
		
		Bundle results = resourceProvider.searchLocations(null, null, countryParam, null, null, null, null, null);
		
		assertThat(results, Matchers.notNullValue());
		assertThat(results.isResource(), Matchers.is(true));
		assertThat(results.getEntry().size(), greaterThanOrEqualTo(1));
	}
	
	@Test
	public void findLocationsByState_shouldReturnMatchingBundleOfLocations() {
		StringAndListParam stateParam = new StringAndListParam().addAnd(new StringOrListParam().add(new StringParam(STATE)));
		when(locationService.searchForLocations(isNull(), isNull(), isNull(), isNull(), argThat(Matchers.is(stateParam)),
		    isNull(), isNull(), isNull())).thenReturn(Collections.singletonList(location));
		
		Bundle results = resourceProvider.searchLocations(null, null, null, null, stateParam, null, null, null);
		
		assertThat(results, Matchers.notNullValue());
		assertThat(results.isResource(), Matchers.is(true));
		assertThat(results.getEntry().size(), greaterThanOrEqualTo(1));
	}
	
	@Test
	public void findLocationsByPostalCode_shouldReturnMatchingBundleOfLocations() {
		StringAndListParam postalCodeParam = new StringAndListParam()
		        .addAnd(new StringOrListParam().add(new StringParam(POSTAL_CODE)));
		when(locationService.searchForLocations(isNull(), isNull(), isNull(), argThat(Matchers.is(postalCodeParam)),
		    isNull(), isNull(), isNull(), isNull())).thenReturn(Collections.singletonList(location));
		
		Bundle results = resourceProvider.searchLocations(null, null, null, postalCodeParam, null, null, null, null);
		
		assertThat(results, Matchers.notNullValue());
		assertThat(results.isResource(), Matchers.is(true));
		assertThat(results.getEntry().size(), greaterThanOrEqualTo(1));
	}
	
	@Test
	public void findLocationsByTags_shouldReturnLocationsContainingGivenTag() {
		TokenAndListParam tag = new TokenAndListParam()
		        .addAnd(new TokenOrListParam(FhirConstants.OPENMRS_FHIR_EXT_LOCATION_TAG, LOGIN_LOCATION_TAG_NAME));
		when(locationService.searchForLocations(isNull(), isNull(), isNull(), isNull(), isNull(), argThat(Matchers.is(tag)),
		    isNull(), isNull())).thenReturn(Collections.singletonList(location));
		
		Bundle results = resourceProvider.searchLocations(null, null, null, null, null, tag, null, null);
		
		assertThat(results, notNullValue());
		assertThat(results.isResource(), is(true));
		assertThat(results.getEntry().size(), greaterThanOrEqualTo(1));
	}
	
	@Test
	public void searchLocations_shouldReturnMatchingBundleOfChainedLocationsByName() {
		ReferenceAndListParam locationParentName = new ReferenceAndListParam();
		locationParentName.addValue(
		    new ReferenceOrListParam().add(new ReferenceParam().setValue("chulaimbo").setChain(Location.SP_NAME)));

		when(locationService.searchForLocations(isNull(), isNull(), isNull(), isNull(), isNull(), isNull(),
		    argThat(Matchers.is(locationParentName)), isNull())).thenReturn(Collections.singletonList(location));

		Bundle results = resourceProvider.searchLocations(null, null, null, null, null, null, locationParentName, null);

		assertThat(results, notNullValue());
		assertThat(results.isResource(), is(true));
		assertThat(results.getEntry().size(), greaterThanOrEqualTo(1));
	}

	@Test
	public void searchLocations_shouldReturnMatchingBundleOfChianedLocationsByCity() {
		ReferenceAndListParam locationParentCity = new ReferenceAndListParam();
		locationParentCity.addValue(
		    new ReferenceOrListParam().add(new ReferenceParam().setValue("kampala").setChain(Location.SP_ADDRESS_CITY)));

		when(locationService.searchForLocations(isNull(), isNull(), isNull(), isNull(), isNull(), isNull(),
		    argThat(Matchers.is(locationParentCity)), isNull())).thenReturn(Collections.singletonList(location));

		Bundle results = resourceProvider.searchLocations(null, null, null, null, null, null, locationParentCity, null);

		assertThat(results, notNullValue());
		assertThat(results.isResource(), is(true));
		assertThat(results.getEntry().size(), greaterThanOrEqualTo(1));
	}

	@Test
	public void searchLocations_shouldReturnMatchingBundleOfChianedLocationsByCountry() {
		ReferenceAndListParam locationParentCountry = new ReferenceAndListParam();
		locationParentCountry.addValue(
		    new ReferenceOrListParam().add(new ReferenceParam().setValue("uganda").setChain(Location.SP_ADDRESS_COUNTRY)));

		when(locationService.searchForLocations(isNull(), isNull(), isNull(), isNull(), isNull(), isNull(),
		    argThat(Matchers.is(locationParentCountry)), isNull())).thenReturn(Collections.singletonList(location));

		Bundle results = resourceProvider.searchLocations(null, null, null, null, null, null, locationParentCountry, null);

		assertThat(results, notNullValue());
		assertThat(results.isResource(), is(true));
		assertThat(results.getEntry().size(), greaterThanOrEqualTo(1));
	}

	@Test
	public void searchLocations_shouldReturnMatchingBundleOfChianedLocationsByPostalCode() {
		ReferenceAndListParam locationParentPostalCode = new ReferenceAndListParam();
		locationParentPostalCode.addValue(new ReferenceOrListParam()
		        .add(new ReferenceParam().setValue("234-30100").setChain(Location.SP_ADDRESS_POSTALCODE)));

		when(locationService.searchForLocations(isNull(), isNull(), isNull(), isNull(), isNull(), isNull(),
		    argThat(Matchers.is(locationParentPostalCode)), isNull())).thenReturn(Collections.singletonList(location));

		Bundle results = resourceProvider.searchLocations(null, null, null, null, null, null, locationParentPostalCode,
		    null);

		assertThat(results, notNullValue());
		assertThat(results.isResource(), is(true));
		assertThat(results.getEntry().size(), greaterThanOrEqualTo(1));
	}

	@Test
	public void searchLocations_shouldReturnMatchingBundleOfChianedLocationsByState() {
		ReferenceAndListParam locationParentState = new ReferenceAndListParam();
		locationParentState.addValue(new ReferenceOrListParam()
		        .add(new ReferenceParam().setValue("najjanankumbi").setChain(Location.SP_ADDRESS_STATE)));

		when(locationService.searchForLocations(isNull(), isNull(), isNull(), isNull(), isNull(), isNull(),
		    argThat(Matchers.is(locationParentState)), isNull())).thenReturn(Collections.singletonList(location));

		Bundle results = resourceProvider.searchLocations(null, null, null, null, null, null, locationParentState, null);

		assertThat(results, notNullValue());
		assertThat(results.isResource(), is(true));
		assertThat(results.getEntry().size(), greaterThanOrEqualTo(1));
	}

	
	@Test
	public void searchLocations_shouldReturnMatchingBundleOfLocations() {
		List<Location> locations = new ArrayList<>();
		locations.add(location);
		when(locationService.searchForLocations(any(), any(), any(), any(), any(), any(), any(), any()))
		        .thenReturn(locations);
		
		StringAndListParam location = new StringAndListParam()
		        .addAnd(new StringOrListParam().add(new StringParam(LOCATION_NAME)));
		
		Bundle resultLocations = resourceProvider.searchLocations(location, null, null, null, null, null, null, null);
		
		assertThat(resultLocations, notNullValue());
		assertThat(resultLocations.isResource(), is(true));
		assertThat(resultLocations.getTotal(), equalTo(1));
		assertThat(resultLocations.getEntry(), notNullValue());
		assertThat(resultLocations.getEntry().get(0).getResource().fhirType(), equalTo("Location"));
		assertThat(resultLocations.getEntry().get(0).getResource().getId(), equalTo(LOCATION_UUID));
	}
	
	@Test
	public void getLocationHistoryById_shouldReturnListOfResource() {
		IdType id = new IdType();
		id.setValue(LOCATION_UUID);
		when(locationService.get(LOCATION_UUID)).thenReturn(location);
		
		List<Resource> resources = resourceProvider.getLocationHistoryById(id);
		assertThat(resources, Matchers.notNullValue());
		assertThat(resources, not(empty()));
		assertThat(resources.size(), Matchers.equalTo(2));
	}
	
	@Test
	public void getLocationHistoryById_shouldReturnProvenanceResources() {
		IdType id = new IdType();
		id.setValue(LOCATION_UUID);
		when(locationService.get(LOCATION_UUID)).thenReturn(location);
		
		List<Resource> resources = resourceProvider.getLocationHistoryById(id);
		assertThat(resources, not(empty()));
		assertThat(resources.stream().findAny().isPresent(), Matchers.is(true));
		assertThat(resources.stream().findAny().get().getResourceType().name(),
		    Matchers.equalTo(Provenance.class.getSimpleName()));
	}
	
	@Test(expected = ResourceNotFoundException.class)
	public void getLocationHistoryByWithWrongId_shouldThrowResourceNotFoundException() {
		IdType idType = new IdType();
		idType.setValue(LOCATION_UUID);
		assertThat(resourceProvider.getLocationHistoryById(idType).isEmpty(), Matchers.is(true));
		assertThat(resourceProvider.getLocationHistoryById(idType).size(), Matchers.equalTo(0));
	}
}
