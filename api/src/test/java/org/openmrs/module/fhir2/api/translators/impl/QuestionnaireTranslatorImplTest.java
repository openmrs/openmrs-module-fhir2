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
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.when;

import java.util.Date;

import org.hl7.fhir.r4.model.Questionnaire;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openmrs.*;
import org.openmrs.api.FormService;
import org.openmrs.customdatatype.datatype.LongFreeTextDatatype;
import org.openmrs.module.fhir2.FhirConstants;

@RunWith(MockitoJUnitRunner.class)
public class QuestionnaireTranslatorImplTest {
	
	private static final String USER_UUID = "68b1e787-e68d-424e-8aac-c3387a0ab7b5";
	
	private static final String FORM_UUID = "1223etbs-0983-4227-2324-87ab68b1e787";
	
	private static final String FORM_RESOURCE_UUID = "c33877b5-424e-4227-2324-c338b68b1e78";
	
	private static final String RESOURCE_DATE_TYPE = "org.openmrs.customdatatype.datatype.LongFreeTextDatatype";
	
	@Mock
	private FormService formService;
	
	private QuestionnaireTranslatorImpl questionnaireTranslator;
	
	@Before
	public void setup() {
		questionnaireTranslator = new QuestionnaireTranslatorImpl();
		questionnaireTranslator.setFormService(formService);
	}
	
	@Test(expected = NullPointerException.class)
	public void shouldTranslateOpenmrsFormToFhirQuestionnaireDoesntContainFHIRQuestionnaire() {
		
		Form form = getFormMock();
		FormResource formResource = getFormResourceMock(form);
		formResource.setName("");
		
		org.hl7.fhir.r4.model.Questionnaire result = questionnaireTranslator.toFhirResource(form);
	}
	
	@Test
	public void shouldTranslateQuestionnaireToOpenmrsForm() {
		Form form = getFormMock();
		when(formService.getForm(FORM_UUID)).thenReturn(form);
		
		Questionnaire questionnaire = new Questionnaire();
		questionnaire.setId(FORM_UUID);
		
		Form result = questionnaireTranslator.toOpenmrsType(questionnaire);
		assertThat(result, notNullValue());
		assertThat(result, equalTo(form));
	}
	
	public Form getFormMock() {
		User user = new User();
		user.setUuid(USER_UUID);
		
		Form formMock = new Form();
		formMock.setUuid(FORM_UUID);
		formMock.setCreator(user);
		formMock.setDateCreated(new Date());
		formMock.setChangedBy(user);
		formMock.setDateChanged(new Date());
		
		return formMock;
	}
	
	public FormResource getFormResourceMock(Form formMock) {
		FormResource formResourceMock = new FormResource();
		formResourceMock.setUuid(FORM_RESOURCE_UUID);
		formResourceMock.setForm(formMock);
		formResourceMock.setName(FhirConstants.FHIR_QUESTIONNAIRE_TYPE);
		formResourceMock.setDatatypeClassname(RESOURCE_DATE_TYPE);
		LongFreeTextDatatype value = new LongFreeTextDatatype();
		formResourceMock.setValue(value);
		
		return formResourceMock;
	}
	
}
