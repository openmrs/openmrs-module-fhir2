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
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.openmrs.Obs;
import org.openmrs.module.fhir2.FhirConstants;

@RunWith(MockitoJUnitRunner.class)
public class ObservationInterpretationTranslatorImpl_2_1Test {
	
	private ObservationInterpretationTranslatorImpl_2_1 observationInterpretationTranslator;
	
	Obs obs;
	
	CodeableConcept interpretation;
	
	@Before
	public void setUp() {
		observationInterpretationTranslator = new ObservationInterpretationTranslatorImpl_2_1();
		obs = new Obs();
		interpretation = new CodeableConcept();
	}
	
	@Test
	public void toFhirResource_shouldTranslateNormalInterpretationCorrectly() {
		obs.setInterpretation(Obs.Interpretation.NORMAL);
		CodeableConcept interpretation = observationInterpretationTranslator.toFhirResource(obs);
		assertThat(interpretation, notNullValue());
		assertThat(interpretation.getCoding().get(0).getCode(), is("N"));
		assertThat(interpretation.getCoding().get(0).getDisplay(), is("Normal"));
		assertThat(interpretation.getText(), is("Normal"));
	}
	
	@Test
	public void toFhirResource_shouldTranslateAbnormalInterpretationCorrectly() {
		obs.setInterpretation(Obs.Interpretation.ABNORMAL);
		CodeableConcept interpretation = observationInterpretationTranslator.toFhirResource(obs);
		assertThat(interpretation, notNullValue());
		assertThat(interpretation.getCoding().get(0).getCode(), is("A"));
		assertThat(interpretation.getCoding().get(0).getDisplay(), is("Abnormal"));
		assertThat(interpretation.getText(), is("Abnormal"));
	}
	
	@Test
	public void toFhirResource_shouldTranslateCriticallyAbnormalInterpretationCorrectly() {
		obs.setInterpretation(Obs.Interpretation.CRITICALLY_ABNORMAL);
		CodeableConcept interpretation = observationInterpretationTranslator.toFhirResource(obs);
		assertThat(interpretation, notNullValue());
		assertThat(interpretation.getCoding().get(0).getCode(), is("AA"));
		assertThat(interpretation.getCoding().get(0).getDisplay(), is("Critically Abnormal"));
		assertThat(interpretation.getText(), is("Critically Abnormal"));
	}
	
	@Test
	public void toFhirResource_shouldTranslateCriticallyHighInterpretationCorrectly() {
		obs.setInterpretation(Obs.Interpretation.CRITICALLY_HIGH);
		CodeableConcept interpretation = observationInterpretationTranslator.toFhirResource(obs);
		assertThat(interpretation, notNullValue());
		assertThat(interpretation.getCoding().get(0).getCode(), is("HH"));
		assertThat(interpretation.getCoding().get(0).getDisplay(), is("Critically High"));
		assertThat(interpretation.getText(), is("Critically High"));
	}
	
	@Test
	public void toFhirResource_shouldTranslateCriticallyLowInterpretationCorrectly() {
		obs.setInterpretation(Obs.Interpretation.CRITICALLY_LOW);
		CodeableConcept interpretation = observationInterpretationTranslator.toFhirResource(obs);
		assertThat(interpretation, notNullValue());
		assertThat(interpretation.getCoding().get(0).getCode(), is("LL"));
		assertThat(interpretation.getCoding().get(0).getDisplay(), is("Critically Low"));
		assertThat(interpretation.getText(), is("Critically Low"));
	}
	
	@Test
	public void toFhirResource_shouldTranslateHighInterpretationCorrectly() {
		obs.setInterpretation(Obs.Interpretation.HIGH);
		CodeableConcept interpretation = observationInterpretationTranslator.toFhirResource(obs);
		assertThat(interpretation, notNullValue());
		assertThat(interpretation.getCoding().get(0).getCode(), is("H"));
		assertThat(interpretation.getCoding().get(0).getDisplay(), is("High"));
		assertThat(interpretation.getText(), is("High"));
	}
	
	@Test
	public void toFhirResource_shouldTranslateLowInterpretationCorrectly() {
		obs.setInterpretation(Obs.Interpretation.LOW);
		CodeableConcept interpretation = observationInterpretationTranslator.toFhirResource(obs);
		assertThat(interpretation, notNullValue());
		assertThat(interpretation.getCoding().get(0).getCode(), is("L"));
		assertThat(interpretation.getCoding().get(0).getDisplay(), is("Low"));
		assertThat(interpretation.getText(), is("Low"));
	}
	
	@Test
	public void toFhirResource_shouldTranslateOffScaleLowInterpretationCorrectly() {
		obs.setInterpretation(Obs.Interpretation.OFF_SCALE_LOW);
		CodeableConcept interpretation = observationInterpretationTranslator.toFhirResource(obs);
		assertThat(interpretation, notNullValue());
		assertThat(interpretation.getCoding().get(0).getCode(), is("<"));
		assertThat(interpretation.getCoding().get(0).getDisplay(), is("Off Scale Low"));
		assertThat(interpretation.getText(), is("Off Scale Low"));
	}
	
	@Test
	public void toFhirResource_shouldTranslateOffScaleHighInterpretationCorrectly() {
		obs.setInterpretation(Obs.Interpretation.OFF_SCALE_HIGH);
		CodeableConcept interpretation = observationInterpretationTranslator.toFhirResource(obs);
		assertThat(interpretation, notNullValue());
		assertThat(interpretation.getCoding().get(0).getCode(), is(">"));
		assertThat(interpretation.getCoding().get(0).getDisplay(), is("Off Scale High"));
		assertThat(interpretation.getText(), is("Off Scale High"));
	}
	
	@Test
	public void toFhirResource_shouldTranslateSignificantChangeDownInterpretationCorrectly() {
		obs.setInterpretation(Obs.Interpretation.SIGNIFICANT_CHANGE_DOWN);
		CodeableConcept interpretation = observationInterpretationTranslator.toFhirResource(obs);
		assertThat(interpretation, notNullValue());
		assertThat(interpretation.getCoding().get(0).getCode(), is("D"));
		assertThat(interpretation.getCoding().get(0).getDisplay(), is("Significant Change Down"));
		assertThat(interpretation.getText(), is("Significant Change Down"));
	}
	
	@Test
	public void toFhirResource_shouldTranslateSignificantChangeUpInterpretationCorrectly() {
		obs.setInterpretation(Obs.Interpretation.SIGNIFICANT_CHANGE_UP);
		CodeableConcept interpretation = observationInterpretationTranslator.toFhirResource(obs);
		assertThat(interpretation, notNullValue());
		assertThat(interpretation.getCoding().get(0).getCode(), is("U"));
		assertThat(interpretation.getCoding().get(0).getDisplay(), is("Significant Change Up"));
		assertThat(interpretation.getText(), is("Significant Change Up"));
	}
	
	@Test
	public void toFhirResource_shouldTranslateResistantInterpretationCorrectly() {
		obs.setInterpretation(Obs.Interpretation.RESISTANT);
		CodeableConcept interpretation = observationInterpretationTranslator.toFhirResource(obs);
		assertThat(interpretation, notNullValue());
		assertThat(interpretation.getCoding().get(0).getCode(), is("R"));
		assertThat(interpretation.getCoding().get(0).getDisplay(), is("Resistant"));
		assertThat(interpretation.getText(), is("Resistant"));
	}
	
	@Test
	public void toFhirResource_shouldTranslateSusceptibleInterpretationCorrectly() {
		obs.setInterpretation(Obs.Interpretation.SUSCEPTIBLE);
		CodeableConcept interpretation = observationInterpretationTranslator.toFhirResource(obs);
		assertThat(interpretation, notNullValue());
		assertThat(interpretation.getCoding().get(0).getCode(), is("S"));
		assertThat(interpretation.getCoding().get(0).getDisplay(), is("Susceptible"));
		assertThat(interpretation.getText(), is("Susceptible"));
	}
	
	@Test
	public void toFhirResource_shouldTranslateIntermediateInterpretationCorrectly() {
		obs.setInterpretation(Obs.Interpretation.INTERMEDIATE);
		CodeableConcept interpretation = observationInterpretationTranslator.toFhirResource(obs);
		assertThat(interpretation, notNullValue());
		assertThat(interpretation.getCoding().get(0).getCode(), is("I"));
		assertThat(interpretation.getCoding().get(0).getDisplay(), is("Intermediate"));
		assertThat(interpretation.getText(), is("Intermediate"));
	}
	
	@Test
	public void toFhirResource_shouldTranslatePositiveInterpretationCorrectly() {
		obs.setInterpretation(Obs.Interpretation.POSITIVE);
		CodeableConcept interpretation = observationInterpretationTranslator.toFhirResource(obs);
		assertThat(interpretation, notNullValue());
		assertThat(interpretation.getCoding().get(0).getCode(), is("POS"));
		assertThat(interpretation.getCoding().get(0).getDisplay(), is("Positive"));
		assertThat(interpretation.getText(), is("Positive"));
	}
	
	@Test
	public void toFhirResource_shouldTranslateNegativeInterpretationCorrectly() {
		obs.setInterpretation(Obs.Interpretation.NEGATIVE);
		CodeableConcept interpretation = observationInterpretationTranslator.toFhirResource(obs);
		assertThat(interpretation, notNullValue());
		assertThat(interpretation.getCoding().get(0).getCode(), is("NEG"));
		assertThat(interpretation.getCoding().get(0).getDisplay(), is("Negative"));
		assertThat(interpretation.getText(), is("Negative"));
	}
	
	@Test
	public void toFhirResource_shouldTranslateVerySusceptibleInterpretationCorrectly() {
		obs.setInterpretation(Obs.Interpretation.VERY_SUSCEPTIBLE);
		CodeableConcept interpretation = observationInterpretationTranslator.toFhirResource(obs);
		assertThat(interpretation, notNullValue());
		assertThat(interpretation.getCoding().get(0).getCode(), is("VS"));
		assertThat(interpretation.getCoding().get(0).getDisplay(), is("Very Susceptible"));
		assertThat(interpretation.getText(), is("Very Susceptible"));
		assertThat(interpretation.getCoding().get(0).getSystem(), is(FhirConstants.OPENMRS_FHIR_EXT_VS_INTERPRETATION));
	}
	
	@Test
	public void toFhirResource_shouldReturnNoFhirInterpretationWhenObsInterpretationIsNull() {
		obs.setInterpretation(null);
		CodeableConcept interpretation = observationInterpretationTranslator.toFhirResource(obs);
		assertThat(interpretation, nullValue());
	}
	
	@Test
	public void toOpenmrsType_shouldReturnNullIfInterpretationSizeIsZero() {
		observationInterpretationTranslator.toOpenmrsType(obs, interpretation);
		assertThat(obs.getInterpretation(), nullValue());
	}
	
	@Test
	public void toOpenmrsType_shouldTranslateNormalCodeCorrectly() {
		Coding coding = new Coding();
		coding.setCode("N");
		interpretation.addCoding(coding);
		observationInterpretationTranslator.toOpenmrsType(obs, interpretation);
		assertThat(obs.getInterpretation(), notNullValue());
		assertThat(obs.getInterpretation(), is(Obs.Interpretation.NORMAL));
	}
	
	@Test
	public void toOpenmrsType_shouldTranslateAbnormalCodeCorrectly() {
		Coding coding = new Coding();
		coding.setCode("A");
		interpretation.addCoding(coding);
		observationInterpretationTranslator.toOpenmrsType(obs, interpretation);
		assertThat(obs.getInterpretation(), notNullValue());
		assertThat(obs.getInterpretation(), is(Obs.Interpretation.ABNORMAL));
	}
	
	@Test
	public void toOpenmrsType_shouldTranslateCriticallyAbnormalCodeCorrectly() {
		Coding coding = new Coding();
		coding.setCode("AA");
		interpretation.addCoding(coding);
		observationInterpretationTranslator.toOpenmrsType(obs, interpretation);
		assertThat(obs.getInterpretation(), notNullValue());
		assertThat(obs.getInterpretation(), is(Obs.Interpretation.CRITICALLY_ABNORMAL));
	}
	
	@Test
	public void toOpenmrsType_shouldTranslateCriticallyHighCodeCorrectly() {
		Coding coding = new Coding();
		coding.setCode("HH");
		interpretation.addCoding(coding);
		observationInterpretationTranslator.toOpenmrsType(obs, interpretation);
		assertThat(obs.getInterpretation(), notNullValue());
		assertThat(obs.getInterpretation(), is(Obs.Interpretation.CRITICALLY_HIGH));
	}
	
	@Test
	public void toOpenmrsType_shouldTranslateCriticallyLowCodeCorrectly() {
		Coding coding = new Coding();
		coding.setCode("LL");
		interpretation.addCoding(coding);
		observationInterpretationTranslator.toOpenmrsType(obs, interpretation);
		assertThat(obs.getInterpretation(), notNullValue());
		assertThat(obs.getInterpretation(), is(Obs.Interpretation.CRITICALLY_LOW));
	}
	
	@Test
	public void toOpenmrsType_shouldTranslateHighCodeCorrectly() {
		Coding coding = new Coding();
		coding.setCode("H");
		interpretation.addCoding(coding);
		observationInterpretationTranslator.toOpenmrsType(obs, interpretation);
		assertThat(obs.getInterpretation(), notNullValue());
		assertThat(obs.getInterpretation(), is(Obs.Interpretation.HIGH));
	}
	
	@Test
	public void toOpenmrsType_shouldTranslateLowCodeCorrectly() {
		Coding coding = new Coding();
		coding.setCode("L");
		interpretation.addCoding(coding);
		observationInterpretationTranslator.toOpenmrsType(obs, interpretation);
		assertThat(obs.getInterpretation(), notNullValue());
		assertThat(obs.getInterpretation(), is(Obs.Interpretation.LOW));
	}
	
	@Test
	public void toOpenmrsType_shouldTranslateOffScaleLowCodeCorrectly() {
		Coding coding = new Coding();
		coding.setCode("<");
		interpretation.addCoding(coding);
		observationInterpretationTranslator.toOpenmrsType(obs, interpretation);
		assertThat(obs.getInterpretation(), notNullValue());
		assertThat(obs.getInterpretation(), is(Obs.Interpretation.OFF_SCALE_LOW));
	}
	
	@Test
	public void toOpenmrsType_shouldTranslateOffScaleHighCodeCorrectly() {
		Coding coding = new Coding();
		coding.setCode(">");
		interpretation.addCoding(coding);
		observationInterpretationTranslator.toOpenmrsType(obs, interpretation);
		assertThat(obs.getInterpretation(), notNullValue());
		assertThat(obs.getInterpretation(), is(Obs.Interpretation.OFF_SCALE_HIGH));
	}
	
	@Test
	public void toOpenmrsType_shouldTranslateSignificantChangeDownCodeCorrectly() {
		Coding coding = new Coding();
		coding.setCode("D");
		interpretation.addCoding(coding);
		observationInterpretationTranslator.toOpenmrsType(obs, interpretation);
		assertThat(obs.getInterpretation(), notNullValue());
		assertThat(obs.getInterpretation(), is(Obs.Interpretation.SIGNIFICANT_CHANGE_DOWN));
	}
	
	@Test
	public void toOpenmrsType_shouldTranslateSignificantChangeUpCodeCorrectly() {
		Coding coding = new Coding();
		coding.setCode("U");
		interpretation.addCoding(coding);
		observationInterpretationTranslator.toOpenmrsType(obs, interpretation);
		assertThat(obs.getInterpretation(), notNullValue());
		assertThat(obs.getInterpretation(), is(Obs.Interpretation.SIGNIFICANT_CHANGE_UP));
	}
	
	@Test
	public void toOpenmrsType_shouldTranslateResistantCodeCorrectly() {
		Coding coding = new Coding();
		coding.setCode("R");
		interpretation.addCoding(coding);
		observationInterpretationTranslator.toOpenmrsType(obs, interpretation);
		assertThat(obs.getInterpretation(), notNullValue());
		assertThat(obs.getInterpretation(), is(Obs.Interpretation.RESISTANT));
	}
	
	@Test
	public void toOpenmrsType_shouldTranslateSusceptibleCodeCorrectly() {
		Coding coding = new Coding();
		coding.setCode("S");
		interpretation.addCoding(coding);
		observationInterpretationTranslator.toOpenmrsType(obs, interpretation);
		assertThat(obs.getInterpretation(), notNullValue());
		assertThat(obs.getInterpretation(), is(Obs.Interpretation.SUSCEPTIBLE));
	}
	
	@Test
	public void toOpenmrsType_shouldTranslateIntermediateCodeCorrectly() {
		Coding coding = new Coding();
		coding.setCode("I");
		interpretation.addCoding(coding);
		observationInterpretationTranslator.toOpenmrsType(obs, interpretation);
		assertThat(obs.getInterpretation(), notNullValue());
		assertThat(obs.getInterpretation(), is(Obs.Interpretation.INTERMEDIATE));
	}
	
	@Test
	public void toOpenmrsType_shouldTranslatePositiveCodeCorrectly() {
		Coding coding = new Coding();
		coding.setCode("POS");
		interpretation.addCoding(coding);
		observationInterpretationTranslator.toOpenmrsType(obs, interpretation);
		assertThat(obs.getInterpretation(), notNullValue());
		assertThat(obs.getInterpretation(), is(Obs.Interpretation.POSITIVE));
	}
	
	@Test
	public void toOpenmrsType_shouldTranslateNegativeCodeCorrectly() {
		Coding coding = new Coding();
		coding.setCode("NEG");
		interpretation.addCoding(coding);
		observationInterpretationTranslator.toOpenmrsType(obs, interpretation);
		assertThat(obs.getInterpretation(), notNullValue());
		assertThat(obs.getInterpretation(), is(Obs.Interpretation.NEGATIVE));
	}
	
	@Test
	public void toOpenmrsType_shouldTranslateVerySusceptibleCodeCorrectly() {
		Coding coding = new Coding();
		coding.setCode("VS");
		interpretation.addCoding(coding);
		observationInterpretationTranslator.toOpenmrsType(obs, interpretation);
		assertThat(obs.getInterpretation(), notNullValue());
		assertThat(obs.getInterpretation(), is(Obs.Interpretation.VERY_SUSCEPTIBLE));
	}
}
