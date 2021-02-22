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

import org.hibernate.SessionFactory;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.openmrs.Concept;
import org.openmrs.ConceptClass;
import org.openmrs.module.fhir2.TestFhirSpringConfiguration;
import org.openmrs.module.fhir2.api.mappings.ObservationCategoryMap;
import org.openmrs.test.BaseModuleContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

@ContextConfiguration(classes = TestFhirSpringConfiguration.class, inheritLocations = false)
public class ObservationCategoryTranslatorImplTest extends BaseModuleContextSensitiveTest {
	
	private final String OBSERVATION_CATEGORY_CONCEPT_CLASS_DATA = "org/openmrs/module/fhir2/mapping/FhirObservationCategoryTest_initial_data.xml";
	
	private final String LABORATORY_CONCEPT_CLASS_UUID = "8d4907b2-c2cc-11de-8d13-0010c6dffd0f";
	
	private final String PROCEDURE_CONCEPT_CLASS_UUID = "8d490bf4-c2cc-11de-8d13-0010c6dffd0f";
	
	private final String EXAM_CONCEPT_CLASS_UUID = "8d491a9a-c2cc-11de-8d13-0010c6dffd0f";
	
	@Mock
	private ObservationCategoryMap categoryMap;
	
	@Autowired
	private SessionFactory sessionFactory;
	
	private Concept concept;
	
	private CodeableConcept codeableConcept;
	
	private ObservationCategoryTranslatorImpl observationCategoryTranslator;
	
	@Before
	public void setup() throws Exception {
		observationCategoryTranslator = new ObservationCategoryTranslatorImpl();
		categoryMap = new ObservationCategoryMap();
		concept = new Concept();
		categoryMap.setSessionFactory(sessionFactory);
		observationCategoryTranslator.setCategoryMap(categoryMap);
		
		executeDataSet(OBSERVATION_CATEGORY_CONCEPT_CLASS_DATA);
	}
	
	@Test
	public void shouldTranslateConceptClassToCodeableConceptIsnull() {
		ConceptClass conceptClass = new ConceptClass();
		// wrong uuid which is not in dataSet
		conceptClass.setUuid("0");
		concept.setConceptClass(conceptClass);
		
		codeableConcept = observationCategoryTranslator.toFhirResource(concept);
		
		assertThat(codeableConcept, equalTo(null));
	}
	
	@Test
	public void shouldTranslateConceptClassToCodeableConceptIsLaboratory() {
		ConceptClass conceptClass = new ConceptClass();
		conceptClass.setUuid(LABORATORY_CONCEPT_CLASS_UUID);
		concept.setConceptClass(conceptClass);
		
		codeableConcept = observationCategoryTranslator.toFhirResource(concept);
		
		assertThat(codeableConcept, notNullValue());
		assertThat(codeableConcept.getCoding().get(0).getDisplay(), equalTo("Laboratory"));
	}
	
	@Test
	public void shouldTranslateConceptClassToCodeableConceptIsProcedure() {
		ConceptClass conceptClass = new ConceptClass();
		conceptClass.setUuid(PROCEDURE_CONCEPT_CLASS_UUID);
		concept.setConceptClass(conceptClass);
		
		codeableConcept = observationCategoryTranslator.toFhirResource(concept);
		
		assertThat(codeableConcept, notNullValue());
		assertThat(codeableConcept.getCoding().get(0).getDisplay(), equalTo("Procedure"));
	}
	
	@Test
	public void shouldTranslateConceptClassToCodeableConceptIsExam() {
		ConceptClass conceptClass = new ConceptClass();
		conceptClass.setUuid(EXAM_CONCEPT_CLASS_UUID);
		concept.setConceptClass(conceptClass);
		
		codeableConcept = observationCategoryTranslator.toFhirResource(concept);
		
		assertThat(codeableConcept, notNullValue());
		assertThat(codeableConcept.getCoding().get(0).getDisplay(), equalTo("Exam"));
	}
}
