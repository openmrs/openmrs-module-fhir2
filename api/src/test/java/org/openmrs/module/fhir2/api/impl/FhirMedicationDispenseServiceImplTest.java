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

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertThrows;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import ca.uhn.fhir.rest.api.server.IBundleProvider;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import org.hl7.fhir.r4.model.MedicationDispense;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.openmrs.module.fhir2.api.search.param.MedicationDispenseSearchParams;

@RunWith(MockitoJUnitRunner.class)
public class FhirMedicationDispenseServiceImplTest {
	
	private String MEDICATION_DISPENSE_UUID = UUID.randomUUID().toString();
	
	private FhirMedicationDispenseServiceImpl dispenseService;
	
	@Before
	public void setup() {
		dispenseService = new FhirMedicationDispenseServiceImpl();
	}
	
	@Test
	public void get_shouldThrowExceptionForSingle() {
		assertThrows(ResourceNotFoundException.class, () -> dispenseService.get(MEDICATION_DISPENSE_UUID));
	}
	
	@Test
	public void get_shouldThrowExceptionForMultiple() {
		List<String> uuids = Collections.singletonList(MEDICATION_DISPENSE_UUID);
		assertThrows(ResourceNotFoundException.class, () -> dispenseService.get(uuids));
	}
	
	@Test
	public void create_shouldThrowException() {
		MedicationDispense dispense = new MedicationDispense();
		dispense.setId(MEDICATION_DISPENSE_UUID);
		assertThrows(UnsupportedOperationException.class, () -> dispenseService.create(dispense));
	}
	
	@Test
	public void update_shouldThrowException() {
		MedicationDispense dispense = new MedicationDispense();
		dispense.setId(MEDICATION_DISPENSE_UUID);
		assertThrows(UnsupportedOperationException.class, () -> dispenseService.update(MEDICATION_DISPENSE_UUID, dispense));
	}
	
	@Test
	public void delete_shouldThrowException() {
		MedicationDispense dispense = new MedicationDispense();
		dispense.setId(MEDICATION_DISPENSE_UUID);
		assertThrows(UnsupportedOperationException.class, () -> dispenseService.delete(MEDICATION_DISPENSE_UUID));
	}
	
	@Test
	public void search_shouldReturnEmptyBundle() {
		IBundleProvider bundle = dispenseService.searchMedicationDispenses(new MedicationDispenseSearchParams());
		assertThat(bundle.size(), equalTo(0));
	}
}
