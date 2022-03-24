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
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertNotNull;

import java.util.List;
import java.util.stream.Collectors;

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
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.Practitioner;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.Visit;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.TestFhirSpringConfiguration;
import org.openmrs.module.fhir2.api.dao.FhirVisitDao;
import org.openmrs.module.fhir2.api.search.param.SearchParameterMap;
import org.openmrs.module.fhir2.api.translators.EncounterTranslator;
import org.openmrs.test.BaseModuleContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

@ContextConfiguration(classes = TestFhirSpringConfiguration.class, inheritLocations = false)
public class VisitSearchQueryTest extends BaseModuleContextSensitiveTest {
	
	private static final String VISIT_INITIAL_DATA_XML = "org/openmrs/module/fhir2/api/dao/impl/FhirVisitDaoImplTest_initial_data.xml";
	
	private static final String LOCATION_INITIAL_DATA_XML = "org/openmrs/module/fhir2/api/dao/impl/FhirLocationDaoImplTest_initial_data.xml";
	
	private static final String VISIT_UUID = "65aefd46-973d-4526-89de-93842c80ad11";
	
	private static final String VISIT_STARTDATE = "2020-01-04";
	
	private static final String DATE_CREATED = "2014-01-04";
	
	private static final String WRONG_DATE_CREATED = "2015-05-04";
	
	private static final String LOCATION_UUID = "c0938432-1691-11df-97a5-7038c432";
	
	private static final String PATIENT_FULL_NAME = "Rick M Sanchez";
	
	private static final String PATIENT_FULL_NAME_2 = "Horatio Test Hornblower";
	
	private static final String PATIENT_IDENTIFIER = "12345K";
	
	private static final String PATIENT_IDENTIFIER_2 = "101-6";
	
	private static final String WRONG_IDENTIFIER = "Wrong identifier";
	
	private static final String PATIENT_UUID = "a7e04421-525f-442f-8138-05b619d16def";
	
	private static final String VISIT_TYPE_UUID = "c0c579b0-8e59-401d-8a4a-976a0b183519";
	
	private static final String PARTICIPANT_IDENTIFIER = "Test";
	
	private static final String PARTICIPANT_VISIT_UUID = "65aefd46-973d-4526-89de-93842c80ad11";
	
	private static final int START_INDEX = 0;
	
	private static final int END_INDEX = 10;
	
	@Autowired
	private FhirVisitDao dao;
	
	@Autowired
	private EncounterTranslator<Visit> translator;
	
	@Autowired
	private SearchQueryInclude<Encounter> searchQueryInclude;
	
	@Autowired
	private SearchQuery<Visit, Encounter, FhirVisitDao, EncounterTranslator<Visit>, SearchQueryInclude<Encounter>> searchQuery;
	
	private List<IBaseResource> get(IBundleProvider results) {
		return results.getResources(START_INDEX, END_INDEX);
	}
	
	private IBundleProvider search(SearchParameterMap theParams) {
		return searchQuery.getQueryResults(theParams, dao, translator, searchQueryInclude);
	}
	
	@Before
	public void setUp() throws Exception {
		executeDataSet(VISIT_INITIAL_DATA_XML);
		executeDataSet(LOCATION_INITIAL_DATA_XML);
	}
	
	@Test
	public void searchForVisits_shouldSearchForVisitsByDate() {
		DateRangeParam date = new DateRangeParam(new DateParam(VISIT_STARTDATE));
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.DATE_RANGE_SEARCH_HANDLER, date);
		IBundleProvider results = search(theParams);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList, not(empty()));
		assertThat(resultList, hasSize(greaterThanOrEqualTo(1)));
		assertThat(((Encounter) resultList.iterator().next()).getId(), equalTo(VISIT_UUID));
	}
	
	@Test
	public void searchForVisits_shouldSearchForVisitsByLocationUUID() {
		ReferenceAndListParam locationReference = new ReferenceAndListParam();
		ReferenceParam location = new ReferenceParam();
		
		location.setValue(LOCATION_UUID);
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
		    equalTo(LOCATION_UUID));
	}
	
	@Test
	public void searchForVisits_shouldSearchForVisitsByParticipantIdentifier() {
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
		assertThat(((Encounter) resultList.iterator().next()).getId(), equalTo(PARTICIPANT_VISIT_UUID));
	}
	
	@Test
	public void searchForVisits_shouldSearchForVisitsBySubjectIdentifier() {
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
	public void searchForVisits_shouldSearchForVisitsByMultipleSubjectIdentifierOr() {
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
	public void searchForVisits_shouldReturnEmptyListOfVisitsByMultipleSubjectIdentifierAnd() {
		ReferenceAndListParam referenceParam = new ReferenceAndListParam();
		ReferenceParam patient = new ReferenceParam();
		
		patient.setValue(PATIENT_IDENTIFIER);
		patient.setChain(Patient.SP_IDENTIFIER);
		
		ReferenceParam patient_2 = new ReferenceParam();
		
		patient_2.setValue(PATIENT_IDENTIFIER_2);
		patient_2.setChain(Patient.SP_IDENTIFIER);
		
		referenceParam.addValue(new ReferenceOrListParam().add(patient)).addAnd(new ReferenceOrListParam().add(patient_2));
		
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.PATIENT_REFERENCE_SEARCH_HANDLER,
		    referenceParam);
		IBundleProvider results = search(theParams);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList, empty());
	}
	
	@Test
	public void searchForVisits_shouldSearchForVisitsBySubjectName() {
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
		assertThat(((Encounter) resultList.iterator().next()).getId(), equalTo(VISIT_UUID));
	}
	
	@Test
	public void searchForVisits_shouldSearchForVisitsByMultipleSubjectNameOr() {
		ReferenceAndListParam referenceParam = new ReferenceAndListParam();
		ReferenceParam patient = new ReferenceParam();
		
		patient.setValue(PATIENT_FULL_NAME);
		patient.setChain(Patient.SP_NAME);
		
		ReferenceParam Patient_2 = new ReferenceParam();
		
		Patient_2.setValue(PATIENT_FULL_NAME_2);
		Patient_2.setChain(Patient.SP_NAME);
		
		referenceParam.addValue(new ReferenceOrListParam().add(patient).add(Patient_2));
		
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.PATIENT_REFERENCE_SEARCH_HANDLER,
		    referenceParam);
		IBundleProvider results = search(theParams);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList, not(empty()));
		assertThat(resultList, hasSize(greaterThanOrEqualTo(1)));
	}
	
	@Test
	public void searchForVisits_shouldSearchForVisitsByTypeUUID() {
		TokenAndListParam typeUuid = new TokenAndListParam().addAnd(new TokenParam(VISIT_TYPE_UUID));
		SearchParameterMap theParams = new SearchParameterMap()
		        .addParameter(FhirConstants.ENCOUNTER_TYPE_REFERENCE_SEARCH_HANDLER, typeUuid);
		
		IBundleProvider results = search(theParams);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList, not(empty()));
		assertThat(resultList, hasSize(greaterThanOrEqualTo(1)));
		assertThat(((Encounter) resultList.iterator().next()).getIdElement().getIdPart(), equalTo(VISIT_UUID));
	}
	
	@Test
	public void searchForVisits_shouldSearchForVisitsByUuid() {
		TokenAndListParam uuid = new TokenAndListParam().addAnd(new TokenParam(VISIT_UUID));
		
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.COMMON_SEARCH_HANDLER,
		    FhirConstants.ID_PROPERTY, uuid);
		
		IBundleProvider results = search(theParams);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList, not(empty()));
		assertThat(resultList, hasSize(equalTo(1)));
		assertThat(((Encounter) resultList.iterator().next()).getIdElement().getIdPart(), equalTo(VISIT_UUID));
	}
	
	@Test
	public void searchForVisits_shouldSearchForVisitsByLastUpdatedDateCreated() {
		DateRangeParam lastUpdated = new DateRangeParam().setUpperBound(DATE_CREATED).setLowerBound(DATE_CREATED);
		
		SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.COMMON_SEARCH_HANDLER,
		    FhirConstants.LAST_UPDATED_PROPERTY, lastUpdated);
		
		IBundleProvider results = search(theParams);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList, not(empty()));
		assertThat(resultList.size(), equalTo(1));
	}
	
	@Test
	public void searchForVisits_shouldSearchForVisitsByMatchingUuidAndLastUpdated() {
		TokenAndListParam uuid = new TokenAndListParam().addAnd(new TokenParam(VISIT_UUID));
		DateRangeParam lastUpdated = new DateRangeParam().setUpperBound(DATE_CREATED).setLowerBound(DATE_CREATED);
		
		SearchParameterMap theParams = new SearchParameterMap()
		        .addParameter(FhirConstants.COMMON_SEARCH_HANDLER, FhirConstants.ID_PROPERTY, uuid)
		        .addParameter(FhirConstants.COMMON_SEARCH_HANDLER, FhirConstants.LAST_UPDATED_PROPERTY, lastUpdated);
		
		IBundleProvider results = search(theParams);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList, not(empty()));
		assertThat(resultList, hasSize(equalTo(1)));
		assertThat(((Encounter) resultList.iterator().next()).getIdElement().getIdPart(), equalTo(VISIT_UUID));
	}
	
	@Test
	public void searchForVisits_shouldReturnEmptyListByMismatchingUuidAndLastUpdated() {
		TokenAndListParam uuid = new TokenAndListParam().addAnd(new TokenParam(VISIT_UUID));
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
