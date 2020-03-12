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

import java.util.Date;

import org.hl7.fhir.r4.model.MedicationRequest;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.openmrs.DrugOrder;

@RunWith(MockitoJUnitRunner.class)
public class MedicationRequestStatusTranslatorImplTest {
	
	private static final String DRUG_ORDER_UUID = "44fdc8ad-fe4d-499b-93a8-8a991c1d477e";
	
	private MedicationRequestStatusTranslatorImpl statusTranslator;
	
	private DrugOrder drugOrder;
	
	@Before
	public void setup() {
		statusTranslator = new MedicationRequestStatusTranslatorImpl();
		
		drugOrder = new DrugOrder();
		drugOrder.setUuid(DRUG_ORDER_UUID);
		drugOrder.setDateActivated(new Date());
	}
	
	@Test
	public void toFirResource_shouldTranslateToActiveStatus() {
		MedicationRequest.MedicationRequestStatus status = statusTranslator.toFhirResource(drugOrder);
		assertThat(status, notNullValue());
		assertThat(status, equalTo(MedicationRequest.MedicationRequestStatus.ACTIVE));
	}
	
	@Test
	public void toFhirResource_shouldTranslateToNull() {
		drugOrder.setVoided(true);
		MedicationRequest.MedicationRequestStatus status = statusTranslator.toFhirResource(drugOrder);
		assertThat(status, notNullValue());
		assertThat(status, equalTo(MedicationRequest.MedicationRequestStatus.NULL));
	}
}
