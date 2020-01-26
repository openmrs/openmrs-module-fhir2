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

import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;

import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.ServiceRequest;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openmrs.Concept;
import org.openmrs.TestOrder;
import org.openmrs.module.fhir2.FhirTestConstants;
import org.openmrs.module.fhir2.api.translators.ConceptTranslator;
import org.openmrs.module.fhir2.api.translators.PatientIdentifierTranslator;

@RunWith(MockitoJUnitRunner.class)
public class ServiceRequestTranslatorImplTest {
	
	private static final String SERVICE_REQUEST_UUID = "4e4851c3-c265-400e-acc9-1f1b0ac7f9c4";
	
	private static final TestOrder.Action ORDER_ACTION = TestOrder.Action.NEW;

	private static final String LOINC_CODE = "1000-1";

	private ServiceRequestTranslatorImpl translator;

	@Mock
	private ConceptTranslator conceptTranslator;

	@Before
	public void setup() {
		translator = new ServiceRequestTranslatorImpl();
		translator.setConceptTranslator(conceptTranslator);
	}
	
	@Test
	public void shouldTranslateOpenmrsTestOrderToFhirServiceRequest() {
		TestOrder order = new TestOrder();
		ServiceRequest result = translator.toFhirResource(order);
		
		assertThat(result, notNullValue());
	}
	
	@Test
	public void shouldTranslateNewOrderToActiveServiceRequest() {
		TestOrder newOrder = new TestOrder();
		newOrder.setAction(ORDER_ACTION);
		
		ServiceRequest result = translator.toFhirResource(newOrder);
		assertThat(result, notNullValue());
		assertThat(result.getStatus(), equalTo(ServiceRequest.ServiceRequestStatus.ACTIVE));
		assertThat(result.getIntent(), equalTo(ServiceRequest.ServiceRequestIntent.ORDER));
	}
	
	@Ignore
	@Test
	public void shouldTranslateRevisedOrderToActiveServiceRequest() {
		TestOrder newOrder = new TestOrder();
		newOrder.setAction(TestOrder.Action.REVISE);
		
		ServiceRequest result = translator.toFhirResource(newOrder);
		assertThat(result, notNullValue());
		assertThat(result.getStatus(), equalTo(ServiceRequest.ServiceRequestStatus.ACTIVE));
		assertThat(result.getIntent(), equalTo(ServiceRequest.ServiceRequestIntent.ORDER));
	}
	
	@Test
	public void shouldTranslateDiscontinuedOrderToRevokedServiceRequest() {
		TestOrder newOrder = new TestOrder();
		newOrder.setAction(TestOrder.Action.DISCONTINUE);
		
		ServiceRequest result = translator.toFhirResource(newOrder);
		assertThat(result, notNullValue());
		assertThat(result.getStatus(), equalTo(ServiceRequest.ServiceRequestStatus.REVOKED));
		assertThat(result.getIntent(), equalTo(ServiceRequest.ServiceRequestIntent.ORDER));
	}

	@Ignore
	@Test
	public void shouldTranslateServiceRequestCode() {
		CodeableConcept codeableConcept = new CodeableConcept();
		Coding loincCoding = codeableConcept.addCoding();
		loincCoding.setSystem(FhirTestConstants.LOINC_SYSTEM_URL);
		loincCoding.setCode(LOINC_CODE);

		ServiceRequest fhirServiceRequest = new ServiceRequest();
		fhirServiceRequest.setId(SERVICE_REQUEST_UUID);
		fhirServiceRequest.setCode(codeableConcept);

		TestOrder translatedOrder = translator.toOpenmrsType(fhirServiceRequest);
		assertThat(translatedOrder, notNullValue());

		Concept result = translatedOrder.getConcept();

		assertThat(result, notNullValue());
		assertThat(result.getConceptMappings(), notNullValue());
		assertThat(result.getConceptMappings(), not(empty()));
		assertThat(result.getConceptMappings(), hasItem((hasProperty("conceptReferenceTerm", hasProperty("code", equalTo(LOINC_CODE))))));
	}

	@Test
	public void shouldTranslateOrderConcept() {
		Concept openmrsConcept = new Concept();
		TestOrder testOrder = new TestOrder();

		testOrder.setConcept(openmrsConcept);

		CodeableConcept codeableConcept = new CodeableConcept();
		Coding loincCoding = codeableConcept.addCoding();
		loincCoding.setSystem(FhirTestConstants.LOINC_SYSTEM_URL);
		loincCoding.setCode(LOINC_CODE);

		when(conceptTranslator.toFhirResource(openmrsConcept)).thenReturn(codeableConcept);

		CodeableConcept result = translator.toFhirResource(testOrder).getCode();

		assertThat(result, notNullValue());
		assertThat(result.getCoding(), notNullValue());
		assertThat(result.getCoding(), hasItem(hasProperty("system", equalTo(FhirTestConstants.LOINC_SYSTEM_URL))));
		assertThat(result.getCoding(), hasItem(hasProperty("code", equalTo(LOINC_CODE))));
	}


	@Test
	public void shouldReturnNullForCreatingNewOrder() {
		ServiceRequest serviceRequest = new ServiceRequest();
		serviceRequest.setId(SERVICE_REQUEST_UUID);
		
		TestOrder result = translator.toOpenmrsType(serviceRequest);
		
		assertThat(result, nullValue());
	}
	
	@Test
	public void shouldReturnNullForUpdatingExistingOrder() {
		ServiceRequest serviceRequest = new ServiceRequest();
		serviceRequest.setId(SERVICE_REQUEST_UUID);
		TestOrder existingOrder = new TestOrder();
		existingOrder.setUuid(SERVICE_REQUEST_UUID);
		
		TestOrder result = translator.toOpenmrsType(existingOrder, serviceRequest);
		
		assertThat(result, nullValue());
	}


}
