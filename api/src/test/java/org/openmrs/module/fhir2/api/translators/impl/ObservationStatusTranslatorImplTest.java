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

import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.hl7.fhir.r4.model.Observation;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.openmrs.Obs;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.notNullValue;

@RunWith(MockitoJUnitRunner.class)
public class ObservationStatusTranslatorImplTest {
	
	private static final Obs.Status OBS_STATUS = Obs.Status.FINAL;
	
	private ObservationStatusTranslatorImpl observationStatusTranslator;
	
	private Obs obs;
	
	@Before
	public void setUp() {
		observationStatusTranslator = new ObservationStatusTranslatorImpl();
		obs = new Obs();
		obs.setStatus(OBS_STATUS);
	}
	
	@Test
	public void toFhirResource_shouldTranslateObsStatusToFhirObservationStatus() {
		Observation.ObservationStatus status = observationStatusTranslator.toFhirResource(obs);
		MatcherAssert.assertThat(status, Matchers.notNullValue());
		MatcherAssert.assertThat(status, Matchers.is(Observation.ObservationStatus.valueOf(OBS_STATUS.toString())));
	}
	
	@Test
	public void toOpenmrsType_shouldTranslatePreliminaryFhirObservationStatusToObsStatus() {
		Observation.ObservationStatus status = Observation.ObservationStatus.PRELIMINARY;
		obs.setStatus(null);
		observationStatusTranslator.toOpenmrsType(obs, status);
		MatcherAssert.assertThat(obs.getStatus(), Matchers.notNullValue());
		MatcherAssert.assertThat(obs.getStatus(), Matchers.is(Obs.Status.PRELIMINARY));
	}
	
	@Test
	public void toOpenmrsType_shouldTranslateFinalFhirObservationStatusToObsStatus() {
		Observation.ObservationStatus status = Observation.ObservationStatus.FINAL;
		obs.setStatus(null);
		observationStatusTranslator.toOpenmrsType(obs, status);
		MatcherAssert.assertThat(obs.getStatus(), Matchers.notNullValue());
		MatcherAssert.assertThat(obs.getStatus(), Matchers.is(Obs.Status.FINAL));
	}
	
	@Test
	public void toOpenmrsType_shouldTranslateAmendedFhirObservationStatusToObsStatus() {
		Observation.ObservationStatus status = Observation.ObservationStatus.AMENDED;
		obs.setStatus(null);
		observationStatusTranslator.toOpenmrsType(obs, status);
		MatcherAssert.assertThat(obs.getStatus(), Matchers.notNullValue());
		MatcherAssert.assertThat(obs.getStatus(), Matchers.is(Obs.Status.AMENDED));
	}
	
	@Test
	public void toOpenmrsType_shouldReturnObsWithNullStatusWhenCalledWithUnsupportedRegisteredStatus() {
		Observation.ObservationStatus status = Observation.ObservationStatus.REGISTERED;
		obs.setStatus(null);
		observationStatusTranslator.toOpenmrsType(obs, status);
		MatcherAssert.assertThat(obs.getStatus(), Matchers.nullValue());
	}
	
	@Test
	public void toOpenmrsType_shouldReturnObsWithNullStatusWhenCalledWithUnsupportedCorrectedStatus() {
		Observation.ObservationStatus status = Observation.ObservationStatus.CORRECTED;
		obs.setStatus(null);
		observationStatusTranslator.toOpenmrsType(obs, status);
		MatcherAssert.assertThat(obs.getStatus(), Matchers.nullValue());
	}
	
	@Test
	public void toOpenmrsType_shouldReturnObsWithNullStatusWhenCalledWithUnsupportedCancelledStatus() {
		Observation.ObservationStatus status = Observation.ObservationStatus.CANCELLED;
		obs.setStatus(null);
		observationStatusTranslator.toOpenmrsType(obs, status);
		MatcherAssert.assertThat(obs.getStatus(), Matchers.nullValue());
	}
	
	@Test
	public void toOpenmrsType_shouldReturnObsWithNullStatusWhenCalledWithUnsupportedEnteredInErrorStatus() {
		Observation.ObservationStatus status = Observation.ObservationStatus.ENTEREDINERROR;
		obs.setStatus(null);
		observationStatusTranslator.toOpenmrsType(obs, status);
		MatcherAssert.assertThat(obs.getStatus(), Matchers.nullValue());
	}
	
	@Test
	public void toOpenmrsType_shouldReturnObsWithNullStatusWhenCalledWithUnsupportedUnknownStatus() {
		Observation.ObservationStatus status = Observation.ObservationStatus.UNKNOWN;
		obs.setStatus(null);
		observationStatusTranslator.toOpenmrsType(obs, status);
		MatcherAssert.assertThat(obs.getStatus(), Matchers.nullValue());
	}
	
	@Test
	public void toOpenmrsType_shouldReturnObsWithNullStatusWhenCalledWithUnsupportedNullStatus() {
		Observation.ObservationStatus status = Observation.ObservationStatus.NULL;
		obs.setStatus(null);
		observationStatusTranslator.toOpenmrsType(obs, status);
		MatcherAssert.assertThat(obs.getStatus(), Matchers.nullValue());
	}
	
	@Test
	public void toOpenmrsType_shouldMapFhirRepresentationToNull() {
		Obs obs = new Obs();
		Observation.ObservationStatus observationStatus = Observation.ObservationStatus.FINAL;
		Obs result = observationStatusTranslator.toOpenmrsType(obs, observationStatus);
		assertThat(result, notNullValue());
	}
}
