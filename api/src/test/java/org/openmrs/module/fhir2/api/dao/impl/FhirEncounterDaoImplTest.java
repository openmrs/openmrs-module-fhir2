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
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

import java.util.Collection;

import ca.uhn.fhir.rest.param.DateParam;
import ca.uhn.fhir.rest.param.DateRangeParam;
import ca.uhn.fhir.rest.param.ReferenceAndListParam;
import ca.uhn.fhir.rest.param.ReferenceOrListParam;
import ca.uhn.fhir.rest.param.ReferenceParam;
import org.hamcrest.Matchers;
import org.hibernate.SessionFactory;
import org.hl7.fhir.r4.model.Location;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.Practitioner;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.Encounter;
import org.openmrs.module.fhir2.TestFhirSpringConfiguration;
import org.openmrs.test.BaseModuleContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;

@ContextConfiguration(classes = TestFhirSpringConfiguration.class, inheritLocations = false)
public class FhirEncounterDaoImplTest extends BaseModuleContextSensitiveTest {
	
	private static final String ENCOUNTER_UUID = "430bbb70-6a9c-4e1e-badb-9d1034b1b5e9";
	
	private static final String ENC_UUID = "e403fafb-e5e4-42d0-9d11-4f52e89d148c";
	
	private static final String UNKNOWN_ENCOUNTER_UUID = "xx923xx-3423kk-2323-232jk23";
	
	private static final String PATIENT_IDENTIFIER = "1000WF";
	
	private static final String WRONG_PATIENT_IDENTIFIER = "12334HD";
	
	private static final String PATIENT_FULL_NAME = "Mr. John Doe";
	
	private static final String PARTICIPANT_FULL_NAME = "John Doe";
	
	private static final String ENCOUNTER_INITIAL_DATA_XML = "org/openmrs/module/fhir2/api/dao/impl/FhirEncounterDaoImplTest_initial_data.xml";
	
	private static final String ENCOUNTER_DATETIME = "2005-01-01T00:00:00.0";
	
	private static final String ENCOUNTER_DATE = "2005-01-01 00:00:00.0";
	
	private static final String PATIENT_GIVEN_NAME = "John";
	
	private static final String PATIENT_FAMILY_NAME = "Doe";
	
	private static final String ENCOUNTER_ADDRESS_CITY = "Boston";
	
	private static final String ENCOUNTER_ADDRESS_COUNTRY = "USA";
	
	private static final String ENCOUNTER_ADDRESS_STATE = "MA";
	
	private static final String PARTICIPANT_IDENTIFIER = "1000WF";
	
	private static final String PARTICIPANT_FAMILY_NAME = "Doe";
	
	private static final String PARTICIPANT_GIVEN_NAME = "John";
	
	@Autowired
	@Qualifier("sessionFactory")
	private SessionFactory sessionFactory;
	
	private FhirEncounterDaoImpl dao;
	
	@Before
	public void setUp() throws Exception {
		dao = new FhirEncounterDaoImpl();
		dao.setSessionFactory(sessionFactory);
		executeDataSet(ENCOUNTER_INITIAL_DATA_XML);
	}
	
	@Test
	public void shouldReturnMatchingEncounter() {
		Encounter encounter = dao.getEncounterByUuid(ENCOUNTER_UUID);
		assertThat(encounter, notNullValue());
		assertThat(encounter.getUuid(), notNullValue());
		assertThat(encounter.getUuid(), equalTo(ENCOUNTER_UUID));
	}
	
	@Test
	public void shouldReturnNullWithUnknownEncounterUuid() {
		Encounter encounter = dao.getEncounterByUuid(UNKNOWN_ENCOUNTER_UUID);
		assertThat(encounter, nullValue());
	}
	
	@Test
	public void searchForEncounters_shouldSearchForEncountersByDate() {
		Collection<Encounter> results = dao.searchForEncounters(new DateRangeParam(new DateParam(ENCOUNTER_DATETIME)), null,
		    null, null);
		
		assertThat(results, Matchers.notNullValue());
		assertThat(results, not(empty()));
		assertThat(results.iterator().next().getEncounterDatetime().toString(), equalTo(ENCOUNTER_DATE));
	}
	
	@Test
	public void searchForEncounters_shouldSearchForEncountersBySubjectName() {
		ReferenceAndListParam subjectReference = new ReferenceAndListParam();
		ReferenceParam subject = new ReferenceParam();
		
		subject.setValue(PATIENT_FULL_NAME);
		subject.setChain(Patient.SP_NAME);
		
		subjectReference.addValue(new ReferenceOrListParam().add(subject));
		
		Collection<Encounter> results = dao.searchForEncounters(null, null, null, subjectReference);
		
		assertThat(results, Matchers.notNullValue());
		assertThat(results, not(empty()));
		assertThat(results.iterator().next().getPatient().getPerson().getPersonName().getFullName(),
		    equalTo(PATIENT_FULL_NAME));
	}
	
	@Test
	public void searchForEncounters_shouldSearchForEncountersBySubjectFamilyName() {
		ReferenceAndListParam subjectReference = new ReferenceAndListParam();
		ReferenceParam subject = new ReferenceParam();
		
		subject.setValue(PATIENT_FAMILY_NAME);
		subject.setChain(Patient.SP_FAMILY);
		
		subjectReference.addValue(new ReferenceOrListParam().add(subject));
		
		Collection<Encounter> results = dao.searchForEncounters(null, null, null, subjectReference);
		
		assertThat(results, Matchers.notNullValue());
		assertThat(results, not(empty()));
		assertThat(results.iterator().next().getPatient().getPerson().getPersonName().getFamilyName(),
		    equalTo(PATIENT_FAMILY_NAME));
	}
	
	@Test
	public void searchForEncounters_shouldSearchForEncountersBySubjectGivenName() {
		ReferenceAndListParam subjectReference = new ReferenceAndListParam();
		ReferenceParam subject = new ReferenceParam();
		
		subject.setValue(PATIENT_GIVEN_NAME);
		subject.setChain(Patient.SP_GIVEN);
		
		subjectReference.addValue(new ReferenceOrListParam().add(subject));
		
		Collection<Encounter> results = dao.searchForEncounters(null, null, null, subjectReference);
		
		assertThat(results, Matchers.notNullValue());
		assertThat(results, not(empty()));
		assertThat(results.iterator().next().getPatient().getPerson().getPersonName().getGivenName(),
		    equalTo(PATIENT_GIVEN_NAME));
	}
	
	@Test
	public void searchForEncounters_shouldSearchForEncountersBySubjectIdentifier() {
		ReferenceAndListParam subjectReference = new ReferenceAndListParam();
		ReferenceParam subject = new ReferenceParam();
		
		subject.setValue(PATIENT_IDENTIFIER);
		subject.setChain(Patient.SP_IDENTIFIER);
		
		subjectReference.addValue(new ReferenceOrListParam().add(subject));
		
		Collection<Encounter> results = dao.searchForEncounters(null, null, null, subjectReference);
		
		assertThat(results, Matchers.notNullValue());
		assertThat(results, not(empty()));
		assertThat(results.iterator().next().getPatient().getPatientIdentifier().getIdentifier(),
		    equalTo(PATIENT_IDENTIFIER));
	}
	
	@Test
	public void searchForEncounters_shouldReturnEmptyCollectionOfEncountersByWrongSubjectIdentifier() {
		ReferenceAndListParam subjectReference = new ReferenceAndListParam();
		ReferenceParam subject = new ReferenceParam();
		
		subject.setValue(WRONG_PATIENT_IDENTIFIER);
		subject.setChain(Patient.SP_IDENTIFIER);
		
		subjectReference.addValue(new ReferenceOrListParam().add(subject));
		
		Collection<Encounter> results = dao.searchForEncounters(null, null, null, subjectReference);
		
		assertThat(results, Matchers.notNullValue());
		assertThat(results, is(empty()));
	}
	
	@Test
	public void searchForEncounters_shouldSearchForEncountersByParticipantIdentifier() {
		ReferenceAndListParam participantReference = new ReferenceAndListParam();
		ReferenceParam participant = new ReferenceParam();
		
		participant.setValue(PARTICIPANT_IDENTIFIER);
		participant.setChain(Practitioner.SP_IDENTIFIER);
		
		participantReference.addValue(new ReferenceOrListParam().add(participant));
		
		Collection<Encounter> results = dao.searchForEncounters(null, null, participantReference, null);
		
		assertThat(results, Matchers.notNullValue());
		assertThat(results, not(empty()));
		assertThat(results.iterator().next().getEncounterProviders().size(), greaterThanOrEqualTo(1));
		assertThat(results.iterator().next().getEncounterProviders().iterator().next().getProvider(), notNullValue());
		assertThat(results.iterator().next().getEncounterProviders().iterator().next().getProvider().getIdentifier(),
		    equalTo(PARTICIPANT_IDENTIFIER));
	}
	
	@Test
	public void searchForEncounters_shouldSearchForEncountersByParticipantGivenName() {
		ReferenceAndListParam participantReference = new ReferenceAndListParam();
		ReferenceParam participant = new ReferenceParam();
		
		participant.setValue(PARTICIPANT_GIVEN_NAME);
		participant.setChain(Practitioner.SP_GIVEN);
		
		participantReference.addValue(new ReferenceOrListParam().add(participant));
		
		Collection<Encounter> results = dao.searchForEncounters(null, null, participantReference, null);
		
		assertThat(results, Matchers.notNullValue());
		assertThat(results, not(empty()));
		assertThat(results.iterator().next().getEncounterProviders().size(), greaterThanOrEqualTo(1));
		assertThat(results.iterator().next().getEncounterProviders().iterator().next().getProvider(), notNullValue());
		assertThat(results.iterator().next().getEncounterProviders().iterator().next().getProvider().getPerson()
		        .getPersonName().getGivenName(),
		    equalTo(PARTICIPANT_GIVEN_NAME));
		
	}
	
	@Test
	public void searchForEncounters_shouldSearchForEncountersByParticipantFamilyName() {
		ReferenceAndListParam participantReference = new ReferenceAndListParam();
		ReferenceParam participant = new ReferenceParam();
		
		participant.setValue(PARTICIPANT_FAMILY_NAME);
		participant.setChain(Practitioner.SP_FAMILY);
		
		participantReference.addValue(new ReferenceOrListParam().add(participant));
		
		Collection<Encounter> results = dao.searchForEncounters(null, null, participantReference, null);
		
		assertThat(results, Matchers.notNullValue());
		assertThat(results, not(empty()));
		assertThat(results.iterator().next().getEncounterProviders().size(), greaterThanOrEqualTo(1));
		assertThat(results.iterator().next().getEncounterProviders().iterator().next().getProvider(), notNullValue());
		assertThat(results.iterator().next().getEncounterProviders().iterator().next().getProvider().getPerson()
		        .getPersonName().getFamilyName(),
		    equalTo(PARTICIPANT_FAMILY_NAME));
		
	}
	
	@Test
	public void searchForEncounters_shouldSearchForEncountersByParticipantName() {
		ReferenceAndListParam participantReference = new ReferenceAndListParam();
		ReferenceParam participant = new ReferenceParam();
		
		participant.setValue(PARTICIPANT_FULL_NAME);
		participant.setChain(Practitioner.SP_NAME);
		
		participantReference.addValue(new ReferenceOrListParam().add(participant));
		
		Collection<Encounter> results = dao.searchForEncounters(null, null, participantReference, null);
		
		assertThat(results, Matchers.notNullValue());
		assertThat(results, not(empty()));
		assertThat(results.iterator().next().getEncounterProviders().size(), greaterThanOrEqualTo(1));
		assertThat(results.iterator().next().getEncounterProviders().iterator().next().getProvider(), notNullValue());
		assertThat(results.iterator().next().getEncounterProviders().iterator().next().getProvider().getPerson()
		        .getPersonName().getFullName(),
		    equalTo(PARTICIPANT_FULL_NAME));
	}
	
	@Test
	public void searchForEncounters_shouldSearchForEncountersByEncounterLocationCity() {
		ReferenceAndListParam locationReference = new ReferenceAndListParam();
		ReferenceParam location = new ReferenceParam();
		
		location.setValue(ENCOUNTER_ADDRESS_CITY);
		location.setChain(Location.SP_ADDRESS_CITY);
		
		locationReference.addValue(new ReferenceOrListParam().add(location));
		
		Collection<Encounter> results = dao.searchForEncounters(null, locationReference, null, null);
		
		assertThat(results, Matchers.notNullValue());
		assertThat(results, not(empty()));
		assertThat(results.iterator().next().getLocation().getCityVillage(), equalTo(ENCOUNTER_ADDRESS_CITY));
	}
	
	@Test
	public void searchForEncounters_shouldSearchForEncountersByEncounterLocationState() {
		ReferenceAndListParam locationReference = new ReferenceAndListParam();
		ReferenceParam location = new ReferenceParam();
		
		location.setValue(ENCOUNTER_ADDRESS_STATE);
		location.setChain(Location.SP_ADDRESS_STATE);
		
		locationReference.addValue(new ReferenceOrListParam().add(location));
		
		Collection<Encounter> results = dao.searchForEncounters(null, locationReference, null, null);
		
		assertThat(results, Matchers.notNullValue());
		assertThat(results, not(empty()));
		assertThat(results.iterator().next().getLocation().getStateProvince(), equalTo(ENCOUNTER_ADDRESS_STATE));
	}
	
	@Test
	public void searchForEncounters_shouldSearchForEncountersBySubjectIdentifierAndGivenName() {
		ReferenceAndListParam subjectReference = new ReferenceAndListParam();
		ReferenceParam subjectIdentifier = new ReferenceParam();
		ReferenceParam subjectGiven = new ReferenceParam();
		
		subjectIdentifier.setValue(PATIENT_IDENTIFIER);
		subjectIdentifier.setChain(Patient.SP_IDENTIFIER);
		
		subjectGiven.setValue(PATIENT_GIVEN_NAME);
		subjectGiven.setChain(Patient.SP_GIVEN);
		
		subjectReference.addValue(new ReferenceOrListParam().add(subjectIdentifier).add(subjectGiven));
		
		Collection<Encounter> results = dao.searchForEncounters(null, null, null, subjectReference);
		
		assertThat(results, Matchers.notNullValue());
		assertThat(results, not(empty()));
		assertThat(results.iterator().next().getUuid(), equalTo(ENCOUNTER_UUID));
		assertThat(results.iterator().next().getPatient().getPatientIdentifier().getIdentifier(),
		    equalTo(PATIENT_IDENTIFIER));
	}
	
	@Test
	public void searchForEncounters_shouldSearchForEncountersByParticipantNameGivenAndFamily() {
		ReferenceAndListParam participantReference = new ReferenceAndListParam();
		ReferenceParam participantName = new ReferenceParam();
		ReferenceParam participantGiven = new ReferenceParam();
		ReferenceParam participantFamily = new ReferenceParam();
		
		participantName.setValue(PARTICIPANT_FULL_NAME);
		participantName.setChain(Practitioner.SP_NAME);
		
		participantGiven.setValue(PARTICIPANT_GIVEN_NAME);
		participantGiven.setChain(Practitioner.SP_GIVEN);
		
		participantFamily.setValue(PARTICIPANT_FAMILY_NAME);
		participantFamily.setChain(Practitioner.SP_FAMILY);
		
		participantReference.addValue(new ReferenceOrListParam().add(participantName));
		
		Collection<Encounter> results = dao.searchForEncounters(null, null, participantReference, null);
		
		assertThat(results, Matchers.notNullValue());
		assertThat(results, not(empty()));
		assertThat(results.iterator().next().getUuid(), equalTo(ENCOUNTER_UUID));
		assertThat(results.iterator().next().getEncounterProviders().size(), greaterThanOrEqualTo(1));
		assertThat(results.iterator().next().getEncounterProviders().iterator().next().getProvider(), notNullValue());
		assertThat(results.iterator().next().getEncounterProviders().iterator().next().getProvider().getPerson()
		        .getPersonName().getFullName(),
		    equalTo(PARTICIPANT_FULL_NAME));
	}
	
	@Test
	public void searchForEncounters_shouldSearchForEncountersByEncounterLocationStateCityAndCountry() {
		ReferenceAndListParam locationReference = new ReferenceAndListParam();
		ReferenceParam locationState = new ReferenceParam();
		ReferenceParam locationCity = new ReferenceParam();
		ReferenceParam locationCountry = new ReferenceParam();
		
		locationState.setValue(ENCOUNTER_ADDRESS_STATE);
		locationState.setChain(Location.SP_ADDRESS_STATE);
		
		locationCity.setValue(ENCOUNTER_ADDRESS_CITY);
		locationCity.setChain(Location.SP_ADDRESS_CITY);
		
		locationCountry.setValue(ENCOUNTER_ADDRESS_COUNTRY);
		locationCountry.setChain(Location.SP_ADDRESS_COUNTRY);
		
		locationReference.addValue(new ReferenceOrListParam().add(locationCity).add(locationCountry).add(locationState));
		
		Collection<Encounter> results = dao.searchForEncounters(null, locationReference, null, null);
		
		assertThat(results, Matchers.notNullValue());
		assertThat(results, not(empty()));
		assertThat(results.iterator().next().getUuid(), equalTo(ENC_UUID));
		assertThat(results.iterator().next().getLocation().getStateProvince(), equalTo(ENCOUNTER_ADDRESS_STATE));
		assertThat(results.iterator().next().getLocation().getCityVillage(), equalTo(ENCOUNTER_ADDRESS_CITY));
		assertThat(results.iterator().next().getLocation().getCountry(), equalTo(ENCOUNTER_ADDRESS_COUNTRY));
	}
}
