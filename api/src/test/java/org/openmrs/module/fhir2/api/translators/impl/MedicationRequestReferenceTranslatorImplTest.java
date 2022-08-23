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
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.when;

import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Reference;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openmrs.DrugOrder;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.api.dao.FhirMedicationRequestDao;

@RunWith(MockitoJUnitRunner.class)
public class MedicationRequestReferenceTranslatorImplTest {
	
	private static final String DRUG_ORDER_UUID = "12345-abcde-12345";
	
	@Mock
	private FhirMedicationRequestDao dao;
	
	private MedicationRequestReferenceTranslatorImpl translator;
	
	@Before
	public void setup() {
		translator = new MedicationRequestReferenceTranslatorImpl();
		translator.setMedicationRequestDao(dao);
	}
	
	@Test
	public void toFhirResource_shouldConvertDrugOrderToReference() {
		DrugOrder drugOrder = new DrugOrder();
		drugOrder.setUuid(DRUG_ORDER_UUID);
		Reference result = translator.toFhirResource(drugOrder);
		assertThat(result, notNullValue());
		assertThat(result.getType(), equalTo(FhirConstants.MEDICATION_REQUEST));
		assertThat(translator.getReferenceId(result).orElse(null), equalTo(DRUG_ORDER_UUID));
	}
	
	@Test
	public void toFhirResource_shouldReturnNullIfDrugOrderNull() {
		Reference result = translator.toFhirResource(null);
		assertThat(result, nullValue());
	}
	
	@Test
	public void toOpenmrsType_shouldConvertReferenceToDrugOrder() {
		Reference ref = new Reference().setReference(FhirConstants.MEDICATION_REQUEST + "/" + DRUG_ORDER_UUID)
		        .setType(FhirConstants.MEDICATION_REQUEST).setIdentifier(new Identifier().setValue(DRUG_ORDER_UUID));
		DrugOrder drugOrder = new DrugOrder();
		drugOrder.setUuid(DRUG_ORDER_UUID);
		when(dao.get(DRUG_ORDER_UUID)).thenReturn(drugOrder);
		
		DrugOrder result = translator.toOpenmrsType(ref);
		assertThat(result, notNullValue());
		assertThat(result.getUuid(), equalTo(DRUG_ORDER_UUID));
	}
	
	@Test
	public void toOpenmrsType_shouldReturnNullIfReferenceNull() {
		DrugOrder result = translator.toOpenmrsType(null);
		assertThat(result, nullValue());
	}
	
	@Test
	public void toOpenmrsType_shouldReturnNullIfReferenceHasNoIdentifier() {
		Reference ref = new Reference().setReference(FhirConstants.MEDICATION_REQUEST + "/" + DRUG_ORDER_UUID)
		        .setType(FhirConstants.MEDICATION_REQUEST);
		DrugOrder result = translator.toOpenmrsType(ref);
		assertThat(result, nullValue());
	}
	
	@Test
	public void toOpenmrsType_shouldReturnNullIfDrugOrderIdentifierHasNoValue() {
		Reference ref = new Reference().setReference(FhirConstants.MEDICATION_REQUEST + "/" + DRUG_ORDER_UUID)
		        .setType(FhirConstants.MEDICATION_REQUEST).setIdentifier(new Identifier());
		
		DrugOrder result = translator.toOpenmrsType(ref);
		assertThat(result, nullValue());
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void toOpenmrsType_shouldThrowExceptionIfReferenceIsntForMedicationRequest() {
		Reference reference = new Reference().setReference("Unknown" + "/" + DRUG_ORDER_UUID).setType("Unknown");
		translator.toOpenmrsType(reference);
	}
}
