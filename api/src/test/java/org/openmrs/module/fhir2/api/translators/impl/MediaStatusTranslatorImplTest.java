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

import org.hl7.fhir.r4.model.Media;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.openmrs.Obs;

@RunWith(MockitoJUnitRunner.class)
public class MediaStatusTranslatorImplTest {
	
	private MediaStatusTranslatorImpl mediaStatusTranslatorImpl;
	
	@Before
	public void setUp() {
		mediaStatusTranslatorImpl = new MediaStatusTranslatorImpl();
	}
	
	@Test
	public void shouldMapMediaStatusToObsStatus() {
		Obs obs = new Obs();
		
		Media.MediaStatus status = mediaStatusTranslatorImpl.toFhirResource(obs);
		
		assertThat(status, is(Media.MediaStatus.NULL));
	}
	
}
