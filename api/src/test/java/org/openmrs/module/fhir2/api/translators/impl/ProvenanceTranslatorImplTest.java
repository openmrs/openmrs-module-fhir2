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
import static org.hamcrest.Matchers.nullValue;
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
import org.openmrs.Patient;
import org.openmrs.Person;
import org.openmrs.User;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.FhirTask;
import org.openmrs.module.fhir2.api.translators.PractitionerReferenceTranslator;

@RunWith(MockitoJUnitRunner.class)
public class ProvenanceTranslatorImplTest {
	
	private static final String TASK_UUID = "67a0a7c1-4bb0-4802-8905-d7c94f549bd4";
	
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
	
	private FhirTask task;
	
	private Person person;
	
	private User user;
	
	private ProvenanceTranslatorImpl<Person> personProvenanceTranslator;
	
	private ProvenanceTranslatorImpl<Patient> patientProvenanceTranslator;
	
	private ProvenanceTranslatorImpl<FhirTask> taskProvenanceTranslator;
	
	@Before
	public void setup() {
		personProvenanceTranslator = new ProvenanceTranslatorImpl<>();
		personProvenanceTranslator.setPractitionerReferenceTranslator(practitionerReferenceTranslator);
		
		patientProvenanceTranslator = new ProvenanceTranslatorImpl<>();
		patientProvenanceTranslator.setPractitionerReferenceTranslator(practitionerReferenceTranslator);
		
		taskProvenanceTranslator = new ProvenanceTranslatorImpl<>();
		taskProvenanceTranslator.setPractitionerReferenceTranslator(practitionerReferenceTranslator);
	}
	
	@Before
	public void initDummyObjects() {
		user = new User();
		user.setUuid(USER_UUID);
		
		person = new Person();
		person.setUuid(PERSON_UUID);
		person.setGender(GENDER);
		person.setCreator(user);
		person.setDateCreated(new Date());
		person.setChangedBy(user);
		person.setDateChanged(new Date());
		
		task = new FhirTask();
		task.setUuid(TASK_UUID);
		task.setCreator(user);
		task.setDateCreated(new Date());
		task.setChangedBy(user);
		task.setDateChanged(new Date());
	}
	
	@Test
	public void shouldGetCreateProvenanceForOpenmrsData() {
		Provenance provenance = personProvenanceTranslator.getCreateProvenance(person);
		assertThat(provenance, notNullValue());
		assertThat(provenance.getId(), notNullValue());
	}
	
	@Test
	public void shouldGetCreateProvenanceForOpenmrsMetadata() {
		Provenance provenance = taskProvenanceTranslator.getCreateProvenance(task);
		assertThat(provenance, notNullValue());
		assertThat(provenance.getId(), notNullValue());
	}
	
	@Test
	public void shouldGetCreateProvenanceForOpenmrsDataWithoutChange() {
		Person person = new Person();
		person.setUuid(PERSON_UUID);
		person.setCreator(user);
		person.setDateCreated(new Date());
		
		Provenance provenance = personProvenanceTranslator.getCreateProvenance(person);
		
		assertThat(provenance, notNullValue());
		assertThat(provenance.getId(), notNullValue());
	}
	
	@Test
	public void shouldGetCreateProvenanceForOpenmrsMetaWithoutChange() {
		FhirTask task = new FhirTask();
		task.setUuid(TASK_UUID);
		task.setCreator(user);
		task.setDateCreated(new Date());
		
		Provenance provenance = taskProvenanceTranslator.getCreateProvenance(task);
		
		assertThat(provenance, notNullValue());
		assertThat(provenance.getId(), notNullValue());
	}
	
	@Test
	public void shouldGetCreateProvenanceWithCorrectUpdateActivityForOpenmrsData() {
		Provenance provenance = personProvenanceTranslator.getCreateProvenance(person);
		assertThat(provenance, notNullValue());
		assertThat(provenance.getActivity().getCoding(), not(empty()));
		assertThat(provenance.getActivity().getCodingFirstRep().getCode(), equalTo(CREATE));
		assertThat(provenance.getActivity().getCodingFirstRep().getDisplay(), equalTo("create"));
		assertThat(provenance.getActivity().getCodingFirstRep().getSystem(),
		    equalTo(FhirConstants.FHIR_TERMINOLOGY_DATA_OPERATION));
	}
	
	@Test
	public void shouldGetCreateProvenanceWithCorrectUpdateActivityForOpenmrsMetadata() {
		Provenance provenance = taskProvenanceTranslator.getCreateProvenance(task);
		assertThat(provenance, notNullValue());
		assertThat(provenance.getActivity().getCoding(), not(empty()));
		assertThat(provenance.getActivity().getCodingFirstRep().getCode(), equalTo(CREATE));
		assertThat(provenance.getActivity().getCodingFirstRep().getDisplay(), equalTo("create"));
		assertThat(provenance.getActivity().getCodingFirstRep().getSystem(),
		    equalTo(FhirConstants.FHIR_TERMINOLOGY_DATA_OPERATION));
	}
	
	@Test
	public void shouldGetCreateProvenanceWithCorrectDateChangedForOpenmrsData() {
		Provenance provenance = personProvenanceTranslator.getCreateProvenance(person);
		assertThat(provenance, notNullValue());
		assertThat(provenance.getRecorded(), notNullValue());
		assertThat(provenance.getRecorded(), DateMatchers.sameDay(new Date()));
	}
	
	@Test
	public void shouldGetCreateProvenanceWithCorrectDateChangedForOpenmrsMetadata() {
		Provenance provenance = taskProvenanceTranslator.getCreateProvenance(task);
		assertThat(provenance, notNullValue());
		assertThat(provenance.getRecorded(), notNullValue());
		assertThat(provenance.getRecorded(), DateMatchers.sameDay(new Date()));
	}
	
	@Test
	public void shouldGetCreateProvenanceWithCorrectPractitionerReferenceForOpenmrsData() {
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
	public void shouldGetCreateProvenanceWithCorrectPractitionerReferenceForOpenmrsMetadata() {
		Reference practitionerRef = new Reference();
		practitionerRef.setReference(FhirConstants.PRACTITIONER + "/" + USER_UUID);
		
		when(practitionerReferenceTranslator.toFhirResource(user)).thenReturn(practitionerRef);
		Provenance provenance = taskProvenanceTranslator.getCreateProvenance(task);
		assertThat(provenance, notNullValue());
		assertThat(provenance.getAgent(), not(empty()));
		assertThat(provenance.getAgentFirstRep().getWho(), notNullValue());
		assertThat(provenance.getAgentFirstRep().getWho(), equalTo(practitionerRef));
	}
	
	@Test
	public void shouldGetCreateProvenanceWithCorrectAgentRoleForOpenmrsData() {
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
	public void shouldGetCreateProvenanceWithCorrectAgentRoleForOpenmrsMetadata() {
		Provenance provenance = taskProvenanceTranslator.getCreateProvenance(task);
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
	public void shouldGetCreateProvenanceWithCorrectAgentTypeForOpenmrsData() {
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
	public void shouldGetCreateProvenanceWithCorrectAgentTypeForOpenmrsMetadata() {
		Provenance provenance = taskProvenanceTranslator.getCreateProvenance(task);
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
	public void shouldGetUpdateProvenanceForOpenmrsData() {
		Provenance provenance = personProvenanceTranslator.getUpdateProvenance(person);
		assertThat(provenance, notNullValue());
		assertThat(provenance.getId(), notNullValue());
	}
	
	@Test
	public void shouldGetUpdateProvenanceForOpenmrsMetadata() {
		Provenance provenance = taskProvenanceTranslator.getUpdateProvenance(task);
		assertThat(provenance, notNullValue());
		assertThat(provenance.getId(), notNullValue());
	}
	
	@Test
	public void shouldGetUpdateProvenanceWithCorrectUpdateActivityForOpenmrsData() {
		Provenance provenance = personProvenanceTranslator.getUpdateProvenance(person);
		assertThat(provenance, notNullValue());
		assertThat(provenance.getActivity().getCoding(), not(empty()));
		assertThat(provenance.getActivity().getCodingFirstRep().getCode(), equalTo(UPDATE));
		assertThat(provenance.getActivity().getCodingFirstRep().getDisplay(), equalTo(REVISE));
		assertThat(provenance.getActivity().getCodingFirstRep().getSystem(),
		    equalTo(FhirConstants.FHIR_TERMINOLOGY_DATA_OPERATION));
	}
	
	@Test
	public void shouldGetUpdateProvenanceWithCorrectUpdateActivityForOpenmrsMetadata() {
		Provenance provenance = taskProvenanceTranslator.getUpdateProvenance(task);
		assertThat(provenance, notNullValue());
		assertThat(provenance.getActivity().getCoding(), not(empty()));
		assertThat(provenance.getActivity().getCodingFirstRep().getCode(), equalTo(UPDATE));
		assertThat(provenance.getActivity().getCodingFirstRep().getDisplay(), equalTo(REVISE));
		assertThat(provenance.getActivity().getCodingFirstRep().getSystem(),
		    equalTo(FhirConstants.FHIR_TERMINOLOGY_DATA_OPERATION));
	}
	
	@Test
	public void shouldGetUpdateProvenanceWithCorrectDateChangedForOpenmrsData() {
		Provenance provenance = personProvenanceTranslator.getUpdateProvenance(person);
		assertThat(provenance, notNullValue());
		assertThat(provenance.getRecorded(), notNullValue());
		assertThat(provenance.getRecorded(), DateMatchers.sameDay(new Date()));
	}
	
	@Test
	public void shouldGetUpdateProvenanceWithCorrectDateChangedForOpenmrsMetadata() {
		Provenance provenance = personProvenanceTranslator.getUpdateProvenance(person);
		assertThat(provenance, notNullValue());
		assertThat(provenance.getRecorded(), notNullValue());
		assertThat(provenance.getRecorded(), DateMatchers.sameDay(new Date()));
	}
	
	@Test
	public void shouldGetUpdateProvenanceWithCorrectPractitionerReferenceForOpenmrsData() {
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
	public void shouldGetUpdateProvenanceWithCorrectPractitionerReferenceForOpenmrsMetadata() {
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
	public void shouldGetUpdateProvenanceWithCorrectAgentRoleForOpenmrsData() {
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
	public void shouldGetUpdateProvenanceWithCorrectAgentRoleForOpenmrsMetadata() {
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
	public void shouldGetUpdateProvenanceWithCorrectAgentTypeForOpenmrsData() {
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
	
	@Test
	public void shouldGetUpdateProvenanceWithCorrectAgentTypeForOpenmrsMetadata() {
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
	
	@Test
	public void shouldNotCreateUpdateActivityProvenanceWhenDateChangedAndChangedByIsNull() {
		Patient patient = new Patient();
		Provenance result = patientProvenanceTranslator.getUpdateProvenance(patient);
		
		assertThat(result, nullValue());
	}
	
	@Test
	public void getUpdateProvenance_shouldReturnNullIfDateChangedAndChangedByAreNull() {
		person.setChangedBy(null);
		person.setDateChanged(null);
		
		assertThat(personProvenanceTranslator.getUpdateProvenance(person), nullValue());
	}
}
