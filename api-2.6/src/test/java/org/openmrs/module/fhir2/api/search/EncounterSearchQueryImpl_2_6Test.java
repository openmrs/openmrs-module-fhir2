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
import org.hl7.fhir.r4.model.Encounter;
import org.hl7.fhir.r4.model.MedicationDispense;
import org.hl7.fhir.r4.model.MedicationRequest;
import org.junit.Before;
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
public class EncounterSearchQueryImpl_2_6Test extends BaseModuleContextSensitiveTest {
	
	public static final String ENCOUNTER_UUID = "y403fafb-e5e4-42d0-9d11-4f52e89d123r"; // encounter 6 from standard test dataset
	
	public static final int START_INDEX = 0;
	
	public static final int END_INDEX = 10;
	
	@Autowired
	private FhirEncounterDao dao;
	
	@Autowired
	private EncounterTranslator<org.openmrs.Encounter> translator;
	
	@Autowired
	private SearchQueryInclude<Encounter> searchQueryInclude;
	
	@Autowired
	private SearchQuery<org.openmrs.Encounter, Encounter, FhirEncounterDao, EncounterTranslator<org.openmrs.Encounter>, SearchQueryInclude<Encounter>> searchQuery;
	
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
	public void searchForEncounters_shouldReverseIncludeMedicationRequestsAndAssociatedMedicationDispensesWithReturnedResults() {
		TokenAndListParam uuid = new TokenAndListParam().addAnd(new TokenParam(ENCOUNTER_UUID));
		HashSet<Include> revIncludes = new HashSet<>();
		revIncludes.add(new Include("MedicationRequest:encounter"));
		revIncludes.add(new Include("MedicationDispense:prescription", true));
		
		SearchParameterMap theParams = new SearchParameterMap()
		        .addParameter(FhirConstants.COMMON_SEARCH_HANDLER, FhirConstants.ID_PROPERTY, uuid)
		        .addParameter(FhirConstants.REVERSE_INCLUDE_SEARCH_HANDLER, revIncludes);
		
		IBundleProvider results = search(theParams);
		
		assertThat(results, notNullValue());
		assertThat(results.size(), equalTo(1));
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, notNullValue());
		
		assertThat(resultList.size(), equalTo(10)); // reverse included resources added as part of the result list
		assertThat(resultList.stream().filter(result -> result instanceof Encounter).collect(Collectors.toList()),
		    is(iterableWithSize(1))); // the actual matched encounter
		assertThat(resultList.stream().filter(result -> result instanceof MedicationRequest).collect(Collectors.toList()),
		    is(iterableWithSize(8))); // 8 requests that reference that encounter
		assertThat(resultList.stream().filter(result -> result instanceof MedicationDispense).collect(Collectors.toList()),
		    is(iterableWithSize(1))); // 1 dispense that references the above requests
	}
}
