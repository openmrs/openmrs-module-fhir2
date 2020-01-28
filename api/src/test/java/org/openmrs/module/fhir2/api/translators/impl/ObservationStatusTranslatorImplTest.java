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
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

import org.hl7.fhir.r4.model.Observation;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.Obs;

public class ObservationStatusTranslatorImplTest {
	
	private ObservationStatusTranslatorImpl observationStatusTranslator;
	
	@Before
	public void setup() {
		observationStatusTranslator = new ObservationStatusTranslatorImpl();
	}
	
	@Test
	public void shouldMapObservationStatusToUnknown() {
		Obs obs = new Obs();
		
		Observation.ObservationStatus result = observationStatusTranslator.toFhirResource(obs);
		
		assertThat(result, is(Observation.ObservationStatus.UNKNOWN));
	}
	
	@Test
	public void shouldMapFhirRepresentationToNull() {
		Obs obs = new Obs();
		Observation.ObservationStatus observationStatus = Observation.ObservationStatus.FINAL;
		
		Obs result = observationStatusTranslator.toOpenmrsType(obs, observationStatus);
		
		assertThat(result, notNullValue());
	}
}
