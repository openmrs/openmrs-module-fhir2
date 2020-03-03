/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.fhir2.api.translators.impl;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.Mockito.when;

import java.util.Date;

import org.exparity.hamcrest.date.DateMatchers;
import org.hl7.fhir.r4.model.Provenance;
import org.hl7.fhir.r4.model.Reference;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openmrs.Person;
import org.openmrs.User;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.api.translators.PractitionerReferenceTranslator;

@RunWith(MockitoJUnitRunner.class)
public class ProvenanceTranslatorImplTest {
	
	private static final String PERSON_UUID = "ghc25312-9798-4e6c-b8f8-269f2dd07cfe";
	
	private static final String USER_UUID = "ddc25312-9798-4e6c-b8f8-269f2dd07cfd";
	
	private static final String CREATE = "CREATE";
	
	private static final String UPDATE = "UPDATE";
	
	private static final String REVISE = "revise";
	
	private static final String AGENT_TYPE_CODE = "author";
	
	private static final String AGENT_TYPE_DISPLAY = "Author";
	
	private static final String AGENT_ROLE_CODE = "AUT";
	
	private static final String AGENT_ROLE_DISPLAY = "author";
	
	private static final String GENDER = "M";
	
	@Mock
	private PractitionerReferenceTranslator<User> practitionerReferenceTranslator;
	
	private Person person;
	
	private User user;
	
	private ProvenanceTranslatorImpl<Person> personProvenanceTranslator;
	
	@Before
	public void setup() {
		personProvenanceTranslator = new ProvenanceTranslatorImpl<>();
		personProvenanceTranslator.setPractitionerReferenceTranslator(practitionerReferenceTranslator);
	}
	
	@Before
	public void initPersonMock() {
		user = new User();
		user.setUuid(USER_UUID);
		
		person = new Person();
		person.setUuid(PERSON_UUID);
		person.setGender(GENDER);
		person.setCreator(user);
		person.setDateCreated(new Date());
		person.setChangedBy(user);
		person.setDateChanged(new Date());
	}
	
	@Test
	public void shouldGetCreateProvenance() {
		Provenance provenance = personProvenanceTranslator.getCreateProvenance(person);
		assertThat(provenance, notNullValue());
		assertThat(provenance.getId(), notNullValue());
	}
	
	@Test
	public void shouldGetCreateProvenanceWithCorrectUpdateActivity() {
		Provenance provenance = personProvenanceTranslator.getCreateProvenance(person);
		assertThat(provenance, notNullValue());
		assertThat(provenance.getActivity().getCoding(), not(empty()));
		assertThat(provenance.getActivity().getCodingFirstRep().getCode(), equalTo(CREATE));
		assertThat(provenance.getActivity().getCodingFirstRep().getDisplay(), equalTo("create"));
		assertThat(provenance.getActivity().getCodingFirstRep().getSystem(),
		    equalTo(FhirConstants.FHIR_TERMINOLOGY_DATA_OPERATION));
	}
	
	@Test
	public void shouldGetCreateProvenanceWithCorrectDateChanged() {
		Provenance provenance = personProvenanceTranslator.getCreateProvenance(person);
		assertThat(provenance, notNullValue());
		assertThat(provenance.getRecorded(), notNullValue());
		assertThat(provenance.getRecorded(), DateMatchers.sameDay(new Date()));
	}
	
	@Test
	public void shouldGetCreateProvenanceWithCorrectPractitionerReference() {
		Reference practitionerRef = new Reference();
		practitionerRef.setReference(FhirConstants.PRACTITIONER + "/" + USER_UUID);
		
		when(practitionerReferenceTranslator.toFhirResource(user)).thenReturn(practitionerRef);
		Provenance provenance = personProvenanceTranslator.getCreateProvenance(person);
		assertThat(provenance, notNullValue());
		assertThat(provenance.getAgent(), not(empty()));
		assertThat(provenance.getAgentFirstRep().getWho(), notNullValue());
		assertThat(provenance.getAgentFirstRep().getWho(), equalTo(practitionerRef));
	}
	
	@Test
	public void shouldGetCreateProvenanceWithCorrectAgentRole() {
		Provenance provenance = personProvenanceTranslator.getCreateProvenance(person);
		assertThat(provenance, notNullValue());
		assertThat(provenance.getAgent(), not(empty()));
		assertThat(provenance.getAgentFirstRep().getRoleFirstRep(), notNullValue());
		assertThat(provenance.getAgentFirstRep().getRoleFirstRep().getCodingFirstRep(), notNullValue());
		assertThat(provenance.getAgentFirstRep().getRoleFirstRep().getCodingFirstRep().getCode(), equalTo(AGENT_ROLE_CODE));
		assertThat(provenance.getAgentFirstRep().getRoleFirstRep().getCodingFirstRep().getDisplay(),
		    equalTo(AGENT_ROLE_DISPLAY));
		assertThat(provenance.getAgentFirstRep().getRoleFirstRep().getCodingFirstRep().getSystem(),
		    equalTo(FhirConstants.FHIR_TERMINOLOGY_PARTICIPATION_TYPE));
	}
	
	@Test
	public void shouldGetCreateProvenanceWithCorrectAgentType() {
		Provenance provenance = personProvenanceTranslator.getCreateProvenance(person);
		assertThat(provenance, notNullValue());
		assertThat(provenance.getAgent(), not(empty()));
		assertThat(provenance.getAgentFirstRep().getType(), notNullValue());
		assertThat(provenance.getAgentFirstRep().getType().getCodingFirstRep(), notNullValue());
		assertThat(provenance.getAgentFirstRep().getType().getCodingFirstRep().getCode(), equalTo(AGENT_TYPE_CODE));
		assertThat(provenance.getAgentFirstRep().getType().getCodingFirstRep().getDisplay(), equalTo(AGENT_TYPE_DISPLAY));
		assertThat(provenance.getAgentFirstRep().getType().getCodingFirstRep().getSystem(),
		    equalTo(FhirConstants.FHIR_TERMINOLOGY_PROVENANCE_PARTICIPANT_TYPE));
	}
	
	@Test
	public void shouldGetUpdateProvenance() {
		Provenance provenance = personProvenanceTranslator.getUpdateProvenance(person);
		assertThat(provenance, notNullValue());
		assertThat(provenance.getId(), notNullValue());
	}
	
	@Test
	public void shouldGetUpdateProvenanceWithCorrectUpdateActivity() {
		Provenance provenance = personProvenanceTranslator.getUpdateProvenance(person);
		assertThat(provenance, notNullValue());
		assertThat(provenance.getActivity().getCoding(), not(empty()));
		assertThat(provenance.getActivity().getCodingFirstRep().getCode(), equalTo(UPDATE));
		assertThat(provenance.getActivity().getCodingFirstRep().getDisplay(), equalTo(REVISE));
		assertThat(provenance.getActivity().getCodingFirstRep().getSystem(),
		    equalTo(FhirConstants.FHIR_TERMINOLOGY_DATA_OPERATION));
	}
	
	@Test
	public void shouldGetUpdateProvenanceWithCorrectDateChanged() {
		Provenance provenance = personProvenanceTranslator.getUpdateProvenance(person);
		assertThat(provenance, notNullValue());
		assertThat(provenance.getRecorded(), notNullValue());
		assertThat(provenance.getRecorded(), DateMatchers.sameDay(new Date()));
	}
	
	@Test
	public void shouldGetUpdateProvenanceWithCorrectPractitionerReference() {
		Reference practitionerRef = new Reference();
		practitionerRef.setReference(FhirConstants.PRACTITIONER + "/" + USER_UUID);
		
		when(practitionerReferenceTranslator.toFhirResource(user)).thenReturn(practitionerRef);
		Provenance provenance = personProvenanceTranslator.getCreateProvenance(person);
		assertThat(provenance, notNullValue());
		assertThat(provenance.getAgent(), not(empty()));
		assertThat(provenance.getAgentFirstRep().getWho(), notNullValue());
		assertThat(provenance.getAgentFirstRep().getWho(), equalTo(practitionerRef));
	}
	
	@Test
	public void shouldGetUpdateProvenanceWithCorrectAgentRole() {
		Provenance provenance = personProvenanceTranslator.getUpdateProvenance(person);
		assertThat(provenance, notNullValue());
		assertThat(provenance.getAgent(), not(empty()));
		assertThat(provenance.getAgentFirstRep().getRoleFirstRep(), notNullValue());
		assertThat(provenance.getAgentFirstRep().getRoleFirstRep().getCodingFirstRep(), notNullValue());
		assertThat(provenance.getAgentFirstRep().getRoleFirstRep().getCodingFirstRep().getCode(), equalTo(AGENT_ROLE_CODE));
		assertThat(provenance.getAgentFirstRep().getRoleFirstRep().getCodingFirstRep().getDisplay(),
		    equalTo(AGENT_ROLE_DISPLAY));
		assertThat(provenance.getAgentFirstRep().getRoleFirstRep().getCodingFirstRep().getSystem(),
		    equalTo(FhirConstants.FHIR_TERMINOLOGY_PARTICIPATION_TYPE));
	}
	
	@Test
	public void shouldGetUpdateProvenanceWithCorrectAgentType() {
		Provenance provenance = personProvenanceTranslator.getUpdateProvenance(person);
		assertThat(provenance, notNullValue());
		assertThat(provenance.getAgent(), not(empty()));
		assertThat(provenance.getAgentFirstRep().getType(), notNullValue());
		assertThat(provenance.getAgentFirstRep().getType().getCodingFirstRep(), notNullValue());
		assertThat(provenance.getAgentFirstRep().getType().getCodingFirstRep().getCode(), equalTo(AGENT_TYPE_CODE));
		assertThat(provenance.getAgentFirstRep().getType().getCodingFirstRep().getDisplay(), equalTo(AGENT_TYPE_DISPLAY));
		assertThat(provenance.getAgentFirstRep().getType().getCodingFirstRep().getSystem(),
		    equalTo(FhirConstants.FHIR_TERMINOLOGY_PROVENANCE_PARTICIPANT_TYPE));
	}
}
