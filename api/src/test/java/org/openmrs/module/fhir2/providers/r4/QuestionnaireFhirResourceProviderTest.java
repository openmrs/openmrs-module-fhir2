/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.fhir2.providers.r4;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.when;

import java.util.*;

import ca.uhn.fhir.rest.api.server.IBundleProvider;
import ca.uhn.fhir.rest.param.*;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.*;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.api.FhirQuestionnaireService;
import org.openmrs.module.fhir2.api.search.param.QuestionnaireSearchParams;
import org.openmrs.module.fhir2.providers.BaseFhirProvenanceResourceTest;

@RunWith(MockitoJUnitRunner.class)
public class QuestionnaireFhirResourceProviderTest extends BaseFhirProvenanceResourceTest<Questionnaire> {
	
	private static final String QUESTIONNAIRE_UUID = "017312a1-cf56-43ab-ae87-44070b801d1c";
	
	private static final String WRONG_QUESTIONNAIRE_UUID = "017312a1-cf56-43ab-ae87-44070b801d1c";
	
	private static final String FORM_NAME = "Form name";
	
	@Mock
	private FhirQuestionnaireService questionnaireService;
	
	private QuestionnaireFhirResourceProvider resourceProvider;
	
	private Questionnaire questionnaire;
	
	@Before
	public void setup() {
		resourceProvider = new QuestionnaireFhirResourceProvider();
		resourceProvider.setFhirQuestionnaireService(questionnaireService);
	}
	
	@Before
	public void initPatient() {
		questionnaire = new Questionnaire();
		questionnaire.setId(QUESTIONNAIRE_UUID);
		setProvenanceResources(questionnaire);
	}
	
	@Test
	public void getResourceType_shouldReturnResourceType() {
		assertThat(resourceProvider.getResourceType(), equalTo(Questionnaire.class));
		assertThat(resourceProvider.getResourceType().getName(), equalTo(Questionnaire.class.getName()));
	}
	
	@Test
	public void getQuestionnaireById_shouldReturnQuestionnaire() {
		IdType id = new IdType();
		id.setValue(QUESTIONNAIRE_UUID);
		when(questionnaireService.get(QUESTIONNAIRE_UUID)).thenReturn(questionnaire);
		
		Questionnaire result = resourceProvider.getQuestionnaireById(id);
		assertThat(result.isResource(), is(true));
		assertThat(result, notNullValue());
		assertThat(result.getId(), notNullValue());
		assertThat(result.getId(), equalTo(QUESTIONNAIRE_UUID));
	}
	
	@Test(expected = ResourceNotFoundException.class)
	public void getQuestionnaireByWithWrongId_shouldThrowResourceNotFoundException() {
		IdType idType = new IdType();
		idType.setValue(WRONG_QUESTIONNAIRE_UUID);
		assertThat(resourceProvider.getQuestionnaireById(idType).isResource(), is(true));
		assertThat(resourceProvider.getQuestionnaireById(idType), nullValue());
	}
	
	@Test
	public void searchQuestionnaires_shouldReturnMatchingBundleOfQuestionnairesByName() {
		StringAndListParam nameParam = new StringAndListParam()
		        .addAnd(new StringOrListParam().add(new StringParam(FORM_NAME)));
		
		when(questionnaireService.searchForQuestionnaires(new QuestionnaireSearchParams(nameParam, null, null, null)))
		        .thenReturn(new MockIBundleProvider<>(Collections.singletonList(questionnaire), 10, 1));
		
		IBundleProvider results = resourceProvider.searchQuestionnaire(nameParam, null, null, null);
		List<IBaseResource> resources = getResources(results);
		
		assertThat(resources, notNullValue());
		assertThat(resources, hasSize(equalTo(1)));
		assertThat(resources.get(0).fhirType(), is(FhirConstants.QUESTIONNAIRE));
		assertThat(resources.get(0).getIdElement().getIdPart(), is(QUESTIONNAIRE_UUID));
	}
	
	private List<IBaseResource> getResources(IBundleProvider result) {
		return result.getResources(0, 10);
	}
	
	private List<IBaseResource> getAllResources(IBundleProvider result) {
		return result.getAllResources();
	}
}
