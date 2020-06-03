/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.fhir2.api.search;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import ca.uhn.fhir.rest.api.SortOrderEnum;
import ca.uhn.fhir.rest.api.SortSpec;
import ca.uhn.fhir.rest.api.server.IBundleProvider;
import ca.uhn.fhir.rest.param.ReferenceAndListParam;
import ca.uhn.fhir.rest.param.ReferenceOrListParam;
import ca.uhn.fhir.rest.param.ReferenceParam;
import ca.uhn.fhir.rest.param.StringAndListParam;
import ca.uhn.fhir.rest.param.StringOrListParam;
import ca.uhn.fhir.rest.param.StringParam;
import ca.uhn.fhir.rest.param.TokenAndListParam;
import ca.uhn.fhir.rest.param.TokenOrListParam;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.Location;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.TestFhirSpringConfiguration;
import org.openmrs.module.fhir2.api.dao.FhirLocationDao;
import org.openmrs.module.fhir2.api.search.param.SearchParameterMap;
import org.openmrs.module.fhir2.api.translators.LocationTranslator;
import org.openmrs.test.BaseModuleContextSensitiveTest;
import org.openmrs.test.SkipBaseSetup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

@SkipBaseSetup
@ContextConfiguration(classes = TestFhirSpringConfiguration.class, inheritLocations = false)
public class LocationSearchQueryImplTest extends BaseModuleContextSensitiveTest {
	
	public static final String LOCATION_UUID = "c0938432-1691-11df-2222-8038u432aabd";
	
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
	
	private static final String LOCATION_INITIAL_DATA_XML = "org/openmrs/module/fhir2/api/dao/impl/FhirLocationDaoImplTest_initial_data.xml";
	
	@Autowired
	private FhirLocationDao fhirLocationDao;
	
	@Autowired
	private LocationTranslator translator;
	
	@Autowired
	SearchQuery<org.openmrs.Location, Location, FhirLocationDao, LocationTranslator> searchQuery;
	
	private static final int START_INDEX = 0;
	
	private static final int END_INDEX = 10;
	
	@Before
	public void setup() throws Exception {
		executeDataSet(INITIAL_XML_DATASET_PACKAGE_PATH);
		executeDataSet(LOCATION_INITIAL_DATA_XML);
	}
	
	private IBundleProvider search(SearchParameterMap theParams) {
		return searchQuery.getQueryResults(theParams, fhirLocationDao, translator);
	}
	
	private List<IBaseResource> get(IBundleProvider results) {
		return results.getResources(START_INDEX, END_INDEX);
	}
	
	@Test
	public void searchForLocations_shouldReturnCorrectLocationByName() {
		StringAndListParam location = new StringAndListParam()
		        .addAnd(new StringOrListParam().add(new StringParam(LOCATION_NAME)));
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.NAME_SEARCH_HANDLER, location);
		IBundleProvider locations = search(theParams);
		
		List<IBaseResource> resultList = get(locations);
		
		assertThat(locations, notNullValue());
		assertThat(resultList.size(), equalTo(1));
		assertThat(((Location) resultList.iterator().next()).getName(), equalTo(LOCATION_NAME));
	}
	
	@Test
	public void searchForLocations_shouldReturnEmptyCollectionWhenCalledWithUnknownName() {
		StringAndListParam location = new StringAndListParam()
		        .addAnd(new StringOrListParam().add(new StringParam(UNKNOWN_LOCATION_NAME)));
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.NAME_SEARCH_HANDLER, location);
		IBundleProvider locations = search(theParams);
		
		assertThat(locations, notNullValue());
		assertThat(get(locations).size(), equalTo(0));
	}
	
	@Test
	public void searchForLocations_shouldReturnCorrectLocationByCity() {
		StringAndListParam city = new StringAndListParam()
		        .addAnd(new StringOrListParam().add(new StringParam(LOCATION_CITY)));
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.CITY_SEARCH_HANDLER, city);
		IBundleProvider locations = search(theParams);
		
		List<IBaseResource> resultList = get(locations);
		
		assertThat(locations, notNullValue());
		assertThat(resultList.size(), equalTo(1));
		assertThat(((Location) resultList.iterator().next()).getAddress().getCity(), equalTo(LOCATION_CITY));
	}
	
	@Test
	public void searchForLocations_shouldReturnEmptyCollectionWhenCalledWithUnknownCity() {
		StringAndListParam city = new StringAndListParam()
		        .addAnd(new StringOrListParam().add(new StringParam(UNKNOWN_LOCATION_CITY)));
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.CITY_SEARCH_HANDLER, city);
		IBundleProvider locations = search(theParams);
		
		assertThat(locations, notNullValue());
		assertThat(get(locations).size(), equalTo(0));
	}
	
	@Test
	public void searchForLocations_shouldReturnCorrectLocationByCountry() {
		StringAndListParam country = new StringAndListParam()
		        .addAnd(new StringOrListParam().add(new StringParam(LOCATION_COUNTRY)));
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.COUNTRY_SEARCH_HANDLER, country);
		IBundleProvider locations = search(theParams);
		
		List<IBaseResource> resultList = get(locations);
		
		assertThat(locations, notNullValue());
		assertThat(resultList.size(), equalTo(2));
		assertThat(((Location) resultList.iterator().next()).getAddress().getCountry(), equalTo(LOCATION_COUNTRY));
	}
	
	@Test
	public void searchForLocations_shouldReturnEmptyCollectionWhenCalledWithUnknownCountry() {
		StringAndListParam country = new StringAndListParam()
		        .addAnd(new StringOrListParam().add(new StringParam(UNKNOWN_LOCATION_COUNTRY)));
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.COUNTRY_SEARCH_HANDLER, country);
		IBundleProvider locations = search(theParams);
		
		assertThat(locations, notNullValue());
		assertThat(get(locations).size(), equalTo(0));
	}
	
	@Test
	public void searchForLocations_shouldReturnCorrectLocationByPostalCode() {
		StringAndListParam postalCode = new StringAndListParam()
		        .addAnd(new StringOrListParam().add(new StringParam(POSTAL_CODE)));
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.POSTALCODE_SEARCH_HANDLER,
		    postalCode);
		IBundleProvider locations = search(theParams);
		
		List<IBaseResource> resultList = get(locations);
		
		assertThat(locations, notNullValue());
		assertThat(resultList.size(), equalTo(2));
		assertThat(((Location) resultList.iterator().next()).getAddress().getPostalCode(), equalTo(POSTAL_CODE));
	}
	
	@Test
	public void searchForLocations_shouldReturnEmptyCollectionWhenCalledWithUnknownPostalCode() {
		StringAndListParam postalCode = new StringAndListParam()
		        .addAnd(new StringOrListParam().add(new StringParam(UNKNOWN_POSTAL_CODE)));
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.POSTALCODE_SEARCH_HANDLER,
		    postalCode);
		IBundleProvider locations = search(theParams);
		
		assertThat(locations, notNullValue());
		assertThat(get(locations).size(), equalTo(0));
	}
	
	@Test
	public void searchForLocations_shouldReturnCorrectLocationByState() {
		StringAndListParam state = new StringAndListParam()
		        .addAnd(new StringOrListParam().add(new StringParam(LOCATION_STATE)));
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.STATE_SEARCH_HANDLER, state);
		IBundleProvider locations = search(theParams);
		
		List<IBaseResource> resultList = get(locations);
		
		assertThat(locations, notNullValue());
		assertThat(resultList.size(), equalTo(2));
		assertThat(((Location) resultList.iterator().next()).getAddress().getState(), equalTo(LOCATION_STATE));
	}
	
	@Test
	public void searchForLocations_shouldReturnEmptyCollectionWhenCalledWithUnknownState() {
		StringAndListParam state = new StringAndListParam()
		        .addAnd(new StringOrListParam().add(new StringParam(UNKNOWN_LOCATION_STATE)));
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.STATE_SEARCH_HANDLER, state);
		IBundleProvider locations = search(theParams);
		
		assertThat(locations, notNullValue());
		assertThat(get(locations).size(), equalTo(0));
	}
	
	@Test
	public void searchForLocations_shouldReturnLocationsContainingGivenTag() {
		TokenAndListParam locationTag = new TokenAndListParam()
		        .addAnd(new TokenOrListParam(FhirConstants.OPENMRS_FHIR_EXT_LOCATION_TAG, LOGIN_LOCATION_TAG_NAME));
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.TAG_SEARCH_HANDLER, locationTag);
		IBundleProvider locations = search(theParams);
		
		List<IBaseResource> resultList = get(locations);
		
		assertThat(locations, notNullValue());
		assertThat(resultList.size(), equalTo(2));
		assertThat(((Location) resultList.iterator().next()).getMeta().getTag().iterator().next().getCode(),
		    equalTo(LOGIN_LOCATION_TAG_NAME));
	}
	
	@Test
	public void searchForLocations_shouldReturnCorrectLocationByParentUUID() {
		ReferenceAndListParam parentLocation = new ReferenceAndListParam()
		        .addAnd(new ReferenceOrListParam().add(new ReferenceParam().setValue(LOCATION_PARENT_ID).setChain(null)));
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.LOCATION_REFERENCE_SEARCH_HANDLER,
		    parentLocation);
		IBundleProvider locations = search(theParams);
		
		List<IBaseResource> resultList = get(locations);
		
		assertThat(locations, notNullValue());
		assertThat(resultList.size(), equalTo(1));
		assertThat(resultList.iterator().next().getIdElement().getIdPart(), equalTo(LOCATION_UUID));
	}
	
	@Test
	public void searchForLocations_shouldReturnCorrectLocationByParentName() {
		ReferenceAndListParam parentLocation = new ReferenceAndListParam().addAnd(
		    new ReferenceOrListParam().add(new ReferenceParam().setValue(LOCATION_PARENT_NAME).setChain("name")));
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.LOCATION_REFERENCE_SEARCH_HANDLER,
		    parentLocation);
		IBundleProvider locations = search(theParams);
		
		List<IBaseResource> resultList = get(locations);
		
		assertThat(locations, notNullValue());
		assertThat(resultList.size(), equalTo(1));
		assertThat(resultList.iterator().next().getIdElement().getIdPart(), equalTo(LOCATION_UUID));
	}
	
	@Test
	public void searchForLocations_shouldReturnCorrectLocationByParentCity() {
		ReferenceAndListParam parentLocation = new ReferenceAndListParam().addAnd(
		    new ReferenceOrListParam().add(new ReferenceParam().setValue(LOCATION_PARENT_CITY).setChain("address-city")));
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.LOCATION_REFERENCE_SEARCH_HANDLER,
		    parentLocation);
		IBundleProvider locations = search(theParams);
		
		List<IBaseResource> resultList = get(locations);
		
		assertThat(locations, notNullValue());
		assertThat(resultList.size(), equalTo(1));
		assertThat(resultList.iterator().next().getIdElement().getIdPart(), equalTo(LOCATION_UUID));
	}
	
	@Test
	public void searchForLocations_shouldReturnCorrectLocationByParentCountry() {
		ReferenceAndListParam parentLocation = new ReferenceAndListParam().addAnd(new ReferenceOrListParam()
		        .add(new ReferenceParam().setValue(LOCATION_PARENT_COUNTRY).setChain("address-country")));
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.LOCATION_REFERENCE_SEARCH_HANDLER,
		    parentLocation);
		IBundleProvider locations = search(theParams);
		
		List<IBaseResource> resultList = get(locations);
		
		assertThat(locations, notNullValue());
		assertThat(resultList.size(), equalTo(1));
		assertThat(resultList.iterator().next().getIdElement().getIdPart(), equalTo(LOCATION_UUID));
	}
	
	@Test
	public void searchForLocations_shouldReturnCorrectLocationByParentPostalCode() {
		ReferenceAndListParam parentLocation = new ReferenceAndListParam().addAnd(new ReferenceOrListParam()
		        .add(new ReferenceParam().setValue(LOCATION_PARENT_POSTAL_CODE).setChain("address-postalcode")));
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.LOCATION_REFERENCE_SEARCH_HANDLER,
		    parentLocation);
		IBundleProvider locations = search(theParams);
		
		List<IBaseResource> resultList = get(locations);
		
		assertThat(locations, notNullValue());
		assertThat(resultList.size(), equalTo(1));
		assertThat(resultList.iterator().next().getIdElement().getIdPart(), equalTo(LOCATION_UUID));
	}
	
	@Test
	public void searchForLocations_shouldReturnCorrectLocationByParentState() {
		ReferenceAndListParam parentLocation = new ReferenceAndListParam().addAnd(
		    new ReferenceOrListParam().add(new ReferenceParam().setValue(LOCATION_PARENT_STATE).setChain("address-state")));
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.LOCATION_REFERENCE_SEARCH_HANDLER,
		    parentLocation);
		IBundleProvider locations = search(theParams);
		
		List<IBaseResource> resultList = get(locations);
		
		assertThat(locations, notNullValue());
		assertThat(resultList.size(), equalTo(1));
		assertThat(resultList.iterator().next().getIdElement().getIdPart(), equalTo(LOCATION_UUID));
	}
	
	@Test
	public void searchForLocations_shouldHandleComplexQuery() {
		StringAndListParam postalCode = new StringAndListParam()
		        .addAnd(new StringOrListParam().add(new StringParam(POSTAL_CODE)));
		StringAndListParam country = new StringAndListParam()
		        .addAnd(new StringOrListParam().add(new StringParam(LOCATION_COUNTRY)));
		SearchParameterMap theParams = new SearchParameterMap()
		        .addParameter(FhirConstants.POSTALCODE_SEARCH_HANDLER, postalCode)
		        .addParameter(FhirConstants.COUNTRY_SEARCH_HANDLER, country);
		IBundleProvider locations = search(theParams);
		
		List<IBaseResource> resultList = get(locations);
		
		assertThat(locations, notNullValue());
		assertThat(resultList.size(), equalTo(2));
		assertThat(((Location) resultList.iterator().next()).getAddress().getPostalCode(), equalTo(POSTAL_CODE));
		assertThat(((Location) resultList.iterator().next()).getAddress().getCountry(), equalTo(LOCATION_COUNTRY));
	}
	
	@Test
	public void searchForLocations_shouldSortLocationsByNameAsRequested() {
		SortSpec sort = new SortSpec();
		sort.setParamName("name");
		sort.setOrder(SortOrderEnum.ASC);
		
		List<Location> resultsList = getNonNullLocationListForSorting(sort);
		// check if the sorting is indeed correct by ascending order
		for (int i = 1; i < resultsList.size(); i++) {
			assertThat(resultsList.get(i - 1).getName(), lessThanOrEqualTo(resultsList.get(i).getName()));
		}
		
		sort.setOrder(SortOrderEnum.DESC);
		
		resultsList = getNonNullLocationListForSorting(sort);
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
		
		List<Location> resultsList = getNonNullLocationListForSorting(sort);
		// check if the sorting is indeed correct by ascending order
		for (int i = 1; i < resultsList.size(); i++) {
			assertThat(resultsList.get(i - 1).getAddress().getCountry(),
			    lessThanOrEqualTo(resultsList.get(i).getAddress().getCountry()));
		}
		
		sort.setOrder(SortOrderEnum.DESC);
		
		resultsList = getNonNullLocationListForSorting(sort);
		// check if the sorting is indeed correct by descending order
		for (int i = 1; i < resultsList.size(); i++) {
			assertThat(resultsList.get(i - 1).getAddress().getCountry(),
			    greaterThanOrEqualTo(resultsList.get(i).getAddress().getCountry()));
		}
	}
	
	@Test
	public void searchForLocations_shouldSortLocationsByStateAsRequested() {
		SortSpec sort = new SortSpec();
		sort.setParamName("address-state");
		sort.setOrder(SortOrderEnum.ASC);
		
		List<Location> resultsList = getNonNullLocationListForSorting(sort);
		// check if the sorting is indeed correct by ascending order
		for (int i = 1; i < resultsList.size(); i++) {
			assertThat(resultsList.get(i - 1).getAddress().getState(),
			    lessThanOrEqualTo(resultsList.get(i).getAddress().getState()));
		}
		
		sort.setOrder(SortOrderEnum.DESC);
		
		resultsList = getNonNullLocationListForSorting(sort);
		// check if the sorting is indeed correct by descending order
		for (int i = 1; i < resultsList.size(); i++) {
			assertThat(resultsList.get(i - 1).getAddress().getState(),
			    greaterThanOrEqualTo(resultsList.get(i).getAddress().getState()));
		}
	}
	
	@Test
	public void searchForLocations_shouldSortLocationsByPostalCodeAsRequested() {
		SortSpec sort = new SortSpec();
		sort.setParamName("address-postalCode");
		sort.setOrder(SortOrderEnum.ASC);
		
		List<Location> resultsList = getNonNullLocationListForSorting(sort);
		// check if the sorting is indeed correct by ascending order
		for (int i = 1; i < resultsList.size(); i++) {
			assertThat(resultsList.get(i - 1).getAddress().getPostalCode(),
			    lessThanOrEqualTo(resultsList.get(i).getAddress().getPostalCode()));
		}
		
		sort.setOrder(SortOrderEnum.DESC);
		
		resultsList = new ArrayList<>(getNonNullLocationListForSorting(sort));
		// check if the sorting is indeed correct by descending order
		for (int i = 1; i < resultsList.size(); i++) {
			assertThat(resultsList.get(i - 1).getAddress().getPostalCode(),
			    greaterThanOrEqualTo(resultsList.get(i).getAddress().getPostalCode()));
		}
	}
	
	@Test
	public void searchForLocations_shouldSortLocationsByCityAsRequested() {
		SortSpec sort = new SortSpec();
		sort.setParamName("address-city");
		sort.setOrder(SortOrderEnum.ASC);
		
		List<Location> resultsList = getNonNullLocationListForSorting(sort);
		// check if the sorting is indeed correct by ascending order
		for (int i = 1; i < resultsList.size(); i++) {
			assertThat(resultsList.get(i - 1).getAddress().getCity(),
			    lessThanOrEqualTo(resultsList.get(i).getAddress().getCity()));
		}
		
		sort.setOrder(SortOrderEnum.DESC);
		
		resultsList = new ArrayList<>(getNonNullLocationListForSorting(sort));
		// check if the sorting is indeed correct by descending order
		for (int i = 1; i < resultsList.size(); i++) {
			assertThat(resultsList.get(i - 1).getAddress().getCity(),
			    greaterThanOrEqualTo(resultsList.get(i).getAddress().getCity()));
		}
	}
	
	private List<Location> getNonNullLocationListForSorting(SortSpec sort) {
		SearchParameterMap theParams = new SearchParameterMap().setSortSpec(sort);
		IBundleProvider locations = search(theParams);
		
		assertThat(locations, notNullValue());
		assertThat(get(locations), not(empty()));
		assertThat(get(locations).size(), greaterThan(1));
		
		List<Location> locationList = (new ArrayList<>(get(locations))).stream().map(p -> (Location) p)
		        .collect(Collectors.toList());
		// Remove locations with sort parameter value null, to allow comparison while asserting.
		switch (sort.getParamName()) {
			case "name":
				locationList.removeIf(p -> p.getName() == null);
				break;
			case "address-city":
				locationList.removeIf(p -> p.getAddress().getCity() == null);
				break;
			case "address-state":
				locationList.removeIf(p -> p.getAddress().getState() == null);
				break;
			case "address-postalCode":
				locationList.removeIf(p -> p.getAddress().getPostalCode() == null);
				break;
			case "address-country":
				locationList.removeIf(p -> p.getAddress().getCountry() == null);
				break;
		}
		
		return locationList;
	}
}
