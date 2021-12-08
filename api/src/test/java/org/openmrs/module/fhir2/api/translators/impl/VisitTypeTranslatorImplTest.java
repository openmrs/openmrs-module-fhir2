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
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openmrs.VisitType;
import org.openmrs.api.VisitService;
import org.openmrs.module.fhir2.FhirConstants;

@RunWith(MockitoJUnitRunner.class)
public class VisitTypeTranslatorImplTest {
	
	private static final String VISIT_TYPE_UUID = "9a3e940d-24d4-4f39-b333-f1fe1aea8ed9";
	
	private static final String VISIT_TYPE_NAME = "Visit Type";
	
	@Mock
	private VisitService visitService;
	
	private VisitTypeTranslatorImpl visitTypeTranslator;
	
	@Before
	public void setup() {
		visitTypeTranslator = new VisitTypeTranslatorImpl();
		visitTypeTranslator.setVisitService(visitService);
	}
	
	@Test
	public void toFhirResource_shouldMapVisitTypeToCodeableConcept() {
		VisitType visitType = new VisitType();
		visitType.setUuid(VISIT_TYPE_UUID);
		visitType.setName(VISIT_TYPE_NAME);
		
		List<CodeableConcept> visitTypes = visitTypeTranslator.toFhirResource(visitType);
		
		assertThat(visitTypes, notNullValue());
		assertThat(visitTypes, not(empty()));
		
		Coding fhirVisitType = visitTypes.get(0).getCodingFirstRep();
		
		assertThat(fhirVisitType, notNullValue());
		assertThat(fhirVisitType.getSystem(), equalTo(FhirConstants.VISIT_TYPE_SYSTEM_URI));
		assertThat(fhirVisitType.getCode(), equalTo(VISIT_TYPE_UUID));
		assertThat(fhirVisitType.getDisplay(), equalTo(VISIT_TYPE_NAME));
	}
	
	@Test
	public void toFhirResource_shouldReturnNullIfVisitTypeIsNull() {
		List<CodeableConcept> visitTypes = visitTypeTranslator.toFhirResource(null);
		
		assertThat(visitTypes, nullValue());
	}
	
	@Test
	public void toOpenmrsObject_shouldMapCodeableConceptsToVisitType() {
		VisitType visitType = new VisitType();
		visitType.setUuid(VISIT_TYPE_UUID);
		visitType.setName(VISIT_TYPE_NAME);
		
		CodeableConcept codeableConcept = new CodeableConcept();
		codeableConcept.addCoding().setSystem(FhirConstants.VISIT_TYPE_SYSTEM_URI).setCode(VISIT_TYPE_UUID)
		        .setDisplay(VISIT_TYPE_NAME);
		when(visitService.getVisitTypeByUuid(VISIT_TYPE_UUID)).thenReturn(visitType);
		
		VisitType result = visitTypeTranslator.toOpenmrsType(Collections.singletonList(codeableConcept));
		
		assertThat(result, notNullValue());
		assertThat(result.getUuid(), equalTo(VISIT_TYPE_UUID));
		assertThat(result.getName(), equalTo(VISIT_TYPE_NAME));
	}
	
	@Test
	public void toOpenmrsObject_shouldNotRequireVisitTypeName() {
		VisitType visitType = new VisitType();
		visitType.setUuid(VISIT_TYPE_UUID);
		visitType.setName(VISIT_TYPE_NAME);
		
		CodeableConcept codeableConcept = new CodeableConcept();
		codeableConcept.addCoding().setSystem(FhirConstants.VISIT_TYPE_SYSTEM_URI).setCode(VISIT_TYPE_UUID);
		when(visitService.getVisitTypeByUuid(VISIT_TYPE_UUID)).thenReturn(visitType);
		
		VisitType result = visitTypeTranslator.toOpenmrsType(Collections.singletonList(codeableConcept));
		
		assertThat(result, notNullValue());
		assertThat(result.getUuid(), equalTo(VISIT_TYPE_UUID));
		assertThat(result.getName(), equalTo(VISIT_TYPE_NAME));
	}
	
	@Test
	public void toOpenmrsObject_shouldReturnNullWhenVisitTypeIsNull() {
		VisitType result = visitTypeTranslator.toOpenmrsType(null);
		
		assertThat(result, nullValue());
	}
	
	@Test
	public void toOpenmrsObject_shouldReturnNullWhenVisitTypeIsEmpty() {
		VisitType result = visitTypeTranslator.toOpenmrsType(new ArrayList<>());
		
		assertThat(result, nullValue());
	}
	
	@Test
	public void toOpenmrsObject_shouldReturnNullWhenVisitTypeNotFound() {
		CodeableConcept codeableConcept = new CodeableConcept();
		codeableConcept.addCoding().setSystem(FhirConstants.VISIT_TYPE_SYSTEM_URI).setCode(VISIT_TYPE_UUID)
		        .setDisplay(VISIT_TYPE_NAME);
		when(visitService.getVisitTypeByUuid(VISIT_TYPE_UUID)).thenReturn(null);
		
		VisitType result = visitTypeTranslator.toOpenmrsType(Collections.singletonList(codeableConcept));
		
		assertThat(result, nullValue());
	}
	
	@Test
	public void toOpenmrsObject_shouldReturnNullWhenVisitTypeSystemIsMissing() {
		CodeableConcept codeableConcept = new CodeableConcept();
		codeableConcept.addCoding().setCode(VISIT_TYPE_UUID).setDisplay(VISIT_TYPE_NAME);
		
		VisitType result = visitTypeTranslator.toOpenmrsType(Collections.singletonList(codeableConcept));
		
		assertThat(result, nullValue());
	}
	
	@Test
	public void toOpenmrsObject_shouldReturnNullWhenVisitTypeSystemIsIncorrect() {
		CodeableConcept codeableConcept = new CodeableConcept();
		codeableConcept.addCoding().setSystem("http://mygreatesystem.com").setCode(VISIT_TYPE_UUID)
		        .setDisplay(VISIT_TYPE_NAME);
		
		VisitType result = visitTypeTranslator.toOpenmrsType(Collections.singletonList(codeableConcept));
		
		assertThat(result, nullValue());
	}
	
	@Test
	public void toOpenmrsObject_shouldReturnNullWhenVisitTypeCodeIsMissing() {
		CodeableConcept codeableConcept = new CodeableConcept();
		codeableConcept.addCoding().setSystem(FhirConstants.VISIT_TYPE_SYSTEM_URI).setDisplay(VISIT_TYPE_NAME);
		
		VisitType result = visitTypeTranslator.toOpenmrsType(Collections.singletonList(codeableConcept));
		
		assertThat(result, nullValue());
	}
}
