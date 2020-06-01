/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.fhir2.api.narrative.impl;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import org.hl7.fhir.r4.model.DiagnosticReport;
import org.hl7.fhir.r4.model.Medication;
import org.hl7.fhir.r4.model.Narrative;
import org.hl7.fhir.r4.model.Patient;
import org.junit.Before;
import org.junit.Test;

public class DefaultNarrativeGeneratorTest {
	
	private DiagnosticReport diagnosticReport;
	
	private Medication medication;
	
	private Patient patient;
	
	private DefaultNarrativeGeneratorImpl defaultNarrativeGenerator;
	
	@Before
	public void setup() {
		diagnosticReport = new DiagnosticReport();
		medication = new Medication();
		patient = new Patient();
		defaultNarrativeGenerator = new DefaultNarrativeGeneratorImpl();
	}
	
	@Test
	public void generateDefaultNarrativeForDiagnosticReportResource() {
		Narrative narrative = defaultNarrativeGenerator.generateDefaultNarrative(diagnosticReport);
		assertThat(narrative, notNullValue());
		assertThat(narrative.getStatusAsString(), notNullValue());
		assertThat(narrative.getStatusAsString(), equalTo("generated"));
		assertThat(narrative.getDivAsString(), notNullValue());
	}
	
	@Test
	public void generateDefaultNarrativeForMedicationResource() {
		Narrative narrative = defaultNarrativeGenerator.generateDefaultNarrative(medication);
		assertThat(narrative, notNullValue());
		assertThat(narrative.getStatusAsString(), notNullValue());
		assertThat(narrative.getStatusAsString(), equalTo("generated"));
		assertThat(narrative.getDivAsString(), notNullValue());
	}
	
	@Test
	public void generateDefaultNarrativeForPatientResource() {
		Narrative narrative = defaultNarrativeGenerator.generateDefaultNarrative(patient);
		assertThat(narrative, notNullValue());
		assertThat(narrative.getStatusAsString(), notNullValue());
		assertThat(narrative.getStatusAsString(), equalTo("generated"));
		assertThat(narrative.getDivAsString(), notNullValue());
	}
}
