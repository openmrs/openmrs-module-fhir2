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
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.fail;
import static org.openmrs.util.PrivilegeConstants.GET_PERSONS;
import static org.openmrs.util.PrivilegeConstants.GET_RELATIONSHIPS;

import java.util.Arrays;
import java.util.List;

import org.hibernate.SessionFactory;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.Relationship;
import org.openmrs.api.APIAuthenticationException;
import org.openmrs.api.context.Context;
import org.openmrs.module.fhir2.BaseFhirContextSensitiveTest;
import org.openmrs.module.fhir2.api.dao.FhirRelatedPersonDao;
import org.openmrs.module.fhir2.api.search.param.SearchParameterMap;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

public class FhirRelatedPersonDaoImplTest extends BaseFhirContextSensitiveTest {
	
	private static final String RELATIONSHIP_UUID = "c3c91630-8563-481b-8efa-48e10c139a3d";
	
	private static final String BAD_RELATIONSHIP_UUID = "d4c91630-8563-481b-8efa-48e10c139w6e";
	
	private static final String RELATIONSHIP_DATA_XML = "org/openmrs/module/fhir2/api/dao/impl/FhirRelatedPersonDaoImplTest_initial_data.xml";
	
	private static final String PERSON_A_UUID = "61b38324-e2fd-4feb-95b7-9e9a2a4400df";
	
	private static final String PERSON_B_UUID = "5c521595-4e12-46b0-8248-b8f2d3697766";
	
	@Autowired
	@Qualifier("sessionFactory")
	private SessionFactory sessionFactory;
	
	@Autowired
	private ObjectFactory<FhirRelatedPersonDao> daoFactory;
	
	private FhirRelatedPersonDao dao;
	
	private FhirRelatedPersonDaoImpl daoImpl;
	
	@Before
	public void setup() throws Exception {
		executeDataSet(RELATIONSHIP_DATA_XML);
		
		dao = daoFactory.getObject();
		daoImpl = new FhirRelatedPersonDaoImpl();
		daoImpl.setSessionFactory(sessionFactory);
	}
	
	@Test
	public void getRelationshipByUuid_shouldReturnMatchingRelationship() {
		Relationship relationship = daoImpl.get(RELATIONSHIP_UUID);
		assertThat(relationship, notNullValue());
		assertThat(relationship.getUuid(), notNullValue());
		assertThat(relationship.getUuid(), equalTo(RELATIONSHIP_UUID));
	}
	
	@Test
	public void getRelationshipWithWrongUuid_shouldReturnNull() {
		Relationship relationship = daoImpl.get(BAD_RELATIONSHIP_UUID);
		assertThat(relationship, nullValue());
	}
	
	@Test
	public void getRelationshipWithUuid_shouldReturnPersonAAndPersonB() {
		Relationship relationship = daoImpl.get(RELATIONSHIP_UUID);
		assertThat(relationship, notNullValue());
		assertThat(relationship.getPersonA(), notNullValue());
		assertThat(relationship.getPersonB(), notNullValue());
		assertThat(relationship.getPersonA().getUuid(), equalTo(PERSON_A_UUID));
		assertThat(relationship.getPersonB().getUuid(), equalTo(PERSON_B_UUID));
	}
	
	@Test
	public void shouldRequireGetPersonsAndGetRelationshipsPrivilegesForGet() {
		Context.logout();
		
		try {
			dao.get(RELATIONSHIP_UUID);
			fail("Expected APIAuthenticationException for missing privileges, but it was not thrown");
		}
		catch (APIAuthenticationException e) {
			assertThat(e.getMessage(), containsString("Privilege"));
		}
		
		try {
			Context.addProxyPrivilege(GET_PERSONS);
			Context.addProxyPrivilege(GET_RELATIONSHIPS);
			assertThat(dao.get(RELATIONSHIP_UUID), notNullValue());
		}
		finally {
			Context.removeProxyPrivilege(GET_PERSONS);
			Context.removeProxyPrivilege(GET_RELATIONSHIPS);
		}
	}
	
	@Test
	public void shouldRequireGetPersonsAndGetRelationshipsPrivilegesForGetByCollection() {
		Context.logout();
		
		try {
			dao.get(Arrays.asList(RELATIONSHIP_UUID));
			fail("Expected APIAuthenticationException for missing privileges, but it was not thrown");
		}
		catch (APIAuthenticationException e) {
			assertThat(e.getMessage(), containsString("Privilege"));
		}
		
		try {
			Context.addProxyPrivilege(GET_PERSONS);
			Context.addProxyPrivilege(GET_RELATIONSHIPS);
			List<Relationship> relationships = dao.get(Arrays.asList(RELATIONSHIP_UUID));
			assertThat(relationships, notNullValue());
		}
		finally {
			Context.removeProxyPrivilege(GET_PERSONS);
			Context.removeProxyPrivilege(GET_RELATIONSHIPS);
		}
	}
	
	@Test
	public void shouldRequireGetPersonsAndGetRelationshipsPrivilegesForGetSearchResults() {
		Context.logout();
		
		try {
			dao.getSearchResults(new SearchParameterMap());
			fail("Expected APIAuthenticationException for missing privileges, but it was not thrown");
		}
		catch (APIAuthenticationException e) {
			assertThat(e.getMessage(), containsString("Privilege"));
		}
		
		try {
			Context.addProxyPrivilege(GET_PERSONS);
			Context.addProxyPrivilege(GET_RELATIONSHIPS);
			List<Relationship> relationships = dao.getSearchResults(new SearchParameterMap());
			assertThat(relationships, notNullValue());
		}
		finally {
			Context.removeProxyPrivilege(GET_PERSONS);
			Context.removeProxyPrivilege(GET_RELATIONSHIPS);
		}
	}
	
	@Test
	public void shouldRequireGetPersonsAndGetRelationshipsPrivilegesForGetSearchResultsCount() {
		Context.logout();
		
		try {
			dao.getSearchResultsCount(new SearchParameterMap());
			fail("Expected APIAuthenticationException for missing privileges, but it was not thrown");
		}
		catch (APIAuthenticationException e) {
			assertThat(e.getMessage(), containsString("Privilege"));
		}
		
		try {
			Context.addProxyPrivilege(GET_PERSONS);
			Context.addProxyPrivilege(GET_RELATIONSHIPS);
			int count = dao.getSearchResultsCount(new SearchParameterMap());
			assertThat(count, notNullValue());
		}
		finally {
			Context.removeProxyPrivilege(GET_PERSONS);
			Context.removeProxyPrivilege(GET_RELATIONSHIPS);
		}
	}
}
