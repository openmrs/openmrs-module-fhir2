/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.fhir2.providers.r3;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;

import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import org.hl7.fhir.dstu3.model.IdType;
import org.hl7.fhir.dstu3.model.RelatedPerson;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openmrs.module.fhir2.api.FhirRelatedPersonService;

@RunWith(MockitoJUnitRunner.class)
public class RelatedPersonFhirResourceProviderTest {
	
	private static final String RELATED_PERSON_UUID = "23f620c3-2ecb-4d80-aea8-44fa1c5ff978";
	
	private static final String WRONG_RELATED_PERSON_UUID = "ca0dfd38-ee20-41a6-909e-7d84247ca192";
	
	@Mock
	private FhirRelatedPersonService relatedPersonService;
	
	private org.hl7.fhir.r4.model.RelatedPerson relatedPerson;
	
	private RelatedPersonFhirResourceProvider resourceProvider;
	
	@Before
	public void setUp() {
		resourceProvider = new RelatedPersonFhirResourceProvider();
		resourceProvider.setRelatedPersonService(relatedPersonService);
	}
	
	@Before
	public void initRelatedPerson() {
		relatedPerson = new org.hl7.fhir.r4.model.RelatedPerson();
		relatedPerson.setId(RELATED_PERSON_UUID);
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
}
