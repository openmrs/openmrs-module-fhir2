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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.when;

import lombok.AccessLevel;
import lombok.Getter;
import org.hl7.fhir.r4.model.RelatedPerson;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openmrs.module.fhir2.api.FhirRelatedPersonService;
import org.openmrs.module.fhir2.web.servlet.BaseFhirResourceProviderTest;
import org.springframework.mock.web.MockHttpServletResponse;

@RunWith(MockitoJUnitRunner.class)
public class RelatedPersonFhirResourceProviderWebTest extends BaseFhirResourceProviderTest<RelatedPersonFhirResourceProvider, RelatedPerson> {
	
	private static final String RELATED_PERSON_UUID = "8a849d5e-6011-4279-a124-40ada5a687de";
	
	private static final String WRONG_RELATED_PERSON_UUID = "9bf0d1ac-62a8-4440-a5a1-eb1015a7cc65";
	
	@Mock
	private FhirRelatedPersonService relatedPersonService;
	
	@Getter(AccessLevel.PUBLIC)
	private RelatedPersonFhirResourceProvider resourceProvider;
	
	@Before
	@Override
	public void setup() throws Exception {
		resourceProvider = new RelatedPersonFhirResourceProvider();
		resourceProvider.setRelatedPersonService(relatedPersonService);
		super.setup();
	}
	
	@Test
	public void shouldReturnRelatedPersonById() throws Exception {
		RelatedPerson relatedPerson = new RelatedPerson();
		relatedPerson.setId(RELATED_PERSON_UUID);
		when(relatedPersonService.getRelatedPersonById(RELATED_PERSON_UUID)).thenReturn(relatedPerson);
		
		MockHttpServletResponse response = get("/RelatedPerson/" + RELATED_PERSON_UUID).accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), equalTo(FhirMediaTypes.JSON.toString()));
		
		RelatedPerson resource = readResponse(response);
		assertThat(resource.getIdElement().getIdPart(), equalTo(RELATED_PERSON_UUID));
	}
	
	@Test
	public void shouldReturn404IfRelatedPersonNotFound() throws Exception {
		when(relatedPersonService.getRelatedPersonById(WRONG_RELATED_PERSON_UUID)).thenReturn(null);
		
		MockHttpServletResponse response = get("/RelatedPerson/" + WRONG_RELATED_PERSON_UUID).accept(FhirMediaTypes.JSON)
		        .go();
		
		assertThat(response, isNotFound());
	}
}
