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

import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ca.uhn.fhir.rest.param.StringAndListParam;
import ca.uhn.fhir.rest.param.StringParam;
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

	@Autowired
	@Qualifier("obsService")
	private ObsService obsService;

	private FhirMediaDaoImpl dao;
	
	@Before
	public void setup() throws Exception {
		executeDataSet(OBS_DATA_XML);
		dao = new FhirMediaDaoImpl();
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
	}

	@Test
	public void createOrUpdate_shouldcreateOrUpdateObs() {
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
		person.setId(1);
		person.setDateCreated(new Date());
		person.setGender("Male");
		obs.setObsId(1);
		obs.setDateCreated(new Date());
		obs.setPerson(person);
		obs.setObsDatetime(new Date());

		concept.setConceptId(1);
		concept.setCreator(new User());
		concept.setDatatype(new ConceptDatatype());
		obs.setConcept(concept);
		dao.createOrUpdate(obs);
		assertThat(obs.getPerson().getNames(), notNullValue());
	}

//	@Test
	public void delete_shouldDeleteObs(){
		Obs obs = dao.get(OBS_UUID);
		assertThat(obs, notNullValue());

		dao.delete(OBS_UUID);
		assertThat(obs, is(null));
	}

//	@Test
	public void search_ShouldReturnSearchQuery() {
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
