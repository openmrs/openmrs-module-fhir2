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
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Optional;

import org.hl7.fhir.r4.model.ValueSet;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.openmrs.Concept;
import org.openmrs.ConceptDescription;
import org.openmrs.ConceptMap;
import org.openmrs.ConceptName;
import org.openmrs.ConceptReferenceTerm;
import org.openmrs.ConceptSet;
import org.openmrs.ConceptSource;
import org.openmrs.module.fhir2.FhirTestConstants;
import org.openmrs.module.fhir2.TestFhirSpringConfiguration;
import org.openmrs.module.fhir2.api.FhirConceptSourceService;
import org.openmrs.module.fhir2.api.translators.ValueSetReferenceTranslator;
import org.openmrs.module.fhir2.model.FhirConceptSource;
import org.openmrs.test.BaseModuleContextSensitiveTest;
import org.springframework.test.context.ContextConfiguration;

@ContextConfiguration(classes = TestFhirSpringConfiguration.class, inheritLocations = false)
public class ValueSetTranslatorImplTest extends BaseModuleContextSensitiveTest {
	
	private static final String CONCEPT_UUID = "0f97e14e-cdc2-49ac-9255-b5126f8a5147";
	
	@Mock
	private FhirConceptSourceService conceptSourceService;
	
	@Mock
	private ValueSetReferenceTranslator valueSetReferenceTranslator;
	
	@Mock
	private Concept concept;
	
	private final ValueSetTranslatorImpl valueSetTranslator = new ValueSetTranslatorImpl();
	
	@Before
	public void setup() {
		valueSetTranslator.setConceptSourceService(conceptSourceService);
		valueSetTranslator.setValueSetReferenceTranslator(valueSetReferenceTranslator);
	}
	
	@Test
	public void shouldTranslateConceptSetToValueSet() {
		ConceptName conceptName = new ConceptName();
		conceptName.setName("test");
		
		ConceptDescription description = new ConceptDescription();
		description.setDescription("test");
		
		when(concept.getUuid()).thenReturn(CONCEPT_UUID);
		when(concept.getName()).thenReturn(conceptName);
		when(concept.getDateChanged()).thenReturn(new Date());
		when(concept.getSet()).thenReturn(true);
		
		Concept concept1 = new Concept();
		ConceptMap conceptMap = mock(ConceptMap.class);
		ConceptReferenceTerm conceptReferenceTerm = mock(ConceptReferenceTerm.class);
		ConceptSource conceptSource = mock(ConceptSource.class);
		
		when(conceptMap.getConceptReferenceTerm()).thenReturn(conceptReferenceTerm);
		when(conceptReferenceTerm.getConceptSource()).thenReturn(conceptSource);
		when(conceptReferenceTerm.getCode()).thenReturn("1000-1");
		when(conceptSource.getName()).thenReturn("LOINC");
		concept1.addConceptMapping(conceptMap);
		
		FhirConceptSource loinc = new FhirConceptSource();
		ConceptSource loincConceptSource = new ConceptSource();
		loincConceptSource.setName("LOINC");
		loinc.setConceptSource(loincConceptSource);
		loinc.setUrl(FhirTestConstants.LOINC_SYSTEM_URL);
		when(conceptSourceService.getFhirConceptSource(loincConceptSource)).thenReturn(Optional.of(loinc));
		
		ConceptSet conceptSet = new ConceptSet();
		conceptSet.setConceptSet(concept);
		conceptSet.setConcept(concept1);
		
		Collection<ConceptSet> conceptSets = new ArrayList<ConceptSet>();
		conceptSets.add(conceptSet);
		
		when(concept.getConceptSets()).thenReturn(conceptSets);
		
		ValueSet valueSet = valueSetTranslator.toFhirResource(concept);
		
		assertThat(valueSet.getId(), equalTo(CONCEPT_UUID));
		assertThat(valueSet.getCompose(), notNullValue());
		assertThat(valueSet.getCompose().getInclude(), hasSize(greaterThanOrEqualTo(1)));
		assertThat((valueSet.getCompose().getInclude().iterator().next()).getSystemElement(), notNullValue());
		assertThat(((valueSet.getCompose().getInclude().iterator().next()).getConcept().iterator().next()).getCode(),
		    notNullValue());
	}
	
}
