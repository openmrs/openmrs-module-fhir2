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
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;

import org.hl7.fhir.r4.model.Condition;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openmrs.Diagnosis;
import org.openmrs.Encounter;
import org.openmrs.User;
import org.openmrs.module.fhir2.api.translators.ConceptTranslator;
import org.openmrs.module.fhir2.api.translators.EncounterReferenceTranslator;
import org.openmrs.module.fhir2.api.translators.PatientReferenceTranslator;
import org.openmrs.module.fhir2.api.translators.PractitionerReferenceTranslator;

@RunWith(MockitoJUnitRunner.class)
public class R3DiagnosisTranslatorImplTest {
	
	@Mock
	private PatientReferenceTranslator patientReferenceTranslator;
	
	@Mock
	private EncounterReferenceTranslator<Encounter> encounterReferenceTranslator;
	
	@Mock
	private PractitionerReferenceTranslator<User> practitionerReferenceTranslator;
	
	@Mock
	private ConceptTranslator conceptTranslator;
	
	private R3DiagnosisTranslatorImpl translator;
	
	@Before
	public void setup() {
		translator = new R3DiagnosisTranslatorImpl();
		translator.setPatientReferenceTranslator(patientReferenceTranslator);
		translator.setEncounterReferenceTranslator(encounterReferenceTranslator);
		translator.setPractitionerReferenceTranslator(practitionerReferenceTranslator);
		translator.setConceptTranslator(conceptTranslator);
		when(practitionerReferenceTranslator.toFhirResource(isNull())).thenReturn(null);
	}
	
	@Test
	public void toFhirResource_shouldOmitClinicalStatus() {
		Diagnosis diagnosis = new Diagnosis();
		diagnosis.setUuid("diag-uuid");
		diagnosis.setVoided(false);
		
		Condition condition = translator.toFhirResource(diagnosis);
		
		assertThat(condition.hasClinicalStatus(), is(false));
	}
}
