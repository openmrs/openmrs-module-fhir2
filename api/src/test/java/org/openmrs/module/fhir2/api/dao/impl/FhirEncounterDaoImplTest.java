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
import static org.hamcrest.Matchers.not;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;

import java.util.Collection;

import ca.uhn.fhir.rest.param.DateParam;
import ca.uhn.fhir.rest.param.DateRangeParam;
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
import org.springframework.test.context.ContextConfiguration;

@ContextConfiguration(classes = TestFhirSpringConfiguration.class, inheritLocations = false)
public class FhirEncounterDaoImplTest extends BaseModuleContextSensitiveTest {
	
	private static final String ENCOUNTER_UUID = "430bbb70-6a9c-4e1e-badb-9d1034b1b5e9";
	
	private static final String UNKNOWN_ENCOUNTER_UUID = "xx923xx-3423kk-2323-232jk23";
	
	private static final String PATIENT_IDENTIFIER = "1000WF";
	
	private static final String PATIENT_FULL_NAME = "Mr. John Doe";
	
	private static final String ENCOUNTER_INITIAL_DATA_XML = "org/openmrs/module/fhir2/api/dao/impl/FhirEncounterDaoImplTest_initial_data.xml";
	
	private static final String ENCOUNTER_DATETIME = "2005-01-01T00:00:00.0";
	
	private static final String ENCOUNTER_DATE = "2005-01-01 00:00:00.0";
	
	private static final String PATIENT_GIVEN_NAME = "John";
	
	private static final String PATIENT_FAMILY_NAME = "Doe";
	
	private static final String ADDRESS_CITY = "Boston";
	
	private static final String ENCOUNTER_ADDRESS_STATE = "MA";
	
	private static final String PARTICIPANT_IDENTIFIER = "1";
	
	private static final String PARTICIPANT_FAMILY_NAME = "Tim";
	
	private static final String PARTICIPANT_GIVEN_NAME = "Him";
	
	@Inject
	@Named("sessionFactory")
	private Provider<SessionFactory> sessionFactoryProvider;
	
	private FhirEncounterDaoImpl dao;
	
	@Before
	public void setUp() throws Exception {
		dao = new FhirEncounterDaoImpl();
		dao.setSessionFactory(sessionFactoryProvider.get());
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
		ReferenceParam subjectReference = new ReferenceParam();
		
		subjectReference.setValue(PATIENT_FULL_NAME);
		subjectReference.setChain(Patient.SP_NAME);
		
		Collection<Encounter> results = dao.searchForEncounters(null, null, null, subjectReference);
		
		assertThat(results, Matchers.notNullValue());
		assertThat(results, not(empty()));
		assertThat(results.iterator().next().getPatient().getPerson().getPersonName().getFullName(),
		    equalTo(PATIENT_FULL_NAME));
	}
	
	@Test
	public void searchForEncounters_shouldSearchForEncountersBySubjectFamilyName() {
		ReferenceParam subjectReference = new ReferenceParam();
		
		subjectReference.setValue(PATIENT_FAMILY_NAME);
		subjectReference.setChain(Patient.SP_FAMILY);
		
		Collection<Encounter> results = dao.searchForEncounters(null, null, null, subjectReference);
		
		assertThat(results, Matchers.notNullValue());
		assertThat(results, not(empty()));
		assertThat(results.iterator().next().getPatient().getPerson().getPersonName().getFamilyName(),
		    equalTo(PATIENT_FAMILY_NAME));
	}
	
	@Test
	public void searchForEncounters_shouldSearchForEncountersBySubjectGivenName() {
		ReferenceParam subjectReference = new ReferenceParam();
		
		subjectReference.setValue(PATIENT_GIVEN_NAME);
		subjectReference.setChain(Patient.SP_GIVEN);
		
		Collection<Encounter> results = dao.searchForEncounters(null, null, null, subjectReference);
		
		assertThat(results, Matchers.notNullValue());
		assertThat(results, not(empty()));
		assertThat(results.iterator().next().getPatient().getPerson().getPersonName().getGivenName(),
		    equalTo(PATIENT_GIVEN_NAME));
	}
	
	@Test
	public void searchForEncounters_shouldSearchForEncountersBySubjectIdentifier() {
		ReferenceParam subjectReference = new ReferenceParam();
		
		subjectReference.setValue(PATIENT_IDENTIFIER);
		subjectReference.setChain(Patient.SP_IDENTIFIER);
		
		Collection<Encounter> results = dao.searchForEncounters(null, null, null, subjectReference);
		
		assertThat(results, Matchers.notNullValue());
		assertThat(results, not(empty()));
		assertThat(results.iterator().next().getPatient().getPatientIdentifier().getIdentifier(),
		    equalTo(PATIENT_IDENTIFIER));
	}
	
	@Test
	public void searchForEncounters_shouldSearchForEncountersByParticipantIdentifier() {
		ReferenceParam participantReference = new ReferenceParam();
		
		participantReference.setChain(Practitioner.SP_IDENTIFIER);
		participantReference.setValue(PATIENT_IDENTIFIER);
		
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
		ReferenceParam participantReference = new ReferenceParam();
		
		participantReference.setValue(PARTICIPANT_GIVEN_NAME);
		participantReference.setChain(Practitioner.SP_GIVEN);
		
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
		ReferenceParam participantReference = new ReferenceParam();
		
		participantReference.setValue(PARTICIPANT_FAMILY_NAME);
		participantReference.setChain(Practitioner.SP_FAMILY);
		
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
		ReferenceParam subjectReference = new ReferenceParam();
		
		subjectReference.setValue(PATIENT_FULL_NAME);
		subjectReference.setChain(Practitioner.SP_NAME);
		
		Collection<Encounter> results = dao.searchForEncounters(null, null, null, subjectReference);
		
		assertThat(results, Matchers.notNullValue());
		assertThat(results, not(empty()));
		assertThat(results.iterator().next().getPatient().getPerson().getPersonName().getFullName(),
		    equalTo(PATIENT_FULL_NAME));
	}
	
	@Test
	public void searchForEncounters_shouldSearchForEncountersByEncounterLocationCity() {
		ReferenceParam locationReference = new ReferenceParam();
		
		locationReference.setValue(ADDRESS_CITY);
		locationReference.setChain(Location.SP_ADDRESS_CITY);
		
		Collection<Encounter> results = dao.searchForEncounters(null, locationReference, null, null);
		
		assertThat(results, Matchers.notNullValue());
		assertThat(results, not(empty()));
		assertThat(results.iterator().next().getLocation().getCityVillage(), equalTo(ADDRESS_CITY));
	}
	
	@Test
	public void searchForEncounters_shouldSearchForEncountersByEncounterLocationState() {
		ReferenceParam locationReference = new ReferenceParam();
		
		locationReference.setValue(ENCOUNTER_ADDRESS_STATE);
		locationReference.setChain(Location.SP_ADDRESS_STATE);
		
		Collection<Encounter> results = dao.searchForEncounters(null, locationReference, null, null);
		
		assertThat(results, Matchers.notNullValue());
		assertThat(results, not(empty()));
		assertThat(results.iterator().next().getLocation().getStateProvince(), equalTo(ENCOUNTER_ADDRESS_STATE));
	}
}
