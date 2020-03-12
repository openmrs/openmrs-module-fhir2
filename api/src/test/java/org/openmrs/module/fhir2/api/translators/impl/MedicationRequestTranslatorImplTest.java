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
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.when;

import org.hl7.fhir.r4.model.MedicationRequest;
import org.hl7.fhir.r4.model.Reference;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openmrs.Drug;
import org.openmrs.DrugOrder;
import org.openmrs.Provider;
import org.openmrs.User;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.api.translators.MedicationReferenceTranslator;
import org.openmrs.module.fhir2.api.translators.MedicationRequestPriorityTranslator;
import org.openmrs.module.fhir2.api.translators.MedicationRequestStatusTranslator;
import org.openmrs.module.fhir2.api.translators.PractitionerReferenceTranslator;

@RunWith(MockitoJUnitRunner.class)
public class MedicationRequestTranslatorImplTest {
	
	private static final String DRUG_ORDER_UUID = "44fdc8ad-fe4d-499b-93a8-8a991c1d477e";
	
	private static final String DRUG_UUID = "99fdc8ad-fe4d-499b-93a8-8a991c1d477g";
	
	private static final String PRACTITIONER_UUID = "88fdc8ad-fe4d-499b-93a8-8a991c1d477d";
	
	@Mock
	private PractitionerReferenceTranslator<Provider> providerPractitionerReferenceTranslator;
	
	@Mock
	private MedicationRequestStatusTranslator medicationRequestStatusTranslator;
	
	@Mock
	private MedicationRequestPriorityTranslator medicationRequestPriorityTranslator;
	
	@Mock
	private MedicationReferenceTranslator medicationReferenceTranslator;
	
	private MedicationRequestTranslatorImpl medicationRequestTranslator;
	
	private MedicationRequest medicationRequest;
	
	private DrugOrder drugOrder;
	
	@Before
	public void setup() {
		medicationRequestTranslator = new MedicationRequestTranslatorImpl();
		medicationRequestTranslator.setStatusTranslator(medicationRequestStatusTranslator);
		medicationRequestTranslator.setPractitionerReferenceTranslator(providerPractitionerReferenceTranslator);
		medicationRequestTranslator.setMedicationRequestPriorityTranslator(medicationRequestPriorityTranslator);
		medicationRequestTranslator.setMedicationReferenceTranslator(medicationReferenceTranslator);
		
		drugOrder = new DrugOrder();
		drugOrder.setUuid(DRUG_ORDER_UUID);
		
		medicationRequest = new MedicationRequest();
		medicationRequest.setId(DRUG_ORDER_UUID);
	}
	
	@Test
	public void toOpenMrsType_shouldTranslateToOpenType() {
		DrugOrder result = medicationRequestTranslator.toOpenmrsType(new DrugOrder(), medicationRequest);
		assertThat(result, notNullValue());
		assertThat(result.getUuid(), notNullValue());
		assertThat(result.getUuid(), equalTo(DRUG_ORDER_UUID));
	}
	
	@Test
	public void toFhirResource_shouldTranslateToFhirResource() {
		MedicationRequest result = medicationRequestTranslator.toFhirResource(drugOrder);
		assertThat(result, notNullValue());
		assertThat(result.getId(), notNullValue());
		assertThat(result.getId(), equalTo(DRUG_ORDER_UUID));
	}
	
	@Test
	public void toOpenMrsType_shouldReturnExistingDrugOrderIfMedicationIsNull() {
		DrugOrder result = medicationRequestTranslator.toOpenmrsType(new DrugOrder(), null);
		assertThat(result, notNullValue());
		assertThat(result, instanceOf(DrugOrder.class));
	}
	
	@Test
	public void toFhirResource_shouldReturnNewInstanceOfMedicationRequest() {
		MedicationRequest result = medicationRequestTranslator.toFhirResource(null);
		assertThat(result, notNullValue());
		assertThat(result, instanceOf(MedicationRequest.class));
		assertThat(result.getId(), nullValue());
	}
	
	@Test
	public void toFhirResource_shouldConvertStatusToFhirType() {
		drugOrder.setVoided(true);
		drugOrder.setVoidedBy(new User());
		when(medicationRequestStatusTranslator.toFhirResource(drugOrder))
		        .thenReturn(MedicationRequest.MedicationRequestStatus.CANCELLED);
		MedicationRequest result = medicationRequestTranslator.toFhirResource(drugOrder);
		assertThat(result, notNullValue());
		assertThat(result.getStatus(), notNullValue());
		assertThat(result.getStatus(), equalTo(MedicationRequest.MedicationRequestStatus.CANCELLED));
	}
	
	@Test
	public void toFhirResource_shouldConvertActiveStatusToFhirType() {
		when(medicationRequestStatusTranslator.toFhirResource(drugOrder))
		        .thenReturn(MedicationRequest.MedicationRequestStatus.ACTIVE);
		MedicationRequest result = medicationRequestTranslator.toFhirResource(drugOrder);
		assertThat(result, notNullValue());
		assertThat(result.getStatus(), notNullValue());
		assertThat(result.getStatus(), equalTo(MedicationRequest.MedicationRequestStatus.ACTIVE));
	}
	
	@Test
	public void toOpenMrsType_shouldTranslateRequesterToOpenMrsType() {
		Provider provider = new Provider();
		provider.setUuid(PRACTITIONER_UUID);
		Reference requesterReference = new Reference();
		requesterReference.setReference(FhirConstants.PRACTITIONER + "/" + PRACTITIONER_UUID);
		medicationRequest.setRequester(requesterReference);
		when(providerPractitionerReferenceTranslator.toOpenmrsType(requesterReference)).thenReturn(provider);
		
		DrugOrder result = medicationRequestTranslator.toOpenmrsType(new DrugOrder(), medicationRequest);
		assertThat(result, notNullValue());
		assertThat(result.getOrderer(), notNullValue());
		assertThat(result.getOrderer().getUuid(), equalTo(PRACTITIONER_UUID));
	}
	
	@Test
	public void toFhirResource_shouldTranslateOrdererToFhirType() {
		Provider provider = new Provider();
		provider.setUuid(PRACTITIONER_UUID);
		drugOrder.setOrderer(provider);
		
		Reference requesterReference = new Reference();
		requesterReference.setReference(FhirConstants.PRACTITIONER + "/" + PRACTITIONER_UUID);
		
		when(providerPractitionerReferenceTranslator.toFhirResource(provider)).thenReturn(requesterReference);
		
		MedicationRequest result = medicationRequestTranslator.toFhirResource(drugOrder);
		assertThat(result, notNullValue());
		assertThat(result.getRequester(), notNullValue());
		assertThat(result.getRequester().getReference(), equalTo(requesterReference.getReference()));
	}
	
	@Test
	public void toOpenMrsType_shouldTranslatePriorityToOpenMrsType() {
		medicationRequest.setPriority(MedicationRequest.MedicationRequestPriority.ROUTINE);
		when(medicationRequestPriorityTranslator.toOpenmrsType(MedicationRequest.MedicationRequestPriority.ROUTINE))
		        .thenReturn(DrugOrder.Urgency.ROUTINE);
		DrugOrder result = medicationRequestTranslator.toOpenmrsType(new DrugOrder(), medicationRequest);
		
		assertThat(result, notNullValue());
		assertThat(result.getUrgency(), notNullValue());
		assertThat(result.getUrgency(), equalTo(DrugOrder.Urgency.ROUTINE));
	}
	
	@Test
	public void toFhirResource_shouldTranslateUrgencyToFhirType() {
		drugOrder.setUrgency(DrugOrder.Urgency.ON_SCHEDULED_DATE);
		when(medicationRequestPriorityTranslator.toFhirResource(DrugOrder.Urgency.ON_SCHEDULED_DATE))
		        .thenReturn(MedicationRequest.MedicationRequestPriority.URGENT);
		MedicationRequest result = medicationRequestTranslator.toFhirResource(drugOrder);
		
		assertThat(result, notNullValue());
		assertThat(result.getPriority(), notNullValue());
		assertThat(result.getPriority(), equalTo(MedicationRequest.MedicationRequestPriority.URGENT));
		
	}
	
	@Test
	public void toOpenMrsType_shouldTranslateMedicationToOpenMrsDrug() {
		Reference medicationRef = new Reference();
		medicationRef.setReference(FhirConstants.MEDICATION + "/" + DRUG_UUID);
		Drug drug = new Drug();
		drug.setUuid(DRUG_UUID);
		
		medicationRequest.setMedication(medicationRef);
		when(medicationReferenceTranslator.toOpenmrsType(medicationRef)).thenReturn(drug);
		DrugOrder result = medicationRequestTranslator.toOpenmrsType(new DrugOrder(), medicationRequest);
		
		assertThat(result, notNullValue());
		assertThat(result.getDrug(), notNullValue());
		assertThat(result.getDrug().getUuid(), equalTo(DRUG_UUID));
	}
	
	@Test
	public void toFhirResource_shouldTranslateDrugToMedicationReference() {
		Drug drug = new Drug();
		drug.setUuid(DRUG_UUID);
		drugOrder.setDrug(drug);
		Reference medicationRef = new Reference();
		medicationRef.setReference(FhirConstants.MEDICATION + "/" + DRUG_UUID);
		
		when(medicationReferenceTranslator.toFhirResource(drug)).thenReturn(medicationRef);
		MedicationRequest result = medicationRequestTranslator.toFhirResource(drugOrder);
		
		assertThat(result, notNullValue());
		assertThat(result.getMedication(), notNullValue());
		assertThat(result.getMedication(), equalTo(medicationRef));
	}
	
}
