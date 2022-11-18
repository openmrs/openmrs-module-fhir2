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
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertNotNull;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import ca.uhn.fhir.model.api.Include;
import ca.uhn.fhir.rest.api.SortOrderEnum;
import ca.uhn.fhir.rest.api.SortSpec;
import ca.uhn.fhir.rest.api.server.IBundleProvider;
import ca.uhn.fhir.rest.param.DateParam;
import ca.uhn.fhir.rest.param.DateRangeParam;
import ca.uhn.fhir.rest.param.ReferenceAndListParam;
import ca.uhn.fhir.rest.param.ReferenceOrListParam;
import ca.uhn.fhir.rest.param.ReferenceParam;
import ca.uhn.fhir.rest.param.TokenAndListParam;
import ca.uhn.fhir.rest.param.TokenParam;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.Encounter;
import org.hl7.fhir.r4.model.Location;
import org.hl7.fhir.r4.model.MedicationRequest;
import org.hl7.fhir.r4.model.Observation;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.Practitioner;
import org.hl7.fhir.r4.model.ServiceRequest;
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
public class EncounterSearchQueryTest extends BaseModuleContextSensitiveTest {
	
	private static final String ENC_UUID = "eec646cb-c847-45a7-98bc-91c8c4f70add";
	
	private static final String ENCOUNTER_UUID = "y403fafb-e5e4-42d0-9d11-4f52e89d123r";
	
	private static final String PATIENT_IDENTIFIER = "101-6";
	
	private static final String WRONG_PATIENT_IDENTIFIER = "12334HD";
	
	private static final String PATIENT_FULL_NAME = "Mr. Horatio Hornblower";
	
	private static final String PATIENT_UUID = "da7f524f-27ce-4bb2-86d6-6d1d05312bd5";
	
	private static final String PARTICIPANT_FULL_NAME = "Super User";
	
	private static final String WRONG_NAME = "Wrong name";
	
	private static final String PARTICIPANT_UUID = "c2299800-cca9-11e0-9572-0800200c9a66";
	
	private static final String WRONG_UUID = "c2299800-cca9-11e0-9572-abcdef0c9a66";
	
	private static final String ENCOUNTER_DATETIME = "2008-08-15T00:00:00.0";
	
	private static final String PATIENT_GIVEN_NAME = "John";
	
	private static final String PATIENT_FAMILY_NAME = "Hornblower2";
	
	private static final String ENCOUNTER_LOCATION_CITY = "Boston";
	
	private static final String ENCOUNTER_LOCATION_COUNTRY = "USA";
	
	private static final String ENCOUNTER_LOCATION_STATE = "MA";
	
	private static final String ENCOUNTER_LOCATION_POSTAL_CODE = "02115";
	
	private static final String ENCOUNTER_LOCATION_UUID = "9356400c-a5a2-4532-8f2b-2361b3446eb8";
	
	private static final String PARTICIPANT_IDENTIFIER = "Test";
	
	private static final String WRONG_IDENTIFIER = "Wrong identifier";
	
	private static final String PARTICIPANT_FAMILY_NAME = "User";
	
	private static final String WRONG_FAMILY_NAME = "Wrong family name";
	
	private static final String PARTICIPANT_GIVEN_NAME = "Super";
	
	private static final String WRONG_GIVEN_NAME = "Wrong given name";
	
	private static final String DATE_CREATED = "2008-08-19";
	
	private static final String WRONG_DATE_CREATED = "2008-08-18";
	
	private static final String ENCOUNTER_TYPE_UUID = "07000be2-26b6-4cce-8b40-866d8435b613";
	
	private static final String ENCOUNTER_UUID_2 = "6519d653-393b-4118-9c83-a3715b82d4ac";
	
	private static final int START_INDEX = 0;
	
	private static final int END_INDEX = 10;
	
	@Autowired
	private FhirEncounterDao dao;
	
	@Autowired
	private EncounterTranslator<org.openmrs.Encounter> translator;
	
	@Autowired
	private SearchQueryInclude<Encounter> searchQueryInclude;
	
	@Autowired
	SearchQuery<org.openmrs.Encounter, Encounter, FhirEncounterDao, EncounterTranslator<org.openmrs.Encounter>, SearchQueryInclude<Encounter>> searchQuery;
	
	private List<IBaseResource> get(IBundleProvider results) {
		return results.getResources(START_INDEX, END_INDEX);
	}
	
	private IBundleProvider search(SearchParameterMap theParams) {
		return searchQuery.getQueryResults(theParams, dao, translator, searchQueryInclude);
	}
	
	private List<IBaseResource> getAllResources(IBundleProvider results) {
		return results.getAllResources();
	}
	
	@Test
	public void searchForEncounters_shouldSearchForEncountersByDate() {
		DateRangeParam date = new DateRangeParam(new DateParam(ENCOUNTER_DATETIME));
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.DATE_RANGE_SEARCH_HANDLER, date);
		IBundleProvider results = search(theParams);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList, not(empty()));
		assertThat(resultList, hasSize(greaterThanOrEqualTo(1)));
		assertThat(((Encounter) resultList.iterator().next()).getId(), equalTo(ENC_UUID));
	}
	
	@Test
	public void searchForEncounters_shouldSearchForEncountersByTypeUUID() {
		TokenAndListParam typeUuid = new TokenAndListParam().addAnd(new TokenParam(ENCOUNTER_TYPE_UUID));
		SearchParameterMap theParams = new SearchParameterMap()
		        .addParameter(FhirConstants.ENCOUNTER_TYPE_REFERENCE_SEARCH_HANDLER, typeUuid);
		
		IBundleProvider results = search(theParams);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList, not(empty()));
		assertThat(resultList, hasSize(greaterThanOrEqualTo(1)));
		assertThat(((Encounter) resultList.iterator().next()).getIdElement().getIdPart(), equalTo(ENCOUNTER_UUID_2));
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
		assertThat(resultList, hasSize(greaterThanOrEqualTo(1)));
		assertThat(((Encounter) resultList.iterator().next()).getId(), equalTo(ENCOUNTER_UUID));
	}
	
	@Test
	public void searchForEncounters_shouldSearchForUniqueEncountersBySubjectName() {
		ReferenceParam subjectReference = new ReferenceParam(Patient.SP_NAME, "Horatio Hornblower");
		ReferenceAndListParam subjectList = new ReferenceAndListParam();
		subjectList.addValue(new ReferenceOrListParam().add(subjectReference));
		
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.PATIENT_REFERENCE_SEARCH_HANDLER,
		    subjectList);
		
		IBundleProvider results = search(theParams);
		
		assertThat(results, notNullValue());
		assertThat(results.size(), equalTo(1));
		
		Set<String> resultSet = new HashSet<>(dao.getSearchResultUuids(theParams));
		assertThat(resultSet.size(), equalTo(1)); // 3 with repetitions
	}
	
	@Test
	public void searchForEncounters_shouldSearchForEncountersByMultipleSubjectNameOr() {
		ReferenceAndListParam referenceParam = new ReferenceAndListParam();
		ReferenceParam patient = new ReferenceParam();
		
		patient.setValue(PATIENT_FULL_NAME);
		patient.setChain(Patient.SP_NAME);
		
		ReferenceParam badPatient = new ReferenceParam();
		
		badPatient.setValue(WRONG_NAME);
		badPatient.setChain(Patient.SP_NAME);
		
		referenceParam.addValue(new ReferenceOrListParam().add(patient).add(badPatient));
		
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.PATIENT_REFERENCE_SEARCH_HANDLER,
		    referenceParam);
		IBundleProvider results = search(theParams);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList, not(empty()));
		assertThat(resultList, hasSize(greaterThanOrEqualTo(1)));
	}
	
	@Test
	public void searchForEncounters_shouldReturnEmptyListOfEncountersByMultipleSubjectNameAnd() {
		ReferenceAndListParam referenceParam = new ReferenceAndListParam();
		ReferenceParam patient = new ReferenceParam();
		
		patient.setValue(PATIENT_FULL_NAME);
		patient.setChain(Patient.SP_NAME);
		
		ReferenceParam badPatient = new ReferenceParam();
		
		badPatient.setValue(WRONG_NAME);
		badPatient.setChain(Patient.SP_NAME);
		
		referenceParam.addValue(new ReferenceOrListParam().add(patient)).addAnd(new ReferenceOrListParam().add(badPatient));
		
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.PATIENT_REFERENCE_SEARCH_HANDLER,
		    referenceParam);
		IBundleProvider results = search(theParams);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList, empty());
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
		assertThat(resultList, hasSize(greaterThanOrEqualTo(1)));
		assertThat(((Encounter) resultList.iterator().next()).getId(), equalTo(ENCOUNTER_UUID));
	}
	
	@Test
	public void searchForEncounters_shouldSearchForUniqueEncountersBySubjectFamilyName() {
		ReferenceParam subjectReference = new ReferenceParam(Patient.SP_FAMILY, "Hornblower");
		ReferenceAndListParam subjectList = new ReferenceAndListParam();
		subjectList.addValue(new ReferenceOrListParam().add(subjectReference));
		
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.PATIENT_REFERENCE_SEARCH_HANDLER,
		    subjectList);
		
		IBundleProvider results = search(theParams);
		
		assertThat(results, notNullValue());
		assertThat(results.size(), equalTo(1));
		
		Set<String> resultSet = new HashSet<>(dao.getSearchResultUuids(theParams));
		assertThat(resultSet.size(), equalTo(1)); // 3 with repetitions
	}
	
	@Test
	public void searchForEncounters_shouldSearchForEncountersByMultipleSubjectFamilyNameOr() {
		ReferenceAndListParam referenceParam = new ReferenceAndListParam();
		ReferenceParam patient = new ReferenceParam();
		
		patient.setValue(PATIENT_FAMILY_NAME);
		patient.setChain(Patient.SP_FAMILY);
		
		ReferenceParam badPatient = new ReferenceParam();
		
		badPatient.setValue(WRONG_FAMILY_NAME);
		badPatient.setChain(Patient.SP_FAMILY);
		
		referenceParam.addValue(new ReferenceOrListParam().add(patient).add(badPatient));
		
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.PATIENT_REFERENCE_SEARCH_HANDLER,
		    referenceParam);
		IBundleProvider results = search(theParams);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList, not(empty()));
		assertThat(resultList, hasSize(greaterThanOrEqualTo(1)));
	}
	
	@Test
	public void searchForEncounters_shouldReturnEmptyListOfEncountersByMultipleSubjectFamilyNameAnd() {
		ReferenceAndListParam referenceParam = new ReferenceAndListParam();
		ReferenceParam patient = new ReferenceParam();
		
		patient.setValue(PATIENT_FAMILY_NAME);
		patient.setChain(Patient.SP_FAMILY);
		
		ReferenceParam badPatient = new ReferenceParam();
		
		badPatient.setValue(WRONG_FAMILY_NAME);
		badPatient.setChain(Patient.SP_FAMILY);
		
		referenceParam.addValue(new ReferenceOrListParam().add(patient)).addAnd(new ReferenceOrListParam().add(badPatient));
		
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.PATIENT_REFERENCE_SEARCH_HANDLER,
		    referenceParam);
		IBundleProvider results = search(theParams);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList, empty());
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
		assertThat(resultList, hasSize(greaterThanOrEqualTo(1)));
		assertThat(((Encounter) resultList.iterator().next()).getId(), equalTo(ENCOUNTER_UUID));
	}
	
	@Test
	public void searchForEncounters_shouldSearchForUniqueEncountersBySubjectGivenName() {
		ReferenceParam subjectReference = new ReferenceParam(Patient.SP_GIVEN, "Horatio");
		ReferenceAndListParam subjectList = new ReferenceAndListParam();
		subjectList.addValue(new ReferenceOrListParam().add(subjectReference));
		
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.PATIENT_REFERENCE_SEARCH_HANDLER,
		    subjectList);
		
		IBundleProvider results = search(theParams);
		
		assertThat(results, notNullValue());
		assertThat(results.size(), equalTo(1));
		
		Set<String> resultSet = new HashSet<>(dao.getSearchResultUuids(theParams));
		assertThat(resultSet.size(), equalTo(1)); // 2 with repetitions
	}
	
	@Test
	public void searchForEncounters_shouldSearchForEncountersByMultipleSubjectGivenNameOr() {
		ReferenceAndListParam referenceParam = new ReferenceAndListParam();
		ReferenceParam patient = new ReferenceParam();
		
		patient.setValue(PATIENT_GIVEN_NAME);
		patient.setChain(Patient.SP_GIVEN);
		
		ReferenceParam badPatient = new ReferenceParam();
		
		badPatient.setValue(WRONG_GIVEN_NAME);
		badPatient.setChain(Patient.SP_GIVEN);
		
		referenceParam.addValue(new ReferenceOrListParam().add(patient).add(badPatient));
		
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.PATIENT_REFERENCE_SEARCH_HANDLER,
		    referenceParam);
		IBundleProvider results = search(theParams);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList, not(empty()));
		assertThat(resultList, hasSize(greaterThanOrEqualTo(1)));
	}
	
	@Test
	public void searchForEncounters_shouldReturnEmptyListOfEncountersByMultipleSubjectGivenNameAnd() {
		ReferenceAndListParam referenceParam = new ReferenceAndListParam();
		ReferenceParam patient = new ReferenceParam();
		
		patient.setValue(PATIENT_GIVEN_NAME);
		patient.setChain(Patient.SP_GIVEN);
		
		ReferenceParam badPatient = new ReferenceParam();
		
		badPatient.setValue(WRONG_GIVEN_NAME);
		badPatient.setChain(Patient.SP_GIVEN);
		
		referenceParam.addValue(new ReferenceOrListParam().add(patient)).addAnd(new ReferenceOrListParam().add(badPatient));
		
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.PATIENT_REFERENCE_SEARCH_HANDLER,
		    referenceParam);
		IBundleProvider results = search(theParams);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList, empty());
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
		assertThat(resultList, hasSize(greaterThanOrEqualTo(1)));
		assertThat(((Encounter) resultList.iterator().next()).getSubject().getReferenceElement().getIdPart(),
		    equalTo(PATIENT_UUID));
	}
	
	@Test
	public void searchForEncounters_shouldSearchForEncountersByMultipleSubjectUuidOr() {
		ReferenceAndListParam referenceParam = new ReferenceAndListParam();
		ReferenceParam patient = new ReferenceParam();
		
		patient.setValue(PATIENT_UUID);
		
		ReferenceParam badPatient = new ReferenceParam();
		
		badPatient.setValue(WRONG_UUID);
		
		referenceParam.addValue(new ReferenceOrListParam().add(patient).add(badPatient));
		
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.PATIENT_REFERENCE_SEARCH_HANDLER,
		    referenceParam);
		
		IBundleProvider results = search(theParams);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList, not(empty()));
		assertThat(resultList, hasSize(greaterThanOrEqualTo(1)));
		assertThat(((Encounter) resultList.iterator().next()).getSubject().getReferenceElement().getIdPart(),
		    equalTo(PATIENT_UUID));
	}
	
	@Test
	public void searchForEncounters_shouldReturnEmptyListOfEncountersByMultipleSubjectUuidAnd() {
		ReferenceAndListParam referenceParam = new ReferenceAndListParam();
		ReferenceParam patient = new ReferenceParam();
		
		patient.setValue(PATIENT_UUID);
		
		ReferenceParam badPatient = new ReferenceParam();
		
		badPatient.setValue(WRONG_UUID);
		
		referenceParam.addValue(new ReferenceOrListParam().add(patient)).addAnd(new ReferenceOrListParam().add(badPatient));
		
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.PATIENT_REFERENCE_SEARCH_HANDLER,
		    referenceParam);
		IBundleProvider results = search(theParams);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList, empty());
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
		assertThat(resultList, hasSize(greaterThanOrEqualTo(1)));
		assertThat(((Encounter) resultList.iterator().next()).getSubject().getReference(), endsWith(PATIENT_UUID));
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
	public void searchForEncounters_shouldSearchForEncountersByMultipleSubjectIdentifierOr() {
		ReferenceAndListParam referenceParam = new ReferenceAndListParam();
		ReferenceParam patient = new ReferenceParam();
		
		patient.setValue(PATIENT_IDENTIFIER);
		patient.setChain(Patient.SP_IDENTIFIER);
		
		ReferenceParam badPatient = new ReferenceParam();
		
		badPatient.setValue(WRONG_IDENTIFIER);
		badPatient.setChain(Patient.SP_IDENTIFIER);
		
		referenceParam.addValue(new ReferenceOrListParam().add(patient).add(badPatient));
		
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.PATIENT_REFERENCE_SEARCH_HANDLER,
		    referenceParam);
		
		IBundleProvider results = search(theParams);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList, not(empty()));
		assertThat(resultList, hasSize(greaterThanOrEqualTo(1)));
		assertThat(((Encounter) resultList.iterator().next()).getSubject().getReference(), endsWith(PATIENT_UUID));
	}
	
	@Test
	public void searchForEncounters_shouldReturnEmptyListOfEncountersByMultipleSubjectIdentifierAnd() {
		ReferenceAndListParam referenceParam = new ReferenceAndListParam();
		ReferenceParam patient = new ReferenceParam();
		
		patient.setValue(PATIENT_IDENTIFIER);
		patient.setChain(Patient.SP_IDENTIFIER);
		
		ReferenceParam badPatient = new ReferenceParam();
		
		badPatient.setValue(WRONG_IDENTIFIER);
		badPatient.setChain(Patient.SP_IDENTIFIER);
		
		referenceParam.addValue(new ReferenceOrListParam().add(patient)).addAnd(new ReferenceOrListParam().add(badPatient));
		
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.PATIENT_REFERENCE_SEARCH_HANDLER,
		    referenceParam);
		IBundleProvider results = search(theParams);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList, empty());
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
		assertThat(resultList, hasSize(greaterThanOrEqualTo(1)));
		assertThat(
		    ((Encounter) resultList.iterator().next()).getParticipantFirstRep().getIndividual().getIdentifier().getValue(),
		    equalTo(PARTICIPANT_IDENTIFIER));
	}
	
	@Test
	public void searchForEncounters_shouldSearchForEncountersByMultipleParticipantIdentifierOr() {
		ReferenceAndListParam participantReference = new ReferenceAndListParam();
		ReferenceParam participant = new ReferenceParam();
		
		participant.setValue(PARTICIPANT_IDENTIFIER);
		participant.setChain(Practitioner.SP_IDENTIFIER);
		
		ReferenceParam badParticipant = new ReferenceParam();
		
		badParticipant.setValue(WRONG_IDENTIFIER);
		badParticipant.setChain(Practitioner.SP_IDENTIFIER);
		
		participantReference.addValue(new ReferenceOrListParam().add(participant).add(badParticipant));
		
		SearchParameterMap theParams = new SearchParameterMap()
		        .addParameter(FhirConstants.PARTICIPANT_REFERENCE_SEARCH_HANDLER, participantReference);
		IBundleProvider results = search(theParams);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList, not(empty()));
		assertThat(resultList, hasSize(greaterThanOrEqualTo(1)));
		assertThat(
		    ((Encounter) resultList.iterator().next()).getParticipantFirstRep().getIndividual().getIdentifier().getValue(),
		    equalTo(PARTICIPANT_IDENTIFIER));
	}
	
	@Test
	public void searchForEncounters_shouldReturnEmptyListOfEncountersByMultipleParticipantIdentifierAnd() {
		ReferenceAndListParam participantReference = new ReferenceAndListParam();
		ReferenceParam participant = new ReferenceParam();
		
		participant.setValue(PARTICIPANT_IDENTIFIER);
		participant.setChain(Practitioner.SP_IDENTIFIER);
		
		ReferenceParam badParticipant = new ReferenceParam();
		
		badParticipant.setValue(WRONG_IDENTIFIER);
		badParticipant.setChain(Practitioner.SP_IDENTIFIER);
		
		participantReference.addValue(new ReferenceOrListParam().add(participant))
		        .addAnd(new ReferenceOrListParam().add(badParticipant));
		
		SearchParameterMap theParams = new SearchParameterMap()
		        .addParameter(FhirConstants.PARTICIPANT_REFERENCE_SEARCH_HANDLER, participantReference);
		IBundleProvider results = search(theParams);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList, empty());
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
		assertThat(resultList, hasSize(greaterThanOrEqualTo(1)));
	}
	
	@Test
	public void searchForEncounters_shouldSearchForEncountersByMultipleParticipantGivenNameOr() {
		ReferenceAndListParam participantReference = new ReferenceAndListParam();
		ReferenceParam participant = new ReferenceParam();
		
		participant.setValue(PARTICIPANT_GIVEN_NAME);
		participant.setChain(Practitioner.SP_GIVEN);
		
		ReferenceParam badParticipant = new ReferenceParam();
		
		badParticipant.setValue(WRONG_GIVEN_NAME);
		badParticipant.setChain(Practitioner.SP_GIVEN);
		
		participantReference.addValue(new ReferenceOrListParam().add(participant).add(badParticipant));
		
		SearchParameterMap theParams = new SearchParameterMap()
		        .addParameter(FhirConstants.PARTICIPANT_REFERENCE_SEARCH_HANDLER, participantReference);
		IBundleProvider results = search(theParams);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList, not(empty()));
		assertThat(resultList, hasSize(greaterThanOrEqualTo(1)));
	}
	
	@Test
	public void searchForEncounters_shouldReturnEmptyListOfEncountersByMultipleParticipantGivenNameAnd() {
		ReferenceAndListParam participantReference = new ReferenceAndListParam();
		ReferenceParam participant = new ReferenceParam();
		
		participant.setValue(PARTICIPANT_GIVEN_NAME);
		participant.setChain(Practitioner.SP_GIVEN);
		
		ReferenceParam badParticipant = new ReferenceParam();
		
		badParticipant.setValue(WRONG_GIVEN_NAME);
		badParticipant.setChain(Practitioner.SP_GIVEN);
		
		participantReference.addValue(new ReferenceOrListParam().add(participant))
		        .addAnd(new ReferenceOrListParam().add(badParticipant));
		
		SearchParameterMap theParams = new SearchParameterMap()
		        .addParameter(FhirConstants.PARTICIPANT_REFERENCE_SEARCH_HANDLER, participantReference);
		IBundleProvider results = search(theParams);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList, empty());
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
		assertThat(resultList, hasSize(greaterThanOrEqualTo(1)));
	}
	
	@Test
	public void searchForEncounters_shouldSearchForEncountersByMultipleParticipantFamilyNameOr() {
		ReferenceAndListParam participantReference = new ReferenceAndListParam();
		ReferenceParam participant = new ReferenceParam();
		
		participant.setValue(PARTICIPANT_FAMILY_NAME);
		participant.setChain(Practitioner.SP_FAMILY);
		
		ReferenceParam badParticipant = new ReferenceParam();
		
		badParticipant.setValue(WRONG_FAMILY_NAME);
		badParticipant.setChain(Practitioner.SP_FAMILY);
		
		participantReference.addValue(new ReferenceOrListParam().add(participant).add(badParticipant));
		
		SearchParameterMap theParams = new SearchParameterMap()
		        .addParameter(FhirConstants.PARTICIPANT_REFERENCE_SEARCH_HANDLER, participantReference);
		IBundleProvider results = search(theParams);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList, not(empty()));
		assertThat(resultList, hasSize(greaterThanOrEqualTo(1)));
	}
	
	@Test
	public void searchForEncounters_shouldReturnEmptyListOfEncountersByMultipleParticipantFamilyNameAnd() {
		ReferenceAndListParam participantReference = new ReferenceAndListParam();
		ReferenceParam participant = new ReferenceParam();
		
		participant.setValue(PARTICIPANT_FAMILY_NAME);
		participant.setChain(Practitioner.SP_FAMILY);
		
		ReferenceParam badParticipant = new ReferenceParam();
		
		badParticipant.setValue(WRONG_FAMILY_NAME);
		badParticipant.setChain(Practitioner.SP_FAMILY);
		
		participantReference.addValue(new ReferenceOrListParam().add(participant))
		        .addAnd(new ReferenceOrListParam().add(badParticipant));
		
		SearchParameterMap theParams = new SearchParameterMap()
		        .addParameter(FhirConstants.PARTICIPANT_REFERENCE_SEARCH_HANDLER, participantReference);
		IBundleProvider results = search(theParams);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList, empty());
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
		assertThat(resultList, hasSize(greaterThanOrEqualTo(1)));
	}
	
	@Test
	public void searchForEncounters_shouldSearchForEncountersByMultipleParticipantNameOr() {
		ReferenceAndListParam participantReference = new ReferenceAndListParam();
		ReferenceParam participant = new ReferenceParam();
		
		participant.setValue(PARTICIPANT_FULL_NAME);
		participant.setChain(Practitioner.SP_NAME);
		
		ReferenceParam badParticipant = new ReferenceParam();
		
		badParticipant.setValue(WRONG_NAME);
		badParticipant.setChain(Practitioner.SP_NAME);
		
		participantReference.addValue(new ReferenceOrListParam().add(participant).add(badParticipant));
		
		SearchParameterMap theParams = new SearchParameterMap()
		        .addParameter(FhirConstants.PARTICIPANT_REFERENCE_SEARCH_HANDLER, participantReference);
		IBundleProvider results = search(theParams);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList, not(empty()));
		assertThat(resultList, hasSize(greaterThanOrEqualTo(1)));
	}
	
	@Test
	public void searchForEncounters_shouldReturnEmptyListOfEncountersByMultipleParticipantNameAnd() {
		ReferenceAndListParam participantReference = new ReferenceAndListParam();
		ReferenceParam participant = new ReferenceParam();
		
		participant.setValue(PARTICIPANT_FULL_NAME);
		participant.setChain(Practitioner.SP_NAME);
		
		ReferenceParam badParticipant = new ReferenceParam();
		
		badParticipant.setValue(WRONG_NAME);
		badParticipant.setChain(Practitioner.SP_NAME);
		
		participantReference.addValue(new ReferenceOrListParam().add(participant))
		        .addAnd(new ReferenceOrListParam().add(badParticipant));
		
		SearchParameterMap theParams = new SearchParameterMap()
		        .addParameter(FhirConstants.PARTICIPANT_REFERENCE_SEARCH_HANDLER, participantReference);
		IBundleProvider results = search(theParams);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList, empty());
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
		assertThat(resultList, hasSize(greaterThanOrEqualTo(1)));
		assertThat(((Encounter) resultList.iterator().next()).getParticipantFirstRep().getIndividual().getReferenceElement()
		        .getIdPart(),
		    equalTo(PARTICIPANT_UUID));
	}
	
	@Test
	public void searchForEncounters_shouldSearchForEncountersByMultipleParticipantUuidOr() {
		ReferenceAndListParam participantReference = new ReferenceAndListParam();
		ReferenceParam participant = new ReferenceParam();
		
		participant.setValue(PARTICIPANT_UUID);
		
		ReferenceParam badParticipant = new ReferenceParam();
		
		badParticipant.setValue(WRONG_UUID);
		
		participantReference.addValue(new ReferenceOrListParam().add(participant).add(badParticipant));
		
		SearchParameterMap theParams = new SearchParameterMap()
		        .addParameter(FhirConstants.PARTICIPANT_REFERENCE_SEARCH_HANDLER, participantReference);
		IBundleProvider results = search(theParams);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList, not(empty()));
		assertThat(resultList, hasSize(greaterThanOrEqualTo(1)));
	}
	
	@Test
	public void searchForEncounters_shouldReturnEmptyListOfEncountersByMultipleParticipantUuidAnd() {
		ReferenceAndListParam participantReference = new ReferenceAndListParam();
		ReferenceParam participant = new ReferenceParam();
		
		participant.setValue(PARTICIPANT_UUID);
		
		ReferenceParam badParticipant = new ReferenceParam();
		
		badParticipant.setValue(WRONG_UUID);
		
		participantReference.addValue(new ReferenceOrListParam().add(participant))
		        .addAnd(new ReferenceOrListParam().add(badParticipant));
		
		SearchParameterMap theParams = new SearchParameterMap()
		        .addParameter(FhirConstants.PARTICIPANT_REFERENCE_SEARCH_HANDLER, participantReference);
		IBundleProvider results = search(theParams);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList, empty());
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
		assertThat(resultList, hasSize(greaterThanOrEqualTo(1)));
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
		assertThat(resultList, hasSize(greaterThanOrEqualTo(1)));
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
		assertThat(resultList, hasSize(greaterThanOrEqualTo(1)));
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
		assertThat(resultList, hasSize(greaterThanOrEqualTo(1)));
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
		assertThat(resultList, hasSize(greaterThanOrEqualTo(1)));
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
		
		subjectReference.addValue(new ReferenceOrListParam().add(subjectIdentifier))
		        .addAnd(new ReferenceOrListParam().add(subjectGiven));
		
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.PATIENT_REFERENCE_SEARCH_HANDLER,
		    subjectReference);
		IBundleProvider results = search(theParams);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList, not(empty()));
		assertThat(resultList, hasSize(greaterThanOrEqualTo(1)));
		assertThat(((Encounter) resultList.iterator().next()).getId(), equalTo(ENCOUNTER_UUID));
		assertThat(((Encounter) resultList.iterator().next()).getSubject().getReference(), endsWith(PATIENT_UUID));
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
		
		participantReference.addValue(new ReferenceOrListParam().add(participantName))
		        .addAnd(new ReferenceOrListParam().add(participantGiven))
		        .addAnd(new ReferenceOrListParam().add(participantFamily));
		
		SearchParameterMap theParams = new SearchParameterMap()
		        .addParameter(FhirConstants.PARTICIPANT_REFERENCE_SEARCH_HANDLER, participantReference);
		IBundleProvider results = search(theParams);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList, not(empty()));
		assertThat(resultList, hasSize(greaterThanOrEqualTo(1)));
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
		
		locationReference.addValue(new ReferenceOrListParam().add(locationCity))
		        .addAnd(new ReferenceOrListParam().add(locationCountry))
		        .addAnd(new ReferenceOrListParam().add(locationState));
		
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.LOCATION_REFERENCE_SEARCH_HANDLER,
		    locationReference);
		IBundleProvider results = search(theParams);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList, not(empty()));
		assertThat(resultList, hasSize(greaterThanOrEqualTo(1)));
	}
	
	@Test
	public void searchForEncounters_shouldSearchForEncountersByUuid() {
		TokenAndListParam uuid = new TokenAndListParam().addAnd(new TokenParam(ENCOUNTER_UUID));
		
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.COMMON_SEARCH_HANDLER,
		    FhirConstants.ID_PROPERTY, uuid);
		
		IBundleProvider results = search(theParams);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList, not(empty()));
		assertThat(resultList, hasSize(equalTo(1)));
		assertThat(((Encounter) resultList.iterator().next()).getIdElement().getIdPart(), equalTo(ENCOUNTER_UUID));
	}
	
	@Test
	public void searchForEncounters_shouldSearchForEncountersByLastUpdatedDateCreated() {
		DateRangeParam lastUpdated = new DateRangeParam().setUpperBound(DATE_CREATED).setLowerBound(DATE_CREATED);
		
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.COMMON_SEARCH_HANDLER,
		    FhirConstants.LAST_UPDATED_PROPERTY, lastUpdated);
		
		IBundleProvider results = search(theParams);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList, not(empty()));
		assertThat(resultList.size(), equalTo(2));
	}
	
	@Test
	public void searchForEncounters_shouldSearchForEncountersByMatchingUuidAndLastUpdated() {
		TokenAndListParam uuid = new TokenAndListParam().addAnd(new TokenParam(ENCOUNTER_UUID));
		DateRangeParam lastUpdated = new DateRangeParam().setUpperBound(DATE_CREATED).setLowerBound(DATE_CREATED);
		
		SearchParameterMap theParams = new SearchParameterMap()
		        .addParameter(FhirConstants.COMMON_SEARCH_HANDLER, FhirConstants.ID_PROPERTY, uuid)
		        .addParameter(FhirConstants.COMMON_SEARCH_HANDLER, FhirConstants.LAST_UPDATED_PROPERTY, lastUpdated);
		
		IBundleProvider results = search(theParams);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList, not(empty()));
		assertThat(resultList, hasSize(equalTo(1)));
		assertThat(((Encounter) resultList.iterator().next()).getIdElement().getIdPart(), equalTo(ENCOUNTER_UUID));
	}
	
	@Test
	public void searchForEncounters_shouldReturnEmptyListByMismatchingUuidAndLastUpdated() {
		TokenAndListParam uuid = new TokenAndListParam().addAnd(new TokenParam(ENCOUNTER_UUID));
		DateRangeParam lastUpdated = new DateRangeParam().setUpperBound(WRONG_DATE_CREATED)
		        .setLowerBound(WRONG_DATE_CREATED);
		
		SearchParameterMap theParams = new SearchParameterMap()
		        .addParameter(FhirConstants.COMMON_SEARCH_HANDLER, FhirConstants.ID_PROPERTY, uuid)
		        .addParameter(FhirConstants.COMMON_SEARCH_HANDLER, FhirConstants.LAST_UPDATED_PROPERTY, lastUpdated);
		
		IBundleProvider results = search(theParams);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList, empty());
	}
	
	@Test
	public void searchForEncounters_shouldIncludePatientsWithReturnedResults() {
		TokenAndListParam uuid = new TokenAndListParam().addAnd(new TokenParam(ENCOUNTER_UUID));
		HashSet<Include> includes = new HashSet<>();
		includes.add(new Include("Encounter:patient"));
		
		SearchParameterMap theParams = new SearchParameterMap()
		        .addParameter(FhirConstants.COMMON_SEARCH_HANDLER, FhirConstants.ID_PROPERTY, uuid)
		        .addParameter(FhirConstants.INCLUDE_SEARCH_HANDLER, includes);
		
		IBundleProvider results = search(theParams);
		
		assertThat(results, notNullValue());
		assertThat(results.size(), equalTo(1));
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList.size(), equalTo(2)); // included resource added as part of the result list
		
		Encounter returnedEncounter = (Encounter) resultList.iterator().next();
		assertThat(resultList, hasItem(allOf(is(instanceOf(Patient.class)),
		    hasProperty("id", equalTo(returnedEncounter.getSubject().getReferenceElement().getIdPart())))));
	}
	
	@Test
	public void searchForEncounters_shouldIncludeParticipantsWithReturnedResults() {
		TokenAndListParam uuid = new TokenAndListParam().addAnd(new TokenParam(ENCOUNTER_UUID));
		HashSet<Include> includes = new HashSet<>();
		includes.add(new Include("Encounter:participant"));
		
		SearchParameterMap theParams = new SearchParameterMap()
		        .addParameter(FhirConstants.COMMON_SEARCH_HANDLER, FhirConstants.ID_PROPERTY, uuid)
		        .addParameter(FhirConstants.INCLUDE_SEARCH_HANDLER, includes);
		
		IBundleProvider results = search(theParams);
		
		assertThat(results, notNullValue());
		assertThat(results.size(), equalTo(1));
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList.size(), equalTo(2)); // included resource added as part of the result list
		
		Encounter returnedEncounter = (Encounter) resultList.iterator().next();
		assertThat(resultList, hasItem(allOf(is(instanceOf(Practitioner.class)), hasProperty("id",
		    equalTo(returnedEncounter.getParticipantFirstRep().getIndividual().getReferenceElement().getIdPart())))));
	}
	
	@Test
	public void searchForEncounters_shouldIncludeLocationsWithReturnedResults() {
		TokenAndListParam uuid = new TokenAndListParam().addAnd(new TokenParam(ENCOUNTER_UUID));
		HashSet<Include> includes = new HashSet<>();
		includes.add(new Include("Encounter:location"));
		
		SearchParameterMap theParams = new SearchParameterMap()
		        .addParameter(FhirConstants.COMMON_SEARCH_HANDLER, FhirConstants.ID_PROPERTY, uuid)
		        .addParameter(FhirConstants.INCLUDE_SEARCH_HANDLER, includes);
		
		IBundleProvider results = search(theParams);
		
		assertThat(results, notNullValue());
		assertThat(results.size(), equalTo(1));
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList.size(), equalTo(2)); // included resource added as part of the result list
		
		Encounter returnedEncounter = (Encounter) resultList.iterator().next();
		assertThat(resultList, hasItem(allOf(is(instanceOf(Location.class)), hasProperty("id",
		    equalTo(returnedEncounter.getLocationFirstRep().getLocation().getReferenceElement().getIdPart())))));
	}
	
	@Test
	public void searchForEncounters_shouldHandleMultipleIncludes() {
		TokenAndListParam uuid = new TokenAndListParam().addAnd(new TokenParam(ENCOUNTER_UUID));
		HashSet<Include> includes = new HashSet<>();
		includes.add(new Include("Encounter:location"));
		includes.add(new Include("Encounter:patient"));
		
		SearchParameterMap theParams = new SearchParameterMap()
		        .addParameter(FhirConstants.COMMON_SEARCH_HANDLER, FhirConstants.ID_PROPERTY, uuid)
		        .addParameter(FhirConstants.INCLUDE_SEARCH_HANDLER, includes);
		
		IBundleProvider results = search(theParams);
		
		assertThat(results, notNullValue());
		assertThat(results.size(), equalTo(1));
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList.size(), equalTo(3)); // included resources(location + patient)  added as part of the result list
		
		Encounter returnedEncounter = (Encounter) resultList.iterator().next();
		assertThat(resultList, hasItem(allOf(is(instanceOf(Patient.class)),
		    hasProperty("id", equalTo(returnedEncounter.getSubject().getReferenceElement().getIdPart())))));
		assertThat(resultList, hasItem(allOf(is(instanceOf(Location.class)), hasProperty("id",
		    equalTo(returnedEncounter.getLocationFirstRep().getLocation().getReferenceElement().getIdPart())))));
	}
	
	@Test
	public void searchForEncounters_shouldReverseIncludeObservationsWithReturnedResults() {
		TokenAndListParam uuid = new TokenAndListParam().addAnd(new TokenParam(ENC_UUID));
		HashSet<Include> revIncludes = new HashSet<>();
		revIncludes.add(new Include("Observation:encounter"));
		
		SearchParameterMap theParams = new SearchParameterMap()
		        .addParameter(FhirConstants.COMMON_SEARCH_HANDLER, FhirConstants.ID_PROPERTY, uuid)
		        .addParameter(FhirConstants.REVERSE_INCLUDE_SEARCH_HANDLER, revIncludes);
		
		IBundleProvider results = search(theParams);
		
		assertThat(results, notNullValue());
		assertThat(results.size(), equalTo(1));
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList.size(), equalTo(7)); // reverse included resources added as part of the result list
		assertThat(resultList.subList(1, 7), everyItem(allOf(is(instanceOf(Observation.class)),
		    hasProperty("encounter", hasProperty("referenceElement", hasProperty("idPart", equalTo(ENC_UUID)))))));
	}
	
	@Test
	public void searchForEncounters_shouldReverseIncludeMedicationRequestsWithReturnedResults() {
		TokenAndListParam uuid = new TokenAndListParam().addAnd(new TokenParam(ENCOUNTER_UUID));
		HashSet<Include> revIncludes = new HashSet<>();
		revIncludes.add(new Include("MedicationRequest:encounter"));
		
		SearchParameterMap theParams = new SearchParameterMap()
		        .addParameter(FhirConstants.COMMON_SEARCH_HANDLER, FhirConstants.ID_PROPERTY, uuid)
		        .addParameter(FhirConstants.REVERSE_INCLUDE_SEARCH_HANDLER, revIncludes);
		
		IBundleProvider results = search(theParams);
		
		assertThat(results, notNullValue());
		assertThat(results.size(), equalTo(1));
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList.size(), equalTo(7)); // reverse included resources added as part of the result list
		assertThat(resultList.subList(1, 7), everyItem(allOf(is(instanceOf(MedicationRequest.class)),
		    hasProperty("encounter", hasProperty("referenceElement", hasProperty("idPart", equalTo(ENCOUNTER_UUID)))))));
	}
	
	@Test
	public void searchForEncounters_shouldReverseIncludeServiceRequestsWithReturnedResults() {
		TokenAndListParam uuid = new TokenAndListParam().addAnd(new TokenParam(ENCOUNTER_UUID));
		HashSet<Include> revIncludes = new HashSet<>();
		revIncludes.add(new Include("ServiceRequest:encounter"));
		
		SearchParameterMap theParams = new SearchParameterMap()
		        .addParameter(FhirConstants.COMMON_SEARCH_HANDLER, FhirConstants.ID_PROPERTY, uuid)
		        .addParameter(FhirConstants.REVERSE_INCLUDE_SEARCH_HANDLER, revIncludes);
		
		IBundleProvider results = search(theParams);
		
		assertThat(results, notNullValue());
		assertThat(results.size(), equalTo(1));
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList.size(), equalTo(4)); // reverse included resources added as part of the result list
		assertThat(resultList.subList(1, 4), everyItem(allOf(is(instanceOf(ServiceRequest.class)),
		    hasProperty("encounter", hasProperty("referenceElement", hasProperty("idPart", equalTo(ENCOUNTER_UUID)))))));
	}
	
	@Test
	public void searchForEncounters_shouldHandleMultipleReverseIncludes() {
		TokenAndListParam uuid = new TokenAndListParam().addAnd(new TokenParam(ENCOUNTER_UUID));
		HashSet<Include> revIncludes = new HashSet<>();
		revIncludes.add(new Include("ServiceRequest:encounter"));
		revIncludes.add(new Include("MedicationRequest:encounter"));
		
		SearchParameterMap theParams = new SearchParameterMap()
		        .addParameter(FhirConstants.COMMON_SEARCH_HANDLER, FhirConstants.ID_PROPERTY, uuid)
		        .addParameter(FhirConstants.REVERSE_INCLUDE_SEARCH_HANDLER, revIncludes);
		
		IBundleProvider results = search(theParams);
		
		assertThat(results, notNullValue());
		assertThat(results.size(), equalTo(1));
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList.size(), equalTo(10)); // reverse included resources (6 medication request + 3 service request) added as part of the result list
		assertThat(resultList.subList(1, 10),
		    everyItem(allOf(anyOf(is(instanceOf(MedicationRequest.class)), is(instanceOf(ServiceRequest.class))),
		        hasProperty("encounter", hasProperty("referenceElement", hasProperty("idPart", equalTo(ENCOUNTER_UUID)))))));
	}
	
	@Test
	public void searchForEncounter_shouldReturnEncounterEverything() {
		TokenAndListParam encounterId = new TokenAndListParam().addAnd(new TokenParam().setValue(ENCOUNTER_UUID));
		
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.EVERYTHING_SEARCH_HANDLER, "")
		        .addParameter(FhirConstants.COMMON_SEARCH_HANDLER, FhirConstants.ID_PROPERTY, encounterId);
		
		populateIncludeForEverythingOperationParams(theParams);
		populateReverseIncludeForEverythingOperationParams(theParams);
		
		IBundleProvider results = search(theParams);
		
		assertThat(results, notNullValue());
		assertThat(results.size(), equalTo(13));
		
		List<IBaseResource> resultList = getAllResources(results);
		
		assertThat(resultList.size(), equalTo(13));
	}
	
	private void populateReverseIncludeForEverythingOperationParams(SearchParameterMap theParams) {
		HashSet<Include> revIncludes = new HashSet<>();
		
		revIncludes.add(new Include(FhirConstants.OBSERVATION + ":" + FhirConstants.INCLUDE_ENCOUNTER_PARAM));
		revIncludes.add(new Include(FhirConstants.DIAGNOSTIC_REPORT + ":" + FhirConstants.INCLUDE_ENCOUNTER_PARAM));
		revIncludes.add(new Include(FhirConstants.MEDICATION_REQUEST + ":" + FhirConstants.INCLUDE_ENCOUNTER_PARAM));
		revIncludes.add(new Include(FhirConstants.SERVICE_REQUEST + ":" + FhirConstants.INCLUDE_ENCOUNTER_PARAM));
		
		theParams.addParameter(FhirConstants.REVERSE_INCLUDE_SEARCH_HANDLER, revIncludes);
	}
	
	private void populateIncludeForEverythingOperationParams(SearchParameterMap theParams) {
		HashSet<Include> includes = new HashSet<>();
		
		includes.add(new Include(FhirConstants.ENCOUNTER + ":" + FhirConstants.INCLUDE_PATIENT_PARAM));
		includes.add(new Include(FhirConstants.ENCOUNTER + ":" + FhirConstants.INCLUDE_LOCATION_PARAM));
		includes.add(new Include(FhirConstants.ENCOUNTER + ":" + FhirConstants.INCLUDE_PARTICIPANT_PARAM));
		
		theParams.addParameter(FhirConstants.INCLUDE_SEARCH_HANDLER, includes);
	}
	
	@Test
	public void shouldReturnCollectionOfEncountersSortedByEncounterDate() {
		SortSpec sort = new SortSpec();
		sort.setParamName("date");
		sort.setOrder(SortOrderEnum.ASC);
		
		SearchParameterMap theParams = new SearchParameterMap().setSortSpec(sort);
		
		IBundleProvider results = search(theParams);
		
		assertThat(results, notNullValue());
		assertThat(results.size(), greaterThan(1));
		
		List<Encounter> resultList = get(results).stream().filter(it -> it instanceof Encounter).map(it -> (Encounter) it)
		        .collect(Collectors.toList());
		
		assertThat(resultList, hasSize(greaterThan(1)));
		
		for (int i = 1; i < resultList.size(); i++) {
			assertNotNull(resultList.get(i - 1).getPeriod().getStart());
			assertNotNull(resultList.get(i).getPeriod().getStart());
			assertThat(resultList.get(i - 1).getPeriod().getStart(),
			    lessThanOrEqualTo(resultList.get(i).getPeriod().getStart()));
		}
		
		sort.setOrder(SortOrderEnum.DESC);
		
		theParams.setSortSpec(sort);
		
		results = search(theParams);
		
		assertThat(results, notNullValue());
		assertThat(results.size(), greaterThan(1));
		
		resultList = get(results).stream().filter(it -> it instanceof Encounter).map(it -> (Encounter) it)
		        .collect(Collectors.toList());
		
		assertThat(resultList, hasSize(greaterThan(1)));
		
		for (int i = 1; i < resultList.size(); i++) {
			assertNotNull(resultList.get(i - 1).getPeriod().getStart());
			assertNotNull(resultList.get(i).getPeriod().getStart());
			assertThat(resultList.get(i - 1).getPeriod().getStart(),
			    greaterThanOrEqualTo(resultList.get(i).getPeriod().getStart()));
		}
	}
}
