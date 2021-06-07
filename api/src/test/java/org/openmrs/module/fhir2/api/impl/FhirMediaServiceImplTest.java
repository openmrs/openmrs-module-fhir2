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

import org.hl7.fhir.r4.model.Media;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openmrs.Obs;
import org.openmrs.module.fhir2.api.dao.FhirMediaDao;
import org.openmrs.module.fhir2.api.search.SearchQueryInclude;
import org.openmrs.module.fhir2.api.translators.MediaTranslator;

import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class FhirMediaServiceImplTest {

	private static  String OBS_UUID = "d085336f-2ddf-40cb-a67f-afd968ab9fa9";

	@Mock
	private  FhirMediaDao dao;

	@Mock
	private SearchQueryInclude<Media> searchQueryInclude;

	@Mock
	private MediaTranslator mediaTranslator;

	private  FhirMediaServiceImpl fhirMediaService;

	@Before
	public void setUp() {
		fhirMediaService = new FhirMediaServiceImpl();
		fhirMediaService.setDao(dao);
		fhirMediaService.setTranslator(mediaTranslator);
		fhirMediaService.setSearchQueryInclude(searchQueryInclude);
	}

	@Test
	public void get_shouldGetComplexObsByUuid(){
		Obs obs = new Obs();
		obs.setUuid(OBS_UUID);
		Media media = new Media();
		System.out.println(dao.get(OBS_UUID));
		lenient().when(dao.get(OBS_UUID)).thenReturn(obs);
		lenient().when(mediaTranslator.toFhirResource(obs)).thenReturn(media);
	}
}
