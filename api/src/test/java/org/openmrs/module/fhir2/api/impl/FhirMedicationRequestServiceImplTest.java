/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.fhir2.api.impl;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.hamcrest.Matchers;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.MedicationRequest;
import org.hl7.fhir.r4.model.Practitioner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openmrs.DrugOrder;
import org.openmrs.Encounter;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.api.dao.FhirMedicationRequestDao;
import org.openmrs.module.fhir2.api.search.SearchQuery;
import org.openmrs.module.fhir2.api.search.SearchQueryBundleProvider;
import org.openmrs.module.fhir2.api.search.param.SearchParameterMap;
import org.openmrs.module.fhir2.api.translators.MedicationRequestTranslator;

import ca.uhn.fhir.rest.api.server.IBundleProvider;
import ca.uhn.fhir.rest.param.ReferenceAndListParam;
import ca.uhn.fhir.rest.param.ReferenceOrListParam;
import ca.uhn.fhir.rest.param.ReferenceParam;

@RunWith(MockitoJUnitRunner.class)
public class FhirMedicationRequestServiceImplTest {
	
	private static final String MEDICATION_REQUEST_UUID = "d102c80f-1yz9-4da3-0099-8902ce886891";
	
	private static final String BAD_MEDICATION_REQUEST_UUID = "d102c80f-1yz9-4da3-0099-8902ce886891";
	
	@Mock
	private MedicationRequestTranslator medicationRequestTranslator;
	
	@Mock
	private FhirMedicationRequestDao dao;

	@Mock
	private SearchQuery<DrugOrder, MedicationRequest, FhirMedicationRequestDao, MedicationRequestTranslator> searchQuery;
	
	private FhirMedicationRequestServiceImpl medicationRequestService;
	
	private MedicationRequest medicationRequest;
	
	private DrugOrder drugOrder;
	
	@Before
	public void setup() {
		medicationRequestService = new FhirMedicationRequestServiceImpl();
		medicationRequestService.setDao(dao);
		medicationRequestService.setTranslator(medicationRequestTranslator);
		medicationRequestService.setSearchQuery(searchQuery);
		
		medicationRequest = new MedicationRequest();
		medicationRequest.setId(MEDICATION_REQUEST_UUID);
		
		drugOrder = new DrugOrder();
		drugOrder.setUuid(MEDICATION_REQUEST_UUID);
	}
	
	@Test
	public void shouldGetMedicationRequestByUuid() {
		when(dao.get(MEDICATION_REQUEST_UUID)).thenReturn(drugOrder);
		when(medicationRequestTranslator.toFhirResource(drugOrder)).thenReturn(medicationRequest);
		
		MedicationRequest result = medicationRequestService.get(MEDICATION_REQUEST_UUID);
		assertThat(result, notNullValue());
		assertThat(result.getId(), notNullValue());
		assertThat(result.getId(), equalTo(MEDICATION_REQUEST_UUID));
	}
	
	@Test
	public void shouldReturnNullForBadMedicationRequestUuid() {
		MedicationRequest result = medicationRequestService.get(BAD_MEDICATION_REQUEST_UUID);
		assertThat(result, nullValue());
	}
	
	//test about search
	
	private List<IBaseResource> get(IBundleProvider results) {
		return results.getResources(0, 10);
	}

	@Test
	public void searchForMedicationRequest_shouldReturnCollectionOfMedicationRequestByParticipant() {
		ReferenceAndListParam participant = new ReferenceAndListParam();
		
		participant.addValue(new ReferenceOrListParam()
		        .add(new ReferenceParam().setValue("1").setChain(Practitioner.SP_IDENTIFIER)));
		
		Collection<DrugOrder> drugOrders = new ArrayList<>();

		drugOrders.add(drugOrder);
		
		medicationRequest.setId(MEDICATION_REQUEST_UUID);
		
		SearchParameterMap theParams = new SearchParameterMap()
		        .addParameter(FhirConstants.PARTICIPANT_REFERENCE_SEARCH_HANDLER, participant);
		
		when(dao.search(any(), anyInt(), anyInt())).thenReturn(drugOrders);
		when(medicationRequestTranslator.toFhirResource(drugOrder)).thenReturn(medicationRequest);

		when(searchQuery.getQueryResults(any(), any(), any()))
		        .thenReturn(new SearchQueryBundleProvider<>(theParams, dao, medicationRequestTranslator));
		
		IBundleProvider results = medicationRequestService.searchForMedicationRequests(null, null, null, participant,
				null);
		
		List<IBaseResource> resultList = get(results);
		
		assertThat(results, Matchers.notNullValue());
		assertThat(resultList, not(empty()));
		assertThat(resultList.size(), greaterThanOrEqualTo(1));
	}
}
