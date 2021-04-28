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

import org.hl7.fhir.r4.model.Media;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.module.fhir2.api.translators.MediaContentTranslator;
import org.openmrs.module.fhir2.api.translators.MediaStatusTranslator;

import java.util.Date;

import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.util.MatcherAssertionErrors.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class MediaTranslatorImplTest {
	
	private static String OBS_UUID = "96c695c9-148b-4788-ac8e-ff2594381ebf";
	
	private static String MEDIA_STATUS = "COMPLETED";
	
	@Mock
	MediaContentTranslator mediaContentTranslator;
	
	@Mock
	MediaStatusTranslator mediaStatusTranslator;
	
	@Mock
	MediaTranslatorImpl mediaTranslator;
	
	@Before
	public void setUp() {
		mediaContentTranslator = new MediaContentTranslatorImpl();
		mediaTranslator.setMediaContentTranslator(mediaContentTranslator);
		mediaTranslator.setMediaStatusTranslator(mediaStatusTranslator);
	}
	
	@Test
	public void toFhir_shouldConvertObsToMedia() {
		Obs obs = new Obs();
		obs.setUuid(OBS_UUID);
		obs.setObsId(1);
		obs.setComment("Hand X-ray");
		obs.setValueTime(new Date());
		obs.setPerson(new Patient());
		
		Media result = mediaTranslator.toFhirResource(obs);
		assertThat(result, notNullValue());
	}
}
