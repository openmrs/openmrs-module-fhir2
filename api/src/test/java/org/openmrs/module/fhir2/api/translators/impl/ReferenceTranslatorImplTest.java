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
import org.mockito.junit.MockitoJUnitRunner;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.model.FhirReference;

@RunWith(MockitoJUnitRunner.class)
public class ReferenceTranslatorImplTest {
	
	private static final String REFERENCE = "0dab1109-829c-4695-b738-eeae2c9a51b6";
	
	private static final String TYPE = FhirConstants.PRACTITIONER;
	
	private ReferenceTranslatorImpl referenceTranslator;
	
	@Before
	public void setup() {
		referenceTranslator = new ReferenceTranslatorImpl();
	}
	
	@Test
	public void toFhirResource_shouldConvertFhirReferenceToReference() {
		FhirReference openmrsReference = new FhirReference();
		openmrsReference.setReference(REFERENCE);
		openmrsReference.setType(TYPE);
		
		Reference result = referenceTranslator.toFhirResource(openmrsReference);
		
		assertThat(result, notNullValue());
		assertThat(result.getType(), equalTo(TYPE));
		assertThat(result.getReference(), equalTo(REFERENCE));
	}
	
	@Test
	public void toFhirResource_shouldReturnNullIfFhirReferenceNull() {
		Reference result = referenceTranslator.toFhirResource(null);
		
		assertThat(result, nullValue());
	}
	
	@Test
	public void toOpenmrsType_shouldConvertReferenceToFhirReference() {
		Reference reference = new Reference().setReference(REFERENCE).setType(TYPE);
		
		FhirReference result = referenceTranslator.toOpenmrsType(reference);
		
		assertThat(result, notNullValue());
		assertThat(result.getReference(), equalTo(REFERENCE));
		assertThat(result.getType(), equalTo(TYPE));
	}
	
	@Test
	public void toOpenmrsType_shouldReturnNullIfReferenceNull() {
		FhirReference result = referenceTranslator.toOpenmrsType(null);
		
		assertThat(result, nullValue());
	}
	
	@Test
	public void toOpenmrsType_shouldUpdateFhirReferenceWithReference() {
		Reference reference = new Reference().setReference(REFERENCE).setType(TYPE);
		
		FhirReference openmrsReference = new FhirReference();
		openmrsReference.setReference("some-uuid");
		openmrsReference.setType(FhirConstants.SERVICE_REQUEST);
		
		FhirReference result = referenceTranslator.toOpenmrsType(openmrsReference, reference);
		
		assertThat(result, notNullValue());
		assertThat(result.getReference(), equalTo(REFERENCE));
		assertThat(result.getType(), equalTo(TYPE));
	}
	
	@Test
	public void toOpenmrsType_shouldCreateFhirReferenceIfNull() {
		Reference reference = new Reference().setReference(REFERENCE).setType(TYPE);
		
		FhirReference result = referenceTranslator.toOpenmrsType(null, reference);
		
		assertThat(result, notNullValue());
		assertThat(result.getReference(), equalTo(REFERENCE));
		assertThat(result.getType(), equalTo(TYPE));
	}
}
