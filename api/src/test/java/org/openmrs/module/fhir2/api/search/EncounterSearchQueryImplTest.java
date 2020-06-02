/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.fhir2.api.search;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;

import java.util.List;

import ca.uhn.fhir.rest.api.server.IBundleProvider;
import ca.uhn.fhir.rest.param.DateParam;
import ca.uhn.fhir.rest.param.DateRangeParam;
import ca.uhn.fhir.rest.param.ReferenceAndListParam;
import ca.uhn.fhir.rest.param.ReferenceOrListParam;
import ca.uhn.fhir.rest.param.ReferenceParam;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.Encounter;
import org.hl7.fhir.r4.model.Location;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.Practitioner;
import org.junit.Test;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.TestFhirSpringConfiguration;
import org.openmrs.module.fhir2.api.dao.FhirEncounterDao;
import org.openmrs.module.fhir2.api.search.param.SearchParameterMap;
import org.openmrs.module.fhir2.api.translators.EncounterTranslator;
import org.openmrs.test.BaseModuleContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

@ContextConfiguration(classes = TestFhirSpringConfiguration.class, inheritLocations = false)
public class EncounterSearchQueryImplTest extends BaseModuleContextSensitiveTest {
	
	private static final String ENC_UUID = "eec646cb-c847-45a7-98bc-91c8c4f70add";
	
	private static final String ENCOUNTER_UUID = "y403fafb-e5e4-42d0-9d11-4f52e89d123r";
	
	private static final String PATIENT_IDENTIFIER = "101-6";
	
	private static final String WRONG_PATIENT_IDENTIFIER = "12334HD";
	
	private static final String PATIENT_FULL_NAME = "Mr. Horatio Hornblower";
	
	private static final String PATIENT_UUID = "da7f524f-27ce-4bb2-86d6-6d1d05312bd5";
	
	private static final String PARTICIPANT_FULL_NAME = "Super User";
	
	private static final String PARTICIPANT_UUID = "c2299800-cca9-11e0-9572-0800200c9a66";
	
	private static final String ENCOUNTER_DATETIME = "2008-08-15T00:00:00.0";
	
	private static final String PATIENT_GIVEN_NAME = "John";
	
	private static final String PATIENT_FAMILY_NAME = "Hornblower2";
	
	private static final String ENCOUNTER_LOCATION_CITY = "Boston";
	
	private static final String ENCOUNTER_LOCATION_COUNTRY = "USA";
	
	private static final String ENCOUNTER_LOCATION_STATE = "MA";
	
	private static final String ENCOUNTER_LOCATION_POSTAL_CODE = "02115";
	
	private static final String ENCOUNTER_LOCATION_UUID = "9356400c-a5a2-4532-8f2b-2361b3446eb8";
	
	private static final String PARTICIPANT_IDENTIFIER = "Test";
	
	private static final String PARTICIPANT_FAMILY_NAME = "User";
	
	private static final String PARTICIPANT_GIVEN_NAME = "Super";
	
	private static final int START_INDEX = 0;
	
	private static final int END_INDEX = 10;
	
	@Autowired
	private FhirEncounterDao dao;
	
	@Autowired
	private EncounterTranslator translator;
	
	@Autowired
	SearchQuery<org.openmrs.Encounter, Encounter, FhirEncounterDao, EncounterTranslator> searchQuery;
	
	private List<IBaseResource> get(IBundleProvider results) {
		return results.getResources(START_INDEX, END_INDEX);
	}
	
	private IBundleProvider search(SearchParameterMap theParams) {
		return searchQuery.getQueryResults(theParams, dao, translator);
	}
	
	@Test
	public void searchForEncounters_shouldSearchForEncountersByDate() {
		DateRangeParam date = new DateRangeParam(new DateParam(ENCOUNTER_DATETIME));
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.DATE_RANGE_SEARCH_HANDLER, date);
		IBundleProvider results = search(theParams);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList, not(empty()));
		assertThat(resultList.size(), greaterThanOrEqualTo(1));
		assertThat(((Encounter) resultList.iterator().next()).getId(), equalTo(ENC_UUID));
	}
	
	@Test
	public void searchForEncounters_shouldSearchForEncountersBySubjectName() {
		ReferenceAndListParam subjectReference = new ReferenceAndListParam();
		ReferenceParam subject = new ReferenceParam();
		
		subject.setValue(PATIENT_FULL_NAME);
		subject.setChain(Patient.SP_NAME);
		
		subjectReference.addValue(new ReferenceOrListParam().add(subject));
		
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.PATIENT_REFERENCE_SEARCH_HANDLER,
		    subjectReference);
		IBundleProvider results = search(theParams);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList, not(empty()));
		assertThat(resultList.size(), greaterThanOrEqualTo(1));
		assertThat(((Encounter) resultList.iterator().next()).getId(), equalTo(ENCOUNTER_UUID));
	}
	
	@Test
	public void searchForEncounters_shouldSearchForEncountersBySubjectFamilyName() {
		ReferenceAndListParam subjectReference = new ReferenceAndListParam();
		ReferenceParam subject = new ReferenceParam();
		
		subject.setValue(PATIENT_FAMILY_NAME);
		subject.setChain(Patient.SP_FAMILY);
		
		subjectReference.addValue(new ReferenceOrListParam().add(subject));
		
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.PATIENT_REFERENCE_SEARCH_HANDLER,
		    subjectReference);
		IBundleProvider results = search(theParams);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList, not(empty()));
		assertThat(resultList.size(), greaterThanOrEqualTo(1));
		assertThat(((Encounter) resultList.iterator().next()).getId(), equalTo(ENCOUNTER_UUID));
	}
	
	@Test
	public void searchForEncounters_shouldSearchForEncountersBySubjectGivenName() {
		ReferenceAndListParam subjectReference = new ReferenceAndListParam();
		ReferenceParam subject = new ReferenceParam();
		
		subject.setValue(PATIENT_GIVEN_NAME);
		subject.setChain(Patient.SP_GIVEN);
		
		subjectReference.addValue(new ReferenceOrListParam().add(subject));
		
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.PATIENT_REFERENCE_SEARCH_HANDLER,
		    subjectReference);
		IBundleProvider results = search(theParams);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList, not(empty()));
		assertThat(resultList.size(), greaterThanOrEqualTo(1));
		assertThat(((Encounter) resultList.iterator().next()).getId(), equalTo(ENCOUNTER_UUID));
	}
	
	@Test
	public void searchForEncounters_shouldSearchForEncountersBySubjectUuid() {
		ReferenceAndListParam subjectReference = new ReferenceAndListParam();
		ReferenceParam subject = new ReferenceParam();
		
		subject.setValue(PATIENT_UUID);
		
		subjectReference.addValue(new ReferenceOrListParam().add(subject));
		
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.PATIENT_REFERENCE_SEARCH_HANDLER,
		    subjectReference);
		IBundleProvider results = search(theParams);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList, not(empty()));
		assertThat(resultList.size(), greaterThanOrEqualTo(1));
		assertThat(((Encounter) resultList.iterator().next()).getSubject().getReferenceElement().getIdPart(),
		    equalTo(PATIENT_UUID));
	}
	
	@Test
	public void searchForEncounters_shouldSearchForEncountersBySubjectIdentifier() {
		ReferenceAndListParam subjectReference = new ReferenceAndListParam();
		ReferenceParam subject = new ReferenceParam();
		
		subject.setValue(PATIENT_IDENTIFIER);
		subject.setChain(Patient.SP_IDENTIFIER);
		
		subjectReference.addValue(new ReferenceOrListParam().add(subject));
		
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.PATIENT_REFERENCE_SEARCH_HANDLER,
		    subjectReference);
		IBundleProvider results = search(theParams);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList, not(empty()));
		assertThat(resultList.size(), greaterThanOrEqualTo(1));
		assertThat(((Encounter) resultList.iterator().next()).getSubject().getIdentifier().getValue(),
		    equalTo(PATIENT_IDENTIFIER));
	}
	
	@Test
	public void searchForEncounters_shouldReturnEmptyCollectionOfEncountersByWrongSubjectIdentifier() {
		ReferenceAndListParam subjectReference = new ReferenceAndListParam();
		ReferenceParam subject = new ReferenceParam();
		
		subject.setValue(WRONG_PATIENT_IDENTIFIER);
		subject.setChain(Patient.SP_IDENTIFIER);
		
		subjectReference.addValue(new ReferenceOrListParam().add(subject));
		
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.PATIENT_REFERENCE_SEARCH_HANDLER,
		    subjectReference);
		IBundleProvider results = search(theParams);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList, is(empty()));
	}
	
	@Test
	public void searchForEncounters_shouldSearchForEncountersByParticipantIdentifier() {
		ReferenceAndListParam participantReference = new ReferenceAndListParam();
		ReferenceParam participant = new ReferenceParam();
		
		participant.setValue(PARTICIPANT_IDENTIFIER);
		participant.setChain(Practitioner.SP_IDENTIFIER);
		
		participantReference.addValue(new ReferenceOrListParam().add(participant));
		
		SearchParameterMap theParams = new SearchParameterMap()
		        .addParameter(FhirConstants.PARTICIPANT_REFERENCE_SEARCH_HANDLER, participantReference);
		IBundleProvider results = search(theParams);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList, not(empty()));
		assertThat(resultList.size(), greaterThanOrEqualTo(1));
		assertThat(
		    ((Encounter) resultList.iterator().next()).getParticipantFirstRep().getIndividual().getIdentifier().getValue(),
		    equalTo(PARTICIPANT_IDENTIFIER));
	}
	
	@Test
	public void searchForEncounters_shouldSearchForEncountersByParticipantGivenName() {
		ReferenceAndListParam participantReference = new ReferenceAndListParam();
		ReferenceParam participant = new ReferenceParam();
		
		participant.setValue(PARTICIPANT_GIVEN_NAME);
		participant.setChain(Practitioner.SP_GIVEN);
		
		participantReference.addValue(new ReferenceOrListParam().add(participant));
		
		SearchParameterMap theParams = new SearchParameterMap()
		        .addParameter(FhirConstants.PARTICIPANT_REFERENCE_SEARCH_HANDLER, participantReference);
		IBundleProvider results = search(theParams);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList, not(empty()));
		assertThat(resultList.size(), greaterThanOrEqualTo(1));
	}
	
	@Test
	public void searchForEncounters_shouldSearchForEncountersByParticipantFamilyName() {
		ReferenceAndListParam participantReference = new ReferenceAndListParam();
		ReferenceParam participant = new ReferenceParam();
		
		participant.setValue(PARTICIPANT_FAMILY_NAME);
		participant.setChain(Practitioner.SP_FAMILY);
		
		participantReference.addValue(new ReferenceOrListParam().add(participant));
		
		SearchParameterMap theParams = new SearchParameterMap()
		        .addParameter(FhirConstants.PARTICIPANT_REFERENCE_SEARCH_HANDLER, participantReference);
		IBundleProvider results = search(theParams);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList, not(empty()));
		assertThat(resultList.size(), greaterThanOrEqualTo(1));
	}
	
	@Test
	public void searchForEncounters_shouldSearchForEncountersByParticipantName() {
		ReferenceAndListParam participantReference = new ReferenceAndListParam();
		ReferenceParam participant = new ReferenceParam();
		
		participant.setValue(PARTICIPANT_FULL_NAME);
		participant.setChain(Practitioner.SP_NAME);
		
		participantReference.addValue(new ReferenceOrListParam().add(participant));
		
		SearchParameterMap theParams = new SearchParameterMap()
		        .addParameter(FhirConstants.PARTICIPANT_REFERENCE_SEARCH_HANDLER, participantReference);
		IBundleProvider results = search(theParams);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList, not(empty()));
		assertThat(resultList.size(), greaterThanOrEqualTo(1));
	}
	
	@Test
	public void searchForEncounters_shouldSearchForEncountersByParticipantUuid() {
		ReferenceAndListParam participantReference = new ReferenceAndListParam();
		ReferenceParam participant = new ReferenceParam();
		
		participant.setValue(PARTICIPANT_UUID);
		
		participantReference.addValue(new ReferenceOrListParam().add(participant));
		
		SearchParameterMap theParams = new SearchParameterMap()
		        .addParameter(FhirConstants.PARTICIPANT_REFERENCE_SEARCH_HANDLER, participantReference);
		IBundleProvider results = search(theParams);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList, not(empty()));
		assertThat(resultList.size(), greaterThanOrEqualTo(1));
		assertThat(((Encounter) resultList.iterator().next()).getParticipantFirstRep().getIndividual().getReferenceElement()
		        .getIdPart(),
		    equalTo(PARTICIPANT_UUID));
	}
	
	@Test
	public void searchForEncounters_shouldSearchForEncountersByEncounterLocationCity() {
		ReferenceAndListParam locationReference = new ReferenceAndListParam();
		ReferenceParam location = new ReferenceParam();
		
		location.setValue(ENCOUNTER_LOCATION_CITY);
		location.setChain(Location.SP_ADDRESS_CITY);
		
		locationReference.addValue(new ReferenceOrListParam().add(location));
		
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.LOCATION_REFERENCE_SEARCH_HANDLER,
		    locationReference);
		IBundleProvider results = search(theParams);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList, not(empty()));
		assertThat(resultList.size(), greaterThanOrEqualTo(1));
	}
	
	@Test
	public void searchForEncounters_shouldSearchForEncountersByEncounterLocationState() {
		ReferenceAndListParam locationReference = new ReferenceAndListParam();
		ReferenceParam location = new ReferenceParam();
		
		location.setValue(ENCOUNTER_LOCATION_STATE);
		location.setChain(Location.SP_ADDRESS_STATE);
		
		locationReference.addValue(new ReferenceOrListParam().add(location));
		
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.LOCATION_REFERENCE_SEARCH_HANDLER,
		    locationReference);
		IBundleProvider results = search(theParams);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList, not(empty()));
		assertThat(resultList.size(), greaterThanOrEqualTo(1));
	}
	
	@Test
	public void searchForEncounters_shouldSearchForEncountersByEncounterLocationCountry() {
		ReferenceAndListParam locationReference = new ReferenceAndListParam();
		ReferenceParam location = new ReferenceParam();
		
		location.setValue(ENCOUNTER_LOCATION_COUNTRY);
		location.setChain(Location.SP_ADDRESS_COUNTRY);
		
		locationReference.addValue(new ReferenceOrListParam().add(location));
		
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.LOCATION_REFERENCE_SEARCH_HANDLER,
		    locationReference);
		IBundleProvider results = search(theParams);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList, not(empty()));
		assertThat(resultList.size(), greaterThanOrEqualTo(1));
	}
	
	@Test
	public void searchForEncounters_shouldSearchForEncountersByEncounterLocationPostalCode() {
		ReferenceAndListParam locationReference = new ReferenceAndListParam();
		ReferenceParam location = new ReferenceParam();
		
		location.setValue(ENCOUNTER_LOCATION_POSTAL_CODE);
		location.setChain(Location.SP_ADDRESS_POSTALCODE);
		
		locationReference.addValue(new ReferenceOrListParam().add(location));
		
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.LOCATION_REFERENCE_SEARCH_HANDLER,
		    locationReference);
		IBundleProvider results = search(theParams);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList, not(empty()));
		assertThat(resultList.size(), greaterThanOrEqualTo(1));
	}
	
	@Test
	public void searchForEncounters_shouldSearchForEncountersByEncounterLocationUuid() {
		ReferenceAndListParam locationReference = new ReferenceAndListParam();
		ReferenceParam location = new ReferenceParam();
		
		location.setValue(ENCOUNTER_LOCATION_UUID);
		
		locationReference.addValue(new ReferenceOrListParam().add(location));
		
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.LOCATION_REFERENCE_SEARCH_HANDLER,
		    locationReference);
		IBundleProvider results = search(theParams);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList, not(empty()));
		assertThat(resultList.size(), greaterThanOrEqualTo(1));
		assertThat(
		    ((Encounter) resultList.iterator().next()).getLocationFirstRep().getLocation().getReferenceElement().getIdPart(),
		    equalTo(ENCOUNTER_LOCATION_UUID));
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
		
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.PATIENT_REFERENCE_SEARCH_HANDLER,
		    subjectReference);
		IBundleProvider results = search(theParams);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList, not(empty()));
		assertThat(resultList.size(), greaterThanOrEqualTo(1));
		assertThat(((Encounter) resultList.iterator().next()).getId(), equalTo(ENCOUNTER_UUID));
		assertThat(((Encounter) resultList.iterator().next()).getSubject().getIdentifier().getValue(),
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
		
		SearchParameterMap theParams = new SearchParameterMap()
		        .addParameter(FhirConstants.PARTICIPANT_REFERENCE_SEARCH_HANDLER, participantReference);
		IBundleProvider results = search(theParams);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList, not(empty()));
		assertThat(resultList.size(), greaterThanOrEqualTo(1));
	}
	
	@Test
	public void searchForEncounters_shouldSearchForEncountersByEncounterLocationStateCityAndCountry() {
		ReferenceAndListParam locationReference = new ReferenceAndListParam();
		ReferenceParam locationState = new ReferenceParam();
		ReferenceParam locationCity = new ReferenceParam();
		ReferenceParam locationCountry = new ReferenceParam();
		
		locationState.setValue(ENCOUNTER_LOCATION_STATE);
		locationState.setChain(Location.SP_ADDRESS_STATE);
		
		locationCity.setValue(ENCOUNTER_LOCATION_CITY);
		locationCity.setChain(Location.SP_ADDRESS_CITY);
		
		locationCountry.setValue(ENCOUNTER_LOCATION_COUNTRY);
		locationCountry.setChain(Location.SP_ADDRESS_COUNTRY);
		
		locationReference.addValue(new ReferenceOrListParam().add(locationCity).add(locationCountry).add(locationState));
		
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.LOCATION_REFERENCE_SEARCH_HANDLER,
		    locationReference);
		IBundleProvider results = search(theParams);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList, not(empty()));
		assertThat(resultList.size(), greaterThanOrEqualTo(1));
	}
}
