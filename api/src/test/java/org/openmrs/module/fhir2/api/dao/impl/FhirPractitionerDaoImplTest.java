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
import static org.junit.Assert.fail;
import static org.openmrs.util.PrivilegeConstants.GET_PROVIDERS;

import java.util.Arrays;
import java.util.List;

import org.hibernate.SessionFactory;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.Provider;
import org.openmrs.ProviderAttribute;
import org.openmrs.api.APIAuthenticationException;
import org.openmrs.api.context.Context;
import org.openmrs.module.fhir2.BaseFhirContextSensitiveTest;
import org.openmrs.module.fhir2.api.dao.FhirPractitionerDao;
import org.openmrs.module.fhir2.api.search.param.SearchParameterMap;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

public class FhirPractitionerDaoImplTest extends BaseFhirContextSensitiveTest {
	
	private static final String PRACTITIONER_INITIAL_DATA_XML = "org/openmrs/module/fhir2/api/dao/impl/FhirPractitionerDaoImplTest_initial_data.xml";
	
	private static final String PRACTITIONER_UUID = "f9badd80-ab76-11e2-9e96-0800200c9a66";
	
	private static final String PERSON_ATTRIBUTE_TYPE_UUID = "FF89DD99-OOX78-KKG89D-XX89CC8";
	
	@Autowired
	@Qualifier("sessionFactory")
	private SessionFactory sessionFactory;
	
	@Autowired
	private ObjectFactory<FhirPractitionerDao> daoFactory;
	
	private FhirPractitionerDao dao;
	
	private FhirPractitionerDaoImpl daoImpl;
	
	@Before
	public void setUp() throws Exception {
		dao = daoFactory.getObject();
		daoImpl = new FhirPractitionerDaoImpl();
		daoImpl.setSessionFactory(sessionFactory);
		executeDataSet(PRACTITIONER_INITIAL_DATA_XML);
	}
	
	@Test
	public void shouldRetrievePractitionerByUuid() {
		org.openmrs.Provider provider = dao.get(PRACTITIONER_UUID);
		assertThat(provider, notNullValue());
		assertThat(provider.getUuid(), notNullValue());
		assertThat(provider.getUuid(), equalTo(PRACTITIONER_UUID));
	}
	
	@Test
	public void getActiveAttributesByPractitionerAndAttributeTypeUuid_shouldReturnPractitionerAttribute() {
		org.openmrs.Provider provider = new org.openmrs.Provider();
		provider.setUuid(PRACTITIONER_UUID);
		
		List<ProviderAttribute> attributeList = dao.getActiveAttributesByPractitionerAndAttributeTypeUuid(provider,
		    PERSON_ATTRIBUTE_TYPE_UUID);
		
		assertThat(attributeList, notNullValue());
	}
	
	@Test
	public void shouldRequireGetProvidersPrivilegeForGet() {
		Context.logout();
		
		try {
			dao.get(PRACTITIONER_UUID);
			fail("Expected APIAuthenticationException for missing privilege, but it was not thrown");
		}
		catch (APIAuthenticationException e) {
			assertThat(e.getMessage(), containsString("Privilege"));
		}
		
		try {
			Context.addProxyPrivilege(GET_PROVIDERS);
			assertThat(dao.get(PRACTITIONER_UUID), notNullValue());
		}
		finally {
			Context.removeProxyPrivilege(GET_PROVIDERS);
		}
	}
	
	@Test
	public void shouldRequireGetProvidersPrivilegeForGetByCollection() {
		Context.logout();
		
		try {
			dao.get(Arrays.asList(PRACTITIONER_UUID));
			fail("Expected APIAuthenticationException for missing privilege, but it was not thrown");
		}
		catch (APIAuthenticationException e) {
			assertThat(e.getMessage(), containsString("Privilege"));
		}
		
		try {
			Context.addProxyPrivilege(GET_PROVIDERS);
			List<Provider> providers = dao.get(Arrays.asList(PRACTITIONER_UUID));
			assertThat(providers, notNullValue());
		}
		finally {
			Context.removeProxyPrivilege(GET_PROVIDERS);
		}
	}
	
	@Test
	public void shouldRequireGetProvidersPrivilegeForGetSearchResults() {
		Context.logout();
		
		try {
			dao.getSearchResults(new SearchParameterMap());
			fail("Expected APIAuthenticationException for missing privilege, but it was not thrown");
		}
		catch (APIAuthenticationException e) {
			assertThat(e.getMessage(), containsString("Privilege"));
		}
		
		try {
			Context.addProxyPrivilege(GET_PROVIDERS);
			List<Provider> providers = dao.getSearchResults(new SearchParameterMap());
			assertThat(providers, notNullValue());
		}
		finally {
			Context.removeProxyPrivilege(GET_PROVIDERS);
		}
	}
	
	@Test
	public void shouldRequireGetProvidersPrivilegeForGetSearchResultsCount() {
		Context.logout();
		
		try {
			dao.getSearchResultsCount(new SearchParameterMap());
			fail("Expected APIAuthenticationException for missing privilege, but it was not thrown");
		}
		catch (APIAuthenticationException e) {
			assertThat(e.getMessage(), containsString("Privilege"));
		}
		
		try {
			Context.addProxyPrivilege(GET_PROVIDERS);
			int count = dao.getSearchResultsCount(new SearchParameterMap());
			assertThat(count, notNullValue());
		}
		finally {
			Context.removeProxyPrivilege(GET_PROVIDERS);
		}
	}
}
