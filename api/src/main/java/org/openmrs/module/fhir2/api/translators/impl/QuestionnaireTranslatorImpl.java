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

import static org.apache.commons.lang3.Validate.notNull;
import static org.openmrs.module.fhir2.api.translators.impl.FhirTranslatorUtils.getLastUpdated;
import static org.openmrs.module.fhir2.api.translators.impl.FhirTranslatorUtils.getVersionId;

import javax.annotation.Nonnull;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AccessLevel;
import lombok.Setter;
import org.hl7.fhir.instance.model.api.IBaseDatatype;
import org.hl7.fhir.r4.model.*;
import org.openmrs.*;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.api.dao.FhirQuestionnaireDao;
import org.openmrs.module.fhir2.api.translators.*;
import org.openmrs.module.fhir2.api.util.FormResourceAuditable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Setter(AccessLevel.PACKAGE)
public class QuestionnaireTranslatorImpl implements QuestionnaireTranslator {
	
	@Autowired
	private FhirQuestionnaireDao questionnaireDao;
	
	@Override
	public org.hl7.fhir.r4.model.Questionnaire toFhirResource(@Nonnull FormResourceAuditable openmrsFormResource) {
		notNull(openmrsFormResource, "The Openmrs FormResource object should not be null");
		Form openmrsForm = openmrsFormResource.getForm();
		
		org.hl7.fhir.r4.model.Questionnaire questionnaire = new org.hl7.fhir.r4.model.Questionnaire();
		questionnaire.setId(openmrsForm.getUuid());
		questionnaire.setTitle("TODO this is a title"); //TODO
		questionnaire.setName(openmrsForm.getName());
		questionnaire.setStatus(
		    openmrsForm.getRetired() ? Enumerations.PublicationStatus.RETIRED : Enumerations.PublicationStatus.ACTIVE);
		questionnaire.setVersion(openmrsForm.getVersion());
		List<CodeType> codeTypes = new ArrayList<>();
		codeTypes.add(new CodeType("Encounter"));
		codeTypes.add(new CodeType("Patient"));
		codeTypes.add(new CodeType("Practitioner"));
		questionnaire.setSubjectType(codeTypes);
		
		List<Extension> extensions = new ArrayList<>();
		
		Expression expression = new Expression(new CodeType(Expression.ExpressionLanguage.APPLICATION_XFHIRQUERY.toCode()));
		expression.setExpression("Encounter");
		Extension encounterContextExtension = new Extension(
		        FhirConstants.SDC_QUESTIONNAIRE_ITEM_EXTRACTION_CONTEXT_STATUS_SYSTEM_URI_R4, expression);
		extensions.add(encounterContextExtension);
		
		IBaseDatatype valueCode = new CodeType("prior-edit");
		Extension entryModeExtension = new Extension(FhirConstants.SDC_QUESTIONNAIRE_ENTRY_MODE_STATUS_SYSTEM_URI_R4,
		        valueCode);
		extensions.add(entryModeExtension);
		
		questionnaire.setExtension(extensions);
		
		questionnaire.getMeta().setLastUpdated(getLastUpdated(openmrsForm));
		questionnaire.getMeta().setVersionId(getVersionId(openmrsForm));
		
		ObjectMapper objectMapper = new ObjectMapper();
		
		ArrayList<Questionnaire.QuestionnaireItemComponent> item = new ArrayList<>();
		
		try {
			JsonNode rootNode = objectMapper.readTree(openmrsFormResource.getValue().toString());
			JsonNode pagesArray = rootNode.path("pages");
			
			for (JsonNode page : pagesArray) {
				item.add(convertPageToItem(page));
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		questionnaire.setItem(item);
		return questionnaire;
	}
	
	private Questionnaire.QuestionnaireItemComponent convertPageToItem(JsonNode page) {
		Questionnaire.QuestionnaireItemComponent item = new Questionnaire.QuestionnaireItemComponent();
		item.setText(page.path("label").asText());
		item.setLinkId(page.path("label").asText().replace(" ", ""));
		item.setType(Questionnaire.QuestionnaireItemType.GROUP);
		
		List<Extension> extensions = new ArrayList<>();
		
		Extension pageContextExtension = new Extension(FhirConstants.SDC_QUESTIONNAIRE_ITEM_CONTROL_STATUS_SYSTEM_URI_R4);
		Coding coding = new Coding();
		coding.setSystem(FhirConstants.QUESTIONNAIRE_ITEM_CONTROL_STATUS_SYSTEM_URI_R4);
		coding.setCode("page");
		coding.setDisplay("Page");

		List<Coding> codingList = new ArrayList<>();
		codingList.add(coding);
		
		CodeableConcept codeableConcept = new CodeableConcept();
		codeableConcept.setCoding(codingList);
		pageContextExtension.setValue(codeableConcept);
		extensions.add(pageContextExtension);
		
		item.setExtension(extensions);
		
		/*		JsonNode sectionsArray = page.path("sections");
				for (JsonNode section : sectionsArray) {
					String sectionLabel = section.path("label").asText();
					System.out.println("  Section: " + sectionLabel);
		
					JsonNode questionsArray = section.path("questions");
					for (JsonNode question : questionsArray) {
						String questionLabel = question.path("label").asText("No label");
						String questionId = question.path("id").asText("No ID");
						System.out.println("    Question: " + questionLabel + " (ID: " + questionId + ")");
					}
				}*/
		return item;
	}
	
	@Override
	public FormResourceAuditable toOpenmrsType(@Nonnull org.hl7.fhir.r4.model.Questionnaire questionnaire) {
		notNull(questionnaire, "The Questionnaire object should not be null");
		return toOpenmrsType(new FormResourceAuditable(), questionnaire);
	}
	
	@Override
	public FormResourceAuditable toOpenmrsType(@Nonnull FormResourceAuditable openmrsForm,
	        @Nonnull org.hl7.fhir.r4.model.Questionnaire questionnaire) {
		notNull(openmrsForm, "The existing Openmrs Form object should not be null");
		notNull(questionnaire, "The Questionnaire object should not be null");
		
		if (questionnaire.hasId()) {
			openmrsForm.setUuid(questionnaire.getIdElement().getIdPart());
		}
		
		openmrsForm.setName(questionnaire.getName());
		//TODO
		
		return openmrsForm;
	}
}
