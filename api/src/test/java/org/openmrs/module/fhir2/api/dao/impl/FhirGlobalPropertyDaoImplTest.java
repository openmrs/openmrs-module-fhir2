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

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;

import javax.inject.Inject;
import javax.inject.Provider;

import java.util.Map;

import org.hibernate.SessionFactory;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.GlobalProperty;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.TestFhirSpringConfiguration;
import org.openmrs.test.BaseModuleContextSensitiveTest;
import org.springframework.test.context.ContextConfiguration;

@ContextConfiguration(classes = TestFhirSpringConfiguration.class, inheritLocations = false)
public class FhirGlobalPropertyDaoImplTest extends BaseModuleContextSensitiveTest {
	
	private static final String PERSON_ATTRIBUE_TYPE_PROPERTY_VALUE = "1289123-230-23n210-11nj2";
	
	private static final String PERSON_ATTRIBUTE_TYPE_INITIAL_DATA_XML = "org/openmrs/module/fhir2/api/dao/impl/FhirGlobalPropertyDaoImplTest_initial_data.xml";
	
	private static final String PERSON_ATTRIBUTE_TYPE_PROPERTY = "fhir2.personAttributeTypeUuid";
	
	private static final String PERSON_ATTRIBUTE_TYPE_PROPERTY_NOT_FOUND = "fhir2.non-found";
	
	private static final String GLOBAL_PROPERTY_UUID = "cd9d0baa-a88b-4553-907a-b4ea7811ebf8";
	
	private static final String GLOBAL_PROPERTY_MODERATE = "1499AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
	
	@Inject
	private Provider<SessionFactory> sessionFactoryProvider;
	
	private FhirGlobalPropertyDaoImpl dao;
	
	@Before
	public void setUp() throws Exception {
		dao = new FhirGlobalPropertyDaoImpl();
		dao.setSessionFactory(sessionFactoryProvider.get());
		executeDataSet(PERSON_ATTRIBUTE_TYPE_INITIAL_DATA_XML);
	}
	
	@Test
	public void shouldRetrieveGlobalPropertyValueByProperty() {
		String globalProperty = dao.getGlobalProperty(PERSON_ATTRIBUTE_TYPE_PROPERTY);
		assertThat(globalProperty, notNullValue());
		assertThat(globalProperty, equalTo(PERSON_ATTRIBUE_TYPE_PROPERTY_VALUE));
	}
	
	@Test
	public void shouldReturnNullWhenGlobalPropertyNotFound() {
		String globalProperty = dao.getGlobalProperty(PERSON_ATTRIBUTE_TYPE_PROPERTY_NOT_FOUND);
		assertThat(globalProperty, nullValue());
	}
	
	@Test
	public void shouldReturnGlobalPropertyObjectWhenPropertyMatched() {
		GlobalProperty property = dao.getGlobalPropertyObject(PERSON_ATTRIBUTE_TYPE_PROPERTY);
		assertThat(property, notNullValue());
		assertThat(property.getUuid(), notNullValue());
		assertThat(property.getUuid(), equalTo(GLOBAL_PROPERTY_UUID));
	}
	
	@Test
	public void shouldReturnGlobalPropertyObjectWithTheCorrectValueWhenPropertyMatched() {
		GlobalProperty property = dao.getGlobalPropertyObject(PERSON_ATTRIBUTE_TYPE_PROPERTY);
		assertThat(property, notNullValue());
		assertThat(property.getPropertyValue(), notNullValue());
		assertThat(property.getPropertyValue(), equalTo(PERSON_ATTRIBUE_TYPE_PROPERTY_VALUE));
	}
	
	@Test
	public void shouldReturnNullGlobalPropertyObjectWhenPropertyNotMatched() {
		GlobalProperty property = dao.getGlobalPropertyObject(PERSON_ATTRIBUTE_TYPE_PROPERTY_NOT_FOUND);
		assertThat(property, nullValue());
	}
	
	@Test
	public void shouldReturnListOfGlobalPropertyValues() {
		Map<String, String> values = dao.getGlobalProperties(FhirConstants.GLOBAL_PROPERTY_MODERATE,
		    FhirConstants.GLOBAL_PROPERTY_SEVERE);
		assertThat(values, notNullValue());
		assertThat(values.size(), greaterThanOrEqualTo(1));
		assertThat(values.get(FhirConstants.GLOBAL_PROPERTY_MODERATE), equalTo(GLOBAL_PROPERTY_MODERATE));
	}
}
