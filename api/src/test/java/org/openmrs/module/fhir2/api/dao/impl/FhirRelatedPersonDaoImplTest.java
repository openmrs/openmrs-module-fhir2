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
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

import org.hibernate.SessionFactory;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.Relationship;
import org.openmrs.module.fhir2.TestFhirSpringConfiguration;
import org.openmrs.test.BaseModuleContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;

@ContextConfiguration(classes = TestFhirSpringConfiguration.class, inheritLocations = false)
public class FhirRelatedPersonDaoImplTest extends BaseModuleContextSensitiveTest {
	
	private static final String RELATIONSHIP_UUID = "c3c91630-8563-481b-8efa-48e10c139a3d";
	
	private static final String BAD_RELATIONSHIP_UUID = "d4c91630-8563-481b-8efa-48e10c139w6e";
	
	private static final String RELATIONSHIP_DATA_XML = "org/openmrs/module/fhir2/api/dao/impl/FhirRelatedPersonDaoImplTest_initial_data.xml";
	
	private static final String PERSON_A_UUID = "61b38324-e2fd-4feb-95b7-9e9a2a4400df";
	
	private static final String PERSON_B_UUID = "5c521595-4e12-46b0-8248-b8f2d3697766";
	
	@Autowired
	@Qualifier("sessionFactory")
	private SessionFactory sessionFactory;
	
	private FhirRelatedPersonDaoImpl relatedPersonDao;
	
	@Before
	public void setup() throws Exception {
		relatedPersonDao = new FhirRelatedPersonDaoImpl();
		relatedPersonDao.setSessionFactory(sessionFactory);
		executeDataSet(RELATIONSHIP_DATA_XML);
	}
	
	@Test
	public void getRelationshipByUuid_shouldReturnMatchingRelationship() {
		Relationship relationship = relatedPersonDao.get(RELATIONSHIP_UUID);
		assertThat(relationship, notNullValue());
		assertThat(relationship.getUuid(), notNullValue());
		assertThat(relationship.getUuid(), equalTo(RELATIONSHIP_UUID));
	}
	
	@Test
	public void getRelationshipWithWrongUuid_shouldReturnNull() {
		Relationship relationship = relatedPersonDao.get(BAD_RELATIONSHIP_UUID);
		assertThat(relationship, nullValue());
	}
	
	@Test
	public void getRelationshipWithUuid_shouldReturnPersonAAndPersonB() {
		Relationship relationship = relatedPersonDao.get(RELATIONSHIP_UUID);
		assertThat(relationship, notNullValue());
		assertThat(relationship.getPersonA(), notNullValue());
		assertThat(relationship.getPersonB(), notNullValue());
		assertThat(relationship.getPersonA().getUuid(), equalTo(PERSON_A_UUID));
		assertThat(relationship.getPersonB().getUuid(), equalTo(PERSON_B_UUID));
	}
	
}
