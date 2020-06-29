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
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;
import static org.mockito.hamcrest.MockitoHamcrest.argThat;

import java.util.Collections;
import java.util.List;

import ca.uhn.fhir.rest.api.server.IBundleProvider;
import ca.uhn.fhir.rest.param.DateRangeParam;
import ca.uhn.fhir.rest.param.StringAndListParam;
import ca.uhn.fhir.rest.param.StringOrListParam;
import ca.uhn.fhir.rest.param.StringParam;
import ca.uhn.fhir.rest.param.TokenAndListParam;
import ca.uhn.fhir.rest.param.TokenOrListParam;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import org.hamcrest.Matchers;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.Enumerations;
import org.hl7.fhir.r4.model.HumanName;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.RelatedPerson;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.api.FhirRelatedPersonService;
import org.openmrs.module.fhir2.providers.BaseFhirProvenanceResourceTest;

@RunWith(MockitoJUnitRunner.class)
public class RelatedPersonFhirResourceProviderTest extends BaseFhirProvenanceResourceTest<RelatedPerson> {
	
	private static final String RELATED_PERSON_UUID = "23f620c3-2ecb-4d80-aea8-44fa1c5ff978";
	
	private static final String WRONG_RELATED_PERSON_UUID = "ca0dfd38-ee20-41a6-909e-7d84247ca192";
	
	private static final String GENDER = "M";
	
	private static final String BIRTH_DATE = "1992-03-04";
	
	private static final String GIVEN_NAME = "Jeanne";
	
	private static final String FAMILY_NAME = "we";
	
	private static final String CITY = "Seattle";
	
	private static final String STATE = "Washington";
	
	private static final String POSTAL_CODE = "98136";
	
	private static final String COUNTRY = "Canada";
	
	private static final int PREFERRED_PAGE_SIZE = 10;
	
	private static final int COUNT = 1;
	
	private static final int START_INDEX = 0;
	
	private static final int END_INDEX = 10;
	
	@Mock
	private FhirRelatedPersonService relatedPersonService;
	
	private RelatedPerson relatedPerson;
	
	private RelatedPersonFhirResourceProvider resourceProvider;
	
	@Before
	public void setUp() {
		resourceProvider = new RelatedPersonFhirResourceProvider();
		resourceProvider.setRelatedPersonService(relatedPersonService);
	}
	
	@Before
	public void initRelatedPerson() {
		HumanName name = new HumanName();
		name.addGiven(GIVEN_NAME);
		name.setFamily(FAMILY_NAME);
		
		relatedPerson = new RelatedPerson();
		relatedPerson.setId(RELATED_PERSON_UUID);
		relatedPerson.setGender(Enumerations.AdministrativeGender.MALE);
		relatedPerson.addName(name);
		setProvenanceResources(relatedPerson);
	}
	
	private List<IBaseResource> get(IBundleProvider results) {
		return results.getResources(START_INDEX, END_INDEX);
	}
	
	@Test
	public void getResourceType_shouldReturnResourceType() {
		assertThat(resourceProvider.getResourceType(), equalTo(RelatedPerson.class));
		assertThat(resourceProvider.getResourceType().getName(), equalTo(RelatedPerson.class.getName()));
	}
	
	@Test
	public void getRelatedPersonById_shouldReturnMatchingRelatedPerson() {
		when(relatedPersonService.get(RELATED_PERSON_UUID)).thenReturn(relatedPerson);
		IdType id = new IdType();
		id.setValue(RELATED_PERSON_UUID);
		RelatedPerson relatedPerson = resourceProvider.getRelatedPersonById(id);
		assertThat(relatedPerson, notNullValue());
		assertThat(relatedPerson.getId(), notNullValue());
		assertThat(relatedPerson.getId(), equalTo(RELATED_PERSON_UUID));
	}
	
	@Test(expected = ResourceNotFoundException.class)
	public void getRelatedPersonWithWrongUuid_shouldThrowResourceNotFoundException() {
		IdType id = new IdType();
		id.setValue(WRONG_RELATED_PERSON_UUID);
		RelatedPerson result = resourceProvider.getRelatedPersonById(id);
		assertThat(result, nullValue());
	}
	
	@Test
	public void searchRelatedPeople_shouldReturnMatchingBundleOfRelatedPeopleByName() {
		StringAndListParam nameParam = new StringAndListParam()
		        .addAnd(new StringOrListParam().add(new StringParam(GIVEN_NAME)));
		when(relatedPersonService.searchForRelatedPeople(argThat(is(nameParam)), isNull(), isNull(), isNull(), isNull(),
		    isNull(), isNull(), isNull())).thenReturn(
		        new MockIBundleProvider<>(Collections.singletonList(relatedPerson), PREFERRED_PAGE_SIZE, COUNT));
		
		IBundleProvider results = resourceProvider.searchRelatedPerson(nameParam, null, null, null, null, null, null, null);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, Matchers.notNullValue());
		assertThat(resultList.iterator().next().fhirType(), equalTo(FhirConstants.RELATED_PERSON));
		assertThat(resultList.size(), greaterThanOrEqualTo(1));
	}
	
	@Test
	public void searchForPeople_shouldReturnMatchingBundleOfPeopleByGender() {
		TokenAndListParam genderParam = new TokenAndListParam().addAnd(new TokenOrListParam().add(GENDER));
		when(relatedPersonService.searchForRelatedPeople(isNull(), argThat(is(genderParam)), isNull(), isNull(), isNull(),
		    isNull(), isNull(), isNull())).thenReturn(
		        new MockIBundleProvider<>(Collections.singletonList(relatedPerson), PREFERRED_PAGE_SIZE, COUNT));
		
		IBundleProvider results = resourceProvider.searchRelatedPerson(null, genderParam, null, null, null, null, null,
		    null);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, Matchers.notNullValue());
		assertThat(resultList.iterator().next().fhirType(), equalTo(FhirConstants.RELATED_PERSON));
		assertThat(resultList.size(), greaterThanOrEqualTo(1));
	}
	
	@Test
	public void searchForRelatedPeople_shouldReturnMatchingBundleOfRelatedPeopleByBirthDate() {
		DateRangeParam birthDateParam = new DateRangeParam().setLowerBound(BIRTH_DATE).setUpperBound(BIRTH_DATE);
		when(relatedPersonService.searchForRelatedPeople(isNull(), isNull(), argThat(is(birthDateParam)), isNull(), isNull(),
		    isNull(), isNull(), isNull())).thenReturn(
		        new MockIBundleProvider<>(Collections.singletonList(relatedPerson), PREFERRED_PAGE_SIZE, COUNT));
		
		IBundleProvider results = resourceProvider.searchRelatedPerson(null, null, birthDateParam, null, null, null, null,
		    null);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, Matchers.notNullValue());
		assertThat(resultList.iterator().next().fhirType(), equalTo(FhirConstants.RELATED_PERSON));
		assertThat(resultList.size(), greaterThanOrEqualTo(1));
	}
	
	@Test
	public void searchForRelatedPeople_shouldReturnMatchingBundleOfRelatedPeopleByCity() {
		StringAndListParam cityParam = new StringAndListParam().addAnd(new StringOrListParam().add(new StringParam(CITY)));
		when(relatedPersonService.searchForRelatedPeople(isNull(), isNull(), isNull(), argThat(is(cityParam)), isNull(),
		    isNull(), isNull(), isNull())).thenReturn(
		        new MockIBundleProvider<>(Collections.singletonList(relatedPerson), PREFERRED_PAGE_SIZE, COUNT));
		
		IBundleProvider results = resourceProvider.searchRelatedPerson(null, null, null, cityParam, null, null, null, null);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, Matchers.notNullValue());
		assertThat(resultList.iterator().next().fhirType(), equalTo(FhirConstants.RELATED_PERSON));
		assertThat(resultList.size(), greaterThanOrEqualTo(1));
	}
	
	@Test
	public void searchForRelatedPeople_shouldReturnMatchingBundleOfRelatedPeopleByState() {
		StringAndListParam stateParam = new StringAndListParam().addAnd(new StringOrListParam().add(new StringParam(STATE)));
		when(relatedPersonService.searchForRelatedPeople(isNull(), isNull(), isNull(), isNull(), argThat(is(stateParam)),
		    isNull(), isNull(), isNull())).thenReturn(
		        new MockIBundleProvider<>(Collections.singletonList(relatedPerson), PREFERRED_PAGE_SIZE, COUNT));
		
		IBundleProvider results = resourceProvider.searchRelatedPerson(null, null, null, null, stateParam, null, null, null);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, Matchers.notNullValue());
		assertThat(resultList.iterator().next().fhirType(), equalTo(FhirConstants.RELATED_PERSON));
		assertThat(resultList.size(), greaterThanOrEqualTo(1));
	}
	
	@Test
	public void searchForRelatedPeople_shouldReturnMatchingBundleOfRelatedPeopleByPostalCode() {
		StringAndListParam postalCodeParam = new StringAndListParam()
		        .addAnd(new StringOrListParam().add(new StringParam(POSTAL_CODE)));
		when(relatedPersonService.searchForRelatedPeople(isNull(), isNull(), isNull(), isNull(), isNull(),
		    argThat(is(postalCodeParam)), isNull(), isNull())).thenReturn(
		        new MockIBundleProvider<>(Collections.singletonList(relatedPerson), PREFERRED_PAGE_SIZE, COUNT));
		
		IBundleProvider results = resourceProvider.searchRelatedPerson(null, null, null, null, null, postalCodeParam, null,
		    null);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, Matchers.notNullValue());
		assertThat(resultList.iterator().next().fhirType(), equalTo(FhirConstants.RELATED_PERSON));
		assertThat(resultList.size(), greaterThanOrEqualTo(1));
	}
	
	@Test
	public void searchForRelatedPeople_shouldReturnMatchingBundleOfRelatedPeopleByCountry() {
		StringAndListParam countryParam = new StringAndListParam()
		        .addAnd(new StringOrListParam().add(new StringParam(COUNTRY)));
		when(relatedPersonService.searchForRelatedPeople(isNull(), isNull(), isNull(), isNull(), isNull(), isNull(),
		    argThat(is(countryParam)), isNull())).thenReturn(
		        new MockIBundleProvider<>(Collections.singletonList(relatedPerson), PREFERRED_PAGE_SIZE, COUNT));
		
		IBundleProvider results = resourceProvider.searchRelatedPerson(null, null, null, null, null, null, countryParam,
		    null);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, Matchers.notNullValue());
		assertThat(resultList.iterator().next().fhirType(), equalTo(FhirConstants.RELATED_PERSON));
		assertThat(resultList.size(), greaterThanOrEqualTo(1));
	}
}
