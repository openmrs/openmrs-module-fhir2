/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.fhir2.providers;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;

import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import lombok.AccessLevel;
import lombok.Getter;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.Observation;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.openmrs.module.fhir2.api.FhirObservationService;

@RunWith(MockitoJUnitRunner.class)
public class ObservationFhirResourceProviderTest extends BaseFhirResourceProviderTest<ObservationFhirResourceProvider> {

	private static final String OBSERVATION_UUID = "1223h34-34nj3-34nj34-34nj";

	private static final String WRONG_OBSERVATION_UUID = "hj243h34-cb4vsd-34xxx34-ope4jj";

	@Mock
	private FhirObservationService observationService;

	@Getter(AccessLevel.PACKAGE)
	private ObservationFhirResourceProvider resourceProvider;

	private Observation observation;

	@Override
	public void setup() throws Exception {
		observationService = Mockito.mock(FhirObservationService.class);
		resourceProvider = new ObservationFhirResourceProvider();
		resourceProvider.setObservationService(observationService);
		super.setup();
	}

	@Before
	public void initObservation() {
		observation = new Observation();
		observation.setId(OBSERVATION_UUID);
		observation.setStatus(Observation.ObservationStatus.UNKNOWN);
	}

	@Test
	public void getResourceType_shouldReturnResourceType() {
		assertThat(resourceProvider.getResourceType(), equalTo(Observation.class));
		assertThat(resourceProvider.getResourceType().getName(), equalTo(Observation.class.getName()));
	}

	@Test
	public void getObservationByUuid_shouldReturnMatchingObservation() {
		when(observationService.getObservationByUuid(OBSERVATION_UUID)).thenReturn(observation);
		IdType id = new IdType();
		id.setId(OBSERVATION_UUID);
		Observation result = resourceProvider.getObservationById(id);
		assertThat(result, notNullValue());
		assertThat(result.getId(), notNullValue());
		assertThat(result.getId(), equalTo(OBSERVATION_UUID));
	}

	@Test(expected = ResourceNotFoundException.class)
	public void getObservationWithWrongUuid_shouldThrowResourceNotFoundException() {
		IdType id = new IdType();
		id.setValue(WRONG_OBSERVATION_UUID);
		Observation result = resourceProvider.getObservationById(id);
		assertThat(result, nullValue());
	}
}
