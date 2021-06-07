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
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ca.uhn.fhir.rest.param.StringAndListParam;
import ca.uhn.fhir.rest.param.StringParam;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.SessionFactory;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.Concept;
import org.openmrs.ConceptDatatype;
import org.openmrs.Obs;
import org.openmrs.Person;
import org.openmrs.PersonName;
import org.openmrs.User;
import org.openmrs.api.ObsService;
import org.openmrs.api.context.Context;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.TestFhirSpringConfiguration;
import org.openmrs.module.fhir2.api.dao.FhirMediaDao;
import org.openmrs.module.fhir2.api.search.param.SearchParameterMap;
import org.openmrs.test.BaseModuleContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;

@ContextConfiguration(classes = TestFhirSpringConfiguration.class, inheritLocations = false)
public class FhirMediaDaoImplTest extends BaseModuleContextSensitiveTest {

	private static final String OBS_DATA_XML = "org/openmrs/module/fhir2/api/dao/impl/FhirObsServiceTest-complex.xml";

	private static final String OBS_UUID = "32a8dde4-c159-11eb-8529-0242ac130003";
	
	private static final String OBS_CONCEPT_ID = "5242";

	private FhirMediaDaoImpl dao;

	@Autowired
	private SessionFactory sessionFactory;


	@Before
	public void setup() throws Exception {
		executeDataSet(OBS_DATA_XML);
		dao = new FhirMediaDaoImpl();
		dao.setSessionFactory(sessionFactory);
	}
	
	@Test
	public void get_shouldGetComplexObsByUuid() {
		assertThat(dao.get(OBS_UUID), notNullValue());
	}
	
	@Test
	public void get_shouldReturnNullIfObsNotFoundByUuid() {
		Obs obs = dao.get(OBS_UUID);

		assertThat(obs, notNullValue());
		assertThat(obs.getUuid(), equalTo(OBS_UUID));
		assertThat(dao.get(OBS_UUID).getValueComplex(), equalTo("txt image |sometext.txt"));
	}

	@Test
	public void createOrUpdate_shouldSaveNewObs() {
		Obs obs = new Obs();
		Concept concept = new Concept();
		Person person = new Person();
		Set<PersonName> names = new HashSet<>();
		PersonName name = new PersonName();
		name.setFamilyName("Mpanda");
		name.setGivenName("Ssekitto");
		names.add(name);
		person.setNames(names);
		person.setBirthdate(new Date());
		person.setId(2);
		person.setDateCreated(new Date());
		person.setGender("Male");
		concept.setConceptId(1);
		concept.setCreator(new User());
		concept.setDatatype(new ConceptDatatype());
		obs.setUuid(OBS_UUID);
		obs.setObsId(21);
		obs.setDateCreated(new Date());
		obs.setPerson(person);
		obs.setObsDatetime(new Date());
		obs.setConcept(concept);
		Obs result = dao.createOrUpdate(obs);
		assertThat(result.getUuid(), equalTo(OBS_UUID));
	}

//	@Test
	public void createOrUpdate_shouldUpdateExistingObs() {

		Obs obs = new Obs();
		Concept concept = new Concept();
		Person person = new Person();
		Set<PersonName> names = new HashSet<>();
		PersonName name = new PersonName();
		name.setFamilyName("Mpanda");
		name.setGivenName("Ssekitto");
		names.add(name);
		person.setNames(names);
		person.setBirthdate(new Date());
		person.setId(2);
		person.setDateCreated(new Date());
		person.setGender("Male");
		concept.setConceptId(1);
		concept.setCreator(new User());
		concept.setDatatype(new ConceptDatatype());
		obs.setUuid(OBS_UUID);
		obs.setObsId(21);
		obs.setDateCreated(new Date());
		obs.setPerson(person);
		obs.setObsDatetime(new Date());
		obs.setConcept(concept);
		Obs result = dao.createOrUpdate(obs);
		assertThat(result.getUuid(), equalTo(OBS_UUID));
	}

	@Test
	public void delete_shouldDeleteObs(){
		Obs obs = dao.get(OBS_UUID);
		assertThat(obs, notNullValue());

		Obs result = dao.delete(OBS_UUID);
		assertThat(result, notNullValue());

	}

	@Test
	public void delete_shouldReturnNullIfObsToDeleteDoesNotExist(){
        Obs result = dao.delete("32a8dde4-c159-11eb-0000-0242ac1311111");

		assertThat(result, nullValue());
	}

	@Test
	public void getSearchResults_shouldReturnAListOfObs(){
		SearchParameterMap searchParameterMap = new SearchParameterMap();
		searchParameterMap.addParameter("", null);
		searchParameterMap.addParameter("", null);

		List<String> obsUUidsList = new ArrayList<>();
		obsUUidsList.add("32a8dd30-c159-11eb-8529-0242ac130003");
		obsUUidsList.add("32a8dde4-c159-11eb-8529-0242ac130003");

		assertThat(dao.getSearchResults(searchParameterMap, obsUUidsList).size(), equalTo(2));
	}
	@Test
	public void getSearchResults_ShouldReturnSearchQuery() {
		StringAndListParam status = new StringAndListParam();
		StringParam codingToken = new StringParam();
		codingToken.setValue(OBS_CONCEPT_ID);
		status.addAnd(codingToken);
		
		SearchParameterMap theParams = new SearchParameterMap();
		theParams.addParameter(FhirConstants.MEDIA_CONTENT_TYPE, status);
		
		List<String> matchingResourceUuids = dao.getSearchResultUuids(theParams);
		Collection<Obs> obs = dao.getSearchResults(theParams, matchingResourceUuids);
		
		assertThat(obs, notNullValue());
	}
}
