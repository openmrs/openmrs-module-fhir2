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

import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.fail;
import static org.openmrs.util.PrivilegeConstants.GET_PERSONS;

import java.util.Arrays;
import java.util.List;

import org.hibernate.SessionFactory;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.Person;
import org.openmrs.PersonAttribute;
import org.openmrs.api.APIAuthenticationException;
import org.openmrs.api.context.Context;
import org.openmrs.module.fhir2.BaseFhirContextSensitiveTest;
import org.openmrs.module.fhir2.api.dao.FhirPersonDao;
import org.openmrs.module.fhir2.api.search.param.SearchParameterMap;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

public class FhirPersonDaoImplTest extends BaseFhirContextSensitiveTest {
	
	private static final String PERSON_UUID = "61b38324-e2fd-4feb-95b7-9e9a2a4400df";
	
	private static final String WRONG_PERSON_UUID = "wrong_person_uuid";
	
	private static final String PERSON_INITIAL_DATA_XML = "org/openmrs/module/fhir2/api/dao/impl/FhirPersonDaoImplTest_initial_data.xml";
	
	private static final String GIVEN_NAME = "John";
	
	private static final String PERSON_ATTRIBUTE_TYPE_UUID = "14d4f066-15f5-102d-96e4-000c29c2a5d7";
	
	@Autowired
	@Qualifier("sessionFactory")
	private SessionFactory sessionFactory;
	
	@Autowired
	private ObjectFactory<FhirPersonDao> daoFactory;
	
	private FhirPersonDao fhirPersonDao;
	
	private FhirPersonDaoImpl fhirPersonDaoImpl;
	
	@Before
	public void setup() throws Exception {
		fhirPersonDao = daoFactory.getObject();
		fhirPersonDaoImpl = new FhirPersonDaoImpl();
		fhirPersonDaoImpl.setSessionFactory(sessionFactory);
		executeDataSet(PERSON_INITIAL_DATA_XML);
	}
	
	@Test
	public void shouldReturnMatchingPerson() {
		Person person = fhirPersonDao.get(PERSON_UUID);
		assertThat(person, notNullValue());
		assertThat(person.getUuid(), equalTo(PERSON_UUID));
		assertThat(person.getGender(), equalTo("M"));
		assertThat(person.getGivenName(), equalTo(GIVEN_NAME));
	}
	
	@Test
	public void shouldReturnNullPersonForPersonNotFoundByUuid() {
		Person person = fhirPersonDao.get(WRONG_PERSON_UUID);
		assertThat(person, nullValue());
	}

    @Test
    public void shouldRequireGetPersonsPrivilegeForGet() {
        Context.logout();

        try {
            fhirPersonDao.get(PERSON_UUID);
            fail("Expected APIAuthenticationException for missing privilege, but it was not thrown");
        }
        catch (APIAuthenticationException e) {
            assertThat(e.getMessage(), containsString("Privilege"));
        }

        try {
            Context.addProxyPrivilege(GET_PERSONS);
            assertThat(fhirPersonDao.get(PERSON_UUID), notNullValue());
        }
        finally {
            Context.removeProxyPrivilege(GET_PERSONS);
        }
    }

    @Test
    public void shouldRequireGetPersonsPrivilegeForGetByCollection() {
        Context.logout();

        try {
            fhirPersonDao.get(Arrays.asList(PERSON_UUID));
            fail("Expected APIAuthenticationException for missing privilege, but it was not thrown");
        }
        catch (APIAuthenticationException e) {
            assertThat(e.getMessage(), containsString("Privilege"));
        }

        try {
            Context.addProxyPrivilege(GET_PERSONS);
            List<Person> persons = fhirPersonDao.get(Arrays.asList(PERSON_UUID));
            assertThat(persons, notNullValue());
        }
        finally {
            Context.removeProxyPrivilege(GET_PERSONS);
        }
    }

    @Test
    public void shouldRequireGetPersonsPrivilegeForGetSearchResults() {
        Context.logout();

        try {
            fhirPersonDao.getSearchResults(new SearchParameterMap());
            fail("Expected APIAuthenticationException for missing privilege, but it was not thrown");
        }
        catch (APIAuthenticationException e) {
            assertThat(e.getMessage(), containsString("Privilege"));
        }

        try {
            Context.addProxyPrivilege(GET_PERSONS);
            List<Person> persons = fhirPersonDao.getSearchResults(new SearchParameterMap());
            assertThat(persons, notNullValue());
        }
        finally {
            Context.removeProxyPrivilege(GET_PERSONS);
        }
    }

    @Test
    public void shouldRequireGetPersonsPrivilegeForGetSearchResultsCount() {
        Context.logout();

        try {
            fhirPersonDao.getSearchResultsCount(new SearchParameterMap());
            fail("Expected APIAuthenticationException for missing privilege, but it was not thrown");
        }
        catch (APIAuthenticationException e) {
            assertThat(e.getMessage(), containsString("Privilege"));
        }

        try {
            Context.addProxyPrivilege(GET_PERSONS);
            int count = fhirPersonDao.getSearchResultsCount(new SearchParameterMap());
            assertThat(count, notNullValue());
        }
        finally {
            Context.removeProxyPrivilege(GET_PERSONS);
        }
    }
	
	@Test
	public void getActiveAttributesByPersonAndAttributeTypeUuid_shouldReturnPersonAttribute() {
		Person person = new Person();
		person.setUuid(PERSON_UUID);
		
		List<PersonAttribute> attributeList = fhirPersonDao.getActiveAttributesByPersonAndAttributeTypeUuid(person,
		    PERSON_ATTRIBUTE_TYPE_UUID);
		
		assertThat(attributeList, notNullValue());
	}
	
	@Test
	public void delete_shouldVoidPerson() {
		Person person = fhirPersonDao.delete(PERSON_UUID);
		
		assertThat(person.getVoided(), equalTo(true));
		assertThat(person.getDateVoided(), not(nullValue()));
		assertThat(person.getVoidedBy(), equalTo(Context.getAuthenticatedUser()));
		assertThat(person.getVoidReason(), equalTo("Voided via FHIR API"));
	}

}
