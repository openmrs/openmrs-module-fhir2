/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.fhir2.api.dao.impl;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import ca.uhn.fhir.rest.api.SortOrderEnum;
import ca.uhn.fhir.rest.api.SortSpec;
import ca.uhn.fhir.rest.param.ReferenceAndListParam;
import ca.uhn.fhir.rest.param.ReferenceOrListParam;
import ca.uhn.fhir.rest.param.ReferenceParam;
import ca.uhn.fhir.rest.param.StringAndListParam;
import ca.uhn.fhir.rest.param.StringOrListParam;
import ca.uhn.fhir.rest.param.StringParam;
import ca.uhn.fhir.rest.param.TokenAndListParam;
import ca.uhn.fhir.rest.param.TokenOrListParam;
import org.hibernate.SessionFactory;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.Location;
import org.openmrs.LocationAttribute;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.TestFhirSpringConfiguration;
import org.openmrs.test.BaseModuleContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;

@ContextConfiguration(classes = TestFhirSpringConfiguration.class, inheritLocations = false)
public class FhirLocationDaoImplTest extends BaseModuleContextSensitiveTest {
	
	private static final String LOCATION_UUID = "c0938432-1691-11df-97a5-7038c432aaba";
	
	private static final String UNKNOWN_LOCATION_UUID = "c0938432-1691-11df-97a5-7038c432aabz";
	
	private static final String LOCATION_NAME = "Test location 7";
	
	private static final String UNKNOWN_LOCATION_NAME = "Location2";
	
	private static final String LOCATION_CITY = "Artuor";
	
	private static final String UNKNOWN_LOCATION_CITY = "ArtuorA";
	
	private static final String LOCATION_COUNTRY = "Kenya";
	
	private static final String UNKNOWN_LOCATION_COUNTRY = "KenyaA";
	
	private static final String POSTAL_CODE = "4069-3100";
	
	private static final String UNKNOWN_POSTAL_CODE = "4015-3100";
	
	private static final String LOCATION_STATE = "province";
	
	private static final String UNKNOWN_LOCATION_STATE = "province state";
	
	private static final String LOGIN_LOCATION_TAG_NAME = "login";
	
	private static final String LOCATION_PARENT_CITY = "Artuor";
	
	private static final String LOCATION_PARENT_COUNTRY = "Kenya";
	
	private static final String LOCATION_PARENT_POSTAL_CODE = "4069-3100";
	
	private static final String LOCATION_PARENT_STATE = "province";
	
	private static final String LOCATION_PARENT_ID = "c0938432-1691-11df-2222-7038c432aabd";
	
	private static final String LOCATION_PARENT_NAME = "Test location 5";
	
	private static final String LOCATION_ATTRIBUTE_TYPE_UUID = "abcde432-1691-11df-97a5-7038c432abcd";
	
	private static final String LOCATION_INITIAL_DATA_XML = "org/openmrs/module/fhir2/api/dao/impl/FhirLocationDaoImplTest_initial_data.xml";
	
	private FhirLocationDaoImpl fhirLocationDao;
	
	@Autowired
	@Qualifier("sessionFactory")
	private SessionFactory sessionFactory;
	
	@Before
	public void setup() throws Exception {
		fhirLocationDao = new FhirLocationDaoImpl();
		fhirLocationDao.setSessionFactory(sessionFactory);
		executeDataSet(LOCATION_INITIAL_DATA_XML);
	}
	
	@Test
	public void getLocationByUuid_shouldReturnMatchingLocation() {
		Location location = fhirLocationDao.getLocationByUuid(LOCATION_UUID);
		
		assertThat(location, notNullValue());
		assertThat(location.getUuid(), equalTo(LOCATION_UUID));
	}
	
	@Test
	public void getLocationByUuid_shouldReturnNullWithUnknownUuid() {
		Location location = fhirLocationDao.getLocationByUuid(UNKNOWN_LOCATION_UUID);
		
		assertThat(location, nullValue());
	}
	
	@Test
	public void searchForLocations_shouldReturnCorrectLocationByName() {
		StringAndListParam location = new StringAndListParam()
		        .addAnd(new StringOrListParam().add(new StringParam(LOCATION_NAME)));
		Collection<Location> locations = fhirLocationDao.searchForLocations(location, null, null, null, null, null, null,
		    null);
		
		assertThat(locations, notNullValue());
		assertThat(locations.size(), equalTo(1));
		assertThat(locations.iterator().next().getName(), equalTo(LOCATION_NAME));
	}
	
	@Test
	public void searchForLocations_shouldReturnEmptyCollectionWhenCalledWithUnknownName() {
		StringAndListParam location = new StringAndListParam()
		        .addAnd(new StringOrListParam().add(new StringParam(UNKNOWN_LOCATION_NAME)));
		Collection<Location> locations = fhirLocationDao.searchForLocations(location, null, null, null, null, null, null,
		    null);
		
		assertThat(locations, notNullValue());
		assertThat(locations.size(), equalTo(0));
	}
	
	@Test
	public void searchForLocations_shouldReturnCorrectLocationByCity() {
		StringAndListParam city = new StringAndListParam()
		        .addAnd(new StringOrListParam().add(new StringParam(LOCATION_CITY)));
		Collection<Location> locations = fhirLocationDao.searchForLocations(null, city, null, null, null, null, null, null);
		
		assertThat(locations, notNullValue());
		assertThat(locations.size(), equalTo(1));
		assertThat(locations.iterator().next().getCityVillage(), equalTo(LOCATION_CITY));
	}
	
	@Test
	public void searchForLocations_shouldReturnEmptyCollectionWhenCalledWithUnknownCity() {
		StringAndListParam city = new StringAndListParam()
		        .addAnd(new StringOrListParam().add(new StringParam(UNKNOWN_LOCATION_CITY)));
		Collection<Location> locations = fhirLocationDao.searchForLocations(null, city, null, null, null, null, null, null);
		
		assertThat(locations, notNullValue());
		assertThat(locations.size(), equalTo(0));
	}
	
	@Test
	public void searchForLocations_shouldReturnCorrectLocationByCountry() {
		StringAndListParam country = new StringAndListParam()
		        .addAnd(new StringOrListParam().add(new StringParam(LOCATION_COUNTRY)));
		Collection<Location> locations = fhirLocationDao.searchForLocations(null, null, country, null, null, null, null,
		    null);
		
		assertThat(locations, notNullValue());
		assertThat(locations.size(), equalTo(2));
		assertThat(locations.iterator().next().getCountry(), equalTo(LOCATION_COUNTRY));
	}
	
	@Test
	public void searchForLocations_shouldReturnEmptyCollectionWhenCalledWithUnknownCountry() {
		StringAndListParam country = new StringAndListParam()
		        .addAnd(new StringOrListParam().add(new StringParam(UNKNOWN_LOCATION_COUNTRY)));
		Collection<Location> locations = fhirLocationDao.searchForLocations(null, null, country, null, null, null, null,
		    null);
		
		assertThat(locations, notNullValue());
		assertThat(locations.size(), equalTo(0));
	}
	
	@Test
	public void searchForLocations_shouldReturnCorrectLocationByPostalCode() {
		StringAndListParam postalCode = new StringAndListParam()
		        .addAnd(new StringOrListParam().add(new StringParam(POSTAL_CODE)));
		Collection<Location> locations = fhirLocationDao.searchForLocations(null, null, null, postalCode, null, null, null,
		    null);
		
		assertThat(locations, notNullValue());
		assertThat(locations.size(), equalTo(2));
		assertThat(locations.iterator().next().getPostalCode(), equalTo(POSTAL_CODE));
	}
	
	@Test
	public void searchForLocations_shouldReturnEmptyCollectionWhenCalledWithUnknownCode() {
		StringAndListParam postalCode = new StringAndListParam()
		        .addAnd(new StringOrListParam().add(new StringParam(UNKNOWN_POSTAL_CODE)));
		Collection<Location> locations = fhirLocationDao.searchForLocations(null, null, null, postalCode, null, null, null,
		    null);
		
		assertThat(locations, notNullValue());
		assertThat(locations.size(), equalTo(0));
	}
	
	@Test
	public void searchForLocations_shouldReturnCorrectLocationByState() {
		StringAndListParam state = new StringAndListParam()
		        .addAnd(new StringOrListParam().add(new StringParam(LOCATION_STATE)));
		Collection<Location> locations = fhirLocationDao.searchForLocations(null, null, null, null, state, null, null, null);
		
		assertThat(locations, notNullValue());
		assertThat(locations.size(), equalTo(2));
		assertThat(locations.iterator().next().getStateProvince(), equalTo(LOCATION_STATE));
	}
	
	@Test
	public void searchForLocations_shouldReturnEmptyCollectionWhenCalledWithUnknownState() {
		StringAndListParam state = new StringAndListParam()
		        .addAnd(new StringOrListParam().add(new StringParam(UNKNOWN_LOCATION_STATE)));
		Collection<Location> locations = fhirLocationDao.searchForLocations(null, null, null, null, state, null, null, null);
		
		assertThat(locations, notNullValue());
		assertThat(locations.size(), equalTo(0));
	}
	
	@Test
	public void searchForLocations_shouldReturnLocationsContainingGivenTag() {
		TokenAndListParam locationTag = new TokenAndListParam()
		        .addAnd(new TokenOrListParam(FhirConstants.OPENMRS_FHIR_EXT_LOCATION_TAG, LOGIN_LOCATION_TAG_NAME));
		Collection<Location> locations = fhirLocationDao.searchForLocations(null, null, null, null, null, locationTag, null,
		    null);
		
		assertThat(locations, notNullValue());
		assertThat(locations.size(), equalTo(2));
		assertThat(locations.iterator().next().getTags().iterator().next().getName(), equalTo(LOGIN_LOCATION_TAG_NAME));
	}
	
	@Test
	public void searchForLocations_shouldReturnCorrectLocationByParentUUID() {
		ReferenceAndListParam parentLocation = new ReferenceAndListParam()
		        .addAnd(new ReferenceOrListParam().add(new ReferenceParam().setValue(LOCATION_PARENT_ID).setChain(null)));
		Collection<Location> locations = fhirLocationDao.searchForLocations(null, null, null, null, null, null,
		    parentLocation, null);
		
		assertThat(locations, notNullValue());
		assertThat(locations.size(), equalTo(1));
		assertThat(locations.iterator().next().getParentLocation().getUuid(), equalTo(LOCATION_PARENT_ID));
	}
	
	@Test
	public void searchForLocations_shouldReturnCorrectLocationByParentName() {
		ReferenceAndListParam parentLocation = new ReferenceAndListParam().addAnd(
		    new ReferenceOrListParam().add(new ReferenceParam().setValue(LOCATION_PARENT_NAME).setChain("name")));
		Collection<Location> locations = fhirLocationDao.searchForLocations(null, null, null, null, null, null,
		    parentLocation, null);
		
		assertThat(locations, notNullValue());
		assertThat(locations.size(), equalTo(1));
		assertThat(locations.iterator().next().getParentLocation().getName(), equalTo(LOCATION_PARENT_NAME));
	}
	
	@Test
	public void searchForLocations_shouldReturnCorrectLocationByParentCity() {
		ReferenceAndListParam parentLocation = new ReferenceAndListParam().addAnd(
		    new ReferenceOrListParam().add(new ReferenceParam().setValue(LOCATION_PARENT_CITY).setChain("address-city")));
		Collection<Location> locations = fhirLocationDao.searchForLocations(null, null, null, null, null, null,
		    parentLocation, null);
		
		assertThat(locations, notNullValue());
		assertThat(locations.size(), equalTo(1));
		assertThat(locations.iterator().next().getParentLocation().getCityVillage(), equalTo(LOCATION_PARENT_CITY));
	}
	
	@Test
	public void searchForLocations_shouldReturnCorrectLocationByParentCountry() {
		ReferenceAndListParam parentLocation = new ReferenceAndListParam().addAnd(new ReferenceOrListParam()
		        .add(new ReferenceParam().setValue(LOCATION_PARENT_COUNTRY).setChain("address-country")));
		Collection<Location> locations = fhirLocationDao.searchForLocations(null, null, null, null, null, null,
		    parentLocation, null);
		
		assertThat(locations, notNullValue());
		assertThat(locations.size(), equalTo(1));
		assertThat(locations.iterator().next().getParentLocation().getCountry(), equalTo(LOCATION_PARENT_COUNTRY));
	}
	
	@Test
	public void searchForLocations_shouldReturnCorrectLocationByParentPostalCode() {
		ReferenceAndListParam parentLocation = new ReferenceAndListParam().addAnd(new ReferenceOrListParam()
		        .add(new ReferenceParam().setValue(LOCATION_PARENT_POSTAL_CODE).setChain("address-postalcode")));
		Collection<Location> locations = fhirLocationDao.searchForLocations(null, null, null, null, null, null,
		    parentLocation, null);
		
		assertThat(locations, notNullValue());
		assertThat(locations.size(), equalTo(1));
		assertThat(locations.iterator().next().getParentLocation().getPostalCode(), equalTo(LOCATION_PARENT_POSTAL_CODE));
	}
	
	@Test
	public void searchForLocations_shouldReturnCorrectLocationByParentState() {
		ReferenceAndListParam parentLocation = new ReferenceAndListParam().addAnd(
		    new ReferenceOrListParam().add(new ReferenceParam().setValue(LOCATION_PARENT_STATE).setChain("address-state")));
		Collection<Location> locations = fhirLocationDao.searchForLocations(null, null, null, null, null, null,
		    parentLocation, null);
		
		assertThat(locations, notNullValue());
		assertThat(locations.size(), equalTo(1));
		assertThat(locations.iterator().next().getParentLocation().getStateProvince(), equalTo(LOCATION_PARENT_STATE));
	}
	
	@Test
	public void searchForLocations_shouldHandleComplexQuery() {
		StringAndListParam postalCode = new StringAndListParam()
		        .addAnd(new StringOrListParam().add(new StringParam(POSTAL_CODE)));
		StringAndListParam country = new StringAndListParam()
		        .addAnd(new StringOrListParam().add(new StringParam(LOCATION_COUNTRY)));
		Collection<Location> locations = fhirLocationDao.searchForLocations(null, null, country, postalCode, null, null,
		    null, null);
		
		assertThat(locations, notNullValue());
		assertThat(locations.size(), equalTo(2));
		assertThat(locations.iterator().next().getCountry(), equalTo(LOCATION_COUNTRY));
		assertThat(locations.iterator().next().getPostalCode(), equalTo(POSTAL_CODE));
	}
	
	@Test
	public void getActiveAttributesByLocationAndAttributeTypeUuid_shouldReturnLocationAttribute() {
		Location location = new Location();
		location.setUuid(LOCATION_UUID);
		
		List<LocationAttribute> attributeList = fhirLocationDao.getActiveAttributesByLocationAndAttributeTypeUuid(location,
		    LOCATION_ATTRIBUTE_TYPE_UUID);
		
		assertThat(attributeList, notNullValue());
	}
	
	@Test
	public void searchForLocations_shouldSortLocationsByNameAsRequested() {
		SortSpec sort = new SortSpec();
		sort.setParamName("name");
		sort.setOrder(SortOrderEnum.ASC);
		
		List<Location> resultsList = new ArrayList<>(getNonNullLocationListForSorting(sort));
		// check if the sorting is indeed correct by ascending order
		for (int i = 1; i < resultsList.size(); i++) {
			assertThat(resultsList.get(i - 1).getName(), lessThanOrEqualTo(resultsList.get(i).getName()));
		}
		
		sort.setOrder(SortOrderEnum.DESC);
		
		resultsList = new ArrayList<>(getNonNullLocationListForSorting(sort));
		// check if the sorting is indeed correct by descending order
		for (int i = 1; i < resultsList.size(); i++) {
			assertThat(resultsList.get(i - 1).getName(), greaterThanOrEqualTo(resultsList.get(i).getName()));
		}
	}
	
	@Test
	public void searchForLocations_shouldSortLocationsByCountryAsRequested() {
		SortSpec sort = new SortSpec();
		sort.setParamName("address-country");
		sort.setOrder(SortOrderEnum.ASC);
		
		List<Location> resultsList = new ArrayList<>(getNonNullLocationListForSorting(sort));
		// check if the sorting is indeed correct by ascending order
		for (int i = 1; i < resultsList.size(); i++) {
			assertThat(resultsList.get(i - 1).getCountry(), lessThanOrEqualTo(resultsList.get(i).getCountry()));
		}
		
		sort.setOrder(SortOrderEnum.DESC);
		
		resultsList = new ArrayList<>(getNonNullLocationListForSorting(sort));
		// check if the sorting is indeed correct by descending order
		for (int i = 1; i < resultsList.size(); i++) {
			assertThat(resultsList.get(i - 1).getCountry(), greaterThanOrEqualTo(resultsList.get(i).getCountry()));
		}
	}
	
	@Test
	public void searchForLocations_shouldSortLocationsByStateAsRequested() {
		SortSpec sort = new SortSpec();
		sort.setParamName("address-state");
		sort.setOrder(SortOrderEnum.ASC);
		
		List<Location> resultsList = new ArrayList<>(getNonNullLocationListForSorting(sort));
		// check if the sorting is indeed correct by ascending order
		for (int i = 1; i < resultsList.size(); i++) {
			assertThat(resultsList.get(i - 1).getStateProvince(), lessThanOrEqualTo(resultsList.get(i).getStateProvince()));
		}
		
		sort.setOrder(SortOrderEnum.DESC);
		
		resultsList = new ArrayList<>(getNonNullLocationListForSorting(sort));
		// check if the sorting is indeed correct by descending order
		for (int i = 1; i < resultsList.size(); i++) {
			assertThat(resultsList.get(i - 1).getStateProvince(),
			    greaterThanOrEqualTo(resultsList.get(i).getStateProvince()));
		}
	}
	
	@Test
	public void searchForLocations_shouldSortLocationsByPostalCodeAsRequested() {
		SortSpec sort = new SortSpec();
		sort.setParamName("address-postalCode");
		sort.setOrder(SortOrderEnum.ASC);
		
		List<Location> resultsList = new ArrayList<>(getNonNullLocationListForSorting(sort));
		// check if the sorting is indeed correct by ascending order
		for (int i = 1; i < resultsList.size(); i++) {
			assertThat(resultsList.get(i - 1).getPostalCode(), lessThanOrEqualTo(resultsList.get(i).getPostalCode()));
		}
		
		sort.setOrder(SortOrderEnum.DESC);
		
		resultsList = new ArrayList<>(getNonNullLocationListForSorting(sort));
		// check if the sorting is indeed correct by descending order
		for (int i = 1; i < resultsList.size(); i++) {
			assertThat(resultsList.get(i - 1).getPostalCode(), greaterThanOrEqualTo(resultsList.get(i).getPostalCode()));
		}
	}
	
	@Test
	public void searchForLocations_shouldSortLocationsByCityAsRequested() {
		SortSpec sort = new SortSpec();
		sort.setParamName("address-city");
		sort.setOrder(SortOrderEnum.ASC);
		
		List<Location> resultsList = new ArrayList<>(getNonNullLocationListForSorting(sort));
		// check if the sorting is indeed correct by ascending order
		for (int i = 1; i < resultsList.size(); i++) {
			assertThat(resultsList.get(i - 1).getCityVillage(), lessThanOrEqualTo(resultsList.get(i).getCityVillage()));
		}
		
		sort.setOrder(SortOrderEnum.DESC);
		
		resultsList = new ArrayList<>(getNonNullLocationListForSorting(sort));
		// check if the sorting is indeed correct by descending order
		for (int i = 1; i < resultsList.size(); i++) {
			assertThat(resultsList.get(i - 1).getCityVillage(), greaterThanOrEqualTo(resultsList.get(i).getCityVillage()));
		}
	}
	
	private List<Location> getNonNullLocationListForSorting(SortSpec sort) {
		Collection<Location> locations = fhirLocationDao.searchForLocations(null, null, null, null, null, null, null, sort);
		
		assertThat(locations, notNullValue());
		assertThat(locations, not(empty()));
		assertThat(locations.size(), greaterThan(1));
		
		List<Location> locationList = new ArrayList<>(locations);
		// Remove people with sort parameter value null, to allow comparison while asserting.
		switch (sort.getParamName()) {
			case "name":
				locationList.removeIf(p -> p.getName() == null);
				break;
			case "address-city":
				locationList.removeIf(p -> p.getCityVillage() == null);
				break;
			case "address-state":
				locationList.removeIf(p -> p.getStateProvince() == null);
				break;
			case "address-postalCode":
				locationList.removeIf(p -> p.getPostalCode() == null);
				break;
			case "address-country":
				locationList.removeIf(p -> p.getCountry() == null);
				break;
		}
		
		return locationList;
	}
}
