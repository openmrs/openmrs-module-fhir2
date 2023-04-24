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
import static org.hamcrest.Matchers.notNullValue;
import static org.openmrs.module.fhir2.FhirConstants.OPENMRS_FHIR_EXT_MEDICATION_REQUEST_FULFILLER_STATUS;

import org.hl7.fhir.r4.model.CodeType;
import org.hl7.fhir.r4.model.Extension;
import org.hl7.fhir.r4.model.MedicationRequest;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openmrs.DrugOrder;
import org.openmrs.Encounter;
import org.openmrs.Order;
import org.openmrs.Provider;
import org.openmrs.module.fhir2.api.translators.ConceptTranslator;
import org.openmrs.module.fhir2.api.translators.DosageTranslator;
import org.openmrs.module.fhir2.api.translators.EncounterReferenceTranslator;
import org.openmrs.module.fhir2.api.translators.MedicationReferenceTranslator;
import org.openmrs.module.fhir2.api.translators.MedicationRequestDispenseRequestComponentTranslator;
import org.openmrs.module.fhir2.api.translators.MedicationRequestPriorityTranslator;
import org.openmrs.module.fhir2.api.translators.MedicationRequestStatusTranslator;
import org.openmrs.module.fhir2.api.translators.OrderIdentifierTranslator;
import org.openmrs.module.fhir2.api.translators.PatientReferenceTranslator;
import org.openmrs.module.fhir2.api.translators.PractitionerReferenceTranslator;

@RunWith(MockitoJUnitRunner.class)
public class MedicationRequestTranslatorImpl_2_2Test {
	
	private MedicationRequestTranslatorImpl_2_2 medicationRequestTranslator;
	
	@Mock
	private MedicationRequestStatusTranslator medicationRequestStatusTranslator;
	
	@Mock
	private PractitionerReferenceTranslator<Provider> practitionerReferenceTranslator;
	
	@Mock
	private MedicationRequestPriorityTranslator medicationRequestPriorityTranslator;
	
	@Mock
	private MedicationReferenceTranslator medicationReferenceTranslator;
	
	@Mock
	private EncounterReferenceTranslator<Encounter> encounterReferenceTranslator;
	
	@Mock
	private PatientReferenceTranslator patientReferenceTranslator;
	
	@Mock
	private ConceptTranslator conceptTranslator;
	
	@Mock
	private DosageTranslator dosageTranslator;
	
	@Mock
	private OrderIdentifierTranslator orderIdentifierTranslator;
	
	@Mock
	private MedicationRequestDispenseRequestComponentTranslator medicationRequestDispenseRequestComponentTranslator;
	
	@Before
	public void setup() {
		medicationRequestTranslator = new MedicationRequestTranslatorImpl_2_2();
		medicationRequestTranslator.setStatusTranslator(medicationRequestStatusTranslator);
		medicationRequestTranslator.setPractitionerReferenceTranslator(practitionerReferenceTranslator);
		medicationRequestTranslator.setMedicationRequestPriorityTranslator(medicationRequestPriorityTranslator);
		medicationRequestTranslator.setMedicationReferenceTranslator(medicationReferenceTranslator);
		medicationRequestTranslator.setEncounterReferenceTranslator(encounterReferenceTranslator);
		medicationRequestTranslator.setPatientReferenceTranslator(patientReferenceTranslator);
		medicationRequestTranslator.setConceptTranslator(conceptTranslator);
		medicationRequestTranslator.setDosageTranslator(dosageTranslator);
		medicationRequestTranslator.setOrderIdentifierTranslator(orderIdentifierTranslator);
		medicationRequestTranslator
		        .setMedicationRequestDispenseRequestComponentTranslator(medicationRequestDispenseRequestComponentTranslator);
		
	}
	
	@Test
	public void toFhirResource_shouldTranslateToFulfillerStatusExtenstion() {
		DrugOrder drugOrder = new DrugOrder();
		drugOrder.setFulfillerStatus(Order.FulfillerStatus.COMPLETED);
		MedicationRequest medicationRequest = medicationRequestTranslator.toFhirResource(drugOrder);
		assertThat(medicationRequest, notNullValue());
		assertThat(
		    medicationRequest.getExtensionByUrl(OPENMRS_FHIR_EXT_MEDICATION_REQUEST_FULFILLER_STATUS).getValue().toString(),
		    equalTo("COMPLETED"));
	}
	
	@Test
	public void toOpenmrsType_shouldTranslateFulfillerStatusExtension() {
		MedicationRequest medicationRequest = new MedicationRequest();
		
		Extension extension = new Extension();
		extension.setUrl(OPENMRS_FHIR_EXT_MEDICATION_REQUEST_FULFILLER_STATUS);
		extension.setValue(new CodeType("COMPLETED"));
		
		medicationRequest.addExtension(extension);
		
		DrugOrder drugOrder = medicationRequestTranslator.toOpenmrsType(medicationRequest);
		assertThat(drugOrder, notNullValue());
		assertThat(drugOrder.getFulfillerStatus(), equalTo(Order.FulfillerStatus.COMPLETED));
		
	}
}
