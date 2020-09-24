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

import org.apache.commons.lang.WordUtils;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.openmrs.Obs;
import org.openmrs.annotation.OpenmrsProfile;
import org.openmrs.module.fhir2.FhirConstants;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

@Primary
@Component
@OpenmrsProfile(openmrsPlatformVersion = "2.1.* - 2.*")
public class ObservationInterpretationTranslatorImpl_2_1 extends ObservationInterpretationTranslatorImpl {
	
	@Override
	public CodeableConcept toFhirResource(Obs obs) {
		if (obs.getInterpretation() == null) {
			return null;
		}
		
		CodeableConcept interpretation = new CodeableConcept();
		switch (obs.getInterpretation()) {
			case NORMAL:
				interpretation.addCoding(addInterpretationCoding("N", obs.getInterpretation()));
				setText(interpretation, obs.getInterpretation());
				break;
			case ABNORMAL:
				interpretation.addCoding(addInterpretationCoding("A", obs.getInterpretation()));
				setText(interpretation, obs.getInterpretation());
				break;
			case CRITICALLY_ABNORMAL:
				interpretation.addCoding(addInterpretationCoding("AA", obs.getInterpretation()));
				setText(interpretation, obs.getInterpretation());
				break;
			case CRITICALLY_HIGH:
				interpretation.addCoding(addInterpretationCoding("HH", obs.getInterpretation()));
				setText(interpretation, obs.getInterpretation());
				break;
			case CRITICALLY_LOW:
				interpretation.addCoding(addInterpretationCoding("LL", obs.getInterpretation()));
				setText(interpretation, obs.getInterpretation());
				break;
			case HIGH:
				interpretation.addCoding(addInterpretationCoding("H", obs.getInterpretation()));
				setText(interpretation, obs.getInterpretation());
				break;
			case LOW:
				interpretation.addCoding(addInterpretationCoding("L", obs.getInterpretation()));
				setText(interpretation, obs.getInterpretation());
				break;
			case OFF_SCALE_LOW:
				interpretation.addCoding(addInterpretationCoding("<", obs.getInterpretation()));
				setText(interpretation, obs.getInterpretation());
				break;
			case OFF_SCALE_HIGH:
				interpretation.addCoding(addInterpretationCoding(">", obs.getInterpretation()));
				setText(interpretation, obs.getInterpretation());
				break;
			case SIGNIFICANT_CHANGE_DOWN:
				interpretation.addCoding(addInterpretationCoding("D", obs.getInterpretation()));
				setText(interpretation, obs.getInterpretation());
				break;
			case SIGNIFICANT_CHANGE_UP:
				interpretation.addCoding(addInterpretationCoding("U", obs.getInterpretation()));
				setText(interpretation, obs.getInterpretation());
				break;
			case RESISTANT:
				interpretation.addCoding(addInterpretationCoding("R", obs.getInterpretation()));
				setText(interpretation, obs.getInterpretation());
				break;
			case SUSCEPTIBLE:
				interpretation.addCoding(addInterpretationCoding("S", obs.getInterpretation()));
				setText(interpretation, obs.getInterpretation());
				break;
			case INTERMEDIATE:
				interpretation.addCoding(addInterpretationCoding("I", obs.getInterpretation()));
				setText(interpretation, obs.getInterpretation());
				break;
			case POSITIVE:
				interpretation.addCoding(addInterpretationCoding("POS", obs.getInterpretation()));
				setText(interpretation, obs.getInterpretation());
				break;
			case NEGATIVE:
				interpretation.addCoding(addInterpretationCoding("NEG", obs.getInterpretation()));
				setText(interpretation, obs.getInterpretation());
				break;
			case VERY_SUSCEPTIBLE:
				interpretation.addCoding(new Coding().setSystem(FhirConstants.OPENMRS_FHIR_EXT_VS_INTERPRETATION)
				        .setCode("VS").setDisplay("Very Susceptible"));
				setText(interpretation, obs.getInterpretation());
				break;
			default:
				return interpretation;
			
		}
		return interpretation;
	}
	
	@Override
	public Obs toOpenmrsType(Obs openmrsObs, CodeableConcept interpretation) {
		if (interpretation.getCoding().size() == 0) {
			return null;
		}
		switch (interpretation.getCoding().get(0).getCode()) {
			case "N":
				openmrsObs.setInterpretation(Obs.Interpretation.NORMAL);
				break;
			case "A":
				openmrsObs.setInterpretation(Obs.Interpretation.ABNORMAL);
				break;
			case "AA":
				openmrsObs.setInterpretation(Obs.Interpretation.CRITICALLY_ABNORMAL);
				break;
			case "HH":
				openmrsObs.setInterpretation(Obs.Interpretation.CRITICALLY_HIGH);
				break;
			case "LL":
				openmrsObs.setInterpretation(Obs.Interpretation.CRITICALLY_LOW);
				break;
			case "H":
				openmrsObs.setInterpretation(Obs.Interpretation.HIGH);
				break;
			case "L":
				openmrsObs.setInterpretation(Obs.Interpretation.LOW);
				break;
			case "<":
				openmrsObs.setInterpretation(Obs.Interpretation.OFF_SCALE_LOW);
				break;
			case ">":
				openmrsObs.setInterpretation(Obs.Interpretation.OFF_SCALE_HIGH);
				break;
			case "D":
				openmrsObs.setInterpretation(Obs.Interpretation.SIGNIFICANT_CHANGE_DOWN);
				break;
			case "U":
				openmrsObs.setInterpretation(Obs.Interpretation.SIGNIFICANT_CHANGE_UP);
				break;
			case "R":
				openmrsObs.setInterpretation(Obs.Interpretation.RESISTANT);
				break;
			case "S":
				openmrsObs.setInterpretation(Obs.Interpretation.SUSCEPTIBLE);
				break;
			case "I":
				openmrsObs.setInterpretation(Obs.Interpretation.INTERMEDIATE);
				break;
			case "POS":
				openmrsObs.setInterpretation(Obs.Interpretation.POSITIVE);
				break;
			case "NEG":
				openmrsObs.setInterpretation(Obs.Interpretation.NEGATIVE);
				break;
			case "VS":
				openmrsObs.setInterpretation(Obs.Interpretation.VERY_SUSCEPTIBLE);
				break;
			default:
				openmrsObs.setInterpretation(null);
				break;
		}
		return openmrsObs;
	}
	
	private Coding addInterpretationCoding(String code, Obs.Interpretation display) {
		Coding coding = new Coding();
		coding.setSystem(FhirConstants.INTERPRETATION_SYSTEM_URI);
		coding.setCode(code);
		coding.setDisplay(WordUtils.capitalizeFully(display.toString().replaceAll("_", " ")));
		return coding;
	}
	
	private void setText(CodeableConcept interpretation, Obs.Interpretation text) {
		interpretation.setText(WordUtils.capitalizeFully(text.toString().replaceAll("_", " ")));
	}
}
