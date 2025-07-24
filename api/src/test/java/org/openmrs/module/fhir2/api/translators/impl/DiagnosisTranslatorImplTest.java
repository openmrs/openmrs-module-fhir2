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
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;

import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Condition;
import org.hl7.fhir.r4.model.Extension;
import org.hl7.fhir.r4.model.IntegerType;
import org.hl7.fhir.r4.model.StringType;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openmrs.ConditionVerificationStatus;
import org.openmrs.Diagnosis;
import org.openmrs.Encounter;
import org.openmrs.User;
import org.openmrs.module.fhir2.api.translators.ConceptTranslator;
import org.openmrs.module.fhir2.api.translators.EncounterReferenceTranslator;
import org.openmrs.module.fhir2.api.translators.PatientReferenceTranslator;
import org.openmrs.module.fhir2.api.translators.PractitionerReferenceTranslator;

@RunWith(MockitoJUnitRunner.class)
public class DiagnosisTranslatorImplTest {
	
	@Mock
	private PatientReferenceTranslator patientReferenceTranslator;
	
	@Mock
	private EncounterReferenceTranslator<Encounter> encounterReferenceTranslator;
	
	@Mock
	private PractitionerReferenceTranslator<User> practitionerReferenceTranslator;
	
	@Mock
	private ConceptTranslator conceptTranslator;
	
	private DiagnosisTranslatorImpl translator;
	
	@Before
	public void setup() {
		translator = new DiagnosisTranslatorImpl();
		translator.setPatientReferenceTranslator(patientReferenceTranslator);
		translator.setEncounterReferenceTranslator(encounterReferenceTranslator);
		translator.setPractitionerReferenceTranslator(practitionerReferenceTranslator);
		translator.setConceptTranslator(conceptTranslator);
		when(practitionerReferenceTranslator.toFhirResource(isNull())).thenReturn(null);
	}
	
	@Test
	public void toFhirResource_shouldAddRankAndCertaintyExtensions() {
		Diagnosis diagnosis = new Diagnosis();
		diagnosis.setUuid("diag-uuid");
		diagnosis.setRank(1);
		diagnosis.setCertainty(ConditionVerificationStatus.CONFIRMED);
		diagnosis.setVoided(false);
		
		Condition result = translator.toFhirResource(diagnosis);
		
		Extension rank = result.getExtensionByUrl("http://openmrs.org/fhir/StructureDefinition/diagnosis-rank");
		assertThat(rank, notNullValue());
		assertThat(((IntegerType) rank.getValue()).getValue(), equalTo(1));
		
		Extension certainty = result.getExtensionByUrl("http://openmrs.org/fhir/StructureDefinition/diagnosis-certainty");
		assertThat(certainty, notNullValue());
		assertThat(((StringType) certainty.getValue()).asStringValue(),
		    equalTo(ConditionVerificationStatus.CONFIRMED.toString()));
	}
	
	@Test
	public void toOpenmrsType_shouldMapVerificationStatusToCertainty() {
		Condition condition = new Condition();
		CodeableConcept verification = new CodeableConcept();
		verification.addCoding(
		    new Coding().setSystem("http://terminology.hl7.org/CodeSystem/condition-ver-status").setCode("provisional"));
		condition.setVerificationStatus(verification);
		
		Diagnosis result = translator.toOpenmrsType(condition);
		assertThat(result.getCertainty(), equalTo(ConditionVerificationStatus.PROVISIONAL));
	}
}
