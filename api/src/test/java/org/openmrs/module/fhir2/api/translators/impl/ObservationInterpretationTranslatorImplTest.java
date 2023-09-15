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

import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.openmrs.Obs;
import org.openmrs.module.fhir2.FhirConstants;

@RunWith(MockitoJUnitRunner.class)
public class ObservationInterpretationTranslatorImplTest {
	
	private ObservationInterpretationTranslatorImpl observationInterpretationTranslator;
	
	private Obs obs;
	
	private CodeableConcept interpretation;
	
	@Before
	public void setUp() {
		observationInterpretationTranslator = new ObservationInterpretationTranslatorImpl();
		obs = new Obs();
		interpretation = new CodeableConcept();
	}
	
	@Test
	public void toFhirResource_shouldTranslateNormalInterpretationCorrectly() {
		obs.setInterpretation(Obs.Interpretation.NORMAL);
		CodeableConcept interpretation = observationInterpretationTranslator.toFhirResource(obs);
		MatcherAssert.assertThat(interpretation, Matchers.notNullValue());
		MatcherAssert.assertThat(interpretation.getCoding().get(0).getCode(), Matchers.is("N"));
		MatcherAssert.assertThat(interpretation.getCoding().get(0).getDisplay(), Matchers.is("Normal"));
		MatcherAssert.assertThat(interpretation.getText(), Matchers.is("Normal"));
	}
	
	@Test
	public void toFhirResource_shouldTranslateAbnormalInterpretationCorrectly() {
		obs.setInterpretation(Obs.Interpretation.ABNORMAL);
		CodeableConcept interpretation = observationInterpretationTranslator.toFhirResource(obs);
		MatcherAssert.assertThat(interpretation, Matchers.notNullValue());
		MatcherAssert.assertThat(interpretation.getCoding().get(0).getCode(), Matchers.is("A"));
		MatcherAssert.assertThat(interpretation.getCoding().get(0).getDisplay(), Matchers.is("Abnormal"));
		MatcherAssert.assertThat(interpretation.getText(), Matchers.is("Abnormal"));
	}
	
	@Test
	public void toFhirResource_shouldTranslateCriticallyAbnormalInterpretationCorrectly() {
		obs.setInterpretation(Obs.Interpretation.CRITICALLY_ABNORMAL);
		CodeableConcept interpretation = observationInterpretationTranslator.toFhirResource(obs);
		MatcherAssert.assertThat(interpretation, Matchers.notNullValue());
		MatcherAssert.assertThat(interpretation.getCoding().get(0).getCode(), Matchers.is("AA"));
		MatcherAssert.assertThat(interpretation.getCoding().get(0).getDisplay(), Matchers.is("Critically Abnormal"));
		MatcherAssert.assertThat(interpretation.getText(), Matchers.is("Critically Abnormal"));
	}
	
	@Test
	public void toFhirResource_shouldTranslateCriticallyHighInterpretationCorrectly() {
		obs.setInterpretation(Obs.Interpretation.CRITICALLY_HIGH);
		CodeableConcept interpretation = observationInterpretationTranslator.toFhirResource(obs);
		MatcherAssert.assertThat(interpretation, Matchers.notNullValue());
		MatcherAssert.assertThat(interpretation.getCoding().get(0).getCode(), Matchers.is("HH"));
		MatcherAssert.assertThat(interpretation.getCoding().get(0).getDisplay(), Matchers.is("Critically High"));
		MatcherAssert.assertThat(interpretation.getText(), Matchers.is("Critically High"));
	}
	
	@Test
	public void toFhirResource_shouldTranslateCriticallyLowInterpretationCorrectly() {
		obs.setInterpretation(Obs.Interpretation.CRITICALLY_LOW);
		CodeableConcept interpretation = observationInterpretationTranslator.toFhirResource(obs);
		MatcherAssert.assertThat(interpretation, Matchers.notNullValue());
		MatcherAssert.assertThat(interpretation.getCoding().get(0).getCode(), Matchers.is("LL"));
		MatcherAssert.assertThat(interpretation.getCoding().get(0).getDisplay(), Matchers.is("Critically Low"));
		MatcherAssert.assertThat(interpretation.getText(), Matchers.is("Critically Low"));
	}
	
	@Test
	public void toFhirResource_shouldTranslateHighInterpretationCorrectly() {
		obs.setInterpretation(Obs.Interpretation.HIGH);
		CodeableConcept interpretation = observationInterpretationTranslator.toFhirResource(obs);
		MatcherAssert.assertThat(interpretation, Matchers.notNullValue());
		MatcherAssert.assertThat(interpretation.getCoding().get(0).getCode(), Matchers.is("H"));
		MatcherAssert.assertThat(interpretation.getCoding().get(0).getDisplay(), Matchers.is("High"));
		MatcherAssert.assertThat(interpretation.getText(), Matchers.is("High"));
	}
	
	@Test
	public void toFhirResource_shouldTranslateLowInterpretationCorrectly() {
		obs.setInterpretation(Obs.Interpretation.LOW);
		CodeableConcept interpretation = observationInterpretationTranslator.toFhirResource(obs);
		MatcherAssert.assertThat(interpretation, Matchers.notNullValue());
		MatcherAssert.assertThat(interpretation.getCoding().get(0).getCode(), Matchers.is("L"));
		MatcherAssert.assertThat(interpretation.getCoding().get(0).getDisplay(), Matchers.is("Low"));
		MatcherAssert.assertThat(interpretation.getText(), Matchers.is("Low"));
	}
	
	@Test
	public void toFhirResource_shouldTranslateOffScaleLowInterpretationCorrectly() {
		obs.setInterpretation(Obs.Interpretation.OFF_SCALE_LOW);
		CodeableConcept interpretation = observationInterpretationTranslator.toFhirResource(obs);
		MatcherAssert.assertThat(interpretation, Matchers.notNullValue());
		MatcherAssert.assertThat(interpretation.getCoding().get(0).getCode(), Matchers.is("<"));
		MatcherAssert.assertThat(interpretation.getCoding().get(0).getDisplay(), Matchers.is("Off Scale Low"));
		MatcherAssert.assertThat(interpretation.getText(), Matchers.is("Off Scale Low"));
	}
	
	@Test
	public void toFhirResource_shouldTranslateOffScaleHighInterpretationCorrectly() {
		obs.setInterpretation(Obs.Interpretation.OFF_SCALE_HIGH);
		CodeableConcept interpretation = observationInterpretationTranslator.toFhirResource(obs);
		MatcherAssert.assertThat(interpretation, Matchers.notNullValue());
		MatcherAssert.assertThat(interpretation.getCoding().get(0).getCode(), Matchers.is(">"));
		MatcherAssert.assertThat(interpretation.getCoding().get(0).getDisplay(), Matchers.is("Off Scale High"));
		MatcherAssert.assertThat(interpretation.getText(), Matchers.is("Off Scale High"));
	}
	
	@Test
	public void toFhirResource_shouldTranslateSignificantChangeDownInterpretationCorrectly() {
		obs.setInterpretation(Obs.Interpretation.SIGNIFICANT_CHANGE_DOWN);
		CodeableConcept interpretation = observationInterpretationTranslator.toFhirResource(obs);
		MatcherAssert.assertThat(interpretation, Matchers.notNullValue());
		MatcherAssert.assertThat(interpretation.getCoding().get(0).getCode(), Matchers.is("D"));
		MatcherAssert.assertThat(interpretation.getCoding().get(0).getDisplay(), Matchers.is("Significant Change Down"));
		MatcherAssert.assertThat(interpretation.getText(), Matchers.is("Significant Change Down"));
	}
	
	@Test
	public void toFhirResource_shouldTranslateSignificantChangeUpInterpretationCorrectly() {
		obs.setInterpretation(Obs.Interpretation.SIGNIFICANT_CHANGE_UP);
		CodeableConcept interpretation = observationInterpretationTranslator.toFhirResource(obs);
		MatcherAssert.assertThat(interpretation, Matchers.notNullValue());
		MatcherAssert.assertThat(interpretation.getCoding().get(0).getCode(), Matchers.is("U"));
		MatcherAssert.assertThat(interpretation.getCoding().get(0).getDisplay(), Matchers.is("Significant Change Up"));
		MatcherAssert.assertThat(interpretation.getText(), Matchers.is("Significant Change Up"));
	}
	
	@Test
	public void toFhirResource_shouldTranslateResistantInterpretationCorrectly() {
		obs.setInterpretation(Obs.Interpretation.RESISTANT);
		CodeableConcept interpretation = observationInterpretationTranslator.toFhirResource(obs);
		MatcherAssert.assertThat(interpretation, Matchers.notNullValue());
		MatcherAssert.assertThat(interpretation.getCoding().get(0).getCode(), Matchers.is("R"));
		MatcherAssert.assertThat(interpretation.getCoding().get(0).getDisplay(), Matchers.is("Resistant"));
		MatcherAssert.assertThat(interpretation.getText(), Matchers.is("Resistant"));
	}
	
	@Test
	public void toFhirResource_shouldTranslateSusceptibleInterpretationCorrectly() {
		obs.setInterpretation(Obs.Interpretation.SUSCEPTIBLE);
		CodeableConcept interpretation = observationInterpretationTranslator.toFhirResource(obs);
		MatcherAssert.assertThat(interpretation, Matchers.notNullValue());
		MatcherAssert.assertThat(interpretation.getCoding().get(0).getCode(), Matchers.is("S"));
		MatcherAssert.assertThat(interpretation.getCoding().get(0).getDisplay(), Matchers.is("Susceptible"));
		MatcherAssert.assertThat(interpretation.getText(), Matchers.is("Susceptible"));
	}
	
	@Test
	public void toFhirResource_shouldTranslateIntermediateInterpretationCorrectly() {
		obs.setInterpretation(Obs.Interpretation.INTERMEDIATE);
		CodeableConcept interpretation = observationInterpretationTranslator.toFhirResource(obs);
		MatcherAssert.assertThat(interpretation, Matchers.notNullValue());
		MatcherAssert.assertThat(interpretation.getCoding().get(0).getCode(), Matchers.is("I"));
		MatcherAssert.assertThat(interpretation.getCoding().get(0).getDisplay(), Matchers.is("Intermediate"));
		MatcherAssert.assertThat(interpretation.getText(), Matchers.is("Intermediate"));
	}
	
	@Test
	public void toFhirResource_shouldTranslatePositiveInterpretationCorrectly() {
		obs.setInterpretation(Obs.Interpretation.POSITIVE);
		CodeableConcept interpretation = observationInterpretationTranslator.toFhirResource(obs);
		MatcherAssert.assertThat(interpretation, Matchers.notNullValue());
		MatcherAssert.assertThat(interpretation.getCoding().get(0).getCode(), Matchers.is("POS"));
		MatcherAssert.assertThat(interpretation.getCoding().get(0).getDisplay(), Matchers.is("Positive"));
		MatcherAssert.assertThat(interpretation.getText(), Matchers.is("Positive"));
	}
	
	@Test
	public void toFhirResource_shouldTranslateNegativeInterpretationCorrectly() {
		obs.setInterpretation(Obs.Interpretation.NEGATIVE);
		CodeableConcept interpretation = observationInterpretationTranslator.toFhirResource(obs);
		MatcherAssert.assertThat(interpretation, Matchers.notNullValue());
		MatcherAssert.assertThat(interpretation.getCoding().get(0).getCode(), Matchers.is("NEG"));
		MatcherAssert.assertThat(interpretation.getCoding().get(0).getDisplay(), Matchers.is("Negative"));
		MatcherAssert.assertThat(interpretation.getText(), Matchers.is("Negative"));
	}
	
	@Test
	public void toFhirResource_shouldTranslateVerySusceptibleInterpretationCorrectly() {
		obs.setInterpretation(Obs.Interpretation.VERY_SUSCEPTIBLE);
		CodeableConcept interpretation = observationInterpretationTranslator.toFhirResource(obs);
		MatcherAssert.assertThat(interpretation, Matchers.notNullValue());
		MatcherAssert.assertThat(interpretation.getCoding().get(0).getCode(), Matchers.is("VS"));
		MatcherAssert.assertThat(interpretation.getCoding().get(0).getDisplay(), Matchers.is("Very Susceptible"));
		MatcherAssert.assertThat(interpretation.getText(), Matchers.is("Very Susceptible"));
		MatcherAssert.assertThat(interpretation.getCoding().get(0).getSystem(),
		    Matchers.is(FhirConstants.OPENMRS_FHIR_EXT_VS_INTERPRETATION));
	}
	
	@Test
	public void toFhirResource_shouldReturnNoFhirInterpretationWhenObsInterpretationIsNull() {
		obs.setInterpretation(null);
		CodeableConcept interpretation = observationInterpretationTranslator.toFhirResource(obs);
		MatcherAssert.assertThat(interpretation, Matchers.nullValue());
	}
	
	@Test
	public void toOpenmrsType_shouldReturnNullIfInterpretationSizeIsZero() {
		observationInterpretationTranslator.toOpenmrsType(obs, interpretation);
		MatcherAssert.assertThat(obs.getInterpretation(), Matchers.nullValue());
	}
	
	@Test
	public void toOpenmrsType_shouldTranslateNormalCodeCorrectly() {
		Coding coding = new Coding();
		coding.setCode("N");
		interpretation.addCoding(coding);
		observationInterpretationTranslator.toOpenmrsType(obs, interpretation);
		MatcherAssert.assertThat(obs.getInterpretation(), Matchers.notNullValue());
		MatcherAssert.assertThat(obs.getInterpretation(), Matchers.is(Obs.Interpretation.NORMAL));
	}
	
	@Test
	public void toOpenmrsType_shouldTranslateAbnormalCodeCorrectly() {
		Coding coding = new Coding();
		coding.setCode("A");
		interpretation.addCoding(coding);
		observationInterpretationTranslator.toOpenmrsType(obs, interpretation);
		MatcherAssert.assertThat(obs.getInterpretation(), Matchers.notNullValue());
		MatcherAssert.assertThat(obs.getInterpretation(), Matchers.is(Obs.Interpretation.ABNORMAL));
	}
	
	@Test
	public void toOpenmrsType_shouldTranslateCriticallyAbnormalCodeCorrectly() {
		Coding coding = new Coding();
		coding.setCode("AA");
		interpretation.addCoding(coding);
		observationInterpretationTranslator.toOpenmrsType(obs, interpretation);
		MatcherAssert.assertThat(obs.getInterpretation(), Matchers.notNullValue());
		MatcherAssert.assertThat(obs.getInterpretation(), Matchers.is(Obs.Interpretation.CRITICALLY_ABNORMAL));
	}
	
	@Test
	public void toOpenmrsType_shouldTranslateCriticallyHighCodeCorrectly() {
		Coding coding = new Coding();
		coding.setCode("HH");
		interpretation.addCoding(coding);
		observationInterpretationTranslator.toOpenmrsType(obs, interpretation);
		MatcherAssert.assertThat(obs.getInterpretation(), Matchers.notNullValue());
		MatcherAssert.assertThat(obs.getInterpretation(), Matchers.is(Obs.Interpretation.CRITICALLY_HIGH));
	}
	
	@Test
	public void toOpenmrsType_shouldTranslateCriticallyLowCodeCorrectly() {
		Coding coding = new Coding();
		coding.setCode("LL");
		interpretation.addCoding(coding);
		observationInterpretationTranslator.toOpenmrsType(obs, interpretation);
		MatcherAssert.assertThat(obs.getInterpretation(), Matchers.notNullValue());
		MatcherAssert.assertThat(obs.getInterpretation(), Matchers.is(Obs.Interpretation.CRITICALLY_LOW));
	}
	
	@Test
	public void toOpenmrsType_shouldTranslateHighCodeCorrectly() {
		Coding coding = new Coding();
		coding.setCode("H");
		interpretation.addCoding(coding);
		observationInterpretationTranslator.toOpenmrsType(obs, interpretation);
		MatcherAssert.assertThat(obs.getInterpretation(), Matchers.notNullValue());
		MatcherAssert.assertThat(obs.getInterpretation(), Matchers.is(Obs.Interpretation.HIGH));
	}
	
	@Test
	public void toOpenmrsType_shouldTranslateLowCodeCorrectly() {
		Coding coding = new Coding();
		coding.setCode("L");
		interpretation.addCoding(coding);
		observationInterpretationTranslator.toOpenmrsType(obs, interpretation);
		MatcherAssert.assertThat(obs.getInterpretation(), Matchers.notNullValue());
		MatcherAssert.assertThat(obs.getInterpretation(), Matchers.is(Obs.Interpretation.LOW));
	}
	
	@Test
	public void toOpenmrsType_shouldTranslateOffScaleLowCodeCorrectly() {
		Coding coding = new Coding();
		coding.setCode("<");
		interpretation.addCoding(coding);
		observationInterpretationTranslator.toOpenmrsType(obs, interpretation);
		MatcherAssert.assertThat(obs.getInterpretation(), Matchers.notNullValue());
		MatcherAssert.assertThat(obs.getInterpretation(), Matchers.is(Obs.Interpretation.OFF_SCALE_LOW));
	}
	
	@Test
	public void toOpenmrsType_shouldTranslateOffScaleHighCodeCorrectly() {
		Coding coding = new Coding();
		coding.setCode(">");
		interpretation.addCoding(coding);
		observationInterpretationTranslator.toOpenmrsType(obs, interpretation);
		MatcherAssert.assertThat(obs.getInterpretation(), Matchers.notNullValue());
		MatcherAssert.assertThat(obs.getInterpretation(), Matchers.is(Obs.Interpretation.OFF_SCALE_HIGH));
	}
	
	@Test
	public void toOpenmrsType_shouldTranslateSignificantChangeDownCodeCorrectly() {
		Coding coding = new Coding();
		coding.setCode("D");
		interpretation.addCoding(coding);
		observationInterpretationTranslator.toOpenmrsType(obs, interpretation);
		MatcherAssert.assertThat(obs.getInterpretation(), Matchers.notNullValue());
		MatcherAssert.assertThat(obs.getInterpretation(), Matchers.is(Obs.Interpretation.SIGNIFICANT_CHANGE_DOWN));
	}
	
	@Test
	public void toOpenmrsType_shouldTranslateSignificantChangeUpCodeCorrectly() {
		Coding coding = new Coding();
		coding.setCode("U");
		interpretation.addCoding(coding);
		observationInterpretationTranslator.toOpenmrsType(obs, interpretation);
		MatcherAssert.assertThat(obs.getInterpretation(), Matchers.notNullValue());
		MatcherAssert.assertThat(obs.getInterpretation(), Matchers.is(Obs.Interpretation.SIGNIFICANT_CHANGE_UP));
	}
	
	@Test
	public void toOpenmrsType_shouldTranslateResistantCodeCorrectly() {
		Coding coding = new Coding();
		coding.setCode("R");
		interpretation.addCoding(coding);
		observationInterpretationTranslator.toOpenmrsType(obs, interpretation);
		MatcherAssert.assertThat(obs.getInterpretation(), Matchers.notNullValue());
		MatcherAssert.assertThat(obs.getInterpretation(), Matchers.is(Obs.Interpretation.RESISTANT));
	}
	
	@Test
	public void toOpenmrsType_shouldTranslateSusceptibleCodeCorrectly() {
		Coding coding = new Coding();
		coding.setCode("S");
		interpretation.addCoding(coding);
		observationInterpretationTranslator.toOpenmrsType(obs, interpretation);
		MatcherAssert.assertThat(obs.getInterpretation(), Matchers.notNullValue());
		MatcherAssert.assertThat(obs.getInterpretation(), Matchers.is(Obs.Interpretation.SUSCEPTIBLE));
	}
	
	@Test
	public void toOpenmrsType_shouldTranslateIntermediateCodeCorrectly() {
		Coding coding = new Coding();
		coding.setCode("I");
		interpretation.addCoding(coding);
		observationInterpretationTranslator.toOpenmrsType(obs, interpretation);
		MatcherAssert.assertThat(obs.getInterpretation(), Matchers.notNullValue());
		MatcherAssert.assertThat(obs.getInterpretation(), Matchers.is(Obs.Interpretation.INTERMEDIATE));
	}
	
	@Test
	public void toOpenmrsType_shouldTranslatePositiveCodeCorrectly() {
		Coding coding = new Coding();
		coding.setCode("POS");
		interpretation.addCoding(coding);
		observationInterpretationTranslator.toOpenmrsType(obs, interpretation);
		MatcherAssert.assertThat(obs.getInterpretation(), Matchers.notNullValue());
		MatcherAssert.assertThat(obs.getInterpretation(), Matchers.is(Obs.Interpretation.POSITIVE));
	}
	
	@Test
	public void toOpenmrsType_shouldTranslateNegativeCodeCorrectly() {
		Coding coding = new Coding();
		coding.setCode("NEG");
		interpretation.addCoding(coding);
		observationInterpretationTranslator.toOpenmrsType(obs, interpretation);
		MatcherAssert.assertThat(obs.getInterpretation(), Matchers.notNullValue());
		MatcherAssert.assertThat(obs.getInterpretation(), Matchers.is(Obs.Interpretation.NEGATIVE));
	}
	
	@Test
	public void toOpenmrsType_shouldTranslateVerySusceptibleCodeCorrectly() {
		Coding coding = new Coding();
		coding.setCode("VS");
		interpretation.addCoding(coding);
		observationInterpretationTranslator.toOpenmrsType(obs, interpretation);
		MatcherAssert.assertThat(obs.getInterpretation(), Matchers.notNullValue());
		MatcherAssert.assertThat(obs.getInterpretation(), Matchers.is(Obs.Interpretation.VERY_SUSCEPTIBLE));
	}
}
