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

import javax.annotation.Nonnull;

import java.util.ArrayList;
import java.util.List;

import lombok.AccessLevel;
import lombok.Setter;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Observation;
import org.hl7.fhir.r4.model.Quantity;
import org.openmrs.ConceptNumeric;
import org.openmrs.Obs;
import org.openmrs.ObsReferenceRange;
import org.openmrs.annotation.OpenmrsProfile;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.api.translators.ObservationReferenceRangeTranslator;
import org.springframework.stereotype.Component;

@Component
@OpenmrsProfile(openmrsPlatformVersion = "2.7.* - 2.*")
@Setter(AccessLevel.PACKAGE)
public class ObservationReferenceRangeTranslatorImpl2_7 implements ObservationReferenceRangeTranslator {
	
	@Override
	public List<Observation.ObservationReferenceRangeComponent> toFhirResource(@Nonnull Obs obs) {
		
		ConceptNumeric conceptNumeric = null;
		if (obs.getConcept() instanceof ConceptNumeric) {
			conceptNumeric = (ConceptNumeric) obs.getConcept();
		}
		
		if (conceptNumeric != null) {
			boolean allowDecimal = conceptNumeric.getAllowDecimal() != null ? conceptNumeric.getAllowDecimal() : true;
			
			Double hiNormal = conceptNumeric.getHiNormal();
			Double lowNormal = conceptNumeric.getLowNormal();
			Double hiCritical = conceptNumeric.getHiCritical();
			Double lowCritical = conceptNumeric.getLowCritical();
			Double hiAbsolute = conceptNumeric.getHiAbsolute();
			Double lowAbsolute = conceptNumeric.getLowAbsolute();
			
			ObsReferenceRange referenceRange = obs.getReferenceRange();
			if (referenceRange != null) {
				hiNormal = referenceRange.getHiNormal();
				lowNormal = referenceRange.getLowNormal();
				hiCritical = referenceRange.getHiCritical();
				lowCritical = referenceRange.getLowCritical();
				hiAbsolute = referenceRange.getHiAbsolute();
				lowAbsolute = referenceRange.getLowAbsolute();
			}
			
			List<Observation.ObservationReferenceRangeComponent> observationReferenceRangeComponentList = new ArrayList<>();
			if (hiNormal != null || lowNormal != null) {
				observationReferenceRangeComponentList.add(createObservationReferenceRange(hiNormal, lowNormal,
				    FhirConstants.OBSERVATION_REFERENCE_NORMAL, allowDecimal));
			}
			
			if (hiCritical != null || lowCritical != null) {
				observationReferenceRangeComponentList.add(createObservationReferenceRange(hiCritical, lowCritical,
				    FhirConstants.OBSERVATION_REFERENCE_TREATMENT, allowDecimal));
			}
			
			if (hiAbsolute != null || lowAbsolute != null) {
				observationReferenceRangeComponentList.add(createObservationReferenceRange(hiAbsolute, lowAbsolute,
				    FhirConstants.OBSERVATION_REFERENCE_ABSOLUTE, allowDecimal));
			}
			
			return observationReferenceRangeComponentList;
		} else {
			return null;
		}
	}
	
	private Observation.ObservationReferenceRangeComponent createObservationReferenceRange(Double hiValue, Double lowValue,
	        String code, boolean allowDecimal) {
		Observation.ObservationReferenceRangeComponent component = new Observation.ObservationReferenceRangeComponent();
		
		if (hiValue != null) {
			if (allowDecimal) {
				component.setHigh(new Quantity().setValue(hiValue));
			} else {
				component.setHigh(new Quantity().setValue(hiValue.longValue()));
			}
		}
		
		if (lowValue != null) {
			if (allowDecimal) {
				component.setLow(new Quantity().setValue(lowValue));
			} else {
				component.setLow(new Quantity().setValue(lowValue.longValue()));
			}
		}
		
		CodeableConcept referenceRangeType = new CodeableConcept();
		Coding coding = referenceRangeType.addCoding().setCode(code);
		
		if (FhirConstants.OBSERVATION_REFERENCE_ABSOLUTE.equals(code)) {
			coding.setSystem(FhirConstants.OPENMRS_FHIR_EXT_OBSERVATION_REFERENCE_RANGE);
		} else {
			coding.setSystem(FhirConstants.OBSERVATION_REFERENCE_RANGE_SYSTEM_URI);
		}
		
		component.setType(referenceRangeType);
		
		return component;
	}
	
}
