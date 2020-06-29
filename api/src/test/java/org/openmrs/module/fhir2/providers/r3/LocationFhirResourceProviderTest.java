/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.fhir2.providers.r3;

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

import ca.uhn.fhir.rest.api.server.IBundleProvider;
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
import org.hl7.fhir.dstu3.model.IdType;
import org.hl7.fhir.dstu3.model.Resource;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.Address;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Location;
import org.hl7.fhir.r4.model.Provenance;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.api.FhirLocationService;
import org.openmrs.module.fhir2.providers.r4.MockIBundleProvider;

@RunWith(MockitoJUnitRunner.class)
public class LocationFhirResourceProviderTest extends BaseFhirR3ProvenanceResourceTest<org.hl7.fhir.r4.model.Location> {
	
	private static final String LOCATION_UUID = "123xx34-623hh34-22hj89-23hjy5";
	
	private static final String WRONG_LOCATION_UUID = "c3467w-hi4jer83-56hj34-23hjy5";
	
	private static final String LOCATION_NAME = "chulaimbo";
	
	private static final String CITY = "kakamega";
	
	private static final String COUNTRY = "Kenya";
	
	private static final String STATE = "Pan villa";
	
	private static final String POSTAL_CODE = "234-30100";
	
	private static final String LOGIN_LOCATION_TAG_NAME = "login";
	
	private static final String LOGIN_LOCATION_TAG_DESCRIPTION = "Identify login locations";
	
	private static final int PREFERRED_PAGE_SIZE = 10;
	
	private static final int COUNT = 1;
	
	private static final int START_INDEX = 0;
	
	private static final int END_INDEX = 10;
	
	@Mock
	private FhirLocationService locationService;
	
	private LocationFhirResourceProvider resourceProvider;
	
	private Location location;
	
	@Before
	public void setup() {
		resourceProvider = new LocationFhirResourceProvider();
		resourceProvider.setLocationService(locationService);
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
		assertThat(resourceProvider.getResourceType(), equalTo(org.hl7.fhir.dstu3.model.Location.class));
		assertThat(resourceProvider.getResourceType().getName(), equalTo(org.hl7.fhir.dstu3.model.Location.class.getName()));
	}
	
	@Test
	public void getLocationByUuid_shouldReturnMatchingLocation() {
		when(locationService.get(LOCATION_UUID)).thenReturn(location);
		IdType id = new IdType();
		id.setValue(LOCATION_UUID);
		org.hl7.fhir.dstu3.model.Location result = resourceProvider.getLocationById(id);
		assertThat(result, notNullValue());
		assertThat(result.getId(), notNullValue());
		assertThat(result.getId(), equalTo(LOCATION_UUID));
	}
	
	@Test(expected = ResourceNotFoundException.class)
	public void getLocationWithWrongUuid_shouldThrowResourceNotFoundException() {
		IdType id = new IdType();
		id.setValue(WRONG_LOCATION_UUID);
		org.hl7.fhir.dstu3.model.Location result = resourceProvider.getLocationById(id);
		assertThat(result, nullValue());
	}
	
	@Test
	public void findLocationsByName_shouldReturnMatchingBundleOfLocations() {
		StringAndListParam nameParam = new StringAndListParam()
		        .addAnd(new StringOrListParam().add(new StringParam(LOCATION_NAME)));
		when(locationService.searchForLocations(argThat(Matchers.is(nameParam)), isNull(), isNull(), isNull(), isNull(),
		    isNull(), isNull(), isNull()))
		            .thenReturn(new MockIBundleProvider<>(Collections.singletonList(location), PREFERRED_PAGE_SIZE, COUNT));
		
		IBundleProvider results = resourceProvider.searchLocations(nameParam, null, null, null, null, null, null, null);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList.get(0).fhirType(), is("Location"));
		assertThat(resultList.size(), greaterThanOrEqualTo(1));
		assertThat(((Location) resultList.iterator().next()).getName(), equalTo(LOCATION_NAME));
	}
	
	@Test
	public void findLocationsByCity_shouldReturnMatchingBundleOfLocations() {
		StringAndListParam cityParam = new StringAndListParam().addAnd(new StringOrListParam().add(new StringParam(CITY)));
		when(locationService.searchForLocations(isNull(), argThat(Matchers.is(cityParam)), isNull(), isNull(), isNull(),
		    isNull(), isNull(), isNull()))
		            .thenReturn(new MockIBundleProvider<>(Collections.singletonList(location), PREFERRED_PAGE_SIZE, COUNT));
		
		IBundleProvider results = resourceProvider.searchLocations(null, cityParam, null, null, null, null, null, null);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList.get(0).fhirType(), is("Location"));
		assertThat(resultList.size(), greaterThanOrEqualTo(1));
		assertThat(((Location) resultList.iterator().next()).getAddress().getCity(), equalTo(CITY));
	}
	
	@Test
	public void findLocationsByCountry_shouldReturnMatchingBundleOfLocations() {
		StringAndListParam countryParam = new StringAndListParam()
		        .addAnd(new StringOrListParam().add(new StringParam(COUNTRY)));
		when(locationService.searchForLocations(isNull(), isNull(), argThat(Matchers.is(countryParam)), isNull(), isNull(),
		    isNull(), isNull(), isNull()))
		            .thenReturn(new MockIBundleProvider<>(Collections.singletonList(location), PREFERRED_PAGE_SIZE, COUNT));
		
		IBundleProvider results = resourceProvider.searchLocations(null, null, countryParam, null, null, null, null, null);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList.get(0).fhirType(), is("Location"));
		assertThat(resultList.size(), greaterThanOrEqualTo(1));
		assertThat(((Location) resultList.iterator().next()).getAddress().getCountry(), equalTo(COUNTRY));
	}
	
	@Test
	public void findLocationsByState_shouldReturnMatchingBundleOfLocations() {
		StringAndListParam stateParam = new StringAndListParam().addAnd(new StringOrListParam().add(new StringParam(STATE)));
		when(locationService.searchForLocations(isNull(), isNull(), isNull(), isNull(), argThat(Matchers.is(stateParam)),
		    isNull(), isNull(), isNull()))
		            .thenReturn(new MockIBundleProvider<>(Collections.singletonList(location), PREFERRED_PAGE_SIZE, COUNT));
		
		IBundleProvider results = resourceProvider.searchLocations(null, null, null, null, stateParam, null, null, null);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList.get(0).fhirType(), is("Location"));
		assertThat(resultList.size(), greaterThanOrEqualTo(1));
		assertThat(((Location) resultList.iterator().next()).getAddress().getState(), equalTo(STATE));
	}
	
	@Test
	public void findLocationsByPostalCode_shouldReturnMatchingBundleOfLocations() {
		StringAndListParam postalCodeParam = new StringAndListParam()
		        .addAnd(new StringOrListParam().add(new StringParam(POSTAL_CODE)));
		when(locationService.searchForLocations(isNull(), isNull(), isNull(), argThat(Matchers.is(postalCodeParam)),
		    isNull(), isNull(), isNull(), isNull()))
		            .thenReturn(new MockIBundleProvider<>(Collections.singletonList(location), PREFERRED_PAGE_SIZE, COUNT));
		
		IBundleProvider results = resourceProvider.searchLocations(null, null, null, postalCodeParam, null, null, null,
		    null);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList.get(0).fhirType(), is("Location"));
		assertThat(resultList.size(), greaterThanOrEqualTo(1));
		assertThat(((Location) resultList.iterator().next()).getAddress().getPostalCode(), equalTo(POSTAL_CODE));
	}
	
	@Test
	public void findLocationsByTags_shouldReturnLocationsContainingGivenTag() {
		TokenAndListParam tag = new TokenAndListParam()
		        .addAnd(new TokenOrListParam(FhirConstants.OPENMRS_FHIR_EXT_LOCATION_TAG, LOGIN_LOCATION_TAG_NAME));
		when(locationService.searchForLocations(isNull(), isNull(), isNull(), isNull(), isNull(), argThat(Matchers.is(tag)),
		    isNull(), isNull()))
		            .thenReturn(new MockIBundleProvider<>(Collections.singletonList(location), PREFERRED_PAGE_SIZE, COUNT));
		
		IBundleProvider results = resourceProvider.searchLocations(null, null, null, null, null, tag, null, null);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList.get(0).fhirType(), is("Location"));
		assertThat(resultList.size(), greaterThanOrEqualTo(1));
		assertThat(((Location) resultList.iterator().next()).getMeta().getTag().iterator().next().getCode(),
		    equalTo(LOGIN_LOCATION_TAG_NAME));
	}
	
	@Test
	public void searchLocations_shouldReturnMatchingBundleOfChainedLocationsByParentName() {
		ReferenceAndListParam locationParentName = new ReferenceAndListParam();
		locationParentName.addValue(
		    new ReferenceOrListParam().add(new ReferenceParam().setValue("chulaimbo").setChain(Location.SP_NAME)));
		
		when(locationService.searchForLocations(isNull(), isNull(), isNull(), isNull(), isNull(), isNull(),
		    argThat(Matchers.is(locationParentName)), isNull()))
		            .thenReturn(new MockIBundleProvider<>(Collections.singletonList(location), PREFERRED_PAGE_SIZE, COUNT));
		
		IBundleProvider results = resourceProvider.searchLocations(null, null, null, null, null, null, locationParentName,
		    null);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList.get(0).fhirType(), is("Location"));
		assertThat(resultList.size(), greaterThanOrEqualTo(1));
	}
	
	@Test
	public void searchLocations_shouldReturnMatchingBundleOfChainedLocationsByParentCity() {
		ReferenceAndListParam locationParentCity = new ReferenceAndListParam();
		locationParentCity.addValue(
		    new ReferenceOrListParam().add(new ReferenceParam().setValue("kampala").setChain(Location.SP_ADDRESS_CITY)));
		
		when(locationService.searchForLocations(isNull(), isNull(), isNull(), isNull(), isNull(), isNull(),
		    argThat(Matchers.is(locationParentCity)), isNull()))
		            .thenReturn(new MockIBundleProvider<>(Collections.singletonList(location), PREFERRED_PAGE_SIZE, COUNT));
		
		IBundleProvider results = resourceProvider.searchLocations(null, null, null, null, null, null, locationParentCity,
		    null);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList.get(0).fhirType(), is("Location"));
		assertThat(resultList.size(), greaterThanOrEqualTo(1));
	}
	
	@Test
	public void searchLocations_shouldReturnMatchingBundleOfChainedLocationsByParentCountry() {
		ReferenceAndListParam locationParentCountry = new ReferenceAndListParam();
		locationParentCountry.addValue(
		    new ReferenceOrListParam().add(new ReferenceParam().setValue("uganda").setChain(Location.SP_ADDRESS_COUNTRY)));
		
		when(locationService.searchForLocations(isNull(), isNull(), isNull(), isNull(), isNull(), isNull(),
		    argThat(Matchers.is(locationParentCountry)), isNull()))
		            .thenReturn(new MockIBundleProvider<>(Collections.singletonList(location), PREFERRED_PAGE_SIZE, COUNT));
		
		IBundleProvider results = resourceProvider.searchLocations(null, null, null, null, null, null, locationParentCountry,
		    null);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList.get(0).fhirType(), is("Location"));
		assertThat(resultList.size(), greaterThanOrEqualTo(1));
	}
	
	@Test
	public void searchLocations_shouldReturnMatchingBundleOfChainedLocationsByParentPostalCode() {
		ReferenceAndListParam locationParentPostalCode = new ReferenceAndListParam();
		locationParentPostalCode.addValue(new ReferenceOrListParam()
		        .add(new ReferenceParam().setValue("234-30100").setChain(Location.SP_ADDRESS_POSTALCODE)));
		
		when(locationService.searchForLocations(isNull(), isNull(), isNull(), isNull(), isNull(), isNull(),
		    argThat(Matchers.is(locationParentPostalCode)), isNull()))
		            .thenReturn(new MockIBundleProvider<>(Collections.singletonList(location), PREFERRED_PAGE_SIZE, COUNT));
		
		IBundleProvider results = resourceProvider.searchLocations(null, null, null, null, null, null,
		    locationParentPostalCode, null);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList.get(0).fhirType(), is("Location"));
		assertThat(resultList.size(), greaterThanOrEqualTo(1));
	}
	
	@Test
	public void searchLocations_shouldReturnMatchingBundleOfChainedLocationsByParentState() {
		ReferenceAndListParam locationParentState = new ReferenceAndListParam();
		locationParentState.addValue(new ReferenceOrListParam()
		        .add(new ReferenceParam().setValue("najjanankumbi").setChain(Location.SP_ADDRESS_STATE)));
		
		when(locationService.searchForLocations(isNull(), isNull(), isNull(), isNull(), isNull(), isNull(),
		    argThat(Matchers.is(locationParentState)), isNull()))
		            .thenReturn(new MockIBundleProvider<>(Collections.singletonList(location), PREFERRED_PAGE_SIZE, COUNT));
		
		IBundleProvider results = resourceProvider.searchLocations(null, null, null, null, null, null, locationParentState,
		    null);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList.get(0).fhirType(), is("Location"));
		assertThat(resultList.size(), greaterThanOrEqualTo(1));
	}
	
	@Test
	public void searchLocations_shouldReturnMatchingBundleOfLocations() {
		List<Location> locations = new ArrayList<>();
		locations.add(location);
		when(locationService.searchForLocations(any(), any(), any(), any(), any(), any(), any(), any()))
		        .thenReturn(new MockIBundleProvider<>(locations, PREFERRED_PAGE_SIZE, COUNT));
		
		StringAndListParam location = new StringAndListParam()
		        .addAnd(new StringOrListParam().add(new StringParam(LOCATION_NAME)));
		
		IBundleProvider resultLocations = resourceProvider.searchLocations(location, null, null, null, null, null, null,
		    null);
		
		List<IBaseResource> resultList = get(resultLocations);
		
		assertThat(resultLocations, notNullValue());
		assertThat(resultList.get(0).fhirType(), is("Location"));
		assertThat(resultList.size(), greaterThanOrEqualTo(1));
		assertThat(((Location) resultList.get(0)).getId(), equalTo(LOCATION_UUID));
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
	
	private List<IBaseResource> get(IBundleProvider results) {
		return results.getResources(START_INDEX, END_INDEX);
	}
}
