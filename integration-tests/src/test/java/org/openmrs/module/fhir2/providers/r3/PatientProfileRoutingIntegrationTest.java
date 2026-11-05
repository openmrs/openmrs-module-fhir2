/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.fhir2.providers.r3;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

import java.util.List;
import java.util.Set;

import lombok.AccessLevel;
import lombok.Getter;
import org.hl7.fhir.dstu3.model.Patient;
import org.hl7.fhir.dstu3.model.Resource;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletResponse;

public class PatientProfileRoutingIntegrationTest extends BaseProfileRoutingR3IntegrationTest<PatientFhirResourceProvider, Patient> {
	
	private static final String PATIENT_PROFILE = openmrsProfile("openmrs-patient");
	
	private static final String PATIENT_UUID = "5946f880-b197-400b-9caa-a3c661d23041"; // patient 7 from the standard test dataset
	
	@Getter(AccessLevel.PUBLIC)
	@Autowired
	private PatientFhirResourceProvider resourceProvider;
	
	@Test
	public void shouldStampPatientProfileOnPatient() throws Exception {
		// when
		MockHttpServletResponse response = get("/Patient/" + PATIENT_UUID).accept(FhirMediaTypes.JSON).go();
		
		// then
		assertThat(response, isOk());
		assertThat(profilesOf(readResponse(response)), contains(PATIENT_PROFILE));
	}
	
	@Test
	public void shouldReturnEveryPatientStampedWhenSearchingByItsOwnProfile() throws Exception {
		// given
		Set<String> expected = idsOf(searchAllPages("/Patient"));
		
		// when
		List<Resource> results = searchAllPages("/Patient?_profile=" + PATIENT_PROFILE);
		
		// then
		assertThat(results, is(not(empty())));
		assertThat(results, everyItem(hasProfiles(contains(PATIENT_PROFILE))));
		assertThat(idsOf(results), equalTo(expected));
	}
}
