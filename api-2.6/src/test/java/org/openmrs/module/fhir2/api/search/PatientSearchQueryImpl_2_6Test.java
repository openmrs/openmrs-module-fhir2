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
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.iterableWithSize;
import static org.hamcrest.Matchers.notNullValue;

import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

import ca.uhn.fhir.model.api.Include;
import ca.uhn.fhir.rest.api.server.IBundleProvider;
import ca.uhn.fhir.rest.param.TokenAndListParam;
import ca.uhn.fhir.rest.param.TokenParam;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.MedicationDispense;
import org.hl7.fhir.r4.model.MedicationRequest;
import org.hl7.fhir.r4.model.Patient;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.TestFhirSpringConfiguration;
import org.openmrs.module.fhir2.api.dao.FhirPatientDao;
import org.openmrs.module.fhir2.api.search.param.SearchParameterMap;
import org.openmrs.module.fhir2.api.translators.PatientTranslator;
import org.openmrs.test.BaseModuleContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

@ContextConfiguration(classes = TestFhirSpringConfiguration.class, inheritLocations = false)
public class PatientSearchQueryImpl_2_6Test extends BaseModuleContextSensitiveTest {
	
	private static final int START_INDEX = 0;
	
	private static final int END_INDEX = 10;
	
	@Autowired
	private PatientTranslator translator;
	
	@Autowired
	private FhirPatientDao dao;
	
	@Autowired
	private SearchQueryInclude<Patient> searchQueryInclude;
	
	@Autowired
	private SearchQuery<org.openmrs.Patient, Patient, FhirPatientDao, PatientTranslator, SearchQueryInclude<Patient>> searchQuery;
	
	private List<IBaseResource> get(IBundleProvider results) {
		return results.getResources(START_INDEX, END_INDEX);
	}
	
	private IBundleProvider search(SearchParameterMap theParams) {
		return searchQuery.getQueryResults(theParams, dao, translator, searchQueryInclude);
	}
	
	@Before
	public void setup() {
		executeDataSet("org/openmrs/api/include/MedicationDispenseServiceTest-initialData.xml");
		updateSearchIndex();
	}
	
	@Test
	public void searchForPatient_shouldReverseIncludeMedicationRequestsAndAssociatedMedicationDispensesWithReturnedResults() {
		TokenAndListParam uuid = new TokenAndListParam().addAnd(new TokenParam("da7f524f-27ce-4bb2-86d6-6d1d05312bd5")); // patient 2 from standard test dataset
		HashSet<Include> revIncludes = new HashSet<>();
		revIncludes.add(new Include("MedicationRequest:patient"));
		revIncludes.add(new Include("MedicationDispense:prescription", true));
		
		SearchParameterMap theParams = new SearchParameterMap()
		        .addParameter(FhirConstants.COMMON_SEARCH_HANDLER, FhirConstants.ID_PROPERTY, uuid)
		        .addParameter(FhirConstants.REVERSE_INCLUDE_SEARCH_HANDLER, revIncludes);
		
		IBundleProvider results = search(theParams);
		
		assertThat(results, notNullValue());
		assertThat(results.size(), equalTo(1));
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		
		assertThat(resultList.size(), equalTo(8)); // reverse included resources added as part of the result list
		assertThat(resultList.stream().filter(result -> result instanceof Patient).collect(Collectors.toList()),
		    is(iterableWithSize(1))); // the actual matched patient
		assertThat(resultList.stream().filter(result -> result instanceof MedicationRequest).collect(Collectors.toList()),
		    is(iterableWithSize(6))); // 6 requests that reference that patient (excluding discontinue orders)
		assertThat(resultList.stream().filter(result -> result instanceof MedicationDispense).collect(Collectors.toList()),
		    is(iterableWithSize(1))); // 1 dispense that references the above requests
	}
	
}
