/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.fhir2.providers;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.mockito.Mockito.when;

import java.util.Collections;

import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.HumanName;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Practitioner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openmrs.module.fhir2.api.FhirPractitionerService;

@RunWith(MockitoJUnitRunner.class)
public class PractitionerFhirResourceProviderTest {

	private static final String PRACTITIONER_UUID = "48fb709b-48aa-4902-b681-926df5156e88";

	private static final String WRONG_PRACTITIONER_UUID = "f8bc0122-21db-4e91-a5d3-92ae01cafe92";

	private static final String GIVEN_NAME = "James";

	private static final String FAMILY_NAME = "pope";

	private static final String WRONG_NAME = "wrong name";

	private static final String PRACTITIONER_IDENTIFIER = "nurse";

	private static final String WRONG_PRACTITIONER_IDENTIFIER = "wrong identifier";

	@Mock
	private FhirPractitionerService practitionerService;

	private PractitionerFhirResourceProvider resourceProvider;

	private Practitioner practitioner;

	@Before
	public void setup() {
		resourceProvider = new PractitionerFhirResourceProvider();
		resourceProvider.setPractitionerService(practitionerService);
	}

	@Before
	public void initPractitioner() {
		HumanName name = new HumanName();
		name.addGiven(GIVEN_NAME);
		name.setFamily(FAMILY_NAME);

		Identifier theIdentifier = new Identifier();
		theIdentifier.setValue(PRACTITIONER_IDENTIFIER);

		practitioner = new Practitioner();
		practitioner.setId(PRACTITIONER_UUID);
		practitioner.addName(name);
		practitioner.addIdentifier(theIdentifier);
	}

	@Test
	public void getResourceType_shouldReturnResourceType() {
		assertThat(resourceProvider.getResourceType(), equalTo(Practitioner.class));
		assertThat(resourceProvider.getResourceType().getName(), equalTo(Practitioner.class.getName()));
	}

	@Test
	public void getPractitionerById_shouldReturnPractitioner() {
		IdType id = new IdType();
		id.setValue(PRACTITIONER_UUID);
		when(practitionerService.getPractitionerByUuid(PRACTITIONER_UUID)).thenReturn(practitioner);

		Practitioner result = resourceProvider.getPractitionerById(id);
		assertThat(result.isResource(), is(true));
		assertThat(result, notNullValue());
		assertThat(result.getId(), notNullValue());
		assertThat(result.getId(), equalTo(PRACTITIONER_UUID));
	}

	@Test(expected = ResourceNotFoundException.class)
	public void getPractitionerByWithWrongId_shouldThrowResourceNotFoundException() {
		IdType idType = new IdType();
		idType.setValue(WRONG_PRACTITIONER_UUID);
		assertThat(resourceProvider.getPractitionerById(idType).isResource(), is(true));
		assertThat(resourceProvider.getPractitionerById(idType), nullValue());
	}

	@Test
	public void findPractitionersByName_shouldReturnMatchingBundleOfPractitioners() {
		when(practitionerService.findPractitionerByName(GIVEN_NAME)).thenReturn(Collections.singletonList(practitioner));

		Bundle results = resourceProvider.findPractitionersByName(GIVEN_NAME);
		assertThat(results, notNullValue());
		assertThat(results.isResource(), is(true));
		assertThat(results.getEntry().size(), greaterThanOrEqualTo(1));
		assertThat(results.getEntryFirstRep().getResource().getChildByName("name").hasValues(), is(true));
	}

	@Test
	public void findPractitionersByWrongName_shouldReturnBundleWithEmptyEntries() {
		Bundle results = resourceProvider.findPractitionersByName(WRONG_NAME);
		assertThat(results, notNullValue());
		assertThat(results.isResource(), is(true));
		assertThat(results.getEntry(), is(empty()));
	}

	@Test
	public void findPractitionersByIdentifier_shouldReturnMatchingBundleOfPractitioners() {
		when(practitionerService.findPractitionerByIdentifier(PRACTITIONER_IDENTIFIER)).thenReturn(Collections.singletonList(practitioner));

		Bundle results = resourceProvider.findPractitionersByIdentifier(PRACTITIONER_IDENTIFIER);
		assertThat(results, notNullValue());
		assertThat(results.isResource(), is(true));
		assertThat(results.getEntry().size(), greaterThanOrEqualTo(1));
		assertThat(results.getEntryFirstRep().getResource().getChildByName("identifier").hasValues(), is(true));
	}

	@Test
	public void findPractitionersByWrongIdentifier_shouldReturnBundleWithEmptyEntries() {
		Bundle results = resourceProvider.findPractitionersByIdentifier(WRONG_PRACTITIONER_IDENTIFIER);
		assertThat(results, notNullValue());
		assertThat(results.isResource(), is(true));
		assertThat(results.getEntry(), is(empty()));
	}
}
