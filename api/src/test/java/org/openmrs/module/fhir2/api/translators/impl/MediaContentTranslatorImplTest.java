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
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertNotNull;

import java.util.Date;

import org.hl7.fhir.r4.model.Attachment;
import org.hl7.fhir.r4.model.Base64BinaryType;
import org.hl7.fhir.r4.model.Media;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openmrs.Obs;
import org.openmrs.module.fhir2.api.translators.MediaContentTranslator;
import org.springframework.util.Base64Utils;

@RunWith(MockitoJUnitRunner.class)
public class MediaContentTranslatorImplTest {
	
	@Mock
	private MediaContentTranslator mediaContentTranslator;
	
	@Before
	public void setUp() {
		mediaContentTranslator = new MediaContentTranslatorImpl();
	}
	
	@Test
	public void shouldTranslateObsToMediaContent() {
		Obs obs = new Obs();
		obs.setObsId(2);
		obs.setValueComplex("");
		obs.setValueText("image/gif");
		obs.setComment("CT Scan");
		obs.setDateCreated(new Date());
		
		Media mediaContent = mediaContentTranslator.toFhirResource(obs);
		assertThat(mediaContent, notNullValue());
	}
	
	@Test
	public void toOpenmrsType_shouldConvertMediaContentToOpenmrsObs() {
		Media mediaContent = new Media();
		mediaContent.setContent(new Attachment()
		        .setDataElement(new Base64BinaryType().setValue(Base64Utils.decode("VGVzdCBFbmNvZGVyCgo".getBytes()))));
		mediaContent.setContent(new Attachment().setContentType("image/gif"));
		mediaContent.setContent(new Attachment().setCreation(new Date()));
		mediaContent.setContent(new Attachment().setTitle("Brain CT-Scan "));
		Obs obsContent = mediaContentTranslator.toOpenmrsType(mediaContent);
		
		assertNotNull(obsContent);
	}
}
