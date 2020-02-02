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

import org.hl7.fhir.r4.model.Reference;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openmrs.Obs;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.api.dao.FhirObservationDao;

@RunWith(MockitoJUnitRunner.class)
public class ObservationReferenceTranslatorImplTest {
	
	private static final String UUID = "94d336e5-ca34-48d1-be15-5b6cb7c92c5e";
	
	@Mock
	private FhirObservationDao dao;
	
	private ObservationReferenceTranslatorImpl observationReferenceTranslator;
	
	@Before
	public void setup() {
		observationReferenceTranslator = new ObservationReferenceTranslatorImpl();
		observationReferenceTranslator.setObservationDao(dao);
	}
	
	@Test
	public void toFhirResource_shouldConvertObsToReference() {
		Obs obs = new Obs();
		obs.setUuid(UUID);
		
		Reference result = observationReferenceTranslator.toFhirResource(obs);
		
		assertThat(result, notNullValue());
		assertThat(result.getType(), equalTo(FhirConstants.OBSERVATION));
		assertThat(observationReferenceTranslator.getReferenceId(result), equalTo(UUID));
	}
	
	@Test
	public void toFhirResource_shouldReturnNullIfObservationNull() {
		Reference result = observationReferenceTranslator.toFhirResource(null);
		
		assertThat(result, nullValue());
	}
}
